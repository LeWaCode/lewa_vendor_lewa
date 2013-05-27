package com.lewa.face.preview.slide.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;


import com.lewa.face.R;

import com.lewa.face.app.ThemeApplication;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.preview.slide.adapter.LocalSlideAdapter;
import com.lewa.face.preview.slide.base.SlideBaseActivity;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeFileFilter;
import com.lewa.face.util.ThemeUtil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.lewa.view.PagerAdapter;
import android.view.View;
import android.widget.TextView;

public class IconsStyle extends SlideBaseActivity{
    
    private static final String TAG = IconsStyle.class.getSimpleName();
    
    private TextView mThemeApply = null;
    private TextView mThemeShare = null;
    private TextView mThemeDelete = null;
    
    private LocalSlideAdapter localSlideAdapter = null;
    
    @Override
	protected void setContentView() {
		setContentView(R.layout.theme_preview_slide_no_model);
	}
    
    @Override
    protected PagerAdapter initAdapter() {
        localSlideAdapter = new LocalSlideAdapter(source, mContext);
        return localSlideAdapter;
    }
    
    @Override
    protected void initOtherViews() {
        // TODO Auto-generated method stub
        findViewById(R.id.theme_check_info).setVisibility(View.GONE);
        
        findViewById(R.id.theme_bottom_bar_online).setVisibility(View.GONE);
        mThemeApply = (TextView) findViewById(R.id.theme_apply);
        mThemeShare = (TextView) findViewById(R.id.theme_share);
        mThemeDelete = (TextView) findViewById(R.id.theme_delete);

        mThemeApply.setOnClickListener(this);
        mThemeShare.setOnClickListener(this);
        mThemeDelete.setOnClickListener(this);
    }
    
    /**
     * look up preview pictures
     * @return
     */
    protected ArrayList<String> getList() {
        
        String pkg = themeBase.getPkg();
        
        if(pkg == null){
            return null;
        }
        
        ArrayList<String> list = new ArrayList<String>();
        File preview = new File(new StringBuilder().append(ThemeConstants.THEME_LOCAL_PREVIEW).append("/").append(ThemeUtil.getNameNoBuffix(pkg)).toString());
        File[] files = preview.listFiles(new ThemeFileFilter(ThemeConstants.PREVIEW_ICONS, ThemeFileFilter.INDEXOF));
        
        if(files != null){
            for(File file : files){
                list.add(file.getAbsolutePath());
            }
        }
        
        return list;   
   }

   @Override
   public void onClick(View v) {
       switch (v.getId()) {
       case R.id.theme_apply:
       {
           FileInputStream iconsSource = null;
           FileOutputStream iconsTarget = null;
           
            try {
                
                String pkg = themeBase.getPkg();
                
                String nameNoLwt = ThemeUtil.getNameNoBuffix(pkg);
                
                
                String soruce = new StringBuilder().append(ThemeConstants.THEME_MODEL_ICONS_STYLE).append("/")
                    .append(ThemeConstants.THEME_MODEL_ICONS)
                    .append("_").append(nameNoLwt).toString();
                
                File sourceFile = new File(soruce);
                
                if(sourceFile.exists()){
                    
                    File iconstemp = new File(ThemeConstants.THEME_FACE_ICONS_TEMP);
                    File iconsparent = iconstemp.getParentFile();
                    if(!iconsparent.exists()){
                        iconsparent.mkdirs();
                        ThemeUtil.changeFilePermission(iconsparent);
                    }
                    
                    iconsSource = new FileInputStream(soruce);
                    iconsTarget = new FileOutputStream(iconstemp);
                    
                    boolean iconssuccess = ThemeUtil.writeSourceToTarget(iconsSource, iconsTarget);
                    
                    if(iconssuccess){
                        File target = new File(ThemeConstants.THEME_FACE_ICONS);
                        iconstemp.renameTo(target);
                        
                        ThemeUtil.changeFilePermission(target);
                        
                        ThemeUtil.showToast(mContext, R.string.theme_set_success, true);
                        
                        SharedPreferences.Editor editor = mContext.getSharedPreferences("CURRENT_USING", Context.MODE_PRIVATE).edit();
                        editor.putString("icons", themeBase.getPkg())
                        .commit();
                      
                        ThemeUtil.applyThemeAndExit(mContext);
                        
                    }else {
                        ThemeUtil.showToast(mContext, R.string.theme_set_fail, true);
                    }
                    
                }else {
                    ThemeUtil.showToast(mContext, R.string.theme_set_success, true);
                }
                
                
                
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                ThemeUtil.showToast(mContext, R.string.theme_set_fail, true);
            }finally{
                try {
                    if(iconsTarget != null){
                        iconsTarget.close();
                        iconsTarget = null;
                    }
                    if(iconsSource != null){
                        iconsSource.close();
                        iconsSource = null;
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
           
           break;
       }
       case R.id.theme_share:
       {
    	   ThemeUtil.shareByBT(themeBase,this);
           break;
       }
       case R.id.theme_delete:
       {
    	   if(ThemeConstants.DEFAULT_THEME_PKG.equals(themeBase.getPkg())){
    		   ThemeUtil.defaultThemeDialog(mContext);
    	   }else {
    		   ThemeUtil.deleteThemeDialog(this, themeBase);
    	   }
           
           break;
       }
       default:
           break;
       }
       
   }

    @Override
    protected void onDestroy() {
        ThemeApplication.activities.remove(this);
        localSlideAdapter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected ThemeBase initThemeBase(Intent intent) {
        
        themeBase = (ThemeBase) intent.getSerializableExtra(ThemeConstants.THEMEBASE); 
        return themeBase;
    }

}