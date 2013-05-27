package com.lewa.player.ui.outer;

import java.util.ArrayList;

import com.lewa.player.ExitApplication;
import com.lewa.player.MusicSetting;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.SleepModeManager;
import com.lewa.player.model.RandomPlaylistTrackAdapter;
import com.lewa.player.model.PlaylistTrackCursorAdapter;
import com.lewa.player.online.LocalAsync;
import com.lewa.player.online.OnlineLoader;
import com.lewa.player.ui.AddPlaylistActivity;
import com.lewa.player.ui.MediaPlaybackHomeActivity;
import com.lewa.player.ui.NowPlayingController;
import com.lewa.player.ui.view.TouchInterceptor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;

public class PlaylistTrackBrowserActivity extends Activity 
        implements View.OnCreateContextMenuListener, MusicUtils.Defs, View.OnTouchListener{

    private static final int REMOVE = CHILD_MENU_BASE + 2;
    private static final int ID_MUSIC_NO_SONGS_CONTENT = 100;
    
	private TouchInterceptor ti;
	private ListAdapter mAdapter;
	private Context mContext;
	private long[] TrackList;
    private boolean mDeletedOneRow = false;
    private int mSelectedPosition;
    private long mSelectedId;
    private String mCurrentTrackName;
    private String mPlaylist;
    private int mIfRandomList;
    private Cursor mTrackCursor;
    private int mPlaylistId;
	private String mTitle = "";
	private int mNoSongsPaddingTop;
    NowPlayingController npc;
    LocalAsync labg;
    private LinearLayout mLinear;
    private TextView mTitleText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
//        IntentFilter f = new IntentFilter();        
//        f.addAction(AddPlaylistActivity.ACTION_ADDPLAYLIST);
//        registerReceiver(receiver, new IntentFilter(f));  
        
		setContentView(R.layout.playlist_trackbrowser);
		mContext = this;
		mPlaylist = "nowplay";
		Intent transInt = this.getIntent();
		if(transInt != null) {
		    mIfRandomList = transInt.getIntExtra("ifRandom", 1);
			if(mIfRandomList == 0) {
                TrackList = MusicUtils.getFavouriteTracks(this);
				mAdapter = new RandomPlaylistTrackAdapter(this, TrackList);
				mTitle = getString(R.string.favourite_list_title);
			} else if(mIfRandomList == 1){
				TrackList = transInt.getLongArrayExtra("randomtrack");
				mAdapter = new RandomPlaylistTrackAdapter(this, TrackList);
				mTitle = getString(R.string.random_list_title);
			} else {
			    TrackList = transInt.getLongArrayExtra("playlistcurrent");
			    mPlaylistId = transInt.getIntExtra("playlistid", 2);
			    mAdapter = new PlaylistTrackCursorAdapter(mContext, this, 
			            R.layout.edit_track_list_item,
	                    null, // cursor
	                    new String[] {}, new int[] {});
			    getPlaylistCursor(((PlaylistTrackCursorAdapter)mAdapter).getQueryHandler(), null, true);
			    mTitle = transInt.getStringExtra("title");
			}
		}
		npc = (NowPlayingController) findViewById(R.id.music_title);
		ti = (TouchInterceptor) findViewById(R.id.curlistTouch);
		ti.setAdapter(mAdapter);
		ti.setOnItemClickListener(itemlis);
		ti.setOnCreateContextMenuListener(this);
	    ti.setDivider(null);
	    
//	    ImageView backImg = (ImageView)findViewById(R.id.nowplayingimage);
//        backImg.setImageDrawable(null);
//        backImg.setImageResource(R.drawable.top_back);
//        backImg.setOnTouchListener(this);
   //     npc.findViewById(R.id.nowplayingText).setOnTouchListener(this);
	    
	    if(MusicUtils.sService != null && npc != null) {
	        npc.setMediaService(MusicUtils.sService);
        }
		if(mAdapter instanceof PlaylistTrackCursorAdapter) {
            ((TouchInterceptor) ti).setDropListener(mDropListener);
            ((TouchInterceptor) ti).setRemoveListener(mRemoveListener);
		}
        //ti.setSelector(R.drawable.playlist_tile_drag);
		ti.setCacheColorHint(0);
		if(MusicUtils.sService != null) {
            npc.setMediaService(MusicUtils.sService);
		}
		Resources resources = getResources();
		mNoSongsPaddingTop = resources.getDimensionPixelOffset(R.dimen.no_songs_padding_top);
		setupNoSongsView();
		
		mTitleText = (TextView)findViewById(R.id.list_title);
		mTitleText.setText(mTitle);
		
		ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
	}

