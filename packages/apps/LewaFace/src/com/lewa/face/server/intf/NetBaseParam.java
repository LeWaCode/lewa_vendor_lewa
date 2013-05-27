/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.face.server.intf;

import android.os.Build;
import com.lewa.face.util.Logs;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeUtil;

/**
 * 
 * @author Administrator
 */
public class NetBaseParam {
	public String screenSchema = "";
	public String actualtype = "";
	public String mType = "";
	public static final String Host = "http://admin.lewatek.com";
	public static final String Port = "";
	public static final String Path = "/themeapi";
	public static final String PrefixUrl = Host + Port + Path;

	public static final String WALLPAPER = "wallpaper";
	public static final String THEMEPACKAGE = "themepackage";
	public static final String LOCKSCREEN = "lockscreen";
	public static final String BOOTANIMATION = "animation";
	public static final String FONT = "font";
	public static final String ICONSTYLE = "ICONSTYLE";
	public static final String FAVORITEBAR = "FAVORITEBAR";
	public static final String SYSTEMUI = "SYSTEMUI";
	public static final String SYSTEM = "system_application";

	public static final String PREVIEW = "preview";
	public static final String THUMB = "thumb";
	public static final String SCREENSCHEMA_WVGA = "WVGA";
	public static final String SCREENSCHEMA_HVGA = "HVGA";
	public static final String SCREENSCHEMA = "resolution";
	public static final String FILENAME = "filename";
	public static final String ID = "id";
	public static final String NAME_ZH = "name_zh";
	public static final String NAME_EN = "name_en";
	public static final String AUTHOR = "author";
	public static final String AUTHOR_EN = "author_en";
	public static final String THEME_SIZE = "theme_size";
	public static final String PKG_VERSION = "theme_version";
	public static final String PKG_NAME = "filename";
	public static final String ATTACHMENT = "attachment";
	public static final String MODULE_NUM = "module_num";
	public static final String VERSION_2_3_7 = "2.3.7";
	public static final String VERSION_4_0 = "4.0";
	public static String SYS_VERSION = Build.VERSION.SDK_INT < 14 ? VERSION_2_3_7
			: VERSION_4_0;
	// public static String SYS_VERSION = VERSION_4_0;
	public int combineModelInt = -1;
	public final String CONST_SYS_VERSION = "system_version";

	public static boolean isPackgeResource(String type) {
		return type.equalsIgnoreCase(ICONSTYLE)
				|| type.equalsIgnoreCase(FAVORITEBAR);
	}

	public NetBaseParam(String type) {
		this.mType = actualtype = type;
		if (type.equals(ICONSTYLE)) {
			combineModelInt = 5;
		} else if (type.equals(FAVORITEBAR)) {
			combineModelInt = 3;
		}
	}

	public int getCombineModelInt() {
		return combineModelInt;
	}

	public static String getCurrentScreenSchema() {
		return ThemeUtil.isWVGA ? SCREENSCHEMA_WVGA : SCREENSCHEMA_HVGA;
	}

	@Override
	public String toString() {
		if(isPackgeResource(mType)){
			return toAllUrl();
		}
		return toGenrericModuleUrl();
	}

	public String toAllUrl() {
		String MODULE = "moduleid=";
		String type = "";
		int moduleId = -1;
		String param = "";
		String moduleParam = "";
		String resolutionParam = "";
		String systemVersionParam = "";
		Logs.i("==> " + this.mType);
		if (mType.equals(WALLPAPER)) {
			resolutionParam = "";
			systemVersionParam = "";
		} else {
			resolutionParam = resolveResolutionParam();
			systemVersionParam = "&" + CONST_SYS_VERSION + "="
					+ NetBaseParam.SYS_VERSION;
		}
		String pagingStr = "";
		String pagingParam = "";
		Logs.i("type " + mType);
		if (mType.equals(ICONSTYLE)) {

			type = "getmodule";
			moduleId = ThemeConstants.ICONS;
			moduleParam += MODULE + moduleId;
		} else if (mType.equals(FAVORITEBAR)) {
			type = "getmodule";
			moduleId = ThemeConstants.LAUNCHER;
			moduleParam += MODULE + moduleId;
		}
		param += moduleParam
		 +(resolutionParam.startsWith("&")?resolutionParam:"&"+resolutionParam) 
//		 + pagingParam
		 + systemVersionParam
		;
		if (!(resolutionParam.trim().equals("")
				&& pagingParam.trim().equals("") && systemVersionParam.trim()
				.equals(""))) {
			param = "?" + param;
		}
		String result = NetBaseParam.PrefixUrl + "/" + type + pagingStr + param;
		Logs.i("=="+result);
		return result;
	}

	public String resolveResolutionParam() {
		String resolutionParam;
		if (mType.equalsIgnoreCase(FONT)) {
			resolutionParam = "";
		} else {
			resolutionParam = SCREENSCHEMA + "=" + screenSchema;
		}
		return resolutionParam;
	}
	public String toGenrericModuleUrl() {
		String resolutionParam;
		String systemVersionParam;
		Logs.i("==> " + mType);
		if (mType.equals(WALLPAPER)) {
			resolutionParam = "";
			systemVersionParam = "";
		} else {
			resolutionParam = resolveResolutionParam();
			systemVersionParam = "&" + CONST_SYS_VERSION + "="
					+ NetBaseParam.SYS_VERSION;
		}
		String pagingStr = "";
		String pagingParam = "";
		if (mType.equals(ICONSTYLE) || mType.equals(FAVORITEBAR)) {
			mType = THEMEPACKAGE;
		}
		String param = resolutionParam + pagingParam ;
		if(!mType.equals(BOOTANIMATION)){
			param+= systemVersionParam;
		}
		if (!(resolutionParam.trim().equals("")
				&& pagingParam.trim().equals("") && systemVersionParam.trim()
				.equals(""))) {
			param = "?" + param;
		}
		return NetBaseParam.PrefixUrl + "/" + mType + pagingStr + param;
	}

}
