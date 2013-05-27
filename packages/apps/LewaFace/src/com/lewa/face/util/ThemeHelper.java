package com.lewa.face.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.Context;

import com.lewa.face.pojos.ThemeBase;

public class ThemeHelper implements ThemeConstants{
    
    /**
     * 
     * @param context
     * @param themeType
     * @param model 模块名
     * @return
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<ThemeBase> getThemeBases(Context context,int themeType,int model){
        ArrayList<ThemeBase> themebases = new ArrayList<ThemeBase>();
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            if(themeType == THEME_LOCAL){
            	switch (model) {
				case ThemeConstants.THEMEPKG:
					fis = context.openFileInput("LOCAL_THEME");
					break;
				case ThemeConstants.ICONS:
					fis = context.openFileInput("LOCAL_ICONS");
					break;
				case ThemeConstants.LOCKSCREEN:
					fis = context.openFileInput("LOCAL_LOCKSCREEN");
					break;
				case ThemeConstants.LAUNCHER:
					fis = context.openFileInput("LOCAL_LAUNCHER");
					break;
				case ThemeConstants.WALLPAPER:
					fis = context.openFileInput("LOCAL_WALLPAPER");
					break;
				case ThemeConstants.LSWALLPAPER:
					fis = context.openFileInput("LOCAL_LSWALLPAPER");
					break;
				case ThemeConstants.BOOTS:
                    fis = context.openFileInput("LOCAL_BOOTS");
                    break;
				case ThemeConstants.FONTS:
                    fis = context.openFileInput("LOCAL_FONTS");
                    break;
				case ThemeConstants.NOTIFY:
					fis = context.openFileInput("LOCAL_NOTIFICATIONS");
					break;
				case ThemeConstants.PIM:
					fis = context.openFileInput("LOCAL_PIM");
					break;
				case ThemeConstants.PHONE:
					fis = context.openFileInput("LOCAL_PHONE");
					break;
				case ThemeConstants.SETTING:
					fis = context.openFileInput("LOCAL_SETTINGS");
					break;
				default:
					break;
				}
                
            }else {
            	switch (model) {
				case ThemeConstants.THEMEPKG:
					fis = context.openFileInput("ONLINE_THEME");
					break;
				case ThemeConstants.ICONS:
					fis = context.openFileInput("ONLINE_ICONS");
					break;
				case ThemeConstants.LOCKSCREEN:
					fis = context.openFileInput("ONLINE_LOCKSCREEN");
					break;
				case ThemeConstants.LAUNCHER:
					fis = context.openFileInput("ONLINE_LAUNCHER");
					break;
				case ThemeConstants.WALLPAPER:
					fis = context.openFileInput("ONLINE_WALLPAPER");
					break;
				case ThemeConstants.LSWALLPAPER:
					fis = context.openFileInput("ONLINE_LSWALLPAPER");
					break;
				case ThemeConstants.BOOTS:
                    fis = context.openFileInput("ONLINE_BOOTS");
                    break;
				case ThemeConstants.FONTS:
                    fis = context.openFileInput("ONLINE_FONTS");
                    break;
				case ThemeConstants.NOTIFY:
					fis = context.openFileInput("ONLINE_NOTIFICATIONS");
					break;
				default:
					break;
				}
            }
            ois = new ObjectInputStream(fis);
            themebases = (ArrayList<ThemeBase>) ois.readObject();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }finally{
            try {
                if(ois != null){
                    ois.close();
                    ois = null;
                }
                if(fis != null){
                    fis.close();
                    fis = null;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return themebases;

    }
    
    /**
     * 
     * @param context
     * @param themeBases
     * @param themeType
     * @param model 模块名
     */
    public static void saveThemeBases(Context context,ArrayList<ThemeBase> themeBases,int themeType,int model){
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
        	if(themeType == THEME_LOCAL){
            	switch (model) {
				case ThemeConstants.THEMEPKG:
					fos = context.openFileOutput("LOCAL_THEME", Context.MODE_PRIVATE);
					break;
				case ThemeConstants.ICONS:
					fos = context.openFileOutput("LOCAL_ICONS", Context.MODE_PRIVATE);
					break;
				case ThemeConstants.LOCKSCREEN:
					fos = context.openFileOutput("LOCAL_LOCKSCREEN", Context.MODE_PRIVATE);
					break;
				case ThemeConstants.LAUNCHER:
					fos = context.openFileOutput("LOCAL_LAUNCHER", Context.MODE_PRIVATE);
					break;
				case ThemeConstants.WALLPAPER:
					fos = context.openFileOutput("LOCAL_WALLPAPER", Context.MODE_PRIVATE);
					break;
				case ThemeConstants.LSWALLPAPER:
					fos = context.openFileOutput("LOCAL_LSWALLPAPER", Context.MODE_PRIVATE);
					break;
				case ThemeConstants.BOOTS:
                    fos = context.openFileOutput("LOCAL_BOOTS", Context.MODE_PRIVATE);
                    break;
				case ThemeConstants.FONTS:
					fos = context.openFileOutput("LOCAL_FONTS",
							Context.MODE_PRIVATE);
					break;
				case ThemeConstants.NOTIFY:
					fos = context.openFileOutput("LOCAL_NOTIFICATIONS",
							Context.MODE_PRIVATE);
					break;
				case ThemeConstants.PIM:
					fos = context.openFileOutput("LOCAL_PIM",
							Context.MODE_PRIVATE);
					break;
				case ThemeConstants.PHONE:
					fos = context.openFileOutput("LOCAL_PHONE",
							Context.MODE_PRIVATE);
					break;
				case ThemeConstants.SETTING:
					fos = context.openFileOutput("LOCAL_SETTINGS",
							Context.MODE_PRIVATE);
					break;
				default:
					break;
				}
                
            }else {
            	switch (model) {
				case ThemeConstants.THEMEPKG:
					fos = context.openFileOutput("ONLINE_THEME", Context.MODE_PRIVATE);
					break;
				case ThemeConstants.ICONS:
					fos = context.openFileOutput("ONLINE_ICONS", Context.MODE_PRIVATE);
					break;
				case ThemeConstants.LOCKSCREEN:
					fos = context.openFileOutput("ONLINE_LOCKSCREEN", Context.MODE_PRIVATE);
					break;
				case ThemeConstants.LAUNCHER:
					fos = context.openFileOutput("ONLINE_LAUNCHER", Context.MODE_PRIVATE);
					break;
				case ThemeConstants.WALLPAPER:
					fos = context.openFileOutput("ONLINE_WALLPAPER", Context.MODE_PRIVATE);
					break;
				case ThemeConstants.LSWALLPAPER:
					fos = context.openFileOutput("ONLINE_LSWALLPAPER", Context.MODE_PRIVATE);
					break;
				case ThemeConstants.BOOTS:
                    fos = context.openFileOutput("ONLINE_BOOTS", Context.MODE_PRIVATE);
                    break;
				case ThemeConstants.FONTS:
                    fos = context.openFileOutput("ONLINE_FONTS", Context.MODE_PRIVATE);
                    break;
				default:
					break;
				}
            }
            oos = new ObjectOutputStream(fos);
            oos.writeObject(themeBases);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            try {
                if(oos != null){
                    oos.close();
                    oos = null;
                }
                if(fos != null){
                    fos.close();
                    fos = null;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
