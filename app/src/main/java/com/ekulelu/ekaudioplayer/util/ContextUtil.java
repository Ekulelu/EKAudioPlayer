package com.ekulelu.ekaudioplayer.util;

import android.app.Application;

/**A util for getting application context
 * Created by aahu on 2016/8/4 0004.
 */
public class ContextUtil extends Application {
    private static ContextUtil instance;

    public static ContextUtil getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
