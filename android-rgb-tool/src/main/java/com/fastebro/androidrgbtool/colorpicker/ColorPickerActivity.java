package com.fastebro.androidrgbtool.colorpicker;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.graphics.Palette;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.commons.BaseActivity;
import com.fastebro.androidrgbtool.model.entities.ScaledPicture;
import com.fastebro.androidrgbtool.model.events.PhotoScaledEvent;
import com.fastebro.androidrgbtool.palette.ImagePaletteActivity;
import com.fastebro.androidrgbtool.palette.PaletteSwatch;
import com.fastebro.androidrgbtool.rgb.PictureScalingManager;
import com.fastebro.androidrgbtool.utils.ImageUtils;
import com.fastebro.androidrgbtool.widgets.RGBPanelData;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import uk.co.senab.photoview.PhotoViewAttacher;


public class ColorPickerActivity extends BaseActivity {
    private final static float PHOTO_SCALING_FACTOR = 3.0f;
    private static final int REQUEST_OPEN_GALLERY = 1;
    @BindView(R.id.color_picker_main_layout)
    RelativeLayout mainLayout;
    @BindView(R.id.iv_photo)
    ImageView imageView;
    @BindView(R.id.color_picker_image)
    ImageView emptyImage;
    @BindView(R.id.color_picker_text)
    TextView emptyText;
    private PhotoViewAttacher attacher;
    private Bitmap bitmap;
    private View.OnTouchListener imgSourceOnTouchListener;
    private RGBPanelData rgbPanelDataLayout;
    private Subscription scalePictureSubscription;
    private String currentPath = null;
    private String currentPhotoPath;
    private boolean deleteFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rgbPanelDataLayout = new RGBPanelData(this);
        rgbPanelDataLayout.setVisibility(View.GONE);

        if (getIntent() != null) {
            // get the path of the image and set it.
            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                currentPath = bundle.getString(ImageUtils.EXTRA_JPEG_FILE_PATH);
                deleteFile = bundle.getBoolean(ImageUtils.EXTRA_DELETE_FILE);
            }

