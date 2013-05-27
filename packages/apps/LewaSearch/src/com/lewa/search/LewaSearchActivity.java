package com.lewa.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.lewa.search.adapter.FileInfoAdapter;
import com.lewa.search.bean.AppFileInfo;
import com.lewa.search.bean.ContactFileInfo;
import com.lewa.search.bean.FileInfo;
import com.lewa.search.bean.ImageFileInfo;
import com.lewa.search.bean.MessageFileInfo;
import com.lewa.search.bean.MusicFileInfo;
import com.lewa.search.bean.NormalFileInfo;
import com.lewa.search.bean.SettingFileInfo;
import com.lewa.search.bean.VideoFileInfo;
import com.lewa.search.bean.WebSearchFileInfo;
import com.lewa.search.db.DBUtil;
import com.lewa.search.decorator.Decorator;
import com.lewa.search.decorator.TextHighLightDecorator;
import com.lewa.search.decorator.TextSimplifiedHighLightDecorator;
import com.lewa.search.match.KeyMatcher;
import com.lewa.search.system.Constants;
import com.lewa.search.system.SoftCache;
import com.lewa.search.system.SystemMode;
import com.lewa.search.util.FileInfoUtil;
import com.lewa.search.util.SearchUtil;
import com.lewa.search.util.WebSearchUtil;
import com.lewa.search.view.SearchEditText;

/**
 * This class defines the Activity on "search" page.
 * @author		wangfan
 * @version	2012.07.04
 */

public class LewaSearchActivity extends Activity implements OnClickListener{  
  
	public String SEARCH_SCHEME = "com.lewa.search";
	
	//this list contains the searching results
	public List<FileInfo> resultList = new ArrayList<FileInfo>();
	
	//this adapter and listView presents the results on the screen
	private FileInfoAdapter fileInfoAdapter;
	private ListView listView;
	
	//this editText get the user input
	private SearchEditText editSearch;

	//this button clear the user input
	private ImageView deleteButton;
	
	//this matcher matches the key in decorator
	private KeyMatcher matcher;
	
	//register a map of decorators for variables showed in the view
	private Map<Integer, Decorator> decorators = new HashMap<Integer, Decorator>();
	
	//register a thread and a handler for search task to avoid time delay in user input
	private SearchThread searchThread;
	private SearchHandler searchHandler;
	
	//each represents whether to search this type of infomation
	private boolean searchMessage;
	private boolean searchContact;
	private boolean searchApp;
	private boolean searchImage;
	private boolean searchMusic;
	private boolean searchSetting;
	private boolean searchFile;
	private boolean sdcardIsEnable = true;
    
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
    	
