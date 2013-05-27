package com.lewa.player;

import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.app.Application;
import android.util.Log;

public class ExitApplication extends Application {
    
    private List<Activity> mainActivity = new ArrayList<Activity>();
    
    public List<Activity> MainActivity() {
        return mainActivity;
    }

    public void addActivity(Activity act) {
        mainActivity.add(act);
    } 

    public void finishAll() {        
        for (Activity act : mainActivity) {
            if (!act.isFinishing()) {
                act.finish();
            }
        }
        mainActivity = null;

    }

}
