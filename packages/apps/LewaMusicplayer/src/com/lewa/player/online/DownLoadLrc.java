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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import com.lewa.player.online.OnlineLoader.bitmapandString;

public class DownLoadLrc extends AsyncTask<String, Integer, Long>{

	

	
	private int mlrcDownStat = 0;
	//private bitmapandString 
	@Override
	protected Long doInBackground(String... params) {
		// TODO Auto-generated method stub
		/*String requestUrl = params[0];
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
			
			URLConnection conn = url.openConnection();  
	        //conn.setRequestMethod("GET"); 
	        //conn =  url.openConnection();
            // Get the response
	        StringBuilder builder = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                            .getInputStream()));
            for (String s = rd.readLine(); s != null; s = rd.readLine()) { 
            	builder.append(s); 
            } 
            String TrackIDResult = null;
            try {
				JSONObject jsonObject = new JSONObject(builder.toString());
				//.getJSONObject("Artist");
				String rsp = jsonObject.getString("rsp"); 
				//JSONArray rsparray = new JSONArray(rsp);
				//String artist = rsparray.get(1).toString();
				JSONObject jsonartist = new JSONObject(rsp); 
				String trackInfo = jsonartist.getString("Track"); 
				JSONArray artistInfoarray = new JSONArray(trackInfo);
				String TrackId = artistInfoarray.get(0).toString();  
                JSONObject jsonTrackId = new JSONObject(TrackId); 
                TrackIDResult = jsonTrackId.getString("TrackID");
                
                String newurl = OnlineLoader.getRequestUrl("GetTrack", TrackIDResult);
				
                URLConnection connLrc = url.openConnection();  
    	        //conn.setRequestMethod("GET"); 
    	        //conn =  url.openConnection();
                // Get the response
    	        StringBuilder builderlrc = new StringBuilder();
                BufferedReader rdlrc = new BufferedReader(new InputStreamReader(connLrc
                                .getInputStream()));
                for (String s = rdlrc.readLine(); s != null; s = rdlrc.readLine()) { 
                	builder.append(s); 
                } 
                
                JSONObject jsonObjectlrc = new JSONObject(builder.toString());
				//.getJSONObject("Artist");
				String rsplrc = jsonObjectlrc.getString("rsp"); 
				//JSONArray rsparray = new JSONArray(rsp);
				//String artist = rsparray.get(1).toString();
				JSONObject jsonartistlrc = new JSONObject(rsplrc); 
				String trackInfolrc = jsonartistlrc.getString("Track"); 
				JSONArray artistInfoarraylrc = new JSONArray(trackInfolrc);
				String TrackIdlrc = artistInfoarraylrc.get(0).toString();  
                JSONObject jsonTrackIdlrc = new JSONObject(TrackIdlrc); 
                String TrackIDResultlrc = jsonTrackIdlrc.getString("LyricUrl");
				
				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			} 

            
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
 
		return Integer.valueOf(params[1]);*/
		SearchLRC sl = new SearchLRC(params[0], params[1], Long.valueOf(params[2]));
		mlrcDownStat = sl.fetchLyric();
		return Long.valueOf(params[2]);
	}

	
	@Override
	protected void onPostExecute(Long result) {
		// TODO Auto-generated method stub
		//super.onPostExecute(result);
		if(result > 0) {
			OnlineLoader.SendtoUpdateLRC(result, mlrcDownStat);
		}
		
		
	}
	
}
