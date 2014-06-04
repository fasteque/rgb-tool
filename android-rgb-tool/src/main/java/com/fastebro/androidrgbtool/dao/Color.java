package com.fastebro.androidrgbtool.dao;

public class Color {
    private short id;
    private String colorName;
    private String colorHex;
    private short colorRGB_R;
    private short colorRGB_G;
    private short colorRGB_B;
    private short colorHSB_H;
    private short colorHSB_S;
    private short colorHSB_B;
    private boolean colorFavorite;

    public void setId(short id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorRGB(short r, short g, short b) {
        this.colorRGB_R = r;
        this.colorRGB_G = g;
        this.colorRGB_B = b;
    }

    public short[] getColorRGB() {
        return new short[] { colorRGB_R, colorRGB_G, colorRGB_B };
    }

    public void setColorHSB(short h, short s, short b) {
        this.colorHSB_H = h;
        this.colorHSB_S = s;
        this.colorHSB_B = b;
    }

    public short[] getColorHSB() {
        return new short[] { colorHSB_H, colorHSB_S, colorHSB_B };
    }

    public void setColorFavorite(boolean colorFavorite) {
        this.colorFavorite = colorFavorite;
    }

    public boolean getColorFavorite() {
        return colorFavorite;
    }

    @Override
    public String toString() {
        return colorName;
    }
}
