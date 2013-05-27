package com.lewa.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

public class AllInfoZone extends RelativeLayout{

	private static final int ANIMATION_DURATION = 200;
	
	public AllInfoZone(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public AllInfoZone(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public AllInfoZone(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public void startInAnimation(){
		setVisibility(View.VISIBLE);
		TranslateAnimation anim=new TranslateAnimation(0, 0, getHeight(), 0);
		anim.setDuration(ANIMATION_DURATION);
		startAnimation(anim);
	}
	
	public void startOutAnimation(){	
		TranslateAnimation anim=new TranslateAnimation(0, 0, 0, getHeight());
		anim.setDuration(ANIMATION_DURATION);
		anim.setAnimationListener(new AnimationListener() {
			
			//@Override
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}
			
			//@Override
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}
			
			//@Override
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				setVisibility(View.GONE);
			}
		});
		startAnimation(anim);
	}
	
}
