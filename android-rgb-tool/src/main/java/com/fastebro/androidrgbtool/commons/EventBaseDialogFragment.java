package com.fastebro.androidrgbtool.commons;

import android.support.v4.app.DialogFragment;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by danielealtomare on 26/12/14.
 * Project: rgb-tool
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
