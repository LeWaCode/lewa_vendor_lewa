package com.lewa.player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;

import com.lewa.player.online.DownLoadAllPicsAsync;
import com.lewa.player.provider.PlayerDBHelper;
import com.lewa.player.ui.*;
import com.lewa.player.IMediaPlaybackService;


public class MusicUtils {

    private static final String TAG = "MusicUtils";
    private static final String ACTION_ADD_PLAYLIST = "com.lewa.player.ui.ADD_PLAYLIST";
    private static final String MUSIC_PREFERENCES = "Music_setting";
    private static final String COVER_PATH = Environment.getExternalStorageDirectory() + "/LEWA/music/playlist/cover/";
    public static boolean mHasSongs = true; 
    public static final String ACTION_DELETEITEM = "DELETEITEM";

    public interface Defs {
        public final static int OPEN_URL = 0;
        public final static int ADD_TO_PLAYLIST = 1;
        public final static int USE_AS_RINGTONE = 2;
        public final static int PLAYLIST_SELECTED = 3;
        public final static int NEW_PLAYLIST = 4;
        public final static int PLAY_SELECTION = 5;
        public final static int GOTO_START = 6;
        public final static int GOTO_PLAYBACK = 7;
        public final static int PARTY_SHUFFLE = 8;
        public final static int SHUFFLE_ALL = 9;
        public final static int DELETE_ITEM = 10;
        public final static int SCAN_DONE = 11;
        public final static int QUEUE = 12;
        public final static int EFFECTS_PANEL = 13;
        public final static int SHARE_LIST = 14;
        public final static int SETTINGS = 15;
        public final static int SEARCH = 16;
        public final static int FOLDER = 17;
        public final static int EDIT_PLAYLIST = 18;
        public final static int SAVE_TO_PLAYLIST = 19;
        public final static int MODIFY_ID3 = 20;
        public final static int MORE_MENU = 21;
        public final static int SLEEP = 22;
        public final static int EQ_SETTING = 23;
        public final static int EXIT = 24;
        public final static int GET_PIC = 25;
        public final static int REFRESH = 26;
        public final static int CHILD_MENU_BASE = 27;  // this should be the last item
        
    }

    public static String makeAlbumsLabel(Context context, int numalbums, int numsongs, boolean isUnknown) {
        // There are two formats for the albums/songs information:
        // "N Song(s)"  - used for unknown artist/album
        // "N Album(s)" - used for known albums
        
        StringBuilder songs_albums = new StringBuilder();

        Resources r = context.getResources();
        if (isUnknown) {
            if (numsongs == 1) {
               // songs_albums.append(context.getString(R.string.onesong));
            } else {
                //String f = r.getQuantityText(R.plurals.Nsongs, numsongs).toString();
                //sFormatBuilder.setLength(0);
                //sFormatter.format(f, Integer.valueOf(numsongs));
                //songs_albums.append(sFormatBuilder);
            }
        } else {
            //String f = r.getQuantityText(R.plurals.Nalbums, numalbums).toString();
           // sFormatBuilder.setLength(0);
           // sFormatter.format(f, Integer.valueOf(numalbums));
           // songs_albums.append(sFormatBuilder);
           //.append(context.getString(R.string.albumsongseparator));
        }
        return songs_albums.toString();
    }

    /**
     * This is now only used for the query screen
     */
    public static String makeAlbumsSongsLabel(Context context, int numalbums, int numsongs, boolean isUnknown) {
        // There are several formats for the albums/songs information:
        // "1 Song"   - used if there is only 1 song
        // "N Songs" - used for the "unknown artist" item
        // "1 Album"/"N Songs" 
        // "N Album"/"M Songs"
        // Depending on locale, these may need to be further subdivided
        
        StringBuilder songs_albums = new StringBuilder();

        if (numsongs == 1) {
            //songs_albums.append(context.getString(R.string.onesong));
        } else {
            Resources r = context.getResources();
            if (! isUnknown) {
                //String f = r.getQuantityText(R.plurals.Nalbums, numalbums).toString();
                //sFormatBuilder.setLength(0);
                //sFormatter.format(f, Integer.valueOf(numalbums));
                //songs_albums.append(sFormatBuilder);
                //songs_albums.append(context.getString(R.string.albumsongseparator));
            }
            //String f = r.getQuantityText(R.plurals.Nsongs, numsongs).toString();
            //sFormatBuilder.setLength(0);
            //sFormatter.format(f, Integer.valueOf(numsongs));
            //songs_albums.append(sFormatBuilder);
        }
        return songs_albums.toString();
    }
    
    public static IMediaPlaybackService sService = null;
    private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();

