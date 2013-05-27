package com.lewa.face.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.app.Application;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.lewa.face.R;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.pojos.ThemeModelInfo;
import com.lewa.face.util.FileMD5;
import com.lewa.face.util.SharedPreferenceUtil;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeHelper;
import com.lewa.face.util.ThemeUtil;


/**
 * Application是一个全局且单例对象，是整个应用程序中生命周期最长的，并且是整个应用程序的入口
 * @author fulw
 *
 */
public class ThemeApplication extends Application{
	public static final String modulesOnApplied = "modulesOnApplied";
	public static final String modulesOnAppliedKey = "modulesOnAppliedKey";
    private static final String TAG = ThemeApplication.class.getSimpleName();
    
    public static ArrayList<Activity> activities = new ArrayList<Activity>();
    public static Drawable defaultBitmap;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
    	Object modules =  SharedPreferenceUtil.getValue(this, modulesOnApplied, modulesOnAppliedKey, String.class);
    	if(modules!=null){
    		ThemeUtil.modulesOnApplied.addAll(Arrays.asList(modules.toString().split(",")));    		
    	}
        /**
         * 存储卡存在才做以下操作
         */
        if(ThemeUtil.isSDCardEnable()){
                        
            ThemeUtil.createDirs();
            
            /**
             * 为默认主题而做的操作
             */
            File defaultTheme = new File("/system/media/default.lwt");
            File defaultTarget = new File(new StringBuilder().append(ThemeConstants.THEME_LWT).append("/").append(ThemeConstants.DEFAULT_THEME_PKG).toString());
            
            /**
             * 如果默认主题包存在，并且SD卡中没有则进行一系列的操作
             */
            
            ArrayList<ThemeBase> themeBases = ThemeHelper.getThemeBases(getBaseContext(), ThemeConstants.THEME_LOCAL, ThemeConstants.THEMEPKG);
            
            if(defaultTheme.exists()){
                boolean different = false;
            	try {
            	    
            	    if(!defaultTarget.exists()){
            	        Log.d(TAG, "copy default.lwt file to sd for no default.lwt");
            	        /**
                         * 复制主题包
                         */
                        FileUtils.copyFile(defaultTheme, defaultTarget);
                        /**
                         * 解压缩主题包
                         */
                        ThemeUtil.unThemeZIP(defaultTarget);
            	    }else {
            	        String source_theme_md5 = FileMD5.getFileMD5String(defaultTheme);
                        
                        String target_theme_md5 = FileMD5.getFileMD5String(defaultTarget);
                        
                        /**
                         * SD卡中存在，但版本号不同，则说明默认主题包有更新
                         */
                        if(!source_theme_md5.equals(target_theme_md5)){
                            Log.d(TAG, "copy default.lwt file to sd for md5 is not match");
                            List<ThemeBase> bases = ThemeHelper.getThemeBases(getBaseContext(), ThemeConstants.THEME_LOCAL, ThemeConstants.THEMEPKG);
                            if(bases.size()>0)
                            ThemeUtil.deleteThemeInfo(getBaseContext(), bases.get(0));
                            /**
                             * 复制主题包
                             */
                            FileUtils.copyFile(defaultTheme, defaultTarget);
                            /**
                             * 解压缩主题包
                             */
                            ThemeUtil.unThemeZIP(defaultTarget);
                            different = true;
                        }
                    }
            	    
	                String themePkg = defaultTheme.getName();
	                
	                /**
	                 * 1、如果默认主题信息没有保存，则保存
	                 * 2、因为默认主题包理论上是必须有的，所以只要themeBases的size只要为0，则说明变脸的数据很有可能被清除过
	                 */
	                if(themeBases.size() == 0 || !themeBases.get(ThemeUtil.DEFAULT).getPkg().equals(themePkg)){
	                    Log.d(TAG, "add default theme info,and the data/data/ may be delete");
	                    
	                    Log.d(TAG, "save default theme info");
	                    /**
	                     * 保存主题信息
	                     */
	                    ThemeUtil.addThemeInfo(getBaseContext(), themePkg, ThemeUtil.DEFAULT,true);
	                }else if(different){
	                	ArrayList<ThemeBase> bases = ThemeHelper.getThemeBases(getBaseContext(), ThemeConstants.THEME_LOCAL, ThemeConstants.THEMEPKG);
	                	
	                	ThemeModelInfo themeModelInfo = new ThemeModelInfo(defaultTarget.getName());
	                	ThemeBase launcherStyle = new ThemeBase(themeModelInfo, themePkg,
	                			null, true);
	                	
	                	launcherStyle.setCnAuthor(launcherStyle.getCnAuthor());
	                	launcherStyle.setEnAuthor(launcherStyle.getEnAuthor());
	                	launcherStyle.setCnName(launcherStyle.getCnName());
	                	launcherStyle.setEnName(launcherStyle.getEnName());
	                	launcherStyle.setPkg(themePkg);
	                	launcherStyle.setSize(ThemeUtil.fileLengthToSize(defaultTarget
	                			.length()));
	                	bases.add(ThemeConstants.DEFAULT,launcherStyle);
	                	ThemeHelper.saveThemeBases(getBaseContext(), bases, ThemeHelper.THEME_LOCAL, ThemeHelper.THEMEPKG);
	                }
	                
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
            }else{
                Log.e(TAG, "add default theme info,and the data/data/ may be delete");
            	/**
            	 * 因为默认主题包理论上是必须有的，所以只要themeBases的size只要为0，则说明变脸的数据很有可能被清除过
            	 */
            	if(themeBases.size() == 0){
            	    ThemeUtil.addThemeInfo(getBaseContext(), ThemeConstants.DEFAULT_THEME_PKG, ThemeUtil.DEFAULT,true);
            	}
            	
            }
        }
        super.onCreate();
        defaultBitmap = getResources().getDrawable(R.drawable.lewa);
    }

    
    
    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        Log.e(TAG, "onLowMemory and runGC()");
        ThemeUtil.runGC();
        
        super.onLowMemory();
        
    }

}
