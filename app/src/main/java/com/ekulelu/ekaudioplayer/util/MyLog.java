package com.ekulelu.ekaudioplayer.util;

import android.support.v4.BuildConfig;
import android.util.Log;

/**
 * Created by aahu on 2016/8/6 0006.
 */
public class MyLog {
    public static boolean isDebug = true;
    public static final String TAG = "MyLog";

    static { //23api版本上，第一次读取的都是false，所以这里先读一遍，保证后面能读到正确的值。
        Log.isLoggable(TAG,Log.VERBOSE);
    }
    private MyLog() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static void v(String string) {
        if (BuildConfig.DEBUG && Log.isLoggable(TAG,Log.VERBOSE)) {
            Log.v(TAG,string);
        }
    }

    public static void d(String string) {
        if (BuildConfig.DEBUG && Log.isLoggable(TAG,Log.DEBUG)) {
            Log.d(TAG,string);
        }
    }

    public static void i(String string) {
        if (BuildConfig.DEBUG && Log.isLoggable(TAG,Log.INFO)) {
            Log.i(TAG,string);
        }
    }

    public static void w(String string) {
        Log.w(TAG,string);
    }

    public static void e(String string) {
        Log.e(TAG,string);

    }

}
