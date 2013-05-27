package com.lewa.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

public class EditModeZone extends LinearLayout{

	private static final int ANIMATION_DURATION = 200;

	private AnimationSet mInAnimation;
	private AnimationSet mOutAnimation;

	public EditModeZone(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public EditModeZone(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public void startInAnimation(){
		setVisibility(View.VISIBLE);
		createAnimations();
		startAnimation(mInAnimation);
	}
	
	public void startOutAnimation(){
		createAnimations();
		startAnimation(mOutAnimation);
		setVisibility(View.GONE);
	}
	
	private void createAnimations() {
		if (mInAnimation == null) {
			mInAnimation = new FastAnimationSet();
			final AnimationSet animationSet = mInAnimation;
			animationSet.setInterpolator(new AccelerateInterpolator());
			animationSet.addAnimation(new AlphaAnimation(0.0f, 1.0f));
			animationSet.addAnimation(new TranslateAnimation(
					Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f,
					Animation.RELATIVE_TO_SELF, 1.0f,
					Animation.RELATIVE_TO_SELF, 0.0f));
			animationSet.setDuration(ANIMATION_DURATION);
		}
		if (mOutAnimation == null) {
			mOutAnimation = new FastAnimationSet();
			final AnimationSet animationSet = mOutAnimation;
			animationSet.setInterpolator(new AccelerateInterpolator());
			animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));

			animationSet.addAnimation(new FastTranslateAnimation(
					Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 1.0f));
			animationSet.setDuration(ANIMATION_DURATION);
		}
	}

	private static class FastTranslateAnimation extends TranslateAnimation {
		public FastTranslateAnimation(int fromXType, float fromXValue,
				int toXType, float toXValue, int fromYType, float fromYValue,
				int toYType, float toYValue) {
			super(fromXType, fromXValue, toXType, toXValue, fromYType,
					fromYValue, toYType, toYValue);
		}

		@Override
		public boolean willChangeTransformationMatrix() {
			return true;
		}

		@Override
		public boolean willChangeBounds() {
			return false;
		}
	}

	private static class FastAnimationSet extends AnimationSet {
		FastAnimationSet() {
			super(false);
		}

		@Override
		public boolean willChangeTransformationMatrix() {
			return true;
		}

		@Override
		public boolean willChangeBounds() {
			return false;
		}
	}
}
