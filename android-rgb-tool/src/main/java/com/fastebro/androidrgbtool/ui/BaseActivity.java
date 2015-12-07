package com.fastebro.androidrgbtool.ui;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Fade;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.fragments.PrintJobDialogFragment;

/**
 * Created by danielealtomare on 26/10/14.
 * Project: rgb-tool
 */
public class BaseActivity extends AppCompatActivity {
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(getSupportActionBar() != null) {
                getSupportActionBar().setElevation(0);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().setNavigationBarColor(getResources().getColor(R.color.primary_dark, getTheme()));
            } else {
                getWindow().setNavigationBarColor(getResources().getColor(R.color.primary_dark));
            }
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Fade());
            getWindow().setAllowEnterTransitionOverlap(true);
        }
    }

    protected void showPrintColorDialog() {
        DialogFragment dialog = PrintJobDialogFragment.newInstance(PrintJobDialogFragment.PRINT_COLOR_JOB);
        dialog.show(getSupportFragmentManager(), null);
    }
}
