package com.fastebro.androidrgbtool.test;

import android.app.Activity;
import com.fastebro.androidrgbtool.ui.AttributionsActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertTrue;

/**
 * Created by danielealtomare on 29/08/14.
 */

/*
    We need to emulate API Level 18 because Robolectric
    does not support API Level 19 yet.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = "./src/main/AndroidManifest.xml", emulateSdk = 18)
public class AttributionsActivityRobolectricTest {
    @Test
    public void testActivityInstance() throws Exception {
        Activity activity = Robolectric.buildActivity(AttributionsActivity.class).create().get();
        assertTrue(activity != null);
    }
}
