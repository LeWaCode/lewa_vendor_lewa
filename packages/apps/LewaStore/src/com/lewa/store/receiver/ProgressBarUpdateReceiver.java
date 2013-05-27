package com.lewa.store.receiver;

import com.lewa.store.R;
import com.lewa.store.activity.ManageActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ProgressBarUpdateReceiver extends BroadcastReceiver{
    
    private String TAG=ProgressBarUpdateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {        
    	synchronized (this) {
            String url = intent.getStringExtra("urlstr");
            int compeleteSize = intent.getIntExtra("compeleteSize", 0);
            int fileSize = intent.getIntExtra("fileSize", 0);
            int isNormal=intent.getIntExtra("isNormal",1);
            if(isNormal==0){
                Toast.makeText(context,context.getString(R.string.download_failed),Toast.LENGTH_LONG).show();
                return;
            }            
            ManageActivity.MapSize.put(url, Integer.valueOf(fileSize));
            ManageActivity.ProgressBars.put(url, Integer.valueOf(compeleteSize));
//            Log.e(TAG," ProgressBarReceiver receiver length==" + compeleteSize+",url=="+url);
            if (compeleteSize > 0 && fileSize > 0) {
                if(null!=ManageActivity.adapter){
                    ManageActivity.adapter.notifyDataSetChanged();
                }
                if (fileSize == compeleteSize) {
                 /* String localFilePath = downloaders.get(url).getLocalfile();
                    String fileName =StrUtils.getFileNameFromLocalPath(localFilePath);
                    System.out.println(fileName + " download success");
                    ProgressBars.remove(url);
                    MapSize.remove(url);
                    downloaders.get(url).delete(url);
                    downloaders.get(url).reset();
                    downloaders.remove(url);
                    isUpdateAllViews=true;
                    updateAllViews();*/
                    Log.e(TAG, "store download success,url=="+url);
                }
            }
		}
    } 
}
