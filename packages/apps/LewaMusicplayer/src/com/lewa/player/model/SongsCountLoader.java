package com.lewa.player.model;

import android.R.integer;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Handler.Callback;
import android.widget.TextView;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;

public class SongsCountLoader implements Callback {

        private static final String LOADER_THREAD_NAME = "SongsCountLoader";

        /**
         * Type of message sent by the UI thread to itself to indicate that some photos
         * need to be loaded.
         */
        private static final int MESSAGE_REQUEST_LOADING = 1;

        /**
         * Type of message sent by the loader thread to indicate that some locations have
         * been loaded.
         */
        private static final int MESSAGE_LOCATIONS_LOADED = 2;
        
        public static final int TYPE_GENRE = 100;
        public static final int TYPE_ARTIST = 101;
        public static final int TYPE_ALBUM = 102;
        
        /**
         * Maintains the state of a particular photo.
         */
        private static class LocationHolder {
            private static final int NEEDED = 0;
            private static final int LOADING = 1;
            private static final int LOADED = 2;

            int state;
            SoftReference<Integer> SongsCountRef;
        }

        /**
         * A soft cache for photos.
         */
        private final ConcurrentHashMap<Integer, LocationHolder> mSongsCountCache =
                new ConcurrentHashMap<Integer, LocationHolder>();

        private final ConcurrentHashMap<TextView, Integer> mPendingRequests =
                new ConcurrentHashMap<TextView, Integer>();

        /**
         * Handler for messages sent to the UI thread.
         */
        private final Handler mMainThreadHandler = new Handler(this);

        /**
         * Thread responsible for loading photos from the database. Created upon
         * the first request.
         */
        private LoaderThread mLoaderThread;

        /**
         * A gate to make sure we only send one instance of MESSAGE_PHOTOS_NEEDED at a time.
         */
        private boolean mLoadingRequested;

        /**
         * Flag indicating if the image loading is paused.
         */
        private boolean mPaused;

        private final Context mContext;

        private int mType;
        
        //add by zhaolei,120323, for artist album
        private int aritstId;
        //end
        /**
         * Constructor.
         *
         * @param context content context
         * @param defaultResourceId the image resource ID to be used when there is
         *            no photo for a contact
         */
        public SongsCountLoader(Context context, int type) {
            mContext = context;
            mType = type;
        }

        public void loadSongsCount(TextView view, int id) {
            if (id <= 0) {
                view.setText(null);
                mPendingRequests.remove(view);
            } else {
                boolean loaded = loadCachedSongsCount(view, id);
                if (loaded) {
                    mPendingRequests.remove(view);
                } else {
                    mPendingRequests.put(view, (id));
                    if (!mPaused) {
                        // Send a request to start loading photos
                        requestLoading();
                    }
                }
            }
        }
        //add by zhaolei,120323, for artist album
        public void setArtistId(int aid){
            aritstId = aid;
        }
        //end

        private boolean loadCachedSongsCount(TextView view, int id) {
            LocationHolder holder = mSongsCountCache.get(id);
            if (holder == null) {
                holder = new LocationHolder();
                mSongsCountCache.put(id, holder);
            } else if (holder.state == LocationHolder.LOADED) {
                int songCnt = 0;
                // Null location reference means that database contains no location for the number
                if (holder.SongsCountRef != null) {
                    songCnt = holder.SongsCountRef.get();
                }

                if (songCnt >= 0) {
                    String string = null;
                        switch (mType) {
                        case TYPE_GENRE: 
                        case TYPE_ALBUM: {
                            string = mContext.getResources().getQuantityString(R.plurals.Nsongs, songCnt, songCnt);
                            break;
                        }
                        case TYPE_ARTIST: {
                            string = mContext.getResources().getQuantityString(R.plurals.Nsongs, songCnt, songCnt);
                            break;
                        }
                        default:
                            break;
                    }
                    view.setText(string);
                    return true;
                }

                // Null location means that the soft reference was released by the GC
                // and we need to reload the location.
                holder.SongsCountRef = null;
            }

            // The location has not been loaded - should display the placeholder location.
//            view.setText(null);
            holder.state = LocationHolder.NEEDED;
            return false;
        }

        /**
         * Stops loading locations, kills the location loader thread and clears all caches.
         */
        public void stop() {
            pause();

            if (mLoaderThread != null) {
                mLoaderThread.quit();
                mLoaderThread = null;
            }

            mPendingRequests.clear();
            mSongsCountCache.clear();
        }

        public void clear() {
            mPendingRequests.clear();
            mSongsCountCache.clear();
        }

