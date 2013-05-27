package com.lewa.PIM.contacts;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.RawContacts;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lewa.PIM.R;

import com.lewa.PIM.contacts.ContactHelper;
import com.lewa.PIM.contacts.ContactInfo;
import com.lewa.PIM.contacts.PageManager;

public class MergeActivity extends Activity implements OnClickListener, OnCheckedChangeListener {
    private Context mContext;
    private LinearLayout mContentArea;
    private Button mergeBtn, preBtn, nextBtn;
    private ArrayList<String> contactIds;
    private ContactInfo info;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.merge_contact_view);
        
        mContext = MergeActivity.this;
        contactIds = getIntent().getStringArrayListExtra("ids");
        mContentArea = (LinearLayout) findViewById(R.id.lin_merge);
        mergeBtn = (Button) findViewById(R.id.merge_btn);
        preBtn = (Button) findViewById(R.id.pre_btn);
        nextBtn = (Button) findViewById(R.id.next_btn);
        mergeBtn.setOnClickListener(this);
        preBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        
        if (!PageManager.isHasPre()) {
            preBtn.setEnabled(false);
        }
        
        if (!PageManager.isHasNext()) {
            nextBtn.setEnabled(false);
        }
        
        setupViews();
    }

    private void setupViews() {
        info = ContactHelper.getSimiliarContactInfo(mContext, contactIds);
        for (int j = 0; j < info.fieldNameMap.size(); j++) {
            int fieldSize = info.fieldNameMap.get(ContactInfo.FIELD[j]).size();
            if (fieldSize != 0) {
                boolean bUnique = false;
                if (ContactInfo.FIELD[j].equals(ContactInfo.DISPLAYNAME) || ContactInfo.FIELD[j].equals(ContactInfo.NICKNAME) 
                        || ContactInfo.FIELD[j].equals(ContactInfo.NOTE) || ContactInfo.FIELD[j].equals(ContactInfo.SIP)) {     // name, nickname, note, sip is unique
                    bUnique = true;
                }
                View titleView = genTitleView(ContactInfo.FIELD[j]);    // first add title view
                mContentArea.addView(titleView);
                LinearLayout layout = genLayout();
                ArrayList<String[]> fieldList = info.fieldNameMap.get(ContactInfo.FIELD[j]);
                for (int k = 0; k < fieldList.size(); k++) {    // then add the item view
                    String displayStr = buildDisplayStr(ContactInfo.FIELD[j], fieldList.get(k));
                    boolean bDefaultChecked = false;            // for unique items , the first is default checked
                    if (bUnique && k == 0) {
                        bDefaultChecked = true;
                    }
                    // setTag for indentify which button is checked
                    StringBuilder tag = new StringBuilder(ContactInfo.FIELD[j]).append("-").append(fieldList.size()).append("-").append(k);  
                    View itemView = bUnique ? genRadioView(displayStr, tag.toString(), bDefaultChecked) : genCheckView(displayStr, tag.toString());
                    layout.addView(itemView);
                    if (k != fieldList.size() - 1) {        // for the last one, do not add line
                        layout.addView(genLine());
                    }
                }
                mContentArea.addView(layout);       // scrollview - linearlayout - (linearlayout .... linearlayout)
            }
        }
    }

    private View genTitleView(String category) {
        View view = getLayoutInflater().inflate(R.layout.merge_title, null);
        TextView tvTitle = (TextView)view.findViewById(R.id.txt_title);
        if (category.equals(ContactInfo.DISPLAYNAME)) {
            tvTitle.setText(R.string.display_name);
        } else if (category.equals(ContactInfo.PHONE)) {
            tvTitle.setText(R.string.phone_number);
        } else if (category.equals(ContactInfo.EMAIL)) {
            tvTitle.setText(R.string.email_address);
        } else if (category.equals(ContactInfo.IM)) {
            tvTitle.setText(R.string.im_address);
        } else if (category.equals(ContactInfo.POSTAL)) {
            tvTitle.setText(R.string.postal_address);
        } else if (category.equals(ContactInfo.ORGNIZATION)) {
            tvTitle.setText(R.string.orgnization_address);
        } else if (category.equals(ContactInfo.NICKNAME)) {
            tvTitle.setText(R.string.nickname_info);
        } else if (category.equals(ContactInfo.WEBSITE)) {
            tvTitle.setText(R.string.website_url);
        } else if (category.equals(ContactInfo.SIP)) {
            tvTitle.setText(R.string.sip_address);
        }  else if (category.equals(ContactInfo.NOTE)) {
            tvTitle.setText(R.string.note_info);
        } 
        return view;
    }
    
    private String buildDisplayStr(String category, String[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String type = null;
         if (category.equals(ContactInfo.PHONE)) {
            type = getString(Phone.getTypeLabelResource(Integer.valueOf(data[0])));
        } else if (category.equals(ContactInfo.EMAIL)) {
            type = getString(Email.getTypeLabelResource(Integer.valueOf(data[0])));
        } else if (category.equals(ContactInfo.IM)) {
            type = getString(Im.getProtocolLabelResource(Integer.valueOf(data[0])));
        } else if (category.equals(ContactInfo.POSTAL)) {
            type = getString(StructuredPostal.getTypeLabelResource(Integer.valueOf(data[0])));
        } else if (category.equals(ContactInfo.ORGNIZATION)) {
            type = getString(Organization.getTypeLabelResource(Integer.valueOf(data[0])));
        }  
        
        if (data.length == 1) {
            sb.append(data[0]);
        } else if (data.length == 2) {
            sb.append(type).append(" :   ").append(data[1]);
        } else {
            sb.append(type).append(" :   ");
            for (int i = 1; i < data.length; i++) {
                sb.append(data[i].replace('\n', ',')).append("  ");
            }
        }
        return sb.toString();
    }
    
    private LinearLayout genLayout() {
        LinearLayout linearlayout = new LinearLayout(mContext);
        LayoutParams layoutparams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        linearlayout.setLayoutParams(layoutparams);
        linearlayout.setOrientation(LinearLayout.VERTICAL);
        return linearlayout;
    }
    
    private View genLine() {
        ImageView imageview = new ImageView(mContext);
        imageview.setImageResource(R.drawable.list_line);
        return imageview;
    }
    
    
    private View genCheckView(String data, String tag) {
        View view = getLayoutInflater().inflate(R.layout.merge_list_check, null); 
        TextView tvText = (TextView)view.findViewById(R.id.txt_text);
        CheckBox checkBox = (CheckBox)view.findViewById(R.id.merge_chcked);
        checkBox.setTag(tag);
        checkBox.setOnCheckedChangeListener(this);        
        tvText.setText(data);
        return view;
    }

    private View genRadioView(String data, String tag, boolean bChecked) {
        View view = getLayoutInflater().inflate(R.layout.merge_list_radio, null);
        TextView tvText = (TextView)view.findViewById(R.id.txt_displayname);
        RadioButton radioBtn = (RadioButton)view.findViewById(R.id.merge_img);
        radioBtn.setChecked(bChecked);
        radioBtn.setTag(tag);
        radioBtn.setOnCheckedChangeListener(this);
        tvText.setText(data);
        return view;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) 
        {
            case R.id.merge_btn:
            {
                new AlertDialog.Builder(mContext)
                .setTitle(R.string.merge)
                .setMessage(R.string.merge_sure)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!checkValidation(v)) {
                            mergeContacts(v);
                        } else {
                            Toast.makeText(mContext, R.string.invalid_merge, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
                break; 
            }            
            case R.id.next_btn:
            {
                Intent intent = new Intent(this, MergeActivity.class);
                intent.putExtra("ids", PageManager.getNextIds());
                startActivity(intent);
                finish();
                break;
            }
            
            case R.id.pre_btn:
            {
                Intent intent = new Intent(this, MergeActivity.class);
                intent.putExtra("ids", PageManager.getPreIds());
                startActivity(intent);
                finish();
                break; 
            }

            default:
                break;
        }      
    }
    
    private boolean checkValidation(View v) {   // Check if no item checked
        int totalSize = 0;
        int notCheckedSize = 0;
        for (int j = 1; j < info.fieldNameMap.size(); j++) {
            int fieldSize = info.fieldNameMap.get(ContactInfo.FIELD[j]).size();
            if (fieldSize != 0) {
                for (int k = fieldSize - 1; k >= 0; k--) {
                    String tag = ContactInfo.FIELD[j]+"-"+fieldSize+"-"+k;
                    View view = v.getRootView().findViewWithTag(tag);
                    if (view instanceof RadioButton) {
                        return false;
                    } else if (view instanceof CheckBox) {
                        if (!((CheckBox)view).isChecked()) {
                            notCheckedSize++;
                        }
                    }
                }
            }
            totalSize += fieldSize;
        }
        if (totalSize != 0 && totalSize == notCheckedSize) {
            return true;
        }
        return false;
    }
    
    private void mergeContacts(View v) {
        // Collect the unselected item
        for (int j = 0; j < info.fieldNameMap.size(); j++) {
            int fieldSize = info.fieldNameMap.get(ContactInfo.FIELD[j]).size();
            if (fieldSize != 0) {
                ArrayList<String[]> fieldList = info.fieldNameMap.get(ContactInfo.FIELD[j]);
                for (int k = fieldSize - 1; k >= 0; k--) {
                    String tag = ContactInfo.FIELD[j]+"-"+fieldSize+"-"+k;
                    View view = v.getRootView().findViewWithTag(tag);
                    if (view instanceof CompoundButton) {
                        if (!((CompoundButton)view).isChecked()) {
                            fieldList.remove(k);
                        }
                    }
                }
            }
        }
        
        // insert new contact
        ProgressDialog dialog = ProgressDialog.show(mContext, getString(R.string.please_wait), getString(R.string.merge_doing), true, false);
        try {
            ContactHelper.insertContact(mContext, null, null, info);
        } catch (Exception e) {
            dialog.dismiss();
            dialog = null;
            Toast.makeText(mContext, R.string.merge_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // delete old contacts
        if (contactIds.size() != 0) {
            String strContactId = ContactHelper.combineIds(contactIds);
            //modify by zenghuaying fix bug #9009
            getContentResolver().delete(RawContacts.CONTENT_URI, RawContacts._ID + " in (" + strContactId + ")", null);
        }
        PageManager.updatePageNo();
        dialog.dismiss();
        dialog = null;
        Toast.makeText(mContext, R.string.merge_success, Toast.LENGTH_SHORT).show();
        
        // go to next
        if (PageManager.getTotalCount() > 0) {
            Intent intent = new Intent(this, MergeActivity.class);
            intent.putExtra("ids", PageManager.getFirstIds());
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String tag = (String) buttonView.getTag();
        String group = null;
        int fieldSize = -1;
        int fieldPos = -1;
        if (tag != null && !tag.isEmpty()) {
            int firstIndex = tag.indexOf("-");
            int lastIndex = tag.lastIndexOf("-");
            if (firstIndex != -1 && lastIndex != -1) {
                group = tag.substring(0, firstIndex);
                fieldSize = Integer.valueOf(tag.substring(firstIndex+1, lastIndex));
                fieldPos = Integer.valueOf(tag.substring(lastIndex+1, tag.length()));
            }
        }
        
        if (buttonView instanceof RadioButton) {
            if (isChecked) {
                View v = buttonView.getRootView();
                for (int i = 0; i < fieldSize; i++) {
                    if (i != fieldPos) {
                        String newTag = group+"-"+fieldSize+"-"+i;
                        ((RadioButton)(v.findViewWithTag(newTag))).setChecked(false);
                    }
                }
            }
        }
    }
}