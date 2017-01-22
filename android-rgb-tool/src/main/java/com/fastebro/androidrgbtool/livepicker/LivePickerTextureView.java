package com.fastebro.androidrgbtool.livepicker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.TextureView;

import timber.log.Timber;

/**
 * Created by danielealtomare on 31/03/16.
 * Project: rgb-tool
 */
public class LivePickerTextureView extends TextureView implements TextureView.SurfaceTextureListener, Camera
        .PreviewCallback {
    private static final String TAG = LivePickerTextureView.class.getCanonicalName();
    private static final int POINTER_RADIUS = 5;
    private Camera camera;
    private Camera.Size previewSize;
    private int[] selectedColor;
    private OnColorPointedListener onColorPointedListener;

    public interface OnColorPointedListener {
        void onColorPointed(int newColor);
    }

    public LivePickerTextureView(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        this.camera.getParameters().getPreviewFormat();

        // Set a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        this.setSurfaceTextureListener(this);

        this.previewSize = camera.getParameters().getPreviewSize();
        this.selectedColor = new int[3];
    }

    public void setOnColorPointedListener(OnColorPointedListener onColorPointedListener) {
        this.onColorPointedListener = onColorPointedListener;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (onColorPointedListener != null) {
            final int midX = previewSize.width / 2;
            final int midY = previewSize.height / 2;

            selectedColor[0] = 0;
            selectedColor[1] = 0;
            selectedColor[2] = 0;

            // Compute the average selected color.
            for (int i = 0; i <= POINTER_RADIUS; i++) {
                for (int j = 0; j <= POINTER_RADIUS; j++) {
                    addColorFromYUV420(data, selectedColor, (i * POINTER_RADIUS + j + 1),
                            (midX - POINTER_RADIUS) + i, (midY - POINTER_RADIUS) + j,
                            previewSize.width, previewSize.height);
                }
            }

            onColorPointedListener.onColorPointed(Color.rgb(selectedColor[0], selectedColor[1], selectedColor[2]));
        }
    }

    // Credits go to http://stackoverflow.com/a/10125048
    private void addColorFromYUV420(byte[] data, int[] averageColor, int count, int x, int y, int width, int height) {
        final int size = width * height;
        final int Y = data[y * width + x] & 0xff;
        final int xby2 = x / 2;
        final int yby2 = y / 2;

        final float V = (float) (data[size + 2 * xby2 + yby2 * width] & 0xff) - 128.0f;
        final float U = (float) (data[size + 2 * xby2 + 1 + yby2 * width] & 0xff) - 128.0f;

        // YUV -> RGB conversion
        float Yf = 1.164f * ((float) Y) - 16.0f;
        int red = (int) (Yf + 1.596f * V);
        int green = (int) (Yf - 0.813f * V - 0.391f * U);
        int blue = (int) (Yf + 2.018f * U);

        // Clip rgb values to [0-255]
        red = red < 0 ? 0 : red > 255 ? 255 : red;
        green = green < 0 ? 0 : green > 255 ? 255 : green;
        blue = blue < 0 ? 0 : blue > 255 ? 255 : blue;

        averageColor[0] += (red - averageColor[0]) / count;
        averageColor[1] += (green - averageColor[1]) / count;
        averageColor[2] += (blue - averageColor[2]) / count;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            camera.setPreviewTexture(surface);
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (Exception e) {
            Timber.d("Camera preview start failure: %s", e.getMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Nothing to do in this case.
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}