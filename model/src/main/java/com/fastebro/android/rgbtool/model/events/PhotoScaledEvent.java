package com.fastebro.android.rgbtool.model.events;

/**
 * Created by danielealtomare on 26/12/14.
 */
public class PhotoScaledEvent {
    public final String photoPath;
    public final boolean deleteFile;

    public PhotoScaledEvent(String photoPath, boolean deleteFile) {
        this.photoPath = photoPath;
        this.deleteFile = deleteFile;
    }
}
