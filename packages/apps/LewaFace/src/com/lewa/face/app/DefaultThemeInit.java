package com.lewa.face.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import com.lewa.face.R;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeHelper;
import com.lewa.face.util.ThemeUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * 此类暂时留着，可能后面会用到
 * 
 * 当用户第一次使用变脸或者乐蛙默认主题有更新时，都要对默认主题包
 * 进行拷贝和默认主题信息的更新
 * @author fulw
 *
 */
public class DefaultThemeInit extends AsyncTask<String, String, String>{
    
    private static final String TAG = DefaultThemeInit.class.getSimpleName();
    
    private Context context;
    private File source;
    private File target;
    
    private ProgressDialog progressDialog;
    
    public DefaultThemeInit(Context context,File source,File target){
        this.context = context;
        this.source = source;
        this.target = target;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(R.string.setting_defalult_theme_title);
        progressDialog.setMessage(context.getString(R.string.setting_defalult_theme_msg));
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        
        copyDefaultTheme(context,source,target);
        
        return null;
    }
    
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        
        progressDialog.dismiss();
        progressDialog = null;
        
    }
    
    
    /**
     * 拷贝默认主题包到SD卡中
     * @param src
     * @param target
     * @param themeBases
     */
    private void copyDefaultTheme(Context context, File src, File target){
        try {
            
            Log.i(TAG, "copy default.lwt file to sd");
            
            /**
             * 复制主题包
             */
            FileUtils.copyFile(src, target);
            
            /**
             * 解压缩主题包
             */
            ThemeUtil.unThemeZIP(target);
            
            String themePkg = src.getName();
            
            updateLocalThemeInfo();
            
            ThemeUtil.addThemeInfo(context, themePkg, ThemeUtil.DEFAULT,true);
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * 更新默认主题信息
     */
    private void updateLocalThemeInfo(){
        ArrayList<ThemeBase> themeBases = ThemeHelper.getThemeBases(context, ThemeConstants.THEME_LOCAL, ThemeConstants.THEMEPKG);
        
        ArrayList<ThemeBase> wallPapers = ThemeHelper.getThemeBases(context, ThemeConstants.THEME_LOCAL, ThemeConstants.WALLPAPER);
        
        ArrayList<ThemeBase> lockScreenWallPapers = ThemeHelper.getThemeBases(context, ThemeConstants.THEME_LOCAL, ThemeConstants.LSWALLPAPER);
        
        ArrayList<ThemeBase> lockScreenStyles = ThemeHelper.getThemeBases(context, ThemeConstants.THEME_LOCAL, ThemeConstants.LOCKSCREEN);
        
        ArrayList<ThemeBase> iconsStyles = ThemeHelper.getThemeBases(context, ThemeConstants.THEME_LOCAL, ThemeConstants.ICONS);
        
        ArrayList<ThemeBase> launcherStyles = ThemeHelper.getThemeBases(context, ThemeConstants.THEME_LOCAL, ThemeConstants.LAUNCHER);
        
        ArrayList<ThemeBase> bootsStyles = ThemeHelper.getThemeBases(context, ThemeConstants.THEME_LOCAL, ThemeConstants.BOOTS);
        
        ArrayList<ThemeBase> fontsStyles = ThemeHelper.getThemeBases(context, ThemeConstants.THEME_LOCAL, ThemeConstants.FONTS);
        
        if(themeBases.size() != 0){
            themeBases.remove(ThemeUtil.DEFAULT);
            
            ThemeHelper.saveThemeBases(context, themeBases, ThemeConstants.THEME_LOCAL, ThemeConstants.THEMEPKG);
        }
        if(wallPapers.size() != 0){
            wallPapers.remove(ThemeUtil.DEFAULT);
            
            ThemeHelper.saveThemeBases(context, wallPapers, ThemeConstants.THEME_LOCAL, ThemeConstants.WALLPAPER);
        }
        if(lockScreenWallPapers.size() != 0){
            lockScreenWallPapers.remove(ThemeUtil.DEFAULT);
            
            ThemeHelper.saveThemeBases(context, lockScreenWallPapers, ThemeConstants.THEME_LOCAL, ThemeConstants.LSWALLPAPER);
        }
        if(lockScreenStyles.size() != 0){
            lockScreenStyles.remove(ThemeUtil.DEFAULT);
            
            ThemeHelper.saveThemeBases(context, lockScreenStyles, ThemeConstants.THEME_LOCAL, ThemeConstants.LOCKSCREEN);
        }
        if(iconsStyles.size() != 0){
            iconsStyles.remove(ThemeUtil.DEFAULT);
            
            ThemeHelper.saveThemeBases(context, iconsStyles, ThemeConstants.THEME_LOCAL, ThemeConstants.ICONS);
        }
        if(launcherStyles.size() != 0){
            launcherStyles.remove(ThemeUtil.DEFAULT);
            
            ThemeHelper.saveThemeBases(context, launcherStyles, ThemeConstants.THEME_LOCAL, ThemeConstants.LAUNCHER);
        }
        if(bootsStyles.size() != 0){
            bootsStyles.remove(ThemeUtil.DEFAULT);
            
            ThemeHelper.saveThemeBases(context, bootsStyles, ThemeConstants.THEME_LOCAL, ThemeConstants.BOOTS);
        }
        if(fontsStyles.size() != 0){
            fontsStyles.remove(ThemeUtil.DEFAULT);
            
            ThemeHelper.saveThemeBases(context, fontsStyles, ThemeConstants.THEME_LOCAL, ThemeConstants.FONTS);
        }
    }

}
