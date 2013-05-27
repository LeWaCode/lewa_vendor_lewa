package com.android.settings.stats;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.settings.R;

public class StatsActivity extends Activity {
    private static final String PREF_NAME = "LewaStats";
    private Button mOkButton, mCancelBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.stats);

        mOkButton = (Button) findViewById(R.id.btn_ok);
        mCancelBtn = (Button) findViewById(R.id.btn_cancel);
        
        mOkButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                
            }
        });
        
        mCancelBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                finish();
            }
        });

        SharedPreferences settings = getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("firstboot", false);
        editor.commit();

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(1);
    }
}
