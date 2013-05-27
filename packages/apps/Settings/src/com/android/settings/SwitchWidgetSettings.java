package com.android.settings;

import android.app.ListActivity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LewaCheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.DragableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ToggleButton;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import com.android.settings.R;

import android.R.bool;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import java.util.List;

/**
 * @author: Woody Guo <guozhenjiang@ndoo.net>
 * @description: Activity to configure the SwitchWidget, i.e., single page style or dula pages style,
 * orders of the switches, which buttons to show when in single page style
 */
public class SwitchWidgetSettings extends ListActivity
{
    private static final boolean DBG = true;
    private static final String TAG = "SwitchWidgetSettings";

    private DragableListView mButtonList;
    private ButtonAdapter mButtonAdapter;

    private ArrayList<ButtonInfo> mButtons;
    private ArrayList<String> mButtonStrings;

    private String mCurrentButtons;
    private String mCurrentTinyButtons;

    public static final int MAX_WIDGET = 11;
   
    //added by zhuyaopeng 2012/07/09
    private String BUILD_MODEL_S5830="GT-S5830";    
    private String BUILD_MODEL_G13="Wildfire S";
    private String BUILD_MODEL_U8800="U8800";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.switch_widget_settings);

        mButtonList = (DragableListView) getListView();
        

        mButtonList.setDropListener(mDropListener);
	    mButtonList.setDragListener(mDragListener);
	    mButtonList.setFixedItem(MAX_WIDGET -1);
        mButtonList.setWindowType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);

        mButtonAdapter = new ButtonAdapter(this);        
        setListAdapter(mButtonAdapter);
    }

    @Override
    public void onDestroy() {
        ((DragableListView) mButtonList).setDropListener(null);
        setListAdapter(null);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        // reload our buttons and invalidate the views for redraw
        mButtonAdapter.reloadButtons();
        mButtonList.invalidateViews();
    }

    @Override
    public void onPause() {
        saveCurrentButtons();
        super.onPause();
    }

    private DragableListView.DropListener mDropListener = new DragableListView.DropListener() {
            public void drop(int from, int to) {
		  
		  Log.d(TAG,"drop from = " + from + "to = " + to);
		  ButtonInfo button = null;
		  String str = null;
                if (from < mButtons.size() + 1) {
			if(from > MAX_WIDGET) {
			      button = mButtons.remove(from - 1);
       	             str = mButtonStrings.remove(from - 1);
			}
			else if(from < MAX_WIDGET) {
			      button = mButtons.remove(from);
       	             str = mButtonStrings.remove(from);
			}
			else {
				return;
			}
		  } 
                if (to <= mButtons.size() + 1) {
		      if(to > MAX_WIDGET) {
	                    mButtons.add(to -1 , button);
	                    mButtonStrings.add(to -1, str);
		      	}
			else {
			      mButtons.add(to, button);
	                    mButtonStrings.add(to, str);
			}

		 	// tell our adapter/listview to reload
                    mButtonList.invalidateViews();
            }
		  saveCurrentButtons();
            }
        };

	private DragableListView.DragListener mDragListener = new DragableListView.DragListener() {
            public void drag(int from, int to) {	  
		  Log.d(TAG,"drag from = " + from + "to = " + to);
		  if(from == MAX_WIDGET - 1 || from == MAX_WIDGET + 1) {
		  	//mButtonList.invalidateViews();
		  }
            }
		 
        };

    private class ButtonAdapter extends BaseAdapter {
        private Context mContext;
        private Resources mSystemUIResources = null;
        private LayoutInflater mInflater;

        public ButtonAdapter(Context c) {
            mContext = c;
            mInflater = LayoutInflater.from(mContext);
            PackageManager pm = mContext.getPackageManager();
            if (pm != null) {
                try {
                    mSystemUIResources = pm.getResourcesForApplication("com.android.systemui");
                } catch(Exception e) {
                    mSystemUIResources = null;
                    Log.e(TAG, "Could not load SystemUI resources", e);
                }
            }

            reloadButtons();
        }

        // In dual pages mode, all buttons are showed in the order you select,
        // but in single page mode, only selected buttons are showed
        public void reloadButtons() {
            for (ButtonInfo button : BUTTONS.values()) {
                button.setSelected(false);
            }

            // First shows all of the buttons
            mCurrentButtons = getCurrentButtons(false);
            ArrayList<String> buttons = getButtonListFromString(mCurrentButtons);
            mButtons = new ArrayList<ButtonInfo>();
            mButtonStrings = new ArrayList<String>();
            for (String button : buttons) {
                if (BUTTONS.containsKey(button)) {
                	if("u880".equalsIgnoreCase(Build.MODEL)){
                		if(button.equals(BUTTON_NIGHT_MODE)){
                			continue;
                		}
                        mButtons.add(BUTTONS.get(button));
                        mButtonStrings.add(button);
                    }else{                    
                        mButtons.add(BUTTONS.get(button));
                        mButtonStrings.add(button);
                    }
                }
            }

            // Set the buttons showed in tiny mode as selected
            mCurrentTinyButtons = getCurrentButtons(true);
            buttons = getButtonListFromString(mCurrentTinyButtons);
            int index;
            ButtonInfo bi;
            for (String button : buttons) {
                index = mButtonStrings.indexOf(button);
                if (index != -1) {
                    bi = (ButtonInfo) mButtons.get(index);
                    bi.setSelected(true);
                }
            }
        }

        public int getCount() {
            return mButtons.size() + 1;
        }

        public Object getItem(int position) {
            return mButtons.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

		public int dip2px(Context context, float dpValue) {
			final float scale = context.getResources().getDisplayMetrics().density;
			return (int) (dpValue * scale + 0.5f);
		}

        private int NOTICE_LINE_POSITION=10;
        private ItemViewHolder holder;
        private String stringTag="com.android.systemui:string";
        private String btnStringName="switchwidgetsettings_notice_title";
        
        public View getView(int position, View convertView, ViewGroup parent) {
	     ButtonInfo button = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.switch_button, null);
                holder = new ItemViewHolder();
                holder.ICON = (ImageView) convertView.findViewById(R.id.icon);
                holder.NAME = (TextView) convertView.findViewById(R.id.name);
                convertView.setTag(holder);                
            }else{
            	holder = (ItemViewHolder) convertView.getTag();
            }

	    if(position > MAX_WIDGET) {
		 button = mButtons.get(position -  1);	
	    }
	    else  if ( position < MAX_WIDGET ){
             button = mButtons.get(position);
	    }
             
            RelativeLayout switchItemLayout=(RelativeLayout)convertView.findViewById(R.id.switch_item_id);
        	LinearLayout noticeLayout=(LinearLayout)convertView.findViewById(R.id.notice_nums_id);
        	Resources res=mContext.getResources();
            if(position == MAX_WIDGET) {
				ViewGroup.LayoutParams params = convertView.getLayoutParams();
				if(params != null && convertView != null) {
			            	  params.height = dip2px(convertView.getContext(),32.0f);
			                convertView.setLayoutParams(params);
				}				
                switchItemLayout.setVisibility(View.GONE);
                noticeLayout.setVisibility(View.VISIBLE);
                //int paddingSize=res.getDimensionPixelSize(R.dimen.switch_layout_padding);
               // noticeLayout.setPadding(0, paddingSize,0,paddingSize);
                noticeLayout.setBackgroundColor(0xffe0e0e0);
                TextView notice=(TextView) noticeLayout.getChildAt(0);
                String lan=Locale.getDefault().getLanguage();
                int textSize=res.getDimensionPixelSize(R.dimen.switch_text_size);
                int paddingLeftSize=res.getDimensionPixelSize(R.dimen.switch_text_left_padding);
                int paddingTopSize=res.getDimensionPixelSize(R.dimen.switch_text_top_padding);
                if(lan.equals("en")){
                	textSize=res.getDimensionPixelSize(R.dimen.switch_text_small_size);
                	notice.setTextSize(textSize);
                	paddingLeftSize=res.getDimensionPixelSize(R.dimen.switch_text_left_small_padding);
                }
                notice.setTextColor(0xff7f7f7f);
                notice.setPadding(paddingLeftSize,paddingTopSize,0,0);
            }else{            	
                ViewGroup.LayoutParams params = convertView.getLayoutParams();
			    if(params != null && convertView != null) {
		            	params.height = dip2px(convertView.getContext(),64.0f);
		                convertView.setLayoutParams(params);
			    }
            	switchItemLayout.setVisibility(View.VISIBLE);
                noticeLayout.setVisibility(View.GONE);
//            if (holder == null) {
             
//                holder.CHECK = (CheckBox)convertView.findViewById(R.id.selection);

/*                holder.CHECK.setOnCheckedChangeListener(
                        new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        final int pos = (Integer) buttonView.getTag();
                        final ButtonInfo bi = mButtons.get(pos);
                        bi.setSelected(isChecked);
                        ((BaseAdapter) mButtonList.getAdapter()).notifyDataSetChanged();
                        if (DBG) {
                            Log.d(TAG, "setOnCheckedChangeListener - " + bi.getId() + " set to " + isChecked);
                        }
                    }
                });*/
//            }

//            holder.CHECK.setTag(position);

         /*   if (DBG) {
                Log.d(TAG, "getView - Position: "
                        + Integer.toString(position) + "; ButtonInfo: " + (button == null ? "NULL" : button.getId()));
            }*/

            if (null != button) {
                /*if (DBG) {
                    Log.d(TAG, "Content checked: " + button.isSelected()
                            + "; Display checked: " + holder.CHECK.isChecked());
                }
                holder.CHECK.setChecked(button.isSelected());*/

                // TODO: cache strings and icons retrieved from the SystemUI package
                // assume no icon first
                holder.ICON.setVisibility(View.GONE);
                // attempt to load the icon for this button
                if (mSystemUIResources != null) {
                    int resId = mSystemUIResources.getIdentifier(button.getIcon(), null, null);
                    if (resId > 0) {
                        try {
                            Drawable d = mSystemUIResources.getDrawable(resId);
                            holder.ICON.setImageDrawable(d);
                            holder.ICON.setVisibility(View.VISIBLE);
                        } catch(Exception e) {
                            Log.e(TAG, "Error retrieving icon drawable", e);
                        }
                    }
                    resId = mSystemUIResources.getIdentifier(button.getTitle(), null, null);
                    if (resId > 0) {
                        try {
                            holder.NAME.setText(mSystemUIResources.getString(resId));
//				Log.i(TAG,"resId=="+resId+",pos="+position +",res = " + mSystemUIResources.getString(resId));
                        } catch(Exception e) {
                            Log.e(TAG, "Error retrieving string", e);
                        }
                    }
                }
            }
        }
            return convertView;
        }
    }

    private final class ItemViewHolder {
        public ImageView ICON;
        public TextView NAME;
//        public CheckBox CHECK;
    }

