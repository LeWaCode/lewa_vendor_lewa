/*
 * Copyright (c) 2011 LewaTek
 * All rights reserved.
 * 
 * DESCRIPTION:
 *
 * WHEN          | WHO               | what, where, why
 * --------------------------------------------------------------------------------
 * 2011-08-29  | GanFeng          | Create file
 */

package com.lewa.os.ui;

import android.app.Activity;
import android.content.Intent;

public interface ActivityResultBridge {
    static final int EVT_ACTIVITY_ON_CREATE_CONTEXT_MENU = 0;
	static final int EVT_ACTIVITY_ON_BACK_PRESSED = 1;
    
    void startActivityForResult(ActivityResultReceiver resultReceiver, Intent intent, int requestCode);
    void handleActivityEvent(Activity activity, int evtCode, Object extra);

    interface ActivityResultReceiver {
        void registerActivityResultBridge(ActivityResultBridge bridge);
        void handleActivityResult(ActivityResultReceiver realReceiver, int requestCode, int resultCode, Intent intent);
    }
}

