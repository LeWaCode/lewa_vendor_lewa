package com.lewa.store.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.lewa.core.base.APIClient;
import com.lewa.core.base.APIErrorMap;
import com.lewa.core.base.APIException;
import com.lewa.core.base.APIResponse;
import com.lewa.core.base.LewaUser;
import com.lewa.core.base.StorePackage;

import com.lewa.store.R;
import com.lewa.store.adapter.BasketBaseAdapter;
import com.lewa.store.adapter.BasketModel;
import com.lewa.store.adapter.PreferencesHelper;
import com.lewa.store.dialog.MessageDialog;
import com.lewa.store.download.Dao;
import com.lewa.store.download.DownloadHelper;
import com.lewa.store.extras.GooglePackages;
import com.lewa.store.items.LewaBasketHelper;
import com.lewa.store.items.LewaStoreUpdatesChecker;
import com.lewa.store.model.AppInfo;
import com.lewa.store.model.AppSourceDao;
import com.lewa.store.model.BasketDataModel;
import com.lewa.store.model.LewaNotification;
import com.lewa.store.pkg.LaunchApp;
import com.lewa.store.pkg.PkgManager;
import com.lewa.store.utils.Constants;
import com.lewa.store.utils.StrUtils;
import com.lewa.store.utils.NetHelper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * on my cloud
 * 
 * Basket usage: basket.setPackageAsDownloaded(<packageIdInt>);
 * basket.getBasket();
 * 
 * @author ypzhu
 * 
 */
