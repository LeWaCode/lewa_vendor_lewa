package com.lewa.app.filemanager.ui;

import com.lewa.filemanager.cpnt.adapter.ColorInterchangedAdapter;

import android.util.Log;
import android.view.MotionEvent;
import android.content.res.Resources.NotFoundException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;

import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import android.widget.Toast;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.actions.OperationUtil;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.ds.uri.NavigationNode;
import com.lewa.filemanager.cpnt.adapter.ThumbnailAdapter;
import com.lewa.base.adapter.MapAdapter.ActionListener;
import com.lewa.base.adapter.MapAdapter.AdaptInfo;
import com.lewa.filemanager.actions.sort.ObjectSort;
import com.lewa.filemanager.config.Config;
import com.lewa.filemanager.ds.database.MimeSrc;
import com.lewa.filemanager.ds.database.copersynch.DatasrcDelete;
import com.lewa.filemanager.ds.database.copersynch.DatasrcRename;
import com.lewa.base.images.FileTypeInfo;
import com.lewa.filemanager.beans.ReportInfo;
import com.lewa.filemanager.actions.OperationManager;
import com.lewa.filemanager.actions.apk.ReportOfInstall;
import com.lewa.filemanager.actions.apk.PackageInstallManager;
import com.lewa.base.adapter.ListenerBox;
import com.lewa.filemanager.activities.views.ViewUtil;
import com.lewa.filemanager.config.Constants;
import com.lewa.filemanager.ds.uri.NavigationConstants;
import com.lewa.filemanager.ds.uri.NavigationPool;
import com.lewa.filemanager.ds.uri.SDCardNode;
import com.lewa.filemanager.beans.FileInfoUtil;
import com.lewa.filemanager.beans.FileUtil;
import com.lewa.filemanager.actions.sort.Sort;
import com.lewa.filemanager.funcgroup.EmptyDirectoryCleaner;
import com.lewa.filemanager.ds.sdcard.StrPrefixFilter;
import com.lewa.filemanager.ds.sdcard.TypeFilter;
import com.lewa.app.filemanager.ui.PathActivity.ReadRuntime;
import com.lewa.base.adapter.MapAdapter;
import com.lewa.filemanager.cpnt.adapter.FileAdapter;
import com.lewa.filemanager.cpnt.adapter.PathAdapter;
import com.lewa.filemanager.cpnt.adapter.FileItemSrc;
import com.lewa.base.adapter.ItemDataSrc;
import com.lewa.filemanager.cpnt.adapter.SelectedIndicator;
import com.lewa.filemanager.activities.views.HighLightListener;
import com.lewa.filemanager.activities.views.ViewAccessor;
import com.lewa.base.CancelClicker;
import com.lewa.filemanager.util.PmCommand;
import com.lewa.filemanager.util.ActivityPool;
import com.lewa.base.DensityUtil;
import com.lewa.base.InputmethodUtil;
import com.lewa.base.Logs;
import com.lewa.base.SharedPreferenceUtil;
import com.lewa.base.RingtoneUtil;
import com.lewa.base.images.FileCategoryHelper.FileCategory;
import com.lewa.base.images.MimeUtil;
import com.lewa.filemanager.config.RuntimeArg;
import com.lewa.filemanager.util.StatusCheckUtil;
import java.util.Collections;
import java.util.Comparator;

