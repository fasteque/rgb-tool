package com.fastebro.androidrgbtool.utils;

import android.app.Activity;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.settings.AboutFragment;

/**
 * Created by Snow Volf on 25.07.2017, 18:28
 *
 * Switch from light to dark theme
 */

public abstract class ThemeWrapper {
    public enum Theme{
        LIGHT,
        DARK,
    }

    public static void applyTheme(Activity ctx){
        int theme;
        switch (Theme.values()[AboutFragment.getThemeIndex(ctx)]){
            case LIGHT:
                theme = R.style.Theme_Rgbtool;
                break;
            case DARK:
                theme = R.style.Theme_Rgbtool_Dark;
                break;
            default:
                theme = R.style.Theme_Rgbtool;
                break;
        }
        ctx.setTheme(theme);
    }
}
