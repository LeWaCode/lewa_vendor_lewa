package com.lewa.player.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.google.android.collect.Lists;
import com.lewa.player.ExitApplication;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.model.AddPlaylistAdapter;
import com.lewa.player.model.Editor;
import com.lewa.player.model.Editor.EditorListener;
import com.lewa.player.ui.view.PhotoEditorView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;

public class AddPlaylistActivity extends Activity implements View.OnClickListener{
    private static final int SUBACTIVITY_ADD_SONGS = 1;
    /** The launch code when taking a picture */
    private static final int CAMERA_WITH_DATA = 2;
    private static final int PHOTO_PICKED_WITH_DATA = 3;
    
    public static final String ACTION_ADDPLAYLIST = "com.lewa.player.ui.ADDPLAYLIST";
    private static final int ICON_SIZE = 96;
    
    private static final File PHOTO_DIR = new File(
            Environment.getExternalStorageDirectory() + "/DCIM/Camera");
    private ArrayList<Dialog> mManagedDialogs = Lists.newArrayList();
    private ArrayList<Integer> marrSongId = new ArrayList<Integer>();
    public AddPlaylistAdapter mAdapter;    
    private File mCurrentPhotoFile;
    private ListView mListView;
    private EditText mEditText;
    private Button mbtnDone;
    private Button mbtnCancel;
    private long mPlaylistId;
    private PhotoEditorView mCover;
    private Bitmap mPhoto;
    
    private String mSortOrder;       
    private String[] mCursorCols = new String[] {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
//            MediaStore.Audio.Media.DATA,
//            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
//            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION
    };
    
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        setContentView(R.layout.playlist_create);
        
        mCover = (PhotoEditorView)findViewById(R.id.edit_cover);
        mCover.setEditorListener(new PhotoListener(false, mCover)); 
        View vAdd = findViewById(R.id.btn_select_songs);
        vAdd.setOnClickListener(this);
        mbtnDone = (Button)findViewById(R.id.btn_done);
        mbtnDone.setOnClickListener(this);
        mbtnCancel = (Button)findViewById(R.id.btn_discard);
        mbtnCancel.setOnClickListener(this);
        
        View title = findViewById(R.id.playlist_create_layout);
        title.setFocusable(true);
        title.setFocusableInTouchMode(true);
        title.requestFocus();
        
        mAdapter = new AddPlaylistAdapter(this, null);
        mEditText = (EditText)findViewById(R.id.edit_text);
        mListView = (ListView)findViewById(R.id.list);
        if (mAdapter != null && mListView != null) {
            mListView.setAdapter(mAdapter);
        }
        Bundle bundle = getIntent().getExtras();
        String playlistName = null;
        if (bundle != null) {
            ArrayList<Integer> arrSongs = bundle.getIntegerArrayList("song_id");
            if (arrSongs != null && arrSongs.size() != 0) {
                marrSongId.addAll(extractSongsId(arrSongs));
                getTrackCursor(mAdapter.getQueryHandler(), true);
                setListViewLayoutParams();
            }     
            arrSongs = bundle.getIntegerArrayList("playlist_songs_id");
            playlistName = bundle.getString("playlist_name");
            if (arrSongs != null && arrSongs.size() != 0) {
                marrSongId.addAll(extractSongsId(arrSongs));
                getTrackCursor(mAdapter.getQueryHandler(), true);
                setListViewLayoutParams();
            }
            
            mPlaylistId = bundle.getInt("playlist_id");
            Bitmap bitmap = MusicUtils.getPlaylistCover((int)mPlaylistId);
            if (bitmap != null && mCover != null) {
                mCover.setPhotoBitmap(bitmap);
            }
        }
        