    public static class ServiceToken {
        ContextWrapper mWrappedContext;
        ServiceToken(ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    public static ServiceToken bindToService(Activity context) {
        return bindToService(context, null);
    }

    public static ServiceToken bindToService(Activity context, ServiceConnection callback) {
        Activity realActivity = context.getParent();
        if (realActivity == null) {
            realActivity = context;
        }
        ContextWrapper cw = new ContextWrapper(realActivity);
        cw.startService(new Intent(cw, MediaPlaybackService.class));
        ServiceBinder sb = new ServiceBinder(callback);
        if (cw.bindService((new Intent()).setClass(cw, MediaPlaybackService.class), sb, 0)) {
            sConnectionMap.put(cw, sb);
            return new ServiceToken(cw);
        }
        Log.e("Music", "Failed to bind to service");
        return null;
    }

    public static void unbindFromService(ServiceToken token) {
        if (token == null) {
            Log.e("MusicUtils", "Trying to unbind with null token");
            return;
        }
        ContextWrapper cw = token.mWrappedContext;
        ServiceBinder sb = sConnectionMap.remove(cw);
        if (sb == null) {
            Log.e("MusicUtils", "Trying to unbind for unknown Context");
            return;
        }
        cw.unbindService(sb);
        if (sConnectionMap.isEmpty()) {
            // presumably there is nobody interested in the service at this point,
            // so don't hang on to the ServiceConnection
            sService = null;
        }
    }

    private static class ServiceBinder implements ServiceConnection {
        ServiceConnection mCallback;
        ServiceBinder(ServiceConnection callback) {
            mCallback = callback;
        }
        
        public void onServiceConnected(ComponentName className, android.os.IBinder service) {
            sService = IMediaPlaybackService.Stub.asInterface(service);
            initAlbumArtCache();
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }
        
        public void onServiceDisconnected(ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            sService = null;
        }
    }
    
    private static PlayerDBHelper lewaDBhelp;
    
    public static void songPlayTimesPlus(Context mContext) {
    	lewaDBhelp = new PlayerDBHelper(mContext,"com_lewa_player.db", true);
    	try {
    		if(sService != null) {
    			long[] playlist = sService.getQueue();
    			int position = sService.getQueuePosition();
    			if(playlist == null)return;
    			if(position >= playlist.length)return;
    			if(sService.getRepeatMode() == MediaPlaybackService.REPEAT_CURRENT) {
        			position = position == 0 ? playlist.length-1 : position;
    			}else {
        			position = position == 0 ? playlist.length-1 : position-1;
    			}
    			int song_id = (int) playlist[position];
    			lewaDBhelp.times_plus(song_id);
    			lewaDBhelp.close();
    			//will change uri for favorite list
    			mContext.getContentResolver().notifyChange(Uri.parse("content://media"), null);
    		}
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static long[] getFavouriteTracks(Context mContext) {
    	lewaDBhelp = new PlayerDBHelper(mContext,"com_lewa_player.db", true);
    	if(lewaDBhelp.getDBFavouriteList() == null) {
    		return null;
    	}
    	long [] trackList = new long[lewaDBhelp.getDBFavouriteList().length];
    	for(int i=0; i< trackList.length; i++) {
    		trackList[i] = getTrackIdFromTrack(mContext, lewaDBhelp.getDBFavouriteList()[i]);
    	}
    	return trackList;
    }
    
    public static long getTrackIdFromTrack(Context context, String name) {
    	long ret = 0;
    	String[] mCursorCols = new String[] {
	            MediaStore.Audio.Media._ID};
		Cursor mCursor = context.getContentResolver().query(
               MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
               mCursorCols, "TITLE LIKE " + "'" + name + "'" , null, null);

	   if (mCursor != null) {
           mCursor.moveToFirst();
           if(mCursor.getCount() > 0) {
               ret = mCursor.getLong(0);
               
           }

    	   mCursor.close();
    	   mCursor = null;
	   }
	   return ret;
    	
    }
    
    public static void deleteFavoriteTracks(Context context, String name) {
        lewaDBhelp = new PlayerDBHelper(context,"com_lewa_player.db", true);
        lewaDBhelp.deleteDBFavoriteList(name);
    }
    
    public static long getCurrentAlbumId() {
        if (sService != null) {
            try {
                return sService.getAlbumId();
            } catch (RemoteException ex) {
            }
        }
        return -1;
    }

    public static long getCurrentArtistId() {
        if (MusicUtils.sService != null) {
            try {
                return sService.getArtistId();
            } catch (RemoteException ex) {
            }
        }
        return -1;
    }
    
    public static String getCurrentTrackName() {
        if (MusicUtils.sService != null) {
            try {
                return sService.getTrackName();
            } catch (RemoteException ex) {
            }
        }
        return "";
    }

    public static long getCurrentAudioId() {
        if (MusicUtils.sService != null) {
            try {
                return sService.getAudioId();
            } catch (RemoteException ex) {
            }
        }
        return -1;
    }
    
    public static int getCurrentShuffleMode() {
        int mode = MediaPlaybackService.SHUFFLE_NONE;
        if (sService != null) {
            try {
                mode = sService.getShuffleMode();
            } catch (RemoteException ex) {
            }
        }
        return mode;
    }
    
    public static void togglePartyShuffle() {
        if (sService != null) {
            int shuffle = getCurrentShuffleMode();
            try {
                if (shuffle == MediaPlaybackService.SHUFFLE_AUTO) {
                    sService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
                } else {
                    sService.setShuffleMode(MediaPlaybackService.SHUFFLE_AUTO);
                }
            } catch (RemoteException ex) {
            }
        }
    }
    
	public static Bitmap getDefaultArtImg(Context mContext) {
		BitmapFactory.Options options = new BitmapFactory.Options();
        // options.inSampleSize = 2;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inDither = false;
        // options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap ret;
        ret = BitmapFactory.decodeStream(mContext.getApplicationContext()
                .getResources().openRawResource(R.drawable.playlist_default_0),
                null, options);
		return ret;
		
	}
	
	public static Bitmap getDefaultBg(Context mContext, int resId) {
	    int bgid = getIntPref(mContext, "playerbg", 0); 
        switch(bgid){
            case 1:
                resId = R.drawable.default_bg_1;
            break;
            case 2:
                resId = R.drawable.default_bg_2;
            break;
            case 3:
                resId = R.drawable.default_bg_3;
            break; 
            default:
                resId = R.drawable.playlist_default;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
           //options.inSampleSize = 2;  
           options.inPurgeable = true;
           options.inInputShareable = true;
           options.inDither = false;
           //options.inPreferredConfig = Bitmap.Config.RGB_565;
           Bitmap ret = BitmapFactory.decodeStream(mContext.
                   getApplicationContext().getResources().openRawResource(resId),
                   null, options);

        return ret;
        
    }
	
	public static void setDefaultBackground(Context context, View view, int bgId) {
	    Bitmap bm = MusicUtils.getDefaultBg(context, bgId);
        if (bm != null) {
            MusicUtils.setBackground(view, bm);
            return;
        }
        view.setBackgroundColor(0xff000000);
	}
    
    public static void setPartyShuffleMenuIcon(Menu menu) {
        MenuItem item = menu.findItem(Defs.PARTY_SHUFFLE);
        if (item != null) {
            int shuffle = MusicUtils.getCurrentShuffleMode();
            if (shuffle == MediaPlaybackService.SHUFFLE_AUTO) {
                //item.setIcon(R.drawable.ic_menu_party_shuffle);
                //item.setTitle(R.string.party_shuffle_off);
            } else {
                //item.setIcon(R.drawable.ic_menu_party_shuffle);
                //item.setTitle(R.string.party_shuffle);
            }
        }
    }
    
    /*
     * Returns true if a file is currently opened for playback (regardless
     * of whether it's playing or paused).
     */
    public static boolean isMusicLoaded() {
        if (MusicUtils.sService != null) {
            try {
                return sService.getPath() != null;
            } catch (RemoteException ex) {
            }
        }
        return false;
    }

    private final static long [] sEmptyList = new long[0];

    public static long [] getSongListForCursor(Cursor cursor) {
        if (cursor == null) {
            return sEmptyList;
        }
        int len = cursor.getCount();
        long [] list = new long[len];
        cursor.moveToFirst();
        int colidx = -1;
        try {
            colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        } catch (IllegalArgumentException ex) {
            colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        }
        for (int i = 0; i < len; i++) {
            list[i] = cursor.getLong(colidx);
            cursor.moveToNext();
        }
        return list;
    }

    public static long [] getSongListForArtist(Context context, long id) {
        final String[] ccols = new String[] { MediaStore.Audio.Media._ID };
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.ARTIST_ID + "=" + id + " AND " + 
        MediaStore.Audio.Media.IS_MUSIC + "=1");
        where.append(getWhereBuilder(context, "_id", 0));
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                ccols, where.toString(), null, "sort_key");
        
        if (cursor != null) {
            long [] list = getSongListForCursor(cursor);
            cursor.close();
            return list;
        }
        return sEmptyList;
    }
    
    public static int getAlbumCountForArtist(Context context, long id) {
        final String[] ccols = new String[] { 
                MediaStore.Audio.Media._ID};
        StringBuilder where = new StringBuilder();
        where.append("_id IN (select album_id from audio where artist_id=" + id + ")");
        where.append(getWhereBuilder(context, "album_id", 0));
        Cursor cursor = query(context, MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                ccols, where.toString(), null, null);
        
        if (cursor != null) {
            int count = cursor.getCount();
            cursor.close();
            return count;
        }
        return 0;
    }

    public static long [] getSongListForAlbum(Context context, long id, long aid) {
        final String[] ccols = new String[] { MediaStore.Audio.Media._ID };
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.ALBUM_ID + " = " + id + " AND " + 
                MediaStore.Audio.Media.IS_MUSIC + "=1");
        
        //modify by zhaolei,120323,for album count
        if(aid > 0) {
            where.append(" AND " + MediaStore.Audio.Media.ARTIST_ID + " = " + aid);
        }
        //end
        
        where.append(getWhereBuilder(context, "_id", 0));
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                ccols, where.toString(), null, "sort_key");

        if (cursor != null) {
            long [] list = getSongListForCursor(cursor);
            cursor.close();
            return list;
        }
        return sEmptyList;
    }
    
