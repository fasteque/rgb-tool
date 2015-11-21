package com.fastebro.androidrgbtool.ui;

import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.fragments.ColorComplementaryFragment;
import com.fastebro.androidrgbtool.fragments.ColorSampleFragment;
import com.fastebro.androidrgbtool.utils.ColorUtils;

public class ColorDetailsActivity extends BaseActivity {

    protected static final String INTENT_EXTRA_RGB_COLOR = "com.fastebro.androidrgbtool.extra.RGB_COLOR";
    private short[] argbValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
}
