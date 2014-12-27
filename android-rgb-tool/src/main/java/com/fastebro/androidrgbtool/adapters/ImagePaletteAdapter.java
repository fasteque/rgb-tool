package com.fastebro.androidrgbtool.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.model.PaletteSwatch;

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
        View gridViewItem;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null) {
            gridViewItem = inflater.inflate(R.layout.palette_grid_view_item, null);
            View swatch = gridViewItem.findViewById(R.id.palette_item_color);
            swatch.setBackgroundColor(swatches.get(position).getRgb());
        } else {
            gridViewItem = convertView;
        }

        return gridViewItem;
    }
}
