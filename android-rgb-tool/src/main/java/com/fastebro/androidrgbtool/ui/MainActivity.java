package com.fastebro.androidrgbtool.ui;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.contracts.ColorDataContract;
import com.fastebro.android.rgbtool.model.events.ColorSelectEvent;
import com.fastebro.android.rgbtool.model.events.PhotoScaledEvent;
import com.fastebro.android.rgbtool.model.events.PrintColorEvent;
import com.fastebro.android.rgbtool.model.events.RGBAInsertionEvent;
import com.fastebro.android.rgbtool.model.events.UpdateSaveColorUIEvent;
import com.fastebro.androidrgbtool.fragments.HexInsertionFragment;
import com.fastebro.androidrgbtool.fragments.PrintJobDialogFragment;
import com.fastebro.androidrgbtool.fragments.RgbaInsertionFragment;
import com.fastebro.androidrgbtool.fragments.SelectPictureDialogFragment;
import com.fastebro.androidrgbtool.print.RGBToolPrintColorAdapter;
import com.fastebro.androidrgbtool.provider.RGBToolContentProvider;
import com.fastebro.androidrgbtool.tasks.PhotoScalingTask;
import com.fastebro.androidrgbtool.utils.BaseAlbumDirFactory;
import com.fastebro.androidrgbtool.utils.UColor;
import com.fastebro.androidrgbtool.utils.UCommon;
import com.fastebro.androidrgbtool.utils.UDatabase;
import com.fastebro.androidrgbtool.utils.UImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends EventBaseActivity {
    @InjectView(R.id.seekBar_R)
    SeekBar seekBar_R;
    @InjectView(R.id.seekBar_G)
    SeekBar seekBar_G;
    @InjectView(R.id.seekBar_B)
    SeekBar seekBar_B;
    @InjectView(R.id.seekBar_O)
    SeekBar seekBar_O;

    // RGB channel: R,G,B.
    @InjectView(R.id.textView_RGB_R)
    TextView textView_RGB_R;
    @InjectView(R.id.textView_RGB_G)
    TextView textView_RGB_G;
    @InjectView(R.id.textView_RGB_B)
    TextView textView_RGB_B;
    @InjectView(R.id.textView_RGB_O)
    TextView textView_RGB_O;

    // HSB: Hue, Saturation, Brightness.
    @InjectView(R.id.textView_HSB_H)
    TextView textView_HSB_H;
    @InjectView(R.id.textView_HSB_S)
    TextView textView_HSB_S;
    @InjectView(R.id.textView_HSB_B)
    TextView textView_HSB_B;

    // Hexadecimal color value.
    @InjectView(R.id.textView_Hexadecimal)
    TextView textView_Hexadecimal;

    // Save color button.
    @InjectView(R.id.btn_save_color)
    ImageButton btn_SaveColor;

    @InjectView(R.id.color_view)
    View colorView;

    private String currentPhotoPath;
    private BaseAlbumDirFactory albumStorageDirFactory = null;

    private float RGB_R_COLOR = 0.0f;
    private float RGB_G_COLOR = 0.0f;
    private float RGB_B_COLOR = 0.0f;
    private float RGB_OPACITY = 255.0f;   // Default value.

    private static final int REQUEST_OPEN_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    protected String hexValue;

    private ShareActionProvider shareActionProvider;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        setContentView(R.layout.activity_main_bottom);

        // Import main layout (with SeekBar sliders).
        LayoutInflater inflater = getLayoutInflater();
        addContentView(inflater.inflate(R.layout.activity_main, null),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );

        ButterKnife.inject(this);
        colorView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        restorePreferences();
        seekBar_R.setProgress((int) RGB_R_COLOR);
        seekBar_G.setProgress((int) RGB_G_COLOR);
        seekBar_B.setProgress((int) RGB_B_COLOR);
        seekBar_O.setProgress((int) RGB_OPACITY);

        // Setting-up SeekBars listeners.
        seekBar_R.setOnSeekBarChangeListener(getRGB());
        seekBar_G.setOnSeekBarChangeListener(getRGB());
        seekBar_B.setOnSeekBarChangeListener(getRGB());
        seekBar_O.setOnSeekBarChangeListener(getRGB());

        // Save color currently displayed.
        btn_SaveColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveColor(RGB_R_COLOR,
                        RGB_G_COLOR,
                        RGB_B_COLOR,
                        RGB_OPACITY,
                        "");
            }
        });

        setColorValuesClickListener();
        refreshUI();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing()) {
            savePreferences();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Check if the device has a camera.
        MenuItem item = menu.findItem(R.id.action_camera);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            item.setVisible(true);
        else
            item.setVisible(false);

        item = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        updateSharedColor();

        item = menu.findItem(R.id.action_print);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            item.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_camera:
                SelectPictureDialogFragment dialogFragment =
                        new SelectPictureDialogFragment();
                dialogFragment.setCancelable(true);
                dialogFragment.show(getSupportFragmentManager(), null);
                return true;
            case R.id.action_color_list:
                showColorList();
                return true;
            case R.id.action_print:
                showPrintColorDialog();
                return true;
            case R.id.action_about:
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setColorValuesClickListener() {
        textView_RGB_R.setOnClickListener(RGBAClickListener);
        textView_RGB_G.setOnClickListener(RGBAClickListener);
        textView_RGB_B.setOnClickListener(RGBAClickListener);
        textView_RGB_O.setOnClickListener(RGBAClickListener);
/*
        textView_Hexadecimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HexInsertionFragment fragment =
                        HexInsertionFragment.newInstance(textView_Hexadecimal.getText().toString());
                fragment.show(getSupportFragmentManager(), null);
            }
        });
*/
    }

    private View.OnClickListener RGBAClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            short[] rgbaValues = new short[]{
                    (short) RGB_R_COLOR,
                    (short) RGB_G_COLOR,
                    (short) RGB_B_COLOR,
                    (short) RGB_OPACITY
            };

            RgbaInsertionFragment fragment = RgbaInsertionFragment.newInstance(rgbaValues);
            fragment.show(getSupportFragmentManager(), null);
        }
    };

    private void showColorList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(new Intent(this, ColorListActivity.class), ActivityOptions
                    .makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivity(new Intent(this, ColorListActivity.class));
        }
    }

    private void showPrintColorDialog() {
        DialogFragment dialog = PrintJobDialogFragment.newInstance(PrintJobDialogFragment.PRINT_COLOR_JOB);
        dialog.show(getSupportFragmentManager(), null);
    }

    private void showAbout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(new Intent(this, AboutActivity.class), ActivityOptions
                    .makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivity(new Intent(this, AboutActivity.class));
        }
    }

    private void saveColor(float RGBRComponent,
                           float RGBGComponent,
                           float RGBBComponent,
                           float RGBOComponent,
                           String colorName) {
        AsyncQueryHandler handler =
                new AsyncQueryHandler(getContentResolver()) {
                };

        float[] hsb = UColor.RGBToHSB(RGBRComponent,
                RGBGComponent,
                RGBBComponent);

        ContentValues values = new ContentValues();
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_NAME, colorName);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_HEX, String.format("#%s%s%s%s",
                UColor.RGBToHex(RGB_OPACITY),
                UColor.RGBToHex(RGB_R_COLOR),
                UColor.RGBToHex(RGB_G_COLOR),
                UColor.RGBToHex(RGB_B_COLOR)));
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_R, (int) RGBRComponent);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_G, (int) RGBGComponent);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_B, (int) RGBBComponent);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_A, (int) RGBOComponent);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_H, (int) hsb[0]);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_S, (int) hsb[1] * 100);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_B, (int) hsb[2] * 100);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_FAVORITE, 1);

        handler.startInsert(-1,
                null,
                RGBToolContentProvider.CONTENT_URI,
                values);

        btn_SaveColor.setVisibility(View.INVISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void printColor(String message) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        // Set job name, which will be displayed in the print queue
        String jobName = getString(R.string.app_name) + " Document";

        // Start a print job, passing in a PrintDocumentAdapter implementation
        // to handle the generation of a print document
        printManager.print(jobName,
                new RGBToolPrintColorAdapter(
                        this,
                        message,
                        RGB_R_COLOR,
                        RGB_G_COLOR,
                        RGB_B_COLOR,
                        RGB_OPACITY),
                null
        );
    }

    private void updateSharedColor() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                UColor.getColorMessage(RGB_R_COLOR,
                        RGB_G_COLOR,
                        RGB_B_COLOR,
                        RGB_OPACITY)
        );
        shareIntent.setType("text/plain");
        setShareIntent(shareIntent);
    }

    private void setShareIntent(Intent shareIntent) {
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }
    }

    public void openDeviceGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        startActivityForResult(galleryIntent, REQUEST_OPEN_GALLERY);
    }

    public void openRGBToolGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(new Intent(this, RGBToolGalleryActivity.class), ActivityOptions
                    .makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivity(new Intent(this, RGBToolGalleryActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OPEN_GALLERY) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    currentPhotoPath = getRealPathFromURI(this, data.getData());
                    handlePhoto(true);
                }
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                handlePhoto(false);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * @return SeekBar listener.
     */
    protected OnSeekBarChangeListener getRGB() {
        return new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar.equals(seekBar_O)) {
                    RGB_OPACITY = progress;
                }

                if (seekBar.equals(seekBar_R)) {
                    RGB_R_COLOR = progress;
                }

                if (seekBar.equals(seekBar_G)) {
                    RGB_G_COLOR = progress;
                }

                if (seekBar.equals(seekBar_B)) {
                    RGB_B_COLOR = progress;
                }

                refreshUI();
            }
        };
    }

    private void updateSaveColorButton() {
        if (UDatabase.findColor(MainActivity.this, RGB_R_COLOR, RGB_G_COLOR,
                RGB_B_COLOR, RGB_OPACITY)) {
            btn_SaveColor.setVisibility(View.INVISIBLE);
        } else {
            btn_SaveColor.setVisibility(View.VISIBLE);
        }
    }

    private void refreshUI() {
        updateRGBField();
        updateHSBField();
        updateHexadecimalField();
        updateColorName();
        updateSharedColor();
        updateSaveColorButton();

        colorView.setBackgroundColor(Color.argb((int) RGB_OPACITY, (int) RGB_R_COLOR, (int) RGB_G_COLOR,
                (int) RGB_B_COLOR));
    }

    /**
     *
     *
     */
    protected void updateRGBField() {
        // RGB channel: R, G, B, OPACITY.
        textView_RGB_R.setText(UColor.getRGB(RGB_R_COLOR));
        textView_RGB_G.setText(UColor.getRGB(RGB_G_COLOR));
        textView_RGB_B.setText(UColor.getRGB(RGB_B_COLOR));
        textView_RGB_O.setText(UColor.getRGB(RGB_OPACITY));
    }

    /**
     * Update HSB values.
     */
    protected void updateHSBField() {
        // Get float array with 3 values for HSB-HSV.
        float[] hsb = UColor.RGBToHSB(RGB_R_COLOR, RGB_G_COLOR, RGB_B_COLOR);

        // Set HSB-HSV single channel value.
        textView_HSB_H.setText(String.format("%.0f", hsb[0]));
        textView_HSB_S.setText(String.format("%.0f%%", (hsb[1] * 100.0f))); // % value.
        textView_HSB_B.setText(String.format("%.0f%%", (hsb[2] * 100.0f))); // % value.
    }

    /**
     * Update hex field.
     */
    protected void updateHexadecimalField() {
        hexValue = String.format("#%s%s%s%s",
                UColor.RGBToHex(RGB_OPACITY),
                UColor.RGBToHex(RGB_R_COLOR),
                UColor.RGBToHex(RGB_G_COLOR),
                UColor.RGBToHex(RGB_B_COLOR));

        textView_Hexadecimal.setText(hexValue);
    }

    /**
     * Update color name by color hex value.
     */
    protected void updateColorName() {

    }

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f;

        albumStorageDirFactory = new BaseAlbumDirFactory();

        try {
            f = setUpPhotoFile();
            currentPhotoPath = f.getAbsolutePath();

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            currentPhotoPath = null;
        }

        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    private File setUpPhotoFile() throws IOException {
        File f = createImageFile();
        currentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = UImage.JPEG_FILE_PREFIX + timeStamp + "_";

        File image = File.createTempFile(imageFileName, UImage.JPEG_FILE_SUFFIX, getAlbumDir());
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    // Photo album for this application
    private String getAlbumName() {
        return getString(R.string.album_name);
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = albumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d(getString(R.string.app_name), "failed to create directory");

                        return null;
                    }
                }
            }

        } else {
            Log.d(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private void handlePhoto(boolean useTempFile) {
        if (currentPhotoPath != null) {
            new PhotoScalingTask(this, currentPhotoPath, useTempFile).execute();
        } else {
            Toast.makeText(this, getString(R.string.error_open_gallery_image), Toast.LENGTH_SHORT).show();
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;

        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);

            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void updateRGBColor(float RGBRComponent,
                                float RGBGComponent,
                                float RGBBComponent,
                                float RGBOComponent) {
        RGB_R_COLOR = RGBRComponent;
        RGB_G_COLOR = RGBGComponent;
        RGB_B_COLOR = RGBBComponent;
        RGB_OPACITY = RGBOComponent;
    }

    private void restorePreferences() {
        SharedPreferences settings = getSharedPreferences(UCommon.PREFS_NAME, 0);

        updateRGBColor(settings.getFloat(UCommon.PREFS_R_COLOR, 0.0f),
                settings.getFloat(UCommon.PREFS_G_COLOR, 0.0f),
                settings.getFloat(UCommon.PREFS_B_COLOR, 0.0f),
                settings.getFloat(UCommon.PREFS_OPACITY, 255.0f));
    }

    private void savePreferences() {
        SharedPreferences settings = getSharedPreferences(UCommon.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(UCommon.PREFS_R_COLOR, RGB_R_COLOR);
        editor.putFloat(UCommon.PREFS_G_COLOR, RGB_G_COLOR);
        editor.putFloat(UCommon.PREFS_B_COLOR, RGB_B_COLOR);
        editor.putFloat(UCommon.PREFS_OPACITY, RGB_OPACITY);
        editor.apply();
    }

    public void onEvent(ColorSelectEvent event) {
        updateRGBColor(event.RGBRComponent,
                event.RGBGComponent,
                event.RGBBComponent,
                event.RGBOComponent);

        refreshUI();

        // Also update the seek bars.
        seekBar_R.setProgress((int) event.RGBRComponent);
        seekBar_G.setProgress((int) event.RGBGComponent);
        seekBar_B.setProgress((int) event.RGBBComponent);
        seekBar_O.setProgress((int) event.RGBOComponent);

        savePreferences();
    }

    public void onEvent(PhotoScaledEvent event) {
        /**
         * Tell the media scanner about the new file so that it is
         * immediately available to the user.
         */
        MediaScannerConnection.scanFile(this,
                new String[]{event.photoPath}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });

        Intent colorPickerIntent = new Intent(this, ColorPickerActivity.class);
        colorPickerIntent.putExtra(UImage.EXTRA_JPEG_FILE_PATH, event.photoPath);
        colorPickerIntent.putExtra(UImage.EXTRA_DELETE_FILE, event.deleteFile);
        startActivity(colorPickerIntent);
    }

    public void onEvent(PrintColorEvent event) {
        printColor(event.message);
    }

    public void onEvent(UpdateSaveColorUIEvent event) {
        updateSaveColorButton();
    }

    public void onEvent(RGBAInsertionEvent event) {
        RGB_R_COLOR = event.rgbaValues[0];
        RGB_G_COLOR = event.rgbaValues[1];
        RGB_B_COLOR = event.rgbaValues[2];
        RGB_OPACITY = event.rgbaValues[3];
        seekBar_R.setProgress(event.rgbaValues[0]);
        seekBar_G.setProgress(event.rgbaValues[1]);
        seekBar_B.setProgress(event.rgbaValues[2]);
        seekBar_O.setProgress(event.rgbaValues[3]);
        refreshUI();
        savePreferences();
    }
}
