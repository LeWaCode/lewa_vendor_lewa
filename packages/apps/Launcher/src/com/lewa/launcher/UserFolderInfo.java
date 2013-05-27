/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lewa.launcher;

import android.content.ContentValues;
import android.content.Intent;

import java.util.ArrayList;

import com.lewa.launcher.LauncherSettings.BaseLauncherColumns;

/**
 * Represents a folder containing shortcuts or apps.
 */
class UserFolderInfo extends FolderInfo {
    /**
     * The apps and shortcuts 
     */
    ArrayList<ApplicationInfo> contents = new ArrayList<ApplicationInfo>();
    //Begin [pan] add
    public FolderIcon mFolderIcon = null; 
    //End
    UserFolderInfo() {
        itemType = LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER;
    }
    
    public UserFolderInfo(UserFolderInfo info) {
        super();
        assignFrom(info);
    }

    @Override
    void assignFrom(ItemInfo info) {
    super.assignFrom(info);
        if (info instanceof UserFolderInfo) {
            UserFolderInfo nfo = (UserFolderInfo)info;
            title = nfo.title.toString();
            intent = new Intent(nfo.intent);
            if (nfo.iconResource != null) {
                iconResource = new Intent.ShortcutIconResource();
                iconResource.packageName = nfo.iconResource.packageName;
                iconResource.resourceName = nfo.iconResource.resourceName;
            }
            icon = nfo.icon;
            filtered = nfo.filtered;
            customIcon = nfo.customIcon;
            activityInfo = nfo.activityInfo;
            contents = new ArrayList<ApplicationInfo>(nfo.contents);
            mFolderIcon = nfo.mFolderIcon;           
        }
    }
    
    /**
     * Add an app or shortcut
     * 
     * @param item
     */
    public void add(ApplicationInfo item) {
        contents.add(item);
    }

    @Override
    void onAddToDatabase(ContentValues values) { 
        super.onAddToDatabase(values);
        values.put(BaseLauncherColumns.TITLE, title.toString());
    }
    //Begin [pan] add
    public void setFolderIcon(FolderIcon icon)  
    {  
    	mFolderIcon=icon;  
    }  
    //End

}
