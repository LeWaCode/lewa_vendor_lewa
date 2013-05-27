/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lewa.launcher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import mobi.intuitit.android.content.LauncherIntent;
import mobi.intuitit.android.content.LauncherMetadata;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.StatusBarManager;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lewa.launcher.AsyncIconLoader.ImageCallback;
import com.lewa.launcher.MySlidingDrawer.OnDrawerCloseListener;
import com.lewa.launcher.MySlidingDrawer.OnDrawerOpenListener;
import com.lewa.launcher.theme.ThemeConstants;
import com.lewa.launcher.theme.ThemeUtils;
import com.lewa.launcher.version.UpdateReminder;
import com.lewa.launcher.version.VersionUpdate;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipException;

import java.util.Timer;
import java.util.TimerTask;
import android.view.MotionEvent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
/**
 * Default launcher application.
 */
public final class Launcher extends Activity implements View.OnClickListener,OnLongClickListener {
    static final String LOG_TAG = "Launcher";
    private static final String STATISTICS_MODVERSION_STRING = "ro.modversion";
    static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";
    private static final String PREFERENCES = "launcher.preferences";

    static final boolean LOGD = false;

    private static final boolean PROFILE_STARTUP = false;
    private static final boolean PROFILE_ROTATE = false;
    private static final boolean DEBUG_USER_INTERFACE = false;

    private static final int MENU_GROUP_NORMAL = 4;
    private static final int MENU_DESKTOP_EDITMODE = Menu.FIRST + 1;
    private static final int MENU_POND = MENU_DESKTOP_EDITMODE + 1;
    private static final int MENU_SHARE_FRIENDS = MENU_POND + 1;
    private static final int MENU_VERSION_UPDATE = MENU_SHARE_FRIENDS + 1;
    private static final int MENU_SETTINGS = MENU_VERSION_UPDATE + 1;
    private static final int MENU_DESKTOP_SETT = MENU_SETTINGS + 1;
    private static final int MENU_PREVIEW = MENU_DESKTOP_SETT + 1;
    // Begin,  added by zhaolei, 20111008
    private static final int MENU_MAINMENU_NEWFOLDER = MENU_PREVIEW + 1;
    private static final int MENU_MAINMENU_SEARCH = MENU_MAINMENU_NEWFOLDER + 1;
    private static final int MENU_GROUP_MAINMENU = 5;
    private static final int MAX_SIZE_IN_SCREEN = 16;
    // End
   
    static final int REQUEST_CREATE_APPWIDGET = 5;        
    static final int WALLPAPER_SCREENS_SPAN = 2;
    static final int DEFAULT_SCREN = 2;
    static final int NUMBER_CELLS_X = 4;
    static final int NUMBER_CELLS_Y = 4;

    private static final int REQUEST_PICK_APPWIDGET = 9;
    private static final int DIALOG_CREATE_SHORTCUT = 1;
    static final int DIALOG_RENAME_FOLDER = 2;

    // Type: int
    private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
    // Type: boolean
    private static final String RUNTIME_STATE_ALL_APPS_FOLDER = "launcher.all_apps_folder";
    // Type: long
    private static final String RUNTIME_STATE_USER_FOLDERS = "launcher.user_folder";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cellX";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cellY";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_spanX";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_spanY";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_COUNT_X = "launcher.add_countX";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_COUNT_Y = "launcher.add_countY";
    // Type: int[]
    private static final String RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS = "launcher.add_occupied_cells";
    // Type: boolean
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
    // Type: long
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";
    // Type: boolean
    private static final LauncherModel sModel = new LauncherModel();

    private static final Object sLock = new Object();
    private static int sScreen = DEFAULT_SCREN;

    private final BroadcastReceiver mApplicationsReceiver = new ApplicationsIntentReceiver();
    private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsIntentReceiver();
    private final ContentObserver mObserver = new FavoritesChangeObserver();
    private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();

    private LayoutInflater mInflater;

    private DragLayer mDragLayer;
    private Workspace mWorkspace;

    public AppWidgetManager mAppWidgetManager;
    public LauncherAppWidgetHost mAppWidgetHost;

    static final int APPWIDGET_HOST_ID = 1024;

    private CellLayout.CellInfo mAddItemCellInfo;
    private final int[] mCellCoordinates = new int[2];
    private FolderInfo mFolderInfo;

    private Drawer mAllAppsGrid;

    private boolean mDesktopLocked = true;
    private Bundle mSavedState;

    private SpannableStringBuilder mDefaultKeySsb = null;

    private boolean mDestroyed;

    private boolean mIsNewIntent;

    private boolean mRestoring;
    private boolean mWaitingForResult;
    static boolean mLocaleChanged;

    private Bundle mSavedInstanceState;

    private DesktopBinder mBinder;

    private DeleteZone mDeleteZone;
    private HomeZone mHomeZone;
    /**
     * ADW: variables to store actual status of elements
     */
    private boolean allAppsOpen = false;
    private final boolean allAppsAnimating = false;
    private boolean showingPreviews = false;
    /**
     * ADW: A lot of properties to store the custom settings
     */
    protected boolean autoCloseFolder = false;

    private DesktopIndicator mDesktopIndicator;

    /**
     * ADW:Wallpaper intent receiver
     */
    private static WallpaperIntentReceiver sWallpaperReceiver;
    private boolean mShouldRestart = false;
    // ADW Theme constants
    private Typeface themeFont = null;
    private View mScreensEditor = null;
    final static int MAX_SCREENS = 9;
    // End

    // Begin [pan add]
    private FixedBar mDrawerToolbar = null;
    private MySlidingDrawer mSlidingDrawer;
    private ApplicationInfo mCurrentBottomButton = null;
    private boolean mIsDropOn = false;
    public boolean mIsDropEnter = false;

    private View mEditorZoneView = null;
    private View mAppInfoListView = null;
    private EditModeZone mEditorZone = null;
    private SenseWorkspace mScreenGrid = null;
    //private LewaTipView mLewaTip = null;
    private boolean mHasDowned = false;

    private BottomBar mBottomBar = null;       
    // End

    // beign zhaolei add 11/09/23
    private ApplicationInfo mAppFolderItemInfo = null;
    private Folder mOpenFolder = null;
    private boolean mIsNewAppFolder;
    // end
    public int mPaddingTop;
    public int mIconPaddingTop;
    public int mBgPaddingTop;
    public final int[] mShortCutStyles = { R.layout.application_boxed_bg,
            R.layout.application_boxed, R.layout.folder_icon };

    public AsyncIconLoader mAsyncIconLoad = null;
    public static boolean mIsRom = false;
    public IconBackgroundSetting mIconBackgroundSetting;
    public int mAppWidgetId = -1;
    static boolean isBindedDraw = false;
    static boolean mIsNeedChanged = false;
    public Drawable mIconBGg = null;
    public Drawable mDefaultIcon = null;
    public Drawable mIconTopg = null;
    public static String TIPVIEW_KEY = "tipviewkey0608";
    
    private LewaPopupTipView mSlidingTip;
    private LewaPopupTipView mAppsTip;
    private boolean mIsfirstcreate = false;
    public static boolean themeChanged = false;
    private StatusBarManager mStatusBarManager;
    private Runnable bgRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeReadyAsyncTask().execute("");
        // Begin, deleted by zhumeiquan for umeng, 20120410
        //MobclickAgent.onEvent(this, statistics_home);
        // End
        loadDefaultResource();
        mIsRom = AlmostNexusSettingsHelper.isRomVersion(this);
        if (!mIsRom) {
            VersionUpdate.removeLeavingInstallingApk();
        }
        mStatusBarManager = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);
        mIconBackgroundSetting = new IconBackgroundSetting(getBaseContext(),
                Build.MODEL, Build.DISPLAY, SystemProperties.get(
                        STATISTICS_MODVERSION_STRING, "UNKNOWN"));


        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setPersistent(true);

        super.onCreate(savedInstanceState);
        
        mInflater = getLayoutInflater();

        mAppWidgetManager = AppWidgetManager.getInstance(this);

        mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
        mAppWidgetHost.startListening();

        if (PROFILE_STARTUP) {
            android.os.Debug.startMethodTracing("/sdcard/launcher");
        }

        checkForLocaleChange();
        setWallpaperDimension();

        // Begin [pan] add]
        SharedPreferences sp = getSharedPreferences("launcher.preferences.almostnexus", MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = sp.edit();
        mIsfirstcreate = sp.getBoolean(TIPVIEW_KEY, false);
        boolean isDefWallpaper = sp.getBoolean("lewadefwallpaper", false);
        if (!isDefWallpaper) {
            setDefaultWallpaper();
            editor.putBoolean("lewadefwallpaper", true);
            editor.commit();
        }
        // End
        setContentView(R.layout.launcher);
        setupViews();
    
        registerIntentReceivers();
        registerContentObservers();

        mSavedState = savedInstanceState;
        restoreState(mSavedState);

        if (PROFILE_STARTUP) { 
            android.os.Debug.stopMethodTracing(); 
        }

        if (!mRestoring) {
            startLoaders();
        }

        // For handling default keys
        mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(mDefaultKeySsb, 0);

        if (!mIsRom) {
            VersionUpdate.removeLeavingInstallingApk();
            new VersionUpdate(this).compareVersionInSharedPreferene();
            new UpdateReminder(this).scheduleStart();
        }
        // end        
        registerReceiver(statusbarTouchReceiver, new IntentFilter("com.android.systemui.statusbar.TOUCH"));
        /*
        if (!mIsfirstcreate) {
             mLewaTip = (LewaTipView) mInflater.inflate(R.layout.tip, null);
             mLewaTip.setLauncher(this, R.drawable.welcome);
             mDragLayer.addView(mLewaTip);
             // create the timer
             Timer timer = new Timer();
             timer.schedule(new initSlindingTip(), 400); 
         }
         */
         //tryShowWelcomeDialog();
    }
    class WelcomeDialog extends Dialog{

        public WelcomeDialog(Context context, int theme) {
            super(context, theme);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            this.dismiss();
            return true;
        }
        
        
    }
    public void tryShowWelcomeDialog(){
        final SharedPreferences spf = getSharedPreferences("launcher.preferences.welcome", MODE_WORLD_READABLE);
        final String curVersion = SystemProperties.get("ro.lewa.version", "N/A");
        boolean welcomeSHowed = spf.getBoolean("IF_SHOWED", false);
        String welcomeSHowedVer = spf.getString("SHOWED_VERSION", "NODATA");// can't be "N/A"
        
        if (welcomeSHowed && welcomeSHowedVer.equals(curVersion)) {
            return;
        }
        final WelcomeDialog dialog = new WelcomeDialog(this, R.style.WelcomeDialog);
        /*Timer timer = new Timer();
        timer.schedule(new TimerTask(){

            @Override
            public void run() {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
            
        }, 7000);*/
        dialog.setOnShowListener(new OnShowListener(){
            @Override
            public void onShow(DialogInterface arg0) {
                SharedPreferences.Editor editor = spf.edit();
                editor.putBoolean("IF_SHOWED", true);
                editor.putString("SHOWED_VERSION", curVersion);
                editor.commit();
            }
        });
        dialog.show();
    }

    private final BroadcastReceiver statusbarTouchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            clearPopupTip();
        }
    };
    
    private class initSlindingTip extends TimerTask {   
        @Override  
        public void run() {               
            Message message = new Message();   
            message.what = 1;   
            mHandler.sendMessage(message);
        }          
    } 
    
    private Handler mHandler = new Handler() {        
        public void handleMessage(Message msg) {   
            switch (msg.what) {   
            case 1: 
                if(!allAppsOpen) {
                    showSlidingTip();
                }  
                break;   
            }   
        };   
    }; 
