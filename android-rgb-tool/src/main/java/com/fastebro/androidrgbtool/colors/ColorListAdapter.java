package com.fastebro.androidrgbtool.colors;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.fastebro.androidrgbtool.R;
import com.fastebro.android.rgbtool.model.events.ColorDeleteEvent;
import com.fastebro.android.rgbtool.model.events.ColorShareEvent;
import com.fastebro.androidrgbtool.utils.ColorUtils;
import com.fastebro.androidrgbtool.widgets.CircleView;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by daltomare on 17/04/14.
 * Project: rgb-tool
 */
class ColorListAdapter extends SimpleCursorAdapter {

    public ColorListAdapter(@NonNull Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        super.bindView(view, context, cursor);

        CircleView color = (CircleView) view.findViewById(R.id.rgb_panel_color);
        TextView rgbValue = (TextView) view.findViewById(R.id.rgb_value);
        TextView hsbValue = (TextView) view.findViewById(R.id.hsb_value);
        ImageButton popupMenu = (ImageButton) view.findViewById(R.id.btn_popup_menu);

        final int colorId = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry._ID));

        int rgbRValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_R));
        int rgbGValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_G));
        int rgbBValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_B));
        int rgbAValue = cursor.getInt(cursor.getColumnIndex(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_A));
        float[] hsb;

        rgbValue.setText("(" + rgbAValue + ", " +
                rgbRValue + ", " + rgbGValue + ", " +
                rgbBValue + ")");

        hsb = ColorUtils.RGBToHSB(rgbRValue, rgbGValue, rgbBValue);

        hsbValue.setText("");
        hsbValue.append("(" + String.format("%.0f", hsb[0]));
        hsbValue.append(", " + String.format("%.0f%%", (hsb[1] * 100.0f)));
        hsbValue.append(", " + String.format("%.0f%%", (hsb[2] * 100.0f)) + ")");

        color.setFillColor(Color.argb(rgbAValue, rgbRValue, rgbGValue, rgbBValue));
        color.setStrokeColor(Color.argb(rgbAValue, rgbRValue, rgbGValue, rgbBValue));

        popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // We need to post a Runnable to show the popup to make sure that the PopupMenu is
                // correctly positioned. The reason being that the view may change position before the
                // PopupMenu is shown.
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        showPopupMenu(v);
                    }
                });
            }
        });

        popupMenu.setTag(colorId);
    }

    private void showPopupMenu(View view) {
        final int colorId = (int) view.getTag();

        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.color_popup, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_remove:
                        EventBus.getDefault().post(new ColorDeleteEvent(colorId));
                        return true;
                    case R.id.menu_share:
                        EventBus.getDefault().post(new ColorShareEvent(colorId));
                        return true;
                }
                return false;
            }
        });

        popup.show();
    }
}
