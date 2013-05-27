/*
 * Copyright (c) 2012 LewaTek
 * All rights reserved.
 * 
 * DESCRIPTION:
 */

package com.lewa.PIM.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
//import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.lewa.PIM.R;
import com.lewa.PIM.util.CommonMethod;

public class AddtoBlackListActivity extends ListActivity implements View.OnClickListener {

    private static final String TAG = "AddtoBlackListActivity";

    private static final String REQUEST_EXTRA_NAME = "name";
    private static final String REQUEST_EXTRA_NUMBERS = "numberlist";
    private static final String REQUEST_EXTRA_ISIMPORTSMS = "type";
    
    private EditText mRemark;
    private CheckBox mIsImportView;
    private String [] mNumbers;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_to_blacklist);

        mRemark = (EditText)findViewById(R.id.descript_text);
        mIsImportView = (CheckBox)findViewById(R.id.isaddsmstoblack);
        mIsImportView.setVisibility(View.VISIBLE);

        ((Button)findViewById(R.id.btn_done)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_discard)).setOnClickListener(this);

        Intent intent = getIntent();
        mNumbers = intent.getStringArrayExtra(REQUEST_EXTRA_NUMBERS);
        /*boolean isimportSMS = intent.getBooleanExtra(REQUEST_EXTRA_ISIMPORTSMS, true);

        if (isimportSMS) {
            mIsImportView.setVisibility(View.GONE);
        } else {
            mIsImportView.setVisibility(View.VISIBLE);
        }*/
        
        setListAdapter(new ArrayAdapter<String>(this, R.layout.addblack_list_item, mNumbers));
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        for (int i = 0; i < mNumbers.length; i++) {
            String name  = CommonMethod.getContactName(this, "", mNumbers[i]);
            if (!TextUtils.isEmpty(name)) {
                mRemark.setText(name);
                break;
             }
        }
    }

    /** {@inheritDoc} */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_done:
                String name = mRemark.getText().toString();
                CommonMethod.requestAddToBlacklist(this, name, mNumbers, mIsImportView.isChecked());
                finish();
                break;
                
            case R.id.btn_discard:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onResume() {
        
        super.onResume();

    }
    
}

