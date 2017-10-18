package com.fastebro.androidrgbtool.settings;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.RGBToolApplication;

/**
 * Created by Snow Volf on 31.08.2017, 18:22
 */

public class Preferences {
    public static void saveTabPosition(int position){
        RGBToolApplication.getCtx().getPreferences().edit().putInt("last_tab", position).apply();
    }

    public static int getTabPosition(){
        int baseInt = RGBToolApplication.getCtx().getPreferences().getInt("last_tab", 0);
        switch (baseInt){
            case 0:
                return R.id.bottom_main;
            case 1:
                return R.id.bottom_details;
            case 2:
                return R.id.bottom_list;
            default:
                return R.id.bottom_main;
        }
    }
}
