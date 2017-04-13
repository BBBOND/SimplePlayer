package com.bbbond.simpleplayer.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.LruCache;

import com.bbbond.simpleplayer.helper.BitmapHelper;
import com.bbbond.simpleplayer.helper.LogHelper;

/**
 * 图片内存缓存工具
 * Created by Weya on 2017/3/24.
 */

public class ImageCacheUtil {

    private static final String TAG = ImageCacheUtil.class.getSimpleName();

    private static final int MAX_CACHE_SIZE = 12 * 1024 * 1024;
    private static final int MAX_IMAGE_WIDTH = 800;
    private static final int MAX_IMAGE_HEIGHT = 480;

    private static final int MAX_ICON_IMAGE_WIDTH = 128;
    private static final int MAX_ICON_IMAGE_HEIGHT = 128;

    private static final int BIG_IMAGE_INDEX = 0;
    private static final int ICON_IMAGE_INDEX = 1;

    private final LruCache<String, Bitmap[]> mCache;

    private static final ImageCacheUtil sInstance = new ImageCacheUtil();

    public static ImageCacheUtil getInstance() {
        return sInstance;
    }

    private ImageCacheUtil() {
        int maxSize = Math.min(MAX_CACHE_SIZE,
                (int) (Math.min(Integer.MAX_VALUE, Runtime.getRuntime().maxMemory() / 4)));
        mCache = new LruCache<String, Bitmap[]>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap[] value) {
                return value[BIG_IMAGE_INDEX].getByteCount()
                        + value[ICON_IMAGE_INDEX].getByteCount();
            }
        };
    }

    /**
     * 获取大图片 800 * 480
     * @param imageUrl 图片资源URL
     * @return bitmap
     */
    public Bitmap getBigImage(String imageUrl) {
        Bitmap[] result = mCache.get(imageUrl);
        return result == null ? null : result[BIG_IMAGE_INDEX];
    }

    /**
     * 获取图标图片 128 * 128
     * @param imageUrl 图片资源URL
     * @return bitmap
     */
    public Bitmap getIconImage(String imageUrl) {
        Bitmap[] result = mCache.get(imageUrl);
        return result == null ? null : result[ICON_IMAGE_INDEX];
    }

    /**
     * 获取在线图片
     * @param imageUrl 图片资源URL
     * @param listener 图片获取结果监听
     */
    public void fetch(final String imageUrl, final FetchListener listener) {
        final Bitmap[] bitmap = mCache.get(imageUrl);
        if (bitmap != null) {
            LogHelper.d(TAG, "图片已存在!");
            if (listener != null)
                listener.onFetched(imageUrl, bitmap[BIG_IMAGE_INDEX], bitmap[ICON_IMAGE_INDEX]);
            return;
        }
        LogHelper.d(TAG, "开始异步获取图片!");

        new AsyncTask<Void, Void, Bitmap[]>() {

            @Override
            protected Bitmap[] doInBackground(Void... params) {
                Bitmap[] bitmaps;
                try {
                    Bitmap bigImage = BitmapHelper.fetchAndRescaleBitmap(imageUrl, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
                    Bitmap iconImage = BitmapHelper.scaleBitmap(bigImage, MAX_ICON_IMAGE_WIDTH, MAX_ICON_IMAGE_HEIGHT);
                    bitmaps = new Bitmap[]{bigImage, iconImage};
                    mCache.put(imageUrl, bitmaps);
                } catch (Exception e) {
                    return null;
                }
                LogHelper.d(TAG, "已将图片存入cache. cache size= ", mCache.size());
                return bitmaps;
            }

            @Override
            protected void onPostExecute(Bitmap[] bitmaps) {
                if (bitmaps == null) {
                    listener.onError(imageUrl, new IllegalArgumentException("Bitmap为空"));
                } else {
                    listener.onFetched(imageUrl, bitmaps[BIG_IMAGE_INDEX], bitmaps[ICON_IMAGE_INDEX]);
                }
            }
        }.execute();
    }

    public static abstract class FetchListener {
        public abstract void onFetched(String imageUrl, Bitmap bigImage, Bitmap iconImage);

        public void onError(String imageUrl, Exception e) {
            LogHelper.e(TAG, e, "Download Error! url: ", imageUrl);
        }
    }
}
