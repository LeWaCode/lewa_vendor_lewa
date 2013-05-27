package com.lewa.PIM.dialpad.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.R.integer;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.PatternMatcher;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;
import android.provider.CallLog.Calls;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.lewa.PIM.IM.event.RegisteredEvents;
import com.lewa.PIM.util.CommonMethod;

import com.lewa.PIM.R;
import com.lewa.PIM.util.SpecialCharSequenceMgr;
import com.lewa.os.util.LocationUtil;
import com.lewa.os.util.Util;

public class DialpadView implements View.OnClickListener,
        View.OnLongClickListener,
        View.OnTouchListener,
        TextWatcher {
    private static final String TAG = "DialpadView";

    public static final int CALLLOG_TYPE_ALL = -1;

    public static final int[] BTNS = {
        R.id.btn_dpkb_switch_caller,
        R.id.btn_dpkb_call,
        R.id.key_1,
        R.id.key_2,
        R.id.key_3,
        R.id.key_4,
        R.id.key_5,
        R.id.key_6,
        R.id.key_7,
        R.id.key_8,
        R.id.key_9,
        R.id.key_star,
        R.id.key_0,
        R.id.key_hash,
        //R.id.btn_dpkb_enter_contact,
        R.id.btn_dpkb_showhide_keyboard,
        R.id.btn_dpkb_backspace,
        R.id.btn_search,
        R.id.btn_filter_calllog,
        R.id.btn_dp_open_dialpad,
        R.id.btn_categorize_all_calllog,
        R.id.btn_categorize_out_calllog,
        R.id.btn_categorize_in_calllog,
        R.id.btn_categorize_miss_calllog,
        R.id.addcontact,
        R.id.joincontact,
        R.id.sendsms
    };
    
    public static final int[] KEYS = {
        KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_1,
        KeyEvent.KEYCODE_2,
        KeyEvent.KEYCODE_3,
        KeyEvent.KEYCODE_4,
        KeyEvent.KEYCODE_5,
        KeyEvent.KEYCODE_6,
        KeyEvent.KEYCODE_7,
        KeyEvent.KEYCODE_8,
        KeyEvent.KEYCODE_9,
        KeyEvent.KEYCODE_STAR,
        KeyEvent.KEYCODE_0,
        KeyEvent.KEYCODE_POUND,
        //KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_UNKNOWN,
        KeyEvent.KEYCODE_UNKNOWN
    };

    private ViewGroup mMainSoftBar;
    private ViewGroup mCategorySoftBar;
    private int mViewType = CALLLOG_TYPE_ALL;

    private View mContentView;
    private View mNumberParent;
    private EditText mNumberEdit;
    private DialpadOwner mOwner;

    private Button mDialKey;

    private boolean mSoundOn;
    private boolean vibarteOn;
    Context context;
    ContentResolver resolver;
    private ToneGenerator mTone;
    //private boolean mVibarteOn;
    private Vibrator mVibrator;

    private long mTimeStampWhenPress = 0;
    private Handler mHandler = new Handler();
    private final int QUERY_LOCATION_DELAY = 400;
    private String location = null;
    private String number = null;
   

    //private Method methodHandleSpecialChars;

    public DialpadView(View paramView, DialpadOwner owner) {
        mContentView = paramView;
        mOwner = owner;

        initSetting();

        View button = null;
        for (int i = 0; i < BTNS.length; ++i) {
            button = mContentView.findViewById(BTNS[i]);
            if (null != button) {
                button.setOnClickListener(this);
                button.setTag(Integer.valueOf(KEYS[i]));

                if ((R.id.key_0 == BTNS[i]) || (R.id.btn_dpkb_backspace == BTNS[i])) {
                    button.setOnLongClickListener(this);
                }

                //Added by GanFeng 20120223, disable the sound effects
                //while clicking the TwelveKeys(key_1~key_hash)
                //in case of the DTMF_TONE_WHEN_DIALING is enabled
                if ((i >= 2) && (i <= 13) && mSoundOn) {
                    button.setSoundEffectsEnabled(false);
                }
            }
        }

        mNumberParent = mContentView.findViewById(R.id.rl_dial_number_parent);

        mNumberEdit = (EditText )mContentView.findViewById(R.id.edt_dpkb_call_number);
        mNumberEdit.addTextChangedListener(this);
        mNumberEdit.setOnClickListener(this);
        mNumberEdit.setOnLongClickListener(this);
        mNumberEdit.setInputType(InputType.TYPE_NULL);
        //add by piero zeng 2012.6.18
        mNumberEdit.setFilters(new InputFilter[] { 
			new InputFilter() {  
				@Override
				public CharSequence filter(CharSequence seq, int start, int end, Spanned dst,
						int dstart, int dend) {   
						return isArrowInput(seq) ? seq : "";   
				    }
			}
		});
        mOwner.registerContextMenu(mNumberEdit);
        //mNumberEdit.setFocusable(true);
        //mNumberEdit.setFocusableInTouchMode(true);
        //mNumberEdit.setSelection(8);
        //mNumberEdit.requestFocus();

        mDialKey = (Button )mContentView.findViewById(R.id.btn_dpkb_call);

        mMainSoftBar = (ViewGroup )mContentView.findViewById(R.id.dp_nodialpad_softkey_bar);
        mCategorySoftBar = (ViewGroup )mContentView.findViewById(R.id.cl_category_softkey_bar);

    }
    /**
     * ÅÐ¶ÏÊäÈë»òÕ³ÌùµÄÊÇ·ñÎªÊý×Ö£¬*ºÍ#
     * @param seq
     * @return
     */
    private boolean isArrowInput(CharSequence seq){
	Pattern pattern = Pattern.compile("^\\+?[\\d\\*,\\#]*");
	
        Matcher isNum = pattern.matcher(seq);
        if( !isNum.matches() )
        {
              return false;
        }
        return true;
    }
  
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        mOwner.loadAllContacts();
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
//        if (null == methodHandleSpecialChars) {
//            try {
//                Class clsSpecialCharSequenceMgr = Class.forName("com.android.contacts.SpecialCharSequenceMgr");
//                Class[] paramTypes = new Class[] {Context.class, String.class, EditText.class};
//                methodHandleSpecialChars = clsSpecialCharSequenceMgr.getDeclaredMethod("handleChars", paramTypes);
//                methodHandleSpecialChars.setAccessible(true);
//            }
//            catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//            catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            }
//            catch (NullPointerException e) {
//                e.printStackTrace();
//            }
//            catch (SecurityException e) {
//                e.printStackTrace();
//            }
//        }
        
           number = s.toString();
//        if (null != methodHandleSpecialChars) { //(SpecialCharSequenceMgr.handleChars(this, number, mNumberEdit)) {
//            Object[] parameters = new Object[] {this, number, mNumberEdit};
//            try {
//                if ((Boolean )methodHandleSpecialChars.invoke(null, parameters)) {
//                    // A special sequence was entered, clear the digits
//                    mNumberEdit.getText().clear();
//                    number = getNumber();
//                }
//            }
//            catch (NullPointerException e) {
//                e.printStackTrace();
//            }
//            catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//            catch (IllegalArgumentException e) {
//                e.printStackTrace();
//            }
//            catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
//        }
        if (SpecialCharSequenceMgr.handleChars(mContentView.getContext(), number, mNumberEdit)) {
            // A special sequence was entered, clear the digits
            mNumberEdit.getText().clear();
            number = getNumber();
        }
        
        
        if (TextUtils.isEmpty(number)) {
            mOwner.categorizeCallLog(CALLLOG_TYPE_ALL);
            mDialKey.setText(R.string.dialpad_call);
        } else {
            mHandler.removeCallbacks(queryLocationRunable);
            // get location string.
            mHandler.postDelayed(queryLocationRunable,QUERY_LOCATION_DELAY);
        }

        //Added by GanFeng 20120203, should show the number when the SpecialCharSequenceMgr fill the mNumberEdit
        if (!TextUtils.isEmpty(number)
                && ((null != mNumberParent) && (View.VISIBLE != mNumberParent.getVisibility()))) {
            mNumberParent.setVisibility(View.VISIBLE);
        }
    }

   Runnable queryLocationRunable = new Runnable() {
	 public void run() {
	 	if(!TextUtils.isEmpty(number)) {
		 	mOwner.filterNumber(number);
			location = LocationUtil.getPhoneLocation(mContentView.getContext(), number);
			if (TextUtils.isEmpty(location)) {
				mDialKey.setText(R.string.dialpad_call);
			} else {
				mDialKey.setText(location);
			}
	 	}
	}
 };
   
    public void onClick(View v) {
        //vibrate();
        //playKeyBeep(v);
        switch (v.getId()) {
            case R.id.key_1:
            case R.id.key_2:
            case R.id.key_3:
            case R.id.key_4:
            case R.id.key_5:
            case R.id.key_6:
            case R.id.key_7:
            case R.id.key_8:
            case R.id.key_9:
            case R.id.key_star:
            case R.id.key_0:
            case R.id.key_hash: {
                vibrate();
                playKeyBeep(v);
                int keyCode = ((Integer)v.getTag()).intValue();
                pressKey(keyCode);
                break;
            }

            case R.id.edt_dpkb_call_number:
                mNumberEdit.setFocusable(true);
                mNumberEdit.setFocusableInTouchMode(true);
                mNumberEdit.requestFocus();
                mOwner.showDialpad(true);
                break;

            case R.id.btn_dpkb_call: {
                String number = getNumber();
                if (!TextUtils.isEmpty(number)) {
                    mOwner.call(number);
                    setNumber(null);
                    showNumberUI();
                }
                else {
                    number = mOwner.getLastDialedNumber();
                    if (!TextUtils.isEmpty(number)) {
                        setNumber(number);
                        show();
                    }
                }
                break;
            }

            /*case R.id.btn_dpkb_enter_contact:
                mOwner.enterContacts();
                break;*/

            case R.id.btn_dpkb_backspace:
                pressKey(KeyEvent.KEYCODE_DEL);
                break;

            case R.id.btn_dp_open_dialpad:
            case R.id.btn_dpkb_showhide_keyboard: {
                View dialKeys = mContentView.findViewById(R.id.dial_num_keys);
                if (View.VISIBLE == dialKeys.getVisibility()) {
                    mOwner.showDialpad(false);
                }
                else {
                    mOwner.showDialpad(true);
                }
                break;
            }
            
            case R.id.btn_search:
                openSearcher();
                break;
                
            case R.id.btn_filter_calllog:
                mCategorySoftBar.setVisibility(View.VISIBLE);
                mMainSoftBar.setVisibility(View.GONE);
                break;

            case R.id.btn_categorize_all_calllog:
                categorizeCallLog(CALLLOG_TYPE_ALL);
                break;
                
            case R.id.btn_categorize_out_calllog:
                categorizeCallLog(Calls.OUTGOING_TYPE);
                break;
                
            case R.id.btn_categorize_in_calllog:
                categorizeCallLog(Calls.INCOMING_TYPE);
                break;
                
            case R.id.btn_categorize_miss_calllog:
                categorizeCallLog(Calls.MISSED_TYPE);
                break;

            case R.id.addcontact: {
                    String number = getNumber();
                    mOwner.newContact(number);
                }
                break;
            
            case R.id.joincontact: {
                    String number = getNumber();
                    mOwner.createContact(number);
                }
                break;

            case R.id.sendsms: {
                    String number = getNumber();
                    mOwner.sendMessage(number);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        int viewId = v.getId();
        if (R.id.key_0 == viewId) {
            pressKey(KeyEvent.KEYCODE_PLUS);
            return true;
        }
        else if (R.id.btn_dpkb_backspace == viewId) {
            setNumber(null);
            View dialKeys = mContentView.findViewById(R.id.dial_num_keys);
            if (View.GONE == dialKeys.getVisibility()) {
                View dialMainView = mContentView.findViewById(R.id.ll_dial_main_view);
                if (View.GONE != dialMainView.getVisibility()) {
                    dialMainView.setVisibility(View.GONE);
                }
                showDialpadSoftbar(false);
            }
            else {
                showNumberUI();
            }
            return true;
        }
        //else if (R.id.edt_dpkb_call_number == viewId) {
        	//mOwner.createClipboard(); //add by piero 2012.6.18
           // return true;
        //}
        
        return false;
    }

    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG, "onTouch: viewId=" + v.getId());
        return false;
    }

    private void pressKey(int keyCode) {
        KeyEvent keyEvt = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        mNumberEdit.onKeyDown(keyCode, keyEvt);
        mNumberEdit.onKeyUp(keyCode, keyEvt);
        if (KeyEvent.KEYCODE_DEL == keyCode) {
            mTimeStampWhenPress = System.currentTimeMillis();
            
            String number = getNumber();
            if (TextUtils.isEmpty(number)) {
                View dialKeys = mContentView.findViewById(R.id.dial_num_keys);
                if (View.GONE == dialKeys.getVisibility()) {
                    View dialMainView = mContentView.findViewById(R.id.ll_dial_main_view);
                    if (View.GONE != dialMainView.getVisibility()) {
                        dialMainView.setVisibility(View.GONE);
                    }
                    showDialpadSoftbar(false);
                }
                else {
                    showNumberUI();
                }
            }
        }
        else {
            showNumberUI();
        }
    }

    public String getNumber() {
        return mNumberEdit.getText().toString();
    }


    public void setNumber(String number) {
	 if (number != null) {
		//add by zenghuaying fix bug #9942 
		char[] numChar = number.toCharArray();
		StringBuffer trimNumber = new StringBuffer();
		for(int i=0;i<numChar.length;i++){
			char num = numChar[i];
			if(num > 47 && num < 58){
				trimNumber.append(num);
			}
			
		}
		//add end
		// delete "//" invalid char  , add by shenqi
	 	//String trimNumber = number.trim().replace("//","");
        if (trimNumber != null ) { 			
          int tailPos = trimNumber.length();
          mNumberEdit.setText(trimNumber);
          mNumberEdit.setSelection(tailPos, tailPos);
        }	
 	 }
	 else {
	 	mNumberEdit.setText(null);
 	 }
    }

     public void stopQueryLocation() {
		mHandler.removeCallbacks(queryLocationRunable);
     }
	 
    public void show() {
        View dialMainView = mContentView.findViewById(R.id.ll_dial_main_view);
        if (View.VISIBLE != dialMainView.getVisibility()) {
            dialMainView.setVisibility(View.VISIBLE);
        }
        
        showNumberUI();
        showDialpadSoftbar(true);
        
        View dialKeys = mContentView.findViewById(R.id.dial_num_keys);
        if (View.VISIBLE != dialKeys.getVisibility()) {
            dialKeys.setVisibility(View.VISIBLE);
        }

        ImageView btnShowHide = (ImageView )mContentView.findViewById(R.id.btn_dpkb_showhide_keyboard);
        if (null != btnShowHide) {
            //btnShowHide.setImageResource(R.drawable.ic_hide_dialpad_normal);
            btnShowHide.setBackgroundResource(R.drawable.bg_hide_dialpad_s);
        }
    }

    public void hide() {
        //showNumberUI();
        if (TextUtils.isEmpty(getNumber())) {
            View dialMainView = mContentView.findViewById(R.id.ll_dial_main_view);
            if (View.GONE != dialMainView.getVisibility()) {
                dialMainView.setVisibility(View.GONE);
            }
        }
        
        showDialpadSoftbar(false);
        View dialKeys = mContentView.findViewById(R.id.dial_num_keys);
        if (View.GONE != dialKeys.getVisibility()) {
            dialKeys.setVisibility(View.GONE);
        }

        ImageView btnShowHide = (ImageView )mContentView.findViewById(R.id.btn_dpkb_showhide_keyboard);
        if (null != btnShowHide) {
            //btnShowHide.setImageResource(R.drawable.ic_show_dialpad_normal);
            btnShowHide.setBackgroundResource(R.drawable.bg_show_dialpad_s);
        }
    }

    public void showDialpadSoftbar(boolean bShowDialpad) {
        if (null != mCategorySoftBar) {
            mCategorySoftBar.setVisibility(View.GONE);
        }

        if (bShowDialpad || !TextUtils.isEmpty(getNumber())) {
            View dialpad_key = mContentView.findViewById(R.id.dp_dialpad_softkey_bar);
            if (View.VISIBLE != dialpad_key.getVisibility()) {
                dialpad_key.setVisibility(View.VISIBLE);
            }
            
            View white_speratorline = mContentView.findViewById(R.id.dp_dialpad_bar_top_separator);
            if (View.VISIBLE != white_speratorline.getVisibility()) {
                white_speratorline.setVisibility(View.GONE);
            }
                        
            View nodialpad_key = mContentView.findViewById(R.id.dp_nodialpad_softkey_bar);
            if (View.GONE != nodialpad_key.getVisibility()) {
                nodialpad_key.setVisibility(View.GONE);
            }
        } else {
            View dialpad_key = mContentView.findViewById(R.id.dp_dialpad_softkey_bar);
            if (View.GONE != dialpad_key.getVisibility()) {
                dialpad_key.setVisibility(View.GONE);
            }

            View white_speratorline = mContentView.findViewById(R.id.dp_dialpad_bar_top_separator);
            if (View.GONE != white_speratorline.getVisibility()) {
                white_speratorline.setVisibility(View.GONE);
            }

            View nodialpad_key = mContentView.findViewById(R.id.dp_nodialpad_softkey_bar);
            if (View.VISIBLE != nodialpad_key.getVisibility()) {
                nodialpad_key.setVisibility(View.VISIBLE);
            }        
        }
    }
    
    public void showNumberUI() {
        if (TextUtils.isEmpty(getNumber())) {
            if (View.GONE != mNumberParent.getVisibility()) {
                mNumberParent.setVisibility(View.GONE);
            }
        }
        else {
            if (View.VISIBLE != mNumberParent.getVisibility()) {
                mNumberParent.setVisibility(View.VISIBLE);
            }
        }
    }

    public long getTimeStamp() {
        return mTimeStampWhenPress;
    }

    private void initSetting() {
        context = mContentView.getContext();
        resolver = context.getContentResolver();
        //boolean soundOn   = ((1 == Settings.System.getInt(resolver, Settings.System.DTMF_TONE_WHEN_DIALING, 1))
        //        && (0 == Settings.System.getInt(resolver, Settings.System.SOUND_EFFECTS_ENABLED, 0)));
        vibarteOn = (1 == Settings.System.getInt(resolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1));
        mSoundOn = (1 == Settings.System.getInt(resolver, Settings.System.DTMF_TONE_WHEN_DIALING, 1));
        
        mTone = new ToneGenerator(AudioManager.STREAM_SYSTEM, 80);

       mVibrator = (Vibrator )context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void playKeyBeep(View view) {
	 mSoundOn = (1 == Settings.System.getInt(resolver, Settings.System.DTMF_TONE_WHEN_DIALING, 1));
        if ((null == mTone) || (null == view) || (null == view.getTag()) || !mSoundOn) {
            return;
        }

        int keyTag = ((Integer )view.getTag()).intValue();
        switch (keyTag) {
            case KeyEvent.KEYCODE_0:
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
            case KeyEvent.KEYCODE_9:
            case KeyEvent.KEYCODE_STAR:
            case KeyEvent.KEYCODE_POUND:
                mTone.startTone((ToneGenerator.TONE_DTMF_0 + (keyTag - KeyEvent.KEYCODE_0)), 100);
                break;

            default:
                mTone.startTone(ToneGenerator.TONE_DTMF_5, 100);
                break;
        }
    }

    private void vibrate() {
	 vibarteOn = (1 == Settings.System.getInt(resolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1));
        if (vibarteOn && null != mVibrator) {
            long[] vibratePattern = new long[] {1L, 20L, 21L};
            mVibrator.cancel();
            mVibrator.vibrate(vibratePattern, -1);
        }
    }
    private void showNodialpadbar() {
        if (null != mMainSoftBar) {
            mMainSoftBar.setVisibility(View.VISIBLE);
        }

        if (null != mCategorySoftBar) {
            mCategorySoftBar.setVisibility(View.GONE);
        }
    }

    private void categorizeCallLog(int logType) {
        mOwner.categorizeCallLog(logType);
        mViewType = logType;
        showNodialpadbar();
    }

    private void openSearcher() {
        mOwner.openSearcher();
        resetToInitialStatus();
    }
    
    public void resetToInitialStatus() {
        Log.e(TAG, "resetToInitialStatus");
        if ((CALLLOG_TYPE_ALL != mViewType)
                || ((null != mMainSoftBar) && (View.VISIBLE != mMainSoftBar.getVisibility()))){
            categorizeCallLog(CALLLOG_TYPE_ALL);
        }
    }

    public static abstract interface DialpadOwner {
        public abstract void call(String number);
        public abstract void enterContacts();
        public abstract void createContact(String number);
        public abstract void newContact(String number);
        public abstract void sendMessage(String number);
        public abstract void filterNumber(String number);
        public abstract void showDialpad(boolean show);
        public abstract String getLastDialedNumber();
        public void categorizeCallLog(int logType);
        public void openSearcher();
        public void loadAllContacts(); // called by beforetext changed.
        //add by piero zeng 2012.6.18
        //register mNumberEdit to Menu
        public void registerContextMenu(View view);
    }
}
