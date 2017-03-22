package com.kim.simpleplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.kim.simpleplayer.model.MediaData;
import com.kim.simpleplayer.service.PlayerService;

import java.util.List;

/**
 * Created by kim on 2017/2/14.
 */

public class SimplePlayer {

    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION = "EXTRA_CURRENT_MEDIA_DESCRIPTION";

    private volatile static SimplePlayer mInstance;

    private List<MediaData> mMediaDataList;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat mMediaController;
    private MediaControllerCompat.TransportControls mTransportControls;

    private PlayerService mPlayerService;
    private boolean bound = false;

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

    public void setMediaDataList(List<MediaData> mediaDataList) {
        mMediaDataList = mediaDataList;
    }

    public void setPlayingActivity(Class mPlayingActivity) {
        SimplePlayer.mPlayingActivity = mPlayingActivity;
    }

    public MediaControllerCompat.TransportControls getTransportControls(Context context) {
        try {
            connectServiceIfNeed(context);
            updateSessionToken(context);
            return mTransportControls;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateSessionToken(Context context) throws RemoteException {
        MediaSessionCompat.Token refreshToken = mPlayerService.getSessionToken();
        if (mSessionToken == null && refreshToken != null ||
                mSessionToken != null && !mSessionToken.equals(refreshToken)) {
            mSessionToken = refreshToken;
            if (mSessionToken != null) {
                mMediaController = new MediaControllerCompat(context, mSessionToken);
                mTransportControls = mMediaController.getTransportControls();
            }
        }
    }

    private void connectServiceIfNeed(Context context) {
        if (!bound && context != null) {
            Intent intent = new Intent(context.getApplicationContext(), PlayerService.class);
            context.getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    public List<MediaData> getMediaDataList() {
        return mMediaDataList;
    }

    public Class getPlayingActivity() {
        return mPlayingActivity;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) iBinder;
            mPlayerService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }
    };
}
