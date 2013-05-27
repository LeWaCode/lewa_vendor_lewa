package com.lewa.player.ui;

import java.text.Collator;

import com.lewa.player.ExitApplication;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.model.SearchLocalSongsAdapter;
import com.lewa.player.ui.view.SearchEditText;

import android.R.integer;
import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.ListView;

public class SearchLocalSongsActivity extends ListActivity 
                implements TextWatcher, TextView.OnEditorActionListener, 
                    OnFocusChangeListener, OnTouchListener{
    public SearchLocalSongsAdapter mAdapter;
    private String mSortOrder;
    ListView mListView;
    private SearchEditText mSearchEditText;
    private LewaSearchBar mSearchBar;
    
    private String[] mCursorCols = new String[] {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
    };
    
    @Override
    protected void onCreate(Bundle bundle) {
        // TODO Auto-generated method stub
        super.onCreate(bundle);
        
        setContentView(R.layout.music_local_search_list_content);
        mListView = getListView();
        mSearchBar = (LewaSearchBar)findViewById(R.id.search_plate);
        mAdapter = new SearchLocalSongsAdapter(this, R.layout.music_local_search_list_item, null, mSearchBar); 
        mListView.setAdapter(mAdapter);
        mListView.setOnFocusChangeListener(this);
        mListView.setOnTouchListener(this);
        
        setupSearchView();
        
        ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        if (mAdapter != null) {
            Cursor cursor = mAdapter.getCursor();
            if (cursor != null && cursor.moveToPosition(position)) {
                int songIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                long songId = cursor.getLong(songIdx);
                long[] allSongIds = MusicUtils.getAllSongs(this);
                int p= -1;
                for(int i=0;i<allSongIds.length;i++){
                	if(songId == allSongIds[i]){
                		p = i;
                		
                		break;
                	}
                }
                MusicUtils.playAll(this, allSongIds, p );
            }
        }
    }

    private void hideSoftKeyboard() {
        // Hide soft keyboard, if visible
        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mListView.getWindowToken(), 0);
    }
    
    private String getTextFilter() {
        if (mSearchEditText != null) {
            return mSearchEditText.getText().toString();
        }
        return null;
    }
    public Cursor getTrackCursor(SearchLocalSongsAdapter.TrackQueryHandler queryhandler, String filter, boolean async) {
        if (queryhandler == null) {
            throw new IllegalArgumentException();
        }
        if (TextUtils.isEmpty(filter)) {
            return new MatrixCursor(mCursorCols); 
        }
        
        Cursor ret = null;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        mSortOrder = MediaStore.Audio.Media.TITLE_KEY;
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.TITLE + " != ''");
        where.append(" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");          
        where.append(MusicUtils.getWhereBuilder(this, "_id", 0));
        
        uri = uri.buildUpon().appendQueryParameter("filter", Uri.encode(filter)).build();
        ret = queryhandler.doQuery(uri,
                mCursorCols, where.toString() , null, mSortOrder, async);
   
        if (ret != null && async) {
            mAdapter.changeCursor(ret);
        }
        return ret;
    }
    
    /**
     * Configures search UI.
     */
    private void setupSearchView() {
        mSearchEditText = (SearchEditText)findViewById(R.id.search_src_text);
        mSearchEditText.addTextChangedListener(this);
        mSearchEditText.setOnEditorActionListener(this);
        mSearchEditText.setText(null);
    }
    
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            hideSoftKeyboard();
            if (TextUtils.isEmpty(getTextFilter())) {
                finish();
            }
            return true;
        }
        return false;
    }
    @Override
    public void afterTextChanged(Editable arg0) {
        // TODO Auto-generated method stub
        onSearchTextChanged();
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
            int arg3) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub
        
    }
    
    protected void onSearchTextChanged() {
        // Set the proper empty string

        Filter filter = mAdapter.getFilter();
        filter.filter(getTextFilter());
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && TextUtils.isEmpty(getTextFilter())) {
            hideSoftKeyboard();
            onBackPressed();
            return true;
        }
        return false;
    }
    
    /**
     * Dismisses the soft keyboard when the list takes focus.
     */
    public void onFocusChange(View view, boolean hasFocus) {
        if (view == mListView && hasFocus) {
            hideSoftKeyboard();
        }
    }

    /**
     * Dismisses the soft keyboard when the list takes focus.
     */
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mListView) {
            hideSoftKeyboard();
        }
        return false;
    }
}
