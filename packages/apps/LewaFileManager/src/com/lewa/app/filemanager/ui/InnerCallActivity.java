/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.app.filemanager.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.ds.database.MimeSrc;
import com.lewa.filemanager.ds.database.CategoryInfo;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.ds.uri.AccountNode;
import com.lewa.base.adapter.MapAdapter.ActionListener;
import com.lewa.filemanager.cpnt.adapter.ThumbnailAdapter;
import com.lewa.base.adapter.ListenerBox;
import com.lewa.filemanager.cpnt.adapter.SelectedIndicator;
import com.lewa.filemanager.activities.views.ViewUtil;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.Logs;
import com.lewa.filemanager.util.ActivityPool;
import com.lewa.filemanager.util.StatusCheckUtil;
import com.lewa.filemanager.util.StatusCheckUtil.MediaScannerBroadcast;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author chenliang
 */
public class InnerCallActivity extends CountActivity {

    private List<Integer> largeSizePics = new ArrayList<Integer>();
    private boolean isLargeSizeAdded;
    public View stub;
    public Button button;
    public Dialog dialog;
    public View feedback_confirm_dialog;
    public int pos;
    public static final String INTF_DATA_FOR_FEEDBACK = "PATHS";
    public static final int INTF_DATA_FOR_FEEDBACK_REQUEST_CODE = 0;
    private String mimetype;
    private static final String MIME_TYPE = "MIME_TYPE";
    private static final String SELEC_TMODE = "SELECT_MODE";
    public static final String SIZE_LIMIT = "SIZE_LIMIT";
    private static final String LIMIT_WORD = "LIMIT_WORD";
    private Boolean isChoiceModeMultiple;
    private String limitWord;
    public long pictureSize;
    private String callbackAction;
    public static final String COUNT_INVOKED = "com.lewa.filemgr.count_start";

