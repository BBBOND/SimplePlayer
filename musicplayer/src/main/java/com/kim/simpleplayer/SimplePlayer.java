package com.kim.simpleplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.kim.simpleplayer.model.MediaData;
import com.kim.simpleplayer.service.PlayerService;

import java.util.List;

/**
 * 简易播放器SDK
 * Created by kim on 2017/2/14.
 */

public class SimplePlayer {

    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION = "EXTRA_CURRENT_MEDIA_DESCRIPTION";

    private volatile static SimplePlayer mInstance;

    private List<MediaData> mMediaDataList;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat mMediaController;
    private MediaControllerCompat.TransportControls mTransportControls;
    private MediaControllerCompat.Callback mcb;

    private PlayerService mPlayerService;

    private static Class mPlayingActivity;

    public static SimplePlayer getInstance() {
        if (mInstance == null) {
            synchronized (SimplePlayer.class) {
                if (mInstance == null) {
                    mInstance = new SimplePlayer();
                }
            }
        }
        return mInstance;
    }

    private SimplePlayer() {
    }

    public void registerMediaControllerCallback(MediaControllerCompat.Callback callback) {
        this.mcb = callback;
        if (mMediaController != null && mcb != null)
            mMediaController.registerCallback(mcb);
    }

    public void unregisterMediaControllerCallback() {
        if (mMediaController != null && mcb != null)
            mMediaController.unregisterCallback(mcb);
        this.mcb = null;
    }
    public void setMediaDataList(List<MediaData> mediaDataList) {
        mMediaDataList = mediaDataList;
    }

    public void setPlayingActivity(Class mPlayingActivity) {
        SimplePlayer.mPlayingActivity = mPlayingActivity;
    }

    public void getTransportControls(final Context context, final GetTransportControlsCallback callback) {
        connectServiceIfNeed(context.getApplicationContext(), new ConnectCallback() {
            @Override
            public void onConnected() {
                try {
                    updateSessionToken(context);
                    if (callback != null)
                        callback.success(mTransportControls);
                } catch (RemoteException e) {
                    if (callback != null)
                        callback.error(e.getMessage());
                }
            }

            @Override
            public void onDisconnected() {
                if (callback != null)
                    callback.error("无法连接");
            }
        });
    }

    public int getState() {
        if (mPlayerService != null)
            return mPlayerService.getState();
        else
            return -2;
    }

    public String getMediaUri() {
        if (mMediaController != null && mMediaController.getMetadata() != null)
            return mMediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
        return null;
    }

    private void updateSessionToken(Context context) throws RemoteException {
        MediaSessionCompat.Token refreshToken = mPlayerService.getSessionToken();
        if (mSessionToken == null && refreshToken != null ||
                mSessionToken != null && !mSessionToken.equals(refreshToken)) {
            mSessionToken = refreshToken;
            if (mMediaController != null && mcb != null)
                mMediaController.unregisterCallback(mcb);
            if (mSessionToken != null) {
                mMediaController = new MediaControllerCompat(context.getApplicationContext(), mSessionToken);
                if (mcb != null)
                    mMediaController.registerCallback(mcb);
                mTransportControls = mMediaController.getTransportControls();
            }
        }
    }

    private void connectServiceIfNeed(Context context, final ConnectCallback callback) {
        if (mPlayerService == null && context != null) {
            Intent intent = new Intent(context.getApplicationContext(), PlayerService.class);
            context.getApplicationContext().bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) iBinder;
                    mPlayerService = binder.getService();
                    if (callback != null)
                        callback.onConnected();
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    mPlayerService = null;
                    if (callback != null)
                        callback.onDisconnected();
                }
            }, Context.BIND_AUTO_CREATE);
        } else if (mPlayerService != null) {
            if (callback != null)
                callback.onConnected();
        } else {
            if (callback != null)
                callback.onDisconnected();
        }
    }

    public List<MediaData> getMediaDataList() {
        return mMediaDataList;
    }

    public Class getPlayingActivity() {
        return mPlayingActivity;
    }

    public void release() {
        if (mMediaController != null && mcb != null)
            mMediaController.unregisterCallback(mcb);
    }

    private interface ConnectCallback {
        void onConnected();

        void onDisconnected();
    }

    public interface GetTransportControlsCallback {
        void success(MediaControllerCompat.TransportControls transportControls);

        void error(String errorMsg);
    }
}
