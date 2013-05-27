/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.activities.views;

import com.lewa.filemanager.ds.database.MediaArgs;
import com.lewa.filemanager.ds.database.MimeSrc;
import android.view.View;
import com.lewa.filemanager.beans.ApkInfoUtil;
import com.lewa.filemanager.config.Config;
import com.lewa.filemanager.ds.uri.NavigationNode;
import com.lewa.filemanager.config.Constants;
import com.lewa.app.filemanager.ui.CommonActivity;
import com.lewa.filemanager.ds.database.SQLManager;
import com.lewa.filemanager.ds.database.CategoryItem;
import com.lewa.filemanager.beans.ApkInfo;
import com.lewa.filemanager.ds.database.CursorCategory;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.beans.MusicInfo;
import com.lewa.app.filemanager.ui.FileCursorActivity;
import com.lewa.filemanager.cpnt.adapter.CountAdapter;
import com.lewa.filemanager.cpnt.adapter.CursorItemSrc;
import com.lewa.base.adapter.ItemDataSrc;
import com.lewa.filemanager.actions.sort.Sort;

/**
 *
 * @author chenliang
 */
public class FileListViewHolder extends ViewHolder {

    FileCursorActivity fca;

    public FileListViewHolder(View view, CommonActivity activity) {
        super(view, activity);
        fca = (FileCursorActivity) activity;
    }

    public Object resetPolicy(CategoryItem cItem, Object sContent) {
        if (fca.policy.getPolicy() == -1) {
            fca.policy.setPolicy(Sort.SORT_POLICY_TITLE);
            fca.policy.setSeq(Sort.SORT_SEQ_ASC);
            sContent = fca.policy.transfer(cItem.categoryInfo.categorySign).getContent();
        }
        return sContent;
    }

    public Object resetPolicyBasedOnDisplayName(CategoryItem cItem, Object sContent) {
        if (fca.policy.getPolicy() == Sort.SORT_POLICY_DISPLAYNAME) {
            fca.policy.setPolicy(Sort.SORT_POLICY_TITLE);
            fca.policy.setSeq(Sort.SORT_SEQ_ASC);
            sContent = fca.policy.transfer(cItem.categoryInfo.categorySign).getContent();
        }
        return sContent;
    }

    public Object resetPolicyBasedOnType(CategoryItem cItem, Object sContent) {
        if (fca.policy.getPolicy() == Sort.SORT_POLICY_TYPE) {
            fca.policy.setPolicy(Sort.SORT_POLICY_TITLE);
            fca.policy.setSeq(Sort.SORT_SEQ_ASC);
            sContent = activity.policy.transfer(cItem.categoryInfo.categorySign).getContent();
            fca.sortMenuMarkInit = fca.SortMenuInAPK;
        }
        return sContent;
    }

    @Override
    public void start() {
        view = fca.buildList();
        fca.setContentView(view);
        fca.initNavBar();
        fca.initListView(CountAdapter.class);
        fca.initBottomBar();
        fca.listview.setAdapter(fca.adapter);
    }

    public void dataChanged() {
        dataChanged(false);
    }

