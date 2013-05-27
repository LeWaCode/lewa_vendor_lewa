package com.lewa.face;

import com.lewa.os.ui.ViewPagerIndicatorActivity.StartParameter;
import java.util.ArrayList;

/**
 * 
 * @author fulw
 *
 */
public class LauncherMain extends ThemeBaseActivity{
    
    @Override
    protected void initStartParameter(ArrayList<StartParameter> activities) {
        activities.add(new StartParameter(com.lewa.face.local.LauncherStyle.class, null,R.string.theme_launcher_local));
        activities.add(new StartParameter(com.lewa.face.online.LauncherStyle.class , null,R.string.theme_launcher_online));
    }

}