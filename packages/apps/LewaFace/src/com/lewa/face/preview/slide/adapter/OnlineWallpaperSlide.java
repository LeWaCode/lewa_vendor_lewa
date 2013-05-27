package com.lewa.face.preview.slide.adapter;

import java.util.ArrayList;

import com.lewa.face.util.ThemeUtil;

import android.content.Context;

public class OnlineWallpaperSlide extends OnlineSlideAdapter{

    public OnlineWallpaperSlide(ArrayList<String> source, Context context,
            String themePkg) {
        super(source, context, themePkg);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String initUrl(String previewPath) {
        // TODO Auto-generated method stub
        return new StringBuilder().append(ThemeUtil.WALLPAPER_PREVIEW_URL).append("/").append(mThemeName).append(".jpg").toString();
    }

}
