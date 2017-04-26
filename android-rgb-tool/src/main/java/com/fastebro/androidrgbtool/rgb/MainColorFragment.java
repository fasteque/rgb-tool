package com.fastebro.androidrgbtool.rgb;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.colors.ColorDataContract;
import com.fastebro.androidrgbtool.colors.RGBToolContentProvider;
import com.fastebro.androidrgbtool.utils.ColorUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by danielealtomare on 16/04/17.
 * Project: rgb-tool
 */

public class MainColorFragment extends Fragment {

    // Save color button.
//    @BindView(R.id.fab_save_color)
//    FloatingActionButton btn_SaveColor;
    @BindView(R.id.color_view)
    View colorView;
    @BindView(R.id.main_picker_image)
    ImageView pickerImage;

    // Hexadecimal color value.
//    @BindView(R.id.tv_hexadecimal)
//    TextView tvHexadecimal;

    @BindView(R.id.red_seek_bar)
    SeekBar seekBarRed;
    @BindView(R.id.green_seek_bar)
    SeekBar seekBarGreen;
    @BindView(R.id.blue_seek_bar)
    SeekBar seekBarBlue;
    @BindView(R.id.opacity_seek_bar)
    SeekBar seekBarOpacity;
    @BindView(R.id.red_tool_tip)
    TextView redToolTip;
    @BindView(R.id.green_tool_tip)
    TextView greenToolTip;
    @BindView(R.id.blue_tool_tip)
    TextView blueToolTip;
    @BindView(R.id.opacity_tool_tip)
    TextView opacityToolTip;
    private Rect thumbRect;
    private int seekBarLeft;

    private Unbinder unbinder;

