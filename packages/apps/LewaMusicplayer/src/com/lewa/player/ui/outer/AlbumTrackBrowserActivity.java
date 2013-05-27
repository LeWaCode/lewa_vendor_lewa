package com.lewa.player.ui.outer;

import com.lewa.player.ExitApplication;
import com.lewa.player.MusicSetting;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.SleepModeManager;
import com.lewa.player.model.AlbumTrackAdapter;
import com.lewa.player.model.NowPlayingCursor;
import com.lewa.player.online.LocalAsync;
import com.lewa.player.online.OnlineLoader;
import com.lewa.player.ui.MediaPlaybackHomeActivity;
import com.lewa.player.ui.MusicFolderActivity;
import com.lewa.player.ui.NowPlayingController;
import com.lewa.player.ui.SearchLocalSongsActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class AlbumTrackBrowserActivity extends Activity 
        implements View.OnCreateContextMenuListener, MusicUtils.Defs, View.OnTouchListener{
    
    private static final int ID_MUSIC_NO_SONGS_CONTENT = 100;

	private ListView mListView;
	private AlbumTrackAdapter mAdapter = null;
	private Context mContext;
	private TextView mAlbumTitle;
	private String mAlbumId;
	private String mArtistId;
	private String mGenreId;
	private long mCurrentTrackId;
    private String mCurrentTrackName;
    private int mCurrentTrackPosition;
	private int ifGenre = 0;
	public Cursor mTrackCursor;
	private String mSortOrder;
	private String[] mCursorCols;
	LocalAsync labg;
	private int mIfOtherGenres;
	private NowPlayingController Albumactionbar;
	private LinearLayout mLinear;
	private String mFolderPath;
	private int mNoSongsPaddingTop;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.albumtracklist);
		mListView = (ListView) findViewById(R.id.albumtracklist);
		mAlbumTitle = (TextView) findViewById(R.id.album_name);
		ifGenre = this.getIntent().getIntExtra("ifgenre", 0);
		mAlbumId = this.getIntent().getStringExtra("album");
		mArtistId = this.getIntent().getStringExtra("artist");
		mGenreId = this.getIntent().getStringExtra("genre");
		mIfOtherGenres = this.getIntent().getIntExtra("othergenre", 0);
		mFolderPath = this.getIntent().getStringExtra("folderPath");
		mContext = this;
		//mAlbumId = savedInstanceState.getString("album");
		//mArtistId = savedInstanceState.getString("artist");
		mListView.setOnItemClickListener(itemClickListen);
		mListView.setDivider(null);
		mListView.setOnCreateContextMenuListener(this);
		mListView.setCacheColorHint(0);
		initAdapter();
        Albumactionbar = (NowPlayingController) findViewById(R.id.albums_title);
//        Albumactionbar.findViewById(R.id.nowplayingText).setOnTouchListener(this);
        if(MusicUtils.sService != null) {
            Albumactionbar.setMediaService(MusicUtils.sService);
        }
		if(mAlbumId != null) {
			String albumName = MusicUtils.getAlbumName(this, Long.valueOf(mAlbumId));
			updateActionBar(Long.valueOf(mAlbumId));
			if(MediaStore.UNKNOWN_STRING.equals(albumName)) {
                albumName = getString(R.string.unknown_album_name);
            }
			mAlbumTitle.setText(albumName);
		} else if(mFolderPath != null) {
		    String titleStr = mFolderPath.substring(mFolderPath.lastIndexOf("/") + 1, mFolderPath.length()); 
	        String title = titleStr.replaceFirst(
	                titleStr.substring(0, 1), titleStr.substring(0, 1).toUpperCase());
		    mAlbumTitle.setText(title);
		}

