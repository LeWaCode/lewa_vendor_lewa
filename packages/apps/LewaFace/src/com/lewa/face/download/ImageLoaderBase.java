package com.lewa.face.download;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.lewa.face.R;
import com.lewa.face.util.Logs;
import com.lewa.face.util.ThemeUtil;

public abstract class ImageLoaderBase {

    /**
     * 1、本类主要用来解决实时解码图片或者实时从网络端下载图片可能造成的OOM异常、ANR异常、滑动卡顿等问题（但是不能从根本上解决这些问题）
     * 2、本类主要封装了从本地解码图片和从网络端下载图片的操作，具体是什么操作要看loadImage()函数中url这一参数，如果url=null，则表示从decodePath
     * （本地SD卡）中解码图片，否则从网络端下载图片，并保存到decodePath路径中
     * 3、本类的对图片的解码和从网络端下载图片都是一个异步的过程，体现在使用了AsyncTask
     * 4、本类的主要思想是：采用一个二级缓存，一个是硬缓存（hardBitmapCache），另一个是软缓存（softBitmapCache），并且采用了LRU。
     * 首先解码和下载的图片会保存到硬缓存中，硬缓存的容量为40，如果超过这一容量时，根据LRU，硬缓存将会把最久时间的对象移除到软缓存中。而软
     * 缓存中的对象采用了软引用，也就意味着当内存紧张时，软缓存中的对象将会被释放掉
     * @author fulw
     *
     */

    private static final String TAG = ImageLoaderBase.class.getSimpleName();
    
    /**
     * 软缓存
     */
    private WeakHashMap<Integer, SoftReference<Bitmap>> softBitmapCache;

    /**
     * 硬缓存
     */
    public LinkedHashMap<Integer, Bitmap> hardBitmapCache;
    
    public static Bitmap defaultBitmap = null;
    private Context mContext = null;
    
    /**
     * 把加载图片异步Task保存起来，以确保在退出时，这些task能正常cancel掉
     */
    private ArrayList<LoadImageTask> loadImageTasks = new ArrayList<LoadImageTask>();
    

    /**
     * 
     * @param hardCacheCapacity 硬缓存容量
     * @param softCacheCapacity 软缓存容量
     */
    @SuppressWarnings("serial")
    public ImageLoaderBase(final int hardCacheCapacity,final int softCacheCapacity,Context context){
        softBitmapCache = new WeakHashMap<Integer, SoftReference<Bitmap>>(softCacheCapacity / 2);
        hardBitmapCache = new LinkedHashMap<Integer, Bitmap>(hardCacheCapacity / 2, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(LinkedHashMap.Entry<Integer, Bitmap> eldest) {
                if (size() > hardCacheCapacity) {
                    softBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
                    return true;
                } else
                    return false;
            }
        };
        mContext = context;
        if (defaultBitmap == null) {
        defaultBitmap = BitmapFactory.decodeResource(mContext.getResources(), defaultImageId());
        }
    }
        
    /**
     * 加载图片并供给ImageView使用
     * @param imageView :Display the bitmap view
     * @param position :GridView或者ListView中item的位置position
     * @param decodePath :存储卡中缓存图片的位置（主要针对item所显示图片对应的路径，可能经过压缩处理的图片）
     * @param url : 如果url=null,表示从存储卡中解码图片，否则从网络端下载图片
     */
    public void loadImage(ImageView imageView,Integer position,String thumbnailPath,String url){
    	/**
         * 从硬缓存或者软缓存中获得bitmap
         */
        Bitmap bitmap = getBitmapFromCache(position);
        
        /**
         * 如果bitmap为空，刚从存储卡中解码图片或者从网络端下载图片
         */
        if(bitmap == null){
            imageView.setImageBitmap(defaultBitmap);
            /**
             * 如果ImageView对象中已经包含了LoadImageTask对象并且还处于running状态，则将LoadImageTask取消
             */
            LoadImageTask loadImageTask = (LoadImageTask) imageView.getTag(R.string.app_name);
            if(loadImageTask != null && loadImageTask.getStatus() == AsyncTask.Status.RUNNING){
                Log.i(TAG,"LoadImageTask is canceled");
                loadImageTask.cancel(true);
            }
            LoadImageTask loadTask = new LoadImageTask(thumbnailPath, url, imageView,position);
            loadTask.execute("");
            loadImageTasks.add(loadTask);
        }else {
            imageView.setImageBitmap(bitmap);
        }
        
    }
    
