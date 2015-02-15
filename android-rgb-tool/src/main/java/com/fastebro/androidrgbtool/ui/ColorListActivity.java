package com.fastebro.androidrgbtool.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.adapters.ColorListAdapter;
import com.fastebro.androidrgbtool.contracts.ColorDataContract;
import com.fastebro.androidrgbtool.events.ColorDeleteEvent;
import com.fastebro.androidrgbtool.events.ColorSelectEvent;
import com.fastebro.androidrgbtool.events.UpdateSaveColorUIEvent;
import com.fastebro.androidrgbtool.provider.RGBToolContentProvider;
import com.fastebro.androidrgbtool.utils.UDatabase;

import de.greenrobot.event.EventBus;

/**
 * Created by danielealtomare on 15/02/15.
 */
public class ColorListActivity extends EventBaseActivity implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private ColorListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_list);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adapter = new ColorListAdapter(this,
                R.layout.color_list_row, null,
                new String[]{ColorDataContract.ColorEntry.COLUMN_COLOR_HEX},
                new int[]{R.id.hex_value}, 0);

        ListView listview = (ListView) findViewById(android.R.id.list);
        listview.setOnItemClickListener(this);
        listview.setAdapter(adapter);

        getSupportLoaderManager().initLoader(0, null, this);

        findViewById(R.id.list_empty_progress).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        getSupportLoaderManager().destroyLoader(0);
        EventBus.getDefault().post(new UpdateSaveColorUIEvent());
        super.onDestroy();
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) adapter.getItem(position);

        if (cursor != null) {
            int rgbRValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_R));
            int rgbGValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_G));
            int rgbBValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_B));
            int rgbAValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_A));

            EventBus.getDefault().post(new ColorSelectEvent(rgbRValue,
                    rgbGValue, rgbBValue, rgbAValue, null));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            } else {
                finish();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        Uri baseUri = RGBToolContentProvider.CONTENT_URI;

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        String select = "((" + ColorDataContract.ColorEntry.COLUMN_COLOR_HEX + " NOTNULL))";
        return new CursorLoader(this, baseUri,
                UDatabase.COLORS_SUMMARY_PROJECTION, select, null,
                ColorDataContract.ColorEntry._ID + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        findViewById(R.id.list_empty_progress).setVisibility(View.GONE);

        if (data.getCount() <= 0) {
            findViewById(R.id.list_empty_text).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.list_empty_text).setVisibility(View.GONE);
        }

        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        adapter.swapCursor(null);
    }

    public void onEvent(ColorDeleteEvent event) {
        String mSelectionClause = ColorDataContract.ColorEntry._ID + "=?";
        String[] mSelectionArgs = {String.valueOf(event.colorId)};

        getContentResolver().delete(
                RGBToolContentProvider.CONTENT_URI,
                mSelectionClause,
                mSelectionArgs);
    }
}
