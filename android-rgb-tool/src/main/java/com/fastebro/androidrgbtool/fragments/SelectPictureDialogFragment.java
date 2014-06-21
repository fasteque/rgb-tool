package com.fastebro.androidrgbtool.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.adapters.ColorListAdapter;
import com.fastebro.androidrgbtool.adapters.SelectPictureListAdapter;
import com.fastebro.androidrgbtool.contracts.ColorDataContract;
import com.fastebro.androidrgbtool.interfaces.OnColorDeleteListener;
import com.fastebro.androidrgbtool.provider.RGBToolContentProvider;
import com.fastebro.androidrgbtool.ui.MainActivity;
import com.fastebro.androidrgbtool.utils.UDatabase;

/**
 * Created by danielealtomare on 21/06/14.
 */
public class SelectPictureDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    private SelectPictureListAdapter mAdapter;

    public SelectPictureDialogFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_picture, container);

        mAdapter = new SelectPictureListAdapter(getActivity(),
                R.layout.select_picture_row,
                getResources().getStringArray(R.array.pick_color_array));

        ListView listview = (ListView) view.findViewById(android.R.id.list);
        listview.setAdapter(mAdapter);
        listview.setOnItemClickListener(this);

        getDialog().setTitle(getString(R.string.pick_color));

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                if (getActivity() != null) {
                    ((MainActivity)getActivity()).openRGBToolGallery();
                    dismiss();
                }
                break;
            case 1:
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).openDeviceGallery();
                    dismiss();
                }
                break;
            case 2:
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).dispatchTakePictureIntent();
                    dismiss();
                }
                break;
            default:
                break;
        }
    }
}
