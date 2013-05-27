package com.android.phone;

import android.content.Context;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class MyGalleryForSwitchCalls extends RelativeLayout {

	private int mCoveflowCenter;
	private Scroller mScroller;
	private float mLastMotionX;

	private float mScrollOffset;
	private float mScrollDeltaX;

	private VelocityTracker mVelocityTracker = null;
	private int mMaximumVelocity;
	
	private InCallScreen mInCallScreen;
	private boolean mIsHoldingState = false;
	private boolean mIsDialingSatae = false;
	private boolean mIsSwapState = false;
	private boolean mChanged = false;
	
	private Handler mObjHandler = new Handler();
	private boolean mSwitchCallsIdle = true;

	private static final int SNAP_VELOCITY = 350;
	private final static float SCALE = 0.6F;

	public MyGalleryForSwitchCalls(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public MyGalleryForSwitchCalls(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public MyGalleryForSwitchCalls(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	private void init() {
		mScroller = new Scroller(getContext());
		setStaticTransformationsEnabled(true);
		final ViewConfiguration configuration = ViewConfiguration
				.get(getContext());
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = getWidth();
	
		mScrollDeltaX = (float) width / 2.0f - width * (1.0f - SCALE)
				/ 2.0f;
		mScrollOffset = (float) width / 2.0f + width * (1.0f - SCALE)
				/ 2.0f;
	}

	private int getCenterOfCoverflow() {
		return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2
				+ getPaddingLeft();
	}

	private static int getCenterOfView(View view) {
		return view.getLeft() + view.getWidth() / 2;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mCoveflowCenter = getCenterOfCoverflow();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		// TODO Auto-generated method stub
		final int childCenter = getCenterOfView(child);

		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);

		float scale = (1.0F - SCALE) * (float) getScrollX() / mScrollDeltaX;

		if (childCenter == mCoveflowCenter) {
			transformImageBitmap(child, t, 1.0f - scale, true);//
		} else {
			transformImageBitmap(child, t, SCALE + scale, false);
		}

		return true;
	}

	private void transformImageBitmap(View child, Transformation t,
			float scale, boolean changed) {
		final Matrix matrix = t.getMatrix();
		float childWidht = child.getMeasuredWidth();
		float childHeight = child.getMeasuredHeight();

		matrix.setScale(scale, scale);
		if (changed) {
			matrix.postTranslate((1F - scale) * childWidht * 1F - mScrollOffset
					* (1.0F - scale), (1F - scale) * childHeight * 0.5F);
		} else {
			matrix.postTranslate((1F - scale) * childWidht * 1F, (1F - scale)
					* childHeight * 0.5F);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(mIsDialingSatae || !mIsHoldingState || !mSwitchCallsIdle) {
			return false;
		}
		
		final float x = event.getX();

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		int scrollX = getScrollX();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			int deltaX = (int) (mLastMotionX - x);
			mLastMotionX = x;
			int availableToScroll = scrollX + deltaX;
			if (availableToScroll <= 0 || availableToScroll >= mScrollDeltaX) {
				return false;
			}
			scrollBy(deltaX, 0);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
			int velocityX = (int) velocityTracker.getXVelocity();
			
			if(velocityX > SNAP_VELOCITY && mChanged) {
				mScroller.startScroll(scrollX, 0, -scrollX, 0, 100);
				mChanged = false;
				mIsSwapState = true;
				mInCallScreen.internalSwapCalls();
				mSwitchCallsIdle = false;
				mObjHandler.postDelayed(mMoveTask, 1000);
			} else if (velocityX < -SNAP_VELOCITY && !mChanged) {
				mScroller.startScroll(scrollX, 0,
						(int) mScrollDeltaX - scrollX, 0, 100);
				mChanged = true;
				mIsSwapState = true;
				mInCallScreen.internalSwapCalls();
				mSwitchCallsIdle = false;
				mObjHandler.postDelayed(mMoveTask, 1000);
			} else {
				if(mChanged) {
					mScroller.startScroll(scrollX, 0,
							(int) mScrollDeltaX - scrollX, 0, 100);
				} else {
					mScroller.startScroll(scrollX, 0, -scrollX, 0, 100);
				}
			}
			invalidate();
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			break;
		}

		return true;
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

	public void setInCallScreen(InCallScreen inCallScreen) {
		mInCallScreen = inCallScreen;
	}
	
	public void resetState() {
		mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, 100);
		mIsHoldingState = false;
		mIsSwapState = false;
		mIsDialingSatae = false;
		mChanged = false; 
		invalidate();
	}
	
	public void setHoldingState(boolean holdingState) {
		mIsHoldingState = holdingState;
	}
	
	public void setDialingState(boolean dialingState) {
		mIsDialingSatae = dialingState;
	}
	
	public boolean getSwitchState() {
		return mIsSwapState;
	}
	
	public void internalSwapCalls() {
		if(mSwitchCallsIdle && !mIsDialingSatae) {
			mIsSwapState = true;
			mInCallScreen.internalSwapCalls();
			int scrollX = getScrollX();
			if(scrollX <= 0) {
				mScroller.startScroll(0, 0, (int) mScrollDeltaX, 0, 100);
				mChanged = true;
			} else {
				mScroller.startScroll(scrollX, 0, -(int) mScrollDeltaX, 0, 100);
				mChanged = false;
			}
			mSwitchCallsIdle = false;
			mObjHandler.postDelayed(mMoveTask, 1000);
		}
	}
	private Runnable mMoveTask = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mSwitchCallsIdle = true;
		}
	};
	
}
