package com.lewa.search.util;

import android.os.Environment;
import android.provider.MediaStore.Images.Media;

/**
 * This class generates sql clauses for searching database. This class has the
 * potential to expand.
 * 
 * @author wangfan
 * @version 2012.07.04
 */

public class SearchUtil {

    // search mode in building search clauses
    public static final int SEARCH_MODE_BLURRED = 0; // where column like %key%
    public static final int SEARCH_MODE_CLEAR = 1; // where column = key
    public static final int SEARCH_MODE_BLURRED_ONLYPRE = 2; // where column
                                                             // like %key
    public static final int SEARCH_MODE_BLURRED_ONLYSUF = 3; // where column
                                                             // like key%
    public static final int SEARCH_MODE_BLURRED_SPECPRE = 4; // where column
                                                             // like %spec +
                                                             // key% spec is a
                                                             // certain string
    public static final int SEARCH_MODE_BLURRED_SPECSUF = 5; // where column
                                                             // like %key +
                                                             // spec%

    // these tags record which column in selection array has clause
    public static final int PROJECTION_ZERO = 0;
    public static final int PROJECTION_ONE = 1;
    public static final int PROJECTION_TWO = 2;
    public static final int PROJECTION_THREE = 3;
    public static final int PROJECTION_FOUR = 4;

    /**
     * This method helps to build selection array.
     * 
     * @param projection
     *            columns willing to get from database
     * @param ids
     *            the id of columns has clauses
     * @param combineMethod
     *            method of combining two clauses:"and" or "or"
     * @param searchMode
     *            search mode defined above
     */
    public static String getSelection(String[] projection, int[] ids,
            String conbineMethod, int searchMode) {
        String selection = "";
        int coloumId;
        String coloum;

        // build match method by search mode
        String matchMethod = searchMode == SEARCH_MODE_BLURRED ? " like ? "
                : " = ? ";

        // build a selection each item, and combine them with "and" or "or"
        for (int i = 0; i < ids.length; i++) {
            coloumId = ids[i];
            coloum = projection[coloumId];

            // build a selection
            selection += " " + coloum + matchMethod;

            // add a connection tag for each selection except the last one
            if (i != ids.length - 1) {
                selection += conbineMethod;
            }
        }

        return selection;
    }

    /**
     * This method helps to build selection argument. This method builds
     * selection arguments with the same key and the same matchMethod.
     * 
     * @param key
     *            use key to build selection argument
     * @param selectionLen
     *            length of selection array
     * @param matchMethod
     *            search mode defined above
     */
    public static String[] getMultipleSelectionArgs(String key,
            int selectionLen, int matchMethod) {
        String[] selectionArgs = new String[selectionLen];
        if (matchMethod == SEARCH_MODE_BLURRED) {
            // blur match, use "like" to match each selection
            for (int i = 0; i < selectionLen; i++) {
                selectionArgs[i] = "%" + key + "%";
            }
        }

        else {
            // clear match, user "=" to match each selection
            for (int i = 0; i < selectionLen; i++) {
                selectionArgs[i] = key;
            }
        }

        return selectionArgs;
    }

    /**
     * This method helps to build selection argument. This method builds
     * selection arguments with the same key but different matchMethods.
     * 
     * @param key
     *            use key to build selection argument
     * @param selectionLen
     *            length of selection array
     * @param matchMethod
     *            [] an array of search modes
     * @param specStr
     *            combine with key to assemble selection argument
     */
    public static String[] getDifferentSelectionArgs(String key,
            int selectionLen, int[] matchMethods, String specStr) {
        String[] selectionArgs = new String[selectionLen];

        for (int i = 0; i < selectionLen; i++) {
            if (matchMethods[i] == SEARCH_MODE_BLURRED_ONLYPRE) {
                selectionArgs[i] = "%" + key;
            } else if (matchMethods[i] == SEARCH_MODE_BLURRED_ONLYSUF) {
                selectionArgs[i] = key + "%";
            } else if (matchMethods[i] == SEARCH_MODE_BLURRED) {
                selectionArgs[i] = "%" + key + "%";
            } else if (matchMethods[i] == SEARCH_MODE_BLURRED_SPECPRE) {
                selectionArgs[i] = "%" + specStr + key + "%";
            } else if (matchMethods[i] == SEARCH_MODE_BLURRED_SPECSUF) {
                selectionArgs[i] = "%" + key + specStr + "%";
            } else {
                selectionArgs[i] = key;
            }

        }
        return selectionArgs;
    }

    public static boolean isSDCardEnable() {

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_SHARED)
                || Environment.getExternalStorageState().equals(
                        Environment.MEDIA_CHECKING)) {
            return true;
        }
        return false;
    }

}
