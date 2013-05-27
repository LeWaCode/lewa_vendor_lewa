package com.lewa.intercept;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.lewa.intercept.intents.InterceptIntents;
import com.lewa.intercept.intents.Constants;
import com.lewa.intercept.util.InterceptUtil;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.InterceptConstants;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

public class BlackNameAddActivity extends Activity implements OnClickListener {
    
    private static final int DIALOG_BLACK_ADD_TIP = 0;
    
    private EditText numberEditText;
    private EditText nameEditText;
    private Spinner modeSpinner;
    private Button comfirmBtn;
    private Button concelBtn;

    private int[] blockModes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.black_name_add_editor);
        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        numberEditText = null;
        nameEditText = null;
        modeSpinner = null;
        comfirmBtn = null;
        concelBtn = null;
    }

    private void initUI() {
        numberEditText = (EditText) findViewById(R.id.number_baie);
        numberEditText.setOnFocusChangeListener(new EditTextFocusChangeListener());
        nameEditText = (EditText) findViewById(R.id.name_baie);
        comfirmBtn = (Button) findViewById(R.id.confirm);
        concelBtn = (Button) findViewById(R.id.cancel);
        modeSpinner = (Spinner) findViewById(R.id.mode_baie);
        comfirmBtn.setOnClickListener(this);
        concelBtn.setOnClickListener(this);

        blockModes = getResources().getIntArray(R.array.modes_array_int);
        ArrayAdapter<CharSequence> adapter
                = ArrayAdapter.createFromResource(this
                , R.array.modes_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(adapter);
        modeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                modeSpinner.setTag(blockModes[position]);
                if (Constants.DBUG) {
                    Log.i(Constants.TAG, "modeSpinner.getTag():"
                            + position + "==" + blockModes[position] + "==" + modeSpinner.getTag(1));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_BLACK_ADD_TIP:
                InterceptUtil.number = numberEditText.getText().toString();
                InterceptUtil.type = Constants.BLOCK_TYPE_BLACK;
                InterceptUtil.isCloseActivity = true;
                return InterceptUtil.createAdd2WhiteTipDialog(this);
            default:
                break;
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                int mode = 0;
                String name = nameEditText.getText().toString();
                String number = numberEditText.getText().toString();
                if (modeSpinner.getTag() == null) {
                   mode = Constants.BLOCK_TYPE_NUMBER_DEFAULT;
                }else {
                   mode = Integer.parseInt(modeSpinner.getTag().toString()); 
                }

                if (number.length() == 0) {
                    Toast.makeText(this, getResources().getString(R.string.noBlackName), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!PhoneNumberUtils.isGlobalPhoneNumber(number)) {
                    Toast.makeText(this, getResources().getString(R.string.globalPhoneNumber_tip), Toast.LENGTH_SHORT).show();
                    return; 
                }

                if (InterceptUtil.addBlockNameToDb(BlackNameAddActivity.this,name,number,mode)) {
                    this.finish();
                }else {
                    switch (InterceptUtil.isInBlackOrWhiteList(BlackNameAddActivity.this,number)) {
                        case Constants.BLOCK_TYPE_BLACK:
                            Toast.makeText(this, getResources().getString(R.string.hasblockwarning), Toast.LENGTH_SHORT).show();
                            break;
                        case Constants.BLOCK_TYPE_WHITE:
                            showDialog(DIALOG_BLACK_ADD_TIP);
                            break;
                        default:
                            break;
                    }
                }
                break;
            case R.id.cancel:
                this.finish();
                break;
            default:
                break;
        }
    }

    public class EditTextFocusChangeListener implements OnFocusChangeListener{

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            // TODO Auto-generated method stub
            if (!hasFocus) {
                String text = numberEditText.getText().toString();
                String name = GetContactName.getContactName(BlackNameAddActivity.this, "", text);
                nameEditText.setText(name);
            }
        }
        
    }
    
}
