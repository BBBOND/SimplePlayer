package com.kim.simpleplayer.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.kim.simpleplayer.helper.LogHelper;
import com.kim.simpleplayer.playback.Playback;

/**
 * Created by hunyuan on 2017/3/22.
 */

public class PlaybackManager implements Playback.Callback {

    private static final String TAG = PlaybackManager.class.getSimpleName();

    private Context mContext;
    private MediaQueueManager mMediaQueueManager;
    private Playback mPlayback;
    private PlaybackServiceCallback mServiceCallback;
    private MediaSessionCallback mMediaSessionCallback;

    public PlaybackManager(Context context,
                           MediaQueueManager mediaQueueManager,
                           Playback playback,
                           PlaybackServiceCallback serviceCallback) {
        this.mContext = context;
        this.mMediaQueueManager = mediaQueueManager;
        this.mPlayback = playback;
        this.mServiceCallback = serviceCallback;
        this.mMediaSessionCallback = new MediaSessionCallback();
        this.mPlayback.setCallback(this);
    }

    @Override
    public void onCompletion() {
        if (mMediaQueueManager.skipQueuePosition(1)) {
            handlePlayRequest();
            mMediaQueueManager.updateMetadata(mContext);
        } else {
            handleStopRequest(null);
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        LogHelper.d(TAG, "设置currentMediaId: ", mediaId);
        mMediaQueueManager.setCurrentItem(mediaId);
    }

    public Playback getPlayback() {
        return mPlayback;
    }

    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return mMediaSessionCallback;
    }

    public void handlePlayRequest() {
        LogHelper.d(TAG, "处理播放请求。mState: " + mPlayback.getState());
        MediaSessionCompat.QueueItem currentMusic = mMediaQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            mServiceCallback.onPlaybackStart();
            mPlayback.play(currentMusic);
        }
    }

    public void handlePauseRequest() {
        LogHelper.d(TAG, "处理暂停请求。mState: " + mPlayback.getState());
        if (mPlayback.isPlaying()) {
            mPlayback.pause();
            mServiceCallback.onPlaybackStop();
        }
    }

    public void handleStopRequest(String withError) {
        LogHelper.d(TAG, "处理停止请求。mState: " + mPlayback.getState());
        mPlayback.stop(true);
        mServiceCallback.onPlaybackStop();
        updatePlaybackState(withError);
    }

    public void updatePlaybackState(String error) {
        LogHelper.d(TAG, "更新状态。playback state: " + mPlayback.getState());
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());
        int state = mPlayback.getState();
        if (error != null) {
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }

        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        MediaSessionCompat.QueueItem currentMusic = mMediaQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
        }

        mServiceCallback.onPlaybackStateUpdated(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            mServiceCallback.onNotificationRequired();
        }
    }

    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            LogHelper.d(TAG, "播放");
            if (mMediaQueueManager.getCurrentMusic() == null) {
                mMediaQueueManager.setRandomItem();
            }
            handlePlayRequest();
        }

        @Override
        public void onSkipToQueueItem(long id) {
            LogHelper.d(TAG, "跳转到: ", id);
            mMediaQueueManager.setCurrentItem(id);
            mMediaQueueManager.updateMetadata(mContext);
        }

        @Override
        public void onSeekTo(long pos) {
            LogHelper.d(TAG, "滑动到: ", pos);
            mPlayback.seekTo((int) pos);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            LogHelper.d(TAG, "通过MediaId播放 mediaId:", mediaId, "  extras=", extras);
            mMediaQueueManager.setCurrentItem(mediaId);
            handlePlayRequest();
        }

        @Override
        public void onPause() {
            LogHelper.d(TAG, "暂停. current state=" + mPlayback.getState());
            handlePauseRequest();
        }

        @Override
        public void onStop() {
            LogHelper.d(TAG, "停止. current state=" + mPlayback.getState());
            handleStopRequest(null);
        }

        @Override
        public void onSkipToNext() {
            LogHelper.d(TAG, "跳转下一首");
            if (mMediaQueueManager.skipQueuePosition(1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("无法跳转");
            }
            mMediaQueueManager.updateMetadata(mContext);
        }

        @Override
        public void onSkipToPrevious() {
            if (mMediaQueueManager.skipQueuePosition(-1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("无法跳转");
            }
            mMediaQueueManager.updateMetadata(mContext);
        }
    }

    public interface PlaybackServiceCallback {
        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }
}
