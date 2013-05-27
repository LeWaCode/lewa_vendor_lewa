package com.lewa.app.filemanager.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import java.io.File;
import java.util.List;

import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.actions.OperationUtil;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.ds.uri.NavigationNode;
import com.lewa.filemanager.actions.sort.ObjectSort;
import com.lewa.filemanager.config.Config;
import com.lewa.filemanager.beans.FileInfoUtil;
import com.lewa.filemanager.ds.uri.NavigationConstants;
import com.lewa.filemanager.ds.uri.SDCardNode;
import com.lewa.filemanager.ds.sdcard.HiddenFilter;
import com.lewa.base.adapter.MapAdapter;
import com.lewa.base.adapter.ItemDataSrc;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.Logs;
import com.lewa.filemanager.activities.views.ViewUtil;
import com.lewa.filemanager.cpnt.adapter.ThumbnailAdapter;
import com.lewa.filemanager.util.StatusCheckUtil;
import java.util.ArrayList;
import java.util.Collections;

public class PathActivity extends CommonActivity {

    public static PathActivity activityInstance;
    public static boolean codePerformed = false;
    protected static final int SWIPE_LAZY = 0;
    public String extRequired = null;

    public void refreshSDcardReadView(int status, NavigationNode navigation, int newPos, int mode) {
        browseCreateView(getViewHandlers(), getOnClickListener(), true);
        adapter.clearDataSrc();
        ((MapAdapter) adapter).notifyDataSetChanged();
        ItemDataSrc itemDataSrc = new ItemDataSrc(fileInfos);
        adapter.reinitSelectedAllBck(itemDataSrc.getCount());
        ((MapAdapter) adapter).setItemDataSrc(itemDataSrc);
        ((MapAdapter) adapter).notifyDataSetChanged();
        new Thread(new Runnable() {

            FileInfo fileInfo;
            HiddenFilter hiddenFilter = new HiddenFilter(Config.SDCARD_HIDE_OPTION);

            public void run() {
                for (int i = 0; i < adapter.getCount(); i++) {
                    fileInfo = (FileInfo) adapter.getItem(i);
                    if (fileInfo.getIsDir()) {
                        if (extRequired != null) {
                            fileInfo.countStr = "";
                            continue;
                        }
                        fileInfo.getFile().list(hiddenFilter);
                        fileInfo.setCount(hiddenFilter.getCount());
                        fileInfo.buildCountStr();
                        hiddenFilter.clearCount();
                        handler.sendEmptyMessage(Constants.OperationContants.DIR_REFRESH);
                    } else {
                        break;
                    }
                }

            }
        }).start();
        setupAfterUILoad(status, navigation, newPos, accessFlag, filesInfo);
        navigationBarSetup(newPos, mode, navigation);
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		activityInstance = this;
		// this.setContentView(R.layout.progress_waiting);
    }

    @Override
    protected boolean backward() {
        boolean result = super.backward();
        if (result) {
            if (this.action_flag != null) {
                Button button = (Button) this.findViewById(R.id.sure);
                if (button.getVisibility() == View.VISIBLE) {
                    button.setVisibility(View.GONE);
                }
            }
        }
        return result;
    }

