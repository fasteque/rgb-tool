package com.fastebro.androidrgbtool.palette;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.RGBToolApplication;
import com.fastebro.androidrgbtool.utils.ClipboardUtils;
import com.fastebro.androidrgbtool.utils.PaletteUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by danielealtomare on 27/12/14.
 * Project: rgb-tool
 */
class ImagePaletteAdapter extends BaseAdapter {
	private final Context context;
	private final ArrayList<PaletteSwatch> swatches;

	public ImagePaletteAdapter(@NonNull Context context, @NonNull ArrayList<PaletteSwatch> swatches) {
		this.context = context;
		this.swatches = swatches;
	}

	@Override
	public int getCount() {
		return swatches.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.palette_grid_view_item, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.color.setBackgroundColor(swatches.get(position).getRgb());
		holder.rgb.setText(String.format("#%s", Integer.toHexString(swatches.get(position).getRgb()).toUpperCase()));
		holder.type.setText(PaletteUtils.getSwatchDescription(swatches.get(position).getType()));
		holder.copy.setOnClickListener(view -> {
			final String colorText = Integer.toHexString(swatches.get(position).getRgb()).toUpperCase();
			ClipboardUtils.copyToClipboard(colorText);
			Snackbar.make(view, colorText + " " + RGBToolApplication.getCtx().getString(R.string.clipboard), Snackbar
					.LENGTH_SHORT).show();
		});
		return convertView;
	}

	static class ViewHolder {
		@BindView(R.id.palette_item_color)
		View color;
		@BindView(R.id.palette_item_rgb)
		TextView rgb;
		@BindView(R.id.palette_item_type)
		TextView type;
		@BindView(R.id.hexadecimalCopy)
		ImageButton copy;

		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}
}
