package com.fastebro.androidrgbtool.events;

/**
 * Created by danielealtomare on 01/03/15.
 */
public class RGBAInsertionEvent {
    public final short[] rgbaValues;

    public RGBAInsertionEvent(short[] rgbaValues) {
        this.rgbaValues = rgbaValues;
    }
}
