package com.fastebro.androidrgbtool;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.runner.RunWith;

import com.fastebro.androidrgbtool.rgb.MainActivity;

/**
 * Created by danielealtomare on 23/01/15.
 * Project: rgb-tool
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityEspressoTest {
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);
}
