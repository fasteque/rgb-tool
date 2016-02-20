package com.fastebro.androidrgbtool.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.view.MenuItem;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.helpers.CustomTabActivityHelper;
import com.fastebro.androidrgbtool.helpers.WebViewFallback;
import com.fastebro.androidrgbtool.commons.BaseActivity;

public class AboutActivity extends BaseActivity implements AboutFragment.OnPreferenceSelectedListener {

    private CustomTabActivityHelper customTabActivityHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupCustomTabHelper();

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AboutFragment())
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        customTabActivityHelper.bindCustomTabsService(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        customTabActivityHelper.unbindCustomTabsService(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            } else {
                finish();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void setupCustomTabHelper() {
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(connectionCallback);
    }

    private final CustomTabActivityHelper.ConnectionCallback connectionCallback = new CustomTabActivityHelper
            .ConnectionCallback() {
        @Override
        public void onCustomTabsConnected() {
            // Use this callback to perform UI changes.
        }

        @Override
        public void onCustomTabsDisconnected() {
            // Use this callback to perform UI changes.
        }
    };

    @Override
    public void onPreferenceWithUriSelected(Uri uri) {
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intentBuilder.setToolbarColor(getResources().getColor(R.color.primary, getTheme()));
        } else {
            //noinspection deprecation
            intentBuilder.setToolbarColor(getResources().getColor(R.color.primary));
        }
        intentBuilder.setShowTitle(true);


        CustomTabActivityHelper.openCustomTab(this, intentBuilder.build(), uri, new WebViewFallback());
    }

    @Override
    public void onPreferenceSendEmailSelected(String[] addresses, String subject) {
        composeEmail(addresses, subject);
    }

    private void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        // Only email apps should handle this.
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.action_not_supported_error), Snackbar.LENGTH_SHORT).show();
        }
    }
}
