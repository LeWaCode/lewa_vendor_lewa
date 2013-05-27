package com.android.fm.radio;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

public class LeWaChannelNumGallery extends Gallery {

    public LeWaChannelNumGallery(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public LeWaChannelNumGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public LeWaChannelNumGallery(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    /*
     * @Override protected int computeHorizontalScrollExtent() { // TODO
     * Auto-generated method stub Log.e("***gallery",
     * "computeHorizontalScrollExtent"); return 0; }
     * 
     * @Override protected int computeHorizontalScrollOffset() { // TODO
     * Auto-generated method stub Log.e("***gallery",
     * "computeHorizontalScrollOffset"); return 0; }
     * 
     * @Override protected int computeHorizontalScrollRange() { // TODO
     * Auto-generated method stub Log.e("***gallery",
     * "computeHorizontalScrollRange"); return 0; }
     */

    @Override
    public void setSpacing(int spacing) {
        // TODO Auto-generated method stub
        Log.e("***gallery", "setSpacing");
        spacing = 0;
        super.setSpacing(spacing);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        // TODO Auto-generated method stub
        /*
         * Log.e("***gallery", "onFling"); int kEvent; if (isScrollingLeft(e1,
         * e2)) { // Check if scrolling left kEvent =
         * KeyEvent.KEYCODE_DPAD_LEFT; } else { // Otherwise scrolling right
         * kEvent = KeyEvent.KEYCODE_DPAD_RIGHT; } onKeyDown(kEvent, null);
         * return true;
         */
        // return super.onFling(e1, e2, velocityX/3, velocityY);
        return false;

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return super.onSingleTapUp(e);
    }

    /*
     * private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) { // TODO
     * Auto-generated method stub return e2.getX() > e1.getX();
     * 
     * }
     */
}
