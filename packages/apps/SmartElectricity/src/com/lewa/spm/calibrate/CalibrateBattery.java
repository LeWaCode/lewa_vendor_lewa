package com.lewa.spm.calibrate;

import java.io.File;


public class CalibrateBattery {

	SuCommander mSuCommander;
	public CalibrateBattery() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public void deleteFile(){
		try {
			mSuCommander = new SuCommander();
			if(queryFile()){
				mSuCommander = new SuCommander();
	             /**
	              * drwx-rwx-r-x-
	              */
				mSuCommander.exec("chmod 777 /data/system/batterystats.bin");
				Thread.currentThread().sleep(500);
				mSuCommander.exec("rm -rvf /data/system/batterystats.bin");
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean queryFile(){
		File file = new File("/data/system/batterystats.bin");
		if (file.exists()){
			return true;
		}else {
			return false;
		}
	}

}
