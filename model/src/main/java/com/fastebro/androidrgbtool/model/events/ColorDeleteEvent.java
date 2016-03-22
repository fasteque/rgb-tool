package com.fastebro.androidrgbtool.model.events;

/**
 * Created by danielealtomare on 17/05/14.
 * Project: rgb-tool
 */
public class ColorDeleteEvent {
    public final int colorId;

    public ColorDeleteEvent(int colorId) {
        this.colorId = colorId;
    }
}
