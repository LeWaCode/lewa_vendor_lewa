package com.lewa.face.preview.slide.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;


import com.lewa.face.R;

import com.lewa.face.app.ThemeApplication;
import com.lewa.face.model.ThemeModelCheckBoxPreference;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.preview.slide.adapter.LocalSlideAdapter;
import com.lewa.face.preview.slide.base.SlideBaseActivity;
import com.lewa.face.util.ThemeActions;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeFileFilter;
import com.lewa.face.util.ThemeUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.lewa.view.PagerAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class LockScreenStyle extends SlideBaseActivity{
    
    private static final String TAG = LockScreenStyle.class.getSimpleName();
    
    private TextView mThemeApply = null;
    private TextView mThemeShare = null;
    private TextView mThemeDelete = null;

    private LocalSlideAdapter localSlideAdapter = null;
    
    @Override
	protected void setContentView() {
		setContentView(R.layout.theme_preview_slide_has_model);
	}
    
    @Override
    protected PagerAdapter initAdapter() {
        localSlideAdapter = new LocalSlideAdapter(source, mContext);
        return localSlideAdapter;
    }
    
    @Override
    protected void initOtherViews() {
        // TODO Auto-generated method stub
    	TextView mThemeModeNumber = (TextView) findViewById(R.id.theme_mode_number);
        mThemeModeNumber.setText(getString(R.string.theme_mode_number, themeBase.getThemeModelInfo().getContainModelNum()));
        
        ImageView themeCheckInfo = (ImageView) findViewById(R.id.theme_check_info);
        themeCheckInfo.setOnClickListener(this);
        
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
        File[] files = preview.listFiles(new ThemeFileFilter(ThemeConstants.PREVIEW_LOCKSCREEN_STYLE, ThemeFileFilter.INDEXOF));
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
           new ApplyLockScreenTask().execute("");
           
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
    	   }else{
    		   ThemeUtil.deleteThemeDialog(this, themeBase);
    	   }
           
           break;
       }
       case R.id.theme_check_info:
       {
           Intent intent = new Intent();
           intent.setClass(mContext, ThemeModelCheckBoxPreference.class);
           intent.putExtra(ThemeConstants.THEMEBASE, themeBase);
           startActivity(intent);
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
    
    private class ApplyLockScreenTask extends AsyncTask<String, Void, Boolean>{

        private ProgressDialog progressDialog;
        
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(R.string.setting_lockscreen_title);
            progressDialog.setMessage(mContext.getString(R.string.setting_lockscreen_msg));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            FileInputStream lockscreenSource = null;
            FileOutputStream lockscreenTarget = null;
            
            FileInputStream wallpaperSource = null;
            FileOutputStream wallpaperTarget = null;
            
             try {
                 
                 String pkg = themeBase.getPkg();
                 
                 String nameNoLwt = ThemeUtil.getNameNoBuffix(pkg);
                 
                 {
                     
                     String wallpaperSourcePath = new StringBuilder().append(ThemeConstants.THEME_WALLPAPER)
                     .append("/").append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_WALLPAPER_PREFIX)
                     .append(nameNoLwt).toString();
                     
                     File lockscreenWallpaper = new File(wallpaperSourcePath);
                     
                     /**
                      * 只有当源文件存在时，才会进行以后的一系列操作
                      */
                     if(lockscreenWallpaper.exists()){
                         
                         /**
                          * 创建临时文件
                          */
                         File wallpapertemp = new File(ThemeConstants.THEME_FACE_LOCKSCREENWALLPAPER_TEMP);
                         File wallpaperparent = wallpapertemp.getParentFile();
                         if(!wallpaperparent.exists()){
                             wallpaperparent.mkdirs();
                             
                             ThemeUtil.changeFilePermission(wallpaperparent);
                         }
                         
                         wallpaperSource = new FileInputStream(wallpaperSourcePath);
                         wallpaperTarget = new FileOutputStream(wallpapertemp);
                         
                         ThemeUtil.writeSourceToTarget(wallpaperSource, wallpaperTarget);
                         
                         File target = new File(ThemeConstants.THEME_FACE_LOCKSCREENWALLPAPER);
                         wallpapertemp.renameTo(target);
                         
                         ThemeUtil.changeFilePermission(target);
                         
                         SharedPreferences.Editor editor = mContext.getSharedPreferences("CURRENT_USING", Context.MODE_PRIVATE).edit();
                         editor.putString("lockscreenwallpaper", themeBase.getPkg())
                         .commit();
                     }
                     
                 }
                 //================================================================
                 {
                     
                     String soruce = new StringBuilder().append(ThemeConstants.THEME_MODEL_LOCKSCREEN_STYLE).append("/")
                         .append(ThemeConstants.THEME_MODEL_LOCKSCREEN)
                         .append("_").append(nameNoLwt).toString();
                     
                     File lockscreenModel = new File(soruce);
                     if(lockscreenModel.exists()){
                         
                         File lockscreentemp = new File(ThemeConstants.THEME_FACE_LOCKSCREEN_TEMP);
                         File lockscreenparent = lockscreentemp.getParentFile();
                         if(!lockscreenparent.exists()){
                             lockscreenparent.mkdirs();
                             
                             ThemeUtil.changeFilePermission(lockscreenparent);
                         }
                         
                         lockscreenSource = new FileInputStream(soruce);
                         lockscreenTarget = new FileOutputStream(lockscreentemp);
                         
                         boolean lockscreensuccess = ThemeUtil.writeSourceToTarget(lockscreenSource, lockscreenTarget);
                         
                         if(lockscreensuccess){
                             File target = new File(ThemeConstants.THEME_FACE_LOCKSCREEN);
                             lockscreentemp.renameTo(target);
                             
                             ThemeUtil.changeFilePermission(target);
                             
                             SharedPreferences.Editor editor = mContext.getSharedPreferences("CURRENT_USING", Context.MODE_PRIVATE).edit();
                             editor.putString("lockscreen", themeBase.getPkg())
                             .commit();
                             
                             ThemeUtil.lockscreenChanged(mContext);
                             
                             Intent applayThemeOver = new Intent();
                             applayThemeOver.putExtra(ThemeConstants.THEMEBASE, themeBase);
                             applayThemeOver.setAction(ThemeActions.APPLAY_THEME_OVER);
                             mContext.sendBroadcast(applayThemeOver);
                             
                             return true;
                         }else {
                             return false;
                         }
                     }
                     
                 }   
                 
             } catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
                 return false;
             }finally{
                 try {
                     if(lockscreenTarget != null){
                         lockscreenTarget.close();
                         lockscreenTarget = null;
                     }
                     if(lockscreenSource != null){
                         lockscreenSource.close();
                         lockscreenSource = null;
                     }
                     if(wallpaperSource != null){
                         wallpaperSource.close();
                         wallpaperSource = null;
                     }
                     if(wallpaperTarget != null){
                         wallpaperTarget.close();
                         wallpaperTarget = null;
                     }
                 } catch (Exception e2) {
                     e2.printStackTrace();
                     return false;
                 }
             }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            
            progressDialog.dismiss();
            progressDialog = null;
            
            if(result){
                ThemeUtil.showToast(mContext, R.string.theme_set_success, true);
            }else{
                ThemeUtil.showToast(mContext, R.string.theme_set_fail, true);
            }
            
        }
        
        
        
    }

    @Override
    protected ThemeBase initThemeBase(Intent intent) {
        
        themeBase = (ThemeBase) intent.getSerializableExtra(ThemeConstants.THEMEBASE); 
        return themeBase;
    }

}
