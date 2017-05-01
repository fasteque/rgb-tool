package com.fastebro.androidrgbtool.rgb;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.colorpicker.ColorPickerActivity;
import com.fastebro.androidrgbtool.colors.ColorListActivity;
import com.fastebro.androidrgbtool.commons.EventBaseActivity;
import com.fastebro.androidrgbtool.gallery.RGBToolGalleryActivity;
import com.fastebro.androidrgbtool.model.entities.ScaledPicture;
import com.fastebro.androidrgbtool.model.events.ColorSelectEvent;
import com.fastebro.androidrgbtool.model.events.ErrorMessageEvent;
import com.fastebro.androidrgbtool.model.events.PhotoScaledEvent;
import com.fastebro.androidrgbtool.model.events.PrintColorEvent;
import com.fastebro.androidrgbtool.model.events.UpdateSaveColorUIEvent;
import com.fastebro.androidrgbtool.print.PrintJobDialogFragment;
import com.fastebro.androidrgbtool.print.RGBToolPrintColorAdapter;
import com.fastebro.androidrgbtool.rgb.widget.CustomSwipeViewPager;
import com.fastebro.androidrgbtool.settings.AboutActivity;
import com.fastebro.androidrgbtool.utils.BaseAlbumDirFactory;
import com.fastebro.androidrgbtool.utils.ColorUtils;
import com.fastebro.androidrgbtool.utils.CommonUtils;
import com.fastebro.androidrgbtool.utils.ImageUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends EventBaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    @BindView(R.id.activity_main_sliding_tabs)
    TabLayout tabLayout;
    @BindView(R.id.activity_main_viewpager)
    CustomSwipeViewPager viewPager;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Rgbtool);
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        setContentView(R.layout.activity_main_rgb);

        ButterKnife.bind(this);

        viewPager.setAdapter(new MainFragmentPagerAdapter(getSupportFragmentManager(), this));
        tabLayout.setupWithViewPager(viewPager);

        restorePreferences();
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

    private void showColorList() {
        startActivity(new Intent(this, ColorListActivity.class), ActivityOptions
                .makeSceneTransitionAnimation(this).toBundle());
    }

    private void showAbout() {
        startActivity(new Intent(this, AboutActivity.class), ActivityOptions
                .makeSceneTransitionAnimation(this).toBundle());
    }

    private void printColor(String message) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        // Set job name, which will be displayed in the print queue
        String jobName = getString(R.string.app_name) + "_Color";

        // Start a print job, passing in a PrintDocumentAdapter implementation
        printManager.print(jobName, new RGBToolPrintColorAdapter(this, message, redColor, greenColor, blueColor,
                opacity), null);
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
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType("image/*");
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());

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

    public void savePreferences() {
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

//        refreshUI();

//        seekBarRed.setProgress(event.RGBRComponent);
//        seekBarGreen.setProgress(event.RGBGComponent);
//        seekBarBlue.setProgress(event.RGBBComponent);
//        seekBarOpacity.setProgress(event.RGBOComponent);

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
//        updateSaveColorButton();
    }

    @Subscribe
    public void onErrorMessageEvent(ErrorMessageEvent event) {
        Snackbar.make(findViewById(android.R.id.content), event.message, Snackbar.LENGTH_SHORT).show();
    }

    public int getRedColor() {
        return redColor;
    }

    public void setRedColor(int redColor) {
        this.redColor = redColor;
    }

    public int getGreenColor() {
        return greenColor;
    }

    public void setGreenColor(int greenColor) {
        this.greenColor = greenColor;
    }

    public int getBlueColor() {
        return blueColor;
    }

    public void setBlueColor(int blueColor) {
        this.blueColor = blueColor;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }
}
