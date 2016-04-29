package com.fastebro.androidrgbtool.livepicker;

import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.utils.CameraUtils;

public class LivePickerActivity extends AppCompatActivity {
    private static final String TAG = LivePickerActivity.class.getName();

    private Camera camera;
    private CameraAsyncTask cameraAsyncTask;
    private FrameLayout livePreviewContainer;
    private LivePickerTextureView livePickerTextureView;
    private boolean isPortrait;

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
        isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        livePreviewContainer = (FrameLayout) findViewById(R.id.live_picker_preview_container);
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

    private class CameraAsyncTask extends AsyncTask<Void, Void, Camera> {

        @Override
        protected Camera doInBackground(Void... params) {
            Camera camera = getFacingBackCameraInstance();

            if (camera == null) {
                LivePickerActivity.this.finish();
            } else {
                /*
                Camera.Parameters cameraParameters = camera.getParameters();
                Camera.Size bestSize = CameraUtils.getBestPreviewSize(
                        cameraParameters.getSupportedPreviewSizes()
                        , livePreviewContainer.getWidth()
                        , livePreviewContainer.getHeight()
                        , isPortrait);
                //set optimal camera preview
                cameraParameters.setPreviewSize(bestSize.width, bestSize.height);
                camera.setParameters(cameraParameters);

                //set camera orientation to match with current device orientation
                CameraUtils.setCameraDisplayOrientation(LivePickerActivity.this, camera);

                //get proportional dimension for the layout used to display preview according to the preview size used
                int[] adaptedDimension = CameraUtils.getProportionalDimension(
                        bestSize
                        , livePreviewContainer.getWidth()
                        , livePreviewContainer.getHeight()
                        , isPortrait);

                //set up params for the layout used to display the preview
                mPreviewParams = new FrameLayout.LayoutParams(adaptedDimension[0], adaptedDimension[1]);
                mPreviewParams.gravity = Gravity.CENTER;
                */
            }

            return camera;
        }
    }
}
