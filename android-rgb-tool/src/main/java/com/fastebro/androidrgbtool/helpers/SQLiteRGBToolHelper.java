package com.fastebro.androidrgbtool.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.fastebro.androidrgbtool.contracts.ColorDataContract;

public class SQLiteRGBToolHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    /**
     * SQLite does not have a separate Boolean storage class.
     * Instead, Boolean values are stored as integers 0 (false) and 1 (true).
     */
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ColorDataContract.ColorEntry.TABLE_NAME + " (" +
                    ColorDataContract.ColorEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ColorDataContract.ColorEntry.COLUMN_COLOR_NAME + TEXT_TYPE + COMMA_SEP +
                    ColorDataContract.ColorEntry.COLUMN_COLOR_HEX + TEXT_TYPE + COMMA_SEP +
                    ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_R + INTEGER_TYPE + COMMA_SEP +
                    ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_G + INTEGER_TYPE + COMMA_SEP +
                    ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_B + INTEGER_TYPE + COMMA_SEP +
                    ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_A + INTEGER_TYPE + COMMA_SEP +
                    ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_H + INTEGER_TYPE + COMMA_SEP +
                    ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_S + INTEGER_TYPE + COMMA_SEP +
                    ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_B + INTEGER_TYPE + COMMA_SEP +
                    ColorDataContract.ColorEntry.COLUMN_COLOR_FAVORITE + INTEGER_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ColorDataContract.ColorEntry.TABLE_NAME;

    private static final String DATABASE_NAME = "rgbtool.db";
    private static final int DATABASE_VERSION = 1;

    public SQLiteRGBToolHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: save the current data

        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);

        // TODO: insert the current data
    }
}