    public static long [] getSongListForGenre(Context context, long id) {
        final String[] ccols = new String[]{};        
        StringBuilder where = new StringBuilder();
        where.append("_id in (SELECT audio_id FROM audio_genres_map WHERE genre_id = " + id);
        where.append(" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1 AND 1=1");
        where.append(MusicUtils.getWhereBuilder(context, "_id", 1));
        where.append(")");
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where.toString(), null, "Upper(sort_key)");

        if (cursor != null) {
            long [] list = getSongListForCursor(cursor);
            cursor.close();
            return list;
        }
        return sEmptyList;
    }
    
    public static long [] getSongListForOtherGenre(Context context){
        final String[] ccols = new String[] { MediaStore.Audio.Media._ID };
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media._ID + " not in (select audio_id from audio_genres_map ))");
        where.append(" AND (" + MediaStore.Audio.Media.IS_MUSIC + "=1 AND 1=1");
        where.append(MusicUtils.getWhereBuilder(context, "_id", 0));
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                ccols, where.toString(), null, "Upper(sort_key)");
        if (cursor != null) {
            long [] list = getSongListForCursor(cursor);
            cursor.close();
            return list;
        }        
        return sEmptyList;
    }

    public static long [] getSongListForPlaylist(Context context, long plid) {
        final String[] ccols = new String[] { MediaStore.Audio.Playlists.Members.AUDIO_ID };
        Cursor cursor = query(context, MediaStore.Audio.Playlists.Members.getContentUri("external", plid),
                ccols, null, null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
        
        if (cursor != null) {
            long [] list = getSongListForCursor(cursor);
            cursor.close();
            return list;
        }
        return sEmptyList;
    }
    
    public static void playPlaylist(Context context, long plid) {
        long [] list = getSongListForPlaylist(context, plid);
        if (list != null) {
            playAll(context, list, -1, false);
        }
    }

    public static long [] getAllSongs(Context context) {
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.IS_MUSIC + "=1");
        where.append(getWhereBuilder(context, "_id", 0));
        Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,  
                new String[] {MediaStore.Audio.Media._ID}, where.toString(),
                null, "Upper(sort_key)");

        try {
            if (c == null || c.getCount() == 0) {
                return null;
            }
            int len = c.getCount();
            long [] list = new long[len];
            for (int i = 0; i < len; i++) {
                c.moveToNext();
                list[i] = c.getLong(0);
            }

            return list;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
    
    public static long [] getAllSongsInDB(Context context) {
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.IS_MUSIC + "=1");
        Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Media._ID}, where.toString(),
                null, null);
        try {
            if (c == null || c.getCount() == 0) {
                return null;
            }
            int len = c.getCount();
            long [] list = new long[len];
            for (int i = 0; i < len; i++) {
                c.moveToNext();
                list[i] = c.getLong(0);
            }

            return list;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
    
    public static boolean ifHasSongs(Context context) {
        Cursor c = null;
        try {
            c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Media._ID}, null,
                    null, null);
            if(c != null && c.getCount() > 0) {
                return true;
            }else {
                return false;
            }
        }catch (Exception ex) {
            return false;
            
        }finally {
            if(c != null) {
                c.close();
                c = null;
            }
        }
    }

    /**
     * Fills out the given submenu with items for "new playlist" and
     * any existing playlists. When the user selects an item, the
     * application will receive PLAYLIST_SELECTED with the Uri of
     * the selected playlist, NEW_PLAYLIST if a new playlist
     * should be created, and QUEUE if the "current playlist" was
     * selected.
     * @param context The context to use for creating the menu items
     * @param sub The submenu to add the items to.
     */
    public static void makePlaylistMenu(Context context, SubMenu sub) {
        makePlaylistMenu(context, sub, -1);
    }
    
    public static void makePlaylistMenu(Context context, SubMenu sub, int playlistId) {
        String[] cols = new String[] {
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME
        };
        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            System.out.println("resolver = null");
        } else {
            String whereclause = MediaStore.Audio.Playlists.NAME + " != ''" + 
                " AND  name != '" + context.getString(R.string.record) + "'";
            Cursor cur = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                cols, whereclause, null,
                MediaStore.Audio.Playlists.NAME);
            sub.clear();
            sub.add(1, Defs.NEW_PLAYLIST, 0, R.string.new_playlist);
            if(!(context instanceof CurrentPlaylistActivity) && !(context instanceof MediaPlaybackActivity)) {
                sub.add(1, Defs.QUEUE, 0, R.string.queue);
            }
            //sub.add(1, Defs.NEW_PLAYLIST, 0, R.string.new_playlist);
            if (cur != null && cur.getCount() > 0) {
                //sub.addSeparator(1, 0);
                cur.moveToFirst();
                while (! cur.isAfterLast()) {
                    if (playlistId == -1 || playlistId != cur.getLong(0)) {
                        Intent intent = new Intent();
                        intent.putExtra("playlist", cur.getLong(0));
    //                    if (cur.getInt(0) == mLastPlaylistSelected) {
    //                        sub.add(0, MusicBaseActivity.PLAYLIST_SELECTED, cur.getString(1)).setIntent(intent);
    //                    } else {
                            sub.add(1, Defs.PLAYLIST_SELECTED, 0, cur.getString(1)).setIntent(intent);
    //                    }
                    }
                    cur.moveToNext();
                }
            }
            if (cur != null) {
                cur.close();
            }
        }
    }

    public static void clearPlaylist(Context context, int plid) {
        
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", plid);
        context.getContentResolver().delete(uri, null, null);
        return;
    }
    
    public static void deleteTracks(Context context, long [] list) {
        
        String [] cols = new String [] { MediaStore.Audio.Media._ID, 
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID };
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media._ID + " IN (");
        for (int i = 0; i < list.length; i++) {
            where.append(list[i]);
            if (i < list.length - 1) {
                where.append(",");
            }
        }
        where.append(")");
        Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols,
                where.toString(), null, null);

        if (c != null) {

            // step 1: remove selected tracks from the current playlist, as well
            // as from the album art cache
            try {
                c.moveToFirst();
                while (! c.isAfterLast()) {
                    // remove from current playlist
                    long id = c.getLong(0);
                    sService.removeTrack(id);
                    // remove from album art cache
                    long artIndex = c.getLong(2);
                    synchronized(sArtCache) {
                        sArtCache.remove(artIndex);
                    }
                    c.moveToNext();
                }
            } catch (RemoteException ex) {
            }

            // step 2: remove selected tracks from the database
            String songNameList = "'" + MusicUtils.getSongName(context, list[0])[0].toString() + "'";
            int length = list.length;

            for (int i = 0; i < length - 1; i++) {
                songNameList += ", ";
                songNameList += "'" + MusicUtils.getSongName(context, list[i])[0].toString() + "'";
            }
            deleteFavoriteTracks(context, songNameList);
            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where.toString(), null);
            
            // step 3: remove files from card
            c.moveToFirst();
            while (! c.isAfterLast()) {
                String name = c.getString(1);
                File f = new File(name);
                try {  // File.delete can throw a security exception
                    if (!f.delete()) {
                        // I'm not sure if we'd ever get here (deletion would
                        // have to fail, but no exception thrown)
                        Log.e("MusicUtils", "Failed to delete file " + name);
                    }
                    c.moveToNext();
                } catch (SecurityException ex) {
                    c.moveToNext();
                }
            }
            c.close();
        }

        String message = context.getResources().getQuantityString(
                R.plurals.NNNtracksdeleted, list.length, Integer.valueOf(list.length));
        
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        // We deleted a number of tracks, which could affect any number of things
        // in the media content domain, so update everything.
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        Intent intent = new Intent();
        intent.putExtra("deleteItemId", list);
        intent.setAction(ACTION_DELETEITEM);
        context.sendBroadcast(intent);
    }
    
    public static void addToCurrentPlaylist(Context context, long [] list) {
        if (sService == null || list == null) {
            return;
        }
        try {
            if(list.length == 0){
                return;
            }
            sService.enqueue(list, MediaPlaybackService.LAST);
            String message = context.getResources().getQuantityString(
                    R.plurals.NNNtrackstoplaylist, list.length, Integer.valueOf(list.length));
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } catch (RemoteException ex) {
        }
    }

    private static ContentValues[] sContentValuesCache = null;

    /**
     * @param ids The source array containing all the ids to be added to the playlist
     * @param offset Where in the 'ids' array we start reading
     * @param len How many items to copy during this pass
     * @param base The play order offset to use for this pass
     */
    private static void makeInsertItems(long[] ids, int offset, int len, int base) {
        // adjust 'len' if would extend beyond the end of the source array
        if (offset + len > ids.length) {
            len = ids.length - offset;
        }
        // allocate the ContentValues array, or reallocate if it is the wrong size
        if (sContentValuesCache == null || sContentValuesCache.length != len) {
            sContentValuesCache = new ContentValues[len];
        }
        // fill in the ContentValues array with the right values for this pass
        for (int i = 0; i < len; i++) {
            if (sContentValuesCache[i] == null) {
                sContentValuesCache[i] = new ContentValues();
            }

            sContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            sContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i]);
        }
    }
    
    public static void addToPlaylist(Context context, long [] ids, long playlistid) {
        if (ids == null) {
            // this shouldn't happen (the menuitems shouldn't be visible
            // unless the selected item represents something playable
            Log.e("MusicBase", "ListSelection null");
        } else {
            int size = ids.length;
            ContentResolver resolver = context.getContentResolver();
            // need to determine the number of items currently in the playlist,
            // so the play_order field can be maintained.
            String[] cols = new String[] {
                    "count(*)"
            };
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
            Cursor cur = resolver.query(uri, cols, null, null, null);
            cur.moveToFirst();
            int base = cur.getInt(0);
            cur.close();
            int numinserted = 0;
            for (int i = 0; i < size; i += 1000) {
                makeInsertItems(ids, i, 1000, base);
                numinserted += resolver.bulkInsert(uri, sContentValuesCache);
            }
            String message = context.getResources().getQuantityString(
                    R.plurals.NNNtrackstoplaylist, numinserted, numinserted);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            //mLastPlaylistSelected = playlistid;
        }
    }

    public static Cursor query(Context context, Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder, int limit) {
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                return null;
            }
            if (limit > 0) {
                uri = uri.buildUpon().appendQueryParameter("limit", "" + limit).build();
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
         } catch (UnsupportedOperationException ex) {
            return null;
        }
        
    }
    public static Cursor query(Context context, Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        return query(context, uri, projection, selection, selectionArgs, sortOrder, 0);
    }
    
    public static boolean isMediaScannerScanning(Context context) {
        boolean result = false;
        Cursor cursor = query(context, MediaStore.getMediaScannerUri(), 
                new String [] { MediaStore.MEDIA_SCANNER_VOLUME }, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                result = "external".equals(cursor.getString(0));
            }
            cursor.close(); 
        } 

        return result;
    }
    
    public static long getArtistId(Context mContext, long song_id) {
    	if(song_id < 0)return -1;
    	long artistid = -1;
    	String[] projection = new String[] {MediaStore.Audio.Media.ARTIST_ID};
    	Cursor mCursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, "_id=" + song_id , null, null);
    	if(mCursor != null && mCursor.getCount() > 0) {
    		mCursor.moveToFirst();
    		artistid = mCursor.getLong(0);
    	}
    	if(mCursor != null) {
    	   mCursor.close();
    	   mCursor = null;
    	}

    	return artistid;
    	
    }
    
    public static String getArtistIdFromAlbum(Context mContext, long albumid) {
    	if(albumid < 0)return null;
    	String artistid = "";
    	String[] projection = new String[] {"artist_id"};
    	Cursor mCursor = mContext.getContentResolver().query(
    			Uri.parse("content://media/external/audio/albums/" + String.valueOf(albumid)),
                projection, null, null, null);
    	if(mCursor != null && mCursor.getCount() > 0) {
    		mCursor.moveToFirst();
    		artistid = mCursor.getString(0);
    	}
		mCursor.close();
		mCursor = null;
    	return artistid;
    	
    }
    
	public static String getArtistName(Context mContext, long artist_id) {
		// TODO Auto-generated method stub
    	if(artist_id < 0) return null;
    	String artist = null;
        String[] projection = new String[] {"artist"};
    	Cursor mCursor = mContext.getContentResolver().query(
    			Uri.parse("content://media/external/audio/artists/" + String.valueOf(artist_id)),
                projection, null , null, null); 
        if (mCursor.getCount() > 0 && mCursor.getColumnCount() > 0) {
        	mCursor.moveToNext();
        	artist = mCursor.getString(0);
        }
		mCursor.close();
		mCursor = null;
    	
		return artist;
	}
	
	public static String getAlbumName(Context mContext, long album_id) {
		// TODO Auto-generated method stub
    	if(album_id < 0) return null;
    	String album = null;
        String[] projection = new String[] {"album"};
    	Cursor mCursor = mContext.getContentResolver().query(
    			Uri.parse("content://media/external/audio/albums/" + String.valueOf(album_id)),
                projection, null , null, null); 
        if (mCursor.getCount() > 0 && mCursor.getColumnCount() > 0) {
        	mCursor.moveToNext();
        	album = mCursor.getString(0);
        }
		mCursor.close();
		mCursor = null;
    	
		return album;
	}
	
    
	public static String[] getSongName(Context mContext, long trackId) {
		
		String[] mCursorCols = new String[] {
	            MediaStore.Audio.Media.ARTIST_ID,
	            MediaStore.Audio.Media.TITLE,
	            MediaStore.Audio.Media.DURATION};
		Cursor mCursor = mContext.getContentResolver().query(
               MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
               mCursorCols, "_id=" + trackId , null, null);
	   long artist_id = 0;
	   String songname = "";
	   String artistname = "";
	   String duration = "";
	   if (mCursor != null) {
           mCursor.moveToFirst();
           if(mCursor.getCount() > 0) {
               artist_id = mCursor.getInt(0);
               artistname = getArtistName(mContext,artist_id);
        	   songname = mCursor.getString(1);
               int secs = mCursor.getInt(2) / 1000;
               if (secs != 0) {
                   duration = MusicUtils.makeTimeString(mContext, secs);
               }
           }

    	   mCursor.close();
    	   mCursor = null;
	   }
	   String[] ret = {songname, artistname, duration};
	   return ret;
	}
	
	public static String getSongPath(Context mContext, long trackId) {
		
		String songPath = null;
		Cursor mCursor = null;
		if(trackId < 0) {
			return null;
		}
		String[] mCursorCols = new String[] {
	            MediaStore.Audio.Media.DATA};
		try {
		mCursor = mContext.getContentResolver().query(
               MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
               mCursorCols, "_id=" + trackId , null, null);

			if(mCursor != null) {
				mCursor.moveToFirst();
				if(mCursor.getCount() > 0) {
					songPath = mCursor.getString(0);
				}
			}
		}catch (Exception ex){
			Log.e(TAG, "exception !! " + ex);
		}finally {
			if(mCursor != null) {
				mCursor.close();
				mCursor = null;
			}
		}

		return songPath;
	}
	
	
    public static void setSpinnerState(Activity a) {
        if (isMediaScannerScanning(a)) {
            // start the progress spinner
            a.getWindow().setFeatureInt(
                    Window.FEATURE_INDETERMINATE_PROGRESS,
                    Window.PROGRESS_INDETERMINATE_ON);

            a.getWindow().setFeatureInt(
                    Window.FEATURE_INDETERMINATE_PROGRESS,
                    Window.PROGRESS_VISIBILITY_ON);
        } else {
            // stop the progress spinner
            a.getWindow().setFeatureInt(
                    Window.FEATURE_INDETERMINATE_PROGRESS,
                    Window.PROGRESS_VISIBILITY_OFF);
        }
    }
    
    private static String mLastSdStatus;

    public static void displayDatabaseError(Activity a) {
        if (a.isFinishing()) {
            // When switching tabs really fast, we can end up with a null
            // cursor (not sure why), which will bring us here.
            // Don't bother showing an error message in that case.
            return;
        }

        String status = Environment.getExternalStorageState();
        int title, message;

        if (android.os.Environment.isExternalStorageRemovable()) {
            //title = R.string.sdcard_error_title;
           // message = R.string.sdcard_error_message;
        } else {
           // title = R.string.sdcard_error_title_nosdcard;
           // message = R.string.sdcard_error_message_nosdcard;
        }
        
        if (status.equals(Environment.MEDIA_SHARED) ||
                status.equals(Environment.MEDIA_UNMOUNTED)) {
            if (android.os.Environment.isExternalStorageRemovable()) {
               // title = R.string.sdcard_busy_title;
               // message = R.string.sdcard_busy_message;
            } else {
               // title = R.string.sdcard_busy_title_nosdcard;
               // message = R.string.sdcard_busy_message_nosdcard;
            }
        } else if (status.equals(Environment.MEDIA_REMOVED)) {
            if (android.os.Environment.isExternalStorageRemovable()) {
              //  title = R.string.sdcard_missing_title;
              //  message = R.string.sdcard_missing_message;
            } else {
              //  title = R.string.sdcard_missing_title_nosdcard;
              //  message = R.string.sdcard_missing_message_nosdcard;
            }
        } else if (status.equals(Environment.MEDIA_MOUNTED)){
            // The card is mounted, but we didn't get a valid cursor.
            // This probably means the mediascanner hasn't started scanning the
            // card yet (there is a small window of time during boot where this
            // will happen).
            a.setTitle("");
            Intent intent = new Intent();
            intent.setClass(a, ScanningProgress.class);
            a.startActivityForResult(intent, Defs.SCAN_DONE);
        } else if (!TextUtils.equals(mLastSdStatus, status)) {
            mLastSdStatus = status;
            Log.d(TAG, "sd card: " + status);
        }

        /*a.setTitle(title);
        View v = a.findViewById(R.id.sd_message);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }
        v = a.findViewById(R.id.sd_icon);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }
        v = a.findViewById(android.R.id.list);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
        v = a.findViewById(R.id.buttonbar);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
        TextView tv = (TextView) a.findViewById(R.id.sd_message);
        tv.setText(message);*/
    }
    
    public static void hideDatabaseError(Activity a) {
/*        View v = a.findViewById(R.id.sd_message);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
        v = a.findViewById(R.id.sd_icon);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
        v = a.findViewById(android.R.id.list);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }*/
    }

    static protected Uri getContentURIForPath(String path) {
        return Uri.fromFile(new File(path));
    }

    
    /*  Try to use String.format() as little as possible, because it creates a
     *  new Formatter every time you call it, which is very inefficient.
     *  Reusing an existing Formatter more than tripled the speed of
     *  makeTimeString().
     *  This Formatter/StringBuilder are also used by makeAlbumSongsLabel()
     */
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    private static final Object[] sTimeArgs = new Object[5];

    public static String makeTimeString(Context context, long secs) {
        String durationformat = context.getString(
                secs < 3600 ? R.string.durationformatshort : R.string.durationformatlong);
        
        /* Provide multiple arguments so the format can be changed easily
         * by modifying the xml.
         */
        sFormatBuilder.setLength(0);

        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = (secs / 60) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;

        return sFormatter.format(durationformat, timeArgs).toString();
    }
    
    public static void shuffleAll(Context context, Cursor cursor) {
        playAll(context, cursor, 0, true);
    }

    public static void playAll(Context context, Cursor cursor) {
        playAll(context, cursor, 0, false);
    }
    
    public static void playAll(Context context, Cursor cursor, int position) {
        playAll(context, cursor, position, false);
    }
    
    public static void playAll(Context context, long [] list, int position) {
        playAll(context, list, position, false);
    }
    
    private static void playAll(Context context, Cursor cursor, int position, boolean force_shuffle) {
    
        long [] list = getSongListForCursor(cursor);
        playAll(context, list, position, force_shuffle);
    }
    
    private static void playAll(Context context, long [] list, int position, boolean force_shuffle) {
        if (list == null || list.length == 0 || sService == null) {
            Log.d("MusicUtils", "attempt to play empty song list");
            // Don't try to play empty playlists. Nothing good will come of it.
           // String message = context.getString(R.string.emptyplaylist, list.length);
           // Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (force_shuffle) {
                sService.setShuffleMode(MediaPlaybackService.SHUFFLE_NORMAL);
            }
            long curid = sService.getAudioId();
            int curpos = sService.getQueuePosition();
            if (position != -1 && curpos == position && curid == list[position]) {
                // The selected file is the file that's currently playing;
                // figure out if we need to restart with a new playlist,
                // or just launch the playback activity.
                long [] playlist = sService.getQueue();
                if (Arrays.equals(list, playlist)) {
                    // we don't need to set a new list, but we should resume playback if needed
                    sService.play();
                    return; // the 'finally' block will still run
                }
            }
            if (position < 0) {
                position = 0;
            }
            sService.open(list, force_shuffle ? -1 : position);
            //sService.play();
        } catch (RemoteException ex) {
        } finally {
            Intent intent = new Intent("com.lewa.player.PLAYBACK_VIEWER")
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
    }
    
    public static String[] getTrackNameNext(Context mContext) {
    	String nextTrackName[] = new String[4];
    	 try {
			long [] playlist = sService.getQueue();
			int len = playlist.length;
			 int curpos = sService.getQueuePosition();
			 int repeatMode = sService.getRepeatMode();
			 int nexNum = len - curpos -1;
			 int p;
			 if(repeatMode != MediaPlaybackService.REPEAT_ALL) {
			     p = nexNum>4?4:nexNum;
			 } else {
			     p = 4;
			 }
			 if(nexNum >= 0) {
				 for(int i= (curpos + 1) % len; i < (curpos + 1 ) % len + p; i++) {
			        String[] projection = new String[] {MediaStore.Audio.Media.TITLE};
			    	Cursor mCursor = query(mContext,
			    			Uri.parse("content://media/external/audio/media/" + String.valueOf(playlist[i % len])),
			                projection, null , null, null); 
			    	if(mCursor != null) {
			    		mCursor.moveToFirst();
			    	}
			    	//String next = mCursor.getString(0);
			    	nextTrackName[i - (curpos + 1) % len] = mCursor.getString(0);
			    	if(mCursor != null) {
			    		mCursor.close();
			    		mCursor = null;
			    	}
				 }
			 }
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		
		}
    	 
		return nextTrackName;
    	
    }
    
    public static void clearQueue() {
        try {
            sService.removeTracks(0, Integer.MAX_VALUE);
        } catch (RemoteException ex) {
        }
    }
    
    // A really simple BitmapDrawable-like class, that doesn't do
    // scaling, dithering or filtering.
    private static class FastBitmapDrawable extends Drawable {
        private Bitmap mBitmap;
        public FastBitmapDrawable(Bitmap b) {
            mBitmap = b;
        }
        @Override
        public void draw(Canvas canvas) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
        @Override
        public void setAlpha(int alpha) {
        }
        @Override
        public void setColorFilter(ColorFilter cf) {
        }
    }
    
    private static int sArtId = -2;
    private static Bitmap mCachedBit = null;
    private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final HashMap<Long, Drawable> sArtCache = new HashMap<Long, Drawable>();
    private static int sArtCacheId = -1;
    
    static {
        // for the cache, 
        // 565 is faster to decode and display
        // and we don't want to dither here because the image will be scaled down later
        sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.RGB_565;
        sBitmapOptionsCache.inDither = false;

        sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        sBitmapOptions.inDither = false;
    }

    public static void initAlbumArtCache() {
        try {
            int id = sService.getMediaMountedCount();
            if (id != sArtCacheId) {
                clearAlbumArtCache();
                sArtCacheId = id; 
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void clearAlbumArtCache() {
        synchronized(sArtCache) {
            sArtCache.clear();
        }
    }
    
    public static Drawable getCachedArtwork(Context context, long artIndex, String albumName, BitmapDrawable defaultArtwork) {
        Drawable d = null;
        synchronized(sArtCache) {
            d = sArtCache.get(artIndex);
        }
        if (d == null) {
            d = defaultArtwork;
            final Bitmap icon = defaultArtwork.getBitmap();
            int w = icon.getWidth();
            int h = icon.getHeight();
            Bitmap b = MusicUtils.getArtworkQuick(context, artIndex, w, h);
            if(b == null) {
                b = MusicUtils.getLocalBitmap(context, albumName, DownLoadAllPicsAsync.ALBUM_PATH);
                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        // Bitmap.createScaledBitmap() can return the same bitmap
                        if (tmp != b) b.recycle();
                        b = tmp;
                    }
                }
            }
            if (b != null) {
                d = new FastBitmapDrawable(b);
                synchronized(sArtCache) {
                    // the cache may have changed since we checked
                    Drawable value = sArtCache.get(artIndex);
                    if (value == null) {
                        sArtCache.put(artIndex, d);
                    } else {
                        d = value;
                    }
                }
            }
        }
        return d;
    }
    
    public static Bitmap getLocalBitmap(Context context, String name, String path) {
        Bitmap b = null;
        InputStream isImg = null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdCardDir = Environment.getExternalStorageDirectory() + path;

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;                
                isImg = new FileInputStream(sdCardDir + name);
                b = BitmapFactory.decodeStream(isImg, null, null);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    if (isImg != null)
                        isImg.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                isImg = null;
            }
        }
        return b;        
    }

    // Get album art for specified album. This method will not try to
    // fall back to getting artwork directly from the file, nor will
    // it attempt to repair the database.
    private static Bitmap getArtworkQuick(Context context, long album_id, int w, int h) {
        // NOTE: There is in fact a 1 pixel border on the right side in the ImageView
        // used to display this drawable. Take it into account now, so we don't have to
        // scale later.
        w -= 1;
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");
                int sampleSize = 1;
                
                // Compute the closest power-of-two scale factor 
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality
                sBitmapOptionsCache.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);
                int nextWidth = sBitmapOptionsCache.outWidth >> 1;
                int nextHeight = sBitmapOptionsCache.outHeight >> 1;
                while (nextWidth>w && nextHeight>h) {
                    sampleSize <<= 1;
                    nextWidth >>= 1;
                    nextHeight >>= 1;
                }

                sBitmapOptionsCache.inSampleSize = sampleSize;
                sBitmapOptionsCache.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);

                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        // Bitmap.createScaledBitmap() can return the same bitmap
                        if (tmp != b) b.recycle();
                        b = tmp;
                    }
                }
                
                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    /** Get album art for specified album. You should not pass in the album id
     * for the "unknown" album here (use -1 instead)
     * This method always returns the default album art icon when no album art is found.
     */
    public static Bitmap getArtwork(Context context, long song_id, long album_id) {
        return getArtwork(context, song_id, album_id, true);
    }

    /** Get album art for specified album. You should not pass in the album id
     * for the "unknown" album here (use -1 instead)
     */
    public static Bitmap getArtwork(Context context, long song_id, long album_id,
            boolean allowdefault) {

        if (album_id < 0) {
            // This is something that is not in the database, so get the album art directly
            // from the file.
            if (song_id >= 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            if (allowdefault) {
                return getDefaultArtwork(context);
            }
            return null;
        }

        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, sBitmapOptions);
            } catch (FileNotFoundException ex) {
                // The album art thumbnail does not actually exist. Maybe the user deleted it, or
                // maybe it never existed to begin with.
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefault) {
                            return getDefaultArtwork(context);
                        }
                    }
                } else if (allowdefault) {
                    bm = getDefaultArtwork(context);
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                }
            }
        }
        
        return null;
    }
    
    // get album art for specified file
    private static final String sExternalMediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();
    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;
        byte [] art = null;
        String path = null;

        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }

        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch (IllegalStateException ex) {
        } catch (FileNotFoundException ex) {
        }
        if (bm != null) {
            mCachedBit = bm;
        }
        return bm;
    }
    
    private static Bitmap getDefaultArtwork(Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return null;
        //BitmapFactory.decodeStream(
          //     context.getResources().openRawResource(R.drawable.albumart_mp_unknown), null, opts);//null
    }
    
    public static int getIntPref(Context context, String name, int def) {
        SharedPreferences prefs =
            context.getSharedPreferences(MUSIC_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getInt(name, def);
    }
    
    public static void setIntPref(Context context, String name, int value) {
        SharedPreferences prefs =
            context.getSharedPreferences(MUSIC_PREFERENCES, Context.MODE_PRIVATE);
        Editor ed = prefs.edit();
        ed.putInt(name, value);
        ed.commit();
        //SharedPreferencesCompat.apply(ed);
    }

    public static void setRingtone(Context context, long id) {
        ContentResolver resolver = context.getContentResolver();
        // Set the flag in the database to mark this as a ringtone
        Uri ringUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        try {
            ContentValues values = new ContentValues(2);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, "1");
            values.put(MediaStore.Audio.Media.IS_ALARM, "1");
            resolver.update(ringUri, values, null, null);
        } catch (UnsupportedOperationException ex) {
            // most likely the card just got unmounted
            Log.e(TAG, "couldn't set ringtone flag for id " + id);
            return;
        }

        String[] cols = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE
        };

        String where = MediaStore.Audio.Media._ID + "=" + id;
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                cols, where , null, null);
        try {
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                Settings.System.putString(resolver, Settings.System.RINGTONE, ringUri.toString());
                String message = context.getString(R.string.ringtone_set, cursor.getString(2));
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    static int sActiveTabIndex = -1;
    

    
    static void updateNowPlaying(Activity a) {
       /* View nowPlayingView = a.findViewById(R.id.nowplaying);
        if (nowPlayingView == null) {
            return;
        }
        try {
            boolean withtabs = false;
            Intent intent = a.getIntent();
            if (intent != null) {
                withtabs = intent.getBooleanExtra("withtabs", false);
            }
            if (true && MusicUtils.sService != null && MusicUtils.sService.getAudioId() != -1) {
                TextView title = (TextView) nowPlayingView.findViewById(R.id.title);
                TextView artist = (TextView) nowPlayingView.findViewById(R.id.artist);
                title.setText(MusicUtils.sService.getTrackName());
                String artistName = MusicUtils.sService.getArtistName();
                if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
                    artistName = a.getString(R.string.unknown_artist_name);
                }
                artist.setText(artistName);
                //mNowPlayingView.setOnFocusChangeListener(mFocuser);
                //mNowPlayingView.setOnClickListener(this);
                nowPlayingView.setVisibility(View.VISIBLE);
                nowPlayingView.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        Context c = v.getContext();
                        c.startActivity(new Intent(c, MediaPlaybackActivity.class));
                    }});
                return;
            }
        } catch (RemoteException ex) {
        }
        nowPlayingView.setVisibility(View.GONE);*/
    }

    public static void setBackground(View v, Bitmap bm) {

        if (bm == null) {
            v.setBackgroundResource(0);
            return;
        }

        int vwidth = v.getWidth();
        int vheight = v.getHeight();
        int bwidth = bm.getWidth();
        int bheight = bm.getHeight();
        float scalex = (float) vwidth / bwidth;
        float scaley = (float) vheight / bheight;
        float scale = Math.max(scalex, scaley) * 1.0f;

        Bitmap.Config config = Bitmap.Config.RGB_565;
        Bitmap bg = Bitmap.createBitmap(vwidth, vheight, config);
        Canvas c = new Canvas(bg);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        ColorMatrix greymatrix = new ColorMatrix();
        //greymatrix.setSaturation(0);
        ColorMatrix darkmatrix = new ColorMatrix();
        darkmatrix.setScale(0.5f, 0.5f, 0.5f, 1.0f);
        greymatrix.postConcat(darkmatrix);
        ColorFilter filter = new ColorMatrixColorFilter(greymatrix);
        paint.setColorFilter(filter);
        Matrix matrix = new Matrix();
        matrix.setTranslate(-bwidth/2, -bheight/2); // move bitmap center to origin
//        matrix.postRotate(10);
        matrix.postScale(scale, scale);
        matrix.postTranslate(vwidth/2, vheight/2);  // Move bitmap center to view center
        c.drawBitmap(bm, matrix, paint);
        v.setBackgroundDrawable(new BitmapDrawable(bg));
    }
    
    public static void darkBackground() {
        Paint paint = new Paint();
    	ColorMatrix greymatrix = new ColorMatrix();
        greymatrix.setSaturation(0);
        ColorMatrix darkmatrix = new ColorMatrix();
        darkmatrix.setScale(.3f, .3f, .3f, 1.0f);
        greymatrix.postConcat(darkmatrix);
        ColorFilter filter = new ColorMatrixColorFilter(greymatrix);
        paint.setColorFilter(filter);
    }

    static int getCardId(Context context) {
        ContentResolver res = context.getContentResolver();
        Cursor c = res.query(Uri.parse("content://media/external/fs_id"), null, null, null, null);
        int id = -1;
        if (c != null) {
            c.moveToFirst();
            id = c.getInt(0);
            c.close();
        }
        return id;
    }

    static class LogEntry {
        Object item;
        long time;

        LogEntry(Object o) {
            item = o;
            time = System.currentTimeMillis();
        }

        void dump(PrintWriter out) {
            sTime.set(time);
            out.print(sTime.toString() + " : ");
            if (item instanceof Exception) {
                ((Exception)item).printStackTrace(out);
            } else {
                out.println(item);
            }
        }
    }

    private static LogEntry[] sMusicLog = new LogEntry[100];
    private static int sLogPtr = 0;
    private static Time sTime = new Time();


    static void debugLog(Object o) {

        sMusicLog[sLogPtr] = new LogEntry(o);
        sLogPtr++;
        if (sLogPtr >= sMusicLog.length) {
            sLogPtr = 0;
        }
    }

    static void debugDump(PrintWriter out) {
        for (int i = 0; i < sMusicLog.length; i++) {
            int idx = (sLogPtr + i);
            if (idx >= sMusicLog.length) {
                idx -= sMusicLog.length;
            }
            LogEntry entry = sMusicLog[idx];
            if (entry != null) {
                entry.dump(out);
            }
        }
    }
    
    public static void deleteItems(final Context context, String desc, final long[] itemList) {
        
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.setTitle(context.getResources().getString(R.string.title_dialog_xml));
        alertDialog.setMessage(desc);

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getResources()
                .getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTracks(context, itemList);
                    }
                });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources()
                .getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        alertDialog.show();
      }
    
    public static void addToNewPlaylist(Context context, long[] id, int resultType) {
        if (id == null) {
            return;
        }
        
        ArrayList<Integer> arrSongsId = new ArrayList<Integer>();
        int size = id.length;
        for (int i = 0; i < size; i++) {
            arrSongsId.add(new Integer((int)id[i]));
        }
        Intent intent = new Intent(MusicUtils.ACTION_ADD_PLAYLIST);
        intent.putIntegerArrayListExtra("song_id", arrSongsId);
        if (resultType >= 0)
            context.startActivity(intent);
        else
            ((Activity)context).startActivityForResult(intent, resultType);
    }
    
    public static int[] storeSortKey(Cursor cursor, int sortKeyIdx) {
        if(cursor == null || cursor.getCount() == 0)
            return null; 
        int totalCount = cursor.getCount();
        cursor.moveToFirst();
        int[] sortKeyArray = new int[cursor.getCount()];
        sortKeyArray[0] = cursor.getPosition();
        if(totalCount == 1) {            
            return sortKeyArray;
        }
        String lastSortKey = cursor.getString(sortKeyIdx).substring(0,1);
        int lastNum = 0;
        cursor.moveToNext();
        for(int i = 1; i < cursor.getCount(); i++) {                
            String currentSortKey = cursor.getString(sortKeyIdx).substring(0,1);
            if((currentSortKey.toUpperCase()).equals(lastSortKey.toUpperCase())) {
                sortKeyArray[i] = lastNum;
            } else {
                sortKeyArray[i] = cursor.getPosition();
                lastNum = cursor.getPosition();
            }
            lastSortKey = currentSortKey;
            cursor.moveToNext();
        }
        return sortKeyArray;
    }
    
    public static String[] getFolderPath(Context context) {
        lewaDBhelp = new PlayerDBHelper(context,"com_lewa_player.db", true);
        String[] lewaFolderPath;
        synchronized(lewaDBhelp){
            lewaFolderPath = lewaDBhelp.getDBFolder();
        }
        
        return lewaFolderPath;
    }
    
    public static void updateFolderPath(Context context, String[] path) {
        lewaDBhelp = new PlayerDBHelper(context,"com_lewa_player.db", true);
        lewaDBhelp.updateDBFolder(path);
    }

    public static ArrayList<String> getPathList(Context context) {
        Cursor dataCursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
                new String[] {MediaStore.Audio.Media.DATA}, MediaStore.Audio.Media.IS_MUSIC + "=1" 
                + " and _data not like '/mnt/sdcard/LEWA/Voice_Recorder%'" 
                + " and _data not like '/mnt/sdcard/LEWA/PIM%'",
                null, MediaStore.Audio.Media.DATA);
        ArrayList<String> list = new ArrayList<String>();
        if(dataCursor == null || dataCursor.getCount() == 0) {
            return list;
        }
        String data;
        String subData;
        if (dataCursor != null) {
            try {
                dataCursor.moveToFirst();
                for (int i = 0; i < dataCursor.getCount(); i++) {
                    data = dataCursor.getString(dataCursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    subData = data.substring(0, data.lastIndexOf("/"));
                    dataCursor.moveToNext();
                    if (list.contains(subData))
                        continue;
                    else {
                        list.add(subData);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "e = " + e);
            } finally {
                dataCursor.close();
            }
        } 
        return list;
    }

    public static ArrayList<Long> getFolderAudioId(Context context, String folderPath, int flag) {
        ArrayList<Long> audioIdList = new ArrayList<Long>();
        String[] cols = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST_ID
        };
        Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols,
                MediaStore.Audio.Media.DATA + " LIKE " + "'"+ folderPath +"/"+"%"+"' AND " 
                        + MediaStore.Audio.Media.IS_MUSIC + "=1",
                null, null);
        int count = c.getCount();
        if(c != null && count > 0) {
            c.moveToFirst(); 
            for(int i = 0; i < count; i++) {
                if(c.getString(1).lastIndexOf("/") == folderPath.length() 
                        && c.getString(1).startsWith(folderPath)) {
                    switch(flag) {
                        case 0:     //all tracks
                        case 3:     //genre
                                audioIdList.add(Long.valueOf(c.getLong(0)));
                            break;
                        case 1:     //album
                                audioIdList.add(Long.valueOf(c.getLong(2)));
                            break;
                        case 2:     //artist
                                audioIdList.add(Long.valueOf(c.getLong(2)));
                            break;
                    }                    
                }
                c.moveToNext();
            }
        }
        c.close();
        return audioIdList;
    }
    
    public static String getWhereBuilder(Context context, String idName, int isGenre) {
        StringBuilder where = new StringBuilder();
        where.append("");
        String[] folderPath = getFolderPath(context);
        int len = 0;
        if(folderPath != null) {
            len = folderPath.length;
        }
        if(len == 0) {
            if(isGenre == 0) {
                where.append(" and _id not in (select " + idName + " from audio where" +
                        " _data like '/mnt/sdcard/LEWA/Voice_Recorder%'" +
                        " or _data like '/mnt/sdcard/LEWA/PIM%')");
            } else if(isGenre == 1) {
                where.append(" and audio_id not in (select " + idName + " from audio where" +
                        " _data like '/mnt/sdcard/LEWA/Voice_Recorder%'" +
                        " or _data like '/mnt/sdcard/LEWA/PIM%')");
            }
            return where.toString();
        } else {
          if(isGenre == 0) {
              where.append(" and _id in ( select " + idName + " from audio where ((");
          } else if(isGenre == 1) {
              where.append(" and audio_id in ( select " + idName + " from audio where ((");
          }
        }
        for(int i = 0; i < len; i++) {
            where.append(" ( _data like '" + folderPath[i] + "/%' ) AND ");
            where.append(" (_data not like '" + folderPath[i] + "/%/%')");
            if(i < len - 1) {
              where.append(") OR (");
          }
        }
        
        where.append(" and _id not in (select " + idName + " from audio where" +
        		" _data like '/mnt/sdcard/LEWA/Voice_Recorder%'" +
        		" or _data like '/mnt/sdcard/LEWA/PIM% ')");
        if(len > 0)
          where.append(")))");
        return where.toString();
    }
    
    public static void updateTrackInfo(Context context, String valuetag[], long songid) {
    	ContentResolver resolver = context.getContentResolver();
    	Uri trackuri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songid);
    	try {
    		ContentValues values = new ContentValues(3);
    		values.put(MediaStore.Audio.Media.TITLE, valuetag[0]);
    		values.put(MediaStore.Audio.Media.ARTIST, valuetag[1]);    		
    		values.put(MediaStore.Audio.Media.ALBUM, valuetag[2]);
    		resolver.update(trackuri, values, null, null);
    	}catch (Exception e){
    		e.printStackTrace();
    	}
    }
    
    public static Bitmap getPlaylistCover(int playlistId) {
        InputStream isImg = null;
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            isImg = new FileInputStream(COVER_PATH + playlistId);
            bitmap = BitmapFactory.decodeStream(isImg,null,options);
        } catch (FileNotFoundException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();                   
        } finally {
               if (isImg != null)
                   try {
                       isImg.close();
                   } catch (IOException e) {
                       // TODO Auto-generated catch block
                       e.printStackTrace();
                   }
        }
        return bitmap;
    }
    
    public static long[] getSongListForFolder(Context context, String path) {
        final String[] ccols = new String[]{};        
        StringBuilder where = new StringBuilder();
        where.append("_data like '" + path + "/%'");
        where.append("AND _data not like '" + path + "/%/%'");
        where.append("AND _data not like '/mnt/sdcard/LEWA/Voice_Recorder%'");
        where.append("AND _data not like '/mnt/sdcard/LEWA/PIM%'");
        where.append(" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1 AND 1=1");
        
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
                ccols, where.toString(), null, "Upper(sort_key)");

        if (cursor != null) {
            long [] list = getSongListForCursor(cursor);
            cursor.close();
            return list;
        }
        return sEmptyList;
    }
}
