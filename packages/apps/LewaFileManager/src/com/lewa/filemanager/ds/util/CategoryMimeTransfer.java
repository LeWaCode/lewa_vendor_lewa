/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.util;

import com.lewa.filemanager.config.Constants;

/**
 *
 * @author Administrator
 */
public class CategoryMimeTransfer {

    public static String transferMime(String mime) {
        if (mime.equals("application/vnd.android.package-archive")) {
            return Constants.CateContants.CATE_PACKAGE;
        } else if (mime.startsWith("application") || mime.startsWith("text")) {
            return Constants.CateContants.CATE_DOCS;
        } else {
            return mime.substring(0, mime.indexOf("/"));
        }
    }
}