    	 super.onCreate(savedInstanceState);
         setContentView(R.layout.main);    
         initSystems();
         initUtils();
         initViews();
         initDecorators();
    }
	
	@Override
    protected void onResume() {
        // TODO Auto-generated method stub
	    sdcardIsEnable = true;
        super.onResume();
    }
	/**
	 * This method initialize system environment and record system state.
	 */
	private void initSystems()
	{
		//initialize a handler to process message
		searchHandler = new SearchHandler();
		
		//initialize language mode
		String language = Locale.getDefault().getLanguage();
		if(language == null || language.equals("zh"))
		{
			SystemMode.langeuageMode = Constants.LANGUAGEMODE_CHINESE;
		}
		else
		{
			SystemMode.langeuageMode = Constants.LANGUAGEMODE_ENGLISH;
		}
	}
	
	/**
	 * This method initialize some system utils
	 */
	private void initUtils()
	{
		 //set contetx for database search
		 DBUtil.context = LewaSearchActivity.this;
		 //initialize softCache to cache some datas and structures used frequently
		 SoftCache.init(LewaSearchActivity.this);
		 
		 //initialize a fileAdapter
		 //each adapter can only have one layout for item
		 //the methodArgs helps to assemble the method name in invocation 
		 fileInfoAdapter = new FileInfoAdapter(getLayoutInflater());
		 fileInfoAdapter.fileList = resultList;
		 fileInfoAdapter.setMethodArgs("get");
	     fileInfoAdapter.setLayoutId(R.layout.listitem); 
	     
	     //initialize a matcher
	     matcher = new KeyMatcher("");
	}   
    
	/**
	 * This method initialize views showed on the screen
	 */
    private void initViews()
    {
    	//initialize the listView
        listView = (ListView)findViewById(R.id.ListEntries);
        listView.setAdapter(fileInfoAdapter);
        listView.setOnTouchListener(listTouchListener);
        listView.setOnItemClickListener(fileEnterListener);   
       
        //initialize the deleteButton used to clear user input
        deleteButton = (ImageView) findViewById(R.id.delete_button);
        deleteButton.setVisibility(View.INVISIBLE);
        deleteButton.setOnClickListener(this);
        
        //inialize the search text for user input
        editSearch = (SearchEditText)findViewById(R.id.search_edit_text);
        editSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
			    if(!SearchUtil.isSDCardEnable()){
			        sdcardIsEnable = false;
		         }
				String key = s.toString();
				if (key.contains("'")) {
				    sdcardIsEnable = true;
                }
				
				//clear list each time when a new search starts
				cleanList();
				if(!key.equals(""))	//user has input,start a new thread
				{
					//updata matchers
					updateMatchers(key);
					//stop current searchThread
					if(searchThread != null)
					{
						searchThread.stoped = true;
						searchThread.interrupt();
                                          searchThread = null;
					}
					
					//check searching scope
					loadSearchInfo();
					
					//start a new searchThread
					searchThread = new SearchThread(key);
					searchThread.start();
				}
				else //user clears input,stop searching
				{
				       if(searchThread != null){
        				    searchThread.stoped = true;
        				    searchThread.interrupt();
                                       searchThread = null;
                                   }
				}
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
				String key = s.toString();
				if(key.equals(""))
				{
					//set deleteButton invisible when user has no input 
					if(deleteButton.getVisibility() == View.VISIBLE)
					{
						deleteButton.setVisibility(View.INVISIBLE);
					}
					
					cleanList();
				}
				else
				{
					//set deleteButton visible when user has input
					if(deleteButton.getVisibility() == View.INVISIBLE)
					{
						deleteButton.setVisibility(View.VISIBLE);
					}
				}
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
		});
    }
    
    /**
	 * This method initialize decorators used for decorating objects showed on the screen
	 */
    private void initDecorators()
    {
    	Resources rs = this.getResources(); 
    	//initialize a TextHighLightDecorator
    	TextHighLightDecorator decoratorHighLight = new TextHighLightDecorator(rs.getColor(R.color.highlight_blue));
        decoratorHighLight.setMatcher(matcher);
        decorators.put(Decorator.DECORATOR_HIGHLIGHT, decoratorHighLight);
        
        //initialize a TextSimplifiedHighLightDecorator without suffix
        TextSimplifiedHighLightDecorator decoratorHighLightSimplifiedNoSuffix = new TextSimplifiedHighLightDecorator(rs.getColor(R.color.highlight_blue), 14, "...", false);
        decoratorHighLightSimplifiedNoSuffix.setMatcher(matcher);
        decorators.put(Decorator.DECORATOR_HIGHLIGHT_AND_SIMPLIFIED_NO_SUFFIX, decoratorHighLightSimplifiedNoSuffix);
        
        //initialize a TextSimplifiedHighLightDecorator with suffix
        TextSimplifiedHighLightDecorator decoratorHighLightSimplified = new TextSimplifiedHighLightDecorator(rs.getColor(R.color.highlight_blue), 14, "...", true);
        decoratorHighLightSimplified.setMatcher(matcher);
        decorators.put(Decorator.DECORATOR_HIGHLIGHT_AND_SIMPLIFIED, decoratorHighLightSimplified);
    }
    
    //create an object lock for searchThreads visiting database 
    static Object searchLock = new Object();
    
    /**
     * This inner class defines a thread for search files and infomation match the key
     * @author		wangfan
     * @version	2012.07.04
     */
    class SearchThread extends Thread {

    	private String key;
    	//a tag represents whether the thread should be shopped
    	//it will be checked before each operation which has the need to visit database
    	private boolean stoped = false;
    	
    	//a temp list to store search result, each thread has his own temp list
    	private List<FileInfo> threadList;
    	
    	public SearchThread(String key)
    	{
    		this.key = key;
    	}
    	
    	public void run()
    	{
    		synchronized (searchLock)
            {
    			//initialize an empty temp list
    			threadList = new ArrayList<FileInfo>();
    			if (!sdcardIsEnable) {
    			    sdcardIsEnable = true;
                  //search setting information
                  if(stoped == false && searchSetting == true)
                  {   
                      List<FileInfo> tempList = searchSettings(key);
                      if(tempList != null && stoped == false)
                      {
                          threadList.addAll(tempList);
                      }
                  }
              
                  //search contact information
                  if(stoped == false && searchContact == true)
                  {
                      List<FileInfo> tempList = searchContracts(key);
                  
                      if(tempList != null)
                      {
                          threadList.addAll(tempList);
                      }
                  }
              
                  //search message information
                  if(stoped == false && searchMessage == true)
                  {
                      List<FileInfo> tempList = searchMessages(key);
                  
                      if(tempList != null  && stoped == false)
                      {
                          threadList.addAll(tempList);
                      }
                  }
              
                  //search application information
                  if(stoped == false && searchApp == true)
                  {
                      List<FileInfo> tempList = searchApps(key);
                  
                      if(tempList != null && stoped == false)
                      {
                          threadList.addAll(tempList);
                      }
                  }
              
                  //search image information
                  if(stoped == false && searchImage == true)
                  {
                      List<FileInfo> tempList = searchImages(key);
                  
                      if(tempList != null && stoped == false)
                      {
                          threadList.addAll(tempList);
                      }
                  }
              
                  //search music information
                  if(stoped == false && searchMusic == true)
                  {
                      List<FileInfo> tempList = searchMusics(key);
                  
                      if(tempList != null && stoped == false)
                      {
                          threadList.addAll(tempList);
                      }
                  }
              
                  //search file information,video files was regarded as normal file information
                  if(searchFile == true)
                  {
                  if(stoped == false)
                  {
                          List<FileInfo> tempList = searchNormals(key);
                      
                          if(tempList != null  && stoped == false)
                          {
                              threadList.addAll(tempList);
                          }
                      }
                  
                      if(stoped == false)
                      {
                      List<FileInfo> tempList = searchVideos(key);
                  
                      if(tempList != null && stoped == false)
                      {
                          threadList.addAll(tempList);
                      }
                  }
                  }
                }

    			
    			if(stoped == false)
    			{
    				List<FileInfo> tempList = buildWebSearchButtons(key);
    				
    				 if(tempList != null && stoped == false)
        			{
        				threadList.addAll(tempList);
        			}
    			}
    		
    			//return the final result to resultList owned by this activity
    			if(stoped == false)
    			{
    				Message msg = new Message();
    				msg.what = Constants.INFO_DATA_CHANGED;
    				Handler handler = LewaSearchActivity.this.searchHandler;
        		
    				//clear resultList before addition
    				resultList.clear();
    				//add result to resultList
    				resultList.addAll(threadList);
    				
    				//send message to notify the change
    				handler.sendMessage(msg); 
    			}    
            }
    	}	
    	
    	/**
    	 * This method was used for searching setting information.
    	 * @param key	key in search
    	 */
    	@SuppressWarnings("unchecked")
        private List<FileInfo> searchSettings(String key)
        {
    		//get search result
        	List<FileInfo> tempList = (List<FileInfo>) FileInfoUtil.searchContentsByKey(Constants.CLASS_SETTING, key, "");  

            if(tempList != null)
        	{
            	//register a TextHighLightDecorator for the name field "title" 
            	Map<String, Decorator> decoratorsControctInfo = new HashMap<String, Decorator>();
            	decoratorsControctInfo.put("title", decorators.get(Decorator.DECORATOR_HIGHLIGHT));
            	decoratize(tempList, decoratorsControctInfo);
            
            	 return tempList;
        	}
            
            return null;
        }
        
    	/**
    	 * This method was used for searching contact information.
    	 * @param key	key in search
    	 */
    	@SuppressWarnings("unchecked")
    	private List<FileInfo> searchContracts(String key)
        {
    		//get search result
            List<FileInfo> tempList = (List<FileInfo>) FileInfoUtil.searchContentsByKey(Constants.CLASS_CONTRACT, key, "_id COLLATE NOCASE");  

            if(tempList != null)
        	{
            	//register a TextHighLightDecorator for the name field "title" 
            	//register a TextHighLightDecorator for the name field "text" 
            	Map<String, Decorator> decoratorsControctInfo = new HashMap<String, Decorator>();
            	decoratorsControctInfo.put("title", decorators.get(Decorator.DECORATOR_HIGHLIGHT));
            	decoratorsControctInfo.put("text", decorators.get(Decorator.DECORATOR_HIGHLIGHT));
            	decoratize(tempList, decoratorsControctInfo);
            
            	 return tempList;
        	}
            
            return null;
        }
        
    	/**
    	 * This method was used for searching message information.
    	 * @param key	key in search
    	 */
        @SuppressWarnings("unchecked")
        private List<FileInfo> searchMessages(String key)
        {
        	//get search result
            List<FileInfo> tempList = (List<FileInfo>) FileInfoUtil.searchContentsByKey(Constants.CLASS_MESSAGE, key, "_id COLLATE NOCASE");  
            
            if(tempList != null)
        	{
            	//register a TextHighLightDecorator for the name field "title" 
            	//register a TextSimplifiedHighLightDecorator without suffix for the name field "text"
            	Map<String, Decorator> decoratorsMessageInfo = new HashMap<String, Decorator>();
            	decoratorsMessageInfo.put("title", decorators.get(Decorator.DECORATOR_HIGHLIGHT));
            	decoratorsMessageInfo.put("text", decorators.get(Decorator.DECORATOR_HIGHLIGHT_AND_SIMPLIFIED_NO_SUFFIX));
            	decoratize(tempList, decoratorsMessageInfo);
            
            	 return tempList;
        	}
            
            return null;
        }
        
        /**
    	 * This method was used for searching message information.
    	 * @param key	key in search
    	 */
        @SuppressWarnings("unchecked")
    	private List<FileInfo> searchApps(String key)
        {
        	//get search result
            List<FileInfo> tempList = (List<FileInfo>) FileInfoUtil.searchContentsByKey(Constants.CLASS_APP, key, null);  

            if(tempList != null)
        	{
            	//register a TextHighLightDecorator for the name field "title" 
            	Map<String, Decorator> decoratorsControctInfo = new HashMap<String, Decorator>();
            	decoratorsControctInfo.put("title", decorators.get(Decorator.DECORATOR_HIGHLIGHT));
            	decoratize(tempList, decoratorsControctInfo);
            
            	 return tempList;
        	}
            
            return null;
        }
        
        /**
    	 * This method was used for searching message information.
    	 * @param key	key in search
    	 */
        @SuppressWarnings("unchecked")
        private List<FileInfo> searchNormals(String key)
        {
        	//get search result
        	List<FileInfo> tempList = (List<FileInfo>) FileInfoUtil.searchContentsByKey(Constants.CLASS_NORMAL, key, "_id COLLATE NOCASE");  
        	
        	if(tempList != null)
        	{
        		//register a TextSimplifiedHighLightDecorator with suffix for the name field "title"
        		Map<String, Decorator> decoratorsNormalInfo = new HashMap<String, Decorator>();
        		decoratorsNormalInfo.put("title", decorators.get(Decorator.DECORATOR_HIGHLIGHT_AND_SIMPLIFIED));
        		decoratize(tempList, decoratorsNormalInfo);
        		
        		 return tempList;
        	}
        	
        	return null;
        }
        
        /**
    	 * This method was used for searching image files.
    	 * @param key	key in search
    	 */
        @SuppressWarnings("unchecked")
        private List<FileInfo> searchImages(String key)
        {
        	List<FileInfo> tempList = (List<FileInfo>) FileInfoUtil.searchContentsByKey(Constants.CLASS_IMAGE, key, "_id COLLATE NOCASE");  
           
        	if(tempList != null)
        	{
        		Map<String, Decorator> decoratorsImageInfo = new HashMap<String, Decorator>();
        		decoratorsImageInfo.put("title", decorators.get(Decorator.DECORATOR_HIGHLIGHT_AND_SIMPLIFIED));
        		decoratize(tempList, decoratorsImageInfo);
        		
        		 return tempList;
        	}
        	
        	return null;
        }
        
        /**
    	 * This method was used for searching music files.
    	 * @param key	key in search
    	 */
        @SuppressWarnings("unchecked")
        private List<FileInfo> searchMusics(String key)
        {
        	//get search result
        	List<FileInfo> tempList = (List<FileInfo>) FileInfoUtil.searchContentsByKey(Constants.CLASS_MUSIC, key, "_id COLLATE NOCASE");  
            
        	if(tempList != null)
        	{
        		//register a TextSimplifiedHighLightDecorator with suffix for the name field "title"
        		Map<String, Decorator> decoratorsMusicInfo = new HashMap<String, Decorator>();
        		decoratorsMusicInfo.put("title", decorators.get(Decorator.DECORATOR_HIGHLIGHT_AND_SIMPLIFIED));
        		decoratize(tempList, decoratorsMusicInfo);
            
        		 return tempList;
        	}
        	
        	return null;
        }
        
        /**
    	 * This method was used for searching video files.
    	 * @param key	key in search
    	 */
        @SuppressWarnings("unchecked")
        private List<FileInfo> searchVideos(String key)
        {
        	//get search result
        	List<FileInfo> tempList = (List<FileInfo>) FileInfoUtil.searchContentsByKey(Constants.CLASS_VIDEO, key, "_id COLLATE NOCASE");  
            
        	if(tempList != null)
        	{
        		//register a TextSimplifiedHighLightDecorator with suffix for the name field "title"
        		Map<String, Decorator> decoratorsVideoInfo = new HashMap<String, Decorator>();
                decoratorsVideoInfo.put("title", decorators.get(Decorator.DECORATOR_HIGHLIGHT_AND_SIMPLIFIED));
                decoratize(tempList, decoratorsVideoInfo);
                
                return tempList;
        	}
        	
        	return null;
        }
        
        /**
    	 * This method was used to pretend to build file items with no need to visit database.
    	 * These items was regarded as buttons to search web resources.
    	 * @param key	key in search
    	 */
        private List<FileInfo> buildWebSearchButtons(String key)
        {
        	//build search buttons
        	List<FileInfo> tempList = (List<FileInfo>) WebSearchUtil.buildWebSearchButtons(LewaSearchActivity.this, key);
        	
        	if(tempList != null)
        	{
        		//register a TextHighLightDecorator for the name field "text"
        		Map<String, Decorator> decoratorsVideoInfo = new HashMap<String, Decorator>();
                decoratorsVideoInfo.put("text", decorators.get(Decorator.DECORATOR_HIGHLIGHT));
                decoratize(tempList, decoratorsVideoInfo);
        		
                return tempList;
        	}
        	
        	return null;
        }
        
        /**
    	 * This method set each item a map decorator in the fileList.
    	 * Each kind of fileList can only register one map of decorators.
    	 * @param fileList	the list to be decorated
    	 * @param decorators	this map should be deliver to each item in this list
    	 */
        private void decoratize(List<FileInfo> fileList, Map<String, Decorator> decorators)
        {
        	FileInfo fileInfo;
        	for(int i = 0; i < fileList.size(); i++)
        	{
        		fileInfo = fileList.get(i);
        		fileInfo.decorators = decorators;
        	}
        }
    }
    
    /**
     * This inner class defines a handler to handle messages sent by searchThread.
     * @author		wangfan
     * @version	2012.07.04
     */
    class SearchHandler extends Handler {
    	public SearchHandler() {
    	
    	}
  
    	public SearchHandler(Looper L) {
    		super(L);
    	}

        @Override
        public void handleMessage(Message msg) 
        {
        	// TODO Auto-generated method stub
        	switch(msg.what)
        	{
				case Constants.INFO_DATA_CHANGED:
				{	
					if(resultList.size() > 0)
					{
						//update views on the screen
						fileInfoAdapter.notifyDataSetChanged();
					}
				}
        	}
        }
    }
    
    /**
     * This method was used for loading search scope from shared preference.
     * This preference can be modified in LewaSearchInfoSettingActivity.
     */
    private void loadSearchInfo()
    {
    	String STORE_NAME = "com.lewa.search_preferences";
		SharedPreferences settings = getSharedPreferences(STORE_NAME, MODE_PRIVATE);  
		
		searchMessage = settings.getBoolean("info_setting_message", true);  
		searchApp = settings.getBoolean("info_setting_app", true);  
		searchContact = settings.getBoolean("info_setting_contact", true);  
		searchImage = settings.getBoolean("info_setting_image", true);  
		searchMusic = settings.getBoolean("info_setting_music", true);  
		searchSetting = settings.getBoolean("info_setting_setting", true);  
		searchFile = settings.getBoolean("info_setting_file", true);  
    }
    
    /**
     * This method was used for updating matcher.
     */
    private void updateMatchers(String key)
    {
    	matcher.updateMatcher(key);
    }
    
    /**
     * This method was used to clear resultList.
     */
    private void cleanList()
    {
    	if(resultList.size() > 0)
    	{
    		resultList.clear();
    	}
    	//resultList has changed,notify to update screen
    	fileInfoAdapter.notifyDataSetChanged();
    }
      
    //refer to PIM ~~~ begin ~~~
    /**
     * Dismisses the search UI along with the keyboard if the filter text is empty.
     */
    public boolean onKeyPreIme(int keyCode, KeyEvent event) 
    {
        if(keyCode == KeyEvent.KEYCODE_BACK && editSearch.getText().equals("")) 
        {
            hideSoftKeyboard();
            onBackPressed();
            
            return true;
        }
        return false;
    }
    
    private void hideSoftKeyboard() {
        // Hide soft keyboard, if visible
        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(listView.getWindowToken(), 0);
    }
    //~~~ end ~~~

    /**
     * Register the click listener.
     */
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		//clear user input when deleteButton was pressed
		if(view == deleteButton)
		{
			editSearch.setText("");
		}
		
		//clear focus on editSearch when listView was touched
		else if(view == listView)
		{
			editSearch.clearFocus();
		}

	}
	
	/**
     * Register the itemClick listener.
     */
	private OnItemClickListener fileEnterListener = new OnItemClickListener() {
		
		@Override  
    	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {   
    		
			//get fileItem clicked and open the file
    		FileInfo fileItem = resultList.get(arg2);
    		enterFile(LewaSearchActivity.this, fileItem);
    	}   
		
		private void enterFile(Context context, FileInfo fileItem)
		{
			switch(fileItem.getFileType())
			{
				case Constants.CLASS_APP:
				{
					if(!FileInfoUtil.openApp(LewaSearchActivity.this, ((AppFileInfo) fileItem).getPackageName()))
					{
						//assemble error text and display it
						Resources re = context.getResources();
						String errorTextPre = re.getString(R.string.enter_app_error_prefix);
						String errorTextSuf = re.getString(R.string.enter_app_error_suffix);
						String errorText;
						
						int appType = ((AppFileInfo) fileItem).getAppType();
						
						if(appType == Constants.APP_SYSTEM_TYPE)
						{
							errorText = errorTextPre + re.getString(R.string.system_app) + errorTextSuf;
									
						}
						else
						{
							errorText = errorTextPre + re.getString(R.string.user_app) + errorTextSuf;
						}
						
						Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show();
					}
					
					break;
				}
				
				case Constants.CLASS_CONTRACT:
				{
					//open contact detail
					FileInfoUtil.openContract(context, Long.valueOf(((ContactFileInfo) fileItem).getContactId()));
			        
					break;
				}
				
				case Constants.CLASS_IMAGE:
				{
					//open image file
					FileInfoUtil.openFile(((ImageFileInfo) fileItem).getFilePath(), context);
					break;
				}
				
				case Constants.CLASS_MESSAGE:
				{
					//open message and locate
					FileInfoUtil.openMessages(context, ((MessageFileInfo) fileItem).getMessageId(), 
							((MessageFileInfo) fileItem).getThreadId(), ((MessageFileInfo) fileItem).getNumber());
					
					break;
				}
				
				case Constants.CLASS_MUSIC:
				{
					//open music file
					FileInfoUtil.openFile(((MusicFileInfo) fileItem).getFilePath(), context);
					break;
				}
				
				case Constants.CLASS_NORMAL:
				{
					//open normal file
					FileInfoUtil.openFile(((NormalFileInfo) fileItem).getFilePath(), context);
					break;
				}
				
				case Constants.CLASS_SETTING:
				{
					//open system setting
					FileInfoUtil.openSetting(context, ((SettingFileInfo) fileItem).getActionName(), 
							((SettingFileInfo) fileItem).getPackageName());
					
					break;
				}
				
				case Constants.CLASS_VIDEO:
				{
					//open video file
					FileInfoUtil.openFile(((VideoFileInfo) fileItem).getFilePath(), context);
					break;
				}
				
				case Constants.CLASS_WEB:
				{
					//open browser to search resources from web
					int webSearchType = ((WebSearchFileInfo) fileItem).getWebSearchType();
					String key = ((WebSearchFileInfo) fileItem).getKey();
					if(webSearchType == Constants.WEB_SEARCH)
					{
						//search information from web
						WebSearchUtil.webSearch(context, Constants.WEB_SEARCH, key);
					}
					else
					{
						//search application from web
						WebSearchUtil.webSearch(context, Constants.APP_SEARCH, key);
					}
				}
			}
		}	
	};
	
	/**
     * Register the touch listener.
     */
	private OnTouchListener listTouchListener = new OnTouchListener() {
		 
		  public boolean onTouch(View v, MotionEvent event) {
		   // TODO Auto-generated method stub
			  
			  //hide keyboard when list was touched
			  hideSoftKeyboard();
			  return false;
		  }
	};
	
	
	/**
     * Create option menu.
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{               
		//register a menu for search setting and load a icon for this menu.
		menu.add(0, Constants.MENU_SEARCH_SETTING, Menu.FIRST, R.string.app_setting).setIcon(R.drawable.ic_menu_setting);       
		return true;       
	}  


	/**
     * Define operation when option item was selected.
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{         
		 switch (item.getItemId()) 
		 {         
		 	case Constants.MENU_SEARCH_SETTING: 
		 	{
		 		//start a LewaSearchSettingActivity
		 		startActivity(new Intent(LewaSearchActivity.this, LewaSearchSettingActivity.class));
		 	}
		 }
		 
		 return super.onContextItemSelected(item);     
	}
	
	private void createAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sdcard_status_changed_title)
        .setMessage(R.string.sdcard_status_changed_msg)
        .setCancelable(false)
        .setPositiveButton(R.string.type_ok, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                finish();
            }
        });
        builder.create().show();
    }
	
	
}  
