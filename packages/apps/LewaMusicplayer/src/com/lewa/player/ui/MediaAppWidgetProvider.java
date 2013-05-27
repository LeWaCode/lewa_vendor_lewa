
package com.lewa.player.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.online.DownLoadAsync;
import com.lewa.player.online.LocalAsync;
import com.lewa.player.online.OnlineLoader;
import com.lewa.player.online.LocalAsync.bitmapandname;
import com.lewa.player.ui.outer.MusicMainEntryActivity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Simple widget to show currently playing album art along
 * with play/pause and next track buttons.  
 */
public class MediaAppWidgetProvider extends AppWidgetProvider {
    static final String TAG = "MusicAppWidgetProvider";
    public static final String CMDAPPWIDGETUPDATE = "appwidgetupdate";

    private static MediaAppWidgetProvider sInstance;
    private Bitmap artistBm;
    
    public static synchronized MediaAppWidgetProvider getInstance() {
        if (sInstance == null) {
            sInstance = new MediaAppWidgetProvider();
        }
        return sInstance;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        defaultAppWidget(context, appWidgetIds);
        
        Intent serviceIntent = new Intent(context, MediaPlaybackService.class);
        context.startService(serviceIntent);
        
        // Send broadcast intent to any running MediaPlaybackService so it can
        // wrap around with an immediate update.
        Intent updateIntent = new Intent(MediaPlaybackService.SERVICECMD);
        updateIntent.putExtra(MediaPlaybackService.CMDNAME,
                MediaAppWidgetProvider.CMDAPPWIDGETUPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);
        
    }
    
    /**
     * Initialize given widgets to default state, where we launch Music on default click
     * and hide actions if service not running.
     */
    private void defaultAppWidget(Context context, int[] appWidgetIds) {
        final Resources res = context.getResources();
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.album_appwidget);
        
        views.setImageViewResource(R.id.widget_album, R.drawable.albumart_mp_unknown_widget);
        views.setImageViewResource(R.id.widget_play, R.drawable.widget_play_selector); 
        
        views.setViewVisibility(R.id.widget_trackname, View.GONE);
        views.setViewVisibility(R.id.widget_artistname, View.GONE);
        views.setViewVisibility(R.id.widget_worning_tip, View.VISIBLE);
        if(MusicUtils.mHasSongs){
        	views.setTextViewText(R.id.widget_worning_tip, context.getText(R.string.click_to_shuffle));
        }else{
        	views.setTextViewText(R.id.widget_worning_tip, context.getText(R.string.phone_no_songs));
        }
        
        views.setTextViewText(R.id.widget_currenttime, "0:00");
        views.setTextViewText(R.id.widget_endtime, "0:00");
        views.setProgressBar(R.id.widget_progressbar, 1000, 0, false);
        
//        views.setViewVisibility(R.id.widget_worning_tip, View.GONE);
        
