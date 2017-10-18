package com.fastebro.androidrgbtool.utils;

import android.app.Activity;
import android.graphics.Typeface;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.RGBToolApplication;

/**
 * Created by Snow Volf on 25.07.2017, 18:28
 * <p>
 * Switch from light to dark theme
 */

public abstract class ThemeWrapper {
	public static void applyTheme(Activity ctx) {
		int theme;
		switch (Theme.values()[getThemeIndex()]) {
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

	public static int getDialogTheme() {
		int theme;
		switch (Theme.values()[getThemeIndex()]) {
			case LIGHT:
				theme = R.style.BottomSheetDialog;
				break;
			case DARK:
				theme = R.style.BottomSheetDialog_Dark;
				break;
			default:
				theme = R.style.BottomSheetDialog;
				break;
		}
		return theme;
	}

	private static int getThemeIndex() {
		return Integer.parseInt(RGBToolApplication.getCtx().getPreferences().getString("about.theme", String.valueOf
				(ThemeWrapper.Theme.LIGHT.ordinal())));
	}

	public static void mono(TextView textView) {
		Typeface typeface = Typeface.createFromAsset(RGBToolApplication.getCtx().getAssets(), "RobotoMono-Regular" +
				".ttf");
		textView.setTypeface(typeface);
	}

	private enum Theme {
		LIGHT,
		DARK,
	}
}