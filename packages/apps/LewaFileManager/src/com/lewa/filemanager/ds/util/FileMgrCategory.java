/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.util;

import com.lewa.filemanager.ds.database.MediaArgs;
import com.lewa.filemanager.beans.FileUtil;
import com.lewa.filemanager.config.Constants;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class FileMgrCategory {

    public static Map<String, String> categoryMap = new HashMap<String, String>();

    static {
        categoryMap.put(MediaArgs.APK_MIME, Constants.CateContants.CATE_PACKAGE);
        categoryMap.put("image", Constants.CateContants.CATE_IMAGES);
        categoryMap.put("audio", Constants.CateContants.CATE_MUSIC);
        categoryMap.put("video", Constants.CateContants.CATE_VIDEO);
        categoryMap.put("application", Constants.CateContants.CATE_DOCS);
        categoryMap.put("text", Constants.CateContants.CATE_DOCS);
        categoryMap.put("lewa/theme", Constants.CateContants.CATE_THEME);
    }

    public static Map<String, String> getCategoryMap() {
        return categoryMap;
    }

    public static String getCategory(String mime) {
        if (categoryMap.containsKey(mime)) {
            return categoryMap.get(mime);
        } else {
            String variable = FileUtil.parseMimePrefix(mime);
            if ((variable = categoryMap.get(variable)) == null) {
                throw new IllegalStateException("cannt find any category mapped the mime");
            }
            return variable;
        }
    }
}
