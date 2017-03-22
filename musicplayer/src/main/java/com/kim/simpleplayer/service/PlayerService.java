package com.kim.simpleplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.kim.simpleplayer.SimplePlayer;
import com.kim.simpleplayer.manager.MediaQueueManager;
import com.kim.simpleplayer.manager.NotificationManager;

import java.util.List;

public class PlayerService extends Service {

    private static final String TAG = PlayerService.class.getSimpleName();

    public static final String ACTION_CMD = "com.kim.simpleplayer.ACTION_CMD";
    public static final String CMD_NAME = "CMD_NAME";
    public static final String CMD_PAUSE = "CMD_PAUSE";

    public static final float VOLUME_DUCK = 0.2f;
    public static final float VOLUME_NORMAL = 1.0f;

    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    private static final int AUDIO_FOCUSED = 2;

    private MediaPlayer mMediaPlayer;
    private MediaSessionCompat mMediaSessionCompat;
    private NotificationManager mNotificationManager;
    private MediaQueueManager mMediaQueueManager;

    private boolean mPlayOnFocusGain;
    private int mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
    private int mState;

    @Override
    public void onCreate() {
        super.onCreate();
        if (SimplePlayer.getMediaDataList() == null) return;
        mMediaQueueManager = new MediaQueueManager(SimplePlayer.getMediaDataList(), new MediaQueueManager.MediaDataUpdateListener() {
            @Override
            public void onMediaDataChange(MediaMetadataCompat metadata) {
                mMediaSessionCompat.setMetadata(metadata);
            }

            @Override
            public void onMediaDataRetrieveError() {
                // TODO: 2017/3/20  更新状态
            }

            @Override
            public void onCurrentQueueIndexUpdated(int queueIndex) {
                // TODO: 2017/3/20 处理播放事件
            }

            @Override
            public void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue) {
                mMediaSessionCompat.setQueue(newQueue);
                mMediaSessionCompat.setQueueTitle(title);
            }
        });

        mMediaSessionCompat = new MediaSessionCompat(this, TAG);

        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        try {
            mNotificationManager = new NotificationManager(this);
        } catch (RemoteException e) {
            throw new IllegalStateException("无法创建通知管理器", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public MediaSessionCompat.Token getSessionToken() {
        if (mMediaSessionCompat != null)
            return mMediaSessionCompat.getSessionToken();
        else
            return null;
    }
}
