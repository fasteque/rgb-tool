package com.fastebro.android.rgbtool.model.events;

/**
 * Created by danielealtomare on 17/05/14.
 */
public class ColorDeleteEvent {
    public final int colorId;

    public ColorDeleteEvent(int colorId) {
        this.colorId = colorId;
    }
}
