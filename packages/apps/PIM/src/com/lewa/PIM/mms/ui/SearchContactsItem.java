package com.lewa.PIM.mms.ui;

import com.lewa.PIM.R;
import com.lewa.PIM.mms.ui.ComposeSearchContactsActivity.SearchContactsListAdapter;
import com.lewa.os.util.ImageUtil;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.RosterData;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SearchContactsItem extends RelativeLayout{
    private ImageView mContactItemPhoto;
    private TextView  mContactName;
    private TextView  mContactInfo;
    private CheckBox  mContactsListChoice;
    private ImageView mContactItemYlImage;
    
    private SearchContactsListAdapter mAdapter;
    
	public SearchContactsItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public SearchContactsItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SearchContactsItem(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();        
        mContactItemPhoto = (ImageView )findViewById(R.id.contact_item_photo);
        mContactName = (TextView )findViewById(R.id.contact_name);
        mContactInfo = (TextView )findViewById(R.id.contact_info);
        mContactsListChoice = (CheckBox)findViewById(R.id.contacts_list_choice);
        mContactsListChoice.setOnCheckedChangeListener(mCheckedListener);
        mContactsListChoice.setOnClickListener(mCheckOnClick);
        mContactItemYlImage = (ImageView )findViewById(R.id.contact_item_yl_image);
    }

    public void bind(Cursor cursor, SearchContactsListAdapter adapter){
    	String number = cursor.getString(3);
    	mContactName.setText(cursor.getString(4));
    	mContactInfo.setText(number);
    	mAdapter = adapter;
    	
    	if (adapter.isPhoneMode()) {			
			String ContactId = cursor.getInt(1) + "";
	    	int PhoneId = 0;
	    	
	    	if (TextUtils.isEmpty(ContactId)) {
	   		
			}else {
		    	mAdapter.getPhotoLoader().loadPhoto(mContactItemPhoto, PhoneId);			
		    	String phoneIdString = mAdapter.getPhoneId(ContactId);	    	
		    	if (!TextUtils.isEmpty(phoneIdString)) {
					PhoneId = Integer.parseInt(phoneIdString);
				}	    	
			}    	
	    	
	    	mAdapter.getPhotoLoader().loadPhoto(mContactItemPhoto, PhoneId);	
    	}else {
    		byte[] imageByte = cursor.getBlob(1);
    		
    		if (imageByte == null) {
    			mAdapter.getPhotoLoader().loadPhoto(mContactItemPhoto, 0);
			}else {
				Bitmap ylBitmap = ImageUtil.byteToBitmap(imageByte);
				mContactItemPhoto.setImageBitmap(ylBitmap);
			}
		}
    	
    	if (mAdapter.getSelectByKey(number)) {
    		mContactsListChoice.setChecked(true);
		}else {
    		mContactsListChoice.setChecked(false);			
		}       	    	
    	
    	if (adapter.isYlUser(number)) {
    		mContactItemYlImage.setVisibility(View.GONE);
		}else {
    		mContactItemYlImage.setVisibility(View.GONE);			
		}
    }    
    
    private OnCheckedChangeListener mCheckedListener = new OnCheckedChangeListener(){

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub
            mAdapter.setSelectByKey(mContactInfo.getText().toString(), isChecked);
        }        
    };
    
    private OnClickListener mCheckOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
            CheckBox check = (CheckBox)v.findViewById(R.id.contacts_list_choice);
            mAdapter.setSelectByKey(mContactInfo.getText().toString(), check.isChecked());
		}
    };
}
