package com.lewa.face;

import com.lewa.os.ui.ViewPagerIndicatorActivity.StartParameter;
import java.util.ArrayList;


/**
 * 
 * @author fulw
 *
 */
public class IconsMain extends ThemeBaseActivity{
    
    @Override
    protected void initStartParameter(ArrayList<StartParameter> activities) {
        activities.add(new StartParameter(com.lewa.face.local.IconsStyle.class, null,R.string.theme_icons_local));
        activities.add(new StartParameter(com.lewa.face.online.IconsStyle.class , null,R.string.theme_icons_online));
    }

}