/**
 * THIS CLASS'S DATA MUST BE KEPT UP-TO-DATE WITH THE DATA IN
 * com.android.systemui.statusbar.switchwidget.SwitchWidget AND
 * com.android.systemui.statusbar.switchwidget.SwitchButton IN THE SystemUI PACKAGE.
 */
    // From com.android.systemui.statusbar.switchwidget.SwitchButton;
    private static final String BUTTON_DELIMITER = "|";

    private static final String BUTTON_SCREEN_OFF = "turnScreenOff";
    private static final String BUTTON_LOCK_NOW = "lockDevice";
    private static final String BUTTON_SHUTDOWN = "shutdownDevice";
    private static final String BUTTON_REBOOT = "rebootDevice";

    private static final String BUTTON_AIRPLANE = "toggleAireplane";
    private static final String BUTTON_AUTOROTATE = "toggleAutoRotate";
    private static final String BUTTON_BLUETOOTH = "toggleBluetooth";
    private static final String BUTTON_GPS = "toggleGps";
    private static final String BUTTON_TORCH = "toggleTorch";
    private static final String BUTTON_DATA = "toggleData";
    private static final String BUTTON_SYNC = "toggleSync";
    private static final String BUTTON_WIFI = "toggleWifi";
    private static final String BUTTON_WIFI_AP = "toggleWifiAp";
    private static final String BUTTON_SOUND = "toggleSound";
    private static final String BUTTON_BRIGHTNESS = "toggleBrightness";
    private static final String BUTTON_NETWORKMODE = "toggleNetworkMode";
    private static final String BUTTON_SWITCH_WIDGET_STYLE = "toggleSwitchWidgetStyle";
    // public static final String BUTTON_SCREEN_CAPTURE = "toggleScreenCapture";
    private static final String BUTTON_NIGHT_MODE = "nightmode";
    public static final String BUTTON_POWER_MANAGER = "powermanager";

