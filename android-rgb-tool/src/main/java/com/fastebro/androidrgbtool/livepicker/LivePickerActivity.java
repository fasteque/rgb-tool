package com.fastebro.androidrgbtool.livepicker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.rgb.MainActivity;
import com.fastebro.androidrgbtool.utils.CameraUtils;
import com.fastebro.androidrgbtool.utils.ClipboardUtils;
import com.fastebro.androidrgbtool.widgets.CircleView;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class LivePickerActivity extends AppCompatActivity
        implements LivePickerTextureView.OnColorPointedListener, View.OnClickListener {
    private static final String TAG = LivePickerActivity.class.getName();

    @BindView(R.id.live_picker_preview_container)
    FrameLayout livePreviewContainer;
    @BindView(R.id.live_picker_pointer_stroke)
    View pointerRing;
    @BindView(R.id.live_picker_btn_back)
    ImageButton btnBack;
    @BindView(R.id.live_picker_last_color)
    CircleView lastColor;
    @BindView(R.id.live_picker_btn_save_color)
    ImageButton btnSaveColor;
    @BindView(R.id.live_picker_btn_flash)
    ImageButton btnFlashToggle;
    @BindView(R.id.live_picker_hex_color)
    TextView txtHexValue;

    private Camera mCamera;
    private CameraAsyncTask mCameraAsyncTask;

    private LivePickerTextureView livePickerTextureView;
    private boolean isPortrait;
    private int mPointedColor;

    private boolean isFlashOn;

    private static Camera getFacingBackCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            Timber.d("Error getting mCamera instance: %s", e.getMessage());
        }
        return camera;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_picker);

        ButterKnife.bind(this);

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        mCameraAsyncTask = new CameraAsyncTask();
        mCameraAsyncTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCameraAsyncTask != null) {
            mCameraAsyncTask.cancel(true);
        }

        releaseCameraPreview();
        releaseCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();

        toggleFlash();
    }

    private void initViews() {
        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnSaveColor.setOnClickListener(view -> {
            // TODO
        });

        if (isFlashSupported()) {
            btnFlashToggle.setVisibility(View.VISIBLE);
            btnFlashToggle.setOnClickListener(view -> toggleFlash());
        }

        txtHexValue.setOnClickListener(view -> {
            ClipboardUtils.copyToClipboard(txtHexValue.getText().toString().substring(1));
            Toast.makeText(LivePickerActivity.this, String.format("%s %s",
                    txtHexValue.getText().toString().substring(1),
                    getString(R.string.clipboard)), Toast.LENGTH_SHORT).show();
        });
    }

    private void releaseCameraPreview() {
        if (livePickerTextureView != null) {
            livePreviewContainer.removeView(livePickerTextureView);
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onColorPointed(int newColor) {
        mPointedColor = newColor;
        pointerRing.getBackground().setColorFilter(mPointedColor, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void onClick(View v) {
        // TODO
        // Set humanized color drawable
        lastColor.setBackground(new ColorDrawable(mPointedColor));
        // Set hex color value
        txtHexValue.setText(String.format("#%06X", (0xffffff & mPointedColor)));
    }

    private boolean isFlashSupported() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private void toggleFlash() {
        if (mCamera != null) {
            final Camera.Parameters parameters = mCamera.getParameters();
            final String flashParameter = isFlashOn ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters
                    .FLASH_MODE_TORCH;
            parameters.setFlashMode(flashParameter);
            mCamera.stopPreview();
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            isFlashOn = !isFlashOn;

            if (btnFlashToggle != null) {
                if (isFlashOn) {
                    btnFlashToggle.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable
                            .ic_flash_off_white));
                } else {
                    btnFlashToggle.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable
                            .ic_flash_on_white));
                }
            }
        }
    }

    private class CameraAsyncTask extends AsyncTask<Void, Void, Camera> {

        FrameLayout.LayoutParams previewParams;

        @Override
        protected Camera doInBackground(Void... params) {
            return getFacingBackCameraInstance();
        }

        @Override
        protected void onPostExecute(Camera camera) {
            if (!isCancelled()) {
                mCamera = camera;
                if (mCamera == null) {
                    LivePickerActivity.this.finish();
                } else {
                    Camera.Parameters cameraParameters = camera.getParameters();
                    Camera.Size bestSize = CameraUtils.getBestPreviewSize(
                            cameraParameters.getSupportedPreviewSizes()
                            , livePreviewContainer.getWidth()
                            , livePreviewContainer.getHeight()
                            , isPortrait);
                    // Set optimal mCamera preview
                    cameraParameters.setPreviewSize(bestSize.width, bestSize.height);
                    // Set focus mode
                    if (cameraParameters.getSupportedFocusModes().contains(
                            Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    }

                    camera.setParameters(cameraParameters);

                    // Set mCamera orientation to match with current device orientation
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

                    // Set up mCamera preview
                    livePickerTextureView = new LivePickerTextureView(LivePickerActivity.this, mCamera);
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
}