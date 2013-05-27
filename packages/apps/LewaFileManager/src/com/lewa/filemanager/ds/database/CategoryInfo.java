/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.database;

import android.graphics.drawable.Drawable;

/**
 *
 * @author Administrator
 */
public class CategoryInfo {
    public static final String CATEGORY_ICON = "dIcon";
    public static final String CATEGORY_NAME = "displayName";
    public static final String CATEGORY_COUNT = "count";
    public static final String CATEGORY_SIZE = "size";
    public static final String CATEGORY_FLAG = "mimetypePrefix";
    public Float length;
    public CategoryInfo() {
        super();
    }
    public String count;
    public Drawable dIcon;
    public String displayName;
    public String categorySign;
    public String size;

    public CategoryInfo( String mimetypePrefix,String displayName,Drawable dIcon ) {
        this.dIcon = dIcon;
        this.displayName = displayName;
        this.categorySign = mimetypePrefix;
    }
    
}
