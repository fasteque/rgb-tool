package com.fastebro.androidrgbtool.ui;

import android.Manifest;
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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.fastebro.android.rgbtool.model.events.ErrorMessageEvent;
import com.fastebro.android.rgbtool.model.events.UpdateHexValueEvent;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.contracts.ColorDataContract;
import com.fastebro.android.rgbtool.model.events.ColorSelectEvent;
import com.fastebro.android.rgbtool.model.events.PhotoScaledEvent;
import com.fastebro.android.rgbtool.model.events.PrintColorEvent;
import com.fastebro.android.rgbtool.model.events.RGBAInsertionEvent;
import com.fastebro.android.rgbtool.model.events.UpdateSaveColorUIEvent;
import com.fastebro.androidrgbtool.fragments.HexInsertionFragment;
import com.fastebro.androidrgbtool.fragments.RgbaInsertionFragment;
import com.fastebro.androidrgbtool.fragments.SelectPictureDialogFragment;
import com.fastebro.androidrgbtool.model.entities.ScaledPicture;
import com.fastebro.androidrgbtool.print.RGBToolPrintColorAdapter;
import com.fastebro.androidrgbtool.provider.RGBToolContentProvider;
import com.fastebro.androidrgbtool.tasks.PictureScalingManager;
import com.fastebro.androidrgbtool.utils.BaseAlbumDirFactory;
import com.fastebro.androidrgbtool.utils.ColorUtils;
import com.fastebro.androidrgbtool.utils.CommonUtils;
import com.fastebro.androidrgbtool.utils.DatabaseUtils;
import com.fastebro.androidrgbtool.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Observer;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends EventBaseActivity  implements ActivityCompat.OnRequestPermissionsResultCallback {
    @Bind(R.id.seekBar_R)
    SeekBar seekBar_R;
    @Bind(R.id.seekBar_G)
    SeekBar seekBar_G;
    @Bind(R.id.seekBar_B)
    SeekBar seekBar_B;
    @Bind(R.id.seekBar_O)
    SeekBar seekBar_O;

    // RGB channel: R,G,B.
    @Bind(R.id.textView_RGB_R)
    TextView textView_RGB_R;
    @Bind(R.id.textView_RGB_G)
    TextView textView_RGB_G;
    @Bind(R.id.textView_RGB_B)
    TextView textView_RGB_B;
    @Bind(R.id.textView_RGB_O)
    TextView textView_RGB_O;

    // HSB: Hue, Saturation, Brightness.
    @Bind(R.id.textView_HSB_H)
    TextView textView_HSB_H;
    @Bind(R.id.textView_HSB_S)
    TextView textView_HSB_S;
    @Bind(R.id.textView_HSB_B)
    TextView textView_HSB_B;

    // Hexadecimal color value.
    @Bind(R.id.textView_Hexadecimal)
    TextView textView_Hexadecimal;

    // Save color button.
    @Bind(R.id.btn_save_color)
    ImageButton btn_SaveColor;

    // Color details.
    @Bind(R.id.btn_color_details)
    ImageButton btn_ColorDetails;

    @Bind(R.id.color_view)
    View colorView;

    private String currentPhotoPath;
    private BaseAlbumDirFactory albumStorageDirFactory = null;

    private int RGB_R_COLOR = 0;
    private int RGB_G_COLOR = 0;
    private int RGB_B_COLOR = 0;
    private int RGB_OPACITY = 255;

    private static final int REQUEST_OPEN_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    protected String hexValue;

    private ShareActionProvider shareActionProvider;

    private Subscription scalePictureSubscription;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Rgbtool);
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        setContentView(R.layout.activity_main_bottom);

        // Import main layout (with SeekBar sliders).
        LayoutInflater inflater = getLayoutInflater();
        addContentView(inflater.inflate(R.layout.activity_main, null),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        );

        ButterKnife.bind(this);
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
                saveColor(RGB_R_COLOR, RGB_G_COLOR, RGB_B_COLOR, RGB_OPACITY, "");
            }
        });

        btn_ColorDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorDetails();
            }
        });

        setColorValuesClickListener();
        refreshUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
        savePreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Check if the device has a camera.
        MenuItem item = menu.findItem(R.id.action_camera);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            item.setVisible(true);
        } else {
            item.setVisible(false);
        }

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
        switch (item.getItemId()) {
            case R.id.action_camera:
                checkWriteExternalStoragePermissions();
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

    private void checkWriteExternalStoragePermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager
                .PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.rationale_external_storage),
                        Snackbar.LENGTH_INDEFINITE).setAction(getString(android.R.string.ok), new View
                        .OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            showSelectPictureDialog();
        }
    }

    private void showSelectPictureDialog() {
        SelectPictureDialogFragment dialogFragment = new SelectPictureDialogFragment();
        dialogFragment.setCancelable(true);
        dialogFragment.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(findViewById(android.R.id.content), R.string.permissions_granted, Snackbar
                        .LENGTH_SHORT).show();
            } else {
                Snackbar.make(findViewById(android.R.id.content), R.string.permissions_not_granted, Snackbar
                        .LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setColorValuesClickListener() {
        textView_RGB_R.setOnClickListener(RGBAClickListener);
        textView_RGB_G.setOnClickListener(RGBAClickListener);
        textView_RGB_B.setOnClickListener(RGBAClickListener);
        textView_RGB_O.setOnClickListener(RGBAClickListener);
        textView_Hexadecimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HexInsertionFragment fragment =
                        HexInsertionFragment.newInstance(textView_Hexadecimal.getText().toString().substring(3));
                fragment.show(getSupportFragmentManager(), null);
            }
        });
    }

    private View.OnClickListener RGBAClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            short[] rgbaValues = new short[]{(short) RGB_R_COLOR, (short) RGB_G_COLOR, (short) RGB_B_COLOR, (short)
                    RGB_OPACITY
            };

            RgbaInsertionFragment fragment = RgbaInsertionFragment.newInstance(rgbaValues);
            fragment.show(getSupportFragmentManager(), null);
        }
    };

    private void showColorList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection unchecked
            startActivity(new Intent(this, ColorListActivity.class), ActivityOptions
                    .makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivity(new Intent(this, ColorListActivity.class));
        }
    }

    private void showAbout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection unchecked
            startActivity(new Intent(this, AboutActivity.class), ActivityOptions
                    .makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivity(new Intent(this, AboutActivity.class));
        }
    }

    private void showColorDetails() {
        Intent colorDetailsIntent =  new Intent(MainActivity.this, ColorDetailsActivity.class);
        short[] argbValues = new short[4];
        argbValues[0] = (short) RGB_OPACITY;
        argbValues[1] = (short) RGB_R_COLOR;
        argbValues[2] = (short) RGB_G_COLOR;
        argbValues[3] = (short) RGB_B_COLOR;
        colorDetailsIntent.putExtra(ColorDetailsActivity.INTENT_EXTRA_RGB_COLOR, argbValues);
        startActivity(colorDetailsIntent);
    }

    private void saveColor(int RGBRComponent, int RGBGComponent, int RGBBComponent, int RGBOComponent, String
            colorName) {
        AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) { };

        float[] hsb = ColorUtils.RGBToHSB(RGBRComponent, RGBGComponent, RGBBComponent);

        ContentValues values = new ContentValues();
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_NAME, colorName);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_HEX, String.format("#%s%s%s%s",
                ColorUtils.RGBToHex(RGB_OPACITY),
                ColorUtils.RGBToHex(RGB_R_COLOR),
                ColorUtils.RGBToHex(RGB_G_COLOR),
                ColorUtils.RGBToHex(RGB_B_COLOR)));
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_R, RGBRComponent);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_G, RGBGComponent);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_B, RGBBComponent);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_A, RGBOComponent);
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
        String jobName = getString(R.string.app_name) + "_Color";

        // Start a print job, passing in a PrintDocumentAdapter implementation
        printManager.print(jobName, new RGBToolPrintColorAdapter(this, message, RGB_R_COLOR, RGB_G_COLOR,
                RGB_B_COLOR, RGB_OPACITY), null);
    }

    private void updateSharedColor() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, ColorUtils.getColorMessage(RGB_R_COLOR, RGB_G_COLOR, RGB_B_COLOR,
                RGB_OPACITY));
        shareIntent.setType("text/plain");
        setShareIntent(shareIntent);
    }

    private void setShareIntent(Intent shareIntent) {
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }
    }

    public void openDeviceGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        startActivityForResult(galleryIntent, REQUEST_OPEN_GALLERY);
    }

    public void openRGBToolGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection unchecked
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
                    currentPhotoPath = getRealPathFromURI(data.getData());
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
        if (DatabaseUtils.findColor(MainActivity.this, RGB_R_COLOR, RGB_G_COLOR, RGB_B_COLOR, RGB_OPACITY)) {
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

        colorView.setBackgroundColor(Color.argb(RGB_OPACITY, RGB_R_COLOR, RGB_G_COLOR, RGB_B_COLOR));
    }

    protected void updateRGBField() {
        // RGB channel: R, G, B, OPACITY.
        textView_RGB_R.setText(ColorUtils.getRGB(RGB_R_COLOR));
        textView_RGB_G.setText(ColorUtils.getRGB(RGB_G_COLOR));
        textView_RGB_B.setText(ColorUtils.getRGB(RGB_B_COLOR));
        textView_RGB_O.setText(ColorUtils.getRGB(RGB_OPACITY));
    }

    /**
     * Update HSB values.
     */
    protected void updateHSBField() {
        // Get float array with 3 values for HSB-HSV.
        float[] hsb = ColorUtils.RGBToHSB(RGB_R_COLOR, RGB_G_COLOR, RGB_B_COLOR);

        // Set HSB-HSV single channel value.
        textView_HSB_H.setText(String.format("%.0f", hsb[0]));
        textView_HSB_S.setText(String.format("%.0f%%", (hsb[1] * 100.0f))); // % value.
        textView_HSB_B.setText(String.format("%.0f%%", (hsb[2] * 100.0f))); // % value.
    }

    /**
     * Update hex field.
     */
    protected void updateHexadecimalField() {
        hexValue = String.format("#%s%s%s%s", ColorUtils.RGBToHex(RGB_OPACITY), ColorUtils.RGBToHex(RGB_R_COLOR),
                ColorUtils.RGBToHex(RGB_G_COLOR), ColorUtils.RGBToHex(RGB_B_COLOR));

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

        String imageFileName = ImageUtils.JPEG_FILE_PREFIX + timeStamp + "_";

        File image = File.createTempFile(imageFileName, ImageUtils.JPEG_FILE_SUFFIX, getAlbumDir());
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
                        return null;
                    }
                }
            }
        }

        return storageDir;
    }

    private void handlePhoto(boolean useTempFile) {
        String destinationPath;

        if (currentPhotoPath != null && (ContextCompat.checkSelfPermission(this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            if (useTempFile) {
                destinationPath = getFilesDir() + new File(currentPhotoPath).getName();
            } else {
                destinationPath = currentPhotoPath;
            }

            scalePictureSubscription = AppObservable.bindActivity(this,
                    PictureScalingManager.scalePictureObservable(currentPhotoPath, destinationPath))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ScaledPicture>() {
                        @Override
                        public void onCompleted() {
                            // Nothing to do.
                        }

                        @Override
                        public void onError(Throwable e) {
                            // Nothing to do.
                        }

                        @Override
                        public void onNext(ScaledPicture scaledPicture) {
                            EventBus.getDefault().post(new PhotoScaledEvent(scaledPicture.getPicturePath(),
                                    scaledPicture.isTempFile()));
                        }
                    });
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.error_open_gallery_image), Snackbar.LENGTH_SHORT).show();
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    private void updateRGBColor(int RGBRComponent, int RGBGComponent, int RGBBComponent, int RGBOComponent) {
        RGB_R_COLOR = RGBRComponent;
        RGB_G_COLOR = RGBGComponent;
        RGB_B_COLOR = RGBBComponent;
        RGB_OPACITY = RGBOComponent;
    }

    private void restorePreferences() {
        SharedPreferences settings = getSharedPreferences(CommonUtils.PREFS_NAME, 0);

        updateRGBColor(settings.getInt(CommonUtils.PREFS_R_COLOR, 0),
                settings.getInt(CommonUtils.PREFS_G_COLOR, 0),
                settings.getInt(CommonUtils.PREFS_B_COLOR, 0),
                settings.getInt(CommonUtils.PREFS_OPACITY, 255));
    }

    private void savePreferences() {
        SharedPreferences settings = getSharedPreferences(CommonUtils.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(CommonUtils.PREFS_R_COLOR, RGB_R_COLOR);
        editor.putInt(CommonUtils.PREFS_G_COLOR, RGB_G_COLOR);
        editor.putInt(CommonUtils.PREFS_B_COLOR, RGB_B_COLOR);
        editor.putInt(CommonUtils.PREFS_OPACITY, RGB_OPACITY);
        editor.apply();
    }

    public void onEvent(ColorSelectEvent event) {
        updateRGBColor(event.RGBRComponent,
                event.RGBGComponent,
                event.RGBBComponent,
                event.RGBOComponent);

        refreshUI();

        // Also update the seek bars.
        seekBar_R.setProgress(event.RGBRComponent);
        seekBar_G.setProgress(event.RGBGComponent);
        seekBar_B.setProgress(event.RGBBComponent);
        seekBar_O.setProgress(event.RGBOComponent);

        savePreferences();
    }

    public void onEvent(PhotoScaledEvent event) {
        /**
         * Tell the media scanner about the new file so that it is
         * immediately available to the user.
         */
        MediaScannerConnection.scanFile(getApplicationContext(),
                new String[]{event.photoPath}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });

        Intent colorPickerIntent = new Intent(this, ColorPickerActivity.class);
        colorPickerIntent.putExtra(ImageUtils.EXTRA_JPEG_FILE_PATH, event.photoPath);
        colorPickerIntent.putExtra(ImageUtils.EXTRA_DELETE_FILE, event.deleteFile);
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

    public void onEvent(UpdateHexValueEvent event) {
        int[] rgb = ColorUtils.hexToRGB(event.hexValue);
        RGB_R_COLOR = rgb[0];
        RGB_G_COLOR = rgb[1];
        RGB_B_COLOR = rgb[2];
        seekBar_R.setProgress(rgb[0]);
        seekBar_G.setProgress(rgb[1]);
        seekBar_B.setProgress(rgb[2]);
        refreshUI();
        savePreferences();
    }

    public void onEvent(ErrorMessageEvent event) {
        Snackbar.make(findViewById(android.R.id.content), event.message, Snackbar.LENGTH_SHORT).show();
    }
}
