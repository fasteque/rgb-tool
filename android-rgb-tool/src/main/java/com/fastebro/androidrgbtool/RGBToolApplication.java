package com.fastebro.androidrgbtool;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by danielealtomare on 08/05/15.
 * Project: rgb-tool
 */
public class RGBToolApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
