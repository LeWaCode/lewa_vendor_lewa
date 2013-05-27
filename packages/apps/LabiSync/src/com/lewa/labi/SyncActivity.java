package com.lewa.labi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.lewa.labi.impl.ResultImpl;
import com.lewa.labi.impl.SyncManager;
import com.lewa.labi.intf.AbsTask;

public class SyncActivity extends Activity implements  OnCancelListener {
    private Context mContext;
    private AbsTask labiTask;
    
    private ProgressDialog mProgressDialog;
    
    private static final int SYNC_DIALOG_ID = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mContext = this;
        labiTask = SyncManager.getThirdPartyInstance(SyncManager.COMPANION_ID_LABI);
        labiTask.setHandler(mHandler);
        
        new AlertDialog.Builder(this)
        .setMessage(getResources().getString(R.string.sync_warning_info))
        .setTitle(R.string.sync_title)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setOnCancelListener(this)
        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        })
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Util.isNetWorkEnable(mContext)) {
                    labiTask.invokeSync(mContext);
                    showDialog(SYNC_DIALOG_ID);
                } else {
                    Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT).show();
                    finish();
                }   
            }
        } )
        .show();  
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SYNC_DIALOG_ID:
            {
                if (mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(this);
                    mProgressDialog.setTitle(getString(R.string.sync_title));
                    mProgressDialog.setMessage(getString(R.string.syncing));
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);                    
                    mProgressDialog.setIndeterminate(false);
                    mProgressDialog.setCancelable(true);
                    //add by zenghuaying fix bug #8597
                    mProgressDialog.setOnDismissListener(new DismissListener());
                    //add end
                }
                return mProgressDialog;
            }
        }
        return super.onCreateDialog(id);
    }
    
    private Handler mHandler = new Handler() {  
        public void handleMessage(Message msg) {
            ResultImpl result = (ResultImpl)msg.obj;
            if (result != null) {
                if (result.getItem("TotalNum") != null && result.getItem("currentNum") != null) {
                    int total = (Integer)result.getItem("TotalNum");
                    int curNum = (Integer)result.getItem("currentNum");
                    
                    if (mProgressDialog != null) {
                        mProgressDialog.setProgressNumberFormat(getString(R.string.sync_format));
                        mProgressDialog.setMax(total);
                        mProgressDialog.setProgress(curNum);
                    }
                }
                String labi_ret = (String)(result.getItem("labi_result"));
                if (labi_ret != null) {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    Toast.makeText(mContext, ("success".equals(labi_ret)) ? R.string.sync_success : R.string.sync_fail, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }  
    };

    //add by zenghuaying fix bug #8597
    private class DismissListener implements OnDismissListener{

        @Override
        public void onDismiss(DialogInterface dialog) {
            finish();
        }
        
    }
    //add end
    
    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }
}