public class BasketActivity extends Activity implements
		LewaBasketHelper.OnBasketResponseListener,
		LewaStoreUpdatesChecker.OnUpdatesResponseListener {

	private String TAG = BasketActivity.class.getSimpleName();

	public ExpandableListView expandListView = null;
	private RelativeLayout loadingLayout = null;
	private Button allInstallBtn = null;
	public BasketBaseAdapter adapter = null;
	public List<AppInfo> allAppList = null;
	private Dao dao = null;
	private AppSourceDao asd = null;
	private LewaNotification notification = null;
	public PkgManager pkg = null;
	private MessageDialog mDialog = null;

	public LewaUser user = null;
	private LewaBasketHelper basket = null;
	private LewaStoreUpdatesChecker updates = null;
	private DownloadHelper downloadHelper = null;

	private PreferencesHelper sp = null;
	private int networkStatusCode = 0;// 0 or 1

	public List<AppInfo> myCloudList = null;
	public List<HashMap<String, Object>> systemAppPackagesMap = null;
	public Map<String, Integer> systemMap = null;

	private Activity activity = null;
	private BasketModel bm = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basket);

		sp = new PreferencesHelper(this, Constants.updateFlag);
		activity = this;
		user = LewaUser.getInstance(this);
		allInstallBtn = (Button) findViewById(R.id.btn_installall);
		loadingLayout = (RelativeLayout) findViewById(R.id.loadingLayout);
		expandListView = (ExpandableListView) findViewById(R.id.list);
		LinearLayout layout = (LinearLayout) findViewById(R.id.introduce);
		this.registerUpdateBasketViewReceiver();
		this.registerAddBasketDataReceiver();
		this.registerRemoveBasketDataReceiver();
		if (NetHelper.isAccessNetwork(this)
				&& user.getRegistrationState() == LewaUser.USER_IS_REGISTERED) {
			Log.i("user state", this.userLoginState() + "");
			downloadHelper = new DownloadHelper(this);
			updates = new LewaStoreUpdatesChecker(this);
			updates.setListener(this);
			dao = new Dao(this);
			asd = new AppSourceDao(this);
			pkg = new PkgManager(this);
			mDialog = new MessageDialog(this);
			notification = new LewaNotification(this);
			bm = new BasketModel(this, pkg);

			adapter = new BasketBaseAdapter(BasketActivity.this, dao, sp,
					activity, pkg, mDialog, expandListView);
			expandListView.setAdapter(adapter);

			if (user.getRegistrationState() == LewaUser.USER_IS_REGISTERED) {
			}
			initPages();
		} else {
			pkg = new PkgManager(this);
			notification = new LewaNotification(this);
			expandListView.setVisibility(View.GONE);
			layout.setVisibility(View.VISIBLE);
			Button gotoLogin = (Button) layout.findViewById(R.id.startButton);
			gotoLogin.setOnClickListener(new ButtonEventListener());
			loadingLayout.setVisibility(View.GONE);
		}
	}

	public void onRefresh() {
		Log.i(TAG,
				"onRefresh(),date==" + sp.getStringValue(Constants.updateTime));
		setContentView(R.layout.basket);

		sp = new PreferencesHelper(this, Constants.updateFlag);
		activity = this;
		user = LewaUser.getInstance(this);
		allInstallBtn = (Button) findViewById(R.id.btn_installall);
		loadingLayout = (RelativeLayout) findViewById(R.id.loadingLayout);
		expandListView = (ExpandableListView) findViewById(R.id.list);
		LinearLayout layout = (LinearLayout) findViewById(R.id.introduce);
		this.registerUpdateBasketViewReceiver();
		this.registerAddBasketDataReceiver();
		this.registerRemoveBasketDataReceiver();
		if (NetHelper.isAccessNetwork(this)
				&& user.getRegistrationState() == LewaUser.USER_IS_REGISTERED) {
			Log.i("user state", this.userLoginState() + "");
			downloadHelper = new DownloadHelper(this);
			updates = new LewaStoreUpdatesChecker(this);
			updates.setListener(this);
			dao = new Dao(this);
			asd = new AppSourceDao(this);
			pkg = new PkgManager(this);
			mDialog = new MessageDialog(this);
			notification = new LewaNotification(this);
			bm = new BasketModel(this, pkg);

			adapter = new BasketBaseAdapter(BasketActivity.this, dao, sp,
					activity, pkg, mDialog, expandListView);
			expandListView.setAdapter(adapter);

			if (user.getRegistrationState() == LewaUser.USER_IS_REGISTERED) {
			}
			initPages();
		} else {
			pkg = new PkgManager(this);
			notification = new LewaNotification(this);
			expandListView.setVisibility(View.GONE);
			layout.setVisibility(View.VISIBLE);
			Button gotoLogin = (Button) layout.findViewById(R.id.startButton);
			gotoLogin.setOnClickListener(new ButtonEventListener());
			loadingLayout.setVisibility(View.GONE);
		}
	}

	Handler dataHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == Constants.HandlerWhatFlagOne) {
				initdatas();
				if (adapter.cloudItemList.size() <= 1
						&& adapter.mobileItemList.size() <= 1) {
					LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View v = mInflater.inflate(R.layout.no_data_error, null);
					setContentView(v);
				}
				loadingLayout.setVisibility(View.GONE);
				expandListView.setVisibility(View.VISIBLE);
				setProperty();

				if (null != sp
						&& sp.getIntValue(Constants.REFRESH_FLAG_STR) == Constants.REFRESH_GETLOCALDATA) {
					Intent intent = new Intent(
							Constants.BROADCAST_REFRESH_STATUS);
					sendBroadcast(intent);
					sp.putValue(Constants.REFRESH_FLAG_STR,
							Constants.REFRESH_NOT_GETLOCALDATA);
					Log.i(TAG, "basket send broadcast refresh ok!");
				}
			}
		}
	};

	private void setProperty() {
		if (null != expandListView) {
			expandListView.setGroupIndicator(null);
			setDefaultExpandGroup(expandListView);
			expandListView
					.setOnGroupClickListener(new BasketGroupClickListener());
			expandListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		}
	}

	public void initPages() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

				systemMap = bm.initSystemMap();

				if (user.getRegistrationState() == LewaUser.USER_IS_REGISTERED) {
					basket = new LewaBasketHelper(BasketActivity.this);
					basket.setBasketResponseListener(BasketActivity.this);
					basket.getBasket();
				}

				Message message = Message.obtain();
				message.what = Constants.HandlerWhatFlagOne;
				dataHandler.sendMessage(message);
			}
		}).start();
	}

	class ButtonEventListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			int vid = view.getId();
			if (vid == R.id.startButton) {

				try {
					if (pkg.isExistAction(Constants.GOTO_LOGIN_ACTION)) {
						if (NetHelper.isAccessNetwork(BasketActivity.this)) {
							Intent i = new Intent();
							i.setAction(Constants.GOTO_LOGIN_ACTION);
							startActivity(i);
						} else {
							Toast.makeText(BasketActivity.this,
									getString(R.string.please_set_network),
									Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(
								BasketActivity.this,
								getString(R.string.please_install_pond_service),
								Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (vid == R.id.btn_installall) {
				// install all
			}
		}
	}

	private View footerView() {
		Button btn = new Button(this);
		btn.setText("安装全部");
		return btn;
	}

	// 用户登录状态
	private int userLoginState() {
		user = LewaUser.getInstance(this);
		user.statUser();
		int stateCode = user.getRegistrationState();
		return stateCode;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	// 数据初始化
	private void initdatas() {
		this.addOnMyCloudtems();
		this.addOnMyPhoneInstalledItems();
	}

	/**
	 * 我的云
	 */
	public void addOnMyCloudtems() {
		if (null != myCloudList && myCloudList.size() > 0) {
			AppInfo ai = null;
			String appUrl = null;
			String packageName = null;
			int size = myCloudList.size();
			for (int i = 0; i < size; i++) {
				ai = myCloudList.get(i);
				packageName = ai.getPackageName().trim();
				if (null != systemMap && !systemMap.containsKey(packageName)) {
					appUrl = ai.getUrl();
					String iconUrl = Constants.DOWNLOAD_ICON_URL
							+ StrUtils.getPackageIntId(appUrl)
							+ Constants.ICON_FILE_SUFFIX;
					adapter.addOnCloudChildItem(ai.getAppId() + "", appUrl,
							iconUrl, ai.getAppName(), "", ai.getAppVersion(),
							ai.getDescription(), packageName);
				}
			}
		}
	}

	public void addOnMyPhoneInstalledItems() {
		List<AppInfo> list = null;
		if (null != asd) {
			list = asd.getMetaAppInfo();
		}
		if (null != list && list.size() > 0) {
			Map<String, Object> map = null;
			String pkgName = null;
			int size = list.size();
			for (int i = 0; i < size; i++) {
				pkgName = list.get(i).getPackageName();
				if (null != bm && null != systemMap
						&& null != systemMap.get(pkgName)) {
					map = bm.getSingleSystemApp(pkgName);
					int appId = list.get(i).getAppId();
					adapter.addMyPhoneChildItem(appId + "", (Drawable) map
							.get("icon"), map.get("appName").toString(), "未知",
							map.get("appVersion").toString(),
							map.get("appDescripition").toString(),
							map.get("packageName").toString());
				}
			}
		}
	}

	public void setDefaultExpandGroup(ExpandableListView elv) {
		int groupCount = 0;
		if (null != adapter) {
			groupCount = adapter.getGroupCount();
		}
		for (int i = 0; i < groupCount; i++) {
			if (null != expandListView) {
				expandListView.expandGroup(i);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "BasketActivity onDestroy()");
		if (dao != null) {
			dao.closeDb();
		}
		if (null != sp) {
			sp.putValue(Constants.REFRESH_FLAG_STR,
					Constants.REFRESH_NOT_GETLOCALDATA);
		}
		this.unRegisterUpdateBasketViewReceiver();
		this.unRegisterAddBasketDataReceiver();
		this.unRegisterRemoveBasketDataReceiver();
		super.onDestroy();
	}

	class BasketGroupClickListener implements
			android.widget.ExpandableListView.OnGroupClickListener {

		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {
			if (null != expandListView) {
				expandListView.expandGroup(groupPosition);
			}
			return true;
		}
	}

	/*********************************************/

	public void processJSON(JSONObject json) {
		Log.d("processJSON", json.toString());
	}

	Handler requestHandler = new Handler() {

		public static final String TAG = "BasketActivity.requestHandler.handleMessage";

		@Override
		public void handleMessage(Message message) {
			APIResponse response = (APIResponse) message.obj;
			JSONObject json = null;
			json = response.getResponseJSON();
			try {
				APIException.sniffResponse(json);
			} catch (APIException e) {
				// either the server returned errors or there is not
				// connectivity
				// please check the APIException error_code, error_message,
				// error_causes
				Log.e(TAG, "APIException");
				return;
			}
			try {
				if (json.has(APIClient.PARAM_OK)
						&& json.getBoolean(APIClient.PARAM_OK)) {
					processJSON(json);
				}
			} catch (JSONException e) {
				// unrecognizable response data
				Log.e(TAG, "JSONException");
			}
		}
	};

	/**
	 * Callback for basket.getBasket() [success]
	 */
	@Override
	public void onBasketListSuccess(StorePackage[] storePackages) {
		myCloudList = new ArrayList<AppInfo>();
		AppInfo ai = null;
		String trueUrl = null;
		if (storePackages.length > 0) {
			int length = storePackages.length;
			for (int i = 0; i < length; i++) {
				// Log.d(TAG, String.format(">>>>>> got package: %s",
				// storePackages[i].getFQN()));
				// System.out.println("Mybasket 数据 index==" + i);
				if (!LaunchApp.isInstallApp(storePackages[i].getFQN().trim())) {
					ai = new AppInfo();
					ai.setAppId(storePackages[i].getPackageInt());// App id
					// ai.setAppName(String.format("%d.%s",storePackages[i].getPackageInt(),storePackages[i].getType()));
					ai.setAppName(storePackages[i].getName());
					ai.setAppVersion(getString(R.string.version_string)
							+ storePackages[i].getVersion());
					trueUrl = downloadHelper
							.getStorePackageURL(storePackages[i]
									.getPackageInt());
					// Log.e(TAG,trueUrl);
					ai.setUrl(trueUrl);
					ai.setDescription(storePackages[i].getDescription());
					ai.setAppLogoUrl(Constants.DOWNLOAD_ICON_URL
							+ storePackages[i].getPackageInt()
							+ Constants.ICON_FILE_SUFFIX);
					ai.setPackageName(storePackages[i].getFQN());
					if (myCloudList != null) {
						myCloudList.add(ai);
					} else {
						// function return, since
						// MainActivity.setAppListActivityDestroy() was called
					}
				}
			}
		}
	}

	/****************** start *******************/
	public UpdateBasketViewReceiver updateViewReceiver = null;

	public void registerUpdateBasketViewReceiver() {
		if (updateViewReceiver == null) {
			updateViewReceiver = new UpdateBasketViewReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.BROADCAST_BASKET_UPDATE_VIEW);
			this.registerReceiver(updateViewReceiver, intentFilter);
		}
	}

	public void unRegisterUpdateBasketViewReceiver() {
		if (null != updateViewReceiver) {
			this.unregisterReceiver(updateViewReceiver);
			updateViewReceiver = null;
		}
	}

	class UpdateBasketViewReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null != expandListView && adapter != null) {
				adapter.notifyDataSetChanged();
				expandListView.invalidateViews();
			}
		}

	}

	private synchronized void removeAllBasketData() {
		if (null != adapter) {
			adapter.child.clear();
			adapter.setCategoryGroups();
		}
		Log.i(TAG, "removeAllBasketData()");
	}

	// refresh basket data
	private void restoreBasketData() {
		this.removeAllBasketData();
		bm = new BasketModel(this, pkg);
		systemMap = bm.initSystemMap();
		this.addOnMyCloudtems();
		this.addOnMyPhoneInstalledItems();
		if (null != adapter) {
			adapter.notifyDataSetChanged();
		}
		Log.i(TAG, "restoreBasketData()");
	}

	/******************* ADD basket data ***********************/
	public AddBasketDataReceiver addBasketDataReceiver = null;

	public void registerAddBasketDataReceiver() {
		if (null == addBasketDataReceiver) {
			addBasketDataReceiver = new AddBasketDataReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.BROADCAST_BASKET_ADD_DATA);
			this.registerReceiver(addBasketDataReceiver, intentFilter);
		}
	}

	public void unRegisterAddBasketDataReceiver() {
		if (null != addBasketDataReceiver) {
			this.unregisterReceiver(addBasketDataReceiver);
			addBasketDataReceiver = null;
		}
	}

	class AddBasketDataReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Log.d(TAG, "AddBasketDataReceiver have received broadcast");
				if (user.getRegistrationState() == LewaUser.USER_IS_REGISTERED) {
					int appId = intent.getIntExtra("appId", 0);
					BasketDataModel bdm = new BasketDataModel();
					if (null != adapter) {
						AppInfo ai = asd.getMetaAppInfoById(appId);
						if (!bdm.isExistMyCloud(adapter.cloudItemList, appId)
								&& null != ai) {
							adapter.addOnCloudChildItem(appId + "",
									ai.getUrl(), ai.getAppLogoUrl(),
									ai.getAppName(), ai.getAppAuthor(),
									ai.getAppVersion(), ai.getDescription(),
									ai.getPackageName());
							adapter.notifyDataSetChanged();
						}
						if (!bdm.isExistMyMobile(adapter.mobileItemList, appId)
								&& null != ai) {
							String packageName = ai.getPackageName();
							if (null != pkg) {
								Map<String, Object> newMap = bm
										.getSingleSystemApp(packageName,
												pkg.getAppPackages());
								Log.d(TAG, "**************************"
										+ newMap.toString());
								if (!adapter.mobileItemList.contains(newMap)) {
									adapter.mobileItemList.add(newMap);
									adapter.notifyDataSetChanged();
								}
							}
						}
						adapter.notifyDataSetChanged();
						if (null != expandListView) {
							setDefaultExpandGroup(expandListView);
							expandListView.invalidate();
						}
					}
				} else {
					Log.e(TAG, "user not register,can not add basket data");
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG,
						"AddBasketDataReceiver onReceive error:"
								+ e.getMessage());
			}
		}
	}

	/******************* end ADD basket data ***********************/

	/******************* remove basket data ***********************/
	public RemoveBasketDataReceiver removeBasketDataReceiver = null;

	public void registerRemoveBasketDataReceiver() {
		if (null == removeBasketDataReceiver) {
			removeBasketDataReceiver = new RemoveBasketDataReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.BROADCAST_BASKET_REMOVE_DATA);
			this.registerReceiver(removeBasketDataReceiver, intentFilter);
		}
	}

	public void unRegisterRemoveBasketDataReceiver() {
		if (null != removeBasketDataReceiver) {
			this.unregisterReceiver(removeBasketDataReceiver);
			removeBasketDataReceiver = null;
		}
	}

	class RemoveBasketDataReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (user.getRegistrationState() == LewaUser.USER_IS_REGISTERED) {
				String packageName = intent.getStringExtra("packageName");
				if (null != adapter) {
					BasketDataModel bdm = new BasketDataModel();
					bdm.removeMyMobile(adapter.mobileItemList, packageName);
					adapter.notifyDataSetChanged();
					expandListView.invalidateViews();
				}
			}
		}
	}

	/******************* end remove basket data ***********************/

	/****************** end ******************/

	/**
	 * Callback for basket.getBasket() [failed]
	 */
	@Override
	public void onBasketListFailure(APIException e) {
		Log.d(TAG,
				String.format(">>>>>> basket failed to load: \"%s\"",
						e.getErrorDescription()));
		Log.d(TAG, e.getRawResponse());
		this.resolveError(e);
	}

	/**
	 * Callback for basket.setPackageAsDownloaded(<packageIdInt>) [success]
	 */
	@Override
	public void onBasketPackageDownloadCompletionSuccess(boolean completed) {
		Log.d(TAG, String.format(">>>>>> basket updated: %s", completed));
	}

	/**
	 * Callback for basket.setPackageAsDownloaded(<packageIdInt>) [failure]
	 */
	@Override
	public void onBasketPackageDownloadCompletionFailure(APIException e) {
		Log.d(TAG,
				String.format(">>>>>> basket failed to update: \"%s\"",
						e.getErrorDescription()));
		Log.d(TAG, e.getRawResponse());
		this.resolveError(e);
	}

	@Override
	public void onFetchUpdatesSuccess(StorePackage[] storePackages) {
		if (storePackages != null) {
			int length = storePackages.length;
			for (int i = 0; i < length; i++) {
				Log.d(TAG, String.format("found update on package %d", i + 1));
			}
		}
	}

	@Override
	public void onFetchUpdatesFailure(APIException e) {
		// handle exception
	}

	@Override
	public void onFetchSystemMessage(String message) {
		// stub-out for push messages
	}

	private void resolveError(APIException e) {
		String code = e.getErrorCode();
		if (code.equals(APIErrorMap.CLIENT_IO_EXCEPTION)) {
			Log.e(TAG, "broken i/o pipe");
		} else if (code.equals(APIErrorMap.ERROR_FORBIDDEN)) {
			Log.e(TAG, "user forbidden!");
		} else if (code.equals(APIErrorMap.ERROR_SERVER_ERROR)
				|| code.equals(APIErrorMap.ERROR_SERVICE_UNAVAILABLE)) {
			Log.e(TAG, "server is messed up");
		}
	}
}