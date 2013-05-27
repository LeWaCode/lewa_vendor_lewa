package com.lewa.face;

import com.lewa.os.ui.ViewPagerIndicatorActivity.StartParameter;
import java.util.ArrayList;


/**
 * 
 * @author fulw
 *
 */
public class PimMain extends ThemeBaseActivity{
    
    @Override
    protected void initStartParameter(ArrayList<StartParameter> activities) {
        activities.add(new StartParameter(com.lewa.face.local.Pim.class, null,R.string.theme_pim_local));
//        activities.add(new StartParameter(com.lewa.face.online.BootAnimation.class , null,R.string.theme_boot_online));
    }

}