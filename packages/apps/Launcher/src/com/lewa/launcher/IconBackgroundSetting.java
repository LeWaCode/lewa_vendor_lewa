package com.lewa.launcher;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.io.IOException;
import java.io.InputStream;




import com.lewa.launcher.theme.ThemeConstants;
import com.lewa.launcher.theme.ThemeUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.Log;

/*add for set icon background by pan
 */

public class IconBackgroundSetting {

    // private static final String TAG = "IconBackgroundSetting";

    // favorite bar
    // dialer contacts browser mms
    private static String dialtCls = "com.android.contacts.DialtactsActivity";
    private static String mmsCls = "com.android.mms.ui.ConversationList";
    private static String contactsCls = "com.android.contacts.DialtactsContactsEntryActivity";
    private static String browserCls = "com.android.browser.BrowserActivity";
    private static String dialtPkg = "com.android.contacts";
    private static String mmsPkg = "com.android.mms";
    private static String contactsPkg = "com.android.contacts";
    private static String browserPkg = "com.android.browser";

    private static String cameraCls = "com.android.camera.Camera";
    private static String cameraPkg = "com.android.camera";

    private static String clockCls = "com.android.deskclock.DeskClock";
    private static String clockPkg = "com.android.deskclock";

    private static String galleryCls = "com.android.camera.GalleryPicker";
    private static String galleryPkg = "com.android.gallery";
    
    private static String themeCls = "com.lewa.face.PkgMain";
    private static String themePkg = "com.lewa.face";

    private static String settingsCls = "com.android.settings.Settings";
    private static String settingsPkg = "com.android.settings";

    private static String recoderCls = "com.android.soundrecorder.SoundRecorder";
    private static String recoderPkg = "com.android.soundrecorder";
    
    private static String calculatorCls = "com.android.calculator2.Calculator";
    private static String calculatorPkg = "com.android.calculator2";
    
    private static String filemanagerCls = "com.lewa.app.filemanager.ui.FileActivity";
    private static String filemanagerPkg = "com.lewa.app.filemanager";
    
    private static String suCls = "com.noshufou.android.su.Su";
    private static String suPkg = "com.noshufou.android.su";
    
    private static String downloadsCls = "com.android.providers.downloads.ui.DownloadList";
    private static String downloadsPkg = "com.android.providers.downloads.ui";
    
    private static String updaterCls = "com.lewa.updater.UpdaterActivity";
    private static String updaterPkg = "com.lewa.updater";
    
    private static String interceptCls = "com.lewa.intercept.MainActivity";
    private static String interceptPkg = "com.lewa.intercept";

    private static String feedbackCls = "com.lewa.feedback.AndroidFeedbackActivity";
    private static String feedbackPkg = "com.lewa.feedback";

    // Icon by us
    private static String browser = "mobi_mgeek_tunnybrowser_browseractivity";
    private static String contacts = "com_android_contacts_dialtactscontactsentryactivity";
    private static String dialt = "com_android_contacts_dialtactsactivity";
    private static String mms = "com_android_mms_ui_conversationlist";
    private static String calendar = "com_android_calendar_launchactivity";
    private static String camera = "com_android_camera_camera";
    private static String gallery = "com_android_camera_gallerypicker";
    private static String email = "com_android_email_activity_welcome";
    private static String music = "com_android_music_musicbrowseractivity";
    private static String settings = "com_android_settings_settings";
    private static String video = "com_android_music_videobrowseractivity";
    private static String clock = "com_android_deskclock_deskclock";
    private static String lewahome = "com_lewa_launcher_launcher";
    private static String download = "com_android_providers_downloads_ui_downloadlist";
    private static String search = "com_android_quicksearchbox_searchactivity";
    private static String voicerecord = "com_android_soundrecorder_soundrecorder";
    private static String calculator = "com_android_calculator2_calculator";
    private static String fm = "com_android_fm_radio_fmradio";
    private static String lewatheme = "com_lewa_face_pkgmain";
    private static String lewa_filemanager = "com_lewa_app_filemanager_ui_fileactivity";
    private static String lewa_cstore = "com_uucun50000868_android_cms_activity_marketloginandregisteractivity";                      
    private static String lewa_spm = "com_lewa_spm_activity_spmactivity";
    private static String lewa_authorization = "com_noshufou_android_su_su";
    private static String lewa_updater = "com_lewa_updater_updateractivity";
    private static String lewa_intercept = "com_lewa_intercept_mainactivity";
    private static String lewa_gallery = "com_alensw_picfolder_galleryactivity";
    private static String lewa_feedback = "com_lewa_feedback_androidfeedbackactivity";
    private static String lewa_game = "com_socogame_ppc_playplusclientactivity";
    private static String lewa_map = "com_autonavi_minimap_noteactivity";
    private static String a60_fm = "com_mediatek_fmradio_fmradioactivity";
    private static String a60_camera = "com_mediatek_camera_camera";
    private static String a60_cameraCls = "com.mediatek.camera.Camera";
    private static String a60_cameraPkg = "com.mediatek.camera";
    private static String lewa_search = "com_lewa_search_lewasearchactivity";

    private static Context mContext;

    private static final Rect sOldBounds = new Rect();
    private static Canvas sCanvas = new Canvas();
    static String phoneType;
    private static final boolean DEBUG = false;
    /**
    * data/system/face目录中是否包含icons这一个文件
    */
    private boolean containIconsFile = false;
    /**
    * 应用的背景图片
    */
    private Drawable bgIconDrawable = null;
    
