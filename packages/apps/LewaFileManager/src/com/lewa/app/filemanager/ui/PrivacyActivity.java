/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.app.filemanager.ui;

import android.os.Bundle;
import android.view.View;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.actions.OperationManager;
import com.lewa.filemanager.actions.OperationUtil;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.beans.FileUtil;
import com.lewa.filemanager.funcgroup.privacy.PrivacyInfo;
import com.lewa.filemanager.ds.uri.NavigationNode;
import com.lewa.filemanager.ds.uri.PrivacyNode;
import com.lewa.filemanager.config.Constants;
import java.io.File;

/**
 *
 * @author Administrator
 */
public class PrivacyActivity extends PathActivity {

    public static PrivacyActivity activityInstance;
    View layout_privacy;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void startup() {
        this.turnToFileBrowsing(getViewHandlers(), getOnClickListener(),
                new PrivacyNode(new FileInfo(new File(Constants.PRIVACY_HOME),this)));
        activityInstance = activityInstance != null ? activityInstance : this;
        layout_privacy = findViewById(R.id.layout_privacy);
        layout_privacy.findViewById(R.id.privacy_move).setOnClickListener(this);
        layout_privacy.findViewById(R.id.privacybar_cancel).setOnClickListener(this);
    }

    @Override
    public Class<? extends NavigationNode> getNavNodeClazz() {
        return PrivacyNode.class;
    }

    @Override
    public Class<? extends FileInfo> getReadInfoClass() {
        return PrivacyInfo.class;
    }

    public void switchToMove() {
        this.layout_privacy.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.privacy_move:
                File file = ((FileInfo) this.navTool.navEntity.peek().producingSource).getFile();
                file.mkdirs();
                String path = FileUtil.getPathWithoutFilename(file).getAbsolutePath();
                operationUtil.setDestination(path);
                operationUtil.invokeOperation(OperationUtil.OPER_TYPE_MOVE_PRIVACY, this);
                break;
            case R.id.privacybar_cancel:
                this.layout_privacy.setVisibility(View.GONE);
                makeSeenToAll(true);
                OperationManager.operationMap.get(OperationManager.operationTaker).makeSelectAll(false, true);
                operationUtil.setCommand(-1);
                operationUtil.dataClear();
                mState = OperationUtil.STATUS_BROWSE;
                mStateCheckBoxVisible = true;
                break;
        }
    }
}
