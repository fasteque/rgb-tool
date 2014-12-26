package com.fastebro.androidrgbtool.events;

/**
 * Created by danielealtomare on 26/12/14.
 */
public class ColorSelectEvent {
    public final float RGBRComponent;
    public final float RGBGComponent;
    public final float RGBBComponent;
    public final float RGBOComponent;
    public final String colorName;

    public ColorSelectEvent(float RGBRComponent,
                            float RGBGComponent,
                            float RGBBComponent,
                            float RGBOComponent,
                            String colorName) {
        this.RGBRComponent = RGBRComponent;
        this.RGBGComponent = RGBGComponent;
        this.RGBBComponent = RGBBComponent;
        this.RGBOComponent = RGBOComponent;
        this.colorName = colorName;
    }
}
