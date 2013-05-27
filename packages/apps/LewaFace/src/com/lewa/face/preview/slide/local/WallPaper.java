package com.lewa.face.preview.slide.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;


import com.lewa.face.R;
import com.lewa.face.app.ThemeApplication;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.preview.slide.adapter.LocalSlideAdapter;
import com.lewa.face.preview.slide.base.SlideBaseActivity;
import com.lewa.face.util.ThemeActions;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeHelper;
import com.lewa.face.util.ThemeUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.lewa.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class WallPaper extends SlideBaseActivity{
    
    private static final String TAG = WallPaper.class.getSimpleName();
    
    private static final int SELECT_PICTRUE = 1;

    private TextView mThemeApply = null;
    private TextView mThemeShare = null;
    private TextView mThemeDelete = null;
    
    Context context = null;
    private LocalSlideAdapter localSlideAdapter = null;
    
    @Override
	protected void setContentView() {
		setContentView(R.layout.theme_preview_slide_no_model);
	}
    
    @Override
    protected PagerAdapter initAdapter() {
        localSlideAdapter = new LocalSlideAdapter(source, mContext);
        return localSlideAdapter;
    }
    
    @Override
    protected void initOtherViews() {
        // TODO Auto-generated method stub
    	findViewById(R.id.theme_check_info).setVisibility(View.GONE);

    	findViewById(R.id.theme_bottom_bar_online).setVisibility(View.GONE);
    	mThemeApply = (TextView) findViewById(R.id.theme_apply);
        mThemeShare = (TextView) findViewById(R.id.theme_share);
        mThemeDelete = (TextView) findViewById(R.id.theme_delete);

        context = this;
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
        
        String nameNoLwt = ThemeUtil.getNameNoBuffix(pkg);
        
        ArrayList<String> list = new ArrayList<String>();
        
        String wallpaperPath = new StringBuilder().append(ThemeConstants.THEME_WALLPAPER)
        .append("/").append(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX)
        .append(nameNoLwt).toString();
        
        
        if(!new File(wallpaperPath).exists()){
            wallpaperPath = new StringBuilder().append(ThemeConstants.THEME_WALLPAPER)
            .append("/")
            .append(nameNoLwt).toString();
        }
        
        list.add(wallpaperPath);
        
        return list;   
   }
    

   @Override
   public void onClick(View v) {
       switch (v.getId()) {
       case R.id.theme_apply:
       {
           setWallPaper();
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
    		   AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
               builder.setTitle(R.string.theme_delete_confirm_title);
               builder.setMessage(R.string.theme_delete_confirm_msg);
               builder.setNegativeButton(R.string.theme_cancel,new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //do nothing
                    alertDialog.dismiss();
                }
            });
               builder.setPositiveButton(R.string.theme_ok, new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    
                    ArrayList<ThemeBase> wallPapers = ThemeHelper.getThemeBases(mContext, ThemeConstants.THEME_LOCAL, ThemeConstants.WALLPAPER);
                    
                    ArrayList<ThemeBase> lockScreenWallPapers = ThemeHelper.getThemeBases(mContext, ThemeConstants.THEME_LOCAL, ThemeConstants.LSWALLPAPER);
                    

                    String themePkg = themeBase.getPkg();
                    String themeName = themeBase.getName();
                    
                    String wallPaperPath = getList().get(0);
                    
                    /**
                     * 删除壁纸本身
                     */
                    for(ThemeBase wallpaper : wallPapers){
                        if(wallpaper.getPkg().equals(themePkg)){
                        
                            ThemeUtil.deleteFile(wallPaperPath);
                            
                            ThemeUtil.deleteFile(new StringBuilder().append(ThemeConstants.THEME_LOCAL_WALLPAPER_THUMBNAIL).append("/").append(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX).append(themeName).toString());
                            
                            wallPapers.remove(wallpaper);
                            
                            ThemeHelper.saveThemeBases(mContext, wallPapers, ThemeConstants.THEME_LOCAL, ThemeConstants.WALLPAPER);
                            break;
                        }
                        
                    }
                    /**
                     * 标识这张壁纸是不是在线壁纸库的。如果是，则删除其对应的锁屏壁纸对应的缩略图。（壁纸原图与锁屏壁纸原图为同一张图）
                     */
                    boolean wallpaperIsOnline = false;
                    if(wallPaperPath.indexOf("wallpaper_") == -1){
                        wallpaperIsOnline = true;
                    }
                    
                    if(wallpaperIsOnline){
                        
                        for(ThemeBase lockScreenWallPaper : lockScreenWallPapers){
                            if(lockScreenWallPaper.getPkg().equals(themePkg)){

                                ThemeUtil.deleteFile(new StringBuilder().append(ThemeConstants.THEME_LOCAL_WALLPAPER_THUMBNAIL).append("/").append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_WALLPAPER_PREFIX).append(themeName).toString());
                                
                                lockScreenWallPapers.remove(lockScreenWallPaper);
                                
                                ThemeHelper.saveThemeBases(mContext, lockScreenWallPapers, ThemeConstants.THEME_LOCAL, ThemeConstants.LSWALLPAPER);
                                break;
                            }
                            
                        }
                        
                    }
                    
                    //start add by zjyu 2012.5.24 change the sharedPreferences when delete the wallPaper
                    SharedPreferences sharedPreferences = context.getSharedPreferences("DOWNLOADED", Context.MODE_PRIVATE);
                    sharedPreferences.edit().putLong(themeBase.getPkg()  , ThemeConstants.DOWNLOADFAIL).commit();
                    //end
                    
                    Intent deleteTheme = new Intent();
                    deleteTheme.setAction(ThemeActions.DELETE_THEME_OVER);
                    WallPaper.this.sendBroadcast(deleteTheme);
                    
                    WallPaper.this.finish();
                        
                    
                }
            });
               alertDialog = builder.create();
               alertDialog.show();
    	   }
           
           break;
       }
       default:
           break;
       }
       
   }
   
   @Override
   protected void onDestroy() {
        if (localSlideAdapter != null) {
            localSlideAdapter.onDestroy();
        }
        ThemeApplication.activities.remove(this);
       super.onDestroy();
   }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        // TODO Auto-generated method stub
        
    }
    
    private class ApplyWallpaperTask extends AsyncTask<String, Void, Boolean>{

        private ProgressDialog progressDialog;
        
        private File temp;
        private FileInputStream source;
        private FileInputStream fis;
        private FileOutputStream fos;
        
        public ApplyWallpaperTask(File temp,FileInputStream source,FileInputStream fis,FileOutputStream fos){
            this.temp = temp;
            this.source = source;
            this.fis = fis;
            this.fos = fos;
        }
        
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(R.string.setting_wallpaper_title);
            progressDialog.setMessage(mContext.getString(R.string.setting_wallpaper_msg));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            
            try {
                WallpaperManager wm = (WallpaperManager) mContext.getSystemService(Context.WALLPAPER_SERVICE);
                
                wm.setStream(fis);
                createFile(temp, source, fos);
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }finally{
                try {
                    if(fos != null){
                        fos.close();
                        fos = null;
                    }
                    if(fis != null){
                        fis.close();
                        fis = null;
                    }
                    if(source != null){
                        source.close();
                        source = null;
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            return true;
               
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            
            progressDialog.dismiss();
            progressDialog = null;
            
            if(result){
                ThemeUtil.showToast(mContext, R.string.theme_set_success, true);
            }else{
                ThemeUtil.showToast(mContext, R.string.theme_set_fail, true);
            }
            
        }
        
        
        
    }
    
    private void setWallPaper(){
        
        /**
         * 这里操作流是一个异步的过程，所以对于流的关闭得在异步中
         */
        FileInputStream source = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        
        String sourceFilePath = getList().get(0);
        
        try {
            File sourceFile = new File(sourceFilePath);
            if(sourceFile.exists()){
                
                
                File temp = createTempFile();
                
                /**
                 * 因为文件流属于一次性消费品，不能重复利用，所以这里需要创建两个同样的对象
                 */
                source = new FileInputStream(sourceFilePath);
                fis = new FileInputStream(sourceFilePath);
                fos = new FileOutputStream(temp);
                
                /**
                 * 设置此options的目的只是为了获得bitmap的高和宽，而此时的bitmap为null
                 */
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                
                BitmapFactory.decodeFile(sourceFilePath, options); //此时返回的bitmap为空

                int bmpWidth = options.outWidth;
                int bmpHeight = options.outHeight;
                
                DisplayMetrics displayMetrics = new DisplayMetrics(); 
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                
                int widthPix = displayMetrics.widthPixels;
                int heightPix = displayMetrics.heightPixels;
                
                /**
                 * 如果图片的宽或者高大于手机分辨率的宽或者高，则表示需要进行裁剪，否则不需要裁剪
                 */
                if(bmpWidth < widthPix * 2 + 50 && bmpHeight < heightPix + 50){
                    
                    /**
                     * 不需要裁剪
                     */
                    new ApplyWallpaperTask(temp,source,fis,fos).execute("");
                    
                }else {
                    /**
                     * 需要进行裁剪,并且有不同分辨率之分
                     */
                    if(ThemeUtil.isWVGA){
                        ThemeUtil.cropImage(this, sourceFilePath, 6, 5, 960, 800, SELECT_PICTRUE);
                    }else {
                        ThemeUtil.cropImage(this, sourceFilePath, 6, 5, 576, 480, SELECT_PICTRUE); 
                    }
                }

            }else {
                ThemeUtil.showToast(mContext, R.string.theme_set_fail, true);
            }
            
             
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             
             ThemeUtil.showToast(mContext, R.string.theme_set_fail, true);
             
         }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        
        if(data == null){
            return;
        }
        
        if (resultCode == RESULT_OK) {
            
            InputStream is = null;
            FileOutputStream fos = null;
            
            Uri uri = data.getData();
            
            try {
                ContentResolver cr = this.getContentResolver();
                 
                is = cr.openInputStream(uri);
                WallpaperManager wm = (WallpaperManager) mContext.getSystemService(Context.WALLPAPER_SERVICE);
                
                wm.setStream(is);
                
                File temp = createTempFile();
                 
                fos = new FileOutputStream(temp);
                
                /**
                 * 对于一次性消费用品，再次使用需要重新创建
                 */
                is = cr.openInputStream(uri);
                createFile(temp,is,fos);
                
                ThemeUtil.showToast(mContext, R.string.theme_set_success, true);
                
            } catch (Exception e) {
                e.printStackTrace();
                
                ThemeUtil.showToast(mContext, R.string.theme_set_fail, true);
                
            }finally{
                try {
                    if(fos != null){
                        fos.close();
                        fos = null;
                    }
                    if(is != null){
                        is.close();
                        is = null;
                    }
                    if(uri != null){
                        File file = new File(new URI(uri.toString()));
                        if(file.exists()){
                            file.delete();
                        }
                    }
                    
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                
            }  
        }  
    }
    
    /**
     * 创建桌面壁纸临时文件
     * @return
     */
    private File createTempFile(){
        File temp = new File(ThemeConstants.THEME_FACE_WALLPAPER_TEMP);
        File parent = temp.getParentFile();
        if(!parent.exists()){
            parent.mkdirs();
            
            ThemeUtil.changeFilePermission(parent);
        }
        return temp;
    }
    
    /**
     * 桌面壁纸文件的生成
     * @param temp
     * @param is
     * @param fos
     */
    private void createFile(File temp,InputStream is,FileOutputStream fos){
        
        ThemeUtil.writeSourceToTarget(is, fos);
        
        File target = new File(ThemeConstants.THEME_FACE_WALLPAPER);
        temp.renameTo(target);
        
        ThemeUtil.changeFilePermission(target);
        
        SharedPreferences.Editor editor = mContext.getSharedPreferences("CURRENT_USING", Context.MODE_PRIVATE).edit();
        editor.putString("wallpaper", themeBase.getPkg())
        .commit();
        
        Intent applayThemeOver = new Intent();
        applayThemeOver.putExtra(ThemeConstants.THEMEBASE, themeBase);
        applayThemeOver.setAction(ThemeActions.APPLAY_THEME_OVER);
        mContext.sendBroadcast(applayThemeOver);
        
    }

    @Override
    protected ThemeBase initThemeBase(Intent intent) {
        
        themeBase = (ThemeBase) intent.getSerializableExtra(ThemeConstants.THEMEBASE); 
        return themeBase;
    }
  
}