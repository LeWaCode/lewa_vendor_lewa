package com.lewa.face.util;

import java.util.Arrays;
import java.util.List;
import android.os.Environment;

/**
 * 
 * @author fulw
 *
 */
public interface ThemeConstants {
    
	public static final String URL =""; 
//		"http://static.lewatek.com/theme";
    
    public static final String THEMEBASE = "themeBase";
    
    /**
     * Some path about themes in sdcard 
     */
    public static final String LEWA = "LEWA";
    
	public static final String SDCARD_ROOT_PATH = Environment
			.getExternalStorageDirectory().getPath();

	public static final String LEWA_PATH = new StringBuilder()
			.append(SDCARD_ROOT_PATH).append("/").append("LEWA").toString();

	public static final String LEWA_THEME_PATH = new StringBuilder()
			.append(LEWA_PATH).append("/").append("theme").toString();

	public static final String THEME_LWT = new StringBuilder()
			.append(LEWA_THEME_PATH).append("/").append("lwt").toString();

	public static final String THEME_WALLPAPER = new StringBuilder()
			.append(LEWA_THEME_PATH).append("/").append("wallpaper").toString();
	public static final String THEME_PIM = new StringBuilder()
			.append(LEWA_THEME_PATH).append("/").append("preview").toString();
	public static final String THEME_SETTING = new StringBuilder()
			.append(LEWA_THEME_PATH).append("/").append("preview").toString();
	public static final String THEME_SYSTEMUI = new StringBuilder()
			.append(LEWA_THEME_PATH).append("/").append("preview").toString();
	public static final String THEME_PHONE = new StringBuilder()
			.append(LEWA_THEME_PATH).append("/").append("preview").toString();
	public static final String THEME_LOCK_SCREEN_WALLPAPER = new StringBuilder()
			.append(LEWA_THEME_PATH).append("/").append("lockscreen")
			.toString();

	public static final String THEME_ICONS = new StringBuilder()
			.append(LEWA_THEME_PATH).append("/").append("icons").toString();

	public static final String THEME_PREVIEW = new StringBuilder()
			.append(LEWA_THEME_PATH).append("/preview").toString();

	public static final String THEME_LOCAL_PREVIEW = new StringBuilder()
			.append(THEME_PREVIEW).append("/").append("local").toString();

	public static final String THEME_ONLINE_PREVIEW = new StringBuilder()
			.append(THEME_PREVIEW).append("/").append("online").toString();

	public static final String THEME_ONLINE_WALLPAPRE = new StringBuilder()
			.append(THEME_ONLINE_PREVIEW).append("/").append("wallpaper")
			.toString();

	public static final String THEME_THUMBNAIL = new StringBuilder()
			.append(LEWA_THEME_PATH).append("/thumbnail").toString();

	public static final String THEME_LOCAL_THUMBNAIL = new StringBuilder()
			.append(THEME_THUMBNAIL).append("/").append("local").toString();

	public static final String THEME_ONLINE_THUMBNAIL = new StringBuilder()
			.append(THEME_THUMBNAIL).append("/").append("online").toString();

	public static final String THEME_LOCAL_WALLPAPER_THUMBNAIL = new StringBuilder()
			.append(THEME_LOCAL_THUMBNAIL).append("/").append("wallpaper")
			.toString();
	public static final String THEME_LOCAL_PIM_THUMBNAIL = new StringBuilder()
			.append(THEME_LOCAL_THUMBNAIL).toString();
	public static final String THEME_LOCAL_PHONE_THUMBNAIL = new StringBuilder()
			.append(THEME_LOCAL_THUMBNAIL).toString();
	public static final String THEME_LOCAL_SETTING_THUMBNAIL = new StringBuilder()
			.append(THEME_LOCAL_THUMBNAIL).toString();
	public static final String THEME_LOCAL_SYSTEMUI_THUMBNAIL = new StringBuilder()
			.append(THEME_LOCAL_THUMBNAIL).toString();

	public static final String THEME_ONLINE_WALLPAPER_THUMBNAIL = new StringBuilder()
			.append(THEME_ONLINE_THUMBNAIL).append("/").append("wallpaper")
			.toString();

	public static final String THEME_RINGTONE = new StringBuilder()
			.append(LEWA_THEME_PATH).append("/").append("ringtone").toString();

	public static final String THEME_MODEL = new StringBuilder()
			.append(LEWA_THEME_PATH).append("/").append("model").toString();

	public static final String THEME_MODEL_LOCKSCREEN_STYLE = new StringBuilder()
			.append(THEME_MODEL).append("/").append("lockscreen").toString();

	public static final String THEME_MODEL_ICONS_STYLE = new StringBuilder()
			.append(THEME_MODEL).append("/").append("icons").toString();

