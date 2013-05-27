package com.lewa.player.online;

import java.util.ArrayList;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.ui.MediaPlaybackHomeActivity;
import com.lewa.player.ui.artist.ArtistMainEntryActivity;
import com.lewa.player.ui.outer.AlbumTrackBrowserActivity;
import com.lewa.player.ui.outer.AllTrackBrowserActivity;
import com.lewa.player.ui.outer.MusicMainEntryActivity;
import com.lewa.player.ui.outer.PlaylistBrowserActivity;
import com.lewa.player.ui.outer.PlaylistTrackBrowserActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class OnlineLoader {

	public static ArrayList<bitmapandString> BitmapStack = null;
	private static Context mContext;
	public static final String UPDATEBG = "com.lewa.player.UpdateArtistBG";
	public static final String UPDATELRC = "com.lewa.player.UpdateLRC";
	public static final String GET_PIC_ACTION = "com.lewa.player.getPic";
	public static final String STOPDOWNLOAD = "com.lewa.player.stopDownload";
	
	public static final int ALBUMDOWNLOAD = 1;
	public static final int ARTISTDOWNLOAD = 0;
	public static SharedPreferences settings;

	static public class bitmapandString {
	    //modify by zhaolei,120327,for artistImg save
		String artistName;   //long artistId;
		//end
		String albumStringFilename;
		Bitmap albumbitmap;
	}

	public static void setContext(Context context) {
		mContext = context;
	}

	public static void getArtistImg(String artistName, int ifFavourit) {
		if(artistName == null) {
		    SendtoUpdate(null);
		    return;
		}
        if (downArtorLrc(0) == 1) {
            if (((IsConnection()) || 
                    isWiFiActive(mContext)) && !artistName.equalsIgnoreCase("<unknown>")) {
                String url = getRequestUrl("SearchArtist", artistName);
                DownLoadAsync downArtistImg = new DownLoadAsync();
                downArtistImg.ifFavourit = ifFavourit;
                downArtistImg.execute(url, artistName);
            } else {
                SendtoUpdate(null);
            }
		} else {
			SendtoUpdate(null);
		}
	}
	
	public static int ifwifionly() {
		settings = mContext.getSharedPreferences("Music_setting",0);
		return settings.getInt("iswifi", 1);
		
	}
	
	public static int downArtorLrc(int which) {
		if(mContext == null)return -1;
		settings = mContext.getSharedPreferences("Music_setting",0);
		if(which == 1) {
			return settings.getInt("downLrc", 1);
		}else if(which == 0) {
			return settings.getInt("downImg", 1);
		}
		return -1;
	}
	
	public static void getSongLrc(String TrackName, String artistName, long songid) {
		if(downArtorLrc(1) == 1) {
			if ((IsConnection()) || (isWiFiActive(mContext))){
				
				DownLoadLrc downLrc = new DownLoadLrc();
				downLrc.execute(TrackName, artistName, String.valueOf(songid));
			}
		}
	}

	public static Boolean IsConnection() {
		ConnectivityManager connec = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED
				|| connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING) {
			return true;
		} else if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED
				|| connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
			return false;
		}
		return false;
	}

	public static boolean isWiFiActive(Context inContext) {
		WifiManager mWifiManager = (WifiManager) inContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
		if (mWifiManager.isWifiEnabled() && ipAddress != 0) {
			System.out.println("**** WIFI is on");
			return true;
		} else {
			System.out.println("**** WIFI is off");
			return false;
		}
	}

	public static void SendtoUpdate(Bitmap backg) {
		Bitmap retbit = backg;
		if(retbit == null) {
			retbit = MusicUtils.getDefaultBg(mContext, R.drawable.playlist_default);
		}
		Intent intent = new Intent();
		intent.setAction(UPDATEBG);
		intent.putExtra("backg", retbit);
//		if(mContext.getClass().equals(MediaPlaybackHomeActivity.class)) {
//			intent.setFlags(0);
//		} else if(mContext.getClass().equals(PlaylistBrowserActivity.class)) {
//			intent.setFlags(2);
//		} else {
//		    intent.setFlags(0);
//		}
//        if (mContext.getClass().equals(PlaylistBrowserActivity.class)) {
//            intent.setFlags(2);
//        } else {
//            intent.setFlags(0);
//        }

		mContext.sendBroadcast(intent);
	}
	
	public static void SendtoUpdateFavourit(Bitmap backg) {
		Bitmap retbit = backg;
		if(retbit == null) {
			retbit = MusicUtils.getDefaultArtImg(mContext);
		}
		Intent intent = new Intent();
		intent.setAction(UPDATEBG);
		intent.putExtra("backg", retbit);
		intent.setFlags(5);

		mContext.sendBroadcast(intent);
	}
	
	public static void SendtoUpdateLRC(long trackid, int state) {
		Intent intent = new Intent();
		intent.setAction(UPDATELRC);
		intent.putExtra("id", trackid);
		intent.putExtra("downStat", state);
		mContext.sendBroadcast(intent);
	}

	public static String getRequestUrl(String request, String keywor) {

		String url = null;
		
		if (request.equals("SearchArtist")) {
			Argument[] argu = new Argument[3];
			argu[0] = new Argument("keyword", keywor);
			argu[1] = new Argument("indexstart", String.valueOf(0));
			argu[2] = new Argument("indexend", String.valueOf(0));
			url = APIhelper.GetUrl("Top100.Search.SearchArtist", argu);
		} else if(request.equals("SearchTrack")) {
			Argument[] argu = new Argument[3];
			argu[0] = new Argument("keyword", keywor);
			argu[1] = new Argument("indexstart", String.valueOf(0));
			argu[2] = new Argument("indexend", String.valueOf(0));
			url = APIhelper.GetUrl("Top100.Search.SearchTrack", argu);
		} else if(request.equals("GetTrack")) {
			Argument[] argu = new Argument[1];
			argu[0] = new Argument("trackid", keywor);
			url = APIhelper.GetUrl("Top100.Track.GetTrack", argu);
		} else if(request.equals("SearchAlbum")) {
		    Argument[] argu = new Argument[3];
            argu[0] = new Argument("keyword", keywor);
            argu[1] = new Argument("indexstart", String.valueOf(0));
            argu[2] = new Argument("indexend", String.valueOf(10));
            url = APIhelper.GetUrl("Top100.Search.SearchAlbum", argu);
		}
		return url;
	}

}
