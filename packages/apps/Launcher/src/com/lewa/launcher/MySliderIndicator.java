package com.lewa.launcher;

import android.content.Context;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class MySliderIndicator extends ViewGroup {
	private View mIndicator;
	private int mItems = 5;

	public MySliderIndicator(Context context) {
		super(context);
		initIndicator(context);
	}

	public MySliderIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		initIndicator(context);
	}

	public MySliderIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initIndicator(context);
	}

	private void initIndicator(Context context) {
	    //mBgAlpha = Color.alpha(mBgColor);
		mIndicator = new SliderIndicator(context);
		addView(mIndicator);
	}

	public void setItems(int items) {
		mItems = items;
		((SliderIndicator) mIndicator).setTotalItems(mItems);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		LinearLayout.LayoutParams params;
		params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		mIndicator.measure(getWidth(), getHeight());
		//mIndicator.setLayoutParams(params);
		mIndicator.layout(0, 0, getWidth(), getHeight());
	}

	public void indicate(float percent) {
		setVisibility(View.VISIBLE);
		int offset = ((int) (getWidth() * percent)) - mIndicator.getLeft();
		((SliderIndicator) mIndicator).setOffset(offset);
		mIndicator.invalidate();
	}
	
	protected void setLeft(int value){
		int width = this.mRight - this.mLeft;
		this.mLeft = value;
		this.mRight = this.mLeft + width;
	}

	private class SliderIndicator extends View {
		
		private Rect mRect;
		private int mTotalItems = 5;
		private Drawable mIndicatorIcon = null;

		public SliderIndicator(Context context) {
			super(context);
			final Resources resources = context.getResources();
			mIndicatorIcon = resources.getDrawable(R.drawable.mainmenu_indicator_normal);
			int height = resources.getDimensionPixelSize(R.dimen.mainmenu_indicator_height);
			mRect = new Rect(0, 0, 0, height);
		}

		@Override
		public void draw(Canvas canvas) {
			//canvas.drawRoundRect(mRect, 2, 2, mPaint);
			if(mTotalItems>1){
				mIndicatorIcon.setBounds(mRect); 			
				mIndicatorIcon.draw(canvas);
			}
		}

		public void setTotalItems(int items) {
			mTotalItems = items;
		}

		public void setOffset(int offset) {
		    if(mTotalItems >0){
    			int width = getWidth() / mTotalItems;
    			mRect.left = offset + this.mLeft;
    			mRect.right = mRect.left + width;
		    }
		}
	}
}
