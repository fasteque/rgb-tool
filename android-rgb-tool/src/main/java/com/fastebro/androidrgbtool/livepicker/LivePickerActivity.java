package com.fastebro.androidrgbtool.livepicker;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.utils.CameraUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LivePickerActivity extends AppCompatActivity {
	/**
	 * Conversion from screen rotation to JPEG orientation.
	 */
	private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
	private static final int REQUEST_CAMERA_PERMISSION = 1;
	private static final String FRAGMENT_DIALOG = "dialog";

	static {
		ORIENTATIONS.append(Surface.ROTATION_0, 90);
		ORIENTATIONS.append(Surface.ROTATION_90, 0);
		ORIENTATIONS.append(Surface.ROTATION_180, 270);
		ORIENTATIONS.append(Surface.ROTATION_270, 180);
	}

	/**
	 * Camera state: Showing camera preview.
	 */
	private static final int STATE_PREVIEW = 0;

	/**
	 * Camera state: Waiting for the focus to be locked.
	 */
	private static final int STATE_WAITING_LOCK = 1;

	/**
	 * Camera state: Waiting for the exposure to be precapture state.
	 */
	private static final int STATE_WAITING_PRECAPTURE = 2;

	/**
	 * Camera state: Waiting for the exposure state to be something other than precapture.
	 */
	private static final int STATE_WAITING_NON_PRECAPTURE = 3;

	/**
	 * Camera state: Picture was taken.
	 */
	private static final int STATE_PICTURE_TAKEN = 4;

	/**
	 * Max preview width that is guaranteed by Camera2 API
	 */
	private static final int MAX_PREVIEW_WIDTH = 1920;

	/**
	 * Max preview height that is guaranteed by Camera2 API
	 */
	private static final int MAX_PREVIEW_HEIGHT = 1080;

	/**
	 * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
	 * {@link TextureView}.
	 */
	private final TextureView.SurfaceTextureListener surfaceTextureListener
			= new TextureView.SurfaceTextureListener() {

		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
			openCamera(isFrontCamera ? CameraCharacteristics.LENS_FACING_FRONT : CameraCharacteristics
					.LENS_FACING_BACK, width, height);
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
			configureTransform(width, height);
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture texture) {
		}

	};

	private boolean isFrontCamera = false;

	/**
	 * ID of the current {@link CameraDevice}.
	 */
	private String cameraId;

	/**
	 * A {@link CameraCaptureSession } for camera preview.
	 */
	private CameraCaptureSession captureSession;

	/**
	 * A reference to the opened {@link CameraDevice}.
	 */
	private CameraDevice cameraDevice;

	/**
	 * The {@link android.util.Size} of camera preview.
	 */
	private Size previewSize;

	/**
	 * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
	 */
	private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {

		@Override
		public void onOpened(@NonNull CameraDevice cameraDevice) {
			// This method is called when the camera is opened.  We start camera preview here.
			cameraOpenCloseLock.release();
			LivePickerActivity.this.cameraDevice = cameraDevice;
			createCameraPreviewSession();
		}

		@Override
		public void onDisconnected(@NonNull CameraDevice cameraDevice) {
			cameraOpenCloseLock.release();
			cameraDevice.close();
			LivePickerActivity.this.cameraDevice = null;
		}

		@Override
		public void onError(@NonNull CameraDevice cameraDevice, int error) {
			cameraOpenCloseLock.release();
			cameraDevice.close();
			LivePickerActivity.this.cameraDevice = null;
			finish();
		}

	};

	/**
	 * An additional thread for running tasks that shouldn't block the UI.
	 */
	private HandlerThread backgroundThread;

	/**
	 * A {@link Handler} for running tasks in the background.
	 */
	private Handler backgroundHandler;

	/**
	 * An {@link ImageReader} that handles still image capture.
	 */
	private ImageReader imageReader;

	/**
	 * This is the output file for our picture.
	 */
	private File outputFile;

	/**
	 * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
	 * still image is ready to be saved.
	 */
	private final ImageReader.OnImageAvailableListener onImageAvailableListener
			= new ImageReader.OnImageAvailableListener() {

		@Override
		public void onImageAvailable(ImageReader reader) {
			backgroundHandler.post(new ImageSaver(reader.acquireNextImage(), outputFile));
		}

	};

	/**
	 * {@link CaptureRequest.Builder} for the camera preview
	 */
	private CaptureRequest.Builder previewRequestBuilder;

	/**
	 * {@link CaptureRequest} generated by {@link #previewRequestBuilder}
	 */
	private CaptureRequest previewRequest;

	/**
	 * The current state of camera state for taking pictures.
	 *
	 * @see #captureCallback
	 */
	private int cameraState = STATE_PREVIEW;

	/**
	 * A {@link Semaphore} to prevent the app from exiting before closing the camera.
	 */
	private Semaphore cameraOpenCloseLock = new Semaphore(1);

	/**
	 * Whether the current camera device supports Flash or not.
	 */
	private boolean flashSupported;

	/**
	 * Orientation of the camera sensor
	 */
	private int sensorOrientation;

	private boolean isPortrait;
	private int pointedColor;

	/**
	 * An {@link LivePickerTextureView} for camera preview.
	 */
	@BindView(R.id.live_picker_texture)
	LivePickerTextureView textureView;
	@BindView(R.id.live_picker_pointer_stroke)
	View pointerRing;
	@BindView(R.id.live_picker_camera_flash)
	ImageButton btnFlash;
	private boolean isFlashOn;
	@BindView(R.id.live_picker_flip_camera)
	ImageButton btnFlipCamera;

	/**
	 * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
	 */
	private CameraCaptureSession.CaptureCallback captureCallback
			= new CameraCaptureSession.CaptureCallback() {

		private void process(CaptureResult result) {
			switch (cameraState) {
				case STATE_PREVIEW: {
					// We have nothing to do when the camera preview is working normally.
					break;
				}
				case STATE_WAITING_LOCK: {
					Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
					if (afState == null) {
						captureStillPicture();
					} else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
							CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
						// CONTROL_AE_STATE can be null on some devices
						Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
						if (aeState == null ||
								aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
							cameraState = STATE_PICTURE_TAKEN;
							captureStillPicture();
						} else {
							runPrecaptureSequence();
						}
					}
					break;
				}
				case STATE_WAITING_PRECAPTURE: {
					// CONTROL_AE_STATE can be null on some devices
					Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
					if (aeState == null ||
							aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
							aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
						cameraState = STATE_WAITING_NON_PRECAPTURE;
					}
					break;
				}
				case STATE_WAITING_NON_PRECAPTURE: {
					// CONTROL_AE_STATE can be null on some devices
					Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
					if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
						cameraState = STATE_PICTURE_TAKEN;
						captureStillPicture();
					}
					break;
				}
			}
		}

		@Override
		public void onCaptureProgressed(@NonNull CameraCaptureSession session,
		                                @NonNull CaptureRequest request,
		                                @NonNull CaptureResult partialResult) {
			process(partialResult);
		}

		@Override
		public void onCaptureCompleted(@NonNull CameraCaptureSession session,
		                               @NonNull CaptureRequest request,
		                               @NonNull TotalCaptureResult result) {
			process(result);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_live_picker);
		ButterKnife.bind(this);

		initView();
	}

	@Override
	public void onResume() {
		super.onResume();

		// Activate fullscreen mode.
		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

		startBackgroundThread();

		// When the screen is turned off and turned back on, the SurfaceTexture is already
		// available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
		// a camera and start preview from here (otherwise, we wait until the surface is ready in
		// the SurfaceTextureListener).
		if (textureView.isAvailable()) {
			openCamera(isFrontCamera ? CameraCharacteristics.LENS_FACING_FRONT : CameraCharacteristics
							.LENS_FACING_BACK,
					textureView.getWidth(), textureView.getHeight());
		} else {
			textureView.setSurfaceTextureListener(surfaceTextureListener);
		}
	}

	@Override
	public void onPause() {
		closeCamera();
		stopBackgroundThread();
		super.onPause();
	}

	private void initView() {
		isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		outputFile = new File(getExternalFilesDir(null), "pic.jpg");

		// Check if the device has a front camera as well.
		if (CameraUtils.getCameraId((CameraManager) getSystemService(CAMERA_SERVICE), CameraCharacteristics
				.LENS_FACING_FRONT) == null) {
			btnFlipCamera.setVisibility(View.GONE);
		} else {
			btnFlipCamera.setOnClickListener(view -> {
				isFrontCamera = !isFrontCamera;
				closeCamera();
				openCamera(isFrontCamera ? CameraCharacteristics.LENS_FACING_FRONT : CameraCharacteristics
								.LENS_FACING_BACK,
						textureView.getWidth(), textureView.getHeight());
			});
		}
	}

	/**
	 * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
	 * is at least as large as the respective texture view size, and that is at most as large as the
	 * respective max size, and whose aspect ratio matches with the specified value. If such size
	 * doesn't exist, choose the largest one that is at most as large as the respective max size,
	 * and whose aspect ratio matches with the specified value.
	 *
	 * @param choices           The list of sizes that the camera supports for the intended output
	 *                          class
	 * @param textureViewWidth  The width of the texture view relative to sensor coordinate
	 * @param textureViewHeight The height of the texture view relative to sensor coordinate
	 * @param maxWidth          The maximum width that can be chosen
	 * @param maxHeight         The maximum height that can be chosen
	 * @param aspectRatio       The aspect ratio
	 * @return The optimal {@code Size}, or an arbitrary one if none were big enough
	 */
	private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
	                                      int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

		// Collect the supported resolutions that are at least as big as the preview Surface
		List<Size> bigEnough = new ArrayList<>();
		// Collect the supported resolutions that are smaller than the preview Surface
		List<Size> notBigEnough = new ArrayList<>();
		int w = aspectRatio.getWidth();
		int h = aspectRatio.getHeight();
		for (Size option : choices) {
			if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
					option.getHeight() == option.getWidth() * h / w) {
				if (option.getWidth() >= textureViewWidth &&
						option.getHeight() >= textureViewHeight) {
					bigEnough.add(option);
				} else {
					notBigEnough.add(option);
				}
			}
		}

		// Pick the smallest of those big enough. If there is no one big enough, pick the
		// largest of those not big enough.
		if (bigEnough.size() > 0) {
			return Collections.min(bigEnough, new CompareSizesByArea());
		} else if (notBigEnough.size() > 0) {
			return Collections.max(notBigEnough, new CompareSizesByArea());
		} else {
			return choices[0];
		}
	}

	private void requestCameraPermission() {
//        if (FragmentCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
//            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
//        } else {
//            FragmentCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
//                    REQUEST_CAMERA_PERMISSION);
//        }
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
	                                       @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                ErrorDialog.newInstance(getString(R.string.request_permission))
//                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
//            }
//        } else {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
	}

	/**
	 * Sets up member variables related to camera.
	 *
	 * @param width  The width of available size for camera preview
	 * @param height The height of available size for camera preview
	 */
	private void setUpCameraOutputs(int lensFacing, int width, int height) {
		CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		try {
			for (String cameraId : manager.getCameraIdList()) {
				CameraCharacteristics characteristics
						= manager.getCameraCharacteristics(cameraId);

				// We don't use a front facing camera in this sample.
				Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
				if (facing == null || facing != lensFacing) {
					continue;
				}

				StreamConfigurationMap map = characteristics.get(
						CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
				if (map == null) {
					continue;
				}

				// For still image captures, we use the largest available size.
				Size largest = Collections.max(
						Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
						new CompareSizesByArea());
				imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
						ImageFormat.JPEG, /*maxImages*/2);
				imageReader.setOnImageAvailableListener(
						onImageAvailableListener, backgroundHandler);

				// Find out if we need to swap dimension to get the preview size relative to sensor
				// coordinate.
				int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
				//noinspection ConstantConditions
				sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
				boolean swappedDimensions = false;
				switch (displayRotation) {
					case Surface.ROTATION_0:
					case Surface.ROTATION_180:
						if (sensorOrientation == 90 || sensorOrientation == 270) {
							swappedDimensions = true;
						}
						break;
					case Surface.ROTATION_90:
					case Surface.ROTATION_270:
						if (sensorOrientation == 0 || sensorOrientation == 180) {
							swappedDimensions = true;
						}
						break;
					default:
						break;
				}

				Point displaySize = new Point();
				getWindowManager().getDefaultDisplay().getSize(displaySize);
				int rotatedPreviewWidth = width;
				int rotatedPreviewHeight = height;
				int maxPreviewWidth = displaySize.x;
				int maxPreviewHeight = displaySize.y;

				if (swappedDimensions) {
					rotatedPreviewWidth = height;
					rotatedPreviewHeight = width;
					maxPreviewWidth = displaySize.y;
					maxPreviewHeight = displaySize.x;
				}

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

				// Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
				// bus' bandwidth limitation, resulting in gorgeous previews but the storage of
				// garbage capture data.
				previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
						rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
						maxPreviewHeight, largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(
                            previewSize.getWidth(), previewSize.getHeight());
                } else {
                    textureView.setAspectRatio(
                            previewSize.getHeight(), previewSize.getWidth());
                }

				// Check if the flash is supported.
				Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
				flashSupported = available == null ? false : available;

				this.cameraId = cameraId;
				return;
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			// Currently an NPE is thrown when the Camera2API is used but not supported on the
			// device this code runs.
			// TODO
		}
	}

	/**
	 * Opens the camera specified by {@link LivePickerActivity#cameraId}.
	 */
	private void openCamera(int lensFacing, int width, int height) {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED) {
			requestCameraPermission();
			return;
		}
		setUpCameraOutputs(lensFacing, width, height);
		configureTransform(width, height);
		CameraManager manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
		try {
			if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
				throw new RuntimeException("Time out waiting to lock camera opening.");
			}
			manager.openCamera(cameraId, stateCallback, backgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
		}
	}

	/**
	 * Closes the current {@link CameraDevice}.
	 */
	private void closeCamera() {
		try {
			cameraOpenCloseLock.acquire();
			if (null != captureSession) {
				captureSession.close();
				captureSession = null;
			}
			if (null != cameraDevice) {
				cameraDevice.close();
				cameraDevice = null;
			}
			if (null != imageReader) {
				imageReader.close();
				imageReader = null;
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
		} finally {
			cameraOpenCloseLock.release();
		}
	}

	/**
	 * Starts a background thread and its {@link Handler}.
	 */
	private void startBackgroundThread() {
		backgroundThread = new HandlerThread("CameraBackground");
		backgroundThread.start();
		backgroundHandler = new Handler(backgroundThread.getLooper());
	}

	/**
	 * Stops the background thread and its {@link Handler}.
	 */
	private void stopBackgroundThread() {
		backgroundThread.quitSafely();
		try {
			backgroundThread.join();
			backgroundThread = null;
			backgroundHandler = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new {@link CameraCaptureSession} for camera preview.
	 */
	private void createCameraPreviewSession() {
		try {
			SurfaceTexture texture = textureView.getSurfaceTexture();
			assert texture != null;

			// We configure the size of default buffer to be the size of camera preview we want.
			texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

			// This is the output Surface we need to start preview.
			Surface surface = new Surface(texture);

			// We set up a CaptureRequest.Builder with the output Surface.
			previewRequestBuilder
					= cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			previewRequestBuilder.addTarget(surface);

			// Here, we create a CameraCaptureSession for camera preview.
			cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
					new CameraCaptureSession.StateCallback() {

						@Override
						public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
							// The camera is already closed
							if (null == cameraDevice) {
								return;
							}

							// When the session is ready, we start displaying the preview.
							captureSession = cameraCaptureSession;
							try {
								// Auto focus should be continuous for camera preview.
								previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
										CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
								// Flash is automatically enabled when necessary.
								setAutoFlash(previewRequestBuilder);

								// Finally, we start displaying the camera preview.
								previewRequest = previewRequestBuilder.build();
								captureSession.setRepeatingRequest(previewRequest,
										captureCallback, backgroundHandler);
							} catch (CameraAccessException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onConfigureFailed(
								@NonNull CameraCaptureSession cameraCaptureSession) {
							// TODO
						}
					}, null
			);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configures the necessary {@link android.graphics.Matrix} transformation to `textureView`.
	 * This method should be called after the camera preview size is determined in
	 * setUpCameraOutputs and also the size of `textureView` is fixed.
	 *
	 * @param viewWidth  The width of `textureView`
	 * @param viewHeight The height of `textureView`
	 */
	private void configureTransform(int viewWidth, int viewHeight) {
		if (null == textureView || null == previewSize || null == this) {
			return;
		}
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		Matrix matrix = new Matrix();
		RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
		RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
		float centerX = viewRect.centerX();
		float centerY = viewRect.centerY();
		if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
			bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
			matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
			float scale = Math.max(
					(float) viewHeight / previewSize.getHeight(),
					(float) viewWidth / previewSize.getWidth());
			matrix.postScale(scale, scale, centerX, centerY);
			matrix.postRotate(90 * (rotation - 2), centerX, centerY);
		} else if (Surface.ROTATION_180 == rotation) {
			matrix.postRotate(180, centerX, centerY);
		}
		textureView.setTransform(matrix);
	}

	/**
	 * Initiate a still image capture.
	 */
	private void takePicture() {
		lockFocus();
	}

	/**
	 * Lock the focus as the first step for a still image capture.
	 */
	private void lockFocus() {
		try {
			// This is how to tell the camera to lock focus.
			previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
					CameraMetadata.CONTROL_AF_TRIGGER_START);
			// Tell #captureCallback to wait for the lock.
			cameraState = STATE_WAITING_LOCK;
			captureSession.capture(previewRequestBuilder.build(), captureCallback,
					backgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Run the precapture sequence for capturing a still image. This method should be called when
	 * we get a response in {@link #captureCallback} from {@link #lockFocus()}.
	 */
	private void runPrecaptureSequence() {
		try {
			// This is how to tell the camera to trigger.
			previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
					CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
			// Tell #captureCallback to wait for the precapture sequence to be set.
			cameraState = STATE_WAITING_PRECAPTURE;
			captureSession.capture(previewRequestBuilder.build(), captureCallback,
					backgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Capture a still picture. This method should be called when we get a response in
	 * {@link #captureCallback} from both {@link #lockFocus()}.
	 */
	private void captureStillPicture() {
		try {
			if (null == cameraDevice) {
				return;
			}
			// This is the CaptureRequest.Builder that we use to take a picture.
			final CaptureRequest.Builder captureBuilder =
					cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
			captureBuilder.addTarget(imageReader.getSurface());

			// Use the same AE and AF modes as the preview.
			captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
					CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
			setAutoFlash(captureBuilder);

			// Orientation
			int rotation = getWindowManager().getDefaultDisplay().getRotation();
			captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

			CameraCaptureSession.CaptureCallback CaptureCallback
					= new CameraCaptureSession.CaptureCallback() {

				@Override
				public void onCaptureCompleted(@NonNull CameraCaptureSession session,
				                               @NonNull CaptureRequest request,
				                               @NonNull TotalCaptureResult result) {
					// TODO
					unlockFocus();
				}
			};

			captureSession.stopRepeating();
			captureSession.capture(captureBuilder.build(), CaptureCallback, null);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves the JPEG orientation from the specified screen rotation.
	 *
	 * @param rotation The screen rotation.
	 * @return The JPEG orientation (one of 0, 90, 270, and 360)
	 */
	private int getOrientation(int rotation) {
		// Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
		// We have to take that into account and rotate JPEG properly.
		// For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
		// For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
		return (ORIENTATIONS.get(rotation) + sensorOrientation + 270) % 360;
	}

	/**
	 * Unlock the focus. This method should be called when still image capture sequence is
	 * finished.
	 */
	private void unlockFocus() {
		try {
			// Reset the auto-focus trigger
			previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
					CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
			setAutoFlash(previewRequestBuilder);
			captureSession.capture(previewRequestBuilder.build(), captureCallback,
					backgroundHandler);
			// After this, the camera will go back to the normal state of preview.
			cameraState = STATE_PREVIEW;
			captureSession.setRepeatingRequest(previewRequest, captureCallback,
					backgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
		if (flashSupported) {
			requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
					CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
		}
	}

	/**
	 * Saves a JPEG {@link Image} into the specified {@link File}.
	 */
	private static class ImageSaver implements Runnable {

		/**
		 * The JPEG image
		 */
		private final Image mImage;
		/**
		 * The file we save the image into.
		 */
		private final File mFile;

		public ImageSaver(Image image, File file) {
			mImage = image;
			mFile = file;
		}

		@Override
		public void run() {
			ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			FileOutputStream output = null;
			try {
				output = new FileOutputStream(mFile);
				output.write(bytes);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				mImage.close();
				if (null != output) {
					try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	/**
	 * Compares two {@code Size}s based on their areas.
	 */
	static class CompareSizesByArea implements Comparator<Size> {

		@Override
		public int compare(Size lhs, Size rhs) {
			// We cast here to ensure the multiplications won't overflow
			return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
					(long) rhs.getWidth() * rhs.getHeight());
		}

	}

	/**
	 * Shows an error message dialog.
	 */
	public static class ErrorDialog extends DialogFragment {

		private static final String ARG_MESSAGE = "message";

		public static ErrorDialog newInstance(String message) {
			ErrorDialog dialog = new ErrorDialog();
			Bundle args = new Bundle();
			args.putString(ARG_MESSAGE, message);
			dialog.setArguments(args);
			return dialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity activity = getActivity();
			return new AlertDialog.Builder(activity)
					.setMessage(getArguments().getString(ARG_MESSAGE))
					.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> activity.finish())
					.create();
		}

	}

	/**
	 * Shows OK/Cancel confirmation dialog about camera permission.
	 */
//    public static class ConfirmationDialog extends DialogFragment {
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            final Fragment parent = getParentFragment();
//            return new AlertDialog.Builder(getActivity())
//                    .setMessage(R.string.request_permission)
//                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            FragmentCompat.requestPermissions(parent,
//                                    new String[]{Manifest.permission.CAMERA},
//                                    REQUEST_CAMERA_PERMISSION);
//                        }
//                    })
//                    .setNegativeButton(android.R.string.cancel,
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    Activity activity = parent.getActivity();
//                                    if (activity != null) {
//                                        activity.finish();
//                                    }
//                                }
//                            })
//                    .create();
//        }
//    }
        /*implements LivePickerTextureView.OnColorPointedListener,
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
            Timber.d("Error getting camera instance: %s", e.getMessage());
        }
        return camera;
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

        FrameLayout.LayoutParams previewParams;

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
    */
}
