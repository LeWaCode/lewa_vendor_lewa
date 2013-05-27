package com.lewa.feedback;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.KeyEvent;

public class AndroidFeedbackActivity extends Activity implements Serializable {
	/**
	 * 
	 */
	private FileMrgCallBack fileMrgCallBack;
	private static final long serialVersionUID = 1L;
	private static final long MAX_SIZE = 65536;
	private static final long LIMIT_SIZE = 300;
	public static final String ACTION_FILEMANAGER_INVOKE = "com.lewa.filemgr.count_start";
	public static final String KEY_FEEDBACK_PATHDATA = "filemgr_path_data";
	/** Called when the activity is first created. */

	private ImageView addPicView;
	private EditText userAdviceEditText;
	private Button sendButton;
	private TextView picLenTextView;
	private ProgressDialog proDialog;
	
	private SendProcessHandler sendHandler;

	private File[] files = new File[1];
	private int fileLenByKB = 0;

	private String phoneInfo;
	private String version;
	private String imei;
	
	private String urlSend;
	private Map<String, String> paramsSend;
	private Map<String, File> filesSend;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		
		sendHandler = new SendProcessHandler(); 
		initTextView();
		initEditText();
		initButton();
	}

	private void getPhoneMsg() {
		phoneInfo = Build.MODEL;
		version = Build.DISPLAY;
		imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
				.getDeviceId();
	}

	private void initTextView() {
		picLenTextView = (TextView) findViewById(R.id.pic_len);
	}

	public class FileMrgCallBack extends BroadcastReceiver {
		AndroidFeedbackActivity androidFeedbackActivity;

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			if (!"AndrdoiFeedbackActivity".equals(intent
					.getAction())) {
				
				return;
			}
			androidFeedbackActivity.getFiles(intent
					.getStringArrayListExtra("PATHS"));
			
			androidFeedbackActivity.picLenTextView.setText(String.valueOf(fileLenByKB));
		}

		public FileMrgCallBack(
			AndroidFeedbackActivity androidFeedbackActivity) {
				super();
				this.androidFeedbackActivity = androidFeedbackActivity;
		}
	}
	
	public void getFiles(List<String> paths) {
		
		files = FileOperation.getFilesFromPaths(paths);
		
		for (String path : paths) {
			Log.i("", "path :" + path.substring(5));
			
			fileLenByKB = 0;
			for (int i = 0; i < paths.size(); i++) {
				fileLenByKB += FileOperation.countFileLenByKB(paths.get(i).substring(5));
			}
		}
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (fileMrgCallBack == null) {
			fileMrgCallBack = new FileMrgCallBack(this);
			IntentFilter filter = new IntentFilter();
			filter.addAction("AndrdoiFeedbackActivity");
			this.registerReceiver(fileMrgCallBack, filter);
		}// onDestroy this.unregisterReceiver(receiver)

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(fileMrgCallBack != null)
		{
			this.unregisterReceiver(fileMrgCallBack);
		}
	}

	private void initEditText() {
		userAdviceEditText = (EditText) findViewById(R.id.user_advice);
	}

	private void initButton() {
		sendButton = (Button) findViewById(R.id.send_button);
		sendButton.setOnClickListener(sendListener);
		
		addPicView = (ImageView) findViewById(R.id.add_pic);
		addPicView.setOnClickListener(getPicsListener);
		addPicView.setOnCreateContextMenuListener(createMenuListener);
		addPicView.setLongClickable(false);
	}
	
	private void lockButton()
    {
    	sendButton.setClickable(false);
    }
    
    private void resumeButton()
    {
    	sendButton.setClickable(true);
    }

	private void sendData() {
		
		getPhoneMsg();
		
		Map<String, String> strParams = new HashMap<String, String>();

		strParams.put("phoneInfo_lewa", this.phoneInfo); // 手机型号
		strParams.put("version_lewa", this.version); // 版本号
		strParams.put("imei_lewa", imei); // Imei号
		strParams.put("userAdvice_lewa", userAdviceEditText.getText()
				.toString());

		Map<String, File> filesMap = new HashMap<String, File>();
		
		if(files != null && files[0] != null)
		{
			for (int i = 0; i < files.length; i++) {
				
				filesMap.put("pic" + i + "_lewa", files[i]);
			}
		}

		try 
		{	
			this.urlSend = SystemConstants.UPLOAD_URL;
			this.paramsSend = strParams;
			this.filesSend = filesMap;

			proDialog.setMessage(getString(R.string.feedback_sending_process_connect));
			sendSleep(1000);
			new Thread(new ConnectThread()).start();
			
		} 
		catch (Exception e) 
		{
			Log.i("error", "Exception = " + e.getMessage());
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode) 
        {
        	case 0:
        		{
        			if(resultCode == Activity.RESULT_OK)
        			{
        				this.finish();
        			}
        		}     
        }
    }

	private OnClickListener sendListener = new OnClickListener() {
		public void onClick(View v) {
			if(!"".equals(userAdviceEditText.getText().toString().trim())) //if((!userAdviceEditText.getText().equals("")) || fileLenByKB != 0) 
			{
				proDialog = ProgressDialog.show(AndroidFeedbackActivity.this,
						getResources().getString(R.string.feedback_sending_title), 
						getResources().getString(R.string.feedback_sending_message), true, true);

				lockButton();
				sendData();
			}
			else
			{
				createDialogWithOneButton(getResources().getString(R.string.feedback_sending_fail_title),
						android.R.drawable.alert_dark_frame, 
						getString(R.string.feedback_sending_fail_message3));
			}
			
		}
	};

	View.OnCreateContextMenuListener createMenuListener = new OnCreateContextMenuListener(){
		@Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
		{
			try 
			{
				menu.setHeaderTitle(getString(R.string.feedback_addpic_title));  
				
				menu.add(0, Menu.FIRST +1, 1, getString(R.string.feedback_addpic_delete)); 
				menu.add(0, Menu.FIRST +2, 2, getString(R.string.feedback_addpic_change)); 
			} 
			catch (ClassCastException e) 
			{
				return;
			}
        }
		
	};
	
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{         
		 switch (item.getItemId()) 
		 {         
		 	case SystemConstants.MENU_ADDPIC_DELETE: 
		 	{
		 		for(int i = 0; i < files.length; i ++)
		 		{
		 			files[i] = null;
		 		}
		 		
		 		fileLenByKB = 0;
		 		picLenTextView.setText(String.valueOf(fileLenByKB));
		 		break;        
		 	}
		 		 
		 	case SystemConstants.MENU_ADDPIC_CHANGE:             
		 	{
		 		Intent intent = new Intent(ACTION_FILEMANAGER_INVOKE);
				intent.addCategory("android.intent.category.DEFAULT");
		        
		        if(isWiFiActive())
		        {	
		        	intent.putExtra("SIZE_LIMIT", (long) (1024 * MAX_SIZE));
		        	intent.putExtra("LIMIT_WORD", "");
		        }
		        else
		        {
		        	String wifiNote = getResources().getString(R.string.feedback_wifi_note);
		        	intent.putExtra("LIMIT_WORD", wifiNote);
		        	intent.putExtra("SIZE_LIMIT", (long) (1024 * LIMIT_SIZE));
		        }
		        
		        intent.putExtra("MIME_TYPE", "image");
		        intent.putExtra("SELECT_MODE", false);
		        intent.putExtra("CALLBACK_ACTION", "AndrdoiFeedbackActivity");
		        startActivity(intent);
		 		break;     
		 	}
		 		    
		 }         
		 return super.onContextItemSelected(item);     
	} 
	
	private OnClickListener getPicsListener = new OnClickListener() {
		public void onClick(View v) {
			Log.v("tag", "len:" + fileLenByKB);
			if(fileLenByKB != 0)
			{
				addPicView.showContextMenu();
			}
			
			else
			{
				Intent intent = new Intent(ACTION_FILEMANAGER_INVOKE);
				intent.addCategory("android.intent.category.DEFAULT");
		        
		        if(isWiFiActive())
		        {	
		        	intent.putExtra("SIZE_LIMIT", (long) (1024 * MAX_SIZE));
		        	intent.putExtra("LIMIT_WORD", "");
		        }
		        else
		        {
		        	String wifiNote = getResources().getString(R.string.feedback_wifi_note);
		        	intent.putExtra("LIMIT_WORD", wifiNote);
		        	intent.putExtra("SIZE_LIMIT", (long) (1024 * LIMIT_SIZE));
		        }
		        
		        intent.putExtra("MIME_TYPE", "image");
		        intent.putExtra("SELECT_MODE", false);
		        intent.putExtra("CALLBACK_ACTION", "AndrdoiFeedbackActivity");
		        startActivity(intent);
			}
			
		}
	};
	
	public boolean isWiFiActive() 
	{       
		ConnectivityManager connectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);       
		if (connectivity != null) 
		{       
			NetworkInfo[] infos = connectivity.getAllNetworkInfo();       
		    if (infos != null) 
		    {       
		    	for(NetworkInfo ni : infos)
		    	{   
		    		
		    		if(ni.getTypeName().equals("WIFI") && ni.isConnected())
		    		{   
		    			return true;   
		    		}   
		    	}   
		    }       
		}       
		return false;       	
	}   
	
	class ConnectThread implements Runnable {
		
    	public void run()
    	{
    		String url = AndroidFeedbackActivity.this.urlSend;
    		
    		boolean state = Upload.connectStart(url);
    		
    		Message msg = new Message();
    		msg.what = state == true? Upload.STATE_CONNECT_SUCCESS: Upload.STATE_CONNECT_FAIL;
    		AndroidFeedbackActivity.this.sendHandler.sendMessage(msg); // 向Handler发送消息,更新UI

    	}
    }
	
	class SendMsgThread implements Runnable {
		
		public Map<String, String> strParams;
		
    	public void run()
    	{
    		Map<String, String> strParams = AndroidFeedbackActivity.this.paramsSend;
    		
    		boolean state = Upload.sendMessage(strParams);
    		
    		Message msg = new Message();
    		msg.what = state == true? Upload.STATE_SEND_MSG_SUCCESS: Upload.STATE_SEND_MSG_FAIL;
    		AndroidFeedbackActivity.this.sendHandler.sendMessage(msg); // 向Handler发送消息,更新UI

    	}
    }
	
	class SendFileThread implements Runnable {
		
		public Map<String, File> filesMap;
		
    	public void run()
    	{
    		Map<String, File> filesMap = AndroidFeedbackActivity.this.filesSend;
    		
    		boolean state = Upload.sendFiles(filesMap);
    		
    		Message msg = new Message();
    		msg.what = state == true? Upload.STATE_SEND_FILE_SUCCESS: Upload.STATE_SEND_FILE_FAIL;
    		AndroidFeedbackActivity.this.sendHandler.sendMessage(msg); // 向Handler发送消息,更新UI

    	}
    }
	
	class CloseThread implements Runnable {
		
    	public void run()
    	{
    		
    		int state = Upload.connectClose();
    		
    		Message msg = new Message();
    		msg.what = Upload.STATE_CLOSE_SUCCESS;
    		
    		Bundle bundle = new Bundle();// 存放数据
    		bundle.putInt("res", state);
    		msg.setData(bundle);
    		
    		AndroidFeedbackActivity.this.sendHandler.sendMessage(msg); // 向Handler发送消息,更新UI

    	}
    }
	
	class SendProcessHandler extends Handler {
    	public SendProcessHandler() {
    	
    	}
  
    	public SendProcessHandler(Looper L) {
    		super(L);
    	}

        @Override
        public void handleMessage(Message msg) 
        {
        	// TODO Auto-generated method stub
        	switch(msg.what)
        	{
				case Upload.STATE_CONNECT_SUCCESS:
				{
					super.handleMessage(msg);
					
					proDialog.setMessage(getString(R.string.feedback_sending_process_send_msg));
					sendSleep(1000);
					new Thread(new SendMsgThread()).start();
					break;
				}
				
				case Upload.STATE_CONNECT_FAIL:
				{
					super.handleMessage(msg);
					
					resumeButton();
					proDialog.dismiss();
					createDialogWithOneButton(getResources().getString(R.string.feedback_sending_fail_title),
							android.R.drawable.alert_dark_frame, 
							getString(R.string.feedback_sending_fail_message4));
				
					break;
				}
				
				case Upload.STATE_SEND_MSG_SUCCESS:
				{
					super.handleMessage(msg);
					
					if(fileLenByKB != 0)
					{
						proDialog.setMessage(getString(R.string.feedback_sending_process_send_file));
						sendSleep(1000);
						new Thread(new SendFileThread()).start();
					}
					else
					{
						proDialog.setMessage(getString(R.string.feedback_sending_process_close));
						new Thread(new CloseThread()).start();
					}
					
					break;
				}
				
				case Upload.STATE_SEND_MSG_FAIL:
				{
					super.handleMessage(msg);
					
					resumeButton();
					proDialog.dismiss();
					createDialogWithOneButton(getResources().getString(R.string.feedback_sending_fail_title),
							android.R.drawable.alert_dark_frame, 
							getString(R.string.feedback_sending_fail_message5));
					break;
				}
			
				case Upload.STATE_SEND_FILE_SUCCESS:
				{
					super.handleMessage(msg);
					
					proDialog.setMessage(getString(R.string.feedback_sending_process_close));
					
					new Thread(new CloseThread()).start();
					break;
				}
				
				case Upload.STATE_SEND_FILE_FAIL:
				{
					super.handleMessage(msg);
					
					resumeButton();
					proDialog.dismiss();
					createDialogWithOneButton(getResources().getString(R.string.feedback_sending_fail_title),
							android.R.drawable.alert_dark_frame, 
							getString(R.string.feedback_sending_fail_message4));
					break;
				}
			
				case Upload.STATE_CLOSE_SUCCESS:
				{
					super.handleMessage(msg);
					
					Bundle bundle = msg.getData();
					int state = bundle.getInt("res");
					resumeButton();
					proDialog.dismiss();
					
					switch(state)		//接收处理
					{
						case Upload.SUCCESS:		//发送成功
						{

							Intent intent = new Intent(); 
				            intent.setClass(AndroidFeedbackActivity.this, AndroidFeedbackResultActivity.class); 
				            startActivityForResult(intent, 0);
							break;
						}
							
						case Upload.REDIRECT:		//重定向
						{
							createDialogWithOneButton(getResources().getString(R.string.feedback_sending_fail_title),
									android.R.drawable.alert_dark_frame, 
									getString(R.string.feedback_sending_fail_message0));
						
							break;
						}
							
						case Upload.CLIENT_ERROR:	  //客户端错误
						{						
							createDialogWithOneButton(getResources().getString(R.string.feedback_sending_fail_title),
									android.R.drawable.alert_dark_frame, 
									getString(R.string.feedback_sending_fail_message1));
							break;
						}
						case Upload.SERVER_ERROR:		//服务器错误
						{
							createDialogWithOneButton(getResources().getString(R.string.feedback_sending_fail_title),
									android.R.drawable.alert_dark_frame, 
									getString(R.string.feedback_sending_fail_message2));
							break;
						}
					}
					break;
				}
        	}
        }
	}
	
	private void sendSleep(long sleepTime)
	{
		try 
		{
			Thread.sleep(sleepTime);
        } 
		catch (InterruptedException e) {
             // TODO Auto-generated catch block
			e.printStackTrace();
        }
	}
	
	private void createDialogWithOneButton(String title, int iconId, String message)
	{
		new AlertDialog.Builder(AndroidFeedbackActivity.this)
		.setTitle(title)
		.setIcon(iconId)
		.setMessage(message)
		.setPositiveButton(getString(R.string.feedback_sending_fail_button), new DialogInterface.OnClickListener()
		{             
			public void onClick(DialogInterface dialog, int which) 
			{
				//do nothing						      
			}
		})
		.create()
		.show();
	}

}