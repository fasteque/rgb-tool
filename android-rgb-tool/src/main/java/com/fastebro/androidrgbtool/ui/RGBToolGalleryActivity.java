package com.fastebro.androidrgbtool.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.AdapterView;
import android.widget.GridView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.adapters.RGBToolImagesCursorAdapter;
import com.fastebro.androidrgbtool.utils.ImageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


public class RGBToolGalleryActivity extends BaseActivity
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

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
                colorPickerIntent.putExtra(ImageUtils.EXTRA_JPEG_FILE_PATH, photoPath);
                colorPickerIntent.putExtra(ImageUtils.EXTRA_DELETE_FILE, false);
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
            mode.setTitle(getString(R.string.rgbtool_gallery_context_menu_title));
            mode.setSubtitle(getString(R.string.rgbtool_gallery_context_menu_subtitle));
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.rgbtool_gallery_context, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteSelectedItems();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        public void onDestroyActionMode(ActionMode mode) {
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            int selectCount = mGridView.getCheckedItemCount();
            switch (selectCount) {
                case 1:
                    mode.setSubtitle(getString(R.string.rgbtool_gallery_context_menu_subtitle));
                    break;
                default:
                    mode.setSubtitle(getString(R.string.rgbtool_gallery_context_menu_subtitle_more, selectCount));
                    break;
            }
        }
    }

    private void deleteSelectedItems() {
        SparseBooleanArray checked = mGridView.getCheckedItemPositions();
        ArrayList<Integer> positions = new ArrayList<Integer>();

        for (int i=0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                positions.add(checked.keyAt(i));
            }
        }

        Collections.sort(positions, Collections.reverseOrder());

        for (int position : positions) {
            Cursor cursor = (Cursor )mGridView.getItemAtPosition(position);
            if(cursor != null) {
                String photoPath = cursor.getString(cursor.getColumnIndex("_data"));
                File  existingFile = new File(photoPath);
                //noinspection ResultOfMethodCallIgnored
                existingFile.delete();
                getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{photoPath});
            }
            mAdapter.notifyDataSetChanged();
        }
    }
}
