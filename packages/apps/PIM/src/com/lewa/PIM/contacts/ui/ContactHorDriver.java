package com.lewa.PIM.contacts.ui;

import com.lewa.PIM.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ContactHorDriver extends LinearLayout{
	
	private Paint mPaint = new Paint();
	private int mWidth;
	private float mDividerHeight;
	private int mBottom;

	public ContactHorDriver(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public ContactHorDriver(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		 mDividerHeight = context.getResources().getDimensionPixelOffset(R.dimen.list_item_divider_height);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		mBottom = b;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mWidth = resolveSize(0, widthMeasureSpec);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.dispatchDraw(canvas);
		mPaint.setColor(0xffd8d8d8);
		canvas.drawLine(0, mBottom-2*mDividerHeight, mWidth, mBottom-mDividerHeight, mPaint);
		
		mPaint.setColor(0xffffffff);
		canvas.drawLine(0, mBottom-mDividerHeight, mWidth, mBottom, mPaint);
	}

	
}
