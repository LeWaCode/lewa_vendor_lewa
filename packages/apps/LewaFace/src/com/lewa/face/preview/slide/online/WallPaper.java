package com.lewa.face.preview.slide.online;

import java.util.ArrayList;

import android.content.Context;
import android.support.lewa.view.PagerAdapter;
import android.view.View;

import com.lewa.face.R;
import com.lewa.face.download.DownloadBase;
import com.lewa.face.download.ModelDownload;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.preview.slide.adapter.OnlinePkgNewServerAdapter;
import com.lewa.face.preview.slide.base.OnlineSlideBase;
import com.lewa.face.util.ThemeConstants;

public class WallPaper extends OnlineSlideBase{
    
    private static final String TAG = WallPaper.class.getSimpleName();
    
    @Override
	protected void setContentView() {
		setContentView(R.layout.theme_preview_slide_no_model);
		
		findViewById(R.id.theme_check_info).setVisibility(View.GONE);
	}
    
    @Override
    protected PagerAdapter initAdapter() {
        // TODO Auto-generated method stub
        onlineSlideAdapter = new OnlinePkgNewServerAdapter(source,mContext,themeBase);
        return onlineSlideAdapter;
    }
    
    @Override
    protected DownloadBase getDownloadBase(ThemeBase themeBase, Context context) {
        // TODO Auto-generated method stub
        return new ModelDownload(themeBase, context);
    }

    @Override
    protected ArrayList<String> getList() {
        String pkg = themeBase.getPkg();
        
        if(pkg == null){
            return null;
        }
        
        ArrayList<String> list = new ArrayList<String>();
        
        String wallpaperPath = new StringBuilder().append(ThemeConstants.THEME_ONLINE_WALLPAPRE)
        .append("/")
        .append(pkg).toString();
        
        list.add(wallpaperPath);
        
        return list;   
    }

}