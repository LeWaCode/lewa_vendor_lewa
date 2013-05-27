/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lewa.PIM.mms.ui;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Browser;
import android.preference.PreferenceManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineHeightSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lewa.PIM.R;
import com.lewa.PIM.mms.data.Contact;
import com.lewa.PIM.mms.data.ContactList;
import com.lewa.PIM.mms.data.WorkingMessage;
import com.lewa.PIM.mms.transaction.SmsMessageSender;
import com.lewa.PIM.mms.transaction.Transaction;
import com.lewa.PIM.mms.transaction.TransactionBundle;
import com.lewa.PIM.mms.transaction.TransactionService;
import com.lewa.PIM.mms.util.DownloadManager;
import com.lewa.PIM.mms.util.SmileyParser;
import com.google.android.mms.ContentType;
import com.google.android.mms.pdu.PduHeaders;
import com.lewa.PIM.mms.MmsConfig;
import android.content.SharedPreferences;


/**
 * This class provides view of a message in the messages list.
 */
public class MessageListItem extends LinearLayout implements
        SlideViewInterface, OnClickListener {
    public static final String     EXTRA_URLS        = "com.lewa.PIM.mms.ExtraUrls";

    private static final String    TAG               = "MessageListItem";
    static final int               MSG_LIST_EDIT_MMS = 1;
    static final int               MSG_LIST_EDIT_SMS = 2;
    
    private final int ADDRESS_NAME_MAX_LENG = 20;

    // private View mMsgListItem;
    private LinearLayout           mMsgLinearLayoutBody;
    private LinearLayout           mMsgItemLayout;
    private View                   mMmsView;
    private ImageView              mImageView;
    private ImageView              mLockedIndicator;
    private ImageView              mDeliveredIndicator;
    private ImageView              mDetailsIndicator;
    //private ViewStub               mMmsLayoutViewStub;
    private ViewStub               mMmsDownloadingViewStub;
    private ImageButton            mSlideShowButton;
    private TextView               mBodyTextView;
    private TextView               mMsgTimeTextView;
    private Button                 mDownloadButton;
    private TextView               mDownloadingLabel;
    private TextView               mMsgTimeText;
    private TextView               mRecorderText;
    private ImageView              mRecorderPlayIcon;
    private LinearLayout           mRecorderBody;
    // private QuickContactBadge mAvatar;
    private Handler                mHandler;
    private MessageItem            mMessageItem;
    private boolean                mBlackBackground;
    private ContactList            mContacts = null;
    private Recorder 			   mRecorder;    
    private RelativeLayout         mYlView;
    private ImageView              mYlImageView;
    private ImageView              mYlSlideShowButton;
    
    public void setRecorder(Recorder recorder){
    	mRecorder = recorder;
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    private void getLayoutItem(MessageItem msgItem) {
        Log.v(TAG,
                "onFinishInflate mMessageItem.getBoxId()="
                        + mMessageItem.getBoxId());

        if (Mms.MESSAGE_BOX_INBOX == msgItem.getBoxId() || Sms.MESSAGE_TYPE_ALL == msgItem.getBoxId()) {
            mMsgItemLayout = (LinearLayout) findViewById(R.id.right_item);
            mMsgItemLayout.setVisibility(View.GONE);
            mMsgItemLayout = (LinearLayout) findViewById(R.id.left_item);
            mMsgItemLayout.setVisibility(View.VISIBLE);
            mMsgLinearLayoutBody = (LinearLayout) findViewById(R.id.left_body);
            mBodyTextView = (TextView) findViewById(R.id.left_text_view);
            mMsgTimeTextView = (TextView) findViewById(R.id.left_msg_time);
            mLockedIndicator = (ImageView) findViewById(R.id.left_locked_indicator);
            mDeliveredIndicator = (ImageView) findViewById(R.id.left_delivered_indicator);
            mDetailsIndicator = (ImageView) findViewById(R.id.left_details_indicator);
            //mMmsLayoutViewStub = (ViewStub) findViewById(R.id.left_mms_layout_view_stub);
            mMmsDownloadingViewStub = (ViewStub) findViewById(R.id.left_mms_downloading_view_stub);
            mMmsView = findViewById(R.id.left_mms_view);
            mImageView = (ImageView) findViewById(R.id.left_image_view);
            mSlideShowButton = (ImageButton) findViewById(R.id.left_play_slideshow_button);
            //YL 
            mMsgTimeText = (TextView) findViewById(R.id.left_msg_time_text);                 
            mRecorderText = (TextView)findViewById(R.id.left_recorder_text);            
            mRecorderPlayIcon = (ImageView) findViewById(R.id.left_recorder_play_icon);
            mRecorderBody = (LinearLayout) findViewById(R.id.left_recorder_body);
            mYlView = (RelativeLayout) findViewById(R.id.left_ims_view);
            mYlImageView = (ImageView) findViewById(R.id.left_ims_image_view);
            mYlSlideShowButton = (ImageView) findViewById(R.id.left_ims_play_slideshow_button);
            mMsgTimeText.setVisibility(View.GONE);
             
            if (msgItem.mType.equals("ims")) {            	            	
            	//mMsgTimeText.setVisibility(View.VISIBLE);            	
            	if (ContentType.isAudioType(msgItem.mDataType)) {
            		mRecorderBody.setVisibility(View.VISIBLE);
            		mBodyTextView.setVisibility(View.GONE);
            		mYlView.setVisibility(View.GONE);
            		mYlSlideShowButton.setVisibility(View.GONE);
            		
				}else if (ContentType.isImageType(msgItem.mDataType)) {
					mRecorderBody.setVisibility(View.GONE);	
					mBodyTextView.setVisibility(View.GONE);
					mYlView.setVisibility(View.VISIBLE);
            		mYlSlideShowButton.setVisibility(View.GONE);
					
				}else if ("video/3gp".equals(msgItem.mDataType) ||
						ContentType.isVideoType(msgItem.mDataType)) {
					mRecorderBody.setVisibility(View.GONE);	
					mBodyTextView.setVisibility(View.GONE);
					mYlView.setVisibility(View.VISIBLE);
            		mYlSlideShowButton.setVisibility(View.VISIBLE);
					
				}else {
					mRecorderBody.setVisibility(View.GONE);
            		mBodyTextView.setVisibility(View.VISIBLE);
            		mYlView.setVisibility(View.GONE);
            		mYlSlideShowButton.setVisibility(View.GONE);
				}
            	
			}else {
            	//mMsgTimeText.setVisibility(View.GONE);
				mRecorderBody.setVisibility(View.GONE);
        		mBodyTextView.setVisibility(View.VISIBLE);
        		mYlView.setVisibility(View.GONE);
        		mYlSlideShowButton.setVisibility(View.GONE);
        	}
            
        } else {
            mMsgItemLayout = (LinearLayout) findViewById(R.id.left_item);
            mMsgItemLayout.setVisibility(View.GONE);
            mMsgItemLayout = (LinearLayout) findViewById(R.id.right_item);
            mMsgItemLayout.setVisibility(View.VISIBLE);
            mMsgLinearLayoutBody = (LinearLayout) findViewById(R.id.right_body);
            mBodyTextView = (TextView) findViewById(R.id.right_text_view);
            mMsgTimeTextView = (TextView) findViewById(R.id.right_msg_time);
            mLockedIndicator = (ImageView) findViewById(R.id.right_locked_indicator);
            mDeliveredIndicator = (ImageView) findViewById(R.id.right_delivered_indicator);
            mDetailsIndicator = (ImageView) findViewById(R.id.right_details_indicator);
            //mMmsLayoutViewStub = (ViewStub) findViewById(R.id.right_mms_layout_view_stub);
            mMmsDownloadingViewStub = (ViewStub) findViewById(R.id.right_mms_downloading_view_stub);
            mMmsView = findViewById(R.id.right_mms_view);
            mImageView = (ImageView) findViewById(R.id.right_image_view);
            mSlideShowButton = (ImageButton) findViewById(R.id.right_play_slideshow_button);
            //YL
            mMsgTimeText = (TextView) findViewById(R.id.right_msg_time_text);            
            mRecorderText = (TextView)findViewById(R.id.right_recorder_text);
            mRecorderPlayIcon = (ImageView) findViewById(R.id.right_recorder_play_icon);
            mRecorderBody = (LinearLayout) findViewById(R.id.right_recorder_body);
            
            mYlView = (RelativeLayout) findViewById(R.id.right_ims_view);
            mYlImageView = (ImageView) findViewById(R.id.right_ims_image_view);
            mYlSlideShowButton = (ImageView) findViewById(R.id.right_ims_play_slideshow_button);
            mMsgTimeText.setVisibility(View.GONE);
            
            if (msgItem.mType.equals("ims")) {

            	//mMsgTimeText.setVisibility(View.VISIBLE);
            	mYlSlideShowButton.setVisibility(View.GONE);
            	            	
            	if (ContentType.isAudioType(msgItem.mDataType)) {
            		mRecorderBody.setVisibility(View.VISIBLE);
            		mBodyTextView.setVisibility(View.GONE);
            		mYlView.setVisibility(View.GONE);
            		
				}else if (ContentType.isImageType(msgItem.mDataType)) {
					mRecorderBody.setVisibility(View.GONE);	
					mBodyTextView.setVisibility(View.GONE);
					mYlView.setVisibility(View.VISIBLE);
					
				}else if ("video/3gp".equals(msgItem.mDataType) ||
						ContentType.isVideoType(msgItem.mDataType)) {
					mRecorderBody.setVisibility(View.GONE);	
					mBodyTextView.setVisibility(View.GONE);
					mYlView.setVisibility(View.VISIBLE);
	            	mYlSlideShowButton.setVisibility(View.VISIBLE);
				}
            	else {
					mRecorderBody.setVisibility(View.GONE);
	        		mBodyTextView.setVisibility(View.VISIBLE);
	        		mYlView.setVisibility(View.GONE);
				}
            	            	
			}else {
            	//mMsgTimeText.setVisibility(View.GONE);				
				mRecorderBody.setVisibility(View.GONE);
        		mBodyTextView.setVisibility(View.VISIBLE);
        		mYlView.setVisibility(View.GONE);
        		mYlSlideShowButton.setVisibility(View.GONE);
			}
        }

        mBodyTextView.setTextSize(Integer.parseInt(PreferenceManager
                .getDefaultSharedPreferences(getContext()).getString(
                        MessagingPreferenceActivity.MESSAGE_FONT_SIZE, "18")));
        // mAvatar = (QuickContactBadge) findViewById(R.id.avatar);
        // mTimeLayout = (LinearLayout) findViewById(R.id.time_botton);
        // ViewGroup.MarginLayoutParams badgeParams =
        // (MarginLayoutParams)mAvatar.getLayoutParams();
        final int badgeWidth = 0;// badgeParams.width + badgeParams.rightMargin
                                 // + badgeParams.leftMargin;

        int lineHeight = mBodyTextView.getLineHeight();
        int effectiveBadgeHeight = 0;// badgeParams.height +
                                     // badgeParams.topMargin -
                                     // mBodyTextView.getPaddingTop();
        final int indentLineCount = (int) ((effectiveBadgeHeight - 1) / lineHeight) + 1;

        mLeadingMarginSpan = new LeadingMarginSpan.LeadingMarginSpan2() {
            public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                    int top, int baseline, int bottom, CharSequence text,
                    int start, int end, boolean first, Layout layout) {
                // no op
            }

            public int getLeadingMargin(boolean first) {
                return first ? badgeWidth : 0;
            }

            public int getLeadingMarginLineCount() {
                return indentLineCount;
            }
        };

    }

    public View getMessageListItemTextView()
    {
        return mBodyTextView;
    }
    
    public void SetContactList(ContactList contacts){
        mContacts = contacts;
    }
    
    public void bind(MessageListAdapter.AvatarCache avatarCache,
            MessageItem msgItem, Boolean blackBackground) {
        mMessageItem = msgItem;
        getLayoutItem(msgItem);
        mBlackBackground = blackBackground;

        setLongClickable(false);

        switch (msgItem.mMessageType) {
        case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
            bindNotifInd(msgItem);
            break;
        default:
            bindCommonMessage(avatarCache, msgItem);
            break;
        }
    }

    public MessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        int color = mContext.getResources().getColor(R.color.timestamp_color);
        mColorSpan = new ForegroundColorSpan(color);
    }

    public MessageItem getMessageItem() {
        return mMessageItem;
    }

    public void setMsgListItemHandler(Handler handler) {
        mHandler = handler;
    }
    
    private String getContectName(String number){
        String name = "";
        String tempNumber = MessageUtils.fixPhoneNumber(number);
        int icount = mContacts.size();
        
        for (int i = 0; i < icount; i++) {
            Contact contact = mContacts.get(i);
            String number1 = MessageUtils.fixPhoneNumber(contact.getNumber());
                        
            if (tempNumber.equals(number1)) {
                name = contact.getName() + "  ";
                break;
            }
        }
        return name;
    }
    
    private String getTimeText(MessageItem msgItem){
        String s = "";
        
        if (mContacts != null && mContacts.size() > 1) {
             s = getContext().getString(R.string.message_send_to);
             
             String nameString = getContectName(msgItem.mAddress); 
             if (!TextUtils.isEmpty(nameString) && nameString.getBytes().length > ADDRESS_NAME_MAX_LENG) {
            	 
            	 if (MessageUtils.isCHchar(nameString)) {
	            	 nameString = nameString.substring(0, 10) + "...";										
				}else {
	            	 nameString = nameString.substring(0, 19) + "...";					
				}
            	 s += nameString;
			}else {
	             s += getContectName(msgItem.mAddress);				
			}
        }
        s += msgItem.mTimestamp;
        return s;
    }

    private String getTimeText(MessageItem msgItem, String str){
        String s = "";

        if (mContacts != null && mContacts.size() > 1) {
            s = getContext().getString(R.string.message_send_to);
            
            String nameString = getContectName(msgItem.mAddress); 
            if (!TextUtils.isEmpty(nameString) && nameString.getBytes().length > ADDRESS_NAME_MAX_LENG) {            	
	           	 if (MessageUtils.isCHchar(nameString)) {
	            	 nameString = nameString.substring(0, 10) + "...";										
				}else {
	            	 nameString = nameString.substring(0, 19) + "...";					
				}
            	s += nameString;
			}else {
	            s += getContectName(msgItem.mAddress);				
			}     
        }
        s += str;
        return s;
    }    
    
    private void bindNotifInd(final MessageItem msgItem) {
        hideMmsViewIfNeeded();

        String msgSizeText = mContext.getString(R.string.message_size_label)
                + String.valueOf((msgItem.mMessageSize + 1023) / 1024)
                + mContext.getString(R.string.kilobyte);

        mBodyTextView.setText(formatMessage(msgItem, msgItem.mContact, null,
                msgItem.mSubject, msgSizeText + "\n" + msgItem.mTimestamp,
                msgItem.mHighlight, msgItem.mTextContentType));
        MsgLinkify.addLinks(mBodyTextView, 0x000f);
        mMsgTimeTextView.setText(getTimeText(msgItem));

        int state = DownloadManager.getInstance().getState(msgItem.mMessageUri);
        switch (state) {
        case DownloadManager.STATE_DOWNLOADING:
            inflateDownloadControls();
            mDownloadingLabel.setVisibility(View.VISIBLE);
            mDownloadButton.setVisibility(View.GONE);
            break;
        case DownloadManager.STATE_UNSTARTED:
        case DownloadManager.STATE_TRANSIENT_FAILURE:
        case DownloadManager.STATE_PERMANENT_FAILURE:
        default:
            setLongClickable(true);
            inflateDownloadControls();
            mDownloadingLabel.setVisibility(View.GONE);
            mDownloadButton.setVisibility(View.VISIBLE);
            mDownloadButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    mDownloadingLabel.setVisibility(View.VISIBLE);
                    mDownloadButton.setVisibility(View.GONE);
                    Intent intent = new Intent(mContext,
                            TransactionService.class);
                    intent.putExtra(TransactionBundle.URI,
                            msgItem.mMessageUri.toString());
                    intent.putExtra(TransactionBundle.TRANSACTION_TYPE,
                            Transaction.RETRIEVE_TRANSACTION);
                    mContext.startService(intent);
                }
            });
            break;
        }

        // Hide the indicators.
        mLockedIndicator.setVisibility(View.GONE);
        mDeliveredIndicator.setVisibility(View.GONE);
        mDetailsIndicator.setVisibility(View.GONE);

        drawLeftStatusIndicator(msgItem);
    }

    private void bindCommonMessage(
            final MessageListAdapter.AvatarCache avatarCache,
            final MessageItem msgItem) {
        if (mDownloadButton != null) {
            mDownloadButton.setVisibility(View.GONE);
            mDownloadingLabel.setVisibility(View.GONE);
        }
        // Since the message text should be concatenated with the sender's
        // address(or name), I have to display it here instead of
        // displaying it by the Presenter.
        mBodyTextView.setTransformationMethod(HideReturnsTransformationMethod
                .getInstance());
        
        if (msgItem.mBoxId == Sms.MESSAGE_TYPE_INBOX && msgItem.mRead == 0) {
	        ContentValues values = new ContentValues();
	        values.put("read", "1");
	        String selection = "_id ='" + msgItem.mMsgId + "'";
            
        	if (msgItem.isIms()) {
				mContext.getContentResolver().update(MessageUtils.PduYlColumns.IMS_URI, values, selection, null);     								
			}else if (msgItem.isSms()) {
                mContext.getContentResolver().update(Sms.Inbox.CONTENT_URI, values, selection, null);
			}
		}
        // Get and/or lazily set the formatted message from/on the
        // MessageItem. Because the MessageItem instances come from a
        // cache (currently of size ~50), the hit rate on avoiding the
        // expensive formatMessage() call is very high.
        CharSequence formattedMessage = msgItem.getCachedFormattedMessage();
        if (formattedMessage == null) {
            formattedMessage = formatMessage(msgItem, msgItem.mContact,
                    msgItem.mBody, msgItem.mSubject, msgItem.mTimestamp,
                    msgItem.mHighlight, msgItem.mTextContentType);
        }
        mBodyTextView.setText(formattedMessage);
        MsgLinkify.addLinks(mBodyTextView, 0x000f);
        
        mBodyTextView.setTag(msgItem);
        mBodyTextView.setOnClickListener(mTextBodyClickListener);
        mBodyTextView.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                return v.showContextMenu();
            }
        });
        
        mMsgTimeTextView.setText(getTimeText(msgItem));
        
        if (msgItem.isSms()) {
            hideMmsViewIfNeeded();
			mMsgLinearLayoutBody.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
				}
			});		

    		mMsgLinearLayoutBody.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    return v.showContextMenu();
                }
            });
        }else if (msgItem.isIms()){
        	
        	if (ContentType.isAudioType(msgItem.mDataType)) {
    			Long duration = msgItem.mPlayTime;			
    			if (duration != null) {
    				Date date = new Date(duration);
    				String s = String.format("%02d : %02d", date.getMinutes(), date.getSeconds());
    				mRecorderText.setText(s);
    			}				            
			}else if (ContentType.isImageType(msgItem.mDataType)) {	
				setImage(msgItem.mYlBitmap);
			}else if ("video/3gp".equals(msgItem.mDataType) ||
					ContentType.isVideoType(msgItem.mDataType)) {
				setYlVieo(msgItem.mYlBitmap);
			}
	        hideMmsViewIfNeeded();            				        	
	        setImsOnClickListener(msgItem);
        }else {
            Presenter presenter = PresenterFactory
                    .getPresenter("MmsThumbnailPresenter", mContext, this,
                            msgItem.mSlideshow);
            presenter.present();

            if (msgItem.mAttachmentType != WorkingMessage.TEXT) {
                inflateMmsView();
                mMmsView.setVisibility(View.VISIBLE);
                setOnClickListener(msgItem);
                drawPlaybackButton(msgItem);
            } else {
                hideMmsViewIfNeeded();
            }
        }

        drawLeftStatusIndicator(msgItem);
        drawRightStatusIndicator(msgItem);

        requestLayout();

        if (MmsConfig.getEasterEggs() && !TextUtils.isEmpty(msgItem.mBody)) {
            SharedPreferences sp = mContext.getSharedPreferences(MessagingPreferenceActivity.LEWA_EASTER_EGGS_FIRST, Context.MODE_WORLD_READABLE);
            boolean bIsfirstStart = sp.getBoolean(MessagingPreferenceActivity.LEWA_EASTER_EGGS_FIRST, true);
                                    
            if (msgItem.getBoxId() == Sms.MESSAGE_TYPE_SENT ){
                                
                if (msgItem.isFailedMessage() || msgItem.isOutgoingMessage()) {                    
                }else {
                    
                    String body = msgItem.mBody;
                    String mmsEasterEggs = getResources().getString(R.string.mms_lewa_easter_eggs);
                    int index = body.toString().indexOf(mmsEasterEggs);    
                    
                    if (index >= 0) {                        
                        if (bIsfirstStart) {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean(MessagingPreferenceActivity.LEWA_EASTER_EGGS_FIRST, false);          
                            editor.commit();
                        }else {
                            return;
                        }
                        
                        if (mContacts != null && mContacts.size() > 1) {
                            NewMessageComposeActivity.showLewaEasterEggsWindow(mContext);
                        }else {
                            ComposeMessageActivity.showLewaEasterEggsWindow(mContext);
                        }
                    } 
                    
                }
            }                
        }        

    }

    private void hideMmsViewIfNeeded() {
        if (mMmsView != null) {
            mMmsView.setVisibility(View.GONE);
        }
    }

    public void startAudio() {
        // TODO Auto-generated method stub
    }

    public void startVideo() {
        // TODO Auto-generated method stub
    }

    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
        // TODO Auto-generated method stub
    }
    
    public void setImage(Bitmap bitmap) {
        try {
            if (null == bitmap) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_picture);
            }
            mYlImageView.setImageBitmap(bitmap);
            mYlImageView.setVisibility(VISIBLE);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setImage: out of memory: ", e);
        }
    }
    
    public void setYlVieo(Bitmap bitmap) {
        try {
            if (null == bitmap) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_video);
            }
            mYlImageView.setImageBitmap(bitmap);
            mYlImageView.setVisibility(VISIBLE);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setImage: out of memory: ", e);
        }
    }
    
    public void setImage(String name, Bitmap bitmap) {
        inflateMmsView();

        try {
            if (null == bitmap) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_picture);
            }
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(VISIBLE);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setImage: out of memory: ", e);
        }
    }    

    private void inflateMmsView() {
//      if (mMmsView == null) {
//      // inflate the surrounding view_stub
//      //mMmsLayoutViewStub.setVisibility(ViewStub.VISIBLE);
//
//      mMmsView = findViewById(R.id.mms_view);
//      mImageView = (ImageView) findViewById(R.id.image_view);
//      mSlideShowButton = (ImageButton) findViewById(R.id.play_slideshow_button);
//  }
    	mMmsView.setVisibility(View.VISIBLE);
		mImageView.setVisibility(View.VISIBLE);
    }

    private void inflateDownloadControls() {
        if (mDownloadButton == null) {
            // inflate the download controls
            mMmsDownloadingViewStub.setVisibility(ViewStub.VISIBLE);
            mDownloadButton = (Button) findViewById(R.id.btn_download_msg);
            mDownloadingLabel = (TextView) findViewById(R.id.label_downloading);
        }
    }

    private LeadingMarginSpan mLeadingMarginSpan;