/*	BroadcastReceiver receiver  = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(intent.getAction().equals(AddPlaylistActivity.ACTION_ADDPLAYLIST)) {
                mTitle = intent.getStringExtra("title");
                if(mTitle != null && npc != null) {
                    npc.setActionBarName(mTitle);
                }
            }
        }
    };*/
	
	private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener() {
        public void drop(int from, int to) {
                // update a saved playlist
            MediaStore.Audio.Playlists.Members.moveItem(
                    mContext.getContentResolver(), mPlaylistId, from, to);
            PlaylistTrackCursorAdapter cursorAdapter = (PlaylistTrackCursorAdapter) mAdapter;
            cursorAdapter.notifyDataSetChanged();
            ti.invalidateViews();
        }
    };
    
    private TouchInterceptor.RemoveListener mRemoveListener =
        new TouchInterceptor.RemoveListener() {
        public void remove(int which) {
            removePlaylistItem(which);
        }
    };
    
    private void removePlaylistItem(int which) {
        View v = ti.getChildAt(which - ti.getFirstVisiblePosition());
        if (v == null) {
            //Log.d(LOGTAG, "No view when removing playlist item " + which);
            return;
        }
        try {
            if (MusicUtils.sService != null
                    && which != MusicUtils.sService.getQueuePosition()) {
                mDeletedOneRow = true;
            }
        } catch (RemoteException e) {
            // Service died, so nothing playing.
            mDeletedOneRow = true;
        }
        v.setVisibility(View.GONE);
        ti.invalidateViews();
        int colidx = mTrackCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members._ID);
        mTrackCursor.moveToPosition(which);
        long id = mTrackCursor.getLong(colidx);
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",
                mPlaylistId);
        mContext.getContentResolver().delete(
                ContentUris.withAppendedId(uri, id), null, null);
        v.setVisibility(View.VISIBLE);
        ti.invalidateViews();
        PlaylistTrackCursorAdapter cursorAdapter = (PlaylistTrackCursorAdapter) mAdapter;
        cursorAdapter.notifyDataSetChanged();
        String message = getString(R.string.remove_done);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
	
	OnItemClickListener itemlis = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
	        if (TrackList == null || TrackList.length == 0) {
	            return;
	        }
	        // When selecting a track from the queue, just jump there instead of
	        // reloading the queue. This is both faster, and prevents accidentally
	        // dropping out of party shuffle.

	        
	        MusicUtils.playAll(mContext, TrackList, position);
	        Intent i = new Intent();
	        i.setClass(mContext, MediaPlaybackHomeActivity.class);
	        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        mContext.startActivity(i);			
		}		
	};
	
	public long[] getRandemSong() {
		return TrackList;
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
	    if(npc != null) {
	       npc.destroyNowplaying();
	       npc = null;
	    }

		TrackList = null;
		if(mAdapter instanceof PlaylistTrackCursorAdapter) {
    		((TouchInterceptor) ti).setDropListener(null);
            ((TouchInterceptor) ti).setRemoveListener(null);
            if (mAdapter != null) {
                ((PlaylistTrackCursorAdapter)mAdapter).changeCursor(mTrackCursor);
            }
		}
//		this.unregisterReceiver(receiver);
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
		mLinear = (LinearLayout) findViewById(R.id.linear_playlist);
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
    public void onCreateContextMenu(ContextMenu menu, View view,
            ContextMenuInfo menuInfoIn) {
        menu.add(0, PLAY_SELECTION, 0, R.string.play_selection);
//        menu.add(0, SHARE_LIST, 0, R.string.share_list);                
        
        if(mAdapter instanceof PlaylistTrackCursorAdapter) {            
            menu.add(0, DELETE_ITEM, 0, R.string.delete_item);
        }        
        SubMenu sub = menu.addSubMenu(0, ADD_TO_PLAYLIST, 0, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(this, sub);
        menu.add(0, USE_AS_RINGTONE, 0, R.string.ringtone_menu);
        if(mAdapter instanceof PlaylistTrackCursorAdapter) {            
            menu.add(0, REMOVE, 0, R.string.remove_from_playlist);
        }        
        
        AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfoIn;
        mSelectedPosition = mi.position;
        mSelectedId = TrackList[mSelectedPosition];
        mCurrentTrackName = MusicUtils.getSongName(mContext, mSelectedId)[0];
        menu.setHeaderTitle(mCurrentTrackName);
    }
	
	@Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case PLAY_SELECTION: {
                // play the track
                int position = mSelectedPosition;
                MusicUtils.playAll(this, TrackList, position);
                return true;
            }
    
            case QUEUE: {
                long[] list = new long[] { mSelectedId };
                MusicUtils.addToCurrentPlaylist(this, list);
                return true;
            }
    
            case NEW_PLAYLIST: {
                long [] list = new long[] { mSelectedId };
                MusicUtils.addToNewPlaylist(this, list, NEW_PLAYLIST);
                return true;
            }
    
            case PLAYLIST_SELECTED: {
                long[] list = new long[] { mSelectedId };
                long playlist = item.getIntent().getLongExtra("playlist", 0);
                MusicUtils.addToPlaylist(this, list, playlist);
                return true;
            }
    
            case USE_AS_RINGTONE:
                // Set the system setting to make this the current ringtone
                MusicUtils.setRingtone(this, mSelectedId);
                return true;
    
            case DELETE_ITEM: {
                long[] list = new long[1];
                list[0] = (int) mSelectedId;
                Bundle b = new Bundle();
                String f;
                if (android.os.Environment.isExternalStorageRemovable()) {
                    f = getString(R.string.delete_song_desc);
                } else {
                    f = getString(R.string.delete_song_desc_nosdcard);
                }
                String desc = String.format(f, mCurrentTrackName);            
                b.putString("description", desc);            
                b.putLongArray("items", list);
                
//                Intent intent = new Intent();
//                intent.setClass(this, DeleteItems.class);
//                intent.putExtras(b);
//                startActivityForResult(intent, -1);
                MusicUtils.deleteItems(this, desc, list);
                return true;
            }
    
            case REMOVE:
                removePlaylistItem(mSelectedPosition);
                return true;
       }
       return super.onContextItemSelected(item);
    }        
    public Cursor getPlaylistCursor(PlaylistTrackCursorAdapter.QueryHandler queryhandler,
            String filter, boolean async) {
        Cursor ret = null;
        StringBuilder where = new StringBuilder();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", mPlaylistId);
        where.append(MediaStore.Audio.Media.TITLE + " != ''"); 
        where.append(MusicUtils.getWhereBuilder(mContext, "_id", 1));
        ret = queryhandler.doQuery(uri, null, where.toString(), null, 
                MediaStore.Audio.Playlists.Members.PLAY_ORDER, async);
        return ret;
    }
    
    public void init(Cursor newCursor) {

        if (mAdapter == null || !(mAdapter instanceof PlaylistTrackCursorAdapter)) {
            return;
        }
        ((PlaylistTrackCursorAdapter)mAdapter).changeCursor(newCursor);
    }
    
    public Cursor getPlaylistCursor() {
        return mTrackCursor;
    }
    
    public void setPlaylistCursor(Cursor cursor) {
        mTrackCursor = cursor;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, SLEEP, 0, R.string.sleep_start).setIcon(R.drawable.sleep_mode);
        if (mIfRandomList == 0 || mIfRandomList == 1) {
            menu.add(0, SAVE_TO_PLAYLIST, 0, R.string.save_to_playlist).setIcon(R.drawable.save_to_playlist);
        } else {
            menu.add(0, EDIT_PLAYLIST, 0, R.string.edit_playlist).setIcon(R.drawable.edit_list);
        }
        menu.add(0, SETTINGS, 0, R.string.settings).setIcon(R.drawable.setting);
        
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);      

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
            case EDIT_PLAYLIST: 
            case SAVE_TO_PLAYLIST: {    
                Intent intent = new Intent();
                intent.setClass(mContext, AddPlaylistActivity.class); 
                if (mIfRandomList != 2 && TrackList != null && TrackList.length != 0) {
                    ArrayList<Integer> songsId = new ArrayList<Integer>();
                    for (int i = 0; i < TrackList.length; i++) {
                        songsId.add(new Integer((int)(TrackList[i])));
                    }
                    intent.putIntegerArrayListExtra("playlist_songs_id", songsId);
                } else if (mIfRandomList == 2) {
                    Cursor cursor = ((PlaylistTrackCursorAdapter)mAdapter).getCursor();
                    if (cursor != null) {
                        int count = cursor.getCount();
                        if (count != 0) {
                            int songIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members.AUDIO_ID);
                            ArrayList<Integer> songsId = new ArrayList<Integer>();
                            for (int i = 0; i < count; i++) {
                                cursor.moveToPosition(i);
                                songsId.add(new Integer(cursor.getInt(songIdIdx)));
                            }
                            intent.putIntegerArrayListExtra("playlist_songs_id", songsId);
                        }
                    }
                }
                if(mIfRandomList == 2) {                    
                    intent.putExtra("playlist_name", mTitle);
                    intent.putExtra("playlist_id", mPlaylistId);
                } 
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
        finish();
        return false;
    }   
    
    public void updateListData() {
        TrackList = null;
        Cursor cursor = ((PlaylistTrackCursorAdapter)mAdapter).getCursor();
        if (cursor != null) {
            int count = cursor.getCount();
            TrackList = new long[count];
            if (count != 0) {
                int songIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members.AUDIO_ID);
                for (int i = 0; i < count; i++) {
                    cursor.moveToPosition(i);
                    TrackList[i] = cursor.getInt(songIdIdx);
                }
            }
        }
        setupNoSongsView();
    }
    
    public void setupNoSongsView() {
        View view = findViewById(ID_MUSIC_NO_SONGS_CONTENT);
        
        if (MusicUtils.mHasSongs == false || TrackList == null || TrackList.length == 0) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            } else {
                view = getLayoutInflater().inflate(R.layout.music_no_songs, ti, false);    
                view.setId(ID_MUSIC_NO_SONGS_CONTENT);
          //      view.setPadding(0, mNoSongsPaddingTop, 0, 0);
                addContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            }
            TextView txtView = (TextView)view.findViewById(R.id.text_no_songs);
            String status = Environment.getExternalStorageState();
            if ( !(status.equals(Environment.MEDIA_MOUNTED))) {
                txtView.setText(R.string.nosd);
            } else {
                txtView.setText(R.string.no_songs);
            }            
        } else {
            if (view != null)
                view.setVisibility(View.GONE);
        }
    }
}
