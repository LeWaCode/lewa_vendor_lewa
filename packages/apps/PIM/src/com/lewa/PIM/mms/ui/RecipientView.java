package com.lewa.PIM.mms.ui;

import com.lewa.PIM.mms.data.Contact;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class RecipientView extends TextView{
    private boolean mSelect = false;
    private Contact mContact = null;
    private int mWidths = 0;
    private int mHeight = 0;

    public RecipientView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public RecipientView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public RecipientView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    
    public void setSelect(boolean bSelect) {
        mSelect = bSelect;
    }

    public boolean getSelect(){
        return mSelect;
    }
    
    public void setContact(Contact contact) {
        mContact = contact;
    }

    public Contact getContact(){
        return mContact;
    }
    
    public int getRecipientWidths(){
        return mWidths;
    }
    
    public int getRecipientHeight(){
        return mHeight;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mWidths = right - left;
        mHeight = bottom - top;
        super.onLayout(changed, left, top, right, bottom);
    }
    
    
}
