package com.lewatek.swapper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.Formatter;
import java.io.BufferedReader;  
import java.io.FileReader;  
import java.io.IOException; 

import com.lewatek.swapper.R;

import java.io.File;

public class Swapper extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {
    private static final String TAG                = "Swapper";

    private static final int    SDCARDDIALOG       = 1;
    private static final int    HELPDIALOG         = 3;

    private final static int    OPENSTATE          = 1;
    private final static int    CLOSESTATE         = 2;
    private final static int    AUTOSTATE          = 3;

    private final static int    DOOPEN             = 0;
    private final static int    DOCLOSE            = 1;           

    private static final int    OPNE_OPDONE        = OPENSTATE * SwapperCommands.MSG_OPDONE;
    private static final int    OPEN_NOSDCARD      = OPENSTATE * SwapperCommands.MSG_NOSDCARD;
    private static final int    OPEN_NOPARTITION   = OPENSTATE * SwapperCommands.MSG_NOPARTITION;

    private static final int    CLOSE_OPDONE       = CLOSESTATE * SwapperCommands.MSG_OPDONE;
    private static final int    CLOSE_NOSDCARD     = CLOSESTATE * SwapperCommands.MSG_NOSDCARD;
    private static final int    CLOSE_NOSWAPON     = CLOSESTATE * SwapperCommands.MSG_NOSWAPON;
    private static final int    CLOSE_NOSYSTEMFILE = CLOSESTATE * SwapperCommands.MSG_NOSYSTEMFILE;
    private static final int    CLOSE_NOPARTITION  = CLOSESTATE * SwapperCommands.MSG_NOPARTITION;
    private static final int    AUTO_OPDONE        = AUTOSTATE * SwapperCommands.MSG_OPDONE;

    private static final int    FREETITLELENGTH    = 5;

    private static int          sCurrentState      = 0;

    Handler                     mHandler           = null;
    SuCommander                 mSu                = null;
    SwapperCommands             mSc                = null;
    ListPreference              mModeList;
    ListPreference              mSwapSizeList;
    CheckBoxPreference          mAutoCheck;
    CheckBoxPreference          mSwitchCheck;
    SharedPreferences           mSettings;
    LinearLayout                mButtomLayout;
    ProgressDialog              mProgressDialog;

