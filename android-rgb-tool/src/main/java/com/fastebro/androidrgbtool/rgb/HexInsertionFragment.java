package com.fastebro.androidrgbtool.rgb;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.fastebro.androidrgbtool.model.events.ErrorMessageEvent;
import com.fastebro.androidrgbtool.model.events.UpdateHexValueEvent;
import com.fastebro.androidrgbtool.R;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by danielealtomare on 26/03/14.
 * Project: rgb-tool
 */
public class HexInsertionFragment extends DialogFragment {
    @BindView(R.id.new_hex_value)
    EditText newHexValue;
    private Unbinder unbinder;

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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_hex_insertion, null);
        unbinder = ButterKnife.bind(this, view);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                .setTitle(getString(R.string.hex_insertion_title))
                .setPositiveButton(getString(R.string.action_common_set),
                        (dialog, id) -> {
                            if(checkHexValue(newHexValue.getText().toString())) {
                                EventBus.getDefault().post(
                                        new UpdateHexValueEvent(newHexValue.getText().toString()));
                            } else {
                                EventBus.getDefault().post(
                                        new ErrorMessageEvent(getString(R.string.hex_insertion_not_valid_error)));
                            }
                            HexInsertionFragment.this.getDialog().cancel();
                        }
                )
                .setNegativeButton(getString(R.string.action_common_cancel),
                        (dialog, id) -> HexInsertionFragment.this.getDialog().cancel()
                );

        newHexValue.setText(hexValue);
        newHexValue.setSelection(newHexValue.getText().length());

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
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