	public static final String THEME_MODEL_LAUNCHER_STYLE = new StringBuilder()
			.append(THEME_MODEL).append("/").append("launcher").toString();

	public static final String THEME_MODEL_BOOTS_STYLE = new StringBuilder()
			.append(THEME_MODEL).append("/").append("boots").toString();
	public static final String THEME_MODEL_NOTIFY_STYLE = new StringBuilder()
			.append(THEME_MODEL).append("/").append("notification").toString();
	public static final String THEME_MODEL_PIM_STYLE = new StringBuilder()
			.append(THEME_MODEL).append("/").append("pim").toString();
	public static final String THEME_MODEL_PHONE_STYLE = new StringBuilder()
			.append(THEME_MODEL).append("/").append("phone").toString();
	public static final String THEME_MODEL_SETTING_STYLE = new StringBuilder()
			.append(THEME_MODEL).append("/").append("setting").toString();
	public static final String THEME_MODEL_FRAMEWORK_STYLE = new StringBuilder()
			.append(THEME_MODEL).append("/").append("framework").toString();
	public static final String THEME_MODEL_FONTS_STYLE = new StringBuilder()
			.append(THEME_MODEL).append("/").append("fonts").toString();

	/**
	 * Some path about themes in data/system/face
	 */
	public static final String THEME_FACE_PATH = "/data/system/face";

	public static final String THEME_FACE_FONTS = new StringBuilder()
			.append(THEME_FACE_PATH).append("/fonts").toString();

	public static final String THEME_FACE_FONTS_TEMP = new StringBuilder()
			.append(THEME_FACE_PATH).append("/fonts_temp").toString();

	public static final String THEME_FACE_BOOTS = new StringBuilder()
			.append(THEME_FACE_PATH).append("/boots").toString();
	public static final String THEME_FACE_NOTIFY = new StringBuilder()
			.append(THEME_FACE_PATH).append("/com.android.systemui").toString();
	public static final String THEME_FACE_PIM = new StringBuilder()
			.append(THEME_FACE_PATH).append("/com.lewa.PIM").toString();
	public static final String THEME_FACE_PHONE = new StringBuilder()
			.append(THEME_FACE_PATH).append("/com.android.phone").toString();
	public static final String THEME_FACE_SETTING = new StringBuilder()
			.append(THEME_FACE_PATH).append("/com.android.settings").toString();
	public static final String THEME_FACE_BOOTS_TEMP = new StringBuilder()
			.append(THEME_FACE_PATH).append("/boots_temp").toString();
	public static final String THEME_FACE_NOTIFY_TEMP = new StringBuilder()
			.append(THEME_FACE_PATH).append("/notify_temp").toString();
	public static final String THEME_FACE_PIM_TEMP = new StringBuilder()
			.append(THEME_FACE_PATH).append("/pim_temp").toString();
	public static final String THEME_FACE_FRAMEWORK_TEMP = new StringBuilder()
			.append(THEME_FACE_PATH).append("/framework_temp").toString();
	public static final String THEME_FACE_FRAMEWORK = new StringBuilder()
			.append(THEME_FACE_PATH).append("/framework-res").toString();

	public static final String THEME_FACE_PHONE_TEMP = new StringBuilder()
			.append(THEME_FACE_PATH).append("/phone_temp").toString();
	public static final String THEME_FACE_SETTING_TEMP = new StringBuilder()
			.append(THEME_FACE_PATH).append("/setting_temp").toString();
	public static final String THEME_FACE_ICONS = new StringBuilder()
			.append(THEME_FACE_PATH).append("/icons").toString();
	public static final String THEME_FACE_ICONS_TEMP = new StringBuilder()
			.append(THEME_FACE_PATH).append("/icons_temp").toString();

	public static final String THEME_FACE_LAUNCHER = new StringBuilder()
			.append(THEME_FACE_PATH).append("/launcher").toString();
	public static final String THEME_FACE_LAUNCHER_TEMP = new StringBuilder()
			.append(THEME_FACE_PATH).append("/launcher_temp").toString();

	public static final String THEME_FACE_LOCKSCREENWALLPAPER = new StringBuilder()
			.append(THEME_FACE_PATH)
			.append("/wallpaper/lock_screen_wallpaper.jpg").toString();
	public static final String THEME_FACE_LOCKSCREENWALLPAPER_TEMP = new StringBuilder()
			.append(THEME_FACE_PATH)
			.append("/wallpaper/lock_screen_wallpaper.jpg_temp").toString();

	public static final String THEME_FACE_WALLPAPER = new StringBuilder()
			.append(THEME_FACE_PATH).append("/wallpaper/wallpaper.jpg")
			.toString();
	public static final String THEME_FACE_WALLPAPER_TEMP = new StringBuilder()
			.append(THEME_FACE_PATH).append("/wallpaper/wallpaper.jpg_temp")
			.toString();

