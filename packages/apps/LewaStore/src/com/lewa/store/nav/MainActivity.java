package com.lewa.store.nav;

import java.util.ArrayList;

import com.lewa.os.ui.ViewPagerIndicator.OnPagerSlidingListener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lewa.os.ui.ViewPagerIndicatorActivity;
import com.lewa.store.R;
import com.lewa.store.activity.AppListActivity;
import com.lewa.store.activity.ManageActivity;
import com.lewa.store.activity.SettingActivity;
import com.lewa.store.adapter.PreferencesHelper;
import com.lewa.store.dialog.IMessageDialogListener;
import com.lewa.store.dialog.MessageDialog;
import com.lewa.store.download.Dao;
import com.lewa.store.download.HttpTimer;
import com.lewa.store.utils.Constants;
import com.lewa.store.utils.NetHelper;
import com.lewa.store.utils.SystemHelper;

public class MainActivity extends ViewPagerIndicatorActivity implements OnPagerSlidingListener{ 
	
	private String TAG=MainActivity.class.getSimpleName();

	private final int TAB_INDEX_BROWSER = 0;
    public final static int TAB_INDEX_MANAGE  = 1;
    private final int TAB_INDEX_BASKET  = 2;
    
    private final int MENU_SETTINGS=0;
	private PreferencesHelper sp = null;
    private MessageDialog dialog;
    private ImageButton refreshBtn=null;
    private boolean flag=false;
    
    private ApplicationManager am=null;
    private Dao dao=null;
    
    //子Activity对象
    public AppListActivity  ala=null;
    public ManageActivity ma=null;
//    public BasketActivity ba=null;
    //存储Bundle状态
    private Bundle saveInstance=null;
    private ArrayList<StartParameter> aClasses=null;
    private RelativeLayout layout=null;
    private MessageDialog mDialog = null;
    private Context context=null;
    private Activity activity=null;
    
    private Handler httpHandler=new Handler(){
    	public void handleMessage(Message msg) {
    		if(msg.what==Constants.HTTP_REQUEST_ERROR){
    			Toast.makeText(MainActivity.this,getString(R.string.cannot_connection_server),Toast.LENGTH_LONG).show();
    		}
    	}
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(aClasses==null){
            aClasses= new ArrayList<StartParameter>();  
        }        
        aClasses.add(new StartParameter(AppListActivity.class, null, R.string.Browser));
        aClasses.add(new StartParameter(ManageActivity.class,  null, R.string.manager));
//        aClasses.add(new StartParameter(BasketActivity.class,null, R.string.basket));        
        setupFlingParm(aClasses, R.layout.main, R.id.pager_detail_indicator, R.id.pager_detail_content);
        //设置默认显示哪个
        super.onCreate(savedInstanceState);
        this.setOnTriggerPagerChange(this);
        
        layout=(RelativeLayout) this.findViewById(R.id.title_bar);
        refreshBtn=(ImageButton) layout.findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new ButtonEventListener());
        
        sp = new PreferencesHelper(this,Constants.updateFlag);
        if(null!=savedInstanceState){
        	saveInstance=savedInstanceState;
        }
        registerRefreshReceiver();
        
        int gotowhere=getIntent().getIntExtra("gotomanager",0);
        if(gotowhere==1){
            this.setDisplayScreen(gotowhere);
        }        
        this.context=this.getApplicationContext();
        this.activity=this;
//        dialog=new MessageDialog(this);        
//        dialog.ShowConfirm(1,"乐蛙提示","是否恢复您的历史数据?",new DialogClickListener());
        
        if(NetHelper.isAccessNetwork(context)){
        	new HttpTimer(Constants.HTTP_TIME_OUT_NUM,httpHandler).start();
        } 
        
