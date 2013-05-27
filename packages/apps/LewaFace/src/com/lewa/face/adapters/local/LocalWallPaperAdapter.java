package com.lewa.face.adapters.local;


import java.io.Serializable;

import java.util.ArrayList;



import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.preview.slide.local.WallPaper;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeHelper;
import com.lewa.face.util.ThemeUtil;

import android.content.Context;
import android.content.Intent;

import android.os.Handler;
import android.view.View;
import com.lewa.face.util.Logs;

/**
 * 
 * @author fulw
 *
 */
public class LocalWallPaperAdapter extends ThumbnailLocalAdapter{
    
    private static final String TAG = LocalWallPaperAdapter.class.getSimpleName();
    
    public LocalWallPaperAdapter(Context context,ArrayList<ThemeBase> themeBases,int themeType,Handler handler){
        super(context, themeBases, themeType, handler);
    	
    }
    
    @Override
    public void onClick(View v) {
        
        Intent intent = new Intent();
        intent.setClass(mContext, WallPaper.class);
        intent.putExtra(ThemeConstants.THEMEBASE, (Serializable)v.getTag());
        mContext.startActivity(intent);
    }
    
    @Override
	protected ArrayList<ThemeBase> updateThemeBases() {
		// TODO Auto-generated method stub
		return ThemeHelper.getThemeBases(mContext, ThemeConstants.THEME_LOCAL, ThemeConstants.WALLPAPER);
	}

    @Override
    protected void attachLabel(ThemeBase themeBase,int position) {
        if(sharedPreferences.getString("wallpaper", ThemeConstants.DEFAULT_THEME_PKG).equals(themeBase.getPkg())){
            statusFlags.put(position, statusFlagBitmap);
        }else{
            statusFlags.put(position, null);
        }
    }

    @Override
    protected String getThumbnailPath(ThemeBase themeBase) {
        // TODO Auto-generated method stub
        return new StringBuilder().append(ThemeConstants.THEME_LOCAL_WALLPAPER_THUMBNAIL).append("/").append(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX).append(ThemeUtil.getNameNoBuffix(themeBase.getPkg())).toString();
    }
   
}
