package com.fastebro.androidrgbtool.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Snow Volf on 28.07.2017, 16:11
 */

public class FragmentUtils {
    public static void iterate(AppCompatActivity activity, int resId, @NonNull Fragment fragment) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(resId, fragment)
                .commit();
    }
}
