/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.actions.apk;

import com.lewa.base.NotifyUtil;
import com.lewa.app.filemanager.ui.SlideActivity;

/**
 *
 * @author chenliang
 */
public class NotifyHelper {

    public static void cancelApkNOtification() {
        NotifyUtil.cancel(SlideActivity.fileActivityInstance, NotifyUtil.ID_INSTALLED);
    }
}
