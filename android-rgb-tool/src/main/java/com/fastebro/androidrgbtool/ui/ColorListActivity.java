package com.fastebro.androidrgbtool.ui;

import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.adapters.ColorListAdapter;
import com.fastebro.androidrgbtool.contracts.ColorDataContract;
import com.fastebro.android.rgbtool.model.events.ColorDeleteEvent;
import com.fastebro.android.rgbtool.model.events.ColorSelectEvent;
import com.fastebro.android.rgbtool.model.events.ColorShareEvent;
import com.fastebro.android.rgbtool.model.events.UpdateSaveColorUIEvent;
import com.fastebro.androidrgbtool.provider.RGBToolContentProvider;
import com.fastebro.androidrgbtool.utils.UColor;
import com.fastebro.androidrgbtool.utils.UDatabase;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by danielealtomare on 15/02/15.
 * Project: rgb-tool
 */
public class ColorListActivity extends EventBaseActivity implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    @InjectView(android.R.id.list)
    ListView listView;

    @InjectView(R.id.list_empty_progress)
    LinearLayout progressBar;

    @InjectView(R.id.list_empty_text)
    TextView emptyListMessage;

    private ColorListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_list);

        ButterKnife.inject(this);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adapter = new ColorListAdapter(this,
                R.layout.color_list_row, null,
                new String[]{ColorDataContract.ColorEntry.COLUMN_COLOR_HEX},
                new int[]{R.id.hex_value}, 0);

        listView.setOnItemClickListener(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);

        getSupportLoaderManager().initLoader(0, null, this);

        progressBar.setVisibility(View.VISIBLE);
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
        progressBar.setVisibility(View.GONE);

        if (data.getCount() <= 0) {
            emptyListMessage.setVisibility(View.VISIBLE);
        } else {
            emptyListMessage.setVisibility(View.GONE);
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

    public void onEvent(ColorShareEvent event) {
        String[] projection = { ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_R,
                ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_G,
                ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_B,
                ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_A
        };
        String selectionClause = ColorDataContract.ColorEntry._ID + "=?";
        String[] selectionArgs = { String.valueOf(event.colorId) };

        Cursor cursor = getContentResolver().query(RGBToolContentProvider.CONTENT_URI,
                projection,
                selectionClause,
                selectionArgs,
                null);

        if(cursor != null) {
            cursor.moveToFirst();
            int rgbRValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_R));
            int rgbGValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_G));
            int rgbBValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_B));
            int rgbAValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_A));
            cursor.close();

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    UColor.getColorMessage(rgbRValue,
                            rgbGValue,
                            rgbBValue,
                            rgbAValue)
            );
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)));
        }
    }
}
