package com.fastebro.androidrgbtool.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.utils.UColor;

public class RGBPanelData extends LinearLayout {
    private ImageButton mDismissPanelButton;
    private TextView mRGBValue;
    private TextView mHSBValue;
    private TextView mHEXValue;

    int alpha;
    int red;
    int green;
    int blue;
    float[] hsb;


    public RGBPanelData(Context context) {
        super(context);
        setupPanel(context);
    }


    public RGBPanelData(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupPanel(context);
    }


    public RGBPanelData(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupPanel(context);
    }


    private void setupPanel(final Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.rgb_data_panel_small, this);

        mDismissPanelButton = (ImageButton) findViewById(R.id.btn_dismiss_panel);
        mDismissPanelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(GONE);
            }
        });

        mRGBValue = (TextView) findViewById(R.id.rgb_value);
        mHSBValue = (TextView) findViewById(R.id.hsb_value);
        mHEXValue = (TextView) findViewById(R.id.hex_value);
    }


    public void updateData(int touchedRGB) {
        alpha = (touchedRGB >> 24) & 0xFF;
        red = (touchedRGB >> 16) & 0xFF;
        green = (touchedRGB >> 8) & 0xFF;
        blue = touchedRGB & 0xFF;
        hsb = UColor.RGBToHSB(red, green, blue);

        setRGBValue();
        setHSBValue();
        setHEXValue(touchedRGB);
    }


    public void setRGBValue() {
        if (mRGBValue != null) {
            mRGBValue.setText("(" + alpha + ", " + red + ", " + green + ", " + blue + ")");
        }
    }


    public void setHSBValue() {
        if (mHSBValue != null) {
            mHSBValue.setText("");
            mHSBValue.append("(" + String.format("%.0f", hsb[0]));
            mHSBValue.append(", " + String.format("%.0f%%", (hsb[1] * 100.0f)));
            mHSBValue.append(", " + String.format("%.0f%%", (hsb[2] * 100.0f)) + ")");
        }
    }


    public void setHEXValue(int touchedRGB) {
        if (mHEXValue != null) {
            mHEXValue.setText("#" + Integer.toHexString(touchedRGB));
        }
    }
}
