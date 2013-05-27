package com.android.settings;

import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

public class LewaKeypadSettings extends PreferenceActivity {
    private static final String KEY_VIRTUAL_KEY = "virtual_key";
    private static final String VOLUME_WAKE_PREF = "pref_volume_wake";
    private static final String BOTTOM_VIRTUAL_KEY = "bottom_virtual_keypad";
    private static final String BUTTON_LIGHT_KEY = "button_light_key";
    private static final String BACK_BTN_UNLOCK_CAMERA = "back_btn_unlock_to_camera_key";

    private CheckBoxPreference mVirtualKey;
    private CheckBoxPreference mVolumeWakePref; 
    private CheckBoxPreference mBottomVirtualKey;
    private CheckBoxPreference mButtonLightKey;
    private CheckBoxPreference mBackUnlockCameraKey;

    private String BUTTON_LIGHT_FILE = "/sys/class/leds/button-backlight/brightness";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.lewa_keypad_settings);
        
        mVirtualKey = (CheckBoxPreference) findPreference(KEY_VIRTUAL_KEY);
        mVirtualKey.setPersistent(false);
        mVolumeWakePref = (CheckBoxPreference) findPreference(VOLUME_WAKE_PREF);
        mBottomVirtualKey = (CheckBoxPreference) findPreference(BOTTOM_VIRTUAL_KEY);
        mBottomVirtualKey.setPersistent(false);
        mButtonLightKey = (CheckBoxPreference) findPreference(BUTTON_LIGHT_KEY);
        mBackUnlockCameraKey = (CheckBoxPreference) findPreference(BACK_BTN_UNLOCK_CAMERA);
        mBackUnlockCameraKey.setPersistent(false);
     
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateState();
    }
    
    private void updateState() {
        mVirtualKey.setChecked(Settings.System.getInt(
            getContentResolver(),
            Settings.System.VIRTUAL_KEY, 0) == 1);
        mVolumeWakePref.setChecked(Settings.System.getInt(
            getContentResolver(),
            Settings.System.VOLUME_WAKE_SCREEN, 0) == 1);
        mBottomVirtualKey.setChecked(Settings.System.getInt(
            getContentResolver(),
            Settings.System.BOTTOM_VIRTUAL_KEY, 0) == 1);
        mBackUnlockCameraKey.setChecked(Settings.System.getInt(
            getContentResolver(),
            Settings.System.BACK_BTN_UNLOCK_CAMERA, 0) == 1);
        if(mVirtualKey.isChecked()) {
            mBottomVirtualKey.setEnabled(true);
        } else {
            mBottomVirtualKey.setEnabled(false);
        }
        if(!modelCheck("U8800")){
            getPreferenceScreen().removePreference(mButtonLightKey);
        }else{
           if(0 == (readOneLine(BUTTON_LIGHT_FILE).compareToIgnoreCase("0\n")))
                mButtonLightKey.setChecked(false);
            else
                mButtonLightKey.setChecked(true);
        }
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mVirtualKey) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.VIRTUAL_KEY,
                    mVirtualKey.isChecked() ? 1 : 0);
            if(mVirtualKey.isChecked()) {
                mBottomVirtualKey.setEnabled(true);
            } else {
                mBottomVirtualKey.setEnabled(false);
            }
            return true;
        } else if (preference == mVolumeWakePref) {
            Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_WAKE_SCREEN,
                    mVolumeWakePref.isChecked()? 1 : 0);
            return true;
        } else if (preference == mBottomVirtualKey) {
            Settings.System.putInt(getContentResolver(), Settings.System.BOTTOM_VIRTUAL_KEY,
                    mBottomVirtualKey.isChecked()? 1 : 0);
            return true;
        } else if (preference == mBackUnlockCameraKey) {
            Settings.System.putInt(getContentResolver(), Settings.System.BACK_BTN_UNLOCK_CAMERA,
                    mBackUnlockCameraKey.isChecked()? 1 : 0);
            return true;
        } else if (preference == mButtonLightKey){
            setFourButtonLightOff(!mButtonLightKey.isChecked());
        }
        return false;
    }
    public boolean modelCheck(String modelName){
        String productVersion = SystemProperties.get("ro.product.model");
        return (0 == productVersion.compareToIgnoreCase(modelName));
    }

    public void setFourButtonLightOff(boolean close) {
        runRootCommand("chmod 644 "+BUTTON_LIGHT_FILE);
        if(close)
            writeOneLine(BUTTON_LIGHT_FILE, "0\n");
        else
            writeOneLine(BUTTON_LIGHT_FILE, "255\n");
        if(close)
            runRootCommand("chmod 444 "+BUTTON_LIGHT_FILE);
    }

    public static boolean writeOneLine(String fname, String value) {
        try {
            FileWriter fw = new FileWriter(fname);
            try {
                fw.write(value);
            } finally {
                fw.close();
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static String readOneLine(String fname) {
        String valueStr = "";
        try{
                FileReader fr = new FileReader(fname);
                int c = 0;
                try{
                    while((c = fr.read()) != -1) {
                        valueStr+=((char)c) ;
                    }
                } finally {
                        fr.close();
                }
        }catch(IOException e) {
            return "";
        }
        return valueStr;
    }

    public static boolean runRootCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                // do nothing
            }
        }
        return true;
    }
}
