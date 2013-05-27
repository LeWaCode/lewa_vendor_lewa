package com.lewa.face.local;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import com.lewa.face.R;
import com.lewa.face.adapters.local.LocalWallPaperAdapter;
import com.lewa.face.adapters.local.ThumbnailLocalAdapter;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.pojos.ThemeModelInfo;
import com.lewa.face.util.ThemeFileFilter;
import com.lewa.face.util.ThemeFileSort;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeHelper;
import com.lewa.face.util.ThemeUtil;
import com.lewa.os.ui.ActivityResultBridge.ActivityResultReceiver;

import android.app.WallpaperManager;
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
public class WallPaper extends LocalBaseActivity{
    
    private static final String TAG = WallPaper.class.getSimpleName();
    
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
        return ThemeHelper.getThemeBases(mContext,ThemeConstants.THEME_LOCAL,ThemeConstants.WALLPAPER);
    }



    @Override
    protected ThumbnailLocalAdapter localAdapterInstance() {
        // TODO Auto-generated method stub
        return new LocalWallPaperAdapter(mContext,themeBases,ThemeConstants.THEME_LOCAL,handler);
    }
   
    @Override
    protected void parseLocalModelInfo(){

        File lwtRoot = new File(ThemeConstants.THEME_WALLPAPER);
        File[] localWallpapers = lwtRoot.listFiles(new ThemeFileFilter(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX,ThemeFileFilter.STARTSWITH));
        
        /**
         * 列出在线下载的桌面壁纸或者锁屏壁纸
         */
        File[] onlines = lwtRoot.listFiles(new ThemeFileFilter(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX, ThemeFileFilter.NOT));
        
        File[] totalFiles = ThemeUtil.contact(localWallpapers, onlines);
             
        if((totalFiles != null) && (totalFiles.length != 0)){
            
            Arrays.sort(totalFiles, new ThemeFileSort());
            
            ArrayList<ThemeBase> themePkgs = ThemeHelper.getThemeBases(mContext, ThemeConstants.THEME_LOCAL, ThemeConstants.THEMEPKG);
            
            int length = totalFiles.length;
            for(int i=0;i<length;i++){
                File wallPaper = totalFiles[i];
                String wallPaperName = wallPaper.getName();
                
                ThemeBase wallPaperInfo = null;
                
                String themePkg = null;
                int position = wallPaperName.lastIndexOf("_");
                if(position != -1){//主题包中的桌面壁纸或者锁屏壁纸
                    themePkg = new StringBuilder().append(wallPaperName.substring(wallPaperName.lastIndexOf("_") + 1)).append(ThemeConstants.BUFFIX_LWT).toString();
                    
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
                    wallPaperInfo = new ThemeBase(themeModelInfo,themePkg,null,true);
                    
                    for(ThemeBase themeInfo : themePkgs){
                        if(themePkg.equals(themeInfo.getPkg())){
                            wallPaperInfo.setCnAuthor(themeInfo.getCnAuthor());
                            wallPaperInfo.setEnAuthor(themeInfo.getEnAuthor());
                            wallPaperInfo.setCnName(themeInfo.getCnName());
                            wallPaperInfo.setEnName(themeInfo.getEnName());
                            break;
                        }
                    }
                    
                }else {//在线下载的桌面壁纸或者锁屏壁纸
                    themePkg = wallPaperName;
                    
                    wallPaperInfo = new ThemeBase();
                    
                    wallPaperInfo.setPkg(themePkg);
                    wallPaperInfo.setCnAuthor("N/A");
                    wallPaperInfo.setEnAuthor("N/A");
                    wallPaperInfo.setCnName(themePkg);
                    wallPaperInfo.setEnName("N/A");
                }
               
                
                wallPaperInfo.setSize(ThemeUtil.fileLengthToSize(wallPaper.length()));
                if(themeBases.size() == 0){
                    themeBases.add(wallPaperInfo);
                }else{
                    themeBases.add(ThemeConstants.SECOND,wallPaperInfo);
                }
                               
            }
            ThemeHelper.saveThemeBases(mContext,themeBases,ThemeConstants.THEME_LOCAL,ThemeConstants.WALLPAPER);
        }
    }
    
    /**
     * 相当于普通Activity的onActivityResult
     */
    @Override
    public void handleActivityResult(ActivityResultReceiver resultReceiver, int requestCode,
            int resultCode, Intent data) {
        
        if(data == null){
            return;
        }
        
        if (resultCode == RESULT_OK) {
          
            Uri uri = data.getData();
            InputStream is = null;
            FileOutputStream fos = null;
            try { 
                ContentResolver cr = this.getContentResolver();
                
                WallpaperManager wm = (WallpaperManager) mContext.getSystemService(Context.WALLPAPER_SERVICE);
                
                is = cr.openInputStream(uri);
                wm.setStream(is);
                
                File temp = createTempFile();
                fos = new FileOutputStream(temp);
                
                is = cr.openInputStream(uri);
                
                createFile(temp, is, fos);
                
                Log.i(TAG, "set walpaper success");
                ThemeUtil.showToast(mContext, R.string.theme_set_success, true);
            } catch (Exception e) {
                ThemeUtil.showToast(mContext, R.string.theme_set_fail, true);
                e.printStackTrace();
            }finally{
                try {
                    if(uri != null){
                        File file = new File(new URI(uri.toString()));
                        if(file.exists()){
                            file.delete();
                        }
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
                SharedPreferences.Editor editor = mContext.getSharedPreferences("CURRENT_USING", Context.MODE_PRIVATE).edit();
                editor.putString("wallpaper", null).commit();
            }  
        }  
    }
    
    /**
     * 创建桌面壁纸临时文件
     * @return
     */
    private File createTempFile(){
        File temp = new File(ThemeConstants.THEME_FACE_WALLPAPER_TEMP);
        File parent = temp.getParentFile();
        if(!parent.exists()){
            parent.mkdirs();
            
            ThemeUtil.changeFilePermission(parent);
        }
        return temp;
    }
    
    /**
     * 桌面壁纸文件的生成
     * @param temp
     * @param is
     * @param fos
     */
    private void createFile(File temp,InputStream is,FileOutputStream fos){
        
        ThemeUtil.writeSourceToTarget(is, fos);
        
        File target = new File(ThemeConstants.THEME_FACE_WALLPAPER);
        temp.renameTo(target);
        ThemeUtil.changeFilePermission(target);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.import_from_apps:
        {
            if(ThemeUtil.isWVGA){
                ThemeUtil.cropImageFromGallery(this, arb, 6, 5, 960, 800, SELECT_PICTRUE);
            }else {
                ThemeUtil.cropImageFromGallery(this, arb, 6, 5, 576, 480, SELECT_PICTRUE); 
            }
            break;
        }
        
        }
    }
    
}
