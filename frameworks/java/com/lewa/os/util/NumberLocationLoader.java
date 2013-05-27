/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lewa.os.util;

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

import com.lewa.os.util.LocationUtil;

public class NumberLocationLoader implements Callback {

    private static final String LOADER_THREAD_NAME = "NumberLocationLoader";

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

    /**
     * Maintains the state of a particular photo.
     */
    private static class LocationHolder {
        private static final int NEEDED = 0;
        private static final int LOADING = 1;
        private static final int LOADED = 2;

        int state;
        String locationRef;
    }

    /**
     * A soft cache for photos.
     */
    private final ConcurrentHashMap<String, LocationHolder> mLocationCache =
            new ConcurrentHashMap<String, LocationHolder>();

    private final ConcurrentHashMap<TextView, String> mPendingRequests =
            new ConcurrentHashMap<TextView, String>();

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

    /**
     * Constructor.
     *
     * @param context content context
     * @param defaultResourceId the image resource ID to be used when there is
     *            no photo for a contact
     */
    public NumberLocationLoader(Context context) {
        mContext = context;
    }

    public void loadLocation(TextView view, String number) {
        if (number.length() <= 2) {
			view.setText(null);
			if (!mPendingRequests.isEmpty()) {
				mPendingRequests.remove(view);
			}
        } else {
            boolean loaded = loadCachedLocation(view, number);
            if (loaded) {
				if (!mPendingRequests.isEmpty()) {
	                mPendingRequests.remove(view);
				}
            } else {
                mPendingRequests.put(view, number);
                if (!mPaused) {
                    // Send a request to start loading photos
                    requestLoading();
                }
            }
        }
    }

    private boolean loadCachedLocation(TextView view, String number) {
        LocationHolder holder = mLocationCache.get(number);
        if (holder == null) {
            holder = new LocationHolder();
            mLocationCache.put(number, holder);
        } else if (holder.state == LocationHolder.LOADED) {
            // Null location reference means that database contains no location for the number
            if (holder.locationRef == null) {
                view.setText(null);
                return true;
            }

            String location = holder.locationRef;
            if (location != null) {
                view.setText(location);
                return true;
            }

            // Null location means that the soft reference was released by the GC
            // and we need to reload the location.
            holder.locationRef = null;
        }

        // The location has not been loaded - should display the placeholder location.
        view.setText(null);
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
        mLocationCache.clear();
    }

    public void clear() {
        mPendingRequests.clear();
        mLocationCache.clear();
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
            String number = mPendingRequests.get(view);
            boolean loaded = loadCachedLocation(view, number);
            if (loaded) {
                iterator.remove();
            }
        }

        if (!mPendingRequests.isEmpty()) {
            requestLoading();
        }
    }

    private void cacheBitmap(String number, String location) {
        if (mPaused) {
            return;
        }

        LocationHolder holder = new LocationHolder();
        holder.state = LocationHolder.LOADED;
        if (location != null) {
            holder.locationRef = location;
        }
        mLocationCache.put(number, holder);
    }

    /**
     * Populates an array of numbers that need to be loaded.
     */
    private void obtainNumbersToLoad(ArrayList<String> numbers) {
        numbers.clear();

        /*
         * Since the call is made from the loader thread, the map could be
         * changing during the iteration. That's not really a problem:
         * ConcurrentHashMap will allow those changes to happen without throwing
         * exceptions. Since we may miss some requests in the situation of
         * concurrent change, we will need to check the map again once loading
         * is complete.
         */
        Iterator<String> iterator = mPendingRequests.values().iterator();
        while (iterator.hasNext()) {
            String number = iterator.next();
            LocationHolder holder = mLocationCache.get(number);
            if (holder != null && holder.state == LocationHolder.NEEDED) {
                // Assuming atomic behavior
                holder.state = LocationHolder.LOADING;
                numbers.add(number);
            }
        }
    }

    /**
     * The thread that performs loading of locations from the database.
     */
    private class LoaderThread extends HandlerThread implements Callback {
        private final ArrayList<String> mNumbers = new ArrayList<String>();
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
            loadLocationsFromDatabase();
            mMainThreadHandler.sendEmptyMessage(MESSAGE_LOCATIONS_LOADED);
            return true;
        }

        private void loadLocationsFromDatabase() {
            obtainNumbersToLoad(mNumbers);

            int count = mNumbers.size();
            if (count == 0) {
                return;
            }

            String number = mNumbers.get(0);
            String location = LocationUtil.getPhoneLocation(mContext, number);
            cacheBitmap(number, location);
        }
    }
}
