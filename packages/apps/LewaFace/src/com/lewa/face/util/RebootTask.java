package com.lewa.face.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.os.PowerManager;
import android.os.Build;

import com.lewa.face.R;

public class RebootTask extends AsyncTask<String, String, String>{

    private ProgressDialog progressDialog;
    private Context mContext;
    
    public RebootTask(Context context){
        mContext = context;
    }
    
    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
        
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(R.string.rebooting_title);
        progressDialog.setMessage(mContext.getString(R.string.rebooting_msg));
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
         ThemeUtil.unzipFontsBeforeReboot();
         return null;
    }

    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        
        progressDialog.dismiss();

        //Begin Add by jiangjiawen for defy's and defy+'s reboot 20120813
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        try {
            Log.i("RebootTask", "reboot now");
            SuCommander sc = new SuCommander();
            if("MB525".equalsIgnoreCase(Build.MODEL) || "MB526".equalsIgnoreCase(Build.MODEL)) {
                pm.reboot("2nd-init");
            } else {
                sc.exec("reboot");
            }
        //End add
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    
    
}
