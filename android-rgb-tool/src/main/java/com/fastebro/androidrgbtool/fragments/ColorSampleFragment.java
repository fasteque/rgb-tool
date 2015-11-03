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

public class ColorSampleFragment extends Fragment {

    private static final String ARG_RGB_COLOR = "arg_rgb_color";
    private static final String ARG_IS_TEXT = "arg_is_text";
    private short[] argbValues;
    private boolean isText;

    @Bind(R.id.firstColorSampleBackground)
    CardView firstColorSampleBackground;
    @Bind(R.id.firstColorSampleTextItalic)
    TextView firstColorSampleTextItalic;
    @Bind(R.id.firstColorSampleTextNormal)
    TextView firstColorSampleTextNormal;
    @Bind(R.id.firstColorSampleTextBold)
    TextView firstColorSampleTextBold;

    @Bind(R.id.secondColorSampleBackground)
    CardView secondColorSampleBackground;
    @Bind(R.id.secondColorSampleTextItalic)
    TextView secondColorSampleTextItalic;
    @Bind(R.id.secondColorSampleTextNormal)
    TextView secondColorSampleTextNormal;
    @Bind(R.id.secondColorSampleTextBold)
    TextView secondColorSampleTextBold;


    public ColorSampleFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameter.
     *
     * @param rgbaValues
     * @return A new instance of fragment ColorSampleFragment.
     */
    public static ColorSampleFragment newInstance(short[] rgbaValues, boolean isText) {
        ColorSampleFragment fragment = new ColorSampleFragment();
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

        if (isText) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                firstColorSampleBackground.setCardBackgroundColor(getResources().getColor(R.color.white, getActivity
                        ().getTheme()));

                secondColorSampleBackground.setCardBackgroundColor(getResources().getColor(R.color.black, getActivity
                        ().getTheme()));
            } else {
                firstColorSampleBackground.setCardBackgroundColor(getResources().getColor(R.color.white));
                secondColorSampleBackground.setCardBackgroundColor(getResources().getColor(R.color.black));
            }

            firstColorSampleTextItalic.setTextColor(Color.argb(argbValues[0], argbValues[1],
                    argbValues[2], argbValues[3]));
            firstColorSampleTextNormal.setTextColor(Color.argb(argbValues[0], argbValues[1],
                    argbValues[2], argbValues[3]));
            firstColorSampleTextBold.setTextColor(Color.argb(argbValues[0], argbValues[1],
                    argbValues[2], argbValues[3]));

            secondColorSampleTextItalic.setTextColor(Color.argb(argbValues[0], argbValues[1],
                    argbValues[2], argbValues[3]));
            secondColorSampleTextNormal.setTextColor(Color.argb(argbValues[0], argbValues[1],
                    argbValues[2], argbValues[3]));
            secondColorSampleTextBold.setTextColor(Color.argb(argbValues[0], argbValues[1],
                    argbValues[2], argbValues[3]));
        } else {
            firstColorSampleBackground.setCardBackgroundColor(Color.argb(argbValues[0], argbValues[1],
                    argbValues[2], argbValues[3]));

            secondColorSampleBackground.setCardBackgroundColor(Color.argb(argbValues[0], argbValues[1],
                    argbValues[2], argbValues[3]));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                firstColorSampleTextItalic.setTextColor(getResources().getColor(R.color.white, getActivity().getTheme
                        ()));
                firstColorSampleTextNormal.setTextColor(getResources().getColor(R.color.white, getActivity
                        ().getTheme()));
                firstColorSampleTextBold.setTextColor(getResources().getColor(R.color.white, getActivity
                        ().getTheme()));

                secondColorSampleTextItalic.setTextColor(getResources().getColor(R.color.black, getActivity
                        ().getTheme()));
                secondColorSampleTextNormal.setTextColor(getResources().getColor(R.color.black, getActivity
                        ().getTheme()));
                secondColorSampleTextBold.setTextColor(getResources().getColor(R.color.black, getActivity
                        ().getTheme()));
            } else {

                firstColorSampleTextItalic.setTextColor(getResources().getColor(R.color.white));
                firstColorSampleTextNormal.setTextColor(getResources().getColor(R.color.white));
                firstColorSampleTextBold.setTextColor(getResources().getColor(R.color.white));

                secondColorSampleTextItalic.setTextColor(getResources().getColor(R.color.black));
                secondColorSampleTextNormal.setTextColor(getResources().getColor(R.color.black));
                secondColorSampleTextBold.setTextColor(getResources().getColor(R.color.black));
            }
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
