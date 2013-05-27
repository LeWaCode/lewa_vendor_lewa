/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.spm.control;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

public class Bluetooth extends SwitchControl{

	public Bluetooth(int mode) {
		super(mode);
	}


	public void change(boolean closeOrOpen) {
		if (closeOrOpen){
			 BluetoothAdapter.getDefaultAdapter().enable();
		}else{
			 BluetoothAdapter.getDefaultAdapter().disable();
		}
	}


	public boolean isOnOff() {
//		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//		if(mBluetoothAdapter!=null){
//			return mBluetoothAdapter.isEnabled();
//		}else{
//			return false;
//		}
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		return bluetoothStateToFiveState(mBluetoothAdapter.getState());
	}
	
	/**
	 * Converts BluetoothAdapter's state values into our Wifi/Bluetooth-common
	 * state values.
	 */
	private boolean bluetoothStateToFiveState(int bluetoothState) {
		boolean flag = false;
		switch (bluetoothState) {
		case BluetoothAdapter.STATE_OFF:
			flag = false;
			break;
		case BluetoothAdapter.STATE_ON:
			flag = true;
			break;
		case BluetoothAdapter.STATE_TURNING_ON:
			flag = false;
			break;
		case BluetoothAdapter.STATE_TURNING_OFF:
			flag = false;
			break;
		default:
			break;
		}
		return flag;
	}

    public static  int getState(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.getState();
    }

}
