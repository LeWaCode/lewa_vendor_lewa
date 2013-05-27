package com.lewa.face.adapters.online;


import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.ArrayList;

import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.preview.slide.online.LockScreenWallPaper;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeUtil;

import android.content.Context;
import android.content.Intent;

import android.view.View;

/**
 * 
 * @author fulw
 *
 */
public class OnlineLockScreenWallPaperAdapter extends ThumbnailOnlineAdapter{
    
    private static final String TAG = OnlineLockScreenWallPaperAdapter.class.getSimpleName();
    
    public OnlineLockScreenWallPaperAdapter(Context context,ArrayList<ThemeBase> themeBases,int themeType){
        super(context, themeBases, themeType);
        
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Intent intent = new Intent();
        intent.setClass(mContext, LockScreenWallPaper.class);
        intent.putExtra(ThemeConstants.THEMEBASE, (Serializable)v.getTag());
        mContext.startActivity(intent);
    }
    
    @Override
    protected void initPath(ThemeBase themeBase) {
        // TODO Auto-generated method stub
        try {
            url = new StringBuilder().append(ThemeUtil.THEME_URL).append("/").append(URLEncoder.encode(themeBase.getName(), "UTF-8")).toString();
            thumbnailPath = new StringBuilder().append(ThemeConstants.THEME_ONLINE_THUMBNAIL).append("/").append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_WALLPAPER_PREFIX).append(ThemeUtil.getNameNoBuffix(themeBase.getPkg())).toString();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    @Override
    protected ArrayList<String> listDownloadFiles() {
        // TODO Auto-generated method stub
        
        ArrayList<String> downloadedWallpaper = new ArrayList<String>();
        
        File wallpaper = new File(new StringBuilder().append(ThemeConstants.THEME_WALLPAPER).toString());
        File[] wallpapers = wallpaper.listFiles();
        
        if(wallpapers != null){
            for(File file : wallpapers){
                downloadedWallpaper.add(file.getName());
            }
        }
      
        return downloadedWallpaper;
    }
    
    @Override
    protected int addFlag(ThemeBase themeBase) {
        if(downloadedModels.contains(ThemeUtil.getNameNoBuffix(themeBase.getPkg()))
                || downloadedModels.contains(new StringBuilder().append(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX).append(ThemeUtil.getNameNoBuffix(themeBase.getPkg())).toString())){
            return ThemeConstants.DOWNLOADED;
        }
        return -1;
    }
    
    
}
