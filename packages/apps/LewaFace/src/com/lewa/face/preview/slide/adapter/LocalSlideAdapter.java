package com.lewa.face.preview.slide.adapter;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;

import android.support.lewa.view.LewaPagerView;
import android.support.lewa.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class LocalSlideAdapter extends PagerAdapter{
    
    private static final String TAG = LocalSlideAdapter.class.getSimpleName();
    
    private ArrayList<String> mSource = null;
    private Context mContext = null;
    
    private HashMap<Integer, SoftReference<Bitmap>> cache = new HashMap<Integer, SoftReference<Bitmap>>();

    public LocalSlideAdapter(ArrayList<String> source,Context context){
        mSource = source;
        mContext = context;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        if(mSource != null){
            return mSource.size();
        }
        return 0;
    }

    @Override
    public Object instantiateItem(View container, int position) {

        ImageView imageView = new ImageView(mContext);
        //imageView.setScaleType(ScaleType.FIT_XY);
        Bitmap bitmap = getBitmapFromCache(position);
        if(bitmap == null){
            bitmap = BitmapFactory.decodeFile(mSource.get(position));
            cache.put(position, new SoftReference<Bitmap>(bitmap));
        }
        imageView.setImageBitmap(bitmap);
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
    
    
    private Bitmap getBitmapFromCache(int position){
        SoftReference<Bitmap> softBitmap = cache.get(position);
        Bitmap bitmap = null;
        if(softBitmap != null){
            bitmap = softBitmap.get();
            if(bitmap != null){
                return bitmap;
            }else {
                cache.remove(position);
            }
        }
        return null;
    }
    
    public void onDestroy(){
        for(Entry<Integer, SoftReference<Bitmap>> entry : cache.entrySet()){
            SoftReference<Bitmap> softBitmap = entry.getValue();
            if(softBitmap != null){
                Bitmap bitmap = softBitmap.get();
                if(bitmap != null && !bitmap.isRecycled()){
                    bitmap.recycle();
                    bitmap = null;
                }
            }
        }
        cache.clear();
        
        if(mSource != null){
            mSource.clear();
        }
    }

}
