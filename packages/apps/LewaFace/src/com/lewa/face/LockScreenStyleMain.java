package com.lewa.face;

import java.util.ArrayList;


/**
 * 
 * @author fulw
 *
 */
public class LockScreenStyleMain extends ThemeBaseActivity{

    @Override
    protected void initStartParameter(ArrayList<StartParameter> activities) {
        activities.add(new StartParameter(com.lewa.face.local.LockScreenStyle.class, null,R.string.theme_lockscreen_local));
        activities.add(new StartParameter(com.lewa.face.online.LockScreenStyle.class , null,R.string.theme_lockscreen_online));
    }

}