        new SystemHelper(this).displayBriefMemory();
    }
    
    private void setActivityObject(){
    	if(null==ala){
    		ala=(AppListActivity) super.getItemActivity(TAB_INDEX_BROWSER);
    	}
    	if(null==ma){
    		ma=(ManageActivity) super.getItemActivity(TAB_INDEX_MANAGE);
    	}
    	/*if(null==ba){
    		ba=(BasketActivity) super.getItemActivity(TAB_INDEX_BASKET);
    	}       */
    }
    
    private static MainActivity instance=null;
    
    public static synchronized MainActivity getInstance(){
		if(instance==null){
			Log.e("cstore", "no single task");
			instance=new MainActivity();
		}
		return instance;
	}
    
	@Override
	public void onChangePagerTrigger(int pos) {
	}
    
    class DialogClickListener implements IMessageDialogListener{

		@Override
		public void onDialogClickOk(int requestCode) {
			// TODO Auto-generated method stub
			setDisplayScreen(TAB_INDEX_MANAGE);//手动切换到第二个swap
		}

		@Override
		public void onDialogClickCancel(int requestCode) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDialogClickClose(int requestCode) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    
	class SetNetworkDialogOnClickListener implements IMessageDialogListener {

		@Override
		public void onDialogClickOk(int requestCode) {
			Intent intent=new Intent();
			intent.setAction(Constants.ACTION_SET_NETWORK);
			startActivity(intent);
		}

		@Override
		public void onDialogClickCancel(int requestCode) {
		}

		@Override
		public void onDialogClickClose(int requestCode) {
		}

	}
    
    boolean isRereshing = false;//是否在刷新    
    
    class ButtonEventListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
		    int vid=v.getId();
		    if(vid==R.id.refreshBtn){
		    	setActivityObject();
		    	Log.e(TAG,"isRefreshing=="+isRereshing);
		    	if(!NetHelper.isAccessNetwork(context)){
		    	    mDialog=new MessageDialog(activity);
//		    	    mDialog.ShowInfo(getString(R.string.please_set_network));
		    	    mDialog.ShowSpecConfirm(Constants.MESSAGE_DIALOG_ID_03,
							getString(R.string.MessageDialog_no_network),
							getString(R.string.MessageDialog_no_network_cannot_refresh),
							new SetNetworkDialogOnClickListener());
//		    	    mDialog.ShowInfo(getString(R.string.MessageDialog_no_network),getString(R.string.MessageDialog_no_network_cannot_refresh));
		    	    return;
		    	}
		    	if(!isRereshing){
		    		if(NetHelper.isAccessNetwork(context)){
		    	       new HttpTimer(Constants.HTTP_TIME_OUT_NUM,httpHandler).start();
		    	    }        
		    	    sp.putValue(Constants.updateTime,Constants.onClickRefresh);
//		    		Log.e(TAG,"begin refresh...");
		    	    ala.onRefresh();
		    	    ma.onRefresh();
//		    	    ba.onRefresh();
			    	isRereshing=true;
		    	}else{
		    		Log.e(TAG,"refreshing...");
		    		Toast.makeText(MainActivity.this,getString(R.string.toast_refreshing),Toast.LENGTH_SHORT).show();
		    	}
/*		    	new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {							
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					    MainActivity.this.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								isRereshing=false;	
							}
						});
					}
				}).start();*/
		    }
		}    	
    }
    
    /*************************************/
    private RefreshStatusReceiver refreshReceiver=null;
    
    public void registerRefreshReceiver() {
    	refreshReceiver = new RefreshStatusReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.BROADCAST_REFRESH_STATUS);
		registerReceiver(refreshReceiver, intentFilter);
	}

	public void unRegisterRefreshReceiver() {
		if (null != refreshReceiver) {
			this.unregisterReceiver(refreshReceiver);
			refreshReceiver = null;
		}
	}
	
	class RefreshStatusReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			isRereshing=false;
		}
		
	}
    /*************************************/    

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_SETTINGS, 0,getString(R.string.menu_setting)).setIcon(R.drawable.ic_menu_setting);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTINGS:
			Intent intent=new Intent(MainActivity.this,SettingActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
		return false;
	} 
	
    class loadingThread implements Runnable{

		@Override
		public void run() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(!NetHelper.checkWifi(MainActivity.this)){
				MainActivity.this.runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						Toast.makeText(MainActivity.this,getString(R.string.check_wifi_status),Toast.LENGTH_SHORT).show();
					}
				});
			}
		}    	
    }
    
    private void setActivityObjectDestroy(){
    	this.ala=null;
    	this.ma=null;
//    	this.ba=null;
    	this.sp=null;
    	this.dialog=null;
    	this.refreshBtn=null;
    	this.am=null;
    	this.dao=null;
    	this.saveInstance=null;
		if(instance!=null){
			instance=null;
		}
    	System.gc();
    }
	
	@Override
	protected void onPause() {
		super.onPause();
		System.gc();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			Log.e(TAG,"keycode back");
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG,"MainActivity onDestroy()");
		super.onDestroy();
		flag=true;
		this.setActivityObject();
		this.setAppListActivityDestroy();
		this.setManageActivityDestroy();
