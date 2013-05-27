package com.lewa.face.filemanager;



import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;



import com.lewa.face.R;
import com.lewa.face.filemanager.ThemeListAdapter.ViewHolder;
import com.lewa.face.util.ThemeActions;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeFileFilter;
import com.lewa.face.util.ThemeUtil;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ThemeFileManager extends Activity implements OnItemClickListener,OnClickListener{
    
    private static final String TAG = ThemeFileManager.class.getSimpleName();
    
	private String ROOTPATH = ThemeConstants.SDCARD_ROOT_PATH;
	private String HAVE_THEMES = "have_themes";
	private String NO_THEMES = "no_themes";
	
	private String newPath; 
	private String importControl = "true"; // add by zjyu
	
    private ListView themeList;
    private ProgressBar searchThemeBar;
    private ImageView noThemeImage;
    private TextView noThemeRemind;
    private Button importThemeBtn;
    private Button importCancelBtn;
    private ProgressDialog themeCopyProgressDialog;
    
    private Context mContext;
    private ArrayList<ThemeFileInfo> themeFileInfos = new ArrayList<ThemeFileInfo>();
    private ArrayList<String> clickThemePath = new ArrayList<String>();
    private ArrayList<String> clickThemeName = new ArrayList<String>();
    
    
    /**
     * 已经导入或者下载过的主题，也就是在"/LEWA/theme/lwt"目录下的lwt文件
     */
    private ArrayList<String> addedThemes = null;
    /**
     * 记录已经导入过的主题名字
     */
    private ArrayList<String> addedFilesName = new ArrayList<String>();
    /**
     * 记录不支持的主题名字
     */
    private ArrayList<String> notSupportName = new ArrayList<String>();
    /**
     * 记录旧的主题名字
     */
    private ArrayList<String> oldThemeName = new ArrayList<String>();
    
    private ThemeListAdapter themeListAdapter = null;
    
    /**
     * how many themes copyed over
     */
    private static int theme_copy_over_count = 0;
    /**
     * how many themes selected to wish importing
     */
    private static int theme_select_count = 0;
    /**
     * to count how many themes checkbox click
     */
    private static int theme_select_number = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mContext = this;
        
        setContentView(R.layout.theme_import);
        
        themeList = (ListView) findViewById(R.id.theme_list);
        themeList.setItemsCanFocus(false);
        themeList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        searchThemeBar = (ProgressBar) findViewById(R.id.search_theme_bar);
        noThemeImage = (ImageView) findViewById(R.id.no_theme_image);
        noThemeRemind = (TextView) findViewById(R.id.no_theme_remind);
        importThemeBtn = (Button) findViewById(R.id.import_theme);
        importThemeBtn.setOnClickListener(this);
        importCancelBtn = (Button) findViewById(R.id.import_cancel);
        importCancelBtn.setOnClickListener(this);
        
        new SearchLwtFile(ROOTPATH).execute("");
        
    }


    private class SearchLwtFile extends AsyncTask<String,String,String>{
        
        private String searchPath;

        public SearchLwtFile(String searchPath){
            this.searchPath = searchPath;
        }
        
        @Override
        protected String doInBackground(String... parameters) {
    	    Drawable icon = mContext.getResources().getDrawable(R.drawable.theme_folder);
    	    SearchFileFilter searchFileFilter = new SearchFileFilter();
    	    
    		searchTheme(searchPath,icon,searchFileFilter);
        	
        	if(themeFileInfos.size() != 0){
        		return HAVE_THEMES;
        	}else {
        		return NO_THEMES;
			}
        	
        }

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
                     if(themeList == null || importThemeBtn == null 
                        || importCancelBtn == null || searchThemeBar == null || noThemeRemind == null){
                            return;
                        }
			if(result.equals(HAVE_THEMES)){
				themeListAdapter = new ThemeListAdapter(mContext, themeFileInfos);
				themeList.setAdapter(themeListAdapter);
				
				themeList.setItemsCanFocus(false);
				themeList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				themeList.setOnItemClickListener(ThemeFileManager.this);
				
				themeList.setVisibility(View.VISIBLE);
				importThemeBtn.setVisibility(View.VISIBLE);
				importThemeBtn.setText(getString(R.string.theme_import, ""));
				importCancelBtn.setVisibility(View.VISIBLE);
				searchThemeBar.setVisibility(View.GONE);
				noThemeRemind.setVisibility(View.GONE);
			}else {
				themeList.setVisibility(View.GONE);
				importThemeBtn.setVisibility(View.GONE);
				importCancelBtn.setVisibility(View.GONE);
				searchThemeBar.setVisibility(View.GONE);
				noThemeImage.setVisibility(View.VISIBLE);
				noThemeRemind.setVisibility(View.VISIBLE);
				
			}
		}
        
        
    }
    
    private ArrayList<String> alreadyAddedThemes(){
    	
    	ArrayList<String> addedThemes = new ArrayList<String>();
    	
    	File lwtRoot = new File(ThemeConstants.THEME_LWT);
    	File[] lwts = lwtRoot.listFiles(new ThemeFileFilter(".lwt", ThemeFileFilter.ENDSWITH));
    	
    	if(lwts != null){
    	    for(File lwt : lwts){
                addedThemes.add(lwt.getName());
            } 
    	}
    	
    	return addedThemes;
    }
    
    private void searchTheme(String rootPath,Drawable icon,SearchFileFilter searchFileFilter){
        if(rootPath.equals(ThemeConstants.LEWA_THEME_PATH)){
            return;
        }
        File rootFile = new File(rootPath);
        if(!rootFile.exists()){
            return;
        }
        File[] rootFiles = rootFile.listFiles(searchFileFilter);
        int fileCount = 0;
        if(rootFiles != null){
            fileCount = rootFiles.length;
        }
        if(fileCount != 0){
            for(int i=0;i<fileCount;i++){
                if(rootFiles[i].getName().endsWith(".lwt")  && rootFiles[i].getPath().indexOf("theme") == -1){
                	
                    ThemeFileInfo themeFileInfo = new ThemeFileInfo();
                    
                    themeFileInfo.setFileIcon(icon);
                    themeFileInfo.setFileName(rootFiles[i].getName());
                    themeFileInfo.setFilePath(rootFiles[i].getPath());
                    themeFileInfo.setFileDate(rootFiles[i].lastModified());
                    themeFileInfo.setFileSize(rootFiles[i].length());
                    
                    themeFileInfos.add(themeFileInfo);
                }else{
                    if(rootFiles[i].getPath().indexOf(ThemeConstants.LEWA) != -1){
                        searchTheme(rootFiles[i].getPath(),icon,searchFileFilter);
                    }
                }
            }
        }
        
    }
    
    private class SearchFileFilter implements FileFilter{

        @Override
        public boolean accept(File file) {
            if((file.isDirectory() && file.getName().equals(ThemeConstants.LEWA)) || file.getName().endsWith(ThemeConstants.BUFFIX_LWT)){
                return true;
            }
            return false;
        }
        
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        
        if(parent.equals(themeList)){
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            CheckBox themeCheckBox = viewHolder.themeCheckBox;
            themeCheckBox.toggle();
            ThemeListAdapter.isSelected.put(position, themeCheckBox.isChecked());
            ThemeFileInfo themeFileInfo = themeFileInfos.get(position);
            
            if(themeCheckBox.isChecked()){
                ++theme_select_number;
                clickThemePath.add(themeFileInfo.getFilePath());
                clickThemeName.add(themeFileInfo.getFileName());
            }else{
                --theme_select_number;
                clickThemePath.remove(themeFileInfo.getFilePath());
                clickThemeName.remove(themeFileInfo.getFileName());
            }
            if(theme_select_number > 0){
                importThemeBtn.setText(getString(R.string.theme_import, "(" + theme_select_number + ")"));
            }else {
                importThemeBtn.setText(getString(R.string.theme_import, ""));
            }
            
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.import_theme:
        {
            theme_select_count = clickThemePath.size();
            
            if(theme_select_count == 0){
                Toast.makeText(mContext, R.string.theme_no_select, Toast.LENGTH_SHORT).show();
                return;
            }
            
            themeCopyProgressDialog = new ProgressDialog(mContext);
            themeCopyProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            themeCopyProgressDialog.setCancelable(false);
            themeCopyProgressDialog.setTitle(R.string.theme_copy_progressbar_title);
            themeCopyProgressDialog.setMessage(mContext.getString(R.string.theme_copy_progressbar_message));
            themeCopyProgressDialog.setMax(theme_select_count);
            themeCopyProgressDialog.show();
            
//          String newPath = ThemeConstants.THEME_LWT;
            newPath = ThemeConstants.THEME_LWT;
            
            addedThemes = alreadyAddedThemes();
            
//            for (int i = 0; i < theme_select_count; i++) {
//                new CopyThemeFile(clickThemePath.get(0),
//                        new StringBuilder().append(newPath).append("/")
//                                .append(clickThemeName.get(0))
//                                .toString(), clickThemeName.get(0))
//                        .execute("");
//            }
            
            new ImportThread().start();
            
            break; 
        }
        case R.id.import_cancel:
        {
            finish();
            break;
        }
        default:
            break;
        }
       
    }

    private class CopyThemeFile extends AsyncTask<String,String,String>{
        
        private String source;
        private String target;
        private String themePkg;

        public CopyThemeFile(String source,String target,String themePkg){
            this.source = source;
            this.target = target;
            this.themePkg = themePkg;
        }

        @Override
        protected String doInBackground(String... parameters) {
            
        	File srcFile = new File(source);
        	String srcFileName = srcFile.getName();
        	
        	
        	
        	for(String pkgName : addedThemes){
        		if(srcFileName.equals(pkgName)){
        			addedFilesName.add(pkgName);
        			return "FAIL";
        		}
        	}
        	
        	ZipFile lwt = null;
        	try {
                lwt = new ZipFile(srcFile);
                
                if(isOldTheme(lwt)){
                    oldThemeName.add(srcFileName);
                    return "FAIL";
                }
                
                if(!isSupportScreen(lwt)){
                    notSupportName.add(srcFileName);
                    return "FAIL";
                };
            } catch (ZipException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }finally{
                
                try {
                    if(lwt != null){
                        lwt.close();
                        lwt = null;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
        	
        	
        	File tarFile = new File(target);
        	
            if(ThemeUtil.copyFile(srcFile, tarFile,true)){
                boolean success = ThemeUtil.unThemeZIP(tarFile);
                if(success){
                    importControl = "true";
                    return "SUCCESS";
                }
            }
            importControl = "true";
            return "FAIL";
                        
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(result.equals("SUCCESS")){
                ++theme_copy_over_count;
                themeCopyProgressDialog.setProgress(theme_copy_over_count);
                ThemeUtil.addThemeInfo(mContext, themePkg, ThemeConstants.SECOND,true);
                
            }else {
                --theme_select_count;
            }
            
            if(theme_copy_over_count == theme_select_count){
            	
            	int size = addedFilesName.size();
            	if(size != 0){
            		StringBuilder sb = new StringBuilder();
            		for(int i=0;i<size;i++){
            			sb.append(addedFilesName.get(i)).append(" ");
            		}
            		Toast.makeText(mContext, getText(R.string.theme_duplicate)+sb.toString(), Toast.LENGTH_SHORT).show();
            	}
            	
            	size = notSupportName.size();
            	if(size != 0){
            	    StringBuilder sb = new StringBuilder();
            	    for(int i=0;i<size;i++){
            	        sb.append(notSupportName.get(i)).append(" ");
            	    }
            	    Toast.makeText(mContext, getText(R.string.theme_notsupport)+sb.toString(), Toast.LENGTH_SHORT).show();
            	}
            	
            	size = oldThemeName.size();
            	if(size != 0){
            	    StringBuilder sb = new StringBuilder();
                    for(int i=0;i<size;i++){
                        sb.append(oldThemeName.get(i)).append(" ");
                    }
                    Toast.makeText(mContext, getText(R.string.old_theme)+sb.toString(), Toast.LENGTH_SHORT).show();
            	}
            	
            	
                /**
                 * if copy over,the count should set 0
                 */
                theme_copy_over_count = 0;
                theme_select_count = 0;
                
                Intent importThemeOver = new Intent();
                importThemeOver.setAction(ThemeActions.ADD_THEME_OVER);
                ThemeFileManager.this.sendBroadcast(importThemeOver);
                
                themeCopyProgressDialog.cancel();
                
                ThemeFileManager.this.finish();
                
            }
            clickThemeName.remove(0);
            clickThemePath.remove(0);
            if (!clickThemeName.isEmpty()) {
                new CopyThemeFile(clickThemePath.get(0),
                        new StringBuilder().append(newPath).append("/").append(clickThemeName.get(0)).toString(), clickThemeName.get(0)).execute("");
            }
        }
        
        
    }
    
    
    /**
     * 判断当前手机是否支持此主题
     */
    private boolean isSupportScreen(ZipFile zipFile) {
        
        InputStream is = null;
        try {
            ZipEntry zipEntry = zipFile.getEntry("theme.json.zh_CN");
            if(zipEntry == null){
                return false;
            }
            is = zipFile.getInputStream(zipEntry);
            
            String jsonStr = IOUtils.toString(is, "GBK");
            
            JSONObject jsonObject = new JSONObject(jsonStr);
            String screen = jsonObject.getString("screen");
            /**
             * 如果主题没有指定screen为WVGA或者HVGA，或者内容为其它，则规定手机不支持此主题
             */
            if(!screen.trim().equals("")){
                if(ThemeUtil.isWVGA){
                    /**
                     * 如果当前手机是WVGA屏且主题中的screen也为"WVGA"，则表示支持该主题
                     */
                    if(screen.equalsIgnoreCase("WVGA")){
                        return true;
                    }
                }else {
                    /**
                     * 如果当前手机是HVGA屏且主题中的screen也为"HVGA"，则表示支持该主题
                     */
                    if(screen.equalsIgnoreCase("HVGA")){
                        return true;
                    }
                }
            }
            return false;    
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e(TAG, "No value for screen!");
        }finally{
            try {
                if(is != null){
                    is.close();
                    is = null;
                }
            } catch (Exception e2) {
                // TODO: handle exception
            }
        }
        return false;
    }

    /**
     * 旧主题中包含com.android.launcher模块
     * @param zipFile
     * @return
     */
    private boolean isOldTheme(ZipFile zipFile){
        ZipEntry zipEntry = zipFile.getEntry("com.android.launcher");
        if(zipEntry != null){
            return true;
        }    
        return false;
    }
    
    
    @Override
    protected void onDestroy() {
        
        themeFileInfos.clear();
        themeFileInfos = null;
        
        clickThemePath.clear();
        clickThemePath = null;
        
        clickThemeName.clear();
        clickThemeName = null;
        
        if(themeListAdapter != null){
            themeListAdapter.clearUp();
        }
        
        if(addedThemes != null){
            addedThemes.clear();
        }
        
        addedFilesName.clear();
        
        notSupportName.clear();
        
        oldThemeName.clear();
        
        theme_select_number = 0;
        
        super.onDestroy();
    }
 /*
  * author by zjyu
  * control import themes one by one
  * **/   
   public class ImportThread extends Thread{

    @Override
    public void run() {
            if (!clickThemeName.isEmpty()) {
//                synchronized (importControl) {
//                    if ("true".equals(importControl)) {

//                        importControl = "false";
                        new CopyThemeFile(clickThemePath.get(0),
                        new StringBuilder().append(newPath).append("/").append(clickThemeName.get(0)).toString(), clickThemeName.get(0)).execute("");
                    }
//                }
                
                
            }
           
    }
       
   }

