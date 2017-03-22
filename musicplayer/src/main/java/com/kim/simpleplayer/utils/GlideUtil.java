package com.kim.simpleplayer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.bumptech.glide.Glide;
import com.kim.simpleplayer.R;
import com.kim.simpleplayer.helper.LogHelper;

/**
 * Created by Weya on 2017/3/18.
 */

public class GlideUtil {

    private static final String TAG = GlideUtil.class.getSimpleName();

    private static final int MAX_ART_WIDTH = 800;
    private static final int MAX_ART_HEIGHT = 480;

    private static final int MAX_ART_WIDTH_ICON = 128;
    private static final int MAX_ART_HEIGHT_ICON = 128;

    public static Bitmap getBigImage(Context context, String artUrl) {
        try {
            return Glide.with(context).load(artUrl).asBitmap().centerCrop().into(MAX_ART_WIDTH, MAX_ART_HEIGHT).get();
        } catch (Exception e) {
            LogHelper.e(TAG, e, "图片载入时出错: " + artUrl);
            return null;
        }
    }

    public static Bitmap getIconImage(Context context, String artUrl) {
        try {
            return Glide.with(context).load(artUrl).asBitmap().centerCrop().into(MAX_ART_WIDTH_ICON, MAX_ART_HEIGHT_ICON).get();
        } catch (Exception e) {
            LogHelper.e(TAG, e, "图片载入时出错: " + artUrl);
            return null;
        }
    }
}
