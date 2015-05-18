package com.fastebro.androidrgbtool.model.entities;

public class Swatch {
    public enum SwatchType { NONE, VIBRANT, LIGHT_VIBRANT, DARK_VIBRANT,
        MUTED, LIGHT_MUTED, DARK_MUTED }

    protected int rgb;
    protected SwatchType type;

    public Swatch() {
        this.rgb = 0;
        this.type = SwatchType.NONE;
    }

    public Swatch(int rgb, SwatchType type) {
        this.rgb = rgb;
        this.type = type;
    }

    public int getRgb() {
        return rgb;
    }

    public void setRgb(int rgb) {
        this.rgb = rgb;
    }

    public SwatchType getType() {
        return type;
    }

    public void setType(SwatchType type) {
        this.type = type;
    }
}
