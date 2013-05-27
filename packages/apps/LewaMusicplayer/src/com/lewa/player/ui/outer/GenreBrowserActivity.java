package com.lewa.player.ui.outer;

import com.lewa.os.ui.PendingContentLoader;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.model.GenreBrowserAdapter;
import com.lewa.player.model.SongsCountLoader;
import com.lewa.player.model.GenreBrowserAdapter.ViewHolder;

import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class GenreBrowserActivity extends ListActivity
        implements View.OnCreateContextMenuListener, MusicUtils.Defs, PendingContentLoader {

    private GenreBrowserAdapter mAdapter;
    private Cursor mGenreCursor;
    private long[] mCurrentGenreList;
    private String mCurrentGenreName;
    private SongsCountLoader mSongsCountLoader;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
        boolean bDelayLoadContent = getIntent().getBooleanExtra("delayloadcontent", false);
        if (!bDelayLoadContent) {
    		ListView genreList = getListView();
    		genreList.setDivider(null);
    		genreList.setCacheColorHint(0);
    		genreList.setOnCreateContextMenuListener(this);
    		
    		IntentFilter f = new IntentFilter();
            f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
            f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
            f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            f.addDataScheme("file");
            registerReceiver(mScanListener, f);

    		mAdapter = (GenreBrowserAdapter) getLastNonConfigurationInstance();
            if (mAdapter == null) {
                // Log.i("@@@", "starting query");
                mAdapter = new GenreBrowserAdapter(getApplication(), this,
                        R.layout.track_list_item, mGenreCursor, new String[] {},
                        new int[] {});
                setListAdapter(mAdapter);
                mSongsCountLoader = new SongsCountLoader(this, SongsCountLoader.TYPE_GENRE);
                mAdapter.setSongsCountLoader(mSongsCountLoader);
                setTitle(R.string.working_albums);
                getGenreCursor(mAdapter.getQueryHandler(), null);
            } else {
                mAdapter.setActivity(this);
                setListAdapter(mAdapter);
                mGenreCursor = mAdapter.getCursor();
                if (mGenreCursor != null) {
                    init(mGenreCursor);
                } else {
                    getGenreCursor(mAdapter.getQueryHandler(), null);
                }
            }
            registerProviderStatusObserver();
        }

	}
	
	@Override
    public void loadContent() {
		// TODO Auto-generated method stub
        boolean bDelayLoadContent = getIntent().getBooleanExtra("delayloadcontent", false);
        if (bDelayLoadContent) {
            getIntent().putExtra("delayloadcontent", false);
            ListView genreList = getListView();
            genreList.setDivider(null);
            genreList.setCacheColorHint(0);
            genreList.setOnCreateContextMenuListener(this);
            
            IntentFilter f = new IntentFilter();
            f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
            f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
            f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            f.addDataScheme("file");
            registerReceiver(mScanListener, f);
            
            mAdapter = (GenreBrowserAdapter) getLastNonConfigurationInstance();
            if (mAdapter == null) {
            // Log.i("@@@", "starting query");
                mAdapter = new GenreBrowserAdapter(getApplication(), this,
                        R.layout.track_list_item, mGenreCursor, new String[] {},
                        new int[] {});
                setListAdapter(mAdapter);
                mSongsCountLoader = new SongsCountLoader(this, SongsCountLoader.TYPE_GENRE);
                mAdapter.setSongsCountLoader(mSongsCountLoader);
                setTitle(R.string.working_albums);
                getGenreCursor(mAdapter.getQueryHandler(), null);
            } else {
                mAdapter.setActivity(this);
                setListAdapter(mAdapter);
                mGenreCursor = mAdapter.getCursor();
                if (mGenreCursor != null) {
                    init(mGenreCursor);
                } else {
                    getGenreCursor(mAdapter.getQueryHandler(), null);
                }
            }
            registerProviderStatusObserver();
        }
    }
	
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        Intent intent = new Intent();
        intent.setClass(this, AlbumTrackBrowserActivity.class);
        if(position < mGenreCursor.getCount()) {
            intent.putExtra("genre", Long.valueOf(id).toString());
        } else {
            intent.putExtra("othergenre", 1);
        }
        intent.putExtra("ifgenre", 1);
        ViewHolder holder = (ViewHolder)v.getTag();
        intent.putExtra("genretitle", holder.title.getText());
        startActivity(intent);
    }
	
	public Cursor getGenreCursor(AsyncQueryHandler async, String filter) {
        String[] cols = new String[] {
                MediaStore.Audio.Genres._ID,
                MediaStore.Audio.Genres.NAME
        };

        Uri uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
        if (!TextUtils.isEmpty(filter)) {
            uri = uri.buildUpon().appendQueryParameter("filter", Uri.encode(filter)).build();
        }

        Cursor ret = null;
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Genres._ID + " IN (select genre_id from audio_genres_map where 1=1 ");
        where.append(MusicUtils.getWhereBuilder(this, "_id", 1));
        where.append(")");
        if (async != null) {
            async.startQuery(0, null, uri,
                    cols, where.toString(), null, MediaStore.Audio.Genres.NAME);
        } else {
            ret = MusicUtils.query(this, uri,
                    cols, where.toString(), null, MediaStore.Audio.Genres.NAME);
        }
        return ret;
    }
	
	public void init(Cursor c) {

        if (mAdapter == null) {
            return;
        }
        mAdapter.changeCursor(c); // also sets mGenreCursor

        if (mGenreCursor == null) {
            MusicUtils.displayDatabaseError(this);
            closeContextMenu();
            mReScanHandler.sendEmptyMessageDelayed(0, 1000);
            return;
        }
	}
	
	private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
                    || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                MusicUtils.setSpinnerState(GenreBrowserActivity.this);
            }
            mReScanHandler.sendEmptyMessage(0);
        }
    };
	
	private Handler mReScanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mAdapter != null) {
                getGenreCursor(mAdapter.getQueryHandler(), null);
            }
        }
    };
    
    private void registerProviderStatusObserver() {
        getContentResolver().registerContentObserver(Uri.parse("content://media"),
                true, mProviderStatusObserver);
    }

    private void unregisterProviderStatusObserver() {
        getContentResolver().unregisterContentObserver(mProviderStatusObserver);
    }
    
    private ContentObserver mProviderStatusObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            if(mAdapter != null) {
                getGenreCursor(mAdapter.getQueryHandler(), null);               
            }
        }
    };
	
	public Cursor getGenreCursor() {
	    return mGenreCursor;
	}
	
	public void setGenreCursor(Cursor cursor) {
	    mGenreCursor = cursor;
	    mAdapter.setCursor(cursor);
	}
	
	@Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
	    if (mAdapter != null) {
            mAdapter.changeCursor(mGenreCursor);
        }
	if (mSongsCountLoader != null) {
	    mSongsCountLoader.stop();
	}
        mAdapter = null;
        unregisterReceiverSafe(mScanListener);
        unregisterProviderStatusObserver();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            mAdapter.notifyDataSetInvalidated();
        }
        if (mSongsCountLoader != null) {
            mSongsCountLoader.resume();
        }
    }
    
    private void unregisterReceiverSafe(BroadcastReceiver receiver) {
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }
	
	public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        // TODO Auto-generated method stub
        menu.add(0, MusicUtils.Defs.PLAY_SELECTION, 0, R.string.play_selection);
        SubMenu sub = menu.addSubMenu(0, MusicUtils.Defs.ADD_TO_PLAYLIST, 0, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(this, sub);
        menu.add(0, MusicUtils.Defs.DELETE_ITEM, 0, R.string.delete_item);
        if(menuInfo != null) {
            AdapterContextMenuInfo mi = (AdapterContextMenuInfo)menuInfo;
            int position = mi.position;
            ViewHolder vh = (ViewHolder)mi.targetView.getTag();
            if((mAdapter.unknownGenresList.length > 0 && position < mGenreCursor.getCount()) || 
                    mAdapter.unknownGenresList.length == 0) {
                mCurrentGenreList = MusicUtils.getSongListForGenre(this, mi.id);
            } else {
                mCurrentGenreList = mAdapter.unknownGenresList;
            }
            mCurrentGenreName = (String) vh.title.getText();
        }
        menu.setHeaderTitle(mCurrentGenreName);
    }
	
	@Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case PLAY_SELECTION: {
                  MusicUtils.playAll(this, mCurrentGenreList, 0);
                return true;
            }

            case QUEUE: {
                MusicUtils.addToCurrentPlaylist(this, mCurrentGenreList);
                return true;
            }

            case NEW_PLAYLIST: {
                MusicUtils.addToNewPlaylist(this, mCurrentGenreList, NEW_PLAYLIST);
                return true;
            }

            case PLAYLIST_SELECTED: {
                long playlist = item.getIntent().getLongExtra("playlist", 0);
                MusicUtils.addToPlaylist(this, mCurrentGenreList, playlist);
                return true;
            }
            case DELETE_ITEM: {
                String f;
                if (android.os.Environment.isExternalStorageRemovable()) {
                    f = getString(R.string.delete_genre_desc);
                } else {
                    f = getString(R.string.delete_genre_desc_nosdcard);
                }
                String desc = String.format(f, mCurrentGenreName);
                MusicUtils.deleteItems(this, desc, mCurrentGenreList);
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }
}
