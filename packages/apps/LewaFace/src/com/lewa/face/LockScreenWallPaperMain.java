package com.lewa.face;

import java.util.ArrayList;

/**
 * 
 * @author fulw
 *
 */
public class LockScreenWallPaperMain extends ThemeBaseActivity{

    @Override
    protected void initStartParameter(ArrayList<StartParameter> activities) {
        activities.add(new StartParameter(com.lewa.face.local.LockScreenWallPaper.class, null,R.string.theme_lockscreen_wallpaper_local));
        activities.add(new StartParameter(com.lewa.face.online.LockScreenWallPaper.class , null,R.string.theme_lockscreen_wallpaper_online));
    }

}