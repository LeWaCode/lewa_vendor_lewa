package com.lewa.face;

import com.lewa.os.ui.ViewPagerIndicatorActivity.StartParameter;
import java.util.ArrayList;


/**
 * 
 * @author fulw
 *
 */
public class SettingMain extends ThemeBaseActivity{
    
    @Override
    protected void initStartParameter(ArrayList<StartParameter> activities) {
        activities.add(new StartParameter(com.lewa.face.local.Setting.class, null,R.string.theme_setting_local));
//        activities.add(new StartParameter(com.lewa.face.online.BootAnimation.class , null,R.string.theme_boot_online));
    }

}