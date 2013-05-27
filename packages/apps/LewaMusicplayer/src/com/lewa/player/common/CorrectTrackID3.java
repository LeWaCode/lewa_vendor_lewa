package com.lewa.player.common;

import java.io.File;

import com.lewa.player.MusicUtils;

import android.content.Context;
import android.util.Log;
import entagged.audioformats.AudioFile;
import entagged.audioformats.AudioFileIO;

public class CorrectTrackID3 {

	public static String[] getIntagedInfo(String path) {
		
		String[] TagInfo = new String[2];
        File ID3track = null;
        try {
        	ID3track = new File(path);
            AudioFile audioFile = AudioFileIO.read(ID3track);
            TagInfo[0] = audioFile.getTag().getFirstArtist();
            TagInfo[1] = audioFile.getTag().getFirstTitle();
            //Log.e("entagged", "audio id = " + cursor.getInt(mAudioIdIdx));

        } catch (Exception e) {
            // TODO Auto-generated catch block
           Log.e("entagged", "Exception : " + e); 
            e.printStackTrace();

            
            
        }finally {
        	
            if(ID3track != null) {
            	ID3track = null;
            }
        }
		return TagInfo;
		
	}	
	
	public static int setIntagedInfo(Context mContext, String[] TagInfo, String path, long songid) {
		
        File ID3track = null;
        try {
        	ID3track = new File(path);
        	AudioFile audioFile = AudioFileIO.read(ID3track);
        	//Log.e("entagged", "audio id = " + cursor.getInt(mAudioIdIdx));

        	audioFile.getTag().setTitle(TagInfo[0]);
            audioFile.getTag().setArtist(TagInfo[1]);
            audioFile.getTag().setAlbum(TagInfo[2]);            
            
            MusicUtils.updateTrackInfo(mContext, TagInfo, songid);
            AudioFileIO.write(audioFile);            
            
        }catch(Exception e) {
        	return 0;
        }finally{
            if(ID3track != null) {
            	
            	ID3track = null;
            }
        }
		return 1;
	}
}
