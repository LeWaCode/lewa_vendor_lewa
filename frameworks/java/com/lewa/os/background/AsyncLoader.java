package com.lewa.os.background;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public abstract class AsyncLoader {
    private static final String LOG_TAG = "AsyncLoader";
    
    private static final String THREAD_NAME = "AsyncLoader";
    
    private static final int LOAD_TOKEN   = 0xD0D0F00D;
    private static final int FINISH_TOKEN = 0xDEADBEEF;

    private Handler mThreadHandler;
    private Handler mResultHandler;

    private final Object mLock = new Object();

    public AsyncLoader() {
        mResultHandler = new ResultsHandler();
    }

    public void load() {
        load(null);
    }

    public void load(LoadListener listener) {
        load(listener, 0);
    }

    public void load(LoadListener listener, long delayMillis) {
        synchronized (mLock) {
            if (mThreadHandler == null) {
                HandlerThread thread = new HandlerThread(
                        THREAD_NAME, android.os.Process.THREAD_PRIORITY_BACKGROUND);
                thread.start();
                mThreadHandler = new RequestHandler(thread.getLooper());
            }

            Message message = mThreadHandler.obtainMessage(LOAD_TOKEN);
    
            RequestArguments args = new RequestArguments();
            args.listener = listener;
            message.obj = args;
    
            mThreadHandler.removeMessages(LOAD_TOKEN);
            mThreadHandler.removeMessages(FINISH_TOKEN);
            mThreadHandler.sendMessageDelayed(message, delayMillis);
        }
    }

    protected abstract LoadResults performLoading();

    protected abstract void publishLoadResults(LoadResults results);

    protected static class LoadResults {
        public LoadResults() {
            // nothing to see here
        }

        /**
         * <p>Contains all the values computed by the loading operation.</p>
         */
        public Object values;
    }

    /**
     * <p>Listener used to receive a notification upon completion of a loading
     * operation.</p>
     */
    public static interface LoadListener {
        /**
         * <p>Notifies the end of a loading operation.</p>
         *
         * @param results the results of the loading operation.
         */
        public void onLoadComplete(LoadResults results);
    }

    private class RequestHandler extends Handler {
        public RequestHandler(Looper looper) {
            super(looper);
        }
        
        public void handleMessage(Message msg) {
            int what = msg.what;
            Message message;
            switch (what) {
                case LOAD_TOKEN:
                    RequestArguments args = (RequestArguments) msg.obj;
                    try {
                        args.results = performLoading();
                    } catch (Exception e) {
                        args.results = new LoadResults();
                        Log.w(LOG_TAG, "An exception occured during performLoading()!", e);
                    } finally {
                        message = mResultHandler.obtainMessage(what);
                        message.obj = args;
                        message.sendToTarget();
                    }

                    synchronized (mLock) {
                        if (mThreadHandler != null) {
                            Message finishMessage = mThreadHandler.obtainMessage(FINISH_TOKEN);
                            mThreadHandler.sendMessageDelayed(finishMessage, 3000);
                        }
                    }
                    break;
                    
                case FINISH_TOKEN:
                    synchronized (mLock) {
                        if (mThreadHandler != null) {
                            mThreadHandler.getLooper().quit();
                            mThreadHandler = null;
                        }
                    }
                    break;
            }
        }
    }

    private class ResultsHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            RequestArguments args = (RequestArguments) msg.obj;

            publishLoadResults(args.results);
            if (args.listener != null) {
                args.listener.onLoadComplete(args.results);
            }
        }
    }

    private static class RequestArguments {
        /**
         * <p>The listener to notify upon completion. Can be null.</p>
         */
        LoadListener listener;

        /**
         * <p>The results of the loading operation.</p>
         */
        LoadResults results;
    }
}
