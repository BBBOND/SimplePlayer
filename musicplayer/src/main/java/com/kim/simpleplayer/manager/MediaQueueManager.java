package com.kim.simpleplayer.manager;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.kim.simpleplayer.helper.QueueHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Weya on 2017/3/12.
 */

public class MediaQueueManager {

    private MediaDataUpdateListener mMediaDataUpdateListener;

    private List<MediaSessionCompat.QueueItem> mPlayingQueue;
    private int mCurrentIndex;

    public MediaQueueManager(MediaDataUpdateListener mMediaDataUpdateListener) {
        this.mMediaDataUpdateListener = mMediaDataUpdateListener;

        mPlayingQueue = Collections.synchronizedList(new ArrayList<MediaSessionCompat.QueueItem>());
        mCurrentIndex = 0;
    }

    /**
     * 设置当前播放位置
     *
     * @param index
     */
    public void setCurrentIndex(int index) {
        if (index >= 0 && index < mPlayingQueue.size()) {
            mCurrentIndex = index;
            mMediaDataUpdateListener.onCurrentQueueIndexUpdated(mCurrentIndex);
        }
    }

    /**
     * 通过MediaID确定当前播放的位置
     *
     * @param mediaId
     * @return
     */
    public boolean setCurrentItem(String mediaId) {
        int index = QueueHelper.getIndexOnQueue(mPlayingQueue, mediaId);
        setCurrentIndex(index);
        return index >= 0;
    }

    /**
     * 跳转相应间隔
     *
     * @param amount -1为上一首/1为下一首
     * @return
     */
    public boolean skipQueuePosition(int amount) {
        int index = mCurrentIndex + amount;
        if (index < 0)
            index = 0;
        else
            index %= mPlayingQueue.size();
        if (!QueueHelper.isCurrentPlayable(mPlayingQueue, index))
            return false;
        mCurrentIndex = index;
        return true;
    }

    /**
     * 获取当前音乐
     *
     * @return
     */
    public MediaSessionCompat.QueueItem getCurrentMusic() {
        if (!QueueHelper.isCurrentPlayable(mPlayingQueue, mCurrentIndex))
            return null;
        return mPlayingQueue.get(mCurrentIndex);
    }

    /**
     * 设置新播放队列，不带初始值
     *
     * @param title
     * @param newQueue
     */
    public void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue) {
        setCurrentQueue(title, newQueue, null);
    }

    /**
     * 设置新播放队列，带初始值
     *
     * @param title
     * @param newQueue
     * @param initialMediaId
     */
    public void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue, String initialMediaId) {
        mPlayingQueue = newQueue;
        int index = 0;
        if (initialMediaId != null)
            index = QueueHelper.getIndexOnQueue(mPlayingQueue, initialMediaId);
        mCurrentIndex = Math.max(index, 0);
        mMediaDataUpdateListener.onQueueUpdated(title, newQueue);
    }

    /**
     * 获取当前队列大小
     * @return
     */
    public int getCurrentQueueSize() {
        if (mPlayingQueue == null)
            return 0;
        return mPlayingQueue.size();
    }



    public interface MediaDataUpdateListener {
        void onMediaDataChange(MediaMetadataCompat metadata);

        void onMediaDataRetrieveError();

        void onCurrentQueueIndexUpdated(int queueIndex);

        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue);
    }
}
