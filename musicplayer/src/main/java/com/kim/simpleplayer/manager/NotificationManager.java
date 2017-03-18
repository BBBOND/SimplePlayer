package com.kim.simpleplayer.manager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.kim.simpleplayer.R;
import com.kim.simpleplayer.helper.LogHelper;
import com.kim.simpleplayer.helper.ResourceHelper;
import com.kim.simpleplayer.service.PlayerService;

import java.util.List;

/**
 * Created by Weya on 2017/3/8.
 */

public class NotificationManager {

    private static final String TAG = NotificationManager.class.getSimpleName();

    public static final String ACTION_PAUSE = "com.kim.simpleplayer.pause";
    public static final String ACTION_PLAY = "com.kim.simpleplayer.play";
    public static final String ACTION_PREV = "com.kim.simpleplayer.prev";
    public static final String ACTION_NEXT = "com.kim.simpleplayer.next";
    public static final String ACTION_STOP_CASTING = "com.kim.simpleplayer.stop_cast";

    private static final int NOTIFICATION_ID = 1994;
    private static final int REQUEST_CODE = 100;

    private final PlayerService mPlayerService;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat mController;
    private MediaControllerCompat.TransportControls mTransportControls;

    private final PendingIntent mPauseIntent;
    private final PendingIntent mPlayIntent;
    private final PendingIntent mPreviousIntent;
    private final PendingIntent mNextIntent;
    private final PendingIntent mStopCastIntent;

    private final NotificationManagerCompat mNotificationManager;

    private final int mNotificationColor;

    private PlaybackStateCompat mPlaybackState;
    private MediaMetadataCompat mMetadata;
    private boolean mStarted = false;

    public NotificationManager(PlayerService playerService) throws RemoteException {
        this.mPlayerService = playerService;

        updateSessionToken();

        mNotificationColor = ResourceHelper.getThemeColor(playerService, R.attr.colorPrimary, Color.DKGRAY);
        mNotificationManager = NotificationManagerCompat.from(playerService);

        String pkg = playerService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(playerService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(playerService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(playerService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(playerService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopCastIntent = PendingIntent.getBroadcast(playerService, REQUEST_CODE,
                new Intent(ACTION_STOP_CASTING).setPackage(pkg),
                PendingIntent.FLAG_CANCEL_CURRENT);

        mNotificationManager.cancelAll();
    }

    private void updateSessionToken() throws RemoteException {
        MediaSessionCompat.Token freshToken = mPlayerService.getSessionToken();
        if (mSessionToken == null && freshToken != null ||
                mSessionToken != null && !mSessionToken.equals(freshToken)) {
            if (mController != null)
                mController.unregisterCallback(mcb);
            mSessionToken = freshToken;
            if (mSessionToken != null) {
                mController = new MediaControllerCompat(mPlayerService, mSessionToken);
                mTransportControls = mController.getTransportControls();
                if (mStarted) {
                    mController.registerCallback(mcb);
                }
            }
        }
    }

    private Notification createNotification() {
        LogHelper.d(TAG, "更新通知栏显示数据，metadata=" + mMetadata);
        if (mMetadata == null || mPlaybackState == null) {
            return null;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mPlayerService);
        int playPauseButtonPosition = 0;

        // 添加上一首按钮
        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            builder.addAction(R.drawable.ic_skip_previous_white_24dp,
                    mPlayerService.getString(R.string.label_previous), mPreviousIntent);
            playPauseButtonPosition = 1;
        }

        // 添加播放暂停按钮
        addPlayPauseAction(builder);

        // 添加下一首按钮
        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            builder.addAction(R.drawable.ic_skip_next_white_24dp,
                    mPlayerService.getString(R.string.label_next), mNextIntent);
        }

        MediaDescriptionCompat description = mMetadata.getDescription();

        String fetchArtUrl = null;
        Bitmap art = null;
        if (description.getIconUri() != null) {

        }
        // TODO: 2017/3/18
        return builder.build();
    }

    private void addPlayPauseAction(NotificationCompat.Builder builder) {
        LogHelper.d(TAG, "更新 暂停播放按钮");
        String label;
        int icon;
        PendingIntent intent;
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            label = mPlayerService.getString(R.string.label_pause);
            icon = R.drawable.uamp_ic_pause_white_24dp;
            intent = mPauseIntent;
        } else {
            label = mPlayerService.getString(R.string.label_play);
            icon = R.drawable.uamp_ic_play_arrow_white_24dp;
            intent = mPlayIntent;
        }
        builder.addAction(new android.support.v7.app.NotificationCompat.Action(icon, label, intent));
    }


    private final MediaControllerCompat.Callback mcb = new MediaControllerCompat.Callback() {
        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            LogHelper.d(TAG, "Session被销毁，重置新Session Token！");
            try {
                updateSessionToken();
            } catch (RemoteException e) {
                LogHelper.e(TAG, e, "无法连接控制器！");
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            mPlaybackState = state;
            LogHelper.d(TAG, "接收到新的播放状态", state);
            if (state.getState() == PlaybackStateCompat.STATE_STOPPED ||
                    state.getState() == PlaybackStateCompat.STATE_NONE) {
                // TODO: 2017/3/16 停止通知
            } else {
                // TODO: 2017/3/16 打开通知栏状态
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            mMetadata = metadata;
            LogHelper.d(TAG, "接收到新播放体", metadata);
            // TODO: 2017/3/16 打开通知栏状态
        }
    };

}