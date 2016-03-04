package com.fastebro.androidrgbtool.utils;

/**
 * Created by daltomare on 13/03/14.
 * Project: rgb-tool
 */
public final class CommonUtils {

    private CommonUtils() {

    }

    public static final String PREFS_NAME = "RGBToolFile";

    /**
     * Until version 1.4.3 (11) color components were stored as float,
     * while starting from version 1.4.4 (12) as int, so they had to
     * be renamed.
     */
//    public static final String PREFS_R_COLOR = "com.fastebro.androidrgbtool.utils.prefs.PREFS_R_COLOR";
//    public static final String PREFS_G_COLOR = "com.fastebro.androidrgbtool.utils.prefs.PREFS_G_COLOR";
//    public static final String PREFS_B_COLOR = "com.fastebro.androidrgbtool.utils.prefs.PREFS_B_COLOR";
//    public static final String PREFS_OPACITY = "com.fastebro.androidrgbtool.utils.prefs.PREFS_OPACITY";

    public static final String PREFS_R_COLOR = "com.fastebro.androidrgbtool.utils.prefs.PREFS_R_COLOR_INT";
    public static final String PREFS_G_COLOR = "com.fastebro.androidrgbtool.utils.prefs.PREFS_G_COLOR_INT";
    public static final String PREFS_B_COLOR = "com.fastebro.androidrgbtool.utils.prefs.PREFS_B_COLOR_INT";
    public static final String PREFS_OPACITY = "com.fastebro.androidrgbtool.utils.prefs.PREFS_OPACITY_INT";
}
