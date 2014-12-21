package com.fastebro.androidrgbtool.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.fastebro.androidrgbtool.R;

/**
 * Created by danielealtomare on 26/03/14.
 */
public class PrintColorDialogFragment extends DialogFragment {
    public PrintColorDialogFragment() { }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     */
    public interface PrintColorDialogListener {
        public void onDialogPositiveClick(String message);
        public void onDialogNegativeClick();
    }

    // Use this instance of the interface to deliver action events
    private PrintColorDialogListener mListener;
    private EditText mMessage;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the PrintColorDialogListener so we can send events to the host
            mListener = (PrintColorDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement PrintColorDialogListener");
        }
    }

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
                                mListener.onDialogPositiveClick(mMessage.getText().toString());
                                PrintColorDialogFragment.this.getDialog().cancel();
                            }
                        }
                )
                .setNegativeButton(getString(R.string.print_color_dialog_message_skip),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mListener.onDialogNegativeClick();
                                PrintColorDialogFragment.this.getDialog().cancel();
                            }
                        }
                );

        return builder.create();
    }
}
