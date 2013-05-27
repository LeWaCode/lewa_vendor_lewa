package com.lewa.player.ui;

import java.text.Collator;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.lewa.player.ExitApplication;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.model.AddPlaylistSongsAdapter;
import com.lewa.player.ui.view.SearchEditText;

public class AddPlaylistSongsActivity extends ListActivity implements View.OnClickListener, TextWatcher, OnEditorActionListener, 
            OnFocusChangeListener, OnTouchListener{
    public AddPlaylistSongsAdapter mAdapter;   
    private String mSortOrder;
    private SearchEditText mSearchEditText;
    private ListView mList;
    
    public ArrayList<Integer> arrSongsIdSel = new ArrayList<Integer>();
    
    private String[] mCursorCols = new String[] {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
//            MediaStore.Audio.Media.DATA,
//            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
//            MediaStore.Audio.Media.ARTIST_ID,
//            MediaStore.Audio.Media.DURATION
    };
    
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        setContentView(R.layout.playlist_multiple_add_list);
        
        mAdapter = new AddPlaylistSongsAdapter(this, null);
        if (mAdapter != null)
            setListAdapter(mAdapter);
        
        mList = (ListView)findViewById(com.android.internal.R.id.list);
        
        findViewById(R.id.btn_done).setOnClickListener(this);
        View layout = findViewById(R.id.pinned_header_list_layout);
        layout.setFocusable(true);
        layout.setFocusableInTouchMode(true);
        layout.requestFocus();
        
        Intent intent = getIntent();
        ArrayList<Integer> arrSongId = intent.getIntegerArrayListExtra("song_id");
        if (arrSongId != null && arrSongId.size() != 0)
            arrSongsIdSel.addAll(arrSongId);  
        getTrackCursor(mAdapter.getQueryHandler(), null, true);
        
        setupSearchView();
        mList.setOnFocusChangeListener(this);
        mList.setOnTouchListener(this);
        
        ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
    }
        
    public void setItemState(int position, boolean isSelect) {
        Cursor cursor = mAdapter.getCursor();
        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        final long songId = cursor.getLong(0);
        if (isSelect == true) {
            if (!arrSongsIdSel.contains(new Integer((int)songId)))
                arrSongsIdSel.add(new Integer((int)songId));
            
        } else {
            arrSongsIdSel.remove(new Integer((int)songId));
        }
        String string = String.format("%s(%d)", getResources().getString(R.string.menu_done), arrSongsIdSel.size());           
        Button btnDone = (Button)findViewById(R.id.btn_done);
        if (btnDone != null)
            btnDone.setText(string);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        AddPlaylistSongsAdapter.ItemViewHolder iteminfo = (AddPlaylistSongsAdapter.ItemViewHolder)v.getTag();;            
        iteminfo.mCheckBox.setChecked(!iteminfo.mCheckBox.isChecked());  
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        int id = view.getId();
        
        switch (id) {
        case R.id.btn_done: {
                int size = arrSongsIdSel.size(); 
                Intent intent = new Intent();
                intent.putIntegerArrayListExtra("song_id", arrSongsIdSel);
                setResult(RESULT_OK, intent);
                finish();
            }
            break;
        default:
            break;
        }
    }

    public Cursor getTrackCursor(AddPlaylistSongsAdapter.TrackQueryHandler queryhandler, String filter, boolean async) {
        if (queryhandler == null) {
            throw new IllegalArgumentException();
        }
        
        Cursor ret = null;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        mSortOrder = MediaStore.Audio.Media.TITLE_KEY;
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.TITLE + " != ''");
        where.append(" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");
        where.append(MusicUtils.getWhereBuilder(this, "_id", 0));
        
        if (!TextUtils.isEmpty(filter)) {
            filter = Uri.decode(filter).trim();
            if (!TextUtils.isEmpty(filter)) {
                String [] searchWords = filter.split(" ");
                Collator col = Collator.getInstance();
                col.setStrength(Collator.PRIMARY);
                for (int i = 0; i < searchWords.length; i++) {
                    String key = MediaStore.Audio.keyFor(searchWords[i]);
                    key = key.replace("\\", "\\\\");
                    key = key.replace("%", "\\%");
                    key = key.replace("_", "\\_");            
                    where.append(" AND " + MediaStore.Audio.Media.ARTIST_KEY +
                            "||" + MediaStore.Audio.Media.TITLE_KEY + " LIKE '%" +
                            key + "%' ESCAPE '\\'");

                }
            }
        }
        ret = queryhandler.doQuery(uri,
                mCursorCols, where.toString() , null, mSortOrder, async);
        
        if (ret != null && async) {
            mAdapter.changeCursor(ret);
        }
        return ret;
    }
    
    /**
     * Dismisses the search UI along with the keyboard if the filter text is empty.
     */
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && TextUtils.isEmpty(getTextFilter())) {
            hideSoftKeyboard();
            onBackPressed();
            return true;
        }
        return false;
    }
    
    /**
     * Configures search UI.
     */
    private void setupSearchView() {
        mSearchEditText = (SearchEditText)findViewById(R.id.search_src_text);
        mSearchEditText.addTextChangedListener(this);
        mSearchEditText.setOnEditorActionListener(this);
//        mSearchEditText.setText(mInitialFilter);
    }
    
    private String getTextFilter() {
        if (mSearchEditText != null) {
            return mSearchEditText.getText().toString();
        }
        return null;
    }
    
    private void hideSoftKeyboard() {
        // Hide soft keyboard, if visible
        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mList.getWindowToken(), 0);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub
        onSearchTextChanged();
    }
    
    /**
     * Performs filtering of the list based on the search query entered in the
     * search text edit.
     */
    protected void onSearchTextChanged() {
        Filter filter = mAdapter.getFilter();
        filter.filter(getTextFilter());
    }
    
    /**
     * Dismisses the soft keyboard when the list takes focus.
     */
    public void onFocusChange(View view, boolean hasFocus) {
        if (view == mList && hasFocus) {
            hideSoftKeyboard();
        }
    }

    /**
     * Dismisses the soft keyboard when the list takes focus.
     */
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mList) {
            hideSoftKeyboard();
        }
        return false;
    }
}