//        ImageView backImg = (ImageView)findViewById(R.id.nowplayingimage);
//        backImg.setImageDrawable(null);
//        backImg.setImageResource(R.drawable.top_play);//(R.drawable.top_back);
//        backImg.setOnTouchListener(this); 
		IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        f.addDataScheme("file");
        registerReceiver(mScanListener, f);
        
        mNoSongsPaddingTop = getResources().getDimensionPixelOffset(R.dimen.no_songs_padding_top);
        setupNoSongsView();
        
        ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
	}
	
	public void updateActionBar(long album) {
		String ActionName = MusicUtils.getAlbumName(mContext, album);		
		Intent intent = new Intent();
		intent.setAction(NowPlayingController.UPDATE_ACTIONBAR);
		intent.putExtra("actionName", ActionName);
		this.sendBroadcast(intent);
	}

    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
                    || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                MusicUtils.setSpinnerState(AlbumTrackBrowserActivity.this);
            }
            mReScanHandler.sendEmptyMessage(0);
        }        
    };
    
    private Handler mReScanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mAdapter != null) {
                getTrackCursor(mAdapter.getQueryHandler(), null, true);                
            }
            setupNoSongsView();
        }
    };
	
	OnItemClickListener itemClickListen = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long id) {
			// TODO Auto-generated method stub
	        if (mTrackCursor.getCount() == 0) {
	            return;
	        }
	        // When selecting a track from the queue, just jump there instead of
	        // reloading the queue. This is both faster, and prevents accidentally
	        // dropping out of party shuffle.
	        if (mTrackCursor instanceof NowPlayingCursor) {
	            if (MusicUtils.sService != null) {
	                try {
	                    MusicUtils.sService.setQueuePosition(position);
	                    return;
	                } catch (RemoteException ex) {
	                }
	            }
	        }
	        MusicUtils.playAll(mContext, mTrackCursor, position);
		}
	}; 

	private void initAdapter() {
		// TODO Auto-generated method stub
		if (mAdapter == null) {
            // Log.i("@@@", "starting query");
            mAdapter = new AlbumTrackAdapter(
                    this, // need to use application context to
                                      // avoid leaks
                    this,
                    
                    R.layout.edit_track_list_item,
                    null, // cursor
                    new String[] {}, new int[] {},
                    false,
                    true);
            mListView.setAdapter(mAdapter);
            setTitle(R.string.working_songs);
            getTrackCursor(mAdapter.getQueryHandler(), null, true);
        } else {
            mTrackCursor = mAdapter.getCursor();
            // If mTrackCursor is null, this can be because it doesn't have
            // a cursor yet (because the initial query that sets its cursor
            // is still in progress), or because the query failed.
            // In order to not flash the error dialog at the user for the
            // first case, simply retry the query when the cursor is null.
            // Worst case, we end up doing the same query twice.
            if (mTrackCursor != null) {
                init(mTrackCursor, false);
            } else {
                setTitle(R.string.working_songs);
                getTrackCursor(mAdapter.getQueryHandler(), null, true);
            }
        }		
	}
	
	public Cursor getTrackCursor(
			AlbumTrackAdapter.TrackQueryHandler queryhandler, String filter,
			boolean async) {
	    
		if (queryhandler == null) {
			throw new IllegalArgumentException();
		}	

		Cursor ret = null;
		
		Uri uri = null;
		StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.TITLE + " != ''");        
        mSortOrder = "Upper(sort_key)";   
		
		if(mFolderPath == null) {
		    where.append(MusicUtils.getWhereBuilder(this, "_id", 0));
            if (ifGenre == 0) {
                if (mAlbumId != null) {
                    where.append(" AND " + MediaStore.Audio.Media.ALBUM_ID + "="
                            + mAlbumId);
                }
                if (mArtistId != null) {
                    where.append(" AND " + MediaStore.Audio.Media.ARTIST_ID + "="
                            + mArtistId);
                    mSortOrder = "track";
                }
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            } else if (ifGenre == 1) {
                // where.append(" AND " + MediaStore.Audio.Genres.Members.GENRE_ID +
                // "="
                // + mGenreId);
                if (mIfOtherGenres != 1)
                    uri = Uri.parse("content://media/external/audio/genres/"
                            + mGenreId + "/members");
                else {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    where.append(" AND " + MediaStore.Audio.Media._ID
                            + " NOT IN (SELECT audio_id FROM audio_genres_map)");
                }
            }
		} else {
		    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		    where.append(" AND _data like '" + mFolderPath + "/%'");
	        where.append(" AND _data not like '" + mFolderPath + "/%/%'");
	        where.append(" AND _data not like '/mnt/sdcard/LEWA/Voice_Recorder%'");
	        where.append(" AND _data not like '/mnt/sdcard/LEWA/PIM%'");
		}

        where.append(" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");

        if (!TextUtils.isEmpty(filter)) {
            uri = uri.buildUpon()
                    .appendQueryParameter("filter", Uri.encode(filter)).build();
        }
        ret = queryhandler.doQuery(uri, mCursorCols, where.toString(), null,
                mSortOrder, async);

        // This special case is for the "nowplaying" cursor, which cannot be
        // handled
        // asynchronously using AsyncQueryHandler, so we do some extra
        // initialization here.
        if (ret != null && async) {
            init(ret, false);
        }
        return ret;
	}
	
	public void init(Cursor newCursor, boolean isLimited) {

        if (mAdapter == null) {
            return;
        }
        mAdapter.changeCursor(newCursor); // also sets mAlbumCursor
    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
	    if(Albumactionbar != null) {
	        Albumactionbar.destroyNowplaying();
	        Albumactionbar = null;
	    }

	    if (mAdapter != null) {
            mAdapter.changeCursor(mTrackCursor);
        }
	    
	    unregisterReceiver(mScanListener);
	    
        mAdapter = null;
        
//        if(labg != null) {
//            labg.restorePreArtistName();
//        }
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
		mLinear = (LinearLayout)findViewById(R.id.linear_albumpage);
        mLinear.post(new Runnable() {
                    
            @Override
            public void run() {
                // TODO Auto-generated method stub
                int id = R.drawable.playlist_default;
                MusicUtils.setDefaultBackground(mContext, mLinear, id);
            }
        });
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfoIn) {
        menu.add(0, PLAY_SELECTION, 0, R.string.play_selection);
        menu.add(0, DELETE_ITEM, 0, R.string.delete_item);        
        SubMenu sub = menu.addSubMenu(0, ADD_TO_PLAYLIST, 0, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(this, sub);
        menu.add(0, USE_AS_RINGTONE, 0, R.string.ringtone_menu);
        
        AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfoIn;
        mTrackCursor.moveToPosition(mi.position);
        mCurrentTrackPosition = mi.position;
        try {
            int id_idx = mTrackCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
            mCurrentTrackId = mTrackCursor.getLong(id_idx);
        } catch (IllegalArgumentException ex) {
            mCurrentTrackId = mi.id;
        }
        mCurrentTrackName = mTrackCursor.getString(mTrackCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        menu.setHeaderTitle(mCurrentTrackName);
    }

	@Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case PLAY_SELECTION: {
                // play the track
                int position = mCurrentTrackPosition;
                MusicUtils.playAll(this, mTrackCursor, position);
                return true;
            }
    
            case QUEUE: {
                long[] list = new long[] {mCurrentTrackId};
                MusicUtils.addToCurrentPlaylist(this, list);
                return true;
            }
    
            case NEW_PLAYLIST: {
                long[] list = new long[] {mCurrentTrackId};
                MusicUtils.addToNewPlaylist(this, list, NEW_PLAYLIST);
                return true;
            }
    
            case PLAYLIST_SELECTED: {
                long[] list = new long[] {mCurrentTrackId};
                long playlist = item.getIntent().getLongExtra("playlist", 0);
                MusicUtils.addToPlaylist(this, list, playlist);
                return true;
            }
    
            case USE_AS_RINGTONE:
                // Set the system setting to make this the current ringtone
                MusicUtils.setRingtone(this, mCurrentTrackId);
                return true;
    
            case DELETE_ITEM: {
                long[] list = new long[1];
                list[0] = (int) mCurrentTrackId;
                String f;
                if (android.os.Environment.isExternalStorageRemovable()) {
                    f = getString(R.string.delete_song_desc);
                } else {
                    f = getString(R.string.delete_song_desc_nosdcard);
                }
                String desc = String.format(f, mCurrentTrackName);
                MusicUtils.deleteItems(this, desc, list);
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, SLEEP, 0, R.string.sleep_start).setIcon(R.drawable.sleep_mode);
//        menu.add(0, FOLDER, 0, R.string.folder).setIcon(R.drawable.folder);
        menu.add(0, SEARCH, 0, R.string.search).setIcon(R.drawable.search);
        menu.add(0, SETTINGS, 0, R.string.settings).setIcon(R.drawable.setting);
        
        return true;
    }
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
//        if(!MusicMainEntryActivity.IsCanUseSdCard()) {
//            menu.getItem(1).setEnabled(false);
//        } else {
//            menu.getItem(1).setEnabled(true);
//        }        

        if(PreferenceManager.getDefaultSharedPreferences(this).getInt("sleep_mode_time", 0) == 0) {
            menu.getItem(0).setTitle(R.string.sleep_start);
        } else {
            menu.getItem(0).setTitle(R.string.sleep_close);
        }        
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case SEARCH: {
                Intent intent = new Intent();
                intent.setClass(this, SearchLocalSongsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }
            case SETTINGS: {
                Intent musicPreferencesIntent = new Intent().setClass(this, MusicSetting.class);
                musicPreferencesIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(musicPreferencesIntent);
                return true;
            }
            case FOLDER: {
                Intent intent = new Intent();
                intent.setClass(this, MusicFolderActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }
            case SLEEP: {
                new SleepModeManager(this);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
    //    finish();
        Intent sPlayIntent = new Intent();
        sPlayIntent.setClass(mContext, MediaPlaybackHomeActivity.class);
        mContext.startActivity(sPlayIntent);

        return false;
    }
    
    public void setupNoSongsView() {
        View view = findViewById(ID_MUSIC_NO_SONGS_CONTENT);
        
        if (MusicUtils.mHasSongs == false) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            } else {
                view = getLayoutInflater().inflate(R.layout.music_no_songs, mListView, false);    
                view.setId(ID_MUSIC_NO_SONGS_CONTENT);
                view.setPadding(0, mNoSongsPaddingTop, 0, 0);
                addContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            }
            TextView txtView = (TextView)view.findViewById(R.id.text_no_songs);
            String status = Environment.getExternalStorageState();
            if ( !(status.equals(Environment.MEDIA_MOUNTED))) {
                txtView.setText(R.string.nosd);
            } else {
                if(mFolderPath == null) {
                    txtView.setText(R.string.no_songs);
                } else {
                    txtView.setText(R.string.no_folders);
                }
            }            
        } else {
            if (view != null)
                view.setVisibility(View.GONE);
        }
    }

    @Override
    public void onContentChanged() {
        // TODO Auto-generated method stub
        super.onContentChanged();
        setupNoSongsView();
    }
    
    
}
