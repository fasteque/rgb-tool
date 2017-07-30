package com.fastebro.androidrgbtool.rgb;


import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.utils.ClipboardUtils;
import com.fastebro.androidrgbtool.utils.ColorUtils;

import java.util.Locale;

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

    // HSL: Hue, Saturation, Lightness.
    @BindView(R.id.textView_HSL_H)
    TextView tvHSL_H;
    @BindView(R.id.textView_HSL_S)
    TextView tvHSL_S;
    @BindView(R.id.textView_HSL_L)
    TextView tvHSL_L;

    // Color details.
    @BindView(R.id.complementaryColor)
    TextView complementaryColorBg;
    @BindView(R.id.complementaryColorText)
    TextView complementaryColorText;
    @BindView(R.id.contrastColor)
    TextView contrastColorBg;
    @BindView(R.id.contrastColorText)
    TextView contrastColorText;

    //Copy buttons
    @BindView(R.id.contrastCopy)
    ImageButton buttonContrastCopy;
    @BindView(R.id.complementaryCopy)
    ImageButton buttonComplementaryCopy;

    // Color samples.
    @BindView(R.id.firstColorSampleTextNormal)
    TextView firstColorSampleTextNormal;
    @BindView(R.id.secondColorSampleTextNormal)
    TextView secondColorSampleTextNormal;
    @BindView(R.id.firstColorSampleTextNormalBg)
    TextView firstColorSampleTextNormalBg;
    @BindView(R.id.secondColorSampleTextNormalBg)
    TextView secondColorSampleTextNormalBg;

    private Unbinder unbinder;

    private final View.OnClickListener RGBAClickListener = v -> {
        if (isAdded()) {
            short[] rgbaValues = new short[]{
                    (short) getMainActivity().getRedColor(),
                    (short) getMainActivity().getGreenColor(),
                    (short) getMainActivity().getBlueColor(),
                    (short) getMainActivity().getOpacity()
            };

            RgbaInsertionFragment fragment = RgbaInsertionFragment.newInstance(rgbaValues);
            fragment.show(getActivity().getSupportFragmentManager(), null);
        }
    };

    public ColorDetailsFragment() {
    }

    // Provide MainActivity instance more simplify
    public MainActivity getMainActivity(){
        return ((MainActivity) getActivity());
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
        updateRGBValues();
        setRGBOValuesClickListener();
        setCopyClickListener();
        updateHSBValues();
        updateHSLValues();
    }



    private void updateColorDetails() {
        int redColor = getMainActivity().getRedColor();
        int blueColor = getMainActivity().getBlueColor();
        int greenColor = getMainActivity().getGreenColor();

        int complementaryColor = ColorUtils.getComplementaryColor(redColor, blueColor, greenColor);
        setRoundedBackground(complementaryColorBg, complementaryColor);
        complementaryColorText.setText(ColorUtils.RGBToHex(complementaryColor));

        int contrastColor = ColorUtils.getContrastColor(redColor, blueColor, greenColor);
        setRoundedBackground(contrastColorBg, contrastColor);
        contrastColorText.setText(ColorUtils.RGBToHex(contrastColor));
    }

    private void updateColorSample() {
        if (isAdded()) {
            int redColor = getMainActivity().getRedColor();
            int blueColor = getMainActivity().getBlueColor();
            int greenColor = getMainActivity().getGreenColor();
            int opacity = getMainActivity().getOpacity();

            // Text.
            firstColorSampleTextNormal.setTextColor(Color.argb(opacity, redColor, greenColor, blueColor));
            setRoundedBackground(firstColorSampleTextNormal, Color.WHITE);
            secondColorSampleTextNormal.setTextColor(Color.argb(opacity, redColor, greenColor, blueColor));
            setRoundedBackground(secondColorSampleTextNormal, Color.BLACK);

            // Background.
            setRoundedBackground(firstColorSampleTextNormalBg, Color.argb(opacity, redColor, greenColor, blueColor));
            setRoundedBackground(secondColorSampleTextNormalBg, Color.argb(opacity, redColor, greenColor, blueColor));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                firstColorSampleTextNormalBg.setTextColor(getResources().getColor(R.color.white, getContext()
                        .getTheme()));
                secondColorSampleTextNormalBg.setTextColor(getResources().getColor(R.color.black, getContext()
                        .getTheme()));
            } else {
                firstColorSampleTextNormalBg.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                secondColorSampleTextNormalBg.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
            }
        }
    }

    private void updateRGBValues() {
        if (isAdded()) {
            // RGB channel: R, G, B, OPACITY.
            tvRGB_R.setText(ColorUtils.getRGB(getMainActivity().getRedColor()));
            tvRGB_G.setText(ColorUtils.getRGB(getMainActivity().getGreenColor()));
            tvRGB_B.setText(ColorUtils.getRGB(getMainActivity().getBlueColor()));
            tvRGB_O.setText(ColorUtils.getRGB(getMainActivity().getOpacity()));
        }
    }

    private void setCopyClickListener() {
        final String[] colorText = new String[1];
        buttonComplementaryCopy.setOnClickListener(v -> {
            colorText[0] = complementaryColorText.getText().toString();
            ClipboardUtils.copyToClipboard(colorText[0]);
            Snackbar.make(buttonComplementaryCopy, colorText[0] + " " + getString(R.string.clipboard), Snackbar.LENGTH_SHORT).show();
        });
        buttonContrastCopy.setOnClickListener(v -> {
            colorText[0] = contrastColorText.getText().toString();
            ClipboardUtils.copyToClipboard(colorText[0]);
            Snackbar.make(buttonContrastCopy, colorText[0] + " " + getString(R.string.clipboard), Snackbar.LENGTH_SHORT).show();
        });
    }

    private void updateHSBValues() {
        if (isAdded()) {
            float[] hsb = ColorUtils.RGBToHSB(getMainActivity().getRedColor(),
                    getMainActivity().getGreenColor(), getMainActivity().getBlueColor());

            tvHSB_H.setText(String.format(Locale.ENGLISH, "%.0f", hsb[0]));
            tvHSB_S.setText(String.format(Locale.ENGLISH, "%.0f%%", (hsb[1] * 100.0f))); // % value.
            tvHSB_B.setText(String.format(Locale.ENGLISH, "%.0f%%", (hsb[2] * 100.0f))); // % value.
        }
    }

    private void updateHSLValues() {
        if (isAdded()) {
            float[] hsl = ColorUtils.RGBToHSL(getMainActivity().getRedColor(),
                    getMainActivity().getGreenColor(), getMainActivity().getBlueColor(),
                    null);

            tvHSL_H.setText(String.format(Locale.ENGLISH, "%.0f", hsl[0]));
            tvHSL_S.setText(String.format(Locale.ENGLISH, "%.0f%%", (hsl[1] * 100.0f))); // % value.
            tvHSL_L.setText(String.format(Locale.ENGLISH, "%.0f%%", (hsl[2] * 100.0f))); // % value.
        }
    }

    private void setRGBOValuesClickListener() {
        tvRGB_R.setOnClickListener(RGBAClickListener);
        tvRGB_G.setOnClickListener(RGBAClickListener);
        tvRGB_B.setOnClickListener(RGBAClickListener);
        tvRGB_O.setOnClickListener(RGBAClickListener);
    }

    private void setRoundedBackground(TextView textView, int argb) {
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius(360);
        shape.setColor(argb);

        textView.setBackground(shape);
    }
}
