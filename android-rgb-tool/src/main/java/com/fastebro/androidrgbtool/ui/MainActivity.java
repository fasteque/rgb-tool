package com.fastebro.androidrgbtool.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.*;
import android.content.*;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import android.widget.ShareActionProvider;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fastebro.androidrgbtool.contracts.ColorDataContract;
import com.fastebro.androidrgbtool.fragments.ColorListDialogFragment;
import com.fastebro.androidrgbtool.fragments.PrintColorDialogFragment;
import com.fastebro.androidrgbtool.interfaces.PhotoScaling;
import com.fastebro.androidrgbtool.print.RGBToolPrintDocumentAdapter;
import com.fastebro.androidrgbtool.provider.RGBToolContentProvider;
import com.fastebro.androidrgbtool.render.GLRender;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.tasks.PhotoScalingTask;
import com.fastebro.androidrgbtool.utils.*;
import com.fastebro.androidrgbtool.view.CustomGLSurfaceView;

public class MainActivity extends Activity
        implements PhotoScaling,
        PrintColorDialogFragment.PrintColorDialogListener,
        ColorListDialogFragment.ColorListDialogListener {
    // Objects.
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

    // Hexadecimal and real color name.
    @InjectView(R.id.textView_Hexadecimal)
    TextView textView_Hexadecimal;

    // Save color button.
    @InjectView(R.id.btn_save_color)
    ImageButton btn_SaveColor;

    private String mCurrentPhotoPath;
    private BaseAlbumDirFactory mAlbumStorageDirFactory = null;

    // Rendering
    private GLSurfaceView glSurfaceView;
    private GLRender mGLRender;

    private boolean isRendered = false;

    private float RGB_R_COLOR = 0.0f;
    private float RGB_G_COLOR = 0.0f;
    private float RGB_B_COLOR = 0.0f;
    private float RGB_OPACITY = 255.0f;   // Default value.

    private static final int REQUEST_OPEN_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    protected String hexValue;

    private ShareActionProvider mShareActionProvider;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getActionBar().setDisplayShowTitleEnabled(false);

        setContentView(R.layout.activity_main_bottom);

        // Import main layout (with SeekBar sliders).
        LayoutInflater inflater = getLayoutInflater();
        addContentView(inflater.inflate(R.layout.activity_main, null),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );


        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

        // Even though the latest emulator supports OpenGL ES 2.0,
        // it has a bug where it doesn't set the reqGlEsVersion so
        // the above check doesn't work. The below will detect if the
        // app is running on an emulator, and assume that it supports
        // OpenGL ES 2.0.
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2 == true) {

            mGLRender = new GLRender();

            glSurfaceView = (CustomGLSurfaceView) findViewById(R.id.custom_gl_surface_view);
            glSurfaceView.setEGLContextClientVersion(2);
            glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glSurfaceView.setZOrderOnTop(true);
            glSurfaceView.setRenderer(mGLRender);

            isRendered = true;
        }

        ButterKnife.inject(this);

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

        refreshUI();
    }


    @Override
    protected void onPause() {

        super.onPause();

        if (isRendered) {

            glSurfaceView.onPause();
        }

        if (isFinishing()) {

            savePreferences();
        }
    }


    @Override
    protected void onResume() {

        super.onResume();

        if (isRendered) {

            glSurfaceView.onResume();
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
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        updateSharedColor();

        item = menu.findItem(R.id.action_print);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            item.setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_camera:
                ImagePickerDialogFragment dialogFragment = new ImagePickerDialogFragment();
                dialogFragment.setCancelable(true);
                dialogFragment.show(getFragmentManager(), null);
                return true;
            case R.id.action_color_list:
                showColorListDialog();
                return true;
            case R.id.action_print:
                showPrintColorDialog();
                return true;
//            case R.id.action_share:
//                updateSharedColor();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void showColorListDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ColorListDialogFragment();
        dialog.show(getFragmentManager(), null);
    }


    private void showPrintColorDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new PrintColorDialogFragment();
        dialog.show(getFragmentManager(), null);
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
                new RGBToolPrintDocumentAdapter(
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
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }


    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, REQUEST_OPEN_GALLERY);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OPEN_GALLERY) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    mCurrentPhotoPath = getRealPathFromURI(this, data.getData());
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
     * @return
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
                    mGLRender.COLOR_OPACITY = RGB_OPACITY / 255.0f;
                }

                if (seekBar.equals(seekBar_R)) {
                    RGB_R_COLOR = progress;
                    mGLRender.R_COLOR = RGB_R_COLOR / 255.0f;
                }

                if (seekBar.equals(seekBar_G)) {
                    RGB_G_COLOR = progress;
                    mGLRender.G_COLOR = RGB_G_COLOR / 255.0f;
                }

                if (seekBar.equals(seekBar_B)) {
                    RGB_B_COLOR = progress;
                    mGLRender.B_COLOR = RGB_B_COLOR / 255.0f;
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
        // Update OpenGL Context (glSurfaceView) and all RGB and hexadecimal values.
        glSurfaceView.requestRender();
        updateRGBField();
        updateHSBField();
        updateHexadecimalField();
        updateColorName();
        updateSharedColor();
        updateSaveColorButton();
    }

    /**
     *
     *
     */
    protected void updateRGBField() {
        // RGB channel: R,G,B,OPACITY.
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
        // Log.v("COLOR_NAME", colorDataProvider.getColorNameByHex(hexValue));
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = null;

        mAlbumStorageDirFactory = new BaseAlbumDirFactory();

        try {
            f = setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();

            f = null;
            mCurrentPhotoPath = null;
        }

        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }


    private File setUpPhotoFile() throws IOException {
        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = UImage.JPEG_FILE_PREFIX + timeStamp + "_";

        File image = File.createTempFile(imageFileName, UImage.JPEG_FILE_SUFFIX, getAlbumDir());
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }


    // Photo album for this application
    private String getAlbumName() {
        return getString(R.string.album_name);
    }


    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d(getString(R.string.app_name), "failed to create directory");

                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }


    private void handlePhoto(boolean useTempFile) {
        if (mCurrentPhotoPath != null) {
            new PhotoScalingTask(this, mCurrentPhotoPath, useTempFile).execute();
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


    @Override
    public void onScalingComplete(String photoPath, boolean deleteFile) {
        Intent colorPickerIntent = new Intent(this, ColorPickerActivity.class);
        colorPickerIntent.putExtra(UImage.EXTRA_JPEG_FILE_PATH, photoPath);
        colorPickerIntent.putExtra(UImage.EXTRA_DELETE_FILE, deleteFile);
        startActivity(colorPickerIntent);
    }


    private void updateRGBColor(float RGBRComponent,
                                float RGBGComponent,
                                float RGBBComponent,
                                float RGBOComponent) {
        RGB_R_COLOR = RGBRComponent;
        RGB_G_COLOR = RGBGComponent;
        RGB_B_COLOR = RGBBComponent;
        RGB_OPACITY = RGBOComponent;

        mGLRender.R_COLOR = RGB_R_COLOR / 255.0f;
        mGLRender.G_COLOR = RGB_G_COLOR / 255.0f;
        mGLRender.B_COLOR = RGB_B_COLOR / 255.0f;
        mGLRender.COLOR_OPACITY = RGB_OPACITY / 255.0f;
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
        editor.commit();
    }


    @Override
    public void onDialogPositiveClick(String message) {
        printColor(message);
    }


    @Override
    public void onDialogNegativeClick() {
        printColor(null);
    }


    @Override
    public void onColorClick(float RGBRComponent,
                             float RGBGComponent,
                             float RGBBComponent,
                             float RGBOComponent,
                             String colorName) {
        updateRGBColor(RGBRComponent,
                RGBGComponent,
                RGBBComponent,
                RGBOComponent);

        refreshUI();

        // Also update the seekbars.
        seekBar_R.setProgress((int) RGBRComponent);
        seekBar_G.setProgress((int) RGBGComponent);
        seekBar_B.setProgress((int) RGBBComponent);
        seekBar_O.setProgress((int) RGBOComponent);

        savePreferences();
    }


    @SuppressLint("ValidFragment")
    private class ImagePickerDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.pick_color)
                    .setItems(R.array.pick_color_array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    openGallery();
                                    break;
                                case 1:
                                    dispatchTakePictureIntent();
                                    break;
                                default:
                                    break;
                            }
                        }
                    });

            return builder.create();
        }
    }
}
