package com.fastebro.androidrgbtool.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by danielealtomare on 27/12/14.
 */
public class PaletteSwatch implements Parcelable {
    public enum SwatchType { NONE, VIBRANT, LIGHT_VIBRANT, DARK_VIBRANT,
        MUTED, LIGHT_MUTED, DARK_MUTED; }

    private int rgb;
    private SwatchType type;

    public PaletteSwatch(int rgb, SwatchType type) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(rgb);
        dest.writeInt(type.ordinal());
    }

    public static final Parcelable.Creator<PaletteSwatch> CREATOR
            = new Parcelable.Creator<PaletteSwatch>() {
        public PaletteSwatch createFromParcel(Parcel in) {
            return new PaletteSwatch(in);
        }

        public PaletteSwatch[] newArray(int size) {
            return new PaletteSwatch[size];
        }
    };

    private PaletteSwatch(Parcel in) {
        rgb = in.readInt();
        type = SwatchType.values()[in.readInt()];   // FIXME
    }
}
