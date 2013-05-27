package com.lewa.face.adapters.local;


import java.io.Serializable;

import java.util.ArrayList;


import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.preview.slide.local.IconsStyle;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeHelper;
import com.lewa.face.util.ThemeUtil;

import android.content.Context;
import android.content.Intent;

import android.os.Handler;
import android.view.View;

/**
 * 
 * @author fulw
 *
 */
public class LocalIconsStyleAdapter extends ThumbnailLocalAdapter{
    
    private static final String TAG = LocalIconsStyleAdapter.class.getSimpleName();
    
    
    public LocalIconsStyleAdapter(Context context,ArrayList<ThemeBase> themeBases,int themeType,Handler handler){
    	super(context, themeBases, themeType, handler);
    }

    @Override
    public void onClick(View v) {
        
        Intent intent = new Intent();
        intent.setClass(mContext, IconsStyle.class);
        intent.putExtra(ThemeConstants.THEMEBASE, (Serializable)v.getTag());
        mContext.startActivity(intent);
    }

	@Override
	protected ArrayList<ThemeBase> updateThemeBases() {
		// TODO Auto-generated method stub
		return ThemeHelper.getThemeBases(mContext, ThemeConstants.THEME_LOCAL, ThemeConstants.ICONS);
	}
	
	@Override
    protected void attachLabel(ThemeBase themeBase,int position) {
	    if(sharedPreferences.getString("icons", "NA").equals(themeBase.getPkg())){
            statusFlags.put(position, statusFlagBitmap);
        } 
    }

    @Override
    protected String getThumbnailPath(ThemeBase themeBase) {
        // TODO Auto-generated method stub
        return new StringBuilder().append(ThemeConstants.THEME_LOCAL_THUMBNAIL).append("/").append(ThemeConstants.THEME_THUMBNAIL_ICONS_PREFIX).append(ThemeUtil.getNameNoBuffix(themeBase.getPkg())).toString();
    }

}
