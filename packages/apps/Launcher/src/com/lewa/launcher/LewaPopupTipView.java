package com.lewa.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

public class LewaPopupTipView {	

    private WindowManager mWindowmanager;
	private WindowManager.LayoutParams mLayoutparams;
	private View mRootView;
	private TextView mTipTextView;
	private boolean mIsShowing;
	private Context mContext;
	
	static final int STYLE_ABOVE = 1;
	static final int STYLE_BELOW = 2;
	
	public LewaPopupTipView(Context context) {

        mRootView= LayoutInflater.from(context).inflate(R.layout.lewapopuptip, null);
        mTipTextView = (TextView)mRootView.findViewById(R.id.texttip);
        
        mWindowmanager = (WindowManager)context.getSystemService(context.WINDOW_SERVICE); 
        mLayoutparams = new WindowManager.LayoutParams();;
        
        mLayoutparams.format = PixelFormat.TRANSPARENT;
        mLayoutparams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        mLayoutparams.x = 0;
        mLayoutparams.y = 0;
        
        mLayoutparams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE
                |LayoutParams.FLAG_FULLSCREEN;  

        mContext = context;
    }
	
	public void show(int style, String tip, int width, int top) {
		switch (style) {
		case STYLE_ABOVE:
		    mTipTextView.setBackgroundResource(R.drawable.popup_inline_above);
			break;
		case STYLE_BELOW:
		    mTipTextView.setBackgroundResource(R.drawable.popup_inline_below);
			break;
		default:
			break;
		}

		mTipTextView.setWidth(width);
		mTipTextView.setText(tip);
		
		mRootView.setPadding(0, top, 0, 0);	
		mWindowmanager.addView(mRootView, mLayoutparams);
		mIsShowing = true;	

        mRootView.setOnTouchListener(new View.OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                dismiss();
                return false;
            }
        });
    }
    
    public void dismiss() {
        if(mIsShowing) {
            mWindowmanager.removeView(mRootView);
        }
        mIsShowing = false;
        SharedPreferences sp = mContext.getSharedPreferences(
                "launcher.preferences.almostnexus", mContext.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = sp.edit();
        
        editor.putBoolean(Launcher.TIPVIEW_KEY, true);
        editor.commit();
    }
	
	public boolean isShowing() {
	    return mIsShowing;
	}
}
