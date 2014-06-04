package com.fastebro.androidrgbtool.utils;

import android.graphics.Color;

public class UColor
{
    /**
     * @param n
     * @return
     */
    public static String getRGB(float n)
    {
        return String.format("%f", n).replaceAll("\\.0*$", "");
    }


    /**
     * @param n
     * @return
     */
    public static String RGBToHex(float n)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(Integer.toHexString((int) n));
        // Add '0' character at first index if the string length < 2.
        if (sb.length() < 2) {
            sb.insert(0, '0');
        }

        return sb.toString().toUpperCase();
    }


    /**
     *
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static float[] RGBToHSB(float r, float g, float b)
    {
        float[] hsb = new float[3];
        Color.RGBToHSV((int) r, (int) g, (int) b, hsb);
        return hsb;
    }


    /**
     *
     * @param rgbRColor
     * @param rgbGColor
     * @param rgbBColor
     * @param rgbOpacity
     * @return
     */
    public static String getColorMessage(float rgbRColor,
                                        float rgbGColor,
                                        float rgbBColor,
                                        float rgbOpacity)
    {
        StringBuilder message = new StringBuilder();

        message.append("RGB Tool");
        message.append(System.getProperty("line.separator"));

        message.append("RGB - ");
        message.append("R: " + UColor.getRGB(rgbRColor));
        message.append("  G: " + UColor.getRGB(rgbGColor));
        message.append("  B: " + UColor.getRGB(rgbBColor));
        message.append(System.getProperty("line.separator"));

        message.append("Opacity: " + UColor.getRGB(rgbOpacity));
        message.append(System.getProperty("line.separator"));

        message.append("HSB - ");
        float[] hsb = UColor.RGBToHSB(rgbRColor, rgbGColor, rgbBColor);
        message.append("H: " + String.format("%.0f", hsb[0]));
        message.append("  S: " + String.format("%.0f%%", (hsb[1] * 100.0f)));
        message.append("  B: " + String.format("%.0f%%", (hsb[2] * 100.0f)));
        message.append(System.getProperty("line.separator"));

        message.append("HEX - " + String.format("#%s%s%s%s",
                UColor.RGBToHex(rgbOpacity),
                UColor.RGBToHex(rgbRColor),
                UColor.RGBToHex(rgbGColor),
                UColor.RGBToHex(rgbBColor)));
        message.append(System.getProperty("line.separator"));

        return message.toString();
    }
}
