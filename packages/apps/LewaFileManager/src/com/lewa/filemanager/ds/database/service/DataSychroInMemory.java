/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.database.service;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import com.lewa.filemanager.ds.database.MediaArgs;
import com.lewa.filemanager.config.Constants;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class DataSychroInMemory {

    public static final String[] PROJECTION = new String[]{MediaArgs.PATH, "_id"};
    public Map<String, FileImage> memoryImage = new HashMap<String, FileImage>();

    public Map<String, FileImage> getMemoryImage() {
        return memoryImage;
    }

    public DataSychroInMemory() {
        memoryImage.put(Constants.CateContants.CATE_IMAGES, new FileImage(Images.Media.EXTERNAL_CONTENT_URI));
        memoryImage.put(Constants.CateContants.CATE_MUSIC, new FileImage(Audio.Media.EXTERNAL_CONTENT_URI));
        memoryImage.put(Constants.CateContants.CATE_VIDEO, new FileImage(Video.Media.EXTERNAL_CONTENT_URI));
        memoryImage.put(Constants.CateContants.CATE_PACKAGE, new FileImage(MediaArgs.otherUri));
        memoryImage.put(Constants.CateContants.CATE_DOCS, new FileImage(MediaArgs.otherUri));
        memoryImage.put(Constants.CateContants.CATE_THEME, new FileImage(MediaArgs.otherUri));
    }

    public void receiveDBImage(Context context, String categoryName) {
        if (categoryName == null || categoryName.equals(Constants.CateContants.CATE_IMAGES)) {
            getContentInjected(queryImage(context, Images.Media.EXTERNAL_CONTENT_URI), memoryImage.get(Constants.CateContants.CATE_IMAGES));
        }
        if (categoryName == null || categoryName.equals(Constants.CateContants.CATE_MUSIC)) {
            getContentInjected(queryImage(context, Audio.Media.EXTERNAL_CONTENT_URI), memoryImage.get(Constants.CateContants.CATE_MUSIC));
        }
        if (categoryName == null || categoryName.equals(Constants.CateContants.CATE_VIDEO)) {
            getContentInjected(queryImage(context, Video.Media.EXTERNAL_CONTENT_URI), memoryImage.get(Constants.CateContants.CATE_VIDEO));
        }
        if (categoryName == null || categoryName.equals(Constants.CateContants.CATE_DOCS)) {
            getContentInjected(queryOthersImage(context, MediaArgs.otherUri, false, MediaArgs.APK_MIME,MediaArgs.THEME_MIME), memoryImage.get(Constants.CateContants.CATE_DOCS));
        }
        if (categoryName == null || categoryName.equals(Constants.CateContants.CATE_PACKAGE)) {
            getContentInjected(queryOthersImage(context, MediaArgs.otherUri, true, MediaArgs.APK_MIME), memoryImage.get(Constants.CateContants.CATE_PACKAGE));
        }
        if (categoryName == null || categoryName.equals(Constants.CateContants.CATE_THEME)) {
            getContentInjected(queryOthersImage(context, MediaArgs.otherUri, true, MediaArgs.THEME_MIME), memoryImage.get(Constants.CateContants.CATE_THEME));
        }
    }

    public Cursor queryImage(Context context, Uri uri) {
        return context.getContentResolver().query(uri, new String[]{MediaArgs.PATH, "_id"}, null, null, "_data asc");
    }

    public Cursor queryOthersImage(Context context, Uri uri, boolean like, String... mimeValue) {
        return MediaArgs.query4NoMedia(context, uri, PROJECTION, like, "_data asc", mimeValue);
    }

    public void getContentInjected(Cursor cursor, FileImage image) {
        String path;
        Integer id;
        while (cursor.moveToNext()) {
            path = cursor.getString(0);
            id = cursor.getInt(1);
            if (path == null || "".equals(path.trim())) {
                image.errorRecords.put(id, path);
            } else {
                image.records.put(path, id);
            }
        }
    }

    public void clearNoUseData(Context context) {
        for (FileImage image : memoryImage.values()) {
            for (Integer id : image.errorRecords.keySet()) {
                FileInfoDBManager.delete(context, image.operUri, id);
            }
            for (Integer id : image.records.values()) {
                FileInfoDBManager.delete(context, image.operUri, id);
            }
        }
    }
}
