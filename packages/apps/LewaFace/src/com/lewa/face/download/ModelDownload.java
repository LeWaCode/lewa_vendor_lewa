package com.lewa.face.download;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.lewa.face.R;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.util.ThemeActions;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeHelper;
import com.lewa.face.util.ThemeUtil;

public class ModelDownload extends DownloadBase{
    
    private static final String TAG = ModelDownload.class.getSimpleName();
    
    public ModelDownload(ThemeBase themeBase, Context context) {
        super(themeBase, context);
    }

    @Override
    protected File targetFile() {

        return new File(new StringBuilder().append(ThemeConstants.THEME_WALLPAPER).append("/").append(ThemeUtil.getNameNoBuffix(name)).toString());
    }

    @Override
    protected String downloadUrl() {
        // TODO Auto-generated method stub
        try {
            return themeBase.attachment;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        };
        return null;
    }

    @Override
    protected void downloadSuccess() {
        // TODO Auto-generated method stub

        try {
            String sourcePath = new StringBuilder().append(ThemeConstants.THEME_ONLINE_WALLPAPER_THUMBNAIL).append("/").append(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX).append(ThemeUtil.getNameNoBuffix(name)).toString();
            File source = new File(sourcePath);
            
            String targetWallpaperPath = new StringBuilder().append(ThemeConstants.THEME_LOCAL_WALLPAPER_THUMBNAIL).append("/").append(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX).append(ThemeUtil.getNameNoBuffix(name)).toString();
            File targetWallpaper = new File(targetWallpaperPath);
            
            FileUtils.copyFile(source, targetWallpaper);
            
            String targetLockScreenWallpaperPath = new StringBuilder().append(ThemeConstants.THEME_LOCAL_WALLPAPER_THUMBNAIL).append("/").append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_WALLPAPER_PREFIX).append(ThemeUtil.getNameNoBuffix(name)).toString();
            File targetLockScreenWallpaper = new File(targetLockScreenWallpaperPath);
            
            FileUtils.copyFile(source, targetLockScreenWallpaper);
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }


    @Override
    protected void onPostExecute(Boolean result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        try{
        String themePkg = themeBase.getPkg();
        
        if(result){
            Log.i(TAG, "Download success : " + themeBase.getPkg());
            
            ArrayList<ThemeBase> wallpapers = ThemeHelper.getThemeBases(context, ThemeConstants.THEME_LOCAL, ThemeConstants.WALLPAPER);
            ArrayList<ThemeBase> lockScreenWallPapers = ThemeHelper.getThemeBases(context, ThemeConstants.THEME_LOCAL, ThemeConstants.LSWALLPAPER);
            
            
            ThemeBase lockscreenwallpaper = new ThemeBase(null,themeBase.getPkg(),null,false);
            lockscreenwallpaper.setCnName(themeBase.getCnName());
            lockscreenwallpaper.setEnName(themeBase.getEnName());
            lockscreenwallpaper.setCnAuthor(themeBase.getCnAuthor());
            lockscreenwallpaper.setEnAuthor(themeBase.getEnAuthor());
            lockscreenwallpaper.setPkg(themeBase.getPkg());
            lockscreenwallpaper.setSize(themeBase.getSize());
            
            wallpapers.add(ThemeConstants.SECOND, themeBase);
            lockScreenWallPapers.add(ThemeConstants.SECOND, lockscreenwallpaper);
            
            ThemeHelper.saveThemeBases(context, wallpapers, ThemeConstants.THEME_LOCAL, ThemeConstants.WALLPAPER);
            
            ThemeHelper.saveThemeBases(context, lockScreenWallPapers, ThemeConstants.THEME_LOCAL, ThemeConstants.LSWALLPAPER);
            if(themePkg != null){
                SharedPreferences.Editor editor = context.getSharedPreferences("DOWNLOADED", Context.MODE_PRIVATE).edit();
                editor.putLong(themePkg, ThemeConstants.DOWNLOADED);
                editor.commit();
            }
            
            Intent importThemeOver = new Intent();
            importThemeOver.setAction(ThemeActions.ADD_THEME_OVER);
            context.sendBroadcast(importThemeOver);
            
            Intent downloadOver = new Intent();
            downloadOver.setAction(ThemeActions.DOWNLOAD_THEME_OVER);
            context.sendBroadcast(downloadOver);
            
            ThemeUtil.showToast(context, R.string.download_complete, true);
        }else {
            
            SharedPreferences.Editor editor = context.getSharedPreferences("DOWNLOADED", Context.MODE_PRIVATE).edit();
            editor.putLong(themePkg, ThemeConstants.DOWNLOADFAIL);
            editor.commit();
            
            Log.e(TAG, "Download fail or unZIP fail");
            ThemeUtil.showToast(context, R.string.download_fail, true);
            
        }
        }catch(Exception e){
           Editor editor = context.getSharedPreferences("DOWNLOADING", Context.MODE_PRIVATE).edit();
           editor.clear();
           editor.commit();
           e.printStackTrace();
        }
    }
    
    

}
