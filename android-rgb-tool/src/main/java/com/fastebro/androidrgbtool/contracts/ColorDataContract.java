package com.fastebro.androidrgbtool.contracts;

import android.provider.BaseColumns;

/**
 * Created by daltomare on 02/04/14.
 */
public final class ColorDataContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public ColorDataContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class ColorEntry implements BaseColumns {
        public static final String TABLE_NAME = "colors";
        public static final String COLUMN_COLOR_NAME = "color_name";
        public static final String COLUMN_COLOR_HEX = "color_hex";
        public static final String COLUMN_COLOR_RGB_R = "color_rgb_r";
        public static final String COLUMN_COLOR_RGB_G = "color_rgb_g";
        public static final String COLUMN_COLOR_RGB_B = "color_rgb_b";
        public static final String COLUMN_COLOR_RGB_A = "color_rgb_a";
        public static final String COLUMN_COLOR_HSB_H = "color_hsb_h";
        public static final String COLUMN_COLOR_HSB_S = "color_hsb_s";
        public static final String COLUMN_COLOR_HSB_B = "color_hsb_b";
        public static final String COLUMN_COLOR_FAVORITE = "color_favorite";
    }
}
