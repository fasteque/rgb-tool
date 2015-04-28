package com.fastebro.androidrgbtool.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fastebro.androidrgbtool.model.entities.Swatch;

/**
 * Created by danielealtomare on 27/12/14.
 */
public class PaletteSwatch extends Swatch implements Parcelable {

    public PaletteSwatch(int rgb, SwatchType type) {
        this.rgb = rgb;
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
