package com.lewa.face.adapters.online;


import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.ArrayList;

import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.preview.slide.online.BootAnimation;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeFileFilter;
import com.lewa.face.util.ThemeUtil;

import android.content.Context;
import android.content.Intent;

import android.view.View;

/**
 * 
 * @author fulw
 *
 */
public class OnlineBootsAdapter extends ThumbnailOnlineAdapter{
    
    private static final String TAG = OnlineBootsAdapter.class.getSimpleName();
    
    public OnlineBootsAdapter(Context context,ArrayList<ThemeBase> themeBases,int themeType){
        super(context, themeBases, themeType);
        
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Intent intent = new Intent();
        intent.setClass(mContext, BootAnimation.class);
        intent.putExtra(ThemeConstants.THEMEBASE, (Serializable)v.getTag());
        mContext.startActivity(intent);
    }
    
    @Override
    protected void initPath(ThemeBase themeBase) {
        // TODO Auto-generated method stub
        try {
            url = new StringBuilder().append(ThemeUtil.THEME_URL).append("/").append(URLEncoder.encode(themeBase.getName(), "UTF-8")).append("/").append("thumbnail_boots.jpg").toString();
            thumbnailPath = new StringBuilder().append(ThemeConstants.THEME_ONLINE_THUMBNAIL).append("/").append(ThemeConstants.THEME_THUMBNAIL_BOOTS_PREFIX).append(ThemeUtil.getNameNoBuffix(themeBase.getPkg())).toString();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    @Override
    protected ArrayList<String> listDownloadFiles() {
        // TODO Auto-generated method stub
        
        ArrayList<String> downloadedPkg = new ArrayList<String>();
        
        File lwt = new File(new StringBuilder().append(ThemeConstants.THEME_LWT).toString());
        File[] lwts = lwt.listFiles(new ThemeFileFilter("lwt", ThemeFileFilter.ENDSWITH));
        
        if(lwts != null){
            for(File file : lwts){
                downloadedPkg.add(file.getName());
            } 
        }
        
        return downloadedPkg;
    }
    
    @Override
    protected int addFlag(ThemeBase themeBase) {
        String pkg = themeBase.getPkg();
        if(downloadedModels.contains(themeBase.getPkg())){
            return ThemeConstants.DOWNLOADED;
        }else if(sharedPreferences.getLong(pkg, -1) == ThemeConstants.DOWNLOADING){
            return ThemeConstants.DOWNLOADING;
        }
        return -1;
    }
    
}
