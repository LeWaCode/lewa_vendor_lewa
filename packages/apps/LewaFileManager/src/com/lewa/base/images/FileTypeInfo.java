/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.base.images;

import android.net.Uri;
import android.provider.MediaStore;
import com.lewa.base.images.FileCategoryHelper.FileCategory;
import com.lewa.filemanager.ds.database.MediaArgs;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.beans.FileUtil;
import com.lewa.filemanager.config.Constants;

/**
 *
 * @author chenliang
 */
public class FileTypeInfo {

    public static final String apkMime = "application/vnd.android.package-archive";
    //注重Apk和Doc的顺序，startswith
    public static FileTypeInfo[] typeInfos = new FileTypeInfo[]{
        new FileTypeInfo(FileCategory.Music, FileInfo.AUDIO_ICON, "audio"),
        new FileTypeInfo(FileCategory.Video, FileInfo.VIDEO_ICON, "video"),
        new FileTypeInfo(FileCategory.Picture, FileInfo.IMAGE_LOADING_ICON, "image"),
        new FileTypeInfo(FileCategory.Doc, FileInfo.DOC_ICON, "application", "text")
    };

    public FileTypeInfo(FileCategory fc, int defaultBg, String... mimes) {
        this.fc = fc;
        this.mimes = mimes;
        this.defaultBg = defaultBg;
    }
    
    public FileCategory fc;
    public int defaultBg;
    public String[] mimes;
    //文件管理器具备的
    public String mime;

    public FileTypeInfo(String ext) {
        this.ext = ext;
    }
    public String ext;
    public String category = "";
    public Uri uri;

    public static FileTypeInfo getTypeInfo(String path) {
        return getTypeInfo(new FileInfo(path));
    }

    public static FileTypeInfo getTypeInfo(FileInfo target) {
        FileTypeInfo typeInfo;
        String ext = null;
        String mime = null;
        Uri uri = null;
        String mimeprefix = "";
        if (target.getName() == null) {
            target.buildName();
        }
        ext = FileUtil.getRealExtension(target.getName());
        typeInfo = new FileTypeInfo(ext);
        mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
        if (mime == null) {
            return typeInfo;
        }
        if (mime.startsWith(Constants.CateContants.CATE_IMAGES)) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            mimeprefix = Constants.CateContants.CATE_IMAGES;
        } else if (mime.startsWith(Constants.CateContants.CATE_MUSIC)) {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            mimeprefix = Constants.CateContants.CATE_MUSIC;
        } else if (mime.startsWith(Constants.CateContants.CATE_VIDEO)) {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            mimeprefix = Constants.CateContants.CATE_VIDEO;
        } else {
            if (target.getName().trim().toLowerCase().endsWith(Constants.LOWERCASE_APK)) {
                mimeprefix = Constants.CateContants.CATE_PACKAGE;
                uri = MediaArgs.otherUri;
            } else if (mime.startsWith(Constants.MIMEContants.MIME_APPLICATION) || mime.startsWith(Constants.MIMEContants.MIME_TEXT)) {
                mimeprefix = Constants.CateContants.CATE_DOCS;
                uri = MediaArgs.otherUri;
            } else if (target.name.trim().toLowerCase().endsWith(Constants.LOWERCASE_LWT)) {
                mimeprefix = Constants.CateContants.CATE_THEME;
                uri = MediaArgs.otherUri;
            }
        }
        typeInfo.mime = mime;
        typeInfo.category = mimeprefix;
        typeInfo.uri = uri;

        return typeInfo;
    }
}
