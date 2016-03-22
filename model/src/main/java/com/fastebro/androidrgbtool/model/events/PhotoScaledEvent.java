package com.fastebro.androidrgbtool.model.events;

/**
 * Created by danielealtomare on 26/12/14.
 * Project: rgb-tool
 */
public class PhotoScaledEvent {
    public final String photoPath;
    public final boolean deleteFile;

    public PhotoScaledEvent(String photoPath, boolean deleteFile) {
        this.photoPath = photoPath;
        this.deleteFile = deleteFile;
    }
}
