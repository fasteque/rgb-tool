package com.fastebro.androidrgbtool.ui;


import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by danielealtomare on 26/12/14.
 * Project: rgb-tool
 */
public abstract class EventBaseActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
