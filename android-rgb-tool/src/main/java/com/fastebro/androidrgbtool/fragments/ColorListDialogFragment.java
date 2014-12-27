package com.fastebro.androidrgbtool.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.adapters.ColorListAdapter;
import com.fastebro.androidrgbtool.contracts.ColorDataContract;
import com.fastebro.androidrgbtool.events.ColorDeleteEvent;
import com.fastebro.androidrgbtool.events.ColorSelectEvent;
import com.fastebro.androidrgbtool.events.UpdateSaveColorUIEvent;
import com.fastebro.androidrgbtool.provider.RGBToolContentProvider;
import com.fastebro.androidrgbtool.ui.MainActivity;
import com.fastebro.androidrgbtool.utils.UDatabase;

import de.greenrobot.event.EventBus;


/**
 * Created by danielealtomare on 17/04/14.
 */
public class ColorListDialogFragment extends EventBaseDialogFragment
        implements
        AdapterView.OnItemClickListener,
        android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    private ColorListAdapter mAdapter;

    public ColorListDialogFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_color_list, container);

        mAdapter = new ColorListAdapter(getActivity(),
                R.layout.color_list_row, null,
                new String[]{ColorDataContract.ColorEntry.COLUMN_COLOR_HEX},
                new int[]{R.id.hex_value}, 0);

        ListView listview = (ListView) view.findViewById(android.R.id.list);
        listview.setOnItemClickListener(this);
        listview.setAdapter(mAdapter);

        getDialog().setTitle(getString(R.string.action_color_list));
        getActivity().getSupportLoaderManager().initLoader(0, null, this);

        view.findViewById(R.id.list_empty_progress).setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onDestroyView() {
        if(getActivity() != null) {
            getActivity().getSupportLoaderManager().destroyLoader(0);
            EventBus.getDefault().post(new UpdateSaveColorUIEvent());
        }
        super.onDestroyView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);

        if (cursor != null) {
            int rgbRValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_R));
            int rgbGValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_G));
            int rgbBValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_B));
            int rgbAValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_A));

            EventBus.getDefault().post(new ColorSelectEvent(rgbRValue,
                    rgbGValue, rgbBValue, rgbAValue, null));
            dismiss();
        }
    }


    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        Uri baseUri = RGBToolContentProvider.CONTENT_URI;

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        String select = "((" + ColorDataContract.ColorEntry.COLUMN_COLOR_HEX + " NOTNULL))";
        return new CursorLoader(getActivity(), baseUri,
                UDatabase.COLORS_SUMMARY_PROJECTION, select, null,
                ColorDataContract.ColorEntry._ID + " DESC");
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> objectLoader, Cursor data) {
        if(getView() != null) {
            getView().findViewById(R.id.list_empty_progress).setVisibility(View.GONE);

            if (data.getCount() <= 0) {
                getView().findViewById(R.id.list_empty_text).setVisibility(View.VISIBLE);
            } else {
                getView().findViewById(R.id.list_empty_text).setVisibility(View.GONE);
            }

            // Swap the new cursor in. (The framework will take care of closing the
            // old cursor once we return.)
            mAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> objectLoader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }


    public void onEvent(ColorDeleteEvent event) {
        String mSelectionClause = ColorDataContract.ColorEntry._ID + "=?";
        String[] mSelectionArgs = {String.valueOf(event.colorId)};

        getActivity().getContentResolver().delete(
                RGBToolContentProvider.CONTENT_URI,
                mSelectionClause,
                mSelectionArgs);
    }
}
