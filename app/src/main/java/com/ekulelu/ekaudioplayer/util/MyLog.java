package com.ekulelu.ekaudioplayer.util;

import android.util.Log;

import com.ekulelu.ekaudioplayer.BuildConfig;

/**
 * Created by aahu on 2016/8/6 0006.
 */
public class MyLog {
//    public static boolean isDebug = true;
    public static final String mTAG = "MyLog";

    private static int mLevel = 7;

    public static final int VERBOSE = 2;

    public static final int DEBUG = 3;

    public static final int INFO = 4;

    public static final int WARN = 5;

    public static final int ERROR = 6;

    public static final int ASSERT = 7;


    private MyLog() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static void v(String string) {
        if (BuildConfig.DEBUG && mLevel >= VERBOSE) {
            Log.v(mTAG,string);
        }
    }

    public static void d(String string) {
        if (BuildConfig.DEBUG && mLevel >= DEBUG) {
            Log.d(mTAG,string);
        }
    }

    public static void i(String string) {
        if (BuildConfig.DEBUG && mLevel >= INFO) {
            Log.i(mTAG,string);
        }
    }

    public static void w(String string) {
        Log.w(mTAG,string);
    }

    public static void e(String string) {
        Log.e(mTAG,string);
    }

    public static void e(int integer) {
        Log.e(mTAG,"" + integer);
    }

    public static void e(long string) {
        Log.e(mTAG,"" + string);
    }

    public static void e(double string) {
        Log.e(mTAG,"" + string);
    }

    public static void e(boolean string) {
        Log.e(mTAG,"" + string);
    }

}
