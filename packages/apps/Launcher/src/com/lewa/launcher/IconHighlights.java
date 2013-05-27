package com.lewa.launcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;

public class IconHighlights {
	public static final int TYPE_DESKTOP = 1;
	public static final int TYPE_DRAWER = 3;
	private static final int PRESSEDCOLOR = 0xffff6600;

	public IconHighlights(Context context) {
		// TODO Auto-generated constructor stub
	}

	private static Drawable oldSelector(Context context, int type) {
		int pressedColor = PRESSEDCOLOR;
		// ADW: Load the specified theme
		Resources themeResources = null;
		themeResources = context.getResources();
		Drawable drawable = null;
		// use_drawer_icons_bg
		int resource_id = 0;

		resource_id = themeResources.getIdentifier("shortcut_selector",
                "drawable", "com.lewa.launcher");

//		if (resource_id != 0) {
//			drawable = themeResources.getDrawable(resource_id);
//		} else {

			drawable = themeResources.getDrawable(R.drawable.shortcut_selector);
//		}
		//drawable.setColorFilter(pressedColor, Mode.SRC_ATOP);
		return drawable;
	}

	public static Drawable getDrawable(Context context, int type) {
		return oldSelector(context, type);
	}
}
