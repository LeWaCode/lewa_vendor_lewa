package com.lewa.cit;

import android.app.Application;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.provider.Settings;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;



public class ConfigurationView extends Activity implements SensorEventListener {
    
    private SensorManager sensorManager = null;  
    private Sensor lightSensor = null;
    private TextView tv = null;
    private TextView mSensor = null ;
    private Handler mHandler;
    private String sConfig = null;
    private static final int UPDATE_RATE = 400;
    @Override
    public void onCreate(Bundle icicle) {
            super.onCreate(icicle);

            setContentView(R.layout.config_viewer);

            Configuration c = getResources().getConfiguration();
            DisplayMetrics m = new DisplayMetrics();
            mHandler = new Handler();
            getWindowManager().getDefaultDisplay().getMetrics(m);
            sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            tv = (TextView)findViewById(R.id.text);
            mSensor = (TextView) findViewById(R.id.light_sensor);
            sConfig = "Configuration\n"
            + "\n"
            + "fontScale=" + c.fontScale + "\n"
            + "hardKeyboardHidden=" + c.hardKeyboardHidden + "\n"
            + "keyboard=" + c.keyboard + "\n"
            + "locale=" + c.locale + "\n"
            + "mcc=" + c.mcc + "\n"
            + "mnc=" + c.mnc + "\n"
            + "navigation=" + c.navigation + "\n"
            + "navigationHidden=" + c.navigationHidden + "\n"
            + "orientation=" + c.orientation + "\n"
            + "screenLayout=0x" + Integer.toHexString(c.screenLayout) + "\n"
            + "touchscreen=" + c.touchscreen + "\n"
            + "uiMode=0x" + Integer.toHexString(c.uiMode) + "\n"
            + "\n"
            + "DisplayMetrics\n"
            + "\n"
            + "density=" + m.density + "\n"
            + "densityDpi=" + m.densityDpi + "\n"
            + "heightPixels=" + m.heightPixels + "\n"
            + "scaledDensity=" + m.scaledDensity + "\n"
            + "widthPixels=" + m.widthPixels + "\n"
            + "xdpi=" + m.xdpi + "\n"
            + "ydpi=" + m.ydpi + "\n"
            ;

    tv.setText(sConfig);

    // Also log it for bugreport purposes.
    Log.d("ConfigurationViewer", sConfig);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mUpdateTask);
        sensorManager.unregisterListener(this, lightSensor);
    }

    @Override
    public void onResume() {
        super.onResume();
        mUpdateTask.run();
        sensorManager.registerListener(this, lightSensor,sensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
        if(sensor.getType() == Sensor.TYPE_LIGHT)
        {
            //set the accuracy to textview
            tv.append("lightSensorAccuracy  = "+accuracy+" \n");
        }
        
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if(event.sensor.getType() == Sensor.TYPE_LIGHT) {
            
        }
    }

    private Runnable mUpdateTask = new Runnable() {
        public void run() {
            boolean autoLcd = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, 1337) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            boolean filterEnabled = Settings.System.getInt(getContentResolver(),
                    Settings.System.LIGHT_FILTER, 0) != 0;

            try {
                IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager
                        .getService("power"));
                if (filterEnabled && autoLcd) {
                    mSensor.setText(String.valueOf(power.getLightSensorValue()) + " / "
                            + String.valueOf(power.getRawLightSensorValue()));
                } else {
                    String value = String.valueOf(power.getLightSensorValue());
                    mSensor.setText(value + " / " + value);
                }

            } catch (Exception e) {
                // Display "-" on any error

                mSensor.setText("- / -");
            }

            mHandler.postDelayed(mUpdateTask, UPDATE_RATE);
        }
    };
}
