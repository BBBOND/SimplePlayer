package com.kim.simpleplayer.manager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.RemoteException;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;

import com.kim.simpleplayer.R;
import com.kim.simpleplayer.SimplePlayer;
import com.kim.simpleplayer.helper.LogHelper;
import com.kim.simpleplayer.helper.ResourceHelper;
import com.kim.simpleplayer.service.PlayerService;
import com.kim.simpleplayer.utils.ImageCacheUtil;

/**
 * Created by Weya on 2017/3/8.
 */

public class NotificationManager extends BroadcastReceiver {

    private static final String TAG = NotificationManager.class.getSimpleName();

    public static final String ACTION_PAUSE = "com.kim.simpleplayer.pause";
    public static final String ACTION_PLAY = "com.kim.simpleplayer.play";
    public static final String ACTION_PREV = "com.kim.simpleplayer.prev";
    public static final String ACTION_NEXT = "com.kim.simpleplayer.next";
    public static final String ACTION_STOP = "com.kim.simpleplayer.stop_cast";

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
    private final PendingIntent mStopIntent;

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
        mStopIntent = PendingIntent.getBroadcast(playerService, REQUEST_CODE,
                new Intent(ACTION_STOP).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);

        mNotificationManager.cancelAll();
    }

    /**
     * 开启通知栏
     */
    public void startNotification() {
        if (!mStarted) {
            mMetadata = mController.getMetadata();
            mPlaybackState = mController.getPlaybackState();

            Notification notification = createNotification();
            if (notification != null) {
                mController.registerCallback(mcb);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);
                filter.addAction(ACTION_STOP);
                mPlayerService.registerReceiver(this, filter);

                mPlayerService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        }
    }

    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            mController.unregisterCallback(mcb);
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mPlayerService.unregisterReceiver(this);
            } catch (Exception ignore) {
            }
            mPlayerService.stopForeground(true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        LogHelper.d(TAG, "接收到广播发来的Action" + action);
        switch (action) {
            case ACTION_PAUSE:
                mTransportControls.pause();
                break;
            case ACTION_PLAY:
                mTransportControls.play();
                break;
            case ACTION_NEXT:
                mTransportControls.skipToNext();
                break;
            case ACTION_PREV:
                mTransportControls.skipToPrevious();
                break;
            case ACTION_STOP:
                Intent i = new Intent(context, PlayerService.class);
                i.setAction(PlayerService.ACTION_CMD);
                i.putExtra(PlayerService.CMD_NAME, PlayerService.CMD_STOP);
                mPlayerService.startService(i);
                break;
            default:
                LogHelper.d(TAG, "为止的广播意图（已忽略）: " + action);
        }
    }

    /**
     * 在SessionToken变化时更新状态
     * 在运行第一次或Session被销毁时调用
     *
     * @throws RemoteException
     */
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

    /**
     * 创建通知栏
     *
     * @return
     */
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

        builder.addAction(R.drawable.ic_close_black_24dp,
                mPlayerService.getString(R.string.label_stop), mStopIntent);

        MediaDescriptionCompat description = mMetadata.getDescription();

        String fetchArtUrl = null;
        Bitmap art = null;
        if (description.getIconUri() != null) {
            String artUrl = description.getIconUri().toString();
            art = ImageCacheUtil.getInstance().getBigImage(artUrl);
            if (art == null) {
                fetchArtUrl = artUrl;
                if (SimplePlayer.getInstance().getDefaultArtImgRes() == -1)
                    art = BitmapFactory.decodeResource(mPlayerService.getResources(),
                            R.drawable.ic_default_art);
                else
                    art = BitmapFactory.decodeResource(mPlayerService.getResources(),
                            SimplePlayer.getInstance().getDefaultArtImgRes());
            }
        } else {
            if (SimplePlayer.getInstance().getDefaultArtImgRes() == -1)
                art = BitmapFactory.decodeResource(mPlayerService.getResources(),
                        R.drawable.ic_default_art);
            else
                art = BitmapFactory.decodeResource(mPlayerService.getResources(),
                        SimplePlayer.getInstance().getDefaultArtImgRes());
        }

        builder
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(playPauseButtonPosition)
                        .setMediaSession(mSessionToken))
                .setColor(mNotificationColor)
                .setSmallIcon(R.drawable.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setUsesChronometer(true)
                .setContentIntent(createContentIntent(description))
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setLargeIcon(art);

        setNotificationPlaybackState(builder);
        if (fetchArtUrl != null) {
            fetchBitmapFromURLAsync(fetchArtUrl, builder);
        }
        return builder.build();
    }

    /**
     * 设置通知栏播放状态
     *
     * @param builder
     */
    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        LogHelper.d(TAG, "更新通知栏播放状态， mPlaybackState=" + mPlaybackState);
        if (mPlaybackState == null || !mStarted) {
            LogHelper.d(TAG, "更新通知栏播放状态. 取消通知栏!");
            mPlayerService.stopForeground(true);
            return;
        }
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING
                && mPlaybackState.getPosition() >= 0) {
            LogHelper.d(TAG, "更新通知栏播放状态. updating playback position to ",
                    (System.currentTimeMillis() - mPlaybackState.getPosition()) / 1000, " seconds");
            builder
                    .setWhen(System.currentTimeMillis() - mPlaybackState.getPosition())
                    .setShowWhen(true)
                    .setUsesChronometer(true);
        } else {
            LogHelper.d(TAG, "更新通知栏播放状态. 隐藏播放的进度");
            builder
                    .setWhen(0)
                    .setShowWhen(false)
                    .setUsesChronometer(false);
        }
        builder.setOngoing(mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    /**
     * 获取点击状态栏之后跳转的意图
     *
     * @param description
     * @return
     */
    private PendingIntent createContentIntent(MediaDescriptionCompat description) {
        if (SimplePlayer.getInstance().getPlayingActivity() == null) return null;
        Intent openUI = new Intent(mPlayerService, SimplePlayer.getInstance().getPlayingActivity());
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (description != null)
            openUI.putExtra(SimplePlayer.EXTRA_CURRENT_MEDIA_DESCRIPTION, description);
        return PendingIntent.getActivity(mPlayerService, REQUEST_CODE,
                openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * 通知栏添加播放或暂停按钮
     *
     * @param builder
     */
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

    /**
     * 异步获取图片
     *
     * @param fetchArtUrl
     * @param builder
     */
    private void fetchBitmapFromURLAsync(String fetchArtUrl, final NotificationCompat.Builder builder) {
        ImageCacheUtil.getInstance().fetch(fetchArtUrl, new ImageCacheUtil.FetchListener() {
            @Override
            public void onFetched(String imageUrl, Bitmap bigImage, Bitmap iconImage) {
                if (mMetadata != null && mMetadata.getDescription().getIconUri() != null &&
                        mMetadata.getDescription().getIconUri().toString().equals(imageUrl)) {
                    LogHelper.d(TAG, "获取bitmap, url: ", imageUrl);
                    builder.setLargeIcon(bigImage);
                    mNotificationManager.notify(NOTIFICATION_ID, builder.build());
                }
            }
        });
    }

    /**
     * 播放器控制回调
     */
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
                stopNotification();
            } else {
                Notification notification = createNotification();
                if (notification != null)
                    mNotificationManager.notify(NOTIFICATION_ID, notification);
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            mMetadata = metadata;
            LogHelper.d(TAG, "接收到新播放体", metadata);
            Notification notification = createNotification();
            if (notification != null)
                mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    };
}
