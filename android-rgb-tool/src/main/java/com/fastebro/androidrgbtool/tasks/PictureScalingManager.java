package com.fastebro.androidrgbtool.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.fastebro.androidrgbtool.model.entities.ScaledPicture;
import com.fastebro.androidrgbtool.utils.UImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by danielealtomare on 5/10/15.
 * Project: rgb-tool
 */
public class PictureScalingManager {
    public static Observable<ScaledPicture> scalePictureObservable(final String sourcePath, final String destinationPath) {
        return Observable.create(new Observable.OnSubscribe<ScaledPicture>() {
            @Override
            public void call(Subscriber<? super ScaledPicture> subscriber) {
                try {
                    boolean useTempFile = !sourcePath.equals(destinationPath);

                    if (useTempFile) {
                        copyFile(sourcePath, destinationPath);
                    }
                    savePrescaledBitmap(destinationPath);

                    ScaledPicture scaledPicture = new ScaledPicture(destinationPath, useTempFile);
                    subscriber.onNext(scaledPicture);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static void copyFile(String inputPath, String destinationPath) {
        InputStream in;
        OutputStream out;

        try {
            in = new FileInputStream(inputPath);
            out = new FileOutputStream(destinationPath);

            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {

        }
    }

    private static void savePrescaledBitmap(String filename) throws IOException {
        File file;
        FileInputStream fis;

        BitmapFactory.Options opts;
        int resizeScale;
        Bitmap bmp;

        file = new File(filename);

        // This bit determines only the width/height of the bitmap without loading the contents
        opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        fis = new FileInputStream(file);
        BitmapFactory.decodeStream(fis, null, opts);
        fis.close();

        // Find the correct scale value. It should be a power of 2
        resizeScale = 1;

        if (opts.outHeight > UImage.JPEG_FILE_IMAGE_MAX_SIZE ||
                opts.outWidth > UImage.JPEG_FILE_IMAGE_MAX_SIZE) {
            resizeScale = (int) Math.pow(2, (int) Math.round(
                    Math.log(UImage.JPEG_FILE_IMAGE_MAX_SIZE /
                            (double) Math.max(opts.outHeight, opts.outWidth))
                            / Math.log(0.5)
            ));
        }

        // Load pre-scaled bitmap
        opts = new BitmapFactory.Options();
        opts.inSampleSize = resizeScale;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        fis = new FileInputStream(file);

        bmp = BitmapFactory.decodeStream(fis, null, opts);

        fis.close();

        // Adjust image orientation
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, bounds);

        ExifInterface exif = new ExifInterface(filename);
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;

        if (orientation == ExifInterface.ORIENTATION_UNDEFINED) {
            rotationAngle = 0;
        }

        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            rotationAngle = 90;
        }

        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            rotationAngle = 180;
        }

        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            rotationAngle = 270;
        }

        // Android BUG: fix orientation if value is equal to 0.
        if (orientation == 0) {
            // set orientation to portrait
            if (bmp.getHeight() > bmp.getWidth()) {
                rotationAngle = 0;
            } else {
                rotationAngle = 90;
            }
        }

        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0,
                bmp.getWidth(),
                bmp.getHeight(), matrix, true);

        // Compress image
        File scaledImageFile = new File(filename);

        OutputStream outStream = new FileOutputStream(scaledImageFile);
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
        outStream.flush();
        outStream.close();
    }
}