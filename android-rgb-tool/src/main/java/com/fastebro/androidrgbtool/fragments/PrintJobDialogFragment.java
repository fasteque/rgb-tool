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
import com.fastebro.android.rgbtool.model.events.PrintColorEvent;
import com.fastebro.android.rgbtool.model.events.PrintPaletteEvent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by danielealtomare on 26/03/14.
 */
public class PrintJobDialogFragment extends DialogFragment {
    @InjectView(R.id.message)
    EditText message;

    public static String ARG_JOB_TYPE = "JOB_TYPE";
    public static final int PRINT_COLOR_JOB = 0;
    public static final int PRINT_PALETTE_JOB = 1;

    public PrintJobDialogFragment() { }

    private int jobType;

    public static PrintJobDialogFragment newInstance(int jobType) {
        PrintJobDialogFragment fragment = new PrintJobDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_JOB_TYPE, jobType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            jobType = getArguments().getInt(ARG_JOB_TYPE, 0);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_print_color, null);
        ButterKnife.inject(this, view);

        String title;
        if (jobType == PRINT_COLOR_JOB) {
            title = getString(R.string.action_print);
        } else {
            title = getString(R.string.action_print_palette);
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                .setTitle(title)
                .setPositiveButton(getString(R.string.print_color_dialog_message_set),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (jobType == PRINT_COLOR_JOB) {
                                    EventBus.getDefault().post(new PrintColorEvent(message.getText().toString()));
                                } else {
                                    EventBus.getDefault().post(new PrintPaletteEvent(message.getText().toString()));
                                }
                                PrintJobDialogFragment.this.getDialog().cancel();
                            }
                        }
                )
                .setNegativeButton(getString(R.string.print_color_dialog_message_skip),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (jobType == PRINT_COLOR_JOB) {
                                    EventBus.getDefault().post(new PrintColorEvent(null));
                                } else {
                                    EventBus.getDefault().post(new PrintPaletteEvent(message.getText().toString()));
                                }
                                PrintJobDialogFragment.this.getDialog().cancel();
                            }
                        }
                );

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
