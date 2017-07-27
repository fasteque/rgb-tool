package com.fastebro.androidrgbtool.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.utils.ThemeWrapper;


public class AboutFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private OnPreferenceSelectedListener onPreferenceSelectedListener;

    public interface OnPreferenceSelectedListener {
        void onPreferenceWithUriSelected(Uri uri);
        void onPreferenceSendEmailSelected(String[] addresses, String subject);
    }

    public AboutFragment() {}

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onPreferenceSelectedListener = (OnPreferenceSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnPreferenceSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        // иначе будет падать на kit-kat
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setCurrentValue((ListPreference) findPreference("about.theme"));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getIntent() != null && preference.getIntent().getData() != null) {
            if (preference.getIntent().getAction().equals(Intent.ACTION_SENDTO)) {
                onPreferenceSelectedListener.onPreferenceSendEmailSelected(new String[]{preference.getIntent()
                        .getData().toString()}, getString(R
                        .string.app_name));
            } else {
                onPreferenceSelectedListener.onPreferenceWithUriSelected(preference.getIntent().getData());
            }
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "about.theme":
                setCurrentValue((ListPreference) findPreference(key));
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent("org.openintents.action.REFRESH_THEME"));
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setCurrentValue(ListPreference listPreference){
        listPreference.setSummary(listPreference.getEntry());
    }

    public static int getThemeIndex(Context ctx) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(ctx).getString("about.theme", String.valueOf(ThemeWrapper.Theme.LIGHT.ordinal())));
    }
}
