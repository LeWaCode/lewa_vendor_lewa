package com.lewa.face.preview.slide.local;

import java.io.File;
import java.util.ArrayList;


import com.lewa.face.R;
import com.lewa.face.app.ThemeApplication;
import com.lewa.face.model.ThemeModelCheckBoxPreference;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.pojos.ThemeModelInfo;
import com.lewa.face.preview.slide.adapter.LocalSlideAdapter;
import com.lewa.face.preview.slide.base.SlideBaseActivity;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeCopyTask;
import com.lewa.face.util.ThemeFileFilter;
import com.lewa.face.util.ThemeUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.lewa.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.lewa.face.util.Logs;

public class ThemePkg extends SlideBaseActivity{
    
    private static final String TAG = ThemePkg.class.getSimpleName();
    
    private TextView mThemeApply = null;
    private TextView mThemeShare = null;
    private TextView mThemeDelete = null;
    
    private LocalSlideAdapter localSlideAdapter = null;
    
    @Override
	protected void setContentView() {
		setContentView(R.layout.theme_preview_slide_has_model);
	}
    
    @Override
    protected PagerAdapter initAdapter() {
        localSlideAdapter = new LocalSlideAdapter(source, mContext);
        return localSlideAdapter;
    }
    
    @Override
    protected void initOtherViews() {
        TextView mThemeModeNumber = (TextView) findViewById(R.id.theme_mode_number);int modeNum = Integer.valueOf(themeBase.getThemeModelInfo()
				.getModelNames().size());
        
         int selected = themeBase.getThemeModelInfo().getModelFiles().contains(ThemeConstants.THEME_MODEL_FRAMEWORK)?modeNum-1:modeNum;
        
        mThemeModeNumber.setText(getString(R.string.theme_mode_number, selected));
        
        ImageView themeCheckInfo = (ImageView) findViewById(R.id.theme_check_info);
        themeCheckInfo.setOnClickListener(this);
        
        findViewById(R.id.theme_bottom_bar_online).setVisibility(View.GONE);
        mThemeApply = (TextView) findViewById(R.id.theme_apply);
        mThemeShare = (TextView) findViewById(R.id.theme_share);
        mThemeDelete = (TextView) findViewById(R.id.theme_delete);

        mThemeApply.setOnClickListener(this);
        mThemeShare.setOnClickListener(this);
        mThemeDelete.setOnClickListener(this);
    }
    
    /**
     * look up preview pictures
     * @return
     */
    protected ArrayList<String> getList() {
        
        String pkg = themeBase.getPkg();
        
        if(pkg == null){
            return null;
        }
        
        ArrayList<String> list = new ArrayList<String>();
        File preview = new File(new StringBuilder().append(ThemeConstants.THEME_LOCAL_PREVIEW).append("/").append(ThemeUtil.getNameNoBuffix(pkg)).toString());
        String[] previewSeq = ThemeConstants.PREVIEW_SEQ;
        for (int i = 0; i < previewSeq.length; i++) {
            forEachFiles(preview.listFiles(new ThemeFileFilter(previewSeq[i], ThemeFileFilter.INDEXOF)), list);
        }
        return list;   
   }
    
   private void forEachFiles(File[] files,ArrayList<String> list){
       if(files != null){
           for(File file : files){
               list.add(file.getAbsolutePath());
           }
       }
   } 

   @Override
   public void onClick(View v) {
       switch (v.getId()) {
       case R.id.theme_check_info:
       {
           Intent intent = new Intent();
           intent.setClass(mContext, ThemeModelCheckBoxPreference.class);
           intent.putExtra(ThemeConstants.THEMEBASE, themeBase);
           startActivity(intent);
           break;
       }
       case R.id.theme_apply:
       {
           new ThemeCopyTask(themeBase,mContext).execute("");
           break;
       }
       case R.id.theme_share:
       {
    	   ThemeUtil.shareByBT(themeBase,this);
           break;
       }
       case R.id.theme_delete:
       {
    	   if(ThemeConstants.DEFAULT_THEME_PKG.equals(themeBase.getPkg())){
    		   ThemeUtil.defaultThemeDialog(mContext);
    	   }else {
    		   ThemeUtil.deleteThemeDialog(this, themeBase);
    	   }
    	  
           break;
       }
       default:
           break;
       }
       
   }
  
   @Override
   protected void onDestroy() {
       ThemeApplication.activities.remove(this);
       if(localSlideAdapter != null){
           localSlideAdapter.onDestroy();
       }
       super.onDestroy();
   }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected ThemeBase initThemeBase(Intent intent) {
        String fromFileManger = intent.getStringExtra(ThemeConstants.FROM_FILE_MANAGER_KEY);
        /**
         * ���Intent�����������ļ�����
         */
        if(fromFileManger != null && fromFileManger.equals(ThemeConstants.FROM_FILE_MANAGER_VALUE)){
            String filePath = intent.getStringExtra(ThemeConstants.FILE_PATH_IN_FILE_MANAGER);
            
            if(filePath == null){
                ThemeUtil.showToast(this, R.string.lwt_file_not_exists, true);
                finish();
                return null;
            }
            
            File lwtFile = new File(filePath);
            
            if(!lwtFile.exists()){
                ThemeUtil.showToast(this, R.string.lwt_file_not_exists, true);
                finish();
                return null;
            }else {
                int position = filePath.lastIndexOf("/");
                /**
                 * �������������,�磺Ĭ��.lwt
                 */
                String themePkg = filePath.substring(position+1, filePath.length());
                
                String themeLocalPreviewPath = new StringBuilder().append(ThemeConstants.THEME_LOCAL_PREVIEW)
                    .append(ThemeUtil.getNameNoBuffix(themePkg))
                    .toString();
                
                File themeLocalPreview = new File(themeLocalPreviewPath);
                /**
                 * ��������Ӧ��Ԥ��ͼ��������Ϣ�Ƿ����
                 */
                if(!themeLocalPreview.exists()){
                    ThemeUtil.unZIPForThemeInfos(filePath, ThemeUtil.getNameNoBuffix(themePkg));
                }
                
                ThemeModelInfo themeModelInfo = new ThemeModelInfo(themePkg, filePath);
               // themeModelInfo.parseTheme(filePath);
                
                themeBase = new ThemeBase(themeModelInfo,themePkg,filePath,true);
                themeBase.setSize(ThemeUtil.fileLengthToSize(lwtFile.length()));
                
                Log.i(TAG, "themeBase == " + themeBase);
             }
        }else {
            themeBase = (ThemeBase) intent.getSerializableExtra(ThemeConstants.THEMEBASE); 
        }

        return themeBase;
    }
}
