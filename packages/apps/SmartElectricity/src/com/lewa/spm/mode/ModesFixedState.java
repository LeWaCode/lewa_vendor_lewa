package com.lewa.spm.mode;

import java.util.HashMap;
import java.util.Map;

import com.lewa.spm.util.Constants;
import com.lewa.spm.util.SharedStorageKeyValuePair;
import com.lewa.spm.mode.ModeSettings;
import com.lewa.spm.mode.ModeDevStatus;

/**
 * This class is used to record all devices' state of each models 
 */
public class ModesFixedState {

	public ModesFixedState() {
	}
    
	public void getModeDefaultSettings(ModeSettings s){
        int mode=s.getMode();
		if (mode==Constants.SPM_MODE_LONG_ID){
			s.flyModeSetted= false;
		}else if (mode==Constants.SPM_MODE_ALARM_ID){
			s.flyModeSetted= true;
		}
       	
		s.dataSetted= true;
		s.timeOutValue= 15000;
		s.hapticSetted= true;
		s.brightnessSetted= true;
		s.brightnessValue= 10;
		s.autoSyncSetted= true;
		s.wifiSetted= true;
		s.gpsSetted= true;
		s.bluetoothSetted= true;
	}
	
}
