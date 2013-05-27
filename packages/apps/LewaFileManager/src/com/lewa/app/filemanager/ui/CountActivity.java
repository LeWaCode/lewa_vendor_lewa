package com.lewa.app.filemanager.ui;

import android.app.ProgressDialog;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.HandlerThread;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.Toast;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.config.Config;
import com.lewa.filemanager.config.RuntimeArg;
import com.lewa.filemanager.ds.database.copersynch.DatasrcDelete;
import com.lewa.filemanager.ds.database.MediaArgs;
import com.lewa.filemanager.ds.database.service.FileScanService;
import com.lewa.filemanager.ds.database.MimeSrc;
import com.lewa.filemanager.ds.uri.NavigationNode;
import com.lewa.filemanager.actions.sort.ObjectSort;
import com.lewa.filemanager.actions.EqualCheck;
import com.lewa.filemanager.ds.database.CategoryInfo;
import com.lewa.filemanager.ds.database.CategoryItem;
import com.lewa.filemanager.actions.OperationUtil;
import com.lewa.base.adapter.MapAdapter;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.DensityUtil;
import com.lewa.filemanager.beans.FileUtil;
import com.lewa.filemanager.actions.sort.DBSort;
import com.lewa.filemanager.actions.sort.Sort;
import com.lewa.filemanager.ds.uri.AccountNode;
import com.lewa.filemanager.ds.uri.NavigationConstants;
import com.lewa.filemanager.activities.views.Switcher;
import com.lewa.filemanager.activities.views.ViewHolder;
import com.lewa.base.Logs;
import com.lewa.filemanager.util.ReadSystemMemory;
import com.lewa.filemanager.ds.database.service.ScanReceiver;
import com.lewa.filemanager.beans.MusicInfo;
import com.lewa.base.SharedPreferenceUtil;
import com.lewa.filemanager.util.StatusCheckUtil;
import java.io.File;
import java.util.Collections;

public class CountActivity extends FileCursorActivity {

    protected static final int MESS_LOAD_FINISH = 0;
    protected static final int MESS_SCAN_FINISH = 1;
    public static final int MESS_UPDATE_UI_COUNT = 2;
    protected TextView totalNumView;
    protected AdapterView categoryAdaptView;
    protected View homeView;
    List<CategoryInfo> category = new ArrayList<CategoryInfo>(0);
    protected CategoryItem categoryItem;
    public static CountActivity categoryActivity;
    boolean startupFlag = false;
    public ProgressDialog pd;
    public int countSharedPrefernce = 0;
    SharedPreferences sp;
    boolean isNoZero = false;
    private View viewAudio;
    private View viewImage;
    private View viewVideo;
    private View viewDoc;
    private View viewApk;
    private View viewTheme;
    private Map blockSizeMap;
	public HomeViewHolder homeViewHolder;
    private boolean scanBarToHideFlag = true;

    public void initDBData(String certainCategory) {
		category.clear();
		Map<String, CategoryItem> wholeCategories = MimeSrc
				.initWholeCategories(context, certainCategory, category);
    }

    @Override
    protected void updateCountActivity(FileInfo fileInfo) {
        DatasrcDelete.recursiveUpdateDel(fileInfo, this);
        refresh();
    }

    @Override
    protected void setHiddenFileShowFlag() {
        this.isHiddenFileShow = Config.ACCOUNT_HIDE_OPTION;
    }

    @Override
    protected void setSort() {
        policy = new DBSort(Sort.SORT_POLICY_TITLE, Sort.SORT_SEQ_ASC);
    }

    @Override
    public boolean isInListDetail() {
        return !this.navTool.isAtTop();
    }
    public boolean finishStarted;


    public List<CategoryInfo> initCount() {
        if (category.isEmpty()) {
            isNoZero = addEmptyCategory(R.string.music, R.drawable.music,
                    category) == 0;
            isNoZero = addEmptyCategory(R.string.image, R.drawable.picture,
                    category) == 0;
            isNoZero = addEmptyCategory(R.string.video, R.drawable.vedio,
                    category) == 0;
            isNoZero = addEmptyCategory(R.string.doc, R.drawable.doc,
                    category) == 0;
            isNoZero = addEmptyCategory(R.string.app, R.drawable.apk,
                    category) == 0;
        }

        return category;
    }
    protected boolean bcurrentpage;
    protected TextView plusNumView;
    protected Boolean launchFlag;

