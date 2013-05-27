package com.lewa.spm.adapter;


import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lewa.spm.R;
import com.lewa.spm.app.GetRunningApp;

public class AppAdapter extends BaseAdapter{
	private Context mContext;
	private List<AppInfo> mAppList;// include all informations about application
	private List<String> mRunningAppUidList;// the only one parameter of application is uid;
	private GetRunningApp appRunning;
	Double appUsageValue = null;
	@SuppressWarnings("unused")
	private final class ViewHolder{
		ImageView mAppIcon;
		ProgressBar mAppPowerConsumeBar;
		TextView mAppNameTxt;
		TextView mAppPowerConsumeTxt;
		TextView mAppIsRunningTxt;
	}

	public AppAdapter(Context mContext, List<AppInfo> mList) {
		this.mContext = mContext;
		this.mAppList = mList;
		appRunning = new GetRunningApp(mContext);
		mRunningAppUidList = appRunning.queryAllRunningAppInfo();
	}

	
	public int getCount() {
		return mAppList.size();
	}

	
	public Object getItem(int arg0) {
		return mAppList.get(arg0);
	}

	
	public long getItemId(int arg0) {
		return arg0;
	}

	
	public View getView(int position, View convertView, ViewGroup parent) {
		AppInfo appInfo = mAppList.get(position);
		String appName = null;
		ViewHolder holder = null;
		if (convertView == null) {
			View view = LayoutInflater.from(mContext).inflate(R.layout.spm_running_app_listitem, null);
			holder = new ViewHolder();
			holder.mAppIcon = (ImageView)view.findViewById(R.id.spm_running_app_icon);
			holder.mAppPowerConsumeBar = (ProgressBar)view.findViewById(R.id.spm_running_app_progress);
			holder.mAppPowerConsumeBar.setMax(100);
			holder.mAppNameTxt = (TextView)view.findViewById(R.id.spm_running_app_name);
			holder.mAppPowerConsumeTxt = (TextView)view.findViewById(R.id.spm_running_app_power_consume_txt);
			holder.mAppIsRunningTxt = (TextView)view.findViewById(R.id.spm_app_is_running);
			view.setTag(holder);
			convertView = view;
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		holder.mAppIcon.setImageDrawable(appInfo.icon);
		appName = (String) appInfo.label;
		holder.mAppNameTxt.setText(appName);
		if (mRunningAppUidList.contains(String.valueOf(appInfo.uid))){// judge is running or not based on compare the application list and the running list
			holder.mAppIsRunningTxt.setText(mContext.getResources().getString(R.string.spm_app_running));
			holder.mAppIsRunningTxt.setTextColor(0xFF515151);
		}else{
			holder.mAppIsRunningTxt.setText(mContext.getResources().getString(R.string.spm_app_not_started));
			holder.mAppIsRunningTxt.setTextColor(0xFFA6A6A6);
		}
		holder.mAppPowerConsumeTxt.setText(appInfo.powerUsage + "%");
		holder.mAppPowerConsumeBar.setProgress(appInfo.powerUsage.intValue());
		return convertView;
	}
	
	
	public void release(){
		if(appRunning != null){
			appRunning.release();
		}
		if(mAppList != null){
			mAppList.clear();
			mAppList = null;
		}
		if(mRunningAppUidList != null){
			mRunningAppUidList.clear();
			mRunningAppUidList = null;
		}
	}
}