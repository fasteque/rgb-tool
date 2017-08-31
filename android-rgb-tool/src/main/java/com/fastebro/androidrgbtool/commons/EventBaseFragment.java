package com.fastebro.androidrgbtool.commons;

import android.support.v4.app.Fragment;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by danielealtomare on 01/05/17.
 * Project: rgb-tool
 */

public class EventBaseFragment extends Fragment {
	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
	}

	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}
}
