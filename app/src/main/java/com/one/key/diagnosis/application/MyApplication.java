package com.one.key.diagnosis.application;

import android.app.Application;
import android.content.Context;

/**
 * Created by wwwfa on 2017/10/16.
 */

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
    private static MyApplication mInstance;

    public static boolean LogShow = true;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }
    public static Context getInstance(){
        return mInstance;
    }
}
