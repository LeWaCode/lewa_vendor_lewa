package com.lewa.face.download;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.lewa.face.R;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.util.Logs;
import com.lewa.face.util.ThemeActions;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeUtil;

public class PkgDownload extends DownloadBase{
    
    private static final String TAG = PkgDownload.class.getSimpleName();
	private boolean parselwtpkg;
    
    public PkgDownload(ThemeBase themeBase, Context context) {
        this(themeBase, context,true);
    }
    public PkgDownload(ThemeBase themeBase, Context context,boolean parselwtpkg) {
        super(themeBase, context);
        this.parselwtpkg = parselwtpkg;
    }
    @Override
    protected File targetFile() {
        // TODO Auto-generated method stub
        return new File(new StringBuilder().append(ThemeConstants.THEME_LWT).append("/").append(name).toString());
    }

    @Override
    protected String downloadUrl() {
        // TODO Auto-generated method stub
    	return themeBase.attachment;
//        try {
//            return new StringBuilder().append(ThemeUtil.THEME_URL).append("/").append(URLEncoder.encode(name, "UTF-8")).toString();
//        } catch (UnsupportedEncodingException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        };
//        return null;
    }

    @Override
    protected void downloadSuccess() {
        // TODO Auto-generated method stub
        ThemeUtil.unThemeZIP(targetFile());
    }

    @Override
    protected void onPostExecute(Boolean result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        
        String themePkg = themeBase.getPkg();
        
        if(result){
            Log.e(TAG, "Download success");
            
            ThemeUtil.addThemeInfo(context, themePkg, ThemeConstants.SECOND,parselwtpkg);
            
            if(themePkg != null){
                SharedPreferences.Editor editor = context.getSharedPreferences("DOWNLOADED", Context.MODE_PRIVATE).edit();
                editor.putLong(themePkg, ThemeConstants.DOWNLOADED);
                editor.commit();
            }
            
            Intent importThemeOver = new Intent();
            importThemeOver.putExtra(ThemeConstants.THEMEBASE, themeBase);
            importThemeOver.setAction(ThemeActions.ADD_THEME_OVER);
            context.sendBroadcast(importThemeOver);

            Intent downloadOver = new Intent();
            downloadOver.setAction(ThemeActions.DOWNLOAD_THEME_OVER);
            context.sendBroadcast(downloadOver);
            
        }else {
            
            SharedPreferences.Editor editor = context.getSharedPreferences("DOWNLOADED", Context.MODE_PRIVATE).edit();
            editor.putLong(themePkg, ThemeConstants.DOWNLOADFAIL);
            editor.commit();
            
            Log.e(TAG, "Download fail or unZIP fail");
            Toast.makeText(context, context.getText(R.string.download_or_unzip_fail), Toast.LENGTH_SHORT).show();
            
        }
    }
    
    

}
