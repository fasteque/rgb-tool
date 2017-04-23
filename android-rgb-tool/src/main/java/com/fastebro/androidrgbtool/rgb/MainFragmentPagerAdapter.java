package com.fastebro.androidrgbtool.rgb;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.fastebro.androidrgbtool.R;

/**
 * Created by danielealtomare on 16/04/17.
 * Project: rgb-tool
 */

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {

    private final Context context;

    public MainFragmentPagerAdapter(FragmentManager fm, @NonNull Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new MainColorFragment();
        } else if (position == 1) {
            return new ColorDetailsFragment();
        }

        // It should never happen.
        return new MainColorFragment();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return context.getString(R.string.main_tab_color);
        } else if (position == 1) {
            return context.getString(R.string.main_tab_details);
        }

        // It should never happen.
        return context.getString(R.string.main_tab_color);
    }
}
