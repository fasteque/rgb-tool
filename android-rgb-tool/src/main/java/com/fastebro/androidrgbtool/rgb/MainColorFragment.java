package com.fastebro.androidrgbtool.rgb;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.commons.EventBaseFragment;
import com.fastebro.androidrgbtool.model.events.RGBAInsertionEvent;
import com.fastebro.androidrgbtool.model.events.UpdateHexValueEvent;
import com.fastebro.androidrgbtool.utils.ClipboardUtils;
import com.fastebro.androidrgbtool.utils.ColorUtils;

import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

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

	// Copy Hexadecimal color value.
	@BindView(R.id.hexadecimalCopy)
	ImageButton buttonCopyHexadecimal;

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

			seekBarRed.setProgress(getMainActivity().getRedColor());
			seekBarGreen.setProgress(getMainActivity().getGreenColor());
			seekBarBlue.setProgress(getMainActivity().getBlueColor());
			seekBarOpacity.setProgress(getMainActivity().getOpacity());
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
			buttonCopyHexadecimal.setOnClickListener(v -> {
				final String colorText = tvHexadecimal.getText().toString().substring(1);
				ClipboardUtils.copyToClipboard(colorText);
				Snackbar.make(v, String.format("%s %s", colorText, getString(R.string.clipboard)), Snackbar
						.LENGTH_SHORT).show();
			});
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
		colorView.setBackgroundColor(Color.argb(getMainActivity().getOpacity(),
				getMainActivity().getRedColor(),
				getMainActivity().getGreenColor(),
				getMainActivity().getBlueColor()));
	}

	private void updateHexadecimalField() {
		String hexValue = String.format("#%s%s%s%s", ColorUtils.RGBToHex(getMainActivity().getOpacity()),
				ColorUtils.RGBToHex(getMainActivity().getRedColor()),
				ColorUtils.RGBToHex(getMainActivity().getGreenColor()),
				ColorUtils.RGBToHex(getMainActivity().getBlueColor()));

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
					getMainActivity().setRedColor(progress);

					thumbRect = seekBar.getThumb().getBounds();

					redToolTip.setX((seekBarLeft / 2) + thumbRect.left);

					if (progress < 10) {
						redToolTip.setText(String.format(Locale.ENGLISH, "  %d", getMainActivity().getRedColor()));
					} else if (progress < 100) {
						redToolTip.setText(String.format(Locale.ENGLISH, " %d", getMainActivity().getRedColor()));
					} else {
						redToolTip.setText(String.format(Locale.ENGLISH, "%d", getMainActivity().getRedColor()));
					}
				}

				if (seekBar.equals(seekBarGreen)) {
					getMainActivity().setGreenColor(progress);

					thumbRect = seekBar.getThumb().getBounds();

					greenToolTip.setX((seekBarLeft / 2) + thumbRect.left);
					if (progress < 10) {
						greenToolTip.setText(String.format(Locale.ENGLISH, "  %d", getMainActivity().getGreenColor()));
					} else if (progress < 100) {
						greenToolTip.setText(String.format(Locale.ENGLISH, " %d", getMainActivity().getGreenColor()));
					} else {
						greenToolTip.setText(String.format(Locale.ENGLISH, "%d", getMainActivity().getGreenColor()));
					}
				}

				if (seekBar.equals(seekBarBlue)) {
					getMainActivity().setBlueColor(progress);

					thumbRect = seekBar.getThumb().getBounds();

					blueToolTip.setX((seekBarLeft / 2) + thumbRect.left);
					if (progress < 10) {
						blueToolTip.setText(String.format(Locale.ENGLISH, "  %d", getMainActivity().getBlueColor()));
					} else if (progress < 100) {
						blueToolTip.setText(String.format(Locale.ENGLISH, " %d", getMainActivity().getBlueColor()));
					} else {
						blueToolTip.setText(String.format(Locale.ENGLISH, "%d", getMainActivity().getBlueColor()));
					}
				}

				if (seekBar.equals(seekBarOpacity)) {
					getMainActivity().setOpacity(progress);

					thumbRect = seekBar.getThumb().getBounds();

					opacityToolTip.setX((seekBarLeft / 2) + thumbRect.left);
					if (progress < 10) {
						opacityToolTip.setText(String.format(Locale.ENGLISH, "  %d", getMainActivity().getOpacity()));
					} else if (progress < 100) {
						opacityToolTip.setText(String.format(Locale.ENGLISH, " %d", getMainActivity().getOpacity()));
					} else {
						opacityToolTip.setText(String.format(Locale.ENGLISH, "%d", getMainActivity().getOpacity()));
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
			int[] rgb = ColorUtils.hexToARGB(event.getHexValue());
			getMainActivity().setOpacity(rgb[0]);
			getMainActivity().setRedColor(rgb[1]);
			getMainActivity().setGreenColor(rgb[2]);
			getMainActivity().setBlueColor(rgb[3]);
			seekBarOpacity.setProgress(rgb[0]);
			seekBarRed.setProgress(rgb[1]);
			seekBarGreen.setProgress(rgb[2]);
			seekBarBlue.setProgress(rgb[3]);
			refreshUI();
			getMainActivity().savePreferences();
		}
	}

	@Subscribe
	public void onRGBAInsertionEvent(RGBAInsertionEvent event) {
		if (isAdded()) {
			getMainActivity().setRedColor(event.getRgbaValues()[0]);
			getMainActivity().setGreenColor(event.getRgbaValues()[1]);
			getMainActivity().setBlueColor(event.getRgbaValues()[2]);
			getMainActivity().setOpacity(event.getRgbaValues()[3]);
			seekBarRed.setProgress(event.getRgbaValues()[0]);
			seekBarGreen.setProgress(event.getRgbaValues()[1]);
			seekBarBlue.setProgress(event.getRgbaValues()[2]);
			seekBarOpacity.setProgress(event.getRgbaValues()[3]);
			refreshUI();
			getMainActivity().savePreferences();
		}
	}

	// Provide MainActivity instance more simplify
	public MainActivity getMainActivity() {
		return ((MainActivity) getActivity());
	}
}