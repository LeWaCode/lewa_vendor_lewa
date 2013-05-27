package com.lewa.face;

import com.lewa.os.ui.ViewPagerIndicatorActivity.StartParameter;
import java.util.ArrayList;


/**
 * 
 * @author fulw
 *
 */
public class PhoneMain extends ThemeBaseActivity{
    
    @Override
    protected void initStartParameter(ArrayList<StartParameter> activities) {
        activities.add(new StartParameter(com.lewa.face.local.Phone.class, null,R.string.theme_phone_local));
//        activities.add(new StartParameter(com.lewa.face.online.BootAnimation.class , null,R.string.theme_boot_online));
    }

}