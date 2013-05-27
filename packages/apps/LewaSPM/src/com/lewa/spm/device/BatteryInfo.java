package com.lewa.spm.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class BatteryInfo {

	public static String acOnline = "/sys/class/power_supply/ac/online";// AC power connect state
	public static String usbOnline = "/sys/class/power_supply/usb/online";// USB power connect state
	public static String battStatus = "/sys/class/power_supply/battery/status";// charging status
	public static String battHealth = "/sys/class/power_supply/battery/health";// battery status
	public static String battPresent = "/sys/class/power_supply/battery/present";// use status
	public static String battCapacity = "/sys/class/power_supply/battery/capacity";// battery level
	public static String battVol = "/sys/class/power_supply/battery/voltage_now";// battery voltage
	public static String battCur = "/sys/class/power_supply/battery/current_now";//battery current
	public static String battTemp = "/sys/class/power_supply/battery/temp";// battery temperature
	public static String battTech = "/sys/class/power_supply/battery/technology";// battery technology

	public static String getInformation(String pathName) {
		
		File tempFile = new File(pathName);
		
		String valueStr = "";
		
		BufferedReader bufferReader = null;
		
		if (tempFile.exists()) {
			
			try {
				
				bufferReader = new BufferedReader(new FileReader(tempFile));

				String tempString = null;

				while ((tempString = bufferReader.readLine()) != null)
					valueStr += tempString;
				
				bufferReader.close();
				
			} catch (IOException ioe) {
				
			}finally{
				if(bufferReader!=null) {
					try {
						bufferReader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			valueStr = "";
			// log toast
		}

		return valueStr;
	}
}