    public MainColorFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_rgb_tab_picker, container, false);
        unbinder = ButterKnife.bind(this, view);
        colorView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isAdded()) {
            // FIXME: restore last shown picture.
            Glide.with(this).load(R.drawable.robot).into(pickerImage);

            seekBarRed.setProgress(((MainActivity) getActivity()).getRedColor());
            seekBarGreen.setProgress(((MainActivity) getActivity()).getGreenColor());
            seekBarBlue.setProgress(((MainActivity) getActivity()).getBlueColor());
            seekBarOpacity.setProgress(((MainActivity) getActivity()).getOpacity());
            seekBarLeft = seekBarRed.getPaddingLeft();

            // Setting-up SeekBars listeners.
            seekBarRed.setOnSeekBarChangeListener(getRGB());
            seekBarGreen.setOnSeekBarChangeListener(getRGB());
            seekBarBlue.setOnSeekBarChangeListener(getRGB());
            seekBarOpacity.setOnSeekBarChangeListener(getRGB());

            // Save color currently displayed.
//        btn_SaveColor.setOnClickListener(v -> saveColor(redColor, greenColor, blueColor, opacity, ""));

            setColorValuesClickListener();
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

//    private final View.OnClickListener RGBAClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            short[] rgbaValues = new short[]{(short) redColor, (short) greenColor, (short) blueColor, (short)
//                    opacity
//            };
//
//            RgbaInsertionFragment fragment = RgbaInsertionFragment.newInstance(rgbaValues);
//            fragment.show(getSupportFragmentManager(), null);
//        }
//    };

    private void refreshUI() {
        updateRGBField();
        updateHSBField();
        updateHexadecimalField();
//        updateSharedColor();
        updateSaveColorButton();
//        updateColorDetails();
//        updateColorSample();
//        colorView.setBackgroundColor(Color.argb(opacity, redColor, greenColor, blueColor));
    }

    private void updateRGBField() {
        // RGB channel: R, G, B, OPACITY.
//        tvRGB_R.setText(ColorUtils.getRGB(redColor));
//        tvRGB_G.setText(ColorUtils.getRGB(greenColor));
//        tvRGB_B.setText(ColorUtils.getRGB(blueColor));
//        tvRGB_O.setText(ColorUtils.getRGB(opacity));
    }

    private void updateHSBField() {
        // Get float array with 3 values for HSB-HSV.
//        float[] hsb = ColorUtils.RGBToHSB(redColor, greenColor, blueColor);

        // Set HSB-HSV single channel value.
//        tvHSB_H.setText(String.format("%.0f", hsb[0]));
//        tvHSB_S.setText(String.format("%.0f%%", (hsb[1] * 100.0f))); // % value.
//        tvHSB_B.setText(String.format("%.0f%%", (hsb[2] * 100.0f))); // % value.
    }

    private void updateHexadecimalField() {
//        String hexValue = String.format("#%s%s%s%s", ColorUtils.RGBToHex(opacity), ColorUtils.RGBToHex(redColor),
//                ColorUtils.RGBToHex(greenColor), ColorUtils.RGBToHex(blueColor));

//        tvHexadecimal.setText(hexValue);
    }

    private void setColorValuesClickListener() {
//        tvRGB_R.setOnClickListener(RGBAClickListener);
//        tvRGB_G.setOnClickListener(RGBAClickListener);
//        tvRGB_B.setOnClickListener(RGBAClickListener);
//        tvRGB_O.setOnClickListener(RGBAClickListener);
//        tvHexadecimal.setOnClickListener(v -> {
//            HexInsertionFragment fragment =
//                    HexInsertionFragment.newInstance(tvHexadecimal.getText().toString().substring(3));
//            fragment.show(getSupportFragmentManager(), null);
//        });
    }

    private void saveColor(int RGBRComponent, int RGBGComponent, int RGBBComponent, int RGBOComponent, String
            colorName) {
//        AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
//        };
//
//        float[] hsb = ColorUtils.RGBToHSB(RGBRComponent, RGBGComponent, RGBBComponent);
//
//        ContentValues values = new ContentValues();
//        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_NAME, colorName);
//        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_HEX, String.format("#%s%s%s%s",
//                ColorUtils.RGBToHex(opacity),
//                ColorUtils.RGBToHex(redColor),
//                ColorUtils.RGBToHex(greenColor),
//                ColorUtils.RGBToHex(blueColor)));
//        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_R, RGBRComponent);
//        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_G, RGBGComponent);
//        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_B, RGBBComponent);
//        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_A, RGBOComponent);
//        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_H, (int) hsb[0]);
//        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_S, (int) hsb[1] * 100);
//        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_B, (int) hsb[2] * 100);
//        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_FAVORITE, 1);
//
//        handler.startInsert(-1, null, RGBToolContentProvider.CONTENT_URI, values);

//        btn_SaveColor.setVisibility(View.INVISIBLE);
    }

    private SeekBar.OnSeekBarChangeListener getRGB() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                /*
                if (seekBar.equals(seekBarRed)) {
                    redColor = progress;

                    thumbRect = seekBar.getThumb().getBounds();

                    redToolTip.setX((seekBarLeft / 2) + thumbRect.left);

                    if (progress < 10) {
                        redToolTip.setText("  " + redColor);
                    } else if (progress < 100) {
                        redToolTip.setText(" " + redColor);
                    } else {
                        redToolTip.setText(redColor + "");
                    }
                }

                if (seekBar.equals(seekBarGreen)) {
                    greenColor = progress;

                    thumbRect = seekBar.getThumb().getBounds();

                    greenToolTip.setX((seekBarLeft / 2) + thumbRect.left);
                    if (progress < 10) {
                        greenToolTip.setText("  " + greenColor);
                    } else if (progress < 100) {
                        greenToolTip.setText(" " + greenColor);
                    } else {
                        greenToolTip.setText(greenColor + "");
                    }
                }

                if (seekBar.equals(seekBarBlue)) {
                    blueColor = progress;

                    thumbRect = seekBar.getThumb().getBounds();

                    blueToolTip.setX((seekBarLeft / 2) + thumbRect.left);
                    if (progress < 10) {
                        blueToolTip.setText("  " + blueColor);
                    } else if (progress < 100) {
                        blueToolTip.setText(" " + blueColor);
                    } else {
                        blueToolTip.setText(blueColor + "");
                    }
                }

                if (seekBar.equals(seekBarOpacity)) {
                    opacity = progress;

                    thumbRect = seekBar.getThumb().getBounds();

                    opacityToolTip.setX((seekBarLeft / 2) + thumbRect.left);
                    if (progress < 10) {
                        opacityToolTip.setText("  " + opacity);
                    } else if (progress < 100) {
                        opacityToolTip.setText(" " + opacity);
                    } else {
                        opacityToolTip.setText(opacity + "");
                    }
                }

                refreshUI();
                */
            }
        };
    }

    private void updateSaveColorButton() {
//        if (DatabaseUtils.findColor(MainActivity.this, redColor, greenColor, blueColor, opacity)) {
//            btn_SaveColor.setVisibility(View.INVISIBLE);
//        } else {
//            btn_SaveColor.setVisibility(View.VISIBLE);
//        }
    }
}
