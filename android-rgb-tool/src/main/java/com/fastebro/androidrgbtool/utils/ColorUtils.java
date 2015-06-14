package com.fastebro.androidrgbtool.utils;

import android.graphics.Color;
import android.support.annotation.FloatRange;

public class ColorUtils {
    /**
     * @param n
     * @return
     */
    public static String getRGB(float n) {
        return String.format("%.0f", n).replaceAll("\\.0*$", "");
    }

    /**
     * @param n
     * @return
     */
    public static String RGBToHex(float n) {
        StringBuffer sb = new StringBuffer();
        sb.append(Integer.toHexString((int) n));
        // Add '0' character at first index if the string length < 2.
        if (sb.length() < 2) {
            sb.insert(0, '0');
        }

        return sb.toString().toUpperCase();
    }

    /**
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static float[] RGBToHSB(@FloatRange(from=0.0, to=255.0) float r,
                                   @FloatRange(from=0.0, to=255.0) float g,
                                   @FloatRange(from=0.0, to=255.0) float b) {
        float[] hsb = new float[3];
        Color.RGBToHSV((int) r, (int) g, (int) b, hsb);
        return hsb;
    }

    /**
     * @param rgbRColor
     * @param rgbGColor
     * @param rgbBColor
     * @param rgbOpacity
     * @return
     */
    public static String getColorMessage(@FloatRange(from=0.0, to=255.0) float rgbRColor,
                                         @FloatRange(from=0.0, to=255.0) float rgbGColor,
                                         @FloatRange(from=0.0, to=255.0) float rgbBColor,
                                         @FloatRange(from=0.0, to=255.0) float rgbOpacity) {
        StringBuilder message = new StringBuilder();

        message.append("RGB Tool");
        message.append(System.getProperty("line.separator"));

        message.append("RGB - ");
        message.append("R: ");
        message.append(ColorUtils.getRGB(rgbRColor));
        message.append("  G: ");
        message.append(ColorUtils.getRGB(rgbGColor));
        message.append("  B: ");
        message.append(ColorUtils.getRGB(rgbBColor));
        message.append(System.getProperty("line.separator"));

        message.append("Opacity: ");
        message.append(ColorUtils.getRGB(rgbOpacity));
        message.append(System.getProperty("line.separator"));

        message.append("HSB - ");
        float[] hsb = ColorUtils.RGBToHSB(rgbRColor, rgbGColor, rgbBColor);
        message.append("H: ");
        message.append(String.format("%.0f", hsb[0]));
        message.append("  S: ");
        message.append(String.format("%.0f%%", (hsb[1] * 100.0f)));
        message.append("  B: ");
        message.append(String.format("%.0f%%", (hsb[2] * 100.0f)));
        message.append(System.getProperty("line.separator"));

        message.append("HEX - ");
        message.append(String.format("#%s%s%s%s",
                ColorUtils.RGBToHex(rgbOpacity),
                ColorUtils.RGBToHex(rgbRColor),
                ColorUtils.RGBToHex(rgbGColor),
                ColorUtils.RGBToHex(rgbBColor)));
        message.append(System.getProperty("line.separator"));

        return message.toString();
    }

    /**
     *
     * @param hexValue
     * @return
     */
    public static int[] hexToRGB(String hexValue) {
        int[] rgb = new int[3];

        if(!"".equals(hexValue)) {
            int rgbValue = Color.parseColor("#" + hexValue);
            rgb[0] = (rgbValue & 0xFF0000) >> 16;
            rgb[1] = (rgbValue & 0xFF00) >> 8;
            rgb[2] = (rgbValue & 0xFF);
        }

        return rgb;
    }
}
