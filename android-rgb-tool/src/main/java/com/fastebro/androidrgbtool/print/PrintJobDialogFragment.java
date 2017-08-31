package com.fastebro.androidrgbtool.print;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.model.events.PrintColorDetailsEvent;
import com.fastebro.androidrgbtool.model.events.PrintColorEvent;
import com.fastebro.androidrgbtool.model.events.PrintPaletteEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by danielealtomare on 26/03/14.
 * Project: rgb-tool
 */
public class PrintJobDialogFragment extends BottomSheetDialogFragment {
	public static final int PRINT_COLOR_JOB = 0;
	public static final int PRINT_PALETTE_JOB = 1;
	public static final int PRINT_COLOR_DETAILS_JOB = 2;
	private static final String ARG_JOB_TYPE = "JOB_TYPE";
	@BindView(R.id.message)
	EditText message;
	private Unbinder unbinder;
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

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_print_color, null);
		unbinder = ButterKnife.bind(this, view);

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
				.setPositiveButton(getString(R.string.action_common_set),
						(dialog, id) -> {
							if (jobType == PRINT_COLOR_JOB) {
								EventBus.getDefault().post(new PrintColorEvent(message.getText().toString()));
							} else if (jobType == PRINT_COLOR_DETAILS_JOB) {
								EventBus.getDefault().post(new PrintColorDetailsEvent(message.getText().toString
										()));
							} else {
								EventBus.getDefault().post(new PrintPaletteEvent(message.getText().toString()));
							}
							PrintJobDialogFragment.this.getDialog().cancel();
						}
				)
				.setNegativeButton(getString(R.string.action_common_skip),
						(dialog, id) -> {
							if (jobType == PRINT_COLOR_JOB) {
								EventBus.getDefault().post(new PrintColorEvent(null));
							} else if (jobType == PRINT_COLOR_DETAILS_JOB) {
								EventBus.getDefault().post(new PrintColorDetailsEvent(null));
							} else {
								EventBus.getDefault().post(new PrintPaletteEvent(message.getText().toString()));
							}
							PrintJobDialogFragment.this.getDialog().cancel();
						}
				);

		return builder.create();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (unbinder != null) {
			unbinder.unbind();
		}
	}

	@IntDef({PRINT_COLOR_JOB, PRINT_PALETTE_JOB, PRINT_COLOR_DETAILS_JOB})
	public @interface JobType {
	}
}
