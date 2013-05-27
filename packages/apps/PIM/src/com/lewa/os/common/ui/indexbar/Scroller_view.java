package com.lewa.os.common.ui.indexbar;

import android.R.integer;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Scroller;

public class Scroller_view extends ViewGroup {
	private Scroller scroller;
	private VelocityTracker mVelocityTracker;
	
	private int currentscreen;
	private int defaultscreen = 0;
	
	public int getCurrentscreen() {
		return currentscreen;
	}

	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;
	
	private static final int SNAP_VELOCITY = 600;
	
	private int touchstate = TOUCH_STATE_REST;
	private int touchslop;
	private float lastmotionX;
	private float lastmotionY;

	public Scroller_view(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
        scroller = new Scroller(context);
		
        currentscreen = defaultscreen;
		touchslop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}


	public Scroller_view(Context context, AttributeSet attrs) {
		
		// TODO Auto-generated constructor stub
		this(context,attrs,0);
	}


	public Scroller_view(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        if (changed) {
            int childLeft = 0;
            int childTop = 0;
            final int childCount = getChildCount();

            for (int i = 0; i < childCount; i++) {
                final View childView = getChildAt(i);
                if (childView.getVisibility() != View.GONE) {
                    final int childWidth = childView.getMeasuredWidth();
                    //modify by zenghuaying fix requirement #11068
                    final int childHeight = childView.getMeasuredHeight();

                    childView.layout(childLeft, childTop, childWidth, childTop
                            + childHeight);
                    childTop += childHeight;
                    //modify end
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        scrollTo(0, currentscreen * height);
    }

    public void snapToDestination() {
        final int screenHeight = getHeight();
        final int destScreen = (getScrollY() + screenHeight / 2) / screenHeight;
        snapToScreen(destScreen);
    }
    
    public void snapToScreen(int whichScreen) {

        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        if (getScrollY() != (whichScreen * getHeight())) {

            final int delta = whichScreen * getHeight() - getScrollY();
            //scroller.startScroll(0, getScrollY(), 0, delta, Math.abs(delta) * 2);
            scroller.startScroll(0, getScrollY(), 0, delta, 150);
            currentscreen = whichScreen;
            //Log.e("************snapToScreen**********currentscreen","" + currentscreen);
            invalidate();
        }

    }
    
    public void setToScreen(int whichScreen) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        currentscreen = whichScreen;
        scrollTo(0, whichScreen * getHeight());
    }
    
    public int getCurScreen() {
        //Log.e("************getCurScreen**********currentscreen", "" + currentscreen);
        return currentscreen;
    }
    
	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		if (scroller.computeScrollOffset()) {
			scrollTo(scroller.getCurrX(), scroller.getCurrY());
			postInvalidate();
		}
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            if (!scroller.isFinished()) {
                scroller.abortAnimation();
            }
            lastmotionX = x;
            lastmotionY = y;
            break;

        case MotionEvent.ACTION_MOVE:
            int deltaX = (int) (lastmotionX - x);
            lastmotionX = x;
            int deltaY = (int) (lastmotionY - y);
            lastmotionY = y;
            scrollBy(0, deltaY);
            break;

        case MotionEvent.ACTION_UP:
            final VelocityTracker velocityTracker = mVelocityTracker;
            velocityTracker.computeCurrentVelocity(1000);
            int velocityY = (int) velocityTracker.getYVelocity();

            if (velocityY > SNAP_VELOCITY && currentscreen > 0) {
                snapToScreen(currentscreen - 1);
            } else if (velocityY < -SNAP_VELOCITY
                    && currentscreen < getChildCount() - 1) {
                snapToScreen(currentscreen + 1);
            } else {
                snapToDestination();
            }

            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            touchstate = TOUCH_STATE_REST;
            break;
        case MotionEvent.ACTION_CANCEL:
            touchstate = TOUCH_STATE_REST;
            break;
        }

        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub

        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE)
                && (touchstate != TOUCH_STATE_REST)) {
            return true;
        }

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
        case MotionEvent.ACTION_MOVE:
            
            final int yDiff = (int) Math.abs(lastmotionY - y);
            if (yDiff > touchslop) {
                touchstate = TOUCH_STATE_SCROLLING;
            }
            break;

        case MotionEvent.ACTION_DOWN:
            lastmotionX = x;
            lastmotionY = y;
            touchstate = scroller.isFinished() ? TOUCH_STATE_REST
                    : TOUCH_STATE_SCROLLING;
            break;

        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            touchstate = TOUCH_STATE_REST;
            break;
        }

        return touchstate != TOUCH_STATE_REST;
    }

}
