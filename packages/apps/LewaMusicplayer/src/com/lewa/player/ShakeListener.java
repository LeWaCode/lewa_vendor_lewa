package com.lewa.player;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class ShakeListener implements SensorEventListener {  
  
    private static final int TIME_THRESHOLD = 100;  
    private static final int SHAKE_DURATION = 1000;  
    public static final int DEFAULT_SHAKE_DEGREE = 15;
  
    private SensorManager mSensorMgr;
    private float mLastX = 0.0f; 
    private float mLastY = 0.0f; 
    private float mLastZ = 0.0f;
    private float x, y, z;
    private long mLastTime = 0;  
    private OnShakeListener mShakeListener;  
    private MediaPlaybackService mServices;
    private long mLastShake;   
    SoundPool mSoundPool;
    int mSoundId;
  
    public interface OnShakeListener {  
        public void onShake();
    }  
  
    public ShakeListener(MediaPlaybackService service) { 
        mServices = service;
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mSoundId = mSoundPool.load(mServices, R.raw.shakecomplete, 1);
        resume();
    }  
  
    public void setOnShakeListener(OnShakeListener listener) {  
        mShakeListener = listener;  
    }  
  
    public void resume() {  
        mSensorMgr = (SensorManager) mServices.getSystemService(Context.SENSOR_SERVICE);  
        if (mSensorMgr == null) {  
            throw new UnsupportedOperationException("Sensors not supported");  
        }  
          
        boolean supported = mSensorMgr.registerListener(this, 
                mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),  
                SensorManager.SENSOR_DELAY_GAME);  
        if (!supported) {  
            mSensorMgr.unregisterListener(this);  
            throw new UnsupportedOperationException(  
                    "Accelerometer not supported");  
        }  
    }
  
    public void pause() {  
        if (mSensorMgr != null) {  
            mSensorMgr.unregisterListener(this);  
            mSensorMgr = null;  
        }  
        if(mSoundPool != null) {
            mSoundPool.unload(mSoundId);
        }
    }  
  
    @Override  
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  
          
    }  
  
    @SuppressWarnings("deprecation")
    @Override  
    public void onSensorChanged(SensorEvent event) {  
  
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {  
            return;  
        }       
        if(!mServices.isPlaying()) {
            return;
        }
  
        long now = System.currentTimeMillis();  
  
        if ((now - mLastTime) > TIME_THRESHOLD) {  
            
            x = event.values[SensorManager.DATA_X];  
            y = event.values[SensorManager.DATA_Y];  
//            z = event.values[SensorManager.DATA_Z];  
            
            int shakeDegree = MusicUtils.getIntPref(mServices, "shake_degree", DEFAULT_SHAKE_DEGREE);
            if (now - mLastShake > SHAKE_DURATION) {
                float xDiff = Math.abs(x - mLastX);
                float yDiff = Math.abs(y - mLastY);
                float XDegree = shakeDegree * 1.0F;
                float yDegree = shakeDegree * 0.5F;

                if ((xDiff > XDegree) && (yDiff > yDegree)) {
                    mLastShake = now;
                        if(mSoundPool != null) {
                            mSoundPool.play(mSoundId, 1.0F, 1.0F, 0, 0, 1.0F);
                            Intent intent = new Intent();
                            intent.setAction(MediaPlaybackService.NEXT_ACTION);                            
                            mServices.sendBroadcast(intent);
                    }
                }
            }

            mLastTime = now;
            mLastX = x;
            mLastY = y;
//            mLastZ = z;            
        } 
    }

}  