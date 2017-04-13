package com.bbbond.simpleplayer.manager;

import android.graphics.Bitmap;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.bbbond.simpleplayer.SimplePlayer;
import com.bbbond.simpleplayer.helper.QueueHelper;
import com.bbbond.simpleplayer.model.MediaData;
import com.bbbond.simpleplayer.utils.ImageCacheUtil;

import java.util.List;

/**
 * 媒体队列管理器
 * Created by Weya on 2017/3/12.
 */

public class MediaQueueManager {

    private MediaDataUpdateListener mMediaDataUpdateListener;

    private List<MediaSessionCompat.QueueItem> mPlayingQueue;
    private int mCurrentIndex;

    public MediaQueueManager(MediaDataUpdateListener mediaDataUpdateListener) {
        this(SimplePlayer.getInstance().getMediaDataList(), mediaDataUpdateListener);
    }

    public MediaQueueManager(List<MediaData> mediaDataList,
                             MediaDataUpdateListener mediaDataUpdateListener) {
        this.mMediaDataUpdateListener = mediaDataUpdateListener;

        mPlayingQueue = QueueHelper.formatMediaData2QueueItem(mediaDataList);
        mCurrentIndex = 0;
    }

    /**
     * 设置当前播放在列表中的位置
     *
     * @param index 列表中的位置
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
     * @param mediaId 音乐ID
     * @return 是否设置成功
     */
    public boolean setCurrentItem(String mediaId) {
        int index = QueueHelper.getIndexOnQueue(mPlayingQueue, mediaId);
        setCurrentIndex(index);
        return index >= 0;
    }

    /**
     * 通过QueueId确定当前播放位置
     *
     * @param queueId 队列中的ID
     * @return 是否设置成功
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
     * @return 是否能够播放
     */
    public boolean skipQueuePosition(int amount) {
        int index = mCurrentIndex + amount;
        if (index < 0)
            if (index + mPlayingQueue.size() >= 0)
                index += mPlayingQueue.size();
            else
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
     * @return 当前播放的音乐，可为空
     */
    public MediaSessionCompat.QueueItem getCurrentMusic() {
        if (!QueueHelper.isCurrentPlayable(mPlayingQueue, mCurrentIndex))
            return null;
        return mPlayingQueue.get(mCurrentIndex);
    }

    /**
     * 设置新播放队列，不带初始值
     *
     * @param title 队列标题
     * @param newQueue 新队列
     */
    public void setCurrentQueue(String title, List<MediaData> newQueue) {
        setCurrentQueue(title, newQueue, null);
    }

    /**
     * 设置新播放队列，带初始值
     *
     * @param title 队列标题
     * @param newQueue 新队列
     * @param initialMediaId 第一首播放的音乐ID
     */
    public void setCurrentQueue(String title, List<MediaData> newQueue, String initialMediaId) {
        mPlayingQueue = QueueHelper.formatMediaData2QueueItem(newQueue);
        int index = 0;
        if (initialMediaId != null)
            index = QueueHelper.getIndexOnQueue(mPlayingQueue, initialMediaId);
        mCurrentIndex = Math.max(index, 0);
        mMediaDataUpdateListener.onQueueUpdated(title, QueueHelper.formatMediaData2QueueItem(newQueue));
    }

    /**
     * 获取当前队列大小
     *
     * @return 当前队列大小
     */
    public int getCurrentQueueSize() {
        if (mPlayingQueue == null)
            return 0;
        return mPlayingQueue.size();
    }

    /**
     * 更新状态Metadata数据
     */
    public void updateMetadata() {
        MediaSessionCompat.QueueItem currentMusic = getCurrentMusic();
        if (currentMusic == null) {
            mMediaDataUpdateListener.onMediaDataRetrieveError();
            return;
        }
        final String musicId = currentMusic.getDescription().getMediaId();
        final MediaMetadataCompat metadata = getMusic(musicId);
        if (metadata == null) {
            throw new IllegalArgumentException("错误的musicId: " + musicId);
        }
        mMediaDataUpdateListener.onMediaDataChange(metadata);

        if (metadata.getDescription().getIconBitmap() == null &&
                metadata.getDescription().getIconUri() != null) {
            final String albumUri = metadata.getDescription().getIconUri().toString();
            ImageCacheUtil.getInstance().fetch(albumUri, new ImageCacheUtil.FetchListener() {
                @Override
                public void onFetched(String imageUrl, Bitmap bigImage, Bitmap iconImage) {
                    MediaMetadataCompat newMetadata = new MediaMetadataCompat.Builder(metadata)
                            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bigImage)
                            .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, iconImage)
                            .build();

                    MediaSessionCompat.QueueItem current = getCurrentMusic();
                    if (current == null)
                        return;
                    String currentPlayingId = current.getDescription().getMediaId();
                    if (musicId.equals(currentPlayingId)) {
                        mMediaDataUpdateListener.onMediaDataChange(newMetadata);
                    }
                }
            });
        }
    }

    /**
     * 通过id获取媒体资源
     * @param musicId id
     * @return 媒体资源
     */
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
