package com.lewa.pond.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

public class CustomProgressDailog {

	private Context mContext;	
	private ProgressDialog progressDialog;
	
	public CustomProgressDailog(Context c){
		this.mContext=c;
		progressDialog=new ProgressDialog(mContext);
	}

    public void setProperties(String title,String msg){
    	progressDialog.setTitle(title);
    	progressDialog.setMessage(msg);
    	progressDialog.setIndeterminate(true);
    	progressDialog.setCancelable(true);
    	progressDialog.setOnCancelListener(new OnCancelListener() {			
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				if(null!=progressDialog){
					progressDialog.dismiss();
				}				
			}
		});
    }
    
    public void show(){
    	if(null!=progressDialog){
    		progressDialog.show();
    	}    	
    }
    
    public void dismiss(){
    	if(null!=progressDialog){
    		progressDialog.dismiss();
    		progressDialog=null;
    	}    	
    }
}