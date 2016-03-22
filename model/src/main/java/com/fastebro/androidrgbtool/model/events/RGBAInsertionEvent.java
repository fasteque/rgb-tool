package com.fastebro.androidrgbtool.model.events;

/**
 * Created by danielealtomare on 01/03/15.
 * Project: rgb-tool
 */
public class RGBAInsertionEvent {
    public final short[] rgbaValues;

    public RGBAInsertionEvent(short[] rgbaValues) {
        this.rgbaValues = rgbaValues;
    }
}