    /**
     * 从缓存中或者Bitmap
     * @param position
     * @return
     */
    public Bitmap getBitmapFromCache(Integer position) {
        
        synchronized (hardBitmapCache) {
            final Bitmap bitmap = hardBitmapCache.get(position);
            if (bitmap != null) {
                hardBitmapCache.remove(position);
                hardBitmapCache.put(position, bitmap);
                return bitmap;
            }
        }

        SoftReference<Bitmap> bitmapReference = softBitmapCache.get(position);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                return bitmap;
            } else {
                softBitmapCache.remove(position);
            }
        }

        return null;
    }
        
    /**
     * 将bitmap存放到缓存中
     * @param bitmap
     * @param position
     */
    private void addToCache(Bitmap bitmap,Integer position){
        if (bitmap != null) {
            synchronized (hardBitmapCache) {
                hardBitmapCache.put(position, bitmap);
            }
        }
    }

    /**
     * 异步解码图片或者从网络端下载图片
     * @author fulw
     *
     */
    public class LoadImageTask extends AsyncTask<String, Void, Bitmap>{
        
        /**
         * item所绑定图片所在的位置
         */
        private String thumbnailPath = null;
        private String url = null;
        private ImageView imageView = null;
        private Integer position = null;
        
        public LoadImageTask(String thumbnailPath,String url,ImageView imageView,Integer position){
            this.thumbnailPath = thumbnailPath;
            this.url = url;
            this.imageView = imageView;
            this.position = position;
            imageView.setTag(R.string.app_name,this);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            if(url != null){
                File thumbnailFile = new File(thumbnailPath);
                try {
                    if(thumbnailFile.exists()){
                        /**
                         * 如果当前Task被cancel，则没必要继续执行
                         */
                        if(isCancelled()){
                            return null;
                        }
                        bitmap = BitmapFactory.decodeFile(thumbnailPath,ThemeUtil.getOptions(1)); // First try to decode from sdcard,because it may be the sdcard already has downloaded
                    }
                } catch (OutOfMemoryError e) {
                    ThemeUtil.resetDownloadThread();
                    ThemeUtil.runGC();
                    e.printStackTrace();
                    Log.e(TAG, "OOM Exception");
                }
                
                if(bitmap == null){
                    /**
                     * 如果当前Task被cancel，则没必要继续执行
                     */
                    if(isCancelled()){
                        return null;
                    }
                    bitmap = downloadBitmap(thumbnailFile,url,this);
                }
            }else {
                /**
                 * 如果当前Task被cancel，则没必要继续执行
                 */
                if(isCancelled()){
                    return null;
                }
                bitmap = BitmapFactory.decodeFile(thumbnailPath,ThemeUtil.getOptions(1));
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if(result != null){
                
                /**
                 * 如果result不为null，则存入缓存中
                 */
                addToCache(result, position);
                
                imageView.setImageBitmap(result);
            }else {
                /**
                 * 显示一张默认的图片
                 */
                imageView.setImageBitmap(defaultBitmap);
            }
        }
        
        
        
    }
    
    protected abstract Bitmap createBitmapFromNetWork(InputStream is,File target);
    
    /**
     * 如果当前没有图片显示是，刚显示默认图片
     * @return
     */
    protected abstract int defaultImageId();
        
    public Bitmap downloadBitmap(File thumbnailFile,String url,LoadImageTask loadImageTask) {
        
        HttpClient client;
      
        HttpGet getRequest = null;
        
        long length = 0;

        try {
            client = new DefaultHttpClient();
            getRequest = new HttpGet(url);
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.e(TAG, "Can't download bitmap :" + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            /**
             * 如果当前Task被cancel，则没必要继续执行
             */
            
            if(loadImageTask!=null&& loadImageTask.isCancelled()){
                return null;
            }
            if (entity != null) {
                InputStream inputStream = null;
                FlushedInputStream fis = null;
                try {
                    length = entity.getContentLength();
                    if(length == 0){
                        Log.e(TAG, "ContentLength == 0");
                        return null;
                    };
                    
                    inputStream = entity.getContent();
                    
                    fis = new FlushedInputStream(inputStream);
                    /**
                     * 如果当前Task被cancel，则没必要继续执行
                     */
                    if(loadImageTask!=null&&loadImageTask.isCancelled()){
                        return null;
                    }
                    return createBitmapFromNetWork(fis,thumbnailFile);
                } finally {
                    if(fis != null){
                        fis.close();
                        fis = null;
                    }
                    if (inputStream != null) {
                        inputStream.close();
                        inputStream = null;
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            getRequest.abort();
            e.printStackTrace();
        } 
        return null;
    }
        
    /**
     * 解决一个bug
     * @author fulw
     *
     */
    public static class FlushedInputStream extends FilterInputStream {
        
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
        
    public void clearCache(){
        Log.i(TAG, "Release the hardcache and soft cache");
        for(Entry<Integer, Bitmap> entry : hardBitmapCache.entrySet()){
            Bitmap bitmap = entry.getValue();
            if(bitmap != null && !bitmap.isRecycled()){
                bitmap.recycle();
                bitmap = null;
            }
        }
        
        hardBitmapCache.clear();
        
        for(Entry<Integer, SoftReference<Bitmap>> entry : softBitmapCache.entrySet()){
            SoftReference<Bitmap> bitmapSoft = entry.getValue();
            if(bitmapSoft != null){
                Bitmap bitmap = bitmapSoft.get();
                if(bitmap != null && !bitmap.isRecycled()){
                    bitmap.recycle();
                    bitmap = null;
                }
            }
        }
        softBitmapCache.clear();
        
        for(LoadImageTask loadImageTask : loadImageTasks){
            loadImageTask.cancel(true);
        }

    }
    

}
