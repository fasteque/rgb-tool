package com.fastebro.androidrgbtool.colordetails;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.print.PrintManager;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.MenuItem;

import com.fastebro.androidrgbtool.model.events.PrintColorDetailsEvent;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.commons.EventBaseActivity;
import com.fastebro.androidrgbtool.print.PrintJobDialogFragment;
import com.fastebro.androidrgbtool.print.RGBToolPrintColorDetailsAdapter;
import com.fastebro.androidrgbtool.utils.ColorUtils;

import org.greenrobot.eventbus.Subscribe;

public class ColorDetailsActivity extends EventBaseActivity {

    public static final String INTENT_EXTRA_RGB_COLOR = "com.fastebro.androidrgbtool.extra.RGB_COLOR";
    private short[] argbValues;
    private int complementaryColor;
    private int contrastColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        argbValues = getIntent().getShortArrayExtra(INTENT_EXTRA_RGB_COLOR);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(String.format("#%s%s%s%s", ColorUtils.RGBToHex(argbValues[0]), ColorUtils
                            .RGBToHex(argbValues[1]), ColorUtils.RGBToHex(argbValues[2]),
                    ColorUtils.RGBToHex(argbValues[3])));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            } else {
                finish();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void printColors() {
        showPrintColorDialog(PrintJobDialogFragment.PRINT_COLOR_DETAILS_JOB);
    }

    @Subscribe
    public void onPrintColorDetailsEvent(PrintColorDetailsEvent event) {
        startPrintJob(event.message);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void startPrintJob(String message) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        // Set job name, which will be displayed in the print queue
        String jobName = getString(R.string.app_name) + "_Color_Details";

        // Start a print job, passing in a PrintDocumentAdapter implementation
        int color = Color.argb(argbValues[0], argbValues[1], argbValues[2], argbValues[3]);

        printManager.print(jobName, new RGBToolPrintColorDetailsAdapter(this, message, color, complementaryColor,
                contrastColor), null);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ColorSampleFragment.newInstance(argbValues, true);
                case 1:
                    return ColorSampleFragment.newInstance(argbValues, false);
                case 2:
                    return ColorComplementaryFragment.newInstance(argbValues);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.color_details_as_text_title);
                case 1:
                    return getString(R.string.color_details_as_background_title);
                case 2:
                    return getString(R.string.color_details_colors_title);
            }
            return null;
        }
    }

    public int getComplementaryColor() {
        return complementaryColor;
    }

    public void setComplementaryColor(int complementaryColor) {
        this.complementaryColor = complementaryColor;
    }

    public int getContrastColor() {
        return contrastColor;
    }

    public void setContrastColor(int contrastColor) {
        this.contrastColor = contrastColor;
    }
}
