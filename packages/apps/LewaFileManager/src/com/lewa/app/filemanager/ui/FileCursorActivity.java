package com.lewa.app.filemanager.ui;

import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.view.View;

import android.view.View.OnClickListener;
import android.widget.Toast;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.ds.database.MediaArgs;
import com.lewa.filemanager.actions.OperationUtil;
import com.lewa.filemanager.ds.database.MimeSrc;
import com.lewa.filemanager.ds.database.CategoryInfo;
import com.lewa.filemanager.actions.sort.DBSort;
import com.lewa.filemanager.actions.EqualCheck;
import com.lewa.filemanager.actions.sort.ObjectSort;
import com.lewa.filemanager.actions.sort.Sort;
import com.lewa.filemanager.cpnt.adapter.FileItemSrc;
import com.lewa.base.adapter.MapAdapter.AdaptInfo;
import com.lewa.filemanager.activities.views.FileListViewHolder;
import com.lewa.filemanager.activities.views.Switcher;
import com.lewa.filemanager.activities.views.ViewHolder;
import com.lewa.filemanager.util.ActivityPool;
import com.lewa.filemanager.config.Constants;

import com.lewa.base.SharedPreferenceUtil;
import com.lewa.filemanager.ds.uri.AccountNode;
import com.lewa.filemanager.ds.uri.NaviSourceIndicator;
import com.lewa.filemanager.ds.uri.NavigationNode;
import java.util.Collections;
import java.util.HashMap;

public abstract class FileCursorActivity extends CommonActivity implements NaviSourceIndicator {

    public ViewHolder getViewHolder() {
        if (viewHolder == null) {
            setViewHolder(newViewHolderInstance());
        }
        return viewHolder;
    }

    public ViewHolder newViewHolderInstance() {
        return new FileListViewHolder(list, this);
    }

    public void setViewHolder(ViewHolder viewHolder) {
        this.viewHolder = viewHolder;
    }

    public void revisit() {
        Switcher.viewFlowTo(getViewHolder(), contentView, this);
    }
    public HashMap<String, Class> datamap;

    public NavigationNode getNaviSource() {
        return navTool.navEntity.peek();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getViewHolder();
        if (policy == null) {
            policy = new DBSort(Sort.SORT_POLICY_TITLE, Sort.SORT_SEQ_ASC);
        }

    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        initData();
    }

    public boolean performCountActivityCopy(int command) throws NotFoundException {
        // TODO Auto-generated method stub
        EqualCheck nc = new EqualCheck(ObjectSort.SORT_POLICY_TITLE, ObjectSort.SORT_SEQ_ASC);
        Collections.sort(OperationUtil.selectedEntities, nc);
        if (nc.isResult()) {
            Toast.makeText(context, R.string.same_name_files_operation_cancel, Toast.LENGTH_LONG).show();
            return true;
        }
        this.adapter.markVisible(false);
        SlideActivity.paramActivity.isInOperation = command;
        showBottomBar(View.GONE);
        PathActivity.activityInstance.switchToCutCopy();
        SlideActivity.fileActivityInstance.setDisplayScreen(1);
        ActivityPool.getInstance().refresh();
        return false;
    }

    private void initData() {
        if (datamap == null) {
        datamap = new HashMap<String, Class>();
        datamap.put(MediaArgs.PATH, String.class);
        datamap.put(MediaArgs.TITLE, String.class);
        datamap.put(MediaArgs.COUNT, Integer.class);
        datamap.put(MediaArgs.SIZE, Integer.class);
        }

    }
    public ViewHolder viewHolder;
    public AdaptInfo apkAdaptInfo;
    public AdaptInfo musicAdaptInfo;
    public static final int SortMenuInAPK = 0;
    public static final int SortMenuInMusic = 1;
    public int sortMenuMarkInit = -1;

    public AdaptInfo getApkAdaptInfo() {
        if (apkAdaptInfo == null) {
            apkAdaptInfo = new AdaptInfo();
            apkAdaptInfo.objectFields = new String[]{
                Constants.ApkInfo.VERSION_CONDITION, //version_cond
                Constants.FieldConstants.SIZE, //size
                Constants.ApkInfo.VERSION_NAME, //versionName
                Constants.FieldConstants.NAME,
                Constants.FieldConstants.ICON_RES,
                Constants.FieldConstants.CHECKBOX_OPTION
            };
            apkAdaptInfo.listviewItemData = new FileItemSrc();
            apkAdaptInfo.viewIds = new int[]{
                R.id.versioncondition, //version_cond
                R.id.apkFileSize, //versionCode
                R.id.fileTimeSize, //size
                R.id.fileNameSubFileNum,
                R.id.fileIcon,
                R.id.handleCheckbox};
            apkAdaptInfo.listviewItemLayoutId = R.layout.filelist_item;
            apkAdaptInfo.actionListeners = viewHandlers;
        }
        return apkAdaptInfo;
    }

    public AdaptInfo getMusicAdaptInfo() {
        if (musicAdaptInfo == null) {
            musicAdaptInfo = new AdaptInfo();
            musicAdaptInfo.objectFields = new String[]{
                Constants.MusicInfo.NAME, //version_cond
                //                Constants.MusicInfo.ALBUM, //size
                Constants.MusicInfo.ALBUMARTIST, //versionName
                Constants.FieldConstants.ICON_RES,
                Constants.FieldConstants.CHECKBOX_OPTION
            };
            musicAdaptInfo.viewIds = new int[]{
                R.id.fileNameSubFileNum,
                //                R.id.album,
                R.id.artist,
                R.id.fileIcon,
                R.id.handleCheckbox
            };
            musicAdaptInfo.listviewItemLayoutId = R.layout.musiclistitem;
            musicAdaptInfo.actionListeners = viewHandlers;
        }
        return musicAdaptInfo;
    }
    protected OnClickListener categoryClickListener = new OnClickListener() {

        public void onClick(View view) {
            String mimeprefix = null;
            switch (((View) view.getParent()).getId()) {
                case R.id.audio:
                    mimeprefix = Constants.CateContants.CATE_MUSIC;
                    break;
                case R.id.image:
                    mimeprefix = Constants.CateContants.CATE_IMAGES;
                    break;
                case R.id.video:
                    mimeprefix = Constants.CateContants.CATE_VIDEO;
                    break;
                case R.id.doc:
                    mimeprefix = Constants.CateContants.CATE_DOCS;
                    break;
                case R.id.apk:
                    mimeprefix = Constants.CateContants.CATE_PACKAGE;
                    break;
                case R.id.theme:
                    mimeprefix = Constants.CateContants.CATE_THEME;
                    break;
            }
            CategoryInfo cateInfo = MimeSrc.categoryRepository.get(mimeprefix).categoryInfo;
            String flag = cateInfo.categorySign;
            String displayname = cateInfo.displayName;
            navTool.navEntity.push(new AccountNode(flag, displayname));
            revisit();
        }
    };
    @Override
	protected void setSort() {
		// TODO Auto-generated method stub
		super.setSort();
        if (policy == null) {
            policy = new DBSort(Sort.SORT_POLICY_TITLE, Sort.SORT_SEQ_ASC);
        }
	}
    protected static boolean isFirstLaunched = true;

    public void recordPageSharedPreference(String rememberedCategory) {
        SharedPreferenceUtil.putValue(context, Constants.SharedPrefernce.RememberedCategory, Constants.SharedPrefernce.KEY_CATEGORY, rememberedCategory);
    }
}
