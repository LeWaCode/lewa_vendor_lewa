package com.lewa.player.ui.outer;

import java.io.File;
import java.util.ArrayList;

import com.lewa.player.model.PlaylistTrackCursorAdapter;
import com.lewa.player.model.RandomPlaylistAdapter;
import com.lewa.player.model.RandomPlaylistAdapter.PlaylistInfo;
import com.lewa.player.online.LocalAsync;
import com.lewa.player.online.OnlineLoader;
import com.lewa.player.ExitApplication;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.ui.AddPlaylistActivity;
import com.lewa.player.ui.NowPlayingController;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class PlaylistBrowserActivity extends Activity
        implements View.OnCreateContextMenuListener, MusicUtils.Defs {   
    public static final int SUBACTIVITY_NEW_PLAYLIST = 1;
    private static final int DELETE_PLAYLIST = CHILD_MENU_BASE + 1;
    private static final int SHARE_LIST = CHILD_MENU_BASE + 2;
    
    public static final String GA_TRACKING_KEY = "ga-tracking-key";
    public static String UPDATE_ALLSONGSIMG = "com.lewa.player.ui.update_allsongs";
    
    private ScrollView mScrollView;
    private LinearLayout mListLinear;
    private LinearLayout topLinear;
    private RelativeLayout topLinearbg;
    protected GridView mPlayListContent;
    protected RandomPlaylistAdapter mGridAdapter;
    private final static int RANDOM_PLAYLIST_COUNT = 21;
    private long[] mRandomListItemId;
    private Context mContext;
    private int mGridViewWidth = 0;
    private LocalAsync labg;
    private Cursor mCursor;
    private ContentResolver mResolver;
    private PlaylistInfo mPlayListSelected;
    private boolean mbHasSongsPre = true;    
    
    String[] mCols = new String[] {
            MediaStore.Audio.Playlists._ID,
            MediaStore.Audio.Playlists.NAME
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist_view);
		topLinearbg = (RelativeLayout) findViewById(R.id.randominfobg);
		topLinearbg.setFocusable(true);
		topLinearbg.setFocusableInTouchMode(true);
		topLinearbg.requestFocus();
/*		topLinear = (LinearLayout) findViewById(R.id.randominfo);
		topLinear.setFocusable(true);
		topLinear.setFocusableInTouchMode(true);
		topLinear.requestFocus();*/
		
	    mListLinear = (LinearLayout)findViewById(R.id.playlist_linear);
	    TextView RandomListTitle = (TextView)findViewById(R.id.randomlisttitle);
	    RandomListTitle.setText(R.string.title_outer_alltrack);
	    
	    IntentFilter filter = new IntentFilter();
	    filter.addAction(AddPlaylistActivity.ACTION_ADDPLAYLIST);
//	    filter.addAction(MusicUtils.ACTION_DELETEITEM);
//	    filter.addAction(UPDATE_ALLSONGSIMG);
	    filter.addAction(NowPlayingController.UPDATE_NOWPLAYINGALBUM);
        this.registerReceiver(receiver, filter);
        
        RandomClickListener randomClickListener = new RandomClickListener();
        topLinearbg.setOnClickListener(randomClickListener);
        topLinearbg.setOnCreateContextMenuListener(this);
        
        mScrollView = (ScrollView)findViewById(R.id.scroll_view);
        mScrollView.scrollTo(0, 0);
        mContext = this;
        mPlayListContent = (GridView)findViewById(R.id.playlist_grid);
        mPlayListContent.setSelector(new ColorDrawable(Color.TRANSPARENT));
        
        mPlayListContent.setOnItemClickListener(ols);
        mGridAdapter = new RandomPlaylistAdapter(this); 
        mPlayListContent.setAdapter(mGridAdapter);
        
        mResolver = getContentResolver();
        startQuery();
        
        SetGridViewLayoutParams();
        mPlayListContent.setFastScrollEnabled(false);
        
            
//        mRandomListItemId = getRandomSongsList();    
//        if(mRandomListItemId != null) {
//            int count = mRandomListItemId.length;
//            if (count > 0) {
//                setPlaylistHomeBackGround(mRandomListItemId[0]);
//                TextView RandomListCount = (TextView)findViewById(R.id.randomlistcount);
//                String string = getResources().getQuantityString(R.plurals.Nsongs, count, count);
//                RandomListCount.setText(string);
//            }
//        } 
        
        registerProviderStatusObserver();
        
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        f.addDataScheme("file");
        registerReceiver(mScanListener, f);
        
        ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);        
    }
	
    public class QueryFilterAsync extends AsyncTask<long [], Integer, long []>{

        @Override
        protected long[] doInBackground(long []... arg0) {
            // TODO Auto-generated method stub
            mRandomListItemId = MusicUtils.getAllSongs(mContext);  //getRandomSongsList();
            return mRandomListItemId;
        }
        
        @Override
        protected void onPostExecute(long[] holder) {
            // TODO Auto-generated method stub
            //super.onPostExecute(result);
            setCount();
        }       
    }
    
    private void setCount() {
        if (mRandomListItemId != null) {
            int count = mRandomListItemId.length;
            if (count > 0) {
                //setPlaylistHomeBackGround(mRandomListItemId[0]);
                topLinearbg.post(new Runnable() {                
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {                    
                            if(MusicUtils.sService != null && MusicUtils.sService.getQueuePosition() >= 0) {
                                setPlaylistHomeBackGround(MusicUtils.sService.getAudioId());
                            } else {
                                OnlineLoader.setContext(mContext);
                                OnlineLoader.SendtoUpdate(null);
                            }
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
                TextView RandomListCount = (TextView) findViewById(R.id.randomlistcount);
                String string = getResources().getQuantityString(
                        R.plurals.Nsongs, count, count);
                RandomListCount.setText(string);
            }
        }
    }
    
    //mScanListener.onReceive or mProviderStatusObserver.onChange, 1 of 2
    boolean isScanned = false;
   
    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                isScanned = true;
            } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)){
                resetPlaylistView(); 
                isScanned = false;
            }
        }
    };

	@Override
    protected void onDestroy() {
        // TODO Auto-generated method stub        
        mRandomListItemId = null;
        mPlayListSelected = null;
        this.unregisterReceiver(receiver);
		this.unregisterReceiver(mScanListener);
        unregisterProviderStatusObserver();
        if(mCursor != null)
        mCursor.close();
//        if(labg != null) {
//            labg.restorePreArtistName();
//        }
        
        super.onDestroy();
    }

    private void registerProviderStatusObserver() {
        mResolver.registerContentObserver(Uri.parse("content://media"),
                true, mProviderStatusObserver);

    }

    private void unregisterProviderStatusObserver() {
        mResolver.unregisterContentObserver(mProviderStatusObserver);
    }
    
    private ContentObserver mProviderStatusObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            if(isScanned) {
                return;
            }
            resetPlaylistView();
        }
    };
    
    public void resetPlaylistView() {
        mRandomListItemId = MusicUtils.getAllSongs(mContext);//getRandomSongsList();
        setCount();
        mGridAdapter = new RandomPlaylistAdapter(mContext);
        if(mGridAdapter != null) {
            mPlayListContent.setAdapter(mGridAdapter);
            startQuery();
        }  
    }

    BroadcastReceiver receiver  = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if((AddPlaylistActivity.ACTION_ADDPLAYLIST).equals(intent.getAction())) {
//                mGridAdapter.notifyDataSetChanged();
//                mPlayListContent.invalidateViews();
                startQuery();
            } else if((MusicUtils.ACTION_DELETEITEM).equals(intent.getAction())) {
                @SuppressWarnings("deprecation")
                long [] deleteIetmIdList = (long[]) intent.getExtra("deleteItemId");
                long deleteItemId = deleteIetmIdList[0];
                if(mRandomListItemId != null) {
                    int len = mRandomListItemId.length;
                    for(int i = 0; i < len; i++) {
                        if(deleteItemId == mRandomListItemId[i]) {
                            mRandomListItemId = MusicUtils.getAllSongs(mContext); //getRandomSongsList();
                            setCount();
                            break;
                        }
                    }
                }
                startQuery();
            }  else if(UPDATE_ALLSONGSIMG.equals(intent.getAction())) {
//                try {
//                    if(MusicUtils.sService != null && MusicUtils.sService.getQueuePosition() >= 0) {
//                        setPlaylistHomeBackGround(MusicUtils.sService.getAudioId());
//                    }
//                } catch (RemoteException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
                Bitmap bm = intent.getParcelableExtra("artistbitmap");
                if(bm != null) {
                    BitmapDrawable db = new BitmapDrawable(getResources(), bm);
                    if(db != null) {
                        topLinearbg.setBackgroundDrawable(db);
                    }
                }
            } else if(intent.getAction().equals(NowPlayingController.UPDATE_NOWPLAYINGALBUM)) {
                LocalAsync labg = new LocalAsync(PlaylistBrowserActivity.this);
                
                try { 
                    long artistId = MusicUtils.sService.getAudioId();
                    labg.setArtistImg(artistId);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }                
            }
        }
    };

    public void SetGridViewLayoutParams() {
        int plus = mGridAdapter.getCount() % 2;
        int vSpaceLine = mGridAdapter.getCount() / 2;
        int lines = plus > 0 ? vSpaceLine + 1 : vSpaceLine;
        mGridViewWidth = getResources().getDimensionPixelOffset(R.dimen.grid_width);
        mPlayListContent.setLayoutParams(new LinearLayout.LayoutParams(mGridViewWidth,
                                lines *(mGridAdapter.getHeight() + mGridAdapter.getPaddingBottom())));
	}
	
    public void setPlaylistHomeBackGround(long songid) {
    	labg = new LocalAsync(this);
    	labg.setArtistImg(songid);
    }

    public void startQuery() {
        mCursor = mResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mCols, 
                " name != '" + getString(R.string.record) + "'", null, null);
        mGridAdapter.setPlaylistCursor(mCursor);
        mGridAdapter.changeCursor(mCursor);
        SetGridViewLayoutParams();
    }
	
	public void setGridViewBack(Bitmap back) {
		if(back == null) {
			//Random random = new Random();
	        //int resourceId = random.nextInt(3);
	        //topLinearbg.setBackgroundResource(R.drawable.playlist_default_0 + resourceId);
			return ;
		}
		Drawable db  = new BitmapDrawable(back);
		topLinearbg.setBackgroundDrawable(db);
	}
	
	OnItemClickListener ols = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> v, View arg1, int position,
				long id) {
			// TODO Auto-generated method stub
			if(position == 0) {
	            Intent intent = new Intent();
	            intent.setClass(mContext, PlaylistTrackBrowserActivity.class);
	            intent.putExtra("ifRandom", 0);
	            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
			}
		}		
	};
	
	private long[] getFavouriteList() {
		
		long[] mFavouriteList = null;
		return mFavouriteList;		
		
	}
	
	private long[] getRandomSongsList() {
	    long[] RandomSongs = new long[RANDOM_PLAYLIST_COUNT];
	    long[] allSongs = MusicUtils.getAllSongs(this);
	    if(allSongs == null)return null;
	    int length = allSongs.length;
	    int trackId;
	    if(length <= RANDOM_PLAYLIST_COUNT) {
	        RandomSongs = allSongs;	        
	    } else {
            int i = 0;
            boolean repeat = false;
            while (i < RANDOM_PLAYLIST_COUNT) {
                trackId = (int)(Math.random() * allSongs.length + 0);
                if (i > 0) {
                    int j = i - 1;
                    while (j + 1 > 0) {
                        if (allSongs[trackId] == RandomSongs[j]) {
                            repeat = true;
                            break;
                        }
                        j--;
                    }
                    if (repeat) {
                        repeat = false;
                        continue;
                    }
                }
                RandomSongs[i] = allSongs[trackId];
                i++;
            }
        }
	    return RandomSongs; 
	}
	
	private class RandomClickListener implements android.view.View.OnClickListener {

        @Override
        public void onClick(View view) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();            
            intent.setClass(mContext, AllTrackBrowserActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfoIn) {
        menu.add(0, PLAY_SELECTION, 0, R.string.play_selection);
//        menu.add(0, SHARE_LIST, 0, R.string.share_list);        
//        SubMenu sub = menu.addSubMenu(0, ADD_TO_PLAYLIST, 0, R.string.add_to_playlist);
//        MusicUtils.makePlaylistMenu(this, sub);        
        menu.setHeaderTitle(R.string.title_outer_alltrack);
        PlaylistInfo info =  new PlaylistInfo();
        info.playlistId = 0;
        info.songsId = mRandomListItemId;
        setPlaylistSelected(info);
    }
    
    public void setPlaylistSelected(PlaylistInfo list) {        
        mPlayListSelected = list;
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        
        switch (item.getItemId()) {
            case PLAY_SELECTION: {
                // play the selected album
                MusicUtils.playAll(mContext, mPlayListSelected.songsId, 0);
                return true;
            }

            case PLAYLIST_SELECTED: {
                long playlist = item.getIntent().getLongExtra("playlist", 0);
                MusicUtils.addToPlaylist(this, mPlayListSelected.songsId, playlist);
                return true;
            }
            
            case QUEUE: {
                MusicUtils.addToCurrentPlaylist(this, mPlayListSelected.songsId);
                return true;
            }

            case NEW_PLAYLIST: {
                MusicUtils.addToNewPlaylist(this, mPlayListSelected.songsId, -1);
                return true;
            }

            case SHARE_LIST: {
                return true;
            }
            
            case DELETE_ITEM: {
                Uri uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mPlayListSelected.playlistId);
                getContentResolver().delete(uri, null, null);
                Toast.makeText(this, R.string.playlist_deleted_message, Toast.LENGTH_SHORT).show();
                deletePlaylistCover(mPlayListSelected.playlistId);
                SetGridViewLayoutParams();
//                if (mGridAdapter.getCursor().getCount() == 0) {
//                    setTitle(R.string.no_playlists_title);
//                }
                return true;
            }
            
            case EDIT_PLAYLIST: {
                Intent intent = new Intent();
                intent.setClass(mContext, AddPlaylistActivity.class); 
               // Cursor cursor = ((PlaylistTrackCursorAdapter) mAdapter).getCursor();
                Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                        "external", mPlayListSelected.playlistId);
                StringBuilder where = new StringBuilder();
                where.append(MediaStore.Audio.Media.TITLE + " != ''"); 
                where.append(MusicUtils.getWhereBuilder(mContext, "_id", 1));
                Cursor cursor = MusicUtils.query(this, uri, null, where.toString(), null, 
                        MediaStore.Audio.Playlists.Members.PLAY_ORDER);
                if (cursor != null) {
                    int count = cursor.getCount();
                    if (count != 0) {
                        int songIdIdx = cursor
                                .getColumnIndexOrThrow(MediaStore.Audio.Genres.Members.AUDIO_ID);
                        ArrayList<Integer> songsId = new ArrayList<Integer>();
                        for (int i = 0; i < count; i++) {
                            cursor.moveToPosition(i);
                            songsId.add(new Integer(cursor.getInt(songIdIdx)));
                        }
                        intent.putIntegerArrayListExtra("playlist_songs_id",
                                songsId);
                    }
                }
                intent.putExtra("playlist_name", mPlayListSelected.playlistName);
                intent.putExtra("playlist_id", mPlayListSelected.playlistId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }
    
    public void deletePlaylistCover(int playlistId) {
        String sdCardDir = Environment.getExternalStorageDirectory()+"/LEWA/music/playlist/cover/"+playlistId;
        File file = new File(sdCardDir);
        if(file.exists()){
            file.delete();
        }
    }
    
    public void setupNoSongsView() {
        if (false == MusicUtils.mHasSongs) {
            mbHasSongsPre = false;            
            topLinearbg.post(new Runnable() {                
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Bitmap bg = MusicUtils.getDefaultBg(PlaylistBrowserActivity.this, 0);
                    topLinearbg.setBackgroundDrawable(new BitmapDrawable(bg));
                }
            });            
            TextView RandomListCount = (TextView)findViewById(R.id.randomlistcount);
            String string = getResources().getQuantityString(R.plurals.Nsongs, 0, 0);
            RandomListCount.setText(string); 
        } else {
            if (mbHasSongsPre == false) {
//                QueryFilterAsync queryfilter = new QueryFilterAsync();
//                queryfilter.execute(null);
                mbHasSongsPre = true;
            }
        }
    }

    public ScrollView getScrollView() {
        return mScrollView;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (true == MusicUtils.mHasSongs) {
            QueryFilterAsync queryfilter = new QueryFilterAsync();
            queryfilter.execute(null);
            mbHasSongsPre = true;
        } else {
            setupNoSongsView();
        }
        mGridAdapter.notifyDataSetChanged();        

//        updateFolderPathInDB();
    }
    
    private void updateFolderPathInDB() {
        ArrayList<String> folderPath = MusicUtils.getPathList(this);
        String [] pathInDB = MusicUtils.getFolderPath(this);
        
        ArrayList<String> pathToSave = new ArrayList<String>();
        String [] pathResult;
        if(pathInDB != null && pathInDB.length > 0) {
            int len = pathInDB.length;
            for (int i = 0; i < len; i++) {
                if (folderPath.contains(pathInDB[i])) {
                    pathToSave.add(pathInDB[i]);
                }
            }
            
            int size = pathToSave.size();
            pathResult = new String[size];
            for (int i = 0; i < size; i++) {
                pathResult[i] = pathToSave.get(i);
            }
        } else {
            pathResult = pathInDB;
        }
        
        MusicUtils.updateFolderPath(this, pathResult);
    }    
}

	

