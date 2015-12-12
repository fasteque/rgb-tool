package com.fastebro.androidrgbtool.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import com.fastebro.androidrgbtool.R;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by danielealtomare on 09/06/14.
 * Project: rgb-tool
 */
public class RGBToolImagesCursorAdapter extends CursorAdapter {
    private final LayoutInflater inflater;

    public RGBToolImagesCursorAdapter(@NonNull Context context) {
        super(context, null, 0);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return getCursor() == null ? 0 : super.getCount();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View itemLayout = inflater.inflate(R.layout.grid_view_item, parent, false);

        ViewHolder holder = new ViewHolder(itemLayout);
        itemLayout.setTag(holder);

        return itemLayout;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int imageID = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        Uri imageUri = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, Integer.toString
                (imageID));

        ViewHolder holder = (ViewHolder) view.getTag();

        Picasso.with(context)
                .load(imageUri)
                .resize(256, 256)
                .centerCrop()
                .into(holder.thumbnail);
    }

    static class ViewHolder {
        @Bind(R.id.thumbnail) ImageView thumbnail;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
