package com.lewa.face.local;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import com.lewa.face.ThemeCustomizePreference;
import com.lewa.face.adapters.local.ThumbnailLocalAdapter;
import com.lewa.face.app.ThemeApplication;
import com.lewa.face.filemanager.ThemeFileManager;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.pojos.ThemeModelInfo;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeUtil;
import com.lewa.os.ui.ActivityResultBridge;
import com.lewa.os.ui.ActivityResultBridge.ActivityResultReceiver;

public abstract class LocalBaseActivity extends Activity implements OnClickListener,OnScrollListener,ActivityResultReceiver{

    private static final String TAG = LocalBaseActivity.class.getSimpleName();
    
    protected GridView mLocalGridView = null;
    protected ThumbnailLocalAdapter mThumbnailLocalAdapter = null;
    protected ArrayList<ThemeBase> themeBases = null;
    protected Context mContext = null;
    /**
     * 主Activity那边传过来的
     */
    protected ActivityResultBridge arb = null;
    private Button mImportThemeBtn = null;
    
    public static boolean mBusy = false; //标识是否存在滚屏操作
    
    public static boolean isCurrentPage = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        
        
        mContext = this;
        
        setContentView(R.layout.theme_local_main);
        
        initViews();
        
        mImportThemeBtn = (Button) findViewById(R.id.import_theme);
        mImportThemeBtn.setOnClickListener(this);
        
        
        mLocalGridView = (GridView) findViewById(R.id.local_theme_grid);
        mLocalGridView.setOnScrollListener(this);
        themeBases = getThemeBases();
        
        int size = themeBases.size();
        if((size == 1 && ThemeConstants.DEFAULT_THEME_PKG.equals(themeBases.get(0).getPkg())) || size == 0){
            
            Log.e(TAG, "The local.obj file is wrong and load data again");
            new LoadLocalModelInfo().execute("");
        }else {
            mThumbnailLocalAdapter = localAdapterInstance();
            mLocalGridView.setAdapter(mThumbnailLocalAdapter);
            mThumbnailLocalAdapter.registerReceiver();
        }
        
        ThemeApplication.activities.add(this);
    }
    
    protected abstract void initViews();
    protected abstract ArrayList<ThemeBase> getThemeBases();
    protected abstract ThumbnailLocalAdapter localAdapterInstance();
    protected abstract void parseLocalModelInfo();

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
       
    }
	protected ArrayList<ThemeBase> addDefaultTheme(ArrayList<ThemeBase> bases) {
		ThemeBase defaultTheme = new ThemeBase(getString(R.string.default_word));
		if (bases.contains(defaultTheme)) {
			return bases;
		} else {
			String defaultlwt = ThemeConstants.THEME_LWT + "/"
					 + "default.lwt";
			File fDefaultLwt = new File(defaultlwt);
			String themePkg = fDefaultLwt.getName();

			ThemeModelInfo themeModelInfo = new ThemeModelInfo(themePkg);
			ThemeBase launcherStyle = new ThemeBase(themeModelInfo, themePkg,
					null, true);

			launcherStyle.setCnAuthor(launcherStyle.getCnAuthor());
			launcherStyle.setEnAuthor(launcherStyle.getEnAuthor());
			launcherStyle.setCnName(launcherStyle.getCnName());
			launcherStyle.setEnName(launcherStyle.getEnName());
			launcherStyle.setPkg(themePkg);
			launcherStyle.setSize(ThemeUtil.fileLengthToSize(fDefaultLwt
					.length()));
			bases.add(ThemeConstants.DEFAULT,launcherStyle);
			return bases;
		}
	}
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        ThemeApplication.activities.remove(this);
        if(mThumbnailLocalAdapter != null){
            mThumbnailLocalAdapter.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
        case OnScrollListener.SCROLL_STATE_IDLE: //Idle态(当前屏幕静止)，进行实际数据的加载显示
            mBusy = false;
            mThumbnailLocalAdapter.notifyDataSetChanged();
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
        case R.id.import_theme:
        {
            Intent intent = new Intent();
            intent.setClass(mContext, ThemeFileManager.class);
            startActivity(intent);
            break;
        }
        case R.id.customize:
        {
            Intent intent = new Intent();
            intent.setClass(mContext, ThemeCustomizePreference.class);
            startActivity(intent);
            break;
        }
        default:
            break;
        }
    }
    
    /**
     * 从主的Activity那边把ActivityresultBridge传过来
     * @param ar
     */
    public void setActivityBirageResult(ActivityResultBridge ar){
        arb = ar;
    }
    
    
    @Override
    public void registerActivityResultBridge(ActivityResultBridge arg0) {
        // TODO Auto-generated method stub
        
    }
    
    
    private class LoadLocalModelInfo extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            parseLocalModelInfo();
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mThumbnailLocalAdapter = localAdapterInstance();
            mLocalGridView.setAdapter(mThumbnailLocalAdapter);
            mThumbnailLocalAdapter.registerReceiver();
        }
        
        
    }
    
    protected Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch(msg.what){
            case 1:
                /**
                 * 如果这里不再set一下Adapter好像UI界面有时候不会更新
                 */
                mLocalGridView.setAdapter(mThumbnailLocalAdapter);
                mThumbnailLocalAdapter.notifyDataSetChanged();
                //mLocalGridView.invalidateViews();
                Log.i(TAG, "update UI");
                break;
            }
        }
        
    };
    
}