        linkButtons(context, views, false /* not playing */);
        pushUpdate(context, appWidgetIds, views);
    }
    
    private void pushUpdate(Context context, int[] appWidgetIds, RemoteViews views) {
        // Update specific list of appWidgetIds if given, otherwise default to all
        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        if (appWidgetIds != null) {
            gm.updateAppWidget(appWidgetIds, views);
        } else {
            gm.updateAppWidget(new ComponentName(context, this.getClass()), views);
        }
    }
    
    /**
     * Check against {@link AppWidgetManager} if there are any instances of this widget.
     */
    
    private boolean hasInstances(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, this.getClass()));
        return (appWidgetIds.length > 0);
    }

    /**
     * Handle a change notification coming over from {@link MediaPlaybackService}
     */
    public void notifyChange(MediaPlaybackService service, String what) {
        if (hasInstances(service)) {
            if (MediaPlaybackService.META_CHANGED.equals(what) ||
                    MediaPlaybackService.PLAYSTATE_CHANGED.equals(what)) {
                artistBm = null;
                performUpdate(service, null);
            }
        }
    }
    
    /**
     * Update all active widget instances by pushing changes 
     */
    public void performUpdate(MediaPlaybackService service, int[] appWidgetIds) {    
        
        final Resources res = service.getResources();
        final RemoteViews views = new RemoteViews(service.getPackageName(), R.layout.album_appwidget);
        
        String titleName = service.getTrackName();
        String artistName = service.getArtistName();
        String showTitleName = titleName;
        String showArtistName = artistName;
        CharSequence errorState = null;
        String title_blank = service.getString(R.string.title_blank);
        String artist_blank = service.getString(R.string.artist_blank);
        
        int len = 0;
        if(titleName != null) {
            len = titleName.trim().getBytes().length;
            if(len > 0 && len <= 4) {
                showTitleName = title_blank + title_blank + titleName + title_blank + title_blank;
            } else if(len <= 9) {
                showTitleName = title_blank + titleName + title_blank;
            }
        }
        if(artistName != null) {
            len = artistName.trim().getBytes().length;
            if(len > 0 && len <= 6) {
                showArtistName = title_blank + title_blank + artistName + title_blank + title_blank;
            } else if(len <= 8) {
                showArtistName = title_blank + artistName + title_blank;
            }
        }
        
        // Format title string with track number, or show SD card message
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_SHARED) ||
                status.equals(Environment.MEDIA_UNMOUNTED) || 
                status.equals(Environment.MEDIA_REMOVED)) {
            if (android.os.Environment.isExternalStorageRemovable()) {
                errorState = res.getText(R.string.nosdcard_title);
            } else {
                errorState = res.getText(R.string.nousb_title);
            }
        } else if (titleName == null) {
        	if(!MusicUtils.mHasSongs){
        		errorState = res.getText(R.string.phone_no_songs);
        	}else{
        		errorState = res.getText(R.string.click_to_shuffle);
        	}
            views.setTextViewText(R.id.widget_currenttime, "0:00");
            views.setTextViewText(R.id.widget_endtime, "0:00");
            views.setProgressBar(R.id.widget_progressbar, 1000, 0, false);
        }
        
        if (errorState != null) {
            // Show error state to user
            views.setViewVisibility(R.id.widget_trackname, View.GONE);
            views.setViewVisibility(R.id.widget_artistname, View.GONE);
            views.setViewVisibility(R.id.widget_worning_tip, View.VISIBLE);
            views.setTextViewText(R.id.widget_worning_tip, errorState);
            views.setImageViewResource(R.id.widget_album,
                    R.drawable.albumart_mp_unknown_widget);
            
        } else {
            // No error, so show normal titles
            views.setViewVisibility(R.id.widget_trackname, View.VISIBLE);
            views.setViewVisibility(R.id.widget_artistname, View.VISIBLE);
            views.setViewVisibility(R.id.widget_worning_tip, View.GONE);
            views.setTextViewText(R.id.widget_trackname, showTitleName);
            views.setTextViewText(R.id.widget_artistname, showArtistName); 
            
            final Context context = service.getApplicationContext();           
            
            String currentTime =  MusicUtils.makeTimeString(context, service.position() / 1000);
            String totalTime = MusicUtils.makeTimeString(context, service.duration() / 1000);
            int progress = 0;
            if(service.duration() != 0) {
                progress = (int) (service.position() * 1000 / service.duration());
            }
            views.setTextViewText(R.id.widget_currenttime, currentTime);
            views.setTextViewText(R.id.widget_endtime, totalTime);
            views.setProgressBar(R.id.widget_progressbar, 1000, progress, false);
            
            String unknown = context.getString(R.string.unknown_artist_name);
            
            if(!(unknown.equals(artistName.trim()))) {
//                artistBm = getArtistBitmap(artistName.trim());
                if(artistBm == null) {
                    artistBm = MusicUtils.getLocalBitmap(context, artistName.trim(), DownLoadAsync.ARTIST_PATH);
                }
                if (artistBm != null) {
                    views.setImageViewBitmap(R.id.widget_album, artistBm);
                } else {
                    views.setImageViewResource(R.id.widget_album,
                            R.drawable.albumart_mp_unknown_widget);
//                    Intent downArtistintent = new Intent();
//                    downArtistintent.setAction("downartist");
//                    downArtistintent.putExtra("artistname", artistName.trim());
//                    context.sendBroadcast(downArtistintent);
                }
            }
        }
        
        // Set correct drawable for pause state
        final boolean playing = service.isPlaying();
        if (playing) {
            views.setImageViewResource(R.id.widget_play, R.drawable.widget_pause_selector);
        } else {
            views.setImageViewResource(R.id.widget_play, R.drawable.widget_play_selector);
        }

        // Link actions buttons to intents
        linkButtons(service, views, playing);
        
        pushUpdate(service, appWidgetIds, views);
    }
    
    private Bitmap getArtistBitmap(CharSequence artistName) {
        Bitmap artistBm = null;
        InputStream isImg = null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdCardDir = Environment.getExternalStorageDirectory() 
                + DownLoadAsync.ARTIST_PATH;

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                isImg = new FileInputStream(sdCardDir + artistName);
                artistBm = BitmapFactory.decodeStream(isImg, null, null);
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
        return artistBm;
    }

    
    
    /**
     * Link up various button actions using {@link PendingIntents}.
     * 
     * @param playerActive True if player is active in background, which means
     *            widget click will launch {@link MediaPlaybackActivity},
     *            otherwise we launch {@link MusicBrowserActivity}.
     */
    private void linkButtons(Context context, RemoteViews views, boolean playerActive) {
        // Connect up various buttons and touch events
        Intent intent;
        PendingIntent pendingIntent;
        
        final ComponentName serviceName = new ComponentName(context, MediaPlaybackService.class);
        
        if (playerActive) {
            intent = new Intent(context, MediaPlaybackHomeActivity.class);
            pendingIntent = PendingIntent.getActivity(context,
                    0 /* no requestCode */, intent, 0 /* no flags */);
            views.setOnClickPendingIntent(R.id.widget_album, pendingIntent);
        } else {
            intent = new Intent(context, MusicMainEntryActivity.class);
            pendingIntent = PendingIntent.getActivity(context,
                    0 /* no requestCode */, intent, 0 /* no flags */);
            views.setOnClickPendingIntent(R.id.widget_album, pendingIntent);
        }
        
        intent = new Intent(MediaPlaybackService.PREVIOUS_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context,
                0 /* no requestCode */, intent, 0 /* no flags */);
        views.setOnClickPendingIntent(R.id.widget_previous, pendingIntent);
        
        intent = new Intent(MediaPlaybackService.TOGGLEPAUSE_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context,
                0 /* no requestCode */, intent, 0 /* no flags */);
        views.setOnClickPendingIntent(R.id.widget_play, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_worning_tip, pendingIntent);
        
        intent = new Intent(MediaPlaybackService.NEXT_ACTION);
        intent.setComponent(serviceName);
        pendingIntent = PendingIntent.getService(context,
                0 /* no requestCode */, intent, 0 /* no flags */);
        views.setOnClickPendingIntent(R.id.widget_next, pendingIntent);        
    }

}
