package com.lewa.search.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import com.lewa.search.LewaSearchActivity;
import com.lewa.search.R;

/**
 * This component expend some functions of Edittext.
 * It refers to PIM.
 * 
 * @author		wangfan
 * @version	2012.07.04
 */

public class SearchEditText  extends EditText {

    private boolean mMagnifyingGlassShown = true;
    private Drawable mMagnifyingGlass;
    private float mHeight;
    private float mWidth;
    private float mDriverHeight;
    private Paint mPanit = new Paint();
    
    private static final int COLOR_DRIVER_UP = 0xFF0872C1;
    private static final int COLOR_DRIVER_DOWN = 0x7FDCFDFF;
    
    public SearchEditText(Context context, AttributeSet attrs) 
    {
        super(context, attrs);
        mMagnifyingGlass = getCompoundDrawables()[2];
        mDriverHeight = context.getResources().getDimensionPixelOffset(R.dimen.list_item_divider_height);
    }

    /**
     * Conditionally shows a magnifying glass icon on the right side of the text field
     * when the text it empty.
     */
    @Override
    public boolean onPreDraw() 
    {
        boolean emptyText = TextUtils.isEmpty(getText());
        if(mMagnifyingGlassShown != emptyText) 
        {
            mMagnifyingGlassShown = emptyText;
            if(mMagnifyingGlassShown) 
            {
                setCompoundDrawables(null, null, mMagnifyingGlass, null);
            } 
            else 
            {
                setCompoundDrawables(null, null, null, null);
            }
            return false;
        }
        return super.onPreDraw();
    }

    /**
     * Forwards the onKeyPreIme call to the view's activity.
     */
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        Context context = getContext();
        if(context instanceof LewaSearchActivity) 
        {
            if(((LewaSearchActivity)context).onKeyPreIme(keyCode, event)) 
            {
                return true;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }
    
	@Override
	protected void dispatchDraw(Canvas canvas) 
	{
		// TODO Auto-generated method stub
		mPanit.setColor(COLOR_DRIVER_UP);
		canvas.drawRect(0, mHeight - 2*mDriverHeight, mWidth, mHeight - mDriverHeight, mPanit);
		mPanit.setColor(COLOR_DRIVER_DOWN);
		canvas.drawRect(0, mHeight - mDriverHeight, mWidth, mHeight, mPanit);
		super.dispatchDraw(canvas);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) 
	{
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		mHeight = getHeight();
		mWidth = getWidth();
	}
}