    public IconBackgroundSetting(Context context, String phone, String romtype,
            String modelVersion) {
            
        mContext = context;
        
        containIconsFile = containIconsFile();
        
        if(containIconsFile){
            bgIconDrawable = getBGIcon();
        }
        
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        if (Launcher.mIsRom) {
            phoneType = phone;
            
            if ("u880".equals(phone)) {
                fm = "com_marvell_fmradio_mainactivity";
            } else if ("Lenovo A60".equals(phone)) {
            	camera = a60_camera;
            	cameraCls = a60_cameraCls;
            	cameraPkg = a60_cameraPkg;
              fm = a60_fm;
            }

            dialt = PhonesClassNameEnum.CM_DIALER.getPhoneClassname();
            dialtCls = PhonesClassNameEnum.CM_DIALERCLS.getPhoneClassname();
            dialtPkg = PhonesClassNameEnum.CM_DIALERPKG.getPhoneClassname();
            contacts = PhonesClassNameEnum.CM_CONTACT.getPhoneClassname();
            contactsCls = PhonesClassNameEnum.CM_CONTACTCLS.getPhoneClassname();
            contactsPkg = dialtPkg;
            mms = PhonesClassNameEnum.CM_MMS.getPhoneClassname();
            mmsCls = PhonesClassNameEnum.CM_MMSCLS.getPhoneClassname();
            mmsPkg = dialtPkg;
            music = PhonesClassNameEnum.CM_MUSIC.getPhoneClassname();
            calendar = PhonesClassNameEnum.CM_CALENDAR.getPhoneClassname();
            video = PhonesClassNameEnum.CM_VIDEO.getPhoneClassname();
        } else {
            setApps(phone, romtype, modelVersion);
        }
    }

    public IconBackgroundSetting() {
    }

    /**
    *是否包含icons这一个文件
    */
    private boolean containIconsFile(){

        File icons = new File("/data/system/face/icons");
        if(icons.exists()){
            return true;
        }
        return false;
        
    }
    //
     /**
    *是否包含某个应用的icon
    */
   
    public static Drawable getAppIcon(Context ctx, String name, String packageName){
        String findName;
        findName = ThemeUtils.getAppIconFileName(ctx, name, packageName);
        if(findName != null){
           Drawable drawable = ThemeUtils.getThemeDrawable(ctx,
                    ThemeConstants.THEME_ICONS_MODEL, findName);

            if (drawable != null) {
                Utilities.setDefaultValue(true);
                drawable = Utilities.createIconThumbnail(drawable, ctx);
                return drawable;
            }
            Log.e("Launcher", "ERROR, getThemeDrawable() failed! "+findName);
        }
        return null;
    }
    
