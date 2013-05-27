package com.lewa.filemanager.actions.sort;

import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.Logs;
import java.util.Comparator;
import java.util.Date;

public class ObjectSort implements Comparator<FileInfo>, Sort {

    public int policy = -1;
    public int seq = -1;

    public ObjectSort(int policy, int seq) {
        super();
        this.policy = policy;
        this.seq = seq;
    }

    public void initFile(FileInfo one) throws NumberFormatException {
        // TODO Auto-generated method stub
        if (!one.isFileBuilt) {
            one.buildFile();
        }
    }

    public void setPolicy(int policy) {
        if (policy == -1) {
            return;
        }
        Logs.i("---- policy " + policy);
        this.policy = policy;
    }

    public int getPolicy() {
        return policy;
    }

    @Override
    public int compare(FileInfo one, FileInfo another) {
        int result = 0;
        initFile(one);
        initFile(another);
        if (one.getIsDir() && !another.getIsDir()) {
            return -1;
        } else if (!one.getIsDir() && another.getIsDir()) {
            return 1;
        } else {
            switch (this.policy) {
                case Sort.SORT_POLICY_SIZE:

                    result = (int) (another.getLeng() - one.getLeng());
                    break;
                case Sort.SORT_POLICY_LAST_MODIFIED_TIME:
                    if (one.unformattedDate == null) {
                        one.unformattedDate = new Date(one.getLastModifiedInt());
                    }
                    if (another.unformattedDate == null) {
                        another.unformattedDate = new Date(another.getLastModifiedInt());
                    }
                    if (one.unformattedDate.after(another.unformattedDate)) {
                        result = -1;
                    } else if (one.unformattedDate.before(another.unformattedDate)) {
                        result = 1;
                    } else if (one.unformattedDate.equals(another.unformattedDate)) {
                        result = 0;
                    }
                    break;
                case Sort.SORT_POLICY_TITLE:
                    another.buildName();
                    one.buildName();
                    result = another.getName().compareToIgnoreCase(one.getName());
                    break;
                case Sort.SORT_POLICY_TYPE:
                    result = another.getType().compareToIgnoreCase(one.getType());
                    break;
            }
            result = seq == Sort.SORT_SEQ_DES ? result : -result;
            return result;
        }
    }

    public int getSeq() {
        return this.seq;
    }

    public void setSeq(int value) {
        if (value == -1) {
            return;
        }
        this.seq = value;
    }

    public Object getContent() {
        return this;
    }

    public Sort transfer(String mimeprefix) {
        if (mimeprefix == null || mimeprefix.startsWith(Constants.CateContants.CATE_IMAGES)
                || mimeprefix.startsWith(Constants.CateContants.CATE_MUSIC)
                || mimeprefix.startsWith(Constants.CateContants.CATE_VIDEO)
                || mimeprefix.startsWith(Constants.CateContants.CATE_PACKAGE)
                || mimeprefix.startsWith(Constants.CateContants.CATE_DOCS)
                || mimeprefix.startsWith(Constants.CateContants.CATE_THEME)) {
            return new DBSort(policy, seq);
        }
        return this;
    }
}
