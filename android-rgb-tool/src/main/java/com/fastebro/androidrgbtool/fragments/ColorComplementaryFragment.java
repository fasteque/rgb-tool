package com.fastebro.androidrgbtool.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.ui.ColorDetailsActivity;
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

    private ShareActionProvider shareActionProvider;
    private int complementaryColor;
    private int contrastColor;


    public ColorComplementaryFragment() {
    }

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
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        complementaryColor = ColorUtils.getComplementaryColor(argbValues[1], argbValues[2], argbValues[3]);
        complementaryColorText.setText(getString(R.string.color_details_complementary, ColorUtils.RGBToHex
                (complementaryColor)));

        contrastColor = ColorUtils.getContrastColor(argbValues[1], argbValues[2], argbValues[3]);
        contrastColorText.setText(getString(R.string.color_details_contrast, ColorUtils.RGBToHex
                (contrastColor)));

        complementaryColorBackground.setCardBackgroundColor(complementaryColor);
        contrastColorBackground.setCardBackgroundColor(contrastColor);

        updateSharedColor();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.complementary_color, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        updateSharedColor();

        item = menu.findItem(R.id.action_print);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            item.setVisible(false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_print:
                ((ColorDetailsActivity)getActivity()).printColor();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSharedColor() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);

        short[] argbComplementaryColorValues = new short[4];
        argbValues[0] = (short) 255;
        argbValues[1] = (short) Color.red(complementaryColor);
        argbValues[2] = (short) Color.blue(complementaryColor);
        argbValues[3] = (short) Color.green(complementaryColor);

        short[] argbContrastColorValues = new short[4];
        argbValues[0] = (short) 255;
        argbValues[1] = (short) Color.red(contrastColor);
        argbValues[2] = (short) Color.blue(contrastColor);
        argbValues[3] = (short) Color.green(contrastColor);

        shareIntent.putExtra(Intent.EXTRA_TEXT, ColorUtils.getComplementaryColorMessage(argbValues,
                argbComplementaryColorValues,
                argbContrastColorValues));
        shareIntent.setType("text/plain");
        setShareIntent(shareIntent);
    }

    private void setShareIntent(Intent shareIntent) {
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }
    }
}
