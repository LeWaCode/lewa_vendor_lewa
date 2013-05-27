package com.lewa.face.adapters;

import java.io.File;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.lewa.face.R;
import com.lewa.face.download.ImageLoaderBase;
import com.lewa.face.util.ThemeUtil;

public class ThumbnailImageLoader extends ImageLoaderBase{
    private static final String TAG = ThumbnailImageLoader.class.getSimpleName();

    public ThumbnailImageLoader(int hardCacheCapacity, int softCacheCapacity,Context context) {
        super(hardCacheCapacity, softCacheCapacity, context);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Bitmap createBitmapFromNetWork(InputStream is, File target) {
        try {
            
            ThemeUtil.createThumbnail(is,target,100,false);
            //ThemeUtil.createThumbnail(is,target,100,true);
            
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
        return R.drawable.lewa;
    }

}
