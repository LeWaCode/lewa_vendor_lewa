package com.lewa.face.model;

import com.lewa.face.R;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.util.ThemeUtil;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public class ThemeDeleteTask extends AsyncTask<String, Integer, String> {
    private ThemeBase themeBase =null;
    private Activity activity =null;
    private ProgressDialog mProgressDialog = null;
    public  ThemeDeleteTask(ThemeBase themeBase, Activity activity) {
        this.themeBase =themeBase;
        this.activity = activity;
        
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle(R.string.theme_delete);
        mProgressDialog.setMessage(activity.getString(R.string.deleting));
        mProgressDialog.show();
    }
    
    @Override
    protected String doInBackground(String... arg0) {
        
        ThemeUtil.deleteThemeInfo(activity, themeBase);
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        try{
            mProgressDialog.cancel();
        }catch(Exception e){
            android.util.Log.e("LewaFace", "Exception on: View not attached to window manager");
        }
       
    }

    

}
