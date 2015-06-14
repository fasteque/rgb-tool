package com.fastebro.androidrgbtool.utils;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.fastebro.androidrgbtool.contracts.ColorDataContract;
import com.fastebro.androidrgbtool.provider.RGBToolContentProvider;

/**
 * Created by danielealtomare on 17/05/14.
 * Project: rgb-tool
 */
public class DatabaseUtils {
    public static final String[] COLORS_SUMMARY_PROJECTION = new String[]
            {
                    ColorDataContract.ColorEntry._ID,
                    ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_R,
                    ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_G,
                    ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_B,
                    ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_A,
                    ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_H,
                    ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_S,
                    ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_B,
                    ColorDataContract.ColorEntry.COLUMN_COLOR_HEX,
                    ColorDataContract.ColorEntry.COLUMN_COLOR_FAVORITE,
                    ColorDataContract.ColorEntry.COLUMN_COLOR_NAME
            };

    public static boolean findColor(@NonNull Context context, float rValue,
                                    float gValue, float bValue, float aValue) {
        // Defines selection criteria for the rows to delete.
        String mSelectionClause = ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_R + "=? AND " +
                ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_G + "=? AND " +
                ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_B + "=? AND " +
                ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_A + "=?";
        String[] mSelectionArgs = {String.valueOf(rValue),
                String.valueOf(gValue),
                String.valueOf(bValue),
                String.valueOf(aValue)};

        Cursor cursor = context.getContentResolver().query(
                RGBToolContentProvider.CONTENT_URI,
                DatabaseUtils.COLORS_SUMMARY_PROJECTION,
                mSelectionClause,
                mSelectionArgs,
                null);

        boolean found = cursor.getCount() > 0;
        cursor.close();

        return found;
    }
}