	public static final String THEME_FACE_LOCKSCREEN = new StringBuilder()
			.append(THEME_FACE_PATH).append("/lockscreen").toString();
	public static final String THEME_FACE_LOCKSCREEN_TEMP = new StringBuilder()
			.append(THEME_FACE_PATH).append("/lockscreen_temp").toString();
    
    /**
     * state for sdcard
     */
    public static final int MEDIA_MOUNTED = 0;
    public static final int MEDIA_UNMOUNTED = 1;
    
    /**
     * theme state
     */
    public static final int THEME_LOCAL = 0;
    public static final int THEME_ONLINE = 1;
    
    public static final int THEME_THUMBNAIL_LOCAL_PREVIEW = 0;
    public static final int THEME_THUMBNAIL_ONLINE_PREVIEW = 1;
    public static final int THEME_THUMBNAIL_MODE = 2;
    public static final int THEME_THUMBNAIL_CUSTOMIZE = 3;
    
    public static final String BUFFIX_LWT = ".lwt";
    public static final String BUFFIX_JPG = ".jpg";
    
    
    /**
     * theme models
     */
    public static final String THEME_MODEL_LOCKSCREEN_WALLPAPER = "wallpaper/lock_screen_wallpaper.jpg";
    
    public static final String THEME_MODEL_WALLPAPER = "wallpaper/wallpaper.jpg";
    
    public static final String THEME_MODEL_LOCKSCREEN = "lockscreen";
    
    public static final String THEME_MODEL_ICONS = "icons";
    
    public static final String THEME_MODEL_LAUNCHER = "launcher";
    
    public static final String THEME_MODEL_FRAMEWORK = "framework-res";
    
    public static final String THEME_MODEL_SYSTEMUI = "com.android.systemui";
    
    public static final String THEME_MODEL_SETTINGS = "com.android.settings";
    
    public static final String THEME_MODEL_PIM = "com.lewa.PIM";
    
    public static final String THEME_MODEL_BOOTANIMATION = "boots/bootanimation.zip";
    
    //public static final String THEME_MODEL_FONTS = "fonts/DroidSansFallback.ttf";
    public static final String[] PREVIEW_SEQ = new String[]{
        ThemeConstants.PREVIEW_LOCKSCREEN_STYLE,
        ThemeConstants.PREVIEW_BOOTS,
        ThemeConstants.PREVIEW_LAUNCHER_STYLE,
			ThemeConstants.PREVIEW_ICONS, ThemeConstants.PREVIEW_PIM,
			ThemeConstants.PREVIEW_PHONE, ThemeConstants.PREVIEW_SETTING,
			ThemeConstants.PREVIEW_NOTIFICATION, ThemeConstants.PREVIEW_FONTS,
			ThemeConstants.PREVIEW_OTHER };
	public static final List<String> PREVIEW_SEQ_LIST = Arrays
			.asList(PREVIEW_SEQ);
    public static final String THEME_MODEL_BOOTS = "boots";
    public static final String THEME_MODEL_NOTIFY = THEME_MODEL_SYSTEMUI;
    public static final String THEME_MODEL_FONTS = "fonts";
    public static final String THEME_MODEL_PHONE = "com.android.phone";
    public static final int THUMBNAIL_WVGA_HEIGHT = 348;
    public static final int THUMBNAIL_WVGA_WIDTH = 240;
    
    //public static final int THUMBNAIL_WVGA_HEIGHT = 232;
    //public static final int THUMBNAIL_WVGA_WIDTH = 159;
    
    public static final int THUMBNAIL_HVGA_HEIGHT = 232;
    public static final int THUMBNAIL_HVGA_WIDTH = 159;
    
    public static final String THEME_PREVIEW_LOCKSCREEN_0 = "preview/preview_lockscreen_0.jpg";
    
    public static final String THEME_THUMBNAIL_LOCKSCREEN_PREFIX = "lockscreen_";
    public static final String THEME_THUMBNAIL_THUMBNAIL = "thumbnail_";
    public static final String THEME_THUMBNAIL_ICONS_PREFIX = "icons_";
    public static final String THEME_THUMBNAIL_PIM_PREFIX = "pim_";
    public static final String THEME_THUMBNAIL_PHONE_PREFIX = "phone_";
    public static final String THEME_THUMBNAIL_SETTING_PREFIX = "setting_";
    public static final String THEME_THUMBNAIL_LAUNCHER_PREFIX = "launcher_";
    
