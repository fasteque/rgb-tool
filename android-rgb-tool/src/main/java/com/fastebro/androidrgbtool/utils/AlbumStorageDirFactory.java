package com.fastebro.androidrgbtool.utils;

import java.io.File;

/**
 * Created by danielealtomare on 05/03/14.
 * Project: rgb-tool
 */
abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}
