package com.lewa.face.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

import com.lewa.face.R;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.pojos.ThemeModelInfo;


import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.app.Activity;

public class ThemeCopyTask extends AsyncTask<String, Integer, String>{
    
    private static final String TAG = ThemeCopyTask.class.getSimpleName();
    
    private ThemeBase mThemeBase = null;
    private Context mContext = null;
    
    /**
     * 如果该主题中包含字体，则标记可能要重启
     */
    private boolean shouldReboot = false;
    
    private ProgressDialog mProgressDialog = null;
    
    public ThemeCopyTask(ThemeBase themeBase,Context context){
        mThemeBase = themeBase;
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
        
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle(R.string.theme_apply_progress_title);
        mProgressDialog.setMessage(mContext.getString(R.string.theme_apply_progress_message));
        mProgressDialog.setMax(100);
        mProgressDialog.show();
        
    }
    
    @Override
    protected String doInBackground(String... params) {
        
        FileInputStream fis = null;
        try {
            if(mThemeBase != null && mThemeBase.getPkg() != null){
                
                /**
                 * 主题源文件
                 */
                File srcFile = null;
                
                if(mThemeBase.getLwtPath() == null){
                    srcFile = new File(new StringBuilder().append(ThemeConstants.THEME_LWT).append("/").append(mThemeBase.getPkg()).toString());
                    fis = new FileInputStream(srcFile);
                }else {
                    srcFile = new File(mThemeBase.getLwtPath());
                    fis = new FileInputStream(srcFile);
                }
                
                /**
                 * 删除旧的开机动画
                 */
                ThemeUtil.removeOldBoots(srcFile);
                
                unLWT(fis);
                
                return "success";
            }else {
                return "fail";
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            try {
                if(fis != null){
                    fis.close();
                    fis = null;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
        }
        
        return "fail";
    }

    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        ArrayList<String> modelNames = new ArrayList<String>();
        boolean hasGlobalModels = false;    
       
        ThemeModelInfo themeModelInfo = mThemeBase.getThemeModelInfo();
        if(themeModelInfo != null){
            SharedPreferences.Editor editor = mContext.getSharedPreferences("CURRENT_USING", Context.MODE_PRIVATE).edit();
            if(themeModelInfo.getPkg() != null){
                editor.putString("pkg", mThemeBase.getPkg());
            }
            if(themeModelInfo.getWallpaper() != null){
                editor.putString("wallpaper", mThemeBase.getPkg());
            }
            if(themeModelInfo.getLockscreenWallpaper() != null){
                editor.putString("lockscreenwallpaper", mThemeBase.getPkg());
            }
            if(themeModelInfo.getLauncher() != null){
                editor.putString("launcher", mThemeBase.getPkg());
            }
            if(themeModelInfo.getIcons() != null){
                editor.putString("icons", mThemeBase.getPkg());
            }
            
            if(themeModelInfo.getBoots() != null){
                editor.putString("boots", mThemeBase.getPkg());
            }
            if(themeModelInfo.getFonts() != null){
                editor.putString("fonts", mThemeBase.getPkg());
            }
            if(themeModelInfo.getLockscreen() != null){
                editor.putString("lockscreen", mThemeBase.getPkg());
                
                ThemeUtil.lockscreenChanged(mContext);
            }
            if(themeModelInfo.getIcons() != null){
                editor.putString("pim", mThemeBase.getPkg());
            }
            
            if(themeModelInfo.getBoots() != null){
                editor.putString("phone", mThemeBase.getPkg());
            }
            if(themeModelInfo.getFonts() != null){
                editor.putString("setting", mThemeBase.getPkg());
            }
            if(themeModelInfo.getLockscreen() != null){
                editor.putString("notify", mThemeBase.getPkg());
                
                ThemeUtil.lockscreenChanged(mContext);
            }
            editor.commit();
            
             ArrayList<String> modelFiles = mThemeBase.getThemeModelInfo().getModelFiles();
            
            for(String modelFile : modelFiles){
                if(modelFile.equals(ThemeConstants.THEME_MODEL_FRAMEWORK) 
						|| modelFile
								.equals(ThemeConstants.THEME_MODEL_SYSTEMUI)
						|| modelFile
								.equals(ThemeConstants.THEME_MODEL_SETTINGS)
						|| modelFile.equals(ThemeConstants.THEME_MODEL_PIM)
						|| modelFile.equals(ThemeConstants.THEME_MODEL_PHONE)
//						|| ThemeModelInfo.otherModels.contains(modelFile)
						) {
                    modelNames.add(modelFile);
                    hasGlobalModels = true;
                }
            }
            
            
            
            
        }
        ThemeUtil.applyThemeAndExit(mContext);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mProgressDialog.cancel();
        ThemeUtil.applyTheme(mThemeBase.getCnName(),modelNames);
       // applyThemeWallPaper(mThemeBase);
       
       // ThemeUtil.applyThemeAndExit(mContext);
        /*if(shouldReboot){
            // apply wallpaper before reboot.
            // TODO: if the user select 'reboot later', the wallpaper will be apply twice. 
            // correct the bug: can't apply the theme wallpaper after reboot when user apply the theme with fonts.
            applyThemeWallPaper(mThemeBase);
            ThemeUtil.applyTheme((Activity)mContext,mThemeBase.getCnName(),modelNames);
            ThemeUtil.reboot(mContext);
        }else {
            ThemeUtil.applyThemeAndExit(mContext);
            ThemeUtil.applyTheme((Activity)mContext,mThemeBase.getCnName(),modelNames);
        }*/
        
       
    }
    // apply wallpaper for theme define by temeBase.
    // TODO: only check the wallpaper dir, maybe need to unzip the wallpaper for lwt file, if we can't find the file in the wallpaper dir.
    private boolean applyThemeWallPaper(ThemeBase themeBase){
        InputStream is = null;
        boolean result = false;
        String wallPaperFileName;
        wallPaperFileName = new StringBuilder(ThemeConstants.THEME_WALLPAPER)
                            .append("/")
                            .append(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX)
                            .append(mThemeBase.getName()).toString();
     
        File wallPaperFile = new File(wallPaperFileName);
        
        if(!wallPaperFile.exists()){
            Log.e(TAG, "ERROR! the wallpaper don't exist:"+wallPaperFileName);
            return false;
        }
        
        try {
            is = new FileInputStream(wallPaperFile);
            WallpaperManager wm = (WallpaperManager) mContext.getSystemService(Context.WALLPAPER_SERVICE);
            if(wm != null){
                wm.setStream(is);
                result = true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
        }finally{
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
       
        return result;
        
    }
    
    private void unLWT(InputStream inputStream){
        
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ZipInputStream zis = null;
        
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        
        File targetFile = new File(ThemeConstants.THEME_FACE_PATH,"_.lwt");
        try {
            mProgressDialog.setProgress(5);
            FileUtils.copyInputStreamToFile(inputStream, targetFile);
            mProgressDialog.setProgress(20);
            fis = new FileInputStream(targetFile);
            bis = new BufferedInputStream(fis, 1024);
            zis = new ZipInputStream(bis);
            
            ZipEntry zipEntry = null;
            byte[] buffer = new byte[1024];
            int temp = -1;
            int progress = 20;
            while((zipEntry = zis.getNextEntry()) != null){
                try {
                    File zipEntryFile = null;
                    /**
                     * 如果是字体文件，则另存文件名为：fonts_temp
                     */
                    if(zipEntry.getName().equals(ThemeConstants.THEME_MODEL_FONTS)){
                        shouldReboot = true;
                        zipEntryFile = new File(ThemeConstants.THEME_FACE_FONTS_TEMP);
                    }else {
                        zipEntryFile = new File(ThemeConstants.THEME_FACE_PATH,zipEntry.getName().replace("\\", "/"));
                    }

                    if(!zipEntry.isDirectory()){
                        
                        fos = new FileOutputStream(zipEntryFile);
                        bos = new BufferedOutputStream(fos, 1024);
                        
                        while((temp = zis.read(buffer)) != -1){
                            bos.write(buffer, 0, temp);
                        }
                        fos.flush();
                        bos.flush();
                        
                        String zipEntryFileName = zipEntryFile.getName();
                        /**
                         * 如果是boots压缩包，则仍需要进行解压
                         */
                        if((zipEntryFileName.indexOf(ThemeConstants.THEME_MODEL_BOOTS) != -1) && ThemeUtil.isZipFile(zipEntryFile)){
                            ThemeUtil.unZip(zipEntryFile,zipEntryFile.getAbsolutePath());
                        }
                        
                    }else{
                        zipEntryFile.mkdirs();
                    }
                    
                    ThemeUtil.changeFilePermission(zipEntryFile);
                    // notify user that the files are being copied. in this
                    // case, we can't get the file's count, so we set a fix
                    // step.
                    // by luoyongxing @20130105
                    if (progress < 88) {
                        progress += 3;
                    }
                    mProgressDialog.setProgress(progress);
                    // end
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                } finally {
                    if(bos != null){
                        bos.close();
                    }
                    if(fos != null){
                        fos.close();
                    }
                }
            }
            mProgressDialog.setProgress(95);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }finally{
            try {
                if(zis != null){
                    zis.closeEntry();
                    zis.close();
                }
                if(bis != null){
                    bis.close();
                }
                if(fis != null){
                    fis.close();
                }
                if(inputStream != null){
                    inputStream.close();
                }
                if(targetFile != null && targetFile.exists()){
                    FileUtils.forceDelete(targetFile);
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
