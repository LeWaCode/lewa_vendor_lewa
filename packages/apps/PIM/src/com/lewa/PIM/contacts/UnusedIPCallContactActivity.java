package com.lewa.PIM.contacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lewa.PIM.R;
import com.lewa.PIM.calllog.view.CallLogListAdapter;
import com.lewa.PIM.mms.data.Contact;
import com.lewa.PIM.mms.ui.ComposeSearchContactsActivity;
import com.lewa.PIM.mms.ui.MessageUtils;
import com.lewa.PIM.mms.ui.NewMessageComposeActivity;
import com.lewa.PIM.mms.ui.SearchContactsItem;
import com.lewa.PIM.settings.IpCallSettingsActivity;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.os.util.ContactPhotoLoader;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.lewa.widget.CursorAdapter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class UnusedIPCallContactActivity extends Activity{
    private static final String TAG = "UnusedIPCallContactActivity";
    private static final int UNUSED_IP_NUMBERS = 137;
    private QuickContactBadge mPhoto;
    private static Drawable defaultContactImage;
    private LinearLayout emptyLayout;
    private TextView mName;
    private TextView mNumber;
    private GetContactsListAdapter contactsListAdapter;

    private Button addNumBtn;
    private ListView contactList;
    private ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String,Object>>();
    private String[] numbers;
    private SharedPreferences mPrefs;
    private ArrayList<String> numberList = new ArrayList<String>();
    private HashMap<String, String> modifyNameMap = new HashMap<String, String>();
    private boolean isUnusedIpCallBeenSet = false;
    private static int RESULT_FOR_IP_SETTING = 200;
    private String numberStr;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.unused_ip_call_contact_list);

        addNumBtn = (Button)findViewById(R.id.add_number_btn);
        contactList = (ListView)findViewById(R.id.unused_ip_call_list);
        emptyLayout = (LinearLayout)findViewById(R.id.empty);

        if (defaultContactImage == null) {
            defaultContactImage = this.getResources().getDrawable(R.drawable.ic_contact_picture);
        }  

        mPrefs = getSharedPreferences("unused_ip_data", Context.MODE_PRIVATE);
        String numStr = mPrefs.getString("unused_ip_numbers", "");

        if(!TextUtils.isEmpty(numStr)){
            numbers = numStr.split(",");
            for(int i=0;i<numbers.length;i++){
                numberList.add(numbers[i]);
            }
            loadContactData(numberList);
            emptyLayout.setVisibility(View.GONE);
        }

        addNumBtn.setOnClickListener(new ButtonListener());
        contactList.setOnItemLongClickListener(new ListItemOnLongClick());

        registerForContextMenu(contactList);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfoIn) {
        // TODO Auto-generated method stub
        super.onCreateContextMenu(menu, v, menuInfoIn);

        AdapterView.AdapterContextMenuInfo menuInfo = null;
        try {
            menuInfo = (AdapterView.AdapterContextMenuInfo) menuInfoIn;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfoIn", e);
            return;
        }

        HashMap<String, Object> itemData = (HashMap<String, Object>)contactsListAdapter.getItem(menuInfo.position);
        String number = (String)itemData.get("item_number");

        menu.add(0, R.id.menu_call_contact, 0, R.string.call_contact);
        menu.add(0, R.id.menu_send_message, 1, R.string.send_message);
        menu.add(0, R.id.menu_edit, 2, R.string.ip_menu_modify);
        menu.add(0, R.id.menu_delete, 3, R.string.ip_menu_delete);
        menu.setHeaderTitle(number);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        AdapterView.AdapterContextMenuInfo menuInfo = null;
        try {
            menuInfo = (AdapterView.AdapterContextMenuInfo )item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfoIn", e);
            return super.onContextItemSelected(item);
        }

        HashMap<String, Object> itemData = (HashMap<String, Object>)contactsListAdapter.getItem(menuInfo.position);
        String name = (String)itemData.get("item_name");
        String number = (String)itemData.get("item_number");

        switch (item.getItemId()) {
        case R.id.menu_call_contact:
            CommonMethod.call(this, number);
            break;	
        case R.id.menu_send_message:
            CommonMethod.sendMessage(this, number, null);
            break;	
        case R.id.menu_edit:
            openContactNameEditor(number,name);
            break;	
        case R.id.menu_delete:
            openDeleteContactItem(number);
            break;	
        default:
            break;
        }		
        return super.onContextItemSelected(item);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Intent intent = new Intent();
            intent.putExtra("unused_ip_call_setting", isUnusedIpCallBeenSet);
            setResult(RESULT_FOR_IP_SETTING, intent);
        } 
        return super.onKeyDown(keyCode, event);
    }


    private void openContactNameEditor(final String number, final String initName) {
        View viewEditor = View.inflate(this, R.layout.edit_text, null);
        final EditText editContactName = (EditText)viewEditor.findViewById(R.id.edt_text_field);
        //editContactName.setInputType(InputType.TYPE_CLASS_PHONE);
        if (!TextUtils.isEmpty(initName)) {
            editContactName.setText(initName);
        } else {
            editContactName.setHint(R.string.contact_name_hint);
        }

        new AlertDialog.Builder(this)
        .setTitle(R.string.modify_description)
        .setView(viewEditor)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String contactName = editContactName.getText().toString().trim();
                if (!TextUtils.isEmpty(contactName)) {
                    if(!initName.equals(contactName)){
                        //modifyNameMap.put(number, contactName);
                        saveModifyName(number,contactName);
                        loadContactData(numberList);
                    }
                }
                else {
                    Toast.makeText(UnusedIPCallContactActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                }
            }
        })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        })
        .setCancelable(false)
        .show();
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        // TODO Auto-generated method stub
        if(id == R.id.dialog_delete_ip_contact){

            return new AlertDialog.Builder(this)
            .setTitle(R.string.deleteConfirmation_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(R.string.deleteConfirmation)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok,
                    new DeleteClickListener()).create();

        }

        return super.onCreateDialog(id, args);
    }

    private class DeleteClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            boolean isRemoveSuccess = numberList.remove(numberStr);
            int size = numberList.size();
            String number;
            StringBuffer numBuffer = new StringBuffer();
            Editor editor = mPrefs.edit();
            if (isRemoveSuccess) {
                if(size > 0){
                    for(int i=0;i<size;i++){
                        number = numberList.get(i);
                        numBuffer.append(number + ",");
                    }
                    String numStr = numBuffer.toString();
                    numStr = numBuffer.toString().substring(0, numStr.lastIndexOf(","));


                    //editor.clear().commit();
                    editor.putString("unused_ip_numbers", numStr).commit();

                }else{ //no numbers added,show empty message
                    editor.clear().commit();
                    isUnusedIpCallBeenSet = false;
                    emptyLayout.setVisibility(View.VISIBLE);
                }
                loadContactData(numberList);
            }
            else {
                Toast.makeText(UnusedIPCallContactActivity.this, R.string.contact_item_delete_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openDeleteContactItem(final String number) {
        numberStr = number;
        showDialog(R.id.dialog_delete_ip_contact);
        //        new AlertDialog.Builder(this)
        //                .setTitle(R.string.delete_description)
        //                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        //                    public void onClick(DialogInterface dialog, int which) {
        //                        boolean isRemoveSuccess = numberList.remove(number);
        //                        int size = numberList.size();
        //                        String number;
        //                        StringBuffer numBuffer = new StringBuffer();
        //                        Editor editor = mPrefs.edit();
        //                        if (isRemoveSuccess) {
        //                            if(size > 0){
        //                             for(int i=0;i<size;i++){
        //                                 number = numberList.get(i);
        //                                 numBuffer.append(number + ",");
        //                              }
        //                              String numStr = numBuffer.toString();
        //                              numStr = numBuffer.toString().substring(0, numStr.lastIndexOf(","));
        //                              //editor.clear().commit();
        //                              editor.putString("unused_ip_numbers", numStr).commit();
        //                        }else{ //no numbers added,show empty message
        //                              editor.clear().commit();
        //                              isUnusedIpCallBeenSet = false;
        //                              emptyLayout.setVisibility(View.VISIBLE);
        //                        }
        //                          loadContactData(numberList);
        //                        }
        //                        else {
        //                            Toast.makeText(UnusedIPCallContactActivity.this, R.string.contact_item_delete_error, Toast.LENGTH_SHORT).show();
        //                        }
        //                    }
        //                })
        //                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
        //                    public void onClick(DialogInterface dialog, int which) {
        //                        dialog.dismiss();
        //                    }
        //                })
        //                .setCancelable(false)
        //                .show();
    }

    private class ListItemOnLongClick implements OnItemLongClickListener{
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                int arg2, long arg3) {
            return false;
        }

    }

    private class ButtonListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            // TODO Auto-generated method stub
            if (view.getId() == R.id.add_number_btn) {
                Intent intent = new Intent(UnusedIPCallContactActivity.this,ComposeSearchContactsActivity.class);
                intent.putExtra("isForIpCall", true);
                intent.putStringArrayListExtra("numberList", numberList);
                startActivityForResult(intent, UNUSED_IP_NUMBERS);
            }
        }

    }

    private void saveModifyName(String number,String name){
        String modifyNameStr = mPrefs.getString("modify_name", "");
        if (!TextUtils.isEmpty(modifyNameStr)) {
            modifyNameStr = modifyNameStr +"," + number + "_" + name;
        }else{
            modifyNameStr = number + "_" + name;
        }
        //mPrefs.edit().clear();
        mPrefs.edit().putString("modify_name", modifyNameStr).commit();
    }

    private void loadModifyName(){
        String modifyNameResult = mPrefs.getString("modify_name", "");
        if (!TextUtils.isEmpty(modifyNameResult)) {
            String[] modifyNameStr = modifyNameResult.split(",");
            for (int i = 0; i < modifyNameStr.length; i++) {
                String[] numberAndName = modifyNameStr[i].split("_");
                modifyNameMap.put(numberAndName[0], numberAndName[1]);
            }
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 180) {
            ArrayList<String> numberArray = new ArrayList<String>();

            numberArray = data.getStringArrayListExtra("number_data");
            if(numberArray != null){
                loadContactData(numberArray);
                emptyLayout.setVisibility(View.GONE);
            }


        }
    }

    /**
     * load the newest contact data
     * (include user modify contactName(store in modifyNameMap))
     * @param numberArray
     */

    private void loadContactData(ArrayList<String> numberArray){
        Drawable avatarDrawable;
        Contact contact;
        //StringBuffer numBuffer = new StringBuffer();
        String number;
        String name;
        int size = numberArray.size();
        numberList = numberArray;
        listItem.clear();
        if(numberArray != null && size > 0){
            loadModifyName();
            for(int i=0;i<size;i++){
                number = numberArray.get(i);
                contact = Contact.get(number, true);
                if(modifyNameMap.containsKey(number)){
                    name = modifyNameMap.get(number);
                }else {
                    name = contact.getName();
                }

                avatarDrawable = contact.getAvatar(this, defaultContactImage);
                HashMap<String, Object> itemMap = new HashMap<String, Object>();

                itemMap.put("item_photo", avatarDrawable);//R.drawable.ic_contact_picture
                itemMap.put("item_name", name);
                itemMap.put("item_number", number);
                listItem.add(itemMap);
            }

            isUnusedIpCallBeenSet = true;
            //GetContactsListAdapter adapter = new GetContactsListAdapter(this, listItem);
            contactsListAdapter = new GetContactsListAdapter(this, listItem);
            contactList.setAdapter(contactsListAdapter);
        }
    }	


    public class GetContactsListAdapter extends BaseAdapter {
        private Context mContext;

        private ArrayList<HashMap<String, Object>> arrayList;

        public GetContactsListAdapter(Context mContext,ArrayList<HashMap<String, Object>> arrayList){
            this.mContext = mContext;
            this.arrayList = arrayList;
        }


        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return arrayList != null ? arrayList.size() : 0;
        }

        @Override
        public Object getItem(int pos) {
            // TODO Auto-generated method stub
            if((arrayList != null && arrayList.size() > 0) && (pos >= 0 && pos < arrayList.size())){
                return arrayList.get(pos);
            }
            return null;
        }

        @Override
        public long getItemId(int pos) {
            // TODO Auto-generated method stub
            // avoid parseLong throw NumberFormatexception by shenqi
            /*if((arrayList != null && arrayList.size() > 0) && (pos >= 0 && pos < arrayList.size())){
				return Long.parseLong((String)arrayList.get(pos).get("item_number"));
			}*/
            return pos;
        }

        @Override
        public View getView(int pos, View arg1, ViewGroup arg2) {
            // TODO Auto-generated method stub
            View layout = LayoutInflater.from(mContext).inflate(R.layout.unsed_ip_call_contact_item, null);
            mPhoto = (QuickContactBadge)layout.findViewById(R.id.contact_item_photo);;
            mName = (TextView) layout.findViewById(R.id.contact_item_name);
            mNumber = (TextView) layout.findViewById(R.id.contact_item_number);
            HashMap<String, Object> itemMap = arrayList.get(pos);
            if(arrayList != null && arrayList.size() > 0){
                mPhoto.setImageDrawable((Drawable)itemMap.get("item_photo"));
                mName.setText((String)itemMap.get("item_name"));
                mNumber.setText((String)itemMap.get("item_number"));

            }
            return layout;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
