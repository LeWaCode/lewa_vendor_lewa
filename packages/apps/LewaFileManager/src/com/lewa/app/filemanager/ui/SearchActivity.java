/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.app.filemanager.ui;

import android.os.Message;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.actions.OperationUtil;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.actions.EqualCheck;
import com.lewa.filemanager.actions.sort.ObjectSort;
import com.lewa.filemanager.ds.uri.NavigationNode;
import com.lewa.filemanager.cpnt.adapter.CountAdapter;
import com.lewa.filemanager.activities.views.SearchListViewHolder;
import com.lewa.filemanager.activities.views.ViewHolder;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.Logs;
import com.lewa.filemanager.util.StatusCheckUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author chenliang
 */
public class SearchActivity extends FileCursorActivity {

    List<QueryThread> queue = new ArrayList<QueryThread>(0);
    public static String staticKeyWords = "";
    public static boolean markfromPathActivity = false;
    boolean isNew;
    public String objectWords = "";

    @Override
    public void startup() {
    }

    @Override
    protected void setHiddenFileShowFlag() {
    }

    @Override
    public void handlerRead(Object previousSource, boolean startRoot) {
    }

    @Override
    public ViewHolder newViewHolderInstance() {
        return new SearchListViewHolder(list, this);
    }

    private Handler searchHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            showBottomBar(View.GONE);
            switch (msg.what) {
                case Constants.OperationContants.SEARCHNOTIFY:
                    Logs.i("rebind --");
                    viewHolder.rebind();
                    break;
                case Constants.OperationContants.SEARCH_NOTIFY_EMPTY:
                    if (adapter != null) {
                        listview.setEmptyView(getEmptyView());
                        adapter.getItemDataSrc().clear();
                        ((CountAdapter) adapter).items.clear();
                        adapter.notifyDataSetChanged();
                        listview.invalidate();
                    }
                    break;
            }
        }
    };
    QueryThread quertThread;
    QueryThread currThread;

    class QueryThread extends Thread {

        public boolean runned;

        public QueryThread(String inputkeyWords) {
            this.inputkeyWords = inputkeyWords;
        }

        public String getInputkeyWords() {
            return inputkeyWords;
        }

        public boolean isRunned() {
            return runned;
        }
        String inputkeyWords;

        @Override
        public void run() {
            try {
                Logs.i("inputkeyWords --" + inputkeyWords);
                if (inputkeyWords.trim().equals("")) {
                    searchHandler.sendEmptyMessage(Constants.OperationContants.SEARCH_NOTIFY_EMPTY);
                } else {
                    Logs.i("dataChanged -- b");
                    viewHolder.dataChanged();
                    Logs.i("dataChanged -- a");
                    searchHandler.sendEmptyMessage(Constants.OperationContants.SEARCHNOTIFY);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            runned = true;
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }

    public void baseKeywordSearch(String inputkeyWords) {
        queue.add(new QueryThread(inputkeyWords));
        QueryThread qthread = queue.get(queue.size() - 1);
        if (currThread != qthread) {
            if (currThread != null) {
                currThread.interrupt();
            }
            if (!qthread.isRunned()) {
                currThread = qthread;
                qthread.start();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (StatusCheckUtil.srcardStateResolve(true, this)) {
            contentView = StatusCheckUtil.no_file_layout;
            return;
        }
        if (listview != null && listview.getCount() > 0) {
            return;
        }
        viewHolder.start();
        if (markfromPathActivity) {
            markfromPathActivity = false;
            objectWords = staticKeyWords;
        }
        EditText text = ((EditText) this.findViewById(R.id.searchBox));
        if(!(text.getText().toString().trim().equals("") && objectWords.trim().equals(""))){
            text.setText(objectWords);
            baseKeywordSearch(objectWords);
        }else{
             findViewById(R.id.magnifying_glass_cancel).setVisibility(View.INVISIBLE);
        }
        
        ((ListView) this.findViewById(R.id.fileListView)).setEmptyView(null);
        View serchlayout = this.findViewById(R.id.searchBoxLayout);
        EditText searchBox = (EditText) this.findViewById(R.id.searchBox);
        View indicateBtn = this.findViewById(R.id.sdcardPathBtn);
        if (indicateBtn.getVisibility() == View.VISIBLE && serchlayout.getVisibility() == View.GONE) {
            indicateBtn.setVisibility(View.GONE);
            serchlayout.setVisibility(View.VISIBLE);
        }
        searchBox.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable editable) {
                Logs.i("---------afterTextChanged" + editable.length());
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void onTextChanged(CharSequence str, int arg1, int arg2, int arg3) {
                Logs.i("---------onTextChanged" + str);
                staticKeyWords = objectWords = str.toString();
                baseKeywordSearch(objectWords);
                if (objectWords.trim().equals("")) {
                    findViewById(R.id.magnifying_glass_cancel).setVisibility(View.INVISIBLE);
                } else {
                    findViewById(R.id.magnifying_glass_cancel).setVisibility(View.VISIBLE);
                }

            }
        });
        this.findViewById(R.id.h_line).setVisibility(View.GONE);
        this.findViewById(R.id.magnifying_glass_cancel).setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                objectWords = "";
                ((EditText) findViewById(R.id.searchBox)).setText(objectWords);
            }
        });
    }

    @Override
    public void refresh() {
        if (StatusCheckUtil.srcardStateResolve(true, this)) {
            return;
        }
        baseKeywordSearch(objectWords);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void access(NavigationNode navig, int newPos, boolean notifyDataSetChanged, int mode, int status) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void access(NavigationNode navig, int newPos, boolean notifyDataSetChanged, int mode, int status, int accessFlag) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void setNavigationPosAndNode(int newPos, int mode, NavigationNode navSource) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getNavBarWholeShowText() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int[] getMenuIds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public View getEmptyView() {
        return null;
    }

    @Override
    protected boolean navigate(MenuItem item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void cutOrCopy(int command) {
        EqualCheck nc = new EqualCheck(ObjectSort.SORT_POLICY_TITLE, ObjectSort.SORT_SEQ_ASC);
        Collections.sort(OperationUtil.selectedEntities, nc);
        if (nc.isResult()) {
            Toast.makeText(context, R.string.same_name_files_operation_cancel, Toast.LENGTH_LONG).show();
            return;
        }
        SlideActivity.paramActivity.isInOperation = command;
        PathActivity.activityInstance.switchToCutCopy();
        SlideActivity.fileActivityInstance.setDisplayScreen(1);
        markfromPathActivity = true;
        finish();
    }

    @Override
    public void sortRefresh() {
    }

    @Override
    protected String getEmptyText() {
        return !markfromPathActivity ? "" : getString(R.string.cannt_find_matched_result);
    }

    @Override
    protected void swithToPrivacyActivity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void prepareMusicInfo(int visible, FileInfo info) {
    }
}
