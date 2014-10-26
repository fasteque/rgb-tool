package com.fastebro.androidrgbtool.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.transition.Fade;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.utils.UImage;
import com.fastebro.androidrgbtool.widgets.RGBPanelData;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.File;


public class ColorPickerActivity extends BaseActivity {
    private ImageView mImageView;
    private PhotoViewAttacher mAttacher;
    private Bitmap mBitmap;
    View.OnTouchListener imgSourceOnTouchListener;
    private RGBPanelData mRGBPanelDataLayout;
    private RelativeLayout mMainLayout;

    private String mCurrentPath = null;
    private boolean mDeleteFile = false;
    private final static float PHOTO_SCALING_FACTOR = 3.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mMainLayout = (RelativeLayout) findViewById(R.id.color_picker_main_layout);

        mRGBPanelDataLayout = new RGBPanelData(this);
        mRGBPanelDataLayout.setVisibility(View.GONE);

        if (getIntent() != null) {
            // get the path of the image and set it.
            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                mCurrentPath = bundle.getString(UImage.EXTRA_JPEG_FILE_PATH);
                mDeleteFile = bundle.getBoolean(UImage.EXTRA_DELETE_FILE);
            }

            if (mCurrentPath != null) {
                mImageView = (ImageView) findViewById(R.id.iv_photo);

                try {
                    mBitmap = BitmapFactory.decodeFile(mCurrentPath);
                    mImageView.setImageBitmap(mBitmap);
                    mImageView.setOnTouchListener(imgSourceOnTouchListener);
                    mAttacher = new PhotoViewAttacher(mImageView);
                    mAttacher.setMaximumScale(mAttacher.getMaximumScale() * PHOTO_SCALING_FACTOR);
                    mAttacher.setOnViewTapListener(new PhotoViewTapListener());
                    mAttacher.setOnPhotoTapListener(new PhotoViewTapListener());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        mMainLayout.addView(mRGBPanelDataLayout, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Fade());
            getWindow().setAllowEnterTransitionOverlap(true);
        }
    }


    @Override
    protected void onDestroy() {
        if (mDeleteFile) {
            //noinspection ResultOfMethodCallIgnored
            new File(mCurrentPath).delete();
        }

        super.onDestroy();
    }

    private class PhotoViewTapListener
            implements PhotoViewAttacher.OnViewTapListener,
            PhotoViewAttacher.OnPhotoTapListener {
        @Override
        public void onViewTap(View view, float x, float y) {
            // Not being used so far.
        }

        @Override
        public void onPhotoTap(View view, float x, float y) {
            // x and y represent the percentage of the Drawable where the user clicked.
            int imageX = (int) (x * mBitmap.getWidth());
            int imageY = (int) (y * mBitmap.getHeight());

            int touchedRGB = mBitmap.getPixel(imageX, imageY);


            if (imageY < mBitmap.getHeight() / 2) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                mRGBPanelDataLayout.setLayoutParams(params);

                mRGBPanelDataLayout.updateData(touchedRGB);

                if (mRGBPanelDataLayout.getVisibility() == View.GONE) {
                    mRGBPanelDataLayout.setVisibility(View.VISIBLE);
                }
            } else {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                mRGBPanelDataLayout.setLayoutParams(params);

                mRGBPanelDataLayout.updateData(touchedRGB);

                if (mRGBPanelDataLayout.getVisibility() == View.GONE) {
                    mRGBPanelDataLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        } else {
            finish();
        }
    }
}
