package com.lewa.launcher.version;

import android.app.Activity;
import android.os.Bundle;

public class UpdateActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		new VersionUpdate(this).askStartNow();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
}
