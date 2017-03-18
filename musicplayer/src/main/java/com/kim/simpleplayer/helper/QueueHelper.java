package com.kim.simpleplayer.helper;

import android.support.v4.media.session.MediaSessionCompat;

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

    public static boolean isCurrentPlayable(List<MediaSessionCompat.QueueItem> playingQueue, int index) {
        return (playingQueue != null && index >= 0 && index < playingQueue.size());
    }
}
