package com.fastebro.androidrgbtool.colorpicker;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.graphics.Palette;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.palette.PaletteSwatch;
import com.fastebro.androidrgbtool.palette.ImagePaletteActivity;
import com.fastebro.androidrgbtool.commons.BaseActivity;
import com.fastebro.androidrgbtool.utils.ImageUtils;
import com.fastebro.androidrgbtool.widgets.RGBPanelData;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.File;
import java.util.ArrayList;


public class ColorPickerActivity extends BaseActivity {
    @BindView(R.id.color_picker_main_layout)
    ConstraintLayout mainLayout;
    @BindView(R.id.iv_photo)
    ImageView imageView;

    private PhotoViewAttacher attacher;
    private Bitmap bitmap;
    private View.OnTouchListener imgSourceOnTouchListener;
    private RGBPanelData rgbPanelDataLayout;

    private String currentPath = null;
    private boolean deleteFile = false;
    private final static float PHOTO_SCALING_FACTOR = 3.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        ButterKnife.bind(this);

        if(getSupportActionBar() != null) {
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

        mainLayout.addView(rgbPanelDataLayout, new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams
                .WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT));
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
        if (item.getItemId() == android.R.id.home) {
            finishAfterTransition();
            return true;
        } else if (item.getItemId() == R.id.action_palette) {
            generatePalette();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void generatePalette() {
        if (bitmap != null) {
            Palette.Builder paletteBuilder = Palette.from(bitmap);
            paletteBuilder.generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    Intent intent = new Intent(ColorPickerActivity.this, ImagePaletteActivity.class);

                    ArrayList<PaletteSwatch> swatches = new ArrayList<>();

                    if(palette.getVibrantSwatch() != null) {
                        swatches.add(new PaletteSwatch(palette.getVibrantSwatch().getRgb(),
                                PaletteSwatch.SwatchType.VIBRANT));
                    }

                    if(palette.getMutedSwatch() != null) {
                        swatches.add(new PaletteSwatch(palette.getMutedSwatch().getRgb(),
                                PaletteSwatch.SwatchType.MUTED));
                    }

                    if(palette.getLightVibrantSwatch() != null) {
                        swatches.add(new PaletteSwatch(palette.getLightVibrantSwatch().getRgb(),
                                PaletteSwatch.SwatchType.LIGHT_VIBRANT));
                    }

                    if(palette.getLightMutedSwatch() != null) {
                        swatches.add(new PaletteSwatch(palette.getLightMutedSwatch().getRgb(),
                                PaletteSwatch.SwatchType.LIGHT_MUTED));
                    }

                    if(palette.getDarkVibrantSwatch() != null) {
                        swatches.add(new PaletteSwatch(palette.getDarkVibrantSwatch().getRgb(),
                                PaletteSwatch.SwatchType.DARK_VIBRANT));
                    }

                    if(palette.getDarkMutedSwatch() != null) {
                        swatches.add(new PaletteSwatch(palette.getDarkMutedSwatch().getRgb(),
                                PaletteSwatch.SwatchType.DARK_MUTED));
                    }

                    intent.putParcelableArrayListExtra(ImagePaletteActivity.EXTRA_SWATCHES, swatches);
                    intent.putExtra(ImagePaletteActivity.FILENAME, Uri.parse(currentPath).getLastPathSegment());

                    startActivity(intent,
                            ActivityOptions.makeSceneTransitionAnimation(ColorPickerActivity.this).toBundle());
                }
            });
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
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout
                        .LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT);
                params.bottomToBottom = R.id.color_picker_main_layout;
                params.lefToLeft = R.id.color_picker_main_layout;
                params.rightToRight = R.id.color_picker_main_layout;

                rgbPanelDataLayout.setLayoutParams(params);
                rgbPanelDataLayout.updateData(touchedRGB);

                if (rgbPanelDataLayout.getVisibility() == View.GONE) {
                    rgbPanelDataLayout.setVisibility(View.VISIBLE);
                }
            } else {
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout
                        .LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT);
                params.topToTop = R.id.color_picker_main_layout;
                params.lefToLeft = R.id.color_picker_main_layout;
                params.rightToRight = R.id.color_picker_main_layout;

                rgbPanelDataLayout.setLayoutParams(params);
                rgbPanelDataLayout.updateData(touchedRGB);

                if (rgbPanelDataLayout.getVisibility() == View.GONE) {
                    rgbPanelDataLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        finishAfterTransition();
    }
}
