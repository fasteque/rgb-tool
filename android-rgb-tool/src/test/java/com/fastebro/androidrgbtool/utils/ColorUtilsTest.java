package com.fastebro.androidrgbtool.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by danielealtomare on 10/06/15.
 * Project: rgb-tool
 */
public class ColorUtilsTest {

    @Test
    public void testGetRGB() throws Exception {
        assertEquals(ColorUtils.getRGB(0), "0");
        assertEquals(ColorUtils.getRGB(100), "100");
        assertEquals(ColorUtils.getRGB(255), "255");
        assertEquals(ColorUtils.getRGB(0.10f), "0");
        assertEquals(ColorUtils.getRGB(100.10f), "100");
        assertEquals(ColorUtils.getRGB(255.10f), "255");
    }

    @Test
    public void testRGBToHex() throws Exception {
        assertEquals(ColorUtils.RGBToHex(0), "00");
        assertEquals(ColorUtils.RGBToHex(16), "10");
        assertEquals(ColorUtils.RGBToHex(255), "FF");
    }

    @Test
    public void testHexToRGB() throws Exception {
        int[] rgb = ColorUtils.hexToRGB("");
        assertNotNull(rgb);
        assertEquals(rgb.length, 3);
        assertEquals(rgb[0], 0);
        assertEquals(rgb[1], 0);
        assertEquals(rgb[2], 0);
    }
}