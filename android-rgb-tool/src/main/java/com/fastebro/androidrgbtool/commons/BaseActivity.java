package com.fastebro.androidrgbtool.commons;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Fade;

import com.fastebro.androidrgbtool.print.PrintJobDialogFragment;
import com.fastebro.androidrgbtool.settings.AboutActivity;
import com.fastebro.androidrgbtool.utils.ThemeWrapper;

/**
 * Created by danielealtomare on 26/10/14.
 * Project: rgb-tool
 */
public abstract class BaseActivity extends AppCompatActivity {
    @SuppressLint("NewApi")

    // Theme Receiver
    private final BroadcastReceiver mThemeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AboutActivity.class.equals(BaseActivity.this.getClass())){
                finish();
                startActivity(getIntent());
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else recreate();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this).registerReceiver(mThemeReceiver, new IntentFilter("org.openintents.action.REFRESH_THEME"));
        ThemeWrapper.applyTheme(this);
        // Replaced with styles-v21
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.light_primary_dark));
//        } else {
//            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.light_primary_dark));
//        }
        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Fade());
        getWindow().setAllowEnterTransitionOverlap(true);
    }

    protected void showPrintColorDialog(int jobType) {
        DialogFragment dialog = PrintJobDialogFragment.newInstance(jobType);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mThemeReceiver);
        super.onDestroy();
    }
}
