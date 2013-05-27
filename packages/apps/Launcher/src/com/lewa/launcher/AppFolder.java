package com.lewa.launcher;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.lewa.launcher.AppInfoList;
import com.lewa.launcher.AppInfoList.AppInfoAdapter;
import com.lewa.launcher.AsyncIconLoader.ImageCallback;
import com.lewa.launcher.DragController.DragListener;
import com.lewa.launcher.LauncherSettings.BaseLauncherColumns;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Folder which contains applications or shortcuts chosen by the user.
 *
 */
public class AppFolder extends Folder implements DropTarget, DragSource,DragListener {
    
 // begin zhaolei add
    private TextView mTitle;
    private EditText mTitleEditor;
    private ImageView mTitleEditImage;
    private ImageView mSaveImage;
    private FolderAdapter mAppFolderAdapter;
    private AllInfoZone mAppinfolistView;
    private AppInfoList mAppInfoList;
    private RelativeLayout mCacheChild;
    private ArrayList<ApplicationInfo> mAppFolderItems = new ArrayList<ApplicationInfo>();
    private ApplicationInfo mPlusInfo = null;
    private static final int MAX_COUNT_IN_APPFOLDER = 12; 
    private final static int mNumColumns = 4;
    private final static int mNumRows = 3;
    
    private int mItemsValidLeft = 0;
    private int mItemsValidTop = 0;
    private int mItemValidWidth = 0;
    private int mItemValidHeight = 0;
    private int mGridViewWidth;
    private int mGridViewHeight;
    private int mToPos = -1;
    private int mIconWidth = 0;
    private boolean mIsEditMode = false;
    
    private ApplicationInfo mDragInfo;
    
    // end
    