    /**
    * 获得背景图片
    */
    private Drawable getBGIcon(){
        InputStream is = null;
        ZipFile zipFile = null;
        try {
            String picturePath = new StringBuilder().append("/data/system/face/icons").toString();
            File file = new File(picturePath);
            zipFile = new ZipFile(file);
            ZipEntry zipEntry = zipFile.getEntry("com_android_iconbg.png");
            if(zipEntry != null){
                is = zipFile.getInputStream(zipEntry);
                return bitmap2drawable(mContext, BitmapFactory.decodeStream(is));
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }finally{
            try {
                if(is != null){
                    is.close();
                    is = null;
                }
                if(zipFile != null){
                    zipFile.close();
                    zipFile = null;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public Drawable getBackGroundResources() {

        Drawable drawable = null;


        Bitmap bitmap = null;
        int sIconHeight = (int) mContext.getResources().getDimension(R.dimen.app_lewaicon_size);
        
        if (containIconsFile) {

            if (bgIconDrawable == null) {
                Log.e("IconBackgroundSetting", "iconbg is not exist");
                Drawable drawableIconBG = mContext.getResources().getDrawable(
                        R.drawable.com_android_iconbg);
                Utilities.setDefaultValue(true);
                if (drawableIconBG != null
                        && sIconHeight != drawableIconBG.getIntrinsicWidth()) {
                    Launcher.mIsNeedChanged = true;
                }
                drawable = Utilities.createIconThumbnail(drawableIconBG,
                        mContext);
            } else {
                Utilities.setDefaultValue(true);
                
                Drawable icon = bgIconDrawable;
                
                if (icon != null && sIconHeight != icon.getIntrinsicWidth()) {
                    Launcher.mIsNeedChanged = true;
                }
                drawable = Utilities.createIconThumbnail(icon, mContext);
            }

        } else {
        
            drawable = bitmap2drawable(R.drawable.com_android_iconbg);
            
            Utilities.setDefaultValue(true);
            
            if (drawable != null && sIconHeight != drawable.getIntrinsicWidth()) {
                Launcher.mIsNeedChanged = true;
            }
            
            drawable = Utilities.createIconThumbnail(drawable, mContext);
        }

        return drawable;
    }

    public Options getBitmapOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inDither = false;

        return options;
    }

    private Drawable bitmap2drawable(Context context, Bitmap bitmap){
       return new BitmapDrawable(context.getResources(), bitmap);
    }
    
    public Drawable bitmap2drawable(int id) {
        return new BitmapDrawable(mContext.getResources(),
                BitmapFactory.decodeStream(mContext.getResources()
                        .openRawResource(id), null, getBitmapOptions()));
    }

    public Drawable getIconResources(String name) {

        Drawable drawable = null;

        int resId = isLewaIcon(name);

        if (resId != -1) {
            drawable = bitmap2drawable(resId);
            Utilities.setDefaultValue(true);
            drawable = Utilities.createIconThumbnail(drawable, mContext);
        }
        return drawable;
    }
    
    public static int isLewaIcon(String name) {
        int resId = -1;

        if (name == null) {
            return -1;
        }

        if (browser.equals(name)) {
            resId = R.drawable.com_android_browser;
        } else if (calendar.equals(name)) {
            resId = R.drawable.com_android_calendar;
        } else if (camera.equals(name)) {
            resId = R.drawable.com_android_camera;
        } else if (gallery.equals(name)) {
            resId = R.drawable.com_android_gallery;
        } else if (dialt.equals(name)) {
            resId = R.drawable.com_android_dialt;
        } else if (contacts.equals(name)) {
            resId = R.drawable.com_android_contacts;
        } else if (email.equals(name)) {
            resId = R.drawable.com_android_email;
        } else if (mms.equals(name)) {
            resId = R.drawable.com_android_mms;
        } else if (music.equals(name)) {
            resId = R.drawable.com_android_music;
        } else if (settings.equals(name)) {
            resId = R.drawable.com_android_settings;
        } else if (video.equals(name)) {
            resId = R.drawable.com_android_video;
        } else if (clock.equals(name)) {
            resId = R.drawable.com_android_deskclock;
        } else if (lewahome.equals(name)) {
            resId = R.drawable.com_lewa_launcher_home;
        } else if (download.equals(name)) { 
            resId = R.drawable.com_android_download; }
         else if (search.equals(name)) {
            resId = R.drawable.com_android_search;
        } else if (voicerecord.equals(name)) {
            resId = R.drawable.com_android_record;
        } else if (calculator.equals(name)) {
            resId = R.drawable.com_android_calculator;
        } else if (fm.equals(name)) {
            resId = R.drawable.com_android_fm;
        } else if (lewatheme.equals(name)) {
            resId = R.drawable.com_android_theme;
        } else if(lewa_filemanager.equals(name)) {
            resId = R.drawable.com_android_filemanager;
        } else if(lewa_cstore.equals(name)) {
            resId = R.drawable.com_android_cstore;
        } else if(lewa_spm.equals(name)) {
            resId = R.drawable.com_android_spm;
        } else if(lewa_authorization.equals(name)) {
            resId = R.drawable.com_android_authorization;
        } else if(lewa_updater.equals(name)) {
            resId = R.drawable.com_android_updater;
        } else if(lewa_intercept.equals(name)) {
            resId = R.drawable.com_android_intercept;
        } else if(lewa_gallery.equals(name)) {
            resId = R.drawable.com_android_gallery;
        } else if(lewa_feedback.equals(name)) {
            resId = R.drawable.com_android_feedback;
        } else if(lewa_game.equals(name)) {
        	resId = R.drawable.com_android_game;
        } else if(lewa_map.equals(name)) {
        	resId = R.drawable.com_android_map;
        } else if(lewa_search.equals(name)) {
        	resId = R.drawable.com_android_search;
        }
        return resId;
    }

    public Drawable getIconResources(String name, Context context) {
        Drawable drawable = null;
        if (name == null) {
            return drawable;
        }

        if (browser.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com.android.browser");
        } else if (calendar.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_calendar");
        } else if (camera.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_camera");
        } else if (gallery.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_gallery");
        } else if (dialt.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_dialt");
        } else if (contacts.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_contacts");
        } else if (email.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_email");
        } else if (mms.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_mms");
        } else if (music.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_music");
        } else if (settings.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_settings");
        } else if (video.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_video");
        } else if (clock.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_deskclock");
        } else if (lewahome.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_lewa_launcher_home");
        } else if (download.equals(name)) { 
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_download"); 
        } else if (search.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_search");
        } else if (voicerecord.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_record");
        } else if (fm.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_fm");
        } else if (calculator.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_calculator");
        } else if (lewatheme.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_theme");
        } else if(lewa_filemanager.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_filemanager");
        } else if(lewa_cstore.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_cstore");
        } else if(lewa_spm.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_spm");
        } else if(lewa_authorization.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_authorization");
        } else if(lewa_updater.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_updater");
        } else if(lewa_intercept.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_intercept");
        }  else if(lewa_gallery.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_gallery");
        } else if(lewa_feedback.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_feedback");
        } else if("com_android_mask".equals(name)){
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_mask");
        } else if(lewa_game.equals(name)) {
            drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_game");
        } else if(lewa_map.equals(name)) {
        	drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_android_map");
        } else if(lewa_search.equals(name)) {
        	drawable = ThemeUtils.getThemeDrawable(context,
                    ThemeConstants.THEME_ICONS_MODEL, "com_lewa_search");
        }

        if (drawable != null) {
            Utilities.setDefaultValue(true);
            drawable = Utilities.createIconThumbnail(drawable, context);
        }

        return drawable;
    }

    Drawable createIconThumbnail(Drawable icon, Context context) {
        final Resources resources = context.getResources();
        int sIconWidth = 0;
        int sIconHeight = 0;
        sIconWidth = sIconHeight = (int) resources.getDimension(R.dimen.app_icon_size);

        // End
        int width = sIconWidth;
        int height = sIconHeight;

        float scale = 1.0f;
        if (icon instanceof PaintDrawable) {
            PaintDrawable painter = (PaintDrawable) icon;
            painter.setIntrinsicWidth(width);
            painter.setIntrinsicHeight(height);
        } else if (icon instanceof BitmapDrawable) {
            // Ensure the bitmap has a density.
            BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                bitmapDrawable.setTargetDensity(context.getResources()
                        .getDisplayMetrics());
            }
        }
        int iconWidth = icon.getIntrinsicWidth();
        int iconHeight = icon.getIntrinsicHeight();

        if (width > 0 && height > 0) {
            if (width < iconWidth || height < iconHeight || scale != 1.0f) {
                final float ratio = (float) iconWidth / iconHeight;

                if (iconWidth > iconHeight) {
                    height = (int) (width / ratio);
                } else if (iconHeight > iconWidth) {
                    width = (int) (height * ratio);
                }

                final Bitmap.Config c = icon.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
                final Bitmap thumb = Bitmap.createBitmap(sIconWidth,
                        sIconHeight, c);
                final Canvas canvas = sCanvas;
                canvas.setBitmap(thumb);
                // Copy the old bounds to restore them later
                // If we were to do oldBounds = icon.getBounds(),
                // the call to setBounds() that follows would
                // change the same instance and we would lose the
                // old bounds
                sOldBounds.set(icon.getBounds());
                final int x = (sIconWidth - width) / 2;
                final int y = (sIconHeight - height) / 2;
                icon.setBounds(x, y, x + width, y + height);
                icon.draw(canvas);
                icon.setBounds(sOldBounds);
                icon = new FastBitmapDrawable(thumb);
            } else if (iconWidth < width && iconHeight < height) {
                final Bitmap.Config c = Bitmap.Config.ARGB_8888;
                final Bitmap thumb = Bitmap.createBitmap(sIconWidth,
                        sIconHeight, c);
                final Canvas canvas = sCanvas;
                canvas.setBitmap(thumb);
                sOldBounds.set(icon.getBounds());
                final int x = (width - iconWidth) / 2;
                final int y = (height - iconHeight) / 2;
                icon.setBounds(x, y, x + iconWidth, y + iconHeight);
                icon.draw(canvas);
                icon.setBounds(sOldBounds);
                icon = new FastBitmapDrawable(thumb);
            }
        }
        return icon;
    }

    public static ComponentName getComponentName(String title) {
        if (title == null) {
            return null;
        }
        ComponentName cn = null;
        if (title.equals("dialt")) {
            cn = new ComponentName(dialtPkg, dialtCls);
        } else if (title.equals("mms")) {
            cn = new ComponentName(mmsPkg, mmsCls);
        } else if (title.equals("contacts")) {
            cn = new ComponentName(contactsPkg, contactsCls);
        } else if (title.equals("browser")) {
            cn = new ComponentName(browserPkg, browserCls);
        } else if (title.equals("theme")) {
            cn = new ComponentName(themePkg, themeCls);
        } else if (title.equals("camera")) {
            cn = new ComponentName(cameraPkg, cameraCls);
        } else if (title.equals("recorder")) {
            cn = new ComponentName(recoderPkg, recoderCls);
        } else if (title.equals("settings")) {
            cn = new ComponentName(settingsPkg, settingsCls);
        } else if (title.equals("clock")) {
            cn = new ComponentName(clockPkg, clockCls);
        } else if (title.equals("calculator")) {
            cn = new ComponentName(calculatorPkg, calculatorCls);
        } else if (title.equals("filemanager")) {
            cn = new ComponentName(filemanagerPkg, filemanagerCls);
        } else if (title.equals("su")) {
            cn = new ComponentName(suPkg, suCls);
        } else if (title.equals("downloads")) {
            cn = new ComponentName(downloadsPkg, downloadsCls);
        } else if (title.equals("updater")) {
            cn = new ComponentName(updaterPkg, updaterCls);
        } else if (title.equals("intercept")) {
            cn = new ComponentName(interceptPkg, interceptCls);
        } else if (title.equals("feedback")) {
              cn = new ComponentName(feedbackPkg, feedbackCls);
        }
        return cn;
    }

    private void setApps(String phonetype, String romtype, String modelVersion) {
        final String phonetypes[] = { "Cyan", "MIUI", "ZTE-U V880",
                "HTC Desire", "Desire HD", "HTC Incredible S", "ME722",
                "MB525", "HTC Desire S", "HTC Sensation Z710e", "MotoA953",
                "ZTE-BLADE", "MT15i", "W721", "HTC Legend", "GT-S5830",
                "GT-S5570", "ZTE-T U880" };
        final int ROM_CM = 0;
        final int ROM_MIUI = 1;
        final int TYPE_ZTEV880 = 2;
        final int TYPE_HTCG7 = 3;
        final int TYPE_HTCHD = 4;
        final int TYPE_HTCIS = 5;
        final int TYPE_ME722 = 6;
        final int TYPE_MB525 = 7;
        final int TYPE_HTCDS = 8;
        final int TYPE_HTCZE = 9;
        final int TYPE_MOTOA953 = 10;
        final int TYPE_ZTEBLADE = 11;
        final int TYPE_MT15I = 12;
        final int TYPE_W721 = 13;
        final int TYPE_HTCG6 = 14;
        final int TYPE_GTS5830 = 15;
        final int TYPE_GTS5570 = 16;
        final int TYPE_U880 = 17;
        int type = 0;
        for (int i = 0; i < phonetypes.length; i++) {
            if (phonetypes[i].equals(romtype.substring(0,
                    romtype.length() > 4 ? 4 : romtype.length()))) {
                type = i;
                break;
            }
            if (phonetypes[i].equals(modelVersion.substring(0,
                    modelVersion.length() > 4 ? 4 : modelVersion.length()))) {
                type = i;
                break;
            }
            if (phonetypes[i].equals(phonetype)) {
                type = i;
                break;
            }
        }
        switch (type) {
        case ROM_CM:
            gallery = PhonesClassNameEnum.CM_GALLERY.getPhoneClassname();
            galleryCls = PhonesClassNameEnum.CM_GALLERYCLS.getPhoneClassname();
            galleryPkg = PhonesClassNameEnum.CM_GALLERYPKG.getPhoneClassname();
            break;
        case ROM_MIUI:
            browser = PhonesClassNameEnum.MIUI_BROWSER.getPhoneClassname();
            dialt = PhonesClassNameEnum.MIUI_DIALER.getPhoneClassname();
            mms = PhonesClassNameEnum.MIUI_MMS.getPhoneClassname();
            camera = PhonesClassNameEnum.MIUI_CAMERA.getPhoneClassname();
            gallery = PhonesClassNameEnum.MIUI_GALLERY.getPhoneClassname();
            music = PhonesClassNameEnum.MIUI_MUSIC.getPhoneClassname();
            // voicedialer = PhonesClassNameEnum.MIUI_VIDEO.getPhoneClassname();
            dialtCls = PhonesClassNameEnum.MIUI_LAB.getPhoneClassname();
            mmsCls = PhonesClassNameEnum.MIUI_RAB.getPhoneClassname();
            browserCls = PhonesClassNameEnum.MIUI_RAB2.getPhoneClassname();
            galleryCls = PhonesClassNameEnum.MIUI_GALLERYCLS
                    .getPhoneClassname();
            galleryPkg = PhonesClassNameEnum.MIUI_GALLERYPKG
                    .getPhoneClassname();
            cameraCls = PhonesClassNameEnum.MIUI_CAMERACLS.getPhoneClassname();
            cameraPkg = PhonesClassNameEnum.MIUI_CAMERAPKG.getPhoneClassname();
            break;
        case TYPE_ZTEV880:
        case TYPE_ZTEBLADE:
            dialt = PhonesClassNameEnum.ZTEV880_DIALER.getPhoneClassname();
            gallery = PhonesClassNameEnum.ZTEV880_GALLERY.getPhoneClassname();
            // maps = PhonesClassNameEnum.ZTEV880_MAP.getPhoneClassname();
            clock = PhonesClassNameEnum.ZTEV880_CLOCK.getPhoneClassname();
            video = PhonesClassNameEnum.ZTEV880_VIDEO.getPhoneClassname();
            dialtCls = PhonesClassNameEnum.ZTEV880_LAB.getPhoneClassname();
            dialtPkg = PhonesClassNameEnum.ZTEV880_LABPN.getPhoneClassname();
            galleryCls = PhonesClassNameEnum.ZTEV880_GALLERYCLS
                    .getPhoneClassname();
            galleryPkg = PhonesClassNameEnum.ZTEV880_GALLERYPKG
                    .getPhoneClassname();
            clockCls = PhonesClassNameEnum.ZTEV880_CLOCKCLS.getPhoneClassname();
            clockPkg = PhonesClassNameEnum.ZTEV880_CLOCKPKG.getPhoneClassname();
            break;
        case TYPE_U880:
            gallery = PhonesClassNameEnum.CM_GALLERY.getPhoneClassname();
            galleryCls = PhonesClassNameEnum.CM_GALLERYCLS.getPhoneClassname();
            galleryPkg = PhonesClassNameEnum.CM_GALLERYPKG.getPhoneClassname();
            
            dialt = PhonesClassNameEnum.ZTEV880_DIALER.getPhoneClassname();
            dialtCls = PhonesClassNameEnum.ZTEV880_LAB.getPhoneClassname();
            dialtPkg = PhonesClassNameEnum.ZTEV880_LABPN.getPhoneClassname();
            
            mms = PhonesClassNameEnum.ZTEU880_MMS.getPhoneClassname();
            mmsCls = PhonesClassNameEnum.ZTEU880_MMSCLS.getPhoneClassname();
            mmsPkg = PhonesClassNameEnum.ZTEU880_MMSPKG.getPhoneClassname();
            
            clock = PhonesClassNameEnum.ZTEV880_CLOCK.getPhoneClassname();
            clockCls = PhonesClassNameEnum.ZTEV880_CLOCKCLS.getPhoneClassname();
            clockPkg = PhonesClassNameEnum.ZTEV880_CLOCKPKG.getPhoneClassname();
            break;
        case TYPE_HTCG7:
        case TYPE_HTCHD:
        case TYPE_HTCIS:
        case TYPE_HTCDS:
        case TYPE_HTCZE:
        case TYPE_HTCG6:
            contacts = PhonesClassNameEnum.HTCG7_CONTACT.getPhoneClassname();
            dialt = PhonesClassNameEnum.HTCG7_DIALER.getPhoneClassname();
            calendar = PhonesClassNameEnum.HTCG7_CALENDAR.getPhoneClassname();
            camera = PhonesClassNameEnum.HTCG7_CAMERA.getPhoneClassname();
            gallery = PhonesClassNameEnum.HTCG7_GALLERY.getPhoneClassname();
            email = PhonesClassNameEnum.HTCG7_EMAIL.getPhoneClassname();
            clock = PhonesClassNameEnum.HTCG7_CLOCK.getPhoneClassname();
            music = PhonesClassNameEnum.HTCG7_MUSIC.getPhoneClassname();
            video = PhonesClassNameEnum.HTCG7_VIDEO.getPhoneClassname();
            settings = PhonesClassNameEnum.HTCG7_SETTINGS.getPhoneClassname();
            dialtCls = PhonesClassNameEnum.HTCG7_LAB.getPhoneClassname();
            contactsCls = PhonesClassNameEnum.HTCG7_LAB2.getPhoneClassname();
            dialtPkg = PhonesClassNameEnum.HTCG7_LABPN.getPhoneClassname();
            contactsPkg = PhonesClassNameEnum.HTCG7_LAB2PN.getPhoneClassname();
            galleryCls = PhonesClassNameEnum.HTCG7_GALLERYCLS
                    .getPhoneClassname();
            galleryPkg = PhonesClassNameEnum.HTCG7_GALLERYPKG
                    .getPhoneClassname();
            cameraCls = PhonesClassNameEnum.HTCG7_CAMERACLS.getPhoneClassname();
            cameraPkg = PhonesClassNameEnum.HTCG7_CAMERAPKG.getPhoneClassname();
            clockCls = PhonesClassNameEnum.HTCG7_CLOCKCLS.getPhoneClassname();
            clockPkg = PhonesClassNameEnum.HTCG7_CLOCKPKG.getPhoneClassname();
            settingsCls = PhonesClassNameEnum.HTCG7_SETTINGSCLS
                    .getPhoneClassname();
            settingsPkg = PhonesClassNameEnum.HTCG7_SETTINGSPKG
                    .getPhoneClassname();

            if (type == TYPE_HTCHD || type == TYPE_HTCIS || type == TYPE_HTCDS) {
                contacts = PhonesClassNameEnum.HTCHD_CONTACT
                        .getPhoneClassname();
                video = PhonesClassNameEnum.HTCHD_VIDEO.getPhoneClassname();
                contactsCls = PhonesClassNameEnum.HTCHD_LAB2
                        .getPhoneClassname();
            } else if (type == TYPE_HTCZE) {
                contacts = PhonesClassNameEnum.HTCHD_CONTACT
                        .getPhoneClassname();
                contactsCls = PhonesClassNameEnum.HTCHD_LAB2
                        .getPhoneClassname();
            }
            break;
        case TYPE_ME722:
        case TYPE_MB525:
        case TYPE_MOTOA953:
            contacts = PhonesClassNameEnum.MOTO_CONTACT.getPhoneClassname();
            dialt = PhonesClassNameEnum.MOTO_DIALER.getPhoneClassname();
            mms = PhonesClassNameEnum.MOTO_MMS.getPhoneClassname();
            camera = PhonesClassNameEnum.MOTO_CAMERA.getPhoneClassname();
            gallery = PhonesClassNameEnum.MOTO_GALLERY.getPhoneClassname();
            email = PhonesClassNameEnum.MOTO_EMAIL.getPhoneClassname();
            video = PhonesClassNameEnum.MOTO_VIDEO.getPhoneClassname();
            clock = PhonesClassNameEnum.MOTO_CLOCK.getPhoneClassname();
            dialtCls = PhonesClassNameEnum.MOTO_LAB.getPhoneClassname();
            mmsCls = PhonesClassNameEnum.MOTO_RAB.getPhoneClassname();
            contactsCls = PhonesClassNameEnum.MOTO_LAB2.getPhoneClassname();
            dialtPkg = PhonesClassNameEnum.MOTO_LABPN.getPhoneClassname();
            mmsPkg = PhonesClassNameEnum.MOTO_RABPN.getPhoneClassname();
            contactsPkg = PhonesClassNameEnum.MOTO_LAB2PN.getPhoneClassname();
            galleryCls = PhonesClassNameEnum.MOTO_GALLERYCLS
                    .getPhoneClassname();
            galleryPkg = PhonesClassNameEnum.MOTO_GALLERYPKG
                    .getPhoneClassname();
            cameraCls = PhonesClassNameEnum.MOTO_CAMERACLS.getPhoneClassname();
            cameraPkg = PhonesClassNameEnum.MOTO_CAMERAPKG.getPhoneClassname();
            clockCls = PhonesClassNameEnum.MOTO_CLOCKCLS.getPhoneClassname();
            clockPkg = PhonesClassNameEnum.MOTO_CLOCKPKG.getPhoneClassname();
            break;
        case TYPE_MT15I:
            contacts = PhonesClassNameEnum.SE_CONTACT.getPhoneClassname();
            dialt = PhonesClassNameEnum.SE_DIALER.getPhoneClassname();
            mms = PhonesClassNameEnum.SE_MMS.getPhoneClassname();
            camera = PhonesClassNameEnum.SE_CAMERA.getPhoneClassname();
            gallery = PhonesClassNameEnum.SE_GALLERY.getPhoneClassname();
            music = PhonesClassNameEnum.SE_MUIC.getPhoneClassname();
            dialtCls = PhonesClassNameEnum.SE_LAB.getPhoneClassname();
            mmsCls = PhonesClassNameEnum.SE_RAB.getPhoneClassname();
            contactsCls = PhonesClassNameEnum.SE_LAB2.getPhoneClassname();
            dialtPkg = PhonesClassNameEnum.SE_LABPN.getPhoneClassname();
            mmsPkg = PhonesClassNameEnum.SE_RABPN.getPhoneClassname();
            contactsPkg = PhonesClassNameEnum.SE_LAB2PN.getPhoneClassname();
            cameraCls = PhonesClassNameEnum.SE_CAMERACLS.getPhoneClassname();
            galleryCls = PhonesClassNameEnum.SE_GALLERYCLS.getPhoneClassname();
            cameraPkg = PhonesClassNameEnum.SE_CAMERAPKG.getPhoneClassname();
            galleryPkg = PhonesClassNameEnum.SE_GALLERYPKG.getPhoneClassname();
            break;
        case TYPE_W721:
            contacts = PhonesClassNameEnum.W721_CONTACT.getPhoneClassname();
            dialt = PhonesClassNameEnum.W721_DIALER.getPhoneClassname();
            mms = PhonesClassNameEnum.W721_MMS.getPhoneClassname();
            calendar = PhonesClassNameEnum.W721_CALENDAR.getPhoneClassname();
            dialtCls = PhonesClassNameEnum.W721_LAB.getPhoneClassname();
            contactsCls = PhonesClassNameEnum.W721_LAB2.getPhoneClassname();
            mmsCls = PhonesClassNameEnum.W721_RAB.getPhoneClassname();
            dialtPkg = PhonesClassNameEnum.W721_LABPN.getPhoneClassname();
            contactsPkg = PhonesClassNameEnum.W721_LAB2PN.getPhoneClassname();
            mmsPkg = PhonesClassNameEnum.W721_RABPN.getPhoneClassname();
            break;
        case TYPE_GTS5830:
        case TYPE_GTS5570:
            contacts = PhonesClassNameEnum.GTS5830_CONTACT.getPhoneClassname();
            dialt = PhonesClassNameEnum.GTS5830_DIALER.getPhoneClassname();
            camera = PhonesClassNameEnum.GTS5830_CAMERA.getPhoneClassname();
            gallery = PhonesClassNameEnum.GTS5830_GALLERY.getPhoneClassname();
            dialtCls = PhonesClassNameEnum.GTS5830_LAB.getPhoneClassname();
            contactsCls = PhonesClassNameEnum.GTS5830_LAB2.getPhoneClassname();
            dialtPkg = PhonesClassNameEnum.GTS5830_LABPN.getPhoneClassname();
            contactsPkg = PhonesClassNameEnum.GTS5830_LAB2PN
                    .getPhoneClassname();
            cameraCls = PhonesClassNameEnum.GTS5830_CAMERACLS
                    .getPhoneClassname();
            galleryCls = PhonesClassNameEnum.GTS5830_GALLERYCLS
                    .getPhoneClassname();
            cameraPkg = PhonesClassNameEnum.GTS5830_CAMERAPKG
                    .getPhoneClassname();
            galleryPkg = PhonesClassNameEnum.GTS5830_GALLERYPKG
                    .getPhoneClassname();
            break;
        default:
            break;
        }
    }

    private enum PhonesClassNameEnum {

        CM_GALLERY("com_cooliris_media_gallery"), CM_GALLERYCLS(
                "com.cooliris.media.Gallery"), CM_GALLERYPKG(
                "com.cooliris.media"), CM_DIALER(
                "com_lewa_pim_ui_dialpadentryactivity"), CM_DIALERCLS(
                "com.lewa.PIM.ui.DialpadEntryActivity"), CM_DIALERPKG(
                "com.lewa.PIM"), CM_CONTACT(
                "com_lewa_pim_ui_contactsentryactivity"), CM_CONTACTCLS(
                "com.lewa.PIM.ui.ContactsEntryActivity"), CM_MMS(
                "com_lewa_pim_ui_messageentryactivity"), CM_MMSCLS(
                "com.lewa.PIM.ui.MessageEntryActivity"),
                
        CM_MUSIC("com_lewa_player_ui_outer_musicmainentryactivity"),        
        CM_CALENDAR("com_when_android_calendar365_calendarmain"),    
        CM_VIDEO("com_lewa_player_ui_video_videobrowseractivity"),

        HTCG7_CONTACT("com_android_htccontacts_contactstabactivity"), // contact
        HTCG7_DIALER("com_android_htcdialer_dialer"), // dialer
        HTCG7_CALENDAR("com_htc_calendar_launchactivity"), // calendar
        HTCG7_CAMERA("com_android_camera_cameraentry"), // camera
        HTCG7_GALLERY("com_htc_album_albummain_activitymaincarousel"), // gallery
        HTCG7_EMAIL("com_htc_android_mail_maillisttab"), // email
        HTCG7_CLOCK("com_htc_android_worldclock_worldclocktabcontrol"), // clock
        HTCG7_MUSIC("com_htc_music_htcmusic"), // music
        HTCG7_VIDEO("com_htc_album_allvideos"), // video
        HTCG7_SETTINGS("com_android_settings_framework_activity_htcsettings"), // settings

        HTCG7_CAMERACLS("com.android.camera.CameraEntry"), // camera
        HTCG7_CAMERAPKG("com.android.camera"), // camera
        HTCG7_GALLERYCLS("com.htc.album.AlbumMain.ActivityMainCarousel"), // gallery
        HTCG7_GALLERYPKG("com.htc.album.AlbumMain"), // gallery
        HTCG7_CLOCKCLS("com.htc.android.worldclock.WorldClockTabControl"), // clock
        HTCG7_CLOCKPKG("com.htc.android.worldclock"), // clock
        HTCG7_SETTINGSCLS("com.android.settings.framework.activity.HtcSettings"), // settings
        HTCG7_SETTINGSPKG("com.android.settings"), // settings

        MIUI_DIALER("com_android_contacts_twelvekeydialer"), // dialer
        MIUI_MMS("com_android_mms_ui_mmstabactivity"), // mms
        MIUI_CAMERA("com_miui_camera_camera"), // camera
        MIUI_GALLERY("com_android_gallery_ui_mainactivity"), // gallery
        MIUI_MUSIC("com_miui_player_draweractivitygroup_mainactivitygroup"), // music
        MIUI_VIDEO("com_android_voicedialer_voicedialeractivity"), // video
        MIUI_BROWSER("com_android_browser_browseractivity"), // browser
        MIUI_GALLERYCLS("com.android.gallery.ui.MainActivity"), MIUI_GALLERYPKG(
                "com.android.gallery"), MIUI_CAMERACLS("com.miui.camera.Camera"), MIUI_CAMERAPKG(
                "com.miui.camera"),

        ZTEV880_DIALER("com_zte_smartdialer_dialerapp"), // dialer
        ZTEV880_GALLERY("com_cooliris_media_gallery"), // gallery
        ZTEV880_MAP("com_autonavi_minimap_noteactivity"), // map
        ZTEV880_CLOCK("zte_com_cn_alarmclock_alarmclock"), // clock
        ZTEV880_VIDEO("com_android_music_videobrowseractivity"), // video

        ZTEV880_GALLERYCLS("com.cooliris.media.Gallery"), ZTEV880_GALLERYPKG(
                "com.cooliris.media"), ZTEV880_CLOCKCLS(
                "zte.com.cn.alarmclock.AlarmClock"), // clock
        ZTEV880_CLOCKPKG("zte.com.cn.alarmclock"), // clock
        
        ZTEU880_MMS("com_android_mms_mainuiselection"),
        ZTEU880_MMSCLS("com.android.mms.MainUISelection"),
        ZTEU880_MMSPKG("com.android.mms"),
        
        ZTEV880_RECORDER("com_android_soundrecorder_recorderactivity"),
        ZTEV880_RECORDERCLS("com.android.soundrecorder.RecorderActivity"),
        ZTEV880_RECORDERPKG("com.android.soundrecorder"),

        MOTO_CONTACT("com_motorola_blur_contacts_viewidentitiesfacetactivity"), // contacts
        MOTO_DIALER("com_motorola_dialer_dialtactscontactsentryactivity"), // dialer
        MOTO_MMS("com_motorola_blur_conversations_ui_conversationlist"), // mms
        MOTO_CAMERA("com_motorola_camera_camera"), // camera
        MOTO_GALLERY("com_motorola_gallery_topscreen"), // gallery
        MOTO_EMAIL("com_motorola_blur_email_mailbox_viewfolderactivity"), // email
        MOTO_VIDEO("com_motorola_camera_camcorder"), // video
        MOTO_CLOCK("com_motorola_blur_alarmclock_alarmclock"), // clock

        MOTO_CAMERACLS("com.motorola.Camera.Camera"), // camera
        MOTO_CAMERAPKG("com.motorola.Camera"), // camera
        MOTO_GALLERYCLS("com.motorola.gallery.TopScreen"), // gallery
        MOTO_GALLERYPKG("com.motorola.gallery"), // gallery
        MOTO_CLOCKCLS("com.motorola.blur.alarmclock.AlarmClock"), // clock
        MOTO_CLOCKPKG("com.motorola.blur.alarmclock"), // clock

        HTCHD_CONTACT("com_android_htccontacts_browselayercarouselactivity"), // contacts
        HTCHD_VIDEO("com_htc_album_tabplugindevice_activityallvideos"), // video

        SE_CONTACT("com_sonyericsson_android_socialphonebook_launchactivity"), // contacts
        SE_DIALER(
                "com_sonyericsson_android_socialphonebook_dialerentryactivity"), // dialer
        SE_MMS("com_sonyericsson_conversations_ui_conversationlistactivity"), // mms
        SE_CAMERA("com_sonyericsson_android_camera_cameraactivity"), // camera
        SE_GALLERY("com_sonyericsson_gallery_gallery"), // gallery
        SE_MUIC("com_sonyericsson_music_playerctivity"), // music

        SE_CAMERACLS("com.sonyericsson.android.camera.CameraActivity"), // camera
        SE_CAMERAPKG("com.sonyericsson.android.camera"), // camera
        SE_GALLERYCLS("com.sonyericsson.gallery.Gallery"), // gallery
        SE_GALLERYPKG("com.sonyericsson.gallery"), // gallery

        W721_CONTACT(
                "com_yulong_android_contacts_ui_main_contactmaintabactivity"), // contact
        W721_DIALER("com_yulong_android_contacts_dial_dialactivity"), // dialer
        W721_MMS("com_yulong_android_mms_ui_mmsmainlistformactivity"), // mms
        W721_CALENDAR("com_yulong_android_calendar_ui_base_launchactivity"), // calendar

        GTS5830_CONTACT("com_sec_android_app_contacts_phonebooktopmenuactivity"), // contact
        GTS5830_DIALER("com_sec_android_app_dialertab_dialertabactivity"), // dialer
        GTS5830_CAMERA("com_sec_android_app_camera_camera"), // camera
        GTS5830_GALLERY("com_cooliris_media_gallery"), // gallery

        GTS5830_CAMERACLS("com.sec.android.app.camera.Camera"), // camera
        GTS5830_CAMERAPKG("com.sec.android.app.camera"), // camera
        GTS5830_GALLERYCLS("com.cooliris.media.Gallery"), GTS5830_GALLERYPKG(
                "com.cooliris.media"),

        MIUI_LAB("com.android.contacts.TwelveKeyDialer"), // LAB
        MIUI_RAB("com.android.mms.ui.MmsTabActivity"), // RAB
        MIUI_RAB2("com.android.browser.BrowserActivity"), // RAB2

        ZTEV880_LAB("com.zte.smartdialer.DialerApp"), // LAB
        ZTEV880_LABPN("com.zte.smartdialer"), // LABpgn

        HTCG7_LAB("com.android.htcdialer.Dialer"), // LAB
        HTCG7_LAB2("com.android.htccontacts.ContactsTabActivity"), // LAB2
        HTCG7_LABPN("com.android.htcdialer"), // LABpgn
        HTCG7_LAB2PN("com.android.htccontacts"), // LAB2

        HTCHD_LAB2("com.android.htccontacts.BrowseLayerCarouselActivity"), // LAB2

        MOTO_LAB("com.motorola.dialer.DialtactsContactsEntryActivity"), // LAB
        MOTO_RAB("com.motorola.blur.conversations.ui.ConversationList"), // RAB
        MOTO_LAB2("com.motorola.blur.contacts.ViewIdentitiesFacetActivity"), // LAB2
        MOTO_LABPN("com.motorola.dialer"), MOTO_RABPN(
                "com.motorola.blur.conversations"), MOTO_LAB2PN(
                "com.motorola.blur.contacts"),

        SE_LAB("com.sonyericsson.android.socialphonebook.DialerEntryActivity"), SE_RAB(
                "com.sonyericsson.conversations.ui.ConversationListActivity"), SE_LAB2(
                "com.sonyericsson.android.socialphonebook.LaunchActivity"), SE_LABPN(
                "com.sonyericsson.android.socialphonebook"), SE_RABPN(
                "com.sonyericsson.conversations"), SE_LAB2PN(
                "com.sonyericsson.android.socialphonebook"),

        W721_LAB("com.yulong.android.contacts.dial.DialActivity"), W721_LAB2(
                "com.yulong.android.contacts.ui.main.ContactMainTabActivity"), W721_RAB(
                "com.yulong.android.mms.ui.MmsMainListFormActivity"), W721_LABPN(
                "com.yulong.android.contacts.dial"), W721_LAB2PN(
                "com.yulong.android.contacts"), W721_RABPN("com.android.mms"),

        GTS5830_LAB("com.sec.android.app.dialertab.DialerTabActivity"), GTS5830_LAB2(
                "com.sec.android.app.contacts.PhoneBookTopMenuActivity"), GTS5830_LABPN(
                "com.sec.android.app.dialertab"), GTS5830_LAB2PN(
                "com.android.contacts");

        private String phoneClassname;

        private PhonesClassNameEnum(String phoneClassname) {
            this.phoneClassname = phoneClassname;
        }

        public String getPhoneClassname() {
            return phoneClassname;
        }

    }
}
