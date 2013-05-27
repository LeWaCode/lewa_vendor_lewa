package com.lewa.face.preview.slide.adapter;

import java.util.ArrayList;

import com.lewa.face.util.ThemeUtil;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;

import android.support.lewa.view.LewaPagerView;
import android.support.lewa.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public abstract class OnlineSlideAdapter extends PagerAdapter{
    
    private static final String TAG = OnlineSlideAdapter.class.getSimpleName();
    
    protected ArrayList<String> mSource = null;
    protected Context mContext = null;
    protected String mThemeName = null;
    
    protected SlideImageLoader slideImageLoader = null;
    

    public OnlineSlideAdapter(ArrayList<String> source,Context context,String themePkg){
        mSource = source;
        mContext = context;
        
        slideImageLoader = new SlideImageLoader(10, 4, mContext);
        
        mThemeName = ThemeUtil.getNameNoBuffix(themePkg);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        if(mSource != null){
            return mSource.size();
        }
        return 0;
    }

    protected abstract String initUrl(String previewPath);
    
    @Override
    public Object instantiateItem(View container, int position) {

        ImageView imageView = new ImageView(mContext);
        
        Bitmap bitmap = slideImageLoader.getBitmapFromCache(position);
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
        }else {
            String previewPath = mSource.get(position);
            slideImageLoader.loadImage(imageView, position, previewPath, initUrl(previewPath));
        }
        
        ((LewaPagerView)container).addView(imageView, 0, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        return imageView;
    }

    @Override
    public boolean isViewFromObject(View view, Object obj) {

        return view == (View)obj;
    }
    
    @Override
    public void destroyItem(View container, int position, Object object) {
        ((LewaPagerView)container).removeView((View)object);

    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
        
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(View arg0) {
        
    }
    
    @Override
    public void finishUpdate(View arg0) {
        
    }
    
    public void onDestroy(){
        slideImageLoader.clearCache();
        if(mSource != null){
            mSource.clear();
        }
    }

}
