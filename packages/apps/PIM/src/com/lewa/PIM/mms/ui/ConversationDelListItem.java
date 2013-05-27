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
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.telephony.CallerInfo;
import com.lewa.PIM.R;
import com.lewa.PIM.mms.data.Contact;
import com.lewa.PIM.mms.data.ContactList;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.os.util.ContactPhotoLoader;

/**
 * This class manages the view for given conversation.
 */
public class ConversationDelListItem extends RelativeLayout implements Contact.UpdateListener {
    private static final String TAG = "ConversationDelListItem";
    private static final boolean DEBUG = false;

    private TextView mSubjectView;
    private TextView mFromView;
    private TextView mDateView;
    private View mAttachmentView;
    private View mErrorIndicator;
    private ImageView mPresenceView;
    private QuickContactBadge mAvatarView;
    private TextView mMessagCount;
    private ImageView mContactItemYlImage;
    
    static private Drawable sDefaultContactImage;
    
    // For posting UI update Runnables from other threads:
    private Handler mHandler = new Handler();

    private ConversationListItemData mConversationHeader;
    private ContactPhotoLoader mPhotoLoader;
    private ConversationDelListAdapter mAdapter;
    private Context mConversationContext;
    
    private static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);    
    
    public ConversationDelListItem(Context context) {
        super(context);
        mConversationContext = context;
    }

    public ConversationDelListItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (sDefaultContactImage == null) {
            sDefaultContactImage = context.getResources().getDrawable(R.drawable.ic_contact_picture);
        }
        mConversationContext = context;
    }
    
    public void setPhotoLoader(ContactPhotoLoader photoLade){
        mPhotoLoader = photoLade;
    }
    
    public void setAdapter(ConversationDelListAdapter adapter){
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
        mMessagCount = (TextView) findViewById(R.id.fromcount);
        mContactItemYlImage = (ImageView) findViewById(R.id.contact_item_yl_image);
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
        if (!ch.isRead()) {
            buf.setSpan(STYLE_BOLD, 0, buf.length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        
        if (MessageUtils.isImsSwitch(mConversationContext) && ch.hasImsUser()) {
        	mContactItemYlImage.setVisibility(View.GONE);
		}else {
        	mContactItemYlImage.setVisibility(View.GONE);			
		}
        
        return buf;
    }

    private void updateAvatarView() {
        ConversationListItemData ch = mConversationHeader;

        Drawable avatarDrawable;
        if (ch.getContacts().size() == 1) {
            Contact contact = ch.getContacts().get(0);
            if (contact.existsInDatabase()) {
            	String tempNumber = MessageUtils.fixPhoneNumber(contact.getNumber());
                int photoId = mAdapter.getPhotoIdToInt(tempNumber);
                mPhotoLoader.loadPhoto(mAvatarView, photoId);
            } else {
                mPhotoLoader.loadSpecialPhoto(mAvatarView, ch.getFrom());
            }
        } else {
            // TODO get a multiple recipients asset (or do something else)
            avatarDrawable = mContext.getResources().getDrawable(R.drawable.ic_contact_header_group);
            mAvatarView.setImageDrawable(avatarDrawable);
        }
        mAvatarView.setOnClickListener(null);
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
        setPresenceIcon(ch.getContacts().getPresenceResId());
        updateAvatarView();
    }

    public void onUpdate(Contact updated) {
        mHandler.post(new Runnable() {
            public void run() {
                updateFromView();
            }
        });
    }
    
    public final void bind(Context context, final ConversationListItemData ch) {
        //if (DEBUG) Log.v(TAG, "bind()");

        setConversationHeader(ch);
        LayoutParams attachmentLayout = (LayoutParams)mAttachmentView.getLayoutParams();
        boolean hasError = ch.hasError();
        // When there's an error icon, the attachment icon is left of the error icon.
        // When there is not an error icon, the attachment icon is left of the date text.
        // As far as I know, there's no way to specify that relationship in xml.
        if (hasError) {
            attachmentLayout.addRule(RelativeLayout.RIGHT_OF, R.id.error);
        } else {
            attachmentLayout.addRule(RelativeLayout.RIGHT_OF, R.id.contact_sticker_photos);
        }
        
        boolean hasAttachment = ch.hasAttachment();
        mAttachmentView.setVisibility(hasAttachment ? VISIBLE : GONE);

        // Date
        mDateView.setText(ch.getDate());

        // From.
        mFromView.setText(formatMessage(ch));
          if (ch.getMessageCount() > 1) {
              mMessagCount.setText(" (" + ch.getMessageCount() + ") ");
              mMessagCount.setVisibility(View.VISIBLE);
          }else {
              mMessagCount.setVisibility(View.GONE);            
          }

        // Register for updates in changes of any of the contacts in this conversation.
        ContactList contacts = ch.getContacts();

        if (DEBUG) Log.v(TAG, "bind: contacts.addListeners " + this);
        Contact.addListener(this);
        setPresenceIcon(contacts.getPresenceResId());

        // Subject
        mSubjectView.setText(ch.getSubject());
        LayoutParams subjectLayout = (LayoutParams)mSubjectView.getLayoutParams();
        // We have to make the subject left of whatever optional items are shown on the right.
        subjectLayout.addRule(RelativeLayout.RIGHT_OF, hasAttachment ? R.id.attachment :
            (hasError ? R.id.error : R.id.contact_sticker_photos));

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
