package com.lewa.face.local;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import com.lewa.face.R;
import com.lewa.face.adapters.local.LocalLockScreenWallPaperAdapter;
import com.lewa.face.adapters.local.ThumbnailLocalAdapter;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.pojos.ThemeModelInfo;
import com.lewa.face.util.ThemeFileFilter;
import com.lewa.face.util.ThemeFileSort;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeHelper;
import com.lewa.face.util.ThemeUtil;
import com.lewa.os.ui.ActivityResultBridge.ActivityResultReceiver;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * 
 * @author fulw
 *
 */
public class LockScreenWallPaper extends LocalBaseActivity{
    
    private static final String TAG = LockScreenWallPaper.class.getSimpleName();
    
    private static final int SELECT_PICTRUE = 1;
    
    @Override
    protected void initViews() {
        findViewById(R.id.customize).setVisibility(View.GONE);
        findViewById(R.id.import_theme).setVisibility(View.GONE);
        
        Button importFromApps = (Button) findViewById(R.id.import_from_apps);
        importFromApps.setOnClickListener(this);
    }

    
    @Override
    protected ArrayList<ThemeBase> getThemeBases() {
        // TODO Auto-generated method stub
        return ThemeHelper.getThemeBases(mContext,ThemeConstants.THEME_LOCAL,ThemeConstants.LSWALLPAPER);
    }



    @Override
    protected ThumbnailLocalAdapter localAdapterInstance() {
        // TODO Auto-generated method stub
        return new LocalLockScreenWallPaperAdapter(mContext,themeBases,ThemeConstants.THEME_LOCAL,handler);
    }
   
    @Override
    protected void parseLocalModelInfo(){
        File lwtRoot = new File(ThemeConstants.THEME_WALLPAPER);
        File[] localLockScreenWallpapers = lwtRoot.listFiles(new ThemeFileFilter(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_WALLPAPER_PREFIX,ThemeFileFilter.STARTSWITH));
        
        /**
         * 列出在线下载的桌面壁纸或者锁屏壁纸
         */
        File[] onlines = lwtRoot.listFiles(new ThemeFileFilter(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX, ThemeFileFilter.NOT));
        
        File[] totalFiles = ThemeUtil.contact(localLockScreenWallpapers, onlines);
             
        if((totalFiles != null) && (totalFiles.length != 0)){
            
            Arrays.sort(totalFiles, new ThemeFileSort());
            
            ArrayList<ThemeBase> themePkgs = ThemeHelper.getThemeBases(mContext, ThemeConstants.THEME_LOCAL, ThemeConstants.THEMEPKG);
            
            int length = totalFiles.length;
            for(int i=0;i<length;i++){
                File lockScreenWallPaper = totalFiles[i];
                String lockScreenWallPaperName = lockScreenWallPaper.getName();
                
                ThemeBase lockscreenWallpaper = null;
                
                String themePkg = null;
                int position = lockScreenWallPaperName.lastIndexOf("_");
                if(position != -1){//主题包中的桌面壁纸或者锁屏壁纸
                    themePkg = new StringBuilder().append(lockScreenWallPaperName.substring(lockScreenWallPaperName.lastIndexOf("_") + 1)).append(ThemeConstants.BUFFIX_LWT).toString();
                    
                    /**
                     * 如果是默认主题资源，则返回
                     */
                    if(themePkg.equals(ThemeConstants.DEFAULT_THEME_PKG)){
                        continue;
                    }
                    
                    /**
                     * 如果模块存在，但是原始lwt文件不存在的话，则视为无效模块，因为主题信息在lwt文件中
                     */
                    String lwtPath = new StringBuilder().append(ThemeConstants.THEME_LWT).append("/").append(themePkg).toString();
                    if(!new File(lwtPath).exists()){
                        continue;
                    }
                    
                    ThemeModelInfo themeModelInfo = new ThemeModelInfo(themePkg);
                    lockscreenWallpaper = new ThemeBase(themeModelInfo,themePkg,null,true);
                    
                    for(ThemeBase themeInfo : themePkgs){
                        if(themePkg.equals(themeInfo.getPkg())){
                            lockscreenWallpaper.setCnAuthor(themeInfo.getCnAuthor());
                            lockscreenWallpaper.setEnAuthor(themeInfo.getEnAuthor());
                            lockscreenWallpaper.setCnName(themeInfo.getCnName());
                            lockscreenWallpaper.setEnName(themeInfo.getEnName());
                            break;
                        }
                    }
                    
                }else {//在线下载的桌面壁纸或者锁屏壁纸
                    themePkg = lockScreenWallPaperName;
                    
                    lockscreenWallpaper = new ThemeBase();
                    
                    lockscreenWallpaper.setPkg(themePkg);
                    lockscreenWallpaper.setCnAuthor("N/A");
                    lockscreenWallpaper.setEnAuthor("N/A");
                    lockscreenWallpaper.setCnName(themePkg);
                    lockscreenWallpaper.setEnName("N/A");
                }
               
                
                lockscreenWallpaper.setSize(ThemeUtil.fileLengthToSize(lockScreenWallPaper.length()));
                if(themeBases.size() == 0){
                    themeBases.add(lockscreenWallpaper);
                 }else{
                    themeBases.add(ThemeConstants.SECOND,lockscreenWallpaper);
                }
                
                
            }
            ThemeHelper.saveThemeBases(mContext, themeBases, ThemeConstants.THEME_LOCAL, ThemeConstants.LSWALLPAPER);
        }
    }


