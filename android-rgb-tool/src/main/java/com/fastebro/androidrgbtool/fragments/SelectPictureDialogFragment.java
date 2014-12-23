package com.fastebro.androidrgbtool.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.adapters.SelectPictureListAdapter;
import com.fastebro.androidrgbtool.managers.RecyclerViewLinearLayoutManager;
import com.fastebro.androidrgbtool.ui.MainActivity;

/**
 * Created by danielealtomare on 21/06/14.
 */
public class SelectPictureDialogFragment extends DialogFragment implements SelectPictureListAdapter.ItemClickListener {
    private RecyclerView recyclerView;
    private SelectPictureListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;


    public SelectPictureDialogFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_picture, container);

        adapter = new SelectPictureListAdapter(getResources().getStringArray(R.array.pick_color_array), this);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        layoutManager = new RecyclerViewLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        getDialog().setTitle(getString(R.string.pick_color));

        return view;
    }

    @Override
    public void onClick(View v, int position, boolean isLongClick) {
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
