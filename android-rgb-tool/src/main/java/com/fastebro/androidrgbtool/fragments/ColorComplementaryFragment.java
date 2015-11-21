package com.fastebro.androidrgbtool.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.utils.ColorUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ColorComplementaryFragment extends Fragment {

    private static final String ARG_RGB_COLOR = "arg_rgb_color";
    private short[] argbValues;

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
    public static ColorComplementaryFragment newInstance(short[] rgbaValues) {
        ColorComplementaryFragment fragment = new ColorComplementaryFragment();
        Bundle args = new Bundle();
        args.putShortArray(ARG_RGB_COLOR, rgbaValues);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            argbValues = getArguments().getShortArray(ARG_RGB_COLOR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_color_complementary, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        int complementaryColor = ColorUtils.getComplementaryColor(argbValues[1], argbValues[2], argbValues[3]);
        complementaryColorText.setText(getString(R.string.color_details_complementary, ColorUtils.RGBToHex
                (complementaryColor)));

        int contrastColor = ColorUtils.getContrastColor(argbValues[1], argbValues[2], argbValues[3]);
        contrastColorText.setText(getString(R.string.color_details_contrast, ColorUtils.RGBToHex
                (contrastColor)));

        complementaryColorBackground.setCardBackgroundColor(complementaryColor);
        contrastColorBackground.setCardBackgroundColor(contrastColor);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
