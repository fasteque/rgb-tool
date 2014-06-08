package com.fastebro.androidrgbtool.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import butterknife.InjectView;
import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.utils.UImage;
import com.fastebro.androidrgbtool.widgets.RGBPanelData;

import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.File;
import java.io.IOException;


public class ColorPickerActivity extends Activity {
    private ImageView mImageView;
    private PhotoViewAttacher mAttacher;
    private Bitmap mBitmap;
    View.OnTouchListener imgSourceOnTouchListener;
    private RGBPanelData mRGBPanelDataLayout;
    private RelativeLayout mMainLayout;
    private boolean isRGBPanelTop = false;

    private String mCurrentPath = null;
    private boolean mDeleteFile = false;
    private final static float PHOTO_SCALING_FACTOR = 3.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        if (getActionBar() != null) {
            getActionBar().hide();
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
    }


    @Override
    protected void onDestroy() {
        if (mDeleteFile) {
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
                if (isRGBPanelTop) {
                    isRGBPanelTop = false;

                    mRGBPanelDataLayout.setVisibility(View.GONE);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    mRGBPanelDataLayout.setLayoutParams(params);

                    mRGBPanelDataLayout.updateData(touchedRGB);

                    mRGBPanelDataLayout.setVisibility(View.VISIBLE);
                } else {
                    mRGBPanelDataLayout.updateData(touchedRGB);

                    if (mRGBPanelDataLayout.getVisibility() == View.GONE) {
                        mRGBPanelDataLayout.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (!isRGBPanelTop) {
                    isRGBPanelTop = true;

                    mRGBPanelDataLayout.setVisibility(View.GONE);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    mRGBPanelDataLayout.setLayoutParams(params);

                    mRGBPanelDataLayout.updateData(touchedRGB);

                    mRGBPanelDataLayout.setVisibility(View.VISIBLE);
                } else {
                    mRGBPanelDataLayout.updateData(touchedRGB);

                    if (mRGBPanelDataLayout.getVisibility() == View.GONE) {
                        mRGBPanelDataLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }
}
