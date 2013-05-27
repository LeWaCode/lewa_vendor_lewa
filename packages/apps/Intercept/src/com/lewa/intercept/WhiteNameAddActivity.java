package com.lewa.intercept;


import com.lewa.intercept.BlackNameAddActivity.EditTextFocusChangeListener;
import com.lewa.intercept.intents.Constants;
import com.lewa.intercept.intents.InterceptIntents;
import com.lewa.intercept.util.InterceptUtil;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.InterceptConstants;
import android.telephony.PhoneNumberUtils; 

public class WhiteNameAddActivity extends Activity implements OnClickListener{


    private static final int DIALOG_WHITE_ADD_TIP = 0;
    
    private EditText mWhiteNumber;
    private EditText mWhiteName;
    private Button mConfirm, mCancel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.white_name_add_editor);
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
        mWhiteNumber = (EditText) findViewById(R.id.number_white);
        mWhiteNumber.setOnFocusChangeListener(new EditTextFocusChangeListener());
        mWhiteName = (EditText) findViewById(R.id.name_white);
        mConfirm = (Button) findViewById(R.id.confirm);
        mCancel = (Button) findViewById(R.id.cancel);
        mConfirm.setOnClickListener(this);
        mCancel.setOnClickListener(this);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_WHITE_ADD_TIP:
                InterceptUtil.number = mWhiteNumber.getText().toString();
                InterceptUtil.type = Constants.BLOCK_TYPE_WHITE;
                InterceptUtil.isCloseActivity = true;
                return InterceptUtil.createAdd2WhiteTipDialog(this);
            default:
                break;
        }
        return null;
    }
    

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.confirm:
                String number = mWhiteNumber.getText().toString();
                String name = mWhiteName.getText().toString();
                if (number.length() == 0) {
                    Toast.makeText(this, getResources().getString(R.string.noWhiteName), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!PhoneNumberUtils.isGlobalPhoneNumber(number)) {
                    Toast.makeText(this, getResources().getString(R.string.globalPhoneNumber_tip), Toast.LENGTH_SHORT).show();
                    return; 
                }

                if (InterceptUtil.addWhiteNameToDB(WhiteNameAddActivity.this,name,number)) {
                    this.finish();
                }else {
                    switch (InterceptUtil.isInBlackOrWhiteList(WhiteNameAddActivity.this,number)) {
                        case Constants.BLOCK_TYPE_BLACK:
                            showDialog(DIALOG_WHITE_ADD_TIP);
                            break;
                        case Constants.BLOCK_TYPE_WHITE:
                            Toast.makeText(this, getResources().getString(R.string.haswhitewarning), Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                }
                break;
            case R.id.cancel:
                this.finish();
                break;
        }
    }
    public class EditTextFocusChangeListener implements OnFocusChangeListener{

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            // TODO Auto-generated method stub
            if (!hasFocus) {
                String text = mWhiteNumber.getText().toString();
                String name = GetContactName.getContactName(WhiteNameAddActivity.this, "", text);
                mWhiteName.setText(name);
            }
        }
        
    }

}
