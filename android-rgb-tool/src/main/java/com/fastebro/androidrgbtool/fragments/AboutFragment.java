package com.fastebro.androidrgbtool.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.fastebro.androidrgbtool.R;


public class AboutFragment extends PreferenceFragment {

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
}
