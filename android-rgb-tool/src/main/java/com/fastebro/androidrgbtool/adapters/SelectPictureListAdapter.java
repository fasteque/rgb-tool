package com.fastebro.androidrgbtool.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.fastebro.androidrgbtool.R;

/**
 * Created by danielealtomare on 21/06/14.
 */
public class SelectPictureListAdapter extends ArrayAdapter {
    private final LayoutInflater inflater;
    private String[] entries;

    public SelectPictureListAdapter(Context context,
        int resource, String[] entries) {
        super(context, resource, R.id.entry_title, entries);
        this.inflater = LayoutInflater.from(context);
        this.entries = entries;

    }

    @Override
    public int getCount() {
        return entries.length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View entry = inflater.inflate(R.layout.select_picture_row, parent, false);
        ImageView icon = (ImageView) entry.findViewById(R.id.entry_icon);
        TextView title = (TextView) entry.findViewById(R.id.entry_title);

        switch (position) {
            case 0:
                icon.setImageResource(R.drawable.ic_action_content_picture);
                title.setText(entries[0]);
                break;
            case 1:
                icon.setImageResource(R.drawable.ic_action_device_access_sd_storage);
                title.setText(entries[1]);
                break;
            case 2:
                icon.setImageResource(R.drawable.ic_action_device_access_camera);
                title.setText(entries[2]);
                break;
        }

        return entry;
    }
}
