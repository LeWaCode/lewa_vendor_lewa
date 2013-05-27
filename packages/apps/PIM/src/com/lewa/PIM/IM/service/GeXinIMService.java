package com.lewa.PIM.IM.service;

import static android.content.Intent.ACTION_BOOT_COMPLETED;

import java.io.File;
import java.util.ArrayList;
import java.util.TimeZone;
import android.preference.PreferenceManager;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Inbox;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.os.Bundle;

import com.lewa.PIM.IM.IMMessage;
import com.lewa.PIM.IM.event.EventManager;
import com.lewa.PIM.mms.transaction.MessagingNotification;
import com.lewa.PIM.mms.ui.MessageUtils;
import com.lewa.PIM.mms.ui.MessagingPreferenceActivity;

import im.gexin.talk.data.Const;
import im.gexin.talk.util.UiUtils;

/**
 * This service essentially plays the role of a "worker thread", allowing us to
 * store incoming messages to the database, update notifications, etc. without
 * blocking the main thread that SmsReceiver runs on.
 */
public class GeXinIMService extends IMService {

	private static boolean mLogin = false;

	protected void ProcessMessage(Message msg) {
		Intent intent = (Intent) msg.obj;
		Intent ResultIntent = new Intent();
		SharedPreferences sp;
		String settingRemind;
		if (intent != null) {
			String action = intent.getAction();
			if (ACTION_SEND_MESSAGE.endsWith(action)) {
				Log.d(TAG, "Receiver GEXIN CMD  = ACTION_SEND_MESSAGE");
				IMMessage sendMessage = IMMessage
						.getIMmessageFromIntent(intent);
				handleSendMessage(sendMessage);		
			} else if (ACTION_START_SERVICE.endsWith(action)) {
				Log.d(TAG, "Receiver GEXIN CMD  = ACTION_START_SERVICE");
				UserStartService();
			} else if (ACTION_STOP_SERVICE.endsWith(action)) {
				Log.d(TAG, "Receiver GEXIN CMD  = ACTION_STOP_SERVICE");
				UserStopService();
			} else if (ACTION_USER_LOGIN.endsWith(action)) {
				Log.d(TAG, "Receiver GEXIN CMD  = ACTION_USER_LOGIN");
				UserLogin();
			} else if (ACTION_USER_LOGOUT.endsWith(action)) {
				Log.d(TAG, "Receiver GEXIN CMD  = ACTION_USER_LOGOUT");
				UserLogout();
			} else if (ACTION_CHECK_USER_REGISTER.endsWith(action)) {
				Log.d(TAG, "Receiver GEXIN CMD  = ACTION_CHECK_USER_REGISTER");
				CheckUserRegister(intent.getExtras());
			} else if (ACTION_CHECK_USER_ONLINE_STATUS.endsWith(action)) {
				Log.d(TAG, "Receiver GEXIN CMD  = ACTION_CHECK_USER_ONLINE_STATUS");
				CheckUserOnlineStatus(intent.getExtras());
			} else if (Const.SERVER_ACTION_UPDATEUI.endsWith(action)) {

				ArrayList<Bundle> bundleList = intent
						.getParcelableArrayListExtra("bundle");
				for (Bundle bundle : bundleList) {					
					int cmd = bundle.getInt(Const.CMD, -2);
					Log.d(TAG, "Receiver GEXIN MSG = " + getReceiveCmdName(cmd));
					switch (cmd) {
					case Const.UPDATEUI_BroadcastReceived:
						UiUtils.sendCacheCMD(serviceInstance);
						break;
					case Const.UPDATEUI_ReceiveNewMsg:
						handleReceivedMessage(bundle);
						break;
					case Const.UPDATEUI_MMS_DOWNLOAD_OK:
						handleReceivedMmsMessage(bundle);
						break;
					case Const.UPDATEUI_MMS_DOWNLOAD_FAILED:
						long mmsID = bundle.getLong("mmsID");
						String phoneNum = bundle.getString("phone");
						Log.d(TAG, "mms download failed! mmsID : " + mmsID
								+ "Phone num" + phoneNum);

						// retry
						if (Math.random() > 0.5)// random 
						{
							Bundle reqbundle = new Bundle();
							reqbundle.putLong("mmsID", mmsID);
							reqbundle.putString("phone", phoneNum);
							UiUtils.sendCMD(this,
									Const.SERVERACTION_DOWNLOAD_MMS, reqbundle);
						}
						break;
					case Const.UPDATEUI_NEED_SEND_SMS:
				        sp = getSharedPreferences(MessagingPreferenceActivity.IMS_AOUT_TO_SMS, Context.MODE_WORLD_READABLE);
				        settingRemind = sp.getString(MessagingPreferenceActivity.IMS_AOUT_TO_SMS, "false");
				        
				        if (settingRemind.equals("true")) {
							resendMessageBySms(bundle);
						}else {
							handleSmsSent(bundle, IMService.IM_SEND_MESSAGE_FAILED);
						}
				        
						break;
					case Const.UPDATEUI_NEED_SEND_MMS:
						handleSmsSent(bundle,IMService.IM_SEND_MESSAGE_FAILED);
						break;
					case Const.UPDATEUI_SendMsgSuccessIntoServer:
						break;
					case Const.UPDATEUI_MsgIsSendding:
						handleSmsUpdateID(bundle);
						break;
					case Const.UPDATEUI_SendMsgSuccess:
						handleSmsSent(bundle,IMService.IM_SEND_MESSAGE_OK);
						break;
					case Const.UPDATEUI_DisConnect:
						ResultIntent.putExtra("com.lewa.PIM.IM.result",
								IMService.IM_REQUEST_RESULT_OK);
						EventManager.inst().fireEvent(this,
								IMService.REQUEST_USER_LOGOUT[GeXinIM],
								ResultIntent);
						mLogin = false;
						break;
					case Const.UPDATEUI_ReConnectSuccess:
						ResultIntent.putExtra("com.lewa.PIM.IM.result",
								IMService.IM_REQUEST_RESULT_OK);
						EventManager.inst().fireEvent(this,
								IMService.REQUEST_USER_LOGIN[GeXinIM],
								ResultIntent);
						mLogin = true;
						break;
					case Const.UPDATEUI_NEED_REGISTER:
						 sp = this.getSharedPreferences(MessagingPreferenceActivity.IMS_CLOSE_STATE, Context.MODE_WORLD_READABLE);
						 settingRemind = sp.getString(MessagingPreferenceActivity.IMS_CLOSE_STATE, "false");   
		  				 if (settingRemind.equals("true")){
							UserRegister();
	  				 	  }
						break;						
					case Const.UPDATEUI_CHECK_ONLINE_RESULT:
						String phone = bundle.getString("phone");
						boolean isOnline = bundle.getBoolean("online");
						
						Bundle reqbundle = new Bundle();
						reqbundle.putString("phone", phone);
						reqbundle.putBoolean("online",isOnline);
						ResultIntent.putExtras(reqbundle);
						EventManager
								.inst()
								.fireEvent(
										this,
										IMService.REQUEST_CHECK_USER_ONLINE_STATUS[IMService.GeXinIM],
										ResultIntent);
						Log.d(TAG, "phone = " + phone + " is Online = "
								+ isOnline);
						break;
					case Const.UPDATEUI_ReadFriendFailed:
						ResultIntent.putExtra("com.lewa.PIM.IM.result",
								IMService.IM_REQUEST_RESULT_FAILED);
						EventManager
								.inst()
								.fireEvent(
										this,
										IMService.REQUEST_CHECK_USER_REGISTER[IMService.GeXinIM],
										ResultIntent);
						break;
					case Const.UPDATEUI_ReadFriendOK:
						ResultIntent.putExtra("com.lewa.PIM.IM.result",
								IMService.IM_REQUEST_RESULT_OK);
						ArrayList<String> registeredPhones = bundle
								.getStringArrayList("register");
						if (registeredPhones != null
								&& !registeredPhones.isEmpty()) {
							for (String registerPhone : registeredPhones) {
								Log.d(TAG, "Register Phone = " + registerPhone);
							}
						}
						ResultIntent.putExtras(bundle);
						EventManager
								.inst()
								.fireEvent(
										this,
										IMService.REQUEST_CHECK_USER_REGISTER[IMService.GeXinIM],
										ResultIntent);
					}
				}
			}
		}
	}

