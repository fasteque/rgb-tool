package com.fastebro.androidrgbtool.ui;


import de.greenrobot.event.EventBus;

/**
 * Created by danielealtomare on 26/12/14.
 * Project: rgb-tool
 */
public class EventBaseActivity extends BaseActivity {

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
