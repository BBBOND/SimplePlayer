package com.kim.simpleplayer;

import android.content.Context;

import com.kim.simpleplayer.model.MediaData;

import java.util.List;

/**
 * Created by kim on 2017/2/14.
 */

public class SimplePlayer {

    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION = "EXTRA_CURRENT_MEDIA_DESCRIPTION";

    private static Context mContext;
    private static List<MediaData> mMediaDataList;

    private static Class mPlayingActivity;

    public static void init(Context context) {
        SimplePlayer.mContext = context;
    }

    public static void setMediaDataList(List<MediaData> mediaDataList) {
        SimplePlayer.mMediaDataList = mediaDataList;
    }

    public static List<MediaData> getMediaDataList() {
        return mMediaDataList;
    }

    public static void release() {
        SimplePlayer.mContext = null;
    }

    public static Class getPlayingActivity() {
        return mPlayingActivity;
    }

    public static void setPlayingActivity(Class mPlayingActivity) {
        SimplePlayer.mPlayingActivity = mPlayingActivity;
    }
}
