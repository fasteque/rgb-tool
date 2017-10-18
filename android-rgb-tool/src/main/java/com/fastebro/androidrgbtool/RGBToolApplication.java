package com.fastebro.androidrgbtool;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import timber.log.Timber;

/**
 * Created by danielealtomare on 08/05/15.
 * Project: rgb-tool
 */
public class RGBToolApplication extends Application {
	private static RGBToolApplication ctx = new RGBToolApplication();
	SharedPreferences preferences;

	public RGBToolApplication() {
		ctx = this;
	}

	public static RGBToolApplication getCtx() {
		return ctx;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// See javadoc for details
		hackVmPolicy();

		// Set default settings
		PreferenceManager.setDefaultValues(this, R.xml.about, false);

		if (BuildConfig.DEBUG) {
			Timber.plant(new Timber.DebugTree());
		}
	}

	public SharedPreferences getPreferences() {
		if (preferences == null)
			preferences = PreferenceManager.getDefaultSharedPreferences(this);
		return preferences;
	}

	/**
	 * Disable file uri exposed check on Android N
	 *
	 * @see android.os.FileUriExposedException
	 * starting with Android N, we need to replace
	 * all file:/// uri with the content:// uri
	 * and use custom FileProvider to manage user files.
	 * <p>
	 * This method replaces stock VmPolicy, and disable
	 * default exposed check.
	 * @see StrictMode.VmPolicy.Builder#detectFileUriExposure()
	 * <p>
	 * This is a temporary hack, and will be removed later.
	 * <p>
	 * //TODO: Remove this code after 2.0.0 release
	 **/
	private void hackVmPolicy() {
		if (Build.VERSION.SDK_INT >= 24) {
			StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
			StrictMode.setVmPolicy(builder.build());
		} else {
			Timber.i("Lower android version %d", Build.VERSION.SDK_INT);
		}
	}
}
