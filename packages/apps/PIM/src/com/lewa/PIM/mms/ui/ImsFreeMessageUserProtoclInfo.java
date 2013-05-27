package com.lewa.PIM.mms.ui;

import com.lewa.PIM.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;

public class ImsFreeMessageUserProtoclInfo extends Activity{
	
    private TextView mTextInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_free_message_dialog_view);      
        mTextInfo = (TextView) findViewById(R.id.text_info);
        mTextInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
        //mTextInfo.scrollTo(0, 400);
    }
    
    Runnable RefreshUIRunable = new Runnable() {
     public void run() {
            mTextInfo.scrollTo(0, 0);
        }
    };
    
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        
        Handler mHandler = new Handler();
        mHandler.postDelayed(RefreshUIRunable,200);
    }
}