    public static final String THEME_THUMBNAIL_WALLPAPER_PREFIX = "wallpaper_";
    public static final String THEME_THUMBNAIL_LOCKSCREEN_WALLPAPER_PREFIX = "lockscreen_wallpaper_";
    public static final String THEME_THUMBNAIL_BOOTS_PREFIX = "boots_";
    public static final String THEME_THUMBNAIL_SYSTEMUI_PREFIX = "notification_";
    public static final String THEME_THUMBNAIL_FONTS_PREFIX = "fonts_";
    public static final String THEME_THUMBNAIL_OTHERS_PREFIX = "other_";
    
    public static final String MODEL_PREVIEW_LOCKSCREEN = "lockscreen";
    public static final String MODEL_PREVIEW_WALLPAPER ="wallpaper";
    public static final String MODEL_PREVIEW_LOCKSCREEN_WALLPAPER ="lock_screen_wallpaper";
    public static final String MODEL_PREVIEW_PIM = "PIM_";
    public static final String MODEL_PREVIEW_PHONE = "phone_";
    public static final String MODEL_PREVIEW_SETTING = "setting_";
    public static final String MODEL_PREVIEW_ICONS = "icons";
    public static final String MODEL_PREVIEW_LAUNCHER = "launcher";
    public static final String MODEL_PREVIEW_BOOTANIMATION = "bootanimation";
    public static final String MODEL_PREVIEW_FONTS = "fonts";
    public static final String MODEL_PREVIEW_NOTIFY = "notification_";
    public static final String MODEL_PREVIEW_OTHERS = "other_";
    public static final String PREVIEW_ICONS = "icons_";
    public static final String PREVIEW_LOCKSCREEN_STYLE = "lockscreen_";
    public static final String PREVIEW_LAUNCHER_STYLE = "launcher_";
    public static final String PREVIEW_BOOTS = "bootanimation_";
    public static final String PREVIEW_FONTS = "fonts_";
    public static final String PREVIEW_NOTIFICATION = "notification_";

    public static final String PREVIEW_SETTING = "setting_";
    public static final String PREVIEW_LOCKSCREEN_WALLPAPER = "lock_screen_wallpaper_";
    public static final String PREVIEW_WALLPAPER = "wallpaper_";
	public static final String PREVIEW_PIM = "pim_";
	public static final String PREVIEW_PHONE= "phone_";
	public static final String PREVIEW_OTHER= "other_";
    public static final int THEMEPKG = 0;
    public static final int ICONS = 1;
    public static final int LOCKSCREEN = 2;
    public static final int LAUNCHER = 3;
    public static final int WALLPAPER = 4;
    public static final int LSWALLPAPER = 5;
    public static final int BOOTS = 6;
    public static final int FONTS = 7;
    public static final int NOTIFY = 8;
    public static final int PIM = 9;
    public static final int PHONE = 10;
    public static final int SETTING = 11;
    
    public static final int LOCAL_PAGE = 0;
    public static final int ONLINE_PAGE = 1;
    
    /**
     * 下载过程中Notification id
     */
    public static final int DOWNLOAD_NOTIFICATION_ID = 1;
    
    /**
     * 下载完成且成功
     */
    public static final int DOWNLOADED = 0;
    /**
     * 下载中
     */
    public static final int DOWNLOADING = 1;
    /**
     * 下载失败
     */
    public static final int DOWNLOADFAIL = 2;
    
    /**
     * 默认主题相关，位于第一个位置
     */
    public static final int DEFAULT = 0;
    public static final int SECOND = 1;
    
    public static final String DEFAULT_THEME_PKG = "default.lwt";
    
    public static final int LOCKSCREEN_CHANGED = 1;
    
    /**
     * Intent发自乐蛙文件管理器的标志
     */
    public static final String FROM_FILE_MANAGER_VALUE ="filemgr";
    
    public static final String FROM_FILE_MANAGER_KEY = "from";
    
    public static final String FILE_PATH_IN_FILE_MANAGER = "com.lewa.lwt.field.filepath";
    
    public static final String LOCAL_THEME_FILE = "/data/data/com.lewa.face/files/LOCAL_THEME";
    /**
     * 发送Intent标志
     */
    public static final String KEY_LEWA_SEND_FLAG = "com.lewa.filemgr.SEND_FLAG";
    public static final int VALUE_LEWA_SEND_FLAG = 1;
    public static final int VALUE_LEWA_MULTY_SEND_FLAG = 2;
    public static final class GoToInvokeLWT {

        public static final String ACTION_INVOKE_LWT = "com.lewa.lwt.action";
        public static final String INVOKE_LWT_FILE_FIELD = "com.lewa.lwt.field.filepath";
        public static final String FLAG_INVOKE_LWT = "filemgr";
        public static final String FLAG_KEY_INVOKE_LWT = "from";
        public static final String ACTION_DELETE_LWT = "com.lewa.lwt.delete.action";
    }
}