private void showSlidingTip() {
        mSlidingTip = new LewaPopupTipView(this);
        String slidingString = getString(R.string.tip_sliding);
        mSlidingTip.show(
                LewaPopupTipView.STYLE_ABOVE, slidingString,
                getResources().getDimensionPixelSize(R.dimen.popWindow_sliding_width), 
                getResources().getDimensionPixelSize(R.dimen.popWindow_sliding_y));
    }

    private void checkForLocaleChange() {
        final LocaleConfiguration localeConfiguration = new LocaleConfiguration();
        readConfiguration(this, localeConfiguration);

        final Configuration configuration = getResources().getConfiguration();

        final String previousLocale = localeConfiguration.locale;
        final String locale = configuration.locale.toString();

        final int previousMcc = localeConfiguration.mcc;
        final int mcc = configuration.mcc;

        final int previousMnc = localeConfiguration.mnc;
        final int mnc = configuration.mnc;

        mLocaleChanged = !locale.equals(previousLocale) || mcc != previousMcc
                || mnc != previousMnc;

        if (mLocaleChanged) {
            localeConfiguration.locale = locale;
            localeConfiguration.mcc = mcc;
            localeConfiguration.mnc = mnc;
            writeConfiguration(this, localeConfiguration);
        }
    }

    private static class LocaleConfiguration {
        public String locale;
        public int mcc = -1;
        public int mnc = -1;
    }

    private static void readConfiguration(Context context,
            LocaleConfiguration configuration) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(context.openFileInput(PREFERENCES));
            configuration.locale = in.readUTF();
            configuration.mcc = in.readInt();
            configuration.mnc = in.readInt();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            // Ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private static void writeConfiguration(Context context,
            LocaleConfiguration configuration) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(context.openFileOutput(PREFERENCES, MODE_PRIVATE));
            out.writeUTF(configuration.locale);
            out.writeInt(configuration.mcc);
            out.writeInt(configuration.mnc);
            out.flush();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            context.getFileStreamPath(PREFERENCES).delete();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    static int getScreen() {
        synchronized (sLock) {
            return sScreen;
        }
    }

    static void setScreen(int screen) {
        synchronized (sLock) {
            sScreen = screen;
        }
    }

    private void startLoaders() {
        boolean loadApplications = sModel.loadApplications(true, this, mLocaleChanged);
        sModel.loadUserItems(!mLocaleChanged, this, mLocaleChanged, loadApplications);
        mRestoring = false;
    }

    private void setWallpaperDimension() {
        WallpaperManager wpm = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);
        Display display = getWindowManager().getDefaultDisplay();
        int disWidth = display.getWidth();
        int disHeight = display.getHeight();
        boolean isPortrait = disWidth < disHeight;
        final int width = isPortrait ? disWidth : disHeight;
        final int height = isPortrait ? disHeight : disWidth;
        wpm.suggestDesiredDimensions(width * WALLPAPER_SCREENS_SPAN, height);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mWaitingForResult = false;
        if (resultCode == RESULT_OK && mAddItemCellInfo != null) {
            switch (requestCode) {
            case REQUEST_PICK_APPWIDGET:
                addAppWidget(data);
                break;
            case REQUEST_CREATE_APPWIDGET:
                completeAddAppWidget(data, mAddItemCellInfo, mWorkspace.getWidgetCellXY(), true);
                mAppWidgetId = -1;
                break;
            default:
                break;
            }
        } else if ((requestCode == REQUEST_PICK_APPWIDGET || requestCode == REQUEST_CREATE_APPWIDGET)
                && resultCode == RESULT_CANCELED && data != null) {
            // Clean up the appWidgetId if we canceled
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //MobclickAgent.onResume(this); 

        if (shouldRestart()) {
            return;
        }
        mWorkspace.setWallpaper(false);
        mWorkspace.setWallpaperScroll(AlmostNexusSettingsHelper.getWallpaperScrolling(this));
        mWorkspace.setEffectType(AlmostNexusSettingsHelper.getDesktopEffect(this));
        mWorkspace.setScreenLoop(AlmostNexusSettingsHelper.getScreenloop(this));
        mWorkspace.setScrollSpeed(AlmostNexusSettingsHelper.getSrollSpeed(this));
        mAllAppsGrid.autoArrange();
        mAllAppsGrid.setScrollSpeed(AlmostNexusSettingsHelper.getSrollSpeed(this));

        if (!mIsNewIntent) {
            mWorkspace.setScrollFinished();
        }

        if (mRestoring) {
            startLoaders();
        }

        // If this was a new intent (i.e., the mIsNewIntent flag got set to true
        // by onNewIntent), then close the search dialog if needed, because it
        // probably came from the user pressing 'home' (rather than, for example,
        // pressing 'back').
        if (mIsNewIntent) {
            // Post to a handler so that this happens after the search dialog
            // tries to open
            // itself again.
            mWorkspace.post(new Runnable() {
                public void run() {
                    // ADW: changed from using ISearchManager to use
                    // SearchManager (thanks to Launcher+ source code)
                    SearchManager searchManagerService = (SearchManager) Launcher.this
                            .getSystemService(Context.SEARCH_SERVICE);
                    try {
                        searchManagerService.stopSearch();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "error stopping search", e);
                    }
                }
            });
        }
        mIsNewIntent = false;
        for (int i = 0; i < mWorkspace.getChildCount(); i++) {
            CellLayout screen = (CellLayout) mWorkspace.getChildAt(i);
            for (int j = 0; j < screen.getChildCount(); j++) {
                screen.getChildAt(j).invalidate();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //MobclickAgent.onPause(this);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        // Flag any binder to stop early before switching
        if (mBinder != null) {
            mBinder.mTerminate = true;
        }
        
        if (PROFILE_ROTATE) {
            android.os.Debug.startMethodTracing("/sdcard/launcher-rotate");
        }
        return null;
    }

    private boolean acceptFilter() {
        final InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        return !inputManager.isFullscreenMode();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = super.onKeyDown(keyCode, event);
        if (!handled && acceptFilter() && keyCode != KeyEvent.KEYCODE_ENTER) {
            boolean gotKey = TextKeyListener.getInstance().onKeyDown(
                    mWorkspace, mDefaultKeySsb, keyCode, event);
            if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
                return onSearchRequested();
            }
        }

        return handled;
    }

    private String getTypedText() {
        return mDefaultKeySsb.toString();
    }

    private void clearTypedText() {
        mDefaultKeySsb.clear();
        mDefaultKeySsb.clearSpans();
        Selection.setSelection(mDefaultKeySsb, 0);
    }

    /**
     * Restores the previous state, if it exists.
     * 
     * @param savedState The previous state.
     */
    private void restoreState(Bundle savedState) {
        if (savedState == null) {
            return;
        }

        final int currentScreen = savedState.getInt(
                RUNTIME_STATE_CURRENT_SCREEN, -1);
        if (currentScreen > -1) {
            mWorkspace.setCurrentScreen(currentScreen);
        }

        final int addScreen = savedState.getInt(
                RUNTIME_STATE_PENDING_ADD_SCREEN, -1);
        if (addScreen > -1) {
            mAddItemCellInfo = new CellLayout.CellInfo();
            final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
            addItemCellInfo.valid = true;
            addItemCellInfo.screen = addScreen;
            addItemCellInfo.cellX = savedState
                    .getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
            addItemCellInfo.cellY = savedState
                    .getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
            addItemCellInfo.spanX = savedState
                    .getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
            addItemCellInfo.spanY = savedState
                    .getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
            addItemCellInfo.findVacantCellsFromOccupied(savedState
                    .getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS),
                    savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_X),
                    savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y));
            mRestoring = true;
        }

        boolean renameFolder = savedState.getBoolean(
                RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
        if (renameFolder) {
            long id = savedState
                    .getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
            mFolderInfo = sModel.getFolderById(this, id);
            mRestoring = true;
        }
    }

    /**
     * Finds all the views we need and configure them properly.
     */
    
    boolean appsTipRemoved;
    private void setupViews() {
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        final DragLayer dragLayer = mDragLayer;

        mWorkspace = (Workspace) dragLayer.findViewById(R.id.workspace);
        final Workspace workspace = mWorkspace;

        mSlidingDrawer = (MySlidingDrawer) dragLayer.findViewById(R.id.drawer);
        final MySlidingDrawer slidingDrawer = mSlidingDrawer;

        mAllAppsGrid = (Drawer) slidingDrawer.getAllApps().inflate();
        final Drawer allAppsGrid = mAllAppsGrid;

        mBottomBar = (BottomBar) mSlidingDrawer.getBottomBar().inflate();
        final BottomBar bottomBar = mBottomBar;

        mDesktopIndicator = mSlidingDrawer.getDeskIndicator();
        mDesktopIndicator.setLauncher(this);

        mDeleteZone = (DeleteZone) dragLayer.findViewById(R.id.delete_zone);
        mHomeZone = (HomeZone) dragLayer.findViewById(R.id.goback);
        final HomeZone goBack = mHomeZone;
        final DeleteZone deleteZone = mDeleteZone;

        mDrawerToolbar = (FixedBar) slidingDrawer.getHandle();
        mDrawerToolbar.setSliderDrawer(slidingDrawer);

        workspace.setOnLongClickListener(this);
        workspace.setDragger(dragLayer);
        workspace.setLauncher(this);
        workspace.setWallpaperHack(true);
        dragLayer.setDragScoller(workspace);
        workspace.setWallpaperScroll(AlmostNexusSettingsHelper
                .getWallpaperScrolling(this));

        slidingDrawer.setLauncher(this);

        slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {
            public void onDrawerClosed() {
                allAppsOpen = false;
                workspace.hideWallpaper(false);
                workspace.unlock();
                CellLayout cl = (CellLayout) workspace.getChildAt(workspace
                        .getCurrentScreen());
                cl.open(true);
                clearPopupTip();                
                appsTipRemoved = true;
            }
        });

        slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
            public void onDrawerOpened() {
                allAppsOpen = true;
                workspace.hideWallpaper(true);
                workspace.lock();
                mAllAppsGrid.invalidate();
                clearPopupTip();
                
                if (!appsTipRemoved && !mIsfirstcreate) {
                    mAppsTip = new LewaPopupTipView(slidingDrawer.getContext());
                    String slidingString = getString(R.string.tip_allapps);
                    mAppsTip.show(LewaPopupTipView.STYLE_BELOW, slidingString,
                            getResources().getDimensionPixelSize(R.dimen.popWindow_apps_width),
                            getResources().getDimensionPixelSize(R.dimen.popWindow_apps_y));
                }
            }
        });

        allAppsGrid.setDragger(dragLayer);
        allAppsGrid.setLauncher(this);

        bottomBar.setOnClickListener(this);

        deleteZone.setLauncher(this);
        deleteZone.setDragController(dragLayer);

        goBack.setLauncher(this);

        dragLayer.addDragListener(deleteZone);
        dragLayer.addDragListener(goBack);
        dragLayer.addDragListener(bottomBar);
        String launcherPath = "/data/system/face/launcher";
        File file = new File(launcherPath);
        if (file.exists()) {
            bottomBar.setBackgroundDrawable(ThemeUtils.getThemeDrawable(this,
                    ThemeConstants.THEME_LAUNCHER_MODEL, "favorite_bg"));
        } else {
            bottomBar.setBackgroundDrawable(bitmap2drawable(R.drawable.favorite_bg));
        }
        mDesktopIndicator.setBackgroundDrawable(bitmap2drawable(R.drawable.desktopindicator_bg));
        slidingDrawer.getContent().setBackgroundDrawable(null);
        slidingDrawer.getContent().setBackgroundColor(0xD8313131);
        goBack.setBackgroundDrawable(bitmap2drawable(R.drawable.desktopindicator_bg));

        mIconPaddingTop = (int) getResources().getDimension(R.dimen.app_icon_top);
        mBgPaddingTop = (int) getResources().getDimension(R.dimen.app_iconbg_top);
        mPaddingTop = mIconPaddingTop + mBgPaddingTop;

    }

    /**
     * Creates a view representing a shortcut.
     * 
     * @param info
     *            The data structure describing the shortcut.
     * 
     * @return A View inflated from R.layout.application.
     */
    View createShortcut(ApplicationInfo info) {
        synchronized (info) {
            return createShortcut(-1, (ViewGroup) mWorkspace.getChildAt(mWorkspace
                    .getCurrentScreen()), info);
        }
    }

    /**
     * Creates a view representing a shortcut inflated from the specified
     * resource.
     * 
     * @param layoutResId
     *            The id of the XML layout used to create the shortcut.
     * @param parent
     *            The group the shortcut belongs to.
     * @param info
     *            The data structure describing the shortcut.
     * 
     * @return A View inflated from layoutResId.
     */
    View createShortcut(int layoutResId, ViewGroup parent, ApplicationInfo info) {
        if (info.iconBackground != null) {
            layoutResId = mShortCutStyles[0];
        } else {
            layoutResId = mShortCutStyles[1];
        }
        final CounterTextView favorite = (CounterTextView) mInflater.inflate(
                layoutResId, parent, false);

        final int bgPaddingTop = mBgPaddingTop;
        final int iconPaddingTop = mIconPaddingTop;
        final int paddingTop = mPaddingTop;

        if (info.iconBackground != null) {
            favorite.mBgPaddingTop = bgPaddingTop + iconPaddingTop;
            favorite.setPadding(2, iconPaddingTop + iconPaddingTop, 2, 0);
        } else {
            favorite.setPadding(2, paddingTop, 2, 0);
        }
        if (!info.isLewaIcon || Launcher.mLocaleChanged) {
            if(mAsyncIconLoad == null){
                mAsyncIconLoad = new AsyncIconLoader(this, mLocaleChanged);
            }
            info = mAsyncIconLoad.loadDrawable(info, false,
                    new ImageCallback() {
                        @Override
                        public void imageLoaded(ApplicationInfo appInfo) {
                            favorite.setCompoundDrawablesWithIntrinsicBounds(
                                    null, appInfo.icon, null, null);
                            favorite.setText(appInfo.title);
                            favorite.setTag(appInfo);
                            
                             if (appInfo.iconBackground != null) {
                                favorite.mBgPaddingTop = bgPaddingTop + iconPaddingTop;
                                favorite.setPadding(2, iconPaddingTop + iconPaddingTop, 2, 0);
                            } else {
                                favorite.mBackGround = null;
                                favorite.mIconTopDrawable = null;
                                favorite.setPadding(2, paddingTop, 2, 0);
                                favorite.setCompoundDrawablePadding(3);
  
                            }
                        }
                    });
        }
        
        favorite.setCompoundDrawablesWithIntrinsicBounds(null, info.icon, null, null);
        favorite.setText(info.title);
        favorite.setTag(info);
        favorite.setOnClickListener(this);
        if (themeFont != null) {
            favorite.setTypeface(themeFont);
        }

        // Begin [pan 110817] add
        if (info.iconBackground != null) {
            favorite.mBackGround = mIconBGg;
            favorite.mIconTopDrawable = mIconTopg;
        }
        favorite.mIsInDesktop = true;
        // End
        return favorite;
    }

    /**
     * Add a widget to the workspace.
     * 
     * @param data
     *            The intent describing the appWidgetId.
     * @param cellInfo
     *            The position on screen where to create the widget.
     */
    private void completeAddAppWidget(Intent data,
            CellLayout.CellInfo cellInfo, final int[] xy,
            final boolean insertAtFirst) {

        Bundle extras = data.getExtras();
        final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
                .getAppWidgetInfo(appWidgetId);

        // Calculate the grid spans needed to fit this widget
        CellLayout layout = (CellLayout) mWorkspace.getChildAt(cellInfo.screen);
        final int[] spans = layout.rectToCell(appWidgetInfo.minWidth, appWidgetInfo.minHeight);

        spans[0] = Math.min(spans[0], 4);
        spans[1] = Math.min(spans[1], 4);
        spans[0] = Math.max(spans[0], 0);
        spans[1] = Math.max(spans[1], 0);

        realAddWidget(appWidgetInfo, cellInfo, spans, appWidgetId, xy, insertAtFirst);
    }

    public void completeAddAppWidget(
            AppWidgetProviderInfo appwidgetproviderinfo, final int[] xy,
            final boolean insertAtFirst) {

        CellLayout.CellInfo cellInfo = new CellLayout.CellInfo();
        cellInfo.screen = mWorkspace.getCurrentScreen();
        final int widgetId;
        if (mAppWidgetId == -1) {
            widgetId = mAppWidgetHost.allocateAppWidgetId();
            try {
                AppWidgetManager appwidgetmanager = mAppWidgetManager;
                ComponentName componentname = appwidgetproviderinfo.provider;
                appwidgetmanager.bindAppWidgetId(widgetId, componentname);
            } catch (IllegalArgumentException illegalargumentexception) {
                Toast.makeText(this, "load app widget error", 500).show();
                return;
            }
        } else {
            widgetId = mAppWidgetId;
            mAppWidgetId = -1;
        }

        final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
                .getAppWidgetInfo(widgetId);

        // Calculate the grid spans needed to fit this widget
        CellLayout layout = (CellLayout) mWorkspace.getChildAt(cellInfo.screen);
        final int[] spans = layout.rectToCell(appWidgetInfo.minWidth,
                appWidgetInfo.minHeight);

        spans[0] = Math.min(spans[0], 4);
        spans[1] = Math.min(spans[1], 4);
        spans[0] = Math.max(spans[0], 0);
        spans[1] = Math.max(spans[1], 0);

        final CellLayout.CellInfo cInfo = cellInfo;

        realAddWidget(appWidgetInfo, cInfo, spans, widgetId, xy, insertAtFirst);

    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return mAppWidgetHost;
    }

    static ApplicationInfo addShortcut(Context context, Intent data,
            CellLayout.CellInfo cellInfo, boolean notify) {
        final ApplicationInfo info = infoFromShortcutIntent(context, data);
        LauncherModel.addItemToDatabase(context, info,
                LauncherSettings.Favorites.CONTAINER_DESKTOP, cellInfo.screen,
                cellInfo.cellX, cellInfo.cellY, notify);
        return info;
    }

    private static ApplicationInfo infoFromShortcutIntent(Context context,
            Intent data) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Bitmap bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        Drawable icon = null;
        boolean filtered = false;
        boolean customIcon = false;
        ShortcutIconResource iconResource = null;

        if (bitmap != null) {
            icon = new FastBitmapDrawable(Utilities.createBitmapThumbnail(
                    bitmap, context));
            filtered = true;
            customIcon = true;
        } else {
            Parcelable extra = data
                    .getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
                try {
                    iconResource = (ShortcutIconResource) extra;
                    final PackageManager packageManager = context
                            .getPackageManager();
                    Resources resources = packageManager
                            .getResourcesForApplication(iconResource.packageName);
                    final int id = resources.getIdentifier(
                            iconResource.resourceName, null, null);
                    icon = resources.getDrawable(id);
                } catch (Exception e) {
                    Log.w(LOG_TAG, "Could not load shortcut icon: " + extra);
                }
            }
        }

        if (icon == null) {
            icon = context.getPackageManager().getDefaultActivityIcon();
        }

        final ApplicationInfo info = new ApplicationInfo();
        info.icon = icon;
        info.filtered = filtered;
        info.title = name;
        info.intent = intent;
        info.customIcon = customIcon;
        info.iconResource = iconResource;
        info.iconBackground = LauncherModel.getBackGroundKey();
        // info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        return info;
    }

    void closeSystemDialogs() {
        getWindow().closeAllPanels();

        try {
            dismissDialog(DIALOG_CREATE_SHORTCUT);
            // Unlock the workspace if the dialog was showing
            mWorkspace.unlock();
        } catch (Exception e) {
            // An exception is thrown if the dialog is not visible, which is fine
        }

        try {
            dismissDialog(DIALOG_RENAME_FOLDER);
            // Unlock the workspace if the dialog was showing
            mWorkspace.unlock();
        } catch (Exception e) {
            // An exception is thrown if the dialog is not visible, which is fine
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            closeSystemDialogs();
            if (mOpenFolder != null) {
                
                // + for update the AllAppSlider after add a new app folder when user press home key by luoyongxing
                 if (mOpenFolder.mInfo != null 
                        &&  mIsNewAppFolder
                        && ((UserFolderInfo) mOpenFolder.mInfo).contents.size() > 0) {
                    addNewAppFolder((UserFolderInfo) mOpenFolder.mInfo);
                    ((UserFolderInfo) mOpenFolder.mInfo).mFolderIcon
                            .updateFolderIcon();
                    mIsNewAppFolder = false;
                    
                    int screen = ((AppFolder) mOpenFolder).mInfo.screen;
                    
                    if(AlmostNexusSettingsHelper.getAutoArrange(this)) {
                        getAllAppsSlidingView().autoArrange();
                    } else {
                        getAllAppsSlidingView().getAdapter().updateDataSet();
                    }
                    getAllAppsSlidingView().snapToScreen(screen);
                }
                // -
                closeFolder(mOpenFolder);
                if(mBottomBar.getVisibility() != View.VISIBLE) {
                    mBottomBar.setVisibility(View.VISIBLE);
                }
            }

            mIsNewIntent = true;
            if ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) {
                if (!isAllAppsVisible()) {
                    // Begin [pan] modify
                    if (isPreviewing()) {
                        stopDesktopEdit();
                    }
                    
                    if (mEditorZoneView != null) {
                        mEditorZone.startOutAnimation();
                        mBottomBar.open();
                        mDragLayer.removeView(mEditorZoneView);
                        mEditorZoneView = null;
                        mEditorZone = null;
                        for (int i = 0; i < mWorkspace.getChildCount(); i++) {
                            ((CellLayout) mWorkspace.getChildAt(i)).setBackgroundDrawable(null);
                        }
                    }
                    mWorkspace.snapToScreen(mWorkspace.getDefaultScreen());
                    // End
                }

                final View v = getWindow().peekDecorView();
                if (v != null && v.getWindowToken() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            mSlidingDrawer.close();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Bundle windowState = savedInstanceState
                .getBundle("android:viewHierarchyState");
        SparseArray<Parcelable> savedStates = null;
        int focusedViewId = View.NO_ID;

        if (windowState != null) {
            savedStates = windowState.getSparseParcelableArray("android:views");
            windowState.remove("android:views");
            focusedViewId = windowState.getInt("android:focusedViewId",
                    View.NO_ID);
            windowState.remove("android:focusedViewId");
        }

        super.onRestoreInstanceState(savedInstanceState);

        if (windowState != null) {
            windowState.putSparseParcelableArray("android:views", savedStates);
            windowState.putInt("android:focusedViewId", focusedViewId);
            windowState.remove("android:Panels");
        }

        mSavedInstanceState = savedInstanceState;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        closeOptionsMenu();
        super.onSaveInstanceState(outState);

        outState.putInt(RUNTIME_STATE_CURRENT_SCREEN,
                mWorkspace.getCurrentScreen());
        if (mWorkspace != null) {
            final ArrayList<Folder> folders = mWorkspace.getOpenFolders();
            if (folders.size() > 0) {
                final int count = folders.size();
                long[] ids = new long[count];
                for (int i = 0; i < count; i++) {
                    final FolderInfo info = folders.get(i).getInfo();
                    ids[i] = info.id;
                }
                outState.putLongArray(RUNTIME_STATE_USER_FOLDERS, ids);
            }
        }
        final boolean isConfigurationChange = getChangingConfigurations() != 0;

        // When the drawer is opened and we are saving the state because of a
        // configuration change
        if (allAppsOpen && isConfigurationChange) {
            outState.putBoolean(RUNTIME_STATE_ALL_APPS_FOLDER, true);
        }

        if (mAddItemCellInfo != null && mAddItemCellInfo.valid
                && mWaitingForResult) {
            final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
            final CellLayout layout = (CellLayout) mWorkspace
                    .getChildAt(addItemCellInfo.screen);

            outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN,
                    addItemCellInfo.screen);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X,
                    addItemCellInfo.cellX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y,
                    addItemCellInfo.cellY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X,
                    addItemCellInfo.spanX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y,
                    addItemCellInfo.spanY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_X,
                    layout.getCountX());
            outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y,
                    layout.getCountY());
            outState.putBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS,
                    layout.getOccupiedCells());
        }

        if (mFolderInfo != null && mWaitingForResult) {
            outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
            outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID,
                    mFolderInfo.id);
        }
    }

    @Override
    public void onDestroy() {
        mDestroyed = true;
        
        try {
            mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Log.w(LOG_TAG,"problem while stopping AppWidgetHost during Launcher destruction", ex);
        }

        TextKeyListener.getInstance().release();

        mAllAppsGrid.setAdapter(null);

        sModel.unbind();
        sModel.abortLoaders();
        sModel.dropApplicationCache();
        mWorkspace.unbindWidgetScrollableViews();
        getContentResolver().unregisterContentObserver(mObserver);
        getContentResolver().unregisterContentObserver(mWidgetObserver);
        unregisterReceiver(mApplicationsReceiver);
        unregisterReceiver(mCloseSystemDialogsReceiver);
        unregisterReceiver(statusbarTouchReceiver);

        mWorkspace.unregisterProvider();
        // Begin [pan 110802 recycle] add
        sModel.clearList();
        if (mAsyncIconLoad != null) {
            mAsyncIconLoad.clearCache();
        }
        ApplicationsAdapter.allItems.clear();
        ApplicationsAdapter.viewCache.clear();
        super.onDestroy();
        // End
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (intent == null) {
            return;
        }

        if (requestCode >= 0) {
            mWaitingForResult = true;
        }

        try {
            super.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Toast.makeText(this, R.string.activity_not_found,
                    Toast.LENGTH_SHORT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(MENU_GROUP_NORMAL, MENU_DESKTOP_EDITMODE, 0,
                R.string.menu_edit).setIcon(R.drawable.ic_menu_edit)
                .setAlphabeticShortcut('E');

        if (!mIsRom) {
            menu.add(MENU_GROUP_NORMAL, MENU_SHARE_FRIENDS, 0,
                    R.string.menu_share_friends)
                    .setIcon(R.drawable.ic_menu_share_friends)
                    .setAlphabeticShortcut('S');

            menu.add(MENU_GROUP_NORMAL, MENU_VERSION_UPDATE, 0,
                    R.string.menu_version_update)
                    .setIcon(R.drawable.ic_menu_update_version)
                    .setAlphabeticShortcut('U');

        } else {
            menu.add(MENU_GROUP_NORMAL, MENU_PREVIEW, 0, R.string.lewa_preview)
            .setIcon(R.drawable.ic_menu_preview)
            .setAlphabeticShortcut('N');

            menu.add(MENU_GROUP_NORMAL, MENU_SHARE_FRIENDS, 0,
                    R.string.menu_wallpaper)
                    .setIcon(R.drawable.ic_menu_wallpaper)
                    .setAlphabeticShortcut('S');

            menu.add(MENU_GROUP_NORMAL, MENU_POND, 0, R.string.menu_lewapond)
            .setIcon(R.drawable.ic_menu_lewa_pond)
            .setAlphabeticShortcut(SearchManager.MENU_KEY);
        }

        final Intent settings = new Intent(
                android.provider.Settings.ACTION_SETTINGS);
        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        menu.add(MENU_GROUP_NORMAL, MENU_SETTINGS, 0, R.string.menu_settings)
                .setIcon(R.drawable.ic_menu_system_sett)
                .setAlphabeticShortcut('P').setIntent(settings);

        // Lewa: add desktop settings
        menu.add(MENU_GROUP_NORMAL, MENU_DESKTOP_SETT, 0,
                R.string.menu_desktop_settings)
                .setIcon(R.drawable.ic_menu_desktop_sett)
                .setAlphabeticShortcut('X');

        menu.add(MENU_GROUP_MAINMENU, MENU_MAINMENU_NEWFOLDER, 0,
                R.string.new_folder).setIcon(android.R.drawable.ic_menu_add);
        menu.add(MENU_GROUP_MAINMENU, MENU_MAINMENU_SEARCH, 0,
                R.string.mainmenu_search).setIcon(
                android.R.drawable.ic_menu_search);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mSlidingDrawer.GetScrollStart() || showingPreviews
                || mEditorZoneView != null || mOpenFolder != null) {
            return false;
        }

        menu.setGroupVisible(MENU_GROUP_NORMAL, !allAppsOpen);
        menu.setGroupVisible(MENU_GROUP_MAINMENU, allAppsOpen);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_POND:
            Intent pond = new Intent("android.settings.SYNC_SETTINGS");
            startActivity(pond);
            return true;
        case MENU_DESKTOP_EDITMODE:
            if (allAppsOpen) {
                mSlidingDrawer.close();
            }
            // Begin [pan] modify
            startEditMode(true);
            // End
            return true;
        case MENU_PREVIEW:
            showPreviews();
            return true;
        case MENU_DESKTOP_SETT:
            showCustomConfig();
            return true;
        case MENU_SHARE_FRIENDS:
            if (mIsRom) {
                startWallpaper();
            } else {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT,
                        this.getString(R.string.menu_share_friends));
                intent.putExtra(Intent.EXTRA_TEXT,
                        this.getString(R.string.text_share_friends));
                startActivity(Intent.createChooser(intent,
                        this.getString(R.string.title_share_methods)));
            }
            return true;
            // begin [chenliang added
        case MENU_VERSION_UPDATE:
            new VersionUpdate(this).maunalUpdate();
            return true;
            // end
        case MENU_MAINMENU_NEWFOLDER:
            createNewAppFolder();
            return true;
        case MENU_MAINMENU_SEARCH:
            onSearchRequested();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void createNewAppFolder() {
        mIsNewAppFolder = true;
        UserFolderInfo appFolderInfo = new UserFolderInfo();
        String str = (new Date()).toString();
        appFolderInfo.setActivity(new ComponentName(str, str), 0);

        appFolderInfo.title = getText(R.string.folder);
        appFolderInfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER;
        appFolderInfo.container = LauncherSettings.Favorites.CONTAINER_MAINMENU;
        appFolderInfo.icon = null;
        appFolderInfo.isUserInstalled = true;
        appFolderInfo.filtered = false;
        openFolder(appFolderInfo);
    }

    public void addNewAppFolder(UserFolderInfo appFolderInfo) {
        AllAppsSlidingView allApps = getAllAppsSlidingView();
        int currentScreen = allApps.getCurrentScreen();
        int totalScreen = allApps.getTotalScreens();
        int lastScreen = totalScreen - 1;

        int i = 0;
        for (i = currentScreen; i <= lastScreen; i++) {
            int sizeInScreen = allApps.getAppCountInScreen(i);
            if (sizeInScreen < MAX_SIZE_IN_SCREEN) {
                appFolderInfo.screen = i;
                appFolderInfo.cellX = sizeInScreen;
                break;
            }
        }
        if (i > lastScreen) {
            appFolderInfo.screen = lastScreen + 1;
            appFolderInfo.cellX = 0;
            totalScreen++;
            getAllAppsSlidingView().addNewPage(appFolderInfo.screen);
        }
        if (appFolderInfo.screen < 0) {
            appFolderInfo.screen = lastScreen;
            appFolderInfo.cellX = allApps.getAppCountInScreen(lastScreen);
        }
        synchronized (ApplicationsAdapter.allItems) {
            final ArrayList<ApplicationInfo> allItems = ApplicationsAdapter.allItems;

            int startPosForToPage = allApps
                    .getFirstPositionInScreen(appFolderInfo.screen);
            int appFolderPos = startPosForToPage + appFolderInfo.cellX;
            allItems.add(appFolderPos, appFolderInfo);

            LauncherModel.moveItemInDatabase(this, appFolderInfo,
                    LauncherSettings.Favorites.CONTAINER_MAINMENU,
                    appFolderInfo.screen, appFolderInfo.cellX, appFolderInfo.cellY);

            LauncherModel.resetApplications(allItems, 0, totalScreen);
            appFolderInfo.mFolderIcon.updateFolderIcon();
        }
    }

    /**
     * Indicates that we want global search for this activity by setting the
     * globalSearch argument for {@link #startSearch} to true.
     */

    @Override
    public boolean onSearchRequested() {
        startSearch(getTypedText(), false, null, true);
        clearTypedText();
        return true;
    }

    public void removeShortcutsForPackage(String packageName) {
        if (packageName != null && packageName.length() > 0) {
            mWorkspace.removeShortcutsForPackage(packageName);
        }
    }

    private void updateShortcutsForPackage(String[] packageName) {
        if (packageName != null && packageName.length > 0) {
            final ArrayList<String> pns = new ArrayList<String>();
            final ArrayList<String> pagesName = new ArrayList<String>();
            for (int i = 0; i < packageName.length; i++) {
                pns.add(packageName[i]);
                pagesName.add(packageName[i]);
            }
            mWorkspace.updateShortcutsForPackage(packageName);
            synchronized (pns) {
                mBottomBar.updateBarForPackage(pns);
                mAllAppsGrid.updateBarForPackage(pns);
            }
            
            for (int k = 0; k < packageName.length; k++ ){
                if(!sModel.mUnabledApps.contains(packageName[k])) {
                    sModel.addPackage(this, packageName[k]);
                }
            }
            sModel.mUnabledApps.clear();
        }
    }

    void addAppWidget(final Intent data) {
        int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        AppWidgetProviderInfo appWidget = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        try {
            Bundle metadata = getPackageManager().getReceiverInfo(
                    appWidget.provider, PackageManager.GET_META_DATA).metaData;
            if (metadata != null) {
                if (metadata
                        .containsKey(LauncherMetadata.Requirements.APIVersion)) {
                    int requiredApiVersion = metadata
                            .getInt(LauncherMetadata.Requirements.APIVersion);

                    if (requiredApiVersion > LauncherMetadata.CurrentAPIVersion) {
                        onActivityResult(REQUEST_CREATE_APPWIDGET,
                                Activity.RESULT_CANCELED, data);
                        // Show a nice toast here to tell the user why the
                        // widget is rejected.
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.adw_version)
                                .setCancelable(true)
                                .setIcon(R.drawable.lewahome)
                                .setPositiveButton(
                                        getString(android.R.string.ok), null)
                                .setMessage(
                                        getString(R.string.scrollable_api_required))
                                .create().show();
                        return;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException expt) {
            // No Metadata available... then it is all OK...
        }
        configureOrAddAppWidget(data);
        // }
    }

    private void configureOrAddAppWidget(Intent data) {
        int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                -1);
        AppWidgetProviderInfo appWidget = mAppWidgetManager
                .getAppWidgetInfo(appWidgetId);
        if (appWidget.configure != null) {
            // Launch over to configure widget, if needed
            Intent intent = new Intent(
                    AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidget.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            // Otherwise just add it
            onActivityResult(REQUEST_CREATE_APPWIDGET, Activity.RESULT_OK, data);
        }
    }

    void processShortcut(Intent intent, int requestCodeApplication,
            int requestCodeShortcut) {
        // Handle case where user selected "Applications"
        String applicationName = getResources().getString(
                R.string.group_applications);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            startActivityForResult(pickIntent, requestCodeApplication);
        } else {
            startActivityForResult(intent, requestCodeShortcut);
        }
    }

    // Begin [pan 110818 add folder] modify
    public void addFolder(String name, boolean insertAtFirst) {
        if (name == null) {
            return;
        }
        UserFolderInfo folderInfo = new UserFolderInfo();
        folderInfo.title = name;
        // End
        CellLayout.CellInfo cellInfo = new CellLayout.CellInfo();
        cellInfo.screen = mWorkspace.getCurrentScreen();
        if (!findSingleSlot(cellInfo)) {
            return;
        }

        // Update the model
        LauncherModel.addItemToDatabase(this, folderInfo,
                LauncherSettings.Favorites.CONTAINER_DESKTOP,
                mWorkspace.getCurrentScreen(), cellInfo.cellX, cellInfo.cellY,
                false);
        sModel.addDesktopItem(folderInfo);
        sModel.addFolder(folderInfo);

        // Create the view
        FolderIcon newFolder = FolderIcon
                .fromXml(R.layout.folder_icon, this, (ViewGroup) mWorkspace
                        .getChildAt(mWorkspace.getCurrentScreen()), folderInfo);
        newFolder.mIsInDesktop = true;
        if (themeFont != null) {
            ((TextView) newFolder).setTypeface(themeFont);
        }
        mWorkspace.addInCurrentScreen(newFolder, cellInfo.cellX,
                cellInfo.cellY, 1, 1, insertAtFirst);
    }

    private boolean findSingleSlot(CellLayout.CellInfo cellInfo) {
        final int[] xy = new int[2];
        if (findSlot(cellInfo, xy, 1, 1)) {
            cellInfo.cellX = xy[0];
            cellInfo.cellY = xy[1];
            return true;
        }
        return false;
    }

    private boolean findSlot(CellLayout.CellInfo cellInfo, int[] xy, int spanX,
            int spanY) {
        if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
            boolean[] occupied = mSavedState != null ? mSavedState
                    .getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS)
                    : null;
            cellInfo = mWorkspace.findAllVacantCells(occupied);
            if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
                Toast.makeText(this, getString(R.string.out_of_space),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    public void showNotifications() {
        try {
            Object service = getSystemService("statusbar");
            if (service != null) {
                Method expand = service.getClass().getMethod("expand");
                expand.invoke(service);
            }
        } catch (Exception e) {
        }
    }
    private void startWallpaper() {
        /*final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper,
                getText(R.string.chooser_wallpaper));
        WallpaperManager wm = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
        WallpaperInfo wi = wm.getWallpaperInfo();
        if (wi != null && wi.getSettingsActivity() != null) {
            LabeledIntent li = new LabeledIntent(getPackageName(),
                    R.string.configure_wallpaper, 0);
            li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { li });
        }
        startActivity(chooser);*/

        final Intent setWallpaper = new Intent("com.lewa.face.WallPaper");
        startActivity(setWallpaper);
    }

    /**
     * Registers various intent receivers. The current implementation registers
     * only a wallpaper intent receiver to let other applications change the
     * wallpaper.
     */
    private void registerIntentReceivers() {
        if (sWallpaperReceiver == null) {
            final Application application = getApplication();
            sWallpaperReceiver = new WallpaperIntentReceiver(application, this);
            IntentFilter filter = new IntentFilter(
                    Intent.ACTION_WALLPAPER_CHANGED);
            application.registerReceiver(sWallpaperReceiver, filter);
        } else {
            sWallpaperReceiver.setLauncher(this);
        }
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mApplicationsReceiver, filter);
        filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mCloseSystemDialogsReceiver, filter);
        // ADW: damn, this should be only for froyo
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        registerReceiver(mApplicationsReceiver, filter);
    }

    /**
     * Registers various content observers. The current implementation registers
     * only a favorites observer to keep track of the favorites applications.
     */
    private void registerContentObservers() {
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(
                LauncherSettings.Favorites.CONTENT_URI, true, mObserver);
        resolver.registerContentObserver(
                LauncherProvider.CONTENT_APPWIDGET_RESET_URI, true,
                mWidgetObserver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                mHasDowned = true;
                return true;
            case KeyEvent.KEYCODE_HOME:
                return true;
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
          //begin zhaolei 120314 for popupwindow
            if(mSlidingTip != null || mAppsTip != null) {
                clearPopupTip();
                return false;
            }
            //end
            switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:              
                // Begin [pan 110815 need downed] modify
                if (!event.isCanceled() && mHasDowned) {
                    mHasDowned = false;
                    // End
                    mWorkspace.dispatchKeyEvent(event);
                    // Begin [pan] add
//                    if (mLewaTip != null) { 
//                        mLewaTip.startAnimation();
//                        return true; 
//                    }
                    // End
                    if (mOpenFolder != null) {
                        if (((AppFolder) mOpenFolder).getAppInfoListState()) {
                            if (mIsNewAppFolder
                                    && ((UserFolderInfo) mOpenFolder.mInfo).contents
                                            .size() > 0) {
                                addNewAppFolder((UserFolderInfo) mOpenFolder.mInfo);
                                ((UserFolderInfo) mOpenFolder.mInfo).mFolderIcon
                                        .updateFolderIcon();
                                mIsNewAppFolder = false;
                            } else {
                                AppFolder openFolder = (AppFolder) mOpenFolder;
                                AllInfoZone appinfolistView = (AllInfoZone) openFolder
                                        .findViewById(R.id.appsinfoview);
                                appinfolistView.startOutAnimation();
                                openFolder.setAppInfoListState(false);
                                appinfolistView.setVisibility(View.INVISIBLE);
                                openFolder.addPlusImage();
                                getAllAppsSlidingView().getAdapter()
                                        .updateDataSet();
                                if (mBottomBar.getVisibility() != View.VISIBLE) {
                                    getDrawerHandle().open();
                                }
                                return true;
                            }
                        }
                        int screen = ((AppFolder) mOpenFolder).mInfo.screen;
                        closeFolder(mOpenFolder);
                        if(AlmostNexusSettingsHelper.getAutoArrange(this)) {
                            getAllAppsSlidingView().autoArrange();
                        } else {
                            getAllAppsSlidingView().getAdapter().updateDataSet();
                        }
                        getAllAppsSlidingView().snapToScreen(screen);
                    } else if (allAppsOpen) {// allAppsOpen
                        mSlidingDrawer.animateClose();
                    }

                    stopDesktopEdit();

                }
                mWorkspace.unlock();
                if (mBottomBar.getVisibility() != View.VISIBLE) {
                    getDrawerHandle().open();
                }                
                return true;
            case KeyEvent.KEYCODE_HOME:
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void closeFolder() {
        Folder folder = mWorkspace.getOpenFolder();
        if (folder != null) {
            closeFolder(folder);
        }
    }

    void closeFolder(Folder folder) {
        folder.getInfo().opened = false;
        ViewGroup parent = (ViewGroup) folder.getParent();
        if (parent != null) {
            parent.removeView(folder);
        }
        folder.onClose();
        mOpenFolder = null;
        mDragLayer.removeDragListener(folder);
    }

    /**
     * When the notification that favorites have changed is received, requests a
     * favorites list refresh.
     */
    private void onFavoritesChanged() {
        mDesktopLocked = true;
        sModel.loadUserItems(false, this, false, false);
    }

    /**
     * Re-listen when widgets are reset.
     */
    private void onAppWidgetReset() {
        mAppWidgetHost.startListening();
    }

    void onDesktopItemsLoaded(ArrayList<ItemInfo> shortcuts,
            ArrayList<LauncherAppWidgetInfo> appWidgets) {
        if (mDestroyed) {
            if (LauncherModel.DEBUG_LOADERS) {
                Log.d(LauncherModel.LOG_TAG,
                        "  ------> destroyed, ignoring desktop items");
            }
            return;
        }

        bindDesktopItems(shortcuts, appWidgets);
    }

    /**
     * Refreshes the shortcuts shown on the workspace.
     */
    private void bindDesktopItems(ArrayList<ItemInfo> shortcuts,
            ArrayList<LauncherAppWidgetInfo> appWidgets) {    
        if (shortcuts == null || appWidgets == null) {
            if (LauncherModel.DEBUG_LOADERS) {
                Log.d(LauncherModel.LOG_TAG, "  ------> a source is null");
            }
            return;
        }

        final Workspace workspace = mWorkspace;
        int count = workspace.getChildCount();
        for (int i = 0; i < count; i++) {
            ((ViewGroup) workspace.getChildAt(i)).removeAllViewsInLayout();
        }

        if (DEBUG_USER_INTERFACE) {
            android.widget.Button finishButton = new android.widget.Button(this);
            finishButton.setText("Finish");
            workspace.addInScreen(finishButton, 1, 0, 0, 1, 1);

            finishButton
                    .setOnClickListener(new android.widget.Button.OnClickListener() {
                        public void onClick(View v) {
                            finish();
                        }
                    });
        }

        // Flag any old binder to terminate early
        if (mBinder != null) {
            mBinder.mTerminate = true;
        }

        mBinder = new DesktopBinder(this, shortcuts, appWidgets);
        mBinder.startBindingItems();
    }

    private void bindItems(Launcher.DesktopBinder binder,
            ArrayList<ItemInfo> shortcuts, int start, int count) {

        final Workspace workspace = mWorkspace;
        final boolean desktopLocked = mDesktopLocked;

        final int end = Math.min(start + DesktopBinder.ITEMS_COUNT, count);
        int i = start;
        for (; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);
            switch ((int) item.container) {
            case LauncherSettings.Favorites.CONTAINER_FAVORITEBAR:
                mBottomBar.setChildView((ApplicationInfo) item);
                break;
            default:
                switch (item.itemType) {
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    final View shortcut = createShortcut((ApplicationInfo) item);
                    workspace.addInScreen(shortcut, item.screen, item.cellX,
                            item.cellY, 1, 1, !desktopLocked);
                    break;
                case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
                    final FolderIcon newFolder = FolderIcon.fromXml(
                            R.layout.folder_icon, this, (ViewGroup) workspace
                                    .getChildAt(workspace.getCurrentScreen()),
                            (UserFolderInfo) item);
                    newFolder.mIsInDesktop = true;
                    if (themeFont != null) {
                        ((TextView) newFolder).setTypeface(themeFont);
                    }
                    workspace.addInScreen(newFolder, item.screen, item.cellX,
                            item.cellY, 1, 1, !desktopLocked);
                    break;

                }
            }
        }

        workspace.requestLayout();

        if (end >= count) {
            finishBindDesktopItems();
            if (PROFILE_STARTUP) {
                android.os.Debug.stopMethodTracing();
            }
            // Begin [pan 110815 load apps]add
            sModel.startLoadApps();
            final ApplicationsAdapter drawerAdapter = sModel
                    .getApplicationsAdapter();
            binder.mDrawerAdapter = drawerAdapter;
            // End
            binder.startBindingDrawer();
        } else {
            binder.obtainMessage(DesktopBinder.MESSAGE_BIND_ITEMS, i, count)
                    .sendToTarget();
        }
    }

    private void finishBindDesktopItems() {
        if (mSavedState != null) {
            if (!mWorkspace.hasFocus()) {
                mWorkspace.getChildAt(mWorkspace.getCurrentScreen())
                        .requestFocus();
            }

            final long[] userFolders = mSavedState
                    .getLongArray(RUNTIME_STATE_USER_FOLDERS);
            if (userFolders != null) {
                for (long folderId : userFolders) {
                    final FolderInfo info = sModel.findFolderById(folderId);
                    if (info != null) {
                        openFolder(info);
                    }
                }
                final Folder openFolder = mWorkspace.getOpenFolder();
                if (openFolder != null) {
                    openFolder.requestFocus();
                }
            }

            final boolean allApps = mSavedState.getBoolean(
                    RUNTIME_STATE_ALL_APPS_FOLDER, false);
            if (allApps) {
                // Begin [pan] modify
                mSlidingDrawer.open();
                // End
            }

            mSavedState = null;
        }

        if (mSavedInstanceState != null) {
            // ADW: sometimes on rotating the phone, some widgets fail to
            // restore its states.... so... damn.
            try {
                super.onRestoreInstanceState(mSavedInstanceState);
            } catch (Exception e) {
            }
            mSavedInstanceState = null;
        }

        if (allAppsOpen && !mAllAppsGrid.hasFocus()) {
            mAllAppsGrid.requestFocus();
        }

        mDesktopLocked = false;
    }

    private void bindDrawer(Launcher.DesktopBinder binder,
            ApplicationsAdapter drawerAdapter) {
        if (drawerAdapter != null) {
            drawerAdapter.buildViewCache((ViewGroup) mAllAppsGrid);
        }
        mAllAppsGrid.setAdapter(drawerAdapter);
        binder.startBindingAppWidgetsWhenIdle();
        isBindedDraw = true;
    }

    private void bindAppWidgets(Launcher.DesktopBinder binder,
            LinkedList<LauncherAppWidgetInfo> appWidgets) {

        final Workspace workspace = mWorkspace;
        final boolean desktopLocked = mDesktopLocked;

        if (!appWidgets.isEmpty()) {
            final LauncherAppWidgetInfo item = appWidgets.removeFirst();

            final int appWidgetId = item.appWidgetId;
            final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
                    .getAppWidgetInfo(appWidgetId);
            item.hostView = mAppWidgetHost.createView(this, appWidgetId,
                    appWidgetInfo);

            item.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            item.hostView.setTag(item);

            workspace.addInScreen(item.hostView, item.screen, item.cellX,
                    item.cellY, item.spanX, item.spanY, !desktopLocked);

            workspace.requestLayout();
            // finish load a widget, send it an intent
            if (appWidgetInfo != null) {
                appwidgetReadyBroadcast(appWidgetId, appWidgetInfo.provider,
                        new int[] { item.spanX, item.spanY });
            }
        }

        if (appWidgets.isEmpty()) {
            if (PROFILE_ROTATE) {
                android.os.Debug.stopMethodTracing();
            }
        } else {
            binder.obtainMessage(DesktopBinder.MESSAGE_BIND_APPWIDGETS)
                    .sendToTarget();
        }
    }

    /**
     * Launches the intent referred by the clicked shortcut.
     * 
     * @param v
     *            The view representing the clicked shortcut.
     */
    public void onClick(View v) {
        if (getOpenFolder() != null) {
            if (!((AppFolder) getOpenFolder()).getAppInfoListState()) {
                closeFolder(getOpenFolder());
            }
            return;
        }

        Object tag = v.getTag();
        
        if (tag == null) {
            return;
        }
        if (tag instanceof ApplicationInfo && !(tag instanceof FolderInfo)) {
            // Open shortcut
            final ApplicationInfo info = (ApplicationInfo) tag;
            final Intent intent = info.intent;
            int[] pos = new int[2];
            v.getLocationOnScreen(pos);
            try {
                intent.setSourceBounds(new Rect(pos[0], pos[1], pos[0]
                        + v.getWidth(), pos[1] + v.getHeight()));
            } catch (NoSuchMethodError e) {

            }
            startActivitySafely(intent);
        } else if (tag instanceof FolderInfo) {
            handleFolderClick((FolderInfo) tag);
        }
    }

    void startActivitySafely(Intent intent) {
        // Begin [pan] add
        if (getComponentName().equals(intent.getComponent())) {
            if (allAppsOpen) {
                mSlidingDrawer.animateClose();
            }
            return;
        }
        // End

        // Begin, added by zhumeiquan, 20121017
        if ( intent.getComponent() != null  && "com.lewa.app.AppList".equals(intent.getComponent().getClassName())) {
            mSlidingDrawer.animateOpen();
            return;
        }
        // End
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "Launcher does not have the permission to launch "
                            + intent
                            + ". Make sure to create a MAIN intent-filter for the corresponding activity "
                            + "or use the exported attribute for this activity.", e);
        }
    }

    private void handleFolderClick(FolderInfo folderInfo) {
        if (!folderInfo.opened) {
            // Close any open folder
            closeFolder();
            // Open the requested folder
            openFolder(folderInfo);
        } else {
            // Find the open folder...
            Folder openFolder = mWorkspace.getFolderForTag(folderInfo);
            int folderScreen;
            if (openFolder != null) {
                folderScreen = mWorkspace.getScreenForView(openFolder);
                // .. and close it
                closeFolder(openFolder);
                if (folderScreen != mWorkspace.getCurrentScreen()) {
                    // Close any folder open on the current screen
                    closeFolder();
                    // Pull the folder onto this screen
                    openFolder(folderInfo);
                }
            }
        }
    }

    /**
     * Opens the user fodler described by the specified tag. The opening of the
     * folder is animated relative to the specified View. If the View is null,
     * no animation is played.
     * 
     * @param folderInfo
     *            The FolderInfo describing the folder to open.
     */
    public void openFolder(FolderInfo folderInfo) {
        Folder openFolder;

        if (folderInfo instanceof UserFolderInfo) {
            openFolder = AppFolder.fromXml(this);
        } else {
            return;
        }

        mOpenFolder = openFolder;

        openFolder.setDragger(mDragLayer);
        openFolder.setLauncher(this);
        mDragLayer.addDragListener(openFolder);

        openFolder.bind(folderInfo);
        folderInfo.opened = true;

        openFolder.invalidate();
        mDragLayer.addView(openFolder);
        openFolder.onOpen();
    }

    /**
     * Returns true if the workspace is being loaded. When the workspace is
     * loading, no user interaction should be allowed to avoid any conflict.
     * 
     * @return True if the workspace is locked, false otherwise.
     */
    boolean isWorkspaceLocked() {
        return mDesktopLocked;
    }

    public boolean onLongClick(View v) {
        if (mDesktopLocked || getOpenFolder() != null) {
            return false;
        }
        // ADW: Show previews on longpressing the dots
        if (allAppsOpen) {
            setExchangeState(true);
            ApplicationInfo info = (ApplicationInfo) v.getTag();
            info = new ApplicationInfo(info);
            setCurrentBottomButton(info);
            v.setVisibility(View.INVISIBLE);
            mBottomBar.setDragView((CounterImageView) v, info.cellX);
            mDragLayer.startDrag(v, mBottomBar, info,
                    DragController.DRAG_ACTION_COPY);
            return true;
        }

        if (!(v instanceof CellLayout)) {
            v = (View) v.getParent();
        }

        CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) v.getTag();

        // This happens when long clicking an item with the dpad/trackball
        if (cellInfo == null) {
            return true;
        }

        if (mWorkspace.allowLongPress()) {
            if (cellInfo.cell == null) {
                if (cellInfo.valid) {
                    startEditMode(true);
                }
            } else {
                if (!(cellInfo.cell instanceof Folder)) {
                    // User long pressed on an item
                    mWorkspace.startDrag(cellInfo);
                    startDesktopEdit();
                }
            }
        }
        return true;
    }

    static LauncherModel getModel() {
        return sModel;
    }

    // Begin [pan] modify
    FixedBar getDrawerHandle() {
        return mDrawerToolbar;
    }
    // End

    Workspace getWorkspace() {
        return mWorkspace;
    }

    BottomBar getBottomBar() {
        return mBottomBar;
    }

    EditModeZone getEditModeZone() {
        return mEditorZone;
    }

    // ADW: we return a View, so classes using this should cast
    // to AllAppsGridView or AllAppsSlidingView if they need to access proper
    // members
    View getApplicationsGrid() {
        return (View) mAllAppsGrid;
    }

    AllAppsSlidingView getAllAppsSlidingView() {
        return (AllAppsSlidingView) mAllAppsGrid;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case DIALOG_CREATE_SHORTCUT:
            break;
        case DIALOG_RENAME_FOLDER:
            if (mFolderInfo != null) {
                EditText input = (EditText) dialog
                        .findViewById(R.id.folder_name);
                final CharSequence text = mFolderInfo.title;
                input.setText(text);
                input.setSelection(0, text.length());
            }
            break;
        }
    }

    /**
     * Receives notifications when applications are added/removed.
     */
    private class ApplicationsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                    || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                    || Intent.ACTION_PACKAGE_ADDED.equals(action)) {

                final String packageName = intent.getData()
                        .getSchemeSpecificPart();
                final boolean replacing = intent.getBooleanExtra(
                        Intent.EXTRA_REPLACING, false);

                if (LauncherModel.DEBUG_LOADERS) {
                    Log.d(LauncherModel.LOG_TAG,
                            "application intent received: " + action
                                    + ", replacing=" + replacing);
                    Log.d(LauncherModel.LOG_TAG, "  --> " + intent.getData());
                }

                if (!Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                    if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                        if (!replacing) {
                            removeShortcutsForPackage(packageName);
                            if (LauncherModel.DEBUG_LOADERS) {
                                Log.d(LauncherModel.LOG_TAG,
                                        "  --> remove package");
                            }
                            sModel.removePackage(Launcher.this, packageName);
                        }
                        // else, we are replacing the package, so a
                        // PACKAGE_ADDED will be sent
                        // later, we will update the package at this time
                    } else {
                        if (!replacing) {
                            if (LauncherModel.DEBUG_LOADERS) {
                                Log.d(LauncherModel.LOG_TAG,
                                        "  --> add package");
                            }
                            sModel.addPackage(Launcher.this, packageName);
                        } else {
                            if (LauncherModel.DEBUG_LOADERS) {
                                Log.d(LauncherModel.LOG_TAG,
                                        "  --> update package " + packageName);
                            }
                            sModel.updatePackage(Launcher.this, packageName);
                            String[] packageNames = new String[1];
                            packageNames[0] = packageName;
                            updateShortcutsForPackage(packageNames);
                        }
                    }
                    removeDialog(DIALOG_CREATE_SHORTCUT);
                } else {
                    if (LauncherModel.DEBUG_LOADERS) {
                        Log.d(LauncherModel.LOG_TAG, "  --> sync package "
                                + packageName);
                    }
                    sModel.syncPackage(Launcher.this, packageName);
                }
            } else {
                // ADW: Damn, this should be only for froyo!!!
                if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE
                        .equals(action)) {
                    final String packages[] = intent
                            .getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
                    if (packages == null || packages.length == 0) {
                        return;
                    } else {
                        updateShortcutsForPackage(packages);
                    }
                } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE
                        .equals(action)) {
                    String packages[] = intent
                            .getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
                    if (packages == null || packages.length == 0) {
                        return;
                    } else {
                        for (int i = 0; i < packages.length; i++) {
                            sModel.mUnabledApps.add(packages[i]);
                        }
                    }
                }
            }
        }
    }

    /**
     * Receives notifications when applications are added/removed.
     */
    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            closeSystemDialogs();
        }
    }

    /**
     * Receives notifications whenever the user favorites have changed.
     */
    private class FavoritesChangeObserver extends ContentObserver {
        public FavoritesChangeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.e("Launcher", "-  Databases changed   -   selfChange = "+selfChange);
            onFavoritesChanged();
        }
    }

    /**
     * Receives notifications whenever the appwidgets are reset.
     */
    private class AppWidgetResetObserver extends ContentObserver {
        public AppWidgetResetObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            onAppWidgetReset();
        }
    }

    private static class DesktopBinder extends Handler implements
            MessageQueue.IdleHandler {
        static final int MESSAGE_BIND_ITEMS = 0x1;
        static final int MESSAGE_BIND_APPWIDGETS = 0x2;
        static final int MESSAGE_BIND_DRAWER = 0x3;

        // Number of items to bind in every pass
        static final int ITEMS_COUNT = 6;

        private final ArrayList<ItemInfo> mShortcuts;
        private final LinkedList<LauncherAppWidgetInfo> mAppWidgets;
        private ApplicationsAdapter mDrawerAdapter;
        private final WeakReference<Launcher> mLauncher;

        public boolean mTerminate = false;

        DesktopBinder(Launcher launcher, ArrayList<ItemInfo> shortcuts,
                ArrayList<LauncherAppWidgetInfo> appWidgets) {
            mLauncher = new WeakReference<Launcher>(launcher);
            mShortcuts = shortcuts;
            // mDrawerAdapter = drawerAdapter;
            // Sort widgets so active workspace is bound first
            final int currentScreen = launcher.mWorkspace.getCurrentScreen();
            final int size = appWidgets.size();
            mAppWidgets = new LinkedList<LauncherAppWidgetInfo>();

            for (int i = 0; i < size; i++) {
                LauncherAppWidgetInfo appWidgetInfo = appWidgets.get(i);
                if (appWidgetInfo.screen == currentScreen) {
                    mAppWidgets.addFirst(appWidgetInfo);
                } else {
                    mAppWidgets.addLast(appWidgetInfo);
                }
            }
        }

        public void startBindingItems() {
            obtainMessage(MESSAGE_BIND_ITEMS, 0, mShortcuts.size())
                    .sendToTarget();
        }

        public void startBindingDrawer() {
            obtainMessage(MESSAGE_BIND_DRAWER).sendToTarget();
        }

        public void startBindingAppWidgetsWhenIdle() {
            // Ask for notification when message queue becomes idle
            final MessageQueue messageQueue = Looper.myQueue();
            messageQueue.addIdleHandler(this);
        }

        public boolean queueIdle() {
            // Queue is idle, so start binding items
            startBindingAppWidgets();
            return false;
        }

        public void startBindingAppWidgets() {
            obtainMessage(MESSAGE_BIND_APPWIDGETS).sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            Launcher launcher = mLauncher.get();
            if (launcher == null || mTerminate) {
                return;
            }

            switch (msg.what) {
            case MESSAGE_BIND_ITEMS: {
                launcher.bindItems(this, mShortcuts, msg.arg1, msg.arg2);
                break;
            }
            case MESSAGE_BIND_DRAWER: {
                launcher.bindDrawer(this, mDrawerAdapter);
                break;
            }
            case MESSAGE_BIND_APPWIDGETS: {
                launcher.bindAppWidgets(this, mAppWidgets);
                break;
            }
            }
        }
    }

    /****************************************************************
     * ADW: Start custom functions/modifications
     ***************************************************************/

    /**
     * ADW: Show the custom settings activity
     */
    private void showCustomConfig() {
        Intent launchPreferencesIntent = new Intent().setClass(this,
                MyLauncherSettings.class);
        startActivity(launchPreferencesIntent);
    }

    protected boolean isPreviewing() {
        return showingPreviews;
    }

    public void showPreviews() {
        if (mEditorZoneView != null) {
            return;
        }
        if (mWorkspace != null && mWorkspace.getChildCount() > 0) {
            if (mEditorZoneView != null || mDesktopLocked) {
                return;
            }
            // Begin [pan] modify
            if (!showingPreviews) {
                showingPreviews = true;
                if (mDesktopIndicator != null) {
                    mDesktopIndicator.hide();
                }
                final Workspace workspace = mWorkspace;
                if (workspace == null)
                    return;
                workspace.lock();
                mScreensEditor = (View) mInflater.inflate(
                        R.layout.screens_editor, null);
                mScreenGrid = (SenseWorkspace) mScreensEditor
                        .findViewById(R.id.gallery_screens);
                mScreenGrid.setBackgroundColor(0x80000000);
                mScreenGrid.setLauncher(this);
                mScreenGrid.setDragger(mDragLayer);
                mScreenGrid.setChildView();
                mDragLayer.addDragListener(mScreenGrid);
                mDragLayer.addView(mScreensEditor);
                if (mBottomBar.getVisibility() == View.VISIBLE) {
                    mBottomBar.close();
                }
                mScreenGrid.open(true);
                // End
            }
        }
    }

    boolean isAllAppsVisible() {
        return allAppsOpen;
    }

    boolean isAllAppsOpaque() {
        return mAllAppsGrid.isOpaque() && !allAppsAnimating;
    }

    /**
     * ADW: wallpaper intent receiver for proper trackicng of wallpaper changes
     */
    private static class WallpaperIntentReceiver extends BroadcastReceiver {
        private WeakReference<Launcher> mLauncher;

        WallpaperIntentReceiver(Application application, Launcher launcher) {
            setLauncher(launcher);
        }

        void setLauncher(Launcher launcher) {
            mLauncher = new WeakReference<Launcher>(launcher);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mLauncher != null) {
                final Launcher launcher = mLauncher.get();
                if (launcher != null) {
                    final Workspace workspace = launcher.getWorkspace();
                    if (workspace != null) {
                        workspace.setWallpaper(true);
                    }
                }
            }
        }
    }

    public void setWindowBackground(boolean lwp) {
        if (!lwp) {
            getWindow().setBackgroundDrawable(null);
            boolean hight = AlmostNexusSettingsHelper.getHighQuality(this);
            if (hight) {
                getWindow().setFormat(PixelFormat.RGBA_8888);
            } else {
                getWindow().setFormat(PixelFormat.RGB_565);
            }
        } else {
            getWindow().setBackgroundDrawable(new ColorDrawable(0));
            getWindow().setFormat(PixelFormat.TRANSPARENT);
        }
    }

    private boolean shouldRestart() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("lewa_theme", Context.MODE_PRIVATE);
            mShouldRestart = sharedPreferences.getBoolean("should_restart", false);
            sharedPreferences.edit().putBoolean("should_restart", false).commit();
            if (mShouldRestart) {
                android.os.Process.killProcess(android.os.Process.myPid());
                finish();
                startActivity(getIntent());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void appwidgetReadyBroadcast(int appWidgetId, ComponentName cname,
            int[] widgetSpan) {
        Intent motosize = new Intent(
                "com.motorola.blur.home.ACTION_SET_WIDGET_SIZE");

        motosize.setComponent(cname);
        motosize.putExtra("appWidgetId", appWidgetId);
        motosize.putExtra("spanX", widgetSpan[0]);
        motosize.putExtra("spanY", widgetSpan[1]);
        motosize.putExtra("com.motorola.blur.home.EXTRA_NEW_WIDGET", true);
        sendBroadcast(motosize);

        Intent ready = new Intent(LauncherIntent.Action.ACTION_READY)
                .putExtra(LauncherIntent.Extra.EXTRA_APPWIDGET_ID, appWidgetId)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                .putExtra(LauncherIntent.Extra.EXTRA_API_VERSION,
                        LauncherMetadata.CurrentAPIVersion).setComponent(cname);
        sendBroadcast(ready);
    }

    /**
     * wjax: Swipe down binding action
     */

    /**
     * wjax: Swipe up binding action
     */

    private void realAddWidget(AppWidgetProviderInfo appWidgetInfo,
            CellLayout.CellInfo cellInfo, int[] spans, int appWidgetId,
            int[] xyCell, boolean insertAtFirst) {
        // Try finding open space on Launcher screen
        if (xyCell == null) {
            xyCell = mCellCoordinates;
        }
        if (!findSlot(cellInfo, xyCell, spans[0], spans[1])) {
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
            return;
        }

        // Build Launcher-specific widget info and save to database
        LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(
                appWidgetId);
        launcherInfo.spanX = spans[0];
        launcherInfo.spanY = spans[1];

        LauncherModel.addItemToDatabase(this, launcherInfo,
                LauncherSettings.Favorites.CONTAINER_DESKTOP,
                mWorkspace.getCurrentScreen(), xyCell[0], xyCell[1], false);

        if (!mRestoring) {
            sModel.addDesktopAppWidget(launcherInfo);

            // Perform actual inflation because we're live
            launcherInfo.hostView = mAppWidgetHost.createView(this,
                    appWidgetId, appWidgetInfo);

            launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            launcherInfo.hostView.setTag(launcherInfo);

            mWorkspace.addInCurrentScreen(launcherInfo.hostView, xyCell[0],
                    xyCell[1], launcherInfo.spanX, launcherInfo.spanY,
                    !mDesktopLocked);
        } else if (sModel.isDesktopLoaded()) {
            sModel.addDesktopAppWidget(launcherInfo);
        }
        // finish load a widget, send it an intent
        if (appWidgetInfo != null) {
            appwidgetReadyBroadcast(appWidgetId, appWidgetInfo.provider, spans);
        }
    }

    public static int getScreenCount(Context context) {
        return AlmostNexusSettingsHelper.getDesktopScreens(context);
    }

    public DesktopIndicator getDesktopIndicator() {
        return mDesktopIndicator;
    }

    @Override
    protected void onRestart(){
        if(themeChanged){
            SharedPreferences sp = getSharedPreferences(
                    "launcher.preferences.almostnexus", Context.MODE_PRIVATE);
            sp.edit().putBoolean("lewadefwallpaper", false).commit();
            themeChanged = false;
            android.os.Process.killProcess(android.os.Process.myPid());
            finish();
            startActivity(getIntent());
         }
        super.onRestart();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public Typeface getThemeFont() {
        return themeFont;
    }

    public void startDesktopEdit() {
        for (int i = 0; i < mWorkspace.getChildCount(); i++) {
            if (mEditorZoneView == null) {
                mWorkspace.getChildAt(i).setBackgroundDrawable(
                        bitmap2drawable(R.drawable.addwidget_bg));
            }
        }
    }

    public void stopDesktopEdit() {
        // Begin [pan] modify
        if (showingPreviews && mScreensEditor != null) {
            showingPreviews = false;

            mWorkspace.clearChildrenCache();
            mWorkspace.unlock();
            if (mScreensEditor != null) {
                if (mScreenGrid != null) {
                    mDragLayer.removeDragListener(mScreenGrid);
                    mScreenGrid = null;
                }
                mDragLayer.removeView(mScreensEditor);
                mScreensEditor = null;
                mDrawerToolbar.open();
            }
        }

        if (mEditorZoneView != null) {
            for (int i = 0; i < mWorkspace.getChildCount(); i++) {
                ((CellLayout) mWorkspace.getChildAt(i))
                        .setBackgroundDrawable(null);
            }
            mBottomBar.open();
            mDragLayer.removeView(mEditorZoneView);
            mEditorZoneView = null;
            mAppInfoListView = null;
        }
        mWorkspace.invalidate();
    }

    @Override
    public void startActivity(Intent intent) {
        try {
            final ResolveInfo resolveInfo = getPackageManager()
                    .resolveActivity(intent, 0);
            if (resolveInfo == null) {
                Toast.makeText(this, R.string.activity_not_found,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            
            super.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found,
                    Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found,
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Begin [pan] add
    public Drawer getDrawer() {
        return mAllAppsGrid;
    }

    public int getHandleHeight() {
        int height = 0;
        if (mDrawerToolbar != null) {
            height = mDrawerToolbar.getHeight();
        }
        return height;
    }

    public int getIndicatorHeight() {
        int height = 0;
        if (mDesktopIndicator != null) {
            height = mDesktopIndicator.getHeight();
        }
        return height;
    }

    public void setCurrentBottomButton(ApplicationInfo info) {
        mCurrentBottomButton = info;
    }

    public ItemInfo getCurrentBottomButton() {
        return mCurrentBottomButton;
    }

    public void setExchangeState(boolean isDropOn) {
        mIsDropOn = isDropOn;
    }

    public boolean getExchangeState() {
        return mIsDropOn;
    }

    public Folder getOpenFolder() {
        return mOpenFolder;
    }

    public void setOpenFolder(Folder folder) {
        mOpenFolder = folder;
    }

    public void setAppFolderState(ApplicationInfo info) {
        mAppFolderItemInfo = info;
    }

    public ApplicationInfo getAppFolderState() {
        return mAppFolderItemInfo;
    }

    public MySlidingDrawer getSlidingDrawer() {
        return mSlidingDrawer;
    }

    public DeleteZone getDeleteZone() {
        return mDeleteZone;
    }

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

    public boolean getIsNewFolder() {
        return mIsNewAppFolder;
    }

    public void setAllAppsState(boolean isopen) {
        allAppsOpen = isopen;
    }

    public boolean isAppInfListVisible() {
        return (mAppInfoListView != null);
    }

    // End
    private void initEditMode() {
        final View wallpaper = mEditorZoneView
                .findViewById(R.id.wallpaper_category);
        final View widget = mEditorZoneView.findViewById(R.id.widget_category);
        final View shortcut = mEditorZoneView
                .findViewById(R.id.shortcut_category);
        final View folder = mEditorZoneView.findViewById(R.id.folder_category);

        final View wallpaperBtn = mEditorZoneView
                .findViewById(R.id.wallpaper_btn);
        final View widgetBtn = mEditorZoneView.findViewById(R.id.widget_btn);
        final View shortcutBtn = mEditorZoneView
                .findViewById(R.id.shortcut_btn);
        final View folderBtn = mEditorZoneView.findViewById(R.id.folder_btn);

        final AllInfoZone appinfolistView = (AllInfoZone) mEditorZoneView
                .findViewById(R.id.appsinfo_view);
        final AppInfoList appinfo_list = (AppInfoList) mEditorZoneView
                .findViewById(R.id.appinfo_list);
        mAppInfoListView = appinfo_list;
        // Wallpaper
        wallpaperBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (appinfolistView.getVisibility() == View.VISIBLE) {
                    appinfolistView.startOutAnimation();
                }
                startWallpaper();

                if (!(Boolean) shortcutBtn.getTag())
                    shortcut.setBackgroundResource(R.drawable.shortcut_btn);
                if (!(Boolean) folderBtn.getTag())
                    folder.setBackgroundResource(R.drawable.folder_btn);
                if (mIsRom && !(Boolean) widgetBtn.getTag())
                    widget.setBackgroundResource(R.drawable.widgets_btn);
            }

        });

        // Widget
        if (mIsRom) {
            widgetBtn.setTag(true);
            widgetBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if ((Boolean) view.getTag()) {
                        if (isPreviewing()) {
                            stopDesktopEdit();
                        }

                        widget.setBackgroundResource(R.drawable.widgets_btn_hi);
                        if (!(Boolean) shortcutBtn.getTag())
                            shortcut.setBackgroundResource(R.drawable.shortcut_btn);
                        if (!(Boolean) folderBtn.getTag())
                            folder.setBackgroundResource(R.drawable.folder_btn);

                        widgetBtn.setTag(false);
                        folderBtn.setTag(true);
                        shortcutBtn.setTag(true);

                        mAddItemCellInfo = new CellLayout.CellInfo();
                        appinfo_list.setAdapter(null);
                        appinfo_list.loadAppList(Launcher.this,
                                LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET);
                        appinfolistView.startInAnimation();
                    } else {
                        widgetBtn.setTag(true);
                        widget.setBackgroundResource(R.drawable.widgets_btn);
                        appinfolistView.startOutAnimation();
                    }

                }

            });
        } else {
            widgetBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (appinfolistView.getVisibility() == View.VISIBLE) {
                        appinfolistView.startOutAnimation();
                    }

                    if (isPreviewing()) {
                        stopDesktopEdit();
                    }
                    mAddItemCellInfo = new CellLayout.CellInfo();
                    pickWidget();

                    if (!(Boolean) shortcutBtn.getTag())
                        shortcut.setBackgroundResource(R.drawable.shortcut_btn);
                    if (!(Boolean) folderBtn.getTag())
                        folder.setBackgroundResource(R.drawable.folder_btn);
                }

            });
        }

        // Shortcut
        shortcutBtn.setTag(true);
        folderBtn.setTag(true);
        shortcutBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if ((Boolean) view.getTag()
                        || appinfolistView.getVisibility() == View.GONE) {

                    if (isPreviewing()) {
                        stopDesktopEdit();
                    }
                    appinfo_list.setAdapter(null);
                    appinfo_list.loadAppList(Launcher.this,
                            LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT);
                    appinfolistView.startInAnimation();

                    shortcut.setBackgroundResource(R.drawable.shortcut_btn_hi);
                    if (mIsRom && !(Boolean) widgetBtn.getTag())
                        widget.setBackgroundResource(R.drawable.widgets_btn);
                    if (!(Boolean) folderBtn.getTag())
                        folder.setBackgroundResource(R.drawable.folder_btn);

                    shortcutBtn.setTag(false);
                    folderBtn.setTag(true);
                    widgetBtn.setTag(true);
                } else {
                    shortcutBtn.setTag(true);
                    shortcut.setBackgroundResource(R.drawable.shortcut_btn);
                    appinfolistView.startOutAnimation();
                }
            }

        });

        // Folder
        folderBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if ((Boolean) view.getTag()
                        || appinfolistView.getVisibility() == View.GONE) {
                    if (isPreviewing()) {
                        stopDesktopEdit();
                    }
                    appinfo_list.setAdapter(null);
                    appinfo_list.loadAppList(Launcher.this,
                            LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER);
                    appinfolistView.startInAnimation();

                    folder.setBackgroundResource(R.drawable.folder_btn_hi);
                    if (mIsRom && !(Boolean) widgetBtn.getTag())
                        widget.setBackgroundResource(R.drawable.widgets_btn);
                    if (!(Boolean) shortcutBtn.getTag())
                        shortcut.setBackgroundResource(R.drawable.shortcut_btn);

                    folderBtn.setTag(false);
                    shortcutBtn.setTag(true);
                    widgetBtn.setTag(true);
                } else {
                    folderBtn.setTag(true);
                    folder.setBackgroundResource(R.drawable.folder_btn);
                    appinfolistView.startOutAnimation();
                }
            }

        });
        appinfo_list.setDragController(mDragLayer);
        appinfo_list.setOnItemLongClickListener(appinfo_list);
    }

    private void pickWidget() {
        int appWidgetId = Launcher.this.mAppWidgetHost.allocateAppWidgetId();

        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // add the search widget
        ArrayList<AppWidgetProviderInfo> customInfo = new ArrayList<AppWidgetProviderInfo>();
        AppWidgetProviderInfo info = new AppWidgetProviderInfo();
        info.provider = new ComponentName(getPackageName(), "XXX.YYY");
        info.label = getString(R.string.group_search);
        info.icon = R.drawable.ic_search_widget;
        customInfo.add(info);
        pickIntent.putParcelableArrayListExtra(
                AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);

        // start the pick activity
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    private void startEditMode(boolean isEnable) {
        if (mEditorZoneView == null) {
            mEditorZoneView = mInflater.inflate(R.layout.editmode_layout, null);
            mEditorZone = (EditModeZone) mEditorZoneView
                    .findViewById(R.id.editmode);
            mDragLayer.addView(mEditorZoneView);
            mDeleteZone.setEditModeZone(mEditorZone);
            initEditMode();
            if (isEnable) {
                for (int i = 0; i < mWorkspace.getChildCount(); i++) {
                    ((CellLayout) mWorkspace.getChildAt(i))
                            .setBackgroundDrawable(bitmap2drawable(R.drawable.addwidget_bg));
                }
            }
        }
        mBottomBar.close();
        mEditorZone.startInAnimation();
    }

    public boolean isEditZoneVisibility() {
        return (mEditorZoneView != null);
    }

    public void removeTipView() {
//        if (mLewaTip != null) {
//            mDragLayer.removeView(mLewaTip);
//            mLewaTip = null;
//        }
    }

    public void setDefaultWallpaper() {
        InputStream wallpaperStream = null;
        try {
            WallpaperManager wpm = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);
            
            String iconPath = "/data/system/face/wallpaper/wallpaper.jpg";
            File file = new File(iconPath);
      
            if (!file.exists()) {
                wallpaperStream = getResources().openRawResource(
                        R.drawable.wallpaper);
            } else {
                wallpaperStream = new FileInputStream(iconPath);
            }

            if (wallpaperStream == null) {
                wallpaperStream = getResources().openRawResource(
                        R.drawable.wallpaper);
            }

            wpm.setStream(wallpaperStream);

        } catch (IOException e) {
            Log.e(Launcher.LOG_TAG, "Failed to set wallpaper: " + e);
        } finally {
            try {
                if (wallpaperStream != null) {
                    wallpaperStream.close();
                    wallpaperStream = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public Options getBitmapOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inDither = false;

        return options;
    }

    public Drawable bitmap2drawable(int id) {
        return new BitmapDrawable(getResources(), BitmapFactory.decodeStream(
                getResources().openRawResource(id), null, getBitmapOptions()));
    }

    public void completeAddShortcut(ApplicationInfo info, boolean insertAtFirst) {
        CellLayout.CellInfo cellInfo = new CellLayout.CellInfo();
        cellInfo.screen = mWorkspace.getCurrentScreen();
        if (!findSingleSlot(cellInfo)) {
            return;
        }
        LauncherModel.addItemToDatabase(this, info,
                LauncherSettings.Favorites.CONTAINER_DESKTOP, cellInfo.screen,
                cellInfo.cellX, cellInfo.cellY, false);
        if (!mRestoring) {
            sModel.addDesktopItem(info);

            final View view = createShortcut(info);

            mWorkspace.addInCurrentScreen(view, cellInfo.cellX, cellInfo.cellY,
                    1, 1, insertAtFirst);
        } else if (sModel.isDesktopLoaded()) {
            sModel.addDesktopItem(info);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        try {
            super.onConfigurationChanged(config);

            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } catch (Exception ex) {
            
        }
    }
    // End

    private class ThemeReadyAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... arg0) {
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                createLewaRoot();
            }
            return "";
        }        
    }
    
    private void createLewaRoot(){
        String lewaSDRootPath = ThemeUtils.getRootSD();
        File lewaSDRootFile = new File(lewaSDRootPath);
        
         if (!lewaSDRootFile.exists()) {
             lewaSDRootFile.mkdirs();
         }
    }

    public void clearPopupTip() {
        if(mSlidingTip != null) {            
            mSlidingTip.dismiss();
            mSlidingTip = null;
        }
        
        if(mAppsTip != null) {
            mAppsTip.dismiss();
            mAppsTip = null;
        }
    }
    
    public boolean isPopupShowing() {
        if(mSlidingTip != null || mAppsTip != null) {            
            return true;
        }
        return false;
    }
    public void setDefaultIcon() {
        if (mDefaultIcon == null) {
            mDefaultIcon = getPackageManager().getDefaultActivityIcon();
            mDefaultIcon = Utilities.createIconThumbnail(mDefaultIcon, this);
        }

       if (mAsyncIconLoad == null) {
             mAsyncIconLoad = new AsyncIconLoader(this, mLocaleChanged);
       }

       if (mIconBGg == null) {
            Utilities.setDefaultValue(false);
            mIconBGg = mIconBackgroundSetting.getBackGroundResources();
       }

       if (mIconTopg == null) {
            Utilities.setDefaultValue(false);
            mIconTopg = mIconBackgroundSetting.getIconResources("com_android_mask",this);
       }
    }
    private static final String DATA_SYSTEM_FACE = "/data/system/face";
    private static final String DEFAULT_ICONS = "icons";
    private static final String DEFAULT_RES = "launcher";

    public static boolean runShellBat(String commond){
        Runtime runtime = Runtime.getRuntime();
        String[] commonds = new String[3];
        commonds[0] = "sh";
        commonds[1] = "-c";
        commonds[2] = commond;
        try {
            Process process = runtime.exec(commonds);
            int exitValue = process.waitFor();
            if(exitValue != 0){
                return true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
    
    private static void changeAccessPermission(String fileName) {
        StringBuilder sb = new StringBuilder("chmod 777 ");
        sb.append(fileName);
        runShellBat(sb.toString());
    }
    
    private static void initFaceDir(){
       
        changeAccessPermission("/data/system/");
        makeDir(DATA_SYSTEM_FACE);
        makeDir(DATA_SYSTEM_FACE+"/wallpaper");
    }
    private static void makeDir(String dirPath){
        File dir = new File(dirPath);
       
        if(!dir.exists()){
             dir.mkdirs();
             changeAccessPermission(dirPath);
        }
         
    }

    private boolean writeSourceToTarget(InputStream source,FileOutputStream target){
        BufferedOutputStream bos = null;
        
        try {
            bos = new BufferedOutputStream(target);
            byte[] buffer = new byte[2048];
            int temp = -1;
            while((temp = source.read(buffer)) != -1){
                bos.write(buffer, 0, temp);
            }
            bos.flush();
            target.flush();
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
         	try {
                if(bos != null){
                    bos.close();
                    bos = null;
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return false;
    }
    
    public void loadDefaultResource() {

        String defaultThemeFileName = "/system/media/default.lwt";
        String wapperFileName = "wallpaper/wallpaper.jpg";
        String iconsFileName = new StringBuilder().append(DATA_SYSTEM_FACE).append("/")
                .append(DEFAULT_ICONS).toString();
        String resFileName = new StringBuilder().append(DATA_SYSTEM_FACE).append("/")
                .append(DEFAULT_RES).toString();
        String wapperFileFullName = new StringBuilder().append(DATA_SYSTEM_FACE).append("/")
                .append(wapperFileName).toString();

        File icons = new File(iconsFileName);
        File res = new File(resFileName);
        File defaultLwtFile = new File(defaultThemeFileName);
        File wallpaper = new File(wapperFileFullName);
        ZipFile defaultLwt = null;
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            initFaceDir();
            if (!icons.exists() || !res.exists() || !wallpaper.exists()) {

                ZipEntry zipEntry;
                defaultLwt = new ZipFile(defaultLwtFile);
                if (!icons.exists()) {
                    zipEntry = defaultLwt.getEntry(DEFAULT_ICONS);
                    if (zipEntry != null) {
                        fos = new FileOutputStream(icons);
                        is = defaultLwt.getInputStream(zipEntry);

                        if (writeSourceToTarget(is, fos)) {
                            changeAccessPermission(iconsFileName);
                        } else {
                            Log.e(LOG_TAG, "ERROR! writed icons failed.");
                        }
                        fos.close();
                        is.close();
                    } else {
                        Log.e(LOG_TAG, "ERROR! read icons failed.");
                    }

                }

                if (!res.exists()) {

                    zipEntry = defaultLwt.getEntry(DEFAULT_RES);
                    if (zipEntry != null) {
                        fos = new FileOutputStream(res);
                        is = defaultLwt.getInputStream(zipEntry);

                        if (writeSourceToTarget(is, fos)) {
                            changeAccessPermission(resFileName);
                        } else {
                            Log.e(LOG_TAG, "ERROR! writed icons failed.");
                        }
                        fos.close();
                        is.close();
                    } else {
                        Log.e(LOG_TAG, "ERROR! read icons failed.");
                    }
                }

                // String imgFileName = new
                // StringBuilder(LockScreenConstants.WALLPAPER_DIR_FP).append(LockScreenConstants.WALLPAPER_FILE_NAME_JPG).toString();

                zipEntry = defaultLwt.getEntry(wapperFileName);
                if (!wallpaper.exists()) {
                    if (zipEntry != null) {
                        fos = new FileOutputStream(wallpaper);
                        is = defaultLwt.getInputStream(zipEntry);
                        if (writeSourceToTarget(is, fos)) {
                            changeAccessPermission(wapperFileFullName);
                            setDefaultWallpaper();
                        } else {
                            Log.e(LOG_TAG, "ERROR! writed wallpaper failed.");
                        }
                    } else {
                        Log.e(LOG_TAG, "ERROR! read wallpaper failed.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                    fos = null;
                }
                if (is != null) {
                    is.close();
                    is = null;
                }

                if (defaultLwt != null) {
                    defaultLwt.close();
                    defaultLwt = null;
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
