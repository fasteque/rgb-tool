package com.fastebro.androidrgbtool.print;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.model.events.PrintColorDetailsEvent;
import com.fastebro.androidrgbtool.model.events.PrintColorEvent;
import com.fastebro.androidrgbtool.model.events.PrintPaletteEvent;
import com.fastebro.androidrgbtool.widgets.SweetInputDialog;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by danielealtomare on 26/03/14.
 * Project: rgb-tool
 */
public class PrintJobDialogFragment extends BottomSheetDialogFragment {
    //@BindView(R.id.message)
    //EditText message;
    //private Unbinder unbinder;

    public static final int PRINT_COLOR_JOB = 0;
    public static final int PRINT_PALETTE_JOB = 1;
    public static final int PRINT_COLOR_DETAILS_JOB = 2;
    private static final String ARG_JOB_TYPE = "JOB_TYPE";
    private int jobType;

    public PrintJobDialogFragment() {
    }

    public static PrintJobDialogFragment newInstance(@JobType int jobType) {
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SweetInputDialog builder = new SweetInputDialog(getActivity());

        // Get the layout inflater
        //LayoutInflater inflater = getActivity().getLayoutInflater();
        //View view = inflater.inflate(R.layout.dialog_print_color, null);
        //unbinder = ButterKnife.bind(this, view);

        String title;
        if (jobType == PRINT_COLOR_JOB) {
            title = getString(R.string.action_print);
        } else {
            title = getString(R.string.action_print_palette);
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        //builder.setView(view)
        builder.setTitle(title);
        builder.setHint(R.string.print_color_dialog_message_hint);
        builder.setPositiveButton(R.string.action_common_set, v -> {
                    if (jobType == PRINT_COLOR_JOB) {
                        EventBus.getDefault().post(new PrintColorEvent(builder.getInputString()));
                    } else if (jobType == PRINT_COLOR_DETAILS_JOB) {
                        EventBus.getDefault().post(new PrintColorDetailsEvent(builder.getInputString()));
                    } else {
                        EventBus.getDefault().post(new PrintPaletteEvent(builder.getInputString()));
                    }
                    PrintJobDialogFragment.this.getDialog().cancel();
                }
        );
        builder.setNegativeButton(R.string.action_common_skip, v -> {
                    if (jobType == PRINT_COLOR_JOB) {
                        EventBus.getDefault().post(new PrintColorEvent(null));
                    } else if (jobType == PRINT_COLOR_DETAILS_JOB) {
                        EventBus.getDefault().post(new PrintColorDetailsEvent(null));
                    } else {
                        EventBus.getDefault().post(new PrintPaletteEvent(builder.getInputString()));
                    }
                    PrintJobDialogFragment.this.getDialog().cancel();
                }
        );

        return builder;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @IntDef({PRINT_COLOR_JOB, PRINT_PALETTE_JOB, PRINT_COLOR_DETAILS_JOB})
    public @interface JobType {
    }
}
