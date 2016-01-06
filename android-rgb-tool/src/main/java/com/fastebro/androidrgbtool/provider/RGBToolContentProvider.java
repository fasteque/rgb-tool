package com.fastebro.androidrgbtool.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.fastebro.androidrgbtool.contracts.ColorDataContract;
import com.fastebro.androidrgbtool.helpers.SQLiteRGBToolHelper;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by danielealtomare on 04/02/14.
 * Project: rgb-tool
 */
public class RGBToolContentProvider extends ContentProvider {
    private SQLiteRGBToolHelper database;

    private static final String AUTHORITY = "com.fastebro.androidrgbtool.rgbtoolprovider";
    private static final String BASE_PATH = "colors";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/colors";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/color";

    // used for the UriMatcher
    private static final int COLORS = 10;
    private static final int COLOR_ID = 20;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, COLORS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", COLOR_ID);
    }


    @Override
    public boolean onCreate() {
        database = new SQLiteRGBToolHelper(getContext());

        return false;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Use SQLiteQueryBuilder instead of query() method.
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(ColorDataContract.ColorEntry.TABLE_NAME);

        int uriType = sURIMatcher.match(uri);

        switch (uriType) {
            case COLORS:
                break;
            case COLOR_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(ColorDataContract.ColorEntry._ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();

        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);

        // Notify potential listeners.
        try {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (NullPointerException e) {
        }

        return cursor;
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);

        SQLiteDatabase sqlDB = database.getWritableDatabase();

        long id;

        if (sqlDB != null) {
            switch (uriType) {
                case COLORS:
                    id = sqlDB.insert(ColorDataContract.ColorEntry.TABLE_NAME, null, values);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }

            // Notify potential listeners.
            try {
                getContext().getContentResolver().notifyChange(uri, null);
            } catch (NullPointerException e) {
            }

            return Uri.parse(BASE_PATH + "/" + id);
        }

        return null;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);

        SQLiteDatabase sqlDB = database.getWritableDatabase();

        int rowsDeleted = 0;

        if (sqlDB != null) {
            switch (uriType) {
                case COLORS:
                    rowsDeleted = sqlDB.delete(ColorDataContract.ColorEntry.TABLE_NAME, selection,
                            selectionArgs);
                    break;
                case COLOR_ID:
                    String id = uri.getLastPathSegment();
                    if (TextUtils.isEmpty(selection)) {
                        rowsDeleted = sqlDB.delete(ColorDataContract.ColorEntry.TABLE_NAME,
                                ColorDataContract.ColorEntry._ID + "=" + id,
                                null);
                    } else {
                        rowsDeleted = sqlDB.delete(ColorDataContract.ColorEntry.TABLE_NAME,
                                ColorDataContract.ColorEntry._ID + "=" + id
                                        + " and " + selection,
                                selectionArgs
                        );
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }

            // Notify potential listeners.
            try {
                getContext().getContentResolver().notifyChange(uri, null);
            } catch (NullPointerException e) {
            }

        }

        return rowsDeleted;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);

        SQLiteDatabase sqlDB = database.getWritableDatabase();

        int rowsUpdated = 0;

        if (sqlDB != null) {
            switch (uriType) {
                case COLORS:
                    rowsUpdated = sqlDB.update(ColorDataContract.ColorEntry.TABLE_NAME,
                            values,
                            selection,
                            selectionArgs);
                    break;
                case COLOR_ID:
                    String id = uri.getLastPathSegment();
                    if (TextUtils.isEmpty(selection)) {
                        rowsUpdated = sqlDB.update(ColorDataContract.ColorEntry.TABLE_NAME,
                                values,
                                ColorDataContract.ColorEntry._ID + "=" + id,
                                null);
                    } else {
                        rowsUpdated = sqlDB.update(ColorDataContract.ColorEntry.TABLE_NAME,
                                values,
                                ColorDataContract.ColorEntry._ID + "=" + id
                                        + " and "
                                        + selection,
                                selectionArgs
                        );
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }

            // Notify potential listeners.
            try {
                getContext().getContentResolver().notifyChange(uri, null);
            } catch (NullPointerException e) {
            }

        }

        return rowsUpdated;
    }


    private void checkColumns(String[] projection) {
        String[] available =
                {
                        ColorDataContract.ColorEntry._ID,
                        ColorDataContract.ColorEntry.COLUMN_COLOR_NAME,
                        ColorDataContract.ColorEntry.COLUMN_COLOR_HEX,
                        ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_R,
                        ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_G,
                        ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_B,
                        ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_A,
                        ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_H,
                        ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_S,
                        ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_B,
                        ColorDataContract.ColorEntry.COLUMN_COLOR_FAVORITE
                };

        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<>(Arrays.asList(available));

            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

}
