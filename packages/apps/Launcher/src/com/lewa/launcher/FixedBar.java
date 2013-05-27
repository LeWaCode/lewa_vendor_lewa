package com.lewa.launcher;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

public class FixedBar extends LinearLayout {
    private static final int ANIM_DURATION = 250;
    
    private MySlidingDrawer mSliderDrawer = null;
    
	
	public FixedBar(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public FixedBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public void open(){
		//setVisibility(View.VISIBLE);
		mSliderDrawer.getBottomBar().setVisibility(View.VISIBLE);
		mSliderDrawer.getDeskIndicator().setVisibility(View.VISIBLE);
		TranslateAnimation anim=new TranslateAnimation(0, 0,getHeight(), 0);
		anim.setDuration(ANIM_DURATION);
		startAnimation(anim);
	}
	
	public void close(){		
		TranslateAnimation anim=new TranslateAnimation(0, 0, 0,getHeight());
		anim.setDuration(ANIM_DURATION);
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
				//setVisibility(View.INVISIBLE);
				mSliderDrawer.getBottomBar().setVisibility(View.INVISIBLE);
				mSliderDrawer.getDeskIndicator().setVisibility(View.INVISIBLE);
			}
		});
		startAnimation(anim);
	}
	
	public void setSliderDrawer(MySlidingDrawer sd){
	    mSliderDrawer = sd;
	}

}
