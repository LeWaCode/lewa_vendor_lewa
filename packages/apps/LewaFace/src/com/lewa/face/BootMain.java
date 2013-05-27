package com.lewa.face;

import com.lewa.os.ui.ViewPagerIndicatorActivity.StartParameter;
import java.util.ArrayList;


/**
 * 
 * @author fulw
 *
 */
public class BootMain extends ThemeBaseActivity{
    
    @Override
    protected void initStartParameter(ArrayList<StartParameter> activities) {
        activities.add(new StartParameter(com.lewa.face.local.BootAnimation.class, null,R.string.theme_boot_local));
        activities.add(new StartParameter(com.lewa.face.online.BootAnimation.class , null,R.string.theme_boot_online));
    }

}