package com.lewa.PIM.mms.MsgPopup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ViewFlipper;

public class MsgViewFlipper extends ViewFlipper{
    
    private boolean mIsBeingDragged = true;
    private float mX = 0;
    private float mY = 0;
    
    public MsgViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
        
    public MsgViewFlipper(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean Ret = false;
        int action = ev.getAction();
        
        if (action == MotionEvent.ACTION_DOWN) {
            mX = ev.getX();
            mY = ev.getY();
            Ret = false;
        }
        
        if (action == MotionEvent.ACTION_MOVE) {
            
            int ex = (int)(mX - ev.getX());
            int ey = (int)(mY - ev.getY());
            
            if (ex > 60 || ex < -60) {
                mIsBeingDragged = true;                
                Ret = true;
            }else {
                mIsBeingDragged = false;
                Ret = false;
            }
        }
        
        if (action == MotionEvent.ACTION_UP) {
            if (mIsBeingDragged) {
                Ret = true;
            }else {
                Ret = false;
            }
            mIsBeingDragged = false;
        }
        return Ret;
    }

}
