package com.lewa.face.preview.slide.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.lewa.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

public class LockScreenWallPaper extends SlideBaseActivity{
    
    private static final String TAG = LockScreenWallPaper.class.getSimpleName();
    
    private static final int SELECT_PICTRUE = 1;
    
    private TextView mThemeApply = null;
    private TextView mThemeShare = null;
    private TextView mThemeDelete = null;
    
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
        .append("/").append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_WALLPAPER_PREFIX)
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
           setLockScreenWallPaper();
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
                    
                    String lockscreenwallPaperPath = getList().get(0);
                    
                    /**
                     * 删除锁屏壁纸本身
                     */
                    for(ThemeBase lockScreenWallPaper : lockScreenWallPapers){
                        if(lockScreenWallPaper.getPkg().equals(themePkg)){
                        
                            ThemeUtil.deleteFile(lockscreenwallPaperPath);
                            
                            ThemeUtil.deleteFile(new StringBuilder().append(ThemeConstants.THEME_LOCAL_WALLPAPER_THUMBNAIL).append("/").append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_WALLPAPER_PREFIX).append(themeName).toString());
                            
                            lockScreenWallPapers.remove(lockScreenWallPaper);
                            
                            ThemeHelper.saveThemeBases(mContext, lockScreenWallPapers, ThemeConstants.THEME_LOCAL, ThemeConstants.LSWALLPAPER);
                            break;
                        }
                        
                    }
                    /**
                     * 标识这张锁屏壁纸是不是在线壁纸库的。如果是，则删除其对应的壁纸对应的缩略图。（壁纸原图与锁屏壁纸原图为同一张图）
                     */
                    boolean lockscreenwallpaperIsOnline = false;
                    if(lockscreenwallPaperPath.indexOf("wallpaper_") == -1){
                        lockscreenwallpaperIsOnline = true;
                    }
                    
                    if(lockscreenwallpaperIsOnline){
                        
                        for(ThemeBase wallpaper : wallPapers){
                            if(wallpaper.getPkg().equals(themePkg)){
                                
                                ThemeUtil.deleteFile(new StringBuilder().append(ThemeConstants.THEME_LOCAL_WALLPAPER_THUMBNAIL).append("/").append(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX).append(themeName).toString());
                                
                                wallPapers.remove(wallpaper);
                                
                                ThemeHelper.saveThemeBases(mContext, wallPapers, ThemeConstants.THEME_LOCAL, ThemeConstants.WALLPAPER);
                                break; 
                            }
                            
                        }
                        
                    }
                    
                    Intent deleteTheme = new Intent();
                    deleteTheme.setAction(ThemeActions.DELETE_THEME_OVER);
                    LockScreenWallPaper.this.sendBroadcast(deleteTheme);
                    
                    LockScreenWallPaper.this.finish();
                        
                    
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
       ThemeApplication.activities.remove(this);
       localSlideAdapter.onDestroy();
       super.onDestroy();
   }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        // TODO Auto-generated method stub
        
    }
    
    private void setLockScreenWallPaper(){
        FileInputStream fis = null;
        FileOutputStream fos = null;
        
         try {
             
             String sourceFilePath = getList().get(0);
             File sourceFile = new File(sourceFilePath);
             if(sourceFile.exists()){
                 
                 File temp = createTempFile();
                 
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
                 if(bmpWidth < widthPix + 50 && bmpHeight < heightPix + 50){
                     
                     /**
                      * 不需要裁剪
                      */
                     createFile(temp, fis, fos);
                     
                     ThemeUtil.showToast(mContext, R.string.theme_set_success, true);
                     
                 }else {
                     /**
                      * 需要进行裁剪,并且有不同分辨率之分
                      */
                     if(ThemeUtil.isWVGA){
                         ThemeUtil.cropImage(this, sourceFilePath, 3, 5, 480, 800, SELECT_PICTRUE);
                     }else {
                         ThemeUtil.cropImage(this, sourceFilePath, 2, 3, 320, 480, SELECT_PICTRUE); 
                     }
                 }

             }else {
                 ThemeUtil.showToast(mContext, R.string.theme_set_fail, true);
            }
             
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             
             ThemeUtil.showToast(mContext, R.string.theme_set_fail, true);
             
         } finally{
             try {
                 if(fis != null){
                     fis.close();
                     fis = null;
                 }
                 if(fos != null){
                     fos.close();
                     fos = null;
                 }
             } catch (Exception e2) {
                 e2.printStackTrace();
             }
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
                 
                File temp = createTempFile();
                 
                fos = new FileOutputStream(temp);
                 
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
     * 创建锁屏壁纸临时文件
     * @return
     */
    private File createTempFile(){
        File temp = new File(ThemeConstants.THEME_FACE_LOCKSCREENWALLPAPER_TEMP);
        File parent = temp.getParentFile();
        if(!parent.exists()){
            parent.mkdirs();
            
            ThemeUtil.changeFilePermission(parent);
        }
        return temp;
    }
    
    /**
     * 锁屏壁纸文件的生成
     * @param temp
     * @param is
     * @param fos
     */
    private void createFile(File temp,InputStream is,FileOutputStream fos){
        
        ThemeUtil.writeSourceToTarget(is, fos);
        
        File target = new File(ThemeConstants.THEME_FACE_LOCKSCREENWALLPAPER);
        temp.renameTo(target);
        
        ThemeUtil.changeFilePermission(target);
        
        SharedPreferences.Editor editor = mContext.getSharedPreferences("CURRENT_USING", Context.MODE_PRIVATE).edit();
        editor.putString("lockscreenwallpaper", themeBase.getPkg())
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
