package com.fastebro.androidrgbtool.livepicker;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.utils.CameraUtils;

public class LivePickerActivity extends AppCompatActivity implements LivePickerTextureView.OnColorPointedListener,
        View.OnClickListener {
    private static final String TAG = LivePickerActivity.class.getName();

    private Camera camera;
    private CameraAsyncTask cameraAsyncTask;
    private FrameLayout livePreviewContainer;
    private LivePickerTextureView livePickerTextureView;
    private boolean isPortrait;
    private int pointedColor;
    private View pointerRing;

    private boolean isFlashOn;

    private static Camera getFacingBackCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return camera;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_picker);

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        cameraAsyncTask = new CameraAsyncTask();
        cameraAsyncTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (cameraAsyncTask != null) {
            cameraAsyncTask.cancel(true);
        }

        releaseCameraPreview();
        releaseCamera();
    }

    private void initViews() {
        // TODO: use ButterKnife
        isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        livePreviewContainer = (FrameLayout) findViewById(R.id.live_picker_preview_container);
        pointerRing = findViewById(R.id.live_picker_pointer_stroke);
    }

    private void releaseCameraPreview() {
        if (livePickerTextureView != null) {
            livePreviewContainer.removeView(livePickerTextureView);
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onColorPointed(int newColor) {
        pointedColor = newColor;
        pointerRing.getBackground().setColorFilter(pointedColor, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void onClick(View v) {
        // TODO
    }

    private class CameraAsyncTask extends AsyncTask<Void, Void, Camera> {

        protected FrameLayout.LayoutParams previewParams;

        @Override
        protected Camera doInBackground(Void... params) {
            return getFacingBackCameraInstance();
        }

        @Override
        protected void onPostExecute(Camera camera) {
            if (!isCancelled()) {
                LivePickerActivity.this.camera = camera;
                if (LivePickerActivity.this.camera == null) {
                    LivePickerActivity.this.finish();
                } else {
                    Camera.Parameters cameraParameters = camera.getParameters();
                    Camera.Size bestSize = CameraUtils.getBestPreviewSize(
                            cameraParameters.getSupportedPreviewSizes()
                            , livePreviewContainer.getWidth()
                            , livePreviewContainer.getHeight()
                            , isPortrait);
                    // Set optimal camera preview
                    cameraParameters.setPreviewSize(bestSize.width, bestSize.height);
                    // Set focus mode
                    if (cameraParameters.getSupportedFocusModes().contains(
                            Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    }

                    camera.setParameters(cameraParameters);

                    // Set camera orientation to match with current device orientation
                    CameraUtils.setCameraDisplayOrientation(LivePickerActivity.this, camera);

                    // Get proportional dimension for the layout used to display preview according to the preview size
                    // used
                    int[] adaptedDimension = CameraUtils.getProportionalDimension(
                            bestSize
                            , livePreviewContainer.getWidth()
                            , livePreviewContainer.getHeight()
                            , isPortrait);

                    // Set up params for the layout used to display the preview
                    previewParams = new FrameLayout.LayoutParams(adaptedDimension[0], adaptedDimension[1]);
                    previewParams.gravity = Gravity.CENTER;

                    // Set up camera preview
                    livePickerTextureView = new LivePickerTextureView(LivePickerActivity.this, LivePickerActivity
                            .this.camera);
                    livePickerTextureView.setOnColorPointedListener(LivePickerActivity.this);
                    livePickerTextureView.setOnClickListener(LivePickerActivity.this);

                    livePreviewContainer.addView(livePickerTextureView, 0, previewParams);
                }
            }
        }

        @Override
        protected void onCancelled(Camera camera) {
            if (camera != null) {
                camera.release();
            }
        }
    }

    private boolean isFlashSupported() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private void toggleFlash() {
        if (camera != null) {
            final Camera.Parameters parameters = camera.getParameters();
            final String flashParameter = isFlashOn ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters
                    .FLASH_MODE_TORCH;
            parameters.setFlashMode(flashParameter);
            camera.stopPreview();
            camera.setParameters(parameters);
            camera.startPreview();
            isFlashOn = !isFlashOn;
            invalidateOptionsMenu();
        }
    }
}
