/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.database.copersynch;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import com.lewa.filemanager.ds.database.MediaArgs;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.beans.FileUtil;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.images.MimeTypeMap;

/**
 *
 * @author Administrator
 */
public class DatasrcDelete {

    public static void recursiveUpdateDel(FileInfo info, Context context) {
        String ext;
        String mime;
        String mimeprefix = null;
        Uri uri = null;
        ext = FileUtil.getRealExtension(info.getName());

        mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
        if (mime == null) {
            return;
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
            if (info.getName().trim().toLowerCase().endsWith(Constants.LOWERCASE_APK)) {
                mimeprefix = Constants.CateContants.CATE_PACKAGE;
                uri = MediaArgs.otherUri;
            } else if (mime.startsWith(Constants.MIMEContants.MIME_APPLICATION) || mime.startsWith(Constants.MIMEContants.MIME_TEXT)) {
                mimeprefix = Constants.CateContants.CATE_DOCS;
                uri = MediaArgs.otherUri;
            } else if (info.name.trim().toLowerCase().endsWith(Constants.LOWERCASE_LWT)) {
                mimeprefix = Constants.CateContants.CATE_THEME;
                uri = MediaArgs.otherUri;
            }

        }
        if (uri != null) {
            context.getContentResolver().delete(uri, MediaStore.Audio.Media.DATA + " like ?", new String[]{info.getPath()});
        }
    }

}
