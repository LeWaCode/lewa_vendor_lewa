package com.lewa.face.online;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.GridView;

import com.lewa.face.R;
import com.lewa.face.adapters.online.ThumbnailOnlineAdapter;
import com.lewa.face.app.ThemeApplication;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.server.intf.ClientResolver;
import com.lewa.face.server.intf.NetBaseParam;
import com.lewa.face.util.Logs;
import com.lewa.face.util.ThemeActions;
import com.lewa.face.util.ThemeUtil;

public abstract class OnlineBaseActivity extends Activity implements OnClickListener,OnScrollListener{
	private ClientResolver clientResolver;
	private static final String TAG = OnlineBaseActivity.class.getSimpleName();
    
    protected Context mContext = null;
    
    private Button mSetNetWorkNoNetWorkBtn = null;
    private Button mSetNetWorkTimeOutBtn = null;
    private Button mRefreshBtn = null;
    
    private Button mNetworkRetryBtn = null;
    
    private GridView mOnlineGridView = null;
    protected ThumbnailOnlineAdapter mOnlinePreviewAdapter = null;
    
    protected static int totalThemes = 0;
    
    protected ArrayList<ThemeBase> themeBases = new ArrayList<ThemeBase>();
    
    private Object url = null;

    
    public static boolean mBusy = false; //标识是否存在滚屏操作
    
    /**
     * 是不是第一次滑到当前屏
     */
    public boolean isFirstDisplay = true;
    /**
     * 第一次滑到当前屏，然后才加载内容
     */
    public Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            isFirstDisplay = false;
            firstDisplay();
            
        }
        
    };
    
    private void firstDisplay(){
        mContext = this;
        url = initUrl();
        clientResolver = new ClientResolver((NetBaseParam)url, ClientResolver.JSON_IMPL,
        		ClientResolver.DEFAULT_PAGE_SIZE);
        
        if(!ThemeUtil.isNetWorkEnable(mContext)){
            setContentView(R.layout.theme_online_no_network);
            netWorkBad();
        }else{
            
            
            setContentView(R.layout.theme_online_loading);
            new LoadPictrues(mContext).execute("");
        }
    }
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = this;
        ThemeApplication.activities.add(this);
        
    }
    
    private void netWorkBad(){
        mSetNetWorkNoNetWorkBtn = (Button) findViewById(R.id.theme_network_set_nonetwork);
        mSetNetWorkNoNetWorkBtn.setOnClickListener(this);
        
        mRefreshBtn = (Button) findViewById(R.id.theme_refresh);
        mRefreshBtn.setOnClickListener(this);
        
    }
    
    private void setNetWork(){
        Intent intent = new Intent();
        intent.setAction(ThemeActions.SET_NETWORK);
        startActivity(intent);
    }
    
    private void refreshNetWork(){
    	url = initUrl();
        if(ThemeUtil.isNetWorkEnable(mContext)){
            
            
            setContentView(R.layout.theme_online_loading);
            new LoadPictrues(mContext).execute("");
        }else{
            setContentView(R.layout.theme_online_no_network);
            netWorkBad();
        }
    }
    
    private InputStream getNetInputStream(String urlStr){
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.connect();
            if(conn.getResponseCode() == 200){
                return conn.getInputStream();
            }else {
                return null;
            }
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
            conn.disconnect();
            conn = null;
        } catch (IOException e) {
            e.printStackTrace();
            conn.disconnect();
            conn = null;
        }
        return null; 
    }
    
    protected abstract Object initUrl();
    
    /**
     * 在解析过程中可能出现问题，所以要解析过程中是否成功
     * @param inputStream
     * @return
     */
    protected abstract boolean parseExcel(InputStream inputStream);
         
    protected abstract ThumbnailOnlineAdapter onlineAdapterInstance();
    
    private class LoadPictrues extends AsyncTask<String, String, String>{
        
        public LoadPictrues(Context context){}
            
        @Override
        protected String doInBackground(String... parms) {
//            InputStream inputStream = null;
            try {
//                inputStream = getNetInputStream(url);
//                if(inputStream != null){
//                    boolean success = parseExcel(inputStream);
//                    if(success){
//                        return "success";
//                    }else {
//                        return "fail";
//                    }
//                }
            	boolean success = true;
//            	if(mContext.getClass() != IconsStyle.class&&mContext.getClass()!=LauncherStyle.class){
//            		success = clientResolver.count(url);
//            	}
            	if (success) {
					
//					totalThemes = clientResolver.getPageResolver()
//							.getTotalCount();
					themeBases = (ArrayList<ThemeBase>) clientResolver
							.getPageResolver().currentPage();
					return "success";
				} else {
					return "fail";
				}
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
//                try {
//                    if(inputStream != null){
//                        inputStream.close();
//                        inputStream = null;
//                    }
//                } catch (Exception e2) {
//                    // TODO: handle exception
//                }
                
            }
            return "fail";
            
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            
            if(result.equals("success")){
                
                setContentView(R.layout.theme_online_main);
                
                mOnlineGridView = (GridView) findViewById(R.id.online_theme_grid);
                mOnlinePreviewAdapter = onlineAdapterInstance();
                mOnlineGridView.setAdapter(mOnlinePreviewAdapter);
                mOnlineGridView.setOnScrollListener(OnlineBaseActivity.this);
            }else {
                networkTimeout();
            }
            
        }

    }
    
    private void networkTimeout(){
        setContentView(R.layout.theme_online_network_timeout);
        
        mSetNetWorkTimeOutBtn = (Button) findViewById(R.id.theme_network_set_timeout);
        mSetNetWorkTimeOutBtn.setOnClickListener(this);
        
        mNetworkRetryBtn = (Button) findViewById(R.id.network_retry);
        mNetworkRetryBtn.setOnClickListener(this);
        
    }
    

    @Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_IDLE: //Idle态(当前屏幕静止)，进行实际数据的加载显示
			mBusy = false;
			mOnlinePreviewAdapter.notifyDataSetChanged();
			break;	
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			mBusy = true;
			break;
		case OnScrollListener.SCROLL_STATE_FLING:
			mBusy = true;
			break;
		default:
			break;
		}
	}

	@Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.theme_network_set_nonetwork:
        case R.id.theme_network_set_timeout:
        {
            setNetWork();
            break; 
        }
        case R.id.theme_refresh:
        case R.id.network_retry:
        {
            refreshNetWork();
            break; 
        }    
        default:
            break;
        }
        
    }
	
	@Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        //isFirstDisplay = true;
        super.onStop();
    }
    
    @Override
    protected void onDestroy() {
        
        if(themeBases != null){
        	themeBases.clear();
        }
        if(mOnlinePreviewAdapter != null){
        	mOnlinePreviewAdapter.onDestroy();
        }
        ThemeApplication.activities.remove(this);
        super.onDestroy();
    }
    
    @Override
    public void onLowMemory() {
        Log.e(TAG, "onLowMemory and runGC()");
        ThemeUtil.runGC();
        super.onLowMemory();
    }
    
}
