package com.lewa.player.ui.outer;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.AbstractCursor;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Playlists;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.lewa.player.ExitApplication;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicSetting;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.SleepModeManager;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;

import com.lewa.player.MusicUtils.ServiceToken;
import com.lewa.player.model.AllTrackBrowserAdapter;
import com.lewa.player.model.NowPlayingCursor;
import com.lewa.player.online.LocalAsync;
import com.lewa.player.online.OnlineLoader;
import com.lewa.player.ui.MusicFolderActivity;
import com.lewa.player.ui.NowPlayingController;
import com.lewa.player.ui.SearchLocalSongsActivity;

public class AllTrackBrowserActivity extends ListActivity implements
        View.OnCreateContextMenuListener, MusicUtils.Defs, ServiceConnection {
    private static final int SAVE_AS_PLAYLIST = CHILD_MENU_BASE + 2;
    private static final int REMOVE = CHILD_MENU_BASE + 5;
    private static final int SEARCH = CHILD_MENU_BASE + 6;

    private static final String LOGTAG = "TrackBrowser";
    private static final int ID_MUSIC_NO_SONGS_CONTENT = 100;

    private String[] mCursorCols;
    private boolean mDeletedOneRow = false;
    private String mCurrentTrackName;
    private String mCurrentAlbumName;
    private String mCurrentArtistNameForAlbum;
    private ListView mTrackList;
    private static Cursor mTrackCursor;
    private AllTrackBrowserAdapter mAdapter;
    private boolean mAdapterSent = false;
    private String mAlbumId;
    private String mArtistId;
    private String mPlaylist;
    private String mGenre;
    private String mSortOrder;
    private int mSelectedPosition;
    private long mSelectedId;
    private static int mLastListPosCourse = -1;
    private static int mLastListPosFine = -1;
    private boolean mUseLastListPos = false;
    private ServiceToken mToken;
    private NowPlayingController Artistactionbar;
    LocalAsync labg;
    private LinearLayout mLinear;
    private int mNoSongsPaddingTop;
    private Context mContext;

    public AllTrackBrowserActivity() {
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mContext = this;
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getBooleanExtra("withtabs", false)) {
                requestWindowFeature(Window.FEATURE_NO_TITLE);
            }
            mArtistId = intent.getStringExtra("artist");
        }
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if(mArtistId == null) {
            setContentView(R.layout.alltracklist);
            Artistactionbar = (NowPlayingController) findViewById(R.id.nowplaying_track);
            if(MusicUtils.sService != null && Artistactionbar != null) {
                Artistactionbar.setMediaService(MusicUtils.sService);
            }
        }

        mCursorCols = new String[] { MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.DURATION,
                "sort_key" };
        
        // setContentView(R.layout.media_picker_activity);
        // mUseLastListPos = MusicUtils.updateButtonBar(this, R.id.songtab);
        mTrackList = getListView();
        mTrackList.setOnCreateContextMenuListener(this);
        mTrackList.setBackgroundDrawable(null);
        mTrackList.setCacheColorHint(0);
        mTrackList.setDivider(null);
        mTrackList.setFastScrollEnabled(true);
		
        mTrackList.setTextFilterEnabled(true);
        mAdapter = (AllTrackBrowserAdapter) getLastNonConfigurationInstance();

        if (mAdapter != null) {
            mAdapter.setActivity(this);
            setListAdapter(mAdapter);
        }
        mToken = MusicUtils.bindToService(this, this);

        mNoSongsPaddingTop = getResources().getDimensionPixelOffset(R.dimen.no_songs_padding_top);
        setupNoSongsView();
        
        ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        f.addDataScheme("file");
        registerReceiver(mScanListener, f);        
        initAdapter();        
    }
    
    public void initAdapter() {
        if (mAdapter == null) {
            mAdapter = new AllTrackBrowserAdapter(
                    getApplication(), // need to use application context to
                                      // avoid leaks
                    this,
                    R.layout.all_tracks_list_item,
                    null, // cursor
                    new String[] {}, new int[] {},
                    "nowplaying".equals(mPlaylist),
                    mPlaylist != null
                            && !(mPlaylist.equals("podcasts") || mPlaylist
                                    .equals("recentlyadded")));
            setListAdapter(mAdapter);
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

    public void onServiceDisconnected(ComponentName name) {
        // we can't really function without the service, so don't
        finish();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        AllTrackBrowserAdapter a = mAdapter;
        mAdapterSent = true;
        return a;
    }

    @Override
    public void onDestroy() {
        ListView lv = getListView();
        if (lv != null) {
            if (mUseLastListPos) {
                mLastListPosCourse = lv.getFirstVisiblePosition();
                View cv = lv.getChildAt(0);
                if (cv != null) {
                    mLastListPosFine = cv.getTop();
                }
            }
        }

        MusicUtils.unbindFromService(mToken);
        try {
            unregisterReceiverSafe(mTrackListListener);
        } catch (IllegalArgumentException ex) {
            // we end up here in case we never registered the listeners
        }

        if (!mAdapterSent && mAdapter != null) {
            mAdapter.changeCursor(mTrackCursor);
        }

        setListAdapter(null);
        mAdapter = null;
        unregisterReceiverSafe(mScanListener);
       
        super.onDestroy();
    }
    
    /**
     * Unregister a receiver, but eat the exception that is thrown if the
     * receiver was never registered to begin with. This is a little easier than
     * keeping track of whether the receivers have actually been registered by
     * the time onDestroy() is called.
     */
    private void unregisterReceiverSafe(BroadcastReceiver receiver) {
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTrackCursor != null) {
            getListView().invalidateViews();
        }
        MusicUtils.setSpinnerState(this);
        if(mArtistId == null) {
            mLinear = (LinearLayout)findViewById(R.id.linear_trackpage);
            mLinear.post(new Runnable() {
                
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    int id = R.drawable.playlist_default;
                    MusicUtils.setDefaultBackground(mContext, mLinear, id);
                }
            });
        }
    }

    @Override
    public void onPause() {
        mReScanHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    /*
     * This listener gets called when the media scanner starts up or finishes,
     * and when the sd card is unmounted.
     */
    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
                    || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                MusicUtils.setSpinnerState(AllTrackBrowserActivity.this);
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
        }
    };

    public void onSaveInstanceState(Bundle outcicle) {
        outcicle.putLong("selectedtrack", mSelectedId);
        outcicle.putString("artist", mArtistId);
        outcicle.putString("album", mAlbumId);
        outcicle.putString("playlist", mPlaylist);
        outcicle.putString("genre", mGenre);
        super.onSaveInstanceState(outcicle);
    }

    public void init(Cursor newCursor, boolean isLimited) {

        if (mAdapter == null) {
            return;
        }
        mAdapter.changeCursor(newCursor); // also sets mTrackCursor

        if (mTrackCursor == null) {
            MusicUtils.displayDatabaseError(this);
            closeContextMenu();
            mReScanHandler.sendEmptyMessageDelayed(0, 1000);
            return;
        }

        MusicUtils.hideDatabaseError(this);

        // Restore previous position
        if (mLastListPosCourse >= 0 && mUseLastListPos) {
            ListView lv = getListView();
            // this hack is needed because otherwise the position doesn't change
            // for the 2nd (non-limited) cursor
            lv.setAdapter(lv.getAdapter());
            lv.setSelectionFromTop(mLastListPosCourse, mLastListPosFine);
            if (!isLimited) {
                mLastListPosCourse = -1;
            }
        }

        // When showing the queue, position the selection on the currently
        // playing track
        // Otherwise, position the selection on the first matching artist, if
        // any
        IntentFilter f = new IntentFilter();
        f.addAction(MediaPlaybackService.META_CHANGED);
        f.addAction(MediaPlaybackService.QUEUE_CHANGED);
        String key = getIntent().getStringExtra("artist");
        if (key != null) {
            int keyidx = mTrackCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID);
            mTrackCursor.moveToFirst();
            while (!mTrackCursor.isAfterLast()) {
                String artist = mTrackCursor.getString(keyidx);
                if (artist.equals(key)) {
                    setSelection(mTrackCursor.getPosition());
                    break;
                }
                mTrackCursor.moveToNext();
            }
        }
        registerReceiver(mTrackListListener, new IntentFilter(f));
        mTrackListListener.onReceive(this, new Intent(
                MediaPlaybackService.META_CHANGED));
    }    
    
    private void removePlaylistItem(int which) {
        View v = mTrackList.getChildAt(which
                - mTrackList.getFirstVisiblePosition());
        if (v == null) {
            Log.d(LOGTAG, "No view when removing playlist item " + which);
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
        mTrackList.invalidateViews();
        if (mTrackCursor instanceof NowPlayingCursor) {
            ((NowPlayingCursor) mTrackCursor).removeItem(which);
        } else {
            int colidx = mTrackCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members._ID);
            mTrackCursor.moveToPosition(which);
            long id = mTrackCursor.getLong(colidx);
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                    "external", Long.valueOf(mPlaylist));
            getContentResolver().delete(ContentUris.withAppendedId(uri, id),
                    null, null);
        }
        v.setVisibility(View.VISIBLE);
        mTrackList.invalidateViews();
    }

    private BroadcastReceiver mTrackListListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getListView().invalidateViews();
        }
    };

    private BroadcastReceiver mNowPlayingListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MediaPlaybackService.META_CHANGED)) {
                getListView().invalidateViews();
            } else if (intent.getAction().equals(
                    MediaPlaybackService.QUEUE_CHANGED)) {
                if (mDeletedOneRow) {
                    // This is the notification for a single row that was
                    // deleted previously, which is already reflected in
                    // the UI.
                    mDeletedOneRow = false;
                    return;
                }
                // The service could disappear while the broadcast was in
                // flight,
                // so check to see if it's still valid
                if (MusicUtils.sService == null) {
                    finish();
                    return;
                }
//                if (mAdapter != null) {
//                    Cursor c = new NowPlayingCursor(MusicUtils.sService,
//                            mCursorCols);
//                    if (c.getCount() == 0) {
//                        finish();
//                        return;
//                    }
//                    mAdapter.changeCursor(c);
//                }
            }
        }
    };

    // Cursor should be positioned on the entry to be checked
    // Returns false if the entry matches the naming pattern used for
    // recordings,
    // or if it is marked as not music in the database.
    private boolean isMusic(Cursor c) {
        int titleidx = c.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int albumidx = c.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int artistidx = c.getColumnIndex(MediaStore.Audio.Media.ARTIST);

        String title = c.getString(titleidx);
        String album = c.getString(albumidx);
        String artist = c.getString(artistidx);
        if (MediaStore.UNKNOWN_STRING.equals(album)
                && MediaStore.UNKNOWN_STRING.equals(artist) && title != null
                && title.startsWith("recording")) {
            // not music
            return false;
        }

        int ismusic_idx = c.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);
        boolean ismusic = true;
        if (ismusic_idx >= 0) {
            ismusic = mTrackCursor.getInt(ismusic_idx) != 0;
        }
        return ismusic;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
            ContextMenuInfo menuInfoIn) {
        menu.add(0, PLAY_SELECTION, 0, R.string.play_selection);        
        menu.add(0, DELETE_ITEM, 0, R.string.delete_item);        
        SubMenu sub = menu.addSubMenu(0, ADD_TO_PLAYLIST, 0, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(this, sub);
        menu.add(0, USE_AS_RINGTONE, 0, R.string.ringtone_menu);
        
        AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfoIn;
        mSelectedPosition = mi.position;
        mTrackCursor = mAdapter.getCursor();
        mTrackCursor.moveToPosition(mSelectedPosition);
        try {
            int id_idx = mTrackCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
            mSelectedId = mTrackCursor.getLong(id_idx);
        } catch (IllegalArgumentException ex) {
            mSelectedId = mi.id;
        }
        mCurrentAlbumName = mTrackCursor.getString(mTrackCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        mCurrentArtistNameForAlbum = mTrackCursor.getString(mTrackCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        mCurrentTrackName = mTrackCursor.getString(mTrackCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        menu.setHeaderTitle(mCurrentTrackName);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case PLAY_SELECTION: {
            // play the track
            int position = mSelectedPosition;
            MusicUtils.playAll(this, mTrackCursor, position);
            return true;
        }

        case QUEUE: {
            long[] list = new long[] { mSelectedId };
            MusicUtils.addToCurrentPlaylist(this, list);
            return true;
        }

        case NEW_PLAYLIST: {
            long[] list = new long[] {mSelectedId};
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
        case REMOVE:
            removePlaylistItem(mSelectedPosition);
            return true;
        }
        return super.onContextItemSelected(item);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        mTrackCursor = mAdapter.getCursor();
        if (mTrackCursor == null || mTrackCursor.getCount() == 0) {
            return;
        }

        MusicUtils.playAll(this, mTrackCursor, position);
    }    

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        switch (requestCode) {
        case SCAN_DONE:
            if (resultCode == RESULT_CANCELED) {
                finish();
            } else {
                getTrackCursor(mAdapter.getQueryHandler(), null, true);
            }
            break;

        case NEW_PLAYLIST:
            if (resultCode == RESULT_OK) {
                Uri uri = intent.getData();
                if (uri != null) {
                    long[] list = new long[] { mSelectedId };
                    MusicUtils.addToPlaylist(this, list,
                            Integer.valueOf(uri.getLastPathSegment()));
                }
            }
            break;

        case SAVE_AS_PLAYLIST:
            if (resultCode == RESULT_OK) {
                Uri uri = intent.getData();
                if (uri != null) {
                    long[] list = MusicUtils.getSongListForCursor(mTrackCursor);
                    int plid = Integer.parseInt(uri.getLastPathSegment());
                    MusicUtils.addToPlaylist(this, list, plid);
                }
            }
            break;
        }
    }

    public Cursor getTrackCursor(
            AllTrackBrowserAdapter.TrackQueryHandler queryhandler,
            String filter, boolean async) {

        if (queryhandler == null) {
            throw new IllegalArgumentException();
        }

        Cursor ret = null;
        // mSortOrder = MediaStore.Audio.Media.TITLE_KEY;
        mSortOrder = "Upper(sort_key)";
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.TITLE + " != ''");
        
        where.append(MusicUtils.getWhereBuilder(this, "_id", 0));

        if (mArtistId != null) {
            where.append(" AND " + MediaStore.Audio.Media.ARTIST_ID + "="
                    + mArtistId);
        }
        where.append(" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        if (!TextUtils.isEmpty(filter)) {
            uri = uri.buildUpon()
                    .appendQueryParameter("filter", Uri.encode(filter)).build();
        }
        ret = queryhandler.doQuery(uri, mCursorCols, where.toString(), null,
                mSortOrder, async);

        if (ret != null && async) {
            init(ret, false);
        }
        return ret;
    }
    
    public String getAlbumId() {
        return mAlbumId;
    }
    
    public Cursor getTrackCursor() {
        return mTrackCursor;
    }
    
    public void setTrackCursor(Cursor cursor) {
        mTrackCursor = cursor;
    }
    
    public String getArtistId() {
        return mArtistId;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);
        
        menu.clear();
        
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
    
    public void setTrackActivityBackGround(String artist) {
        labg = new LocalAsync(this);
        labg.LocalArtistImg(artist);
    }
    
    public void setTrackBackground(Bitmap back) {
        if(back == null)return;
        MusicUtils.setBackground(mLinear, back);
    }
    
    public void setupNoSongsView() {
        View view = findViewById(ID_MUSIC_NO_SONGS_CONTENT);
        
        if (MusicUtils.mHasSongs == false&&MusicUtils.getFolderPath(AllTrackBrowserActivity.this).length>0) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            } else {
                view = getLayoutInflater().inflate(R.layout.music_no_songs, mTrackList, false);    
                view.setId(ID_MUSIC_NO_SONGS_CONTENT);
//                view.setPadding(0, mNoSongsPaddingTop, 0, 0);
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