	private void handleSendMessage(IMMessage sendMessage) {
		if (!mLogin) {
			UiUtils.sendCMD(this, Const.SERVERACTION_RELOGIN, null);
		}
		Uri smsUri = null;

		if (sendMessage.getSendFileUrls() != null
				&& sendMessage.getSendFileUrls().length > 0) {
			long mTimestamp = System.currentTimeMillis();
			String fileUriString = sendMessage.getSendFileUrls()[0];
			Uri fileUri = Uri.parse(fileUriString);
			String MimeType = sendMessage.getSendFileUrlsMimeType()[0];
			File sendFile = new File(fileUri.getPath());
			try {
				ContentValues values = new ContentValues(3);
				values.put(Sms.ADDRESS, sendMessage.getSendAddress());
				values.put(MessageUtils.PduYlColumns.PATH, sendFile.getParent());
				values.put(MessageUtils.PduYlColumns.FILE_NAME,
						sendFile.getName());
				values.put(MessageUtils.PduYlColumns.M_DATA_TYPE, MimeType);
				Log.d(TAG, "Path =[" + sendFile.getParent() + "] name = ["
						+ sendFile.getName() + "} MimeType =[" + MimeType + "]");
				smsUri = SqliteWrapper.insert(this, this.getContentResolver(),
						MessageUtils.PduYlColumns.IMS_OUTBOX_URI, values);

			} catch (SQLiteException e) {
				SqliteWrapper.checkSQLiteException(this, e);
			}
			if (smsUri != null) {
				smsqueue.offer(smsUri);
			}
			Bundle bundle = new Bundle();
			bundle.putLong("mmsID", UiUtils.getNextMmsID());
			bundle.putString("phone", sendMessage.getSendAddress());
			bundle.putStringArray("fileName",
					new String[] { sendFile.getName() });
			bundle.putStringArray("mime", new String[] { MimeType });
			bundle.putStringArray("uri", new String[] { fileUriString });
			bundle.putString("subject", "Subject");
			UiUtils.sendCMD(this, Const.SERVERACTION_SEND_GXMMS, bundle);
		} else {
			long mTimestamp = System.currentTimeMillis();
			try {
				ContentValues values = new ContentValues(2);
				values.put(Sms.ADDRESS, sendMessage.getSendAddress());
				values.put(Inbox.BODY, sendMessage.getText());
				smsUri = SqliteWrapper.insert(this, this.getContentResolver(),
						MessageUtils.PduYlColumns.IMS_OUTBOX_URI, values);

			} catch (SQLiteException e) {
				SqliteWrapper.checkSQLiteException(this, e);
			}
			if (smsUri != null) {
				smsqueue.offer(smsUri);
			}
			Bundle bundle = new Bundle();
			bundle.putString("phone", sendMessage.getSendAddress());
			bundle.putString("content", sendMessage.getText());
			UiUtils.sendCMD(this, Const.SERVERACTION_SEND_MESSAGE, bundle);

		}
	}

