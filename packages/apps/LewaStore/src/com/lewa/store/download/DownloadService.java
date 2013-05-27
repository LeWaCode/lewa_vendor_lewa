package com.lewa.store.download;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.lewa.core.base.APIException;
import com.lewa.core.base.LewaUser;
import com.lewa.core.base.StorePackage;
import com.lewa.store.R;
import com.lewa.store.activity.ManageActivity;
import com.lewa.store.extras.GooglePackages;
import com.lewa.store.extras.SinaPackages;
import com.lewa.store.items.LewaBasketHelper;
import com.lewa.store.model.AppInfo;
import com.lewa.store.model.AppSourceDao;
import com.lewa.store.model.LewaNotification;
import com.lewa.store.model.StoreException;
import com.lewa.store.nav.MainActivity;
import com.lewa.store.pkg.LaunchApp;
import com.lewa.store.pkg.PkgManager;
import com.lewa.store.utils.Constants;
import com.lewa.store.utils.FileHelper;
import com.lewa.store.utils.StorageCheck;
import com.lewa.store.utils.StrUtils;
import com.lewa.store.utils.SystemHelper;
import com.lewa.store.download.Downloader;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class DownloadService extends Service implements
		LewaBasketHelper.OnBasketResponseListener {

	private String TAG = DownloadService.class.getSimpleName();

	public Message msg = null;
	private NotificationManager nm = null;
	private RemoteViews remoteViews = null;
	private Intent remoteIntent = null;
	private PendingIntent remotePendingIntent = null;
	private Notification remoteNotification = null;

	private Downloader downloader = null;
	private Dao dao = null;
	private AppSourceDao asd = null;
	private LewaNotification notification = null;
	private LewaUser user = null;
	private LewaBasketHelper basketHelper = null;
	private Context sContext = null;
	private DownloadHelper downloadHelper = null;

	private GooglePackages gpk = null;
	private Map<String, String> gMap = null;

	private BlockingQueue<Runnable> queue = null;
	private ThreadPoolExecutor executor = null;

	private Handler serviceHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == Constants.HandlerWhatFlagTwo) {
				Integer appid = (Integer) msg.obj;
				int completeSize = msg.arg1;
				int fileSize = msg.arg2
						+ Constants.PROGRESSBAR_NOTIFICATION_TEMPSIZE;
				AppInfo ai = asd.getMetaAppInfoById(appid.intValue());
				if (completeSize != 0 && fileSize != 0 && ai != null) {
					if (null != remoteViews) {
						remoteViews.setProgressBar(R.id.notification_progress,
								fileSize, completeSize, false);
						int per = completeSize * 100 / fileSize;
						remoteViews.setTextViewText(R.id.notification_appname,
								ai.getAppName());
						remoteViews.setTextViewText(R.id.text_download, per
								+ "%");
						remoteNotification.contentView = remoteViews;
						remoteNotification.contentIntent = remotePendingIntent;
						nm.notify(Constants.NOTIFICATION_DOWNLOADING_ID,
								remoteNotification);
					}
				}
			} else if (msg.what == Constants.HandlerWhatFlagDownloading) {
				Integer appid = (Integer) msg.obj;
				AppInfo ai = asd.getMetaAppInfoById(appid.intValue());
				toast(ai.getAppName() + " "
						+ getString(R.string.toast_downloading), true);
			} else if (msg.what == Constants.HandlerWhatFlagThree) {// 网络异常
				Integer appid = (Integer) msg.obj;
				AppInfo ai = asd.getMetaAppInfoById(appid.intValue());
				notification
						.deleteNotification(Constants.NOTIFICATION_DOWNLOADING_ID);
				notification.notifyDownloadFailed(ai.getAppName());
				toast(getString(R.string.downloading_network_not_available),
						true);

				ManageActivity.failedMaps.put(appid,
						dao.getDownloadUrlById(appid));
				invalidateManageViews();
			} else if (msg.what == Constants.HandlerWhatFlagFour) {// 其他异常
				Integer appid = (Integer) msg.obj;
				AppInfo ai = asd.getMetaAppInfoById(appid.intValue());
				notification
						.deleteNotification(Constants.NOTIFICATION_DOWNLOADING_ID);
				notification.notifyDownloadFailed(ai.getAppName());
				toast(ai.getAppName() + " "
						+ getString(R.string.download_error), true);

				ManageActivity.failedMaps.put(appid,
						dao.getDownloadUrlById(appid));
				invalidateManageViews();
			} else if (msg.what == Constants.HandlerWhatFlagFive) {// no space
																	// left on
																	// device
				Integer appid = (Integer) msg.obj;
				AppInfo ai = asd.getMetaAppInfoById(appid.intValue());
				notification
						.deleteNotification(Constants.NOTIFICATION_DOWNLOADING_ID);
				notification.notifyDownloadFailedResult(ai.getAppName(),
						getString(R.string.storage_exception_no_enough_memory));
				toast(ai.getAppName() + " "
						+ getString(R.string.download_error), true);

				ManageActivity.failedMaps.put(appid,
						dao.getDownloadUrlById(appid));
				invalidateManageViews();
			} else if (msg.what == Constants.HandlerWhatFlagSix) {// unexpected end of stream
				Integer appid = (Integer) msg.obj;
				AppInfo ai = asd.getMetaAppInfoById(appid.intValue());
				notification.deleteNotification(Constants.NOTIFICATION_DOWNLOADING_ID);
				notification.notifyDownloadFailedResult(ai.getAppName(),getString(R.string.storage_exception_write_disk_error));
				toast(ai.getAppName() + " "+ getString(R.string.download_error), true);

				ManageActivity.failedMaps.put(appid,dao.getDownloadUrlById(appid));
				invalidateManageViews();
			}/*
			 * else if(msg.what==Constants.HandlerWhatFlagSeven){//No such file
			 * or directory Integer appid = (Integer) msg.obj; AppInfo ai =
			 * asd.getMetaAppInfoById(appid.intValue());
			 * notification.deleteNotification
			 * (Constants.NOTIFICATION_DOWNLOADING_ID);
			 * notification.notifyDownloadFailedResult
			 * (ai.getAppName(),getString(
			 * R.string.storage_exception_write_disk_error));
			 * toast(ai.getAppName
			 * ()+" "+getString(R.string.download_error),true);
			 * 
			 * ManageActivity.failedMaps.put(appid,
			 * dao.getDownloadUrlById(appid)); invalidateManageViews(); }
			 */
			else if (msg.what == Constants.HandlerWhatFalgInstallFailed) {// install apk failed
				Integer appid = (Integer) msg.obj;
				AppInfo ai = asd.getMetaAppInfoById(appid.intValue());
				notification.deleteNotification(Constants.NOTIFICATION_DOWNLOADING_ID);
				notification.notifyInstallFailedResult(ai.getAppName(),getString(R.string.storage_exception_lowmemory));
				toast(ai.getAppName() + " "+ getString(R.string.notification_install_failed), true);

				// ManageActivity.failedMaps.put(appid,
				// dao.getDownloadUrlById(appid));//暂不记录安装失败
			}
		}
	};

	/********************/

	private DownloadSucessReceiver downSucessReceiver = null;

	private void registerDownloadSucessReceiver() {
		if (null == downSucessReceiver) {
			downSucessReceiver = new DownloadSucessReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.BROADCAST_DOWNLOAD_SUCESS);
			registerReceiver(downSucessReceiver, intentFilter);
		}
	}

	private void unRegisterDownloadSucessReceiver() {
		if (null != downSucessReceiver) {
			this.unregisterReceiver(downSucessReceiver);
			downSucessReceiver = null;
		}
	}

	class DownloadSucessReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized (this) {
				String url = intent.getStringExtra("urlstr");
				int compeleteSize = intent.getIntExtra("compeleteSize", 0);
				int fileSize = intent.getIntExtra("fileSize", 0);
				String localfile = intent.getStringExtra("localfile");
				if (compeleteSize == fileSize) {
					final int packageInt = StrUtils.getPackageIntId(url);
					String pkgName = dao.getPackageNameById(packageInt);

					if (!StorageCheck.checkPhysicalStorage(context, fileSize,
							notification)) {
						Log.e(TAG, "storage exception,stop install.");
						notification.notifyInstallFailedResult(StrUtils.getLocalApkName(localfile),getString(R.string.storage_exception_write_disk_error));
						// notification.notifyDownloadFailed(StrUtils.getLocalApkName(localfile));
						return;
					}
					new InstallAsyncTask().execute(localfile, pkgName,
							packageInt, sContext, url);
					if (user.getRegistrationState() == LewaUser.USER_IS_REGISTERED) {
						Log.i(TAG, "History id==" + packageInt);
						new Thread(new Runnable() {
                            @Override
                            public void run() {
                                basketHelper.setPackageAsDownloaded(packageInt);
                            }
                        }).start();
					}
				}
			}
		}
	}

	/********************/
	// init remote Notification
	private void initNotifyDownloading() {
		remoteNotification = new Notification();
		remoteNotification.icon = R.drawable.notification_downloading;
		remoteNotification.flags |=Notification.FLAG_ONGOING_EVENT;
		remoteNotification.flags |=Notification.FLAG_NO_CLEAR;
		remoteViews = new RemoteViews(this.getPackageName(),
				R.layout.notification_download_layout);
		remoteViews.setImageViewResource(R.id.image_download,
				R.drawable.notification_downloading);
		remoteIntent = new Intent(this, MainActivity.getInstance().getClass());
		remoteIntent.putExtra("gotomanager", 1);
		remotePendingIntent = PendingIntent.getActivity(this,
				R.string.app_name, remoteIntent, 0);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		sContext = this;
		dao = new Dao(this);
		asd = new AppSourceDao(this);
		notification = new LewaNotification(this);

		user = LewaUser.getInstance(this);
		basketHelper = new LewaBasketHelper(this);
		basketHelper.setBasketResponseListener(this);
		downloadHelper = new DownloadHelper(this);

		this.registerDownloadSucessReceiver();
		initNotifyDownloading();

		this.gpk = new GooglePackages();
		this.gMap = gpk.getGoogleSpecPackages();

		queue = new LinkedBlockingDeque<Runnable>();
		executor = new ThreadPoolExecutor(Constants.COREPOOLSIZE,
				Constants.MAXPOOLSIZE, Constants.KEEPALIVETIME,
				TimeUnit.SECONDS, queue);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		synchronized (this) {
			if (null != intent) {
				Log.i(TAG, "onStart intent==" + intent.toString());
				int packageIdInt = intent.getIntExtra("packageIdInt", 0);
				String urlstr = downloadHelper.getStoreItemURL(packageIdInt);
				String localfile = intent.getStringExtra("localfile");
				Log.d(TAG, "download url==" + urlstr);

				int threadcount = Constants.THREAD_COUNT;
				downloader = ManageActivity.downloaders.get(urlstr);
				if (downloader == null) {
					downloader = new Downloader(urlstr, packageIdInt,
							localfile, threadcount, this, dao, executor,
							serviceHandler);
					ManageActivity.downloaders.put(urlstr, downloader);
					ManageActivity.downloadings.add(packageIdInt);
				}
				if (downloader.isdownloading()) {
					return 0;
				}
				downloader.recordDownloadInfo(0, urlstr, packageIdInt);

				Intent i = new Intent(Constants.BROADCAST_DOWNLOAD_SERVICE);
				i.putExtra("packageIdInt", packageIdInt);
				i.putExtra("url", urlstr);
				sendBroadcast(i);

				downloader.download();
			} else {
				Log.e(TAG, "download error,intent=null");
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private Handler downloadHanlder = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.HandlerWhatFalgGoogleServiceNotice:
				Toast.makeText(
						sContext,
						getString(R.string.google_service_not_installed)
								+ getString(R.string.GoogleServicesFramework),
						Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	private void stopAllDownloaders() {
		synchronized (this) {
			for (Map.Entry<String, Downloader> entry : ManageActivity.downloaders
					.entrySet()) {
				Downloader tempdownloader = entry.getValue();
				tempdownloader.cancel();
			}
			ManageActivity.downloaders.clear();
		}
	}

	@Override
	public void onDestroy() {
		FileHelper.umount();
		this.unRegisterDownloadSucessReceiver();
		if (null != dao) {
			dao.deleteAll();
			dao.closeDb();
		}
		stopAllDownloaders();
		if (null != executor) {
			executor.shutdown();
			Log.d(TAG, "exec close");
		}
		Log.d(TAG, "service stop!");
		super.onDestroy();
		System.gc();
	}

	class InstallAsyncTask extends AsyncTask<Object, Object, Object> {

		private String TAG = InstallAsyncTask.class.getSimpleName();

		private int appId;
		private String url = null;
		private String localPath = null;
		private boolean installFlag = false;
		private String googlePackageName = null;
		private Context c = null;
		private String pkgName = "";

		@Override
		protected Object doInBackground(Object... params) {
			this.localPath = params[0].toString();
			this.pkgName = params[1].equals("") ? "" : params[1].toString();
			this.appId = ((Integer) params[2]);
			this.c = (Context) params[3];
			this.url = params[4].toString();
			if (null != pkgName) {
				checkIsGooglePackage(pkgName);
			}
			if (null == this.googlePackageName) {
				Log.d(TAG, "spec packageName==null");
				String locationStr = PkgManager.getAppInstallLocation(this.c);
				Log.i(TAG, "locationStr==" + locationStr);
				if (PkgManager.isInstalledSucess(localPath)) {
					this.installFlag = true;
				}
			} else {
				Log.i(TAG, "spec packageName==" + this.googlePackageName);
				this.localPath = FileHelper.rename(this.localPath,
						gMap.get(this.googlePackageName));
				FileHelper.chmodGeneralFile(this.localPath);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (FileHelper.suCopyFile(sContext, this.localPath,
						Constants.SYSTEM_DIR + File.separator,
						gMap.get(this.googlePackageName))) {
					installFlag = true;
				}
				String newFileName = gMap.get(this.googlePackageName);
				int k = 0;
				while (!LaunchApp.isInstallGooglePackages(newFileName)) {
					k++;
					if (LaunchApp.isInstallGooglePackages(newFileName)) {
						FileHelper.chmodSysApp(sContext, Constants.SYSTEM_DIR
								+ File.separator + newFileName);
						Log.e(TAG, "chmod " + newFileName + " compare times=="
								+ k);
						k = 0;
						break;
					}
					// Log.e(TAG,"k="+k);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (LaunchApp.isInstallGooglePackages(newFileName)) {
					FileHelper.chmodFile(sContext, Constants.SYSTEM_DIR
							+ File.separator + newFileName);
					Log.e(TAG, "chmod once");
				}
				if (this.googlePackageName.equals("com.google.android.gm")) {
					FileHelper.delFile(localPath);
				}
			}
			return this.installFlag;
		}

		public void checkIsGooglePackage(String pkgName) {
			if(gMap.containsKey(pkgName)){
				this.googlePackageName=pkgName.trim();
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			// check google service
			if (gMap.containsKey(this.pkgName) && !ManageActivity.isWhiteListItem(this.pkgName)) {
				if (!LaunchApp.isInstallGooglePackages(Constants.Google.GOOGLE_SERVICE_FRAMEWORK)) {
					Message mess = Message.obtain();
					mess.what = Constants.HandlerWhatFalgGoogleServiceNotice;
					downloadHanlder.sendMessage(mess);
					Log.i(TAG, "not install google service");
				}
			}
			if (installFlag) {
				Log.d(TAG, "install success,flag==" + installFlag);
				
				synchronized (this) {
					nm.cancel(Constants.NOTIFICATION_DOWNLOADING_ID);

					Log.i(TAG, "have removed downloading noti");

					// add basket data,to BasketActivity
					/*
					 * Intent ii = new
					 * Intent(Constants.BROADCAST_BASKET_ADD_DATA);
					 * ii.putExtra("appId",this.appId); sendBroadcast(ii);
					 */

					notification.notifyDownloadSuccess(StrUtils
							.getLocalFileName(localPath));

					// refresh adapter,listview
					/*
					 * Intent intent=new
					 * Intent(Constants.BROADCAST_APPLIST_UPDATE_VIEW);
					 * intent.putExtra(Constants.UPDATE_APPLIST_ITEMS,Constants.
					 * UPDATE_APPLIST_ALL_ITEMS_ID); sendBroadcast(intent);
					 */

					//AppListActivity
					Intent intent = new Intent(Constants.BROADCAST_APPLIST_UPDATE_VIEW);
					intent.putExtra(Constants.UPDATE_APPLIST_ITEMS,Constants.UPDATE_APPLIST_ITEMS_ID);
					sendBroadcast(intent);

					// remove progress bar
					Intent in = new Intent(Constants.BROADCAST_REMOVE_PROGRESSBAR);
					in.putExtra("url", this.url);
					sendBroadcast(in);

					//ManageActivity
					Intent i = new Intent(
							Constants.BROADCAST_MANAGE_UPDATE_VIEW);
					i.putExtra(Constants.UPDATE_INSTALLED_ITEMS,
							Constants.UPDATE_INSTALLED_ITEMS_ID);
					sendBroadcast(i);

					/*
					 * Intent ient = new
					 * Intent(Constants.BROADCAST_MANAGE_UPDATE_VIEW);
					 * i.putExtra(Constants.UPDATE_INSTALLED_ITEMS,Constants.
					 * UPDATE_MANAGE_ADAPTER_VIEWS_ID); sendBroadcast(ient);
					 */
				}
			} else {
				synchronized (this) {
					Log.e(TAG, "install failed,pkgName=" + this.pkgName);
					// noti
					StoreException se = new StoreException();
					se.setHandler(serviceHandler);
					se.obtainMessage();
					se.sendMessage(Constants.HandlerWhatFalgInstallFailed,
							this.appId);
					se = null;

					invalidateManageViews();

					// refresh applist adapter,listview
					Intent intent = new Intent(
							Constants.BROADCAST_APPLIST_UPDATE_VIEW);
					intent.putExtra(Constants.UPDATE_APPLIST_ITEMS,
							Constants.UPDATE_APPLIST_ALL_ITEMS_ID);
					sendBroadcast(intent);

					// 给BasketActivity
					// refreshBasketView();
				}
			}
			// 处理下载中的app id
			if (appId > 0) {
				for (int i = 0; i < ManageActivity.downloadings.size(); i++) {
					if (appId == ManageActivity.downloadings.get(i).intValue()) {
						ManageActivity.downloadings.remove(i);
						break;
					}
				}
				this.printDownloadingId(ManageActivity.downloadings);
			}
			new SystemHelper(sContext).stopStoreService();
		}

		/*
		 * private void refreshBasketView(){ Intent intent = new
		 * Intent(Constants.BROADCAST_BASKET_UPDATE_VIEW);
		 * sendBroadcast(intent); intent=null; }
		 */

		private void printDownloadingId(List<Integer> list) {
			if (null != list) {
				if (list.size() == 0) {
					Log.d(TAG, "no downloading app id");
					return;
				}
				int size = list.size();
				for (int i = 0; i < size; i++) {
					Log.d(TAG, "downloading app id===" + list.get(i).intValue());
				}
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			// Log.i("zhu", "onProgressUpdate is running...");
			Log.i("zhu", "values " + values[0]);
			super.onProgressUpdate(values);
		}
	}

	private void invalidateManageViews() {
		synchronized (this) {
			// update manage views
			Intent i = new Intent(Constants.BROADCAST_MANAGE_UPDATE_VIEW);
			i.putExtra(Constants.UPDATE_INSTALLED_ITEMS,
					Constants.UPDATE_MANAGE_ADAPTER_VIEWS_ID);
			sendBroadcast(i);
		}
	}

	@Override
	public void onBasketListSuccess(StorePackage[] storePackages) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBasketListFailure(APIException e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBasketPackageDownloadCompletionSuccess(boolean completed) {
		// TODO Auto-generated method stub
		Log.i(TAG, String.format("Service download completed? " + completed));
	}

	@Override
	public void onBasketPackageDownloadCompletionFailure(APIException e) {
		// TODO Auto-generated method stub

	}

	public void toast(String msg, boolean isLong) {
		if (isLong) {
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}