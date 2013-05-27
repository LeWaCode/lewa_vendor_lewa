package com.lewa.fc;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.KeyEvent;

public class AndroidFcActivity extends Activity {
    /** Called when the activity is first created. */
	
	private final int LOG_GET = 0;
	
	private Button sendButton;
	private EditText sendEditText;
	private TextView logLenTextView;
	private TextView logDetailTextView;
	private ImageView isLogSendCheckBox;	//由ImageView替代CheckBox
	private ImageView[] imageViews = new ImageView[5];
	private ImageView arrowImageView;
	
	private ProgressDialog proDialog;
	
	private GetLogHandler getLogHandler;
	private SendProcessHandler sendHandler;
	
	private int imageId = 0;
	private boolean isLogSend = true;
	
	private String urlSend;
	private Map<String, String> paramsSend;
	
	private String phoneInfo;
	private String version;
	private String imei;
	private String time;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);    
        setContentView(R.layout.main);
        
        getPhoneMsg();
        try
        {
        	getLogHandler = new GetLogHandler();

        	Thread logWriteThread = new Thread(new LogWriteThread());   
            logWriteThread.start();   
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
        sendHandler = new SendProcessHandler(); 
        
        initImageView();
        initTextView();
        initEditText();
        initCheckBox();
        initButton();
    }
    
    private void getPhoneMsg()
    {
    	phoneInfo = Build.MODEL;
    	version = Build.DISPLAY;
    	imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
    	
    	Time t = new Time();
    	t.setToNow();
    	int year = t.year;
    	int month = t.month;
    	int date = t.monthDay;
    	int hour = t.hour; 
    	int minute = t.minute;
    	int second = t.second;
    	time = year + "/" + month + "/" + date + "  " + hour + ":" + minute + ":" + second;
    }
    
    private void initTextView()
    {
    	logLenTextView = (TextView)findViewById(R.id.log_len);
    	logDetailTextView = (TextView)findViewById(R.id.log_detail);
    	
    	logDetailTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
    	logDetailTextView.setOnClickListener(getLogListener);
    }
    
    private void initEditText()
    {
    	sendEditText = (EditText)findViewById(R.id.send_text);

    }
    
    private void initCheckBox()
    {
    	isLogSendCheckBox = (ImageView)findViewById(R.id.log_send);
    	isLogSendCheckBox.setImageDrawable(getResources().getDrawable(
    			R.drawable.check_on));
    	
    	isLogSendCheckBox.setOnClickListener(new OnClickListener() {  

			@Override
			public void onClick(View arg0) 
			{
				// TODO Auto-generated method stub
				if(isLogSend)
				{
					isLogSend = false;
					isLogSendCheckBox.setImageDrawable(getResources().getDrawable(
		                    R.drawable.check_off));
				}
				else
				{
					isLogSend = true;
					isLogSendCheckBox.setImageDrawable(getResources().getDrawable(
		                    R.drawable.check_on));
				}
			}
    	}); 
    }
    
    private void initImageView()
    {
    	imageViews[0] = (ImageView)findViewById(R.id.image1); 
    	imageViews[1] = (ImageView)findViewById(R.id.image2);
    	imageViews[2] = (ImageView)findViewById(R.id.image3);
    	imageViews[3] = (ImageView)findViewById(R.id.image4);
    	imageViews[4] = (ImageView)findViewById(R.id.image5);
    	
    	arrowImageView = (ImageView)findViewById(R.id.arrow_image);
    	
    	imageViews[0].setOnClickListener(new OnClickListener() {   
    		public void onClick(View v) {   
    			imageId = 0;
    			arrowImageView.setImageDrawable(getResources().getDrawable(
                        R.drawable.triangle1));
    		}
    	});
    	
    	imageViews[1].setOnClickListener(new OnClickListener() {   
    		public void onClick(View v) {   
    			imageId = 1;
    			arrowImageView.setImageDrawable(getResources().getDrawable(
                        R.drawable.triangle2));
    		}
    	});
    	
    	imageViews[2].setOnClickListener(new OnClickListener() {   
    		public void onClick(View v) {   
    			imageId = 2;
    			arrowImageView.setImageDrawable(getResources().getDrawable(
                        R.drawable.triangle3));
    		}
    	});
    	
    	imageViews[3].setOnClickListener(new OnClickListener() {   
    		public void onClick(View v) {   
    			imageId = 3;
    			arrowImageView.setImageDrawable(getResources().getDrawable(
                        R.drawable.triangle4));
    		}
    	});
    	
    	imageViews[4].setOnClickListener(new OnClickListener() {   
    		public void onClick(View v) {   
    			imageId = 4;
    			arrowImageView.setImageDrawable(getResources().getDrawable(
                        R.drawable.triangle5));
    		}
    	});
    }
    
    private void initButton()
    {
    	sendButton = (Button)findViewById(R.id.send_button);
    	sendButton.setOnClickListener(sendListener);
    }
    
    private void lockButton()
    {
    	sendButton.setClickable(false);
    }
    
    private void resumeButton()
    {
    	sendButton.setClickable(true);
    }
    
    class ConnectThread implements Runnable {
		
    	public void run()
    	{
    		String url = AndroidFcActivity.this.urlSend;
    		
    		boolean state = Upload.connectStart(url);
    		
    		Message msg = new Message();
    		msg.what = state == true? Upload.STATE_CONNECT_SUCCESS: Upload.STATE_CONNECT_FAIL;
    		AndroidFcActivity.this.sendHandler.sendMessage(msg); 

    	}
    }
	
	class SendMsgThread implements Runnable {
		
		public Map<String, String> strParams;
		
    	public void run()
    	{
    		Map<String, String> strParams = AndroidFcActivity.this.paramsSend;
    		
    		boolean state = Upload.sendMessage(strParams);
    		
    		Message msg = new Message();
    		msg.what = state == true? Upload.STATE_SEND_MSG_SUCCESS: Upload.STATE_SEND_MSG_FAIL;
    		AndroidFcActivity.this.sendHandler.sendMessage(msg); 
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
    		
    		AndroidFcActivity.this.sendHandler.sendMessage(msg); 

    	}
    }

    class GetLogHandler extends Handler {
   
    	public GetLogHandler() {
    		
    	}

    	public GetLogHandler(Looper L) {
    		super(L);
    	}

        @Override
        public void handleMessage(Message msg) {
        	// TODO Auto-generated method stub
        	switch(msg.what){

			case LOG_GET:
				super.handleMessage(msg);
	            Bundle bundle = msg.getData();
	           	String logLen = String.valueOf(bundle.getInt("logLen"));
	         	AndroidFcActivity.this.logLenTextView.setText(logLen);
			}
        	
        	super.handleMessage(msg);
         		
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
					
					proDialog.setMessage(getString(R.string.fc_sending_process_send_msg));
					sendSleep(1000);
					new Thread(new SendMsgThread()).start();
					break;
				}
				
				case Upload.STATE_CONNECT_FAIL:
				{
					super.handleMessage(msg);
					
					resumeButton();
					proDialog.dismiss();
					
					createDialogWithOneButton(getResources().getString(R.string.fc_sending_fail_title),
							android.R.drawable.alert_dark_frame, 
							getString(R.string.fc_sending_fail_message4));
				
					break;
				}
				
				case Upload.STATE_SEND_MSG_SUCCESS:
				{
					super.handleMessage(msg);
					
					proDialog.setMessage(getString(R.string.fc_sending_process_close));
					sendSleep(1000);
					new Thread(new CloseThread()).start();
					
					break;
				}
				
				case Upload.STATE_SEND_MSG_FAIL:
				{
					super.handleMessage(msg);
					
					resumeButton();
					proDialog.dismiss();
					
					createDialogWithOneButton(getResources().getString(R.string.fc_sending_fail_title),
							android.R.drawable.alert_dark_frame, 
							getString(R.string.fc_sending_fail_message5));
					break;
				}
			
				case Upload.STATE_CLOSE_SUCCESS:
				{
					super.handleMessage(msg);
					
					Bundle bundle = msg.getData();
					int state = bundle.getInt("res");
					resumeButton();
					proDialog.dismiss();
					
					switch(state)	//接收处理
					{
						case Upload.SUCCESS:		//发送成功
						{
							Intent intent = new Intent(); 
				            intent.setClass(AndroidFcActivity.this, AndroidFcResultActivity.class); 
				            startActivityForResult(intent, 0);
				            
							break;
						}
							
						case Upload.REDIRECT:		//重定向
						{
							createDialogWithOneButton(getResources().getString(R.string.fc_sending_fail_title),
									android.R.drawable.alert_dark_frame, 
									getString(R.string.fc_sending_fail_message0));
						
							break;
						}
							
						case Upload.CLIENT_ERROR:	  //客户端错误
						{
							createDialogWithOneButton(getResources().getString(R.string.fc_sending_fail_title),
									android.R.drawable.alert_dark_frame, 
									getString(R.string.fc_sending_fail_message1));
							break;
						}
						case Upload.SERVER_ERROR:		//服务器错误
						{
							createDialogWithOneButton(getResources().getString(R.string.fc_sending_fail_title),
									android.R.drawable.alert_dark_frame, 
									getString(R.string.fc_sending_fail_message2));
							break;
						}
					}
					break;
				}
        	}
        }
	}
  
    class LogWriteThread implements Runnable {
    	public void run()
    	{
    		MyLog.logWrite();
    		
    		Message msg = new Message();
    		msg.what = LOG_GET;
    		Bundle bundle = new Bundle();// 存放数据
    		bundle.putInt("logLen", (MyLog.getErrorText().length() / 1024) + 1);
    		msg.setData(bundle);
    		
    		AndroidFcActivity.this.getLogHandler.sendMessage(msg); // 向Handler发送消息,更新UI
    	}
    }
    
    private void sendData()
    {

		Map<String, String> strParams = new HashMap<String, String>();
		strParams.put("phoneInfo_lewa", this.phoneInfo);	//手机型号
		strParams.put("version_lewa", this.version);	//版本号
		strParams.put("time_lewa", this.time);	//时间
		strParams.put("imei_lewa", imei);	//Imei号		
		strParams.put("imageId_lewa", String.valueOf(this.imageId));		//图片ID
		strParams.put("userDetail_lewa", sendEditText.getText().toString());		//用户输入信息
		
		if(isLogSend)
		{
			strParams.put("logFile_lewa", MyLog.getErrorText());	//log信息
		}
				
		this.urlSend = SystemConstants.UPLOAD_LOG_URL;
		this.paramsSend = strParams;

		proDialog.setMessage(getString(R.string.fc_sending_process_connect));
		sendSleep(1000);
		new Thread(new ConnectThread()).start();
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
  
			proDialog = ProgressDialog.show(AndroidFcActivity.this,
					getResources().getString(R.string.fc_sending_title), 
					getResources().getString(R.string.fc_sending_message), true, true);
				
			lockButton();
			sendData();
			
    	}
     };
     
     private OnClickListener getLogListener = new OnClickListener() {   
    	public void onClick(View v) {   
    		Intent intent = new Intent(); 
            intent.setClass(AndroidFcActivity.this, LogDetailActivity.class); 
            startActivity(intent); 
    	}
     };
     
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
 		new AlertDialog.Builder(AndroidFcActivity.this)
 		.setTitle(title)
 		.setIcon(iconId)
 		.setMessage(message)
 		.setPositiveButton(getString(R.string.fc_sending_fail_button), new DialogInterface.OnClickListener()
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