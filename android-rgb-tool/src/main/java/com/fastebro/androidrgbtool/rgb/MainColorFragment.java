package com.fastebro.androidrgbtool.rgb;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.commons.EventBaseFragment;
import com.fastebro.androidrgbtool.model.events.RGBAInsertionEvent;
import com.fastebro.androidrgbtool.model.events.UpdateHexValueEvent;
import com.fastebro.androidrgbtool.utils.ColorUtils;

import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by danielealtomare on 16/04/17.
 * Project: rgb-tool
 */

public class MainColorFragment extends EventBaseFragment {

    // Save color button.
//    @BindView(R.id.fab_save_color)
//    FloatingActionButton btn_SaveColor;
    @BindView(R.id.color_view)
    View colorView;
    @BindView(R.id.main_picker_image)
    ImageView pickerImage;

    // Hexadecimal color value.
    @BindView(R.id.tv_hexadecimal)
    TextView tvHexadecimal;

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

            tvHexadecimal.setOnClickListener(v -> {
                if (isAdded()) {
                    HexInsertionFragment fragment =
                            HexInsertionFragment.newInstance(tvHexadecimal.getText().toString().substring(1));
                    fragment.show(getActivity().getSupportFragmentManager(), null);
                }
            });

            refreshUI();
        }
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
        updateHexadecimalField();
//        updateSharedColor();
        updateSaveColorButton();
//        updateColorDetails();
//        updateColorSample();
        colorView.setBackgroundColor(Color.argb(((MainActivity) getActivity()).getOpacity(),
                ((MainActivity) getActivity()).getRedColor(),
                ((MainActivity) getActivity()).getGreenColor(),
                ((MainActivity) getActivity()).getBlueColor()));
    }

    private void updateHexadecimalField() {
        String hexValue = String.format("#%s%s%s%s", ColorUtils.RGBToHex(((MainActivity) getActivity()).getOpacity()),
                ColorUtils.RGBToHex(((MainActivity) getActivity()).getRedColor()),
                ColorUtils.RGBToHex(((MainActivity) getActivity()).getGreenColor()),
                ColorUtils.RGBToHex(((MainActivity) getActivity()).getBlueColor()));

        tvHexadecimal.setText(hexValue);
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
                if (seekBar.equals(seekBarRed)) {
                    ((MainActivity) getActivity()).setRedColor(progress);

                    thumbRect = seekBar.getThumb().getBounds();

                    redToolTip.setX((seekBarLeft / 2) + thumbRect.left);

                    if (progress < 10) {
                        redToolTip.setText("  " + ((MainActivity) getActivity()).getRedColor());
                    } else if (progress < 100) {
                        redToolTip.setText(" " + ((MainActivity) getActivity()).getRedColor());
                    } else {
                        redToolTip.setText(((MainActivity) getActivity()).getRedColor() + "");
                    }
                }

                if (seekBar.equals(seekBarGreen)) {
                    ((MainActivity) getActivity()).setGreenColor(progress);

                    thumbRect = seekBar.getThumb().getBounds();

                    greenToolTip.setX((seekBarLeft / 2) + thumbRect.left);
                    if (progress < 10) {
                        greenToolTip.setText("  " + ((MainActivity) getActivity()).getGreenColor());
                    } else if (progress < 100) {
                        greenToolTip.setText(" " + ((MainActivity) getActivity()).getGreenColor());
                    } else {
                        greenToolTip.setText(((MainActivity) getActivity()).getGreenColor() + "");
                    }
                }

                if (seekBar.equals(seekBarBlue)) {
                    ((MainActivity) getActivity()).setBlueColor(progress);

                    thumbRect = seekBar.getThumb().getBounds();

                    blueToolTip.setX((seekBarLeft / 2) + thumbRect.left);
                    if (progress < 10) {
                        blueToolTip.setText("  " + ((MainActivity) getActivity()).getBlueColor());
                    } else if (progress < 100) {
                        blueToolTip.setText(" " + ((MainActivity) getActivity()).getBlueColor());
                    } else {
                        blueToolTip.setText(((MainActivity) getActivity()).getBlueColor() + "");
                    }
                }

                if (seekBar.equals(seekBarOpacity)) {
                    ((MainActivity) getActivity()).setOpacity(progress);

                    thumbRect = seekBar.getThumb().getBounds();

                    opacityToolTip.setX((seekBarLeft / 2) + thumbRect.left);
                    if (progress < 10) {
                        opacityToolTip.setText("  " + ((MainActivity) getActivity()).getOpacity());
                    } else if (progress < 100) {
                        opacityToolTip.setText(" " + ((MainActivity) getActivity()).getOpacity());
                    } else {
                        opacityToolTip.setText(((MainActivity) getActivity()).getOpacity() + "");
                    }
                }

                refreshUI();
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

    // Events management
    @Subscribe
    public void onUpdateHexValueEvent(UpdateHexValueEvent event) {
        if (isAdded()) {
            int[] rgb = ColorUtils.hexToARGB(event.hexValue);
            ((MainActivity) getActivity()).setOpacity(rgb[0]);
            ((MainActivity) getActivity()).setRedColor(rgb[1]);
            ((MainActivity) getActivity()).setGreenColor(rgb[2]);
            ((MainActivity) getActivity()).setBlueColor(rgb[3]);
            seekBarOpacity.setProgress(rgb[0]);
            seekBarRed.setProgress(rgb[1]);
            seekBarGreen.setProgress(rgb[2]);
            seekBarBlue.setProgress(rgb[3]);
            refreshUI();
            ((MainActivity) getActivity()).savePreferences();
        }
    }

    @Subscribe
    public void onRGBAInsertionEvent(RGBAInsertionEvent event) {
        if (isAdded()) {
            ((MainActivity) getActivity()).setRedColor(event.rgbaValues[0]);
            ((MainActivity) getActivity()).setGreenColor(event.rgbaValues[1]);
            ((MainActivity) getActivity()).setBlueColor(event.rgbaValues[2]);
            ((MainActivity) getActivity()).setOpacity(event.rgbaValues[3]);
            seekBarRed.setProgress(event.rgbaValues[0]);
            seekBarGreen.setProgress(event.rgbaValues[1]);
            seekBarBlue.setProgress(event.rgbaValues[2]);
            seekBarOpacity.setProgress(event.rgbaValues[3]);
            refreshUI();
            ((MainActivity) getActivity()).savePreferences();
        }
    }
}
