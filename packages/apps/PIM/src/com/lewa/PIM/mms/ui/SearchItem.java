package com.lewa.PIM.mms.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Telephony.Threads;
import android.provider.Telephony.Sms.Conversations;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.android.internal.telephony.CallerInfo;
import com.lewa.PIM.R;
import com.lewa.PIM.contacts.LayoutQuickContactBadge;
import com.lewa.PIM.mms.data.Contact;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.os.util.ContactPhotoLoader;
import com.lewa.os.util.LocationUtil;

public class SearchItem extends LinearLayout {
    private TextView mTitle;
    private TextView mSnippet;
    private QuickContactBadge mPhoto;
    private ImageView mImsImageView;
    
    private String mNumber = "";
    private long mThreadId = 0;
    private long mRowid = 0;
    private boolean mIsGroup = false;
    
    private Context mSearchContext;
    private ContactPhotoLoader mPhotoLoader;
    
    static private Drawable sDefaultContactImage;
    
    public SearchItem(Context context) {
        super(context);
    }

    public SearchItem(Context context, AttributeSet attrs) {
        super(context, attrs);  
        if (sDefaultContactImage == null) {
            sDefaultContactImage = context.getResources().getDrawable(R.drawable.ic_contact_picture);
        }         
    }

    public SearchItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
    } 
    
    public void setPhotoLoader(ContactPhotoLoader photoLade){
        mPhotoLoader = photoLade;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mTitle = (TextView)findViewById(R.id.title);
        mSnippet = (TextView)findViewById(R.id.subtitle);
        mPhoto = (QuickContactBadge)findViewById(R.id.contact_item_photo);
        mImsImageView = (ImageView)findViewById(R.id.contact_item_yl_image);
    }
    
    private static final String[] ALL_THREADS_PROJECTION = {
        Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
        Threads.SNIPPET, Threads.SNIPPET_CHARSET, Threads.READ, Threads.ERROR,
        Threads.HAS_ATTACHMENT
    };   
    
    private static final Uri sAllThreadsUri =
        Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
    

    private boolean isGroupThread(int thread){
        boolean isGroup = false;

        Cursor cursor = getContext().getContentResolver().query(
                sAllThreadsUri,
                ALL_THREADS_PROJECTION,
                "_id = '" + thread +"'",
                null,
                Conversations.DEFAULT_SORT_ORDER);
        
        if (cursor.moveToFirst()) {
            int recipientId = cursor.getColumnIndex(Threads.RECIPIENT_IDS);
            String recipientString = cursor.getString(recipientId);
            String[] list = recipientString.split(" ");
            isGroup = list.length > 1 ? true : false;
        }
        
        cursor.close();
        
        return isGroup;
    }
    
    public void bindData(Cursor cursor, String searchString, Context searchContext){
        
        int threadIdPos = cursor.getColumnIndex("thread_id");
        int addressPos  = cursor.getColumnIndex("address");
        int bodyPos     = cursor.getColumnIndex("body");
        int rowidPos    = cursor.getColumnIndex("_id");  
        mSearchContext = searchContext;        
        String address = cursor.getString(addressPos);
        Contact contact = address != null ? Contact.get(address, false) : null;
        
        String titleString = contact != null ? contact.getName() : "";
        String bodyString = cursor.getString(bodyPos);
        
        if (contact == null) {
            String sLocation = CommonMethod.getSpecialPhone(address);
            if (!TextUtils.isEmpty(sLocation)) {
                titleString = sLocation;
            }
            
        }else if (!contact.existsInDatabase()){
            String sLocation = CommonMethod.getSpecialPhone(address);
            if (!TextUtils.isEmpty(sLocation)) {
                titleString = sLocation;
            }
        }
        
        if (titleString.equals(CallerInfo.UNKNOWN_NUMBER)) {
            titleString = getResources().getString(R.string.unknown);
        }
        else if (titleString.equals(CallerInfo.PRIVATE_NUMBER)) {
            titleString = getResources().getString(R.string.private_num);
        }
        else if (titleString.equals(CallerInfo.PAYPHONE_NUMBER)) {
            titleString = getResources().getString(R.string.payphone);
        }
        
        int pos = bodyString.indexOf(searchString);
        
        if (bodyString.length() > 10 && pos > 3) {
            bodyString = "..." + bodyString.substring(bodyString.indexOf(searchString));
            pos = 3;
        }
        mTitle.setText(titleString);
        
        if (pos == -1) {
            mSnippet.setText(bodyString);
        }else {
            SpannableStringBuilder style=new SpannableStringBuilder(bodyString);
            //style.setSpan(new BackgroundColorSpan(Color.RED),0,4,Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            style.setSpan(new ForegroundColorSpan(0xff007aff),pos,searchString.length() + pos,Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            mSnippet.setText(style);                
        }
        //snippet.setText(bodyString);
        //snippet.setText(cursor.getString(bodyPos), searchString);
        mNumber = address;
        mThreadId = cursor.getLong(threadIdPos);
        mRowid = cursor.getLong(rowidPos);        
        mIsGroup = isGroupThread((int)mThreadId);
        
        updateAvatarView(contact, address);
        
        boolean isIms = ((SearchMessageActivity)searchContext).isImsUser(mNumber);
        if (isIms) {
        	mImsImageView.setVisibility(View.GONE);
		}else {
			mImsImageView.setVisibility(View.GONE);
		}
              
        this.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mIsGroup) {
                    final Intent onClickIntent = NewMessageComposeActivity.createIntent(mContext, mThreadId);
                    getContext().startActivity(onClickIntent);
                    
                }else {
                    final Intent onClickIntent = ComposeMessageActivity.createIntent(mContext, mThreadId);
                    onClickIntent.putExtra("thread_id", mThreadId);
                    onClickIntent.putExtra("select_id", mRowid);
                    onClickIntent.putExtra("number", mNumber);
                    getContext().startActivity(onClickIntent);                        
                }
            }
        }); 
    }

    private void updateAvatarView(Contact contact, String number) {

        Drawable avatarDrawable;
        if (!mIsGroup) {
            if (contact != null && contact.existsInDatabase()) {
                mPhoto.setOnClickListener(null);
                mPhoto.assignContactUri(contact.getUri());
                String tempNmber = MessageUtils.fixPhoneNumber(contact.getNumber());
                int photoId = ((SearchMessageActivity)mSearchContext).getPhotoIdToInt(tempNmber);
                mPhotoLoader.loadPhoto(mPhoto, photoId);
                
            } else {
                new LayoutQuickContactBadge.UnknownQCBOnClickListener(
                        mSearchContext,
                        number,
                        mPhoto);
                
                if (!TextUtils.isEmpty(number)) {
                    mPhotoLoader.loadSpecialPhoto(mPhoto, number);                    
                }else {
                    avatarDrawable = mSearchContext.getResources().getDrawable(R.drawable.ic_contact_header_unknow);
                    mPhoto.setImageDrawable(avatarDrawable);                    
                }
            }
        } else {
            // TODO get a multiple recipients asset (or do something else)
            avatarDrawable = mSearchContext.getResources().getDrawable(R.drawable.ic_contact_header_group);
            //mAvatarView.assignContactUri(null);
            mPhoto.setImageDrawable(avatarDrawable);
            mPhoto.setOnClickListener(null);
        }
        mPhoto.setVisibility(View.VISIBLE);
    }    
};   