    @Override
    protected void swithToPrivacyActivity() {
        EqualCheck nc = new EqualCheck(ObjectSort.SORT_POLICY_TITLE, ObjectSort.SORT_SEQ_ASC);
        Collections.sort(OperationUtil.selectedEntities, nc);
        if (nc.isResult()) {
            Toast.makeText(context, R.string.same_name_files_operation_cancel, Toast.LENGTH_LONG).show();
            operationUtil.dataClear();
            return;
        }
        SlideActivity.paramActivity.isInOperation = OperationUtil.OPER_TYPE_MOVE_PRIVACY;
        showBottomBar(View.GONE);
        SlideActivity.fileActivityInstance.setDisplayScreen(2);
        ((PrivacyActivity) PrivacyActivity.activityInstance).switchToMove();
    }

    @Override
    protected void cutOrCopy(int command) {
        if (performCountActivityCopy(command)) {
            return;
        }
    }
    protected View progressbar_updown;
    protected Bundle savedInstanceState;
    protected MapAdapter homeAdapter;

    @Override
    protected void onResume() {
        super.onResume();
		Logs.i("======== categoryVisitFlag " + categoryVisitFlag);
		if (categoryVisitFlag) {
			String value_lwt_deleted = (String) SharedPreferenceUtil.getValue(
					context, Constants.SharedPrefernce.RememberedCategory,
					Constants.SharedPrefernce.KEY_LWT_ISDELETED, String.class);
			if (value_lwt_deleted != null
					&& !value_lwt_deleted.trim().equals("")) {
				File deletedLWT = new File(value_lwt_deleted);
				if (!deletedLWT.exists()) {
					DatasrcDelete.recursiveUpdateDel(new FileInfo(deletedLWT,
							context), context);
					this.viewHolder.refresh();
				}
				SharedPreferenceUtil.putValue(context,
						Constants.SharedPrefernce.RememberedCategory,
						Constants.SharedPrefernce.KEY_LWT_ISDELETED, "");
			}
			Logs.i("======== categoryVisitFlag after " + categoryVisitFlag);
		} else {
			categoryVisitFlag = true;
		}
	}

