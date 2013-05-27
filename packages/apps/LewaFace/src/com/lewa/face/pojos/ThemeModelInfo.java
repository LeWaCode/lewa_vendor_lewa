package com.lewa.face.pojos;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.util.Log;
import com.lewa.face.R;
import com.lewa.face.util.Logs;
import com.lewa.face.util.ThemeConstants;

public class ThemeModelInfo implements Serializable{

    
    
    /**
     * 
     */
    private static final long serialVersionUID = 4047148437084588586L;
    
    private String lockscreenWallpaper;
    private String wallpaper;
    private String lockscreen;
    private String icons;
    private String launcher;
    private String framework;
    private String systemui;
    private String settings;
    private String pim;
    private String boots;
    private String fonts;
	private String phone;
	private List<String> allModels = new ArrayList<String>();
	public static List<String> existingModels = Arrays.asList(new String[] {
			ThemeConstants.THEME_MODEL_LOCKSCREEN_WALLPAPER,
			ThemeConstants.THEME_MODEL_WALLPAPER,
			ThemeConstants.THEME_MODEL_LOCKSCREEN,
			ThemeConstants.THEME_MODEL_LAUNCHER,
			ThemeConstants.THEME_MODEL_ICONS,
			ThemeConstants.THEME_MODEL_LAUNCHER,
			ThemeConstants.THEME_MODEL_FRAMEWORK,
			ThemeConstants.THEME_MODEL_SYSTEMUI,
			ThemeConstants.THEME_MODEL_SETTINGS,
			ThemeConstants.THEME_MODEL_PIM, ThemeConstants.THEME_MODEL_BOOTS,
			ThemeConstants.THEME_MODEL_FONTS 
			,ThemeConstants.THEME_MODEL_PHONE
			});

	public static List<String> excludeModelKeys = Arrays.asList(new String[] {
			"theme.json", "theme.json.zh_CN" });

	public static Comparator<String> comparator = new Comparator<String>() {

		@Override
		public int compare(String object1, String object2) {
			return 0;
		}
	};
	public static Set<String> otherModels = new HashSet<String>();
    private String containModelNum;
    
    private String pkg;
    
    private ArrayList<Integer> modelNames = new ArrayList<Integer>();
    private ArrayList<String> modelFiles = new ArrayList<String>();
	Integer modleNum = 0;
    
    public ThemeModelInfo(String pkg){
        this.pkg = pkg;
		allModels.addAll(existingModels);
        parseTheme(null);
    }


    public ThemeModelInfo(String pkg, String themeFilePatch){
        this.pkg = pkg;
		allModels.addAll(existingModels);
        parseTheme(themeFilePatch);
    }
    
    public String getLockscreenWallpaper() {
		return lockscreenWallpaper;
	}

	public void setLockscreenWallpaper(String lockscreenWallpaper) {
		this.lockscreenWallpaper = lockscreenWallpaper;
	}

	public String getWallpaper() {
        return wallpaper;
    }

    public void setWallpaper(String wallpaper) {
        this.wallpaper = wallpaper;
    }

    public String getIcons() {
        return icons;
    }

    public void setIcons(String icons) {
        this.icons = icons;
    }

    public String getLauncher() {
        return launcher;
    }

    public void setLauncher(String launcher) {
        this.launcher = launcher;
    }
    
    public String getBoots() {
        return boots;
    }

    public void setBoots(String boots) {
        this.boots = boots;
    }

    public String getFonts() {
        return fonts;
    }

    public void setFonts(String fonts) {
        this.fonts = fonts;
    }

    public String getLockscreen() {
        return lockscreen;
    }

    public void setLockscreen(String lockscreen) {
        this.lockscreen = lockscreen;
    }
    
    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public String getSystemui() {
        return systemui;
    }

