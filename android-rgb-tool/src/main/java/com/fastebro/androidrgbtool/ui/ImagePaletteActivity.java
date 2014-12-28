package com.fastebro.androidrgbtool.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.adapters.ImagePaletteAdapter;
import com.fastebro.androidrgbtool.model.PaletteSwatch;

import java.util.ArrayList;

/**
 * Created by danielealtomare on 27/12/14.
 */
public class ImagePaletteActivity extends BaseActivity {
    public static final String EXTRA_SWATCHES = "com.fastebro.androidrgbtool.EXTRA_SWATCHES";
    private GridView paletteGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_palette);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if(getIntent().getParcelableArrayListExtra(EXTRA_SWATCHES) != null) {
            ArrayList<PaletteSwatch> swatches = getIntent().getParcelableArrayListExtra(EXTRA_SWATCHES);

            paletteGrid = (GridView) findViewById(R.id.palette_grid);
            paletteGrid.setAdapter(new ImagePaletteAdapter(this, swatches));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            } else {
                finish();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
