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
import android.widget.ImageView;

import com.lewa.base.Logs;
import com.lewa.base.images.FileCategoryHelper.FileCategory;
import com.lewa.base.images.FileIconLoader.IconLoadFinishListener;

public class FileIconHelper {

    private FileIconLoader mIconLoader;

    public FileIconHelper(Context context) {
        mIconLoader = new FileIconLoader(context);
    }

    public void setIcon(ThumbnailBase thumbnailBase, ImageView fileImage) {
        String filePath = thumbnailBase.filePath;
        long fileId = thumbnailBase.dbId;
        FileTypeInfo type = FileCategoryHelper.getCategoryFromPath(filePath);
        FileCategory fc = type.fc;
        boolean set = false;
        mIconLoader.cancelRequest(fileImage);
        switch (fc) {
            case Music:
                if(thumbnailBase.thumbnailPath!=null){
                    filePath = thumbnailBase.thumbnailPath;
                }
            case Apk:                            
            case Picture:
            case Video:
                set = mIconLoader.loadIcon(fileImage, filePath, fileId, fc);
                break;
        }
        if (!set || fc == FileCategory.Video||fc == FileCategory.Music) {
            fileImage.setImageResource(type.defaultBg);
            set = true;
        }
    }
}
