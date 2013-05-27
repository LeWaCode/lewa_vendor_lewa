package com.lewa.PIM.contacts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.google.android.collect.Lists;
import com.lewa.PIM.R;
import com.lewa.PIM.contacts.ViewContactActivity.ViewEntry;
import com.lewa.PIM.contacts.model.Editor;
import com.lewa.PIM.contacts.model.Editor.EditorListener;
import com.lewa.PIM.contacts.ui.widget.PhotoEditorView;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.os.util.ImageUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.ContactsContract.User;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ContactsAccountEditActivity extends Activity implements OnClickListener{
    
    private static final int CAMERA_WITH_DATA = 1;
    private static final int PHOTO_PICKED_WITH_DATA = 2;
    private static final int EDIT_ACCOUNT_INFO = 10;
    
    private static final int ICON_SIZE = 96;    
    private static final File PHOTO_DIR = new File(
            Environment.getExternalStorageDirectory() + "/DCIM/Camera");
    
    private PhotoEditorView mCover;
    private Bitmap mPhoto;
    private long mUserId = 0;
    private String mNickName;
    private String mRemark;
    private String mEmail;
    private String mPhone;
    
    private ArrayList<Dialog> mManagedDialogs = Lists.newArrayList();
    private File mCurrentPhotoFile;
    private ContentResolver mResolver;
    private EditText mNameEdit;
    private EditText mRemarkEdit;
    private EditText mEmailEdit;
    private EditText mPhoneEdit;
    private TextView mRemarkLabel;
    private TextView mEmailLabel;
    private TextView mPhoneLabel;
    private Button mbtnDone;
    private Button mbtnCancel;

    
    @Override
    protected void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        
        mUserId = getIntent().getLongExtra("user_id", 0);
        
        setContentView(R.layout.contacts_account_info_content_edit);
        mResolver = getContentResolver();
        
        mCover = (PhotoEditorView)findViewById(R.id.edit_cover);
        mCover.setEditorListener(new PhotoListener(false, mCover)); 
        
        mNameEdit = (EditText)findViewById(R.id.edit_text_name);
        mNameEdit.addTextChangedListener(mTextWatcher);
        
        View view = findViewById(R.id.yiliao_remark);
        mRemarkLabel = (TextView)view.findViewById(R.id.text_label);
        mRemarkLabel.setText(R.string.yiliao_remark_label);
        mRemarkEdit = (EditText)view.findViewById(R.id.text_edit);
        mRemarkEdit.setHint(R.string.yiliao_remark_prompt);
        view.setVisibility(View.GONE);
        
        view = findViewById(R.id.yiliao_email);
        mEmailLabel = (TextView)view.findViewById(R.id.text_label);
        mEmailLabel.setText(R.string.yiliao_email_label);
        mEmailEdit = (EditText)view.findViewById(R.id.text_edit);
        
        view = findViewById(R.id.yiliao_phone);
        mPhoneLabel = (TextView)view.findViewById(R.id.text_label);
        mPhoneLabel.setText(R.string.yiliao_phone_label);
        mPhoneEdit = (EditText)view.findViewById(R.id.text_edit);
        
        mbtnDone = (Button)findViewById(R.id.btn_done);
        mbtnDone.setOnClickListener(this);
        mbtnCancel = (Button)findViewById(R.id.btn_discard);
        mbtnCancel.setOnClickListener(this);
        
        setupView();
        
        View layout = findViewById(R.id.account_info_edit_layout);
        layout.setFocusable(true);
        layout.setFocusableInTouchMode(true);
        layout.requestFocus();
    }
    
    private void setupView() {
        Cursor cursor;
        cursor = mResolver.query(User.CONTENT_URI, null, User._ID + "=" + mUserId, null, null);
        try {
            if (cursor.moveToFirst()) {            
                mNickName = cursor.getString(cursor.getColumnIndex(User.NICK_NAME));
                if (mNickName != null)
                    mNameEdit.setText(mNickName);
                mRemark = cursor.getString(cursor.getColumnIndex(User.REMARK));  
                if (mRemark != null) 
                    mRemarkEdit.setText(mRemark);
                mEmail = cursor.getString(cursor.getColumnIndex(User.EMAIL));  
                if (mEmail != null)
                    mEmailEdit.setText(mEmail);
                mPhone = cursor.getString(cursor.getColumnIndex(User.USER_ID));
                if (mPhone != null)
                    mPhoneEdit.setText(mPhone);                
                byte[] b = cursor.getBlob(cursor.getColumnIndex(User.PHOTO));
                if (b != null && b.length != 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length, null);
                    mPhoto = bitmap;
                    mCover.setImageBitmap(mPhoto);
                }
            }
        } finally {
            cursor.close();
            cursor = null;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (Dialog dialog : mManagedDialogs) {
            dismissDialog(dialog);
        }
    }
    
    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        int id = view.getId();
        
        switch (id) {
        case R.id.btn_done: {
                SaveAccountInfo();
                Intent intent = new Intent();
                intent.putExtra("user_id", mUserId);
                setResult(EDIT_ACCOUNT_INFO, intent);
                finish();
            }
            break;
        case R.id.btn_discard: {
                finish();
            }
            break;
        default:
            break;
        } 
    }
    
    TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // don't care about this one
        }
        
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String newText = mNameEdit.getText().toString();
            if (newText.trim().length() == 0) {
                mbtnDone.setEnabled(false);
            } else {
                mbtnDone.setEnabled(true);
            }
        };
        
        @Override
        public void afterTextChanged(Editable s) {
            // don't care about this one
        }
    };
    
    private void SaveAccountInfo() {
        ContentValues values = new ContentValues();
        
        if (mPhoto != null)
            values.put(User.PHOTO, ImageUtil.Bitmap2Bytes(mPhoto));
        values.put(User.NICK_NAME, mNameEdit.getText().toString());
        values.put(User.REMARK, mRemarkEdit.getText().toString());
        values.put(User.EMAIL, mEmailEdit.getText().toString());
        values.put(User.USER_ID, mPhoneEdit.getText().toString());
        if (mUserId != 0) {
            mResolver.update(User.CONTENT_URI, values, User._ID + "=" + mUserId, null);
        } else {
            Uri uri = mResolver.insert(User.CONTENT_URI, values);
            mUserId = ContentUris.parseId(uri);
        }
    }
    
    /**
     * Creates a dialog offering two options: take a photo or pick a photo from the gallery.
     */
    private Dialog createPickPhotoDialog() {        

        // Wrap our context to inflate list items using correct theme
        final Context dialogContext = new ContextThemeWrapper(this,
                android.R.style.Theme_Light);

        String[] choices;
        choices = new String[2];
        choices[0] = getString(R.string.take_photo);
        choices[1] = getString(R.string.pick_photo);
        final ListAdapter adapter = new ArrayAdapter<String>(dialogContext,
                android.R.layout.simple_list_item_1, choices);

        final AlertDialog.Builder builder = new AlertDialog.Builder(dialogContext);
        builder.setTitle(R.string.attachToContact);//jxli
        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch(which) {
                    case 0:
                        doTakePhoto();
                        break;
                    case 1:
                        doPickPhotoFromGallery();
                        break;
                }
            }
        });
        return builder.create();
    }
    
    /**
     * Dismiss the given {@link Dialog}.
     */
    static void dismissDialog(Dialog dialog) {
        try {
            // Only dismiss when valid reference and still showing
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            
        }
    }
    
    boolean doPickPhotoAction() {
        showAndManageDialog(createPickPhotoDialog());
        return true;
    }
    
    /**
     * Start managing this {@link Dialog} along with the {@link Activity}.
     */
    private void startManagingDialog(Dialog dialog) {
        synchronized (mManagedDialogs) {
            mManagedDialogs.add(dialog);
        }
    }
    
    void showAndManageDialog(Dialog dialog) {
        startManagingDialog(dialog);
        dialog.show();
    }
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case CAMERA_WITH_DATA:{
                doCropPhoto(mCurrentPhotoFile);
                break;
            }    
            case PHOTO_PICKED_WITH_DATA: {
                if (data != null) {
                    mPhoto = data.getParcelableExtra("data");
                    mCover.setPhotoBitmap(mPhoto);
                } else {
                    // The contact that requested the photo is no longer present.
                    // TODO: Show error message
                }
                break;
            }
            default:
                break;
        }
    }
    
    /**
     * Pick a specific photo to be added under the currently selected tab.
     */
    protected void doTakePhoto() {
        try {
            // Launch camera to take photo for selected contact
            PHOTO_DIR.mkdirs();
            mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());
            final Intent intent = getTakePickIntent(mCurrentPhotoFile);
            startActivityForResult(intent, CAMERA_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Launches Gallery to pick a photo.
     */
    protected void doPickPhotoFromGallery() {
        try {
            // Launch picker to choose photo for selected contact
            final Intent intent = getPhotoPickIntent();
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Create a file name for the icon photo using current time.
     */
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }
    
    /**
     * Constructs an intent for capturing a photo and storing it in a temporary file.
     */
    public static Intent getTakePickIntent(File f) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }
    
    /**
     * Constructs an intent for picking a photo from Gallery, cropping it and returning the bitmap.
     */
    public static Intent getPhotoPickIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", ICON_SIZE);
        intent.putExtra("outputY", ICON_SIZE);
        intent.putExtra("return-data", true);
        return intent;
    }
    
    /**
     * Sends a newly acquired photo to Gallery for cropping
     */
    protected void doCropPhoto(File f) {
        try {

            // Add the image to the media store
            MediaScannerConnection.scanFile(
                    this,
                    new String[] { f.getAbsolutePath() },
                    new String[] { null },
                    null);

            // Launch gallery to crop the photo
            final Intent intent = getCropImageIntent(Uri.fromFile(f));
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {
            Toast.makeText(this, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Constructs an intent for image cropping.
     */
    public static Intent getCropImageIntent(Uri photoUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", ICON_SIZE);
        intent.putExtra("outputY", ICON_SIZE);
        intent.putExtra("return-data", true);
        return intent;
    }
    
    /**
     * Class that listens to requests coming from photo editors
     */
    private class PhotoListener implements EditorListener, DialogInterface.OnClickListener {
        private long mRawContactId;
        private boolean mReadOnly;
        private PhotoEditorView mEditor;

        public PhotoListener(boolean readOnly, PhotoEditorView editor) {
//            mRawContactId = rawContactId;
            mReadOnly = readOnly;
            mEditor = editor;
        }

        public void onRequest(int request) {

            if (request == EditorListener.REQUEST_PICK_PHOTO) {
                if (mEditor.hasSetPhoto()) {
                    // There is an existing photo, offer to remove, replace, or promoto to primary
                    createPhotoDialog().show();
                } else if (!mReadOnly) {
                    // No photo set and not read-only, try to set the photo
                    doPickPhotoAction();
                }
            }
        }

        /**
         * Prepare dialog for picking a new {@link EditType} or entering a
         * custom label. This dialog is limited to the valid types as determined
         * by {@link EntityModifier}.
         */
        public Dialog createPhotoDialog() {
            Context context = ContactsAccountEditActivity.this;

            // Wrap our context to inflate list items using correct theme
            final Context dialogContext = new ContextThemeWrapper(context,
                    android.R.style.Theme_Light);

            String[] choices;
            choices = new String[2];
            choices[0] = getString(R.string.removePicture);
            choices[1] = getString(R.string.changePicture);

            final ListAdapter adapter = new ArrayAdapter<String>(dialogContext,
                    android.R.layout.simple_list_item_1, choices);

            final AlertDialog.Builder builder = new AlertDialog.Builder(dialogContext);
            builder.setTitle(R.string.attachToContact);
            builder.setSingleChoiceItems(adapter, -1, this);
            return builder.create();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // TODO Auto-generated method stub
            dialog.dismiss();

            switch (which) {
                case 0:
                    // Remove the photo
                    mEditor.setPhotoBitmap(null);
                    break;

                case 1:
                    // Pick a new photo for the contact
                    doPickPhotoAction();
                    break;
            }
        }

        @Override
        public void onDeleted(Editor editor) {
            // TODO Auto-generated method stub
            
        }
   }

}
