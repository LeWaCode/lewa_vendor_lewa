package com.lewa.filemanager.ds.database;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;

import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import com.lewa.app.filemanager.R;
import com.lewa.base.Logs;
import com.lewa.filemanager.config.Config;
import com.lewa.filemanager.beans.FileUtil;
import com.lewa.filemanager.config.Constants;
import java.util.List;

public class MimeSrc {

    public static Map<String, CategoryItem> categoryRepository = new HashMap<String, CategoryItem>();
    public static final String SIZE_Empty = "0 B";

    public static CategoryItem getEmptyCategory(Context context, String cDisplayName, Drawable dIcon) {
        CategoryInfo cinfo = new CategoryInfo();
        cinfo.size = SIZE_Empty;
        cinfo.displayName = cDisplayName;
        cinfo.dIcon = dIcon;

        CategoryItem categoryItem = new CategoryItem();
        categoryItem.categoryInfo = cinfo;
        return categoryItem;
    }

    public static Map<String, CategoryItem> getCategoryRepository() {
        return categoryRepository;
    }

    public static Map<String, CategoryItem> initWholeCategories(Context context, String certainCategory, List<CategoryInfo> category) {
        categoryRepository = new HashMap<String, CategoryItem>();
        makeMediaCursorCategory(certainCategory, category, new CategoryInfo(Constants.CateContants.CATE_MUSIC, context.getString(R.string.music), context.getResources().getDrawable(R.drawable.music)), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, context);
        makeMediaCursorCategory(certainCategory, category, new CategoryInfo(Constants.CateContants.CATE_IMAGES, context.getString(R.string.image), context.getResources().getDrawable(R.drawable.picture)), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, context);
        makeMediaCursorCategory(certainCategory, category, new CategoryInfo(Constants.CateContants.CATE_VIDEO, context.getString(R.string.video), context.getResources().getDrawable(R.drawable.vedio)), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, context);
        makeMediaCursorCategory(certainCategory, category, new CategoryInfo(Constants.CateContants.CATE_DOCS, context.getString(R.string.doc), context.getResources().getDrawable(R.drawable.doc)), MediaArgs.otherUri, context);
        makeMediaCursorCategory(certainCategory, category, new CategoryInfo(Constants.CateContants.CATE_PACKAGE, context.getString(R.string.app), context.getResources().getDrawable(R.drawable.apk)), MediaArgs.otherUri, context);
        if (Config.isLewaRom) {
            makeMediaCursorCategory(certainCategory, category, new CategoryInfo(Constants.CateContants.CATE_THEME, context.getString(R.string.theme), context.getResources().getDrawable(R.drawable.theme)), MediaArgs.otherUri, context);
        }
        return categoryRepository;
    }

    private static void makeMediaCursorCategory(String certainCategory, List<CategoryInfo> category, CategoryInfo categoryInfo, Uri uri, Context context) throws NotFoundException {
        CursorCategory item = new CursorCategory();
        item.categoryInfo = categoryInfo;
        item.uri = uri;
        categoryRepository.put(categoryInfo.categorySign, item);
        if (certainCategory == null || certainCategory.equals(categoryInfo.categorySign)) {
            if (categoryRepository.containsKey(certainCategory)) {
                category.add(item.categoryInfo);
            }
        }
    }

    public static void recountCategoryNum(Context context) {
        try{
        MimeSrc.deleAllEmptyPathData(context);
        CategoryItem[] items = new CategoryItem[categoryRepository.values().size()];
        categoryRepository.values().<CategoryItem>toArray(items);
        CursorCategory cc;
        for (CategoryItem item : items) {
            if (item instanceof CursorCategory) {
                cc = (CursorCategory) item;
                if (cc != null && cc.countCursor != null) {
                    cc.countCursor.close();
                    cc.countCursor = null;
                }
                queryDBCategories(cc.categoryInfo, cc, context, cc.uri);
                if (cc.countCursor == null || cc.countCursor.getCount() == 0) {
                    ((CursorCategory) item).categoryInfo.count = "0";
                    return;
                }
                if (cc.countCursor != null) {
                    cc.countCursor.moveToFirst();
                    ((CursorCategory) item).categoryInfo.count = cc.countCursor.getInt(0) + "";
                    ((CursorCategory) item).categoryInfo.size = FileUtil.formatSize(cc.countCursor.getLong(1));
                    Logs.i("----------------cc"+cc);
                    ((CursorCategory) item).categoryInfo.length = ((Long) cc.countCursor.getLong(1)).floatValue();
                }
            }
        }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void queryDBCategories(CategoryInfo categoryInfo, CursorCategory item, Context context, Uri uri) {
        if (Constants.CateContants.CATE_PACKAGE.equals(categoryInfo.categorySign)) {
            item.countCursor = MediaArgs.countQuery4NoMedia(context, uri, true, MediaArgs.APK_MIME);
        } else if (Constants.CateContants.CATE_DOCS.equals(categoryInfo.categorySign)) {
            item.countCursor = MediaArgs.countQuery4NoMedia(context, uri, false, null);
        } else if (Constants.CateContants.CATE_THEME.equals(categoryInfo.categorySign)) {
            item.countCursor = MediaArgs.countQuery4NoMedia(context, uri, true, MediaArgs.THEME_MIME);
        } else {
            item.countCursor = context.getContentResolver().query(uri, MediaArgs.CATEGORYPROJECTION, null, null, null);
        }
    }

    private static void putCategory(CategoryInfo categoryInfo) {
        CategoryItem item = new CategoryItem();
        item.categoryInfo = categoryInfo;
        categoryRepository.put(categoryInfo.categorySign, item);
    }

    public static void deleAllEmptyPathDataFromDB(Context context, Uri uri) {
        try {
            context.getContentResolver().delete(uri, "_data is null or _data like ''", null);
        } catch (Exception e) {
            if (e.getMessage().contains("Unknown URI")) {
                return;
            }
        }
    }

    public static void deleAllEmptyPathData(Context context) {
        try {
            deleAllEmptyPathDataFromDB(context, Audio.Media.EXTERNAL_CONTENT_URI);
            deleAllEmptyPathDataFromDB(context, Images.Media.EXTERNAL_CONTENT_URI);
            deleAllEmptyPathDataFromDB(context, Video.Media.EXTERNAL_CONTENT_URI);
            deleAllEmptyPathDataFromDB(context, MediaArgs.otherUri);
        } catch (Exception e) {
        }
    }
}
