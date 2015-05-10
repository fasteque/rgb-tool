package com.fastebro.androidrgbtool.model.entities;

public class ScaledPicture {
    private String picturePath;
    private boolean isTempFile;

    public ScaledPicture(String picturePath, boolean isTempFile) {
        this.picturePath = picturePath;
        this.isTempFile = isTempFile;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public boolean isTempFile() {
        return isTempFile;
    }

    public void setIsTempFile(boolean isTempFile) {
        this.isTempFile = isTempFile;
    }
}
