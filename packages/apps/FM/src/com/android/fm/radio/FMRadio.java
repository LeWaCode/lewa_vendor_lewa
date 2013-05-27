package com.android.fm.radio;

import java.lang.ref.WeakReference;
import android.os.SystemProperties;
import java.util.*;
import java.io.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.content.res.Resources.Theme;

import android.gesture.GestureOverlayView;
import android.view.GestureDetector.OnGestureListener;
import android.hardware.fmradio.FmConfig;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fm.R;
import com.android.fm.utils.FrequencyPicker;
import com.android.fm.utils.FrequencyPickerDialog;

public class FMRadio extends Activity implements OnMenuItemClickListener,
OnTouchListener, OnGestureListener, OnItemClickListener {
    public static final String LOGTAG = "FMRadio";

    public static final boolean LOG_STATUS = false;

    public static final String FM_LOCKSCREEN = "com.android.fm.metachanged";

    /* menu Identifiers */

    /* Dialog Identifiers */

    private static final int DIALOG_SELECT_PRESET_LIST = 3;

    private static final int DIALOG_PRESETS_LIST = 4;

    private static final int DIALOG_PRESET_LIST_RENAME = 5;

    private static final int DIALOG_PRESET_LIST_DELETE = 6;

    private static final int DIALOG_PICK_FREQUENCY = 8;

    private static final int DIALOG_PRESET_OPTIONS = 10;

    private static final int DIALOG_PRESET_RENAME = 11;

    private static final int DIALOG_CMD_FAILED = 13;

    /* Activity Return ResultIdentifiers */
    private static final int ACTIVITY_RESULT_SETTINGS = 1;

    /* Activity Return ResultIdentifiers */
    private static final int MAX_PRESETS_PER_PAGE = 11;

    /* Station's Audio is Stereo */
    private static final int FMRADIO_UI_STATION_AUDIO_STEREO = 1;

    /* Station's Audio is Mono */
    private static final int FMRADIO_UI_STATION_AUDIO_MONO = 2;

    /*
     * The duration during which the "Sleep: xx:xx" string will be toggling
     */
    private static final int SLEEP_TOGGLE_SECONDS = 60;

    /*
     * The number of Preset Stations to create. The hardware supports a maximum
     * of 12.
     */
    private static final int NUM_AUTO_PRESETS_SEARCH = 12;

    /*
     * Command time out: For asynchonous operations, if no response is received
     * with int this duration, a timeout msg will be displayed.
     */
    private static final int CMD_TIMEOUT_DELAY_MS = 5000;

    private static final int MSG_CMD_TIMEOUT = 101;

    private static final int CMD_NONE = 0;

    private static final int CMD_TUNE = 1;

    private static final int CMD_FMON = 2;

    private static final int CMD_FMOFF = 3;

    private static final int CMD_FMCONFIGURE = 4;

    private static final int CMD_MUTE = 5;

    private static final int CMD_SEEK = 6;

    private static final int CMD_SCAN = 7;

    private static final int CMD_SEEKPI = 8;

    private static final int CMD_SEARCHLIST = 9;

    private static final int CMD_CANCELSEARCH = 10;

    private static final int CMD_SET_POWER_MODE = 11;

    private static final int CMD_SET_AUDIO_MODE = 12;

    private static final int CMD_SET_AUTOAF = 13;

    private static final int CMD_GET_INTERNALANTENNA_MODE = 14;

    // private static final int PRESETS_OPTIONS_RENAME = 0;
    private static final int PRESETS_OPTIONS_PLAY = 0;

    private static final int PRESETS_OPTIONS_DELETE = 1;

    private static final int PRESETS_OPTIONS_SEARCHPI = 2;

    private static final int DIRECTION_LEFT = 0;
    private static final int DIRECTION_RIGHT = 1;

    /** Sleep Handling: After the timer expires, the app needs to shut down */
    private static final int SLEEPTIMER_EXPIRED = 0x1001;

    private static final int SLEEPTIMER_UPDATE = 0x1002;

    private static final int HANDLE_FMGALLERY = 1;

    private static final int HANDLE_AUTOSEARCHCHANNEL = 2;

    private static final int HANDLE_PROGRESSDILOAGDISMISS = 3;

    private static final int HANDLE_PROGRESSDILOAG = 4;

    private static final int HANDLE_PROGRESSDILOAGTITLE = 5;

    private static final int CHANNELNUM = 204;
    // add by even 2012-03-07
    private static final String HEADSET_STATE_PATH = "/sys/class/switch/h2w/state";

    protected static final boolean RECORDING_ENABLE = false;

    /* Current Status Indicators */
    private static boolean mRecording = false;

    private static boolean mIsScaning = false;

    private static boolean mIsSeeking = false;

    private static boolean mIsSearching = false;

    private static int mScanPty = 0;

    /* Asynchronous command active */
    private static int mCommandActive = 0;

    /* Command that failed (Sycnhronous or Asynchronous) */
    private static int mCommandFailed = 0;

    private static IFMRadioService mService = null;

    private static FmSharedPreferences mPrefs;

    private static PresetStation mTunedStation = new PresetStation("", 106500);

    private PresetStation mPresetButtonStation = null;

    private EarPhoneReceiver mEarPhoneReceiver;

    private Context context;

    DialogUtil.DialogAbstract da = new DialogUtil.DialogAbstract();

    List<Integer> channelNum = new ArrayList<Integer>();

    /* Middle row in the station info layout */

    private View checkView;
    /* channel show in the layout */
    private TextView mFmChannelShowText;
    private TextView mFmAutoSearchText;

    private CheckBox exitDialogCheck;

    private HorizontalScrollView mHsv;
    /* Indicator of frequency */
    private LeWaChannelNumGallery mFmChannelGallery;
    private ChannelNumAdapter channelNumAdapter;

    /* Button Resources */
    private ImageButton mOnOffButton;

    /* 11 Preset Buttons */
    private Button[] mPresetButtons = { null, null, null, null, null, null,
            null, null, null, null, null };

    private int mEarPhoneState = 0;
    private int channelFreTag = 0;
    private int autoSearchChannelFinnal = 0;

    private int mStereo = -1;
    private int mButtonCollectId = 0;
    private int mButtonSelectedId = 0;

    private boolean mSleepCancelled = false;
    private boolean mBluetoothEnabled = false;
    // private boolean mInitialBtState = false;

    private double mOutputFreq;

    private Menu mMenu;

    private MenuItem mItem;

    /*
     * Phone time when the App has to be shut down, calculated based on what the
     * user configured
     */
    private long mSleepAtPhoneTime = 0;

    private Thread autoSearchThread = null;
    private Thread mSleepUpdateHandlerThread = null;

    private ProgressBar autoSearchBar;

    private ProgressDialog mBluetoothStartingDialog;

    private ProgressDialog mSearchDialog;// add by chenhengheng

    private GestureDetector mGestureDetector = new GestureDetector(this);

    private List<Integer> searchStartToFinishList = new ArrayList<Integer>();

    private List<String> searchStartToFinishStringList = new ArrayList<String>();

    private AlertDialog mAlertDlg = null;

    private int earState;

    private boolean mBooRequireBluetooth = false;
    // bluetooth open or close if fm closed
    private boolean mBooBluetoothOpen = false;

    private boolean mCheckFlag = false;

    public static final int FLING_MIN_DISTANCE = 50;
    public static final int FLING_MIN_VELOCITY = 100;
    public static final int FLING_MINUS_VALUE = 100;

    /* Radio Vars */
    final Handler mHandler = new Handler();

    private Handler mGalleyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case HANDLE_FMGALLERY:
                int freq = (Integer) msg.arg1;
                if (channelNum.contains(freq)) {
                    int index = channelNum.indexOf(freq);
                    mFmChannelGallery.setSelection(index);
                }
                break;
            case HANDLE_AUTOSEARCHCHANNEL:
                searchFrequencyFromGallery(msg.arg1);
                break;
            case HANDLE_PROGRESSDILOAGDISMISS:
                if (mSearchDialog != null) {
                    mSearchDialog.dismiss();
                    mSearchDialog = null;
                }
                break;
            case HANDLE_PROGRESSDILOAGTITLE:
                if (mSearchDialog != null) {
                    mSearchDialog.setMessage(msg.obj.toString());
                }
                break;
            default:
                break;
            }
        }
    };

    private Handler mColorResetHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1)
                mPresetButtons[mButtonCollectId].setTextColor(0xff2c85c6);
            else
                mPresetButtons[mButtonCollectId].setTextColor(0xff999999);
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOG_STATUS) {
            Log.v(LOGTAG, "Activity onCreate");
        }
        context = getApplicationContext();

        // add by even 2012-03-15
        mBooRequireBluetooth = context.getResources().getBoolean(
                R.bool.require_bt);
        earState = getEarState();
        mEarPhoneReceiver = new EarPhoneReceiver();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.lewafmradio);
        initUI();
        enableRadioOnOffUI();

        // add by maowenjiang 2012-03-07
        if (earState == 0) {
            if (null != autoSearchThread && autoSearchThread.isAlive()) {
                autoSearchThread.interrupt();
                autoSearchThread = null;
            }

            if (mSearchDialog != null) {
                mSearchDialog.dismiss();
                mSearchDialog = null;
            }
            popDialog(true);

            if (isFmOn() && mService != null) {
                disableRadio();
                enableRadioOnOffUI(false);
            }
        }

        mPrefs = new FmSharedPreferences(this);
        mPrefs.Load();
        mCommandActive = CMD_NONE;
        mCommandFailed = CMD_NONE;

        // load autosearch,by george,
        stringToAutoSearchList(mPrefs.getAutoSearchStations());

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mEarPhoneReceiver, iFilter);
        try {
            if (mService != null) {
                mService.registerCallbacks(mServiceCallbacks);
            } else {
                if (LOG_STATUS) {
                    Log.v(LOGTAG, "FMRadio: onStart -> mService is null");
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // added by even 2012-04-09
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (false == bindToService(FMRadio.this, osc)) {
                    Log.e(LOGTAG, "onCreate: Failed to Start Service");
                } else {
                    if (LOG_STATUS) Log.d(LOGTAG, "onCreate: Start Service completed successfully");
                }
            }

        }, 1000);

    }

    @Override
    public void onStart() {
        super.onStart();
        if(LOG_STATUS) Log.d(LOGTAG, "onStart");
        if (mBooRequireBluetooth) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mIntentReceiver, filter);
        }

    }

    @Override
    public void onResume() {
        if(LOG_STATUS) Log.d(LOGTAG, "onResume");
        super.onResume();
        // Re-load FM preferences
        mPrefs.Load();
        // Grab the station from the tuned frequency
        PresetStation station = FmSharedPreferences
                .getStationFromFrequency(FmSharedPreferences
                        .getTunedFrequency());
        // If we were able to retrieve the station then set that as our tuned
        // station
        if (station != null) {
            mTunedStation.Copy(station);
        }

        int initFreq = mTunedStation.getFrequency();
        mFmChannelShowText.setText(FrequencyPicker
                .formatFrequencyString(initFreq));
        Message msg = Message.obtain();
        msg.what = HANDLE_FMGALLERY;
        // maowenjiang modify 2012-03-06
        // msg.arg1 = initFreq;
        msg.arg1 = (Integer) channelNum.get(mFmChannelGallery
                .getSelectedItemPosition() % channelNum.size());
        mGalleyHandler.sendMessage(msg);

        setupPresetLayout();
        updateStationInfoToUI();

        if(LOG_STATUS) Log.d(LOGTAG, "onResume end");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(LOG_STATUS) Log.d(LOGTAG, "onPause");
        FmSharedPreferences.setTunedFrequency(mTunedStation.getFrequency());
        mPrefs.Save();
    }

    @Override
    public void onRestart() {
        if(LOG_STATUS) Log.d(LOGTAG, "onRestart");
        super.onRestart();
        if (mBooRequireBluetooth && earState > 0) {
            asyncCheckAndEnableRadio();
        } else if (earState > 0) {
            if(LOG_STATUS) Log.d(LOGTAG, "Service enableRadio");
            enableRadio();
        }
    }

    @Override
    public void onStop() {
        if(LOG_STATUS) Log.d(LOGTAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if(LOG_STATUS) Log.d(LOGTAG, "onDestroy");
        endSleepTimer();
        unbindFromService(this);
        mService = null;
        if (mIntentReceiver != null && mBooRequireBluetooth) {
            unregisterReceiver(mIntentReceiver);
            mIntentReceiver = null;
        }
        // Log.d(LOGTAG, "onDestroy: unbindFromService completed");
        unregisterReceiver(mEarPhoneReceiver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(LOG_STATUS) Log.d(LOGTAG, "onActivityResult : requestCode -> " + requestCode);
        if(LOG_STATUS) Log.d(LOGTAG, "onActivityResult : resultCode -> " + resultCode);
    }

    private void autoSearchChannel() {
        int templast = 0;
        int tempvalue = 0;
        Message msg = Message.obtain();

        try {
            int autoSearchListSize = searchStartToFinishList.size();
            int tuneFreq = 87500;
            int finalTuneFreq = 106500;
            while (mService.isFmOn()) {
                SeekNextStation();
                if (searchStartToFinishList.contains(autoSearchChannelFinnal)) {
                    if (autoSearchListSize > 1
                            && autoSearchChannelFinnal < searchStartToFinishList
                            .get(autoSearchListSize - 1)) {
                        if (!searchStartToFinishList.contains(finalTuneFreq)) {
                            searchStartToFinishList.add(finalTuneFreq);
                            autoSearchListSize ++;
                        }
                        tuneFreq = finalTuneFreq;
                        sendGalleyMsg(msg, HANDLE_AUTOSEARCHCHANNEL, tuneFreq);
                        break;
                    } 
                    continue;
                } else if (channelNum.contains(autoSearchChannelFinnal)) {
                    if (autoSearchListSize > 1
                            && autoSearchChannelFinnal < searchStartToFinishList
                            .get(autoSearchListSize - 1)) {
                        if (!searchStartToFinishList.contains(finalTuneFreq)) {
                            searchStartToFinishList.add(finalTuneFreq);
                            autoSearchListSize ++;
                        }
                        tuneFreq = finalTuneFreq;
                        sendGalleyMsg(msg, HANDLE_AUTOSEARCHCHANNEL, tuneFreq);
                        break;
                    } else {
                        searchStartToFinishList.add(autoSearchChannelFinnal);
                        autoSearchListSize ++;
                        if (LOG_STATUS) Log.d(LOGTAG, "autoSearchChannel while: " + searchStartToFinishList.size());

                        msg = Message.obtain();
                        msg.what = HANDLE_PROGRESSDILOAGTITLE;
                        msg.obj = getResources().getString(
                                R.string.dialog_autosearch_message_result,
                                "" + autoSearchChannelFinnal / 1000f);
                        mGalleyHandler.sendMessage(msg);
                        tuneFreq = autoSearchChannelFinnal;
                        sendGalleyMsg(msg, HANDLE_AUTOSEARCHCHANNEL, tuneFreq);
                        Thread.sleep(100);
                        continue;
                    }
                    // add by maowenjiang 2012-03-06
                } else {
                    if (autoSearchChannelFinnal < 87.5
                            || autoSearchChannelFinnal > 107.7) {
                        if (!searchStartToFinishList.contains(finalTuneFreq)) {
                            searchStartToFinishList.add(finalTuneFreq);
                            autoSearchListSize ++;
                        }
                        tuneFreq = finalTuneFreq;
                        sendGalleyMsg(msg, HANDLE_AUTOSEARCHCHANNEL, tuneFreq);
                        break;
                    }
                    continue;
                }
                // add by maowenjiang 2012-03-06
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendGalleyMsg(msg, HANDLE_PROGRESSDILOAGDISMISS, null);

        Collections.sort(searchStartToFinishList);

        for (int i = 0; i < searchStartToFinishList.size(); ++i) {
            searchStartToFinishStringList.add(""
                    + searchStartToFinishList.get(i) / 1000f + " MHZ");
        }
        // autosearch,by george
        mPrefs.setAutoSearchStations(autoSearchListToString(searchStartToFinishList));
    }

    private void sendGalleyMsg(Message msg, int what, Integer arg1) {
        msg = Message.obtain();
        msg.what = what;
        if(arg1 != null) msg.arg1 = arg1;
        mGalleyHandler.sendMessage(msg);
    }

    public int getBtnIndex(Button btns[], View view) {
        for (int i = 0; i < btns.length; i++) {
            if (view.getId() == btns[i].getId())
                return i;
        }
        return -1;
    }

    private void initUI() {
        autoSearchBar = (ProgressBar) findViewById(R.id.auto_search_bar);
        autoSearchBar.setIndeterminate(false);
        autoSearchBar.setVisibility(View.INVISIBLE);
        mOnOffButton = (ImageButton) findViewById(R.id.btn_onoff);
        mOnOffButton.setOnClickListener(mTurnOnOffClickListener);

        mFmChannelShowText = (TextView) findViewById(R.id.fm_channel_text);

        mFmAutoSearchText = (TextView) findViewById(R.id.auto_search_bar_text);
        mHsv = (HorizontalScrollView) findViewById(R.id.fm_channel_collect_hsv);
        mFmChannelGallery = (LeWaChannelNumGallery) findViewById(R.id.fm_channle_num_gallery);
        channelNumAdapter = new ChannelNumAdapter(this);
        mFmChannelGallery.setOnItemClickListener(this);
        mFmChannelGallery.setAdapter(channelNumAdapter);
        mFmChannelGallery.setOnTouchListener(this);
        mFmChannelGallery.setFocusable(true);
        mFmChannelGallery.setClickable(true);
        mFmChannelGallery.setLongClickable(true);
        mGestureDetector.setIsLongpressEnabled(true);

        /* 11 Preset Buttons */
        mPresetButtons[0] = (Button) findViewById(R.id.presets_button_1);
        mPresetButtons[1] = (Button) findViewById(R.id.presets_button_2);
        mPresetButtons[2] = (Button) findViewById(R.id.presets_button_3);
        mPresetButtons[3] = (Button) findViewById(R.id.presets_button_4);
        mPresetButtons[4] = (Button) findViewById(R.id.presets_button_5);
        mPresetButtons[5] = (Button) findViewById(R.id.presets_button_6);
        mPresetButtons[6] = (Button) findViewById(R.id.presets_button_7);
        mPresetButtons[7] = (Button) findViewById(R.id.presets_button_8);
        mPresetButtons[8] = (Button) findViewById(R.id.presets_button_9);
        mPresetButtons[9] = (Button) findViewById(R.id.presets_button_10);
        mPresetButtons[10] = (Button) findViewById(R.id.presets_button_11);

        for (int nButton = 0; nButton < MAX_PRESETS_PER_PAGE; nButton++) {
            mPresetButtons[nButton]
                    .setOnClickListener(mPresetButtonClickListener);
            mPresetButtons[nButton]
                    .setOnLongClickListener(mPresetButtonOnLongClickListener);
        }
    }

    private void setSpeakerFunc(boolean on) {
        if (on) {
            switchToSpeaker();
        } else {
            switchToHeadset();
        }
    }

    private void switchToSpeaker() {
        AudioSystem.setForceUse(AudioSystem.FOR_MEDIA,
                AudioSystem.FORCE_SPEAKER);
        AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_OUT_FM,
                AudioSystem.DEVICE_STATE_UNAVAILABLE, "");
        AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_OUT_FM,
                AudioSystem.DEVICE_STATE_AVAILABLE, "");
    }

    private void switchToHeadset() {
        AudioSystem.setForceUse(AudioSystem.FOR_MEDIA, AudioSystem.FORCE_NONE);
        AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_OUT_FM,
                AudioSystem.DEVICE_STATE_UNAVAILABLE, "");
        AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_OUT_FM,
                AudioSystem.DEVICE_STATE_AVAILABLE, "");
    }

    private void fmChannelBtnLight(float i) {
        for (int id = 0; id < MAX_PRESETS_PER_PAGE; id++) {
            if (mPresetButtons[id].getText().equals(String.valueOf(i))) {
                mPresetButtons[id].setTextColor(0xff4f94cd);
            } else {
                mPresetButtons[id].setTextColor(0xff999999);
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_PICK_FREQUENCY: {
            FmConfig fmConfig = FmSharedPreferences.getFMConfiguration();
            return new FrequencyPickerDialog(this, fmConfig,
                    mTunedStation.getFrequency(), mFrequencyChangeListener);
        }
        case DIALOG_PRESET_OPTIONS: {
            return createPresetOptionsDlg(id);
        }
        case DIALOG_PRESET_RENAME: {
            return createPresetRenameDlg(id);
        }
        case DIALOG_CMD_FAILED: {
            return createCmdFailedDlg(id);
        }
        default:
            break;
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        int curListIndex = FmSharedPreferences.getCurrentListIndex();
        PresetList curList = FmSharedPreferences.getStationList(curListIndex);
        switch (id) {
        case DIALOG_PRESET_LIST_RENAME: {
            EditText et = (EditText) dialog.findViewById(R.id.list_edit);
            if (et != null) {
                et.setText(curList.getName());
            }
            break;
        }
        case DIALOG_SELECT_PRESET_LIST: {
            AlertDialog alertDlg = ((AlertDialog) dialog);
            ListView lv = (ListView) alertDlg.findViewById(R.id.list);
            if (lv != null) {
                updateSelectPresetListDlg(lv);
            }
            break;
        }
        case DIALOG_PRESETS_LIST: {
            AlertDialog alertDlg = ((AlertDialog) dialog);
            alertDlg.setTitle(curList.getName());
            break;
        }
        case DIALOG_PICK_FREQUENCY: {
            ((FrequencyPickerDialog) dialog).UpdateFrequency(mTunedStation
                    .getFrequency());
            break;
        }
        case DIALOG_PRESET_RENAME: {
            EditText et = (EditText) dialog.findViewById(R.id.list_edit);
            if ((et != null) && (mPresetButtonStation != null)) {
                et.setText(mPresetButtonStation.getName());
            }
            break;
        }
        case DIALOG_PRESET_OPTIONS: {
            AlertDialog alertDlg = ((AlertDialog) dialog);
            if ((alertDlg != null) && (mPresetButtonStation != null)) {
                alertDlg.setTitle(mPresetButtonStation.getName());
            }
            break;
        }

        default:
            break;
        }
    }

    /**
     * @return true if a wired headset is connected.
     */
    boolean isWiredHeadsetAvailable() {
        boolean bAvailable = false;
        if (mService != null) {
            try {
                bAvailable = mService.isWiredHeadsetAvailable();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return bAvailable;
    }

    /**
     * @return true if a internal antenna is available.
     */
    boolean isAntennaAvailable() {
        boolean bAvailable = false;
        if (mService != null) {
            try {
                bAvailable = mService.isAntennaAvailable();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return bAvailable;
    }

    private Dialog createPresetOptionsDlg(int id) {
        if (mPresetButtonStation != null) {
            AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
            dlgBuilder.setTitle(mPresetButtonStation.getName());
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(getResources().getString(R.string.preset_play));
            arrayList.add(getResources().getString(R.string.preset_delete));
            String piString = mPresetButtonStation.getPIString();
            if (!TextUtils.isEmpty(piString)) {
                arrayList.add(getResources().getString(R.string.preset_search,
                        piString));
            }

            dlgBuilder.setCancelable(true);
            dlgBuilder
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    mPresetButtonStation = null;
                    removeDialog(DIALOG_PRESET_OPTIONS);
                }
            });
            String[] items = new String[arrayList.size()];
            arrayList.toArray(items);
            dlgBuilder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (mPresetButtonStation != null) {
                        switch (item) {
                        /*
                         * case PRESETS_OPTIONS_RENAME: { // Rename
                         * showDialog(DIALOG_PRESET_RENAME); break; }
                         */
                        case PRESETS_OPTIONS_PLAY: {
                            // Play
                            SharedPreferences channelIdSp = getSharedPreferences(
                                    "presetbuttonchannleid",
                                    Context.MODE_WORLD_READABLE);
                            int presetButtonId = channelIdSp.getInt(
                                    "presetbuttonchannleid", -1);

                            initSearchFrequencyResult((Integer) channelNum
                                    .get(presetButtonId));

                            mFmChannelGallery.setSelection(presetButtonId);
                            break;
                        }
                        case PRESETS_OPTIONS_DELETE: {
                            // Delete
                            int curListIndex = FmSharedPreferences
                                    .getCurrentListIndex();
                            if (LOG_STATUS) Log.v(LOGTAG, "delete curListIndex:" + curListIndex);
                            // obsoleted by george,2011-12-29
                            if (false) {
                                FmSharedPreferences.removeStation(curListIndex,
                                        mPresetButtonStation);
                            } else {
                                int index = FmSharedPreferences
                                        .getIndexFromStation(mPresetButtonStation);
                                // add by even 2012-03-07
                                if (index == -1) {
                                    index = 0;
                                }
                                FmSharedPreferences.setStation(curListIndex,
                                        index, "", 0);

                            }
                            mPresetButtonStation = null;
                            setupPresetLayout();
                            mPrefs.Save();
                            break;
                        }
                        case PRESETS_OPTIONS_SEARCHPI: {
                            // SearchPI
                            String piString = mPresetButtonStation
                                    .getPIString();
                            int pi = mPresetButtonStation.getPI();
                            if ((!TextUtils.isEmpty(piString)) && (pi > 0)) {
                                initiatePISearch(pi);
                            }
                            mPresetButtonStation = null;
                            break;
                        }
                        default: {
                            // Should not happen
                            mPresetButtonStation = null;
                            break;
                        }
                        }// switch item
                    }// if(mPresetButtonStation != null)
                    removeDialog(DIALOG_PRESET_OPTIONS);
                }// onClick
            });
            return dlgBuilder.create();
        }
        return null;
    }

    private void updateSelectPresetListDlg(ListView lv) {
    }

    private Dialog createPresetRenameDlg(int id) {
        if (mPresetButtonStation == null) {
            return null;
        }
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(
                R.layout.alert_dialog_text_entry, null);
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setTitle(R.string.dialog_presetlist_rename_title);
        dlgBuilder.setView(textEntryView);
        dlgBuilder.setPositiveButton(R.string.alert_dialog_ok,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText mTV = (EditText) textEntryView
                        .findViewById(R.id.list_edit);
                CharSequence newName = mTV.getEditableText();
                String nName = String.valueOf(newName);
                mPresetButtonStation.setName(nName);
                mPresetButtonStation = null;
                setupPresetLayout();
                mPrefs.Save();
                removeDialog(DIALOG_PRESET_RENAME);
            }
        });
        dlgBuilder.setNegativeButton(R.string.alert_dialog_cancel,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                removeDialog(DIALOG_PRESET_RENAME);
            }
        });
        return (dlgBuilder.create());
    }

    private Dialog createCmdFailedDlg(int id) {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setIcon(R.drawable.alert_dialog_icon).setTitle(
                R.string.fm_command_failed_title);
        dlgBuilder.setMessage(R.string.fm_cmd_failed_msg);

        dlgBuilder.setPositiveButton(R.string.alert_dialog_ok,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                removeDialog(DIALOG_CMD_FAILED);
                mCommandFailed = CMD_NONE;
            }
        });

        return (dlgBuilder.create());
    }

    private void RestoreDefaults() {
        FmSharedPreferences.SetDefaults();
        mPrefs.Save();
    }

    private View.OnClickListener mPresetListClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            showDialog(DIALOG_SELECT_PRESET_LIST);
        }
    };

    private View.OnLongClickListener mPresetListButtonOnLongClickListener = new View.OnLongClickListener() {
        public boolean onLongClick(View view) {
            showDialog(DIALOG_PRESETS_LIST);
            return true;
        }
    };

    private View.OnClickListener mPresetButtonClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            PresetStation station = (PresetStation) view.getTag();
            mButtonSelectedId = view.getId();
            // moded by george,2011-12-29
            if (station != null && (station.getFrequency() != 0)) {
                searchFrequencyFromGallery(station.getFrequency());
            } else {
                int index = FMRadio.this.getBtnIndex(mPresetButtons, view);
                addToPresets(index);
            }
        }
    };

    private View.OnLongClickListener mPresetButtonOnLongClickListener = new View.OnLongClickListener() {
        public boolean onLongClick(View view) {
            PresetStation station = (PresetStation) view.getTag();
            mPresetButtonStation = station;
            // moded by george,2011-12-29
            if (station != null && (station.getFrequency() != 0)) {
                for (int i = 0; i < channelNum.size(); i++) {
                    if ((Integer) channelNum.get(i) == station.getFrequency()) {
                        SharedPreferences presetButtonChannelSp = getBaseContext()
                                .getSharedPreferences("presetbuttonchannleid",
                                        Context.MODE_WORLD_WRITEABLE);
                        SharedPreferences.Editor editor = presetButtonChannelSp
                                .edit();
                        editor.putInt("presetbuttonchannleid", i);
                        editor.commit();
                        break;
                    }
                }
                showDialog(DIALOG_PRESET_OPTIONS);
            }
            return true;
        }
    };

    final FrequencyPickerDialog.OnFrequencySetListener mFrequencyChangeListener = new FrequencyPickerDialog.OnFrequencySetListener() {
        public void onFrequencySet(FrequencyPicker view, int frequency) {
            if (LOG_STATUS) Log.d(LOGTAG, "mFrequencyChangeListener: onFrequencyChanged to : " + frequency);
            searchFrequencyFromGallery(frequency);
        }
    };

    private View.OnClickListener mTurnOnOffClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            SharedPreferences turnOnOffActionSp = getSharedPreferences(
                    "exitdialogcheck", Context.MODE_WORLD_READABLE);
            mCheckFlag = turnOnOffActionSp.getBoolean("exitdialogcheck", false);
            if (mCheckFlag == false) {
                addExitDialogCheck();
                // Toggle BT on/off depending on value in preferences
                if (mBooRequireBluetooth) {
                    toggleRadioOffBluetoothBehaviour();
                } else {
                    popFMExitDialog();
                }
            } else {
                if (mBooRequireBluetooth) {
                    toggleRadioOffBluetoothBehaviour();
                } else {
                    if (isFmOn()) {
                        disableRadio();
                    }
                    finish();
                }
            }
            setTurnOnOffButtonImage();
        }
    };

    // added by even 2012-03-17
    private void popFMExitDialog() {
        AlertDialog.Builder exitBuilder = new AlertDialog.Builder(FMRadio.this);
        exitBuilder
        .setTitle(R.string.exit_dialog_title)
        .setMessage(R.string.exit_dialog_message)
        .setView(checkView)
        .setPositiveButton(R.string.exit_dialog_positive_btn,
                new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                ischecked();
                if (isFmOn()) {
                    disableRadio();
                }

                finish();
                return;
            }
        })
        .setNegativeButton(R.string.exit_dialog_negative_btn,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        }).create().show();
    }

    private View.OnClickListener mSeekUpClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            SeekNextStation();
        }
    };

    private View.OnClickListener mSeekDownClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            SeekPreviousStation();
        }
    };

    private View.OnClickListener mSpeakerSwitchClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            boolean speakerState = !FmSharedPreferences.getSpeaker();
            setSpeakerFunc(speakerState);
            FmSharedPreferences.setSpeaker(speakerState);
        }
    };

    private void addExitDialogCheck() {
        LayoutInflater inflater = LayoutInflater.from(FMRadio.this);
        checkView = inflater.inflate(R.layout.exitdialogcheckview, null);
        exitDialogCheck = (CheckBox) checkView
                .findViewById(R.id.fm_exit_dialog_check);
        exitDialogCheck.setText(R.string.exit_dialog_choise);
    }

    void ischecked() {
        SharedPreferences fmExitDialogCheckSp = getBaseContext()
                .getSharedPreferences("exitdialogcheck",
                        Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor editor = fmExitDialogCheckSp.edit();
        if (LOG_STATUS) Log.d("***ischecked", exitDialogCheck.isChecked() + "");
        editor.putBoolean("exitdialogcheck", exitDialogCheck.isChecked());
        editor.commit();
    }

    private void setTurnOnOffButtonImage() {
        if (isFmOn() == true) {
            mOnOffButton.setImageResource(R.drawable.power_enable);
        } else {
            // Find a icon to indicate off
            mOnOffButton.setImageResource(R.drawable.power_disable);
        }
    }

    /**
     * add by chenhengheng excuted asyncFreqInit when ui start
     */

    private void asyncCheckAndEnableRadio() {
        if(LOG_STATUS) Log.d(LOGTAG, "asyncCheckAndEnableRadio");
        // Save the initial state of the BT adapter
        mBluetoothEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled();

        if (mBluetoothEnabled) {
            if (!isFmOn()) {
                enableRadio();
            }
            enableRadioOnOffUI();
        } else {
            // Enable the BT adapter
            BluetoothAdapter.getDefaultAdapter().enable();

            AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected void onPreExecute() {
                    if (mBluetoothStartingDialog != null) {
                        mBluetoothStartingDialog.dismiss();
                        mBluetoothStartingDialog = null;
                    }
                    mBluetoothStartingDialog = ProgressDialog.show(
                            FMRadio.this, null, getString(R.string.init_FM),
                            true, false);
                    super.onPreExecute();
                }

                @Override
                protected Boolean doInBackground(Void... params) {
                    int n = 0;
                    try {
                        while (!mBluetoothEnabled && n < 30) {
                            Thread.sleep(1000);
                            ++n;
                        }
                    } catch (InterruptedException e) {
                    } finally {
                        return true;
                    }
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    if (mBluetoothStartingDialog != null
                            && mBluetoothStartingDialog.isShowing()) {
                        mBluetoothStartingDialog.dismiss();
                        mBluetoothStartingDialog = null;
                    }
                    if (mBluetoothEnabled) {
                        if (!isFmOn()) {
                            enableRadio();
                        }
                        enableRadioOnOffUI();
                    } else {
                        Toast toast = Toast.makeText(FMRadio.this,
                                getString(R.string.need_bluetooth),
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 240);
                        toast.show();
                    }
                    super.onPostExecute(result);
                }
            };
            task.execute();
        }
    }

    private void enableRadio() {
        if(LOG_STATUS) Log.d(LOGTAG, "enableRadio");
        mIsScaning = false;
        mIsSeeking = false;
        mIsSearching = false;
        boolean bStatus = false;

        if (mService != null) {
            try {
                // reset volume to avoid a bug that volume will be MAX
                int vol = AudioSystem
                        .getStreamVolumeIndex(AudioSystem.STREAM_FM);
                AudioSystem.setStreamVolumeIndex(AudioSystem.STREAM_FM, vol);

                if (!isPhoneInCall()) {
                    mService.unMute();
                    bStatus = mService.fmOn();
                }

                if (bStatus) {
                    if (isAntennaAvailable() || (mEarPhoneState == 1)) {
                        // Set the previously tuned frequency
                        int tunedFreq = FmSharedPreferences.getTunedFrequency();
                        tuneRadio(tunedFreq);

                        searchFrequencyFromGallery(tunedFreq);

                        // The output device is not set on a FM radio power on
                        // so we do it here
                        if (FmSharedPreferences.getSpeaker()) {
                            switchToSpeaker();
                        } else {
                            switchToHeadset();
                        }

                        // // Update the speaker icon
                        // setSpeakerUI(FmSharedPreferences.getSpeaker());

                        // Turn on the FM radio
                        enableRadioOnOffUI(true);

                        // added by even 2012-03-19
                        if (isFmOn()) {
                            if ((searchStartToFinishList.size() == 0)
                                    && (null == mAlertDlg)) {
                                // when bluetooth enabled search dialog open
                                // modify by even 2012-03-15
                                if (mBooRequireBluetooth && mBluetoothEnabled
                                        || !mBooRequireBluetooth) {
                                    mSearchDialog = ProgressDialog
                                            .show(FMRadio.this,
                                                    getResources()
                                                    .getString(
                                                            R.string.dialog_autosearch_title),
                                                            getResources()
                                                            .getString(
                                                                    R.string.dialog_autosearch_message),
                                                                    true);

                                    // delete by maowenjiang 2012-03-06
                                    autoSearchThread = new Thread(
                                            autoSearchRunnable);
                                    autoSearchThread.start();
                                }
                            }
                        }
                    } else {
                        if (LOG_STATUS) {
                            Log.v(LOGTAG,
                                    " if (isAntennaAvailable() || (mEarPhoneState == 1))  mEarPhoneState = "
                                            + mEarPhoneState);
                        }
                        disableRadio();
                        return;
                    }
                } else {
                    Log.e(LOGTAG, " mService.fmOn failed");
                    mCommandFailed = CMD_FMON;
                    showDialog(DIALOG_CMD_FAILED);
                }
            } catch (RemoteException e) {
                Log.e(LOGTAG, "RemoteException in enableRadio", e);
            }
        }
    }

    private void disableRadio() {
        boolean bStatus = false;

        // Cancel a frequency search if one was ongoing
        cancelSearch();
        endSleepTimer();

        // Check if our service stub is valid
        if (mService != null) {
            try {
                // Save the current tuned frequency so on on re-enable we revert
                // back to it
                // FmSharedPreferences.setTunedFrequency(mFreqIndicator.getFrequency());
                FmSharedPreferences.setTunedFrequency((Integer) channelNum
                        .get(mFmChannelGallery.getSelectedItemPosition()
                                % channelNum.size()));
                // Mute the audio
                mService.mute();

                // Turn off the FM radio
                bStatus = mService.fmOff();

                // Update the On/Off button in the UI to show off state
                // enableRadioOnOffUI();

                if (bStatus == false) {
                    mCommandFailed = CMD_FMOFF;
                    Log.e(LOGTAG, " mService.fmOff failed");
                    // showDialog(DIALOG_CMD_FAILED);
                } else {
                    /* shut down force use */
                    // added by even 2012-03-19 close speaker when close FM
                    setSpeakerFunc(false);
                    FmSharedPreferences.setSpeaker(false);
                    mPrefs.Save();
                }

            } catch (RemoteException e) {
                Log.e(LOGTAG, "RemoteException in disableRadio", e);
            }
        }
    }

    private void toggleRadioOffBluetoothBehaviour() {
        if(LOG_STATUS) Log.d(LOGTAG, "toggleRadioOffBluetoothBehavior");

        // Check if the BT adapter is currently enabled
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            switch (mPrefs.getBluetoothExitBehaviour()) {
            case 0:
                if (LOG_STATUS) {
                    Log.v(LOGTAG,
                            "toggleRadioOffBluetoothBehavior: Preference is to leave BT "
                                    + "adapter on so not disabling");
                }
                break;
            case 1: // Restore initial BT state
                // if (!mInitialBtState) {
                // BluetoothAdapter.getDefaultAdapter().disable();
                // }
                if (!mCheckFlag) {
                    // modify by even 2012-03-21
                    if (mEarPhoneState != 0) {
                        popFMExitDialog();
                    }
                } else {
                    if (isFmOn()) {
                        disableRadio();
                    }
                    finish();
                }
                break;

            case 2: // Prompt for action
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.prompt_disable_bt)
                .setPositiveButton(R.string.prompt_yes,
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                            int id) {
                        mBooBluetoothOpen = false;
                        if (!mCheckFlag) {
                            popFMExitDialog();
                        } else {
                            if (isFmOn()) {
                                disableRadio();
                            }
                            finish();
                        }
                    }
                })
                .setNegativeButton(R.string.prompt_no,
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                            int id) {
                        mBooBluetoothOpen = true;
                        if (!mCheckFlag) {
                            popFMExitDialog();
                        } else {
                            if (isFmOn()) {
                                disableRadio();
                            }

                            finish();
                        }
                    }
                }).show();
                break;

            case 3: // Always disable bluetooth
                BluetoothAdapter.getDefaultAdapter().disable();
                break;
            }
        }
    }

    public static void fmConfigure() {
        boolean bStatus = false;
        if (mService != null) {
            try {
                bStatus = mService.fmReconfigure();
                if (bStatus == false) {
                    mCommandFailed = CMD_FMCONFIGURE;
                    Log.e(LOGTAG, " mService.fmReconfigure failed");
                    // showDialog(DIALOG_CMD_FAILED);
                }
            } catch (RemoteException e) {
                Log.e(LOGTAG, "RemoteException in fmConfigure", e);
            }
        }
    }

    public static void fmAutoAFSwitch() {
        boolean bStatus = false;
        if (mService != null) {
            try {
                bStatus = mService.enableAutoAF(FmSharedPreferences
                        .getAutoAFSwitch());
                if (bStatus == false) {
                    mCommandFailed = CMD_SET_AUTOAF;
                    Log.e(LOGTAG, " mService.enableAutoAF failed");
                    // showDialog(DIALOG_CMD_FAILED);
                }
            } catch (RemoteException e) {
                Log.e(LOGTAG, "RemoteException in fmAutoAFSwitch", e);
            }
        }
    }

    public static void fmAudioOutputMode() {
        boolean bStatus = false;
        if (mService != null) {
            try {
                bStatus = mService.enableStereo(FmSharedPreferences
                        .getAudioOutputMode());
                if (bStatus == false) {
                    mCommandFailed = CMD_SET_AUDIO_MODE;
                    Log.e(LOGTAG, " mService.enableStereo failed");
                    // showDialog(DIALOG_CMD_FAILED);
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private boolean startRecord() {
        mRecording = true;
        return mRecording;
    }

    private boolean isRecording() {
        return mRecording;
    }

    private boolean stopRecord() {
        mRecording = false;
        return mRecording;
    }

    private void addToPresets(int index) {
        if (-1 == index)
            return;
        int currentListIndex = FmSharedPreferences.getCurrentListIndex();
        String name = FrequencyPicker.formatFrequencyString(mTunedStation
                .getFrequency());
        if (false) {
            // obsolete by george,2011-12-29
            FmSharedPreferences.addStation(name, mTunedStation.getFrequency(),
                    currentListIndex);
        } else {
            // by george,fill all PresentList with '0' station
            if (MAX_PRESETS_PER_PAGE > FmSharedPreferences
                    .getListStationCount()) {
                for (int i = (FmSharedPreferences.getListStationCount()); i < (MAX_PRESETS_PER_PAGE); i++) {
                    FmSharedPreferences.addStation("", 0, currentListIndex);
                }
            }
            if (currentListIndex != -1) {
                FmSharedPreferences.setStation(currentListIndex, index, name,
                        mTunedStation.getFrequency());
            }
        }

        setupPresetLayout();
        // mHsv.scrollTo(mPresetButtons[(mButtonCollectId)].getLeft(),
        // mPresetButtons[mButtonCollectId].getMeasuredHeight());
    }

    private void enableRadioOnOffUI() {
        boolean bEnable = isFmOn();

        /* Disable if no antenna/headset is available */
        if (!isAntennaAvailable()) {
            bEnable = false;
        }

        enableRadioOnOffUI(bEnable);
    }

    private void enableRadioOnOffUI(boolean bEnable) {
        if (bEnable == true) {
            mFmChannelShowText.setTextColor(0xff2C85C6);
        } else {
            mFmChannelShowText.setTextColor(0xff999999);
        }
        setTurnOnOffButtonImage();

        for (int nButton = 0; nButton < MAX_PRESETS_PER_PAGE; nButton++) {
            mPresetButtons[nButton].setEnabled(bEnable);
        }
        mFmChannelGallery.setEnabled(bEnable);
        mHsv.setEnabled(bEnable);

    }

    private void updateSearchProgress() {
        if (mService != null) {
            try {
                int freq = mService.getFreq();

                // loop for up to 4 seconds waiting for search to find a station
                for (int i = 0; i < 8 && mIsSeeking; i++) {
                    int freqb = mService.getFreq();
                    if (freq != freqb) {
                        // if frequencies don't match wait 500ms then try again
                        freq = freqb;
                        Thread.sleep(500);
                    } else {
                        // if frequencies do match seeking is finished
                        mIsSeeking = false;
                        autoSearchChannelFinnal = freq;
                        // searchStartToFinishList.add(mService.getFreq());
                    }
                }

                if (mIsSeeking) {
                    // if the loop completed without stopping on a station
                    // cancel the search
                    cancelSearch();
                } else {
                    // if a station was found update the display with the new
                    mTunedStation.setFrequency(freq);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupPresetLayout() {
        // int numStations = FmSharedPreferences.getListStationCount();
        int addedStations = 0;
        mButtonCollectId = 0;
        /*
         * For every station, save the station as a tag and update the display
         * on the preset Button.
         */
        for (int buttonIndex = 0; (buttonIndex < MAX_PRESETS_PER_PAGE); buttonIndex++) {
            if (mPresetButtons[buttonIndex] != null) {
                PresetStation station = FmSharedPreferences
                        .getStationInList(buttonIndex);
                String display = "+";
                // added by george,2011-12-29
                if (station != null && (station.getFrequency() != 0)) {
                    display = station.getName();
                    mPresetButtons[buttonIndex].setText(display);
                    mPresetButtons[buttonIndex].setTextColor(0xff999999);
                    mPresetButtons[buttonIndex].setTag(station);
                    mButtonCollectId = buttonIndex;
                    addedStations++;
                } else {
                    mPresetButtons[buttonIndex].setText(display);
                    mPresetButtons[buttonIndex].setTextColor(0xff999999);
                    mPresetButtons[buttonIndex].setTag(station);
                }
            }
        }
    }

    private void updateStationInfoToUI() {
        FmSharedPreferences.setTunedFrequency(mTunedStation.getFrequency());
    }

    private boolean isFmOn() {
        boolean bOn = false;
        if (mService != null) {
            try {
                bOn = mService.isFmOn();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return (bOn);
    }

    private boolean isMuted() {
        boolean bMuted = false;
        if (mService != null) {
            try {
                bMuted = mService.isMuted();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return (bMuted);
    }

    private boolean isPhoneInCall() {
        int state = TelephonyManager.getDefault().getCallState();
        if (state == TelephonyManager.CALL_STATE_RINGING
                || state == TelephonyManager.CALL_STATE_OFFHOOK) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isScanActive() {
        return (mIsScaning);
    }

    private boolean isSeekActive() {
        return (mIsSeeking);
    }

    private boolean isSearchActive() {
        return (mIsSearching);
    }

    public static PresetStation getCurrentTunedStation() {
        return mTunedStation;
    }

    private void SeekPreviousStation() {
        if(LOG_STATUS) Log.d(LOGTAG, "SeekPreviousStation");
        if (mService != null) {
            try {
                if (!isSeekActive()) {
                    mIsSeeking = mService.seek(false);
                    if (mIsSeeking == false) {
                        mCommandFailed = CMD_SEEK;
                        Log.e(LOGTAG, " mService.seek failed");
                        showDialog(DIALOG_CMD_FAILED);
                    }

                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        resetFMStationInfoUI();
        updateSearchProgress();
    }

    private void SeekNextStation() {
        if(LOG_STATUS) Log.d(LOGTAG, "SeekNextStation");
        if (mService != null) {
            try {
                if(LOG_STATUS) Log.d(LOGTAG, "SeekNextStation isSeekActive:" + isSeekActive());
                if (!isSeekActive()) {
                    mIsSeeking = mService.seek(true);
                    if (mIsSeeking == false) {
                        mCommandFailed = CMD_SEEK;
                        Log.e(LOGTAG, " mService.seek failed");
                        showDialog(DIALOG_CMD_FAILED);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        resetFMStationInfoUI();
        updateSearchProgress();
    }

    /** Scan related */
    private void initiateSearch(int pty) {
        synchronized (this) {
            mIsScaning = true;
            if (mService != null) {
                try {
                    mIsScaning = mService.scan(pty);
                    if (mIsScaning == false) {
                        mCommandFailed = CMD_SCAN;
                        Log.e(LOGTAG, " mService.scan failed");
                        showDialog(DIALOG_CMD_FAILED);
                    } else {
                        mScanPty = pty;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                updateSearchProgress();
            }
        }
    }

    /** SEEK Station with the matching PI */
    private void initiatePISearch(int pi) {
        if(LOG_STATUS) Log.d(LOGTAG, "initiatePISearch");
        if (mService != null) {
            try {
                if (!isSeekActive()) {
                    mIsSeeking = mService.seekPI(pi);
                    if (mIsSeeking == false) {
                        mCommandFailed = CMD_SEEKPI;
                        Log.e(LOGTAG, " mService.seekPI failed");
                        showDialog(DIALOG_CMD_FAILED);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        updateSearchProgress();
    }

    private void cancelSearch() {
        synchronized (this) {
            if (mService != null) {
                try {
                    if ((mIsScaning == true) || (mIsSeeking == true)
                            || (mIsSearching == true)) {
                        if (true == mService.cancelSearch()) {
                            mIsScaning = false;
                            mIsSeeking = false;
                            mIsSearching = false;
                        } else {
                            mCommandFailed = CMD_CANCELSEARCH;
                            Log.e(LOGTAG, " mService.cancelSearch failed");
                            showDialog(DIALOG_CMD_FAILED);
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        updateSearchProgress();
    }

    /** get Strongest Stations */
    private void initiateSearchList() {
        synchronized (this) {
            mIsSearching = false;
            if (mService != null) {
                try {
                    mIsSearching = mService
                            .searchStrongStationList(NUM_AUTO_PRESETS_SEARCH);
                    if (mIsSearching == false) {
                        mCommandFailed = CMD_SEARCHLIST;
                        showDialog(DIALOG_CMD_FAILED);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                updateSearchProgress();
            }
        }
    }

    private void initiateSleepTimer(long seconds) {
        mSleepAtPhoneTime = (SystemClock.elapsedRealtime()) + (seconds * 1000);

        mSleepCancelled = false;
        if (mSleepUpdateHandlerThread == null) {
            mSleepUpdateHandlerThread = new Thread(null, doSleepProcessing,
                    "SleepUpdateThread");
        }
        /* Launch he dummy thread to simulate the transfer progress */
        if (mSleepUpdateHandlerThread.getState() == Thread.State.TERMINATED) {
            mSleepUpdateHandlerThread = new Thread(null, doSleepProcessing,
                    "SleepUpdateThread");
        }
        /* If the thread state is "new" then the thread has not yet started */
        if (mSleepUpdateHandlerThread.getState() == Thread.State.NEW) {
            mSleepUpdateHandlerThread.start();
        }
    }

    private void endSleepTimer() {
        mSleepAtPhoneTime = 0;
        mSleepCancelled = true;
    }

    private boolean hasSleepTimerExpired() {
        boolean expired = true;
        if (isSleepTimerActive()) {
            long timeNow = ((SystemClock.elapsedRealtime()));
            if (timeNow < mSleepAtPhoneTime) {
                expired = false;
            }
        }
        return expired;
    }

    private boolean isSleepTimerActive() {
        boolean active = false;
        if (mSleepAtPhoneTime > 0) {
            active = true;
        }
        return active;
    }

    private void searchFrequencyFromGallery(int frequency) {
        if (channelNum.contains(frequency)) {
            int index = channelNum.indexOf(frequency);
            mFmChannelGallery.setSelection(index);
            initSearchFrequencyResult(frequency);
        }
    }

    private void initSearchFrequencyResult(final int frequency) {
        mFmChannelShowText.setText("" + frequency / 1000f);
        fmChannelBtnLight(frequency / 1000f);

        mHandler.post(new Runnable() {

            @Override
            public void run() {
                tuneRadio(frequency);
            }
        });

    }

    private void updateExpiredSleepTime() {
    }

    private Handler mUIUpdateHandlerHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SLEEPTIMER_EXPIRED: {
                mSleepAtPhoneTime = 0;
                if (mSleepCancelled != true) {
                    disableRadio();
                }
                return;
            }
            case SLEEPTIMER_UPDATE: {
                updateExpiredSleepTime();
                break;
            }
            default:
                break;
            }
            super.handleMessage(msg);
        }
    };

    /* Thread processing */
    private Runnable doSleepProcessing = new Runnable() {
        public void run() {
            boolean sleepTimerExpired = hasSleepTimerExpired();
            while (sleepTimerExpired == false) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Message statusUpdate = new Message();
                statusUpdate.what = SLEEPTIMER_UPDATE;
                mUIUpdateHandlerHandler.sendMessage(statusUpdate);
                sleepTimerExpired = hasSleepTimerExpired();
            }
            Message finished = new Message();
            finished.what = SLEEPTIMER_EXPIRED;
            mUIUpdateHandlerHandler.sendMessage(finished);
        }
    };

    private static StringBuilder sFormatBuilder = new StringBuilder();

    private static Formatter sFormatter = new Formatter(sFormatBuilder,
            Locale.getDefault());

    private static final Object[] sTimeArgs = new Object[5];

    private String makeTimeString(long secs) {
        String durationformat = getString(R.string.durationformat);

        /*
         * Provide multiple arguments so the format can be changed easily by
         * modifying the xml.
         */
        sFormatBuilder.setLength(0);

        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = (secs / 60) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;

        return sFormatter.format(durationformat, timeArgs).toString();
    }

    private void tuneRadio(int frequency) {
        if ((mService != null)) {
            boolean bStatus = false;
            try {

                mTunedStation.setName("");
                mTunedStation.setPI(0);
                mTunedStation.setPty(0);
                mTunedStation.setFrequency(frequency);
                updateStationInfoToUI();
                bStatus = mService.tune(frequency);
                if (bStatus) {
                    mCommandActive = CMD_TUNE;
                } else {
                    mCommandFailed = CMD_TUNE;
                    Log.e(LOGTAG, " mService.tune failed");
                    showDialog(DIALOG_CMD_FAILED);
                }
            } catch (RemoteException e) {
                // Log.e(LOGTAG, "RemoteException in tuneRadio", e);
            }
        }
    }

    private void resetFMStationInfoUI() {
        mTunedStation.setFrequency(FmSharedPreferences.getTunedFrequency());
        mTunedStation.setName("");
        mTunedStation.setPI(0);
        mTunedStation.setRDSSupported(false);
        // mTunedStation.setPI(20942);
        mTunedStation.setPty(0);
        updateStationInfoToUI();
    }

    private boolean startThread(Thread t) {
        if (t == null) {
            t = new Thread();
        }
        if (!t.isAlive()) {
            t.destroy();
            t.start();
        }
        return t.isAlive();
    }

    final Runnable mRadioEnabled = new Runnable() {
        public void run() {
            /* Update UI to FM On State */
            enableRadioOnOffUI(true);
            /* Tune to the last tuned frequency */
            for (int i = 0; i < channelNum.size(); i++) {
                int channel = (Integer) channelNum.get(i);
                if (channel == FmSharedPreferences.getTunedFrequency()) {
                    mFmChannelGallery.setSelection(i);
                    FrequencyPicker.formatFrequencyString(channel);
                    break;
                }
            }

        }
    };

    final Runnable mRadioDisabled = new Runnable() {
        public void run() {
            /* shut down force use */
            AudioSystem.setForceUse(AudioSystem.FOR_MEDIA,
                    AudioSystem.FORCE_NONE);
            /* Update UI to FM Off State */
            enableRadioOnOffUI(false);

            if (mPrefs.getHeadsetDcBehaviour()) {
                toggleRadioOffBluetoothBehaviour();
            }
        }
    };

    final Runnable mUpdateStationInfo = new Runnable() {
        public void run() {
            int tunedFreq = FmSharedPreferences.getTunedFrequency();
            PresetStation station = FmSharedPreferences
                    .getStationFromFrequency(tunedFreq);
            if (station != null) {
                mTunedStation.Copy(station);
            }

            updateSearchProgress();
            resetFMStationInfoUI();
            if (mFmChannelShowText != null) {

                mFmChannelShowText.setText("" + tunedFreq / 1000f);
            }
        }
    };

    Runnable autoSearchRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                while (mService == null) {
                    Thread.sleep(100);
                }
                while (!mService.isFmOn()) {
                    Thread.sleep(100);
                }

                // modify by maowenjiang 2012-02-29
                // tuneRadio(FmSharedPreferences.getTunedFrequency());
                tuneRadio(87600);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (searchStartToFinishList.size() != 0) { // auto search (if listis
                // null)
                searchStartToFinishList.clear();
            }

            if (searchStartToFinishStringList.size() != 0) { // auto search (if
                // listis null)
                searchStartToFinishStringList.clear();
            }

            autoSearchChannel();
        }
    };

    final Runnable mSearchComplete = new Runnable() {
        public void run() {
            if(LOG_STATUS) Log.d(LOGTAG, "mSearchComplete: ");
            mScanPty = 0;
            mIsScaning = false;
            mIsSeeking = false;
            mIsSearching = false;
            updateSearchProgress();
            resetFMStationInfoUI();
        }
    };

    final Runnable mSearchListComplete = new Runnable() {
        public void run() {
            if(LOG_STATUS) Log.d(LOGTAG, "mSearchListComplete: ");
            mIsSearching = false;

            /* Now get the list */
            if (mService != null) {
                try {
                    int[] searchList = mService.getSearchList();
                    if (searchList != null) {
                        /* Add the stations into the preset list */
                        int currentList = FmSharedPreferences
                                .getCurrentListIndex();
                        for (int station = 0; (station < searchList.length)
                                && (station < NUM_AUTO_PRESETS_SEARCH); station++) {
                            int frequency = searchList[station];
                            if ((frequency < FmSharedPreferences
                                    .getUpperLimit())
                                    && (frequency > FmSharedPreferences
                                            .getLowerLimit())) {
                                FmSharedPreferences.addStation("", frequency,
                                        currentList);
                            }

                            if (frequency == 0) {
                                break;
                            }
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            updateSearchProgress();
            resetFMStationInfoUI();
            setupPresetLayout();
        }
    };

    final Runnable mOnStereo = new Runnable() {
        public void run() {
            // do nothing, we can show stereo icon later here.
        }
    };

    final Runnable mUpdateRadioText = new Runnable() {
        public void run() {
            String str = "";
            if (mService != null) {
                try {
                    /* Get PTY and PI and update the display */
                    int tempInt = mService.getProgramType();
                    /* Save PTY */
                    mTunedStation.setPty(tempInt);
                    tempInt = mService.getProgramID();
                    if (tempInt != 0) {
                        mTunedStation.setPI(tempInt);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /* Create runnable for posting */
    final Runnable mUpdateProgramService = new Runnable() {
        public void run() {
            String str = "";
            if (mService != null) {
                try {
                    /* Get PTY and PI and update the display */
                    int tempInt = mService.getProgramType();
                    /* Save PTY */
                    mTunedStation.setPty(tempInt);

                    tempInt = mService.getProgramID();
                    /* Save the program ID */
                    if (tempInt != 0) {
                        mTunedStation.setPI(tempInt);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void DebugToasts(String str, int duration) {
        // Log.d(LOGTAG, "Debug:" + str);
    }

    /**
     * This Handler will scroll the text view. On startScroll, the scrolling
     * starts after SCROLLER_START_DELAY_MS The Text View is scrolled left one
     * character after every SCROLLER_UPDATE_DELAY_MS When the entire text is
     * scrolled, the scrolling will restart after SCROLLER_RESTART_DELAY_MS
     */
    private static final class ScrollerText extends Handler {

        private static final byte SCROLLER_STOPPED = 0x51;

        private static final byte SCROLLER_STARTING = 0x52;

        private static final byte SCROLLER_RUNNING = 0x53;

        private static final int SCROLLER_MSG_START = 0xF1;

        private static final int SCROLLER_MSG_TICK = 0xF2;

        private static final int SCROLLER_MSG_RESTART = 0xF3;

        private static final int SCROLLER_START_DELAY_MS = 1000;

        private static final int SCROLLER_RESTART_DELAY_MS = 3000;

        private static final int SCROLLER_UPDATE_DELAY_MS = 200;

        private final WeakReference<TextView> mView;

        private byte mStatus = SCROLLER_STOPPED;

        String mOriginalString;

        int mStringlength = 0;

        int mIteration = 0;

        ScrollerText(TextView v) {
            mView = new WeakReference<TextView>(v);
        }

        /**
         * Scrolling Message Handler
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SCROLLER_MSG_START:
                mStatus = SCROLLER_RUNNING;
                updateText();
                break;
            case SCROLLER_MSG_TICK:
                updateText();
                break;
            case SCROLLER_MSG_RESTART:
                if (mStatus == SCROLLER_RUNNING) {
                    startScroll();
                }
                break;
            }
        }

        /**
         * Moves the text left by one character and posts a delayed message for
         * next update after SCROLLER_UPDATE_DELAY_MS. If the entire string is
         * scrolled, then it displays the entire string and waits for
         * SCROLLER_RESTART_DELAY_MS for scrolling restart
         */
        void updateText() {
            if (mStatus != SCROLLER_RUNNING) {
                return;
            }

            removeMessages(SCROLLER_MSG_TICK);

            final TextView textView = mView.get();
            if (textView != null) {
                String szStr2 = "";
                if (mStringlength > 0) {
                    mIteration++;
                    if (mIteration >= mStringlength) {
                        mIteration = 0;
                        sendEmptyMessageDelayed(SCROLLER_MSG_RESTART,
                                SCROLLER_RESTART_DELAY_MS);
                    } else {
                        sendEmptyMessageDelayed(SCROLLER_MSG_TICK,
                                SCROLLER_UPDATE_DELAY_MS);
                    }
                    szStr2 = mOriginalString.substring(mIteration);
                }
                textView.setText(szStr2);
            }
        }

        /**
         * Stops the scrolling The textView will be set to the original string.
         */
        void stopScroll() {
            mStatus = SCROLLER_STOPPED;
            removeMessages(SCROLLER_MSG_TICK);
            removeMessages(SCROLLER_MSG_RESTART);
            removeMessages(SCROLLER_MSG_START);
            resetScroll();
        }

        /**
         * Resets the scroll to display the original string.
         */
        private void resetScroll() {
            mIteration = 0;
            final TextView textView = mView.get();
            if (textView != null) {
                textView.setText(mOriginalString);
            }
        }

        /**
         * Starts the Scrolling of the TextView after a delay of
         * SCROLLER_START_DELAY_MS Starts only if Length > 0
         */
        void startScroll() {
            final TextView textView = mView.get();
            if (textView != null) {
                mOriginalString = (String) textView.getText();
                mStringlength = mOriginalString.length();
                if (mStringlength > 0) {
                    mStatus = SCROLLER_STARTING;
                    sendEmptyMessageDelayed(SCROLLER_MSG_START,
                            SCROLLER_START_DELAY_MS);
                }
            }
        }
    }

    public static IFMRadioService sService = null;

    private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();

    public static boolean bindToService(Context context) {
        if (LOG_STATUS) {
            Log.d(LOGTAG, "bindToService: Context");
        }
        return bindToService(context, null);
    }

    public static boolean bindToService(Context context,
            ServiceConnection callback) {
        if (LOG_STATUS) {
            Log.d(LOGTAG,
                    "bindToService: Context with serviceconnection callback");
        }

        context.startService(new Intent(context, FMRadioService.class));
        ServiceBinder sb = new ServiceBinder(callback);
        sConnectionMap.put(context, sb);
        return context.bindService(
                (new Intent()).setClass(context, FMRadioService.class), sb,
                Context.BIND_AUTO_CREATE);
    }

    public static void unbindFromService(Context context) {
        ServiceBinder sb = (ServiceBinder) sConnectionMap.remove(context);
        if (sb == null) {
            Log.e(LOGTAG, "Trying to unbind for unknown Context");
            return;
        }
        context.unbindService(sb);
        if (sConnectionMap.isEmpty()) {
            // presumably there is nobody interested in the service at this
            // point,
            // so don't hang on to the ServiceConnection
            sService = null;
        }
    }

    private static class ServiceBinder implements ServiceConnection {
        ServiceConnection mCallback;

        ServiceBinder(ServiceConnection callback) {
            mCallback = callback;
        }

        public void onServiceConnected(ComponentName className,
                android.os.IBinder service) {
            sService = IFMRadioService.Stub.asInterface(service);
            if (LOG_STATUS) {
                Log.d(LOGTAG, "onServiceConnected: mCallback" + mCallback);
            }
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            sService = null;
        }
    }

    private ServiceConnection osc = new ServiceConnection() {
        public void onServiceConnected(ComponentName classname, IBinder obj) {
            mService = IFMRadioService.Stub.asInterface(obj);
            if (LOG_STATUS) {
                Log.d(LOGTAG, "ServiceConnection: osc service: " + mService);
            }

            if (mService != null) {
                try {
                    mService.registerCallbacks(mServiceCallbacks);

                    // modify by even 2012-03-15
                    if (mBooRequireBluetooth && earState > 0) {
                        asyncCheckAndEnableRadio();
                    } else if (earState > 0) {
                        if (LOG_STATUS) {
                            Log.d(LOGTAG, "Service enableRadio");
                        }

                        enableRadio();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return;
            } else {
                // Log.e(LOGTAG, "IFMRadioService onServiceConnected failed");
            }
            if (LOG_STATUS) {
                Log.d(LOGTAG, "IFMRadioService getIntent().getData():"
                        + getIntent().getData());
            }

            if (getIntent().getData() == null) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(FMRadio.this, FMRadio.class);
                startActivity(intent);
            }
            finish();
        }

        public void onServiceDisconnected(ComponentName classname) {
        }
    };

    private IFMRadioServiceCallbacks.Stub mServiceCallbacks = new IFMRadioServiceCallbacks.Stub() {
        public void onEnabled() {
            if(LOG_STATUS) Log.d(LOGTAG, "mServiceCallbacks.onEnabled :");
            mHandler.post(mRadioEnabled);
        }

        public void onDisabled() {
            if(LOG_STATUS) Log.d(LOGTAG, "mServiceCallbacks.onDisabled :");
            mHandler.post(mRadioDisabled);
        }

        public void onTuneStatusChanged() {
            if(LOG_STATUS) Log.d(LOGTAG, "mServiceCallbacks.onTuneStatusChanged :");
            mHandler.post(mUpdateStationInfo);
        }

        public void onProgramServiceChanged() {
            if(LOG_STATUS) Log.d(LOGTAG, "mServiceCallbacks.onProgramServiceChanged :");
            mHandler.post(mUpdateProgramService);
        }

        public void onRadioTextChanged() {
            if(LOG_STATUS) Log.d(LOGTAG, "mServiceCallbacks.onRadioTextChanged :");
            mHandler.post(mUpdateRadioText);
        }

        public void onAlternateFrequencyChanged() {
            if(LOG_STATUS) Log.d(LOGTAG, "mServiceCallbacks.onAlternateFrequencyChanged :");
        }

        public void onSignalStrengthChanged() {
            if(LOG_STATUS) Log.d(LOGTAG, "mServiceCallbacks.onSignalStrengthChanged :");
        }

        public void onSearchComplete() {
            if(LOG_STATUS) Log.d(LOGTAG, "mServiceCallbacks.onSearchComplete :");
            mHandler.post(mSearchComplete);
        }

        public void onSearchListComplete() {
            if(LOG_STATUS) Log.d(LOGTAG, "mServiceCallbacks.onSearchListComplete :");
            mHandler.post(mSearchListComplete);
        }

        public void onMute(boolean bMuted) {
            // do nothing when FM is muted
        }

        public void onAudioUpdate(boolean bStereo) {
            if ((bStereo) && (FmSharedPreferences.getAudioOutputMode())) {
                mStereo = FMRADIO_UI_STATION_AUDIO_STEREO;
            } else {
                mStereo = FMRADIO_UI_STATION_AUDIO_MONO;
            }
            if(LOG_STATUS) Log.d(LOGTAG, "mServiceCallbacks.onAudioUpdate :" + mStereo);
            mHandler.post(mOnStereo);
        }

        public void onStationRDSSupported(boolean bRDSSupported) {
            /*
             * Depending on the signal strength etc, RDS Lock Sync/Supported may
             * toggle, Since if a station Supports RDS, it will not change its
             * support intermittently just save the status and ignore any
             * "unsupported" state.
             */
            if (bRDSSupported) {
                mTunedStation.setRDSSupported(true);
            }
        }
    };

    class ChannelNumAdapter extends BaseAdapter {

        Context mContext;
        DisplayMetrics dm;

        public ChannelNumAdapter(Context context) {
            mContext = context;
            for (int i = 0; i < CHANNELNUM; i++) {
                int value = 87500 + (i * 100);
                channelNum.add(i, value);

            }

            dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            TextView channelNumText = new TextView(mContext);
            int index = (Integer) channelNum.get(arg0 % channelNum.size());
            channelNumText.setText(String.valueOf(index / 1000f));
            channelNumText.setTag(index);
            channelNumText.setVisibility(View.GONE);
            if (dm.widthPixels == 320 && dm.heightPixels == 480) {
                channelNumText.setPadding(0, 180, 0, 20);
            } else if (dm.widthPixels == 480 && dm.heightPixels == 800) {
                channelNumText.setPadding(0, 270, 0, 20);
            }

            if ((index % 500) == 0) {
                Float num = index / 1000f;
                if (num == 108.0) {
                    channelNumText.setWidth(100);
                }
                channelNumText.setVisibility(View.VISIBLE);
            } else {
                channelNumText.setWidth(20);
            }
            channelNumText.setTextSize(20);
            return channelNumText;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        int selectionId = arg2;
        if (LOG_STATUS) {
            Log.d(LOGTAG, "onTouchEventUP selectionId:+" + selectionId);
            Log.d(LOGTAG, "onTouchEventDown selectionId:" + selectionId);
        }

        int index = (Integer) channelNum.get(selectionId
                % channelNum.size());
        if(LOG_STATUS) Log.d(LOGTAG, "down index : " + index);

        initSearchFrequencyResult(index);
    }

    @Override
    public boolean onMenuItemClick(MenuItem arg0) {

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String model = SystemProperties.get("ro.product.device");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_option, menu);
        mMenu = menu;
        MenuItem speakerItem = mMenu.findItem(R.id.speaker_onoff);
        if ("blade".equalsIgnoreCase(model)
            || "n880s".equalsIgnoreCase(model)) {
            speakerItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.speaker_onoff:
            mItem = item;
            if (LOG_STATUS) {
                Log.d(LOGTAG, "setting!!!");
            }
            boolean speakerState = !FmSharedPreferences.getSpeaker();
            if (LOG_STATUS) {
                Log.d(LOGTAG, "speakerState = " + speakerState);
            }
            setSpeakerFunc(speakerState);
            if (speakerState) {
                item.setTitle(R.string.speaker_off);
            } else {
                item.setTitle(R.string.speaker_on);
            }
            FmSharedPreferences.setSpeaker(speakerState);
            return true;

        case R.id.auto_search:
            mSearchDialog = ProgressDialog.show(FMRadio.this, getResources()
                    .getString(R.string.dialog_autosearch_title),
                    getResources()
                    .getString(R.string.dialog_autosearch_message),
                    true);
            new Thread(autoSearchRunnable).start();
            return true;
        case R.id.manual_search:
            if (LOG_STATUS) {
                Log.d(LOGTAG, "option menu");
            }
            showDialog(DIALOG_PICK_FREQUENCY);
            return true;
        case R.id.search_list:
            if (searchStartToFinishStringList.size() == 0) {
                return false;
            }

            final AlertDialog.Builder menuBuilder = new AlertDialog.Builder(
                    FMRadio.this);
            menuBuilder.setTitle(getResources().getString(
                    R.string.menu_auto_search_list));
            menuBuilder.setItems(searchStartToFinishStringList
                    .toArray(new String[searchStartToFinishStringList.size()]),
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (LOG_STATUS) {
                        Log.d(LOGTAG, "onMenuItemClick item = " + item);
                    }
                    int frequency = (int) (Float
                            .parseFloat(searchStartToFinishStringList
                                    .get(item).replace("MHZ", "")) * 1000);
                    if (LOG_STATUS) {
                        Log.d(LOGTAG,
                                "frequency:"
                                        + Float.parseFloat(searchStartToFinishStringList
                                                .get(item).replace(
                                                        "MHZ", "")));
                    }
                    searchFrequencyFromGallery(frequency);
                    dialog.dismiss();
                }
            });
            menuBuilder.show();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        boolean b = mGestureDetector.onTouchEvent(event);
        if (LOG_STATUS) {
            Log.d(LOGTAG, "onTouch");
        }
        switch (event.getAction()) {
        case 0:// down
            break;
        case 1:// up
            break;
        case 2:// move
            if (LOG_STATUS) {
                Log.d(LOGTAG, "action move=========" + event.getAction());
            }
            int FLING_MINUS_VALUE = 100;

            // modify by even 2012-04-10
            int index = channelNum.get(mFmChannelGallery
                    .getSelectedItemPosition() % channelNum.size());
            Log.i(LOGTAG, "move index : " + index);
            if (LOG_STATUS) {
                Log.d(LOGTAG, "#mFmChannelShowText.getText : "
                        + mFmChannelShowText.getText());
            }

            initSearchFrequencyResult(index);
            break;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        if (LOG_STATUS) {
            Log.d(LOGTAG, "onDown");
        }
        return false;
    }

    // press && move quickly && up
    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
            float arg3) {
        if (LOG_STATUS) {
            Log.d(LOGTAG, "onFling");
        }

        if (searchStartToFinishList.size() == 0) { // auto search (if list is
            // null)
            // autoSearchBar.setVisibility(View.VISIBLE);
            // autoSearchBar.setProgress(0);
        } else {
            float minusVlaue = mFmChannelGallery.getHeight() - arg0.getY();
            if (minusVlaue > FLING_MINUS_VALUE) {
                if (arg0.getX() - arg1.getX() > FLING_MIN_DISTANCE
                        && Math.abs(arg2) > FLING_MIN_VELOCITY) {
                    // Fling left
                    onFlingMoveLocative(DIRECTION_LEFT);

                } else if (arg1.getX() - arg0.getX() > FLING_MIN_DISTANCE
                        && Math.abs(arg2) > FLING_MIN_VELOCITY) {
                    // Fling right
                    onFlingMoveLocative(DIRECTION_RIGHT);
                }
            }
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        // TODO Auto-generated method stub
    }

    // press && drag
    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
            float arg3) {
        if (LOG_STATUS) {
            Log.d(LOGTAG, "onScroll");
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        if (LOG_STATUS) {
            Log.d(LOGTAG, "onShowPress");
        }
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        // Log.e(LOGTAG, "onSingleTapUp");
        return false;
    }

    private static int result_frequency = 0;

    /**
     * 
     * @param direction
     */
    private void onFlingMoveLocative(int direction) {
        if (LOG_STATUS) {
            Log.d(LOGTAG, "searchStartToFinishList size:"
                    + searchStartToFinishList.size());
            Log.d(LOGTAG, "onFlingMoveLocative direction:" + direction);
        }
        int index = 0;
        switch (direction) {
        case DIRECTION_LEFT:
            if (LOG_STATUS) {
                Log.d(LOGTAG,
                        "Position LEFT 0:"
                                + (mFmChannelGallery.getSelectedItemPosition() % channelNum
                                        .size()));
                Log.d(LOGTAG,
                        "Position LEFT 1:"
                                + mFmChannelGallery.getSelectedItemPosition());
            }
            index = mFmChannelGallery.getSelectedItemPosition()
                    % channelNum.size();
            if (LOG_STATUS) {
                Log.d(LOGTAG,
                        "Position LEFT 2 channelNum:" + channelNum.get(index));
                Log.d(LOGTAG,
                        "onFlingMoveLocative searchStartToFinishList size: "
                                + searchStartToFinishList.size());
            }
            index = binarySearch(searchStartToFinishList,
                    channelNum.get(index), 1);
            break;
        case DIRECTION_RIGHT:

            if (LOG_STATUS) {
                Log.d(LOGTAG,
                        "Position RIGHT 0:"
                                + mFmChannelGallery.getSelectedItemPosition()
                                % channelNum.size());
                Log.d(LOGTAG,
                        "Position RIGHT 1:"
                                + mFmChannelGallery.getSelectedItemPosition());
            }
            index = mFmChannelGallery.getSelectedItemPosition()
                    % channelNum.size();
            if (LOG_STATUS) {
                Log.d(LOGTAG,
                        "onFlingMoveLocative searchStartToFinishList size: "
                                + searchStartToFinishList.size());
                Log.d(LOGTAG,
                        "Position RIGHT 2 channelNum:" + channelNum.get(index));
            }
            index = binarySearch(searchStartToFinishList,
                    channelNum.get(index), 0);
            break;
        default:
            break;
        }
        result_frequency = searchStartToFinishList.get(index);
        if (LOG_STATUS) {
            Log.d(LOGTAG, "result_frequency:" + result_frequency);
        }
        searchFrequencyFromGallery(result_frequency);
    }

    private int binarySearch(List<Integer> dataset, int data, int direction) {
        int beginIndex = 0;
        int endIndex = dataset.size() - 1;
        int midIndex = -1;
        int index = 0;

        if (beginIndex > endIndex) {
            // case 4:invalid dataset
            index = -1;// XXX:
            return index;
        } else if (data < dataset.get(beginIndex)) {
            // case 1:
            if (direction == 0) {
                index = endIndex;
            } else if (direction == 1) {
                index = 0;
            }
            return index;
        } else if (data > dataset.get(endIndex)) {
            // case 2:
            if (direction == 1) {
                index = endIndex;
            } else if (direction == 0) {
                index = 0;
            }
            return index;
        }

        // case 3:
        while (beginIndex <= endIndex) {
            midIndex = (beginIndex + endIndex) >>> 1; // idIndex =
        // (beginIndex +
        // endIndex) / 2

        if (data < dataset.get(midIndex)) {
            endIndex = midIndex - 1;
        } else if (data > dataset.get(midIndex)) {
            beginIndex = midIndex + 1;
        } else {
            if (direction == 1) {
                index = midIndex - 1;
                if (index < 0)
                    index = dataset.size() - 1;
            } else if (direction == 0) {
                index = midIndex + 1;
                if (index > (dataset.size() - 1))
                    index = 0;
            }
            return index;
        }
        }

        int value = dataset.get(midIndex);
        if (direction == 1) {
            if (value > data) {
                index = midIndex;
            } else {
                index = midIndex + 1;
            }
        } else if (direction == 0) {
            if (value > data) {
                index = midIndex - 1;
            } else {
                index = midIndex;
            }
        }
        if (index < 0)
            index = dataset.size() - 1;
        if (index > (dataset.size() - 1))
            index = 0;
        return index;
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                mBluetoothEnabled = state == BluetoothAdapter.STATE_ON;
                if (!mBluetoothEnabled) {
                    // Bluetooth is disabled so we should turn off FM too.
                    if (isFmOn()) {
                        finish();
                        disableRadio();
                        return;
                    }
                }
            }
        }
    };

    private class EarPhoneReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                mEarPhoneState = intent.getIntExtra("state", 0);
                if (LOG_STATUS) {
                    Log.d(LOGTAG, "mEarPhoneReceiver mEarPhoneState : "
                            + mEarPhoneState);
                }
                if (mEarPhoneState == 0) {
                    if (null != autoSearchThread && autoSearchThread.isAlive()) {
                        autoSearchThread.interrupt();
                        autoSearchThread = null;
                    }

                    if (mSearchDialog != null) {
                        mSearchDialog.dismiss();
                        mSearchDialog = null;
                    }
                    popDialog(true);

                    if (isFmOn()) {
                        if (LOG_STATUS) {
                            Log.d(LOGTAG, "mEarPhoneReceiver stop isFmOn");
                        }
                        disableRadio();
                        enableRadioOnOffUI(false);
                    }
                } else {
                    if (LOG_STATUS) {
                        Log.d(LOGTAG, "mEarPhoneReceiver enableRadio");
                    }
                    try {
                        if (LOG_STATUS) {
                            Log.d(LOGTAG, "searchStartToFinishList.size():"
                                    + searchStartToFinishList.size());
                            Log.d(LOGTAG, "mAlertDlg:" + mAlertDlg);
                            Log.d(LOGTAG, "ear tuneRadio");
                        }

                        // when list is empty,by george
                        // modify by even 2012-03-15
                        if (!isFmOn()) {
                            if (mBooRequireBluetooth) {
                                asyncCheckAndEnableRadio();
                            } else {
                                enableRadio();
                            }
                        } else {
                            tuneRadio(FmSharedPreferences.getTunedFrequency());

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    popDialog(false);
                }
            }
        }
    }

    public void popDialog(boolean open) {
        if (LOG_STATUS) {
            Log.d(LOGTAG, "popDialog run " + open);
        }
        if (open) {

            da.context = this;

            da.title = getResources().getString(R.string.fm_pop_title);

            da.message = getResources().getString(
                    R.string.need_headset_for_antenna);
            da.neutralButtonText = getResources()
                    .getString(R.string.fm_pop_btn);
            da.neutralButtonClickListener = DialogUtil.getNewCancelOption(this);

            if (mItem != null) {
                mItem.setTitle(R.string.speaker_on);
            }

            DialogInterface.OnClickListener popBtnListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // modify by even 2012-03-20
                    if (mEarPhoneState == 0) {
                        finish();
                        android.os.Process.killProcess(android.os.Process
                                .myPid());
                        System.exit(0);
                    } else {
                        if (mAlertDlg != null) {
                            mAlertDlg.cancel();
                            mAlertDlg.dismiss();
                            mAlertDlg = null;
                        }

                    }
                }
            };

            da.neutralButtonClickListener = popBtnListener;
            if (mAlertDlg == null) {
                mAlertDlg = DialogUtil.showNeutralDialog(da);
            }
        } else {
            if (mAlertDlg != null) {
                mAlertDlg.cancel();
                mAlertDlg.dismiss();
                mAlertDlg = null;
            }
        }
    }

    private void stringToAutoSearchList(String stations) {
        searchStartToFinishList.clear();
        searchStartToFinishStringList.clear();
        if (stations.length() > 0) {
            String[] ss = stations.split(" ");
            for (int index = 0; index < ss.length; ++index) {
                searchStartToFinishList.add(Integer.parseInt(ss[index]));
                searchStartToFinishStringList.add((Integer.parseInt(ss[index]))
                        / 1000f + " MHZ");
            }
        }
    }

    private String autoSearchListToString(List<Integer> intList) {
        if (intList.size() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append(intList.get(0));
            for (int index = 1; index < intList.size(); ++index) {
                sb.append(" " + intList.get(index));
            }
            return sb.toString();
        }
        return "";
    }

    // add by maowenjiang 2012-03-07
    private int getEarState() {
        FileReader file = null;
        char[] buffer = new char[1024];
        int len = 0;
        try {
            file = new FileReader(HEADSET_STATE_PATH);
            len = file.read(buffer, 0, 1024);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int headsetState = Integer.valueOf((new String(buffer, 0, len)).trim());
        return headsetState;
    }
}
