package com.fastebro.androidrgbtool.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by danielealtomare on 03/03/14.
 */
public class UImage {
    public final static String EXTRA_JPEG_FILE_PATH = "com.fastebro.androidrgbtool.extra.jpeg.file.path";
    public final static String EXTRA_DELETE_FILE = "com.fastebro.androidrgbtool.extra.delete.file";
    public final static String JPEG_FILE_PREFIX = "IMG_";
    public final static String JPEG_FILE_SUFFIX = ".jpg";
    public static final int JPEG_FILE_IMAGE_MAX_SIZE = 1080;

}