    public AppFolder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }   
    
    /**
     * Creates a new UserFolder, inflated from R.layout.user_folder.
     *
     * @param context The application's context.
     *
     * @return A new UserFolder.
     */
    static AppFolder fromXml(Context context) {
		return (AppFolder) LayoutInflater.from(context).inflate(
				R.layout.app_folder, null);
    }    
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTitle = (TextView) findViewById(R.id.folder_title);
        mTitleEditor = (EditText) findViewById(R.id.folder_title_editor);
        mTitleEditImage = (ImageView) findViewById(R.id.folder_edit_image);
        mSaveImage = (ImageView) findViewById(R.id.folder_save); 
        mCacheChild = (RelativeLayout) findViewById(R.id.cacheChild);
        mAppinfolistView = (AllInfoZone) findViewById(R.id.appsinfoview);
        mAppInfoList = (AppInfoList) findViewById(R.id.appinfolist);
        mCacheChild = (RelativeLayout) findViewById(R.id.cacheChild);

        EditClickListener editClickListener = new EditClickListener();
        mTitleEditImage.setOnClickListener(editClickListener);

        SaveClickListener saveClickListener = new SaveClickListener();
        mSaveImage.setOnClickListener(saveClickListener);
    }

    @Override
    void bind(FolderInfo info) {
        super.bind(info);

        CharSequence appFolderTitle = info.title;
        mTitle.setText(appFolderTitle);
        mTitleEditor.setText(getInfo().title);

        mAppFolderItems = ((UserFolderInfo) info).contents;
        mAppFolderAdapter = new FolderAdapter(mContext, mAppFolderItems);
        addPlusImage();   
    }
    
    void getLayoutParameter(){
        mGridViewWidth = mContent.getMeasuredWidth();
        mGridViewHeight = mContent.getMeasuredHeight();

        mItemsValidTop = mCacheChild.getMeasuredHeight() - mGridViewHeight
                + getPaddingTop() + mContent.getPaddingTop() ;
        mItemsValidLeft = mCacheChild.getLeft() + mCacheChild.getPaddingLeft() + mContent.getLeft();

        mItemValidWidth = mGridViewWidth / mNumColumns;
        mItemValidHeight = mGridViewHeight / mNumRows;
    }

    @Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
        return true;
    }

    @Override
    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
		if(source instanceof AppInfoList && y <= mCacheChild.getBottom()){
		    addAppInFolder((ApplicationInfo)dragInfo);
		}
    }

    @Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
	}
 
    @Override
    public void onDragOver(DragSource source, int x, int y, int xOffset,
            int yOffset, Object dragInfo) {  
        if (source instanceof AppInfoList) {
            return;
        }
        getLayoutParameter();
         
        int dragPos = ((ApplicationInfo)dragInfo).cellX;
        
        mDragInfo = (ApplicationInfo) dragInfo;
        
        int toPos = (x - mItemsValidLeft) / mItemValidWidth
                + (y - mItemsValidTop) / mItemValidHeight * mNumColumns;
        
        if(toPos >= mAppFolderItems.size() && toPos < MAX_COUNT_IN_APPFOLDER){   
            toPos = mAppFolderItems.size() - 1;
        }         
        
        int off = 12;  
       
        if(toPos >= MAX_COUNT_IN_APPFOLDER || toPos < 0
                ||(x - xOffset <= -off || x + mIconWidth - xOffset >= getMeasuredWidth() + off)){
            leaveAppFolder();
            return;
        }  
        
        if( dragPos == toPos) {
            return;
        }
        moveAppPosition(dragPos, toPos);  
    }
    
    private void moveAppPosition(int dragPos, int toPos){
        ArrayList<ApplicationInfo> allAppFolderItems = mAppFolderItems;        
        ApplicationInfo dragInfo = mDragInfo;
        
        allAppFolderItems.remove(dragPos);       
        allAppFolderItems.add(toPos,dragInfo);
        resetApplications(allAppFolderItems);
        ((UserFolderInfo) mInfo).mFolderIcon.updateFolderIcon(); 
        mToPos = toPos;
        setContentAdapter(mAppFolderAdapter); 
        mContent.getAdapter().getView(toPos, null, mContent).setVisibility(INVISIBLE);
    }    
    
    private void resetApplications(ArrayList<ApplicationInfo> apps) {
		synchronized (apps) {
			int index = 0;
			int count = apps.size();
			ApplicationInfo info;
			for (int i = 0; i < count; ++i) {
				info = apps.get(i);
					
				if (info.cellX != index) {
				
					info.cellX = index;
					LauncherModel.moveItemInDatabase(mLauncher, info, info.container,
							info.screen, info.cellX, info.cellY);
				}
				index++;
			}
		}
	}
    
        
    private boolean leaveAppFolder(){
        if(!getAppInfoListState()){
            mLauncher.closeFolder(this);
            mLauncher.setOpenFolder(null);
        }        
        return true;
    }

    @Override
	public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {        
        if(mPlusInfo == null && mLauncher.getOpenFolder() != null) {
            addPlusImage();
        }
        
    }

    @Override
    public void onDropCompleted(View target, boolean success) {
        if(!success){
            return;
        }
        if(mInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {
            if (target instanceof Workspace || target instanceof FolderIcon || target instanceof HomeZone
                    || target instanceof DeleteZone || target instanceof BottomBar) {
                //((UserFolderInfo) mInfo).mFolderIcon.setVisibility(VISIBLE);
                //((UserFolderInfo) mInfo).contents.add(mDragCellX, mDragInfo);
                //((UserFolderInfo) mInfo).mFolderIcon.updateFolderIcon();
            } else if(target instanceof AllAppsSlidingView) {
                ((UserFolderInfo) mInfo).contents.remove(mDragInfo);
                ((UserFolderInfo) mInfo).mFolderIcon.updateFolderIcon();
            } 
            mLauncher.getAllAppsSlidingView().onDropCompleted(target, success);
        } else if (mInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER) {
            if (target instanceof Workspace) {
                ((UserFolderInfo) mInfo).contents.remove(mDragInfo);
                ((UserFolderInfo) mInfo).mFolderIcon.updateFolderIcon();
            }
        }   
    }

    // When the folder opens, we need to refresh the GridView's selection by
    // forcing a layout
    @Override
    void onOpen() {
        super.onOpen();
        requestFocus();        
    }
    
    @Override
    void onClose() {
        super.onClose();
        if(mPlusInfo != null){
            mAppFolderAdapter.remove(mPlusInfo);
            mPlusInfo = null;
        }        
    }
    
    public void removeFromAdapter(ApplicationInfo info){
        mAppFolderAdapter.remove(info);
    }
    
    public int countInAppFolder(){
        return mAppFolderAdapter.getCount();
    }
    
    public ApplicationInfo getFirstInfo(){
        return mAppFolderAdapter.getItem(0);
    }
    
    public boolean getAppInfoListState(){
        if(mAppInfoList != null){
            return mAppInfoList.getAppListState();
        }
        return false;  
    }
    
    public void setAppInfoListState(boolean isOpen){
        mAppInfoList.setAppListState(isOpen);
    }
    
    FolderAdapter getFolderAdapter(){
       return mAppFolderAdapter;  
    }
    
    @Override
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        
        ApplicationInfo app = (ApplicationInfo) parent.getItemAtPosition(position);

        if(getAppInfoListState()) {
            return;
        }

        if(app != null) {
            if(app.intent != null)
                mLauncher.startActivitySafely(app.intent);
        }            
        mLauncher.closeFolder(this);        
        mLauncher.setOpenFolder(null);
        if (mLauncher.getBottomBar().getVisibility() != View.VISIBLE)
            mLauncher.getDrawerHandle().open();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        
        if(getAppInfoListState()) {
            return false;
        }

        if ((!view.isInTouchMode())
                || (mPlusInfo != null && position == mAppFolderItems.size() - 1)) {
            return false;
        }        
        
        if (mPlusInfo != null) {
            mAppFolderItems.remove(mPlusInfo);
            mPlusInfo = null;            
        }        
            
        ApplicationInfo appInfo = (ApplicationInfo) parent.getItemAtPosition(position);
        mIconWidth= view.getWidth();
        mDragger.startDrag(view, this, appInfo, DragController.DRAG_ACTION_MOVE);
        
        return true;
    }
    
    public void addAppInFolder(ApplicationInfo info) {
        if (mAppFolderAdapter.getCount() < MAX_COUNT_IN_APPFOLDER) {
            info.screen = mInfo.screen;
            info.cellX = mAppFolderAdapter.getCount();
            if(mLauncher.isAllAppsVisible()){
            	synchronized (ApplicationsAdapter.allItems) {
					
	                final ArrayList<ApplicationInfo> allItems = ApplicationsAdapter.allItems;
	                int pos = info.cellX;                
	                if(mAppFolderAdapter.getCount() == 0){
	                    mInfo.id =  LauncherModel.addFolderInDatabase(mLauncher, mInfo,
	                            LauncherSettings.Favorites.CONTAINER_MAINMENU, mInfo.screen,
	                            mInfo.cellX, mInfo.cellY); 
	                    FolderIcon appFolderIcon = FolderIcon.fromXml(R.layout.folder_icon,
	                            mLauncher, mLauncher.getAllAppsSlidingView(), (UserFolderInfo)mInfo);
	                    ((UserFolderInfo)mInfo).setFolderIcon(appFolderIcon);
	                }
	                allItems.remove(info);
	                LauncherModel.addOrMoveItemInDatabase(mLauncher, info,
	                        mInfo.id, info.screen, info.cellX, 0);
	                LauncherModel.resetApplications(allItems, 0, mLauncher
	                        .getAllAppsSlidingView().getTotalScreens());
	                mLauncher.getAllAppsSlidingView().getAdapter().updateDataSet();
	                AppInfoAdapter listAdapter = mAppInfoList.getAdapter();
	                listAdapter.remove(info);
	                mAppInfoList.setAdapter(listAdapter);
	                pos = mAppInfoList.getCurrentPos();
	                if(pos >= listAdapter.getCount()){
	                    pos = listAdapter.getCount() - 1;
	                }
	                mAppInfoList.setSelection(pos);
            	}
            } else {
                LauncherModel.addItemToDatabase(mLauncher, info, mInfo.id, info.screen, info.cellX, 0, false);
            }
            mAppFolderAdapter.add(info);            
            ((UserFolderInfo) mInfo).contents = mAppFolderItems;
            ((UserFolderInfo) mInfo).mFolderIcon.updateFolderIcon();              
        } else {
            Toast t = Toast.makeText(getContext(), R.string.full_folder,
                    Toast.LENGTH_SHORT);
            t.show();
        }
        if(info.cellX == MAX_COUNT_IN_APPFOLDER - 1) {
            mPlusInfo = null;
        }
    }
    
    public AppInfoList getAppInfoList(){
        return mAppInfoList;
    }
    
    public void addPlusImage(){
        mToPos = -1;
        if (mAppFolderAdapter.getCount() < MAX_COUNT_IN_APPFOLDER) {
            mPlusInfo = new ApplicationInfo();
            mPlusInfo.screen = -500;
            mAppFolderAdapter.add(mPlusInfo);
        }  
        setContentAdapter(mAppFolderAdapter);
    }
    
    public void removePlusImage(){
        mAppFolderAdapter.remove(mPlusInfo);
    }
    
    private class FolderAdapter extends ArrayAdapter<ApplicationInfo> {
    	private LayoutInflater mInflater;
    	private int mTextColor = 0;
    	private boolean useThemeTextColor = false;
        private Typeface themeFont=null;
    	
		public FolderAdapter(Context context, ArrayList<ApplicationInfo> icons) {
			super(context, 0,icons);
			mInflater=LayoutInflater.from(context);
		}
		@Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            
            ApplicationInfo info = getItem(position);
            
            if(info == null) {
                return null;
            }

            if (info.iconBackground != null) {
                convertView = mInflater.inflate(mLauncher.mShortCutStyles[0],
                        parent, false);
            } else {
                convertView = mInflater.inflate(mLauncher.mShortCutStyles[1], parent,
                        false);
            }

            CounterTextView plusView = (CounterTextView) convertView;

            if (info.screen == -500) {

                Drawable plus = mLauncher.getResources().getDrawable(
                        R.drawable.lewa_app_plus);           
                plusView.setCompoundDrawablesWithIntrinsicBounds(null,
                        plus, null, null);
                plusView.mSelectedBackGround = null;
                plusView.mBgPaddingTop = mLauncher.mBgPaddingTop;
                plusView.setPadding(0, mLauncher.mIconPaddingTop, 0, 0);           
                
                plusView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        
                        if(!mLauncher.isAllAppsVisible()) {
                            mLauncher.getDrawerHandle().close();
                        }

                        removePlusImage();
                     
                        mAppinfolistView.setVisibility(View.VISIBLE);
                        mAppInfoList.setAppListState(true);
                        mAppInfoList.setIsAppFolder(true);
                        mAppinfolistView.startInAnimation();
                        mAppInfoList.loadAppList(mLauncher,
                                BaseLauncherColumns.ITEM_TYPE_SHORTCUT);
                        mAppInfoList.setDragController(mLauncher.getDragLayer());
                        mAppInfoList.setOnItemLongClickListener(mAppInfoList);
                        mAppInfoList.setOnItemSelectedListener(mAppInfoList);
                        if(mIsEditMode){
                            saveTitle();
                        }
                    }
                });
                 
                plusView.setText("");
                return plusView;
            }
            
            info.cellX = position;

            convertView.setBackgroundResource(0);
            final CounterTextView textView = (CounterTextView)convertView;
            
            if(!info.isLewaIcon || Launcher.mLocaleChanged) {
    			info = mLauncher.mAsyncIconLoad.loadDrawable(info, false ,new ImageCallback() {
    				
    				@Override
    				public void imageLoaded(ApplicationInfo appInfo) {
    					// TODO Auto-generated method stub
    					textView.setCompoundDrawablesWithIntrinsicBounds(null, appInfo.icon, null,
    							null);
    					textView.setText(appInfo.title);
    					textView.setTag(appInfo);
    				}
    			});
    		}
            final int iconBgPadding = mLauncher.mBgPaddingTop;
            if (info.iconBackground != null) {  
                int iconPaddingTop = mLauncher.mIconPaddingTop;
                textView.mBgPaddingTop = iconBgPadding;
                textView.setPadding(2, iconPaddingTop, 2, 0);
            }else {
                textView.setPadding(2, iconBgPadding, 2, 0);
            }
            
            textView.setCompoundDrawablesWithIntrinsicBounds(null, info.icon,
                    null, null);
            
            textView.mIsInFolder = true;
            textView.setText(info.title);
            if (useThemeTextColor) {
                textView.setTextColor(mTextColor);
            }
            // ADW: Custom font
            if (themeFont != null)
                textView.setTypeface(themeFont);
            // so i'd better not use it, sorry themers
            if(info.iconBackground != null){
            	textView.mBackGround = mLauncher.mIconBGg;
            	textView.mIconTopDrawable = mLauncher.mIconTopg;
    		}
            /**
             * if the app is system app,should not add the iconbg cover
             */
            //if(info.iconBackground != null ){  	
	         //   textView.mIcon_bg = mLauncher.mIcon_bg;
	        //}
            
            if(position == mToPos){
                convertView.setVisibility(INVISIBLE);
            }
            return convertView;
        }
    }

    private class EditClickListener implements android.view.View.OnClickListener {

        @Override
        public void onClick(View view) {

            mTitleEditor.setVisibility(View.VISIBLE);
            mTitleEditor.requestFocus();
            mIsEditMode = true;
            
            openKeyboard();
            mSaveImage.setVisibility(View.VISIBLE);
            mTitle.setVisibility(View.INVISIBLE);
            mTitleEditImage.setVisibility(View.INVISIBLE);
        }
    }

    private class SaveClickListener implements android.view.View.OnClickListener {

        @Override
        public void onClick(View view) {
            saveTitle();            
        }
    }
    
    private void saveTitle() {
        FolderInfo info = getInfo();

        info.title = mTitleEditor.getText().toString();
        LauncherModel.updateItemInDatabase(mContext, info);        

        mTitle.setText(info.title);
        mTitle.setVisibility(View.VISIBLE);
        mTitleEditor.setVisibility(View.INVISIBLE);
        mIsEditMode = false;
        closeKeyboard();

        mSaveImage.setVisibility(View.INVISIBLE);
        mTitleEditImage.setVisibility(View.VISIBLE);

        if (mLauncher.getOpenFolder() != null && !mLauncher.getIsNewFolder()) {
            final FolderIcon folderIcon = ((UserFolderInfo)mInfo).mFolderIcon;
            if (folderIcon != null) {
                folderIcon.setText(info.title);
                mLauncher.getAllAppsSlidingView().requestLayout();
            }
        }

    }

    private void openKeyboard() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager imm = (InputMethodManager) mTitleEditor
                        .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 200);
    }

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) mTitleEditor
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mTitleEditor.getWindowToken(), 0);
    }

}
