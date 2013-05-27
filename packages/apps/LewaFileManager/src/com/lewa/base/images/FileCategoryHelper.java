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
package com.lewa.base.images;

import android.content.Context;

import com.lewa.filemanager.beans.FileInfo;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;

public class FileCategoryHelper {

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_PATH = 1;
    public static final int COLUMN_SIZE = 2;
    public static final int COLUMN_DATE = 3;

    public enum FileCategory {

        All, Music, Video, Picture, Theme, Doc, Zip, Apk, Custom, NoType, Favorite, NoRecorded,Folder
    }
    private static String APK_EXT = "apk";
    private static String THEME_EXT = "lwt";
    private static String[] ZIP_EXTS = new String[]{
        "zip", "rar"
    };
    public static HashMap<FileCategory, FilenameExtFilter> filters = new HashMap<FileCategory, FilenameExtFilter>();
    public static HashMap<FileCategory, Integer> categoryNames = new HashMap<FileCategory, Integer>();

    public static FileCategory[] sCategories = new FileCategory[]{
        FileCategory.Music, FileCategory.Video, FileCategory.Picture, FileCategory.Theme,
        FileCategory.Doc, FileCategory.Zip, FileCategory.Apk, FileCategory.NoType
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

    public static FileTypeInfo getCategoryFromPath(String path) {
        if(new File(path).isDirectory()){
            return new FileTypeInfo(FileCategory.Folder, FileInfo.DIR_RES, null);
        }
        int dotPosition = path == null ? -1 : path.lastIndexOf('.');
        if (dotPosition > -1) {
            String ext = path.substring(dotPosition + 1);
            if (ext.equalsIgnoreCase(APK_EXT)) {
                return new FileTypeInfo(FileCategory.Apk, FileInfo.PACKAGE_ICON, null);
            }
            if (ext.equalsIgnoreCase(THEME_EXT)) {
                return new FileTypeInfo(FileCategory.Theme, FileInfo.THEME_ICON, null);
            }
        }
        return MimeUtil.isTypeFromMime(path, FileTypeInfo.typeInfos);
    }
}
