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

import android.R.integer;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.telephony.CallerInfo;
import com.google.android.mms.ContentType;
import com.lewa.PIM.R;
import com.lewa.PIM.contacts.LayoutQuickContactBadge;
import com.lewa.PIM.mms.data.Contact;
import com.lewa.PIM.mms.data.ContactList;
import com.lewa.PIM.mms.util.SmileyParser;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.os.util.ContactPhotoLoader;

/**
 * This class manages the view for given conversation.
 */
public class ConversationListItem extends RelativeLayout implements Contact.UpdateListener {
    private static final String TAG = "ConversationListItem";
    private static final boolean DEBUG = false;

    private TextView mSubjectView;
    private TextView mFromView;
    private TextView mDateView;
    private View mAttachmentView;
    private View mErrorIndicator;
    private ImageView mPresenceView;
    private QuickContactBadge mAvatarView;
    private TextView mUnreadMessagCount;
    private TextView mMessagCount;
    private TextView mDraftText;
    static private Drawable sDefaultContactImage;
    private ContactPhotoLoader mPhotoLoader;
    private ConversationListAdapter mAdapter;
    private ImageView mContactItemYlImage;
    private Context mConversationContext;
    private ImageView mImsState;
    private LinearLayout mSubjectLayout;
    // For posting UI update Runnables from other threads:
    private Handler mHandler = new Handler();

    private ConversationListItemData mConversationHeader;

    private static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    
    private int mylImageWidth;
    private int mylImageMarg;
    
    public ConversationListItem(Context context) {
        super(context);
        mConversationContext = context;
    }

