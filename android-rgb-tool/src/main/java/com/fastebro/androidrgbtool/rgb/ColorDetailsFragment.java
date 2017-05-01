package com.fastebro.androidrgbtool.rgb;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.utils.ColorUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by danielealtomare on 16/04/17.
 * Project: rgb-tool
 */

public class ColorDetailsFragment extends Fragment {

    // RGB channel: R,G,B.
    @BindView(R.id.textView_RGB_R)
    TextView tvRGB_R;
    @BindView(R.id.textView_RGB_G)
    TextView tvRGB_G;
    @BindView(R.id.textView_RGB_B)
    TextView tvRGB_B;
    @BindView(R.id.textView_RGB_O)
    TextView tvRGB_O;

    // HSB: Hue, Saturation, Brightness.
    @BindView(R.id.textView_HSB_H)
    TextView tvHSB_H;
    @BindView(R.id.textView_HSB_S)
    TextView tvHSB_S;
    @BindView(R.id.textView_HSB_B)
    TextView tvHSB_B;

    // Color details.
    @BindView(R.id.complementaryColorBackground)
    CardView complementaryColorBackground;
    @BindView(R.id.complementaryColorText)
    TextView complementaryColorText;
    @BindView(R.id.contrastColorBackground)
    CardView contrastColorBackground;
    @BindView(R.id.contrastColorText)
    TextView contrastColorText;

    // Color samples.
    @BindView(R.id.firstColorSampleBackground)
    CardView firstColorSampleBackground;
    @BindView(R.id.firstColorSampleTextNormal)
    TextView firstColorSampleTextNormal;
    @BindView(R.id.secondColorSampleBackground)
    CardView secondColorSampleBackground;
    @BindView(R.id.secondColorSampleTextNormal)
    TextView secondColorSampleTextNormal;
    @BindView(R.id.firstColorSampleBackgroundBg)
    CardView firstColorSampleBackgroundBg;
    @BindView(R.id.firstColorSampleTextNormalBg)
    TextView firstColorSampleTextNormalBg;
    @BindView(R.id.secondColorSampleBackgroundBg)
    CardView secondColorSampleBackgroundBg;
    @BindView(R.id.secondColorSampleTextNormalBg)
    TextView secondColorSampleTextNormalBg;

    private Unbinder unbinder;

    private final View.OnClickListener RGBAClickListener = v -> {
        if (isAdded()) {
            short[] rgbaValues = new short[]{(short) ((MainActivity) getActivity()).getRedColor(),
                    (short) ((MainActivity) getActivity()).getGreenColor(),
                    (short) ((MainActivity) getActivity()).getBlueColor(),
                    (short) ((MainActivity) getActivity()).getOpacity()
            };

            RgbaInsertionFragment fragment = RgbaInsertionFragment.newInstance(rgbaValues);
            fragment.show(getActivity().getSupportFragmentManager(), null);
        }
    };

    public ColorDetailsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_rgb_tab_details, container, false);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshUI();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && isResumed()) {
            refreshUI();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    private void refreshUI() {
        updateColorDetails();
        updateColorSample();
        updateRGBField();
        setRGBOValuesClickListener();
        updateHSBField();
    }

    private void updateColorDetails() {
        int redColor = ((MainActivity) getActivity()).getRedColor();
        int blueColor = ((MainActivity) getActivity()).getBlueColor();
        int greenColor = ((MainActivity) getActivity()).getGreenColor();

        int complementaryColor = ColorUtils.getComplementaryColor(redColor, blueColor, greenColor);
        complementaryColorText.setText(getString(R.string.color_details_complementary, ColorUtils.RGBToHex
                (complementaryColor)));
        complementaryColorBackground.setCardBackgroundColor(complementaryColor);

        int contrastColor = ColorUtils.getContrastColor(redColor, blueColor, greenColor);
        contrastColorText.setText(getString(R.string.color_details_contrast, ColorUtils.RGBToHex
                (contrastColor)));
        contrastColorBackground.setCardBackgroundColor(contrastColor);
    }

    private void updateColorSample() {
        // Text.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            firstColorSampleBackground.setCardBackgroundColor(getResources().getColor(R.color.white, getTheme()));
//            secondColorSampleBackground.setCardBackgroundColor(getResources().getColor(R.color.black, getTheme()));
        } else {
            firstColorSampleBackground.setCardBackgroundColor(getResources().getColor(R.color.white));
            secondColorSampleBackground.setCardBackgroundColor(getResources().getColor(R.color.black));
        }

//        firstColorSampleTextNormal.setTextColor(Color.argb(opacity, redColor, greenColor, blueColor));
//        secondColorSampleTextNormal.setTextColor(Color.argb(opacity, redColor, greenColor, blueColor));

        // Background.
//        firstColorSampleBackgroundBg.setCardBackgroundColor(Color.argb(opacity, redColor, greenColor, blueColor));
//        secondColorSampleBackgroundBg.setCardBackgroundColor(Color.argb(opacity, redColor, greenColor, blueColor));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            firstColorSampleTextNormalBg.setTextColor(getResources().getColor(R.color.white, getTheme()));
//            secondColorSampleTextNormalBg.setTextColor(getResources().getColor(R.color.black, getTheme()));
        } else {
            firstColorSampleTextNormalBg.setTextColor(getResources().getColor(R.color.white));
            secondColorSampleTextNormalBg.setTextColor(getResources().getColor(R.color.black));
        }
    }

    private void updateRGBField() {
        // RGB channel: R, G, B, OPACITY.
        tvRGB_R.setText(ColorUtils.getRGB(((MainActivity) getActivity()).getRedColor()));
        tvRGB_G.setText(ColorUtils.getRGB(((MainActivity) getActivity()).getGreenColor()));
        tvRGB_B.setText(ColorUtils.getRGB(((MainActivity) getActivity()).getBlueColor()));
        tvRGB_O.setText(ColorUtils.getRGB(((MainActivity) getActivity()).getOpacity()));
    }

    private void updateHSBField() {
        // Get float array with 3 values for HSB-HSV.
        float[] hsb = ColorUtils.RGBToHSB(((MainActivity) getActivity()).getRedColor(),
                ((MainActivity) getActivity()).getGreenColor(), ((MainActivity) getActivity()).getBlueColor());

        // Set HSB-HSV single channel value.
        tvHSB_H.setText(String.format("%.0f", hsb[0]));
        tvHSB_S.setText(String.format("%.0f%%", (hsb[1] * 100.0f))); // % value.
        tvHSB_B.setText(String.format("%.0f%%", (hsb[2] * 100.0f))); // % value.
    }

    private void setRGBOValuesClickListener() {
        tvRGB_R.setOnClickListener(RGBAClickListener);
        tvRGB_G.setOnClickListener(RGBAClickListener);
        tvRGB_B.setOnClickListener(RGBAClickListener);
        tvRGB_O.setOnClickListener(RGBAClickListener);
    }
}
