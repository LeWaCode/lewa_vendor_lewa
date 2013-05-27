package com.lewa.face.local;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.lewa.face.R;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.pojos.ThemeModelInfo;
import com.lewa.face.util.ThemeActions;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeFileFilter;
import com.lewa.face.util.ThemeFileSort;
import com.lewa.face.util.ThemeHelper;
import com.lewa.face.util.ThemeUtil;
import com.lewa.face.widget.FontsPreferenceScreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

/**
 * 
 * @author fulw
 *
 */
public class Font extends PreferenceActivity{
    
    private static final String TAG = Font.class.getSimpleName();
   
    private PreferenceScreen preferenceScreen;
    
    private Drawable defaultThumbnail = null;
    
    private Drawable currentUsing = null;
    
    private Context context = null;
    
    private SharedPreferences sharedPreferences = null;
    
    /**
     * 临时的Preference
     */
    private Preference tempPreference;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        context = this;
        
        setContentView(R.layout.theme_fonts_preference);
        
        addPreferencesFromResource(R.xml.fonts);
        
        preferenceScreen = getPreferenceScreen();
        preferenceScreen.setOrderingAsAdded(true);
        
        defaultThumbnail = getResources().getDrawable(R.drawable.bg_text_style);
        
        currentUsing = getResources().getDrawable(R.drawable.theme_using);
        
        registerReceiver();
        
        sharedPreferences = getSharedPreferences("CURRENT_USING", Context.MODE_PRIVATE);
        
        ArrayList<ThemeBase> themeBases = getThemeBases();
        
