package com.fastebro.androidrgbtool.rgb;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;

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

    public MainColorFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_rgb_tab_picker, container, false);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
