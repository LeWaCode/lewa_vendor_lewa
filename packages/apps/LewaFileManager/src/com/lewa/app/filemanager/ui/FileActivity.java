/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.app.filemanager.ui;

import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.view.WindowManager;
import com.lewa.os.ui.ViewPagerIndicatorActivity;

/**
 *
 * @author Administrator
 */
public class FileActivity extends ViewPagerIndicatorActivity {

    public static SlideActivity slideActivity;
    public static boolean created = false;
    public static FileActivity activity;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {

        try {
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        if (!created) {
            slideActivity = new SlideActivity(this);
            slideActivity.preCreate(this);
            created = true;
            this.onCreate(icicle);
        } else {
            super.onCreate(icicle);
            slideActivity.setEntryActivity(this);
            slideActivity.postCreate();
        }
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        activity = this;
        // ToDo add your GUI initialization code here        
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        slideActivity.onEventDestroy();
        super.onDestroy();
    }

    @Override
    public void onAttachedToWindow() {
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
        super.onAttachedToWindow();
    }
}