    public ConversationListItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (sDefaultContactImage == null) {
            sDefaultContactImage = context.getResources().getDrawable(R.drawable.ic_contact_picture);
        }
        mConversationContext = context;
    }    

    public void setPhotoLoader(ContactPhotoLoader photoLade){
        mPhotoLoader = photoLade;
    }
    
    public void setAdapter(ConversationListAdapter adapter){
        mAdapter = adapter;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mFromView = (TextView) findViewById(R.id.from);
        mSubjectView = (TextView) findViewById(R.id.subject);

        mDateView = (TextView) findViewById(R.id.date);
        mAttachmentView = findViewById(R.id.attachment);
        mErrorIndicator = findViewById(R.id.error);
        mPresenceView = (ImageView) findViewById(R.id.presence);
        mAvatarView = (QuickContactBadge) findViewById(R.id.avatar);
        mUnreadMessagCount = (TextView) findViewById(R.id.contact_item_notification_count);
        mMessagCount = (TextView) findViewById(R.id.fromcount);
        mDraftText = (TextView) findViewById(R.id.draft_text); 
        mContactItemYlImage = (ImageView) findViewById(R.id.contact_item_yl_image);
        mImsState = (ImageView) findViewById(R.id.contact_yl_number_state);
        mSubjectLayout = (LinearLayout) findViewById(R.id.subject_layout);
        		
		Drawable mylImage = mContext.getResources().getDrawable(R.drawable.icon_contact_header_online);
		mylImageWidth = mylImage.getIntrinsicWidth();
		
		Resources resources = mConversationContext.getResources();
		mylImageMarg = resources.getDimensionPixelOffset(R.dimen.list_item_gap_between_yl_image_right);
    }

    public void setPresenceIcon(int iconId) {
        if (iconId == 0) {
            mPresenceView.setVisibility(View.GONE);
        } else {
            mPresenceView.setImageResource(iconId);
            mPresenceView.setVisibility(View.VISIBLE);
        }
    }

    public ConversationListItemData getConversationHeader() {
        return mConversationHeader;
    }

    private void setConversationHeader(ConversationListItemData header) {
        mConversationHeader = header;
    }

    /**
     * Only used for header binding.
     */
    public void bind(String title, String explain) {
        mFromView.setText(title);
        mSubjectView.setText(explain);
    }

    private CharSequence formatMessage(ConversationListItemData ch) {
        String from = ch.getFrom();
        
        if (ch.getContacts().size() == 1){
            Contact contact = ch.getContacts().get(0);
            if (!contact.existsInDatabase()){
                String sLocation = CommonMethod.getSpecialPhone(from);
                if (!TextUtils.isEmpty(sLocation)) {
                    from = sLocation;
                }
            }
        }
        
        if (from.equals(CallerInfo.UNKNOWN_NUMBER)) {
            from = getResources().getString(R.string.unknown);
        }
        else if (from.equals(CallerInfo.PRIVATE_NUMBER)) {
            from = getResources().getString(R.string.private_num);
        }
        else if (from.equals(CallerInfo.PAYPHONE_NUMBER)) {
            from = getResources().getString(R.string.payphone);
        }

        SpannableStringBuilder buf = new SpannableStringBuilder(from);

        // Unread messages are shown in bold
//        if (!ch.isRead()) {
//            buf.setSpan(STYLE_BOLD, 0, buf.length(),
//                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//        }
        return buf;
    }

    private void updateAvatarView() {
        ConversationListItemData ch = mConversationHeader;
        Drawable avatarDrawable;
        if (ch.getContacts().size() == 1) {
            Contact contact = ch.getContacts().get(0);

            if (contact.existsInDatabase()) {
                mAvatarView.setOnClickListener(null);
                mAvatarView.assignContactUri(contact.getUri());        
                String tempNumber = MessageUtils.fixPhoneNumber(contact.getNumber());
                int photoId = mAdapter.getPhotoIdToInt(tempNumber);
                mPhotoLoader.loadPhoto(mAvatarView, photoId);
            } else {
                mPhotoLoader.loadSpecialPhoto(mAvatarView, ch.getFrom());
                new LayoutQuickContactBadge.UnknownQCBOnClickListener(
                        mContext,
                        contact.getNumber(),
                        mAvatarView);
            }
        }else if (ch.getContacts().size() == 0) {
            avatarDrawable = mContext.getResources().getDrawable(R.drawable.ic_contact_header_unknow);
            mAvatarView.setOnClickListener(null);
            mAvatarView.setImageDrawable(avatarDrawable);            
        }else {
            // TODO get a multiple recipients asset (or do something else)
            avatarDrawable = mContext.getResources().getDrawable(R.drawable.ic_contact_header_group);
            mAvatarView.setOnClickListener(null);
            mAvatarView.setImageDrawable(avatarDrawable);
        }
        mAvatarView.setVisibility(View.VISIBLE);
    }

    private void updateFromView() {
        ConversationListItemData ch = mConversationHeader;
        ch.updateRecipients();
        mFromView.setText(formatMessage(ch));
        if (ch.getMessageCount() > 1) {
            mMessagCount.setText(" (" + ch.getMessageCount() + ") ");
            mMessagCount.setVisibility(View.VISIBLE);
        }else {
            mMessagCount.setVisibility(View.GONE);                        
        }
        
        if (ch.hasDraft()) {
            mDraftText.setText(mContext.getResources().getString(R.string.has_draft));   
            mDraftText.setVisibility(View.VISIBLE);
        }
        else {
            mDraftText.setVisibility(View.GONE);
        }
        
        //setPresenceIcon(ch.getContacts().getPresenceResId());
        updateAvatarView();
    }

    public void onUpdate(Contact updated) {
        mHandler.post(new Runnable() {
            public void run() {
                updateFromView();
            }
        });
    }
    
    private static final String[] READ_PROJECTION = new String[] {
        "read"
    };
    
    private int queryUnreadSmsCount(ContentResolver resolver, long threadId) {
        int count = 0;
        Cursor cursor = null;
        
        try {
            cursor = resolver.query(Uri.parse("content://sms"),
                    null,
                    " (type = 1) AND (read = 0) AND (thread_id = "+ threadId +")",
                    null,
                    "address");

            count += cursor.getCount(); 
            
        } catch (Exception e) {
            Log.e(TAG, "queryUnreadSmsCount() error uri= content://sms");
        }finally{
            if (cursor != null) {
                cursor.close();
            }
        }
        
        //get mms unread count
        try {
            String  sQuery = "* from pdu" + 
				            " where (pdu.msg_box = 1) AND" +  
				            " (pdu.read = 0) AND (pdu.thread_id = " + threadId + ")";

            cursor = resolver.query(Uri.parse("content://mms"),
                        new String[]{sQuery + "--"},
                        null,
                        null,
                        null);
            
            
            count += cursor.getCount();
            
        } catch (Exception e) {
            Log.e(TAG, "queryUnreadSmsCount() error uri= content://mms");
        }finally{
            if (cursor != null) {
                cursor.close();
            }
        }

        try {
            cursor = resolver.query(MessageUtils.PduYlColumns.IMS_URI,
                                                    READ_PROJECTION,
                                                    " (msg_box = 1) AND (read = 0) AND (thread_id = "+ threadId +")",
                                                    null,
                                                    null); 
            
            count += cursor.getCount();
            
        } catch (Exception e) {
            Log.e(TAG, "queryUnreadSmsCount() error uri=" + MessageUtils.PduYlColumns.IMS_URI.toString());
        }finally{
            if (cursor != null) {
                cursor.close();
            }
        }        
        return count;
    }
    
    public final void bind(Context context, final ConversationListItemData ch) {
        //if (DEBUG) Log.v(TAG, "bind()");

        setConversationHeader(ch);
        //LayoutParams attachmentLayout = (LayoutParams)mAttachmentView.getLayoutParams();
        boolean hasError = ch.hasError();
        // When there's an error icon, the attachment icon is left of the error icon.
        // When there is not an error icon, the attachment icon is left of the date text.
        // As far as I know, there's no way to specify that relationship in xml.
//        if (hasError) {
//            attachmentLayout.addRule(RelativeLayout.RIGHT_OF, R.id.error);
//        } else {
//            attachmentLayout.addRule(RelativeLayout.RIGHT_OF, R.id.contact_sticker_photos);
//        }

        boolean hasAttachment = ch.hasAttachment();
        mAttachmentView.setVisibility(hasAttachment ? VISIBLE : GONE);

        // Date
        mDateView.setText(ch.getDate());

        // From.
        CharSequence from = formatMessage(ch);        
        SpannableStringBuilder buf = new SpannableStringBuilder(from);
        TextPaint paint = mFromView.getPaint();  
        paint.setFakeBoldText(false);  
        
        if (!ch.isRead()) {
            ContactList list = ch.getContacts();
            if (list.size() == 1) {
                int addressCount = queryUnreadSmsCount(getContext().getContentResolver() ,ch.getThreadId());
                if ( addressCount > 0) {
                    if (addressCount > 99) {                        
                        mUnreadMessagCount.setText("!");
                    }else{
                        mUnreadMessagCount.setText("" + addressCount);                        
                    }
                    mUnreadMessagCount.setVisibility(View.VISIBLE);
                    paint = mFromView.getPaint();  
                    paint.setFakeBoldText(true);  
                }
            }else {
                buf.setSpan(STYLE_BOLD, 0, buf.length(),
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                
                mUnreadMessagCount.setVisibility(View.GONE);            
            }
        }
        else {
            mUnreadMessagCount.setVisibility(View.GONE);            
        }        
		
		android.widget.LinearLayout.LayoutParams fromparams = 
			(android.widget.LinearLayout.LayoutParams)mFromView.getLayoutParams();
		
		android.widget.RelativeLayout.LayoutParams subjectparams = 
			(android.widget.RelativeLayout.LayoutParams)mSubjectLayout.getLayoutParams();

		
        if (MessageUtils.isImsSwitch(mConversationContext) && ch.hasImsUser()) {
        	mContactItemYlImage.setVisibility(View.GONE);
        	ContactList list = ch.getContacts();
        	if (MessageUtils.getImsNumberState(list.get(0).getNumber())) {
				mImsState.setImageResource(R.drawable.icon_contact_header_online);
			}else {
				mImsState.setImageResource(R.drawable.icon_contact_header_offline);
			}
        	
        	mImsState.setVisibility(View.VISIBLE);
        	subjectparams.leftMargin = mylImageWidth + mylImageMarg;
        	fromparams.leftMargin = mylImageMarg;
		}else {
        	mContactItemYlImage.setVisibility(View.GONE);
        	mImsState.setVisibility(View.GONE);
        	
        	if (MessageUtils.isImsSwitch(mConversationContext) == false) {
        		
        		fromparams.leftMargin = mylImageMarg;				
        		subjectparams.leftMargin = mylImageMarg;        		
			}else {
				fromparams.leftMargin = mylImageWidth + mylImageMarg;				
				subjectparams.leftMargin = mylImageWidth + mylImageMarg;
			}        	
		}

        mFromView.setLayoutParams(fromparams);
		mSubjectLayout.setLayoutParams(subjectparams);
		
        mFromView.setText(buf);
        
        //message count
        if (ch.getMessageCount() > 1) {
            mMessagCount.setText(" (" + ch.getMessageCount() + ") ");
            mMessagCount.setVisibility(View.VISIBLE);
        }else {
            mMessagCount.setVisibility(View.GONE);            
        }
        //has draft
        if (ch.hasDraft()) {
            mDraftText.setText(mContext.getResources().getString(R.string.has_draft));   
            mDraftText.setVisibility(View.VISIBLE);
        }
        else {
            mDraftText.setVisibility(View.GONE);
        }

        // Register for updates in changes of any of the contacts in this conversation.
        ContactList contacts = ch.getContacts();

        if (DEBUG) Log.v(TAG, "bind: contacts.addListeners " + this);
        Contact.addListener(this);
        //setPresenceIcon(contacts.getPresenceResId());

        // Subject
        buf.clear();
        String subjectStr;
        if ((ch.hasImsUser()) && (!TextUtils.isEmpty(ch.getImsDataType()))) {
        	if (ContentType.isAudioType(ch.getImsDataType())) {
        		subjectStr = getResources().getString(R.string.mms_audio_message_text);
			}else if (ContentType.isImageType(ch.getImsDataType())){
        		subjectStr = getResources().getString(R.string.mms_image_message_text);				
			}else if (ContentType.isVideoType(ch.getImsDataType())) {
        		subjectStr = getResources().getString(R.string.mms_video_message_text);								
			}else {
				subjectStr = ch.getSubject();				
			}
		}else {
			subjectStr = ch.getSubject();
		}
        SmileyParser parser = SmileyParser.getInstance();
        buf.append(parser.addSmileySpans(subjectStr));        			
        mSubjectView.setText(buf);
        //LayoutParams subjectLayout = (LayoutParams)mSubjectView.getLayoutParams();
        // We have to make the subject left of whatever optional items are shown on the right.
        //subjectLayout.addRule(RelativeLayout.RIGHT_OF, hasAttachment ? R.id.attachment :
        //    (hasError ? R.id.error : R.id.contact_sticker_photos));

        // Transmission error indicator.
        mErrorIndicator.setVisibility(hasError ? VISIBLE : GONE);

        updateAvatarView();
    }

    public final void unbind() {
        if (DEBUG) Log.v(TAG, "unbind: contacts.removeListeners " + this);
        // Unregister contact update callbacks.
        Contact.removeListener(this);
    }
}
