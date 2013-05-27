package com.lewa.player.online;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicUtils;


public class LocalAsync {
	
	private Context mContext;
//    private long preArtistId = -1;
    private int ifFavouritBG = 0;
    
    private static String preArtistName = "";
    
	public LocalAsync(Context context) {
		mContext = context;
	}
	
	public LocalAsync(Context context, int m) {
		mContext = context;
		ifFavouritBG = m;
	}
	
	public class bitmapandid {
		Bitmap b;
		long aid;
	}
	
	public class bitmapandname {
	    Bitmap b;
	    String aName;
	}
	//modify by zhaolei,120322,for artistImg save
	public void LocalArtistImg(String artistName) {
		if(artistName == null){
			return;
		}
//        if(MediaStore.UNKNOWN_STRING.equals(artistName)) {   // preArtistName.equals(artistName)
//            return;
//        } else {
//            preArtistName = artistName;
            LocalImgAsync getLocalImg = new LocalImgAsync();
            getLocalImg.execute(artistName);
//		}
	}
	
	public class LocalLrcAsync extends AsyncTask<Long, Integer, bitmapandid>{

		@Override
		protected bitmapandid doInBackground(Long... arg0) {
			// TODO Auto-generated method stub
			bitmapandid bitmapCacha = new bitmapandid();
			InputStream isImg = null;
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {

                String sdCardDir = Environment.getExternalStorageDirectory()
                        .toString();
                try {
                    isImg = new FileInputStream(sdCardDir + arg0[0]);   
                    bitmapCacha.b = BitmapFactory.decodeStream(isImg);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    bitmapCacha.aid = Long.valueOf(arg0[0]);
                    return bitmapCacha;
                } finally {
					if(isImg != null) {
						try {
							isImg.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}
						isImg = null;
					}
				}
			}
			bitmapCacha.aid = Long.valueOf(arg0[0]);
			return bitmapCacha;
		}
		
		@Override
		protected void onPostExecute(bitmapandid result) {
			// TODO Auto-generated method stub
			//super.onPostExecute(result);
			if(result !=null && result.b != null) {
				OnlineLoader.setContext(mContext);
				if(ifFavouritBG != 1) {
					OnlineLoader.SendtoUpdate(result.b);
				}else {
					OnlineLoader.SendtoUpdateFavourit(result.b);
				}

            }else {
            //    downArtistImg(result.aid);
			}
			this.cancel(true);
			
			
		}
		
        @Override 
        protected void onCancelled() { 
            super.onCancelled(); 
            
        } 
		
	}
	
	
	public class LocalImgAsync extends AsyncTask<String, Integer, bitmapandname>{

		@Override
		protected bitmapandname doInBackground(String... arg0) {
			// TODO Auto-generated method stub
		    bitmapandname bitmapCacha = new bitmapandname();
			InputStream isImg = null;
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			    String sdCardDir = Environment.getExternalStorageDirectory() + DownLoadAsync.ARTIST_PATH;
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    options.inPurgeable = true;
                    options.inInputShareable = true;
                    options.inDither = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    isImg = new FileInputStream(sdCardDir + arg0[0]);
                    bitmapCacha.b = BitmapFactory.decodeStream(isImg, null, null);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					bitmapCacha.aName = arg0[0];
					return bitmapCacha;
				} finally {
					try {
						if(isImg != null)
						isImg.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					isImg = null;
				}
			}
			bitmapCacha.aName = arg0[0];
			return bitmapCacha;
		}
		
		@Override
		protected void onPostExecute(bitmapandname result) {
			// TODO Auto-generated method stub
			//super.onPostExecute(result);
			if(result !=null && result.b != null) {
				OnlineLoader.setContext(mContext);
				if(ifFavouritBG != 1) {
					OnlineLoader.SendtoUpdate(result.b);
				}else {
					OnlineLoader.SendtoUpdateFavourit(result.b);
				}
			}else {
			    //jczou #8167			   
		        if(MediaPlaybackService.isSearchArt())
		            downArtistImg(result.aName);
			}
			try {
				this.finalize();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//this.cancel(true);
			
			
		}
		
        @Override 
        protected void onCancelled() { 
            super.onCancelled();             
        } 		
	}
	
	public void downArtistImg(String artistName) {
		OnlineLoader.setContext(mContext);
        OnlineLoader.getArtistImg(artistName,ifFavouritBG);		
	}
	
	public void setArtistImg(long song_id) {
		long artistid = MusicUtils.getArtistId(mContext, song_id);
        String artistName = MusicUtils.getArtistName(mContext, artistid);
        if(artistid > 0) {
            if(artistName != null) {
                LocalArtistImg(artistName);
            } else {
              //  OnlineLoader.SendtoUpdate(null);
                downArtistImg(artistName);
            }
        }
	}
	
	public void restorePreArtistName() {
	    preArtistName = "";
	}
}
