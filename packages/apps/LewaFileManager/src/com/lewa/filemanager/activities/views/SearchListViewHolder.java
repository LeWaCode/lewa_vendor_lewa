/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.activities.views;

import android.database.Cursor;
import android.graphics.Color;
import android.provider.MediaStore.Audio;
import android.view.View;
import com.lewa.filemanager.ds.database.MediaArgs;
import com.lewa.filemanager.ds.database.SQLManager;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.actions.sort.DBSort;
import com.lewa.app.filemanager.ui.SearchActivity;
import com.lewa.filemanager.actions.sort.Sort;
import com.lewa.filemanager.cpnt.adapter.CountAdapter;
import com.lewa.filemanager.cpnt.adapter.SearchAdapter;
import com.lewa.filemanager.cpnt.adapter.CursorItemSrc;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.Logs;

/**
 *
 * @author chenliang
 */
public class SearchListViewHolder extends ViewHolder {

    SearchActivity searchActivity;
    Cursor mediaCursor = null;
    Cursor filesCursor = null;
    CursorItemSrc dataSrc;

    public SearchListViewHolder(View view, SearchActivity activity) {
        super(view, activity);
        searchActivity = activity;
    }

    @Override
    public synchronized void dataChanged() {
        Sort sort = searchActivity.policy.transfer(null);
        String sContent = DBSort.ORDERBY + sort.getContent();
        Logs.i("inputkeyWords " + searchActivity.objectWords);
        String mediaSql = SQLManager.getSearchPart_Media_Sql(searchActivity.objectWords, sContent);
        Logs.i("inputkeyWords " + mediaSql);
        String filesSql = SQLManager.getSearchPart_Files_Sql(searchActivity.objectWords, sContent);
        if (dataSrc != null) {
            dataSrc.clear();
        }
        if (mediaCursor != null) {
            mediaCursor.close();
            mediaCursor = null;
        }
        if (filesCursor != null) {
            filesCursor.close();
            filesCursor = null;
        }
        mediaCursor = searchActivity.getContentResolver().query(Audio.Media.EXTERNAL_CONTENT_URI, new String[]{mediaSql + "--"}, null, null, null);
        filesCursor = searchActivity.getContentResolver().query(MediaArgs.otherUri, new String[]{filesSql + "--"}, null, null, null);
        dataSrc = new CursorItemSrc(searchActivity.context, mediaCursor, searchActivity.datamap, FileInfo.class, null);
        (searchActivity.adapter).setItemDataSrc(dataSrc);
        (searchActivity.adapter).getItemDataSrc().swapCursor(filesCursor);
        searchActivity.adapter.reinitSelectedAllBck(dataSrc.getCount());
        ((SearchAdapter)searchActivity.adapter).switchKeyWords(searchActivity.objectWords);
    }

    @Override
    public void refresh() {
    }

    @Override
    public void start() {
        searchActivity.setContentView(searchActivity.buildList());
        searchActivity.initNavBar();
        searchActivity.initListView(CountAdapter.class);
        searchActivity.initBottomBar();
        view = searchActivity.listview;
        searchActivity.buildAdapter(SearchAdapter.class, searchActivity.getAdaptInfo());
        ((SearchAdapter)searchActivity.adapter).setHighlightInfo(searchActivity.objectWords, new String[]{Constants.FieldConstants.NAME}, Color.RED);
        searchActivity.listview.setAdapter(searchActivity.adapter);
        searchActivity.adapter.initPics(0,150);
    }

    public synchronized void rebind() {
        if (((Cursor) searchActivity.adapter.getItemDataSrc().getContent()) != null && ((Cursor) searchActivity.adapter.getItemDataSrc().getContent()).isClosed()) {
            return;
        }
//        searchActivity.setListViewImageLoadMode(searchActivity.listview, ThumbnailLoader.MODE_DIRECT);
        searchActivity.adapter.notifyDataSetChanged();
    }
}
