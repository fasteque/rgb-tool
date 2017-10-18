package com.fastebro.androidrgbtool.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.fastebro.androidrgbtool.R;


public class AboutFragment extends PreferenceFragment {
	public static final String FRAGMENT_TAG = "fragment_about";

	private OnPreferenceSelectedListener onPreferenceSelectedListener;

	public AboutFragment() {
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		try {
			onPreferenceSelectedListener = (OnPreferenceSelectedListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString() + " must implement OnPreferenceSelectedListener");
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
						.getData().toString()}, getString(R.string.app_name));
			} else {
				onPreferenceSelectedListener.onPreferenceWithUriSelected(preference.getIntent().getData());
			}
			return true;
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	public interface OnPreferenceSelectedListener {
		void onPreferenceWithUriSelected(Uri uri);

		void onPreferenceSendEmailSelected(String[] addresses, String subject);
	}
}
