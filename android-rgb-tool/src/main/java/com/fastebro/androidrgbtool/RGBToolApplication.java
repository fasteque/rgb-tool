package com.fastebro.androidrgbtool;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import timber.log.Timber;

/**
 * Created by danielealtomare on 08/05/15.
 * Project: rgb-tool
 */
public class RGBToolApplication extends Application {

    private static final String LOG_TAG = "RGBTool";
    private RefWatcher refWatcher;

    public static RefWatcher getRefWatcher(@NonNull Context context) {
        RGBToolApplication application = (RGBToolApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    @Override public void onCreate() {
        super.onCreate();
        refWatcher = LeakCanary.install(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Timber.tag(LOG_TAG);
        }
    }
}
