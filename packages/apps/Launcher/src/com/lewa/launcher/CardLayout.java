package com.lewa.launcher;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class CardLayout extends FrameLayout{
    
    public CardLayout(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    
    public CardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
    
    public CardLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	} 
    
    
}
