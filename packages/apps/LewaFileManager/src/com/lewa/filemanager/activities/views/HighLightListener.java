/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.activities.views;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.lewa.app.filemanager.R;

/**
 *
 * @author Administrator
 */
public class HighLightListener implements OnTouchListener {

    Drawable previousBackground;
    int paddingLeft;
    int paddingRight;
    int paddingTop;
    int paddingBottom;

    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                previousBackground = view.getBackground();
                paddingBottom = view.getPaddingBottom();
                paddingTop = view.getPaddingTop();
                paddingLeft = view.getPaddingLeft();
                paddingRight = view.getPaddingRight();
                view.setBackgroundResource(R.drawable.bg_common_pressed);
                view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MASK:

                view.setBackgroundDrawable(previousBackground);
        }
        return false;
    }
}