	private void handleSmsSent(Bundle bundle, int SentResult) {
		int count;
		ContentValues values = new ContentValues(1);
		int msgID = bundle.getInt("msgID");
		Log.v(TAG, "handleSmsSent sending mid =  " + msgID);
		if (SentResult == IMService.IM_SEND_MESSAGE_OK) {
			values.put(MessageUtils.PduYlColumns.MSG_BOX, 2);
		}
		else {
			values.put(MessageUtils.PduYlColumns.MSG_BOX, 5);
		}
		count = this.getContentResolver().update(
				MessageUtils.PduYlColumns.IMS_URI, values, "m_id = " + msgID,
				null);

	}

	private void handleSmsUpdateID(Bundle bundle) {
		int count;
		ContentValues values = new ContentValues(1);
		Uri senduri = smsqueue.poll();
		Log.v(TAG, "handleSmsSent sending uri: " + senduri);
		int msgID = bundle.getInt("msgID");
		values.put("m_id", "" + msgID);
		Log.v(TAG, "handleSmsUpdateID Message mid =  " + msgID);
		if (senduri != null) {
			count = this.getContentResolver().update(
					MessageUtils.PduYlColumns.IMS_URI, values,
					"_id = " + senduri.getPathSegments().get(0), null);
		}

	}

	protected boolean UserLogout() {
		UiUtils.sendCMD(this, Const.SERVERACTION_DISCONNECT, null);
		return true;
	}

	protected boolean UserLogin() {
		UiUtils.sendCMD(this, Const.SERVERACTION_RELOGIN, null);
		return true;
	}

	protected boolean UserStartService() {
		UiUtils.sendCMD(this, Const.SERVERACTION_ACTIVITY_START, null);
		return true;
	}

	protected boolean UserStopService() {
		UiUtils.sendCMD(this, Const.SERVERACTION_STOPSERVICE, null);
		return true;
	}

