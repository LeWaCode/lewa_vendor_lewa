package com.lewa.search.db;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.lewa.search.bean.VideoFileInfo;
import com.lewa.search.system.Constants;
import com.lewa.search.system.SoftCache;
import com.lewa.search.util.SearchUtil;

/**
 * This class defines a method for searching video files from database.
 * 
 * @author wangfan
 * @version 2012.07.04
 */

public class VideoDB {
    // uri for searching video files in database
    private static Uri uri = Constants.URI_VIDEO;
    // this thumbnail caches in softCache, load when system starts
    private static Drawable thumbnail;
    // this list contains results for return
    public static List<VideoFileInfo> list = new ArrayList<VideoFileInfo>();

    /**
     * This method search matched video files
     * 
     * @param key
     *            key in searching video files
     * @param sortMode
     *            defines how to sort the list
     */
    public static List<VideoFileInfo> searchVideos(String key, String sortMode) {
        // load thumbnail
        thumbnail = SoftCache.videoThumb;

        // clear list before each time of searching
        list.clear();
        // this projection tells which column are to get from database
        String[] projection = new String[] { "_display_name", "_data" };

        // this selectinIds records which columns have searching clauses
        // in this case, index "PROJECTION_ZERO"("_display_name") has searching
        // clause
        int[] selectionIds;

        selectionIds = new int[] { SearchUtil.PROJECTION_ZERO };

        // get selection
        String selection = SearchUtil.getSelection(projection, selectionIds,
                "OR", SearchUtil.SEARCH_MODE_BLURRED);
        // get selection arguments
        String[] selectionArgs = SearchUtil.getMultipleSelectionArgs(key,
                selectionIds.length, SearchUtil.SEARCH_MODE_BLURRED);

        // search matched results from database
        Cursor cur = DBUtil.searchByKey(uri, projection, selection,
                selectionArgs, sortMode);

        // create an empty item, it is a temp item
        VideoFileInfo normalItem;
        String text;
        String title;
        String filePath;

        // load thumbnail only at the first item
        boolean loadThumb = true;
        if (cur != null && cur.moveToFirst()) {
            int index_Title = cur.getColumnIndex("_display_name");
            int index_Text = cur.getColumnIndex("_data");

            do {
                // get file information from search results
                title = cur.getString(index_Title);
                filePath = cur.getString(index_Text);
                text = filePath;

                if (loadThumb == true) // create new item with thumbnail
                {
                    if ((title != null && title.toLowerCase().contains(
                            key.toLowerCase()))
                            || (text != null && text.toLowerCase().contains(
                                    key.toLowerCase()))) {
                        normalItem = new VideoFileInfo(title, text, thumbnail,
                                filePath, Constants.CLASS_VIDEO);
                        // only the first item has thumbnail
                        loadThumb = false;

                        list.add(normalItem);
                    }
                } else // create new item without thumbnail
                {
                    if ((title != null && title.toLowerCase().contains(key.toLowerCase()))
                            || (text != null && text.toLowerCase().contains(key.toLowerCase()))) {
                        normalItem = new VideoFileInfo(title, text, null,
                                filePath, Constants.CLASS_VIDEO);

                        list.add(normalItem);
                    }
                }

            } while (cur.moveToNext());
        }
        if (cur != null) {
            cur.close();
        }

        return list;
    }
}
