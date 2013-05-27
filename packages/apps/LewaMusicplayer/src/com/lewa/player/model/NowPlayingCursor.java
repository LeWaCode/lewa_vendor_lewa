package com.lewa.player.model;

import java.util.Arrays;

import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MusicUtils;

import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

public class NowPlayingCursor extends AbstractCursor
{
	
	private Context mContext;
    private String [] mCols;
    private Cursor mCurrentPlaylistCursor;     // updated in onMove
    private int mSize;          // size of the queue
    private long[] mNowPlaying;
    private long[] mCursorIdxs;
    private int mCurPos;
    private IMediaPlaybackService mService;
	
    public NowPlayingCursor(Context context, IMediaPlaybackService service, String [] cols)
    {
        mCols = cols;
        mService  = service;
        mContext = context;
        makeNowPlayingCursor();
        
    }
    private void makeNowPlayingCursor() {
        mCurrentPlaylistCursor = null;
        try {
            mNowPlaying = mService.getQueue();
        } catch (RemoteException ex) {
            mNowPlaying = new long[0];
        }
        mSize = mNowPlaying.length;
        if (mSize == 0) {
            return;
        }

        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media._ID + " IN (");
        for (int i = 0; i < mSize; i++) {
            where.append(mNowPlaying[i]);
            if (i < mSize - 1) {
                where.append(",");
            }
        }
        where.append(")");

        mCurrentPlaylistCursor = MusicUtils.query(mContext,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mCols, where.toString(), null, MediaStore.Audio.Media._ID);

        if (mCurrentPlaylistCursor == null) {
            mSize = 0;
            return;
        }
        
        int size = mCurrentPlaylistCursor.getCount();
        mCursorIdxs = new long[size];
        mCurrentPlaylistCursor.moveToFirst();
        int colidx = mCurrentPlaylistCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        for (int i = 0; i < size; i++) {
            mCursorIdxs[i] = mCurrentPlaylistCursor.getLong(colidx);
            mCurrentPlaylistCursor.moveToNext();
        }
        mCurrentPlaylistCursor.moveToFirst();
        mCurPos = -1;
        
        // At this point we can verify the 'now playing' list we got
        // earlier to make sure that all the items in there still exist
        // in the database, and remove those that aren't. This way we
        // don't get any blank items in the list.
        try {
            int removed = 0;
            for (int i = mNowPlaying.length - 1; i >= 0; i--) {
                long trackid = mNowPlaying[i];
                int crsridx = Arrays.binarySearch(mCursorIdxs, trackid);
                if (crsridx < 0) {
                    //Log.i("@@@@@", "item no longer exists in db: " + trackid);
                    removed += mService.removeTrack(trackid);
                }
            }
            if (removed > 0) {
                mNowPlaying = mService.getQueue();
                mSize = mNowPlaying.length;
                if (mSize == 0) {
                    mCursorIdxs = null;
                    return;
                }
            }
        } catch (RemoteException ex) {
            mNowPlaying = new long[0];
        }
    }

    @Override
    public int getCount()
    {
        return mSize;
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition)
    {
        if (oldPosition == newPosition)
            return true;
        
        if (mNowPlaying == null || mCursorIdxs == null || newPosition >= mNowPlaying.length) {
            return false;
        }

        // The cursor doesn't have any duplicates in it, and is not ordered
        // in queue-order, so we need to figure out where in the cursor we
        // should be.
       
        long newid = mNowPlaying[newPosition];
        int crsridx = Arrays.binarySearch(mCursorIdxs, newid);
        mCurrentPlaylistCursor.moveToPosition(crsridx);
        mCurPos = newPosition;
        
        return true;
    }

    public boolean removeItem(int which)
    {
        try {
            if (mService.removeTracks(which, which) == 0) {
                return false; // delete failed
            }
            int i = (int) which;
            mSize--;
            while (i < mSize) {
                mNowPlaying[i] = mNowPlaying[i+1];
                i++;
            }
            onMove(-1, (int) mCurPos);
        } catch (RemoteException ex) {
        }
        return true;
    }
    
    public void moveItem(int from, int to) {
        try {
            mService.moveQueueItem(from, to);
            mNowPlaying = mService.getQueue();
            onMove(-1, mCurPos); // update the underlying cursor
        } catch (RemoteException ex) {
        }
    }

    private void dump() {
        String where = "(";
        for (int i = 0; i < mSize; i++) {
            where += mNowPlaying[i];
            if (i < mSize - 1) {
                where += ",";
            }
        }
        where += ")";
        Log.i("NowPlayingCursor: ", where);
    }

    @Override
    public String getString(int column)
    {
        try {
            return mCurrentPlaylistCursor.getString(column);
        } catch (Exception ex) {
            onChange(true);
            return "";
        }
    }

    @Override
    public short getShort(int column)
    {
        return mCurrentPlaylistCursor.getShort(column);
    }

    @Override
    public int getInt(int column)
    {
        try {
            return mCurrentPlaylistCursor.getInt(column);
        } catch (Exception ex) {
            onChange(true);
            return 0;
        }
    }

    @Override
    public long getLong(int column)
    {
        try {
            return mCurrentPlaylistCursor.getLong(column);
        } catch (Exception ex) {
            onChange(true);
            return 0;
        }
    }

    @Override
    public float getFloat(int column)
    {
        return mCurrentPlaylistCursor.getFloat(column);
    }

    @Override
    public double getDouble(int column)
    {
        return mCurrentPlaylistCursor.getDouble(column);
    }

    @Override
    public boolean isNull(int column)
    {
        return mCurrentPlaylistCursor.isNull(column);
    }

    @Override
    public String[] getColumnNames()
    {
        return mCols;
    }
    
    @Override
    public void deactivate()
    {
        if (mCurrentPlaylistCursor != null)
            mCurrentPlaylistCursor.deactivate();
    }

    @Override
    public boolean requery()
    {
        makeNowPlayingCursor();
        return true;
    }


}
