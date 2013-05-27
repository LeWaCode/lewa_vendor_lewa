package com.lewa.PIM.mms.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.lewa.PIM.R;
import com.lewa.PIM.util.CommonMethod;

public class ImsFreeMessageUserProtoclDialog extends Activity implements OnClickListener{
	private TextView mCheckUserProtocl;
	private Button mFreeMessageOpening;
	private Button mFreeMessageToLater;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pre_free_message_dialog);
		mCheckUserProtocl = (TextView) findViewById(R.id.check_user_protocl);
		String string = getResources().getString(R.string.message_ims_user_service_check_protocol);
		mCheckUserProtocl.setText(Html.fromHtml("<u>"+string+"</u>"));
		mCheckUserProtocl.setOnClickListener(this);
		mFreeMessageOpening = (Button) findViewById(R.id.free_message_opening);
		mFreeMessageOpening.setOnClickListener(this);
		mFreeMessageToLater = (Button) findViewById(R.id.free_message_to_later);
		mFreeMessageToLater.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.free_message_to_later:
			finish();
			break;
			
		case R.id.free_message_opening:
	        SharedPreferences sp = getSharedPreferences(MessagingPreferenceActivity.IMS_FIRST_OPEN, Context.MODE_WORLD_READABLE);
        	SharedPreferences.Editor editor = sp.edit();
        	editor.putBoolean(MessagingPreferenceActivity.IMS_FIRST_OPEN, false);
        	editor.commit();	
        	
        	sp = getSharedPreferences(MessagingPreferenceActivity.IMS_CLOSE_STATE, Context.MODE_WORLD_WRITEABLE);
        	editor = sp.edit();        	
        	editor.putString(MessagingPreferenceActivity.IMS_CLOSE_STATE, "true");
        	editor.commit();
        	
        	CommonMethod.setLWMsgOnoff(this, true); 
        	
        	Intent intent = new Intent();
        	intent.setAction(MessagingPreferenceActivity.ACTION_YILIAO_FIRST_OPEN);
        	sendBroadcast(intent);
	        finish();
			break;

		case R.id.check_user_protocl:
			Intent infoIntent = new Intent(this, ImsFreeMessageUserProtoclInfo.class);
			startActivity(infoIntent);
			break;
			
		default:
			break;
		}
		
	}

}
