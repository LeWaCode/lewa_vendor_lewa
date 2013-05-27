package com.lewa.launcher.theme;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;


public class ThemeUtils {
    
    private static final String TAG = "ThemeUtils";
    
    public static String getRootSD(){
        return  new StringBuilder()
            .append(Environment.getExternalStorageDirectory().getPath())
            .append("/")
            .append(ThemeConstants.SD_ROOT)
            .toString();
    }
    
    private static Drawable bitmap2drawable(Context context, Bitmap bitmap){
        return new BitmapDrawable(context.getResources(), bitmap);
    }
    public static String getAppIconFileName(Context context, String name, String packageName){
        String picturePath = "/data/system/face/icons";
        File file = new File(picturePath);
        if(!file.exists()){
            Log.i(TAG,"pictrue is not exist : == " + picturePath);
            return null;
        }
       ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            String fileName = new StringBuilder(name).append(".png").toString();
            
            ZipEntry zipEntry = zipFile.getEntry(fileName);

            if(zipEntry != null){
                return name;
            }else{
                fileName = new StringBuilder(packageName).append(".png").toString();
                zipEntry = zipFile.getEntry(fileName);
                if(zipEntry != null){
                    return packageName;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            
            return null;
        }finally{
            try {
               
                if(zipFile != null){
                    zipFile.close();
                    zipFile = null;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

       
        
    }
    public static Drawable getThemeDrawable(Context context,String modelName,String pictureName){
        
        String picturePath = new StringBuilder().append("/data/system/face/").append(modelName).toString();
        File file = new File(picturePath);
        Resources resources = context.getResources();
        if(!file.exists()){
            Log.i(TAG,"pictrue is not exist : == " + pictureName);
            return null;
        }
        InputStream is = null;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            ZipEntry zipEntry = zipFile.getEntry(new StringBuilder().append(pictureName).append(".png").toString());

            if(zipEntry != null){
                is = zipFile.getInputStream(zipEntry);
                return bitmap2drawable(context, BitmapFactory.decodeStream(is));
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "File not found :==" + pictureName);
            return null;
        }finally{
            try {
                if(is != null){
                    is.close();
                    is = null;
                }
                if(zipFile != null){
                    zipFile.close();
                    zipFile = null;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    

}
