/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.database.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 *
 * @author Administrator
 */
public class FileScanService extends Service {

    public static final String ActionScanService = "ActionScanService";
    Context context;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        context = this;
        String type = intent==null?null:intent.getStringExtra(ScanReceiver.SCAN_TYPE);
        ScanHelper.scan(type, context);
    }
}
