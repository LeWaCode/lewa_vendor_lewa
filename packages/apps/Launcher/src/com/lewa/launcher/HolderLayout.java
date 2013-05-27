package com.lewa.launcher;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class HolderLayout extends ViewGroup {
   
	public HolderLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		setWillNotDraw(false);
	}

	public HolderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		setWillNotDraw(false);
	}

	public HolderLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		setWillNotDraw(false);
	}

	@Override
	protected boolean addViewInLayout(View child, int index,
			LayoutParams params, boolean preventRequestLayout) {
		// TODO Auto-generated method stub
		return super.addViewInLayout(child, index, params, preventRequestLayout);
	}

	/*@Override
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            view.setDrawingCacheEnabled(enabled);
            // Update the drawing caches
            view.buildDrawingCache(true);
        }
    }*/

/*    @Override
    protected void setChildrenDrawnWithCacheEnabled(boolean enabled) {
        super.setChildrenDrawnWithCacheEnabled(enabled);
    }*/


	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		// TODO Auto-generated method stub
		//super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}
	
	@Override
	protected void dispatchSetPressed(boolean pressed) {
		// TODO Auto-generated method stub
		//super.dispatchSetPressed(pressed);
	}

	@Override
	public void dispatchSetSelected(boolean selected) {
		// TODO Auto-generated method stub
		super.dispatchSetSelected(selected);
	}
	
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	//Log.d("HolderLayout","INTERCEPT");
		return true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	//Log.d("HolderLayout","TOUCH");
		return true;
    }

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub
		
	}
}
