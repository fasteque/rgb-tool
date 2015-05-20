package com.fastebro.androidrgbtool.ui;

import android.os.Bundle;

import de.greenrobot.event.EventBus;

/**
 * Created by danielealtomare on 26/12/14.
 * Project: rgb-tool
 */
public class EventBaseActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
