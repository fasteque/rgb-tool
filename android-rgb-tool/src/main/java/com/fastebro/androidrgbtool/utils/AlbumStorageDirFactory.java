package com.fastebro.androidrgbtool.utils;

import java.io.File;

/**
 * Created by danielealtomare on 05/03/14.
 */
abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}
