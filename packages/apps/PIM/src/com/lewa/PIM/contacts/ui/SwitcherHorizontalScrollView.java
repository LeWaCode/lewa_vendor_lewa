package com.lewa.PIM.contacts.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class SwitcherHorizontalScrollView extends HorizontalScrollView{

    private boolean m_move = true;
    
    public SwitcherHorizontalScrollView(Context context) {
        super(context);
    }

    public SwitcherHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public SwitcherHorizontalScrollView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public void SetMove(boolean b) {
        m_move = b;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK){
        
            case MotionEvent.ACTION_MOVE:
                
                if (m_move == false) {
                    return false;
                }
                break;
                
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }
    
}
