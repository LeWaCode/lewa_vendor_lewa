package com.lewa.intercept;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lewa.intercept.intents.Constants;
import com.lewa.intercept.intents.InterceptIntents;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.InterceptConstants;

public class WhiteNameEditActivity extends Activity implements OnClickListener{
    
    private static final String TAG = "WhiteNameEditActivity";
    
    private int blockMode = 0;
    private String userId = null;
    private String userName = null;
    private String blockUserNumber = null;
    
    private TextView mWhiteNumber;
    private EditText mWhiteName;
    private Button mConfirm, mCancel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.white_name_editor);
        
        Bundle b = this.getIntent().getExtras();
        userId = b.getString("userId"); // get userInfo from BlockNameActivity
        userName = b.getString("userName");
        blockUserNumber = b.getString("userNumber");
        
        initUI();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWhiteNumber = null;
        mWhiteName = null;
        mConfirm = null;
        mCancel = null;
    }
    
    private void initUI() {
        mWhiteNumber = (TextView) findViewById(R.id.number_white);
        mWhiteName = (EditText) findViewById(R.id.name_white);
        mConfirm = (Button) findViewById(R.id.confirm);
        mCancel = (Button) findViewById(R.id.cancel);
        mConfirm.setOnClickListener(this);
        mCancel.setOnClickListener(this);

//        mWhiteNumber.setEnabled(false);
        mWhiteNumber.setText(blockUserNumber);
        mWhiteName.setText(userName);
    }
    
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
        case R.id.confirm:
            if (updateDataBaseInterceptData(userId)) {
                this.finish();
            }
            break;
        case R.id.cancel:
            this.finish();
            break;
        }
    }
    
    // update the data
    public boolean updateDataBaseInterceptData(String userId) { // Update DataBase
        if (Constants.DBUG) {
            Log.i(Constants.TAG, "updateDataBaseInterceptData()");
        }
        
        userName = mWhiteName.getText().toString();
        blockUserNumber = mWhiteNumber.getText().toString();

        /*
        if(blockUserNumber.length() == 0){
            Toast.makeText(this, getResources().getString(R.string.noName), Toast.LENGTH_SHORT).show();
            return false;
        }
        */

        ContentValues values = new ContentValues();
        values.put(InterceptConstants.COLUMN_NAME, userName);

        String selection = InterceptConstants.COLUMN_BLOCK_NAME_ID + " = ?";
        int result = getContentResolver().update(
                InterceptConstants.CONTENT_URI, values, selection, new String[] { userId });
        
        if (result > 0){
           return true;
        }else {
           return false;
        }
    }
}