    public void setSystemui(String systemui) {
        this.systemui = systemui;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
    public String getPim() {
        return pim;
    }

    public void setPim(String pim) {
        this.pim = pim;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }
    
    public String getContainModelNum() {
        return containModelNum;
    }

    public void setContainModelNum(String containModelNum) {
        this.containModelNum = containModelNum;
    }
    
    public ArrayList<Integer> getModelNames() {
        return modelNames;
    }

    public void setModelNames(ArrayList<Integer> modelNames) {
        this.modelNames = modelNames;
    }
    
    public ArrayList<String> getModelFiles() {
        return modelFiles;
    }

    public void setModelFiles(ArrayList<String> modelFiles) {
        this.modelFiles = modelFiles;
    }
    
    /**
     * �涨���pathΪ�գ����ʾ�����Ϊtheme/lwt/��
     * ���path��Ϊ�գ����ʾ�����Ϊ����λ��
     * @param path
     */
    public void parseTheme(String path){
        
        if(pkg == null){
           return;
        }
        
        File themeFile = null;
        
        if(path == null){
            themeFile = new File(new StringBuilder().append(ThemeConstants.THEME_LWT).append("/").append(pkg).toString());
        }else {
            themeFile = new File(path);
        }
        
        if(!themeFile.exists()){
            return;
        }
        
        ZipFile themeZip = null;
        try {
            themeZip = new ZipFile(themeFile);
            int modleNum = 0;
            if(containModel(themeZip, ThemeConstants.THEME_MODEL_LOCKSCREEN_WALLPAPER)){
                lockscreenWallpaper = ThemeConstants.THEME_MODEL_LOCKSCREEN_WALLPAPER;
                modelFiles.add(ThemeConstants.THEME_MODEL_LOCKSCREEN_WALLPAPER);
                modelNames.add(R.string.theme_model_lockscreen_wallpaper);
                ++modleNum;
            }
            
            if(containModel(themeZip, ThemeConstants.THEME_MODEL_WALLPAPER)){
                wallpaper = ThemeConstants.THEME_MODEL_WALLPAPER;
                modelFiles.add(ThemeConstants.THEME_MODEL_WALLPAPER);
                modelNames.add(R.string.theme_model_wallpaper);
                ++modleNum;
            }
            
            if(containModel(themeZip, ThemeConstants.THEME_MODEL_LOCKSCREEN)){
                lockscreen = ThemeConstants.THEME_MODEL_LOCKSCREEN;
                modelFiles.add(ThemeConstants.THEME_MODEL_LOCKSCREEN);
                modelNames.add(R.string.theme_model_lockscreen_style);
                ++modleNum;
            }
            
            if(containModel(themeZip, ThemeConstants.THEME_MODEL_ICONS)){
                icons = ThemeConstants.THEME_MODEL_ICONS;
                modelFiles.add(ThemeConstants.THEME_MODEL_ICONS);
                modelNames.add(R.string.theme_model_icon_style);
                ++modleNum;
            }
            
            if(containModel(themeZip, ThemeConstants.THEME_MODEL_LAUNCHER)){
                launcher = ThemeConstants.THEME_MODEL_LAUNCHER;
                modelFiles.add(ThemeConstants.THEME_MODEL_LAUNCHER);
                modelNames.add(R.string.theme_model_launcher);
                ++modleNum;
            }
            
            /**
             * 是否包含系统界面模块
             */
            if(containModel(themeZip, ThemeConstants.THEME_MODEL_FRAMEWORK)){
                framework = ThemeConstants.THEME_MODEL_FRAMEWORK;
                modelFiles.add(ThemeConstants.THEME_MODEL_FRAMEWORK);
                modelNames.add(R.string.theme_model_framework);
                ++modleNum;
            }
            
            /**
             * 是否包含状态栏模块
             */
            if(containModel(themeZip, ThemeConstants.THEME_MODEL_SYSTEMUI)){
                systemui = ThemeConstants.THEME_MODEL_SYSTEMUI;
                modelFiles.add(ThemeConstants.THEME_MODEL_SYSTEMUI);
                modelNames.add(R.string.theme_model_systemui);
                ++modleNum;
            }
            
            /**
             * 是否包含系统设置模块
             */
            if(containModel(themeZip, ThemeConstants.THEME_MODEL_SETTINGS)){
                settings = ThemeConstants.THEME_MODEL_SETTINGS;
                modelFiles.add(ThemeConstants.THEME_MODEL_SETTINGS);
                modelNames.add(R.string.theme_model_settings);
                ++modleNum;
            }
            
            /**
             * 是否包含PIM模块
             */
            if(containModel(themeZip, ThemeConstants.THEME_MODEL_PIM)){
                pim = ThemeConstants.THEME_MODEL_PIM;
                modelFiles.add(ThemeConstants.THEME_MODEL_PIM);
                modelNames.add(R.string.theme_model_pim);
                 ++modleNum;
            }        	Log.i("", " ------------ pre_add_phone ");

            if (containModel(themeZip, ThemeConstants.THEME_MODEL_PHONE)) {
            	Log.i("", " ------------ after_add_phone ");
				phone = ThemeConstants.THEME_MODEL_PHONE;
				modelFiles.add(ThemeConstants.THEME_MODEL_PHONE);
				modelNames.add(R.string.theme_model_phone);
                 ++modleNum;
            }
            if(containModel(themeZip, ThemeConstants.THEME_MODEL_BOOTS)){
                boots = ThemeConstants.THEME_MODEL_BOOTS;
                modelFiles.add(ThemeConstants.THEME_MODEL_BOOTS);
                modelNames.add(R.string.theme_model_boots);
                ++modleNum;
            }
            
            if(containModel(themeZip, ThemeConstants.THEME_MODEL_FONTS)){
                fonts = ThemeConstants.THEME_MODEL_FONTS;
                modelFiles.add(ThemeConstants.THEME_MODEL_FONTS);
                modelNames.add(R.string.theme_model_fonts);
                ++modleNum;
            }
			
			handleModels(themeZip);
            containModelNum = String.valueOf(modleNum);
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
			e.printStackTrace();
        }finally{
            try{
                if(themeZip != null){
                    themeZip.close();
                    themeZip = null;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            
        }
    }
    
    private boolean containModel(ZipFile themeZip,String modelName){
        ZipEntry zipEntry = themeZip.getEntry(modelName);
        if(zipEntry != null){
            return true;
        }
        return false;
    }
    
	private void handleModels(ZipFile themeZip) {
		otherModels.clear();
		Enumeration enu = themeZip.entries();
		while (enu.hasMoreElements()) {
			ZipEntry en = (ZipEntry) enu.nextElement();
			Logs.i("------ parse "+en.getName());
			if (this.allModels.contains(en.getName())) {
				continue;
			}
			if (en.isDirectory()) {
				continue;
			}
			if (excludeModelKeys.contains(en.getName())) {
				continue;
			}
			if (en.getName().toLowerCase().startsWith("preview")) {
				continue;
			}
			otherModels.add(en.getName());			
			++modleNum;
		}
		modelFiles.addAll(otherModels);
	}
}
