/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.base.images;

import com.lewa.base.images.FileCategoryHelper.FileCategory;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.beans.FileUtil;
import java.io.File;

/**
 *
 * @author Administrator
 */
public class MimeUtil {

    public static final String parseMime(String name) {
        String ext = FileUtil.getRealExtension(name);
        return parseMimeFromExt(ext);
    }

    public static final String parseMimeFromPath(String path) {
        return parseMime(FileUtil.getName(path));
    }

    public static final FileTypeInfo isTypeFromMime(String path, FileTypeInfo[] types) {
        String filemime = parseMime(FileUtil.getName(path));
        if (filemime == null || filemime.trim().equals("")) {
            return new FileTypeInfo(FileCategory.NoType, FileInfo.DOC_ICON, null);
        }
        FileTypeInfo type;
        for (int i = 0; i < types.length; i++) {
            type = types[i];
            if (type != null) {
                for (String m : type.mimes) {
                    if (filemime.trim().toUpperCase().startsWith(m.toUpperCase())) {
                        return type;
                    }
                }
            }
        }
        return new FileTypeInfo(FileCategory.NoRecorded, FileInfo.DOC_ICON, null);
    }

    public static final String parseMime(File file) {
        return parseMime(file.getName());
    }

    public static final String parseMimeFromExt(String ext) {
        if (ext == null || ext.trim().equals("")) {
            return "";
        }
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
        if (mime == null) {
            return "";
        }
        return mime.split("/")[0];
    }

    public static final String parseWholeMime(String ext) {
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
        return mime;
    }
}
