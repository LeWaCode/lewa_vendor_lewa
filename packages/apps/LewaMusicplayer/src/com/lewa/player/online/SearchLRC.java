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
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class SearchLRC {
	private URL url;
	public static final String LRC_PATH = "/LEWA/music/lrc/";
	public static final String DEFAULT_LOCAL = "GB2312";
	StringBuffer sb = new StringBuffer();
	long songid = -1;
	String trackName;
	

	public SearchLRC(String musicName, String singerName, long song_id) {

		musicName = musicName.replace(' ', '+');
		if(singerName != null)
		singerName = singerName.replace(' ', '+');
	    trackName = musicName;
		songid = song_id;
		String mName = "";
		String mSinger = "";
		if(musicName != null) {
			mName = URLEncoder.encode(musicName);
		}
		if(singerName != null) {
			mSinger = URLEncoder.encode(singerName);
		}

		String strUrl = "http://box.zhangmen.baidu.com/x?op=12&count=1&title="
				+ mName + "$$" + mSinger + "$$$$";

		Log.d("test", strUrl);

		try {
			url = new URL(strUrl);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		BufferedReader br = null;
		String s;

		try {
			InputStreamReader in = new InputStreamReader(url.openStream());
			Log.d("the encode is ", in.getEncoding());
			br = new BufferedReader(in);
		} catch (IOException e1) {
			Log.d("tag", "br is null");
		}

		try {

			while ((s = br.readLine()) != null) {
				sb.append(s + "\r\n");
				//br.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int fetchLyric() {
		int begin = 0, end = 0, number = 0;// number=0表示暂无歌词
		String strid = "";
		begin = sb.indexOf("<lrcid>");
		Log.d("test", "sb = " + sb);

		if (begin != -1) {
			end = sb.indexOf("</lrcid>", begin);
			strid = sb.substring(begin + 7, end);
			number = Integer.parseInt(strid);
		}else {
			return -1;
		}
		
		String geciURL = "http://box.zhangmen.baidu.com/bdlrc/" + number / 100
				+ "/" + number + ".lrc";
		Log.d("test", "geciURL = " + geciURL);
		
		//ArrayList gcContent = new ArrayList();
		//String s = new String();
		try {
			url = new URL(geciURL);
		} catch (MalformedURLException e2) {
			e2.printStackTrace();
		}
		HttpURLConnection con;
		InputStream inputStream = null;
		try {
			con = (HttpURLConnection) url.openConnection();
			con.connect();
			inputStream = con.getInputStream();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		lrctosdcard(inputStream);
		return 0;

	}
	
	public void lrctosdcard(InputStream in) {
		if (in == null)
		    return;
		
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{   
			
		    String sdCardDir = Environment.getExternalStorageDirectory() + LRC_PATH;
		    //modify by zhaolei,120327,for lrc save
		    File file = new File(sdCardDir, trackName);  //String.valueOf(songid)
		    //end
		    file.getParentFile().mkdirs();
		    int BUFFER_SIZE = 1024; 
		    byte[] buf = new byte[BUFFER_SIZE];    
		    FileOutputStream out = null;
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
		   		int size = 0;
	            out = new FileOutputStream(file);
	            while ((size = in.read(buf)) != -1)     
	                out.write(buf, 0, size);    
	        } 
			catch (FileNotFoundException e) 
			{
	            e.printStackTrace();
	        } catch (IOException e) 
	        {
	            e.printStackTrace();
	          
	        }
	        finally {
	        	try {
	        	    if (out != null)
	        	        out.close();
	        	    if (in != null)
	        	        in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	        	
	        }
		}
	}

}
