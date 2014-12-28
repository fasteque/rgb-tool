package com.fastebro.androidrgbtool.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.model.PaletteSwatch;
import com.fastebro.androidrgbtool.utils.UPalette;

import java.util.ArrayList;

/**
 * Created by danielealtomare on 27/12/14.
 */
public class ImagePaletteAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<PaletteSwatch> swatches;

    public ImagePaletteAdapter(Context context, ArrayList<PaletteSwatch> swatches) {
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

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.palette_grid_view_item, null);
            holder = new ViewHolder();
            holder.color = convertView.findViewById(R.id.palette_item_color);
            holder.rgb = (TextView) convertView.findViewById(R.id.palette_item_rgb);
            holder.type = (TextView) convertView.findViewById(R.id.palette_item_type);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.color.setBackgroundColor(swatches.get(position).getRgb());
        holder.rgb.setText("#" + Integer.toHexString(swatches.get(position).getRgb()).toUpperCase());
        holder.type.setText(UPalette.getSwatchDescription(context, swatches.get(position).getType()));

        return convertView;
    }

    private class ViewHolder {
        View color;
        TextView rgb;
        TextView type;
    }
}
