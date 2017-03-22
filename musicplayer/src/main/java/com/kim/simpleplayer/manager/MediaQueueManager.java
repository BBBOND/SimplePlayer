package com.kim.simpleplayer.manager;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.kim.simpleplayer.SimplePlayer;
import com.kim.simpleplayer.helper.QueueHelper;
import com.kim.simpleplayer.model.MediaData;
import com.kim.simpleplayer.utils.GlideUtil;

import java.util.List;

/**
 * Created by Weya on 2017/3/12.
 */

public class MediaQueueManager {

    private MediaDataUpdateListener mMediaDataUpdateListener;

    private List<MediaSessionCompat.QueueItem> mPlayingQueue;
    private int mCurrentIndex;

    public MediaQueueManager(List<MediaData> mediaDataList,
                             MediaDataUpdateListener mMediaDataUpdateListener) {
        this.mMediaDataUpdateListener = mMediaDataUpdateListener;

        mPlayingQueue = QueueHelper.formatMediaData2QueueItem(mediaDataList);
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
     * 通过QueueId确定当前播放位置
     * @param queueId
     * @return
     */
    public boolean setCurrentItem(long queueId) {
        int index = QueueHelper.getIndexOnQueue(mPlayingQueue, queueId);
        setCurrentIndex(index);
        return index >= 0;
    }

    /**
     * 设置随机播放
     */
    public void setRandomItem() {
        setCurrentIndex((int) (Math.random() * mPlayingQueue.size()));
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
     *
     * @return
     */
    public int getCurrentQueueSize() {
        if (mPlayingQueue == null)
            return 0;
        return mPlayingQueue.size();
    }

    public void updateMetadata(Context context) {
        MediaSessionCompat.QueueItem currentMusic = getCurrentMusic();
        if (currentMusic == null) {
            mMediaDataUpdateListener.onMediaDataRetrieveError();
            return;
        }
        final String musicId = currentMusic.getDescription().getMediaId();
        MediaMetadataCompat metadata = getMusic(musicId);
        if (metadata == null) {
            throw new IllegalArgumentException("错误的musicId: " + musicId);
        }
        mMediaDataUpdateListener.onMediaDataChange(metadata);

        if (metadata.getDescription().getIconBitmap() == null &&
                metadata.getDescription().getIconUri() != null) {
            String albumUri = metadata.getDescription().getIconUri().toString();
            metadata = new MediaMetadataCompat.Builder(metadata)
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, GlideUtil.getBigImage(context, albumUri))
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, GlideUtil.getIconImage(context, albumUri))
                    .build();
            MediaSessionCompat.QueueItem current = getCurrentMusic();
            if (current == null)
                return;
            String currentPlayingId = current.getDescription().getMediaId();
            if (musicId.equals(currentPlayingId)) {
                mMediaDataUpdateListener.onMediaDataChange(metadata);
            }
        }
    }

    public MediaMetadataCompat getMusic(String musicId) {
        for (MediaData data : SimplePlayer.getInstance().getMediaDataList()) {
            if (data.getMediaId().equals(musicId))
                return data.getMediaMetadata();
        }
        return null;
    }


    public interface MediaDataUpdateListener {
        void onMediaDataChange(MediaMetadataCompat metadata);

        void onMediaDataRetrieveError();

        void onCurrentQueueIndexUpdated(int queueIndex);

        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue);
    }
}
