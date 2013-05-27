package com.lewa.player.online;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lewa.player.online.OnlineLoader.bitmapandString;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class DownLoadAsync extends AsyncTask<String, String, bitmapandString>{
	
	public static final String ARTIST_PATH = "/LEWA/music/artist/";
	public int ifFavourit = 0;
	//private bitmapandString 
	@Override
	protected bitmapandString doInBackground(String... params) {
		// TODO Auto-generated method stub
		String requestUrl = params[0];
		Bitmap bitmap = null;
		bitmapandString bundle = new bitmapandString();
		URL url;
		try {
			String startfix = requestUrl.substring(0, requestUrl.indexOf("keyword=") + 8);
			String keyword = requestUrl.substring(requestUrl.indexOf("keyword=") + 8, requestUrl.indexOf("&api_sig"));
			String encodeUrl = URLEncoder.encode(keyword);
			String strurl = startfix + encodeUrl + requestUrl.substring(requestUrl.indexOf("&api_sig"), requestUrl.length());
			//String encodeUrl = URLEncoder.encode(requestUrl.substring(requestUrl.indexOf("keyword=") + 8));
			url = new URL(strurl);
			
			String ImageUrl = null;
			URLConnection conn = url.openConnection();  
	        //conn.setRequestMethod("GET"); 
	        //conn =  url.openConnection();
			try {
                // Get the response
    	        StringBuilder builder = new StringBuilder();
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                                .getInputStream()));
                for (String s = rd.readLine(); s != null; s = rd.readLine()) { 
                	builder.append(s); 
                }             
            
				JSONObject jsonObject = new JSONObject(builder.toString());
				//.getJSONObject("Artist");
				String rsp = jsonObject.getString("rsp"); 
				//JSONArray rsparray = new JSONArray(rsp);
				//String artist = rsparray.get(1).toString();
				JSONObject jsonartist = new JSONObject(rsp); 
				String artistInfo = jsonartist.getString("Artist"); 
				JSONArray artistInfoarray = new JSONArray(artistInfo);
				String artistImageUrl = artistInfoarray.get(0).toString();  
                JSONObject jsonImageUrl = new JSONObject(artistImageUrl); 
				ImageUrl = jsonImageUrl.getString("ImageUrl");				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				bundle.artistName = params[1];   //modify by zhaolei,120327,img save
				return bundle;
			} 

			URL coverUrl = new URL(ImageUrl);
            HttpURLConnection con = (HttpURLConnection) coverUrl.openConnection();
            con.setDoInput(true);
            con.connect();
            InputStream inputStream=con.getInputStream();
            
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = BitmapFactory.decodeStream(inputStream, null, null); 
            inputStream.close();
            
            
            bundle.albumbitmap = bitmap;
            bundle.artistName = params[1];   //modify by zhaolei,120327,img save
            
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bundle.albumbitmap = null;
			return bundle;
		} finally {
			
		}
 
		return bundle;
	}
	
	@Override
	protected void onPostExecute(bitmapandString result) {
		// TODO Auto-generated method stub
		//super.onPostExecute(result);
		if(result != null) {
			//OnlineLoader.BitmapStack.add(result);
//			if(ifFavourit != 1) {
//			    if("-1000".equals(result.artistName)) {
//			        OnlineLoader.SendtoUpdate(null); 
//			    } else {
//			        OnlineLoader.SendtoUpdate(result.albumbitmap);
//			    }
//			}else {
//			    if("-1000".equals(result.artistName)) {
//                    OnlineLoader.SendtoUpdateFavourit(null); 
//                } else {
//                    OnlineLoader.SendtoUpdateFavourit(result.albumbitmap);
//                }	
//			}
		    
            if ("-1000".equals(result.artistName) || result.albumbitmap == null) {
                OnlineLoader.SendtoUpdate(null);
            } else {
                if (bitmaptoSDcard(result)) {
                    OnlineLoader.SendtoUpdate(result.albumbitmap);
                }
            }
//			if(result.albumbitmap != null)
//			    bitmaptoSDcard(result);
		}
		
		this.cancel(true);
	}
	
    @Override 
    protected void onCancelled() { 
        super.onCancelled();         
    } 
	
    private boolean bitmaptoSDcard(bitmapandString albumtofile) {

		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{   
			
		    String sdCardDir = Environment.getExternalStorageDirectory() + ARTIST_PATH;
		    File file = new File(sdCardDir,albumtofile.artistName);
		    file.getParentFile().mkdirs();
            
		    if(!file.exists()) {
		    	try {
					file.createNewFile();					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		   	try 
		   	{
	            FileOutputStream out = new FileOutputStream(file);
	            if(albumtofile.albumbitmap.compress(Bitmap.CompressFormat.PNG, 100, out))
 	            {
	                out.flush();
	                //Log.v(TAG,"Success");
	                out.close();
	                return true;
	            }
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();	          
	        }
		}
		return false;
    }

	
}