/*    private static final String BUTTONS_ALL = 
//    		BUTTON_SCREEN_OFF + BUTTON_DELIMITER
    		BUTTON_LOCK_NOW + BUTTON_DELIMITER
    		+BUTTON_NIGHT_MODE+BUTTON_DELIMITER
            + BUTTON_REBOOT + BUTTON_DELIMITER
            + BUTTON_AIRPLANE + BUTTON_DELIMITER
            + BUTTON_AUTOROTATE + BUTTON_DELIMITER
            + BUTTON_BLUETOOTH + BUTTON_DELIMITER
            + BUTTON_GPS + BUTTON_DELIMITER
            + BUTTON_DATA + BUTTON_DELIMITER
            + BUTTON_WIFI + BUTTON_DELIMITER
//            + BUTTON_SYNC + BUTTON_DELIMITER
//            + BUTTON_WIFI_AP + BUTTON_DELIMITER
            + BUTTON_SOUND + BUTTON_DELIMITER
            + BUTTON_BRIGHTNESS + BUTTON_DELIMITER
//            +BUTTON_NOTICE+BUTTON_DELIMITER
//            + BUTTON_TORCH + BUTTON_DELIMITER
            + BUTTON_NETWORKMODE + BUTTON_DELIMITER
            + BUTTON_SHUTDOWN
              + BUTTON_DELIMITER 
            // + BUTTON_SCREEN_CAPTURE + BUTTON_DELIMITER
            // + BUTTON_SWITCH_WIDGET_STYLE
            ;*/
    public static String BUTTONS_ALL =
            BUTTON_LOCK_NOW + BUTTON_DELIMITER
            //+BUTTON_POWER_MANAGER+BUTTON_DELIMITER
            +BUTTON_NIGHT_MODE+BUTTON_DELIMITER
            + BUTTON_TORCH+BUTTON_DELIMITER
            + BUTTON_AIRPLANE + BUTTON_DELIMITER
            + BUTTON_AUTOROTATE + BUTTON_DELIMITER
            + BUTTON_BLUETOOTH + BUTTON_DELIMITER
            + BUTTON_GPS + BUTTON_DELIMITER
            + BUTTON_DATA + BUTTON_DELIMITER
            + BUTTON_WIFI + BUTTON_DELIMITER
            + BUTTON_SOUND + BUTTON_DELIMITER
            + BUTTON_BRIGHTNESS + BUTTON_DELIMITER
            + BUTTON_SHUTDOWN + BUTTON_DELIMITER
            + BUTTON_NETWORKMODE + BUTTON_DELIMITER
            + BUTTON_REBOOT
