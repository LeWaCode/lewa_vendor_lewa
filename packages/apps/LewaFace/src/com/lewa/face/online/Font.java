package com.lewa.face.online;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

import com.lewa.face.R;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.server.intf.ClientResolver;
import com.lewa.face.server.intf.NetBaseParam;
import com.lewa.face.server.intf.UrlParam;
import com.lewa.face.util.ThemeActions;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeUtil;
import com.lewa.face.widget.FontsPreferenceScreen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 
 * @author fulw
 *
 */
public class Font extends PreferenceActivity implements OnClickListener,
		OnSharedPreferenceChangeListener {
	private ClientResolver clientResolver;
    private static final String TAG = Font.class.getSimpleName();
    
    protected Context mContext = null;
    
    private Button mSetNetWorkNoNetWorkBtn = null;
    private Button mSetNetWorkTimeOutBtn = null;
    private Button mRefreshBtn = null;
    
    private Button mNetworkRetryBtn = null;
    
	private Object url = null;
    
    private int totalThemes = 0;
    
    private PreferenceScreen preferenceScreen;
    
    /**
     * 临时的Preference
     */
    private Preference tempPreference = null;
    
    private Drawable defaultThumbnail = null;
    
    private Drawable downloaded = null;
    
    private Drawable downloading = null;
    
    private SharedPreferences sharedPreferences = null;
    
    /**
     * 是不是第一次滑到当前屏
     */
    public boolean isFirstDisplay = true;
    
    private ArrayList<ThemeBase> themeBases = new ArrayList<ThemeBase>();
    
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
        
        if(!ThemeUtil.isNetWorkEnable(mContext)){
            setContentView(R.layout.theme_fonts_online_no_network);
            netWorkBad();
        }else{
            
            url = initUrl();
			clientResolver = new ClientResolver((NetBaseParam)url, ClientResolver.JSON_IMPL,
					ClientResolver.DEFAULT_PAGE_SIZE);
            setContentView(R.layout.theme_fonts_online_loading);
            
            new LoadFonts(mContext).execute("");
        }
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
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
        if(ThemeUtil.isNetWorkEnable(mContext)){
            
            url = initUrl();
            
            setContentView(R.layout.theme_fonts_online_loading);
            
            new LoadFonts(mContext).execute("");
        }else{
            setContentView(R.layout.theme_fonts_online_no_network);
            netWorkBad();
        }
    }
    
    private void networkTimeout(){
        setContentView(R.layout.theme_fonts_online_network_timeout);
        
        mSetNetWorkTimeOutBtn = (Button) findViewById(R.id.theme_network_set_timeout);
        mSetNetWorkTimeOutBtn.setOnClickListener(this);
        
        mNetworkRetryBtn = (Button) findViewById(R.id.network_retry);
        mNetworkRetryBtn.setOnClickListener(this);
        
    }
    
	private Object initUrl() {
        // TODO Auto-generated method stub
		return UrlParam.newUrlParam(UrlParam.FONT);
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
    
    private boolean parseExcel(InputStream inputStream) {
        Workbook book = null;
        try {  
            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("UTF-8"); 
            book = Workbook.getWorkbook(inputStream,ws);
            book.getNumberOfSheets();  
            Sheet sheet = book.getSheet(0); //get the first sheet 
            int totalRows = sheet.getRows();
            totalThemes = totalRows - 1;
              
            for (int i = 1; i < totalRows; ++i) {// the first row can't contain
                
                ThemeBase themeBase = new ThemeBase(null,ThemeConstants.LEWA,null,false);
                
                themeBase.setCnName(sheet.getCell(1,i).getContents());
                themeBase.setEnName(sheet.getCell(2,i).getContents());
                themeBase.setCnAuthor(sheet.getCell(3,i).getContents());
                themeBase.setEnAuthor(sheet.getCell(4,i).getContents());
                themeBase.setVersion(sheet.getCell(5,i).getContents());
                themeBase.setSize(sheet.getCell(6, i).getContents());
                themeBase.setPkg(sheet.getCell(7,i).getContents());
                //themeBase.setName(sheet.getCell(8,i).getContents());
                
                themeBases.add(themeBase);
                
            }
            Log.i(TAG, "Total theme numbers is " + totalThemes);
             return true;
        } catch (Exception e) {  
           e.printStackTrace();
           return false;
        }finally{
            
            try {
                if(inputStream != null){
                    inputStream.close();
                    inputStream = null;
                }
                if(book != null){
                    book.close();
                    book = null;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }  
    }
    
    
    private class LoadFonts extends AsyncTask<String, String, String>{
        
        public LoadFonts(Context context){}
            
        @Override
        protected String doInBackground(String... parms) {
			// InputStream inputStream = null;
            try {
				// inputStream = getNetInputStream(url);
				//
				// if(inputStream != null){
				// boolean success = parseExcel(inputStream);
				// if(success){
				// return "success";
				// }else {
				// return "fail";
				// }
				// }
				boolean success = true;
                    if(success){

					// totalThemes = clientResolver.getPageResolver()
					// .getTotalCount();
					themeBases = (ArrayList<ThemeBase>) clientResolver
							.getPageResolver().currentPage();
                        return "success";
                    }else {
                        return "fail";
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
//				try {
//					if (inputStream != null) {
//						inputStream.close();
//						inputStream = null;
//					}
//				} catch (Exception e2) {
//					// TODO: handle exception
//				}
                
            }
            return "fail";
            
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                if(result.equals("success")){
                    setContentView(R.layout.theme_fonts_preference);
                    
                    addPreferencesFromResource(R.xml.fonts);
                    
					defaultThumbnail = getResources().getDrawable(
							R.drawable.bg_text_style);
                    
					downloaded = getResources().getDrawable(
							R.drawable.theme_downloaded);
                    
					downloading = getResources().getDrawable(
							R.drawable.theme_downloading);
                    
                    preferenceScreen = getPreferenceScreen();
                    
					sharedPreferences = mContext.getSharedPreferences(
							"DOWNLOADED", Context.MODE_PRIVATE);
					sharedPreferences
							.registerOnSharedPreferenceChangeListener(Font.this);
                    
                    int size = themeBases.size();
                    for(int i=0;i<size;i++){
                        
                        ThemeBase themeBase = themeBases.get(i);
                        
						FontsPreferenceScreen fontsScreen = new FontsPreferenceScreen(
								Font.this);
                        
						String thumbnailPath = new StringBuilder()
								.append(ThemeConstants.THEME_LOCAL_THUMBNAIL)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_FONTS_PREFIX)
								.append(themeBase.getName()).toString();
                        
						Drawable fontsDrawable = Drawable
								.createFromPath(thumbnailPath);
                        
                        if(fontsDrawable == null){
				String url = themeBase.thumbnailpath;
                             Drawable fontsThumbnail = null;
                            if(url != null){
                                File thumbnailFile = new File(thumbnailPath);
    				    fontsThumbnail = downloadDrawable(
    									thumbnailFile, url);
                            }
                            if(fontsThumbnail != null){
                                fontsScreen.setmThumbnail(fontsThumbnail);
                            }else {
                                fontsScreen.setmThumbnail(defaultThumbnail);
                            }
                        }else {
                            fontsScreen.setmThumbnail(fontsDrawable);
                        }
                        
                        if(sharedPreferences.getLong(themeBase.getPkg(), -1) == ThemeConstants.DOWNLOADING){
                            fontsScreen.setmStatusFlag(downloading);
						} else if (sharedPreferences.getLong(
								themeBase.getPkg(), -1) == ThemeConstants.DOWNLOADED) {
                            fontsScreen.setmStatusFlag(downloaded);
                        }
                        
                        if(ThemeUtil.isEN){
                            fontsScreen.setTitle(themeBase.getEnName());
                        }else {
                            fontsScreen.setTitle(themeBase.getCnName());
                        }
                        
                        fontsScreen.setmThemeBase(themeBase);
                        
                        preferenceScreen.addPreference(fontsScreen);
                    }
                    
                }else {
                    networkTimeout();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }

    }

    
    private Drawable downloadDrawable(File thumbnailFile,String url) {
        
        final HttpClient client =  new DefaultHttpClient();
      
        final HttpGet getRequest = new HttpGet(url);
        
        long length = 0;

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.e(TAG, "Can't download bitmap :" + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
           
            if (entity != null) {
                InputStream inputStream = null;
                FlushedInputStream fis = null;
                FileOutputStream fos = null;
                try {
                    length = entity.getContentLength();
                    if(length == 0){
                        Log.e(TAG, "ContentLength == 0");
                        return null;
                    };
                    
                    inputStream = entity.getContent();
                    
                    fis = new FlushedInputStream(inputStream);
                    
                    fos = new FileOutputStream(thumbnailFile);
                    
                    ThemeUtil.writeSourceToTarget(fis, fos);
                    
                    return Drawable.createFromPath(thumbnailFile.getAbsolutePath());
                } finally {
                    if(fos != null){
                        fos.close();
                        fos = null;
                    }
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
        } catch (IOException e) {
            getRequest.abort();
            e.printStackTrace();
        } catch (IllegalStateException e) {
            getRequest.abort();
            e.printStackTrace();
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
    private class FlushedInputStream extends FilterInputStream {
        
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
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        
        Intent intent = new Intent();
        intent.setClass(this, com.lewa.face.preview.slide.online.Font.class);
        intent.putExtra(ThemeConstants.THEMEBASE, ((FontsPreferenceScreen)preference).getmThemeBase());
        
        this.tempPreference = preference;
        
        startActivity(intent);
        
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    /**
     * 如果xml发生了变化也就意味着数据也跟着变化了，key发生变化的那个key-value
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
        
        if(sharedPreferences.getLong(key, -1) == -1){ //如果状态是-1，那么没必要继续执行
            return;
        }else if(sharedPreferences.getLong(key, -1) == ThemeConstants.DOWNLOADING
                || sharedPreferences.getLong(key, -1) == ThemeConstants.DOWNLOADED
                || sharedPreferences.getLong(key, -1) == ThemeConstants.DOWNLOADFAIL){ //不同的状态贴不同的标签
            
            addFlag();
            
            ThemeUtil.dcontrolFlag = "true"; //add by zjyu
        }
       
    }
    
    private void addFlag(){
        
        preferenceScreen.removeAll();
        
        int size = themeBases.size();
        for(int i=0;i<size;i++){
            
            ThemeBase themeBase = themeBases.get(i);
            
            FontsPreferenceScreen fontsScreen = new FontsPreferenceScreen(Font.this);
            
            String thumbnailPath = new StringBuilder().append(ThemeConstants.THEME_LOCAL_THUMBNAIL).append("/")
            .append(ThemeConstants.THEME_THUMBNAIL_FONTS_PREFIX).append(themeBase.getName()).toString();
            
            
            Drawable fontsDrawable = Drawable.createFromPath(thumbnailPath);
            
            if(fontsDrawable == null){
                fontsScreen.setmThumbnail(defaultThumbnail);
            }else {
                fontsScreen.setmThumbnail(fontsDrawable);
            }
            
            if(sharedPreferences.getLong(themeBase.getPkg(), -1) == ThemeConstants.DOWNLOADING){
                fontsScreen.setmStatusFlag(downloading);
            }else if(sharedPreferences.getLong(themeBase.getPkg(), -1) == ThemeConstants.DOWNLOADED){
                fontsScreen.setmStatusFlag(downloaded);
            }else {
                fontsScreen.setmStatusFlag(null);
            }
            
            if(ThemeUtil.isEN){
                fontsScreen.setTitle(themeBase.getEnName());
            }else {
                fontsScreen.setTitle(themeBase.getCnName());
            }
            
            fontsScreen.setmThemeBase(themeBase);
            
            preferenceScreen.addPreference(fontsScreen);
        }
    }
}
