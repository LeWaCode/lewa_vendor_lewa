package com.lewa.PIM.mms.MsgPopup;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.internal.telephony.CallerInfo;
import com.lewa.os.util.LocationUtil;
import com.lewa.PIM.IM.IMClient;
import com.lewa.PIM.IM.IMMessage;
import com.lewa.PIM.IM.service.IMService;
import com.lewa.PIM.mms.LogTag;
import com.lewa.PIM.mms.transaction.MessagingNotification;
import com.lewa.PIM.mms.transaction.SmsReceiver;
import com.lewa.PIM.mms.transaction.SmsReceiverService;
import com.lewa.PIM.mms.ui.MessageUtils;
import com.lewa.PIM.mms.ui.MessagingPreferenceActivity;
import com.lewa.PIM.mms.ui.NewMessageComposeActivity;
import com.lewa.PIM.mms.util.Recycler;
import com.lewa.PIM.mms.util.SmileyParser;
import com.lewa.PIM.mms.data.Conversation;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.PIM.util.RosterResponseReceiver;
import com.lewa.PIM.R;
import com.lewa.os.util.ContactPhotoLoader;

public class DestktopMessageActivity extends Activity implements
        OnGestureListener, OnClickListener {
    private Context                 mContext;
    private MsgViewFlipper          mFlipper;
    private ImageView               mUserIcon;
    private ImageView               mCancelIcon;
    private ImageView               mReplyIcon;
    private TextView                mUserName;
    private TextView                mLocation;
    private TextView                mContent;
    private TextView                mTime;
    private TextView                mContentIndicator;
    private TextView                mSmsCount;
    private EditText                mReplyText;
    private GestureDetector         mGestureDetector;
    private ContactPhotoLoader      mPhotoLoader;
    private HashMap<String, String> mPhotoList;
    public static String            mActionString      = "";
    public static boolean           mIsMessagePOPStart = false;
    private  boolean mRequestDeliveryReport;
    
    private PopupReceiver           myReceiver;
    private RelativeLayout          mMsgCountPanl;
    private ThreadPopMessageQueryHandler mQueryHandler;
    private RosterResponseReceiver mResponseReceiver;
    
    private static final int POP_SMS_QUERY_TOKEN       = 1001;
    private static final int POP_MMS__QUERY_TOKEN      = 1002;
    private static final int POP_IMS__QUERY_TOKEN      = 1003;
    
    private static final int NOTIFICATION_ID 		   = 123;
    
    private static final String IMS_OFFLINE			   = "OFFLINE";
    private static final String IMS_ONLINE			   = "ONLINE";
    private static final String NO_IMS_USER			   = "NULL";
    
    private static DestktopMessageActivity mDestktopMessageActivity = null;
    private HashMap<String, String> mSmsIdMap;

    public static void setActionString(String s) {
        mActionString = s;
    }

    public static void setMessagePOPStart(boolean state) {
        mIsMessagePOPStart = state;
    }

    public static boolean getMessagePOPStart() {
        return mIsMessagePOPStart;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.msgmain);

        mContext = this;        
        mDestktopMessageActivity = this;
        
        mGestureDetector = new GestureDetector(this);
        mFlipper = (MsgViewFlipper) findViewById(R.id.sms_list);
        mReplyIcon = (ImageView) findViewById(R.id.reply_btn);
        mReplyText = (EditText) findViewById(R.id.reply_area);
        mReplyText.addTextChangedListener(mTextWatcher);
        mReplyText.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				MessagingNotification.cancelNotification(mContext, NOTIFICATION_ID);;
			}
		});
        mContentIndicator = (TextView) findViewById(R.id.content_indicator);
        mSmsCount = (TextView) findViewById(R.id.smscount);
        mMsgCountPanl = (RelativeLayout) findViewById(R.id.smscount_panl);

        mReplyIcon.setOnClickListener(this);
        
        mQueryHandler = new ThreadPopMessageQueryHandler(getContentResolver());
        
        IntentFilter filterContact = new IntentFilter();
		mResponseReceiver = new RosterResponseReceiver();
		filterContact.addAction(RosterResponseReceiver.ACTION_YILIAO_STATUS_NUMBERS_DETAIL);
		registerReceiver(mResponseReceiver, filterContact);
        
        myReceiver = new PopupReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.lewa.PIM.mms.MsgPopup.POPUP_Receive");
        filter.addAction(RosterResponseReceiver.ACTION_YILIAO_STATUS_NUMBERS_ONLINE);
        registerReceiver(myReceiver, filter);
        
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture, R.drawable.ic_contact_header_unknow);
        mPhotoList = MessageUtils.getPhotoId(mContext.getContentResolver());
        
        mSmsIdMap = new HashMap<String, String>();
        //read new message
        startQuryMessage();
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mRequestDeliveryReport = prefs.getBoolean(
                MessagingPreferenceActivity.SMS_DELIVERY_REPORT_MODE,
                false);
    }

    private class PopupReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
        	if (intent == null || intent.getAction() == null){
        		return;
        	}
        	
        	if (intent.getAction().equals("com.lewa.PIM.mms.MsgPopup.POPUP_Receive")) {
                startQuryMessage();				
			}else if (intent.getAction().equals(RosterResponseReceiver.ACTION_YILIAO_STATUS_NUMBERS_ONLINE)) {
                String number = intent.getStringExtra("phone");
                boolean isOnline = intent.getBooleanExtra("online", false);
                
                if (isOnline) {
                    View view = mFlipper.getChildAt(mFlipper.getDisplayedChild());
                    TextView name = (TextView) view.findViewById(R.id.user_name);
                    String mobileReceiver = MessageUtils.fixPhoneNumber(name.getTag().toString());
                    
                    if (mobileReceiver.equals(number)) {
						ImageView imageView = (ImageView)view.findViewById(R.id.imessage_status);
						imageView.setBackgroundResource(R.drawable.icon_contact_header_online);
						imageView.setTag(IMS_ONLINE);
					}
				}
                updateButtonState();
			}
        }
    };

    private void startQuryMessage() {
        Uri uri;
        if (mActionString.equals("android.provider.Telephony.SMS_RECEIVED")) {
            uri = Uri.parse("content://sms/inbox");
        } 
//        else if (mActionString.equals("android.provider.Telephony.WAP_PUSH_RECEIVED")) {
//            uri = Uri.parse("content://mms/inbox");
//        }
        else if(mActionString.equals("com.lewa.PIM.IM.MESSAGE_RECEIVED")) {
        	uri = Uri.parse("content://mms-sms/pdu_yl");
        }
        else {
            
            if (mFlipper.getChildCount() == 0) {
                finish();                
            }
            return;
        }
        this.startQury(uri);
    }
    
    private void setMessageInfo(HashMap<String, String> info){
        if (info.size() == 0) {
            if (mFlipper.getChildCount() == 0) {
                finish();
            }
            return;
        }
        
        if (mFlipper.getChildCount() != 0) {
            mTime = (TextView) mFlipper.getChildAt(mFlipper.getDisplayedChild()).findViewById(R.id.time);
            mUserName = (TextView) mFlipper.getChildAt(mFlipper.getDisplayedChild()).findViewById(R.id.user_name);
            //sPreDate = mTime.getText().toString();
            //sTelephone = mUserName.getTag().toString().trim();
        }
        
        if (mFlipper.getChildCount() == 0) {
            mMsgCountPanl.setVisibility(View.GONE);
        }
        else {
            mMsgCountPanl.setVisibility(View.VISIBLE);
        }
        
        View v = getView(info);
        
        Uri uri = Uri.parse(info.get("msg_uri"));
        
        if (uri.equals(Uri.parse("content://sms/inbox")) ||
        	uri.equals(Uri.parse("content://mms-sms/pdu_yl")) ) {
            v.setTag(info.get("sms_id"));
        } 
        else {
            v.setTag(info.get("mms_id"));
        }
        
        mFlipper.invalidate();
        mFlipper.addView(v);
        AddSetsmscount();
        mFlipper.setDisplayedChild(mFlipper.getDisplayedChild());            
        setSmsCount();     
        updateButtonState();
    }

    static final String[] PHONES_SUMMARY_PROJECTION = new String[] {
            Contacts.DISPLAY_NAME, CommonDataKinds.Phone.NUMBER,
            Contacts.SORT_KEY_PRIMARY, CommonDataKinds.Phone.CONTACT_ID,
            Photo.PHOTO_ID                         };

    TextWatcher mTextWatcher = new TextWatcher() {
         @Override
         public void afterTextChanged(Editable s) {
         }

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String string = s.toString();
			if (!TextUtils.isEmpty(string)) {
				showTextCount(s);
			}             
			updateButtonState();             
		}
     };

    private View getView(HashMap<String, String> smsInfo) {
        View layout = LayoutInflater.from(this).inflate(R.layout.msgflipper_item, null);
        mUserIcon = (ImageView) layout.findViewById(R.id.user_icon);
        mCancelIcon = (ImageView) layout.findViewById(R.id.cancel_btn);
        mUserName = (TextView) layout.findViewById(R.id.user_name);
        mUserName.setOnClickListener(this);
        mUserName.setSelected(true);
        mLocation = (TextView) layout.findViewById(R.id.location);
        mContent = (TextView) layout.findViewById(R.id.content);
        mTime = (TextView) layout.findViewById(R.id.time);        
        ImageView delMessagBtn = (ImageView) layout.findViewById(R.id.delmsg_icon);
        delMessagBtn.setOnClickListener(this);
        
        ImageView imsimage = (ImageView)layout.findViewById(R.id.contact_item_yl_image);
        LinearLayout stateLayout = (LinearLayout) layout.findViewById(R.id.imessage_status_layout);
        String address = "";
        Uri uri = Uri.parse(smsInfo.get("msg_uri"));
        mUserIcon.setTag(uri);
        
        if (uri.equals(Uri.parse("content://sms/inbox")) ||
        	uri.equals(Uri.parse("content://mms-sms/pdu_yl"))) {
            if (!TextUtils.isEmpty(smsInfo.get("sms_name"))) {
                mUserName.setText(Html.fromHtml("<u>"+smsInfo.get("sms_name")+"</u>"));
                mLocation.setText(smsInfo.get("sms_address"));
            } else {
                mUserName.setText(Html.fromHtml("<u>"+smsInfo.get("sms_address")+"</u>"));
                mLocation.setText(LocationUtil.getPhoneLocation(this, smsInfo.get("sms_address")));
            }
            
            mUserName.setTag(smsInfo.get("sms_address"));
            mContent.setMovementMethod(ScrollingMovementMethod.getInstance());
            
            SpannableStringBuilder buf = new SpannableStringBuilder();
            SmileyParser parser = SmileyParser.getInstance();
            buf.append(parser.addSmileySpans(smsInfo.get("sms_body")));
            mContent.setText(buf);            
            mTime.setText(smsInfo.get("sms_date"));
            address = smsInfo.get("sms_address");
            
            ArrayList<String> list = new ArrayList<String>();
            list.add(MessageUtils.fixPhoneNumber(address));
            
            ImageView imageOnline = (ImageView)layout.findViewById(R.id.imessage_status);
            
            if (MessageUtils.isYiliaoNumber(mContext, list)) {
				imsimage.setVisibility(View.GONE);
				stateLayout.setVisibility(View.VISIBLE);			
				imageOnline.setTag(IMS_OFFLINE);
				CommonMethod.CheckUserOnlineStatus(mDestktopMessageActivity, list);
			}else {
				imageOnline.setTag(NO_IMS_USER);
				imsimage.setVisibility(View.GONE);
				stateLayout.setVisibility(View.GONE);
			}
        } else if (uri.equals(Uri.parse("content://mms/inbox"))) {
            if (!TextUtils.isEmpty(smsInfo.get("mms_name"))) {
                mUserName.setText(Html.fromHtml("<u>"+smsInfo.get("mms_name")+"</u>"));
                mLocation.setText(smsInfo.get("mms_address"));                
            } else {
                mUserName.setText(Html.fromHtml("<u>"+smsInfo.get("mms_address")+"</u>"));
                mLocation.setText(LocationUtil.getPhoneLocation(this,smsInfo.get("mms_address")));
            }
            mUserName.setTag(smsInfo.get("mms_address"));
            mContent.setMovementMethod(ScrollingMovementMethod.getInstance());
            mContent.setText(smsInfo.get("mms_subject"));
            mTime.setText(smsInfo.get("mms_date"));

            address = MessageUtils.fixPhoneNumber(smsInfo.get("mms_address"));
            imsimage.setVisibility(View.GONE);
        }

        mContent.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                TextView name = (TextView) mFlipper.getChildAt(mFlipper.getDisplayedChild()).findViewById(R.id.user_name);                
                String number = name.getTag().toString().trim();
                
                Uri msgUri = Uri.parse("smsto:" + number);
                Intent msgIntent = new Intent(Intent.ACTION_SENDTO, msgUri);
                startActivity(msgIntent);
                finish();
            }
        });
        
        long contactId = CommonMethod.getContactId(mContext, null, address);
        
        if (contactId == 0) {
            mPhotoLoader.loadSpecialPhoto(mUserIcon, address);
        }else {
            int photoId = 0;
            if (mPhotoList.get(address) != null) {
                photoId = Integer.parseInt(mPhotoList.get(address));
            }
            mPhotoLoader.loadPhoto(mUserIcon, photoId);            
        }

        mCancelIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setSmsState();
                updataMessageReadState();
                
                if (mFlipper.getChildCount() > 1) {
                    mFlipper.removeViewAt(mFlipper.getDisplayedChild());
                    setSmsCount();
                } else {
                    finish();
                }
            }
        });
        return layout;
    }
    
    public static void colseMessagePOP(){
        if (mDestktopMessageActivity != null) {
            mDestktopMessageActivity.finish();
        }
    } 
    
    public void updataMessageReadState() {
        try {
        	if (mFlipper == null) {
            	mFlipper = (MsgViewFlipper) findViewById(R.id.sms_list);				
			}
        	
	        String sms_id = (String) mFlipper.getChildAt(mFlipper.getDisplayedChild()).getTag();
	
	        ContentValues values = new ContentValues();
	        values.put("read", "1");
	        String selection = "_id ='" + sms_id + "'";
            getContentResolver().update(Sms.Inbox.CONTENT_URI, values, selection, null);
            
            getContentResolver().update(MessageUtils.PduYlColumns.IMS_URI, values, selection, null);            
            
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onResume() {
        
        if (null != mPhotoLoader) {
            mPhotoLoader.resume();                    
        }
        super.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 == null || e2 == null) {
            return false;
        }
        
        if (mFlipper.getChildCount() == 1)
        {
        	return false;
        }
        
        if ((e1.getX() - e2.getX()) > 100) {
            mFlipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.msg_push_left_in));
            mFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.msg_push_left_out));
            mFlipper.showNext();
            setSmsCount();
            setSmsState();
            updataMessageReadState();      
            updateButtonState();
            return true;
        } else if ((e1.getX() - e2.getX()) < -100) {
            mFlipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.msg_push_right_in));
            mFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.msg_push_right_out));
            mFlipper.showPrevious();
            setSmsCount();
            setSmsState();
            updataMessageReadState();
            updateButtonState();
            return true;
        }
        return false;
    }

    private void AddSetsmscount() {
        mFlipper.invalidate();
        mSmsCount.setText((mFlipper.getChildCount()) + "/" + (mFlipper.getChildCount()));
    }

    private void setSmsCount() {
        mFlipper.invalidate();
        mSmsCount.setText((mFlipper.getDisplayedChild() + 1) + "/"+ (mFlipper.getChildCount()));
    }
    
    private void setSmsState(){
    	
        try {
        	if (mFlipper == null) {
            	mFlipper = (MsgViewFlipper) findViewById(R.id.sms_list);				
			}
        	
	        String sms_id = (String) mFlipper.getChildAt(mFlipper.getDisplayedChild()).getTag();
	        ContentValues values = new ContentValues(1);
	        values.put("seen", 1);

            getContentResolver().update(Sms.Inbox.CONTENT_URI,
                    values,
                    "seen=0 and _id ='" + sms_id + "'",
                    null);
        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
    
    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    protected void onDestroy() {
        mDestktopMessageActivity = null;
        
        if (null != mPhotoLoader) {
            mPhotoLoader.stop();            
        }
        
        if (myReceiver != null) {
            unregisterReceiver(myReceiver);			
		}
        
        if (mResponseReceiver != null) {
			unregisterReceiver(mResponseReceiver);
		}
        
        mIsMessagePOPStart = false;
        Conversation.markAllConversationsAsSeen(getApplicationContext()); 
        
        super.onDestroy();
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.user_name:
        case R.id.call_icon: {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                View currentview = this.getCurrentFocus();
                if (currentview != null) {
                    imm.hideSoftInputFromWindow(currentview.getWindowToken(), 0);
                }
            }
            setSmsState();
            updataMessageReadState();
            //Signread();
            //MessagingNotification.cancelNotification(getApplicationContext(), 123);
            int index = mFlipper.getDisplayedChild();
            View view = mFlipper.getChildAt(index);
            mUserName = (TextView) view.findViewById(R.id.user_name);
            String mMobileNumber = mUserName.getTag().toString();
            Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mMobileNumber));
            startActivity(in);
            if (mFlipper.getChildCount() > 1) {
                mFlipper.removeViewAt(index);
                setSmsCount();
                mReplyText.setText("");
            } else {
                finish();
            }
            break;
        }

        case R.id.delmsg_icon: {            
            new AlertDialog.Builder(mContext).setTitle(R.string.alter_title)
                .setMessage(R.string.alter_message).setPositiveButton(                        
                            R.string.alter_positive,                            
                            new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,int which) {
                                    
                                    String sms_id = (String) mFlipper.getChildAt(mFlipper.getDisplayedChild()).getTag();                                    
                                    Uri uri = (Uri)mFlipper.getChildAt(mFlipper.getDisplayedChild()).findViewById(R.id.user_icon).getTag();
                                    
                                    int count = 0;
                                    if (uri.toString().equals("content://sms/inbox")) {
                                        count = getContentResolver().delete(Uri.parse("content://sms/"+ sms_id), null, null);
                                        mSmsIdMap.remove(sms_id);
                                    }else if (uri.toString().equals("content://mms-sms/pdu_yl")) {
                                    	int mms_id = Integer.parseInt(sms_id);
                                    	Uri deleteUri = ContentUris.withAppendedId(MessageUtils.PduYlColumns.IMS_URI, mms_id);
                                        count = getContentResolver().delete(deleteUri, null, null);                                        
                                    }
                                    else {
                                        int mms_id = Integer.parseInt(sms_id);
                                        Uri deleteUri = ContentUris.withAppendedId(Mms.CONTENT_URI, mms_id);
                                        count = getContentResolver().delete(deleteUri, null, null);                                                                                
                                    }
                                    
                                    if (count == 1) {
                                        Toast.makeText(mContext,R.string.alter_successed_toast,Toast.LENGTH_SHORT).show();
                                    }
                                    
                                    if (mFlipper.getChildCount() > 1) {
                                        mFlipper.removeViewAt(mFlipper.getDisplayedChild());
                                        setSmsCount();
                                        updateButtonState();
                                    } else {
                                        finish();
                                    }
                                }
                            }).setNegativeButton(
                            R.string.alter_negtive,
                            new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,int which) {
                                    dialog.dismiss();
                                }
                            }).show();
            break;
        }

        case R.id.reply_btn: {
            //contentResolver = getContentResolver();
            String mReplyContent = mReplyText.getText().toString();
            if (mReplyContent.length() != 0) {
                int index = mFlipper.getDisplayedChild();
                View view = mFlipper.getChildAt(index);
                mUserName = (TextView) view.findViewById(R.id.user_name);
                String mMobileReceiver = mUserName.getTag().toString();
                ImageView imsimagview = (ImageView) view.findViewById(R.id.imessage_status);
                String type = imsimagview.getTag().toString();
                
				String toshort = null;
                                
                if (!TextUtils.isEmpty(type) && type.equals(IMS_ONLINE)) {
                	sendIMMessageWorker(mReplyContent, mMobileReceiver, IMService.GeXinIM);
                	toshort = getResources().getString(R.string.message_ims_sending_string);
				}else {               
					toshort = getResources().getString(R.string.message_sending_string);
//	                SmsManager smsmanager = SmsManager.getDefault();
//	                List<String> text = smsmanager.divideMessage(mReplyContent);
//	                for (String i : text) {
//	                    smsmanager.sendTextMessage(mMobileReceiver, null, i, null,null);
//	                }
//	                ContentValues values = new ContentValues();
//	                values.put("date", System.currentTimeMillis());
//	                values.put("read", 0);
//	                values.put("type", 2);
//	                values.put("address", mMobileReceiver);
//	                values.put("body", mReplyContent);
//	                getContentResolver().insert(Uri.parse("content://sms"), values);
	                
	    			Sms.addMessageToUri(getContentResolver(),
	    					Uri.parse("content://sms/queued"), mMobileReceiver, mReplyContent,
	    					null, null, true /* read */, mRequestDeliveryReport, -1L);
	                
	                mContext.sendBroadcast(new Intent(SmsReceiverService.ACTION_SEND_MESSAGE,
	    	                null,
	    	                mContext,
	    	                SmsReceiver.class));
                }
                
                setSmsState();
                updataMessageReadState();
                
                InputMethodManager inputMethodManager =
                    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager.isActive()) {
                    inputMethodManager.hideSoftInputFromWindow(mReplyText.getWindowToken(), 0);			
        		}
                    
				Toast.makeText(this, toshort, Toast.LENGTH_SHORT).show(); 
                if (mFlipper.getChildCount() > 1) {
                    mFlipper.removeViewAt(index);
                    setSmsCount();
                    mReplyText.setText("");
                    updateButtonState();
                } else {
                    finish();
                }
                break;
            } else {
                Toast.makeText(mContext, R.string.replycontent,Toast.LENGTH_SHORT).show();
            }
        }

        default:
            break;
        }
    }

    private final class ThreadPopMessageQueryHandler extends AsyncQueryHandler {
        public ThreadPopMessageQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            HashMap<String, String> map = new HashMap<String, String>();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String smsaddress;
            String smsContentbody = null;
            
            switch (token) {
            case POP_SMS_QUERY_TOKEN:
            {                
                if (cursor != null) {
                  if ((cursor != null) && (cursor.moveToFirst())) {
                     
                     //usericonflag = false;
                      map.put("sms_id", String.valueOf(cursor.getInt(0)));
                      map.put("msg_uri", "content://sms/inbox");

                      String tempSmsId = String.valueOf(cursor.getInt(0));
                      if (mSmsIdMap.get(tempSmsId) != null) {
						return;
                      }
                      mSmsIdMap.put(tempSmsId, tempSmsId);                      
                      
                      smsaddress = cursor.getString(2).toString();
                      smsaddress = MessageUtils.fixPhoneNumber(smsaddress);
                      map.put("sms_address", smsaddress);
                      
                      
                      long contactId = CommonMethod.getContactId(mContext, null, smsaddress);
                      String contactName = "";
                      
                      if (contactId == 0) {
                          String sLocation = CommonMethod.getSpecialPhone(smsaddress);
                          if (!TextUtils.isEmpty(sLocation)) {
                              contactName = sLocation;
                          }
                          
                          if (smsaddress.equals(CallerInfo.UNKNOWN_NUMBER)) {
                              contactName = getResources().getString(R.string.unknown);
                          }
                          else if (smsaddress.equals(CallerInfo.PRIVATE_NUMBER)) {
                              contactName = getResources().getString(R.string.private_num);
                          }
                          else if (smsaddress.equals(CallerInfo.PAYPHONE_NUMBER)) {
                              contactName = getResources().getString(R.string.payphone);
                          }
                          
                      }else {
                          contactName = CommonMethod.getContactName(mContext, null, smsaddress);                        
                      }
                      map.put("sms_name", contactName);
                      
                      smsContentbody = cursor.getString(4);
                      if (!TextUtils.isEmpty(smsContentbody)) {
						smsContentbody = smsContentbody.trim().replaceAll("\r\n", "\n");
                      }else {
						smsContentbody = "";
                      }                      
                      map.put("sms_body", smsContentbody);
                      map.put("sms_date",sdf.format(new Date(cursor.getLong(5))));                      
                  } 
                }                
            }                
                break;
            case POP_IMS__QUERY_TOKEN:{                
                if (cursor != null) {
                  if ((cursor != null) && (cursor.moveToFirst())) {
                     
                     //usericonflag = false;
                      map.put("sms_id", String.valueOf(cursor.getInt(0)));
                      map.put("msg_uri", "content://mms-sms/pdu_yl");
  
                      smsaddress = cursor.getString(2).toString();
  
                      map.put("sms_address", smsaddress);
                      
                      long contactId = CommonMethod.getContactId(mContext, null, smsaddress);
                      String contactName = "";
                      
                      if (contactId == 0) {
                          String sLocation = CommonMethod.getSpecialPhone(smsaddress);
                          if (!TextUtils.isEmpty(sLocation)) {
                              contactName = sLocation;
                          }
                          
                          if (smsaddress.equals(CallerInfo.UNKNOWN_NUMBER)) {
                              contactName = getResources().getString(R.string.unknown);
                          }
                          else if (smsaddress.equals(CallerInfo.PRIVATE_NUMBER)) {
                              contactName = getResources().getString(R.string.private_num);
                          }
                          else if (smsaddress.equals(CallerInfo.PAYPHONE_NUMBER)) {
                              contactName = getResources().getString(R.string.payphone);
                          }
                          
                      }else {
                          contactName = CommonMethod.getContactName(mContext, null, smsaddress);                        
                      }
                      map.put("sms_name", contactName);
                      
                      smsContentbody = cursor.getString(4);
                      if (!TextUtils.isEmpty(smsContentbody)) {
						smsContentbody = smsContentbody.trim().replaceAll("\r\n", "\n");
                      }else {
						smsContentbody = "";
                      }    
                      map.put("sms_body", smsContentbody);
                      map.put("sms_date",sdf.format(new Date(cursor.getLong(5))));                      
                  } 
                }                
            }                
                break;
                
            case POP_MMS__QUERY_TOKEN:{                 
                if ((cursor != null) && (cursor.moveToFirst())) {
                    int id = cursor.getInt(cursor.getColumnIndex("_id"));
                    String subject = cursor.getString(cursor.getColumnIndex("sub"));

                    Date mmsDate = new Date(cursor.getLong(cursor.getColumnIndex("date")) * 1000);
                    String mmsDateShow = sdf.format(mmsDate);
                    map.put("mms_id", String.valueOf(id));
                    map.put("msg_uri", "content://mms/inbox");
                    
                    try {
                        if (subject == null) {
                            subject = "";
                        }
                        String transferSubStr = new String(subject.getBytes("ISO8859_1"), "utf-8");
                        map.put("mms_subject", transferSubStr);
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }

                    map.put("mms_date", mmsDateShow);
                    Cursor cursorAdd = null;
                    String selectionAddCondition = new String("msg_id=" + id);
                    Uri uriAddr = Uri.parse("content://mms/" + id + "/addr");
                    
                    try {
                        cursorAdd = getContentResolver().query(uriAddr, null,selectionAddCondition, null, null);
                        
                        if ((cursorAdd != null && cursorAdd.moveToFirst())) {
                            smsaddress = cursorAdd.getString(cursorAdd.getColumnIndex("address"));
                            map.put("mms_address", smsaddress);
                            Log.v("DestktopMessageActivity", "smsaddress = "+ smsaddress);
                            
                            long contactId = CommonMethod.getContactId(mContext, null, smsaddress);
                            String contactName = "";
                            
                            if (contactId == 0) {
                                String sLocation = CommonMethod.getSpecialPhone(smsaddress);
                                if (!TextUtils.isEmpty(sLocation)) {
                                    contactName = sLocation;
                                }
                                
                                if (smsaddress.equals(CallerInfo.UNKNOWN_NUMBER)) {
                                    contactName = getResources().getString(R.string.unknown);
                                }
                                else if (smsaddress.equals(CallerInfo.PRIVATE_NUMBER)) {
                                    contactName = getResources().getString(R.string.private_num);
                                }
                                else if (smsaddress.equals(CallerInfo.PAYPHONE_NUMBER)) {
                                    contactName = getResources().getString(R.string.payphone);
                                }
                                
                            }else {
                                contactName = CommonMethod.getContactName(mContext, null, smsaddress);                        
                            }
                            map.put("mms_name", contactName);
                            cursorAdd.close();
                        }
                        
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    finally{                        
                        if (cursorAdd != null) {
                            cursor.close();                                                                  
                        }
                    }
                }
            }
                break;
                
            default:
                break;
            }
            
            setMessageInfo(map);
        }
    }
    
    private void startQury(Uri uri) {   
    	String[] projection = new String[] { "_id", "person", "address","read", "body", "date" };
        if (uri.equals(Uri.parse("content://sms/inbox"))) {
            
            
            mQueryHandler.startQuery(POP_SMS_QUERY_TOKEN,
                                    null, 
                                    uri, 
                                    projection, 
                                    "read = 0 or seen = 0", 
                                    null, 
                                    "date desc");

        } else if (uri.equals(Uri.parse("content://mms/inbox"))) {
            
            mQueryHandler.startQuery(POP_MMS__QUERY_TOKEN,
                                    null, 
                                    uri, 
                                    null, 
                                    "read = 0", 
                                    null, 
                                    "date desc");                        
        }else if (uri.equals(Uri.parse("content://mms-sms/pdu_yl"))) {
        
        	mQueryHandler.startQuery(POP_IMS__QUERY_TOKEN,
                                null, 
                                uri, 
                                projection, 
                                "read = 0 or seen = 0", 
                                null, 
                                "date desc");                        
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updataMessageReadState();
        setSmsCount();
    }
    
    private void sendIMMessageWorker(String msgText, String semiSepRecipients, int IMType) {
        IMMessage senderMsg = new IMMessage(IMType);
        senderMsg.setSendAddress(semiSepRecipients);
        senderMsg.setText(msgText);
        senderMsg.setThreadId(null);
        try {
            IMClient.sendMessage(mContext,senderMsg);
            // Make sure this thread isn't over the limits in message count
            //Recycler.getSmsRecycler().deleteOldMessagesByThreadId(mActivity, threadId);
        } catch (Exception e) {
            Log.e("DestktopMessageActivity", "Failed to send SMS message");
        }
    }
    
    private void updateButtonState(){
    	String string = mReplyText.getText().toString();
    	
    	if (TextUtils.isEmpty(string)) {
    		mReplyIcon.setBackgroundResource(R.drawable.bg_msg_normal_send);
    		mReplyIcon.setEnabled(false);
		}else {
            View view = mFlipper.getChildAt(mFlipper.getDisplayedChild());
			ImageView imageView = (ImageView)view.findViewById(R.id.imessage_status);
			String online = imageView.getTag().toString();
            if (!TextUtils.isEmpty(online) && online.equals(IMS_ONLINE)) {
            	mReplyIcon.setBackgroundResource(R.drawable.compose_send_free_selector);				
			}else {
            	mReplyIcon.setBackgroundResource(R.drawable.compose_hei_send_selector);
			}
            mReplyIcon.setEnabled(true);
		}
    }
    
    public void showTextCount(CharSequence s){
        String str = s.toString();
        String sTextCount = "";
        int iCount = 0;
        int iNumberNow = 0;                        
        int textCount = str.length();
                
        if (mReplyText.getLineCount() < 2) {
        	mContentIndicator.setText("");
        	mContentIndicator.setVisibility(View.GONE);                
            return;
        }
        
        if (isCHchar(str) == false) {               
            int firstCount = textCount / 160;
            int second = textCount - 160;
            
            //0 -- 160 message count == 1
            if (firstCount < 1 ) {
                iCount = 1;
                iNumberNow = 160 - (textCount % 160);
            }
            //161 -- 161 + 146 message count == 2
            else if ((second < 146 && second >= 0) && firstCount >= 1) {
                iCount = 2;
                iNumberNow = 146 - ((textCount - 160) % 146);
            }
            //161 + 146 -- 161 + 146 + 153 message count > 2
            else {
                iCount = ((textCount - 160 - 146) / 153);                
                int tCount = iCount * 153;                
                iNumberNow = 153 - ((textCount - 160 - 146 - tCount) % 153);
                iCount += 3;
            }
            
        }else{
            int firstCount = textCount / 70;
            int second = textCount - 70;
            
            //0 -- 70 message count == 1
            if (firstCount < 1 ) {
                iCount = 1;
                iNumberNow = 70 - (textCount % 70);
            }
            //70 -- 70 + 64 message count == 2
            else if ((second < 64 && second >= 0) && firstCount >= 1) {
                iCount = 2;
                iNumberNow = 64 - ((textCount - 70) % 64);
            }
            //70 + 64 -- 70 + 64 + 67 message count > 2
            else {
                iCount = ((textCount - 70 - 64) / 67);                
                int tCount = iCount * 67;                
                iNumberNow = 67 - ((textCount - 70 - 64 - tCount) % 67);
                iCount += 3;
            }                                
        }
        //sTextCount = "(" + iNumberNow + "/" + iContent + ")" + iCount;
        sTextCount = "" + iNumberNow + "/" + iCount;
                    
        mContentIndicator.setText(sTextCount);
        mContentIndicator.setVisibility(View.VISIBLE);  
    }
    
    private boolean isCHchar(String str){
        boolean isCh = false;
        int icount = str.length();
        
        for (int i = 0; i < icount; i++) {
             String s = str.substring(i, i+1);
             byte ch[] = s.getBytes();
             
             if (ch.length > 1) {
                isCh = true;
                break;
            }
        }                        
        return isCh;
    }
}