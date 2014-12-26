package com.fastebro.androidrgbtool.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.events.PrintColorEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by danielealtomare on 26/03/14.
 */
public class PrintColorDialogFragment extends DialogFragment {
    public PrintColorDialogFragment() { }

    private EditText mMessage;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_print_color, null);

        mMessage = (EditText) view.findViewById(R.id.message);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                .setTitle(getString(R.string.action_print))
                        // Add action buttons
                .setPositiveButton(getString(R.string.print_color_dialog_message_set),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                EventBus.getDefault().post(new PrintColorEvent(mMessage.getText().toString()));
                                PrintColorDialogFragment.this.getDialog().cancel();
                            }
                        }
                )
                .setNegativeButton(getString(R.string.print_color_dialog_message_skip),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                EventBus.getDefault().post(new PrintColorEvent(null));
                                PrintColorDialogFragment.this.getDialog().cancel();
                            }
                        }
                );

        return builder.create();
    }
}
