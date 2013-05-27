package com.lewa.PIM.mms.ui;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.provider.ContactsContract.RosterData;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lewa.PIM.R;
import com.lewa.PIM.util.CommonMethod;

public class NumberListItem extends RelativeLayout{
    private TextView mToText;
    private TextView mNumberText;
    private ImageView mImage;
    private ImageView mImsOnlineState;
    
    private int mylImageWidth;
    private int mylImageMarg;
    
    public NumberListItem(Context context) {
        super(context);
    }

    public NumberListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberListItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
    }
    
    private void getLayoutItem(){
        mToText = (TextView)findViewById(R.id.text_to);
        mNumberText = (TextView)findViewById(R.id.text_number);
        mImage = (ImageView)findViewById(R.id.icon_indicator);
        mImsOnlineState = (ImageView)findViewById(R.id.contact_yl_number_state);
        
		Drawable mylImage = mContext.getResources().getDrawable(R.drawable.icon_contact_header_online);
		mylImageWidth = mylImage.getIntrinsicWidth();
		
		Resources resources = mContext.getResources();
		mylImageMarg = resources.getDimensionPixelOffset(R.dimen.list_item_gap_between_yl_image_right);
    }
    
    public void binder(int position, String number, String to, boolean expansion){
        getLayoutItem();
        
        mToText.setText(to);
        mNumberText.setText(number);
        
        if (expansion) {
            mImage.setImageResource(R.drawable.ic_number_down);
        }
        else {
            mImage.setImageResource(R.drawable.ic_number_up);
        }
        
        if (position > 0) {
            mImage.setVisibility(View.GONE);
        }
        else {
            mImage.setVisibility(View.VISIBLE);
        }
        
        if (CommonMethod.getLWMsgOnoff(mContext) == false) {
        	mImsOnlineState.setVisibility(View.GONE);
			return;
		}
        
        Cursor cursor = null;
        try {
        	cursor = mContext.getContentResolver().query(RosterData.CONTENT_URI, 
        										new String[]{RosterData.STATUS}, 
        										RosterData.ROSTER_USER_ID + " = '" + number + "' ", 
        										null, 
        										null);
        	int state = -1;
            if (cursor.moveToNext()){
            	state = cursor.getInt(0);
            }            
            cursor.close();
            
    		android.widget.LinearLayout.LayoutParams subjectparams = 
    			(android.widget.LinearLayout.LayoutParams)mNumberText.getLayoutParams();
            
            if (state == -1) {
        		subjectparams.leftMargin = mylImageWidth + mylImageMarg;        		        		
            	mImsOnlineState.setVisibility(View.GONE);
			}else if (state == 1){
				mImsOnlineState.setVisibility(View.VISIBLE);
				mImsOnlineState.setImageResource(R.drawable.icon_contact_header_online);
        		subjectparams.leftMargin = 0;        		        		
			}else {
				mImsOnlineState.setVisibility(View.VISIBLE);
				mImsOnlineState.setImageResource(R.drawable.icon_contact_header_offline);				
        		subjectparams.leftMargin = 0;        		        		
			}
    		mNumberText.setLayoutParams(subjectparams);
            
        }catch (Exception e) {
        	if (cursor != null) {
				cursor.close();
			}
		}  
    }
}
