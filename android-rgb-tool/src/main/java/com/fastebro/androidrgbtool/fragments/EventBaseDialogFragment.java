package com.fastebro.androidrgbtool.fragments;

import android.support.v4.app.DialogFragment;

import de.greenrobot.event.EventBus;

/**
 * Created by danielealtomare on 26/12/14.
 */
public class EventBaseDialogFragment extends DialogFragment {
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
