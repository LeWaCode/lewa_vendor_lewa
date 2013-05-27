/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lewa.face.loadimage;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;

import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.util.Log;

import com.lewa.face.loadimage.MediaFile.MediaFileType;

import java.io.FilenameFilter;
import java.util.HashMap;

public class FileCategoryHelper {
    public static final int COLUMN_ID = 0;

    public static final int COLUMN_PATH = 1;

    public static final int COLUMN_SIZE = 2;

    public static final int COLUMN_DATE = 3;

    private static final String LOG_TAG = "FileCategoryHelper";

    public enum FileCategory {
        All, Music, Video, Picture, Theme, Doc, Zip, Apk, Custom, Other, Favorite
    }

    private static String APK_EXT = "apk";
    private static String THEME_EXT = "mtz";
    private static String[] ZIP_EXTS  = new String[] {
            "zip", "rar"
    };

    public static HashMap<FileCategory, FilenameExtFilter> filters = new HashMap<FileCategory, FilenameExtFilter>();

    public static HashMap<FileCategory, Integer> categoryNames = new HashMap<FileCategory, Integer>();

    static {
//        categoryNames.put(FileCategory.All, R.string.category_all);
//        categoryNames.put(FileCategory.Music, R.string.category_music);
//        categoryNames.put(FileCategory.Video, R.string.category_video);
//        categoryNames.put(FileCategory.Picture, R.string.category_picture);
//        categoryNames.put(FileCategory.Theme, R.string.category_theme);
//        categoryNames.put(FileCategory.Doc, R.string.category_document);
//        categoryNames.put(FileCategory.Zip, R.string.category_zip);
//        categoryNames.put(FileCategory.Apk, R.string.category_apk);
//        categoryNames.put(FileCategory.Other, R.string.category_other);
//        categoryNames.put(FileCategory.Favorite, R.string.category_favorite);
    }

    public static FileCategory[] sCategories = new FileCategory[] {
            FileCategory.Music, FileCategory.Video, FileCategory.Picture, FileCategory.Theme,
            FileCategory.Doc, FileCategory.Zip, FileCategory.Apk, FileCategory.Other
    };

    private FileCategory mCategory;

    private Context mContext;

    public FileCategoryHelper(Context context) {
        mContext = context;

        mCategory = FileCategory.All;
    }

    public FileCategory getCurCategory() {
        return mCategory;
    }

    public void setCurCategory(FileCategory c) {
        mCategory = c;
    }

    public int getCurCategoryNameResId() {
        return categoryNames.get(mCategory);
    }

    public void setCustomCategory(String[] exts) {
        mCategory = FileCategory.Custom;
        if (filters.containsKey(FileCategory.Custom)) {
            filters.remove(FileCategory.Custom);
        }

        filters.put(FileCategory.Custom, new FilenameExtFilter(exts));
    }

    public FilenameFilter getFilter() {
        return filters.get(mCategory);
    }

    private HashMap<FileCategory, CategoryInfo> mCategoryInfo = new HashMap<FileCategory, CategoryInfo>();

    public HashMap<FileCategory, CategoryInfo> getCategoryInfos() {
        return mCategoryInfo;
    }

    public CategoryInfo getCategoryInfo(FileCategory fc) {
        if (mCategoryInfo.containsKey(fc)) {
            return mCategoryInfo.get(fc);
        } else {
            CategoryInfo info = new CategoryInfo();
            mCategoryInfo.put(fc, info);
            return info;
        }
    }

    public class CategoryInfo {
        public long count;

        public long size;
    }

    private void setCategoryInfo(FileCategory fc, long count, long size) {
        CategoryInfo info = mCategoryInfo.get(fc);
        if (info == null) {
            info = new CategoryInfo();
            mCategoryInfo.put(fc, info);
        }
        info.count = count;
        info.size = size;
    }


    private String buildDocSelection() {
//        StringBuilder selection = new StringBuilder();
//        Iterator<String> iter = Util.sDocMimeTypesSet.iterator();
//        while(iter.hasNext()) {
//            selection.append("(" + FileColumns.MIME_TYPE + "=='" + iter.next() + "') OR ");
//        }
//        return  selection.substring(0, selection.lastIndexOf(")") + 1);
        return null;
    }

    private String buildSelectionByCategory(FileCategory cat) {
        String selection = null;
        switch (cat) {
            case Theme:
                selection = "_data" + " LIKE '%.mtz'";
                break;
            case Doc:
                selection = buildDocSelection();
                break;
//            case Zip:
//                selection = "(" + "mimetype" + " == '" + Util.sZipFileMimeType + "')";
//                break;
            case Apk:
                selection = "_data" + " LIKE '%.apk'";
                break;
            default:
                selection = null;
        }
        return selection;
    }

    private Uri getContentUriByCategory(FileCategory cat) {
        Uri uri;
        String volumeName = "external";
        switch(cat) {
            case Theme:
            case Doc:
            case Zip:
//            case Apk:
//                uri = Files.getContentUri(volumeName);
//                break;
            case Music:
                uri = Audio.Media.getContentUri(volumeName);
                break;
            case Video:
                uri = Video.Media.getContentUri(volumeName);
                break;
            case Picture:
                uri = Images.Media.getContentUri(volumeName);
                break;
           default:
               uri = null;
        }
        return uri;
    }


    private boolean refreshMediaCategory(FileCategory fc, Uri uri) {
        String[] columns = new String[] {
                "COUNT(*)", "SUM(_size)"
        };
        Cursor c = mContext.getContentResolver().query(uri, columns, buildSelectionByCategory(fc), null, null);
        if (c == null) {
            Log.e(LOG_TAG, "fail to query uri:" + uri);
            return false;
        }

        if (c.moveToNext()) {
            setCategoryInfo(fc, c.getLong(0), c.getLong(1));
            Log.v(LOG_TAG, "Retrieved " + fc.name() + " info >>> count:" + c.getLong(0) + " size:" + c.getLong(1));
            c.close();
            return true;
        }

        return false;
    }

    public static FileCategory getCategoryFromPath(String path) {
        MediaFileType type = MediaFile.getFileType(path);
        if (type != null) {
            if (MediaFile.isAudioFileType(type.fileType)) return FileCategory.Music;
            if (MediaFile.isVideoFileType(type.fileType)) return FileCategory.Video;
            if (MediaFile.isImageFileType(type.fileType)) return FileCategory.Picture;
//            if (Util.sDocMimeTypesSet.contains(type.mimeType)) return FileCategory.Doc;
        }

        int dotPosition = path.lastIndexOf('.');
        if (dotPosition < 0) {
            return FileCategory.Other;
        }

        String ext = path.substring(dotPosition + 1);
        if (ext.equalsIgnoreCase(APK_EXT)) {
            return FileCategory.Apk;
        }
        if (ext.equalsIgnoreCase(THEME_EXT)) {
            return FileCategory.Theme;
        }

        if (matchExts(ext, ZIP_EXTS)) {
            return FileCategory.Zip;
        }

        return FileCategory.Other;
    }

    private static boolean matchExts(String ext, String[] exts) {
        for (String ex : exts) {
            if (ex.equalsIgnoreCase(ext))
                return true;
        }
        return false;
    }
}
