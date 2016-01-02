package com.fastebro.androidrgbtool.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

/**
 * Created by danielealtomare on 13/06/14.
 * Project: rgb-tool
 */
public class CheckableRelativeLayout extends RelativeLayout implements Checkable {
    private static final int[] STATE_CHECKABLE = {android.R.attr.state_pressed};
    private boolean checked = false;

    public CheckableRelativeLayout(@NonNull Context context) {
        super(context);
    }

    public CheckableRelativeLayout(@NonNull Context context, @NonNull AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableRelativeLayout(@NonNull Context context, @NonNull AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        refreshDrawableState();
    }

    public boolean isChecked() {
        return checked;
    }

    public void toggle() {
        setChecked(!checked);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (checked) mergeDrawableStates(drawableState, STATE_CHECKABLE);

        return drawableState;
    }
}
