package com.lewa.face.local;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.lewa.face.R;
import com.lewa.face.adapters.local.LocalLauncherStyleAdapter;
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

/**
 * 
 * @author fulw
 *
 */
public class LauncherStyle extends LocalBaseActivity{
    
    private static final String TAG = LauncherStyle.class.getSimpleName();
    
    @Override
    protected void initViews() {
        findViewById(R.id.customize).setVisibility(View.GONE);
        findViewById(R.id.import_from_apps).setVisibility(View.GONE);
    }

    
    @Override
    protected ArrayList<ThemeBase> getThemeBases() {
        // TODO Auto-generated method stub
        ArrayList<ThemeBase> bases = ThemeHelper.getThemeBases(mContext,ThemeConstants.THEME_LOCAL,ThemeConstants.LAUNCHER);
        if(bases.contains(new ThemeBase(getString(R.string.default_word)))){
        	return bases;        	
        }
        else{
        	String defaultlwt = ThemeConstants.THEME_LWT+"/"+getString(R.string.default_word)+".lwt";
        	File fDefaultLwt = new File(defaultlwt);
        	String themePkg = new StringBuilder().append(fDefaultLwt.getName().substring(fDefaultLwt.getName().lastIndexOf("_") + 1)).append(ThemeConstants.BUFFIX_LWT).toString();
                
            ThemeModelInfo themeModelInfo = new ThemeModelInfo(themePkg);
            ThemeBase launcherStyle = new ThemeBase(themeModelInfo,themePkg,null,true);
            
                    launcherStyle.setCnAuthor(launcherStyle.getCnAuthor());
                    launcherStyle.setEnAuthor(launcherStyle.getEnAuthor());
                    launcherStyle.setCnName(launcherStyle.getCnName());
                    launcherStyle.setEnName(launcherStyle.getEnName());
            launcherStyle.setPkg(themePkg);
            launcherStyle.setSize(ThemeUtil.fileLengthToSize(fDefaultLwt.length()));
        	bases.add(launcherStyle);
        	return bases;
        }
    }



    @Override
    protected ThumbnailLocalAdapter localAdapterInstance() {
        // TODO Auto-generated method stub
        return new LocalLauncherStyleAdapter(mContext,themeBases,ThemeConstants.THEME_LOCAL,handler);
    }

    @Override
    protected void parseLocalModelInfo(){
        File launcherRoot = new File(ThemeConstants.THEME_MODEL_LAUNCHER_STYLE);
        File[] localLauncher = launcherRoot.listFiles(new ThemeFileFilter(ThemeConstants.THEME_MODEL_LAUNCHER,ThemeFileFilter.STARTSWITH));
        if((localLauncher != null) && (localLauncher.length != 0)){
            
            Arrays.sort(localLauncher, new ThemeFileSort());
            
            ArrayList<ThemeBase> themePkgs = ThemeHelper.getThemeBases(mContext, ThemeConstants.THEME_LOCAL, ThemeConstants.THEMEPKG);
            
            int length = localLauncher.length;
            for(int i=0;i<length;i++){
                File launcher = localLauncher[i];
                String launcherName = launcher.getName();
                
                String themePkg = new StringBuilder().append(launcherName.substring(launcherName.lastIndexOf("_") + 1)).append(ThemeConstants.BUFFIX_LWT).toString();
                
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
                ThemeBase launcherStyle = new ThemeBase(themeModelInfo,themePkg,null,true);
                
                for(ThemeBase themePkgInfo : themePkgs){
                    if(themePkg.equals(themePkgInfo.getPkg())){
                        launcherStyle.setCnAuthor(themePkgInfo.getCnAuthor());
                        launcherStyle.setEnAuthor(themePkgInfo.getEnAuthor());
                        launcherStyle.setCnName(themePkgInfo.getCnName());
                        launcherStyle.setEnName(themePkgInfo.getEnName());
                        break;
                    }
                }
               
                launcherStyle.setPkg(themePkg);
                launcherStyle.setSize(ThemeUtil.fileLengthToSize(launcher.length()));
                if(themeBases.size() == 0){
                    themeBases.add(launcherStyle);
                }else{
                    themeBases.add(ThemeConstants.SECOND,launcherStyle);
                }
                
            }
            ThemeHelper.saveThemeBases(mContext, themeBases, ThemeConstants.THEME_LOCAL, ThemeConstants.LAUNCHER);
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
