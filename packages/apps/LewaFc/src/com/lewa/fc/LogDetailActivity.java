package com.lewa.fc;

import com.lewa.fc.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class LogDetailActivity extends Activity {

	
	
	private Button rebackButton;
	private TextView logTextView;
	
	private String logText;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);    
		setContentView(R.layout.log_layout);
		
	    logText = MyLog.getErrorText();
	    
 		initButton();
		initTextView();
	}
	
	private void initButton()
	{
		rebackButton = (Button) findViewById(R.id.reback);
		rebackButton.setOnClickListener(submitButtonListener);
	}
	
	private void initTextView()
	{
		logTextView = (TextView) findViewById(R.id.log_detail);
		logTextView.setText(logText);
	}
	
	private OnClickListener submitButtonListener = new OnClickListener() {   
    	public void onClick(View v) {  
            LogDetailActivity.this.finish();
    	}
     };

}
