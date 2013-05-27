package com.lewa.player.ui.view;

import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.model.*;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Contacts.People;
import android.provider.MediaStore.Audio.Playlists;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class CurrentPlaylistView extends ViewGroup {

	public TouchInterceptor ti;
	private TrackListAdapter mAdapter;
	private Context mContext;
	private boolean mEditMode;
	private String mAlbumId;
	private String mArtistId;
	private String mPlaylist;
	private String mGenre;
	public Cursor mTrackCursor;
	private String mSortOrder;
	private String[] mCursorCols;
    private String[] mPlaylistMemberCols;
    private boolean mDeletedOneRow = false;
    private boolean mIsOrderChanged = false;
    
	public CurrentPlaylistView(Context context, String playlist) {
		super(context);
		// TODO Auto-generated constructor stub
		View currentplaylist = LayoutInflater.from(context).inflate(
				R.layout.currentlist_view, null);
		currentplaylist.setId(1);
		ti = (TouchInterceptor) currentplaylist.findViewById(R.id.curlistTouch);
		//ti.setBackgroundDrawable(null);
		//ti.setCacheColorHint(0xFF515151);
		ti.setCacheColorHint(0);

		this.addView(currentplaylist);
		ti.setOnItemClickListener(itemClickListen);
		ti.setFastScrollEnabled(true);
		mContext = context;
		mPlaylist = playlist;
		if(mPlaylist.equals("nowplaying")) {
			this.mEditMode = true;
		}
		mCursorCols = new String[] { MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ARTIST_ID,
				MediaStore.Audio.Media.DURATION };
		
        mPlaylistMemberCols = new String[] {
                MediaStore.Audio.Playlists.Members._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Playlists.Members.PLAY_ORDER,
                MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.Media.IS_MUSIC
        };
        
       	IntentFilter filterReceiver = new IntentFilter();
       	filterReceiver.addAction(MediaPlaybackService.META_CHANGED);
       	mContext.registerReceiver(mTrackListListener, filterReceiver);

	}
	
	public TrackListAdapter getAdapter() {
	    return mAdapter;
	}
	
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
	        /*if (mTrackCursor instanceof NowPlayingCursor) {
	            if (MusicUtils.sService != null) {
	                try {
	                    MusicUtils.sService.setQueuePosition(MusicUtils.sService.getQueue()[position]);
	                    return;
	                } catch (RemoteException ex) {
	                }
	            }
	        }*/
	        MusicUtils.playAll(mContext, mTrackCursor, position);
		}
		
	};
	
    private TouchInterceptor.DropListener mDropListener =
        new TouchInterceptor.DropListener() {
        public void drop(int from, int to) {
            NowPlayingCursor c = (NowPlayingCursor) mTrackCursor;
            c.moveItem(from, to);
            mAdapter.notifyDataSetChanged();
            ti.invalidateViews();
            mDeletedOneRow = true;
            mIsOrderChanged = true;
        }
    };
    
    private TouchInterceptor.RemoveListener mRemoveListener =
        new TouchInterceptor.RemoveListener() {
        public void remove(int which) {
            removePlaylistItem(which);
        }
    };
    
    public void removePlaylistItem(int which) {
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
        if (mTrackCursor instanceof NowPlayingCursor) {
            ((NowPlayingCursor)mTrackCursor).removeItem(which);
        } else {
            int colidx = mTrackCursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Playlists.Members._ID);
            mTrackCursor.moveToPosition(which);
            long id = mTrackCursor.getLong(colidx);
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",
                    Long.valueOf(mPlaylist));
            mContext.getContentResolver().delete(
                    ContentUris.withAppendedId(uri, id), null, null);
        }
        v.setVisibility(View.VISIBLE);
        ti.invalidateViews();
    }
	
    public void updateList() {
    	if(mAdapter != null) {
    		getTrackCursor(mAdapter.getQueryHandler(), null, true);
    	}else {
    		initAdatper();
    	}
    	ti.setSelection(getNowPlayingPos());
    }
    
	public void initAdatper() {
		if (mAdapter == null) {

	        //mAdapter = (TrackListAdapter) mContext.getLastNonConfigurationInstance();

//			mAdapter = new TrackListAdapter(
//					mContext, // need to use application context to
//								// avoid leaks
//					this,
//					R.layout.edit_track_list_item,
//					null, // cursor
//					new String[] {}, new int[] {},
//					"nowplaying".equals(mPlaylist),
//					mPlaylist != null
//							&& !(mPlaylist.equals("podcasts") || mPlaylist
//									.equals("recentlyadded")));
//			
			// setTitle(R.string.working_songs);
			
			getTrackCursor(mAdapter.getQueryHandler(), null, true);
			ti.setAdapter((ListAdapter)mAdapter);
			
		} else {
			mTrackCursor = mAdapter.getCursor();
			// If mTrackCursor is null, this can be because it doesn't have
			// a cursor yet (because the initial query that sets its cursor
			// is still in progress), or because the query failed.
			// In order to not flash the error dialog at the user for the
			// first case, simply retry the query when the cursor is null.
			// Worst case, we end up doing the same query twice.
			if (mTrackCursor != null) {
				// init(mTrackCursor, false);
			} else {
				// setTitle(R.string.working_songs);
				getTrackCursor(mAdapter.getQueryHandler(), null, true);
			}
		}
        if (mEditMode) {
            ((TouchInterceptor) ti).setDropListener(mDropListener);
            ((TouchInterceptor) ti).setRemoveListener(mRemoveListener);
            ti.setDivider(null);
            //ti.setSelector(R.drawable.playlist_tile_drag);
        }
		this.invalidate();        
        ti.setSelection(getNowPlayingPos());
	}

	public Cursor getTrackCursor(
			TrackListAdapter.TrackQueryHandler queryhandler, String filter,
			boolean async) {

		if (queryhandler == null) {
			throw new IllegalArgumentException();
		}

		Cursor ret = null;
		mSortOrder = MediaStore.Audio.Media.TITLE_KEY;
		StringBuilder where = new StringBuilder();
		where.append(MediaStore.Audio.Media.TITLE + " != ''");

		if (mGenre != null) {
			Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external",
					Integer.valueOf(mGenre));
			mSortOrder = MediaStore.Audio.Genres.Members.DEFAULT_SORT_ORDER;
			ret = queryhandler.doQuery(uri, mCursorCols, where.toString(),
					null, mSortOrder, async);
		} else if (mPlaylist != null) {
			if (mPlaylist.equals("nowplaying")) {
				if (MusicUtils.sService != null) {
					ret = new NowPlayingCursor(mContext,MusicUtils.sService, mCursorCols);
					if (ret.getCount() == 0) {
						// finish();
					}
				} else {
					// Nothing is playing.
				}
			} else if (mPlaylist.equals("podcasts")) {
				where.append(" AND " + MediaStore.Audio.Media.IS_PODCAST + "=1");
				Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				if (!TextUtils.isEmpty(filter)) {
					uri = uri.buildUpon()
							.appendQueryParameter("filter", Uri.encode(filter))
							.build();
				}
				ret = queryhandler.doQuery(uri, mCursorCols, where.toString(),
						null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER, async);
			} else if (mPlaylist.equals("recentlyadded")) {
				// do a query for all songs added in the last X weeks
				Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				if (!TextUtils.isEmpty(filter)) {
					uri = uri.buildUpon()
							.appendQueryParameter("filter", Uri.encode(filter))
							.build();
				}
				/*int X = MusicUtils.getIntPref(this, "numweeks", 2)
						* (3600 * 24 * 7);
				where.append(" AND " + MediaStore.MediaColumns.DATE_ADDED + ">");
				where.append(System.currentTimeMillis() / 1000 - X);*/
				ret = queryhandler.doQuery(uri, mCursorCols, where.toString(),
						null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER, async);
			} else {
				Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
						"external", Long.valueOf(mPlaylist));
				if (!TextUtils.isEmpty(filter)) {
					uri = uri.buildUpon()
							.appendQueryParameter("filter", Uri.encode(filter))
							.build();
				}
				mSortOrder = MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER;
				ret = queryhandler.doQuery(uri, mPlaylistMemberCols,
						where.toString(), null, mSortOrder, async);
			}
		} else {
			if (mAlbumId != null) {
				where.append(" AND " + MediaStore.Audio.Media.ALBUM_ID + "="
						+ mAlbumId);
				mSortOrder = MediaStore.Audio.Media.TRACK + ", " + mSortOrder;
			}
			if (mArtistId != null) {
				where.append(" AND " + MediaStore.Audio.Media.ARTIST_ID + "="
						+ mArtistId);
			}
			where.append(" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");
			Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			if (!TextUtils.isEmpty(filter)) {
				uri = uri.buildUpon()
						.appendQueryParameter("filter", Uri.encode(filter))
						.build();
			}
			ret = queryhandler.doQuery(uri, mCursorCols, where.toString(),
					null, mSortOrder, async);
		}

		// This special case is for the "nowplaying" cursor, which cannot be
		// handled
		// asynchronously using AsyncQueryHandler, so we do some extra
		// initialization here.
		if (ret != null && async) {
			init(ret, false);
			setTitle();
		}
		return ret;
	}


    private void setTitle() {

        CharSequence fancyName = null;
        if (mAlbumId != null) {
            int numresults = mTrackCursor != null ? mTrackCursor.getCount() : 0;
            if (numresults > 0) {
                mTrackCursor.moveToFirst();
                int idx = mTrackCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                fancyName = mTrackCursor.getString(idx);
                // For compilation albums show only the album title,
                // but for regular albums show "artist - album".
                // To determine whether something is a compilation
                // album, do a query for the artist + album of the
                // first item, and see if it returns the same number
                // of results as the album query.
                String where = MediaStore.Audio.Media.ALBUM_ID + "='" + mAlbumId +
                        "' AND " + MediaStore.Audio.Media.ARTIST_ID + "=" + 
                        mTrackCursor.getLong(mTrackCursor.getColumnIndexOrThrow(
                                MediaStore.Audio.Media.ARTIST_ID));
                Cursor cursor = MusicUtils.query(mContext, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Media.ALBUM}, where, null, null);
                if (cursor != null) {
                    if (cursor.getCount() != numresults) {
                        // compilation album
                        fancyName = mTrackCursor.getString(idx);
                    }    
                    cursor.deactivate();
                }
                if (fancyName == null || fancyName.equals(MediaStore.UNKNOWN_STRING)) {
                    fancyName = "unknow";
                }
            }
        } else if (mPlaylist != null) {
            if (mPlaylist.equals("nowplaying")) {
                if (MusicUtils.getCurrentShuffleMode() == MediaPlaybackService.SHUFFLE_AUTO) {
                   //fancyName = getText(R.string.partyshuffle_title);
                } else {
                   // fancyName = getText(R.string.nowplaying_title);
                }
            } else if (mPlaylist.equals("podcasts")){
               // fancyName = getText(R.string.podcasts_title);
            } else if (mPlaylist.equals("recentlyadded")){
              //  fancyName = getText(R.string.recentlyadded_title);
            } else {
                String [] cols = new String [] {
                MediaStore.Audio.Playlists.NAME
                };
                Cursor cursor = MusicUtils.query(mContext,
                        ContentUris.withAppendedId(Playlists.EXTERNAL_CONTENT_URI, Long.valueOf(mPlaylist)),
                        cols, null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() != 0) {
                        cursor.moveToFirst();
                        fancyName = cursor.getString(0);
                    }
                    cursor.deactivate();
                }
            }
        } else if (mGenre != null) {
            String [] cols = new String [] {
            MediaStore.Audio.Genres.NAME
            };
            Cursor cursor = MusicUtils.query(mContext,
                    ContentUris.withAppendedId(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, Long.valueOf(mGenre)),
                    cols, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    fancyName = cursor.getString(0);
                }
                cursor.deactivate();
            }
        }

        if (fancyName != null) {
            //setTitle(fancyName);
        } else {
           // setTitle(R.string.tracks_title);
        }
    }
    
	protected BroadcastReceiver mTrackListListener = new BroadcastReceiver() {
		
		@Override
		 public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(MediaPlaybackService.META_CHANGED)) {
				//mAdapter.bindView(null, context, mTrackCursor);
				mAdapter.notifyDataSetInvalidated();
				//ti.postInvalidate();
			}
		}

	};
	
	public void destroyView() {
		mContext.unregisterReceiver(mTrackListListener);
		this.mTrackCursor.close();
		this.mTrackCursor = null;
		mCursorCols = null;
		mPlaylistMemberCols = null;
		this.removeAllViews();
		
		
	}
	
	public void init(Cursor newCursor, boolean isLimited) {

        if (mAdapter == null) {
            return;
        }
        mAdapter.changeCursor(newCursor); // also sets mTrackCursor
        
        if (mTrackCursor == null) {
           // MusicUtils.displayDatabaseError(this);
            //closeContextMenu();
           // mReScanHandler.sendEmptyMessageDelayed(0, 1000);
            return;
        }

        //MusicUtils.hideDatabaseError(this);
        
        setTitle();

        // Restore previous position
        /*if (mLastListPosCourse >= 0 && mUseLastListPos) {
            ListView lv = getListView();
            // this hack is needed because otherwise the position doesn't change
            // for the 2nd (non-limited) cursor
            lv.setAdapter(lv.getAdapter());
            lv.setSelectionFromTop(mLastListPosCourse, mLastListPosFine);
            if (!isLimited) {
                mLastListPosCourse = -1;
            }
        }*/

        // When showing the queue, position the selection on the currently playing track
        // Otherwise, position the selection on the first matching artist, if any
        //IntentFilter f = new IntentFilter();
        //f.addAction(MediaPlaybackService.META_CHANGED);
        //f.addAction(MediaPlaybackService.QUEUE_CHANGED);
        if ("nowplaying".equals(mPlaylist)) {
            /*try {
                int cur = MusicUtils.sService.getQueuePosition();
                setSelection(cur);
                registerReceiver(mNowPlayingListener, new IntentFilter(f));
                mNowPlayingListener.onReceive(this, new Intent(MediaPlaybackService.META_CHANGED));
            } catch (RemoteException ex) {
            }*/
        } /*else {
            String key = getIntent().getStringExtra("artist");
            if (key != null) {
                int keyidx = mTrackCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID);
                mTrackCursor.moveToFirst();
                while (! mTrackCursor.isAfterLast()) {
                    String artist = mTrackCursor.getString(keyidx);
                    if (artist.equals(key)) {
                        setSelection(mTrackCursor.getPosition());
                        break;
                    }
                    mTrackCursor.moveToNext();
                }
            }
            registerReceiver(mTrackListListener, new IntentFilter(f));
            mTrackListListener.onReceive(this, new Intent(MediaPlaybackService.META_CHANGED));
        }*/
     //   ti.setSelection(getNowPlayingPos());
        
        
    }
	
	private int getNowPlayingPos() {	    
	    if(mPlaylist == null) {
	        return -1;
	    }
	    
	    long id = -1;
	    int pos = -1;
	    
	    boolean isNowPlaying = ("nowplaying".equals(mPlaylist));
        boolean disableNowPlayingIndicator = (mPlaylist != null);
        int audioIdIdx = 0;    
	    
        if (MusicUtils.sService != null) {          
            try {
                if (isNowPlaying) {
                    id = MusicUtils.sService.getQueuePosition();
                } else {
                    id = MusicUtils.sService.getAudioId();
                }
            } catch (RemoteException ex) {
            }
            Cursor cursor = mTrackCursor;
            cursor.moveToFirst();
            while (cursor != null && !cursor.isAfterLast()) {
                try {
                    audioIdIdx = cursor.getColumnIndexOrThrow(
                            MediaStore.Audio.Playlists.Members.AUDIO_ID);
                } catch (IllegalArgumentException ex) {
                    audioIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                }
                if ((isNowPlaying && cursor.getPosition() == id) ||
                        (!isNowPlaying && !disableNowPlayingIndicator && cursor.getLong(audioIdIdx) == id)) {
                    pos = cursor.getPosition();
                    break;
                }
                cursor.moveToNext();
            }
        }
        return pos;
    }
	
	@Override
	protected void onLayout(boolean arg0, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			switch (child.getId()) {
			case 1:
				child.setVisibility(View.VISIBLE);
				child.measure(r - l, b - t);
				child.layout(l, t, r, b);
				break;
			default:
				//
			}
		}
	}
	
	public boolean getIsOrderChanged() {
	    return mIsOrderChanged;
	}
	
	public void setIsOrderChanged(boolean isChanged) {
        mIsOrderChanged = isChanged;
    }

}
