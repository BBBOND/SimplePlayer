package com.bbbond.simpleplayer.helper;

import android.util.Log;

/**
 * Created by Weya on 2017/3/16.
 */

public class LogHelper {

    private static boolean isDebug = false;

    public static void init(boolean isDebug) {
        LogHelper.isDebug = isDebug;
    }

    public static void d(String tag, Object... msg) {
        if (isDebug)
            log(tag, Log.DEBUG, null, msg);
    }

    public static void e(String tag, Throwable t, Object... messages) {
        if (isDebug)
            log(tag, Log.ERROR, t, messages);
    }

    private static void log(String tag, int level, Throwable t, Object... messages) {
        String message;
        if (t == null && messages != null && messages.length == 1) {
            // handle this common case without the extra cost of creating a stringbuffer:
            message = messages[0].toString();
        } else {
            StringBuilder sb = new StringBuilder();
            if (messages != null) for (Object m : messages) {
                sb.append(String.valueOf(m));
            }
            if (t != null) {
                sb.append("\n").append(Log.getStackTraceString(t));
            }
            message = sb.toString();
        }
        Log.println(level, tag, ">>>>>>LogHelper  " + message);
    }
}
