package com.bbbond.simpleplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.bbbond.simpleplayer.model.MediaData;
import com.bbbond.simpleplayer.service.PlayerService;

import java.util.ArrayList;
import java.util.List;

/**
 * 简易播放器SDK，遵循谷歌MediaSession框架。
 * 已实现功能：
 *      1. 播放本地和远端音乐
 *      2. 通知栏控制
 *      3. 音乐进度监听
 *      4. 添加音乐进入队列
 *      5. 专辑图片自定义
 *      6. MediaSession状态监听
 *      7. 音乐播放状态、总长度、SessionToken、URL获取
 *      8. 是否继续播放下一首
 *      9. 来电、来短信状态控制
 * Created by BBBOND on 2017/2/14.
 */

public class SimplePlayer {

    // 当前播放音乐的描述
    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION = "EXTRA_CURRENT_MEDIA_DESCRIPTION";

    private volatile static SimplePlayer mInstance;

    private List<MediaData> mMediaDataList;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat mMediaController;
    private MediaControllerCompat.TransportControls mTransportControls;
    private MediaControllerCompat.Callback mcb;

    private OnProgressChangeListener mOnProgressChangeListener;

    private PlayerService mPlayerService;

    private int mDefaultArtImgRes = -1;
    private int mSmallNotificationIcon = -1;
    private boolean mPlayContinuously = false;
    private static Class mPlayingActivity;

    /**
     * 单例，不创建播放service
     * @return SimplePlayer单例
     */
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

    /**
     * 注册媒体控制器回调，用于监听MediaSession的状态变化
     *
     * onSessionDestroyed()
     *
     * onMetadataChanged(metadata)
     *
     * onPlaybackStateChanged(state)
     *      PlaybackStateCompat.STATE_PAUSED
     *      PlaybackStateCompat.STATE_PLAYING
     *      PlaybackStateCompat.STATE_STOPPED
     *      PlaybackStateCompat.STATE_BUFFERING
     *
     * @param callback 媒体控制器回调
     */
    public void registerMediaControllerCallback(MediaControllerCompat.Callback callback) {
        this.mcb = callback;
        if (mMediaController != null && mcb != null)
            mMediaController.registerCallback(mcb);
    }

    /**
     * 反注册媒体控制器回调
     */
    public void unregisterMediaControllerCallback() {
        if (mMediaController != null && mcb != null)
            mMediaController.unregisterCallback(mcb);
        this.mcb = null;
    }

    /**
     * 设置音乐媒体列表
     * @param title 音乐列表的标题，默认'music'
     * @param mediaDataList 音乐列表
     * @param initialMediaId 第一首播放的音乐ID
     */
    public void setMediaDataList(@NonNull String title, List<MediaData> mediaDataList, String initialMediaId) {
        mMediaDataList = mediaDataList;
        if (mPlayerService != null) {
            mPlayerService.setCurrentQueue(title, mediaDataList, initialMediaId);
        }
    }

    /**
     * 获取当前播放列表
     * @return 当前播放列表
     */
    public List<MediaData> getMediaDataList() {
        return mMediaDataList;
    }

    /**
     * 添加新的音乐到媒体列表
     * @param mediaData 新的音乐媒体
     * @return 是否添加成功
     */
    public boolean addMediaData(MediaData mediaData) {
        if (mMediaDataList == null) {
            mMediaDataList = new ArrayList<>();
        }
        String mediaId = mediaData.getMediaId();
        if (mediaId == null || mediaId.isEmpty()) {
            return false;
        }
        for (MediaData m : mMediaDataList) {
            if (mediaId.equals(m.getMediaId())) {
                return false;
            }
        }
        mMediaDataList.add(mediaData);
        if (mPlayerService != null) {
            mPlayerService.setCurrentQueue("music", mMediaDataList, null);
        }
        return true;
    }

    /**
     * 获取进度条监听器
     * @return 进度条监听器
     */
    public OnProgressChangeListener getOnProgressChangeListener() {
        return mOnProgressChangeListener;
    }

