package com.fastebro.androidrgbtool.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ColorComplementaryFragment extends Fragment {

    private static final String ARG_RGB_COLOR = "arg_rgb_color";
    private static final String ARG_IS_TEXT = "arg_is_text";
    private short[] argbValues;
    private boolean isText;

    @Bind(R.id.complementaryColorBackground)
    CardView complementaryColorBackground;
    @Bind(R.id.complementaryColorText)
    TextView complementaryColorText;

    @Bind(R.id.contrastColorBackground)
    CardView contrastColorBackground;
    @Bind(R.id.contrastColorText)
    TextView contrastColorText;


    public ColorComplementaryFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameter.
     *
     * @param rgbaValues
     * @return A new instance of fragment ColorSampleFragment.
     */
    public static ColorComplementaryFragment newInstance(short[] rgbaValues, boolean isText) {
        ColorComplementaryFragment fragment = new ColorComplementaryFragment();
        Bundle args = new Bundle();
        args.putShortArray(ARG_RGB_COLOR, rgbaValues);
        args.putBoolean(ARG_IS_TEXT, isText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            argbValues = getArguments().getShortArray(ARG_RGB_COLOR);
            isText = getArguments().getBoolean(ARG_IS_TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_color_sample, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
