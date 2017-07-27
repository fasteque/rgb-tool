package com.fastebro.androidrgbtool.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.fastebro.androidrgbtool.RGBToolApplication;

/**
 * Created by Snow Volf on 26.07.2017, 15:55
 */

public class ClipboardUtils {
    // Копирование текста в буфер обмена / Copy text to clipboard
    public static void copyToClipboard(String s){
        ClipboardManager clipboard = (ClipboardManager) RGBToolApplication.getCtx().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("JavaGirl", s);
        clipboard.setPrimaryClip(clip);
    }
}
