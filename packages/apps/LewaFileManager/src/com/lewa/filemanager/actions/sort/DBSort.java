/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.actions.sort;

import android.provider.MediaStore;
import com.lewa.filemanager.config.Config;

/**
 *
 * @author Administrator
 */
public class DBSort implements Sort {

    public int policy = -1;
    public int seq = -1;
    public String strSeq;
    public String strPolicy;
    public static String ORDERBY = " order by ";

    public DBSort(int policy, int seq) {
        this.policy = policy;
        this.seq = seq;
        this.setPolicy(policy);
        this.setSeq(seq);
    }

    public int getPolicy() {
        return policy;
    }

    public int getSeq() {
        return seq;
    }

    public Object getSeqContent() {
        return " " + strSeq + " ";
    }

    public Object getPolicyContent() {
        return " " + strPolicy + " ";
    }

    public Object getContent() {
        return getPolicyContent().toString() + getSeqContent().toString();
    }

    public Sort transfer(String mimeprefix) {
        return this;
    }

    public void setPolicy(int value) {
        switch (value) {
            case Sort.SORT_POLICY_TITLE:
                strPolicy = "upper(" + MediaStore.Audio.Media.TITLE + ")";
                break;
            case Sort.SORT_POLICY_SIZE:
                strPolicy = MediaStore.Audio.Media.SIZE;
                break;
            case Sort.SORT_POLICY_LAST_MODIFIED_TIME:
                strPolicy = MediaStore.Audio.Media.DATE_MODIFIED;
                break;
            case Sort.SORT_POLICY_TYPE:
                strPolicy = MediaStore.Audio.Media.MIME_TYPE;
                break;
            case Sort.SORT_POLICY_ALBUM:
                strPolicy = " album ";
                break;
            case Sort.SORT_POLICY_ARTIST:
                strPolicy = Config.isLewaRom ? " artist_sort_key " : " artist ";
                break;
            case Sort.SORT_POLICY_DISPLAYNAME:
                strPolicy = "upper(" + MediaStore.Audio.Media.DISPLAY_NAME + ")";
                break;
        }
        policy = value;
    }

    public void setSeq(int value) {
        switch (value) {
            case Sort.SORT_SEQ_ASC:
                strSeq = "asc";
                break;
            case Sort.SORT_SEQ_DES:
                strSeq = "desc";
                break;
        }
        seq = value;
    }
}
