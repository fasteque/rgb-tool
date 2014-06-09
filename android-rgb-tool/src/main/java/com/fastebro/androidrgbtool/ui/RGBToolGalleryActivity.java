package com.fastebro.androidrgbtool.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.adapters.RGBToolImagesCursorAdapter;
import com.fastebro.androidrgbtool.utils.UImage;


public class RGBToolGalleryActivity extends Activity
    implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    @InjectView(R.id.grid_view)
    GridView mGridView;

    RGBToolImagesCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rgbtool_gallery);
        ButterKnife.inject(this);

        mAdapter = new RGBToolImagesCursorAdapter(this);

        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);

        getLoaderManager().initLoader(RGBToolImagesQuery.QUERY_ID,
                null,
                this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == RGBToolImagesQuery.QUERY_ID) {
            return new CursorLoader(this,
                    RGBToolImagesQuery.IMAGES_URI,
                    new String[] {"_data", "_id"},
                    "_data LIKE ?",
                    new String[] {"%" + getString(R.string.album_name) +"%"},
                    null);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor )parent.getItemAtPosition(position);

        if(cursor != null) {
            String photoPath = cursor.getString(cursor.getColumnIndex("_data"));

            if(!"".equals(photoPath)) {
                Intent colorPickerIntent = new Intent(this, ColorPickerActivity.class);
                colorPickerIntent.putExtra(UImage.EXTRA_JPEG_FILE_PATH, photoPath);
                colorPickerIntent.putExtra(UImage.EXTRA_DELETE_FILE, false);
                startActivity(colorPickerIntent);
                finish();
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    interface RGBToolImagesQuery {
        int QUERY_ID = 1;
        Uri IMAGES_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }
}
