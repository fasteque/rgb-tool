package com.fastebro.androidrgbtool;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.fastebro.androidrgbtool.ui.MainActivity;

/**
 * Created by danielealtomare on 23/01/15.
 */

@LargeTest
public class MainActivityEspressoTest extends ActivityInstrumentationTestCase2<MainActivity> {
    public MainActivityEspressoTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    public MainActivityEspressoTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
        Thread.sleep(3000);
    }

    @SuppressWarnings("unchecked")
    public void testClickActionBarItem() {
        onView(withId(R.id.action_camera))
                .perform(click());
    }
}
