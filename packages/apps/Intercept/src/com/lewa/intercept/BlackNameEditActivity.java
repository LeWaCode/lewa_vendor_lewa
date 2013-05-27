package com.lewa.intercept;

import com.lewa.intercept.R;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lewa.intercept.intents.Constants;
import com.lewa.intercept.intents.InterceptIntents;

import android.provider.InterceptConstants;

public class BlackNameEditActivity extends Activity implements OnClickListener {
    // private final String TAG = "Intercept";

    private int blockMode = 0;
    private String userId = null;
    private String userName = null;
    private String blockUserNumber = null;

    private TextView numberEditText;
    private EditText nameEditText;
    private Spinner modeSpinner;
    private Button comfirmBtn;
    private Button concelBtn;

    private int[] blockModes;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.black_name_editor);

        Bundle b = this.getIntent().getExtras();

        userId = b.getString("userId"); // get userInfo from BlockNameActivity

        blockMode = b.getInt("userMode");

        switch (blockMode) {
            case Constants.BLOCK_TYPE_NUMBER_DEFAULT:
                position = 0;
                break;
            case Constants.BLOCK_TYPE_NUMBER_CALL:
                position = 1;
                break;
            case Constants.BLOCK_TYPE_NUMBER_MSG:
                position = 2;
                break;
            default:
                break;
        }

        userName = b.getString("userName");
        blockUserNumber = b.getString("userNumber");

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
        numberEditText = (TextView) findViewById(R.id.number_baie);
        nameEditText = (EditText) findViewById(R.id.name_baie);
        comfirmBtn = (Button) findViewById(R.id.confirm);
        concelBtn = (Button) findViewById(R.id.cancel);
        comfirmBtn.setOnClickListener(this);
        concelBtn.setOnClickListener(this);

        numberEditText.setText(blockUserNumber);
        nameEditText.setText(userName);

        blockModes = getResources().getIntArray(R.array.modes_array_int);

        modeSpinner = (Spinner) findViewById(R.id.mode_baie);
        ArrayAdapter<CharSequence> adapter
                = ArrayAdapter.createFromResource(this
                , R.array.modes_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        modeSpinner.setAdapter(adapter);
        modeSpinner.setSelection(position);
        modeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                modeSpinner.setTag(blockModes[position]);
                blockMode = blockModes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                if (updateDataBaseInterceptData(userId)) {
                   this.finish();
                }
                break;
            case R.id.cancel:
                this.finish();
                break;
            default:
                break;
        }
    }

    // update the data
    public boolean updateDataBaseInterceptData(String userId) { // Update DataBase
        if (Constants.DBUG) {
            Log.i(Constants.TAG, "updateDataBaseInterceptData()");
        }
        
        userName = nameEditText.getText().toString();
        blockUserNumber = numberEditText.getText().toString();

        if(blockUserNumber.length() == 0){
            Toast.makeText(this, getResources().getString(R.string.noBlackName), Toast.LENGTH_SHORT).show();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(InterceptConstants.COLUMN_MODE, String.valueOf(blockMode));
        values.put(InterceptConstants.COLUMN_NAME, userName);
        values.put(InterceptConstants.COLUMN_NUMBER, blockUserNumber);

        String selection = InterceptConstants.COLUMN_BLOCK_NAME_ID + " = ?";
        int result = getContentResolver().update(
                InterceptConstants.CONTENT_URI, values, selection, new String[] { userId });

        if(result > 0) {
            Intent intent = new Intent(InterceptIntents.LEWA_INTERCEPT_UPATEBLACKINCACHE_ACTION);
            intent.putExtra("number", blockUserNumber);
            intent.putExtra("newNumberType", blockMode);
            sendBroadcast(intent);
        }
        return true;
    }
}
