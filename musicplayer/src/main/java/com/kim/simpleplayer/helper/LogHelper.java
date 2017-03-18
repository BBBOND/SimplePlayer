package com.kim.simpleplayer.helper;

import android.util.Log;

import com.kim.simpleplayer.BuildConfig;

/**
 * Created by Weya on 2017/3/16.
 */

public class LogHelper {

    public static void d(String tag, Object... msg) {
        if (BuildConfig.DEBUG) {
            log(tag, Log.DEBUG, null, msg);
        }
    }

    public static void e(String tag, Throwable t, Object... messages) {
        log(tag, Log.ERROR, t, messages);
    }

    private static void log(String tag, int level, Throwable t, Object... messages) {
        if (Log.isLoggable(tag, level)) {
            String message;
            if (t == null && messages != null && messages.length == 1) {
                // handle this common case without the extra cost of creating a stringbuffer:
                message = messages[0].toString();
            } else {
                StringBuilder sb = new StringBuilder();
                if (messages != null) for (Object m : messages) {
                    sb.append(m);
                }
                if (t != null) {
                    sb.append("\n").append(Log.getStackTraceString(t));
                }
                message = sb.toString();
            }
            Log.println(level, tag, message);
        }
    }
}
