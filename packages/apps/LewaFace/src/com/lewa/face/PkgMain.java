package com.lewa.face;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeUtil;
import com.lewa.os.ui.ViewPagerIndicatorActivity.StartParameter;


/**
 * 
 * @author fulw
 *
 */
public class PkgMain extends ThemeBaseActivity{
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        ThemeUtil.initLocale(this);
        
        ThemeUtil.createFaceDir();
        
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(ThemeConstants.DOWNLOAD_NOTIFICATION_ID);
    }

    @Override
    public void onChangePagerTrigger(int position) {
        // TODO Auto-generated method stub
        super.onChangePagerTrigger(position);
    }

    @Override
    protected void initStartParameter(ArrayList<StartParameter> activities) {
        activities.add(new StartParameter(com.lewa.face.local.ThemePkg.class, null,R.string.theme_local));
        activities.add(new StartParameter(com.lewa.face.online.ThemePkg.class , null,R.string.theme_online));
    }
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        
        ThemeUtil.exitApplication(this);
        
        super.onDestroy();
    }
    
   

}

