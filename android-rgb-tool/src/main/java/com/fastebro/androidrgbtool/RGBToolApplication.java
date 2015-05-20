package com.fastebro.androidrgbtool;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by danielealtomare on 08/05/15.
 * Project: rgb-tool
 */
public class RGBToolApplication extends Application {

    private RefWatcher refWatcher;

    public static RefWatcher getRefWatcher(Context context) {
        RGBToolApplication application = (RGBToolApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    @Override public void onCreate() {
        super.onCreate();
        refWatcher = LeakCanary.install(this);
    }
}
