package com.fastebro.androidrgbtool.rgb;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.model.events.ErrorMessageEvent;
import com.fastebro.androidrgbtool.model.events.UpdateHexValueEvent;
import com.fastebro.androidrgbtool.widgets.SweetInputDialog;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by danielealtomare on 26/03/14.
 * Project: rgb-tool
 */
public class HexInsertionFragment extends BottomSheetDialogFragment {

    private static final String ARG_HEX_VALUE = "HEX_VALUE";

    public HexInsertionFragment() { }

    private String hexValue;

    public static HexInsertionFragment newInstance(String hexValue) {
        HexInsertionFragment fragment = new HexInsertionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HEX_VALUE, hexValue);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            hexValue = getArguments().getString(ARG_HEX_VALUE, "000000");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SweetInputDialog builder = new SweetInputDialog(getActivity());

        builder.setTitle(R.string.hex_insertion_title);
        builder.setPositiveButton(R.string.action_common_set, v -> {
                            if(checkHexValue(builder.getInputString())) {
                                EventBus.getDefault().post(
                                        new UpdateHexValueEvent(builder.getInputString()));
                            } else if (!checkHexValue(builder.getInputString()) && builder.getInputString().length() == 6){
                                EventBus.getDefault().post(
                                        new UpdateHexValueEvent("FF" + builder.getInputString()));
                            } else {
                                EventBus.getDefault().post(
                                        new ErrorMessageEvent(getString(R.string.hex_insertion_not_valid_error)));
                            }
                            HexInsertionFragment.this.getDialog().cancel();
                        }
                );
        builder.setNegativeButton(R.string.action_common_cancel, v -> HexInsertionFragment.this.getDialog().cancel()
                );
        builder.setHint(R.string.hex_insertion_message_hint);
        builder.setCharactersToUse("1234567890ABCDEFabcdef");
        builder.setInputString(hexValue);
        builder.setMaxLines(1);

        return builder;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private boolean checkHexValue(String hexValue) {
        if("".equals(hexValue) || hexValue.length() != 8) {
            return false;
        } else {
            try {
                //noinspection ResultOfMethodCallIgnored
                Long.parseLong(hexValue, 16);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
    }
}
