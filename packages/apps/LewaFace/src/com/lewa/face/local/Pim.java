package com.lewa.face.local;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Intent;
import android.view.View;

import com.lewa.face.R;
import com.lewa.face.adapters.local.LocalPimAdapter;
import com.lewa.face.adapters.local.ThumbnailLocalAdapter;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.pojos.ThemeModelInfo;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeFileFilter;
import com.lewa.face.util.ThemeFileSort;
import com.lewa.face.util.ThemeHelper;
import com.lewa.face.util.ThemeUtil;
import com.lewa.os.ui.ActivityResultBridge.ActivityResultReceiver;

/**
 * 
 * @author fulw
 *
 */
public class Pim extends LocalBaseActivity{
    
    @Override
    protected void initViews() {
        findViewById(R.id.customize).setVisibility(View.GONE);
        findViewById(R.id.import_from_apps).setVisibility(View.GONE);
    }

    
    @Override
    protected ArrayList<ThemeBase> getThemeBases() {
        // TODO Auto-generated method stub
		ArrayList<ThemeBase> bases = ThemeHelper.getThemeBases(mContext,
				ThemeConstants.THEME_LOCAL, ThemeConstants.PIM);
		return addDefaultTheme(bases);
    }



    @Override
    protected ThumbnailLocalAdapter localAdapterInstance() {
        // TODO Auto-generated method stub
        return new LocalPimAdapter(mContext,themeBases,ThemeConstants.THEME_LOCAL,handler);
//    	return null;
    }

    @Override
    protected void parseLocalModelInfo(){
        File bootsRoot = new File(ThemeConstants.THEME_MODEL_PIM_STYLE);
        File[] localBoots = bootsRoot.listFiles(new ThemeFileFilter(ThemeConstants.THEME_MODEL_PIM,ThemeFileFilter.STARTSWITH));
        if((localBoots != null) && (localBoots.length != 0)){
            
            Arrays.sort(localBoots, new ThemeFileSort());
            
            
            ArrayList<ThemeBase> themePkgInfos = ThemeHelper.getThemeBases(mContext, ThemeConstants.THEME_LOCAL, ThemeConstants.PIM);
            
            int length = localBoots.length;
            for(int i=0;i<length;i++){
                File bootsFile = localBoots[i];
                String bootsName = bootsFile.getName();
                
                String themePkg = new StringBuilder().append(bootsName.substring(bootsName.lastIndexOf("_") + 1)).append(ThemeConstants.BUFFIX_LWT).toString();
                
                /**
                 * 如果是默认主题资源，则返�?
                 */
                if(themePkg.equals(ThemeConstants.DEFAULT_THEME_PKG)){
                    continue;
                }
                
                /**
                 * 如果模块存在，但是原始lwt文件不存在的话，则视为无效模块，因为主题信息在lwt文件�?
                 */
                String lwtPath = new StringBuilder().append(ThemeConstants.THEME_LWT).append("/").append(themePkg).toString();
                if(!new File(lwtPath).exists()){
                    continue;
                }
                
                ThemeModelInfo themeModelInfo = new ThemeModelInfo(themePkg);
                ThemeBase boots = new ThemeBase(themeModelInfo,themePkg,null,true);
                
                for(ThemeBase themePkgInfo : themePkgInfos){
                    if(themePkg.equals(themePkgInfo.getPkg())){
                        boots.setCnAuthor(themePkgInfo.getCnAuthor());
                        boots.setEnAuthor(themePkgInfo.getEnAuthor());
                        boots.setCnName(themePkgInfo.getCnName());
                        boots.setEnName(themePkgInfo.getEnName());
                        break;
                    }
                }
               
                boots.setPkg(themePkg);
                boots.setSize(ThemeUtil.fileLengthToSize(bootsFile.length()));
                /**
                 * 临时FC解决办法
                 */
                if(themeBases.size() == 0){
                    themeBases.add(boots);
                }else{
                    themeBases.add(ThemeConstants.SECOND,boots);
                }
            }
            ThemeHelper.saveThemeBases(mContext, themeBases, ThemeConstants.THEME_LOCAL, ThemeConstants.PIM);
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
