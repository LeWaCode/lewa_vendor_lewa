package com.lewa.player.ui;

import java.util.ArrayList;

import com.lewa.player.ExitApplication;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.model.AlbumBrowserAdapter;
import com.lewa.player.model.MusicFolderAdapter;
import com.lewa.player.model.SongsCountLoader;
import com.lewa.player.ui.outer.AlbumTrackBrowserActivity;
import com.lewa.player.ui.outer.AllTrackBrowserActivity;
import com.lewa.player.ui.outer.MusicMainEntryActivity;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class MusicFolderActivity extends ListActivity implements View.OnClickListener, MusicUtils.Defs{

    private static final int ID_MUSIC_NO_SONGS_CONTENT = 100;
    private ArrayList<String> mFolderPathInDB = new ArrayList<String>();    
    private ArrayList<String> mPathList = new ArrayList<String>();
    
    private MusicFolderAdapter mAdapter;
    private int mIsOuter;
    private LinearLayout mLinear;
    private NowPlayingController Artistactionbar;
    private String mSelectedPath;
    private int mNoSongsPaddingTop;
    
    public MusicFolderActivity() {
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);
        
        Intent intent = this.getIntent();
        mIsOuter = intent.getIntExtra("isOuter", 0);
        if(mIsOuter == 1) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setTheme(android.R.style.Theme_NoTitleBar);
            setContentView(R.layout.alltracklist);
            Artistactionbar = (NowPlayingController) findViewById(R.id.nowplaying_track);
            if(MusicUtils.sService != null && Artistactionbar != null) {
                Artistactionbar.setMediaService(MusicUtils.sService);
            }
            this.setTheme(android.R.style.Theme_Light_NoTitleBar);
            TextView title = (TextView) findViewById(R.id.track_title);
            title.setText(R.string.title_folder);
        } else {            
            setContentView(R.layout.select_folder);
            this.setTheme(android.R.style.Theme_Light);
            this.setTitle(R.string.select_folder);
        }
        mPathList = MusicUtils.getPathList(this);
        String[] folderPathinDB = MusicUtils.getFolderPath(this);
        
        mFolderPathInDB.clear();
        if (mPathList != null && folderPathinDB != null) {
            if (folderPathinDB.length > 0) {
                for (int i = 0; i < folderPathinDB.length; i++) {
                    mFolderPathInDB.add(folderPathinDB[i]);
                }
            } else {
                for (int i = 0; i < mPathList.size(); i++) {
                    mFolderPathInDB.add(mPathList.get(i));
                }
            }
            
            mAdapter = new MusicFolderAdapter(this, mPathList, mFolderPathInDB, mIsOuter);
//            initAdapter();
            if(mAdapter != null) {
                setListAdapter(mAdapter);
            }            
        }
        if(mIsOuter == 0) {
            findViewById(R.id.folder_done).setOnClickListener(this);
        }
        
        ListView lv = getListView();
        lv.setCacheColorHint(0);
        if(mIsOuter == 1) {
            lv.setDivider(null);
            lv.setOnCreateContextMenuListener(this);
            
            IntentFilter f = new IntentFilter();
            f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
            f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
            f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            f.addDataScheme("file");
            registerReceiver(mScanListener, f); 
            
            mNoSongsPaddingTop = getResources().getDimensionPixelOffset(R.dimen.no_songs_padding_top);
            setupNoSongsView();
        }
        
        ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
    }
    
    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
                    || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                MusicUtils.setSpinnerState(MusicFolderActivity.this);
                mReScanHandler.sendEmptyMessage(0);
            }
            if(Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                mReScanHandler.sendEmptyMessage(1);
            }   
        }
    };
    
    private Handler mReScanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1) {
//                mAdapter.setOuter(2);
                mAdapter = new MusicFolderAdapter(MusicFolderActivity.this, mPathList, mFolderPathInDB, 2);
            } else {
//                mAdapter.setOuter(mIsOuter);
                mPathList = MusicUtils.getPathList(MusicFolderActivity.this);
                String[] folderPathinDB = MusicUtils.getFolderPath(MusicFolderActivity.this);
                if(mFolderPathInDB != null && mFolderPathInDB.size() != 0) {
                    mFolderPathInDB.clear();
                }
                
                if (mPathList != null && folderPathinDB != null) {
                    
                    if (folderPathinDB.length > 0) {
                        for (int i = 0; i < folderPathinDB.length; i++) {
                            mFolderPathInDB.add(folderPathinDB[i]);
                        }
                    } else {
                        for (int i = 0; i < mPathList.size(); i++) {
                            mFolderPathInDB.add(mPathList.get(i));
                        }
                    }
                    
                    mAdapter = new MusicFolderAdapter(MusicFolderActivity.this, 
                            mPathList, mFolderPathInDB, mIsOuter);
                }
            }
            if(mAdapter != null) {
                setListAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }
            setupNoSongsView();
        }
    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        mAdapter = null;
        if(Artistactionbar != null) {
            Artistactionbar.destroyNowplaying();
        }
        if(mIsOuter == 1) {
            unregisterReceiver(mScanListener);
        }
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
        if(mIsOuter == 1) {
            mLinear = (LinearLayout)findViewById(R.id.linear_trackpage);
            mLinear.post(new Runnable() {
                
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    int id = R.drawable.playlist_default;
                    MusicUtils.setDefaultBackground(MusicFolderActivity.this, mLinear, id);
                }
            });
        }
    }

    public void setItemState(int position, boolean isSelect) {
        String path = mPathList.get(position);
        if (isSelect == true) {
            if (!mFolderPathInDB.contains(path)) {
                mFolderPathInDB.add(path);  
            }                         
        } else {
            mFolderPathInDB.remove(path);
        }
    }    
    
    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        int id = view.getId();
        
        switch (id) {
        case R.id.folder_done:
            
            if(mFolderPathInDB != null && mFolderPathInDB.size() == 0) {
                Toast.makeText(this, R.string.no_folder, 500).show();                
                return;
            }
            
            String[] path = new String[mFolderPathInDB.size()];
            for(int i = 0;i < path.length; i++) {
                path[i] = mFolderPathInDB.get(i);
            }
            MusicUtils.updateFolderPath(this, path);
            finish();
            
            Intent intent = new Intent();            
            intent.setClass(this, MusicMainEntryActivity.class);
            intent.putExtra("folder", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            break;
        default:
            break;
        }
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        if(mIsOuter == 0) {
            CheckBox itemCheckBox = (CheckBox)v.getTag();
            itemCheckBox.setChecked(!itemCheckBox.isChecked());
        } else {
            Intent intent = new Intent();
            intent.setClass(this, AlbumTrackBrowserActivity.class);
            intent.putExtra("folderPath", v.getTag().toString());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
    
    public void setSelectedFolder(String path) {
        mSelectedPath = path;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        // TODO Auto-generated method stub
        menu.add(0, PLAY_SELECTION, 0, R.string.play_selection);
        SubMenu sub = menu.addSubMenu(0, ADD_TO_PLAYLIST, 0, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(this, sub);
        
        AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfo;
        int position = mi.position;
        View selectView = mAdapter.getView(position, null, null);
        mSelectedPath = selectView.getTag().toString();        
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(mSelectedPath == null) {
            return true;
        }
        
        long[] selectList = MusicUtils.getSongListForFolder(this, mSelectedPath);
        switch (item.getItemId()) {
            case PLAY_SELECTION: {
                // play the selected album
                MusicUtils.playAll(this, selectList, 0);
                return true;
            }
            
            case PLAYLIST_SELECTED: {
                long playlist = item.getIntent().getLongExtra("playlist", 0);
                MusicUtils.addToPlaylist(this, selectList, playlist);
                return true;
            }
            
            case QUEUE: {
                MusicUtils.addToCurrentPlaylist(this, selectList);
                return true;
            }

            case NEW_PLAYLIST: {
                MusicUtils.addToNewPlaylist(this, selectList, -1);
                return true;
            }
        }
        return super.onContextItemSelected(item);        
    }
    
    public void setupNoSongsView() {
        View view = findViewById(ID_MUSIC_NO_SONGS_CONTENT);
        
        if (MusicUtils.mHasSongs == false) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            } else {
                view = getLayoutInflater().inflate(R.layout.music_no_songs, getListView(), false);    
                view.setId(ID_MUSIC_NO_SONGS_CONTENT);
//                view.setPadding(0, mNoSongsPaddingTop, 0, 0);
                addContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            }
            TextView txtView = (TextView)view.findViewById(R.id.text_no_songs);
            String status = Environment.getExternalStorageState();
            if (!(status.equals(Environment.MEDIA_MOUNTED))) {
                txtView.setText(R.string.nosd);
            } else {                
                txtView.setText(R.string.no_folders);
            }            
        } else {
            if (view != null)
                view.setVisibility(View.GONE);
        }
    }
}
