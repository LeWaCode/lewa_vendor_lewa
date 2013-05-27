package com.lewa.face.preview.slide.adapter;

import java.io.File;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.lewa.face.R;
import com.lewa.face.download.ImageLoaderBase;
import com.lewa.face.util.ThemeUtil;

public class SlideImageLoader extends ImageLoaderBase{

    private static final String TAG = SlideImageLoader.class.getSimpleName();
    
    public SlideImageLoader(int hardCacheCapacity, int softCacheCapacity,Context context) {
        super(hardCacheCapacity, softCacheCapacity, context);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Bitmap createBitmapFromNetWork(InputStream is, File target) {
        try {
            if(!target.getParentFile().exists()){
                target.getParentFile().mkdirs();
            }
            ThemeUtil.createThumbnail(is,target,80,false);
            return BitmapFactory.decodeFile(target.getAbsolutePath(), ThemeUtil.getOptions(1));
        } catch (OutOfMemoryError e) {
            ThemeUtil.resetDownloadThread();
            ThemeUtil.runGC();
            e.printStackTrace();
            Log.e(TAG, "OOM Exception");
            
        }
        return null;
    }

    @Override
    protected int defaultImageId() {
        // TODO Auto-generated method stub
        return R.drawable.theme_no_default;
    }

}
