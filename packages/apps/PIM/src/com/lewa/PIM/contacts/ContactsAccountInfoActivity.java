package com.lewa.PIM.contacts;

import java.util.ArrayList;

import com.lewa.PIM.R;
import com.lewa.PIM.contacts.ViewContactActivity.ViewCache;
import com.lewa.PIM.contacts.ViewContactActivity.ViewEntry;
import com.lewa.PIM.widget.ContactHeaderWidget;
import com.lewa.os.util.LocationUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.User;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ContactsAccountInfoActivity extends Activity implements ContactHeaderWidget.ContactHeaderListener, OnClickListener{
    
//    private ImageView mEditAccountView;
    protected ContactHeaderWidget mContactHeaderWidget;
    private ContentResolver mResolver;
    private ViewAdapter mAdapter;
    private ListView mListView;
    private View mEmptyView;
    private Button mAddButton;
    
    private long mUserId;
    
    ArrayList<ViewEntry> mRemarkEntries = new ArrayList<ViewEntry>();
    ArrayList<ViewEntry> mYPhoneEntries = new ArrayList<ViewEntry>();
    ArrayList<ViewEntry> mYEmailEntries = new ArrayList<ViewEntry>();
    ArrayList<ArrayList<ViewEntry>> mSections = new ArrayList<ArrayList<ViewEntry>>();
    
    private static final String YILIAO_PHONE_CONTENT_ITEM_TYPE = "yiliao/phone";
    private static final String YILIAO_REMARK_CONTENT_ITEM_TYPE = "yiliao/remark";
    private static final String YILIAO_EMAIL_CONTENT_ITEM_TYPE = "yiliao/email";
    
    private static final boolean SHOW_SEPARATORS = false;
    private static final int EDIT_ACCOUNT_INFO = 10;
    
    @Override
    protected void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        
        setContentView(R.layout.contacts_account_info_content);
        mUserId = getIntent().getLongExtra("user_id", 0);
        
        mListView = (ListView) findViewById(R.id.account_data);
//        mEditAccountView = (ImageView)findViewById(R.id.image_account_edit);
//        mEditAccountView.setOnClickListener(this);
        mEmptyView = findViewById(R.id.account_empty);
        mAddButton = (Button)findViewById(R.id.btn_account_add);
        mAddButton.setOnClickListener(this);
        
        mContactHeaderWidget = (ContactHeaderWidget )findViewById(R.id.account_detail_header);
        mContactHeaderWidget.showEditContact(true);
        mContactHeaderWidget.setNoPhotoResource(R.drawable.ic_contact_picture);
        mContactHeaderWidget.setContactHeaderListener(this);
        
        mResolver = getContentResolver();        
        
        // Build the list of sections. The order they're added to mSections dictates the
        // order they are displayed in the list.
        mSections.add(mRemarkEntries);
        mSections.add(mYPhoneEntries);
        mSections.add(mYEmailEntries);

        buildYiliaoEntries();
    }

    private final void buildYiliaoEntries() {
        String text;
        
        if (mUserId == 0) {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.VISIBLE);
                mContactHeaderWidget.setDisplayName(R.string.yiliao_account_myself);
                mContactHeaderWidget.setPhotoImageResource(R.drawable.contact_head_imessage);
                mContactHeaderWidget.setPhoto(R.drawable.ic_contact_picture);
            }
            return;
        }
        mEmptyView.setVisibility(View.GONE);
        
        Cursor cursor = mResolver.query(User.CONTENT_URI, null, User._ID + "=" + mUserId, null, null);
        mContactHeaderWidget.bindFromUserUri(ContentUris.withAppendedId(User.CONTENT_URI, mUserId), true);
        try {
            if (cursor.moveToFirst()) {            
                text = cursor.getString(cursor.getColumnIndex(User.NICK_NAME));
                mContactHeaderWidget.setDisplayName(text, null);
                
                text = cursor.getString(cursor.getColumnIndex(User.REMARK));
                if (text != null && text.length() != 0) {
                    ViewEntry entryRemark = new ViewEntry();
                    entryRemark.data = text;
                    entryRemark.label = getString(R.string.yiliao_remark_label);
                    entryRemark.mimetype = YILIAO_REMARK_CONTENT_ITEM_TYPE;
                    entryRemark.maxLines = 1;
                    mRemarkEntries.add(entryRemark);
                    text = null;
                }
                
                text = cursor.getString(cursor.getColumnIndex(User.EMAIL));
                if (text != null && text.length() != 0) {
                    ViewEntry entryYEmail = new ViewEntry();
                    entryYEmail.data = text;
                    entryYEmail.label = getString(R.string.yiliao_email_label);
                    entryYEmail.mimetype = YILIAO_EMAIL_CONTENT_ITEM_TYPE;
                    entryYEmail.maxLines = 1;
                    mYEmailEntries.add(entryYEmail);
                    text = null;
                }
                
                text = cursor.getString(cursor.getColumnIndex(User.USER_ID));
                if (text != null && text.length() != 0) {
                    ViewEntry entryYPhone = new ViewEntry();
                    entryYPhone.data = text;
                    entryYPhone.label = getString(R.string.yiliao_phone_label);
                    entryYPhone.mimetype = YILIAO_PHONE_CONTENT_ITEM_TYPE;
                    entryYPhone.maxLines = 1;
                    mYPhoneEntries.add(entryYPhone);
                }
            }
        } finally {
            cursor.close();
            cursor = null;
        }
        
        if (mAdapter == null) {
            mAdapter = new ViewAdapter(this, mSections);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.setSections(mSections, SHOW_SEPARATORS);
        }
    }
    
    private final class ViewAdapter extends ContactEntryAdapter<ViewEntry>
            implements View.OnClickListener {

        ViewAdapter(Context context, ArrayList<ArrayList<ViewEntry>> sections) {
            super(context, sections, SHOW_SEPARATORS);
        }

        public void onClick(View v) {
            Intent intent = (Intent) v.getTag();
            startActivity(intent);
        }

        public int getCount() {
       
            return super.getCount();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {            
            ViewEntry entry = null;
            entry = getEntry(mSections, position, false);
            View v;
        
        // Cache the children
            ViewCache views = new ViewCache();
            
            // Create a new view if needed
            v = mInflater.inflate(R.layout.list_item_text_icons, parent, false);
        
            // Cache the children
            views.label = (TextView) v.findViewById(android.R.id.text2);
            views.data = (TextView) v.findViewById(android.R.id.text1);
            views.footer = (TextView) v.findViewById(R.id.footer);
            views.actionIcon = (ImageView) v.findViewById(R.id.action_icon);
            views.primaryIcon = (ImageView) v.findViewById(R.id.primary_icon);
            views.presenceIcon = (ImageView) v.findViewById(R.id.presence_icon);
            views.mmsSim_2 = (ImageView) v.findViewById(R.id.secondary_action_button);
            v.setTag(views);
        
            // Update the entry in the view cache
            views.entry = entry;
            views.position = position;
            
            // Bind the data to the view
            bindView(v, entry);
            return v;
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            // getView() handles this
            throw new UnsupportedOperationException();
        }

        @Override
        protected void bindView(View view, ViewEntry entry) {
            final Resources resources = mContext.getResources();
            ViewCache views = (ViewCache) view.getTag();
    
            // Set the label
            TextView label = views.label;
            setMaxLines(label, entry.maxLabelLines);
            label.setText(entry.label);
        
            // Set the data
            TextView data = views.data;
            if (data != null) {
                data.setText(entry.data);
                setMaxLines(data, entry.maxLines);
            }

            //set other control gone
            views.footer.setVisibility(View.GONE);
            views.actionIcon.setVisibility(View.INVISIBLE);
            views.primaryIcon.setVisibility(View.INVISIBLE);
            views.presenceIcon.setVisibility(View.INVISIBLE);
            views.mmsSim_2.setVisibility(View.INVISIBLE);
        }
    
        private void setMaxLines(TextView textView, int maxLines) {
            if (maxLines == 1) {
                textView.setSingleLine(true);
        
                //Modified by GanFeng 20120111, fix bug1853
                textView.setEllipsize((TextUtils.TruncateAt.MARQUEE)); //(TextUtils.TruncateAt.END);
                textView.setSelected(true);
            } else {
                textView.setSingleLine(false);
                textView.setMaxLines(maxLines);
                textView.setEllipsize(null);
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);
        
//        final MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.account_option_menu, menu);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId()) {
            case R.id.menu_switch_account: {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.menu_switch_account);
                alert.setMessage(R.string.menu_switch_account);
                alert.setNegativeButton(android.R.string.cancel, null);
                alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //
                }});
                alert.show();
                break;
            }
            
            case R.id.menu_change_passwd: {
                break;
            }
            
            default:
                break;
        }
        
        return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EDIT_ACCOUNT_INFO: {
                if (data != null) {
                    mUserId = data.getLongExtra("user_id", 0);
                }
                clearSections();
                buildYiliaoEntries();
                break; 
            }
            default:
                break;
        }
    }
    
    @Override
    public void onEditContactClick(View view) {
        Intent intent = new Intent();
        intent.setClass(this, ContactsAccountEditActivity.class);
        intent.putExtra("user_id", mUserId);
        startActivityForResult(intent, EDIT_ACCOUNT_INFO);
    }

    @Override
    public void onPhotoClick(View view) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDisplayNameClick(View view) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onAddToContactsClick(View view) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        
        clearSections();
    }
    
    private void clearSections() {
        final int numSections = mSections.size();
        for (int i = 0; i < numSections; i++) {
            mSections.get(i).clear();
        }
    }

    @Override
    public void onInviteFriendClick(View view) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onGroupChatClick(View view) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        int id = view.getId();
        switch (id) {
            case R.id.btn_account_add: {
                Intent intent = new Intent(this, ContactsAccountEditActivity.class);
                intent.putExtra("user_id", mUserId);
                startActivityForResult(intent, EDIT_ACCOUNT_INFO);
            }
            break;
        }
    }
}
