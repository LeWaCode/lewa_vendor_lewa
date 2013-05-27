package com.lewa.store.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class AsyncImageLoader {

	private static String TAG = AsyncImageLoader.class.getSimpleName();

//	private HashMap<String, SoftReference<Drawable>> imageCache;
	private HashMap<String, Drawable> imageCache;
	private BlockingQueue<Runnable> queue;
	private ThreadPoolExecutor executor;

	public AsyncImageLoader() {
//		imageCache = new HashMap<String, SoftReference<Drawable>>();
		imageCache = new HashMap<String, Drawable>();
		queue = new LinkedBlockingQueue<Runnable>();
		executor = new ThreadPoolExecutor(1,2, 30, TimeUnit.SECONDS, queue);
	}
	
	 public void fetchDrawableOnThread(final Context context, final String imageUrl,final ImageView imageView) {
			if (imageCache.containsKey(imageUrl)) {
				Drawable drawable = imageCache.get(imageUrl);
				
				if (drawable != null) {
					Log.e(TAG,"drawable !=null,load image from map,imageUrl=="+imageUrl);
					imageView.setVisibility(View.VISIBLE);
				    imageView.setImageDrawable(drawable);
				    return;
				}else{
					imageView.setVisibility(View.VISIBLE);
					imageView.setImageDrawable(context.getPackageManager().getDefaultActivityIcon());
				}
			}else{
				imageView.setVisibility(View.VISIBLE);
				imageView.setImageDrawable(context.getPackageManager().getDefaultActivityIcon());
				Log.e(TAG,"imageCache not contains,url="+imageUrl);
			}
			
			final Handler handler = new Handler() {
				public void handleMessage(Message message) {
					Drawable drawable=(Drawable) message.obj;
					imageView.setVisibility(View.VISIBLE);
				    imageView.setImageDrawable(drawable);
				}
			};

			executor.execute(new Runnable() {
				@Override
				public void run() {
					Drawable drawable = loadImageFromUrl(context, imageUrl);
					imageCache.put(imageUrl, drawable);
					Message message = handler.obtainMessage(0, drawable);
					handler.sendMessage(message);
				}
		});
	}

	public Drawable loadDrawable(final Context context, final String imageUrl,final ImageCallback imageCallback) {
		if (imageCache.containsKey(imageUrl)) {
//			SoftReference<Drawable> softReference = imageCache.get(imageUrl);
//			Drawable drawable = softReference.get();
			Drawable drawable = imageCache.get(imageUrl);
			
			if (drawable != null) {
//				Log.e(TAG,"load image from map,imageUrl=="+imageUrl);
				return drawable;
			}
		}
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Drawable) message.obj, imageUrl);
//				Log.e(TAG,"have downloaded image,url="+imageUrl);
			}
		};

		executor.execute(new Runnable() {
			@Override
			public void run() {
				Drawable drawable = loadImageFromUrl(context, imageUrl);
//				imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
				imageCache.put(imageUrl, drawable);
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);
			}
		});
		return null;
	}

		public static Drawable loadImageFromUrl(Context context, String imageUrl) {
		Drawable drawable = null;
		if (imageUrl == null){
			return null;
		}
		String fileName = "";
		if (imageUrl != null && imageUrl.length() != 0) {
			fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
		}
		File file = new File(context.getCacheDir(), fileName);
		if (!file.exists() && !file.isDirectory()) {
            FileOutputStream fos=null;
            ByteArrayOutputStream bao = null;
            InputStream is=null;
            try {
				int BUFFER_SIZE = 1024*4;
				byte[] buffer = new byte[BUFFER_SIZE];
				fos = new FileOutputStream(file);
				bao=new ByteArrayOutputStream(BUFFER_SIZE);
				is = new URL(imageUrl).openStream();
				int len=0;
				while ((len=is.read(buffer))!= -1) {
					fos.write(buffer, 0, len);
					bao.write(buffer, 0, len);
				}
				drawable=Drawable.createFromStream(new ByteArrayInputStream(bao.toByteArray()), "src");
				if(null!=drawable){
					Log.d(TAG, "get pic from server:" + drawable.toString());
				}
			} catch (IOException e) {
				Log.e(TAG, e.toString() + "pic download and save error！");
			}catch(Exception e){
				e.printStackTrace();
			}finally {
                if(null!=bao){
                    try {
                        bao.close();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                if(null!=fos){
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                if(null!=is){
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
		} else {
			FileInputStream fis=null;
			try {
				fis = new FileInputStream(file);
				drawable=Drawable.createFromStream(fis, "src");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}finally {
                if(null!=fis){
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
		}
		return drawable;
	}

    public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, String imageUrl);
	}
}
