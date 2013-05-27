package com.lewa.player.ui;

import java.util.ArrayList;

import com.lewa.player.ExitApplication;
import com.lewa.player.R;
import com.lewa.player.ui.view.VerticalSeekBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class MusicEQActivity extends Activity {

    private Spinner eqSpinner;
    private AudioManager mAudioManager;
    private SeekBar volSeekBar;
    private VerticalSeekBar mLowerSeekBar;
    private VerticalSeekBar mLowSeekBar;
    private VerticalSeekBar mMiddleSeekBar;
    private VerticalSeekBar mHighSeekBar;
    private VerticalSeekBar mHigherSeekBar;
    private Button msureBtn;
    private Button mcanslBtn;
    public RadioButton mCheckBox;
	private SharedPreferences music_settings;
	private Editor prefsPrivateEditor;	
	private ArrayList<Short[]> genre = new ArrayList<Short[]>();
	private String[] GENRES = null;
	int eqchoise = 0;
	int mPreEqchoise = 0;
    private short[][] genres = {
    						  {0, 0, 0, 0, 0},
    						  {300, 0, 0, 0, 300},
    						  {500, 300, -200, 400, 400},
    						  {600, 0, 200, 400, 100},
    						  {300, 0, 0, 200, -100},
    						  {400, 100, 900, 300, 0},
    						  {500, 300, 0, 100, 300},
    						  {400, 200, -200, 200, 500},
    						  {-100, 200, 500, 100, -200},
    						  {500, 300, -100, 300, 500}};
    Short [] DIYlevels = new Short [5];
    Short [] TempLevels = new Short [5];
    boolean isCustom = false;
    
    public static final String ACTION_UPDATE_EQ = "com.lewa.player.EQUPDATE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
  
		setContentView(R.layout.music_eq);
		
		music_settings = this.getSharedPreferences("Music_setting", 0);
		prefsPrivateEditor = music_settings.edit();
		eqchoise = music_settings.getInt("whicheq", 0);
		mPreEqchoise = eqchoise;
		
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        eqSpinner = (Spinner) findViewById(R.id.eqChoose);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.eq_genres, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eqSpinner.setAdapter(adapter);
        eqSpinner.setSelection(eqchoise);
        
        volSeekBar = (SeekBar) findViewById(R.id.volSetSeekbar);
        setVolum();     
        volSeekBar.setOnSeekBarChangeListener(volChangeListener);
        new Thread(new volThread()).start();
        
        mLowerSeekBar = (VerticalSeekBar) findViewById(R.id.lowerSeekbar);
        mLowSeekBar = (VerticalSeekBar) findViewById(R.id.lowSeekbar);
        mMiddleSeekBar = (VerticalSeekBar) findViewById(R.id.middleSeekbar);
        mHighSeekBar = (VerticalSeekBar) findViewById(R.id.highSeekbar);
        mHigherSeekBar = (VerticalSeekBar) findViewById(R.id.higherSeekbar);
        
        mLowerSeekBar.setMax(2000);
        mLowSeekBar.setMax(2000);
        mMiddleSeekBar.setMax(2000);
        mHighSeekBar.setMax(2000);
        mHigherSeekBar.setMax(2000);
        
        msureBtn = (Button) findViewById(R.id.okayButton);
        mcanslBtn = (Button) findViewById(R.id.cancelButton);
        msureBtn.setOnClickListener(txtLisen);
        mcanslBtn.setOnClickListener(txtLisen);
        GENRES = getResources().getStringArray(R.array.eq_genres);

        eqSpinner.setOnItemSelectedListener(itemSelected);             
        
        mLowerSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        mLowSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        mMiddleSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        mHighSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        mHigherSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        init();

        ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
	}
	
	Handler volHandler = new Handler(){
        public void handleMessage(Message msg) {  
            switch (msg.what) {  
            case 1:  
                setVolum();  
                break;
           }
        }
    };
    
    private void setVolum() {        
        volSeekBar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        int pro = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volSeekBar.setProgress(pro);
    }     
    
    class volThread implements Runnable {   
        public void run() {  
            while (!Thread.currentThread().isInterrupted()) {

                Message message = new Message();
                message.what = 1;
                volHandler.sendMessage(message);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
	
    OnClickListener txtLisen = new OnClickListener(){

        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (v.getId() == R.id.okayButton) {
                prefsPrivateEditor.putInt("LowerEQ", TempLevels[0]);
                prefsPrivateEditor.putInt("LowEQ", TempLevels[1]);
                prefsPrivateEditor.putInt("MiddleEQ", TempLevels[2]);
                prefsPrivateEditor.putInt("HighEQ", TempLevels[3]);
                prefsPrivateEditor.putInt("HigherEQ", TempLevels[4]);
                prefsPrivateEditor.commit();
                prefsPrivateEditor.putInt("whicheq", eqchoise).commit();
                
                updateEq();             
            } else if (v.getId() == R.id.cancelButton) {
                restoreEq();                
            }
            finish();
        }
        
    };
    
    public void updateEq() {
        
        Intent intent = new Intent();
        intent.putExtra("levles", setEqAsString(TempLevels));
        intent.setAction(ACTION_UPDATE_EQ);
        
        sendBroadcast(intent);
    }
    
    public void restoreEq() {
        
        Intent intent = new Intent();
        intent.putExtra("levles", setEqAsString(DIYlevels));
        intent.setAction(ACTION_UPDATE_EQ);
        
        sendBroadcast(intent);
    }
    
    private String setEqAsString(Short[] levels) {
        String eqString = "";
        
        for(int i = 0;i < 5;i++) {
            
            eqString += levels[i].toString();
            if(i < 4) {
                eqString += ";";
            }
        }
        
        return eqString;
    }
    
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        restoreEq();
    }
    
    OnItemSelectedListener itemSelected = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            if(position >= 11){
                isCustom = false;
                return;
            }
            
            eqchoise = position;            
         
            if(position < genres.length) {
                mLowerSeekBar.setProgress(genres[position][0] + 1000);
                mLowSeekBar.setProgress(genres[position][1] + 1000);
                mMiddleSeekBar.setProgress(genres[position][2] + 1000);
                mHighSeekBar.setProgress(genres[position][3] + 1000);
                mHigherSeekBar.setProgress(genres[position][4] + 1000);
            } else if(DIYlevels.length != 0){
                mLowerSeekBar.setProgress(DIYlevels[0] + 1000);
                mLowSeekBar.setProgress(DIYlevels[1] + 1000);
                mMiddleSeekBar.setProgress(DIYlevels[2] + 1000);
                mHighSeekBar.setProgress(DIYlevels[3] + 1000);
                mHigherSeekBar.setProgress(DIYlevels[4] + 1000);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
            
        }
    };
    
    public void init() {
        for (int i = 0; i < genres.length; i++) {
            Short[] levels = new Short[5];
            levels[0] = genres[i][0];
            levels[1] = genres[i][1];
            levels[2] = genres[i][2];
            levels[3] = genres[i][3];
            levels[4] = genres[i][4];

            genre.add(levels);
        }
        short l1 = (short) music_settings.getInt("LowerEQ", 0);
        short l2 = (short) music_settings.getInt("LowEQ", 0);
        short l3 = (short) music_settings.getInt("MiddleEQ", 0);
        short l4 = (short) music_settings.getInt("HighEQ", 0);
        short l5 = (short) music_settings.getInt("HigherEQ", 0);

        DIYlevels[0] = l1;
        DIYlevels[1] = l2;
        DIYlevels[2] = l3;
        DIYlevels[3] = l4;
        DIYlevels[4] = l5;

        TempLevels[0] = l1;
        TempLevels[1] = l2;
        TempLevels[2] = l3;
        TempLevels[3] = l4;
        TempLevels[4] = l5;

        mLowerSeekBar.setProgress(l1 + 1000);
        mLowSeekBar.setProgress(l2 + 1000);
        mMiddleSeekBar.setProgress(l3 + 1000);
        mHighSeekBar.setProgress(l4 + 1000);
        mHigherSeekBar.setProgress(l5 + 1000);
    }

    public void setDIYeq(short i, short progress){
        TempLevels[i] = progress;
    }
    
    OnSeekBarChangeListener volChangeListener = new OnSeekBarChangeListener() {
        
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
            // TODO Auto-generated method stub
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
        }
        
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
            
        }
        
    };

    VerticalSeekBar.OnSeekBarChangeListener seekBarChangeListener = new VerticalSeekBar.OnSeekBarChangeListener() {

        public void onProgressChanged(VerticalSeekBar VerticalSeekBar,
                int progress, boolean fromUser) {
            
            if (VerticalSeekBar.getId() == R.id.lowerSeekbar) {
                setDIYeq((short) 0, (short) (progress - 1000));
            } else if (VerticalSeekBar.getId() == R.id.lowSeekbar) {
                setDIYeq((short) 1, (short) (progress - 1000));
            } else if (VerticalSeekBar.getId() == R.id.middleSeekbar) {
                setDIYeq((short) 2, (short) (progress - 1000));
            } else if (VerticalSeekBar.getId() == R.id.highSeekbar) {
                setDIYeq((short) 3, (short) (progress - 1000));
            } else if (VerticalSeekBar.getId() == R.id.higherSeekbar) {
                setDIYeq((short) 4, (short) (progress - 1000));
            }

            updateEq();
        }

        public void onStartTrackingTouch(VerticalSeekBar VerticalSeekBar) {
            // TODO Auto-generated method stub
            if(VerticalSeekBar.getId() != R.id.volSetSeekbar) {
                eqchoise = genre.size();
                eqSpinner.setSelection(GENRES.length - 1);
                isCustom = true;
            }
        }

        public void onStopTrackingTouch(VerticalSeekBar VerticalSeekBar) {
            // TODO Auto-generated method stub
            
        }

    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        isCustom = false;
        super.onDestroy();
    } 
}