public abstract class CommonActivity extends Activity
        implements View.OnClickListener, OnMenuItemClickListener, ViewAccessor {

    public static final int ACCESS_ORIGINAL = 0;
    public static final int ACCESS_HANDLER = 1;
    protected static final int MENU_PATH_OFFSET = 100;
    protected static final int MENU_NEW_FOLDER = Menu.FIRST + 4;
    protected static final int MENU_DELETE = Menu.FIRST + 5;
    protected static final int MENU_RENAME = Menu.FIRST + 6;
    protected static final int MENU_SEND = Menu.FIRST + 7;
    protected static final int MENU_OPEN = Menu.FIRST + 8;
    protected static final int MENU_MOVE = Menu.FIRST + 9;
    protected static final int MENU_COPY = Menu.FIRST + 10;
    protected static final int MENU_FILEINFO = Menu.FIRST + 11;
    protected static final int MENU_REFRESH = Menu.FIRST + 12;
    protected static final int MENU_ORDER = Menu.FIRST + 13;
    protected static final int MEMU_GROUP_TITLEBTN = Menu.FIRST + 14;
    protected static final int MEMU_MOVEINTO_PRIVACY = Menu.FIRST + 15;
    protected static final int MENU_INSTALL = Menu.FIRST + 16;
    protected static final int MENU_RINGTONESET = Menu.FIRST + 17;
    protected static final int MENU_CLOCK = Menu.FIRST + 18;
    protected static final int MENU_SMS = Menu.FIRST + 19;
    protected static final int MENU_COMINGCALL = Menu.FIRST + 20;
    protected static final int MENU_CLEAN = Menu.FIRST + 21;
    protected static final int MENU_SHOW_HIDDEN = Menu.FIRST + 22;
    protected static final int MENU_MY_PIC_SIZE = Menu.FIRST + 23;
    protected static final int MENU_PREFER_SDCARD = Menu.FIRST + 24;
    protected static final int MENU_PREFER_MEMORY = Menu.FIRST + 25;
    protected static final int MENU_PREFER_SYSTEM = Menu.FIRST + 26;
    protected static final int MENU_MORE_SEND = Menu.FIRST + 27;
    protected static final int DIALOG_NEW_FOLDER = 1;
    protected static final int DIALOG_ORDER = 2;
    protected static final int DIALOG_DELETE = 3;
    protected static final int DIALOG_RENAME = 4;
    protected static final int DIALOG_FILEINFO = 5;
    protected static final int DIALOG_CLEAN_DIR = 6;
    protected static final int DIALOG_MY_PIC_SIZE = 7;
    protected static final int DIALOG_PREFER_INSTALL = 8;
    public static final int DIALOG_INSTALL_REPORT = 9;
    public static final int OPTION_UI_BOTTOM_BAR = 0;
    public static final int MODE_HISTORY = 1;
    public static final int MODE_NEW = 0;
    public static final int MODE_CURR = 2;
    public NavigationPool navTool = new NavigationPool();
    public FileAdapter adapter;
    protected OperationUtil operationUtil;
    protected FileInfo operatedfile;
    protected Integer mState = OperationUtil.mOperationState;
    protected boolean mStateCheckBoxVisible = true;
    private Drawable selector;
    protected Button back;
    protected Button sdcardPathBtn;
    public ListView fileListView;
    protected LinearLayout mEmptyLayout;
    public ListView listview;
    protected AdaptInfo adaptInfo;
    protected View bottomToolbar;
    protected View toolbar_cut;
    protected View toolbar_copy;
    protected View toolbar_del;
    protected View toolbar_cancel;
    protected View toolbar_selectAll;
    protected View pasteToolbar;
    protected View pasteBar_paste;
    protected View pasteBar_cancel;
    protected View contextMenuView;
    protected List<FileInfo> pwdDir;
    protected List<MenuItem> menuItems = new ArrayList<MenuItem>();
    protected ViewGroup menuKeyView = null;
    public CommonActivity context;
    protected AlertDialog operatingDialog;
    boolean hasSelection;
    public Sort policy;
    boolean isHiddenFileShow;
    private boolean selectwhole;
    public ProgressDialog renameWaitDialog;
    private MapAdapter reportAdapter;
    public Dialog installDialog;
    //invoked func start
    public String action_flag;
    protected List<Integer> largeSizePics = new ArrayList<Integer>();
    public View stub;
    public Button button;
    public Dialog dialog;
    public View feedback_confirm_dialog;
    public int pos;
    public static final String INTF_DATA_FOR_FEEDBACK = "PATHS";
    public static final int INTF_DATA_FOR_FEEDBACK_REQUEST_CODE = 0;
    protected String mimetype;
    protected static final String MIME_TYPE = "MIME_TYPE";
    protected static final String SELEC_TMODE = "SELECT_MODE";
    public static final String SIZE_LIMIT = "SIZE_LIMIT";
    protected static final String LIMIT_WORD = "LIMIT_WORD";
    protected Boolean isChoiceModeMultiple = null;
    protected String limitWord;
    public long pictureSize;
    protected String callbackAction;
    public static final String COUNT_INVOKED = "com.lewa.filemgr.count_start";
    public static int max_pic_char_num = 8;//限制图片筛选输入的最大字数， 32GB = 33554432 KB，输入不能超过8位，
    
    public void showBackBar() {
        if (action_flag == null) {
            return;
        }
        findViewById(R.id.feedback_sure).setVisibility(adapter.hasSelected() ? View.VISIBLE : View.GONE);
        ((Button) findViewById(R.id.sure)).setText(getString(R.string.sure) + (adapter.hasSelected() ? " ( " + adapter.getTotalSelection() + " ) " : ""));
    }

    public void initInvokedVar(Intent intent) {
        action_flag = intent == null ? null : intent.getAction();
        if (action_flag == null || !action_flag.trim().equals(Constants.InvokedPath.ACTION_INVOKED_PATH)) {
            return;
        }
        this.isChoiceModeMultiple = intent.getBooleanExtra(SELEC_TMODE, false);
        this.callbackAction = intent.getStringExtra(Constants.InvokedPath.CALLBACK_ACTION);
        button = (Button) findViewById(R.id.sure);
        Logs.i("------------->> sepererator" + button);
        button.setOnClickListener(new android.view.View.OnClickListener() {

            public void onClick(View arg0) {
                sendBack();
            }
        });
    }

    public void sendBack() {
        Intent intent = new Intent(callbackAction);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putStringArrayListExtra(INTF_DATA_FOR_FEEDBACK, toPathsInfoUtil(operationUtil.getOperationTargets()));
        sendBroadcast(intent);
        ActivityPool.getInstance().exit();
    }

    public Boolean singleChoiceInsertion(View view, Integer pos, FileInfo fileInfo, boolean isChecked) {
        if (action_flag == null) {
            return true;
        }
        CheckBox checkbox = (CheckBox) view;
        if (fileInfo.isDir && isChecked) {
            checkbox.setChecked(false);
            return null;
        }
        if (isChoiceModeMultiple == null) {
            return true;
        }

        if (!isChoiceModeMultiple) {

            if (!((CheckBox) view).isChecked()) {
                checkbox = null;
            }
            makeSelectAll(false, true, checkbox, pos);
        }
        return false;
    }

    public ArrayList<String> toPathsInfoUtil(List<FileInfo> infoes) {
        ArrayList<String> paths = new ArrayList<String>(0);
        for (FileInfo fi : infoes) {
            paths.add(fi.getPath());
        }
        return paths;
    }
    //invoked func end

    public void addContextMenus(ContextMenu menu) {
        menu.add(0, MENU_FILEINFO, 0, R.string.menu_fileinfo);
        if (this.getClass() != SearchActivity.class) {
            menu.add(0, MENU_RENAME, 0, R.string.menu_rename);
        }
//        if (!(this instanceof PrivacyActivity)) {
//            menu.add(0, MEMU_MOVEINTO_PRIVACY, 0, R.string.move_to_privacy_box);
//        }

        operatedfile.buildFile();
        String mime = FileTypeInfo.getTypeInfo(operatedfile).mime;
        if (mime != null && mime.toUpperCase().startsWith("AUDIO")) {
            SubMenu submenu = menu.addSubMenu(R.string.ringtone_set);
            submenu.add(MENU_RINGTONESET, MENU_CLOCK, 0, R.string.ringtone_clock);
            submenu.add(MENU_RINGTONESET, MENU_SMS, 0, R.string.ringtone_sms);
            submenu.add(MENU_RINGTONESET, MENU_COMINGCALL, 0, R.string.ringtone_comingcall);
        }
        menu.add(0, MENU_MOVE, 0, R.string.menu_move);
        menu.add(0, MENU_COPY, 0, R.string.menu_copy);
        menu.add(0, MENU_SEND, 0, R.string.menu_send);
        menu.add(0, MENU_DELETE, 0, R.string.menu_delete);
    }

    public Dialog createReportDialog(CancelClicker cancelClicker) {
        Dialog dialog;
        View view = LayoutInflater.from(this).inflate(R.layout.clean_dialog, null);
        dialog = new AlertDialog.Builder(this).setTitle(R.string.totalInstallFinishedHint).setView(view).setCancelable(false).setNeutralButton(R.string.i_got_it,
                cancelClicker).create();
        return dialog;
    }

    public void doAfterInstall() {
        if (installDialog.isShowing()) {
            installDialog.cancel();
            installDialog.dismiss();
        }
        this.makeSelectAll(false, true);
        this.showBottomBar(View.GONE);
    }

    public void buildAdapter(Class<? extends MapAdapter> adpaterClass, AdaptInfo adaptInfo) {
        constructAdapter(adpaterClass, adaptInfo);
//        adapter.setmThumbnailLoader(new ThumbnailLoader(handler, getContext(), ThumbnailLoader.MODE_LAZY, null));
        adapter.setUihandler(handler);
        listview = (ListView) this.findViewById(R.id.fileListView);
        if (!Config.isLewaRom) {
            listview.setCacheColorHint(Color.parseColor("#ebebeb"));
        }
        setEmptyView();
        listview.setOnCreateContextMenuListener(this);
        if (getOnClickListener() != null) {
            listview.setOnItemClickListener(getOnClickListener());
        }
    }

    public void doInstall() {
        PackageInstallManager.toInstallPaths = OperationUtil.toListPath(context);
        PmCommand.exec(PmCommand.installLocation, this);
        PackageInstallManager.getInstance().beginInstall(PackageInstallManager.toInstallPaths);
        doAfterInstall();
    }

    public View getEmptyView() {
        return findViewById(R.id.nofile_linearLayout);
    }

    public void send() {
        operationUtil.multipleSend(this);
        this.makeSelectAll(false, true);
    }

    public void setEmptyView() {
        if (getEmptyView() != null) {
            listview.setEmptyView(getEmptyView());
            setEmptyViewText(getEmptyText());
        }
    }

    public AdaptInfo getAdaptInfo() {
        // TODO Auto-generated method stub
        if (adaptInfo == null) {
            adaptInfo = new AdaptInfo();
            adaptInfo.objectFields = new String[]{Constants.FieldConstants.SIZE,
                Constants.FieldConstants.NAME, Constants.FieldConstants.LASTMODIFIED,
                Constants.FieldConstants.COUNT, Constants.FieldConstants.ICON_RES,
                Constants.FieldConstants.CHECKBOX_OPTION};
            adaptInfo.listviewItemData = new FileItemSrc();
            adaptInfo.viewIds = new int[]{R.id.fileSize,
                R.id.fileNameSubFileNum, R.id.fileTimeSize,
                R.id.subFileNum, R.id.fileIcon, R.id.handleCheckbox};
            adaptInfo.listviewItemLayoutId = R.layout.filelist_item;
            adaptInfo.actionListeners = getViewHandlers();
        }
        return adaptInfo;
    }

    public boolean isSelectwhole() {
        return selectwhole;
    }

    public View buildList() {
        return LayoutInflater.from(this).inflate(R.layout.sdcardui, null);
    }
    public View contentView;

    public boolean browseCreateView(ActionListener[] handlers, OnItemClickListener listener, boolean notifyDataChanged) {
        if (list == null) {
            contentView = list = buildList();
            Logs.i("", "11 #--" + contentView);
            this.setContentView(list);
            initNavBar();
            initListView(PathAdapter.class);
            initBottomBar();
            this.initInvokedVar(this.getIntent());
            listview.setAdapter(adapter);
        } else {
            if (contentView != list) {
                contentView = list;
                this.setContentView(list);
            }
            notifyDataChanged = true;
        }
        return notifyDataChanged;
    }

    public ThumbnailAdapter.ActionListener[] getViewHandlers() {
        return viewHandlers;
    }

    public OnItemClickListener getOnClickListener() {
        return listItemClickListener;
    }
    protected ThumbnailAdapter.ActionListener[] viewHandlers = new ThumbnailAdapter.ActionListener[]{
        new ThumbnailAdapter.ActionListener(R.id.handleCheckbox, ThumbnailAdapter.ActionListener.OnClick) {

            @Override
            public void handle(View view, ListenerBox listener) {
                ThumbnailAdapter baseAdapter = (ThumbnailAdapter) listener.getBaseAdapter();
                View v = ViewUtil.findListViewByItemView(view);
                if (v == null) {
                    return;
                }
                ListView listview = (ListView) v.getParent();
                Integer pos = baseAdapter.getViewContentMap().get(v);
                if (pos == null) {
                    return;
                }
                boolean isChecked = ((CheckBox) view).isChecked();
                FileInfo fileInfo = (FileInfo) baseAdapter.getItem(pos);
                if (fileInfo == null) {
                    return;
                }
                Boolean result = singleChoiceInsertion(view, pos, fileInfo, isChecked);
                if (result == null) {
                    return;
                }
                isChecked = ((CheckBox) view).isChecked();
                baseAdapter.addSelected(pos, isChecked);
                if (isChecked) {
                    operationUtil.addOperationTarget(fileInfo);
                } else {
                    operationUtil.<FileInfo>removeOperationTarget(fileInfo);
                }
                showBackBar();
                if (result != null && !result) {
                    return;
                }
                if (context.getClass() == SearchActivity.class) {
                    InputmethodUtil.setupInputWindow(false, context);
                }

                treatBottmBar(baseAdapter);
                handler.sendEmptyMessage(Constants.OperationContants.CHOOSE_TEXT_CHANGED);
                Logs.i("", "i ============== " + fileInfo.path + " " + operationUtil.getOperationTargets().size() + " " + isChecked);
            }
        }
    };
    OnItemClickListener listItemClickListener = new ListItemClickListener();

    public void showReport(Dialog dialog, String reportMessage, int reportCount, final List<ReportInfo> reportInfos) {
        final Dialog cleandialog = dialog;
        if (reportAdapter == null) {
            reportAdapter = new ColorInterchangedAdapter(this, getCleanAdaptInfo());
            ((ListView) ((AlertDialog) cleandialog).findViewById(R.id.cleanedlist)).setAdapter(reportAdapter);
        }
        reportAdapter.notifyDataSetChanged();
        ((TextView) cleandialog.findViewById(R.id.cleancount)).setText(reportMessage);
        if (!Config.isLewaRom) {
            ((TextView) cleandialog.findViewById(R.id.cleancount)).setTextColor(Color.WHITE);
        }
        final View clean_detail_line = ((AlertDialog) cleandialog).findViewById(R.id.clean_detail_line);
        clean_detail_line.setVisibility(View.GONE);
        ((Button) cleandialog.findViewById(R.id.cleanmoredetail)).setVisibility(View.GONE);
        (cleandialog.findViewById(R.id.cleandetail)).setVisibility(View.GONE);
        if (reportCount == 0) {
            ((Button) cleandialog.findViewById(R.id.cleanmoredetail)).setVisibility(View.GONE);
        } else {
            ((Button) cleandialog.findViewById(R.id.cleanmoredetail)).setText(R.string.click_more_detail);
            ((Button) cleandialog.findViewById(R.id.cleanmoredetail)).setVisibility(View.VISIBLE);
            ((Button) cleandialog.findViewById(R.id.cleanmoredetail)).setOnClickListener(new View.OnClickListener() {

                boolean reportListShowed;

                public void onClick(View arg0) {
                    Button cleanDetailView = (Button) ((AlertDialog) cleandialog).findViewById(R.id.cleanmoredetail);
                    LinearLayout cleanLayout = (LinearLayout) ((AlertDialog) cleandialog).findViewById(R.id.cleandetail);
                    View clean_detail_line = ((AlertDialog) cleandialog).findViewById(R.id.clean_detail_line);
                    if (!reportListShowed) {
                        reportListShowed = true;
                        clean_detail_line.setVisibility(View.VISIBLE);
                        clean_detail_line.requestLayout();
                        cleanDetailView.setText(R.string.click_hide_detail);
                        reportAdapter.setItemDataSrc(new ItemDataSrc(reportInfos));
                        reportAdapter.notifyDataSetChanged();
                        cleanLayout.setVisibility(View.VISIBLE);
                    } else {
                        reportListShowed = false;
                        cleanDetailView.setText(R.string.click_more_detail);
                        cleanLayout.setVisibility(View.GONE);
                        clean_detail_line.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    protected void ringtoneSetup(ContextMenu menu) {
        Logs.i("", " ringtoneSetup ");
    }

    public void treatBottmBar() {
        treatBottmBar(adapter);
    }

    public void treatBottmBar(ThumbnailAdapter baseAdapter) {
        if (baseAdapter.isSelected()) {
            showBottomBar(View.VISIBLE);
            SelectedIndicator.clearOthersSelectionState(context);
            SelectedIndicator.putSelected(context);
        } else {
            showBottomBar(View.GONE);
        }
    }

    protected void prepareMusicInfo(int visible, FileInfo info) {
        if (info.getFile().isFile()) {
            visible = this.navTool.navEntity.peek().producingSource.toString().toUpperCase().equals("AUDIO") ? View.VISIBLE : View.GONE;
        }
        value_artist.setVisibility(visible);
        value_album.setVisibility(visible);
        key_artist.setVisibility(visible);
        key_album.setVisibility(visible);
    }

    protected void showInstallMenu(ContextMenu menu, ContextMenuInfo menuInfo) {
        menu.add(0, MENU_PREFER_MEMORY, 0, R.string.prefer_memory);
        menu.add(0, MENU_PREFER_SDCARD, 0, R.string.prefer_sdcard);
        menu.add(0, MENU_PREFER_SYSTEM, 0, R.string.prefer_system);
    }

    protected void showApkViewContextMenux(ContextMenu menu, ContextMenuInfo menuInfo) {
    }

    protected class ListItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            FileInfo fileInfo = ((FileInfo) (parent.getItemAtPosition(position)));
            if (((ThumbnailAdapter) parent.getAdapter()).hasSelected() && OperationUtil.mOperationState != OperationUtil.STATUS_CUT_COPY) {
                Logs.i("", "=============== (((FileAdapter) parent.getAdapter()).selectedNum " + (((ThumbnailAdapter) parent.getAdapter()).selectedNum));
                CheckBox checkBox = (CheckBox) ViewUtil.findViewById(view,
                        R.id.handleCheckbox);
                checkBox.performClick();
                treatBottmBar((ThumbnailAdapter) parent.getAdapter());
                return;
            }
            enterDir(fileInfo);
            if (!fileInfo.getIsDir()) {
                if (!fileInfo.getFile().exists()) {
                    updateCountActivity(fileInfo);
                    return;
                }
                if (context.getClass() != SearchActivity.class && (FileInfoUtil.isFileType(fileInfo) == FileInfoUtil.FileType.Apk || FileInfoUtil.isFileType(fileInfo) == FileInfoUtil.FileType.Lwt)) {
                    recordPageSharedPreference(navTool.navEntity.peek().producingSource.toString());
                }
                if (FileInfoUtil.isFileType(fileInfo) == FileInfoUtil.FileType.Lwt) {
                    SharedPreferenceUtil.putValue(context, Constants.SharedPrefernce.RememberedCategory, Constants.SharedPrefernce.KEY_LWT_ISDELETED, fileInfo.path);
                    Intent intent = new Intent(Constants.GoToInvokeLWT.ACTION_INVOKE_LWT);
                    intent.putExtra(Constants.GoToInvokeLWT.FLAG_KEY_INVOKE_LWT, Constants.GoToInvokeLWT.FLAG_INVOKE_LWT);
                    intent.putExtra(Constants.GoToInvokeLWT.INVOKE_LWT_FILE_FIELD, fileInfo.path);
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                    }
                    return;
                }
                operationUtil.openFile(fileInfo, context);
                return;
            }
        }

        public void enterDir(FileInfo fileInfo) {
            if (fileInfo.isDir) {
                Integer firstPos = ((MapAdapter) listview.getAdapter()).getViewContentMap().get(listview.getChildAt(0));
                access(new SDCardNode(fileInfo), firstPos, true, MODE_NEW, operationUtil.mOperationState);
            }
        }
    };

    public void reFreshCheckboxVisible() {
        boolean isVisible = OperationUtil.mOperationState != OperationUtil.STATUS_CUT_COPY;
        if (adapter!=null&&adapter.isVisible != isVisible) {
            this.makeSeenToAll(isVisible);
        }
    }

    public void recordPageSharedPreference(String rememberedCategory) {
    }

    public void initListView(Class<? extends MapAdapter> adpaterClass) {
        getAdaptInfo();
        buildAdapter(adpaterClass, adaptInfo);
    }

    public void pasteCancel() {
        this.showPasteToolBar(View.GONE);
        if (CountActivity.categoryActivity != null) {
            CountActivity.categoryActivity.makeSeenToAll(true);
        }
        if (SearchActivity.markfromPathActivity) {
            this.startActivity(new Intent(this, SearchActivity.class));
        }
        makeSeenToAll(true);
        OperationManager.operationMap.get(OperationManager.operationTaker).makeSelectAll(false, true);
        OperationManager.operationMap.get(OperationManager.operationTaker).makeSeenToAll(true);
        OperationManager.operationMap.get(OperationManager.operationTaker).showBottomBar(View.GONE);
        operationUtil.setCommand(-1);
        operationUtil.dataClear();
        OperationUtil.mOperationState = OperationUtil.STATUS_BROWSE;
        operationUtil.mOperationState = OperationUtil.STATUS_BROWSE;
        mStateCheckBoxVisible = true;

    }

    protected abstract void setHiddenFileShowFlag();

    protected void setSort() {
        policy = new ObjectSort(ObjectSort.SORT_POLICY_TITLE, ObjectSort.SORT_SEQ_ASC);
    }

    public boolean isHiddenFileShow() {
        return isHiddenFileShow;
    }

    public ThumbnailAdapter getAdapter() {
        return adapter;
    }

    protected void updateCountActivity(FileInfo fileInfo) {
    }

    public void constructAdapter(Class<? extends MapAdapter> adapterClazz, AdaptInfo adaptInfo) {
        try {
            adapter = (FileAdapter) adapterClazz.getConstructor(Context.class, AdaptInfo.class).newInstance(this, adaptInfo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void constructAdapter(Class<? extends MapAdapter> adapterClazz) {
        try {
            adapter = (FileAdapter) adapterClazz.getConstructor(Context.class).newInstance(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public abstract void handlerRead(Object previousSource, boolean startRoot);
    public Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.OperationContants.FINISH_OPERATION:
                    updateOperation(msg);
                    break;
                case Constants.OperationContants.ICON_LOADED:
                    ((ThumbnailAdapter) listview.getAdapter()).notifyDataSetChanged();
                    break;
                case Constants.OperationContants.SEARCHNOTIFY:
                case Constants.OperationContants.DIR_REFRESH:
                    adapter.notifyDataSetChanged();
                    break;
                case Constants.OperationContants.CHOOSE_TEXT_CHANGED:
                    selectTextImageChange();
                    break;
                case Constants.OperationContants.RENAME_FINISH:
                    handlerLocate(msg.arg1);
                    break;
                case Constants.OperationContants.RENAMING:
                    sortRefresh();
                    final int selection = adapter.getPathOnlyItemPos((FileInfo) msg.obj);
                    handlerLocate(selection);
                    renameWaitDialog.cancel();
                    break;
                case Constants.OperationContants.CLEAN_NOTIFY:
                    adapter.notifyDataSetChanged();
                    showDialog(DIALOG_CLEAN_DIR);
                    refresh();
                    break;
                case Constants.OperationContants.REFRESH_APKINFO:
                    adapter.notifyDataSetChanged();
                    break;
                case Constants.OperationContants.LOAD_PATHACTIVITY:
                    if (PathActivity.activityInstance != null) {
                        PathActivity.activityInstance.firstTimeStartup(ACCESS_ORIGINAL, false, 0);;
                    }
                    break;
                case Constants.OperationContants.LOAD_PATHACTIVITY_VIEW:
                    ReadRuntime thisRrt = (ReadRuntime) msg.obj;
                    if (PathActivity.activityInstance != null) {

                        PathActivity.activityInstance.refreshSDcardReadView(thisRrt.status, thisRrt.navNode, thisRrt.listviewPos, thisRrt.readMode);
                    }
                    break;
                case Constants.OperationContants.ONINSTALL_REPORT:
                    showDialog(CommonActivity.DIALOG_INSTALL_REPORT);
                    break;
            }
        }

        public void updateOperation(Message msg) {
            MimeSrc.recountCategoryNum(context);
            operationUtil.mOperationState = OperationUtil.STATUS_BROWSE;
            operationUtil.duplicated.clear();
            operationUtil.pasteOperEntities.clear();
            OperationManager.clearMarks();
            makeSeenToAll(true);
            showBottomBar(View.GONE);
            ((MapAdapter) adapter).notifyDataSetChanged();
            listview.requestLayout();
            listview.invalidate();
            refresh();
            ActivityPool.getInstance().refresh(context);
            Dialog dialog = (Dialog) msg.obj;
            if (dialog != null) {
                dialog.dismiss();
                dialog.cancel();
            }
        }

        public void handlerLocate(int passedPos) {
            while (listview.getSelectedItemPosition() != passedPos) {
                listview.setSelection(passedPos);
                if (listview.isFocused()) {
                    listview.clearFocus();
                }
                listview.requestFocusFromTouch();
            }
        }
    };

    public void navigationBarSetup(int newPos, int mode, NavigationNode navigation) {
        this.setNavigationPosAndNode(newPos, mode, navigation);
        this.sdcardPathBtn.setText(getNavBarWholeShowText());
    }

    public boolean searchFileInfoInList(List<FileInfo> infos, FileInfo info, Comparator<FileInfo> comparator) throws IllegalStateException {
        return Collections.binarySearch(infos, info, comparator) >= 0;

    }

    public List<FileInfo> setupPrevUILoad() {
        List<FileInfo> filesInfo = (this.adapter == null || this.adapter.getItemDataSrc() == null) ? null : (List<FileInfo>) this.adapter.getItemDataSrc().getContent();
        return filesInfo;
    }

    public void setupAfterUILoad(int status, NavigationNode navigation, int newPos, int mode, List<FileInfo> filesInfo) {
        if (this.hasToOperatingOption() && status == OperationUtil.STATUS_CUT_COPY) {
            this.showBottomBar(View.GONE);
        } else {
            if (!(SlideActivity.paramActivity.isInOperation != -1
                    || OperationUtil.getOperType() == OperationUtil.OPER_TYPE_CUT || OperationUtil.getOperType() == OperationUtil.OPER_TYPE_COPY)) {
                OperationUtil.dataClear();
            }
        }
        if (mode == MODE_HISTORY) {
            listview.clearFocus();
        }
        if (mode == MODE_NEW) {
            if (adapter != null) {
                adapter.markVisible(true);
            }
        }
        if (mode == MODE_CURR) {
            listview.setSelection(newPos);
        }
        this.makeSelectAll(false, false);
        reFreshCheckboxVisible();
    }

    public void updateDelOnCategory() {
        for (FileInfo info : OperationUtil.operMatcher.keySet()) {
            DatasrcDelete.recursiveUpdateDel(info, this);
        }
        CommonActivity.refreshCountActivity();
        PathActivity.activityInstance.refresh();
    }

    public void updateDelOnCategory4Privacy() {
        for (FileInfo info : OperationUtil.operMatcher.keySet()) {
            DatasrcDelete.recursiveUpdateDel(info, this);
        }
    }

    public static void refreshCountActivity() {
        CountActivity.categoryActivity.status = OperationUtil.STATUS_BROWSE;
        CountActivity.categoryActivity.refresh();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Logs.i("", "-------- start" + this);
        this.context = this;
        ActivityPool.getInstance().addActivity(this);
        if (StatusCheckUtil.srcardStateResolve(true, this)) {
            return;
        }
        initDialogs();
        initOperation();
        startup();
    }

    @Override
    public void onAttachedToWindow() {
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
        super.onAttachedToWindow();
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (StatusCheckUtil.srcardStateResolve(true, this)) {
            return;
        }
		this.context = this;
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				SelectedIndicator.clearState.put(context, true);
				initListViewSelector();
				setSort();
				setHiddenFileShowFlag();
			}

		}).start();
	}
    public boolean hasToOperatingOption() {
        Logs.i("", "=== operationSrc.size()==0 " + (operationUtil.selectedEntities.size() == 0) + " operationSrc.isEmpty() " + operationUtil.selectedEntities.isEmpty());
        return operationUtil.selectedEntities.size() == 0 || operationUtil.selectedEntities.isEmpty() ? false
                : true;
    }

    private void initListViewSelector() throws NotFoundException {
        if (selector == null) {
            selector = this.getResources().getDrawable(R.drawable.bg_common_pressed);
        }
    }

    public void initNavBar() {
        // TODO Auto-generated method stub
        sdcardPathBtn = (Button) findViewById(R.id.sdcardPathBtn);
        sdcardPathBtn.setOnCreateContextMenuListener(this);
        sdcardPathBtn.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openContextMenu(v);
            }
        });
        sdcardPathBtn.setOnTouchListener(new View.OnTouchListener() {

            String text = null;

            public boolean onTouch(View view, MotionEvent mevent) {
                view.setBackgroundResource(view.isPressed() ? R.drawable.lewa_bt_folder_installed_pressed : R.drawable.button);
                switch (mevent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        view.setBackgroundResource(R.drawable.lewa_bt_folder_installed_pressed);
                        break;
                    case MotionEvent.ACTION_OUTSIDE:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        view.setBackgroundResource(R.drawable.button);
                        openContextMenu(view);
                }
                reserveText(view);
                return true;
            }

            public void reserveText(View view) {
                text = ((TextView) view).getText().toString();
                view.setPadding(DensityUtil.dip2px(context, 8), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                ((TextView) view).setText(text);
                view.requestLayout();
                view.invalidate();
            }
        });
    }

    public void initOperation() {
        operationUtil = new OperationUtil();
        OperationManager.operationMap.put(operationUtil, this);
        operationUtil.mOperationState = OperationUtil.STATUS_BROWSE;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // TODO Auto-generated method stub
        if (policy == null) {
            return false;
        }
        int policyType = -1;
        int policySeq = -1;
        switch (item.getItemId()) {
            case R.id.menu_cateogry_sort_name:
                policyType = ObjectSort.SORT_POLICY_TITLE;
                policySeq = ObjectSort.SORT_SEQ_ASC;
                break;
            case R.id.menu_cateogry_sort_size:
                policyType = ObjectSort.SORT_POLICY_SIZE;
                policySeq = ObjectSort.SORT_SEQ_ASC;
                break;
            case R.id.menu_cateogry_sort_time:
                policyType = ObjectSort.SORT_POLICY_LAST_MODIFIED_TIME;
                policySeq = ObjectSort.SORT_SEQ_DES;
                break;
            case R.id.menu_cateogry_sort_type:
                policyType = ObjectSort.SORT_POLICY_TYPE;
                policySeq = ObjectSort.SORT_SEQ_ASC;
                break;
            case R.id.menu_album_sort_artist:
                policyType = ObjectSort.SORT_POLICY_ARTIST;
                policySeq = ObjectSort.SORT_SEQ_ASC;
                break;
        }
        item.setChecked(true);
        policy.setSeq(policySeq);
        policy.setPolicy(policyType);
        sortRefresh();
        this.showBottomBar(View.GONE);
        this.showPasteToolBar(View.GONE);
        return false;
    }

    public abstract void startup();

    protected void turnToFileBrowsing(ActionListener[] handlers,
            OnItemClickListener listener, NavigationNode nav) {
        turnToFileBrowsing(handlers,
                listener, nav, -1);
    }

    protected void turnToFileBrowsing(ActionListener[] handlers,
            OnItemClickListener listener, NavigationNode nav, int accessFlag) {
        boolean notifyDataChanged = false;
        try {
            access(nav, nav.defaultPosition, notifyDataChanged, MODE_NEW, operationUtil.mOperationState, accessFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean setLoadThumbnailBeginEnd(AbsListView view, int tailVisiblePos) {
        if (view.getChildCount() <= 0) {
            return true;
        }
        View child = view.getChildAt(0);
        if (child == null) {
            return true;
        }
        Integer idx = ((ThumbnailAdapter) view.getAdapter()).getViewContentMap().get(child);
        idx = idx == null ? 0 : idx;
        Integer idxTailChild = -1;
        idxTailChild = (tailVisiblePos > -1 ? tailVisiblePos : ((ThumbnailAdapter) view.getAdapter()).getViewContentMap().get(view.getChildAt(view.getChildCount() - 1)));
        idxTailChild = idxTailChild == null ? 0 : idxTailChild;
        adapter.setThreadLoadBeginNum(idx);
        adapter.setThreadLoadEndNum(idxTailChild);
        return false;
    }
    protected View list;

    protected Context getContext() {
        return CommonActivity.this;
    }
    public List<FileInfo> filesInfo;
    public int accessFlag;
    public int newPos;
    public NavigationNode navigation;
    public int status = -1;

    public void initBottomBar() {
        bottomToolbar = findViewById(R.id.bottom_toolbar);
        toolbar_cut = bottomToolbar.findViewById(R.id.toolbar_cut);
        toolbar_copy = bottomToolbar.findViewById(R.id.toolbar_copy);
        toolbar_del = bottomToolbar.findViewById(R.id.toolbar_del);
        toolbar_cancel = bottomToolbar.findViewById(R.id.toolbar_cancel);
        toolbar_selectAll = bottomToolbar.findViewById(R.id.toolbar_select);

        toolbar_cut.setOnClickListener(this);
        toolbar_copy.setOnClickListener(this);
        toolbar_del.setOnClickListener(this);
        toolbar_cancel.setOnClickListener(this);
        toolbar_selectAll.setOnClickListener(this);

        toolbar_cut.setOnTouchListener(touchEffectListner);
        toolbar_copy.setOnTouchListener(touchEffectListner);
        toolbar_del.setOnTouchListener(touchEffectListner);
        toolbar_cancel.setOnTouchListener(touchEffectListner);
        toolbar_selectAll.setOnTouchListener(touchEffectListner);

        pasteToolbar = findViewById(R.id.layout_paste);
        pasteBar_paste = pasteToolbar.findViewById(R.id.toolbar_paste);
        pasteBar_cancel = pasteToolbar.findViewById(R.id.pastebar_cancel);
        pasteBar_paste.setOnClickListener(this);
        pasteBar_cancel.setOnClickListener(this);
        pasteBar_paste.setOnTouchListener(touchEffectListner);
        pasteBar_cancel.setOnTouchListener(touchEffectListner);
        //install
        findViewById(R.id.operate).setOnCreateContextMenuListener(this);
        findViewById(R.id.operate).setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openContextMenu(v);
            }
        });
        findViewById(R.id.operate).setOnTouchListener(touchEffectListner);
    }

    public void showBottomBar(int visibility) {

        bottomToolbar.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            TextView selectAll = ((TextView) this.toolbar_selectAll.findViewById(R.id.toolbar_select_text));
            if (selectAll == null) {
                return;
            }
            selectAll.setText(R.string.select_all);
        }
    }

    protected abstract void access(NavigationNode navig, int newPos,
            boolean notifyDataSetChanged, int mode, int status);

    protected abstract void access(NavigationNode navig, int newPos,
            boolean notifyDataSetChanged, int mode, int status, int accessFlag);

    protected abstract void setNavigationPosAndNode(int newPos, int mode,
            NavigationNode navSource);

    public abstract String getNavBarWholeShowText();

    public String getSDcardPath() {
        return NavigationConstants.SDCARD_PATH;
    }

    public void hideMusicMenu(Menu menu, boolean visible) {
        menu.findItem(R.id.menu_album_sort_artist).setVisible(visible);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sdcardmenu, menu);
        setMenuItemById(menu, menu.size());
        setMenuItemById(menu.findItem(R.id.menu_sort).getSubMenu(), menu.findItem(R.id.menu_sort).getSubMenu().size());
        return true;

    }

    public abstract int[] getMenuIds();

    public void setMenuItemById(Menu menu, int idcount) {
        for (int i = 0; i < idcount; i++) {
            setMenuItem(menu, i);
        }
    }

    public void setMenuItem(Menu menu, int menuItemIdx) {
        MenuItem mItem = menu.getItem(menuItemIdx);
        mItem.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort:
                return super.onOptionsItemSelected(item);
            case R.id.menu_new_dir:
                showDialog(DIALOG_NEW_FOLDER);
                return true;
            case R.id.menu_refresh:
                refresh(true);
                this.showBottomBar(View.GONE);
                this.showPasteToolBar(View.GONE);
                return true;
            case MENU_SHOW_HIDDEN:
                Config.SDCARD_HIDE_OPTION = !Config.SDCARD_HIDE_OPTION;
                refresh();
                return true;
            case MENU_CLEAN:
                EmptyDirectoryCleaner.clearCleanData();
                if (reportAdapter != null) {
                    reportAdapter.notifyDataSetChanged();
                }
                Toast.makeText(context, R.string.cleanstarted, Toast.LENGTH_SHORT).show();
                new HandlerThread("", android.os.Process.THREAD_PRIORITY_BACKGROUND) {

                    @Override
                    public void run() {
                        new File(context.getClass() == PathActivity.class ? ((FileInfo) navTool.navEntity.peek().producingSource).path : "/mnt/sdcard").listFiles(new EmptyDirectoryCleaner(new Integer[]{TypeFilter.FILTER_BOTH_DIR_FILE}, Constants.HIDDEN_EXCLUDED, Constants.HIDDEN_INCLUDED));
                        handler.sendEmptyMessage(Constants.OperationContants.CLEAN_NOTIFY);
                    }
                }.start();
                return true;

            case MENU_MY_PIC_SIZE:
                showDialog(DIALOG_MY_PIC_SIZE);
                return true;

            default:
                this.sortRefresh();
                return super.onOptionsItemSelected(item);
        }
    }
    ContextMenu thismenu;
    ContextMenuInfo thismenuInfo;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
            ContextMenuInfo menuInfo) {
        thismenu = menu;
        thismenuInfo = menuInfo;
        Logs.i("------------------" + menu.size() + " " + menuInfo);


        contextMenuView = view;
        switch (view.getId()) {
            case R.id.fileListView:
                if (OperationUtil.mOperationState == OperationUtil.STATUS_CUT_COPY) {
                    return;
                }
                if (this.adapter.hasSelected()) {
                    if (this.pasteToolbar.getVisibility() == View.VISIBLE) {
                        return;
                    }
                    // toolbar is in visible state
                    view.performClick();
                    return;
                }
                fileListContextMenux(menu, menuInfo);
                break;
            case R.id.sdcardPathBtn:
                sdcardPathBtnContextMenux(menu, menuInfo);
                break;
            case R.id.operate:
                showApkViewContextMenux(menu, menuInfo);
                break;
            default:
                break;
        }
    }

    protected void fileListContextMenux(ContextMenu menu,
            ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        FileInfo item;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            item = (FileInfo) adapter.getItem(info.position);
        } catch (ClassCastException e) {
            Logs.e("", "bad menuInfo" + e.getMessage());
            return;
        }
        item.buildFile();
        menu.setHeaderTitle(item.getName());
        if (item.getIconRes() instanceof Drawable) {
            menu.setHeaderIcon((Drawable) item.getIconRes());
        }
        operatedfile = item;
        addContextMenus(menu);
    }

    protected void sdcardPathBtnContextMenux(ContextMenu menu,
            ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.path_choice);
        Logs.i("", "pass sdcardbutton");
        for (int i = 0; i < this.navTool.navEntity.size() - 1; i++) {
            Logs.i("", "sdcardbutton " + i);
            menu.add(MEMU_GROUP_TITLEBTN, this.MENU_PATH_OFFSET + i, 0,
                    this.navTool.navEntity.get(i).displayname);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        FileInfo listviewItem = null;

        if (item.getGroupId() == MENU_RINGTONESET) {
            switch (item.getItemId()) {
                case MENU_CLOCK:
                    RingtoneUtil.setVoice(context, this.operatedfile.path, RingtoneUtil.ALARM);
                    Toast.makeText(context, R.string.ringtone_set_success, Toast.LENGTH_SHORT).show();
                    return true;
                case MENU_SMS:
                    RingtoneUtil.setVoice(context, this.operatedfile.path, RingtoneUtil.NOTIFICATION);
                    Toast.makeText(context, R.string.ringtone_set_success, Toast.LENGTH_SHORT).show();
                    return true;
                case MENU_COMINGCALL:
                    RingtoneUtil.setVoice(context, this.operatedfile.path, RingtoneUtil.RINGTONE);
                    Toast.makeText(context, R.string.ringtone_set_success, Toast.LENGTH_SHORT).show();
                    return true;
            }
        }
        FileInfo path = null;
        CheckBox cb = null;
        if (this.contextMenuView instanceof ListView) {

            listviewItem = (FileInfo) ((ThumbnailAdapter) listview.getAdapter()).getItem(menuInfo.position);// Remember
            // current
            // selection
            operatedfile = path = listviewItem;
        }

        ListAdapter adapter = listview.getAdapter();
        if (adapter == null) {
            return false;
        }

        if (item.getItemId() > MENU_PATH_OFFSET - 1 && item.getItemId() < MENU_PATH_OFFSET + this.navTool.navEntity.size() - 1) {
            navigate(item);
            return true;
        }
        switch (item.getItemId()) {
            case MEMU_MOVEINTO_PRIVACY:
                OperationManager.operationTaker = operationUtil;
                OperationUtil.dataClear();
                operationUtil.addOperationTarget(path);
                swithToPrivacyActivity();

                return true;
            case MENU_MOVE:
                OperationUtil.mOperationState = OperationUtil.STATUS_CUT_COPY;
                OperationManager.operationTaker = operationUtil;
                operationUtil.addOperationTarget(path);
                cutOrCopy(OperationUtil.OPER_TYPE_CUT);
                return true;
            case MENU_COPY:
                OperationUtil.mOperationState = OperationUtil.STATUS_CUT_COPY;
                OperationManager.operationTaker = operationUtil;
                operationUtil.addOperationTarget(path);
                cutOrCopy(OperationUtil.OPER_TYPE_COPY);
                return true;
            case MENU_RENAME:
                OperationManager.operationTaker = operationUtil;
                showDialog(DIALOG_RENAME);

                return true;
            case MENU_DELETE:
                OperationManager.operationTaker = operationUtil;
                operationUtil.addOperationTarget(path);
                operationUtil.invokeOperation(
                        OperationUtil.OPER_TYPE_DEL, this);
                operationUtil.mOperationState = OperationUtil.STATUS_BROWSE;
                return true;

            case MENU_FILEINFO:
                OperationManager.operationTaker = operationUtil;
                showDialog(DIALOG_FILEINFO);
                return true;
            case MENU_SEND:
                operationUtil.addOperationTarget(path);
                operationUtil.multipleSend(this);
                return true;
            case MENU_INSTALL:
                showDialog(DIALOG_PREFER_INSTALL);
//                doInstall();
                return true;
        }
        return false;
    }

    protected abstract boolean navigate(MenuItem item);
    OnTouchListener touchEffectListner = new HighLightListener();

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_NEW_FOLDER:
                LayoutInflater inflater = LayoutInflater.from(this);
                View view = (ViewGroup) inflater.inflate(
                        R.layout.dialog_new_folder, null);
                final EditText et = (EditText) view.findViewById(R.id.foldername);

                et.setText(R.string.menu_new_folder);
                Selection.setSelection(new SpannableString(this.getString(R.string.menu_new_folder)), 0, 4);
                return operatingDialog = new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.menu_new_dir).setView(view).setPositiveButton(android.R.string.ok,
                        new OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {
                                createNewFolder(et.getText().toString());
                            }
                        }).setNegativeButton(android.R.string.cancel,
                        new CancelClicker(this)).create();
            case DIALOG_CLEAN_DIR:
                View cleanView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.clean_dialog, null);
                return new AlertDialog.Builder(this).setTitle(getString(R.string.cleanfinished)).setView(cleanView).setCancelable(false).setNeutralButton(R.string.i_got_it,
                        new OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.cancel();
                                ((AlertDialog) dialog).findViewById(R.id.cleanmoredetail).setVisibility(View.GONE);
                                dialog.dismiss();
                            }
                        }).create();
            case DIALOG_DELETE:
                return new AlertDialog.Builder(this).setTitle(getString(R.string.really_delete, operatedfile.getName())).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(android.R.string.ok,
                        new OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {
                                operationUtil.recursiveDelete(new File(operatedfile.getPath()));
                            }
                        }).setNegativeButton(android.R.string.cancel,
                        new CancelClicker(this)).create();

            case DIALOG_RENAME:
                inflater = LayoutInflater.from(this);
                view = inflater.inflate(R.layout.dialog_new_folder, null);
                final EditText et2 = (EditText) view.findViewById(R.id.foldername);
                return new AlertDialog.Builder(this).setTitle(R.string.menu_rename).setView(view).setPositiveButton(android.R.string.ok,
                        new OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {
                                if (et2.getText().toString().trim().equals("")) {
                                    Toast.makeText(context, R.string.target_name_is_not_allow_null, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String fullname = et2.getText().toString().trim();
                                Boolean result = OperationUtil.renameFileOrFolder(new File(operatedfile.getPath()), fullname, context);
                                if (result == null) {
                                    return;
                                }
                                if (!result) {
                                    Toast.makeText(context, R.string.rename_fail, Toast.LENGTH_LONG).show();
                                    return;
                                }
                                FileInfo targetInfo = new FileInfo(new File(FileUtil.getParent(operatedfile.getPath()) + "/" + fullname), PathActivity.activityInstance);
                                targetInfo.buildName();
                                post_rename(operatedfile, targetInfo, result, (CommonActivity) context);

                            }
                        }).setNegativeButton(android.R.string.cancel,
                        new CancelClicker(this)).create();
            case DIALOG_FILEINFO:
                inflater = LayoutInflater.from(this);
                view = inflater.inflate(R.layout.dialog_detail, null);
                return new AlertDialog.Builder(this).setTitle(R.string.menu_fileinfo).setView(view).setNeutralButton(R.string.i_got_it,
                        new CancelClicker(this)).create();
            case DIALOG_PREFER_INSTALL:
                inflater = LayoutInflater.from(this);
                view = inflater.inflate(R.layout.dialog_prefer_install, null);
                view.findViewById(R.id.prefer_memory).setOnClickListener(this);
                view.findViewById(R.id.prefer_memory).setOnTouchListener(touchEffectListner);
                view.findViewById(R.id.prefer_sdcard).setOnClickListener(this);
                view.findViewById(R.id.prefer_sdcard).setOnTouchListener(touchEffectListner);
                view.findViewById(R.id.prefer_system).setOnClickListener(this);
                view.findViewById(R.id.prefer_system).setOnTouchListener(touchEffectListner);
                if (!Config.isLewaRom) {
                    ((TextView) view.findViewById(R.id.prefer_memory)).setTextColor(Color.WHITE);
                    ((TextView) view.findViewById(R.id.prefer_sdcard)).setTextColor(Color.WHITE);
                    ((TextView) view.findViewById(R.id.prefer_system)).setTextColor(Color.WHITE);
                }
                installDialog = new AlertDialog.Builder(this).setTitle(R.string.setupto).setView(view).create();
                return installDialog;
            case DIALOG_INSTALL_REPORT:
                return createReportDialog(new CancelClicker(this) {

                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        PackageInstallManager.getInstance().clear();
                        PackageInstallManager.getInstance().clearHistory();
                        super.onClick(dialog, arg1);
                    }
                });
            case DIALOG_MY_PIC_SIZE:
                inflater = LayoutInflater.from(this);
                view = inflater.inflate(R.layout.dialog_my_pic_size, null);
                return new AlertDialog.Builder(this).setTitle(R.string.menu_my_pic_size).setView(view).setPositiveButton(android.R.string.ok,
                        new OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {
                                String sKsize = ((EditText) ((AlertDialog) dialog).findViewById(R.id.set_size)).getText().toString();
                                
//                                Logs.i("--------- sKsize " + sKsize);
                                Float iKsize = 0F;
                                try {
                                    iKsize = Float.parseFloat(sKsize);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
//                                Logs.i("--------- iKsize " + iKsize + " " + Config.image_filter_size);
                                if (iKsize != Config.image_filter_size) {
//                                    Logs.i("--------- iKsize != " + iKsize + " " + Config.image_filter_size);
                                    SharedPreferenceUtil.putValue(context, Constants.SharedPrefernce.SP_IMAGE_FILTER_SIZE, Constants.SharedPrefernce.KEY_IMAGE_FILTER_SIZE, iKsize.floatValue());
                                    Config.image_filter_size = iKsize;
                                }
                                refresh();
                            }
                        }).setNegativeButton(android.R.string.cancel,
                        new CancelClicker(this)).create();

        }
        return super.onCreateDialog(id);

    }

    protected void post_rename(FileInfo operatedfile, FileInfo target, Boolean result, CommonActivity commonActivity) {
        updatePostRename(result, target, operatedfile, commonActivity);
    }

    protected void mediaDel() {
        CommonActivity activity = OperationManager.operationMap.get(OperationManager.operationTaker);

        activity.status = OperationUtil.STATUS_BROWSE;
        String mime = null;
        String ext = null;
        Uri uri = null;
        for (FileInfo info : OperationManager.operationTaker.getOperationTargets()) {
            ext = FileUtil.getRealExtension(info.getName());
            mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());


            if (!CountActivity.categoryActivity.navTool.isAtTop()) {
                CountActivity.categoryActivity.refresh();
            }
        }
    }

    public boolean isMediaNav() {
        if (this.navTool.navEntity.peek().producingSource.equals(Constants.CateContants.CATE_IMAGES)
                || this.navTool.navEntity.peek().producingSource.equals(Constants.CateContants.CATE_MUSIC)
                || this.navTool.navEntity.peek().producingSource.equals(Constants.CateContants.CATE_VIDEO)) {
            return true;
        }
        return false;
    }

    public boolean updatePostRename(Boolean result, FileInfo target, FileInfo operatedfile, CommonActivity commonActivity) throws NumberFormatException {
        if (result == null) {
            return true;
        }
        operatedfile.buildFile();
        DatasrcRename.invokeUpdate(target, operatedfile, commonActivity, operatedfile.getIsDir());
        ActivityPool.getInstance().refresh(commonActivity instanceof PathActivity ? null : commonActivity);
        return false;
    }
    TextView value_artist;
    TextView value_album;
    TextView key_artist;
    TextView key_album;
    TextView reportTitleView;

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        final EditText et;
        switch (id) {
            case DIALOG_NEW_FOLDER:
                if (this.navTool.navEntity.isEmpty()) {
                    return;
                }
                et = (EditText) dialog.findViewById(R.id.foldername);
                FileInfo fi = new FileInfo();
                fi.setName(this.getString(R.string.menu_new_folder));
                List<FileInfo> names = new ArrayList<FileInfo>();
                StrPrefixFilter fileFetcher = new StrPrefixFilter(new Integer[]{TypeFilter.FILTER_RETAIN_DIRECTORY, Config.getHiddenOption(Config.SDCARD_HIDE_OPTION)}, Constants.HIDDEN_EXCLUDED, Constants.HIDDEN_INCLUDED, names, fi.getName());
                ((FileInfo) this.navTool.navEntity.peek().producingSource).getFile().listFiles(fileFetcher);
                String showText;
                if (names.size() == 0) {
                    showText = fi.getName();
                } else {
                    showText = fi.getName() + " (" + (this.operationUtil.tackleNewFolderText(names, fi.getName()) + 1) + ")";
                }
                et.setText(showText);
                et.setSelection(0, et.getText().length());
                InputmethodUtil.setupInputWindow(true, context);
                break;
            case DIALOG_CLEAN_DIR:
                showReport(dialog, getString(R.string.cleancount, EmptyDirectoryCleaner.getCleanedCount()), EmptyDirectoryCleaner.getCleanedCount(), EmptyDirectoryCleaner.cleanedEntities);
                break;
            case DIALOG_DELETE:
                ((AlertDialog) dialog).setTitle(getString(R.string.really_delete,
                        operatedfile.getName()));
                break;
            case DIALOG_RENAME:
                ((TextView) dialog.findViewById(R.id.foldernametext)).setText(operatedfile.isDir ? R.string.folder_name : R.string.file_name);
                et = (EditText) dialog.findViewById(R.id.foldername);
                String name = operatedfile.getName();
                et.setText(name);
                InputmethodUtil.setupInputWindow(true, this);
                break;
            case DIALOG_INSTALL_REPORT:
                String reportMessage = ReportOfInstall.messageStr;
                int reportCount = ReportOfInstall.failed.size();
                final List<ReportInfo> reportInfos = ReportOfInstall.failed;
                ListView lv = ((ListView) ((AlertDialog) dialog).findViewById(R.id.cleanedlist));
                if (reportTitleView == null) {
                    reportTitleView = new TextView(context);
                    int dip = 12;
                    if (DensityUtil.getDensity(context) == DensityUtil.WVGA) {
                        dip = 10;
                    } else if (DensityUtil.getDensity(context) == DensityUtil.HVGA) {
                        dip = 16;
                    }
                    reportTitleView.setTextSize(DensityUtil.dip2px(context, dip));
                    reportTitleView.setTextColor(Color.BLACK);
                    lv.addHeaderView(reportTitleView);
                }
                if (DensityUtil.getDensity(context) == DensityUtil.WVGA) {
                    ((TextView) ((AlertDialog) dialog).findViewById(R.id.cleancount)).setTextSize(DensityUtil.dip2px(context, 9));
                    if (!Config.isLewaRom) {
                        ((TextView) ((AlertDialog) dialog).findViewById(R.id.cleancount)).setTextColor(Color.WHITE);
                    }
                }
                reportTitleView.setText("    " + getString(R.string.directly_install) + ReportOfInstall.failureStr);
                Logs.i("reportCount ---" + reportCount);
                showReport(dialog, reportMessage, reportCount, reportInfos);
                break;
            case DIALOG_FILEINFO:
                int visible = 0;
                value_artist = (TextView) dialog.findViewById(R.id.value_artist);
                value_album = (TextView) dialog.findViewById(R.id.value_album);
                key_artist = (TextView) dialog.findViewById(R.id.text_artist);
                key_album = (TextView) dialog.findViewById(R.id.text_album);

                TextView value_name = (TextView) dialog.findViewById(R.id.value_name);
                TextView value_size = (TextView) dialog.findViewById(R.id.value_size);
                TextView value_location = (TextView) dialog.findViewById(R.id.value_location);
                TextView value_lastmodifiedtime = (TextView) dialog.findViewById(R.id.value_lastmodifiedtime);
                TextView value_readable = (TextView) dialog.findViewById(R.id.value_readable);
                TextView value_writable = (TextView) dialog.findViewById(R.id.value_writable);
                TextView value_hidden = (TextView) dialog.findViewById(R.id.value_hidden);
                if (!Config.isLewaRom) {
                    value_artist.setTextColor(Color.WHITE);
                    value_album.setTextColor(Color.WHITE);
                    key_artist.setTextColor(Color.WHITE);
                    key_album.setTextColor(Color.WHITE);
                    value_name.setTextColor(Color.WHITE);
                    value_size.setTextColor(Color.WHITE);
                    value_location.setTextColor(Color.WHITE);
                    value_lastmodifiedtime.setTextColor(Color.WHITE);
                    value_readable.setTextColor(Color.WHITE);
                    value_writable.setTextColor(Color.WHITE);
                    value_hidden.setTextColor(Color.WHITE);

                    ((TextView) dialog.findViewById(R.id.text_name)).setTextColor(Color.WHITE);
                    ((TextView) dialog.findViewById(R.id.text_size)).setTextColor(Color.WHITE);
                    ((TextView) dialog.findViewById(R.id.text_location)).setTextColor(Color.WHITE);
                    ((TextView) dialog.findViewById(R.id.text_lastmodifiedtime)).setTextColor(Color.WHITE);
                    ((TextView) dialog.findViewById(R.id.text_readable)).setTextColor(Color.WHITE);
                    ((TextView) dialog.findViewById(R.id.text_writable)).setTextColor(Color.WHITE);
                    ((TextView) dialog.findViewById(R.id.text_hidden)).setTextColor(Color.WHITE);
                }

                File file = new File(operatedfile.getPath());
                if (file.isDirectory()) {
                    value_size.setVisibility(View.GONE);
                    dialog.findViewById(R.id.text_size).setVisibility(View.GONE);
                    visible = View.GONE;
                } else {
                    dialog.findViewById(R.id.text_size).setVisibility(View.VISIBLE);
                    value_size.setVisibility(View.VISIBLE);
                    value_size.setText(operatedfile.getSizeText().trim());
                }
                prepareMusicInfo(visible, operatedfile);

                value_name.setText(operatedfile.getName());
                value_location.setText(FileUtil.getParent(operatedfile.getPath()));
                value_lastmodifiedtime.setText(operatedfile.getLastModified());
                value_readable.setText(file.canRead() ? R.string.yes : R.string.no);
                value_writable.setText(file.canWrite() ? R.string.yes : R.string.no);
                value_hidden.setText(file.isHidden() ? R.string.yes : R.string.no);
                break;
            case DIALOG_MY_PIC_SIZE:
                final EditText mFilterSize = ((EditText) dialog.findViewById(R.id.set_size));
                mFilterSize.addTextChangedListener(new TextWatcher() {
                    private CharSequence temp;
                    private int selectionStart;
                    private int selectionEnd;

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                         temp = s;
                    }

                    @Override       
                    public void afterTextChanged(Editable s) {
                        selectionStart = mFilterSize.getSelectionStart();
                        selectionEnd = mFilterSize.getSelectionEnd();
                        if (temp.length() > max_pic_char_num) {
                        	if(selectionStart - (temp.length() - max_pic_char_num) >= 0){
                            s.delete(selectionStart - (temp.length() - max_pic_char_num), selectionEnd);
                        	}
                            int tempSelection = selectionEnd;
                            if(s.length() <= max_pic_char_num){
                            mFilterSize.setText(s);
                            }
                            mFilterSize.setSelection(tempSelection);//设置光标在最后
//                            Toast.makeText(mContext, mContext.getResources().getString(R.string.pic_exceed_max), Toast.LENGTH_SHORT).show();
                        }
                    }
           });
                Float ksize = (Float) SharedPreferenceUtil.getValue(context, Constants.SharedPrefernce.SP_IMAGE_FILTER_SIZE, Constants.SharedPrefernce.KEY_IMAGE_FILTER_SIZE, Float.class);
                mFilterSize.setText(ksize.toString());
                mFilterSize.selectAll();
                if (!Config.isLewaRom) {
                    ((TextView) dialog.findViewById(R.id.atleast)).setTextColor(Color.WHITE);
                    ((TextView) dialog.findViewById(R.id.upper)).setTextColor(Color.WHITE);
                }
