package com.fastebro.androidrgbtool.model.events;

/**
 * Created by danielealtomare on 26/12/14.
 * Project: rgb-tool
 */
public class UpdateHexValueEvent {
    public final String hexValue;

    public UpdateHexValueEvent(String hexValue) {
        this.hexValue = hexValue;
    }
}