//    private LineHeightSpan    mSpan          = new LineHeightSpan() {
//                                                 public void chooseHeight(
//                                                         CharSequence text,
//                                                         int start, int end,
//                                                         int spanstartv, int v,
//                                                         FontMetricsInt fm) {
//                                                     fm.ascent -= 10;
//                                                 }
//                                             };

    TextAppearanceSpan        mTextSmallSpan = new TextAppearanceSpan(
                                                     mContext,
                                                     android.R.style.TextAppearance_Small);

    ForegroundColorSpan       mColorSpan     = null;                                       // set
                                                                                            // in
                                                                                            // ctor

    private CharSequence formatMessage(MessageItem msgItem, String contact,
            String body, String subject, String timestamp, Pattern highlight,
            String contentType) {
        SpannableStringBuilder buf = new SpannableStringBuilder();// )TextUtils.replace(template,
        // new String[] { "%s" },
        // new CharSequence[] { contact }));

        boolean hasSubject = !TextUtils.isEmpty(subject);
        if (hasSubject) {
            String subjectString = mContext.getResources().getString(
                    R.string.inline_subject, subject);            
            SmileyParser parser = SmileyParser.getInstance();
            buf.append(parser.addSmileySpans(subjectString));
        }

        if (!TextUtils.isEmpty(body)) {
            // Converts html to spannable if ContentType is "text/html".
            if (contentType != null
                    && ContentType.TEXT_HTML.equals(contentType)) {
                buf.append("\n");
                buf.append(Html.fromHtml(body));
            } else {
                if (hasSubject) {
                    buf.append(" - ");
                }
                SmileyParser parser = SmileyParser.getInstance();
                buf.append(parser.addSmileySpans(body));
            }
        }
        // If we're in the process of sending a message (i.e. pending), then we
        // show a "Sending..."
        // string in place of the timestamp.
        /*
         * if (msgItem.isSending()) { timestamp =
         * mContext.getResources().getString(R.string.sending_message); }
         */
        // We always show two lines because the optional icon bottoms are
        // aligned with the
        // bottom of the text field, assuming there are two lines for the
        // message and the sent time.
        // buf.append("\n");
        int startOffset = buf.length();

        // startOffset = buf.length();
        // buf.append(TextUtils.isEmpty(timestamp) ? " " : timestamp);

        // buf.setSpan(mTextSmallSpan, startOffset, buf.length(),
        // Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // buf.setSpan(mSpan, startOffset+1, buf.length(), 0);

        // Make the timestamp text not as dark
        if (mBlackBackground) {
            int colorc = mContext.getResources().getColor(
                    R.color.timestamp_color_grey);
            buf.setSpan(new ForegroundColorSpan(colorc), startOffset,
                    buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
           // buf.setSpan(mColorSpan, startOffset, buf.length(),
           //         Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (highlight != null) {
            Matcher m = highlight.matcher(buf.toString());
            while (m.find()) {
                buf.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), 0);
            }
        }
        
        buf.setSpan(mLeadingMarginSpan, 0, buf.length(), 0);
        return buf;
    }

    private void drawPlaybackButton(MessageItem msgItem) {
        switch (msgItem.mAttachmentType) {
        case WorkingMessage.SLIDESHOW:
        case WorkingMessage.AUDIO:
        case WorkingMessage.VIDEO:
            // Show the 'Play' button and bind message info on it.
        	if (msgItem.mAttachmentType == WorkingMessage.AUDIO) {
				mImageView.setVisibility(View.GONE);
			}
            mSlideShowButton.setTag(msgItem);
            // Set call-back for the 'Play' button.
            mSlideShowButton.setOnClickListener(this);
            mMsgLinearLayoutBody.setTag(msgItem);
            mMsgLinearLayoutBody.setOnClickListener(this);
            mSlideShowButton.setVisibility(View.VISIBLE);
            setLongClickable(true);

            // When we show the mSlideShowButton, this list item's
            // onItemClickListener doesn't
            // get called. (It gets set in ComposeMessageActivity:
            // mMsgListView.setOnItemClickListener) Here we explicitly set the
            // item's
            // onClickListener. It allows the item to respond to embedded html
            // links and at the
            // same time, allows the slide show play button to work.
//            setOnClickListener(new OnClickListener() {
//                public void onClick(View v) {
//                    onMessageListItemClick();
//                }
//            });
            break;
        default:
            mSlideShowButton.setVisibility(View.GONE);
            mMsgLinearLayoutBody.setOnClickListener(null);
            break;
        }
    }

    // OnClick Listener for the playback button
    public void onClick(View v) {
        MessageItem mi = (MessageItem) v.getTag();
        switch (mi.mAttachmentType) {
        case WorkingMessage.VIDEO:
        case WorkingMessage.AUDIO:
        case WorkingMessage.SLIDESHOW:
        	MessageUtils.viewMmsMessageAttachment(mContext, mi.mMessageUri, mi.mSlideshow);	
            break;
        }
    }        
    
    Runnable mRunnable = new Runnable(){
	   @Override
	   public void run() {
		   MessageItem item = getMessageItem();
		   if (item.mhandler != null) {
			   int resId = 0;
			   switch (item.mPlayIndex) {			   
				case 0:
					resId = R.drawable.message_icon_voice_w_1;
					break;
				case 1:
					resId = R.drawable.message_icon_voice_w_2;					
					break;
				case 2:
					resId = R.drawable.message_icon_voice_w_3;										
					break;
	
				case 3:
					resId = R.drawable.message_icon_voice_w_4;										
					break;
					
				case 4:
					resId = R.drawable.message_icon_voice_w_5;										
					break;
					
				case 5:
					resId = R.drawable.message_icon_voice_w_6;										
					break;
					
				case 6:
					resId = R.drawable.message_icon_voice_w_7;										
					break;
					
				case 7:
					resId = R.drawable.message_icon_voice_w_8;										
					break;
					
				default:
					break;
			   }				   
			   item.mPlayIndex++;
			   item.mPlayIndex = item.mPlayIndex > 7 ? 0 : item.mPlayIndex;
			   mRecorderPlayIcon.setBackgroundResource(resId);
			   
			   if (item.mDataType.equals("audio/ogg")) {
				   int time = mRecorder.progress()%60;
				   Long duration = item.mPlayTime;
				   Date date = new Date(duration);
				   
				   if (time >= date.getSeconds()) {
					   mRecorder.stop();
				   }
			   }
			   
			   if (mRecorder.state() == mRecorder.IDLE_STATE) {
				   mRecorderPlayIcon.setBackgroundResource(R.drawable.message_icon_voice_w_0);
				   PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE); 
				   boolean screen = pm.isScreenOn();
				   if (screen == false) {
					   ComposeMessageActivity.mmsMusicPlayUnRegisterListener();
				   }
			   }else {
				   item.mhandler.postDelayed(this, 300);							
			   }
		   }
	   }
    };
    
    public void onMessageListItemClick() {
        URLSpan[] spans = mBodyTextView.getUrls();
        
        if (spans.length == 0) {
            // Do nothing.
        } else if (spans.length == 1) {
            Uri uri = Uri.parse(spans[0].getURL());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra(Browser.EXTRA_APPLICATION_ID,
                    mContext.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            mContext.startActivity(intent);
        } else {
            final java.util.ArrayList<String> urls = MessageUtils
                    .extractUris(spans);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                    android.R.layout.select_dialog_item, urls) {
                public View getView(int position, View convertView,
                        ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    try {
                        String url = getItem(position).toString();
                        TextView tv = (TextView) v;
                        Drawable d = mContext.getPackageManager()
                                .getActivityIcon(
                                        new Intent(Intent.ACTION_VIEW, Uri
                                                .parse(url)));
                        if (d != null) {
                            d.setBounds(0, 0, d.getIntrinsicHeight(),
                                    d.getIntrinsicHeight());
                            tv.setCompoundDrawablePadding(10);
                            tv.setCompoundDrawables(d, null, null, null);
                        }
                        final String telPrefix = "tel:";
                        if (url.startsWith(telPrefix)) {
                            url = PhoneNumberUtils.formatNumber(url
                                    .substring(telPrefix.length()));
                        }
                        tv.setText(url);
                    } catch (android.content.pm.PackageManager.NameNotFoundException ex) {
                        ;
                    }
                    return v;
                }
            };

            AlertDialog.Builder b = new AlertDialog.Builder(mContext);

            DialogInterface.OnClickListener click = new DialogInterface.OnClickListener() {
                public final void onClick(DialogInterface dialog, int which) {
                    if (which >= 0) {
                        Uri uri = Uri.parse(urls.get(which));
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID,
                                mContext.getPackageName());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        mContext.startActivity(intent);
                    }
                    dialog.dismiss();
                }
            };

            b.setTitle(R.string.select_link_title);
            b.setCancelable(true);
            b.setAdapter(adapter, click);

            b.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public final void onClick(DialogInterface dialog,
                                int which) {
                            dialog.dismiss();
                        }
                    });

            b.show();
        }
    }

    private void setOnClickListener(final MessageItem msgItem) {
        switch (msgItem.mAttachmentType) {
        case WorkingMessage.IMAGE:
        case WorkingMessage.VIDEO:
            mImageView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    MessageUtils.viewMmsMessageAttachment(mContext, null,
                            msgItem.mSlideshow);
                }
            });
            mImageView.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    return v.showContextMenu();
                }
            });
            break;

        default:
            mImageView.setOnClickListener(null);
            break;
        }
    }

    private void drawLeftStatusIndicator(MessageItem msgItem) {
        switch (msgItem.mBoxId) {
        case Mms.MESSAGE_BOX_INBOX:
        case Sms.MESSAGE_TYPE_ALL:	
            mMsgLinearLayoutBody.setBackgroundResource(R.drawable.listitem_background_lightgrey);
            break;
            
        case Mms.MESSAGE_BOX_SENT:
        case Mms.MESSAGE_BOX_DRAFTS:
        case Sms.MESSAGE_TYPE_FAILED:
        case Sms.MESSAGE_TYPE_QUEUED:
        case Mms.MESSAGE_BOX_OUTBOX:
             if(msgItem.isIms()) {
                 mMsgLinearLayoutBody.setBackgroundResource(R.drawable.listitem_background_lightgreen);
             } else {
            	 mMsgLinearLayoutBody.setBackgroundResource(R.drawable.listitem_background_lightblue);
             }
            break;

        default:
            mMsgLinearLayoutBody.setBackgroundResource(R.drawable.listitem_background_lightblue);
            break;
        }
    }

    private void setErrorIndicatorClickListener(final MessageItem msgItem) {
        String type = msgItem.mType;
        final int what;
        if (type.equals("sms")) {
            what = MSG_LIST_EDIT_SMS;
        } else {
            what = MSG_LIST_EDIT_MMS;
        }
        mDeliveredIndicator.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (null != mHandler) {
                    Message msg = Message.obtain(mHandler, what);
                    msg.obj = new Long(msgItem.mMsgId);
                    msg.sendToTarget();
                }
            }
        });
    }

    private void drawRightStatusIndicator(MessageItem msgItem) {
        // Locked icon
        if (msgItem.mLocked) {
            mLockedIndicator.setImageResource(R.drawable.ic_lock_message_sms);
            mLockedIndicator.setVisibility(View.VISIBLE);
        } else {
            mLockedIndicator.setVisibility(View.GONE);
        }
        
        if (msgItem.isOutgoingMessage()){
            String string = String.format("%s...", getContext().getString(R.string.send));
            mMsgTimeTextView.setText(string);            
        }

        // Delivery icon
        if (msgItem.isFailedMessage()) {
        	
            mDeliveredIndicator
                    .setImageResource(R.drawable.ic_list_alert_sms_failed);
            setErrorIndicatorClickListener(msgItem);
            mDeliveredIndicator.setVisibility(View.VISIBLE);
            mMsgTimeTextView.setText(getTimeText(msgItem, getContext().
                    getString(R.string.notification_failed_multiple_title) + msgItem.mTimestamp));
        } else if (msgItem.mDeliveryStatus == MessageItem.DeliveryStatus.FAILED) {
//            mDeliveredIndicator
//                    .setImageResource(R.drawable.ic_list_alert_sms_failed);
//            mDeliveredIndicator.setVisibility(View.VISIBLE);
//            mMsgTimeTextView.setText(getTimeText(msgItem, getContext().
//                    getString(R.string.notification_failed_multiple_title) + msgItem.mTimestamp));
        	mDeliveredIndicator.setVisibility(View.GONE);
        } else if (msgItem.mDeliveryStatus == MessageItem.DeliveryStatus.RECEIVED) {
            String s = mMsgTimeTextView.getText().toString();
            String reciverString = getResources().getString(R.string.message_item_received_label) + msgItem.mTimesDate;           
            mMsgTimeTextView.setText(getTimeText( msgItem,reciverString)); 
            mDeliveredIndicator.setVisibility(View.GONE);
//            mDeliveredIndicator
//                    .setImageResource(R.drawable.ic_sms_mms_delivered);
//            mDeliveredIndicator.setVisibility(View.VISIBLE);
        } else {
            mDeliveredIndicator.setVisibility(View.GONE);
        }

        // Message details icon
//        if (msgItem.mDeliveryStatus == MessageItem.DeliveryStatus.INFO
//                || msgItem.mReadReport) {
//            mDetailsIndicator.setImageResource(R.drawable.ic_sms_mms_details);
//            mDetailsIndicator.setVisibility(View.VISIBLE);
//        } else {
//            mDetailsIndicator.setVisibility(View.GONE);
//        }
    }

    public void setImageRegionFit(String fit) {
        // TODO Auto-generated method stub
    }

    public void setImageVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    public void setText(String name, String text) {
        // TODO Auto-generated method stub
    }

    public void setTextVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    public void setVideo(String name, Uri video) {
        inflateMmsView();

        try {
            Bitmap bitmap = VideoAttachmentView.createVideoThumbnail(mContext,
                    video);
            if (null == bitmap) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_video);
            }
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(VISIBLE);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setVideo: out of memory: ", e);
        }
    }

    public void setVideoVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    public void stopAudio() {
        // TODO Auto-generated method stub
    }

    public void stopVideo() {
        // TODO Auto-generated method stub
    }

    public void reset() {
        if (mImageView != null) {
            mImageView.setVisibility(GONE);
        }
    }

    public void setVisibility(boolean visible) {
        // TODO Auto-generated method stub
    }

    public void pauseAudio() {
        // TODO Auto-generated method stub

    }

    public void pauseVideo() {
        // TODO Auto-generated method stub

    }

    public void seekAudio(int seekTo) {
        // TODO Auto-generated method stub

    }

    public void seekVideo(int seekTo) {
        // TODO Auto-generated method stub

    }
    private void setImsOnClickListener(final MessageItem msgItemPram) {		
		mMsgLinearLayoutBody.setTag(msgItemPram);
		
    	if (ContentType.isAudioType(msgItemPram.mDataType)) {    		
    		mMsgLinearLayoutBody.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						String toshort = getResources().getString(R.string.mms_input_play_no_sdcard);
						Toast.makeText(getContext(), toshort, Toast.LENGTH_SHORT).show(); 
						return;
					}
					MessageItem msgItem = (MessageItem)v.getTag();
        			String path = msgItem.mFilePath + "/" + msgItem.mFileName;        			
        			if (mRecorder.state() == mRecorder.IDLE_STATE) {
        				mRecorder.play(path);
        				ComposeMessageActivity.mmsMusicPlayRegisterListener();
            			if (msgItem.mhandler != null) {
            				msgItem.mPlayIndex = 0;
            				msgItem.mhandler.postDelayed(mRunnable, 300);
    					}
            			
					}else if (mRecorder.state() == mRecorder.PLAYING_STATE){
						mRecorder.stop();
            			if (msgItem.mhandler != null) {
            				msgItem.mhandler.removeCallbacks(mRunnable);
    					}
            			mRecorderPlayIcon.setBackgroundResource(R.drawable.message_icon_voice_w_0);
					}					
				}
			});    		
		}else if("video/3gp".equals(msgItemPram.mDataType) ||
				ContentType.isVideoType(msgItemPram.mDataType)){
			
			mMsgLinearLayoutBody.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent mediaIntent = new Intent(Intent.ACTION_VIEW);
					MessageItem msgItem = (MessageItem)v.getTag();
					String path = msgItem.mFilePath + "/" + msgItem.mFileName; 
					Uri uri = Uri.parse(path);
					String type = "video/*";
					mediaIntent.setDataAndType(uri, type);
					mContext.startActivity(mediaIntent);					
				}
			});
		}else if (ContentType.isImageType(msgItemPram.mDataType) == true) {
			mMsgLinearLayoutBody.setOnClickListener(new OnClickListener() {
				
				@Override
				
				public void onClick(View v) {
					MessageItem msgItem = (MessageItem)v.getTag();
					String path = msgItem.mFilePath + "/" + msgItem.mFileName;
					File fp = new File(path);
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(fp), "image/*");
					mContext.startActivity(intent);				
				}
			});			
		}else {
			mMsgLinearLayoutBody.setOnClickListener(null);			
		}
		mMsgLinearLayoutBody.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                return v.showContextMenu();
            }
        });
    }
    private OnClickListener mTextBodyClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			MessageItem item = (MessageItem)v.getTag();			
			if (item != null && item.isMms() && item.mAttachmentType != WorkingMessage.TEXT) {
				
				if (LinkMovementMethod.getIsOnClick() == false) {
					MessageUtils.viewMmsMessageAttachment(mContext, item.mMessageUri, item.mSlideshow);						
				}
			}
			if (LinkMovementMethod.getIsOnClick()){
				LinkMovementMethod.setIsOnClick(false);
			}
		}
	};
}