//                InputmethodUtil.setupInputWindow(true, null, context);
                break;
        }
    }
    AdaptInfo reportAdaptInfo;

    public AdaptInfo getCleanAdaptInfo() {
        if (reportAdaptInfo == null) {
            reportAdaptInfo = new AdaptInfo();
            reportAdaptInfo.objectFields = new String[]{ReportInfo.FIRSTLINE_NAME, ReportInfo.SECONDLINE_DETAIL};
            reportAdaptInfo.listviewItemData = new FileItemSrc();
            reportAdaptInfo.viewIds = new int[]{R.id.cleanname, R.id.cleanpath};
            reportAdaptInfo.listviewItemLayoutId = R.layout.cleanitem;
        }
        return reportAdaptInfo;
    }

    protected void createNewFolder(String foldername) {

        if (!TextUtils.isEmpty(foldername)) {
            foldername = foldername.trim();
            if (this.navTool.navEntity.isEmpty()) {
                return;
            }
            File file = FileUtil.getFile(
                    ((FileInfo) (this.navTool.navEntity.peek().producingSource)).getFile(),
                    foldername);
            if (file.mkdirs()) {
                refresh();
            }
        }
    }

    public void clearUISelect() {// and remove checked options
        if (this.listview.getAdapter() != null) {
            ((ThumbnailAdapter) this.listview.getAdapter()).clearSelectOption();
            this.listview.requestLayout();
            OperationManager.operationTaker.dataClear();
            operationUtil.dataClear();
        }
    }

    protected abstract void cutOrCopy(int command);

    protected void paste() {
        File file = ((FileInfo) this.navTool.navEntity.peek().producingSource).getFile();
        Logs.i("--------target " + file.getAbsolutePath());
        if (isChildRelation(file, operationUtil.initPathList(operationUtil.selectedEntities))) {
            showPasteToolBar(View.GONE);
            int operId = -1;
            if (OperationUtil.getOperType() == OperationUtil.OPER_TYPE_CUT) {
                operId = R.string.cut;
            } else if (OperationUtil.getOperType() == OperationUtil.OPER_TYPE_COPY) {
                operId = R.string.copy;
            }
            operationUtil.duplicated.clear();
            operationUtil.dataClear();
            makeSeenToAll(true);
            // FileActivity.fileActivity.isInOperation = -1;
            Toast.makeText(
                    this,
                    this.getString(R.string.child_invalidoperation,
                    this.getString(operId)), Toast.LENGTH_LONG).show();
            return;
        }
        String path = FileUtil.getPathWithoutFilename(file).getAbsolutePath();
        operationUtil.setDestination(path);
        operationUtil.invokeOperation(this);
        showPasteToolBar(View.GONE);
        SearchActivity.markfromPathActivity = false;
    }

    public boolean isChildRelation(File to, List<String> from) {
        if (to.getAbsolutePath().equals(NavigationConstants.SDCARD_PATH)) {
            return false;
        }
        File varto = to;
        while (true) {
            if (from.contains(varto.getParentFile().getAbsolutePath())
                    || from.contains(varto.getAbsolutePath())) {
                return true;
            }
            varto = varto.getParentFile();
            String parentPath = varto.getAbsolutePath();
            if (parentPath.equals(NavigationConstants.SDCARD_PATH)) {
                break;
            }

        }
        return false;
    }

    protected void showPasteToolBar(int visibleOption) {
        // TODO Auto-generated method stub
        this.pasteToolbar.setVisibility(visibleOption);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Logs.i("-----------KEYCODE_HOME " + (keyCode == KeyEvent.KEYCODE_HOME));
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("isNew", true);
            startActivity(intent);
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {

            if (RuntimeArg.isInInstall) {
                FileActivity.slideActivity.endJob();
            }
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addCategory(Intent.CATEGORY_HOME);
            this.startActivity(intent);
            return super.onKeyDown(keyCode, event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (this.action_flag != null && this.navTool.isAtTop()) {
                ActivityPool.getInstance().exit();
                return true;
            } else if (this.pasteToolbar != null
                    && this.pasteToolbar.getVisibility() == View.VISIBLE) {
                if (this.navTool.isAtTop()) {
                    pasteCancel();
                    return true;
                } else {
                    this.backward();
                    return true;
                }
            } else if (bottomToolbar != null
                    && bottomToolbar.getVisibility() == View.VISIBLE) {

                this.makeSelectAll(false, true);
                this.showBottomBar(View.GONE);
                this.listview.requestLayout();
                OperationUtil.mOperationState = OperationUtil.STATUS_BROWSE;
                return true;
            } else if (installDialog != null && installDialog.isShowing()) {
                doAfterInstall();
                return true;
            } else {
                if (this.getClass() == SearchActivity.class) {
                    finish();
                    return true;
                }
                if (StatusCheckUtil.isSDCardAvailable()) {
                    if (backward()) {
                        return true;
                    }
                }
            }
            ActivityPool.getInstance().exit();
            return true;
        } else {
            return false;
        }
    }

    protected boolean backward() {

        if (!this.navTool.isAtTop()) {
            this.navTool.navEntity.pop();

            sdcardPathBtn.setText(this.navTool.navEntity.peek().displayname.replaceAll("/", " / "));
            this.access(this.navTool.navEntity.peek(), this.navTool.navEntity.peek().defaultPosition, true,
                    MODE_HISTORY, operationUtil.mOperationState);
            return true;
        } else {
            return false;
        }

    }

    public void refresh(boolean bOption) {
        if (!bOption) {
            return;
        }
        refresh();
    }

    public void refresh() {
        if (StatusCheckUtil.srcardStateResolve(true, this)) {
            return;
        }
        PathActivity.activityInstance.firstTimeStartup();
        if (!this.navTool.navEntity.isEmpty()) {
            this.access(this.navTool.navEntity.peek(), -1, true, MODE_CURR,
                    operationUtil.mOperationState);
            reFreshCheckboxVisible();
            return;
        }
    }

    protected void clearSelectWithUIOption(int hideOption) {
        this.clearUISelect();
        if (hideOption == OPTION_UI_BOTTOM_BAR) {
            if (this.bottomToolbar.isShown()) {
                this.showBottomBar(View.GONE);
            }
        }
    }

    public void selectAllOrNot() {
        boolean selectAll = adapter.isSelectAll();
        Logs.i("", "selectOption ====== " + selectAll);
        makeSelectAll(!selectAll, true);
        this.selectTextImageChange();
        if (this.listview.getAdapter() != null) {
            if (selectAll) {
                this.showBottomBar(View.GONE);
                OperationUtil.mOperationState = OperationUtil.STATUS_BROWSE;
            }
        }
    }

    public void selectTextImageChange() {
        boolean selectAll = adapter.isSelectAll();
        TextView tv = ((TextView) ViewUtil.findViewById(bottomToolbar, R.id.toolbar_select_text));
        if (selectAll) {
            ((ImageView) this.toolbar_selectAll.findViewById(R.id.toolbar_select_image)).setImageResource(R.drawable.lewa_ic_folder_cancel);
            tv.setText(R.string.cancel);
        } else {
            ((ImageView) this.toolbar_selectAll.findViewById(R.id.toolbar_select_image)).setImageResource(R.drawable.lewa_ic_folder_all);
            tv.setText(R.string.select_all);
        }
        bottomToolbar.requestLayout();
    }

    public void makeSelectAll(boolean select, boolean dataSychronized) {
        makeSelectAll(select, dataSychronized, null, null);
    }

    public void makeSelectAll(boolean select, boolean dataSychronized, CheckBox excludeView, Integer pos) {
        selectwhole = true;
        for (int i = 0; i < this.listview.getChildCount(); i++) {
            CheckBox cb = (CheckBox) ViewUtil.findViewById(
                    listview.getChildAt(i), R.id.handleCheckbox);
            cb.setChecked(select);
            if (excludeView != null) {
                excludeView.setChecked(!select);
            }
        }
        if (this.listview.getAdapter() != null) {
            ((ThumbnailAdapter) this.listview.getAdapter()).selectAll(select);
        }
        if (dataSychronized) {
            addAllItemToOperationUtil(select);
        }
        selectwhole = false;
    }

    protected void addAllItemToOperationUtil(boolean addOrRemove) {
        operationUtil.dataClear();
        if (!addOrRemove) {
            return;
        }
        FileInfo item = null;
        if (this.listview.getAdapter() != null) {
            Logs.i("", "=========<> " + adapter.getCount());
            for (int i = 0; i < adapter.getCount(); i++) {
                try {
                    item = (FileInfo) ((FileAdapter) adapter).getSimpleFileItem(i);
                } catch (Exception e) {
                    e.printStackTrace();
                   
                }
                
                if (addOrRemove) {
                    operationUtil.addOperationTarget(item);
                }
            }
        }
    }

    public void makeSeenToAll(boolean visible) {
        if (adapter != null) {
            adapter.markVisible(visible);
            for (int i = 0; i < this.listview.getChildCount(); i++) {
                CheckBox cb = (CheckBox) ViewUtil.findViewById(
                        listview.getChildAt(i), R.id.handleCheckbox);
                if (cb != null) {
                    cb.setVisibility(visible ? View.VISIBLE : View.GONE);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    public boolean isInListDetail() {
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_cancel:
                send();
                break;
            case R.id.toolbar_select:
                this.selectAllOrNot();
                listview.requestLayout();
                break;
            case R.id.toolbar_del:
                OperationManager.operationTaker = operationUtil;
                StatusCheckUtil.setCommonActivity(this);
                operationUtil.invokeOperation(
                        OperationUtil.OPER_TYPE_DEL, this);
                operationUtil.mOperationState = OperationUtil.STATUS_BROWSE;
                break;
            case R.id.toolbar_cut:
                OperationManager.operationTaker = operationUtil;
                cutOrCopy(OperationUtil.OPER_TYPE_CUT);
                break;
            case R.id.toolbar_copy:
                OperationManager.operationTaker = operationUtil;
                cutOrCopy(OperationUtil.OPER_TYPE_COPY);
                break;
            case R.id.toolbar_paste:
                StatusCheckUtil.setCommonActivity(this);
                paste();
                mStateCheckBoxVisible = true;
                break;
            case R.id.pastebar_cancel:
                pasteCancel();
                break;
            case R.id.prefer_memory:
                PmCommand.installLocation = PmCommand.InstallLocation_Internal;
                doInstall();
                break;
            case R.id.prefer_sdcard:
                PmCommand.installLocation = PmCommand.InstallLocation_External;
                doInstall();
                break;
            case R.id.prefer_system:
                PmCommand.installLocation = PmCommand.InstallLocation_System_Decide;
                doInstall();
                break;
        }
    }

    public abstract void sortRefresh();

    public String prepareMime() {
        return "*/*";
    }

    protected void setEmptyViewText(String emptytext) {
        ((TextView) this.listview.getEmptyView().findViewById(R.id.nofile_text)).setText(emptytext);
    }

    protected abstract String getEmptyText();

    protected abstract void swithToPrivacyActivity();

    private void initDialogs() {
        if (renameWaitDialog == null) {
            renameWaitDialog = new ProgressDialog(this);
            renameWaitDialog.setMessage(context.getString(R.string.renaming));
            renameWaitDialog.setCancelable(false);
            renameWaitDialog.setIndeterminate(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (renameWaitDialog != null) {
            renameWaitDialog.dismiss();
            renameWaitDialog = null;
        }
        if (OperationUtil.operatingDialog != null) {
            OperationUtil.operatingDialog.dismiss();
            OperationUtil.operatingDialog = null;
        }

    }
}
