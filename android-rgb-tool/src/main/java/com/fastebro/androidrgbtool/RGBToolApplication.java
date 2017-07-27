package com.fastebro.androidrgbtool;

import android.app.Application;
import android.preference.PreferenceManager;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import timber.log.Timber;

/**
 * Created by danielealtomare on 08/05/15.
 * Project: rgb-tool
 */
// ACRA
@ReportsCrashes(
        mailTo = "daniele.altomare@gmail.com",
        mode = ReportingInteractionMode.NOTIFICATION,
        resNotifTitle = R.string.error,
        resNotifText = R.string.error_send_mail,
        resNotifTickerText = R.string.error_dlg_content,
        resToastText = R.string.error,
        resDialogTitle = R.string.error,
        resDialogIcon = R.mipmap.ic_launcher,
        resDialogText = R.string.error_dlg_content,
        resDialogTheme = R.style.ACRA,
        customReportContent = {
                ReportField.APP_VERSION_NAME,
                ReportField.APP_VERSION_CODE,
                ReportField.ANDROID_VERSION,
                ReportField.BRAND,
                ReportField.PHONE_MODEL,
                ReportField.STACK_TRACE,
                ReportField.LOGCAT,
        }
)
public class RGBToolApplication extends Application {
    private static RGBToolApplication ctx = new RGBToolApplication();

    public RGBToolApplication(){
        ctx = this;
    }

    @Override public void onCreate() {
        super.onCreate();
        // ACRA initialization
        ACRA.init(this);

        // Set default settings
        PreferenceManager.setDefaultValues(this, R.xml.about, false);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public static RGBToolApplication getCtx(){
        return ctx;
    }
}