//		this.setBasketActivityDestroy();
		this.unRegisterRefreshReceiver();
		this.setActivityObjectDestroy();
		System.gc();
	}
	
	private void setAppListActivityDestroy(){
		if(null!=ala && ala.allAppList!=null){
			ala.allAppList.clear();
			ala.allAppList=null;
		}
		if(null!=ala && ala.systemAppPackagesMap!=null){
			ala.systemAppPackagesMap.clear();
			ala.systemAppPackagesMap=null;
		}
		if(null!=ala && ala.systemMap!=null){
			ala.systemMap.clear();
			ala.systemMap=null;
		}
		if(null!=ala && ala.categories!=null){
			ala.categories.clear();
			ala.categories=null;
		}
		if(null!=ala && ala.packageCategories!=null){
			ala.packageCategories.clear();
			ala.packageCategories=null;
		}
		if(null!=ala && ala.pkg!=null){
			ala.pkg=null;
		}
		if (null!=ala && ala.adapter!=null) {
			ala.adapter.unRegisterUpdateAppListReceiver();
		}		
	}
	
	private void setManageActivityDestroy(){
		if(null!=ma && null!=ma.dao){
			ma.dao.closeDb();
			ma.dao=null;
		}		
		if(null!=ma && ma.allAppList!=null){
			ma.allAppList.clear();
			ma.allAppList=null;
		}
			
		if(null!=ma && ma.adapter!=null){
			ma.adapter=null;
		}
		if(null!=ma && ma.systemAppPackagesMap!=null){
			ma.systemAppPackagesMap.clear();
			ma.systemAppPackagesMap=null;
		}
		if(null!=ma && ma.pkg!=null){
			ma.pkg=null;
		}
		if(null!=ma && ma.systemMap!=null){
			ma.systemMap.clear();
			ma.systemMap=null;
		}
		/*if(null!=ma && ma.downloadings!=null){
			ma.downloadings.clear();
		}
		if(null!=ma && ma.MapSize!=null){
			ma.MapSize.clear();
		}
		if(null!=ma && ma.downloaders!=null){
			ma.downloaders.clear();
		}
		if(null!=ma && ma.ProgressBars!=null){
			ma.ProgressBars.clear();
		}*/
	}
	
/*	private void setBasketActivityDestroy(){
		if(null!=ba && ba.allAppList!=null){
			ba.allAppList.clear();
			ba.allAppList=null;
		}
		if(null!=ba && ba.pkg!=null){
			ba.pkg=null;
		}
		if(null!=ba && ba.user!=null){
			ba.user=null;
		}
		if(null!=ba && ba.myCloudList!=null){
			ba.myCloudList.clear();
			ba.myCloudList=null;
		}
		if(null!=ba && ba.systemAppPackagesMap!=null){
			ba.systemAppPackagesMap.clear();
			ba.systemAppPackagesMap=null;
		}
		if(null!=ba && ba.systemMap!=null){
			ba.systemMap.clear();
			ba.systemMap=null;
		}
	}*/
}