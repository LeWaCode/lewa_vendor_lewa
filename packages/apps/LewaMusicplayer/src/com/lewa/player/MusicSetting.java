package com.lewa.player;

import com.lewa.player.ui.MusicFolderActivity;
import com.lewa.player.ui.outer.MusicAboutActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MusicSetting extends PreferenceActivity implements OnPreferenceChangeListener, 
        OnPreferenceClickListener {
    
	private SharedPreferences music_settings;
	private Editor prefsPrivateEditor;	
	
	private String ifBacklight;
	private String ifDownImg;
	private String ifDownLrcBio;
//	private String downStream;
	private String ifWifi;
	private String[] mDowmStreamStr = null;
	private int mDownVal;
	CheckBoxPreference backlightCheBox;
	CheckBoxPreference downImgCheBox;
	CheckBoxPreference downLrcBioCheBox;
//	ListPreference downStreamList;
//	CheckBoxPreference downStreamCheBox;
	ListPreference bgList;
	private int mBgVal;
	private String [] mBgStr;
	private String mBgkey;
	
	private String isShake;
	private String shake_degree;
	private String isFade;
	CheckBoxPreference shakeCheBox;
	CheckBoxPreference fadeCheBox;
	Preference shakedegreePref;
	int mShakeProgress;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		 super.onCreate(savedInstanceState);
		 music_settings = this.getSharedPreferences("Music_setting", 0);
		 
		 prefsPrivateEditor = music_settings.edit();  

		 addPreferencesFromResource(R.xml.music_preference);
		 
		 ifBacklight = getResources().getString(R.string.setting_backlight_key);
		 backlightCheBox = (CheckBoxPreference)findPreference(ifBacklight);
		 
		 ifDownImg = getResources().getString(R.string.setting_downimg_key);
		 downImgCheBox = (CheckBoxPreference)findPreference(ifDownImg);
         
		 ifDownLrcBio = getResources().getString(R.string.setting_downlrc_key);
		 downLrcBioCheBox = (CheckBoxPreference)findPreference(ifDownLrcBio);
		 
		 isShake = getResources().getString(R.string.setting_shake_key);
		 shakeCheBox = (CheckBoxPreference)findPreference(isShake);
		 
		 isFade = getResources().getString(R.string.setting_fade_key);
		 fadeCheBox = (CheckBoxPreference)findPreference(isFade);
		 
//		 ifWifi = getResources().getString(R.string.setting_download_key);
//		 downStreamCheBox = (CheckBoxPreference)findPreference(ifWifi);
//         mDownVal = Integer.valueOf(downStreamList.getValue());
//         mDowmStreamStr = getResources().getStringArray(R.array.download_method);
//         if(mDowmStreamStr != null && mDownVal >=0 && mDownVal < mDowmStreamStr.length)
//             downStreamList.setSummary(mDowmStreamStr[mDownVal]);
		 
		 mBgkey = getResources().getString(R.string.setting_bg_key);
		 bgList = (ListPreference)findPreference(mBgkey);
		 mBgVal = Integer.valueOf(bgList.getValue());
		 mBgStr = getResources().getStringArray(R.array.bg_choise);
		 if(mBgStr != null && mBgVal >=0 && mBgVal < mBgStr.length) {
		     bgList.setSummary(mBgStr[mBgVal]);
		 }
		 
		 backlightCheBox.setOnPreferenceChangeListener(this);
         backlightCheBox.setOnPreferenceClickListener(this);         
         downImgCheBox.setOnPreferenceChangeListener(this);
         downImgCheBox.setOnPreferenceClickListener(this);
         downLrcBioCheBox.setOnPreferenceChangeListener(this);
         downLrcBioCheBox.setOnPreferenceClickListener(this);
         fadeCheBox.setOnPreferenceChangeListener(this);
         fadeCheBox.setOnPreferenceClickListener(this);
//         downStreamCheBox.setOnPreferenceChangeListener(this);
//         downStreamCheBox.setOnPreferenceClickListener(this);
         bgList.setOnPreferenceChangeListener(this);
         
         Preference aboutPreference = findPreference("music_about");
         aboutPreference.setOnPreferenceClickListener(this);
         
         Preference folderPreference = findPreference("music_select_folder");
         folderPreference.setOnPreferenceClickListener(this);
         
         shakeCheBox.setOnPreferenceChangeListener(this);
         shakeCheBox.setOnPreferenceClickListener(this);
         
         shake_degree = getResources().getString(R.string.shake_degree_key);
         shakedegreePref = (Preference)findPreference(shake_degree);
         shakedegreePref.setOnPreferenceClickListener(this);
         if(shakeCheBox.isChecked()) {
             shakedegreePref.setEnabled(true);
         } else {
             shakedegreePref.setEnabled(false);
         }
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
	    if(preference.getKey().equals(ifBacklight)) {
            if(!(backlightCheBox.isChecked())) {
                backlightCheBox.setChecked(true);
            } else {
                backlightCheBox.setChecked(false);
            }             
        }
	    if(preference.getKey().equals(ifDownImg)) {
            if(!(downImgCheBox.isChecked())) {
                downImgCheBox.setChecked(true);
            } else {
                downImgCheBox.setChecked(false);
            }             
        }
	    if(preference.getKey().equals(ifDownLrcBio)) {
            if(!(downLrcBioCheBox.isChecked())) {
                downLrcBioCheBox.setChecked(true);
            } else {
                downLrcBioCheBox.setChecked(false);
            }             
        }
	    if(preference.getKey().equals(ifWifi)){
	        mDownVal = Integer.valueOf((String)newValue);
            if(mDowmStreamStr != null && mDownVal >=0 && mDownVal < mDowmStreamStr.length)
                preference.setSummary(mDowmStreamStr[mDownVal]);
            if(mDownVal == 0) {
                prefsPrivateEditor.putInt("iswifi", 1).commit();
            } else {
                prefsPrivateEditor.putInt("iswifi", 0).commit();
            }
        }
	    if(preference.getKey().equals(isShake)) {
            if(!(shakeCheBox.isChecked())) {
                shakeCheBox.setChecked(true);
                shakedegreePref.setEnabled(true);
            } else {
                shakeCheBox.setChecked(false);
                shakedegreePref.setEnabled(false);
            }
        }
	    if(preference.getKey().equals(mBgkey)){
            mBgVal = Integer.valueOf((String)newValue);
            if(mBgStr != null && mBgVal >=0 && mBgVal < mBgStr.length) {
                bgList.setSummary(mBgStr[mBgVal]);
            }
            prefsPrivateEditor.putInt("playerbg", mBgVal).commit();
        }
	    if(preference.getKey().equals(isFade)){
	    	if(!(fadeCheBox.isChecked())){
	    		fadeCheBox.setChecked(true);

				//Log.i("test","test1");
	    	}else{
	    		fadeCheBox.setChecked(false);

				//Log.i("test","test0");
	    	}
	    }
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		if(preference.getKey().equals(isFade)){
			if(fadeCheBox.isChecked()){
				prefsPrivateEditor.putInt("isFade", 1).commit();
				//Log.i("test","test1");
			}else{
				prefsPrivateEditor.putInt("isFade", 0).commit();
				
				//Log.i("test","test0");
			}
		}else if(preference.getKey().equals(ifBacklight)) {
            if(backlightCheBox.isChecked()) {
                prefsPrivateEditor.putInt("backlight", 1).commit();
            } else {
                prefsPrivateEditor.putInt("backlight", 0).commit();
            }             
        } else if(preference.getKey().equals(ifDownImg)) {
            if(downImgCheBox.isChecked()) {
                prefsPrivateEditor.putInt("downImg", 1).commit();
            } else {
                prefsPrivateEditor.putInt("downImg", 0).commit();
            } 
        } else if(preference.getKey().equals(ifDownLrcBio)) {
            if(downLrcBioCheBox.isChecked()) {
                prefsPrivateEditor.putInt("downLrc", 1).commit();
            } else {
                prefsPrivateEditor.putInt("downLrc", 0).commit();
            } 
        } else if(preference.getKey().equals("music_about")) {
            Intent intent = new Intent().setClass(this, MusicAboutActivity.class);
            startActivity(intent);
        } else if(preference.getKey().equals(isShake)) {
            if(shakeCheBox.isChecked()) {
                prefsPrivateEditor.putInt("shake", 1).commit();                
            } else {
                prefsPrivateEditor.putInt("shake", 0).commit();
            }
            Intent intent = new Intent();
            intent.setAction(MediaPlaybackService.SHAKE);
            sendBroadcast(intent);
        } else if(preference.getKey().equals(shake_degree)) {
            LinearLayout inputLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.shake_degree_set, null);        
            AlertDialog.Builder builder = new AlertDialog.Builder(this);                
            builder.setView(inputLayout);
//            builder.setTitle(R.string.clear_history);
            SeekBar seek = (SeekBar) inputLayout.findViewById(R.id.shake_seekbar);
            seek.setMax(20);
            mShakeProgress = MusicUtils.getIntPref(this, "shake_degree", ShakeListener.DEFAULT_SHAKE_DEGREE);
            seek.setProgress(mShakeProgress - 5);
            seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                        boolean fromUser) {
                    // TODO Auto-generated method stub
                    mShakeProgress = progress + 5;
                }
            });
            
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {                    
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub    
                    prefsPrivateEditor.putInt("shake_degree", mShakeProgress).commit();                    
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {                    
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                }
            }); 
            
            builder.show();
        } else if(preference.getKey().equals("music_select_folder")) {
            Intent intent = new Intent().setClass(this, MusicFolderActivity.class);
            startActivity(intent);
        }
        
		return false;
	}
}
