package com.lewa.face.local;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


import com.lewa.face.R;
import com.lewa.face.adapters.local.LocalLockScreenStyleAdapter;
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
public class LockScreenStyle extends LocalBaseActivity{
    
    private static final String TAG = LockScreenStyle.class.getSimpleName();
    
    @Override
    protected void initViews() {
        findViewById(R.id.customize).setVisibility(View.GONE);
        findViewById(R.id.import_from_apps).setVisibility(View.GONE);
    }

    
    @Override
    protected ArrayList<ThemeBase> getThemeBases() {
        // TODO Auto-generated method stub
        return ThemeHelper.getThemeBases(mContext,ThemeConstants.THEME_LOCAL,ThemeConstants.LOCKSCREEN);
    }



    @Override
    protected ThumbnailLocalAdapter localAdapterInstance() {
        // TODO Auto-generated method stub
        return new LocalLockScreenStyleAdapter(mContext,themeBases,ThemeConstants.THEME_LOCAL,handler);
    }
   
    @Override
    protected void parseLocalModelInfo(){
        File lockscreenRoot = new File(ThemeConstants.THEME_MODEL_LOCKSCREEN_STYLE);
        File[] localLockScreens = lockscreenRoot.listFiles(new ThemeFileFilter(ThemeConstants.THEME_MODEL_LOCKSCREEN,ThemeFileFilter.STARTSWITH));
        if((localLockScreens != null) && (localLockScreens.length != 0)){
            
            Arrays.sort(localLockScreens, new ThemeFileSort());
            
            ArrayList<ThemeBase> themePkgs = ThemeHelper.getThemeBases(mContext, ThemeConstants.THEME_LOCAL, ThemeConstants.THEMEPKG);
            
            int length = localLockScreens.length;
            for(int i=0;i<length;i++){
                File lockscreen = localLockScreens[i];
                String lockscreenName = lockscreen.getName();
                
                String themePkg = new StringBuilder().append(lockscreenName.substring(lockscreenName.lastIndexOf("_") + 1)).append(ThemeConstants.BUFFIX_LWT).toString();
                
                
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
                ThemeBase lockscreenStyle = new ThemeBase(themeModelInfo,themePkg,null,true);
                /**
                 * the flag is record the theme contain lockscreen? if no,the lockscreen it may be 
                 * .....
                 */
                for(ThemeBase themeBase : themePkgs){
                    if(themePkg.equals(themeBase.getPkg())){
                        lockscreenStyle.setCnAuthor(themeBase.getCnAuthor());
                        lockscreenStyle.setEnAuthor(themeBase.getEnAuthor());
                        lockscreenStyle.setCnName(themeBase.getCnName());
                        lockscreenStyle.setEnName(themeBase.getEnName());
                        break;
                    }
                }
                lockscreenStyle.setPkg(themePkg);
                lockscreenStyle.setSize(ThemeUtil.fileLengthToSize(lockscreen.length()));
                if(themeBases.size() == 0){
                    themeBases.add(lockscreenStyle);
                }else{
                    themeBases.add(ThemeConstants.SECOND,lockscreenStyle);
                }
            }
            ThemeHelper.saveThemeBases(mContext, themeBases, ThemeConstants.THEME_LOCAL, ThemeConstants.LOCKSCREEN);
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
