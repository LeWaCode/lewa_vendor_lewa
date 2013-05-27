package com.lewa.cit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceGroup;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GenerateInfo extends PreferenceActivity implements OnPreferenceClickListener,OnPreferenceChangeListener,DialogInterface.OnClickListener{
    private static final int ITEM1 = Menu.FIRST;
    private static final int ITEM2 = Menu.FIRST+1;

    private Handler mHandler = new Handler();
    private final static String MAGIC_GENERATE_LOG_SDCARD = "*5392*564#";
    private final static String MAGIC_GENERATE_LOG_DATA = "*5392*5641#";
    private final static String LOG_FILE_SDCARD = "/sdcard/LEWA/LOG/%s/";
    private final static String LOG_FILE_DATA = "/data/data/com.lewa.cit/LEWA/LOG/%s/";
    private static String LOG_TO_SDCARD;
    private static String LOG_TO_DATA;
    private boolean isSaveToData = true;
    private Calendar cal ;
    private SimpleDateFormat sf ;

    private Preference toSdcardPref;
    private Preference toDataPref;
    private Preference startTermEmulator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.generatelog_preference);

        cal = Calendar.getInstance();
        sf = new SimpleDateFormat("yyyyMMddHH");
        
        toSdcardPref = (Preference)findPreference("to_sdcard");
        toDataPref = (Preference)findPreference("to_data");

        LOG_TO_SDCARD = String.format(LOG_FILE_SDCARD,sf.format(cal.getTime()));
        Log.v("cit","lewa"+LOG_TO_SDCARD);
        LOG_TO_DATA = String.format(LOG_FILE_DATA,sf.format(cal.getTime()));
        Log.v("cit","lewa"+LOG_TO_DATA);

        toSdcardPref.setSummary(String.format(getString(R.string.sum_out_to_sdcard),LOG_TO_SDCARD));
        toDataPref.setSummary(String.format(getString(R.string.sum_out_to_data),LOG_TO_DATA));

        toSdcardPref.setOnPreferenceClickListener(this);
        toDataPref.setOnPreferenceClickListener(this);
        checkSDCard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        return true;
    }

    private boolean checkSDCard()
    {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        else {
            Toast.makeText(GenerateInfo.this, getString(R.string.sdcard_inexist),
                        Toast.LENGTH_LONG).show();
            toSdcardPref.setEnabled(false);
            return false;
        }
    }
    
    /** 
    * calculate free space in sdcard
    * Get Free Sapce On Sdcard
    */  
    private int GetFreeSpaceOnSd() {  
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory() .getPath());  
        double sdFreeMB = ((double)stat.getAvailableBlocks() * (double) stat.getBlockSize()) / (1024*1024);
        return (int) sdFreeMB;  
    } 

    @Override  
    public boolean onPreferenceChange(Preference preference, Object newValue) {  
        // TODO Auto-generated method stub  
        Log.v("cit", "preference is changed");
        Log.v("cit", preference.getKey());

        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        switch (which) {  
            case Dialog.BUTTON_NEGATIVE:
                break;
            case Dialog.BUTTON_NEUTRAL:
                Toast.makeText(GenerateInfo.this, "neutral ",
                        Toast.LENGTH_LONG).show();
                break;
            case Dialog.BUTTON_POSITIVE:
                
                generateRadioLog(isSaveToData);
                generateDmesgLog(isSaveToData);
                generateKmesgLog(isSaveToData);
                generateTracesLog(isSaveToData);
                generateTombStonesLog(isSaveToData);
                generateRecoveryLog(isSaveToData);
                generateLog(isSaveToData);
                break;
        }
    }

    @Override  
    public boolean onPreferenceClick(Preference preference) {  
        // TODO Auto-generated method stub  
        Log.v("Cit ", "preference is clicked");
        Log.v("Key_Cit", preference.getKey());
        if(preference.getKey().equals("to_sdcard")) {

            Log.v(" cit ","positive button is clicked");
            int freeSpace = 0;
            freeSpace = GetFreeSpaceOnSd();
            Log.v("cit",String.format("free Space = %d",freeSpace));
            
            //if free Space is small than 5 MB
            if(freeSpace > 5) {
                try{
                    File fd = new File("/sdcard/LEWA/LOG/"+sf.format(cal.getTime())+"/");
                    if(!fd.exists()) {
                        if(fd.mkdirs()) {
                            Log.v("cit","cit dir make on sdcard succeed");
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
                isSaveToData = false;
                ShowAlert(String.format(getString(R.string.make_sure),(isSaveToData ? LOG_TO_DATA:LOG_TO_SDCARD)));
            }
        } else if(preference.getKey().equals("to_data")) {

            try{
                try{
                    File fd = new File("/data/data/com.lewa.cit/LEWA/LOG/"+sf.format(cal.getTime())+"/");
                    if(!fd.exists()) {
                        if(fd.mkdirs()) {
                            Log.v("cit","dir make on data succeed");
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
                //Toast.makeText(this,"Saved",Toast.LENGTH_LONG).show();
            } catch(Exception e) {
                e.printStackTrace();
            }
            isSaveToData = true;
            ShowAlert(String.format(getString(R.string.make_sure),(isSaveToData ? LOG_TO_DATA:LOG_TO_SDCARD)));

        } else {

            return false;
        }
        return true;
    }

    private void ShowAlert(String args ){

        AlertDialog.Builder myBuilder = new AlertDialog.Builder(this);
        myBuilder.setTitle(getString(R.string.alert));
        myBuilder.setMessage(args);
        myBuilder.setPositiveButton(getString(R.string.ok), this);
        myBuilder.setNegativeButton(getString(R.string.cancel), this);
        myBuilder.show();
    }

    private void generateLog(final boolean saveToData) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                java.lang.Process p = null;
                try {
                    p = Runtime.getRuntime().exec("sh");
                    java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
                    os.writeBytes("/system/bin/logcat -v time > " + (saveToData ? LOG_TO_DATA : LOG_TO_SDCARD)+"logcat.log");
                    os.flush();
                    os.close();
                    Thread.sleep(3000);
                    p.destroy();
                    p = null;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            android.widget.Toast toast = android.widget.Toast.makeText(
                                    getApplicationContext()
                                    ,String.format(getString(R.string.toast_log_generated)
                                            , (saveToData ? LOG_TO_DATA:LOG_TO_SDCARD))
                                    , android.widget.Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("cit ", " lewacit Exception ");
                } finally {
                    if (null != p) {
                        p.destroy();
                        p = null;
                    }
                }
            }
        };

        thread.start();
    }

    private void generateRadioLog(final boolean saveToData) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                java.lang.Process p = null;
                try {
                    p = Runtime.getRuntime().exec("sh");
                    java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
                    os.writeBytes("/system/bin/logcat -b radio -v time > " + (saveToData ? LOG_TO_DATA : LOG_TO_SDCARD)+"radio.log");
                    Log.d("cit ", " write bytes ok ");
                    os.flush();
                    os.close();
                    Thread.sleep(3000);
                    p.destroy();
                    p = null;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != p) {
                        p.destroy();
                        p = null;
                    }
                }
            }
        };

        thread.start();
    }

    private void generateDmesgLog(final boolean saveToData) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                java.lang.Process p = null;
                try {
                    p = Runtime.getRuntime().exec("su0");
                    java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
                    os.writeBytes("/system/bin/dmesg  > " + (saveToData ? LOG_TO_DATA : LOG_TO_SDCARD)+"dmesg.log");
                    Log.d("cit ", " write bytes ok ");
                    os.flush();
                    os.close();
                    Thread.sleep(3000);
                    p.destroy();
                    p = null;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != p) {
                        p.destroy();
                        p = null;
                    }
                }
            }
        };

        thread.start();
    }
    
    private void generateKmesgLog(final boolean saveToData) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                java.lang.Process p = null;
                try {
                    p = Runtime.getRuntime().exec("su0");
                    java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
                    os.writeBytes("/system/bin/cat /proc/last_kmsg > " + (saveToData ? LOG_TO_DATA : LOG_TO_SDCARD)+"last_kmsg.log");
                    Log.d("cit ", " write bytes ok ");
                    os.flush();
                    os.close();
                    Thread.sleep(3000);
                    p.destroy();
                    p = null;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != p) {
                        p.destroy();
                        p = null;
                    }
                }
            }
        };

        thread.start();
    }

    private void generateTracesLog(final boolean saveToData) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                java.lang.Process p = null;
                try {
                    p = Runtime.getRuntime().exec("su0");
                    java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
                    os.writeBytes("/system/bin/cat /data/anr/traces.txt > " + (saveToData ? LOG_TO_DATA : LOG_TO_SDCARD)+"traces.log");
                    Log.d("cit ", " write bytes ok ");
                    os.flush();
                    os.close();
                    Thread.sleep(3000);
                    p.destroy();
                    p = null;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != p) {
                        p.destroy();
                        p = null;
                    }
                }
            }
        };

        thread.start();
    }

    private void generateTombStonesLog(final boolean saveToData) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                java.lang.Process p = null;
                try {
                    p = Runtime.getRuntime().exec("su0");
                    java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
                    os.writeBytes("/system/bin/cat /data/tombstones > " + (saveToData ? LOG_TO_DATA : LOG_TO_SDCARD)+"tombstones.log");
                    Log.d("cit ", " write bytes ok ");
                    os.flush();
                    os.close();
                    Thread.sleep(3000);
                    p.destroy();
                    p = null;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != p) {
                        p.destroy();
                        p = null;
                    }
                }
            }
        };

        thread.start();
    }

    private void generateRecoveryLog(final boolean saveToData) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                java.lang.Process p = null;
                try {
                    p = Runtime.getRuntime().exec("su0");
                    java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
                    os.writeBytes("/system/bin/cat /cache/recovery/last_log > " + (saveToData ? LOG_TO_DATA : LOG_TO_SDCARD)+"recovery.log");
                    Log.d("cit ", " write bytes ok ");
                    os.flush();
                    os.close();
                    Thread.sleep(3000);
                    p.destroy();
                    p = null;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != p) {
                        p.destroy();
                        p = null;
                    }
                }
            }
        };

        thread.start();
    }
}
