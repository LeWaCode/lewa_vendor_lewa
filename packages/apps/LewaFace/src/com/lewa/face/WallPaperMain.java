package com.lewa.face;

import java.util.ArrayList;


/**
 * 
 * @author fulw
 *
 */
public class WallPaperMain extends ThemeBaseActivity{
    

    @Override
    protected void initStartParameter(ArrayList<StartParameter> activities) {
        
        activities.add(new StartParameter(com.lewa.face.local.WallPaper.class, null,R.string.theme_wallpaper_local));
        activities.add(new StartParameter(com.lewa.face.online.WallPaper.class , null,R.string.theme_wallpaper_online));
    }

}