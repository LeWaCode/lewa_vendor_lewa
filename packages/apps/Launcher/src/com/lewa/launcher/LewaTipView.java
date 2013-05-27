/*
 * Copyright (c) 2011 Lewa
 * All rights reserved.
 * 
 * DESCRIPTION:The description of the content of the file.
 *
 * WHEN        | WHO             | what, where, why
 * --------------------------------------------------------------------------------
 * 2011-08-01  | Pan qianbo      | Create file
 */

package com.lewa.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

public class LewaTipView extends RelativeLayout implements AnimationListener {

	private Launcher mLauncher;
	private Animation animation_1;
	private Context context;
	private boolean isstarted = false;

	public LewaTipView(Context context) {
		super(context);
		this.context = context;
	}

	public LewaTipView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public LewaTipView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	@Override
	protected void onFinishInflate() {
		animation_1 = AnimationUtils.loadAnimation(context,
				R.anim.tip_translate_animation);
		animation_1.setFillAfter(true);
		animation_1.setAnimationListener(this);

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			final int yDiff = (int) Math.abs(ev.getY());
			final int xDiff = (int) Math.abs(ev.getX());
			if (yDiff > 26.5f | xDiff > 26.5f) {
				startAnimation();
			}
		}
		return true;
	}

	public void setLauncher(Launcher launcher, int resid) {
		mLauncher = launcher;
		ImageView content = (ImageView) findViewById(R.id.content);
		content.setScaleType(ScaleType.FIT_XY);
		content.setImageDrawable(getResources().getDrawable(resid));
	}

	public void startAnimation() {
		if (!isstarted) {
			startAnimation(animation_1);
			isstarted = true;
			SharedPreferences sp = mLauncher.getSharedPreferences(
					"launcher.preferences.almostnexus", mLauncher.MODE_WORLD_READABLE);
			SharedPreferences.Editor editor = sp.edit();
			
			editor.putBoolean(Launcher.TIPVIEW_KEY, true);
            editor.commit();
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		mLauncher.removeTipView();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public void onAnimationStart(Animation animation) {

	}

}