//            + BUTTON_SCREEN_CAPTURE + BUTTON_DELIMITER
//            + BUTTON_SWITCH_WIDGET_STYLE
            ;

    private static String NIGHT_MODE_STRING="com.android.systemui:string/power_widgetbutton_nightmode";
    
    public static HashMap<String, ButtonInfo> BUTTONS = new HashMap<String, ButtonInfo>();
    static {
    	BUTTONS.put(BUTTON_NIGHT_MODE, new ButtonInfo(
                BUTTON_NIGHT_MODE,NIGHT_MODE_STRING, "com.android.systemui:drawable/stat_nightmode_on"));
        BUTTONS.put(BUTTON_AIRPLANE, new ButtonInfo(
                BUTTON_AIRPLANE, "com.android.systemui:string/title_toggle_airplane", "com.android.systemui:drawable/stat_airplane_on"));
        BUTTONS.put(BUTTON_AUTOROTATE, new ButtonInfo(
                BUTTON_AUTOROTATE, "com.android.systemui:string/title_toggle_autorotate", "com.android.systemui:drawable/stat_orientation_on"));
        BUTTONS.put(BUTTON_BLUETOOTH, new ButtonInfo(
                BUTTON_BLUETOOTH, "com.android.systemui:string/title_toggle_bluetooth", "com.android.systemui:drawable/stat_bluetooth_on"));
        BUTTONS.put(BUTTON_BRIGHTNESS, new ButtonInfo(
                BUTTON_BRIGHTNESS, "com.android.systemui:string/title_toggle_brightness", "com.android.systemui:drawable/stat_brightness_on"));
        BUTTONS.put(BUTTON_TORCH, new ButtonInfo(
                BUTTON_TORCH, "com.android.systemui:string/title_toggle_flashlight", "com.android.systemui:drawable/stat_torch_on"));
        BUTTONS.put(BUTTON_GPS, new ButtonInfo(
                BUTTON_GPS, "com.android.systemui:string/title_toggle_gps", "com.android.systemui:drawable/stat_gps_on"));
        BUTTONS.put(BUTTON_LOCK_NOW, new ButtonInfo(
                BUTTON_LOCK_NOW, "com.android.systemui:string/title_toggle_locknow", "com.android.systemui:drawable/stat_lockscreen"));
        BUTTONS.put(BUTTON_DATA, new ButtonInfo(
                BUTTON_DATA, "com.android.systemui:string/title_toggle_mobiledata", "com.android.systemui:drawable/stat_data_on"));
        BUTTONS.put(BUTTON_NETWORKMODE, new ButtonInfo(
                BUTTON_NETWORKMODE, "com.android.systemui:string/title_toggle_networkmode", "com.android.systemui:drawable/stat_2g3g_on"));
       /* BUTTONS.put(BUTTON_SCREEN_OFF, new ButtonInfo(
                BUTTON_SCREEN_OFF, "com.android.systemui:string/title_toggle_sleep", "com.android.systemui:drawable/switch_screen_off"));*/
        BUTTONS.put(BUTTON_SOUND, new ButtonInfo(
                BUTTON_SOUND, "com.android.systemui:string/title_toggle_sound", "com.android.systemui:drawable/stat_ring_off_1"));
       /* BUTTONS.put(BUTTON_SYNC, new ButtonInfo(
                BUTTON_SYNC, "com.android.systemui:string/title_toggle_sync", "com.android.systemui:drawable/switch_sync_on"));*/
        BUTTONS.put(BUTTON_WIFI, new ButtonInfo(
                BUTTON_WIFI, "com.android.systemui:string/title_toggle_wifi", "com.android.systemui:drawable/stat_wifi_on"));
       /* BUTTONS.put(BUTTON_WIFI_AP, new ButtonInfo(
                BUTTON_WIFI_AP, "com.android.systemui:string/title_toggle_wifiap", "com.android.systemui:drawable/switch_wifi_ap_on"));*/
        BUTTONS.put(BUTTON_SHUTDOWN, new ButtonInfo(
                BUTTON_SHUTDOWN, "com.android.systemui:string/title_toggle_shutdown", "com.android.systemui:drawable/stat_poweroff"));
        BUTTONS.put(BUTTON_REBOOT, new ButtonInfo(
                BUTTON_REBOOT, "com.android.systemui:string/title_toggle_reboot", "com.android.systemui:drawable/stat_reboot"));
       // BUTTONS.put(BUTTON_POWER_MANAGER, new ButtonInfo(
       // 		BUTTON_POWER_MANAGER, "com.android.systemui:string/title_toggle_powermanager", "com.android.systemui:drawable/stat_power_on"));
        /*
         * BUTTONS.put(BUTTON_SWITCH_WIDGET_STYLE, new ButtonInfo(
         *         BUTTON_SWITCH_WIDGET_STYLE, "com.android.systemui:string/title_toggle_switchwidgetstyle", "com.android.systemui:drawable/switch_widget_style_dual"));
         * BUTTONS.put(BUTTON_SCREEN_CAPTURE, new ButtonInfo(
         *         BUTTON_SCREEN_CAPTURE, "com.android.systemui:string/title_toggle_screencapture", "com.android.systemui:drawable/switch_screencapture_on"));
         */
    }

    private String tempStr="";
    
    public String getCurrentButtons(boolean tinyMode) {
        String buttons = Settings.System.getString(getContentResolver()
                , tinyMode ? Settings.System.SWITCH_WIDGET_BUTTONS_TINY
                : Settings.System.SWITCH_WIDGET_BUTTONS);
	    Log.d(TAG,"get CurrentButtons from db, ButtonString = [" + buttons + "]"); 
	    if(null!=buttons && buttons.equals(BUTTON_SWITCH_WIDGET_STYLE)){
	    	String temp=BUTTONS_ALL;
        	if(!BUILD_MODEL_S5830.equalsIgnoreCase(Build.MODEL) && !BUILD_MODEL_G13.equalsIgnoreCase(Build.MODEL)){
        		temp=temp.replace(BUTTON_TORCH+BUTTON_DELIMITER,"");
                BUTTONS_ALL=temp;
        	}
            ArrayList<String> allButtons = getButtonListFromString(temp);
            for (String b : allButtons) {
                if (-1 == buttons.indexOf(b)) {
                    buttons += BUTTON_DELIMITER;
                    buttons += b;
                }
            }
            Log.e(TAG,"only more...,buttons=="+buttons);
	    }
        if (null == buttons) {
        	Log.d(TAG,"getCurrentButtons,buttons==null");
            buttons = BUTTONS_ALL;
            if(!BUILD_MODEL_S5830.equalsIgnoreCase(Build.MODEL) && !BUILD_MODEL_G13.equalsIgnoreCase(Build.MODEL)){
        		buttons=buttons.replace(BUTTON_TORCH+BUTTON_DELIMITER,"");
                BUTTONS_ALL=buttons;
        	}
        } else if (!tinyMode) {
        	Log.d(TAG,"tinyMode==false");
        	String temp=BUTTONS_ALL;
        	if(!BUILD_MODEL_S5830.equalsIgnoreCase(Build.MODEL) && !BUILD_MODEL_G13.equalsIgnoreCase(Build.MODEL)){
        		temp=temp.replace(BUTTON_TORCH+BUTTON_DELIMITER,"");
                BUTTONS_ALL=temp;
        	}
            ArrayList<String> allButtons = getButtonListFromString(temp);
            for (String b : allButtons) {
                if (-1 == buttons.indexOf(b)) {
                    buttons += BUTTON_DELIMITER;
                    buttons += b;
                }
            }
            tempStr=buttons;
        }else if(tinyMode){
        	Log.d(TAG,"tinyMode==true");
        	if(tempStr!=null && !buttons.equals(tempStr+BUTTON_DELIMITER+BUTTON_SWITCH_WIDGET_STYLE)){
        		tempStr+=BUTTON_DELIMITER;
        		tempStr+=BUTTON_SWITCH_WIDGET_STYLE;
        		buttons=tempStr;
        		Log.d(TAG,"final,tempStr="+tempStr);
        		if(!BUILD_MODEL_S5830.equalsIgnoreCase(Build.MODEL) && !BUILD_MODEL_G13.equalsIgnoreCase(Build.MODEL)){
            		buttons=buttons.replace(BUTTON_TORCH+BUTTON_DELIMITER,"");
            	}
        	}
        }
        Log.d(TAG,"finally,getCurrentButtons=="+buttons);
        return buttons;
    }

    public void saveCurrentButtons() {
        String str = getButtonStringFromList(true);
	    Log.d(TAG,"save ButtonString=="+str);
	    Log.d(TAG,"mCurrentTinyButtons=="+mCurrentTinyButtons);
	    Log.d(TAG,"mCurrentButtons=="+mCurrentButtons);
        if (!mCurrentTinyButtons.equals(str)) {
            Settings.System.putString(
                    getContentResolver(), Settings.System.SWITCH_WIDGET_BUTTONS_TINY, str);
        }

        str = getButtonStringFromList(false);
        if (!mCurrentButtons.equals(str)) {
            Settings.System.putString(
                    getContentResolver(), Settings.System.SWITCH_WIDGET_BUTTONS, str);
        }
    }

    public ArrayList<String> getButtonListFromString(String buttons) {
        return new ArrayList<String>(Arrays.asList(buttons.split("\\|")));
    }

    public String getButtonStringFromList(boolean tinyMode) {
        if (mButtons == null || mButtons.size() <= 0) {
            return "";
        }
        if (tinyMode) {
            StringBuilder sb = new StringBuilder();
            ButtonInfo bi ;
            for (int i = 0; i < mButtons.size(); i++) {
                bi = mButtons.get(i);
//                if (bi.isSelected()) {
                    sb.append(bi.getId());
                    sb.append(BUTTON_DELIMITER);
//                }
            }
            
            Log.d(TAG,"getButtonsStringfromList,mButtons=="+mButtons);
            Log.d(TAG,"sb=="+sb.toString());

            // Switch toggle should always be selected
            if (sb.indexOf(BUTTON_SWITCH_WIDGET_STYLE) == -1) {
                return sb.append(BUTTON_SWITCH_WIDGET_STYLE).toString();
            }
            return sb.subSequence(0, sb.length()-BUTTON_DELIMITER.length()).toString();
        } else {
            StringBuilder sb = new StringBuilder(mButtons.get(0).getId());
            for(int i = 1; i < mButtons.size(); i++) {
                sb.append(BUTTON_DELIMITER);
                sb.append(mButtons.get(i).getId());
            }
            return sb.toString();
        }
    }

    public static class ButtonInfo {
        private String mId;
        private String mTitle;
        private String mIcon;
        private boolean mSelected;

        public ButtonInfo(String id, String title, String icon) {
            mId = id;
            mTitle = title;
            mIcon = icon;
        }

        public String getId() { return mId; }
        public String getTitle() { return mTitle; }
        public String getIcon() { return mIcon; }

        public boolean isSelected() { return mSelected; }
        public void setSelected(boolean selected) { mSelected = selected;

        }

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "title :"+mTitle;
		}
    }
}
