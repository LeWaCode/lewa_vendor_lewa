/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.actions.sort;

/**
 *
 * @author Administrator
 */
public interface Sort {

    public static final int SORT_POLICY_TITLE = 0;
    public static final int SORT_POLICY_LAST_MODIFIED_TIME = 1;
    public static final int SORT_POLICY_SIZE = 2;
    public static final int SORT_POLICY_TYPE = 3;
    public static final int SORT_POLICY_DISPLAYNAME = 4;
    public static final int SORT_POLICY_ALBUM = 5;
    public static final int SORT_POLICY_ARTIST = 6;
    public static final int SORT_SEQ_ASC = 0;
    public static final int SORT_SEQ_DES = 1;

    public int getPolicy();

    public int getSeq();

    public void setPolicy(int value);

    public void setSeq(int value);

    public Object getContent();

    public Sort transfer(String mimeprefix);
}
