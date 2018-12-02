package com.fastebro.androidrgbtool.palette;

import android.os.Parcel;
import android.os.Parcelable;

import com.fastebro.androidrgbtool.model.entities.Swatch;

/**
 * Created by danielealtomare on 27/12/14.
 * Project: rgb-tool
 */
public class PaletteSwatch extends Swatch implements Parcelable {

    public static final Parcelable.Creator<PaletteSwatch> CREATOR
            = new Parcelable.Creator<PaletteSwatch>() {
        public PaletteSwatch createFromParcel(Parcel in) {
            return new PaletteSwatch(in);
        }

        public PaletteSwatch[] newArray(int size) {
            return new PaletteSwatch[size];
        }
    };

    public PaletteSwatch(int rgb, SwatchType type) {
        this.setRgb(rgb);
        this.setType(type);
    }

    private PaletteSwatch(Parcel in) {
        setRgb(in.readInt());
        setType(SwatchType.values()[in.readInt()]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getRgb());
        dest.writeInt(getType().ordinal());
    }
}
