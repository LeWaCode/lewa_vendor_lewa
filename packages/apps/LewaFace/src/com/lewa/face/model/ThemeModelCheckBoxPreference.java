package com.lewa.face.model;

import java.util.ArrayList;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lewa.face.R;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeUtil;
import com.lewa.face.widget.IconCheckBoxPreferenceScreen;
import android.widget.Toast;

public class ThemeModelCheckBoxPreference extends PreferenceActivity implements OnClickListener{

	private static final String TAG = ThemeModelCheckBoxPreference.class.getSimpleName();
    
	private ThemeBase themeBase = null;
	private int modeNum = 0;
	private Button apply = null;
	private TextView totalModel = null;
	
    private PreferenceScreen preferenceScreen;
    private ArrayList<String> selectModelFiles = new ArrayList<String>();
    
    private int selected = 0;
	private ThemeModelCopyTask themeModelCopyTask;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.theme_model_preference);
        
        addPreferencesFromResource(R.xml.model);
        
        preferenceScreen = getPreferenceScreen();
        
	}
	private void init() {
		selectModelFiles.clear();preferenceScreen.removeAll();
        themeBase = (ThemeBase) getIntent().getSerializableExtra(ThemeConstants.THEMEBASE);
        if(themeBase.getThemeModelInfo().getContainModelNum() == null){
            Toast toast = Toast.makeText(this, R.string.theme_file_demaged, Toast.LENGTH_SHORT);
            toast.show();
            this.finish();
            return;
        }
		modeNum = Integer.valueOf(themeBase.getThemeModelInfo()
				.getModelNames().size());
        
        selected = themeBase.getThemeModelInfo().getModelFiles().contains(ThemeConstants.THEME_MODEL_FRAMEWORK)?modeNum-1:modeNum;
        
        
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inDensity = DisplayMetrics.DENSITY_DEFAULT;
        
        /**
         * 动态获得preference的个数
         */
        for(int i=0;i<modeNum;i++){
            String modelName = (String) getText(themeBase.getThemeModelInfo().getModelNames().get(i));
            if (modelName.equalsIgnoreCase(getString(R.string.theme_model_boots))
                    || modelName.equalsIgnoreCase(getString(R.string.theme_model_fonts))
                    || modelName.equalsIgnoreCase(getString(R.string.theme_model_icon_style))
                    || modelName.equalsIgnoreCase(getString(R.string.theme_model_launcher))
                    || modelName.equalsIgnoreCase(getString(R.string.theme_model_lockscreen_style))
                    || modelName.equalsIgnoreCase(getString(R.string.theme_model_lockscreen_wallpaper))
                    || modelName.equalsIgnoreCase(getString(R.string.theme_model_wallpaper))
					|| modelName
							.equalsIgnoreCase(getString(R.string.theme_model_systemui))
					|| modelName
							.equalsIgnoreCase(getString(R.string.theme_model_phone))
					|| modelName
							.equalsIgnoreCase(getString(R.string.theme_model_settings))
					|| modelName
							.equalsIgnoreCase(getString(R.string.theme_model_pim))) {
        	IconCheckBoxPreferenceScreen iconCheckBox = new IconCheckBoxPreferenceScreen(this);
        	        	
        	iconCheckBox.setTitle(modelName);
        	
        	String modelFile = themeBase.getThemeModelInfo().getModelFiles().get(i);
        	
        	Drawable drawable = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), modelBmp(modelFile), opts));
        	iconCheckBox.setmIcon(drawable);
        	
        	iconCheckBox.setKey(modelFile);
        	
				
        	
        	preferenceScreen.addPreference(iconCheckBox);
        	
            }
        }
		
        totalModel = (TextView) findViewById(R.id.totalModel);
		int modules = selected; 
		totalModel.setText(getString(R.string.theme_mode_number, modules+""));
		apply = (Button) findViewById(R.id.theme_apply_model);
		apply.setEnabled(modules != 0);
selected = 0;
        apply.setText(getString(R.string.theme_apply_model, "(" + selected + ")"));
        apply.setOnClickListener(this);
    }

    @Override
	protected void onStart() {
		super.onStart();
		init();
	}
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.theme_apply_model:
        {
            if(selected <= 0){
                ThemeUtil.showToast(this, R.string.theme_mode_empty_toast, true);
                return;
            }
			themeModelCopyTask = new ThemeModelCopyTask(themeBase, this,
					selectModelFiles);
			themeModelCopyTask.execute("");
            break;
        }
        default:
            break;
        }
        
    }

    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        // TODO Auto-generated method stub
        
        CheckBox checkBox = ((IconCheckBoxPreferenceScreen)preference).getmCheckBox();
        String modelFile = (String) preference.getKey();
        if(checkBox.isChecked()){
            checkBox.setChecked(false);
            --selected;
            selectModelFiles.remove(modelFile);
        }else {
            checkBox.setChecked(true);
            ++selected;
            selectModelFiles.add(modelFile);
        }
        ((IconCheckBoxPreferenceScreen)preference).setSelected(checkBox.isChecked());
        if(selected>0){
            apply.setText(getString(R.string.theme_apply_model, "(" + selected + ")"));
        }else {
            apply.setText(getString(R.string.theme_apply_model, ""));
        }
        
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    /**
     * 根据模块名获得对应的icon
     * @param modelFile
     * @return
     */
    private int modelBmp(String modelFile){
        if(modelFile.equals(ThemeConstants.THEME_MODEL_LOCKSCREEN_WALLPAPER)){
            return R.drawable.ls_wallpaper;
        }else if(modelFile.equals(ThemeConstants.THEME_MODEL_WALLPAPER)){
            return R.drawable.wallpaper;
        }else if(modelFile.equals(ThemeConstants.THEME_MODEL_LOCKSCREEN)){
            return R.drawable.ls_style;
        }else if(modelFile.equals(ThemeConstants.THEME_MODEL_ICONS)){
            return R.drawable.icons_style;
        }else if(modelFile.equals(ThemeConstants.THEME_MODEL_LAUNCHER)){
            return R.drawable.launcher_style;
        }else if(modelFile.equals(ThemeConstants.THEME_MODEL_BOOTS)){
            return R.drawable.ic_boot_animation;
        }else if(modelFile.equals(ThemeConstants.THEME_MODEL_FONTS)){
            return R.drawable.ic_text_style;
        }else if(modelFile.equals(ThemeConstants.THEME_MODEL_SYSTEMUI)){
            return R.drawable.ic_notification;
        }else if(modelFile.equals(ThemeConstants.THEME_MODEL_SETTINGS)){
            return R.drawable.ic_set;
        }else if(modelFile.equals(ThemeConstants.THEME_MODEL_PHONE)){
            return R.drawable.ic_calling;
        }else if(modelFile.equals(ThemeConstants.THEME_MODEL_PIM)){
            return R.drawable.ic_pm;
        }
        return R.drawable.icons_style;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
		if(themeModelCopyTask!=null){
		themeModelCopyTask.onProgressDialogDestroyed();
		}
        selectModelFiles.clear();
    }

    
}
