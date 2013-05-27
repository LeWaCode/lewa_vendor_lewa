package com.lewa.PIM.mms.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import junit.framework.Test;
import android.R.bool;
import android.R.integer;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Telephony.Mms;
import android.support.lewa.widget.CursorAdapter;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import com.lewa.PIM.R;
import com.lewa.PIM.mms.data.Contact;
import com.lewa.PIM.mms.data.ContactList;
import com.lewa.PIM.mms.data.WorkingMessage;
public class RecipientEdit extends LinearLayout implements 
                                    OnClickListener,
                                    OnFocusChangeListener{
    private Context mContext;
    private LayoutInflater mFactory;
    private RecipientsAdapter mRecipientsadapter = null;
    private RowLayout.LayoutParams  mLayoutparams = null;
    private NewMessageComposeActivity mComposeActivity = null;
        
    private ListView            mListView;
    private RowLayout           mRowLayout; 
    private ImageView           mSearchButton;
    private ImageView           mAddSearchButton;    
    private TextView            mRecipientShow;
    private LinearLayout        mRecipientList;
    private EditText            mRecipientsEditor;
    private LinearLayout        mRecipientAddContact;
    private ScrollView          mRecipientRowsScroller;
    private FrameLayout 		mRecipientListLayout;
    
    public static final int INPUT_MOD = 0;
    public static final int ADD_CONTACTS_MOD = 1;
    
    private int mRecipientScrollerHeight;
    
    public static final int CONTACT_ID_INDEX = 1;
    public static final int TYPE_INDEX       = 2;
    public static final int NUMBER_INDEX     = 3;
    public static final int LABEL_INDEX      = 4;
    public static final int NAME_INDEX       = 5;
    
    public static boolean mIsHdip = true;
    
    public RecipientEdit(Context context) {
        super(context);
        mContext = context;
    }

    public RecipientEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    
    public void setNewMessageComposeActivity(NewMessageComposeActivity activity){
        mComposeActivity = activity;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mListView = (ListView)findViewById(R.id.suggestion_list);
        mSearchButton = (ImageView)findViewById(R.id.search_button);
        mAddSearchButton = (ImageView)findViewById(R.id.add_contact_search_button);
        mRecipientList = (LinearLayout)findViewById(R.id.recipient_list);
        mRecipientShow = (TextView)findViewById(R.id.recipient_show);
        mRecipientsEditor = (EditText)findViewById(R.id.recipients_editor);
        mRecipientAddContact = (LinearLayout)findViewById(R.id.recipient_add_contact);
        mRecipientRowsScroller = (ScrollView)findViewById(R.id.recipient_rows_scroller);
        mRecipientListLayout = (FrameLayout)findViewById(R.id.recipient_list_layout);
        mRowLayout = (RowLayout)findViewById(R.id.recipient_rows);
        mRowLayout.setRecipientEdit(this);
        mRowLayout.setOnClickListener(mRecipientViewClickListener);
        
        mFactory = LayoutInflater.from(mContext);
        mLayoutparams = new RowLayout.LayoutParams(mLayoutparams.WRAP_CONTENT , mLayoutparams.WRAP_CONTENT);
        
        mRecipientsEditor.setOnFocusChangeListener(this);
        mRecipientsEditor.setOnKeyListener(mEditEnterHandler);
        mRecipientsEditor.setOnClickListener(this);
        mRecipientsEditor.addTextChangedListener(mTextEditorWatcher);
        mRecipientShow.setOnClickListener(this);
        
        if (mIsHdip) {
            mRecipientScrollerHeight = 60;			
		}else {
			mRecipientScrollerHeight = 30;
		}
 
        mRecipientsadapter = new RecipientsAdapter(mContext);
        mListView.setAdapter(mRecipientsadapter);
        mRecipientsadapter.setListView(mRecipientListLayout);
        mRecipientsadapter.mIsHdip = mIsHdip;
        mListView.setOnItemClickListener(mRecipientsItemClickListener);
        
        mSearchButton.setOnClickListener(mSearchOnClickListene);
        mAddSearchButton.setOnClickListener(mSearchOnClickListene);
    }
    
    private OnClickListener mSearchOnClickListene = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, ComposeSearchContactsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);            
        }
    };
    
    private OnItemClickListener mRecipientsItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cursor = (Cursor)mRecipientsadapter.getItem(position);
            String s = cursor.getString(NUMBER_INDEX);
            addRecipient(s);
            mRecipientsEditor.setText("");
        }
    };
    
    private TextWatcher mTextEditorWatcher = new TextWatcher(){

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            //mRecipientsadapter.changeCursor(searchRecipients(s));
        	if (TextUtils.isEmpty(s)) {
        		mRecipientListLayout.setVisibility(View.GONE);
			}else {
				mRecipientListLayout.setVisibility(View.VISIBLE);
			}
        	
            mRecipientsadapter.getFilter().filter(s);
            String text = s.toString();
            if (!TextUtils.isEmpty(text)) {
                String endStr = text.substring(text.length() - 1, text.length());                
                
                byte [] ch = endStr.getBytes();
                if (ch.length > 1) {
                    if (endStr.equals(" ") ||
                        endStr.equals("£»") ||
                        endStr.equals("£¬")) {
                        text = text.substring(0, text.length() - 1);
                        if (text.length() >0) {
                            addRecipient(text);                                                    
                        }
                    }                                        
                }else {                    
                    if (ch[0] == ' ' ||
                        ch[0] == ';' ||
                        ch[0] == '\'' ||                        
                        ch[0] == ',') {
                        text = text.substring(0, text.length() - 1);
                        if (text.length() >0) {
                            addRecipient(text);                                                    
                        }
                     }                    
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int counts) {
            mComposeActivity.updateWorkingMessage(getNumbers(), containsEmail());
        }        
    };    
    
    private View.OnKeyListener mEditEnterHandler = new View.OnKeyListener() {
        
        public boolean onKey(View v, int keyCode, KeyEvent event) {            
            String recipient = mRecipientsEditor.getText().toString();
            recipient = RecipientTextReplaceChar(recipient);
            
          if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
              if (!TextUtils.isEmpty(recipient)) {                                      
                  addRecipient(recipient);
              }
          }else if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
              
              if (TextUtils.isEmpty(recipient)) {
                  int childCount = mRowLayout.getChildCount() - 1;
                  int select = getScrollerSelectChildIndex();
                  
                  if (childCount > 0 && select == -1) {
                      delScrollerSelectChild(childCount - 1);
                  }else if(childCount > 0 && select >= 0) {
                      delScrollerSelectChild(select);                
                  }
              }else {                  
                  delScrollerSelectChild(getScrollerSelectChildIndex());                
              }
          }          
          return false;
        }
    };
    
    private OnClickListener mRecipientViewClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            clearScrollerChildState();
            
            if (RecipientView.class == v.getClass()) {
                RecipientView view = (RecipientView)v;
                view.setSelect(true);
                view.setBackgroundResource(R.drawable.recipient_view_bg_pressed);
                view.setTextColor(0xffffffff);                
            }

            InputMethodManager inputMethodManager =
                (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);            
                inputMethodManager.showSoftInput(mRecipientsEditor, InputMethodManager.SHOW_FORCED);
        }
    };
    
    private void clearScrollerChildState(){
        int childCount = mRowLayout.getChildCount() - 1;
        for (int i = 0; i < childCount; i++) {
            RecipientView view = (RecipientView)mRowLayout.getChildAt(i);
            view.setSelect(false);
            view.setBackgroundResource(R.drawable.recipient_view_bg_normal);
            view.setTextColor(0xff515151);
        }
    }
    
    private void delScrollerSelectChild(int index){
        int childCount = mRowLayout.getChildCount() - 1;               
        
        if (index >= 0 && index < childCount) {
            mRowLayout.removeViewAt(index);
        }
        mComposeActivity.updateWorkingMessage(getNumbers(), containsEmail());
        showHint(); 
        
        childCount = mRowLayout.getChildCount() - 1;          
        if (childCount == 1) {
			mComposeActivity.setVioceButtonState(View.VISIBLE);
		}else {
			mComposeActivity.setVioceButtonState(View.GONE);
		} 
    }
    
    private void delScrollerAllChild(){
        int childCount = mRowLayout.getChildCount() - 1;
        
        if (childCount > 0) {
            mRowLayout.removeViews(0, childCount);
        }
        showHint();
        mComposeActivity.setVioceButtonState(View.GONE);
        mComposeActivity.setHideRecentContacts(View.VISIBLE);
    }
    
    private int getScrollerSelectChildIndex(){
        int childCount = mRowLayout.getChildCount() - 1;
        int select = -1;
        
        for (int i = 0; i < childCount; i++) {
            RecipientView view = (RecipientView)mRowLayout.getChildAt(i);
            if (view.getSelect() == true) {
                select = i;
                break;
            }
        }
        return select;
    }
    
    private void showRecipientList(){
        List<String> list = getNames();
        String recipientList = mContext.getResources().getString(R.string.send);
        recipientList += " : ";
        
        if (list.size() > 0) {
            recipientList += TextUtils.join(",", list);
            mRecipientShow.setText(recipientList);
        }else{            
            mRecipientShow.setText(recipientList);
        }        
    }    
    
    private void showHint(){
        int childCount = mRowLayout.getChildCount() - 1;
        String recipient = mRecipientsEditor.getText().toString();
        recipient = RecipientTextReplaceChar(recipient);
        
        if (childCount > 0) {
            if (recipient.length() == 0) {
                mRecipientsEditor.setHint("");
            }
        }else {
            if (recipient.length() == 0) {
                String hintStr = getResources().getString(R.string.to_hint);
                mRecipientsEditor.setHint(hintStr);
            }
        }
    }
    
    private boolean isValidAddress(String number, boolean isMms) {
        if (isMms) {
            return MessageUtils.isValidMmsAddress(number);
        } else {
            // TODO: PhoneNumberUtils.isWellFormedSmsAddress() only check if the number is a valid
            // GSM SMS address. If the address contains a dialable char, it considers it a well
            // formed SMS addr. CDMA doesn't work that way and has a different parser for SMS
            // address (see CdmaSmsAddress.parse(String address)). We should definitely fix this!!!
            return PhoneNumberUtils.isWellFormedSmsAddress(number)
                    || Mms.isEmailAddress(number);
        }
    }

    public boolean hasValidRecipient(boolean isMms) {
        
        int childCount = mRowLayout.getChildCount() - 1;        
        for (int i = 0; i < childCount; i++) {
            RecipientView textview = (RecipientView)mRowLayout.getChildAt(i);
            Contact contact = textview.getContact();
            if (isValidAddress(contact.getNumber(), isMms)){
                return true;                    
            }
        }
        return false;
    }
    
    public void setScrollerLayout(int widths){                
        int maxHeight = mRecipientScrollerHeight * 2;
        
        ViewGroup.LayoutParams params = mRecipientRowsScroller.getLayoutParams();
        
        int line = -1;
        
        if (widths > 0) {
            line = widths / mRecipientRowsScroller.getWidth();            
        }
        
        if (line > 2) {
            params.height = maxHeight;
            mRecipientRowsScroller.setLayoutParams(params);                        
        }else {
            params.height = mLayoutparams.WRAP_CONTENT;
            mRecipientRowsScroller.setLayoutParams(params);            
        }
    }
    
    public RecipientsAdapter getAdapter(){
        return mRecipientsadapter;
    }
    
    public void addStringForList(String list){
        String[] strings = list.split(",");
        int count = strings.length;
        delScrollerAllChild();
        for (int i = 0; i < count; i++) {
            addRecipient(strings[i]);
        }
    }
    
    public boolean hasText(){
        boolean isb = false;
        if (!TextUtils.isEmpty(mRecipientsEditor.getText())) {
            String text = mRecipientsEditor.getText().toString();
            text = RecipientTextReplaceChar(text);
            
            if (!TextUtils.isEmpty(text)) {
                isb = true;                
            }
        }        
        return isb;
    }
    
    public int getRecipientCount(){
        int count = mRowLayout.getChildCount() - 1;
        return count;
    }
    
    public void addRecipient(){
        addRecipient(mRecipientsEditor.getText().toString());
    }
    
    public void addRecipient(ContactList contacts){
        int size = contacts.size();
        
        for (int i = 0; i < size; i++){
            addRecipient(contacts.get(i));
        }                
    }    
    
    public void addRecipient(String recipient){
        if (TextUtils.isEmpty(recipient)){
            return;
        }
        
        recipient = RecipientTextReplaceChar(recipient);
        if (!TextUtils.isEmpty(recipient)) {
            Contact contact = Contact.get(recipient, true);
            addRecipient(contact);
        }
    }
    
    public void addRecipient(Contact c){
            RecipientView textview = (RecipientView)mFactory.inflate(R.layout.recipient_view, null, false);            
            String recipient = c.getName();
            textview.setText(recipient);
            textview.setContact(c);
            textview.setOnClickListener(mRecipientViewClickListener);
            RowLayout.LayoutParams layoutparams = new RowLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            int index = mRowLayout.getChildCount() - 1;
            mRowLayout.addView(textview, index, layoutparams);
            mRecipientsEditor.setText("");
            mComposeActivity.updateWorkingMessage(getNumbers(), containsEmail());
            showHint();
            
            int childCount = mRowLayout.getChildCount() - 1;
            if (childCount == 1) {
				mComposeActivity.setVioceButtonState(View.VISIBLE);
			}else {
				mComposeActivity.setVioceButtonState(View.GONE);
			}
    }
    
    public boolean containsEmail(){
        boolean hasEmail = false;
        int childCount = mRowLayout.getChildCount() - 1;
        
        for (int i = 0; i < childCount; i++) {
            RecipientView textview = (RecipientView)mRowLayout.getChildAt(i);
            if (textview.getContact().isEmail()) {
                hasEmail = true;
                break;
            }
        }
        return hasEmail;
    }    
    
    public ContactList constructContactsFromInput() {
        int childCount = mRowLayout.getChildCount() - 1;        
        ContactList list = new ContactList();
        for (int i = 0; i < childCount; i++) {
            RecipientView textview = (RecipientView)mRowLayout.getChildAt(i);            
            Contact contact = textview.getContact();
            list.add(contact);            
        }
        return list;
    }
    
    public List<String> getNames(){
        List<String> list = new ArrayList<String>();        
        int childCount = mRowLayout.getChildCount() - 1;
        
        for (int i = 0; i < childCount; i++) {
            RecipientView textview = (RecipientView)mRowLayout.getChildAt(i);
            list.add(textview.getContact().getName());            
        }
        return list;
    }
    
    public List<String> getNumbers(){
        List<String> list = new ArrayList<String>();        
        int childCount = mRowLayout.getChildCount() - 1;
        
        for (int i = 0; i < childCount; i++) {
            RecipientView textview = (RecipientView)mRowLayout.getChildAt(i);
            String sNumber = textview.getContact().getNumber();
            if (!TextUtils.isEmpty(sNumber)) {
            	String string = MessageUtils.fixPhoneNumber(textview.getContact().getNumber()); 
                list.add(string);					
			}
        }
        
        if (!TextUtils.isEmpty(mRecipientsEditor.getText())) {
            String text = mRecipientsEditor.getText().toString();
            text = RecipientTextReplaceChar(text);
            if (!TextUtils.isEmpty(text)) {
                list.add(text);                
            }
        } 
        return list;
    }
    
    public void setMod(int mod){
        if (mod == INPUT_MOD) {
            mRecipientAddContact.setVisibility(View.GONE);
            mRecipientList.setVisibility(View.VISIBLE);
            mRecipientsEditor.setFocusable(true);
            mRecipientsEditor.setFocusableInTouchMode(true);
            mRecipientsEditor.requestFocus();
            InputMethodManager inputMethodManager =
                (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);            
                inputMethodManager.showSoftInput(mRecipientsEditor, InputMethodManager.SHOW_FORCED);
        }else{
            mRecipientAddContact.setVisibility(View.VISIBLE);
            mRecipientList.setVisibility(View.GONE);
            String recipient = mRecipientsEditor.getText().toString();
            addRecipient(recipient);
            showRecipientList();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        mComposeActivity.hideAddAttachmentPanel();
        switch (id) {
        case R.id.recipient_show: {
            setMod(INPUT_MOD);
        }
            break;
            
        case R.id.recipients_editor:{
            clearScrollerChildState();
            InputMethodManager inputMethodManager =
                (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);            
                inputMethodManager.showSoftInput(mRecipientsEditor, InputMethodManager.SHOW_FORCED);                               
        }
            break;
            
        default:
            break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (true == hasFocus) {
            setMod(INPUT_MOD);
            mComposeActivity.hideAddAttachmentPanel();
        }else {
            setMod(ADD_CONTACTS_MOD);
        }
    }
    
    private String RecipientTextReplaceChar(String old){
    	String newString = null;
    	
    	newString = old.replaceAll("\n", "");
    	newString = old.replaceAll("'", "");
    	return newString;
    }
}
