/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.lewa.PIM.widget;

//import com.android.internal.R;

import com.lewa.PIM.R;
import com.lewa.PIM.ui.DetailEntry;
import com.lewa.os.util.ImageUtil;

import android.Manifest;
import android.R.integer;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.User;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.QuickContactBadge;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Header used across system for displaying a title bar with contact info. You
 * can bind specific values on the header, or use helper methods like
 * {@link #bindFromContactId(long)} to populate asynchronously.
 * <p>
 * The parent must request the {@link Manifest.permission#READ_CONTACTS}
 * permission to access contact data.
 */
public class ContactHeaderWidget extends FrameLayout implements View.OnClickListener {

    private static final String TAG = "ContactHeaderWidget";

    private ImageView mPhotoView;
    private TextView mDisplayNameView;
    private CheckBox mStarredView;
    private ImageView mAddToContactsView;
    private ImageView mEditContactView;
    private ImageView mInviteFriendView;
    private ImageView mGroupChatView;
    private ImageView mStatusView;
    
    private int mNoPhotoResource;
    private QueryHandler mQueryHandler;

    protected Uri mContactUri;
    protected String mPhoneNumber;
    protected Uri mUserUri;
    
    protected String[] mExcludeMimes = null;

    protected ContentResolver mContentResolver;
    
    private static int mImsState = DetailEntry.ACTION_YILIAO_ONLINE_IMAGE_NULL;

    /**
     * Interface for callbacks invoked when the user interacts with a header.
     */
    public interface ContactHeaderListener {
        public void onPhotoClick(View view);
        public void onDisplayNameClick(View view);
        public void onAddToContactsClick(View view);
        public void onEditContactClick(View view);
        public void onInviteFriendClick(View view);
        public void onGroupChatClick(View view);
    }

    private ContactHeaderListener mListener;


    private interface ContactQuery {
        //Projection used for the summary info in the header.
        String[] COLUMNS = new String[] {
            Contacts._ID,
            Contacts.LOOKUP_KEY,
            Contacts.PHOTO_ID,
            Contacts.DISPLAY_NAME,
            Contacts.PHONETIC_NAME,
            Contacts.STARRED,
            Contacts.CONTACT_PRESENCE,
            Contacts.CONTACT_STATUS,
            Contacts.CONTACT_STATUS_TIMESTAMP,
            Contacts.CONTACT_STATUS_RES_PACKAGE,
            Contacts.CONTACT_STATUS_LABEL,
        };
        int _ID = 0;
        int LOOKUP_KEY = 1;
        int PHOTO_ID = 2;
        int DISPLAY_NAME = 3;
        int PHONETIC_NAME = 4;
        //TODO: We need to figure out how we're going to get the phonetic name.
        //static final int HEADER_PHONETIC_NAME_COLUMN_INDEX
        int STARRED = 5;
        int CONTACT_PRESENCE_STATUS = 6;
        int CONTACT_STATUS = 7;
        int CONTACT_STATUS_TIMESTAMP = 8;
        int CONTACT_STATUS_RES_PACKAGE = 9;
        int CONTACT_STATUS_LABEL = 10;
    }

    private interface PhotoQuery {
        String[] COLUMNS = new String[] {
            Photo.PHOTO
        };

        int PHOTO = 0;
    }

    //Projection used for looking up contact id from phone number
    protected static final String[] PHONE_LOOKUP_PROJECTION = new String[] {
        PhoneLookup._ID,
        PhoneLookup.LOOKUP_KEY,
    };
    protected static final int PHONE_LOOKUP_CONTACT_ID_COLUMN_INDEX = 0;
    protected static final int PHONE_LOOKUP_CONTACT_LOOKUP_KEY_COLUMN_INDEX = 1;

    //Projection used for looking up contact id from email address
    protected static final String[] EMAIL_LOOKUP_PROJECTION = new String[] {
        RawContacts.CONTACT_ID,
        Contacts.LOOKUP_KEY,
    };
    protected static final int EMAIL_LOOKUP_CONTACT_ID_COLUMN_INDEX = 0;
    protected static final int EMAIL_LOOKUP_CONTACT_LOOKUP_KEY_COLUMN_INDEX = 1;

    protected static final String[] CONTACT_LOOKUP_PROJECTION = new String[] {
        Contacts._ID,
    };
    protected static final int CONTACT_LOOKUP_ID_COLUMN_INDEX = 0;

    private static final int TOKEN_CONTACT_INFO = 0;
    private static final int TOKEN_PHONE_LOOKUP = 1;
    private static final int TOKEN_EMAIL_LOOKUP = 2;
    private static final int TOKEN_PHOTO_QUERY = 3;
    private static final int TOKEN_SPECIAL_NUMBER_PHOTO_QUERY = 4;
    private static final int TOKEN_YILIAO_PHOTO_QUERY = 5;

    public ContactHeaderWidget(Context context) {
        this(context, null);
    }

    public ContactHeaderWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContactHeaderWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContentResolver = mContext.getContentResolver();

        LayoutInflater inflater =
            (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.contact_header, this);

        mPhotoView = (ImageView) findViewById(R.id.photo);
        mStatusView = (ImageView) findViewById(R.id.imessage_status);
        
        mDisplayNameView = (TextView) findViewById(R.id.name);
        mDisplayNameView.setSelected(true);

        mStarredView = (CheckBox )findViewById(R.id.star);
        mStarredView.setOnClickListener(this);

        mAddToContactsView = (ImageView )findViewById(R.id.add_to_contacts);
        mAddToContactsView.setOnClickListener(this);

        mEditContactView = (ImageView )findViewById(R.id.edit_contact);
        mEditContactView.setOnClickListener(this);
        
        mInviteFriendView = (ImageView)findViewById(R.id.invite_friend);
        mInviteFriendView.setOnClickListener(this);
        
        mGroupChatView = (ImageView)findViewById(R.id.group_chat);
        mGroupChatView.setOnClickListener(this);
        
        mNoPhotoResource = R.drawable.ic_contact_picture_other;

        resetAsyncQueryHandler();
    }

    public void enableClickListeners() {
        mDisplayNameView.setOnClickListener(this);
        mPhotoView.setOnClickListener(this);
    }

    /**
     * Set the given {@link ContactHeaderListener} to handle header events.
     */
    public void setContactHeaderListener(ContactHeaderListener listener) {
        mListener = listener;
    }

    private void performPhotoClick() {
        if (mListener != null) {
            mListener.onPhotoClick(mPhotoView);
        }
    }

    private void performDisplayNameClick() {
        if (mListener != null) {
            mListener.onDisplayNameClick(mDisplayNameView);
        }
    }

    private void performAddToContactsClick() {
        if (mListener != null) {
            mListener.onAddToContactsClick(mAddToContactsView);
        }
    }

    private void performEditContactClick() {
        if (mListener != null) {
            mListener.onEditContactClick(mEditContactView);
        }
    }

    private void performInviteFriendClick() {
        if (mListener != null) {
            mListener.onInviteFriendClick(mInviteFriendView);
        }
    }

    private void performGroupChatClick() {
        if (mListener != null) {
            mListener.onGroupChatClick(mGroupChatView);
        }
    }
    
    private class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            try{
                if (this != mQueryHandler) {
                    Log.d(TAG, "onQueryComplete: discard result, the query handler is reset!");
                    return;
                }

                switch (token) {
                    case TOKEN_PHOTO_QUERY:
                    case TOKEN_YILIAO_PHOTO_QUERY:
                    case TOKEN_SPECIAL_NUMBER_PHOTO_QUERY: {
                        //Set the photo
                        Bitmap photoBitmap = null;
                        if (cursor != null && cursor.moveToFirst()
                                && !cursor.isNull(PhotoQuery.PHOTO)) {
                            byte[] photoData = cursor.getBlob(PhotoQuery.PHOTO);
                            photoBitmap = BitmapFactory.decodeByteArray(photoData, 0,
                                    photoData.length, null);
                            photoBitmap = ImageUtil.toRoundCorner(photoBitmap, 30);
                        }

                        if (photoBitmap != null) {
                            BitmapDrawable bd= new BitmapDrawable(photoBitmap);
                            mPhotoView.setBackgroundDrawable(bd);
                        } else {
                            mPhotoView.setBackgroundDrawable(loadPlaceholderPhoto(null));
                        }                        
                        //mPhotoView.setScaleType(ImageView.ScaleType.FIT_XY);
                        
                        if (cookie != null && cookie instanceof Uri) {
                            //mPhotoView.assignContactUri((Uri) cookie);
                        }
                        invalidate();
                        break;
                    }
                    case TOKEN_CONTACT_INFO: {
                        if (cursor != null && cursor.moveToFirst()) {
                            bindContactInfo(cursor);
                            Uri lookupUri = Contacts.getLookupUri(cursor.getLong(ContactQuery._ID),
                                    cursor.getString(ContactQuery.LOOKUP_KEY));

                            final long photoId = cursor.getLong(ContactQuery.PHOTO_ID);

                            if (photoId == 0) {
                                mPhotoView.setBackgroundDrawable(loadPlaceholderPhoto(null));
                                if (cookie != null && cookie instanceof Uri) {
                                    //mPhotoView.assignContactUri((Uri) cookie);
                                }
                                invalidate();
                            } else {
                                startPhotoQuery(photoId, lookupUri,
                                        false /* don't reset query handler */);
                            }
                        } else {
                            // shouldn't really happen
                            //setDisplayName(null, null);
                            setSocialSnippet(null);
                            startSpecialNumberPhotoQuery(false); //setPhoto(loadPlaceholderPhoto(null));
                        }
                        break;
                    }
                    case TOKEN_PHONE_LOOKUP: {
                        if (cursor != null && cursor.moveToFirst()) {
                            long contactId = cursor.getLong(PHONE_LOOKUP_CONTACT_ID_COLUMN_INDEX);
                            String lookupKey = cursor.getString(
                                    PHONE_LOOKUP_CONTACT_LOOKUP_KEY_COLUMN_INDEX);
                            bindFromContactUriInternal(Contacts.getLookupUri(contactId, lookupKey),
                                    false /* don't reset query handler */);
                        } else {
                            String phoneNumber = (String) cookie;
                            setDisplayName(phoneNumber, null);
                            setSocialSnippet(null);
                            setPhoto(loadPlaceholderPhoto(null));
                            //mPhotoView.assignContactFromPhone(phoneNumber, true);
                        }
                        break;
                    }
                    case TOKEN_EMAIL_LOOKUP: {
                        if (cursor != null && cursor.moveToFirst()) {
                            long contactId = cursor.getLong(EMAIL_LOOKUP_CONTACT_ID_COLUMN_INDEX);
                            String lookupKey = cursor.getString(
                                    EMAIL_LOOKUP_CONTACT_LOOKUP_KEY_COLUMN_INDEX);
                            bindFromContactUriInternal(Contacts.getLookupUri(contactId, lookupKey),
                                    false /* don't reset query handler */);
                        } else {
                            String emailAddress = (String) cookie;
                            setDisplayName(emailAddress, null);
                            setSocialSnippet(null);
                            setPhoto(loadPlaceholderPhoto(null));
                            //mPhotoView.assignContactFromEmail(emailAddress, true);
                        }
                        break;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    /**
     * @hide
     */
    public void setSelectedContactsAppTabIndex(int value) {
        //mPhotoView.setSelectedContactsAppTabIndex(value);
    }

    public void setNoPhotoResource(int photoRes) {
        mNoPhotoResource = photoRes;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    /**
     * Turn on/off showing of the add_to_contacts element.
     */
    public void showAddToContacts(boolean show) {
        HideAllActionIcon();
        mAddToContactsView.setVisibility(show? View.VISIBLE : View.GONE);
    }

    /**
     * Turn on/off showing of the edit_contact element.
     */
    public void showEditContact(boolean show) {
        HideAllActionIcon();
        
		if (mImsState == DetailEntry.ACTION_YILIAO_ONLINE_IMAGE_ONLINE) {
			setImessageStatus(true);
		}else if (mImsState == DetailEntry.ACTION_YILIAO_ONLINE_IMAGE_NO_ONLINE) {
			setImessageStatus(false);
		}else {
			setImessageHide();
		}
		
        mEditContactView.setVisibility(show? View.VISIBLE : View.GONE);
    }
    
    public void showInviteFriend(boolean show) {
        HideAllActionIcon();
        mInviteFriendView.setVisibility(show? View.VISIBLE : View.GONE);
    }
    
    public void showGroupChat(boolean show) {
        HideAllActionIcon();
        mGroupChatView.setVisibility(show? View.VISIBLE : View.GONE);
    }
    
    public void  HideAllActionIcon() {
        mAddToContactsView.setVisibility(View.GONE);
        mEditContactView.setVisibility(View.GONE);
        mInviteFriendView.setVisibility(View.GONE);
        mGroupChatView.setVisibility(View.GONE);
    }
    /**
     * Turn on/off showing of the star element.
     */
    public void showStar(boolean showStar) {
        mStarredView.setVisibility(showStar ? View.VISIBLE : View.GONE);
    }

    /**
     * Manually set the starred state of this header widget. This doesn't change
     * the underlying {@link Contacts} value, only the UI state.
     */
    public void setStared(boolean starred) {
        mStarredView.setChecked(starred);
    }

    /**
     * Manually set the presence.
     */
    public void setPresence(int presence) {
    }

    /**
     * Manually set the contact uri
     */
    public void setContactUri(Uri uri) {
        setContactUri(uri, true);
    }

    /**
     * Manually set the contact uri
     */
    public void setContactUri(Uri uri, boolean sendToFastrack) {
        mContactUri = uri;
        if (sendToFastrack) {
            //mPhotoView.assignContactUri(uri);
        }
    }
    
    public void setUserUri(Uri uri) {
        mUserUri = uri;
    }
    
    /**
     * Manually set the photo to display in the header. This doesn't change the
     * underlying {@link Contacts}, only the UI state.
     */
    public void setPhoto(Drawable drawable) {
        mPhotoView.setBackgroundDrawable(drawable);
    }
    
    public void setPhoto(Bitmap bitmap) {
        BitmapDrawable bd = new BitmapDrawable(bitmap);
        mPhotoView.setBackgroundDrawable(bd);
    }
    
    public void setPhoto(int resId) {
        mPhotoView.setBackgroundResource(resId);
    }
    
    public void setPhotoImageResource(int resId) {
        mPhotoView.setImageResource(resId);
    }
    
    public void setPhotoImageDrawable(Drawable drawable) {
        mPhotoView.setImageDrawable(drawable);
    }

    public void setImessageStatus(boolean bOnline) {
        mStatusView.setVisibility(View.VISIBLE);
        mStatusView.setImageResource(bOnline == true ? R.drawable.icon_contact_header_online : R.drawable.icon_contact_header_offline);
    }
    
    public void setImessageHide(){
    	mStatusView.setVisibility(View.GONE);
    }
    
    /**
     * Manually set the display name and phonetic name to show in the header.
     * This doesn't change the underlying {@link Contacts}, only the UI state.
     */
    public void setDisplayName(CharSequence displayName, CharSequence phoneticName) {
        mDisplayNameView.setText(displayName);
    }
    
    public void setDisplayName(int resId) {
        mDisplayNameView.setText(resId);
    }
    /**
     * Manually set the social snippet text to display in the header.
     */
    public void setSocialSnippet(CharSequence snippet) {
    }

    /**
     * Set a list of specific MIME-types to exclude and not display. For
     * example, this can be used to hide the {@link Contacts#CONTENT_ITEM_TYPE}
     * profile icon.
     */
    public void setExcludeMimes(String[] excludeMimes) {
        mExcludeMimes = excludeMimes;
        //mPhotoView.setExcludeMimes(excludeMimes);
    }

    /**
     * Convenience method for binding all available data from an existing
     * contact.
     *
     * @param contactLookupUri a {Contacts.CONTENT_LOOKUP_URI} style URI.
     */
    public void bindFromContactLookupUri(Uri contactLookupUri) {
        bindFromContactUriInternal(contactLookupUri, true /* reset query handler */);
    }

    /**
     * Convenience method for binding all available data from an existing
     * contact.
     *
     * @param contactUri a {Contacts.CONTENT_URI} style URI.
     * @param resetQueryHandler whether to use a new AsyncQueryHandler or not.
     */
    private void bindFromContactUriInternal(Uri contactUri, boolean resetQueryHandler) {
        mContactUri = contactUri;
        startContactQuery(contactUri, resetQueryHandler);
    }
    
    public void bindFromUserUri(Uri uri, boolean resetQueryHandler) {
        mUserUri = uri;
        startYiliaoPhotoQuery(resetQueryHandler);
    }
    
    /**
     * Convenience method for binding all available data from an existing
     * contact.
     *
     * @param emailAddress The email address used to do a reverse lookup in
     * the contacts database. If more than one contact contains this email
     * address, one of them will be chosen to bind to.
     */
    public void bindFromEmail(String emailAddress) {
        resetAsyncQueryHandler();

        mQueryHandler.startQuery(TOKEN_EMAIL_LOOKUP, emailAddress,
                Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(emailAddress)),
                EMAIL_LOOKUP_PROJECTION, null, null, null);
    }

    /**
     * Convenience method for binding all available data from an existing
     * contact.
     *
     * @param number The phone number used to do a reverse lookup in
     * the contacts database. If more than one contact contains this phone
     * number, one of them will be chosen to bind to.
     */
    public void bindFromPhoneNumber(String number) {
        resetAsyncQueryHandler();

        mQueryHandler.startQuery(TOKEN_PHONE_LOOKUP, number,
                Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)),
                PHONE_LOOKUP_PROJECTION, null, null, null);
    }

    /**
     * startContactQuery
     *
     * internal method to query contact by Uri.
     *
     * @param contactUri the contact uri
     * @param resetQueryHandler whether to use a new AsyncQueryHandler or not
     */
    private void startContactQuery(Uri contactUri, boolean resetQueryHandler) {
        if (resetQueryHandler) {
            resetAsyncQueryHandler();
        }

        mQueryHandler.startQuery(TOKEN_CONTACT_INFO, contactUri, contactUri, ContactQuery.COLUMNS,
                null, null, null);
    }

    /**
     * startPhotoQuery
     *
     * internal method to query contact photo by photo id and uri.
     *
     * @param photoId the photo id.
     * @param lookupKey the lookup uri.
     * @param resetQueryHandler whether to use a new AsyncQueryHandler or not.
     */
    protected void startPhotoQuery(long photoId, Uri lookupKey, boolean resetQueryHandler) {
        if (resetQueryHandler) {
            resetAsyncQueryHandler();
        }

        mQueryHandler.startQuery(TOKEN_PHOTO_QUERY, lookupKey,
                ContentUris.withAppendedId(Data.CONTENT_URI, photoId), PhotoQuery.COLUMNS,
                null, null, null);
    }

    private void startSpecialNumberPhotoQuery(boolean resetQueryHandler) {
        if (TextUtils.isEmpty(mPhoneNumber)) {
            mQueryHandler.onQueryComplete(TOKEN_SPECIAL_NUMBER_PHOTO_QUERY, null, null);
            return;
        }
        
        if (resetQueryHandler) {
            resetAsyncQueryHandler();
        }

        mQueryHandler.startQuery(
                TOKEN_SPECIAL_NUMBER_PHOTO_QUERY,
                null,
                Uri.parse("content://com.lewa.providers.location/special_phone"),
                new String[] {"logo"},
                "number='" + mPhoneNumber + "'",
                null,
                null);
    }

    private void startYiliaoPhotoQuery(boolean resetQueryHandler) {
        if (resetQueryHandler) {
            resetAsyncQueryHandler();
        }

        mQueryHandler.startQuery(TOKEN_YILIAO_PHOTO_QUERY, null, mUserUri, new String[]{User.PHOTO}, null, null, null);
    }
    
    /**
     * Method to force this widget to forget everything it knows about the contact.
     * We need to stop any existing async queries for phone, email, contact, and photos.
     */
    public void wipeClean() {
        resetAsyncQueryHandler();

        setDisplayName(null, null);
        setPhoto(loadPlaceholderPhoto(null));
        setSocialSnippet(null);
        setPresence(0);
        mContactUri = null;
        mExcludeMimes = null;
    }


    private void resetAsyncQueryHandler() {
        // the api AsyncQueryHandler.cancelOperation() doesn't really work. Since we really
        // need the old async queries to be cancelled, let's do it the hard way.
        mQueryHandler = new QueryHandler(mContentResolver);
    }

    /**
     * Bind the contact details provided by the given {@link Cursor}.
     */
    protected void bindContactInfo(Cursor c) {
        final String displayName = c.getString(ContactQuery.DISPLAY_NAME);
        final String phoneticName = c.getString(ContactQuery.PHONETIC_NAME);
        this.setDisplayName(displayName, phoneticName);

        final boolean starred = c.getInt(ContactQuery.STARRED) != 0;
        mStarredView.setChecked(starred);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_to_contacts: {
                performAddToContactsClick();
                break;
            }

            case R.id.edit_contact: {
                performEditContactClick();
                break;
            }
            case R.id.invite_friend: {
                performInviteFriendClick();
                break;
            }
            case R.id.group_chat: {
                performGroupChatClick();
                break;
            }
            case R.id.star: {
                // Toggle "starred" state
                // Make sure there is a contact
                if (mContactUri != null) {
                    final ContentValues values = new ContentValues(1);
                    values.put(Contacts.STARRED, mStarredView.isChecked());
                    mContentResolver.update(mContactUri, values, null, null);
                }
                break;
            }
            case R.id.photo: {
                performPhotoClick();
                break;
            }
            case R.id.name: {
                performDisplayNameClick();
                break;
            }
        }
    }

    private Drawable loadPlaceholderPhoto(BitmapFactory.Options options) {
        if (mNoPhotoResource == 0) {
            return null;
        }
        return new BitmapDrawable(BitmapFactory.decodeResource(mContext.getResources(), mNoPhotoResource, options));
    }
    
    public static void setImsState(int state){
    	mImsState = state;
    }
}