	boolean categoryVisitFlag;
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
		categoryVisitFlag = false;
        homeViewHolder = new HomeViewHolder(homeView, this);
		categoryActivity = this;
		String action = this.getIntent().getAction();
		if (action != null && action.equals(InnerCallActivity.COUNT_INVOKED)) {
        } else {
            contentView = homeView;

            if (!Config.isLewaRom) {
                Intent globalScan = new Intent(this, FileScanService.class);
                this.startService(globalScan);
            }
        }

    }

    public void setScanBarHide(boolean scanBarToHideFlag) {
        this.scanBarToHideFlag = scanBarToHideFlag;
    }

    public void hideScanBar(int makeHide) {
        View scanBar = findViewById(R.id.scanProgress);
        if (scanBar != null) {
            scanBar.setVisibility(makeHide);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (this.navTool.isAtTop()) {
            return false;
        }
        menu.removeItem(R.id.menu_new_dir);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.navTool.getCurrNavEntitySource() != null && this.navTool.getCurrNavEntitySource().toString().toLowerCase().equals(Constants.CateContants.CATE_IMAGES)) {
            if (menu.findItem(MENU_MY_PIC_SIZE) == null) {
                menu.add(0, MENU_MY_PIC_SIZE, 0, getString(R.string.menu_my_pic_size)).setIcon(R.drawable.ic_menu_pic);
            }
        } else {
            menu.removeItem(MENU_MY_PIC_SIZE);
        }
        super.onPrepareOptionsMenu(menu);
        Menu m = menu.findItem(R.id.menu_sort).getSubMenu();
        if (this.navTool.navEntity.peek().producingSource.toString().toUpperCase().equals("AUDIO")) {
            if (Config.isLewaRom) {
                hideMusicMenu(m, true);
            }
        } else {
            if (sortMenuMarkInit == SortMenuInAPK || sortMenuMarkInit == SortMenuInMusic) {
                menu.findItem(R.id.menu_sort).getSubMenu().findItem(R.id.menu_cateogry_sort_name).setChecked(true);
            }
            MenuItem sortType = menu.findItem(R.id.menu_sort).getSubMenu().findItem(R.id.menu_cateogry_sort_type);
            sortType.setVisible(!this.navTool.navEntity.peek().producingSource.equals(Constants.CateContants.CATE_PACKAGE));
            hideMusicMenu(m, false);
        }
        return true;
    }
    public boolean finished;

    @Override
    public void startup() {

        // TODO Auto-generated method stub
        if (pd != null && !pd.isShowing()) {
            pd.show();
        }
        new Thread() {

            @Override
            public void run() {
                super.run();

                navTool.push(new AccountNode("", NavigationConstants.CATEGORYHOME));
                blockSizeMap = FileUtil.getAvailableStore(NavigationConstants.SDCARD_PATH);
				MusicInfo.init(context);
                
//                if (scanReceiver == null) {
//                    StatusCheckUtil.broadcastRec = new MediaScannerBroadcast();
//                    StatusCheckUtil.registerSDcardIntentListener(CountActivity.categoryActivity, StatusCheckUtil.broadcastRec);
//                    scanReceiver = new ScanReceiver(categoryActivity);
//                    IntentFilter filter = new IntentFilter();
//                    filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
//                    filter.addAction(ScanReceiver.ACTION_RECEIVER_SCAN);
//                    filter.addAction(ScanReceiver.ACTION_SCANNING);
//                    registerReceiver(scanReceiver, filter);
//
//                    apkNotifyBroadcast = new ApkNotifyBroadcast();
//                    IntentFilter filterCancelApkNotify = new IntentFilter();
//                    filterCancelApkNotify.addAction(ApkNotifyBroadcast.ACTION_APK_NOTIFY_CANCEL);
//                    registerReceiver(apkNotifyBroadcast, filterCancelApkNotify);
//
//                    packageReceiver = new PackageReceiver(categoryActivity);
//                    IntentFilter pfilter = new IntentFilter();
//                    pfilter.addAction(Intent.ACTION_PACKAGE_ADDED);
//                    pfilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
//                    pfilter.addAction(Constants.GoToInvokeLWT.ACTION_DELETE_LWT);
//                    pfilter.addDataScheme("package");
//                    registerReceiver(packageReceiver, pfilter);
//                    lwtReceiver = new LWTReceiver(categoryActivity);
//                    IntentFilter lwtfilter = new IntentFilter();
//                    lwtfilter.addAction(Constants.GoToInvokeLWT.ACTION_DELETE_LWT);
//                    registerReceiver(lwtReceiver, lwtfilter);
                Float image_filter_size = Config.image_filter_size = (Float) SharedPreferenceUtil.getValue(context, Constants.SharedPrefernce.SP_IMAGE_FILTER_SIZE, Constants.SharedPrefernce.KEY_IMAGE_FILTER_SIZE, Float.class);
                if (image_filter_size == null || image_filter_size == -1) {
                    SharedPreferenceUtil.putValue(context, Constants.SharedPrefernce.SP_IMAGE_FILTER_SIZE, Constants.SharedPrefernce.KEY_IMAGE_FILTER_SIZE, 0F);
                    Config.image_filter_size = 0F;
                }

//                }
                finished = true;
            }
        }.start();
        Switcher.viewFlowTo(homeViewHolder, contentView, this);

		if (categoryVisitFlag) {
			String rememberedCategory = (String) SharedPreferenceUtil.getValue(
					context, Constants.SharedPrefernce.RememberedCategory,
					Constants.SharedPrefernce.KEY_CATEGORY, String.class);

			if (rememberedCategory != null
					&& !rememberedCategory.trim().equals("")) {
				String value_lwt_deleted = (String) SharedPreferenceUtil
						.getValue(context,
								Constants.SharedPrefernce.RememberedCategory,
								Constants.SharedPrefernce.KEY_LWT_ISDELETED,
								String.class);
				if (value_lwt_deleted != null
						&& !value_lwt_deleted.trim().equals("")) {
					File deletedLWT = new File(value_lwt_deleted);
					if (!deletedLWT.exists()) {
						DatasrcDelete.recursiveUpdateDel(new FileInfo(
								deletedLWT, context), context);
					}
				}
				int r = 0;
				if (rememberedCategory.equals("apk")) {
					r = R.string.app;
				} else if (rememberedCategory.equals("lewa/theme")) {
					r = R.string.theme;
				}
				this.navTool.navEntity.push(new AccountNode(rememberedCategory,
						getString(r)));
				this.revisit();
				new HandlerThread("",
						android.os.Process.THREAD_PRIORITY_BACKGROUND) {

					@Override
					public void run() {
						SharedPreferenceUtil.putValue(context,
								Constants.SharedPrefernce.RememberedCategory,
								Constants.SharedPrefernce.KEY_CATEGORY, "");
					}
				}.start();
			}
		}
	}

    @Override
    protected String getEmptyText() {
        return this.getString(R.string.nofile, this.getString(R.string.position_category));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (this.navTool.isAtTop()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    protected int addEmptyCategory(int nameRes, int DrawableRes,
            List<CategoryInfo> category) {
//		if (sp == null) {
//			sp = this.getPreferences(countSharedPrefernce);
//		}
//		int count = sp.getInt(this.getString(nameRes), 0);
		int count = 0;
        CategoryItem categoryItem = MimeSrc.getEmptyCategory(this,
                this.getString(nameRes),
                this.getResources().getDrawable(DrawableRes));
        categoryItem.categoryInfo.count = "" + count;
        category.add(categoryItem.categoryInfo);
        return count;
    }

    @Override
    public void access(NavigationNode navigation, int newPos,
            boolean notifyDataSetChanged, int mode, int status, int accessFlag) {
        access(navigation, newPos, notifyDataSetChanged, mode, status);
    }

    @Override
    public void access(NavigationNode navigation, int newPos,
            boolean notifyDataSetChanged, int mode, int status) {
    }

    @Override
    public void handlerRead(Object previousSource, boolean startRoot) {
    }

    @Override
    protected void setNavigationPosAndNode(int newPos, int mode, NavigationNode navSource) {
        if (mode == MODE_NEW) {
            if (this.navTool.navEntity.size() != 0) {
                listview.setSelection(newPos);
            }
        }
    }

    @Override
    protected boolean backward() {
        if (!this.navTool.isAtTop()) {
            showBottomBar(View.GONE);
            Switcher.viewFlowTo(this.homeViewHolder, contentView, this);
            this.navTool.navEntity.pop();

            return true;
        }
        return false;
    }

    public void switchApkBar(boolean switchon) {
        if (switchon) {
            this.bottomToolbar.findViewById(R.id.toolbar_cancel).setVisibility(View.GONE);
            this.bottomToolbar.findViewById(R.id.toolbar_cancel_v_line).setVisibility(View.GONE);
            this.bottomToolbar.findViewById(R.id.operate).setVisibility(View.VISIBLE);
            this.bottomToolbar.findViewById(R.id.apkpre_v_line).setVisibility(View.VISIBLE);
        } else {
            this.bottomToolbar.findViewById(R.id.toolbar_cancel).setVisibility(View.VISIBLE);
            this.bottomToolbar.findViewById(R.id.toolbar_cancel_v_line).setVisibility(View.VISIBLE);
            this.bottomToolbar.findViewById(R.id.operate).setVisibility(View.GONE);
            this.bottomToolbar.findViewById(R.id.apkpre_v_line).setVisibility(View.GONE);
        }

    }

    @Override
    public void showBottomBar(int visibility) {
        if (this.navTool.getCurrNavEntitySource().toString().toLowerCase().equals(Constants.CateContants.CATE_PACKAGE)) {
            boolean switchOptn = false;
            if (visibility == View.VISIBLE) {
                switchOptn = true;
            } else if (visibility == View.GONE) {
                switchOptn = false;
            }
            switchApkBar(switchOptn);
        } else {
            switchApkBar(false);
        }
        super.showBottomBar(visibility);
    }

    @Override
    protected void showApkViewContextMenux(ContextMenu menu, ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.operate);
        menu.add(0, MENU_INSTALL, 0, R.string.batch_install).setCheckable(false).setChecked(false).setOnMenuItemClickListener(new OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem arg0) {
                if (RuntimeArg.isInInstall) {
                    Toast.makeText(context, R.string.prohibetRepeatingExectuing, Toast.LENGTH_LONG).show();
                    showBottomBar(View.GONE);
                    return true;
                }
                showDialog(DIALOG_PREFER_INSTALL);
                return false;
            }
        });
        menu.add(0, MENU_MORE_SEND, 0, R.string.share).setCheckable(false).setChecked(false).setOnMenuItemClickListener(new OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem arg0) {
                send();
                return false;
            }
        });

    }

    @Override
    public String getNavBarWholeShowText() {
        String text = "";
        NavigationNode node;
        for (int i = 0; i < this.navTool.navEntity.size(); i++) {
            if ((node = this.navTool.navEntity.get(i)) == null) {
                continue;
            }
            text += " / " + node.displayname;
        }
        text = text.substring(3);
        return text;
    }

    @Override
    protected boolean navigate(MenuItem item) {
        // TODO Auto-generated method stub
        Logs.i("", "---" + item.getTitle());
        int theId = item.getItemId() - MENU_PATH_OFFSET;
        if (theId < this.navTool.navEntity.size()) {
            if (theId == 0) {
                this.backward();
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if (this.pd != null) {
            this.pd.dismiss();
        }
        pd = null;

        super.onDestroy();
    }

    public void refresh(boolean callScanner) {
        if (!callScanner) {
            refresh();
            return;
        }
        //scan flagss
		if (Config.isLewaRom
				|| this.navTool.navEntity.peek().producingSource.toString()
						.toUpperCase().equals("AUDIO")
				|| this.navTool.navEntity.peek().producingSource.toString()
						.toUpperCase().contains("VIDEO")
				|| this.navTool.navEntity.peek().producingSource.toString()
						.toUpperCase().contains("IMAGE")) {
			mediaScannerRefresh();
            return;
        }
        Intent intent = new Intent(this, FileScanService.class);
        intent.putExtra(ScanReceiver.SCAN_TYPE,
                this.navTool.navEntity.peek().producingSource.toString());
        this.startService(intent);
	}
	private void mediaScannerRefresh() {
		if (StatusCheckUtil.MediaScannerBroadcast.scanCat != null) {
			Toast.makeText(
					context,
					"",
					Toast.LENGTH_SHORT).show();
			return;
		}
		CountActivity.categoryActivity.setScanBarHide(false);
		this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
				.parse("file://" + Environment.getExternalStorageDirectory())));
		StatusCheckUtil.mediaScanning = true;
		if (CountActivity.categoryActivity.navTool.navEntity.size() != 0) {
			StatusCheckUtil.MediaScannerBroadcast.scanCat = CountActivity.categoryActivity.navTool.navEntity
					.peek().displayname;
			Toast.makeText(
					context,
					context.getString(R.string.scaning_toast,
							StatusCheckUtil.MediaScannerBroadcast.scanCat),
					Toast.LENGTH_LONG).show();
		}
    }

    @Override
    public void refresh() {
        if (StatusCheckUtil.srcardStateResolve(true, this)) {
            return;
        }
        if (this.navTool.isAtTop()) {
            Switcher.viewFlowTo(homeViewHolder, contentView, this);
            return;
        }
        getViewHolder().refresh();
        reFreshCheckboxVisible();
    }

    public void recovery() {

        if (this.navTool.navEntity.isEmpty() || this.navTool.isAtTop()) {
            this.startup();
            Logs.i("============homeViewHolder recovery");
            return;
        }
        this.revisit();
        reFreshCheckboxVisible();
    }

    @Override
    protected void prepareMusicInfo(int visible, FileInfo info) {
        super.prepareMusicInfo(visible, info);
        if (this.navTool.navEntity.peek().producingSource.toString().toUpperCase().equals("AUDIO")) {
            MusicInfo mi = (MusicInfo) info;
            value_artist.setText(mi.artist);
            value_album.setText(mi.album.replace("-", "").trim());
        }
    }

    @Override
    public void sortRefresh() {
        refresh();
    }

    @Override
    public String prepareMime() {
        String mimePrefix = this.navTool.navEntity.peek().producingSource.toString();
        if (mimePrefix.equals(Constants.CateContants.CATE_DOCS) || mimePrefix.equals(Constants.CateContants.CATE_PACKAGE) || mimePrefix.equals(Constants.CateContants.CATE_MUSIC)) {
            return "*/*";
        } else {
            return mimePrefix + "/*";
        }
    }

    @Override
    public int[] getMenuIds() {
        int[] result = null;
        if (!this.navTool.isAtTop()) {
            result = new int[]{R.id.menu_cateogry_sort_name, R.id.menu_cateogry_sort_time, R.id.menu_cateogry_sort_size, R.id.menu_cateogry_sort_type};
        }
        return result;
    }