        int size = themeBases.size();
        if((size == 1 && ThemeConstants.DEFAULT_THEME_PKG.equals(themeBases.get(0).getPkg())) || size == 0){
            
            Log.e(TAG, "The local.obj file is wrong and load data again");
            parseLocalModelInfo();
            
            /**
             * 此时数据有了更新
             */
            themeBases = getThemeBases();
            size = themeBases.size();
            
            addPreference(size, themeBases);
        }else {
            addPreference(size, themeBases);
        }
        
        
       
        
    }
    
    private void addPreference(int size,ArrayList<ThemeBase> themeBases){
        
        for(int i=0;i<size;i++){
            
            ThemeBase themeBase = themeBases.get(i);
            
            FontsPreferenceScreen fontsScreen = new FontsPreferenceScreen(context);
            
            String thumbnailPath = new StringBuilder().append(ThemeConstants.THEME_LOCAL_THUMBNAIL).append("/")
            .append(ThemeConstants.THEME_THUMBNAIL_FONTS_PREFIX).append(themeBase.getName()).toString();
            
            Drawable fontsThumbnail = Drawable.createFromPath(thumbnailPath);
            
            if(fontsThumbnail == null){
                fontsScreen.setmThumbnail(defaultThumbnail);
            }else {
                fontsScreen.setmThumbnail(fontsThumbnail);
            }
            
            if(sharedPreferences.getString("fonts", ThemeConstants.DEFAULT_THEME_PKG).equals(themeBase.getPkg())){
                fontsScreen.setmStatusFlag(currentUsing);
            }
            
            if(ThemeUtil.isEN){
                fontsScreen.setTitle(themeBase.getEnName());
            }else {
                fontsScreen.setTitle(themeBase.getCnName());
            }
            
            fontsScreen.setmThemeBase(themeBase);
            
            preferenceScreen.addPreference(fontsScreen);
        }
    }
    
    private void parseLocalModelInfo(){
        File fontsRoot = new File(ThemeConstants.THEME_MODEL_FONTS_STYLE);
        File[] localFonts = fontsRoot.listFiles(new ThemeFileFilter(ThemeConstants.THEME_MODEL_FONTS,ThemeFileFilter.STARTSWITH));
        if((localFonts != null) && (localFonts.length != 0)){
            
            Arrays.sort(localFonts, new ThemeFileSort());
            
            
            ArrayList<ThemeBase> themeBases = ThemeHelper.getThemeBases(context, ThemeConstants.THEME_LOCAL, ThemeConstants.FONTS);
            
            int length = localFonts.length;
            for(int i=0;i<length;i++){
                File fontsFile = localFonts[i];
                String fontsName = fontsFile.getName();
                
                String themePkg = new StringBuilder().append(fontsName.substring(fontsName.lastIndexOf("_") + 1)).append(ThemeConstants.BUFFIX_LWT).toString();
                
                /**
                 * 如果是默认主题资源，则返回
                 */
                if(themePkg.equals(ThemeConstants.DEFAULT_THEME_PKG)){
                    continue;
                }
                
                /**
                 * 如果模块存在，但是原始lwt文件不存在的话，则视为无效模块，因为主题信息在lwt文件中
                 */
                String lwtPath = new StringBuilder().append(ThemeConstants.THEME_LWT).append("/").append(themePkg).toString();
                if(!new File(lwtPath).exists()){
                    continue;
                }
                
                ThemeModelInfo themeModelInfo = new ThemeModelInfo(themePkg);
                ThemeBase fonts = new ThemeBase(themeModelInfo,themePkg,null,true);
                
                for(ThemeBase themePkgInfo : themeBases){
                    if(themePkg.equals(themePkgInfo.getPkg())){
                        fonts.setCnAuthor(themePkgInfo.getCnAuthor());
                        fonts.setEnAuthor(themePkgInfo.getEnAuthor());
                        fonts.setCnName(themePkgInfo.getCnName());
                        fonts.setEnName(themePkgInfo.getEnName());
                        break;
                    }
                }
               
                fonts.setPkg(themePkg);
                fonts.setSize(ThemeUtil.fileLengthToSize(fontsFile.length()));
                /**
                 * 临时FC解决办法
                 */
                if(themeBases.size() == 0){
                    themeBases.add( fonts);
                }else{
                    themeBases.add(ThemeConstants.SECOND,fonts);
                }
            }
            ThemeHelper.saveThemeBases(context, themeBases, ThemeConstants.THEME_LOCAL, ThemeConstants.FONTS);
        }
    }
    
    private void registerReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ThemeActions.ADD_THEME_OVER);
        intentFilter.addAction(ThemeActions.DELETE_THEME_OVER);
        intentFilter.addAction(ThemeActions.APPLAY_THEME_OVER);
        this.registerReceiver(themeChanged, intentFilter);
    }
    
    private ArrayList<ThemeBase> getThemeBases() {
        // TODO Auto-generated method stub
        return ThemeHelper.getThemeBases(context,ThemeConstants.THEME_LOCAL,ThemeConstants.FONTS);
    }

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		
        Intent intent = new Intent();
        intent.setClass(context, com.lewa.face.preview.slide.local.Font.class);
        intent.putExtra(ThemeConstants.THEMEBASE, ((FontsPreferenceScreen)preference).getmThemeBase());
        this.tempPreference = preference;
        startActivity(intent);
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	private BroadcastReceiver themeChanged = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(ThemeActions.DELETE_THEME_OVER.equals(action)){
                if(preferenceScreen != null && tempPreference != null){
                    preferenceScreen.removePreference(tempPreference);
                }
            }else if(ThemeActions.ADD_THEME_OVER.equals(action)){
                
                ThemeBase themeBase = (ThemeBase) intent.getSerializableExtra(ThemeConstants.THEMEBASE);
                
                if(themeBase != null){
                    FontsPreferenceScreen fontsScreen = new FontsPreferenceScreen(Font.this);
                    
                    String thumbnailPath = new StringBuilder().append(ThemeConstants.THEME_LOCAL_THUMBNAIL).append("/")
                    .append(ThemeConstants.THEME_THUMBNAIL_FONTS_PREFIX).append(themeBase.getName()).toString();
                    
                    Drawable fontsThumbnail = Drawable.createFromPath(thumbnailPath);
                    
                    if(fontsThumbnail == null){
                        fontsScreen.setmThumbnail(defaultThumbnail);
                    }else {
                        fontsScreen.setmThumbnail(fontsThumbnail);
                    }
                    
                    if(ThemeUtil.isEN){
                        fontsScreen.setTitle(themeBase.getEnName());
                    }else {
                        fontsScreen.setTitle(themeBase.getCnName());
                    }
                    
                    fontsScreen.setmThemeBase(themeBase);
                    
                    preferenceScreen.addPreference(fontsScreen);
                }
                
                
            }

        }
    };
    
}
