package com.kim.simpleplayer;

import android.content.Context;

import com.kim.simpleplayer.model.MediaData;

import java.util.List;

/**
 * Created by kim on 2017/2/14.
 */

public class SimplePlayer {

    private static Context mContext;
    private static List<MediaData> mMediaDataList;

    public static void init(Context context) {
        SimplePlayer.mContext = context;
    }

    public static void setMediaDataList(List<MediaData> mediaDataList) {
        SimplePlayer.mMediaDataList = mediaDataList;
    }

    public static void release() {
        SimplePlayer.mContext = null;
    }

}
