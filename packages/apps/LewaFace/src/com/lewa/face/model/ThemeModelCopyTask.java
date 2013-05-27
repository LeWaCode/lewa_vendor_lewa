package com.lewa.face.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.lewa.face.R;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeUtil;



public class ThemeModelCopyTask extends AsyncTask<String, Integer, String>{
    
    private static final String TAG = ThemeModelCopyTask.class.getSimpleName();
    
    private ThemeBase mThemeBase = null;
    private Context mContext = null;
    private ArrayList<String> mSelectModelFiles = null; 
    
    private ProgressDialog mProgressDialog = null;
    
    private int length = 0;
    
    public ThemeModelCopyTask(ThemeBase themeBase,Context context,ArrayList<String> selectModelFiles){
        mThemeBase = themeBase;
        mContext = context;
        mSelectModelFiles = selectModelFiles;
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
        length = mSelectModelFiles.size();
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle(R.string.theme_apply_progress_title);
        mProgressDialog.setMessage(mContext.getString(R.string.theme_apply_progress_message));
		mProgressDialog.setMax(length);
        mProgressDialog.show();
        
    }
    
    @Override
    protected String doInBackground(String... params) {
        
        FileInputStream fis = null;
        try {
            if(mThemeBase != null && mThemeBase.getPkg() != null){
                
                if(mThemeBase.getLwtPath() == null){
                    fis = new FileInputStream(new StringBuilder().append(ThemeConstants.THEME_LWT).append("/").append(mThemeBase.getPkg()).toString());
                }else {
                    File srcFile = new File(mThemeBase.getLwtPath());
                    fis = new FileInputStream(srcFile);
                }
                
				moveModels(fis, mThemeBase);
                
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

	public void onProgressDialogDestroyed() {
		// TODO Auto-generated method stub
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = null;

	}
    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
		if (mProgressDialog != null) {
        mProgressDialog.cancel();
		}
        if(mSelectModelFiles.contains(ThemeConstants.THEME_MODEL_FONTS)){
            ThemeUtil.reboot(mContext);
        }else {
            ThemeUtil.applyThemeAndExit(mContext);
        }
       
    }
    
    
    
	private void moveModels(InputStream inputStream, ThemeBase mThemeBase) {
        ZipFile zipFile = null;
        InputStream is = null;
        BufferedInputStream bis = null;
        
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        
        File targetFile = new File(ThemeConstants.THEME_FACE_PATH,"_.lwt");
        try {
            mProgressDialog.setProgress(1);
            FileUtils.copyInputStreamToFile(inputStream, targetFile);
            mProgressDialog.setProgress(3);
            
            zipFile = new ZipFile(targetFile);
            
            SharedPreferences.Editor editor = mContext.getSharedPreferences("CURRENT_USING", Context.MODE_PRIVATE).edit();
            if(length == Integer.valueOf(mThemeBase.getThemeModelInfo().getContainModelNum())){
                editor.putString("pkg", mThemeBase.getPkg());
            }
			Set<String> ms = ThemeUtil.modulesOnApplied;
            for(int i=0;i<length;i++){
                String modelName = mSelectModelFiles.get(i);
                
                if(modelName.equals(ThemeConstants.THEME_MODEL_BOOTS)){
                    File boots = new File(ThemeConstants.THEME_FACE_BOOTS);
                    if(boots.exists()){
                        FileUtils.forceDelete(boots);
                    }
                }
                
                ZipEntry zipEntry = zipFile.getEntry(modelName);
                mProgressDialog.setProgress(3 + (i+1));
                if(zipEntry != null){
                    try {
                        
                        File zipEntryFile = null;
                        /**
                         * 如果是字体文件，则另存文件名为：fonts_temp
                         */
                        if(zipEntry.getName().equals("fonts")){
                            zipEntryFile = new File(ThemeConstants.THEME_FACE_FONTS_TEMP);
                        }else {
                            zipEntryFile = new File(ThemeConstants.THEME_FACE_PATH,zipEntry.getName().replace("\\", "/"));
                        }
						if (modelName.toLowerCase().contains(
								ThemeConstants.THEME_MODEL_SYSTEMUI
										.toLowerCase())
								|| modelName.toLowerCase().contains(
										ThemeConstants.THEME_MODEL_PHONE
												.toLowerCase())
								|| modelName.toLowerCase().contains(
										ThemeConstants.THEME_MODEL_PIM
												.toLowerCase())
								|| modelName.toLowerCase().contains(
										ThemeConstants.THEME_MODEL_SETTINGS
												.toLowerCase())) {
							if (!ms.contains(modelName)) {
								ms.add(modelName);
							}
						}
                        
                        String zipEntryFileName = zipEntryFile.getName();
                        
						ThemeUtil.changeFilePermission(zipEntryFile);
                        fos = new FileOutputStream(zipEntryFile);
                        bos = new BufferedOutputStream(fos);
                        
                        is = zipFile.getInputStream(zipEntry);
                        bis = new BufferedInputStream(is);
                        byte[] buffer = new byte[1024];
                        int temp = 0;
                        while ((temp = bis.read(buffer)) != -1) {
                            bos.write(buffer, 0, temp);
                        }
                        bos.flush();
                        fos.flush();
                        
                        /**
                         * 如果是boots压缩包，则仍需要进行解压
                         */
                        if((zipEntryFileName.indexOf(ThemeConstants.THEME_MODEL_BOOTS) != -1) && ThemeUtil.isZipFile(zipEntryFile)){
                            ThemeUtil.unZip(zipEntryFile,zipEntryFile.getAbsolutePath());
                        }
                     
                        
                        
                        if(modelName.indexOf(ThemeConstants.THEME_MODEL_LOCKSCREEN_WALLPAPER) != -1){
                            editor.putString("lockscreenwallpaper", mThemeBase.getPkg());
                        }
                        if(modelName.indexOf(ThemeConstants.THEME_MODEL_WALLPAPER) != -1){
                            editor.putString("wallpaper", mThemeBase.getPkg());
                        }
                        if(modelName.indexOf(ThemeConstants.THEME_MODEL_LOCKSCREEN) != -1){
                            editor.putString("lockscreen", mThemeBase.getPkg());
                            
                            ThemeUtil.lockscreenChanged(mContext);
                        }
                        if(modelName.indexOf(ThemeConstants.THEME_MODEL_ICONS) != -1){
                            editor.putString("icons", mThemeBase.getPkg());
                        }
                        if(modelName.indexOf(ThemeConstants.THEME_MODEL_LAUNCHER) != -1){
                            editor.putString("launcher", mThemeBase.getPkg());
                        }
                        if(modelName.indexOf(ThemeConstants.THEME_MODEL_BOOTS) != -1){
                            editor.putString("boots", mThemeBase.getPkg());
                        }
                        if(modelName.indexOf(ThemeConstants.THEME_MODEL_FONTS) != -1){
                            editor.putString("fonts", mThemeBase.getPkg());
                        }
						if (modelName
								.indexOf(ThemeConstants.THEME_MODEL_NOTIFY) != -1) {
							editor.putString("notification",
									mThemeBase.getPkg());
						}
						if (modelName.indexOf(ThemeConstants.THEME_MODEL_PIM) != -1) {
							editor.putString("pim", mThemeBase.getPkg());
						}
						if (modelName.indexOf(ThemeConstants.THEME_MODEL_PHONE) != -1) {
							editor.putString("phone", mThemeBase.getPkg());
						}
						if (modelName
								.indexOf(ThemeConstants.THEME_MODEL_SETTINGS) != -1) {
							editor.putString("settings", mThemeBase.getPkg());
						}
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally{
                        try {
                            if(fos != null){
                                fos.close();
                                fos = null;
                            }
                            if(bos != null){
                                bos.close();
                                bos = null;
                            }
                            if(bis != null){
                                bis.close();
                                bis = null;
                            }
                            if(is != null){
                                is.close();
                                is = null;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
           
            editor.commit();
			ThemeUtil.applyTheme(mThemeBase.getCnName(), ms);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }finally{
            try {
                if(zipFile != null){
                    zipFile.close();
                    zipFile = null;
                }
                if(targetFile != null && targetFile.exists()){
                    FileUtils.forceDelete(targetFile);
                    mProgressDialog.setProgress(length + 4);
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
