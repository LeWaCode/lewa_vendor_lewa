package com.lewa.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CounterImageView extends ImageView {
    //ADW custom notifier counters
	
	public Drawable mBackGround = null;
	//public Drawable mIcon_bg = null;
	public int mBgPaddingTop = 0;
	
	public Drawable mSelectedBackGround = null;
	private boolean mBackgroundSizeChanged;
	
	public Drawable mIconTopDrawable;
	
    public CounterImageView(Context context) {
        super(context);
        init();
    }

    public CounterImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CounterImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    private void init() {
        mSelectedBackGround = IconHighlights.getDrawable(getContext(),
                IconHighlights.TYPE_DESKTOP);
        setBackgroundDrawable(null);
        mSelectedBackGround.setCallback(this);
    }
    
    @Override
    protected boolean setFrame(int left, int top, int right, int bottom) {
        if (mLeft != left || mRight != right || mTop != top || mBottom != bottom) {
            mBackgroundSizeChanged = true;
        }
        return super.setFrame(left, top, right, bottom);
    }
    
    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mSelectedBackGround || super.verifyDrawable(who);
    }

    @Override
    protected void drawableStateChanged() {
        Drawable d = mSelectedBackGround;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
        super.drawableStateChanged();
    }   
    

    @Override
    public void draw(Canvas canvas) {
        /*
         * add bg when click 
         */
        final Drawable background = mSelectedBackGround;
        if (background != null) {
            final int scrollX = mScrollX;
            final int scrollY = mScrollY;

            if (mBackgroundSizeChanged) {
                background.setBounds(0, 0, getWidth(), getHeight());
                mBackgroundSizeChanged = false;
            }
                
            if ((scrollX | scrollY) != 0) {
                background.draw(canvas);
            } else {                
                background.draw(canvas);
                canvas.translate(-scrollX, -scrollY);
            }
        }
        translateDrawable(mBackGround, canvas);
        //End
        super.draw(canvas);
        
        translateDrawable(mIconTopDrawable, canvas);
    }
    
    private void translateDrawable(Drawable drawable, Canvas canvas) {
        if (drawable != null) {
        	canvas.save();
        	int l = (getWidth() - drawable.getIntrinsicWidth()) / 2;
            if (l > 0) {
            	drawable.setBounds(l, mBgPaddingTop,
                        l + drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight() + mBgPaddingTop);
                if (Launcher.mIsNeedChanged) {
                    canvas.translate(l, mBgPaddingTop);
                }
                drawable.draw(canvas);
        	}
        	canvas.restore();
        }
    }
}
