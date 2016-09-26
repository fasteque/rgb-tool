package com.fastebro.androidrgbtool.rgb;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.colorpicker.ColorPickerActivity;
import com.fastebro.androidrgbtool.colors.ColorDataContract;
import com.fastebro.androidrgbtool.colors.ColorListActivity;
import com.fastebro.androidrgbtool.colors.RGBToolContentProvider;
import com.fastebro.androidrgbtool.commons.EventBaseActivity;
import com.fastebro.androidrgbtool.gallery.RGBToolGalleryActivity;
import com.fastebro.androidrgbtool.model.entities.ScaledPicture;
import com.fastebro.androidrgbtool.model.events.ColorSelectEvent;
import com.fastebro.androidrgbtool.model.events.ErrorMessageEvent;
import com.fastebro.androidrgbtool.model.events.PhotoScaledEvent;
import com.fastebro.androidrgbtool.model.events.PrintColorEvent;
import com.fastebro.androidrgbtool.model.events.RGBAInsertionEvent;
import com.fastebro.androidrgbtool.model.events.UpdateHexValueEvent;
import com.fastebro.androidrgbtool.model.events.UpdateSaveColorUIEvent;
import com.fastebro.androidrgbtool.print.PrintJobDialogFragment;
import com.fastebro.androidrgbtool.print.RGBToolPrintColorAdapter;
import com.fastebro.androidrgbtool.settings.AboutActivity;
import com.fastebro.androidrgbtool.utils.BaseAlbumDirFactory;
import com.fastebro.androidrgbtool.utils.ColorUtils;
import com.fastebro.androidrgbtool.utils.CommonUtils;
import com.fastebro.androidrgbtool.utils.DatabaseUtils;
import com.fastebro.androidrgbtool.utils.ImageUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends EventBaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    @BindView(R.id.red_seek_bar)
    SeekBar seekBarRed;
    @BindView(R.id.green_seek_bar)
    SeekBar seekBarGreen;
    @BindView(R.id.blue_seek_bar)
    SeekBar seekBarBlue;
    @BindView(R.id.opacity_seek_bar)
    SeekBar seekBarOpacity;
    @BindView(R.id.red_tool_tip)
    TextView redToolTip;
    @BindView(R.id.green_tool_tip)
    TextView greenToolTip;
    @BindView(R.id.blue_tool_tip)
    TextView blueToolTip;
    @BindView(R.id.opacity_tool_tip)
    TextView opacityToolTip;
    private Rect thumbRect;
    private int seekBarLeft;

    // RGB channel: R,G,B.
    @BindView(R.id.textView_RGB_R)
    TextView tvRGB_R;
    @BindView(R.id.textView_RGB_G)
    TextView tvRGB_G;
    @BindView(R.id.textView_RGB_B)
    TextView tvRGB_B;
    @BindView(R.id.textView_RGB_O)
    TextView tvRGB_O;

    // HSB: Hue, Saturation, Brightness.
    @BindView(R.id.textView_HSB_H)
    TextView tvHSB_H;
    @BindView(R.id.textView_HSB_S)
    TextView tvHSB_S;
    @BindView(R.id.textView_HSB_B)
    TextView tvHSB_B;

    // Hexadecimal color value.
    @BindView(R.id.tv_hexadecimal)
    TextView tvHexadecimal;

    // Save color button.
    @BindView(R.id.fab_save_color)
    FloatingActionButton btn_SaveColor;
    @BindView(R.id.color_view)
    View colorView;

    // Color details.
    @BindView(R.id.complementaryColorBackground)
    CardView complementaryColorBackground;
    @BindView(R.id.complementaryColorText)
    TextView complementaryColorText;
    @BindView(R.id.contrastColorBackground)
    CardView contrastColorBackground;
    @BindView(R.id.contrastColorText)
    TextView contrastColorText;
    private int complementaryColor;
    private int contrastColor;

    // Color samples.
    @BindView(R.id.firstColorSampleBackground)
    CardView firstColorSampleBackground;
    @BindView(R.id.firstColorSampleTextNormal)
    TextView firstColorSampleTextNormal;
    @BindView(R.id.secondColorSampleBackground)
    CardView secondColorSampleBackground;
    @BindView(R.id.secondColorSampleTextNormal)
    TextView secondColorSampleTextNormal;
    @BindView(R.id.firstColorSampleBackgroundBg)
    CardView firstColorSampleBackgroundBg;
    @BindView(R.id.firstColorSampleTextNormalBg)
    TextView firstColorSampleTextNormalBg;
    @BindView(R.id.secondColorSampleBackgroundBg)
    CardView secondColorSampleBackgroundBg;
    @BindView(R.id.secondColorSampleTextNormalBg)
    TextView secondColorSampleTextNormalBg;

    private String currentPhotoPath;
    private BaseAlbumDirFactory albumStorageDirFactory = null;

    private int redColor = 0;
    private int greenColor = 0;
    private int blueColor = 0;
    private int opacity = 255;

    private static final int REQUEST_OPEN_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private ShareActionProvider shareActionProvider;
    private Subscription scalePictureSubscription;
    private BottomSheetBehavior bottomSheetBehavior;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Rgbtool);
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        setContentView(R.layout.activity_main_rgb);

        ButterKnife.bind(this);

        View bottomSheet = findViewById(R.id.bottom_sheet_container);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        colorView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        restorePreferences();
        seekBarRed.setProgress(redColor);
        seekBarGreen.setProgress(greenColor);
        seekBarBlue.setProgress(blueColor);
        seekBarOpacity.setProgress(opacity);
        seekBarLeft = seekBarRed.getPaddingLeft();

        // Setting-up SeekBars listeners.
        seekBarRed.setOnSeekBarChangeListener(getRGB());
        seekBarGreen.setOnSeekBarChangeListener(getRGB());
        seekBarBlue.setOnSeekBarChangeListener(getRGB());
        seekBarOpacity.setOnSeekBarChangeListener(getRGB());

        // Save color currently displayed.
        btn_SaveColor.setOnClickListener(v -> saveColor(redColor, greenColor, blueColor, opacity, ""));

        setColorValuesClickListener();
        refreshUI();
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (scalePictureSubscription != null && scalePictureSubscription.isUnsubscribed()) {
            scalePictureSubscription.unsubscribe();
        }

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
                showPrintColorDialog(PrintJobDialogFragment.PRINT_COLOR_JOB);
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
                        Snackbar.LENGTH_INDEFINITE).setAction(getString(android.R.string.ok), view -> {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
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
//        tvRGB_R.setOnClickListener(RGBAClickListener);
//        tvRGB_G.setOnClickListener(RGBAClickListener);
//        tvRGB_B.setOnClickListener(RGBAClickListener);
//        tvRGB_O.setOnClickListener(RGBAClickListener);
        tvHexadecimal.setOnClickListener(v -> {
            HexInsertionFragment fragment =
                    HexInsertionFragment.newInstance(tvHexadecimal.getText().toString().substring(3));
            fragment.show(getSupportFragmentManager(), null);
        });
    }

//    private final View.OnClickListener RGBAClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            short[] rgbaValues = new short[]{(short) redColor, (short) greenColor, (short) blueColor, (short)
//                    opacity
//            };
//
//            RgbaInsertionFragment fragment = RgbaInsertionFragment.newInstance(rgbaValues);
//            fragment.show(getSupportFragmentManager(), null);
//        }
//    };

    private void showColorList() {
        startActivity(new Intent(this, ColorListActivity.class), ActivityOptions
                .makeSceneTransitionAnimation(this).toBundle());
    }

    private void showAbout() {
        startActivity(new Intent(this, AboutActivity.class), ActivityOptions
                .makeSceneTransitionAnimation(this).toBundle());
    }

    private void saveColor(int RGBRComponent, int RGBGComponent, int RGBBComponent, int RGBOComponent, String
            colorName) {
        AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
        };

        float[] hsb = ColorUtils.RGBToHSB(RGBRComponent, RGBGComponent, RGBBComponent);

        ContentValues values = new ContentValues();
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_NAME, colorName);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_HEX, String.format("#%s%s%s%s",
                ColorUtils.RGBToHex(opacity),
                ColorUtils.RGBToHex(redColor),
                ColorUtils.RGBToHex(greenColor),
                ColorUtils.RGBToHex(blueColor)));
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_R, RGBRComponent);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_G, RGBGComponent);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_B, RGBBComponent);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_RGB_A, RGBOComponent);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_H, (int) hsb[0]);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_S, (int) hsb[1] * 100);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_HSB_B, (int) hsb[2] * 100);
        values.put(ColorDataContract.ColorEntry.COLUMN_COLOR_FAVORITE, 1);

        handler.startInsert(-1, null, RGBToolContentProvider.CONTENT_URI, values);

        btn_SaveColor.setVisibility(View.INVISIBLE);
    }

    private void printColor(String message) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        // Set job name, which will be displayed in the print queue
        String jobName = getString(R.string.app_name) + "_Color";

        // Start a print job, passing in a PrintDocumentAdapter implementation
        printManager.print(jobName, new RGBToolPrintColorAdapter(this, message, redColor, greenColor,
                blueColor, opacity), null);
    }

    private void updateSharedColor() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, ColorUtils.getColorMessage(redColor, greenColor, blueColor,
                opacity));
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
        startActivity(new Intent(this, RGBToolGalleryActivity.class), ActivityOptions
                .makeSceneTransitionAnimation(this).toBundle());
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

    private OnSeekBarChangeListener getRGB() {
        return new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar.equals(seekBarRed)) {
                    redColor = progress;

                    thumbRect = seekBar.getThumb().getBounds();

                    redToolTip.setX((seekBarLeft / 2) + thumbRect.left);

                    if (progress < 10) {
                        redToolTip.setText("  " + redColor);
                    } else if (progress < 100) {
                        redToolTip.setText(" " + redColor);
                    } else {
                        redToolTip.setText(redColor + "");
                    }
                }

                if (seekBar.equals(seekBarGreen)) {
                    greenColor = progress;

                    thumbRect = seekBar.getThumb().getBounds();

                    greenToolTip.setX((seekBarLeft / 2) + thumbRect.left);
                    if (progress < 10) {
                        greenToolTip.setText("  " + greenColor);
                    } else if (progress < 100) {
                        greenToolTip.setText(" " + greenColor);
                    } else {
                        greenToolTip.setText(greenColor + "");
                    }
                }

                if (seekBar.equals(seekBarBlue)) {
                    blueColor = progress;

                    thumbRect = seekBar.getThumb().getBounds();

                    blueToolTip.setX((seekBarLeft / 2) + thumbRect.left);
                    if (progress < 10) {
                        blueToolTip.setText("  " + blueColor);
                    } else if (progress < 100) {
                        blueToolTip.setText(" " + blueColor);
                    } else {
                        blueToolTip.setText(blueColor + "");
                    }
                }

                if (seekBar.equals(seekBarOpacity)) {
                    opacity = progress;

                    thumbRect = seekBar.getThumb().getBounds();

                    opacityToolTip.setX((seekBarLeft / 2) + thumbRect.left);
                    if (progress < 10) {
                        opacityToolTip.setText("  " + opacity);
                    } else if (progress < 100) {
                        opacityToolTip.setText(" " + opacity);
                    } else {
                        opacityToolTip.setText(opacity + "");
                    }
                }

                refreshUI();
            }
        };
    }

    private void updateSaveColorButton() {
        if (DatabaseUtils.findColor(MainActivity.this, redColor, greenColor, blueColor, opacity)) {
            btn_SaveColor.setVisibility(View.INVISIBLE);
        } else {
            btn_SaveColor.setVisibility(View.VISIBLE);
        }
    }

    private void refreshUI() {
        updateRGBField();
        updateHSBField();
        updateHexadecimalField();
        updateSharedColor();
        updateSaveColorButton();
        updateColorDetails();
        updateColorSample();
        colorView.setBackgroundColor(Color.argb(opacity, redColor, greenColor, blueColor));
    }

    private void updateRGBField() {
        // RGB channel: R, G, B, OPACITY.
        tvRGB_R.setText(ColorUtils.getRGB(redColor));
        tvRGB_G.setText(ColorUtils.getRGB(greenColor));
        tvRGB_B.setText(ColorUtils.getRGB(blueColor));
        tvRGB_O.setText(ColorUtils.getRGB(opacity));
    }

    private void updateHSBField() {
        // Get float array with 3 values for HSB-HSV.
        float[] hsb = ColorUtils.RGBToHSB(redColor, greenColor, blueColor);

        // Set HSB-HSV single channel value.
        tvHSB_H.setText(String.format("%.0f", hsb[0]));
        tvHSB_S.setText(String.format("%.0f%%", (hsb[1] * 100.0f))); // % value.
        tvHSB_B.setText(String.format("%.0f%%", (hsb[2] * 100.0f))); // % value.
    }

    private void updateHexadecimalField() {
        String hexValue = String.format("#%s%s%s%s", ColorUtils.RGBToHex(opacity), ColorUtils.RGBToHex(redColor),
                ColorUtils.RGBToHex(greenColor), ColorUtils.RGBToHex(blueColor));

        tvHexadecimal.setText(hexValue);
    }

    private void updateColorDetails() {
        complementaryColor = ColorUtils.getComplementaryColor(redColor, blueColor, greenColor);
        complementaryColorText.setText(getString(R.string.color_details_complementary, ColorUtils.RGBToHex
                (complementaryColor)));
        complementaryColorBackground.setCardBackgroundColor(complementaryColor);

        contrastColor = ColorUtils.getContrastColor(redColor, blueColor, greenColor);
        contrastColorText.setText(getString(R.string.color_details_contrast, ColorUtils.RGBToHex
                (contrastColor)));
        contrastColorBackground.setCardBackgroundColor(contrastColor);
    }

    private void updateColorSample() {
        // Text.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            firstColorSampleBackground.setCardBackgroundColor(getResources().getColor(R.color.white, getTheme()));
            secondColorSampleBackground.setCardBackgroundColor(getResources().getColor(R.color.black, getTheme()));
        } else {
            firstColorSampleBackground.setCardBackgroundColor(getResources().getColor(R.color.white));
            secondColorSampleBackground.setCardBackgroundColor(getResources().getColor(R.color.black));
        }

        firstColorSampleTextNormal.setTextColor(Color.argb(opacity, redColor, greenColor, blueColor));
        secondColorSampleTextNormal.setTextColor(Color.argb(opacity, redColor, greenColor, blueColor));

        // Background.
        firstColorSampleBackgroundBg.setCardBackgroundColor(Color.argb(opacity, redColor, greenColor, blueColor));
        secondColorSampleBackgroundBg.setCardBackgroundColor(Color.argb(opacity, redColor, greenColor, blueColor));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            firstColorSampleTextNormalBg.setTextColor(getResources().getColor(R.color.white, getTheme()));
            secondColorSampleTextNormalBg.setTextColor(getResources().getColor(R.color.black, getTheme()));
        } else {
            firstColorSampleTextNormalBg.setTextColor(getResources().getColor(R.color.white));
            secondColorSampleTextNormalBg.setTextColor(getResources().getColor(R.color.black));
        }
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

            scalePictureSubscription =
                    PictureScalingManager.scalePictureObservable(currentPhotoPath, destinationPath)
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

    private String getRealPathFromURI(Uri contentUri) {
        String path = null;
        String document_id = null;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();
        }

        if (document_id != null) {
            cursor = getContentResolver().query(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
            if (cursor != null) {
                cursor.moveToFirst();
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                cursor.close();
            }
        }

        return path;
    }

    private void updateRGBColor(int RGBRComponent, int RGBGComponent, int RGBBComponent, int RGBOComponent) {
        redColor = RGBRComponent;
        greenColor = RGBGComponent;
        blueColor = RGBBComponent;
        opacity = RGBOComponent;
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
        editor.putInt(CommonUtils.PREFS_R_COLOR, redColor);
        editor.putInt(CommonUtils.PREFS_G_COLOR, greenColor);
        editor.putInt(CommonUtils.PREFS_B_COLOR, blueColor);
        editor.putInt(CommonUtils.PREFS_OPACITY, opacity);
        editor.apply();
    }

    @Subscribe
    public void onColorSelectEvent(ColorSelectEvent event) {
        updateRGBColor(event.RGBRComponent,
                event.RGBGComponent,
                event.RGBBComponent,
                event.RGBOComponent);

        refreshUI();

        seekBarRed.setProgress(event.RGBRComponent);
        seekBarGreen.setProgress(event.RGBGComponent);
        seekBarBlue.setProgress(event.RGBBComponent);
        seekBarOpacity.setProgress(event.RGBOComponent);

        savePreferences();
    }

    @Subscribe
    public void onPhotoScaledEvent(PhotoScaledEvent event) {
        /**
         * Tell the media scanner about the new file so that it is
         * immediately available to the user.
         */
        MediaScannerConnection.scanFile(getApplicationContext(),
                new String[]{event.photoPath}, null,
                (path, uri) -> {
                });

        Intent colorPickerIntent = new Intent(this, ColorPickerActivity.class);
        colorPickerIntent.putExtra(ImageUtils.EXTRA_JPEG_FILE_PATH, event.photoPath);
        colorPickerIntent.putExtra(ImageUtils.EXTRA_DELETE_FILE, event.deleteFile);
        startActivity(colorPickerIntent);
    }

    @Subscribe
    public void onPrintColorEvent(PrintColorEvent event) {
        printColor(event.message);
    }

    @SuppressWarnings("UnusedParameters")
    @Subscribe
    public void onUpdateSaveColorUIEvent(UpdateSaveColorUIEvent event) {
        updateSaveColorButton();
    }

    @Subscribe
    public void onRGBAInsertionEvent(RGBAInsertionEvent event) {
        redColor = event.rgbaValues[0];
        greenColor = event.rgbaValues[1];
        blueColor = event.rgbaValues[2];
        opacity = event.rgbaValues[3];
        seekBarRed.setProgress(event.rgbaValues[0]);
        seekBarGreen.setProgress(event.rgbaValues[1]);
        seekBarBlue.setProgress(event.rgbaValues[2]);
        seekBarOpacity.setProgress(event.rgbaValues[3]);
        refreshUI();
        savePreferences();
    }

    @Subscribe
    public void onUpdateHexValueEvent(UpdateHexValueEvent event) {
        int[] rgb = ColorUtils.hexToRGB(event.hexValue);
        redColor = rgb[0];
        greenColor = rgb[1];
        blueColor = rgb[2];
        seekBarRed.setProgress(rgb[0]);
        seekBarGreen.setProgress(rgb[1]);
        seekBarBlue.setProgress(rgb[2]);
        refreshUI();
        savePreferences();
    }

    @Subscribe
    public void onErrorMessageEvent(ErrorMessageEvent event) {
        Snackbar.make(findViewById(android.R.id.content), event.message, Snackbar.LENGTH_SHORT).show();
    }
}
