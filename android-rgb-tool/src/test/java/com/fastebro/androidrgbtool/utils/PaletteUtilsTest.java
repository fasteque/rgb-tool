package com.fastebro.androidrgbtool.utils;

import android.content.Context;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.palette.PaletteSwatch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by danielealtomare on 05/03/16.
 * Project: rgb-tool
 */
@RunWith(MockitoJUnitRunner.class)
public class PaletteUtilsTest {

    private static final String VIBRANT = "Vibrant";
    private static final String LIGHT_VIBRANT = "Light vibrant";
    private static final String DARK_VIBRANT = "Dark vibrant";
    private static final String MUTED = "Muted";
    private static final String LIGHT_MUTED = "Light muted";
    private static final String DARK_MUTED = "Dark muted";

    @Mock
    Context mockContext;

    @Before
    public void initMocks() {
        when(mockContext.getString(R.string.swatch_vibrant)).thenReturn(VIBRANT);
        when(mockContext.getString(R.string.swatch_light_vibrant)).thenReturn(LIGHT_VIBRANT);
        when(mockContext.getString(R.string.swatch_dark_vibrant)).thenReturn(DARK_VIBRANT);
        when(mockContext.getString(R.string.swatch_muted)).thenReturn(MUTED);
        when(mockContext.getString(R.string.swatch_light_muted)).thenReturn(LIGHT_MUTED);
        when(mockContext.getString(R.string.swatch_dark_muted)).thenReturn(DARK_MUTED);
    }

    @Test
    public void paletteUtils_getVibrantSwatchDescription() {
        String description = PaletteUtils.getSwatchDescription(mockContext, PaletteSwatch.SwatchType.VIBRANT);
        assertTrue(description.equals(mockContext.getString(R.string.swatch_vibrant)));
    }
}