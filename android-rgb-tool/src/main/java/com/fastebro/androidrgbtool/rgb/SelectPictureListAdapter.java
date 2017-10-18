package com.fastebro.androidrgbtool.rgb;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by danielealtomare on 21/06/14.
 * Project: rgb-tool
 */
public class SelectPictureListAdapter extends RecyclerView.Adapter<SelectPictureListAdapter.ViewHolder> {
	private final String[] entries;
	private final ItemClickListener listener;

	public SelectPictureListAdapter(@NonNull String[] entries, @NonNull ItemClickListener listener) {
		this.entries = entries;
		this.listener = listener;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View itemView = LayoutInflater.
				from(viewGroup.getContext()).
				inflate(R.layout.select_picture_row, viewGroup, false);

		return new ViewHolder(itemView, listener::onClick);
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int i) {
		switch (i) {
			case 0:
				viewHolder.icon.setImageResource(R.drawable.ic_action_content_picture);
				viewHolder.title.setText(entries[0]);
				break;
			case 1:
				viewHolder.icon.setImageResource(R.drawable.ic_action_device_access_sd_storage);
				viewHolder.title.setText(entries[1]);
				break;
			case 2:
				viewHolder.icon.setImageResource(R.drawable.ic_action_device_access_camera);
				viewHolder.title.setText(entries[2]);
				break;
		}
	}

	@Override
	public int getItemCount() {
		return entries.length;
	}


	public interface ItemClickListener {
		void onClick(View v, int position, boolean isLongClick);
	}

	static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		final ItemClickListener listener;
		@BindView(R.id.entry_icon)
		ImageView icon;
		@BindView(R.id.entry_title)
		TextView title;

		public ViewHolder(View v, ItemClickListener listener) {
			super(v);
			ButterKnife.bind(this, v);
			this.listener = listener;
			v.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			listener.onClick(v, getLayoutPosition(), false);
		}
	}
}