package com.yjn.jetpack.controlgui_android;

import android.app.Application;

public class MyApplication extends Application {

    private static MyApplication sInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static MyApplication getInstance() {
        return sInstance;
    }
}
