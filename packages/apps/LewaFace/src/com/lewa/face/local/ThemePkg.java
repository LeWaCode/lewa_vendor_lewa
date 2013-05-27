package com.lewa.face.local;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.lewa.face.R;
import com.lewa.face.adapters.local.LocalThemePkgAdapter;
import com.lewa.face.adapters.local.ThumbnailLocalAdapter;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.pojos.ThemeModelInfo;
import com.lewa.face.util.ThemeFileSort;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeFileFilter;
import com.lewa.face.util.ThemeHelper;
import com.lewa.face.util.ThemeUtil;
import com.lewa.os.ui.ActivityResultBridge.ActivityResultReceiver;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

/**
 * 
 * @author fulw
 *
 */
public class ThemePkg extends LocalBaseActivity{
    
    private static final String TAG = ThemePkg.class.getSimpleName();
    
    @Override
    protected void initViews() {
    	 Button customize = (Button) findViewById(R.id.customize);
        if(customize != null){
            customize.setOnClickListener(this);
        }
        Button importButton  = (Button) findViewById(R.id.import_from_apps);
        if(importButton != null){
            importButton.setVisibility(View.GONE);
        }
    }

    
    @Override
    protected ArrayList<ThemeBase> getThemeBases() {
        // TODO Auto-generated method stub
        return ThemeHelper.getThemeBases(mContext,ThemeConstants.THEME_LOCAL,ThemeConstants.THEMEPKG);
    }



    @Override
    protected ThumbnailLocalAdapter localAdapterInstance() {
        // TODO Auto-generated method stub
        return new LocalThemePkgAdapter(mContext,themeBases,ThemeConstants.THEME_LOCAL,handler);
    }
   
    @Override
    protected void parseLocalModelInfo(){
   
        File lwtRoot = new File(ThemeConstants.THEME_LWT);
        File[] localThemes = lwtRoot.listFiles(new ThemeFileFilter(ThemeConstants.BUFFIX_LWT,ThemeFileFilter.ENDSWITH));
        
        if((localThemes != null) && (localThemes.length != 0)){
        	Arrays.sort(localThemes, new ThemeFileSort());
        	
            int length = localThemes.length;
            for(int i=0;i<length;i++){
                File lwt = localThemes[i];
                String themePkg = lwt.getName();
                /**
                 * 如果是默认主题资源，则返回
                 */
                if(themePkg.equals(ThemeConstants.DEFAULT_THEME_PKG)){
                    continue;
                }
                String nameNoLwt = ThemeUtil.getNameNoBuffix(themePkg);
                String thumbnailPath = new StringBuilder().append(ThemeConstants.THEME_LOCAL_THUMBNAIL)
                	.append("/").append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_PREFIX)
                	.append(nameNoLwt).toString();
                File thumbnail = new File(thumbnailPath);
                
                ZipFile zip = null;
                InputStream source = null;
                FileOutputStream target = null;
                FileInputStream fis = null;
                try {
                    
                    zip = new ZipFile(lwt);
                    
                    /**
                     * 如果是锁屏、字体、开机动画主题包，则不在本地主题中罗列出来
                     */
                    if(zip.getEntry(ThemeConstants.THEME_MODEL_ICONS) == null &&
                            (zip.getEntry(ThemeConstants.THEME_MODEL_LOCKSCREEN) != null
                             || zip.getEntry(ThemeConstants.THEME_MODEL_BOOTS) != null
                             || zip.getEntry(ThemeConstants.THEME_MODEL_FONTS) != null)){
                        continue;
                    }
                    
                    if(!thumbnail.exists()){
                	
                    	ZipEntry entry = zip.getEntry(ThemeConstants.THEME_PREVIEW_LOCKSCREEN_0);
                    	if(entry != null){
                    		source = zip.getInputStream(entry);
                    	
                    		String targetFilePath = new StringBuilder().append(ThemeConstants.THEME_LOCAL_PREVIEW)
                    			.append("/").append(nameNoLwt).append("/preview_lockscreen_0.jpg").toString();
                    		File targetFile = new File(targetFilePath);
                    		if(!targetFile.getParentFile().exists()){
                    			targetFile.getParentFile().mkdirs();
                    		}
                    		target = new FileOutputStream(targetFile);
                    		ThemeUtil.writeSourceToTarget(source, target);
                    		
                    		fis = new FileInputStream(targetFile);
                    		ThemeUtil.createThumbnailForBuffix(entry.getName(),ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_PREFIX,nameNoLwt,fis);
                    	}
					
                	
                }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    try {
                        if(target != null){
                            target.close();
                            target = null;
                        }
                        if(source != null){
                            source.close();
                            source = null;
                        }
                        if(fis != null){
                            fis.close();
                            fis = null;
                        }
                        if(zip != null){
                            zip.close();
                            zip = null;
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                
                ThemeModelInfo themeModelInfo = new ThemeModelInfo(themePkg);
                ThemeBase themePkgInfo = new ThemeBase(themeModelInfo,themePkg,null,true);
                
                String pkgPath = new StringBuilder().append(ThemeConstants.THEME_LWT)
                .append("/").append(themePkg)
                .toString();
            
                File pkgFile = new File(pkgPath);
                if(pkgFile.exists()){
                    themePkgInfo.setSize(ThemeUtil.fileLengthToSize(pkgFile.length()));
                }
                /**
                 * 临时FC解决办法
                 */
                if(themeBases.size() == 0){
                    themeBases.add(themePkgInfo);
                }else{
                    themeBases.add(ThemeConstants.SECOND,themePkgInfo);
                }
                
                
            }
            ThemeHelper.saveThemeBases(mContext, themeBases, ThemeConstants.THEME_LOCAL, ThemeConstants.THEMEPKG);
        }
    }
    
    /**
     * 相当于普通Activity的onActivityResult
     */
    @Override
    public void handleActivityResult(ActivityResultReceiver resultReceiver, int requestCode,
            int resultCode, Intent data) {
        
    }
}
