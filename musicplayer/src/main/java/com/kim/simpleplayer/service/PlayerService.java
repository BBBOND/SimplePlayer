package com.kim.simpleplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.kim.simpleplayer.SimplePlayer;
import com.kim.simpleplayer.helper.LogHelper;
import com.kim.simpleplayer.manager.MediaQueueManager;
import com.kim.simpleplayer.manager.NotificationManager;
import com.kim.simpleplayer.manager.PlaybackManager;
import com.kim.simpleplayer.playback.LocalPlayback;

import java.lang.ref.WeakReference;
import java.util.List;

public class PlayerService extends Service implements PlaybackManager.PlaybackServiceCallback {

    private static final String TAG = PlayerService.class.getSimpleName();

    public static final String ACTION_CMD = "com.kim.simpleplayer.ACTION_CMD";
    public static final String CMD_NAME = "CMD_NAME";
    public static final String CMD_PAUSE = "CMD_PAUSE";

    private static final int STOP_DELAY = 30000;

    private PlaybackManager mPlaybackManager;

    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);
    private final IBinder binder = new PlayerBinder();

    private MediaSessionCompat mMediaSessionCompat;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        if (SimplePlayer.getInstance().getMediaDataList() == null) return;
        MediaQueueManager mediaQueueManager = new MediaQueueManager(SimplePlayer.getInstance().getMediaDataList(),
                new MediaQueueManager.MediaDataUpdateListener() {
                    @Override
                    public void onMediaDataChange(MediaMetadataCompat metadata) {
                        mMediaSessionCompat.setMetadata(metadata);
                    }

                    @Override
                    public void onMediaDataRetrieveError() {
                        mPlaybackManager.updatePlaybackState("无法取到媒体数据");
                    }

                    @Override
                    public void onCurrentQueueIndexUpdated(int queueIndex) {
                        mPlaybackManager.handlePlayRequest();
                    }

                    @Override
                    public void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue) {
                        mMediaSessionCompat.setQueue(newQueue);
                        mMediaSessionCompat.setQueueTitle(title);
                    }
                });

        LocalPlayback playback = new LocalPlayback(this, mediaQueueManager);
        mPlaybackManager = new PlaybackManager(this, mediaQueueManager, playback, this);


        mMediaSessionCompat = new MediaSessionCompat(this, TAG);
        mMediaSessionCompat.setCallback(mPlaybackManager.getMediaSessionCallback());
        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mPlaybackManager.updatePlaybackState(null);

        try {
            mNotificationManager = new NotificationManager(this);
        } catch (RemoteException e) {
            throw new IllegalStateException("无法创建通知管理器", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            String command = intent.getStringExtra(CMD_NAME);
            if (ACTION_CMD.equals(action)) {
                if (CMD_PAUSE.equals(command)) {
                    mPlaybackManager.handlePauseRequest();
                }
            }
        }

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogHelper.d(TAG, "onDestroy");
        mPlaybackManager.handleStopRequest(null);
        mNotificationManager.stopNotification();
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mMediaSessionCompat.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public MediaSessionCompat.Token getSessionToken() {
        if (mMediaSessionCompat != null)
            return mMediaSessionCompat.getSessionToken();
        else
            return null;
    }

    public int getState() {
        if (mPlaybackManager != null && mPlaybackManager.getPlayback() != null)
            return mPlaybackManager.getPlayback().getState();
        else
            return -1;
    }

    @Override
    public void onPlaybackStart() {
        mMediaSessionCompat.setActive(true);
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        startService(new Intent(getApplicationContext(), PlayerService.class));
    }

    @Override
    public void onNotificationRequired() {
        mNotificationManager.startNotification();
    }

    @Override
    public void onPlaybackStop() {
        mMediaSessionCompat.setActive(false);
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        stopForeground(true);
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mMediaSessionCompat.setPlaybackState(newState);
    }

    private static class DelayedStopHandler extends Handler {
        private final WeakReference<PlayerService> mWeakReference;

        private DelayedStopHandler(PlayerService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            PlayerService service = mWeakReference.get();
            if (service != null && service.mPlaybackManager.getPlayback() != null) {
                if (service.mPlaybackManager.getPlayback().isPlaying()) {
                    LogHelper.d(TAG, "Ignoring delayed stop since the media player is in use.");
                    return;
                }
                LogHelper.d(TAG, "Stopping service with delay handler.");
                service.stopSelf();
            }
        }
    }

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }
}