    int                         mOldHowSwap;
    int                         mSwapSize;
    int                         mHowSwap;
    String                      mResultMs;
    String[]                    mHowSwap_array;
    String[]                    mOnoffModeArray;
    boolean                     mBreadyShutDown    = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SwapperCommands.DBUG) {
            Log.d(TAG, "onCreate");
        }
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.swapper_settings);

        initUI(savedInstanceState);
        mOnoffModeArray = getResources().getStringArray(R.array.onoff_mode);

        if (SwapperCommands.DBUG) {
            Log.d(TAG, "init howswap:" + mHowSwap);
            Log.d(TAG, "init swapSize:" + mSwapSize);
        }
        
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Swapper.this.handleMessage(msg);
            }
        };
        mSc = new SwapperCommands(this, mHandler);
        if (null != mSc && mSc.isRunning()) {
            sCurrentState = AUTOSTATE;
            mResultMs = Swapper.this.getResources().getString(R.string.swap_autorun);
            mProgressDialog = ProgressDialog.show(Swapper.this, "", mResultMs, true);
        }
        if(SwapperCommands.DBUG){
            Log.d(TAG,"getTotalMemory:"+getTotalMemory());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SwapperCommands.DBUG) {
            Log.d(TAG, "onResume");
        }
        if (mHowSwap == SwapperCommands.SYSTEMFILE_METHOD || mHowSwap == SwapperCommands.PARTITION_METHOD) {
            mSwapSizeList.setEnabled(false);
        }

        CharSequence modeText = "";
        String swapTotalMemory = getTotalMemory();
        if (swapTotalMemory != null) {
            if (mHowSwap == SwapperCommands.SDCARDFILE_METHOD) {
                modeText = Swapper.this.getResources().getString(R.string.swap_switch_on_filemode, swapTotalMemory);
            }
            if (mHowSwap == SwapperCommands.PARTITION_METHOD) {
                modeText = Swapper.this.getResources().getString(R.string.swap_switch_on_partmode, swapTotalMemory);
            }

            if (SwapperCommands.DBUG) {
                Log.d(TAG, "onResume init howSwap:" + mHowSwap + "==modeText:" + modeText.toString());
            }
            mSwitchCheck.setSummaryOn(modeText);
            mSwitchCheck.setChecked(true);
        } else {
            mSwitchCheck.setChecked(false);
        }
        mAutoCheck.setEnabled(mSwitchCheck.isChecked());

        if (SwapperCommands.DBUG) {
            Log.d(TAG, "sc.isRunning:" + mSc.isRunning());
        }
        if(mSc.isRunning()){
            if(swapTotalMemory == null){
                mResultMs = getDialogMessage(mHowSwap,DOOPEN);
            }else{
                mResultMs = getDialogMessage(mHowSwap,DOCLOSE);
            }
            mProgressDialog = ProgressDialog.show(Swapper.this, "", mResultMs, true);
        }
    }

    private void initUI(Bundle savedInstanceState) {
        Settings.System.putInt(this.getContentResolver(),
                        Settings.System.IS_REBOOT_RUN, 0);

        mAutoCheck = (CheckBoxPreference) findPreference("autorun");
        mSwitchCheck = (CheckBoxPreference) findPreference("switch");
        mModeList = (ListPreference) findPreference("howswap");
        mSwapSizeList = (ListPreference) findPreference("swapSize");

        mSwitchCheck.setOnPreferenceClickListener(this);
        mAutoCheck.setOnPreferenceClickListener(this);
        mModeList.setOnPreferenceChangeListener(this);
        mSwapSizeList.setOnPreferenceChangeListener(this);

        mHowSwap_array = getResources().getStringArray(R.array.howswap);

        if (savedInstanceState != null) {
            mHowSwap = savedInstanceState.getInt("howSwap");
            mSwapSize = savedInstanceState.getInt("swapSize");
        } else {
            mHowSwap = SwapperCommands.readHowSwap(this, mSettings);
            mSwapSize = SwapperCommands.readSwapSize(this, mSettings, SwapperCommands.SDCARDFILE_METHOD);
        }
        mOldHowSwap = mHowSwap;
        String modeText = "";
        switch (mHowSwap) {
            case SwapperCommands.INVALID_METHOD:
            case SwapperCommands.SDCARDFILE_CLOSE_METHOD:
            case SwapperCommands.SDCARDFILE_METHOD:
                mHowSwap = SwapperCommands.SDCARDFILE_METHOD;
                break;
            case SwapperCommands.PARTITION_CLOSE_METHOD:
            case SwapperCommands.PARTITION_METHOD:                
                mHowSwap = SwapperCommands.PARTITION_METHOD;
                break;
            default:
                break;
        }
        modeText = Swapper.this.getResources().getStringArray(R.array.howswap)[mHowSwap - 1];
        mModeList.setSummary(modeText);
        mModeList.setValue("" + mHowSwap);
        mSwapSizeList.setSummary(mSwapSize + "MB");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                showDialog(HELPDIALOG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleMessage(Message msg) {
        int resultState = sCurrentState * msg.what;
        boolean isSwitch = false;
        CharSequence switchSummary = "";
        String modeSummary = "";
        String sizeSummary = "";
        String toastMsg = "";
        String swapTotalMemory = getTotalMemory();
        switch (resultState) {
            case OPEN_NOSDCARD:
                toastMsg = getResources().getString(R.string.no_sdcard_open);
                isSwitch = false;
                break;
            case OPEN_NOPARTITION:
                toastMsg = getResources().getString(R.string.no_partition);
                isSwitch = false;
                break;
            case CLOSE_NOSDCARD:
                toastMsg = getResources().getString(R.string.no_sdcard_close);
                isSwitch = true;
                break;
            case CLOSE_NOPARTITION:
                toastMsg = getResources().getString(R.string.no_partition);
                isSwitch = true;
                break;
            case CLOSE_NOSWAPON:
                toastMsg = getResources().getString(R.string.no_swap);
                isSwitch = true;
                break;
            case CLOSE_NOSYSTEMFILE:
                // toastMsg = getResources().getString(R.string.no_systemfile);
                isSwitch = true;
                break;
            case OPNE_OPDONE:
            case AUTO_OPDONE: {
                if (swapTotalMemory != null) {
                    if (mHowSwap == SwapperCommands.SDCARDFILE_METHOD) {
                        mResultMs = Swapper.this.getResources().getString(R.string.swap_switch_on_filemode, swapTotalMemory);
                    }
                    if (mHowSwap == SwapperCommands.PARTITION_METHOD) {
                        mResultMs = Swapper.this.getResources().getString(R.string.swap_switch_on_partmode, swapTotalMemory);
                    }
                    if (mHowSwap != SwapperCommands.INVALID_METHOD) {
                        modeSummary = mHowSwap_array[mHowSwap - 1];
                    }
                    mOldHowSwap = mHowSwap;
                    Settings.System.putInt(this.getContentResolver(), Settings.System.SWAPPER_HOWSWAP, mHowSwap);

                    if (mHowSwap == SwapperCommands.SDCARDFILE_METHOD) {
                        Settings.System.putInt(this.getContentResolver(), Settings.System.IS_SDCARDSWAP_OPEN, 1);
                    }else{
                        Settings.System.putInt(this.getContentResolver(), Settings.System.IS_SDCARDSWAP_OPEN, 0);
                    }
                    mSwitchCheck.setSummaryOn(mResultMs);
                    toastMsg = getResources().getString(R.string.swap_open);
                    isSwitch = true;
                } else {
                    toastMsg = getResources().getString(R.string.swap_open_failed);
                    isSwitch = false;
                }
            }
            break;
            case CLOSE_OPDONE: {
                if (swapTotalMemory == null) {
                    mSwapSize = SwapperCommands.readSwapSize(this, mSettings, mHowSwap);

                    if (mHowSwap == SwapperCommands.SDCARDFILE_METHOD) {
                        Settings.System.putInt(this.getContentResolver(), Settings.System.IS_SDCARDSWAP_OPEN, 0);
                    }
                    mSwitchCheck.setSummaryOff(getResources().getString(R.string.swap_switch_off));

                    switch (mHowSwap) {
                        case SwapperCommands.SDCARDFILE_METHOD:
                            mHowSwap = SwapperCommands.SDCARDFILE_CLOSE_METHOD;
                            break;
                        case SwapperCommands.PARTITION_METHOD:
                            mHowSwap = SwapperCommands.PARTITION_CLOSE_METHOD;
                            break;
                        default:
                            break;
                    }
                    mOldHowSwap = mHowSwap;
                    if (SwapperCommands.DBUG) {
                        Log.d(TAG, "mOldHowSwap:" + mOldHowSwap);
                    }
                    toastMsg = Swapper.this.getResources().getString(R.string.swap_close);
                    isSwitch = false;
                } else {
                    toastMsg = Swapper.this.getResources().getString(R.string.swap_close_failed);
                    isSwitch = true;
                }
            }
            break;
            default:
                break;
        }

        storeParams(mSettings, "" + mHowSwap, "" + mSwapSize);
        mSwitchCheck.setEnabled(true);
        mSwitchCheck.setChecked(isSwitch);
        mAutoCheck.setEnabled(mSwitchCheck.isChecked());
        if (!isSwitch) {
            mAutoCheck.setDefaultValue(isSwitch);
            mAutoCheck.setChecked(isSwitch);
        }
        if (!toastMsg.equals("")) {
            Toast.makeText(Swapper.this, toastMsg, Toast.LENGTH_SHORT).show();
        }
        closeProgressDialog();
    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (state != null) {
            mHowSwap = state.getInt("howSwap");
            mSwapSize = state.getInt("swapSize");
        } else {
            mHowSwap = SwapperCommands.readHowSwap(this, mSettings);
            mSwapSize = SwapperCommands.readSwapSize(this, mSettings, mHowSwap);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("howSwap", mHowSwap);
        outState.putInt("swapSize", mSwapSize);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder;
        AlertDialog alertDialog;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.sdcard_dialog, (ViewGroup) findViewById(R.id.sdcard_parent));
        TextView text = (TextView) layout.findViewById(R.id.sdcard_content);

        switch (id) {
            case SDCARDDIALOG:
                text.setText(getString(R.string.sdcard_text));

                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.sdcard_title);
                builder.setView(layout);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openSwap();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSwitchCheck.setChecked(false);
                        dialog.cancel();
                    }
                });

                return alertDialog = builder.create();
            case HELPDIALOG:
                text.setText(getString(R.string.desc_text));

                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.help);
                builder.setView(layout);
                builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                return alertDialog = builder.create();
            default:
                break;
        }
        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SwapperCommands.DBUG) {
            Log.d(TAG, "onPause");
            Log.d(TAG, "howswap:" + mHowSwap);
            Log.d(TAG, "swapSize:" + mSwapSize);
        }
        mSc = new SwapperCommands(this, null);
        if (null != mSc && mSc.isRunning()) {
            sCurrentState = AUTOSTATE;
        }

        storeParams(mSettings, "" + mHowSwap, "" + mSwapSize);
        //mSc.setHandler(null);
        closeProgressDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void storeParams(SharedPreferences settings, String howSwap, String swapSize) {
        Editor editor = settings.edit();
        editor.putString("swapSize", swapSize + "");
        editor.putString("howswap", howSwap + "");
        editor.commit();
        Settings.System.putInt(this.getContentResolver(), Settings.System.SWAPPER_HOWSWAPSIZE, Integer.parseInt(swapSize));
        Settings.System.putInt(this.getContentResolver(), Settings.System.SWAPPER_HOWSWAP, Integer.parseInt(howSwap));
    }

    private void sendMessage(int what) {
        Message msg = mHandler.obtainMessage(what);
        mHandler.sendMessage(msg);
    }
    
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey().equals("switch")){
           if (mSwitchCheck.isChecked()) {
               showDialog(SDCARDDIALOG);
           } else {
               closeSwap();
           }
        }
        return true;
    }

    private void closeSwap() {
        sCurrentState = CLOSESTATE;
        if (SwapperCommands.DBUG) {
            Log.d(TAG, "close oldHowSwap:" + mOldHowSwap);
            Log.d(TAG, "close swapSize:" + mSwapSize);
        }
       
        if (mOldHowSwap == SwapperCommands.PARTITION_METHOD) {
            if (!SwapperCommands.getSwapFileState(mOldHowSwap)) {
                Swapper.this.sendMessage(SwapperCommands.MSG_NOPARTITION);
                return;
            }
        } else if (mOldHowSwap == SwapperCommands.SYSTEMFILE_METHOD) {
            if (!SwapperCommands.getSwapFileState(mOldHowSwap)) {
                Swapper.this.sendMessage(SwapperCommands.MSG_NOSYSTEMFILE);
                return;
            }
        } else if (mOldHowSwap == SwapperCommands.SDCARDFILE_METHOD) {
            if (!SwapperCommands.sdcardIsExists()) {
                Swapper.this.sendMessage(SwapperCommands.MSG_NOSDCARD);
                return;
            }
        }
        mResultMs = getDialogMessage(mOldHowSwap,DOCLOSE);
        mProgressDialog = ProgressDialog.show(Swapper.this, "", mResultMs, true);
        mSwitchCheck.setEnabled(false);
        try {
            mSc.swapOff(mOldHowSwap, false);
        } catch (Exception e) {
            e.printStackTrace();
            if (null != mProgressDialog) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
    }

    private void openSwap() {
        sCurrentState = OPENSTATE;
        mHowSwap = SwapperCommands.readHowSwap(this, mSettings); 
        switch (mHowSwap) {
            case SwapperCommands.SDCARDFILE_CLOSE_METHOD:
                mHowSwap = SwapperCommands.SDCARDFILE_METHOD;
                break;
            case SwapperCommands.PARTITION_CLOSE_METHOD:
                mHowSwap = SwapperCommands.PARTITION_METHOD;
                break;
            default:
                break;
        }
        mSwapSize = SwapperCommands.readSwapSize(this, mSettings, SwapperCommands.SDCARDFILE_METHOD);

        if (SwapperCommands.DBUG) {
            Log.d(TAG, "open oldHowSwap:" + mOldHowSwap);
            Log.d(TAG, "open howswap:" + mHowSwap);
            Log.d(TAG, "open swapSize:" + mSwapSize);
        }

        if (SwapperCommands.readSystemAvailSize() < 32 && mHowSwap == SwapperCommands.SYSTEMFILE_METHOD) {
            if (new File(SwapperCommands.SYSTEM_SWAPFILE).exists()) {
                Toast.makeText(Swapper.this, Swapper.this.getResources().getString(R.string.system_open), Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toast.makeText(Swapper.this, Swapper.this.getResources().getString(R.string.system_size_limit), Toast.LENGTH_SHORT).show();
                return;
            }

        }

        if (mHowSwap == SwapperCommands.PARTITION_METHOD) {
            if (!SwapperCommands.getSwapFileState(mHowSwap)) {
                Swapper.this.sendMessage(SwapperCommands.MSG_NOPARTITION);
                return;
            }
        } else if (mHowSwap == SwapperCommands.SYSTEMFILE_METHOD) {
            // TODO:
        } else if (mHowSwap == SwapperCommands.SDCARDFILE_METHOD) {
            if (!SwapperCommands.sdcardIsExists()) {
                Swapper.this.sendMessage(SwapperCommands.MSG_NOSDCARD);
                return;
            }
            if (SwapperCommands.readSdCardAvailSize() < mSwapSize) {
                Swapper.this.sendMessage(SwapperCommands.MSG_NOSDCARD);
                return;
            }
        }
        mResultMs = getDialogMessage(mHowSwap,DOOPEN);
        mProgressDialog = ProgressDialog.show(Swapper.this, "", mResultMs, true);
        mSwitchCheck.setEnabled(false);
        try {
            if (true) {
                if (mOldHowSwap != mHowSwap && mOldHowSwap != SwapperCommands.INVALID_METHOD) {
                    if (SwapperCommands.DBUG) {
                        Log.d(TAG, "close before open---" + "oldHowSwap:" + mOldHowSwap);
                    }
                    mSc.swapOff(mOldHowSwap, false);
                }
                mSc.swapOn(false, mHowSwap, mSwapSize);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (null != mProgressDialog) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
    }
    public String getDialogMessage(int howSwap,int doIndex){
        String resultMs = null;
        switch (howSwap) {
                case SwapperCommands.SDCARDFILE_METHOD:
                    resultMs = Swapper.this.getResources().getStringArray(R.array.onoff_filemode)[doIndex];
                    break;
                case SwapperCommands.PARTITION_METHOD:
                    resultMs = Swapper.this.getResources().getStringArray(R.array.onoff_partmode)[doIndex];
                    break;
                case SwapperCommands.SYSTEMFILE_METHOD:
                    resultMs = Swapper.this.getResources().getStringArray(R.array.onoff_sysmode)[doIndex];
                    break;
                default:
                    break;
        }
        return resultMs;
    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals("howswap")) {
            String modeText = Swapper.this.getResources().getStringArray(R.array.howswap)[Integer.valueOf(newValue.toString()) - 1];
            mModeList.setSummary(modeText);
            if (newValue.toString().equals("" + SwapperCommands.SYSTEMFILE_METHOD) || newValue.toString().equals("" + SwapperCommands.PARTITION_METHOD)) {
                mSwapSizeList.setEnabled(false);
            } else {
                mSwapSizeList.setEnabled(true);
            }
            mHowSwap = Integer.parseInt(newValue.toString());
        } else if (preference.getKey().equals("swapSize")) {
            mSwapSizeList.setSummary(newValue.toString() + "MB");
            mSwapSize = Integer.parseInt(newValue.toString());
        }
        return true;
    }

    private String getTotalMemory() {
        String swapInfoFile = "/proc/swaps";// swap info file
        String swapReadLine = null;
        String totalSwapMemory = null;
        String[] arrayOfString = null;
        long initial_memory = 0;
        FileReader localFileReader = null;
        BufferedReader localBufferedReader = null;
        try {
           localFileReader = new FileReader(swapInfoFile);
           localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            int index = 0;
            String line = null;
            while((line=(localBufferedReader.readLine())) != null){
               index++;
               if(index == 2){
                  swapReadLine = line;
                  break;
               }
            }
            if(swapReadLine != null){
                arrayOfString = swapReadLine.split("\\s+");
                if(SwapperCommands.DBUG){
                    for (String num : arrayOfString) {
                        Log.d(swapReadLine, num + "\t");
                    }
                }
                initial_memory = Integer.valueOf(arrayOfString[2]).intValue() * 1024;// get swap size
                if(SwapperCommands.DBUG){
                    Log.d(TAG,  "initial_memory:"+initial_memory);
                }
                totalSwapMemory =  Formatter.formatFileSize(getBaseContext(), initial_memory);
            }
        } catch (Exception e1) {
              e1.printStackTrace();
        }finally{
            try {
                if(localBufferedReader != null){
                    localBufferedReader.close();
                }
                if(localFileReader != null){
                    localFileReader.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return totalSwapMemory;// Byte change to KB or MB 
    }

}