	protected boolean UserRegister() {
		UiUtils.sendCMD(this, Const.SERVERACTION_REGISTER, null);
		return true;
	}

	private boolean CheckUserRegister(Bundle parambundle) {
		Bundle bundle = new Bundle();
		ArrayList<String> userArrayList = parambundle
				.getStringArrayList("com.lewa.PIM.IM.content.EXTRA_STRING_ARRAY");
		bundle.putStringArray("phones", (String[]) (userArrayList
				.toArray(new String[userArrayList.size()])));
		UiUtils.sendCMD(this, Const.SERVERACTION_CHECK_REGISTER, bundle);
		return true;
	}

	private boolean CheckUserOnlineStatus(Bundle parambundle) {

		String[] queryNums = parambundle
				.getStringArray("com.lewa.PIM.IM.content.EXTRA_STRING_ARRAY");
		
		boolean mNetworkConnect = checkNetworkConnect();
		Log.d(TAG, "network is connected  =  " + mNetworkConnect);
		for (String tempString : queryNums) {
			Bundle bundle = new Bundle();
			bundle.putString("phone", tempString);
			Log.d(TAG, "Check phone [" + tempString + "] status");
			if(mNetworkConnect){
				UiUtils.sendCMD(this, Const.SERVERACTION_CHECK_ONLINE_STATUS,
					bundle);
			}
			else {
				Intent ResultIntent = new Intent();
				Bundle reqbundle = new Bundle();
				reqbundle.putString("phone", tempString);
				reqbundle.putBoolean("online",false);
				ResultIntent.putExtras(reqbundle);
				EventManager
						.inst()
						.fireEvent(
									this,
									IMService.REQUEST_CHECK_USER_ONLINE_STATUS[IMService.GeXinIM],
									ResultIntent);					
			}
		}
		return true;
	}

	
	/** method for clients */
	@Override
	public boolean getLoginStatus() {
		return mLogin;
	}

	final private String getReceiveCmdName(int id) {
		switch (id) {
		case -1:
			return "BroadcastReceived";
		case 0:
			return "Service_Start";
		case 1:
			return "LoginFailed";
		case 2:
			return "DisConnect";
		case 3:
			return "ReConnectSuccess";
		case 4:
			return "SendRegisterSMSFailed";
		case 5:
			return "UpdateMsgStatus";
		case 6:
			return "MsgIsSendding";
		case 7:
			return "SendMsgSuccess";
		case 8:
			return "SendMsgSuccessIntoServer";
		case 9:
			return "SendMsgSuccessBySMS";
		case 10:
			return "SendMsgFailed";
		case 11:
			return "ReceiveNewMsg";
		case 12:
			return "ReceiveNewSMS";
		case 13:
			return "ReceiveNewMMS";
		case 14:
			return "MmsDownloadOk";
		case 15:
			return "MmsDownloadFailed";
		case 16:
			return "ReadLocalDataOK";
		case 17:
			return "ReadLocalDataFailed";
		case 18:
			return "ReadFriendOK";
		case 19:
			return "ReadFriendFailed";
		case 20:
			return "IamDead";
		case 21:
			return "UpdateVersion";
		case 22:
			return "UpdateVersion_New_Version";
		case 23:
			return "UpdateVersion_No_New_Version";
		case 24:
			return "NoNetwork";
		case 25:
			return "SD_CARD_UNAVAILABLE";
		case 26:
			return "NEED_SEND_MMS_SMS";
		case 27:
			return "NEED_SEND_MMS";
		case 28:
			return "NEED_SEND_SMS";
		case 29:
			return "DOWNLOAD_REALIMAGE_OK";
		case 30:
			return "DOWNLOAD_REALIMAGE_FAILED";
		case 31:
			return "REFRESH_ONLINE_STATUS";
		case 32:
			return "CHECK_ONLINE_RESULT";
		case 33:
			return "FLUX_COUNT_OK";
		case 34:
			return "FLUX_COUNT_FAILED";
		case 35:
			return "REFRESH_PROFILE";
		case 36:
			return "NEED_SET_PROFILE";
		case 37:
			return "SET_PROFILE_OK";
		case 38:
			return "SET_PROFILE_FAILED";
		case 39:
			return "UPDATE_PROFILE";
		case 40:
			return "PROFILE_DOWNLOAD_FAILED";
		case 41:
			return "REFRESH_NET_PHOTO";
		case 42:
			return "UPDATEUI_NEED_REGISTER";
		default:
			return "unknown";
		}
	}

}
