package com.lewa.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;

public class DesktopIndicator extends ViewGroup implements AnimationListener,
		View.OnClickListener {
	private View mIndicator;
	private int mItems = 3;
	private int mCurrent = 0;
	// Begin [pan] add
	private Launcher mLauncher = null;

	// End

	public DesktopIndicator(Context context) {
		super(context);
		initIndicator(context);
	}

	public DesktopIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		initIndicator(context);
	}

	public DesktopIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initIndicator(context);
	}

	private void initIndicator(Context context) {
		mIndicator = new PreviewPager(context);
		((PreviewPager) mIndicator).setTotalItems(mItems);
		((PreviewPager) mIndicator).setCurrentItem(mCurrent);
		addView(mIndicator);
	}

	public void setItems(int items) {
		mItems = items;
		((PreviewPager) mIndicator).setTotalItems(mItems);
		((PreviewPager) mIndicator).setCurrentItem(mCurrent);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mIndicator.measure(getWidth(), getHeight());// 20
		mIndicator.setLayoutParams(params);
		mIndicator.layout(0, 0, getWidth(), getHeight());
	}

	public void indicate(float percent) {
		setVisibility(View.VISIBLE);
		int position = Math.round(mItems * percent);
		if (position == mCurrent)
			return;

		((PreviewPager) mIndicator).setCurrentItem(position);

		mCurrent = position;
	}

	public void fullIndicate(int position) {
		((PreviewPager) mIndicator).setCurrentItem(position);
		mCurrent = position;
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		setVisibility(View.INVISIBLE);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

	public void hide() {
		// TODO Auto-generated method stub
		setVisibility(View.INVISIBLE);
	}

	public void show() {
		// TODO Auto-generated method stub
		setVisibility(View.VISIBLE);
	}

	public void setLauncher(Launcher launcher) {
		mLauncher = launcher;
	}

	// End

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (mLauncher.getOpenFolder() != null) {
			if (!((AppFolder) mLauncher.getOpenFolder()).getAppInfoListState()) {
				mLauncher.closeFolder(mLauncher.getOpenFolder());
			}
			return;
		}
	}
}