    /**
     * 相当于普通Activity的onActivityResult
     */
    @Override
    public void handleActivityResult(ActivityResultReceiver resultReceiver, int requestCode,
            int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(data == null){
//            ThemeUtil.showToast(mContext, R.string.theme_set_fail, true);
            return;
        }
        
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();  
            ContentResolver cr = this.getContentResolver();
            InputStream is = null;
            try {
                 is = cr.openInputStream(uri);
                 
                 setLockScreenWallPaper(is);
                 
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                try {
                    if(is != null){
                        is.close();
                        is = null;
                    }
                    if(uri != null){
                        File file = new File(new URI(uri.toString()));
                        if(file.exists()){
                            file.delete();
                        }
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                SharedPreferences.Editor editor = mContext.getSharedPreferences("CURRENT_USING", Context.MODE_PRIVATE).edit();
                editor.putString("wallpaper", null).commit();
            }  
        }  
    }
    
    private void setLockScreenWallPaper(InputStream is){
        FileOutputStream fos = null;
         try {
             
             if(is != null){
            	 
            	 String lockscreenWallPaper = "/data/system/face/wallpaper/lock_screen_wallpaper.jpg.temp";
                 File temp = new File(lockscreenWallPaper);
                 File parent = temp.getParentFile();
                 if(!parent.exists()){
                     parent.mkdirs();
                     
                     ThemeUtil.changeFilePermission(parent);
                 }
                 
                 fos = new FileOutputStream(temp);
                 
                 ThemeUtil.writeSourceToTarget(is, fos);
                 
                 File target = new File("/data/system/face/wallpaper/lock_screen_wallpaper.jpg");
                 temp.renameTo(target);
                 
                 ThemeUtil.changeFilePermission(target);
                 
                 Log.i(TAG, "set lockscreenwalpaper success");
                 ThemeUtil.showToast(mContext, R.string.theme_set_success, true);
                 
             }else {
            	 ThemeUtil.showToast(mContext, R.string.theme_set_fail, true);
			}
             
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             ThemeUtil.showToast(mContext, R.string.theme_set_fail, true);
         }finally{
             try {
                 
                 if(fos != null){
                     fos.close();
                     fos = null;
                 }
                 
             } catch (Exception e2) {
                 e2.printStackTrace();
             }
         }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.import_from_apps:
        {
            if(ThemeUtil.isWVGA){
                ThemeUtil.cropImageFromGallery(this, arb, 3, 5, 480, 800, SELECT_PICTRUE);
            }else {
                ThemeUtil.cropImageFromGallery(this, arb, 2, 3, 320, 480, SELECT_PICTRUE); 
            }
            break;
        }
        
        }
    }
    
}
