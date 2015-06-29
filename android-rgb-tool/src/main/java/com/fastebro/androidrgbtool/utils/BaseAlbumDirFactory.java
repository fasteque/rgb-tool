package com.fastebro.androidrgbtool.utils;

import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by danielealtomare on 05/03/14.
 * Project: rgb-tool
 */
public class BaseAlbumDirFactory extends AlbumStorageDirFactory {
    @Override
    public File getAlbumStorageDir(@NonNull String albumName) {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = getAlbumStorageDir(albumName);
            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        return null;
                    }
                }
            }
        }

        return storageDir;
    }
}
