package com.lewa.spm;

import com.lewa.spm.device.BatteryInfo;
import com.lewa.spm.device.DevStatus;
import com.lewa.spm.device.SwitchManager;
import com.lewa.spm.element.ConsumeValue;
import com.lewa.spm.service.MonitorService;
import com.lewa.spm.utils.CalcUtils;
import com.lewa.spm.utils.ModeUtils;
import com.lewa.spm.utils.PrefUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LewaCheckBox;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SPMActivity extends Activity {
	/** Called when the activity is first created. */

	private final String TAG = "SPMActivity";

	public final int MENU_TIME = Menu.FIRST;
	public final int MENU_POWER = Menu.FIRST + 1;

	public static SPMActivity mSPMActivity;

	private TextView lift_time_text_show, 
					next_time_hour_num_show,
					next_time_hour_text_show, 
					next_time_min_num_show,
					next_time_min_text_show;

	private final int CHECK_DATA = 0;
	private final int CHECK_WLAN = 1;
	private final int CHECK_BT = 2;
	private final int CHECK_GPS = 3;
	private final int CHECK_FEEL = 4;

	private final int WINDOW_BRIGHTNESS = 0;
	private final int WINDOW_TIMEOUT = 1;
	private final int WINDOW_ALERT_TIME = 2;
	private final int WINDOW_ALERT_POWER = 3;
	private final int WINDOW_OPTIMIZED_NORMAL = 4;
	private final int WINDOW_OPTIMIZED_SUPER = 5;
	
	private final int CLICK_PIC_NORMAL = 0;
	private final int CLICK_PIC_SUPER = 1;
	private final int CURRENT_POWER_LEVEL = 20;
	
	private ImageView ivSuperman;
	private Button optBtn;

	private LewaCheckBox cbData, cbWlan, cbBluetooth, cbGps,cbTouch;
	private TextView screen_light_text, lock_screen_text;
	
    private static final int MINIMUM_BACKLIGHT = 30;
    private static final int MAXIMUM_BACKLIGHT = 225;

    private RelativeLayout rlTopButton;
    private RelativeLayout rlFeedback,rlData,rlWlan,rlBt,rlTimeout,rlLight,rlGps;    
    
	BroadcastReceiver mReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ConsumeValue.ACTION_UPDATE_UI_ENERGY)) {				
				updateUITitle(false,PrefUtils.getInstance(SPMActivity.this).getCurRunningMode(),false);
			} else if (intent.getAction().equals(ConsumeValue.ACTION_UPDATE_UI_INTELS_TIME) || intent.getAction().equals(ConsumeValue.ACTION_UPDATE_UI_INTELS_POWER)) {
				
				updateUITitle(true,ConsumeValue.MODE_AIR,false);
				updateUIList();
				
				if(intent.getAction().equals(ConsumeValue.ACTION_UPDATE_UI_INTELS_POWER)
						&&!PrefUtils.getInstance(SPMActivity.this).getIntelPowerAlertCheck()){
					showDialog(WINDOW_ALERT_POWER);
				}else if(intent.getAction().equals(ConsumeValue.ACTION_UPDATE_UI_INTELS_TIME)
						&& !PrefUtils.getInstance(SPMActivity.this).getIntelTimeAlertCheck()){
					Bundle mBundle = intent.getExtras();
					boolean mBoolean = mBundle.getBoolean(ConsumeValue.PARAM_INTEL_TIME_START);
					if(mBoolean) {
						showDialog(WINDOW_ALERT_TIME);
					}
				}
				
			}
		};
	};

	Handler mColorHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what > 0) {
				if(msg.what!=1){//TODO fix the +1 minute bug,but we should check the reason.
					lift_time_text_show.setVisibility(TextView.VISIBLE);
					lift_time_text_show.setTextColor(Color.GREEN);
					lift_time_text_show.setText("+" + String.valueOf(msg.what)
							+ getString(R.string.spm_minute));
				}
			} else if (msg.what < 0) {
				lift_time_text_show.setVisibility(TextView.VISIBLE);
				lift_time_text_show.setTextColor(Color.RED);
				lift_time_text_show.setText(String.valueOf(msg.what)
						+ getString(R.string.spm_minute));
			} else {
				lift_time_text_show.setVisibility(TextView.INVISIBLE);
			}
			super.handleMessage(msg);
		}
	};

	Handler mSwitchHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CHECK_DATA:
				cbData.setEnabled(false);
				SwitchManager.getInstance(SPMActivity.this).mobileDataSwitch(cbData.isChecked());
				mStatusHandler.sendEmptyMessage(CHECK_DATA);
				break;
			case CHECK_WLAN:
				cbWlan.setEnabled(false);				
				SwitchManager.getInstance(SPMActivity.this).wifiSwitch(cbWlan.isChecked());
				mStatusHandler.sendEmptyMessage(CHECK_WLAN);
				break;
			case CHECK_BT:
				cbBluetooth.setEnabled(false);				
				SwitchManager.getInstance(SPMActivity.this).btSwitch(cbBluetooth.isChecked());
				mStatusHandler.sendEmptyMessage(CHECK_BT);
				break;
			case CHECK_GPS:
				cbGps.setEnabled(false);
				SwitchManager.getInstance(SPMActivity.this).gpsSwitch(cbGps.isChecked());
				mStatusHandler.sendEmptyMessage(CHECK_GPS);
				break;
			case CHECK_FEEL:
				cbTouch.setEnabled(false);
				SwitchManager.getInstance(SPMActivity.this).setHapticFb(cbTouch.isChecked());
				mStatusHandler.sendEmptyMessage(CHECK_FEEL);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	Handler mStatusHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CHECK_DATA:
				if ((cbData.isChecked() == DevStatus.getInstance(SPMActivity.this).getMobileDataStatus())) {
					updateUITitle(true, ConsumeValue.MODE_NORMAL,false);//TODO it should be false???
					cbData.setEnabled(true);
					PrefUtils.getInstance(SPMActivity.this).setCurRunningMode(ConsumeValue.MODE_NORMAL);					
				} else {
					mStatusHandler.sendEmptyMessageDelayed(CHECK_DATA, 100);
				}
				break;
			case CHECK_WLAN:
				if (cbWlan.isChecked() == DevStatus.getInstance(SPMActivity.this).getWifiStatus()) {
					updateUITitle(true, ConsumeValue.MODE_NORMAL,false);
					cbWlan.setEnabled(true);
					PrefUtils.getInstance(SPMActivity.this).setCurRunningMode(ConsumeValue.MODE_NORMAL);
				} else {					
					mStatusHandler.sendEmptyMessageDelayed(CHECK_WLAN, 500);
				}
				break;
			case CHECK_BT:
				if (cbBluetooth.isChecked()	== DevStatus.getInstance(SPMActivity.this).getBTStatus()) {					
					updateUITitle(true, ConsumeValue.MODE_NORMAL,false);
					cbBluetooth.setEnabled(true);
					PrefUtils.getInstance(SPMActivity.this).setCurRunningMode(ConsumeValue.MODE_NORMAL);					
				} else {					
					mStatusHandler.sendEmptyMessageDelayed(CHECK_BT, 1000);					
				}
				break;
			case CHECK_GPS:
				if (cbGps.isChecked() == DevStatus.getInstance(SPMActivity.this).getGpsStatus()) {
					updateUITitle(true, ConsumeValue.MODE_NORMAL,false);
					cbGps.setEnabled(true);
					PrefUtils.getInstance(SPMActivity.this).setCurRunningMode(ConsumeValue.MODE_NORMAL);					
				} else {
					mStatusHandler.sendEmptyMessageDelayed(CHECK_GPS, 500);
				}
				break;
			case CHECK_FEEL:
				if (cbTouch.isChecked()	== DevStatus.getInstance(SPMActivity.this).getHapticFb()) {
					updateUITitle(true, ConsumeValue.MODE_NORMAL,false);
					cbTouch.setEnabled(true);
					PrefUtils.getInstance(SPMActivity.this).setCurRunningMode(ConsumeValue.MODE_NORMAL);					
				} else {
					mStatusHandler.sendEmptyMessageDelayed(CHECK_FEEL, 500);
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);        
		setContentView(R.layout.main);
		
		mSPMActivity = this;
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConsumeValue.ACTION_UPDATE_UI_ENERGY);
		filter.addAction(ConsumeValue.ACTION_UPDATE_UI_INTELS_TIME);
		filter.addAction(ConsumeValue.ACTION_UPDATE_UI_INTELS_POWER);
		registerReceiver(mReceiver, filter);

		initUI();
		updateUITitle(false, ConsumeValue.MODE_NORMAL,false);
		updateUIList();

		this.startService(new Intent(this, MonitorService.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuItem timeItem = menu.add(0, MENU_TIME, 0, getString(R.string.spm_change_mode_by_time_title));		
		timeItem.setIcon(R.drawable.spm_ic_menu_time);
		
		MenuItem powerItem = menu.add(0, MENU_POWER, 1, getString(R.string.spm_change_mode_by_power_title));
		powerItem.setIcon(R.drawable.spm_ic_menu_low_battery);
		
		return super.onCreateOptionsMenu(menu);
	}

	private void initUI() {
		// Set Title
		lift_time_text_show = (TextView) findViewById(R.id.life_time_text_show);

		next_time_hour_num_show = (TextView) findViewById(R.id.next_time_hour_num_show);
		next_time_hour_text_show = (TextView) findViewById(R.id.next_time_hour_text_show);
		next_time_min_num_show = (TextView) findViewById(R.id.next_time_min_num_show);
		next_time_min_text_show = (TextView) findViewById(R.id.next_time_min_text_show);

		rlTopButton = (RelativeLayout)findViewById(R.id.button_relativelayout);
		ivSuperman = (ImageView)findViewById(R.id.spm_power_superman);
		
		// Set Button state
		optBtn = (Button) findViewById(R.id.spm_power_button);
		if(PrefUtils.getInstance(this).getOptBtnNormalState()){
			optBtn.setText(getString(R.string.spm_power_normal_save_mode));			
			clickPicTrans(CLICK_PIC_NORMAL);
			optBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.spm_bt_energy_conservation));
		}else{
			optBtn.setText(getString(R.string.spm_power_super_save_mode));			
			clickPicTrans(CLICK_PIC_SUPER);			
			optBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.lewa_bt_energy_conservation_pressed01));
		}
		
		optBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (PrefUtils.getInstance(SPMActivity.this).getOptBtnNormalState()){
					showDialog(WINDOW_OPTIMIZED_SUPER);
				}else{
					showDialog(WINDOW_OPTIMIZED_NORMAL);
				}		
			}
		});
		
		optBtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(arg1.getAction() != MotionEvent.ACTION_UP){
					if (Integer.parseInt(BatteryInfo.getInformation(BatteryInfo.battCapacity)) < CURRENT_POWER_LEVEL){
						ivSuperman.setBackgroundDrawable(getResources().getDrawable(R.drawable.lw_low_power_highlide));
					}else{
						ivSuperman.setBackgroundDrawable(getResources().getDrawable(R.drawable.lw_high_power_highlide));
					}
				}
				return false;				
			}
		});
		
		/*
		 * Set button click event
		 */
		cbData = (LewaCheckBox)findViewById(R.id.data_choise_check);
		cbData.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mSwitchHandler.sendEmptyMessage(CHECK_DATA);				
			}
		});
		cbData.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(cbData.isChecked()){
					Toast.makeText(SPMActivity.this, getString(R.string.spm_toast_data_alert), Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		cbWlan = (LewaCheckBox) findViewById(R.id.wlan_choise_check);
		cbWlan.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mSwitchHandler.sendEmptyMessage(CHECK_WLAN);				
			}
		});
		cbWlan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(cbWlan.isChecked()){
					Toast.makeText(SPMActivity.this, getString(R.string.spm_toast_wlan_alert), Toast.LENGTH_SHORT).show();
				}
			}
		});

		cbBluetooth = (LewaCheckBox) findViewById(R.id.bt_choise_check);
		cbBluetooth.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mSwitchHandler.sendEmptyMessage(CHECK_BT);				
			}
		});
		cbBluetooth.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(cbBluetooth.isChecked()){
					Toast.makeText(SPMActivity.this, getString(R.string.spm_toast_bluetooth_alert), Toast.LENGTH_SHORT).show();
				}
			}
		});

		screen_light_text = (TextView) findViewById(R.id.screen_light_choise_value);	
		lock_screen_text = (TextView) findViewById(R.id.lock_screen_choise_value);

		cbGps = (LewaCheckBox) findViewById(R.id.gps_choise_check);
		cbGps.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mSwitchHandler.sendEmptyMessage(CHECK_GPS);				
			}
		});
		cbGps.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(cbGps.isChecked()){
					Toast.makeText(SPMActivity.this, getString(R.string.spm_toast_gps_alert), Toast.LENGTH_SHORT).show();
				}
			}
		});

		cbTouch = (LewaCheckBox) findViewById(R.id.touch_choise_check);
		cbTouch.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mSwitchHandler.sendEmptyMessage(CHECK_FEEL);				
			}
		});
		cbTouch.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(cbTouch.isChecked()){
					Toast.makeText(SPMActivity.this, getString(R.string.spm_toast_touch_alert), Toast.LENGTH_SHORT).show();
				}				
			}
		});
		
		/*
		 * Set line click event
		 */
		
		rlData = (RelativeLayout)findViewById(R.id.relativeLayoutData);
		rlData.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				cbData.setChecked(!cbData.isChecked());
				if(cbData.isChecked()){
					Toast.makeText(SPMActivity.this, getString(R.string.spm_toast_data_alert), Toast.LENGTH_SHORT).show();
				}
				mSwitchHandler.sendEmptyMessage(CHECK_DATA);				
			}
		});
		
		rlWlan = (RelativeLayout)findViewById(R.id.relativeLayoutWlan);
		rlWlan.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				cbWlan.setChecked(!cbWlan.isChecked());
				if(cbWlan.isChecked()){
					Toast.makeText(SPMActivity.this, getString(R.string.spm_toast_wlan_alert), Toast.LENGTH_SHORT).show();
				}
				mSwitchHandler.sendEmptyMessage(CHECK_WLAN);				
			}
		});
		
		rlBt = (RelativeLayout)findViewById(R.id.relativeLayoutBluetooth);
		rlBt.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				cbBluetooth.setChecked(!cbBluetooth.isChecked());
				if(cbBluetooth.isChecked()){
					Toast.makeText(SPMActivity.this, getString(R.string.spm_toast_bluetooth_alert), Toast.LENGTH_SHORT).show();
				}
				mSwitchHandler.sendEmptyMessage(CHECK_BT);				
			}
		});
		
		rlTimeout = (RelativeLayout)findViewById(R.id.relativeLayoutTimeout);
		rlTimeout.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {				
				showDialog(WINDOW_TIMEOUT);				
			}
		});
		
		rlLight = (RelativeLayout)findViewById(R.id.relativeLayoutLight);
		rlLight.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				showDialog(WINDOW_BRIGHTNESS);				
			}
		});
		
		rlGps = (RelativeLayout)findViewById(R.id.relativeLayoutGps);
		rlGps.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				cbGps.setChecked(!cbGps.isChecked());
				if(cbGps.isChecked()){
					Toast.makeText(SPMActivity.this, getString(R.string.spm_toast_gps_alert), Toast.LENGTH_SHORT).show();
				}
				mSwitchHandler.sendEmptyMessage(CHECK_GPS);
			}
		});
		
		rlFeedback = (RelativeLayout)findViewById(R.id.relativeLayoutFeedback);
		rlFeedback.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				cbTouch.setChecked(!cbTouch.isChecked());
				if(cbTouch.isChecked()){
					Toast.makeText(SPMActivity.this, getString(R.string.spm_toast_touch_alert), Toast.LENGTH_SHORT).show();
				}
				mSwitchHandler.sendEmptyMessage(CHECK_FEEL);
			}
		});
	}

	protected void clickPicTrans(int clickFlag) {
		switch (clickFlag) {
		case CLICK_PIC_SUPER:
			ivSuperman.setBackgroundDrawable(getResources().getDrawable(R.drawable.lw_power_selected));
			break;
		case CLICK_PIC_NORMAL:
			if (Integer.parseInt(BatteryInfo.getInformation(BatteryInfo.battCapacity)) > CURRENT_POWER_LEVEL){
				ivSuperman.setBackgroundDrawable(getResources().getDrawable(R.drawable.lw_high_power_animation));
			}else{
				ivSuperman.setBackgroundDrawable(getResources().getDrawable(R.drawable.lw_low_power_animation));
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @param anim
	 *            if the animation should be shown for title.
	 * @param toModeNum
	 *            the mode we stand.
	 */
	private void updateUITitle(boolean anim, int toModeNum , boolean alert) {

		int battColorLevel = Integer.parseInt(BatteryInfo
				.getInformation(BatteryInfo.battCapacity)); 
		
		float battLevel = battColorLevel / 100f;

		double lifeValue = CalcUtils.getInstance(this).Lift(toModeNum);
		
		if (anim) {
			double oldValue = PrefUtils.getInstance(this).getOldConsumeValue();
			PrefUtils.getInstance(this).setOldConsumeValue((int) lifeValue);

			double delValue = lifeValue - oldValue;

			int time = ((int) Math.round(delValue * battLevel));
			if (time != 0) {
				mColorHandler.sendEmptyMessage(time);
				mColorHandler.sendEmptyMessageDelayed(0, 2000);
			}
		}
		
		if(battColorLevel<=15){
			next_time_hour_text_show.setTextColor(0xfff46987);			
			next_time_hour_num_show.setTextColor(0xfff46987);
			next_time_min_text_show.setTextColor(0xfff46987);
			next_time_min_num_show.setTextColor(0xfff46987);
		}else if(battColorLevel>=26){
			next_time_hour_text_show.setTextColor(0xff4bb5ef);
			next_time_hour_num_show.setTextColor(0xff4bb5ef);
			next_time_min_text_show.setTextColor(0xff4bb5ef);
			next_time_min_num_show.setTextColor(0xff4bb5ef);
		}else{
			next_time_hour_text_show.setTextColor(0xffeda366);
			next_time_hour_num_show.setTextColor(0xffeda366);
			next_time_min_text_show.setTextColor(0xffeda366);
			next_time_min_num_show.setTextColor(0xffeda366);
		}

		int iHours = CalcUtils.getInstance(this).getHoursFromTime(lifeValue,
				battLevel);
		if (iHours == 0) {
			next_time_hour_text_show.setVisibility(TextView.INVISIBLE);
			next_time_hour_num_show.setVisibility(TextView.INVISIBLE);
		} else {
			next_time_hour_text_show.setVisibility(TextView.VISIBLE);
			next_time_hour_num_show.setVisibility(TextView.VISIBLE);
			next_time_hour_num_show.setText(String.valueOf(iHours));
		}

		int iMinutes = CalcUtils.getInstance(this).getMinutesFromString(
				lifeValue, battLevel);
//		if (iMinutes == 0) {
//			next_time_min_text_show.setVisibility(TextView.INVISIBLE);
//			next_time_min_num_show.setVisibility(TextView.INVISIBLE);
//		} else {
			next_time_min_text_show.setVisibility(TextView.VISIBLE);
			next_time_min_num_show.setVisibility(TextView.VISIBLE);
			if(iMinutes>9)
				next_time_min_num_show.setText(String.valueOf(iMinutes));
			else
				next_time_min_num_show.setText(String.valueOf("0"+iMinutes));
//		}
	}

	/*
	 * private CheckBox data_check,wlan_check, bluetooth_check ,gps_check,
	 * touch_check; private TextView screen_light_text,lock_screen_text;
	 */
	private void updateUIList() {
		cbData.setChecked(DevStatus.getInstance(this).getMobileDataStatus());
		cbWlan.setChecked(DevStatus.getInstance(this).getWifiStatus());
		cbBluetooth.setChecked(DevStatus.getInstance(this).getBTStatus());
		cbGps.setChecked(DevStatus.getInstance(this).getGpsStatus());
		screen_light_text.setText(getBrightScreenFormat());
		lock_screen_text.setText(getLockScreenTimeFormat());
		cbTouch.setChecked(DevStatus.getInstance(this).getHapticFb());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent mIntent = null;
		switch (item.getItemId()) {
		case MENU_TIME:
			mIntent = new Intent(SPMActivity.this, SetTimeActivity.class);
			startActivityForResult(mIntent, ConsumeValue.MODE_INTEL_TIME);
			break;
		case MENU_POWER:
			mIntent = new Intent(SPMActivity.this, SetPowerActivity.class);
			startActivityForResult(mIntent, ConsumeValue.MODE_INTEL_POWER);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private String getLockScreenTimeFormat() {
		int time = DevStatus.getInstance(this).getTimeoutStatus() / 1000;
		if (time < 60){
			return time + getString(R.string.spm_seconds);
		} else {
			return (time / 60) + getString(R.string.spm_minute);
		}
	}

	private String getBrightScreenFormat() {
		if (DevStatus.getInstance(this).isAutoBrightness()) {
			return getString(R.string.spm_auto);
		} else {
			return String.valueOf((int) ((DevStatus.getInstance(this)
					.getBrightStatus() / 255f) * 100)) + "%";
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		Dialog tempDialog = null;
		switch (id) {
		case WINDOW_TIMEOUT:
			tempDialog = createTimeoutDialog();
			break;
		case WINDOW_BRIGHTNESS:
			tempDialog = createBrightnessDialog();
			break;
		case WINDOW_ALERT_TIME:
			tempDialog = createIntelTimeDialog();
			break;
		case WINDOW_ALERT_POWER:
			tempDialog = createIntelPowerDialog();
			break;
		case WINDOW_OPTIMIZED_NORMAL:
		case WINDOW_OPTIMIZED_SUPER:
			tempDialog = createOptimizedDialog(id);
		default:
			break;
		}

		return tempDialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
	}
	
	private Dialog createBrightnessDialog() {

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.brightness_window,
				(ViewGroup) findViewById(R.id.brigheness_parent));

		final CheckBox brightCheckBox = (CheckBox) layout.findViewById(R.id.automatic_mode);
		final SeekBar brightSeekBar = (SeekBar) layout.findViewById(R.id.seekbar);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		
		builder.setTitle(getString(R.string.spm_screen_light_show))
				.setPositiveButton(getString(R.string.btn_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								screen_light_text.setText(getBrightScreenFormat());								
								if(!brightCheckBox.isChecked()){
									SwitchManager.getInstance(SPMActivity.this).
										brightSwitch(brightSeekBar.getProgress()+MINIMUM_BACKLIGHT);
								}else{
									SwitchManager.getInstance(SPMActivity.this).setAutoBrightness(true);
								}
								removeDialog(WINDOW_BRIGHTNESS);
							}
						})
				.setNegativeButton(getString(R.string.btn_cancel), 
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								removeDialog(WINDOW_BRIGHTNESS);
							}
						});
		
		

		
		OnSeekBarChangeListener yourSeekBarListener = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// NA
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// NA
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBark, int progress,
					boolean fromUser) {
				SwitchManager.getInstance(SPMActivity.this).brightSwitch(progress+MINIMUM_BACKLIGHT);
			}
		};
		brightSeekBar.setMax(MAXIMUM_BACKLIGHT);
		brightSeekBar.setOnSeekBarChangeListener(yourSeekBarListener);
		
		brightCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					brightSeekBar.setVisibility(View.GONE);					
				} else {					
					brightSeekBar.setVisibility(View.VISIBLE);
					brightSeekBar.setProgress(DevStatus.getInstance(SPMActivity.this).getBrightStatus()-MINIMUM_BACKLIGHT);
				}
				SwitchManager.getInstance(SPMActivity.this).setAutoBrightness(isChecked);
			}
		});
		
		//init value
		if(DevStatus.getInstance(this).isAutoBrightness()) {
			brightCheckBox.setChecked(true);
			brightSeekBar.setVisibility(View.GONE);
		} else {
			brightCheckBox.setChecked(false);
			brightSeekBar.setVisibility(View.VISIBLE);
			brightSeekBar.setProgress(DevStatus.getInstance(this).getBrightStatus()-MINIMUM_BACKLIGHT);
		}
		
		AlertDialog alert = builder.create();
		return alert;
	}

	private Dialog createTimeoutDialog() {

		final String[] items = this.getResources().getStringArray(
				R.array.screen_timeout_entries);
		final String[] values = this.getResources().getStringArray(
				R.array.screen_timeout_values);

		int defValue = DevStatus.getInstance(this).getTimeoutStatus();
		int indexValue = -1;

		for (int i = 0; i < values.length; i++) {
			if (Integer.valueOf(values[i]) == defValue) {
				indexValue = i;				
				break;
			} else {
				continue;
			}
		}

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(this.getString(R.string.spm_screen_timeout_show));

		builder.setSingleChoiceItems(items, indexValue,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						SwitchManager.getInstance(SPMActivity.this)
								.timeoutSwitch(Integer.valueOf(values[item]));
						lock_screen_text.setText(getLockScreenTimeFormat());
						removeDialog(WINDOW_TIMEOUT);
					}
				});

		builder.setNegativeButton(getString(R.string.btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						removeDialog(WINDOW_TIMEOUT);
					}
				});

		AlertDialog alert = builder.create();

		return alert;
	}
	
	private Dialog createIntelTimeDialog(){
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.alert_window,(ViewGroup) findViewById(R.id.alert_parent));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		
		final CheckBox alertCheckBox = (CheckBox) layout.findViewById(R.id.cb_alert_choise);
		alertCheckBox.setChecked(PrefUtils.getInstance(this).getIntelTimeAlertCheck());
		alertCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				PrefUtils.getInstance(SPMActivity.this).setIntelTimeAlertCheck(isChecked);
			}
		});		
		
		TextView alertTextView = (TextView)layout.findViewById(R.id.tv_alert_info);
		
		alertTextView.setText(
				getString(R.string.spm_alert_time_prefix)+
				PrefUtils.getInstance(this).getIntelTimeFrom()+"-"+
				PrefUtils.getInstance(this).getIntelTimeTo()+
				getString(R.string.spm_alert_time_postfix));
		
		builder.setTitle(getString(R.string.spm_alert_time_title))
				.setPositiveButton(getString(R.string.spm_alert_button_left),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								PrefUtils.getInstance(SPMActivity.this).setIntelTimeAlertCheck(alertCheckBox.isChecked());														    	
						    	startActivityForResult(new Intent(SPMActivity.this, SetTimeActivity.class),0);
								dialog.dismiss();
							}
						})
				.setNegativeButton(getString(R.string.spm_alert_button_right), 
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		
		AlertDialog alert = builder.create();
		
		return alert;
	}
	
	private Dialog createIntelPowerDialog(){
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.alert_window,(ViewGroup)findViewById(R.id.alert_parent));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		
		final CheckBox alertCheckBox = (CheckBox) layout.findViewById(R.id.cb_alert_choise);
		alertCheckBox.setChecked(PrefUtils.getInstance(this).getIntelPowerAlertCheck());
		alertCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				PrefUtils.getInstance(SPMActivity.this).setIntelPowerAlertCheck(isChecked);
			}
		});
		
		TextView alertTextView = (TextView)layout.findViewById(R.id.tv_alert_info);
		alertTextView.setText(
				getString(R.string.spm_alert_power_prefix)+
				PrefUtils.getInstance(this).getIntelLowPower()+
				getString(R.string.spm_alert_power_postfix));
		
		builder.setTitle(getString(R.string.spm_alert_power_title))
				.setPositiveButton(getString(R.string.spm_alert_button_left),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								PrefUtils.getInstance(SPMActivity.this).setIntelPowerAlertCheck(alertCheckBox.isChecked());								
								startActivityForResult(new Intent(SPMActivity.this, SetPowerActivity.class),1);
								dialog.dismiss();
							}
						})
				.setNegativeButton(getString(R.string.spm_alert_button_right),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id){
								dialog.cancel();
							}
						});
		
		AlertDialog alert = builder.create();
		
		return alert;
	}
	
	private Dialog createOptimizedDialog(final int toMode){
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.optimized_window,(ViewGroup)findViewById(R.id.alert_parent));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		
		TextView optTimeTextView = (TextView) layout.findViewById(R.id.tv_opt_postfix_text);
		//TODO set opt time,need fetch the time?YES
		optTimeTextView.setText(calcLifeChange(ConsumeValue.MODE_AIR)+getString(R.string.spm_minute));		
		optTimeTextView.setTextColor(Color.GREEN);
		
		if(toMode==WINDOW_OPTIMIZED_NORMAL){
			TextView optAlertDataText = (TextView) layout.findViewById(R.id.tv_opt_data_text);
			TextView optAlertDataValue = (TextView) layout.findViewById(R.id.tv_opt_data_value);
			if(ModeUtils.getInstance(SPMActivity.this).maskNormalModeList(ConsumeValue.OPT_DATA_ID)){				
				optAlertDataText.setVisibility(View.VISIBLE);				
				optAlertDataValue.setVisibility(View.VISIBLE);
				optAlertDataValue.setText(getString(R.string.spm_opt_alert_value_on));
			}else{
				optAlertDataText.setVisibility(View.GONE);				
				optAlertDataValue.setVisibility(View.GONE);
			}
			
			TextView optAlertWlanText = (TextView) layout.findViewById(R.id.tv_opt_wlan_text);
			TextView optAlertWlanValue = (TextView) layout.findViewById(R.id.tv_opt_wlan_value);
			if(ModeUtils.getInstance(SPMActivity.this).maskNormalModeList(ConsumeValue.OPT_WLAN_ID)){				
				optAlertWlanText.setVisibility(View.VISIBLE);				
				optAlertWlanValue.setVisibility(View.VISIBLE);
				optAlertWlanValue.setText(getString(R.string.spm_opt_alert_value_off));
			}else{
				optAlertWlanText.setVisibility(View.GONE);				
				optAlertWlanValue.setVisibility(View.GONE);
			}
			
			TextView optAlertBluetoothText = (TextView) layout.findViewById(R.id.tv_opt_bluetooth_text);
			TextView optAlertBluetoothValue = (TextView) layout.findViewById(R.id.tv_opt_bluetooth_value);
			if(ModeUtils.getInstance(SPMActivity.this).maskNormalModeList(ConsumeValue.OPT_BLUETOOTH_ID)){				
				optAlertBluetoothText.setVisibility(View.VISIBLE);
				optAlertBluetoothValue.setVisibility(View.VISIBLE);
				optAlertBluetoothValue.setText(getString(R.string.spm_opt_alert_value_off));
			}else{
				optAlertBluetoothText.setVisibility(View.GONE);
				optAlertBluetoothValue.setVisibility(View.GONE);
			}
			
			TextView optAlertBrightText = (TextView) layout.findViewById(R.id.tv_opt_bright_text);
			TextView optAlertBrightValue = (TextView) layout.findViewById(R.id.tv_opt_bright_value);
			if(ModeUtils.getInstance(SPMActivity.this).maskNormalModeList(ConsumeValue.OPT_BRIGHT_ID)){				
				optAlertBrightText.setVisibility(View.VISIBLE);
				optAlertBrightValue.setVisibility(View.VISIBLE);
				optAlertBrightValue.setText(getString(R.string.spm_opt_alert_value_auto));
			}else{
				optAlertBrightText.setVisibility(View.GONE);
				optAlertBrightValue.setVisibility(View.GONE);
			}
			
			TextView optAlertTimeoutText = (TextView) layout.findViewById(R.id.tv_opt_timeout_text);
			TextView optAlertTimeoutValue = (TextView) layout.findViewById(R.id.tv_opt_timeout_value);
			if(ModeUtils.getInstance(SPMActivity.this).maskNormalModeList(ConsumeValue.OPT_TIMEOUT_ID)){				
				optAlertTimeoutText.setVisibility(View.VISIBLE);
				optAlertTimeoutValue.setVisibility(View.VISIBLE);
				optAlertTimeoutValue.setText(getString(R.string.spm_opt_alert_value_one_min));
			}else{
				optAlertTimeoutText.setVisibility(View.GONE);
				optAlertTimeoutValue.setVisibility(View.GONE);
			}
			
			TextView optAlertGpsText = (TextView) layout.findViewById(R.id.tv_opt_gps_text);
			TextView optAlertGpsValue = (TextView) layout.findViewById(R.id.tv_opt_gps_value);			
			if(ModeUtils.getInstance(SPMActivity.this).maskNormalModeList(ConsumeValue.OPT_GPS_ID)){				
				optAlertGpsText.setVisibility(View.VISIBLE);
				optAlertGpsValue.setVisibility(View.VISIBLE);
				optAlertGpsValue.setText(getString(R.string.spm_opt_alert_value_off));
			}else{
				optAlertGpsText.setVisibility(View.GONE);
				optAlertGpsValue.setVisibility(View.GONE);
			}
			
			TextView optAlertTouchText = (TextView) layout.findViewById(R.id.tv_opt_touch_text);				
			TextView optAlertTouchValue = (TextView) layout.findViewById(R.id.tv_opt_touch_value);
			if(ModeUtils.getInstance(SPMActivity.this).maskNormalModeList(ConsumeValue.OPT_TOUCH_ID)){
				optAlertTouchText.setVisibility(View.VISIBLE);
				optAlertTouchValue.setVisibility(View.VISIBLE);
				optAlertTouchValue.setText(getString(R.string.spm_opt_alert_value_on));
				
			}else{
				optAlertTouchText.setVisibility(View.GONE);
				optAlertTouchValue.setVisibility(View.GONE);
			}
			
			builder.setTitle(getString(R.string.spm_power_normal_save_mode_title));
			
		}else{
			TextView optAlertDataText = (TextView) layout.findViewById(R.id.tv_opt_data_text);
			TextView optAlertDataValue = (TextView) layout.findViewById(R.id.tv_opt_data_value);
			if(ModeUtils.getInstance(SPMActivity.this).maskSuperModeList(ConsumeValue.OPT_DATA_ID)){				
				optAlertDataText.setVisibility(View.VISIBLE);				
				optAlertDataValue.setVisibility(View.VISIBLE);
				optAlertDataValue.setText(getString(R.string.spm_opt_alert_value_off));
			}else{
				optAlertDataText.setVisibility(View.GONE);
				optAlertDataValue.setVisibility(View.GONE);
			}
			
			TextView optAlertWlanText = (TextView) layout.findViewById(R.id.tv_opt_wlan_text);			
			TextView optAlertWlanValue = (TextView) layout.findViewById(R.id.tv_opt_wlan_value);
			if(ModeUtils.getInstance(SPMActivity.this).maskSuperModeList(ConsumeValue.OPT_WLAN_ID)){
				optAlertWlanText.setVisibility(View.VISIBLE);
				optAlertWlanValue.setVisibility(View.VISIBLE);
				optAlertWlanValue.setText(getString(R.string.spm_opt_alert_value_off));
			}else{
				optAlertWlanText.setVisibility(View.GONE);
				optAlertWlanValue.setVisibility(View.GONE);
			}
			
			TextView optAlertBluetoothText = (TextView) layout.findViewById(R.id.tv_opt_bluetooth_text);			
			TextView optAlertBluetoothValue = (TextView) layout.findViewById(R.id.tv_opt_bluetooth_value);			
			if(ModeUtils.getInstance(SPMActivity.this).maskSuperModeList(ConsumeValue.OPT_BLUETOOTH_ID)){				
				optAlertBluetoothText.setVisibility(View.VISIBLE);
				optAlertBluetoothValue.setVisibility(View.VISIBLE);
				optAlertBluetoothValue.setText(getString(R.string.spm_opt_alert_value_off));
			}else{
				optAlertBluetoothText.setVisibility(View.GONE);
				optAlertBluetoothValue.setVisibility(View.GONE);
			}
			
			TextView optAlertBrightText = (TextView) layout.findViewById(R.id.tv_opt_bright_text);			
			TextView optAlertBrightValue = (TextView) layout.findViewById(R.id.tv_opt_bright_value);
			if(ModeUtils.getInstance(SPMActivity.this).maskSuperModeList(ConsumeValue.OPT_BRIGHT_ID)){				
				optAlertBrightText.setVisibility(View.VISIBLE);
				optAlertBrightValue.setVisibility(View.VISIBLE);
				optAlertBrightValue.setText(getString(R.string.spm_opt_alert_value_thr_per));
			}else{
				optAlertBrightText.setVisibility(View.GONE);
				optAlertBrightValue.setVisibility(View.GONE);
			}
			
			TextView optAlertTimeoutText = (TextView) layout.findViewById(R.id.tv_opt_timeout_text);			
			TextView optAlertTimeoutValue = (TextView) layout.findViewById(R.id.tv_opt_timeout_value);
			if(ModeUtils.getInstance(SPMActivity.this).maskSuperModeList(ConsumeValue.OPT_TIMEOUT_ID)){
				optAlertTimeoutText.setVisibility(View.VISIBLE);
				optAlertTimeoutValue.setVisibility(View.VISIBLE);
				optAlertTimeoutValue.setText(getString(R.string.spm_opt_alert_value_thr_sec));
			}else{
				optAlertTimeoutText.setVisibility(View.GONE);
				optAlertTimeoutValue.setVisibility(View.GONE);
			}
			
			TextView optAlertGpsText = (TextView) layout.findViewById(R.id.tv_opt_gps_text);			
			TextView optAlertGpsValue = (TextView) layout.findViewById(R.id.tv_opt_gps_value);
			if(ModeUtils.getInstance(SPMActivity.this).maskSuperModeList(ConsumeValue.OPT_GPS_ID)){
				optAlertGpsText.setVisibility(View.VISIBLE);
				optAlertGpsValue.setVisibility(View.VISIBLE);
				optAlertGpsValue.setText(getString(R.string.spm_opt_alert_value_off));
			}else{
				optAlertGpsText.setVisibility(View.GONE);
				optAlertGpsValue.setVisibility(View.GONE);
			}
			
			TextView optAlertTouchText = (TextView) layout.findViewById(R.id.tv_opt_touch_text);			
			TextView optAlertTouchValue = (TextView) layout.findViewById(R.id.tv_opt_touch_value);
			if(ModeUtils.getInstance(SPMActivity.this).maskSuperModeList(ConsumeValue.OPT_TOUCH_ID)){
				optAlertTouchText.setVisibility(View.VISIBLE);
				optAlertTouchValue.setVisibility(View.VISIBLE);
				optAlertTouchValue.setText(getString(R.string.spm_opt_alert_value_off));
			}else{
				optAlertTouchText.setVisibility(View.GONE);
				optAlertTouchValue.setVisibility(View.GONE);
			}
			
			builder.setTitle(getString(R.string.spm_power_super_save_mode_title));
			
		}
		
		builder.setPositiveButton(getString(R.string.btn_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {								
											
								if (PrefUtils.getInstance(SPMActivity.this).getOptBtnNormalState()){
									clickPicTrans(CLICK_PIC_SUPER);
									optBtn.setText(getString(R.string.spm_power_super_save_mode));
									optBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.lewa_bt_energy_conservation_pressed01));	
									ModeUtils.getInstance(SPMActivity.this).enterMode(ConsumeValue.MODE_OPT_SUPER);
									updateUITitle(true, ConsumeValue.MODE_OPT_SUPER,false);
									PrefUtils.getInstance(SPMActivity.this).setOptBtnNormalState(false);
									
								}else{
									clickPicTrans(CLICK_PIC_NORMAL);
									optBtn.setText(getString(R.string.spm_power_normal_save_mode));
									optBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.spm_bt_energy_conservation));	
									ModeUtils.getInstance(SPMActivity.this).enterMode(ConsumeValue.MODE_OPT_NORMAL);
									updateUITitle(true, ConsumeValue.MODE_OPT_NORMAL,false);
									PrefUtils.getInstance(SPMActivity.this).setOptBtnNormalState(true);
								}
								
								updateUIList();								
								if(toMode==WINDOW_OPTIMIZED_NORMAL){
									removeDialog(WINDOW_OPTIMIZED_NORMAL);
								}else{
									removeDialog(WINDOW_OPTIMIZED_SUPER);
								}
							}
						});
		
		builder.setNegativeButton(getString(R.string.btn_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id){								
								if(toMode==WINDOW_OPTIMIZED_SUPER){
									removeDialog(WINDOW_OPTIMIZED_SUPER);
								}else{
									removeDialog(WINDOW_OPTIMIZED_NORMAL);
								}								
							}
						});
		
		AlertDialog alert = builder.create();
		return alert;
	}
	
	private int calcLifeChange(int toModeNum){
		float battLevelPercent = Integer.parseInt(BatteryInfo.getInformation(BatteryInfo.battCapacity)) / 100f;
		double toValue = CalcUtils.getInstance(this).Lift(toModeNum);
		double fromValue = PrefUtils.getInstance(this).getOldConsumeValue();
		return (int) Math.round((toValue - fromValue) * battLevelPercent);
	}
}