//install

	public class HomeViewHolder extends ViewHolder {

        public HomeViewHolder(View view, CommonActivity activity) {
            super(view, activity);
        }

        @Override
        public void start() {
            view = LayoutInflater.from(context).inflate(R.layout.category_2_0, null);
            contentView = view;
            activity.setContentView(view);
            init();
        }

        public void dataChanged() {
            while (!finished) {
            }
            if (MimeSrc.categoryRepository == null || MimeSrc.categoryRepository.isEmpty()) {
                initCount();
                initDBData(null);
            }
            MimeSrc.recountCategoryNum(activity);
        }

        public void rebind() {
            notifyCountData();
            uiStatusUpdate();
        }

        private void init() {
            viewAudio = findViewById(R.id.audio);
            ((ImageView) (viewAudio.findViewById(R.id.categoryImage))).setImageResource(R.drawable.music);
            ((TextView) (viewAudio.findViewById(R.id.categoryTypeName))).setText(R.string.music);
            ((TextView) (viewAudio.findViewById(R.id.categoryTotalNum))).setText(FileUtil.addParentheses("0"));
            ((TextView) (viewAudio.findViewById(R.id.categoryTotalSize))).setText("0 B");
            viewAudio.findViewById(R.id.itemLinearLayout).setOnClickListener(categoryClickListener);
            viewImage = findViewById(R.id.image);
            ((ImageView) (viewImage.findViewById(R.id.categoryImage))).setImageResource(R.drawable.picture);
            ((TextView) (viewImage.findViewById(R.id.categoryTypeName))).setText(R.string.image);
            ((TextView) (viewImage.findViewById(R.id.categoryTotalNum))).setText(FileUtil.addParentheses("0"));
            ((TextView) (viewImage.findViewById(R.id.categoryTotalSize))).setText("0 B");
            viewImage.findViewById(R.id.itemLinearLayout).setOnClickListener(categoryClickListener);
            viewVideo = findViewById(R.id.video);
            ((ImageView) (viewVideo.findViewById(R.id.categoryImage))).setImageResource(R.drawable.vedio);
            ((TextView) (viewVideo.findViewById(R.id.categoryTypeName))).setText(R.string.video);
            ((TextView) (viewVideo.findViewById(R.id.categoryTotalNum))).setText(FileUtil.addParentheses("0"));
            ((TextView) (viewVideo.findViewById(R.id.categoryTotalSize))).setText("0 B");
            viewVideo.findViewById(R.id.itemLinearLayout).setOnClickListener(categoryClickListener);
            viewDoc = findViewById(R.id.doc);
            ((ImageView) (viewDoc.findViewById(R.id.categoryImage))).setImageResource(R.drawable.doc);
            ((TextView) (viewDoc.findViewById(R.id.categoryTypeName))).setText(R.string.doc);
            ((TextView) (viewDoc.findViewById(R.id.categoryTotalNum))).setText(FileUtil.addParentheses("0"));
            ((TextView) (viewDoc.findViewById(R.id.categoryTotalSize))).setText("0 B");
            viewDoc.findViewById(R.id.itemLinearLayout).setOnClickListener(categoryClickListener);
            viewApk = findViewById(R.id.apk);
            ((ImageView) (viewApk.findViewById(R.id.categoryImage))).setImageResource(R.drawable.apk);
            ((TextView) (viewApk.findViewById(R.id.categoryTypeName))).setText(R.string.app);
            ((TextView) (viewApk.findViewById(R.id.categoryTotalNum))).setText(FileUtil.addParentheses("0"));
            ((TextView) (viewApk.findViewById(R.id.categoryTotalSize))).setText("0 B");
            viewApk.findViewById(R.id.itemLinearLayout).setOnClickListener(categoryClickListener);
            if (Config.isLewaRom) {
                viewTheme = findViewById(R.id.theme);
                ((ImageView) (viewTheme.findViewById(R.id.categoryImage))).setImageResource(R.drawable.theme);
                ((TextView) (viewTheme.findViewById(R.id.categoryTypeName))).setText(R.string.theme);
                ((TextView) (viewTheme.findViewById(R.id.categoryTotalNum))).setText(FileUtil.addParentheses("0"));
                ((TextView) (viewTheme.findViewById(R.id.categoryTotalSize))).setText("0 B");
                viewTheme.findViewById(R.id.itemLinearLayout).setOnClickListener(categoryClickListener);
            }
        }

        private void notifyCountData() {

            try {
            bind_2_0(findViewById(R.id.audio), MimeSrc.categoryRepository.get(Constants.CateContants.CATE_MUSIC).categoryInfo);
            bind_2_0(findViewById(R.id.image), MimeSrc.categoryRepository.get(Constants.CateContants.CATE_IMAGES).categoryInfo);
            bind_2_0(findViewById(R.id.video), MimeSrc.categoryRepository.get(Constants.CateContants.CATE_VIDEO).categoryInfo);
            bind_2_0(findViewById(R.id.doc), MimeSrc.categoryRepository.get(Constants.CateContants.CATE_DOCS).categoryInfo);
            bind_2_0(findViewById(R.id.apk), MimeSrc.categoryRepository.get(Constants.CateContants.CATE_PACKAGE).categoryInfo);
            if (Config.isLewaRom) {
                bind_2_0(findViewById(R.id.theme), MimeSrc.categoryRepository.get(Constants.CateContants.CATE_THEME).categoryInfo);
            }
                setSdcard();
                setMemory();
                setSDProgress();
                setMemProgress();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void bind_2_0(View view, CategoryInfo cateInfo) {
            Logs.i("----cateInfo" + cateInfo);
            ((TextView) view.findViewById(R.id.categoryTotalNum)).setText(FileUtil.addParentheses(cateInfo.count));
            ((TextView) view.findViewById(R.id.categoryTotalSize)).setText(cateInfo.size);
        }

        private void setSdcard() throws Exception {
            HashMap<String, Object> blockSizeMap = FileUtil.getAvailableStore(NavigationConstants.SDCARD_PATH);
            String availSize = blockSizeMap.get("availaBlocks").toString();
            String totalSize = blockSizeMap.get("totalBlocks").toString();
            ((TextView) findViewById(R.id.sdcard_name_cnst)).setText(getString(R.string.sdcardhome) + ":");
            ((TextView) findViewById(R.id.sdcard_capability)).setText(totalSize);
            ((TextView) findViewById(R.id.sdcard_avai_cnst)).setText(getString(R.string.available) + ":");
            ((TextView) findViewById(R.id.sdcard_avai)).setText(availSize);
        }

        private void setMemory() throws Exception {
            ((TextView) findViewById(R.id.memory_name_cnst)).setText(getString(R.string.memory) + ":");
            ((TextView) findViewById(R.id.memory_capability)).setText(ReadSystemMemory.getTotalMemoryText());
            ((TextView) findViewById(R.id.memory_avai_cnst)).setText(getString(R.string.available) + ":");
            ((TextView) findViewById(R.id.memory_avai)).setText(ReadSystemMemory.getAvailableInternalMemorySizeText());
        }

        private void setSDProgress() throws Exception {
            View cntr = findViewById(R.id.sdcard);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) cntr.getLayoutParams();
            lp.width = ((Double) (DensityUtil.getScreenWidth(activity) * 0.95)).intValue();
            cntr.setLayoutParams(lp);

            long cntrWidth = cntr.getLayoutParams().width;
            if (blockSizeMap == null || blockSizeMap.isEmpty()) {
                blockSizeMap = FileUtil.getAvailableStore(NavigationConstants.SDCARD_PATH);
            }
            Float totalSize = ((Long) blockSizeMap.get("totalSize")).floatValue();
            Float musicProgress = (MimeSrc.getCategoryRepository().get(Constants.CateContants.CATE_MUSIC).categoryInfo.length / totalSize);
            Float imageProgress = MimeSrc.getCategoryRepository().get(Constants.CateContants.CATE_IMAGES).categoryInfo.length / totalSize;
            Float videoProgress = MimeSrc.getCategoryRepository().get(Constants.CateContants.CATE_VIDEO).categoryInfo.length / totalSize;
            Float docProgress = MimeSrc.getCategoryRepository().get(Constants.CateContants.CATE_DOCS).categoryInfo.length / totalSize;
            Float apkProgress = MimeSrc.getCategoryRepository().get(Constants.CateContants.CATE_PACKAGE).categoryInfo.length / totalSize;
            Float themeProgress;
            if (Config.isLewaRom) {
                themeProgress = MimeSrc.getCategoryRepository().get(Constants.CateContants.CATE_THEME).categoryInfo.length / totalSize;
            } else {
                themeProgress = 0f;
            }
            Float otherProgress = (1 - (ReadSystemMemory.getAvailableExternalMemorySize() / totalSize) - musicProgress - imageProgress - videoProgress - docProgress - apkProgress);

            LayoutParams musicLayoutParams = findViewById(R.id.music_progress).getLayoutParams();
            musicLayoutParams.width = ((Float) (musicProgress * cntrWidth)).intValue();
            LayoutParams imageLayoutParams = findViewById(R.id.picture_progress).getLayoutParams();
            imageLayoutParams.width = ((Float) (imageProgress * cntrWidth)).intValue();

            LayoutParams videoLayoutParams = findViewById(R.id.video_progress).getLayoutParams();
            videoLayoutParams.width = ((Float) (videoProgress * cntrWidth)).intValue();

            LayoutParams docLayoutParams = findViewById(R.id.doc_progress).getLayoutParams();
            docLayoutParams.width = ((Float) (docProgress * cntrWidth)).intValue();

            LayoutParams apkLayoutParams = findViewById(R.id.apk_progress).getLayoutParams();
            apkLayoutParams.width = ((Float) (apkProgress * cntrWidth)).intValue();

            LayoutParams themeLayoutParams = findViewById(R.id.theme_progress).getLayoutParams();
            themeLayoutParams.width = ((Float) (themeProgress * cntrWidth)).intValue();

            LayoutParams otherLayoutParams = findViewById(R.id.other_progress).getLayoutParams();
            otherLayoutParams.width = ((Float) (otherProgress * cntrWidth)).intValue();

            findViewById(R.id.music_progress).setLayoutParams(musicLayoutParams);
            findViewById(R.id.picture_progress).setLayoutParams(imageLayoutParams);
            findViewById(R.id.video_progress).setLayoutParams(videoLayoutParams);
            findViewById(R.id.doc_progress).setLayoutParams(docLayoutParams);
            findViewById(R.id.apk_progress).setLayoutParams(apkLayoutParams);
            findViewById(R.id.theme_progress).setLayoutParams(themeLayoutParams);
            findViewById(R.id.other_progress).setLayoutParams(otherLayoutParams);

        }

        private void setMemProgress() throws Exception {
            View cntr = findViewById(R.id.memory);
            LayoutParams lp = cntr.getLayoutParams();
            lp.width = ((Double) (DensityUtil.getScreenWidth(activity) * 0.95)).intValue();
            cntr.setLayoutParams(lp);

            long cntrWidth = cntr.getLayoutParams().width;
            Float memProgress = 1 - ReadSystemMemory.getAvailableInternalMemorySize().floatValue() / ReadSystemMemory.getTotalInternalMemorySize().floatValue();
            LayoutParams memLayoutParams = findViewById(R.id.used).getLayoutParams();
            memLayoutParams.width = ((Float) (memProgress * cntrWidth)).intValue();
            findViewById(R.id.used).setLayoutParams(memLayoutParams);
        }

        private void uiStatusUpdate() {
            int visibility = -1;
            if (scanBarToHideFlag) {
                visibility = View.GONE;
            } else {
                visibility = View.VISIBLE;
            }
            hideScanBar(visibility);
        }

        @Override
        public void refresh() {
        }
    }
}
