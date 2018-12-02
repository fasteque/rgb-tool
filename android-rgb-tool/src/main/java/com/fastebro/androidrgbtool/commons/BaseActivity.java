package com.fastebro.androidrgbtool.commons;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Fade;

import com.fastebro.androidrgbtool.print.PrintJobDialogFragment;

/**
 * Created by danielealtomare on 26/10/14.
 * Project: rgb-tool
 */
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Fade());
        getWindow().setAllowEnterTransitionOverlap(true);
    }

    protected void showPrintColorDialog() {
        DialogFragment dialog = PrintJobDialogFragment.newInstance(PrintJobDialogFragment.PRINT_COLOR_JOB);
        dialog.show(getSupportFragmentManager(), null);
    }
}