            if (currentPath != null) {
                try {
                    bitmap = BitmapFactory.decodeFile(currentPath);
                    imageView.setImageBitmap(bitmap);
                    imageView.setOnTouchListener(imgSourceOnTouchListener);
                    attacher = new PhotoViewAttacher(imageView);
                    attacher.setMaximumScale(attacher.getMaximumScale() * PHOTO_SCALING_FACTOR);
                    attacher.setOnViewTapListener(new PhotoViewTapListener());
                    attacher.setOnPhotoTapListener(new PhotoViewTapListener());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        mainLayout.addView(rgbPanelDataLayout, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams
                .WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        // Activity launched via shortcut
        if (bitmap == null) {
            emptyImage.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.VISIBLE);
            emptyImage.setOnClickListener(v -> openDeviceGallery());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (scalePictureSubscription != null && scalePictureSubscription.isUnsubscribed()) {
            scalePictureSubscription.unsubscribe();
        }
    }

    @Override
    protected void onDestroy() {
        if (deleteFile) {
            //noinspection ResultOfMethodCallIgnored
            new File(currentPath).delete();
            getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media.DATA + "=?", new String[]{currentPath});
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.color_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishAfterTransition();
                return true;
            case R.id.action_palette:
                generatePalette();
                return true;
            case R.id.action_load: {
                openDeviceGallery();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void generatePalette() {
        if (bitmap != null) {
            Palette.Builder paletteBuilder = Palette.from(bitmap);
            paletteBuilder.generate(palette -> {
                Intent intent = new Intent(ColorPickerActivity.this, ImagePaletteActivity.class);

                ArrayList<PaletteSwatch> swatches = new ArrayList<>();

                if (palette.getVibrantSwatch() != null) {
                    swatches.add(new PaletteSwatch(palette.getVibrantSwatch().getRgb(),
                            PaletteSwatch.SwatchType.VIBRANT));
                }

                if (palette.getMutedSwatch() != null) {
                    swatches.add(new PaletteSwatch(palette.getMutedSwatch().getRgb(),
                            PaletteSwatch.SwatchType.MUTED));
                }

                if (palette.getLightVibrantSwatch() != null) {
                    swatches.add(new PaletteSwatch(palette.getLightVibrantSwatch().getRgb(),
                            PaletteSwatch.SwatchType.LIGHT_VIBRANT));
                }

                if (palette.getLightMutedSwatch() != null) {
                    swatches.add(new PaletteSwatch(palette.getLightMutedSwatch().getRgb(),
                            PaletteSwatch.SwatchType.LIGHT_MUTED));
                }

                if (palette.getDarkVibrantSwatch() != null) {
                    swatches.add(new PaletteSwatch(palette.getDarkVibrantSwatch().getRgb(),
                            PaletteSwatch.SwatchType.DARK_VIBRANT));
                }

                if (palette.getDarkMutedSwatch() != null) {
                    swatches.add(new PaletteSwatch(palette.getDarkMutedSwatch().getRgb(),
                            PaletteSwatch.SwatchType.DARK_MUTED));
                }

                intent.putParcelableArrayListExtra(ImagePaletteActivity.EXTRA_SWATCHES, swatches);

                intent.putExtra(ImagePaletteActivity.FILENAME, Uri.parse(currentPath != null ?
                        currentPath : currentPhotoPath).getLastPathSegment());

                startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(ColorPickerActivity.this).toBundle());
            });
        }
    }

    @Override
    public void onBackPressed() {
        finishAfterTransition();
    }

    // Copypaste from MainActivity
    private void openDeviceGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUEST_OPEN_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OPEN_GALLERY && resultCode == RESULT_OK && data != null) {
            currentPhotoPath = getRealPathFromURI(data.getData());
            handlePhoto();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
            if (cursor != null) {
                cursor.moveToFirst();
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                cursor.close();
            }
        }

        return path;
    }

    private void handlePhoto() {
        String destinationPath;

        if (currentPhotoPath != null && (ContextCompat.checkSelfPermission(this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            if (true) {
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

            if (currentPhotoPath != null) {
                try {
                    bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    imageView.setImageBitmap(bitmap);
                    imageView.setOnTouchListener(imgSourceOnTouchListener);
                    attacher = new PhotoViewAttacher(imageView);
                    attacher.setMaximumScale(attacher.getMaximumScale() * PHOTO_SCALING_FACTOR);
                    attacher.setOnViewTapListener(new PhotoViewTapListener());
                    attacher.setOnPhotoTapListener(new PhotoViewTapListener());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.error_open_gallery_image), Snackbar.LENGTH_SHORT).show();
        }
    }

    private class PhotoViewTapListener
            implements PhotoViewAttacher.OnViewTapListener,
            PhotoViewAttacher.OnPhotoTapListener {
        @Override
        public void onViewTap(View view, float x, float y) {
            // Not being used so far.
        }

        @Override
        public void onOutsidePhotoTap() {
            // Not being used so far.
        }

        @Override
        public void onPhotoTap(View view, float x, float y) {
            // x and y represent the percentage of the Drawable where the user clicked.
            int imageX = (int) (x * bitmap.getWidth());
            int imageY = (int) (y * bitmap.getHeight());

            int touchedRGB = bitmap.getPixel(imageX, imageY);

            if (imageY < bitmap.getHeight() / 2) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams
                        .WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                rgbPanelDataLayout.setLayoutParams(params);
                rgbPanelDataLayout.updateData(touchedRGB);

                if (rgbPanelDataLayout.getVisibility() == View.GONE) {
                    rgbPanelDataLayout.setVisibility(View.VISIBLE);
                }
            } else {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams
                        .WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                rgbPanelDataLayout.setLayoutParams(params);
                rgbPanelDataLayout.updateData(touchedRGB);

                if (rgbPanelDataLayout.getVisibility() == View.GONE) {
                    rgbPanelDataLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
