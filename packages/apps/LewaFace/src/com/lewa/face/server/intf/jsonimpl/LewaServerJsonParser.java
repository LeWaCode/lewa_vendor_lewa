/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.face.server.intf.jsonimpl;

import com.lewa.face.server.intf.NetBaseParam;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.server.intf.NetHelper;
import com.lewa.face.server.intf.UrlParam;
import com.lewa.face.util.Logs;
import com.lewa.face.util.ThemeConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Administrator
 */
public class LewaServerJsonParser {

	public static int parseCount(String str) {
		try {
			return Integer.parseInt(str.replaceAll("\"", "").trim());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static ThemeBase appendThemeBase(String str, ThemeBase tb) {
		JSONObject jsonobj = null;
		if (str == null) {
			// tb.thumbnailpath = "";
			// tb.previewpath = new ArrayList<String>();
			return null;
		}
		try {
			jsonobj = new JSONObject(str);
			tb.thumbnailpath = jsonobj.getString("thumb");
		} catch (Exception ex) {
			// tb.thumbnailpath = "";
		}

		try {
			tb.previewpath = Arrays.asList(jsonobj.getString("preview").split(
					","));
		} catch (Exception ex) {
			// tb.previewpath = new ArrayList<String>();
		}
		Logs.i("-----> tb.thumbnailpath " + tb.thumbnailpath);
		Logs.i("-----> tb.previewpath " + tb.previewpath);
		return tb;
	}

	public static List<ThemeBase> parseListThemeBase(String str,
			boolean parsePackage, Object... obj) {

		try {
			Logs.i("-------------- parseListThemeBase " + parsePackage + " "
					+ str);
			List<ThemeBase> themes = new ArrayList<ThemeBase>(0);
			if (str.trim().toLowerCase().contains("nothing")
					|| str.trim().toLowerCase().contains("wrong")) {
				return themes;
			}
			JSONArray array = new JSONArray(str);
			int count = array.length();
			for (int i = 0; i < count; i++) {
				JSONObject jo = (JSONObject) array.get(i);
				ThemeBase themeBase = new ThemeBase(null, ThemeConstants.LEWA,
						null, false);
				themeBase.setCnName(jo.getString(NetBaseParam.NAME_ZH));// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
				themeBase.setEnName(jo.getString(NetBaseParam.NAME_EN));// 锟斤拷锟斤拷英锟斤拷锟斤拷锟斤拷
				if(jo.has("themeId")){
					themeBase.setId(jo.getString("themeId"));
				}
				if(jo.has("previewPath")){
					themeBase.previewpath.addAll(Arrays.asList(jo.getString("previewPath").split(",")));
				}
				if(jo.has("thumbnailPath")){
					themeBase.thumbnailpath = jo.getString("thumbnailPath");
				}
				try {
					
					if(jo.has(NetBaseParam.ID)){
						themeBase.setId(jo.getString(NetBaseParam.ID));
					}
					themeBase.setCnAuthor(jo.getString(NetBaseParam.AUTHOR));// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
					themeBase.setEnAuthor(jo.getString(NetBaseParam.AUTHOR_EN));// 锟斤拷锟斤拷英锟斤拷锟斤拷锟斤拷
					themeBase.setPkg(jo.getString(NetBaseParam.PKG_NAME));
					// themeBase.setIdFromLewaServer(jo.getInt(NetBaseParam.ID));//锟斤拷锟�
					// Logs.i("----------------- PREVIEW" +
					// jo.has(NetBaseParam.PREVIEW));

					if (jo.has(NetBaseParam.ATTACHMENT)) {
						themeBase.attachment = jo
								.getString(NetBaseParam.ATTACHMENT);
					}
					if (parsePackage) {
						themeBase
								.setSize(jo.getString(NetBaseParam.THEME_SIZE));
						themeBase.setVersion(jo
								.getString(NetBaseParam.PKG_VERSION));//
						if (jo.getString(NetBaseParam.LOCKSCREEN).equals("0")) {// 锟角凤拷锟斤拷锟斤拷锟�
																				// themeBase.setContainLockScreen(false);
						} else {
							themeBase.setContainLockScreen(true);
						}
						themeBase.setModelNum(jo
								.getString(NetBaseParam.MODULE_NUM));

						String regetUrl = NetBaseParam.PrefixUrl
								+ "/modulepreview?themeid=" + themeBase.getId()
								+ "&moduleid="
								+ ((UrlParam) obj[0]).getCombineModelInt();

						appendThemeBase(NetHelper.getNetString(regetUrl),
								themeBase);
					} else {
						if (jo.has(NetBaseParam.THUMB)) {
							Logs.i("----------------- THUMB"
									+ jo.getString(NetBaseParam.THUMB));
							themeBase.thumbnailpath = jo
									.getString(NetBaseParam.THUMB);
						}
						if (jo.has(NetBaseParam.PREVIEW)) {
							themeBase.previewpath = Arrays.asList(jo.getString(
									NetBaseParam.PREVIEW).split(","));
							Logs.i("----------------- PREVIEW"
									+ jo.getString(NetBaseParam.PREVIEW));
						}
					}
				} catch (JSONException js) {

				}
				themes.add(themeBase);
			}
			return themes;
		}catch(JSONException se){
			se.printStackTrace();
		}
		return null;
	}
}
