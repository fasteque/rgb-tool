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
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.adapters.RGBToolImagesCursorAdapter;
import com.fastebro.androidrgbtool.utils.UImage;


public class RGBToolGalleryActivity extends Activity
    implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

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
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        mGridView.setMultiChoiceModeListener(new MultiChoiceModeListener());

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
            }
        }
    }

    interface RGBToolImagesQuery {
        int QUERY_ID = 1;
        Uri IMAGES_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    public class MultiChoiceModeListener implements
            GridView.MultiChoiceModeListener {
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Select Items");
            mode.setSubtitle("One item selected");
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            int selectCount = mGridView.getCheckedItemCount();
            switch (selectCount) {
                case 1:
                    mode.setSubtitle("One item selected");
                    break;
                default:
                    mode.setSubtitle("" + selectCount + " items selected");
                    break;
            }
        }
    }
}