        String defaultname = null;
        if (playlistName != null) {
            defaultname = playlistName;
        } else {
            defaultname = icicle != null ? icicle.getString("defaultname") : makePlaylistName();
        }
        if (defaultname == null) {
            finish();
            return;
        }
        mEditText.setText(defaultname);
        mEditText.setSelection(defaultname.length());
        mEditText.addTextChangedListener(mTextWatcher);
        
        ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
    }

    TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // don't care about this one
        }
        
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String newText = mEditText.getText().toString();
            if (newText.trim().length() == 0) {
                mbtnDone.setEnabled(false);
            } else {
                mbtnDone.setEnabled(true);
                // check if playlist with current name exists already, and warn the user if so.
//                if (idForplaylist(newText) >= 0) {
//                    mbtnDone.setText(R.string.create_playlist_overwrite_text);
//                } else {
//                    mbtnDone.setText(R.string.create_playlist_create_text);
//                }
            }
        };
        
        @Override
        public void afterTextChanged(Editable s) {
            // don't care about this one
        }
    };
    
    private String makePlaylistName() {

        String template = getString(R.string.new_playlist_name_template);
        int num = 1;

        String[] cols = new String[] {
                MediaStore.Audio.Playlists.NAME
        };
        ContentResolver resolver = getContentResolver();
        String whereclause = MediaStore.Audio.Playlists.NAME + " != ''";
        Cursor c = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
            cols, whereclause, null,
            MediaStore.Audio.Playlists.NAME);

        if (c == null) {
            return null;
        }
        
        String suggestedname;
        suggestedname = String.format(template, num++);
        
        // Need to loop until we've made 1 full pass through without finding a match.
        // Looping more than once shouldn't happen very often, but will happen if
        // you have playlists named "New Playlist 1"/10/2/3/4/5/6/7/8/9, where
        // making only one pass would result in "New Playlist 10" being erroneously
        // picked for the new name.
        boolean done = false;
        while (!done) {
            done = true;
            c.moveToFirst();
            while (! c.isAfterLast()) {
                String playlistname = c.getString(0);
                if (playlistname.compareToIgnoreCase(suggestedname) == 0) {
                    suggestedname = String.format(template, num++);
                    done = false;
                }
                c.moveToNext();
            }
        }
        c.close();
        return suggestedname;
    }
    
    public ArrayList<Integer> extractSongsId(ArrayList<Integer> data) {
        ArrayList<Integer> freshData = new ArrayList<Integer>();
        Integer songId = null;
        int size = data.size();        
        for (int i = 0; i < size; i++) {
            songId = data.get(i);
            if (!freshData.contains(songId))
                freshData.add(songId);
        }
        return freshData;
    }
    
    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        int id = view.getId();
        closeKeyboard();
        
        switch (id) {
        case R.id.btn_select_songs: {
                Intent intent = new Intent();
                intent.putIntegerArrayListExtra("song_id", marrSongId);
                intent.setClass(this, AddPlaylistSongsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, SUBACTIVITY_ADD_SONGS);
            }
            break;
        case R.id.btn_done: {
                SavaPlayList();
                if (mPhoto != null)
                    bitmaptoSDcard(mPhoto, mPlaylistId);
                Intent intent = new Intent();
                intent.putExtra("playlist_id", mPlaylistId);
                intent.putExtra("title", mEditText.getText().toString());
                intent.setAction(ACTION_ADDPLAYLIST);
                this.sendBroadcast(intent);
//                setResult(RESULT_OK, intent);
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
    
    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) mEditText
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
        case SUBACTIVITY_ADD_SONGS: {
            if (resultCode == RESULT_OK) {    
                Bundle bundle = data.getExtras();
                marrSongId.clear();
                marrSongId.addAll(bundle.getIntegerArrayList("song_id"));
                getTrackCursor(mAdapter.getQueryHandler(), true); 
                setListViewLayoutParams();
            }
            break;
        }
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
    
    private void setListViewLayoutParams() {
        int size = marrSongId.size();
        int height = 0;
        if (size != 0) {
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.playlist_multiple_add_list_item, mListView, false);
            height = view.getLayoutParams().height + 1;
        }
        mListView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, height * size));
    }
    
    private Cursor getTrackCursor(AddPlaylistAdapter.TrackQueryHandler queryhandler, boolean async) {
        if (queryhandler == null) {
            throw new IllegalArgumentException();
        }
        
        Cursor ret = null;
        int size = marrSongId.size();
        mSortOrder = MediaStore.Audio.Media.TITLE_KEY;
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.TITLE + " != ''");
        where.append(" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");
        String strSongId = new String("");
        for (int i = 0; i < size; i++) {
            if (i == 0)
                strSongId += ((Integer)marrSongId.get(i)).toString();
            else
                strSongId += "," + ((Integer)marrSongId.get(i)).toString();
        }
        where.append(" AND " + MediaStore.Audio.Media._ID + " in (" + strSongId + ")");
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ret = queryhandler.doQuery(uri,
                mCursorCols, where.toString() , null, mSortOrder, async);
        
        if (ret != null && async) {
            mAdapter.changeCursor(ret);
        }
        return ret;
    }
    
    public void SavaPlayList() {        
        String name = mEditText.getText().toString();
        if (name != null && name.length() > 0) {
            ContentResolver resolver = getContentResolver();
            int id = mPlaylistId > 0 ? (int)mPlaylistId : idForplaylist(name);
            Uri uri;
            if (id >= 0) {
                uri = ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id);
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.Playlists.NAME, name);
                resolver.update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values, MediaStore.Audio.Playlists._ID + " = " + String.valueOf(id), null);
                MusicUtils.clearPlaylist(this, id);
            } else {
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.Playlists.NAME, name);
                uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
            }
            
            int size = marrSongId.size();
            long[] songIds = new long[size];
            for (int i = 0; i < size; i++)
                songIds[i] = marrSongId.get(i);
            mPlaylistId = ContentUris.parseId(uri);
            MusicUtils.addToPlaylist(this, songIds, mPlaylistId);
            setResult(RESULT_OK, (new Intent()).setData(uri));
        }
    }
    
    private int idForplaylist(String name) {
        Cursor c = MusicUtils.query(this, MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Playlists._ID },
                MediaStore.Audio.Playlists.NAME + "=?",
                new String[] { name },
                MediaStore.Audio.Playlists.NAME);
        int id = -1;
        if (c != null) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                id = c.getInt(0);
            }
            c.close();
        }
        return id;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (Dialog dialog : mManagedDialogs) {
            dismissDialog(dialog);
        }
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
        builder.setTitle(R.string.dialog_title_list_cover);
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
     * Pick a specific photo to be added under the currently selected tab.
     */
    protected void doTakePhoto() {
        try {
            // Launch camera to take photo for selected contact
            PHOTO_DIR.mkdirs();
            mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());
            final Intent intent = getTakePickIntent(mCurrentPhotoFile);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
    private void bitmaptoSDcard(Bitmap bitmap, long id) {

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {               
            String sdCardDir = Environment.getExternalStorageDirectory()+"/LEWA/music/playlist/cover/";
            File file = new File(sdCardDir,String.valueOf(id));
            file.getParentFile().mkdirs();
            
            if(!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try 
            {
                FileOutputStream out=new FileOutputStream(file);
                if(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out))
                {
                    out.flush();
                    //Log.v(TAG,"Success");
                    out.close();
                }
            } 
            catch (FileNotFoundException e) 
            {
                e.printStackTrace();
            } catch (IOException e) 
            {
                e.printStackTrace();              
            }
        }    
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
            Context context = AddPlaylistActivity.this;

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
            builder.setTitle(R.string.attachToPlaylist);
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
