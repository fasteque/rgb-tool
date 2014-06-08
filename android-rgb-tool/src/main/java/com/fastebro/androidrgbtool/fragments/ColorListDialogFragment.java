package com.fastebro.androidrgbtool.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.adapters.ColorListAdapter;
import com.fastebro.androidrgbtool.contracts.ColorDataContract;
import com.fastebro.androidrgbtool.interfaces.OnColorDeleteListener;
import com.fastebro.androidrgbtool.provider.RGBToolContentProvider;
import com.fastebro.androidrgbtool.utils.UDatabase;

/**
 * Created by danielealtomare on 17/04/14.
 */
public class ColorListDialogFragment extends DialogFragment
        implements
        LoaderManager.LoaderCallbacks,
        OnColorDeleteListener,
        AdapterView.OnItemClickListener {

    private ColorListAdapter mAdapter;

    public ColorListDialogFragment() {
    }


    /* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 */
    public interface ColorListDialogListener {
        public void onColorClick(float RGBRComponent,
                                 float RGBGComponent,
                                 float RGBBComponent,
                                 float RGBOComponent,
                                 String colorName);
    }

    // Use this instance of the interface to deliver action events
    private ColorListDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the ColorListDialogListener so we can send events to the host
            mListener = (ColorListDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ColorListDialogListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_color_list, container);

        mAdapter = new ColorListAdapter(getActivity(),
                R.layout.color_list_row, null,
                new String[]{ColorDataContract.ColorEntry.COLUMN_COLOR_HEX},
                new int[]{R.id.hex_value}, 0);
        mAdapter.setOnColorClickListener(this);

        ListView listview = (ListView) view.findViewById(android.R.id.list);
        listview.setOnItemClickListener(this);
        listview.setAdapter(mAdapter);

        getDialog().setTitle(getString(R.string.action_color_list));
        getLoaderManager().initLoader(0, null, this);

        view.findViewById(R.id.list_empty_progress).setVisibility(View.VISIBLE);

        return view;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);

        if (cursor != null) {
            int rgbRValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_R));
            int rgbGValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_G));
            int rgbBValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_B));
            int rgbAValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_A));

            if (mListener != null) {
                mListener.onColorClick(rgbRValue,
                        rgbGValue, rgbBValue, rgbAValue, null);
                dismiss();
            }
        }
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
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
    public void onLoadFinished(Loader loader, Object data) {
        Log.d("DEBUG", "onLoadFinished");
        getView().findViewById(R.id.list_empty_progress).setVisibility(View.GONE);

        if (((Cursor) data).getCount() <= 0) {
            getView().findViewById(R.id.list_empty_text).setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(R.id.list_empty_text).setVisibility(View.GONE);
        }

        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor((Cursor) data);
    }


    @Override
    public void onLoaderReset(Loader loader) {
        Log.d("DEBUG", "onLoaderReset");
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }


    @Override
    public void onColorClick(int colorId) {
        // Defines selection criteria for the rows to delete.
        String mSelectionClause = ColorDataContract.ColorEntry._ID + "=?";
        String[] mSelectionArgs = {String.valueOf(colorId)};

        Log.d("DEBUG", "delete");
        getActivity().getContentResolver().delete(
                RGBToolContentProvider.CONTENT_URI,
                mSelectionClause,
                mSelectionArgs);
    }
}
