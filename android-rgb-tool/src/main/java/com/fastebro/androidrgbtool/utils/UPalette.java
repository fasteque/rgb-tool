package com.fastebro.androidrgbtool.utils;

import android.content.Context;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.model.PaletteSwatch;

/**
 * Created by danielealtomare on 28/12/14.
 */
public class UPalette {
    public static String getSwatchDescription(Context context, PaletteSwatch.SwatchType type) {
        switch (type) {
            case VIBRANT:
                return context.getString(R.string.swatch_vibrant);
            case LIGHT_VIBRANT:
                return context.getString(R.string.swatch_light_vibrant);
            case DARK_VIBRANT:
                return context.getString(R.string.swatch_dark_vibrant);
            case MUTED:
                return context.getString(R.string.swatch_muted);
            case LIGHT_MUTED:
                return context.getString(R.string.swatch_light_muted);
            case DARK_MUTED:
                return context.getString(R.string.swatch_dark_muted);
            default:
                return "";
        }
    }
}