    @Override
    public void startup() {
        if (StatusCheckUtil.srcardStateResolve(true, this)) {
            return;

        }
        context = this;
        Intent intent = this.getIntent();
        this.limitWord = intent.getStringExtra(LIMIT_WORD);
        this.isChoiceModeMultiple = intent.getBooleanExtra(SELEC_TMODE, false);
        this.pictureSize = intent.getLongExtra(SIZE_LIMIT, -1);
        this.mimetype = intent.getStringExtra(MIME_TYPE);
        this.callbackAction = intent.getStringExtra(Constants.InvokedPath.CALLBACK_ACTION);
//        Logs.i("", "param pass " + this.limitWord + " " + this.isChoiceModeMultiple + " " + this.pictureSize + " " + this.mimetype);
        CategoryInfo item;
        initDBData(mimetype);
        try {
            item = MimeSrc.getCategoryRepository().get(mimetype).categoryInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (pictureSize == -1) {
            if (limitWord == null) {
                throw new IllegalArgumentException("interface invalid invoked");
            } else {
                limitWord = "";
            }
        }
        String flag = item.categorySign;
        String displayname = item.displayName;
        navTool.navEntity.push(new AccountNode(flag, displayname));
        this.revisit();
        sdcardPathBtn.setOnClickListener(null);
        feedback_confirm_dialog = this.findViewById(R.layout.feedback_confirm_dialog);
        largeSizePics.removeAll(largeSizePics);
        dialog = new AlertDialog.Builder(this).setTitle(R.string.reminder).setMessage(limitWord).setIcon(R.drawable.feedback_gantan).setView(feedback_confirm_dialog).setPositiveButton(android.R.string.ok,
                new OnClickListener() {

                    public void onClick(DialogInterface dialog,
                            int which) {
                        sendBack();
                    }
                }).setNegativeButton(android.R.string.cancel,
                new OnClickListener() {

                    public void onClick(DialogInterface dialog,
                            int which) {
                        // Cancel should not do anything.
                    }
                }).create();
        button = (Button) this.findViewById(R.id.sure);
        button.setOnClickListener(new android.view.View.OnClickListener() {

            public void onClick(View arg0) {

                if (!largeSizePics.isEmpty()) {
                    dialog.show();
                } else {
                    sendBack();
                }

            }
        });
    }

    public void sendBack() {
        Intent intent = new Intent(callbackAction);
        intent.putStringArrayListExtra(INTF_DATA_FOR_FEEDBACK, toPathsInfoUtil(operationUtil.getOperationTargets()));
        sendBroadcast(intent);
        ActivityPool.getInstance().exit();
        
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (StatusCheckUtil.broadcastRec == null) {
            StatusCheckUtil.broadcastRec = new MediaScannerBroadcast();
            Logs.i("-------------------- broadcastRec");
            StatusCheckUtil.registerSDcardIntentListener(this, StatusCheckUtil.broadcastRec);
        }
    }

    public ArrayList<String> toPathsInfoUtil(List<FileInfo> infoes) {
        ArrayList<String> paths = new ArrayList<String>(0);
        for (FileInfo fi : infoes) {
            paths.add(fi.getPath());
        }
        return paths;
    }

    @Override
    protected boolean backward() {
        return false;
    }

    @Override
    public View buildList() {
        return LayoutInflater.from(this).inflate(R.layout.sdcardui, null, true);
    }

    @Override
    public ActionListener[] getViewHandlers() {
        return new ThumbnailAdapter.ActionListener[]{
                    new ThumbnailAdapter.ActionListener(R.id.handleCheckbox, ThumbnailAdapter.ActionListener.OnCheckChanged) {

                        CheckBox checkbox;

                        @Override
                        public void handle(View view, ListenerBox listener) {
                            // TODO Auto-generated method stub
                            Logs.i("", "---------->---");
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
                            if (!isChoiceModeMultiple) {
                                checkbox = (CheckBox) view;

                                if (!((CheckBox) view).isChecked()) {
                                    checkbox = null;
                                }
                                makeSelectAll(false, true, checkbox, pos);
                            }
                            FileInfo fi = checkboxHandle(listener, view);
                            if (fi == null) {
                                return;
                            }
                            findViewById(R.id.feedback_sure).setVisibility(adapter.hasSelected() ? View.VISIBLE : View.GONE);
                            button.setText(getString(R.string.sure) + (adapter.hasSelected() ? " ( " + adapter.getTotalSelection() + " ) " : ""));
                            if (pictureSize == -1) {
                                return;
                            }
                            if (fi == null) {
                                return;
                            }
                            if (fi.getLeng() > pictureSize) {
                                if (((CheckBox) view).isChecked()) {
                                    if (largeSizePics.lastIndexOf(pos) == -1) {
                                        largeSizePics.add(pos);
                                    }
                                } else {
                                    if (largeSizePics.lastIndexOf(pos) != -1) {
                                        largeSizePics.remove(largeSizePics.lastIndexOf(pos));
                                    }
                                }
                            }
                        }
                    }
                };
    }

    public FileInfo checkboxHandle(ListenerBox listener, View view) {
        // TODO Auto-generated method stub
        ThumbnailAdapter baseAdapter = (ThumbnailAdapter) listener.getBaseAdapter();
        View v = ViewUtil.findListViewByItemView(view);
        if (v == null) {
            return null;
        }
        ListView listview = (ListView) v.getParent();
        Integer pos = baseAdapter.getViewContentMap().get(v);
        if (pos == null) {
            return null;
        }
        FileInfo fileInfo = (FileInfo) baseAdapter.getItem(pos);
        boolean isChecked = ((CheckBox) view).isChecked();
        fileInfo.isUISelected = isChecked;
        baseAdapter.addSelected(pos, isChecked);
        if (baseAdapter.isSelected()) {
            hasSelection = false;
        } else {
            if (!hasSelection) {
                SelectedIndicator.putSelected(context);
                hasSelection = true;
                SelectedIndicator.clearOthersSelectionState(context);
            }
        }
        if (isChecked) {
            operationUtil.addOperationTarget(fileInfo);
        } else {
            operationUtil.removeOperationTarget(fileInfo);
        }
        return fileInfo;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
