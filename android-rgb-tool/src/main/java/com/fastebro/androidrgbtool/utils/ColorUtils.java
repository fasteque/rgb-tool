package com.fastebro.androidrgbtool.utils;

import android.graphics.Color;
import android.support.annotation.IntRange;

public final class ColorUtils {

    private ColorUtils() {

    }

    public static String getRGB(float n) {
        return String.format("%.0f", n).replaceAll("\\.0*$", "");
    }

    public static String RGBToHex(int n) {
        StringBuffer sb = new StringBuffer();
        sb.append(Integer.toHexString(n));
        // Add '0' character at first index if the string length < 2.
        if (sb.length() < 2) {
            sb.insert(0, '0');
        }

        return sb.toString().toUpperCase();
    }

    public static float[] RGBToHSB(@IntRange(from=0, to=255) int r,
                                   @IntRange(from=0, to=255) int g,
                                   @IntRange(from=0, to=255) int b) {
        float[] hsb = new float[3];
        Color.RGBToHSV(r, g, b, hsb);
        return hsb;
    }

    public static String getColorMessage(@IntRange(from=0, to=255) int rgbRColor,
                                         @IntRange(from=0, to=255) int rgbGColor,
                                         @IntRange(from=0, to=255) int rgbBColor,
                                         @IntRange(from=0, to=255) int rgbOpacity) {
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

    public static String getComplementaryColorMessage(short[] argbColorValues, short[] argbComplementaryColorValues,
                                                      short[] argbContrastColorValues) {
        StringBuilder message = new StringBuilder();

        message.append("RGB Tool");
        message.append(System.getProperty("line.separator"));

        message.append("Color - ");
        message.append(String.format("#%s%s%s%s",
                ColorUtils.RGBToHex(argbColorValues[0]),
                ColorUtils.RGBToHex(argbColorValues[1]),
                ColorUtils.RGBToHex(argbColorValues[2]),
                ColorUtils.RGBToHex(argbColorValues[3])));
        message.append(System.getProperty("line.separator"));

        // Opacity is fixed as FF.
        message.append("Complementary - ");
        message.append(String.format("#FF%s%s%s",
                ColorUtils.RGBToHex(argbComplementaryColorValues[1]),
                ColorUtils.RGBToHex(argbComplementaryColorValues[2]),
                ColorUtils.RGBToHex(argbComplementaryColorValues[3])));
        message.append(System.getProperty("line.separator"));

        // Opacity is fixed as FF.
        message.append("Contrast - ");
        message.append(String.format("#FF%s%s%s",
                ColorUtils.RGBToHex(argbContrastColorValues[1]),
                ColorUtils.RGBToHex(argbContrastColorValues[2]),
                ColorUtils.RGBToHex(argbContrastColorValues[3])));
        message.append(System.getProperty("line.separator"));

        return message.toString();
    }

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

    public static int getComplementaryColor(@IntRange(from = 0, to = 255) short rgbRColor,
                                            @IntRange(from = 0, to = 255) short rgbGColor,
                                            @IntRange(from = 0, to = 255) short rgbBColor) {
        float[] hsv = new float[3];
        Color.RGBToHSV(rgbRColor, rgbGColor, rgbBColor, hsv);
        hsv[0] = (hsv[0] + 180) % 360;

        return Color.HSVToColor(hsv);
    }

    public static int getContrastColor(@IntRange(from = 0, to = 255) short rgbRColor,
                                       @IntRange(from = 0, to = 255) short rgbGColor,
                                       @IntRange(from = 0, to = 255) short rgbBColor) {
        float[] hsv = new float[3];
        Color.RGBToHSV(rgbRColor, rgbGColor, rgbBColor, hsv);
        if (hsv[2] < 0.5) {
            hsv[2] = 0.7f;
        } else {
            hsv[2] = 0.3f;
        }
        hsv[1] = hsv[1] * 0.2f;

        return Color.HSVToColor(hsv);
    }
}
