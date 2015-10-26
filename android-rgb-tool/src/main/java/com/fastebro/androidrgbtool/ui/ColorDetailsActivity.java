package com.fastebro.androidrgbtool.ui;

import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.fragments.ColorSampleFragment;

public class ColorDetailsActivity extends BaseActivity {

    protected static final String INTENT_EXTRA_RGB_COLOR = "com.fastebro.androidrgbtool.extra.RGB_COLOR";
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;
    private short[] rgbaValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        rgbaValues = getIntent().getShortArrayExtra(INTENT_EXTRA_RGB_COLOR);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // TODO: used the static method to get a new instance.
            return new ColorSampleFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.color_details_as_text_title);
                case 1:
                    return getString(R.string.color_details_as_background_title);
            }
            return null;
        }
    }
}
