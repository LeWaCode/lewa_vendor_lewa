package com.lewa.fc;

import com.lewa.fc.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class AndroidFcResultActivity extends Activity {
    /** Called when the activity is first created. */
	
	private Button finishButton;
	private Button continueButton;
	
	private Intent intentFrom = new Intent();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);    
		setContentView(R.layout.result_layout);
		
		initButton();
	}
	
	private void initButton()
	{
		finishButton = (Button)findViewById(R.id.finish_button);
		finishButton.setOnClickListener(finishButtonnListener);
		
		continueButton = (Button)findViewById(R.id.continue_button);
		continueButton.setOnClickListener(continueButtonListener);
	}

	private OnClickListener finishButtonnListener = new OnClickListener() {   
    	public void onClick(View v) {  
    		setResult(Activity.RESULT_OK, intentFrom);
            AndroidFcResultActivity.this.finish();
    	}
     };	
     
     private OnClickListener continueButtonListener = new OnClickListener() {   
     	public void onClick(View v) {  
     		
     		/*
     		Intent intent = new Intent(Intent.ACTION_MAIN);
     		intent.addCategory(Intent.CATEGORY_LAUNCHER);            
     		ComponentName cn = new ComponentName("lewa.wf.advice", "AndroidAdviceDemoActivity");            
     		intent.setComponent(cn);
     		startActivity(intent);
     		*/
     		PackageManager packageManager = AndroidFcResultActivity.this.getPackageManager();  
     		Intent intent = new Intent();  
     		try 
     		{  
     			intent = packageManager.getLaunchIntentForPackage("com.lewa.feedback");  
     		} 
     		catch (Exception e) 
     		{  
     			Log.i("tag", e.toString());  
     		}  
     		startActivity(intent);  
     		
     		setResult(Activity.RESULT_OK, intentFrom);
            AndroidFcResultActivity.this.finish();

     		/*
     		Intent intent = new Intent(); 
            intent.setClass(AndroidFcResultActivity.this, AndroidFcDemoActivity.class); 
            startActivity(intent); */
     	}
      };	
}