        /**
         * Temporarily stops loading locations from the database.
         */
        public void pause() {
            mPaused = true;
        }

        /**
         * Resumes loading locations from the database.
         */
        public void resume() {
            mPaused = false;
            if (!mPendingRequests.isEmpty()) {
                requestLoading();
            }
        }

        private void requestLoading() {
            if (!mLoadingRequested) {
                mLoadingRequested = true;
                mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
            }
        }

        /**
         * Processes requests on the main thread.
         */
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REQUEST_LOADING: {
                    mLoadingRequested = false;
                    if (!mPaused) {
                        if (mLoaderThread == null) {
                            mLoaderThread = new LoaderThread(mContext.getContentResolver());
                            mLoaderThread.start();
                        }

                        mLoaderThread.requestLoading();
                    }
                    return true;
                }

                case MESSAGE_LOCATIONS_LOADED: {
                    if (!mPaused) {
                        processLoadedLocations();
                    }
                    return true;
                }
            }
            return false;
        }

        private void processLoadedLocations() {
            Iterator<TextView> iterator = mPendingRequests.keySet().iterator();
            while (iterator.hasNext()) {
                TextView view = iterator.next();
                int id = mPendingRequests.get(view);
                boolean loaded = loadCachedSongsCount(view, id);
                if (loaded) {
                    iterator.remove();
                }
            }

            if (!mPendingRequests.isEmpty()) {
                requestLoading();
            }
        }

        private void cacheSongsCount(int id, int count) {
            if (mPaused) {
                return;
            }

            LocationHolder holder = new LocationHolder();
            holder.state = LocationHolder.LOADED;
            if (count >= 0) {
                holder.SongsCountRef = new SoftReference<Integer>(count);
            }
            mSongsCountCache.put(id, holder);
        }

        /**
         * Populates an array of numbers that need to be loaded.
         */
        private void obtainSongsToLoad(ArrayList<Integer> ids) {
            ids.clear();

            /*
             * Since the call is made from the loader thread, the map could be
             * changing during the iteration. That's not really a problem:
             * ConcurrentHashMap will allow those changes to happen without throwing
             * exceptions. Since we may miss some requests in the situation of
             * concurrent change, we will need to check the map again once loading
             * is complete.
             */
            Iterator<Integer> iterator = mPendingRequests.values().iterator();
            while (iterator.hasNext()) {
                int id = iterator.next();
                LocationHolder holder = mSongsCountCache.get(id);
                if (holder != null && holder.state == LocationHolder.NEEDED) {
                    // Assuming atomic behavior
                    holder.state = LocationHolder.LOADING;
                    ids.add(id);
                }
            }
        }

        /**
         * The thread that performs loading of locations from the database.
         */
        private class LoaderThread extends HandlerThread implements Callback {
            private final ArrayList<Integer> mSonsIds = new ArrayList<Integer>();
            private Handler mLoaderThreadHandler;

            public LoaderThread(ContentResolver resolver) {
                super(LOADER_THREAD_NAME);
            }

            /**
             * Sends a message to this thread to load requested locations.
             */
            public void requestLoading() {
                if (mLoaderThreadHandler == null) {
                    mLoaderThreadHandler = new Handler(getLooper(), this);
                }
                mLoaderThreadHandler.sendEmptyMessage(0);
            }

            /**
             * Receives the above message, loads locations and then sends a message
             * to the main thread to process them.
             */
            public boolean handleMessage(Message msg) {
                loadSongsCountFromDatabase();
                mMainThreadHandler.sendEmptyMessage(MESSAGE_LOCATIONS_LOADED);
                return true;
            }

            private void loadSongsCountFromDatabase() {
                obtainSongsToLoad(mSonsIds);

                int count = mSonsIds.size();
                if (count == 0) {
                    return;
                }

                int id = mSonsIds.get(0);
                int songsCnt = 0;
                switch (mType) {
                    case TYPE_GENRE: {
                        songsCnt = MusicUtils.getSongListForGenre(mContext, id).length;
                        break;
                    }
                    case TYPE_ARTIST: {
//                        songsCnt = MusicUtils.getAlbumCountForArtist(mContext, id);
                        songsCnt = MusicUtils.getSongListForArtist(mContext, id).length;
                        break;
                    }
                    case TYPE_ALBUM: {
                        songsCnt = MusicUtils.getSongListForAlbum(mContext, id, aritstId).length;
                        break;
                    }
                    default:
                        break;
                }
                cacheSongsCount(id, songsCnt);
            }
        }
    }