    @Override
    protected void onStart() {


        super.onStart();
        if (Constants.InvokedPath.ACTION_INVOKED_PATH.equals(this.getIntent().getAction())) {
            extRequired = this.getIntent().getStringExtra(Constants.InvokedPath.ACTION_INVOKED_PATH_EXT);
            final String callbackAction = getIntent().getStringExtra(Constants.InvokedPath.CALLBACK_ACTION);
            if (extRequired != null) {
                listItemClickListener = new ListItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        FileInfo fileInfo = ((FileInfo) (parent.getItemAtPosition(position)));
                        if (((ThumbnailAdapter) parent.getAdapter()).hasSelected() && OperationUtil.mOperationState != OperationUtil.STATUS_CUT_COPY) {
                            if (!fileInfo.isDir) {
                                CheckBox checkBox = (CheckBox) ViewUtil.findViewById(view,
                                        R.id.handleCheckbox);
                                checkBox.performClick();
                            }
                            return;
                        }
                        enterDir(fileInfo);
                    }
                };
            }
            }
		handler.postDelayed(new Runnable() {

			public void run() {
				handler.sendEmptyMessage(Constants.OperationContants.LOAD_PATHACTIVITY);
			}
		},20);
		this.navTool.navEntity.clear();
		this.navTool.navEntity.push(new SDCardNode(new FileInfo(new File(
				NavigationConstants.SDCARD_PATH), this),
				NavigationConstants.SDCARD));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (OperationUtil.mOperationState == OperationUtil.STATUS_CUT_COPY) {
            return false;
        }
        if (StatusCheckUtil.srcardStateResolve(true, this)) {
            return false;
        }
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_CLEAN, 0, getString(R.string.menu_onekeyclean));
        menu.add(0, MENU_SHOW_HIDDEN, 0, getString(R.string.show_hidden_file));
        menu.findItem(MENU_CLEAN).setIcon(R.drawable.clean);
        menu.findItem(MENU_SHOW_HIDDEN).setIcon(R.drawable.showhidden);
        Menu m = menu.findItem(R.id.menu_sort).getSubMenu();
        hideMusicMenu(m, false);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem mi = menu.findItem(MENU_SHOW_HIDDEN);
        mi.setTitle(Config.SDCARD_HIDE_OPTION ? getString(R.string.show_hidden_file) : getString(R.string.dont) + getString(R.string.show_hidden_file));
        return true;
    }

    public void firstTimeStartup() {
        firstTimeStartup(ACCESS_ORIGINAL, false, 350);
    }

    public void firstTimeStartup(final int readWay, boolean isInPaste, final int waittime) {
        if (StatusCheckUtil.srcardStateResolve(true, this)) {
            return;
        }
        if (!PathActivity.codePerformed) {
            PathActivity.codePerformed = true;
            if (!isInPaste) {
            contentView = LayoutInflater.from(this).inflate(R.layout.progress_waiting, null);
            this.setContentView(this.contentView);
                turnToFileBrowsing(viewHandlers, listItemClickListener,
                        new SDCardNode(new FileInfo(new File(NavigationConstants.SDCARD_PATH), context)), (Integer) (ACCESS_ORIGINAL));

            } else {
                NavigationNode node = ((SDCardNode) navTool.navEntity.peek());

                        try {
                    access(node, node.defaultPosition, true, MODE_CURR, operationUtil.mOperationState, (Integer) (ACCESS_ORIGINAL));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }
    public ReadRuntime rrt = new ReadRuntime();


    public static class ReadRuntime {

        public int readMode = -1;
        public int status = -1;
        public int listviewPos = -1;
        public NavigationNode navNode;
    }

    public void startup() {
        if (PathActivity.codePerformed) {
            this.turnToFileBrowsing(getViewHandlers(), getOnClickListener(),
                    new SDCardNode(new FileInfo(new File(getSDcardPath()), this)));
        }
    }
    boolean refereshFlag;

    protected void access(NavigationNode navigation, int newPos,
            boolean notifyDataSetChanged, int mode, int status) {
        access(navigation, newPos,
                notifyDataSetChanged, mode, status, -1);
    }

    protected void access(final NavigationNode arguNavigation, final int arguNewPos,
            boolean notifyDataSetChanged, final int arguMode, final int arguStatus, int forceAccessMode) {

//        new Thread() {
//
//            @Override
//            public void run() {
//                super.run();
                try {
                filesInfo = setupPrevUILoad();

                accessFlag = arguMode;
                newPos = arguNewPos;
                navigation = arguNavigation;
                status = arguStatus;
                if (fileInfos != null) {
//                    ThumbnailLoader.destroyImages(fileInfos);
                    fileInfos = null;
                }
                fileInfos = new ArrayList<FileInfo>(0);
                File[] files = ((FileInfo) navigation.producingSource).getFile().listFiles(new HiddenFilter(Config.SDCARD_HIDE_OPTION));
                FileInfoUtil.getFileInfos(fileInfos, getReadInfoClass(), files, context, extRequired);
                    if (!fileInfos.isEmpty() && policy != null) {
                        try {
                            Collections.sort(fileInfos, (ObjectSort) policy);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
//                    Message msg = new Message();
//                    msg.what = Constants.OperationContants.LOAD_PATHACTIVITY_VIEW;
//                    msg.obj = rrt;
//                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        refreshSDcardReadView(status, arguNavigation, arguNewPos, arguMode);

//            }
//        }.start();
    }

    public Class<? extends FileInfo> getReadInfoClass() {
        return FileInfo.class;
    }
    List<FileInfo> fileInfos = null;

    @Override
    public void sortRefresh() {
        if (adapter == null) {
            return;
        }
        List<FileInfo> fileInfos = (List<FileInfo>) (this.adapter.getItemDataSrc().getContent());
        Collections.sort(fileInfos, (ObjectSort) policy);
        Logs.i("----------policy " + policy.getPolicy());
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void setHiddenFileShowFlag() {
        this.isHiddenFileShow = Config.SDCARD_HIDE_OPTION;
    }

    @Override
    protected String getEmptyText() {
        return this.getString(R.string.nofile, this.getString(R.string.position_dir));
    }

    @Override
    protected void swithToPrivacyActivity() {
        SlideActivity.paramActivity.isInOperation = OperationUtil.OPER_TYPE_MOVE_PRIVACY;
        showBottomBar(View.GONE);
        SlideActivity.fileActivityInstance.setDisplayScreen(2);
        PrivacyActivity.activityInstance.switchToMove();
    }

    public void revisit() {
    }

    @Override
    public void handlerRead(Object previousSource, boolean startRoot) {
    }

    @Override
    public int[] getMenuIds() {
        return new int[]{R.id.menu_cateogry_sort_name, R.id.menu_cateogry_sort_time, R.id.menu_cateogry_sort_size, R.id.menu_cateogry_sort_type};
    }
    public void recovery() {
        setSort();
        this.firstTimeStartup(ACCESS_ORIGINAL, true, 0);
    }
    @Override
    protected void setNavigationPosAndNode(int newPos, int mode, NavigationNode nav) {
        FileInfo file = (FileInfo) nav.producingSource;
        Logs.i("this.navTool.navEntity.peek() ------------- " + (mode == MODE_NEW) + " " + this.navTool.navEntity.peek());
        if (mode == MODE_NEW) {

            if (this.navTool.navEntity.size() != 0) {
                ((SDCardNode) this.navTool.navEntity.peek()).setClickPosition(newPos);
            }
            if (!file.getPath().equals(NavigationConstants.SDCARD_PATH)) {
                this.navTool.push(
                        NavigationNode.buildNode(getNavNodeClazz(), file));
            }

            if (!this.navTool.navEntity.isEmpty() && this.navTool.navEntity.peek().displayname != null && this.navTool.navEntity.peek().displayname.equals(NavigationConstants.SDCARD)) {
                ((SDCardNode) this.navTool.navEntity.peek()).defaultPosition = -1;
            }
            this.listview.setSelection(0);
        } else if (mode == MODE_HISTORY) {
            int rememberedPos = ((SDCardNode) this.navTool.navEntity.peek()).defaultPosition;
            if (rememberedPos != -1) {
                this.listview.setSelection(rememberedPos);
            }
        }
    }

    public Class<? extends NavigationNode> getNavNodeClazz() {
        return SDCardNode.class;
    }

    public void switchToCutCopy() {
        if (SlideActivity.paramActivity.isInOperation != -1) {
            passCut_Copy(SlideActivity.paramActivity.isInOperation);
        } else if (OperationUtil.getOperType() == OperationUtil.OPER_TYPE_CUT
                || OperationUtil.getOperType() == OperationUtil.OPER_TYPE_COPY) {
            passCut_Copy(OperationUtil.getOperType());
        }
    }

    private void passCut_Copy(int command) {
        OperationUtil.mOperationState = OperationUtil.STATUS_CUT_COPY;
        firstTimeStartup(ACCESS_ORIGINAL, true, 0);
        cutOrCopy(command);

    }

    @Override
    public String getNavBarWholeShowText() {
        // TODO Auto-generated method stub
        return this.navTool.getShowText();
    }

    @Override
    protected void cutOrCopy(int command) {
        OperationUtil.mOperationState = OperationUtil.STATUS_CUT_COPY;
        operationUtil.setCommand(command);
        this.showBottomBar(View.GONE);
        this.showPasteToolBar(View.VISIBLE);
        makeSeenToAll(false);
        if (CountActivity.categoryActivity != null && CountActivity.categoryActivity.adapter != null) {
            CountActivity.categoryActivity.makeSeenToAll(false);
        }
        mStateCheckBoxVisible = false;
        mState = OperationUtil.STATUS_CUT_COPY;
    }

    @Override
    protected boolean navigate(MenuItem item) {
        // TODO Auto-generated method stub
        return fileNavigation(item);
    }

    private boolean fileNavigation(MenuItem item) {
        if (item.getItemId() - this.MENU_PATH_OFFSET < this.navTool.navEntity.size()) {
            this.makeSelectAll(false, false);
            int offset = (this.navTool.navEntity.size() - 1) - (item.getItemId() - this.MENU_PATH_OFFSET);
            NavigationNode nn = this.navTool.backward(offset);
            this.access(nn, nn.defaultPosition, true,
                    MODE_HISTORY, operationUtil.mOperationState);
            this.showBottomBar(View.GONE);
            return true;
        }
        return false;
    }
}
