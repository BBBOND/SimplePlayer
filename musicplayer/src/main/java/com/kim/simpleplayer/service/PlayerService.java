package com.kim.simpleplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.List;

public class PlayerService extends Service {

    private MediaPlayer mMediaPlayer;
    private MediaSessionCompat mMediaSessionCompat;

    public PlayerService() {
        mMediaSessionCompat = new MediaSessionCompat(this, "SimplePlayer");
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
