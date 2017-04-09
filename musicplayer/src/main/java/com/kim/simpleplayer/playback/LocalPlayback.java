package com.kim.simpleplayer.playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.kim.simpleplayer.SimplePlayer;
import com.kim.simpleplayer.helper.LogHelper;
import com.kim.simpleplayer.manager.MediaQueueManager;
import com.kim.simpleplayer.service.PlayerService;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by bbbond on 2017/3/20.
 */

public class LocalPlayback implements Playback,
        AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnBufferingUpdateListener {

    private static final String TAG = LocalPlayback.class.getSimpleName();

    public static final float VOLUME_DUCK = 0.2f;
    public static final float VOLUME_NORMAL = 1.0f;

    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    private static final int AUDIO_FOCUSED = 2;

    private final Context mContext;
    private final WifiManager.WifiLock mWifiLock;
    private int mState;
    private boolean mPlayOnFocusGain;
    private Callback mCallback;
    private final MediaQueueManager mMediaQueueManager;
    private volatile boolean mAudioNoisyReceiverRegistered;
    private volatile int mCurrentPosition;
    private volatile String mCurrentMediaId;

    private int mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
    private final AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;

    private Timer timer;
    private final ProgressHandler mProgressHandler = new ProgressHandler(this);
    private final IntentFilter mAudioNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private final BroadcastReceiver mAudioNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                LogHelper.d(TAG, "耳机断开连接");
                if (isPlaying()) {
                    Intent i = new Intent(context, PlayerService.class);
                    i.setAction(PlayerService.ACTION_CMD);
                    i.putExtra(PlayerService.CMD_NAME, PlayerService.CMD_PAUSE);
                    mContext.startService(i);
                }
            }
        }
    };

    public LocalPlayback(Context context, MediaQueueManager mediaQueueManager) {
        this.mContext = context.getApplicationContext();
        this.mMediaQueueManager = mediaQueueManager;
        this.mAudioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        this.mWifiLock = ((WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "uAmp_lock");
        this.mState = PlaybackStateCompat.STATE_NONE;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop(boolean notifyListeners) {
        mState = PlaybackStateCompat.STATE_STOPPED;
        if (notifyListeners && mCallback != null)
            mCallback.onPlaybackStatusChanged(mState);
        mCurrentPosition = getCurrentStreamPosition();

        giveUpAudioFocus();
        unregisterAudioNoisyReceiver();
        relaxResources(true);
    }

    @Override
    public void setState(int state) {
        this.mState = state;
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean isPlaying() {
        return mPlayOnFocusGain || (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }

    @Override
    public int getCurrentStreamPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : mCurrentPosition;
    }

    @Override
    public int getDuration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0;
    }

    @Override
    public void updateLastKnownStreamPosition() {
        if (mMediaPlayer != null)
            mCurrentPosition = mMediaPlayer.getCurrentPosition();
    }

    @Override
    public void play(MediaSessionCompat.QueueItem item) {
        mPlayOnFocusGain = true;
        tryToGetAudioFocus();
        registerAudioNoisyReceiver();
        String mediaId = item.getDescription().getMediaId();
        boolean mediaHasChanged = !TextUtils.equals(mediaId, mCurrentMediaId);
        if (mediaHasChanged) {
            mCurrentPosition = 0;
            mCurrentMediaId = mediaId;
        }
        if (mState == PlaybackStateCompat.STATE_PAUSED && !mediaHasChanged && mMediaPlayer != null) {
            configMediaPlayerState();
        } else {
            mState = PlaybackStateCompat.STATE_STOPPED;
            relaxResources(false);
            MediaMetadataCompat track = mMediaQueueManager.getMusic(item.getDescription().getMediaId());

            String source = track.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
            if (source != null && source.startsWith("http")) {
                source = source.replaceAll(" ", "%20");
            }
            try {
                createMediaPlayerIfNeeded();

                mState = PlaybackStateCompat.STATE_BUFFERING;

                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(source);

                mMediaPlayer.prepareAsync();
                if (source != null && source.startsWith("http"))
                    mWifiLock.acquire();

                if (mCallback != null) {
                    mCallback.onPlaybackStatusChanged(mState);
                }
            } catch (IOException e) {
                LogHelper.e(TAG, e, "播放媒体异常！");
                if (mCallback != null)
                    mCallback.onError(e.getMessage());
            }
        }

    }

    @Override
    public void pause() {
        LogHelper.d(TAG, "暂停");
        if (mState == PlaybackStateCompat.STATE_PLAYING) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentPosition = mMediaPlayer.getCurrentPosition();
            }
            relaxResources(false);
        }
        mState = PlaybackStateCompat.STATE_PAUSED;
        if (mCallback != null)
            mCallback.onPlaybackStatusChanged(mState);
        unregisterAudioNoisyReceiver();
    }

    @Override
    public void seekTo(int position) {
        LogHelper.d(TAG, "滑动到: " + position);
        if (mMediaPlayer == null) {
            mCurrentPosition = position;
        } else {
            if (mMediaPlayer.isPlaying()) {
                mState = PlaybackStateCompat.STATE_BUFFERING;
            }
            registerAudioNoisyReceiver();
            mMediaPlayer.seekTo(position);
            if (mCallback != null)
                mCallback.onPlaybackStatusChanged(mState);
        }
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        this.mCurrentMediaId = mediaId;
    }

    @Override
    public String getCurrentMediaId() {
        return mCurrentMediaId;
    }

    @Override
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public void setCurrentStreamPosition(int pos) {
        this.mCurrentPosition = pos;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        LogHelper.d(TAG, "焦点改变: " + focusChange);
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            mAudioFocus = AUDIO_FOCUSED;
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            boolean canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
            mAudioFocus = canDuck ? AUDIO_NO_FOCUS_CAN_DUCK : AUDIO_NO_FOCUS_NO_DUCK;

            if (mState == PlaybackStateCompat.STATE_PLAYING && !canDuck)
                mPlayOnFocusGain = true;
        } else {
            LogHelper.d(TAG, "忽略不支持的焦点变化类型: " + focusChange);
        }
        configMediaPlayerState();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        LogHelper.d(TAG, "移动进度条完成: " + mediaPlayer.getCurrentPosition());
        mCurrentPosition = mediaPlayer.getCurrentPosition();
        if (mState == PlaybackStateCompat.STATE_BUFFERING) {
            registerAudioNoisyReceiver();
            mMediaPlayer.start();
            mState = PlaybackStateCompat.STATE_PLAYING;
            startListeningProgress();
        }
        if (mCallback != null)
            mCallback.onPlaybackStatusChanged(mState);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        LogHelper.d(TAG, "MediaPlayer播放完成！");
        if (mCallback != null)
            mCallback.onCompletion();
        mCurrentPosition = 0;
        if (SimplePlayer.getInstance().getOnProgressChangeListener() != null)
            SimplePlayer.getInstance().getOnProgressChangeListener().completion();
        stopListeningProgress();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        LogHelper.d(TAG, "MediaPlayer准备完成！");
        configMediaPlayerState();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        LogHelper.d(TAG, "播放发生异常: what: " + what + ", extra: " + extra);
        if (mCallback != null)
            mCallback.onError("MediaPlayer error " + what + " (" + extra + ")");
        stopListeningProgress();
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (SimplePlayer.getInstance().getOnProgressChangeListener() != null)
            SimplePlayer.getInstance().getOnProgressChangeListener().secondaryProgressChanged(percent);
    }

    private void tryToGetAudioFocus() {
        LogHelper.d(TAG, "尝试获取焦点");
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocus = AUDIO_FOCUSED;
        } else {
            mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    private void giveUpAudioFocus() {
        LogHelper.d(TAG, "放弃焦点");
        if (mAudioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            mContext.registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            mContext.unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }

    private void relaxResources(boolean releaseMediaPlayer) {
        LogHelper.d(TAG, "释放资源. 是否释放播放器资源=", releaseMediaPlayer);

        // 如果MediaPlayer不为空，停止并释放
        if (releaseMediaPlayer && mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        // 如果持有wifi锁，释放他
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
        stopListeningProgress();
    }

    private void configMediaPlayerState() {
        LogHelper.d(TAG, "配置播放器状态. mAudioFocus=", mAudioFocus);
        if (mAudioFocus == AUDIO_NO_FOCUS_NO_DUCK) {
            // 如果没有焦点也不允许duck，必须暂停
            if (mState == PlaybackStateCompat.STATE_PLAYING) {
                pause();
            }
        } else {  // 有焦点
            registerAudioNoisyReceiver();
            if (mAudioFocus == AUDIO_NO_FOCUS_CAN_DUCK) {
                mMediaPlayer.setVolume(VOLUME_DUCK, VOLUME_DUCK); // 降低音量
            } else {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setVolume(VOLUME_NORMAL, VOLUME_NORMAL); // 音量复原
                }
            }
            // 如果在失去焦点时是播放状态，需要继续播放
            if (mPlayOnFocusGain) {
                if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                    LogHelper.d(TAG, "开始播放: " + mCurrentPosition);
                    if (mCurrentPosition == mMediaPlayer.getCurrentPosition()) {
                        mMediaPlayer.start();
                        mState = PlaybackStateCompat.STATE_PLAYING;
                    } else {
                        mMediaPlayer.seekTo(mCurrentPosition);
                        mState = PlaybackStateCompat.STATE_BUFFERING;
                    }
                    startListeningProgress();
                }
                mPlayOnFocusGain = false;
            }
        }
        if (mCallback != null) {
            mCallback.onPlaybackStatusChanged(mState);
        }
    }

    private void createMediaPlayerIfNeeded() {
        LogHelper.d(TAG, "按需创建播放器. needed? ", (mMediaPlayer == null));
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();

            // 确保播放器在播放时持有休眠锁(wake-lock)。
            // 如果不这样，在播放时CPU可能会进入休眠状态，导致回调停止。
            mMediaPlayer.setWakeMode(mContext.getApplicationContext(),
                    PowerManager.PARTIAL_WAKE_LOCK);

            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
        } else {
            mMediaPlayer.reset();
        }
    }

    private void startListeningProgress() {
        if (timer == null) {
            LogHelper.d(TAG, "开始监听播放进度");
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mMediaPlayer == null)
                        return;
                    if (isPlaying()) {
                        mProgressHandler.sendEmptyMessage(0); // 发送消息
                    }
                }
            }, 0, 1000);
        }
    }

    private void stopListeningProgress() {
        LogHelper.d(TAG, "取消监听播放进度");
        if (timer != null)
            timer.cancel();
        timer = null;
        mProgressHandler.removeCallbacksAndMessages(null);
    }

    private static class ProgressHandler extends Handler {
        private final WeakReference<LocalPlayback> mWeakReference;

        private ProgressHandler(LocalPlayback playback) {
            mWeakReference = new WeakReference<LocalPlayback>(playback);
        }

        @Override
        public void handleMessage(Message msg) {
            LocalPlayback playback = mWeakReference.get();
            SimplePlayer.OnProgressChangeListener listener = SimplePlayer.getInstance().getOnProgressChangeListener();
            if (playback != null && listener != null) {
                int position = playback.getCurrentStreamPosition();
                int duration = playback.getDuration();
                if (duration > 0) {
                    // 计算进度（获取进度条最大刻度*当前音乐播放位置 / 当前音乐时长）
                    long pos = 100 * position / duration;
                    listener.progressChanged((int) pos);
                }
            }
        }
    }
}
