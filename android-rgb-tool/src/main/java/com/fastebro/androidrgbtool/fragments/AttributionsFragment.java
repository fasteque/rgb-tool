package com.fastebro.androidrgbtool.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.fastebro.androidrgbtool.R;


public class AttributionsFragment extends PreferenceFragment {

    public AttributionsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.attributions);
    }
}
