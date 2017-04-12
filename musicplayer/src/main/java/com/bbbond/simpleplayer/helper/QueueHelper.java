package com.bbbond.simpleplayer.helper;

import android.support.v4.media.session.MediaSessionCompat;

import com.bbbond.simpleplayer.model.MediaData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Weya on 2017/3/12.
 */

public class QueueHelper {

    public static int getIndexOnQueue(List<MediaSessionCompat.QueueItem> playingQueue, String mediaId) {
        int index = 0;
        for (MediaSessionCompat.QueueItem item : playingQueue) {
            if (mediaId.equals(item.getDescription().getMediaId())) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public static int getIndexOnQueue(List<MediaSessionCompat.QueueItem> playingQueue, long queueId) {
        int index = 0;
        for (MediaSessionCompat.QueueItem item : playingQueue) {
            if (queueId == item.getQueueId()) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public static boolean isCurrentPlayable(List<MediaSessionCompat.QueueItem> playingQueue, int index) {
        return (playingQueue != null && index >= 0 && index < playingQueue.size());
    }

    public static List<MediaSessionCompat.QueueItem> formatMediaData2QueueItem(List<MediaData> mediaDataList) {
        List<MediaSessionCompat.QueueItem> queueItemList = Collections.synchronizedList(new ArrayList<MediaSessionCompat.QueueItem>());
        if (mediaDataList == null)
            return queueItemList;
        for (MediaData mediaData : mediaDataList) {
            queueItemList.add(new MediaSessionCompat.QueueItem(mediaData.getMediaMetadata().getDescription(), mediaDataList.indexOf(mediaData)));
        }
        return queueItemList;
    }
}
