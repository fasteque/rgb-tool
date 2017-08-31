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
		return new File(Environment.getExternalStorageDirectory() + "/" + albumName);
	}
}
