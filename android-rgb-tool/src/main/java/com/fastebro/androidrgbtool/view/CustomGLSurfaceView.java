package com.fastebro.androidrgbtool.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class CustomGLSurfaceView extends GLSurfaceView {

    public CustomGLSurfaceView(Context context) {

        super(context);
    }

    public CustomGLSurfaceView(Context context, AttributeSet attribs) {

        super(context, attribs);
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {

        super.onResume();
    }

}
