package com.fastebro.androidrgbtool.utils;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.RGBToolApplication;
import com.fastebro.androidrgbtool.palette.PaletteSwatch;

import java.util.ArrayList;

/**
 * Created by danielealtomare on 28/12/14.
 * Project: rgb-tool
 */
public final class PaletteUtils {

    private PaletteUtils() {

    }

    //TODO: Context can be replaced with RGBToolApplication.getCtx()
    public static String getSwatchDescription(PaletteSwatch.SwatchType type) {
        switch (type) {
            case VIBRANT:
                return RGBToolApplication.getCtx().getString(R.string.swatch_vibrant);
            case LIGHT_VIBRANT:
                return RGBToolApplication.getCtx().getString(R.string.swatch_light_vibrant);
            case DARK_VIBRANT:
                return RGBToolApplication.getCtx().getString(R.string.swatch_dark_vibrant);
            case MUTED:
                return RGBToolApplication.getCtx().getString(R.string.swatch_muted);
            case LIGHT_MUTED:
                return RGBToolApplication.getCtx().getString(R.string.swatch_light_muted);
            case DARK_MUTED:
                return RGBToolApplication.getCtx().getString(R.string.swatch_dark_muted);
            default:
                return "";
        }
    }

    public static String getPaletteMessage(String filename, ArrayList<PaletteSwatch> swatches) {
        StringBuilder message = new StringBuilder();

        message.append("RGB Tool - Image Palette");
        message.append(System.getProperty("line.separator"));

        if(!"".equals(filename)) {
            message.append("File: ");
            message.append(filename);
            message.append(System.getProperty("line.separator"));
        }

        for(PaletteSwatch swatch : swatches) {
            message.append(PaletteUtils.getSwatchDescription(swatch.getType()));
            message.append(": ");
            message.append("HEX - ");
            message.append("#");
            message.append(Integer.toHexString(swatch.getRgb()).toUpperCase());
            message.append(System.getProperty("line.separator"));
        }

        return message.toString();
    }
}