    /**
     * 设置进度条监听器
     * @param onProgressChangeListener 进度条监听器
     */
    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        this.mOnProgressChangeListener = onProgressChangeListener;
    }

    /**
     * 获取MediaSession的控制器，首次获取时将初始化播放Service，因此获取控制器并不是实时的，需要通过一个回调接收值，而且接收的值可能为空
     * @param context 上下文
     * @param callback 用于接收控制器的回调
     */
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

    /**
     * 获取当前播放的状态
     * @return -2 播放Service为空， -1 内部错误，
     *    其他状态为
     *      PlaybackStateCompat.STATE_PAUSED
     *      PlaybackStateCompat.STATE_PLAYING
     *      PlaybackStateCompat.STATE_STOPPED
     *      PlaybackStateCompat.STATE_BUFFERING
     */
    public int getState() {
        if (mPlayerService != null)
            return mPlayerService.getState();
        else
            return -2;
    }

    /**
     * 获取当前播放的音乐URL，可能为空，也可通过{@link SimplePlayer#getTransportControls(Context, GetTransportControlsCallback)}获取控制器后再获取
     * @return 当前播放的音乐URL
     */
    public String getMediaUri() {
        if (mMediaController != null && mMediaController.getMetadata() != null)
            return mMediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
        return null;
    }

    /**
     * 设置通知栏点击进入的Activity
     * @param playingActivity 通知栏点击进入的Activity
     */
    public void setPlayingActivity(Class playingActivity) {
        SimplePlayer.mPlayingActivity = playingActivity;
    }

    /**
     * 获取通知栏点击进入的Activity
     * @return 通知栏点击进入的Activity
     */
    public Class getPlayingActivity() {
        return mPlayingActivity;
    }

    /**
     * 获取默认音乐图片资源
     * @return 默认音乐图片资源
     */
    public int getDefaultArtImgRes() {
        return mDefaultArtImgRes;
    }

    /**
     * 设置默认音乐图片资源
     * @param defaultArtImg 默认音乐图片资源
     */
    public void setDefaultArtImgRes(@DrawableRes int defaultArtImg) {
        this.mDefaultArtImgRes = defaultArtImg;
    }

    /**
     * 获取通知栏SmallIcon资源
     * @return 通知栏SmallIcon资源
     */
    public int getSmallNotificationIcon() {
        return mSmallNotificationIcon;
    }

    /**
     * 设置通知栏SmallIcon资源
     * @param smallNotificationIcon 通知栏SmallIcon资源
     */
    public void setSmallNotificationIcon(@DrawableRes int smallNotificationIcon) {
        this.mSmallNotificationIcon = smallNotificationIcon;
    }

    /**
     * 是否继续播放下一首
     * @return true是/false否
     */
    public boolean isPlayContinuously() {
        return mPlayContinuously;
    }

    /**
     * 设置是否继续播放下一首
     * @param playContinuously true是/false否
     */
    public void setPlayContinuously(boolean playContinuously) {
        this.mPlayContinuously = playContinuously;
    }

    /**
     * 获取音乐总长度
     * @return 音乐总长度
     */
    public int getDuration() {
        if (mPlayerService != null)
            return mPlayerService.getDuration();
        else
            return 0;
    }

//    public void release() {
//    }

    /**
     * 更新SessionToken
     * @param context 上下文
     * @throws RemoteException MediaControllerCompat创建时可能会报的错
     */
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

    /**
     * 在播放Service为空时，绑定Service，由于是异步调取，因此需要回调获取状态
     * @param context 上下文
     * @param callback 绑定状态回调
     */
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

    private interface ConnectCallback {
        void onConnected();

        void onDisconnected();
    }

    public interface GetTransportControlsCallback {
        void success(MediaControllerCompat.TransportControls transportControls);

        void error(String errorMsg);
    }

    public interface OnProgressChangeListener {
        void progressChanged(int progress);

        void secondaryProgressChanged(int progress);

        void completion();
    }
}
