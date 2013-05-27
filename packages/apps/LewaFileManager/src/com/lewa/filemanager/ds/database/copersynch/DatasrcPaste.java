/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.database.copersynch;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import com.lewa.filemanager.ds.util.ContentValuesUtil;
import com.lewa.base.images.FileTypeInfo;
import com.lewa.filemanager.ds.database.MediaArgs;
import com.lewa.filemanager.actions.OperationUtil;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.beans.FileUtil;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.images.MimeTypeMap;
import com.lewa.filemanager.util.StatusCheckUtil;

/**
 *
 * @author Administrator
 */
public class DatasrcPaste {

    public static String updateCutCopyOnSingleFile(FileInfo info, FileInfo target, FileInfo src, Context context) throws IllegalStateException {
        String ext;
        String mime;
        String mimePrefix = null;
        Uri uri = null;
        info.buildName();
        ext = FileUtil.getRealExtension(info.getName());
        mime = ext == null || ext.trim().equals("") ? null : MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
        if (mime == null) {
            return null;
        }
        if (mime.startsWith(Constants.CateContants.CATE_IMAGES)) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            mimePrefix = Constants.CateContants.CATE_IMAGES;
        } else if (mime.startsWith(Constants.CateContants.CATE_MUSIC)) {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            mimePrefix = Constants.CateContants.CATE_MUSIC;
        } else if (mime.startsWith(Constants.CateContants.CATE_VIDEO)) {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            mimePrefix = Constants.CateContants.CATE_VIDEO;
        } else {
            if (target.getName().trim().toLowerCase().endsWith(Constants.LOWERCASE_APK)) {
                mimePrefix = Constants.CateContants.CATE_PACKAGE;
                uri = MediaArgs.otherUri;
            } else if (mime.startsWith(Constants.MIMEContants.MIME_APPLICATION) || mime.startsWith(Constants.MIMEContants.MIME_TEXT)) {
                mimePrefix = Constants.CateContants.CATE_DOCS;
                uri = MediaArgs.otherUri;
            } else if (target.name.trim().toLowerCase().endsWith(Constants.LOWERCASE_LWT)) {
                mimePrefix = Constants.CateContants.CATE_THEME;
                uri = MediaArgs.otherUri;
            }

        }
        if (uri != null) {
            ContentValues cv = ContentValuesUtil.constructContentValues(info, FileTypeInfo.getTypeInfo(info));
            if (OperationUtil.getOperType() == OperationUtil.OPER_TYPE_COPY) {
                if (info.overrideFlag == null) {
                    if (uri.equals(Audio.Media.EXTERNAL_CONTENT_URI)) {
                        StatusCheckUtil.mediaUpdating++;
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(info.getFile().getParentFile())));
                        return null;
                    }
                    context.getContentResolver().insert(uri, cv);
                } else {
                    context.getContentResolver().update(uri, cv, MediaStore.Audio.Media.DATA + " like ?", new String[]{info.getPath()});
                }
            } else if (OperationUtil.getOperType() == OperationUtil.OPER_TYPE_CUT) {

                if (info.overrideFlag != null && info.overrideFlag) {
                    if (uri.equals(Audio.Media.EXTERNAL_CONTENT_URI)) {
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + src.path)));
                    } else {
                        context.getContentResolver().delete(uri, MediaStore.Audio.Media.DATA + " like ?", new String[]{src.getPath()});
                    }
                    context.getContentResolver().update(uri, cv, MediaStore.Audio.Media.DATA + " like ?", new String[]{info.getPath()});
                } else {
                    context.getContentResolver().update(uri, cv, MediaStore.Audio.Media.DATA + " like ?", new String[]{src.getPath()});
                }
            }
        }
        return mimePrefix;
    }
}
