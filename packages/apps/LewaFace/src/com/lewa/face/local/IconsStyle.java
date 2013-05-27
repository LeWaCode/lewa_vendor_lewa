package com.lewa.face.local;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.lewa.face.R;
import com.lewa.face.adapters.local.LocalIconsStyleAdapter;
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
public class IconsStyle extends LocalBaseActivity{
    
    private static final String TAG = IconsStyle.class.getSimpleName();
   
    @Override
    protected void initViews() {
        findViewById(R.id.customize).setVisibility(View.GONE);
        findViewById(R.id.import_from_apps).setVisibility(View.GONE);
    }

    
    @Override
    protected ArrayList<ThemeBase> getThemeBases() {
        // TODO Auto-generated method stub
        return ThemeHelper.getThemeBases(mContext,ThemeConstants.THEME_LOCAL,ThemeConstants.ICONS);
    }



    @Override
    protected ThumbnailLocalAdapter localAdapterInstance() {
        // TODO Auto-generated method stub
        return new LocalIconsStyleAdapter(mContext,themeBases,ThemeConstants.THEME_LOCAL,handler);
    }

    @Override
    protected void parseLocalModelInfo(){
        File iconsRoot = new File(ThemeConstants.THEME_MODEL_ICONS_STYLE);
        File[] localIcons = iconsRoot.listFiles(new ThemeFileFilter(ThemeConstants.THEME_MODEL_ICONS,ThemeFileFilter.STARTSWITH));
        if((localIcons != null) && (localIcons.length != 0)){
            
            Arrays.sort(localIcons, new ThemeFileSort());
            
            
            ArrayList<ThemeBase> themePkgInfos = ThemeHelper.getThemeBases(mContext, ThemeConstants.THEME_LOCAL, ThemeConstants.ICONS);
            
            int length = localIcons.length;
            for(int i=0;i<length;i++){
                File icon = localIcons[i];
                String iconName = icon.getName();
                
                String themePkg = new StringBuilder().append(iconName.substring(iconName.lastIndexOf("_") + 1)).append(ThemeConstants.BUFFIX_LWT).toString();
                
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
                ThemeBase iconsStyle = new ThemeBase(themeModelInfo,themePkg,null,true);
                
                for(ThemeBase themePkgInfo : themePkgInfos){
                    if(themePkg.equals(themePkgInfo.getPkg())){
                        iconsStyle.setCnAuthor(themePkgInfo.getCnAuthor());
                        iconsStyle.setEnAuthor(themePkgInfo.getEnAuthor());
                        iconsStyle.setCnName(themePkgInfo.getCnName());
                        iconsStyle.setEnName(themePkgInfo.getEnName());
                        break;
                    }
                }
               
                iconsStyle.setPkg(themePkg);
                iconsStyle.setSize(ThemeUtil.fileLengthToSize(icon.length()));
                 /**
                 * 如果是默认主题资源，则返回
                 */
                 
                
                if(themeBases.size() == 0){
                    themeBases.add(iconsStyle);
                }else{
                    themeBases.add(ThemeConstants.SECOND,iconsStyle);
                }

            }
            ThemeHelper.saveThemeBases(mContext, themeBases, ThemeConstants.THEME_LOCAL, ThemeConstants.ICONS);
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