    public void dataChanged(boolean refresh) {
        ApkInfoUtil.bindFlag = false;
        
        int waittime = 180;
        Class clazz = FileInfo.class;
        NavigationNode navigation = fca.navTool.navEntity.peek();
        CategoryItem cItem = MimeSrc.getCategoryRepository().get(navigation.producingSource);
        Sort sort = fca.policy.transfer(cItem.categoryInfo.categorySign);
        Object sContent = sort.getContent();
        CursorCategory cursorCategory = null;
        ItemDataSrc dataSrc = null;
        if (cItem != null && cItem instanceof CursorCategory) {
            cursorCategory = ((CursorCategory) cItem);
            if (cursorCategory.contentCursor != null) {
                cursorCategory.contentCursor.close();
                cursorCategory.contentCursor = null;
            }
            if (cItem.categoryInfo.categorySign.equals(Constants.CateContants.CATE_MUSIC)) {
                if (fca.policy.getPolicy() == Sort.SORT_POLICY_TITLE) {
                    fca.policy.setPolicy(Sort.SORT_POLICY_DISPLAYNAME);
                    fca.policy.setSeq(Sort.SORT_SEQ_ASC);
                    sContent = fca.policy.transfer(cItem.categoryInfo.categorySign).getContent();
                }
                if (Config.isLewaRom) {
                    cursorCategory.contentCursor = SQLManager.queryMusic(fca.context, sContent.toString());
                    fca.adapter.changeAdaptInfo(fca.getMusicAdaptInfo());
                    clazz = MusicInfo.class;
                } else {
                    cursorCategory.contentCursor = fca.context.getContentResolver().query(cursorCategory.uri, MediaArgs.LISTPROJECTION, null, null, sContent.toString());
                    fca.adapter.changeAdaptInfo(fca.getAdaptInfo());
                }
            } else if (cItem.categoryInfo.categorySign.equals(Constants.CateContants.CATE_IMAGES)) {
                if (fca.policy.getPolicy() == Sort.SORT_POLICY_ALBUM || fca.policy.getPolicy() == Sort.SORT_POLICY_ARTIST) {
                    fca.policy.setPolicy(Sort.SORT_POLICY_TITLE);
                    fca.policy.setSeq(Sort.SORT_SEQ_ASC);
                    sContent = fca.policy.transfer(cItem.categoryInfo.categorySign).getContent();
                    fca.sortMenuMarkInit = fca.SortMenuInMusic;
                } else {
                    sContent = resetPolicy(cItem, sContent);
                    fca.sortMenuMarkInit = -1;
                }
                if(Config.image_filter_size.isInfinite()) Config.image_filter_size = 0F;
                cursorCategory.contentCursor = fca.context.getContentResolver().query(cursorCategory.uri, new String[]{SQLManager.getImagesQuerySql(Config.image_filter_size, sContent.toString()) + "--"}, null, null, null);
                fca.adapter.changeAdaptInfo(fca.getAdaptInfo());
            } else if (cItem.categoryInfo.categorySign.equals(Constants.CateContants.CATE_VIDEO)) {
                if (fca.policy.getPolicy() == Sort.SORT_POLICY_ALBUM || fca.policy.getPolicy() == Sort.SORT_POLICY_ARTIST) {
                    fca.policy.setPolicy(Sort.SORT_POLICY_TITLE);
                    fca.policy.setSeq(Sort.SORT_SEQ_ASC);
                    sContent = fca.policy.transfer(cItem.categoryInfo.categorySign).getContent();
                    fca.sortMenuMarkInit = fca.SortMenuInMusic;
                } else {
                    sContent = resetPolicy(cItem, sContent);
                    fca.sortMenuMarkInit = -1;
                }
                cursorCategory.contentCursor = fca.context.getContentResolver().query(cursorCategory.uri, MediaArgs.LISTPROJECTION, null, null, sContent.toString());
                fca.adapter.changeAdaptInfo(fca.getAdaptInfo());
            } else if (cItem.categoryInfo.categorySign.equals(Constants.CateContants.CATE_DOCS)) {
                if (fca.policy.getPolicy() == Sort.SORT_POLICY_ALBUM || fca.policy.getPolicy() == Sort.SORT_POLICY_ARTIST) {
                    fca.policy.setPolicy(Sort.SORT_POLICY_TITLE);
                    fca.policy.setSeq(Sort.SORT_SEQ_ASC);
                    sContent = fca.policy.transfer(cItem.categoryInfo.categorySign).getContent();
                    fca.sortMenuMarkInit = fca.SortMenuInMusic;
                } else {
                    sContent = resetPolicyBasedOnDisplayName(cItem, sContent);
                    sContent = resetPolicy(cItem, sContent);
                    fca.sortMenuMarkInit = -1;
                }
                cursorCategory.contentCursor = MediaArgs.listQuery4NoMedia(fca.context, cursorCategory.uri, false, "application/vnd.android.package-archive", sContent.toString());
                fca.adapter.changeAdaptInfo(fca.getAdaptInfo());

            } else if (cItem.categoryInfo.categorySign.equals(Constants.CateContants.CATE_PACKAGE)) {
                if (fca.policy.getPolicy() == Sort.SORT_POLICY_ALBUM || fca.policy.getPolicy() == Sort.SORT_POLICY_ARTIST) {
                    fca.policy.setPolicy(Sort.SORT_POLICY_TITLE);
                    fca.policy.setSeq(Sort.SORT_SEQ_ASC);
                    sContent = fca.policy.transfer(cItem.categoryInfo.categorySign).getContent();
                    fca.sortMenuMarkInit = fca.SortMenuInMusic;
                } else {
                    sContent = resetPolicy(cItem, sContent);
                    sContent = resetPolicyBasedOnType(cItem, sContent);
                    sContent = resetPolicyBasedOnDisplayName(cItem, sContent);
                }
                cursorCategory.contentCursor = MediaArgs.listQuery4NoMedia(fca.context, cursorCategory.uri, true, "application/vnd.android.package-archive", sContent.toString());
                fca.adapter.changeAdaptInfo(fca.getApkAdaptInfo());
                clazz = ApkInfo.class;
                waittime = 400;
            } else if (cItem.categoryInfo.categorySign.equals(Constants.CateContants.CATE_THEME)) {
                if (fca.policy.getPolicy() == Sort.SORT_POLICY_ALBUM || fca.policy.getPolicy() == Sort.SORT_POLICY_ARTIST) {
                    fca.policy.setPolicy(Sort.SORT_POLICY_TITLE);
                    fca.policy.setSeq(Sort.SORT_SEQ_ASC);
                    sContent = fca.policy.transfer(cItem.categoryInfo.categorySign).getContent();
                    fca.sortMenuMarkInit = fca.SortMenuInMusic;
                } else {
                    sContent = resetPolicyBasedOnDisplayName(cItem, sContent);
                    sContent = resetPolicy(cItem, sContent);
                    fca.sortMenuMarkInit = -1;
                }
                cursorCategory.contentCursor = MediaArgs.listQuery4NoMedia(fca.context, cursorCategory.uri, true, "lewa/theme", sContent.toString());
                fca.adapter.changeAdaptInfo(fca.getAdaptInfo());
            }
            dataSrc = new CursorItemSrc(fca.context, cursorCategory.contentCursor, fca.datamap, clazz);
            (fca.adapter).setItemDataSrc(dataSrc);
            fca.adapter.reinitSelectedAllBck(dataSrc.getCount());
            if (!refresh) {
                fca.listview.setAdapter(fca.adapter);
            }
            fca.adapter.notifyDataSetChanged();
            fca.adapter.initPics(0, waittime);           
        }

    }

    public void rebind() {
        fca.setupAfterUILoad(fca.status, fca.navigation, fca.newPos, fca.MODE_NEW, fca.filesInfo);
        fca.navigationBarSetup(fca.newPos, fca.MODE_NEW, fca.navigation);
    }

    public void refresh() {
        try {
            if(fca.adapter==null||fca.listview==null){
                return;
            }
            Integer pos = fca.adapter.getViewContentMap().get(fca.listview.getChildAt(0));
            dataChanged(true);
            fca.setupAfterUILoad(fca.status, fca.navigation, pos == null ? 0 : pos, fca.MODE_CURR, fca.filesInfo);
            fca.navigationBarSetup(fca.navTool.navEntity.peek().defaultPosition, fca.MODE_CURR, fca.navigation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
