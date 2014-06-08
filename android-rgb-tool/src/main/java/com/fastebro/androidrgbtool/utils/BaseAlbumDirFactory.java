package com.fastebro.androidrgbtool.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by danielealtomare on 05/03/14.
 */
public class BaseAlbumDirFactory extends AlbumStorageDirFactory {
    @Override
    public File getAlbumStorageDir(String albumName) {
        return new File(Environment.getExternalStorageDirectory() + "/" + albumName);
    }
}
