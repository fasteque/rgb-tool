package com.fastebro.androidrgbtool.model.events;

/**
 * Created by danielealtomare on 26/12/14.
 * Project: rgb-tool
 */
public class ColorSelectEvent {
    public final int RGBRComponent;
    public final int RGBGComponent;
    public final int RGBBComponent;
    public final int RGBOComponent;
    public final String colorName;

    public ColorSelectEvent(int RGBRComponent,
                            int RGBGComponent,
                            int RGBBComponent,
                            int RGBOComponent,
                            String colorName) {
        this.RGBRComponent = RGBRComponent;
        this.RGBGComponent = RGBGComponent;
        this.RGBBComponent = RGBBComponent;
        this.RGBOComponent = RGBOComponent;
        this.colorName = colorName;
    }
}
