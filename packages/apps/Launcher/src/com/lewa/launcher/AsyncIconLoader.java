package com.lewa.launcher;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AsyncIconLoader {

	private HashMap<Intent, ApplicationInfo> imageCache;
	private boolean mLocaleChanged;
	private Context mContext;
	final ContentResolver mResolver;
	private ExecutorService executorService = Executors.newCachedThreadPool();
											//Executors.newSingleThreadExecutor();
											//Executors.newFixedThreadPool(5);  

	public AsyncIconLoader(Context context, boolean localeChanged) {
		this.imageCache = new HashMap<Intent, ApplicationInfo>();
		this.mLocaleChanged = localeChanged;
		this.mContext = context;
		mResolver = context.getContentResolver();
	}

	public ApplicationInfo loadDrawable(final ApplicationInfo info,
			final boolean isFromsdCard, final ImageCallback imageCallback) {
		if (info == null) {
			return null;
		}
		final Intent intent = info.intent;

		final ActivityInfo activityInfo = info.activityInfo;

		if (activityInfo == null && !isFromsdCard  ) {// 
		    // reopen by luoyongxing for customzied icon coherence  20120822
			final ResolveInfo resolveInfo = mContext.getPackageManager().resolveActivity(intent, 0);
			if (resolveInfo == null && (!info.isLewaIcon)) {
                               // Begin, deleted by zhumeiquan for App List button, 20121017
				//info.title = info.intent.getComponent().getPackageName();
				//info.container = ItemInfo.NO_ID;
				// End
				return info;
			} else if (resolveInfo != null) {
				info.activityInfo = resolveInfo.activityInfo;
                            
                           if(info.intent != null 
                                && "android.intent.action.VIEW".equals(info.intent.getAction())
                                && "http://bbs.lewaos.com/forum.php?mobile=yes".equals(info.intent.getDataString())){
                                info.isLewaIcon = true;
                                info.iconBackground = null;
                                // can't get the correct title any more, so we don't change language.
                                mLocaleChanged = false;
                            }
			} else {
				return info;
			}
		}

		if (imageCache.containsKey(intent)) {
			ApplicationInfo appInfo = imageCache.get(intent);
			if (appInfo != null && appInfo.icon != null) {
				return appInfo;
			}
		}

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				if (message.what == 0){
					imageCallback.imageLoaded((ApplicationInfo) message.obj);}
			}
		};

		executorService.submit(new Runnable() {
            public void run() {
            	synchronized (info) {
            		
					final PackageManager manager = mContext.getPackageManager();
					Drawable drawable = null;
					if (isFromsdCard) {

						drawable = Launcher.getModel().getApplicationInfoIcon(
								manager, info, mContext);

						if (drawable == null) {
							final ResolveInfo resolveInfo = mContext
									.getPackageManager().resolveActivity(
											info.intent, 0);
							if (resolveInfo != null) {
								info.activityInfo = resolveInfo.activityInfo;
								drawable = Launcher.getModel()
										.getIconForsdApps(manager,
												info,
												mContext);
							}
						}
					} else if (!info.isLewaIcon) {
						drawable = Launcher.getModel().getIconForsdApps(
								manager, info, mContext);
					}

					String title = null;

					if (mLocaleChanged) {
						title = Launcher.getModel().getLabel(manager,
								info.activityInfo);
						final ContentValues values = new ContentValues();
						values.put(LauncherSettings.Favorites.TITLE, title);

						mResolver
								.update(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION,
										values,
										"_id=?",
										new String[] { String.valueOf(info.id) });
					}
					if (title != null && !title.equals(info.title)) {
						info.title = title;
					}
					if (drawable != null && !drawable.equals(info.icon)) {
						info.icon.setCallback(null);
						info.icon = drawable;
					}
					if (info.intent != null) {
						imageCache.put(info.intent, info);
					}
					Message message = handler.obtainMessage(0, info);
					handler.sendMessage(message);
				}
			
            }
        });
		return info;
	}

	public interface ImageCallback {
		public void imageLoaded(ApplicationInfo appInfo);
	}
	
	public void clearCache(){
		if(imageCache != null) {
			imageCache.clear();
		}
	}
}