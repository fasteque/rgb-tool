package com.fastebro.androidrgbtool;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.fastebro.androidrgbtool.rgb.MainActivity;

/**
 * Created by danielealtomare on 23/01/15.
 * Project: rgb-tool
 */

@LargeTest
public class MainActivityEspressoTest extends ActivityInstrumentationTestCase2<MainActivity> {
    public MainActivityEspressoTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
