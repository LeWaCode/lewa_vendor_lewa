package com.lewa.launcher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.Html;
import android.text.Spanned;
import android.widget.ScrollView;
import android.widget.TextView;

public final class AlmostNexusSettingsHelper {

	public static final int CACHE_LOW=1;
	public static final int CACHE_AUTO=2;
	public static final int CACHE_DISABLED=3;
	
    public static final int DESKTOP_ROWS = 4;
    public static final int DESKTOP_COLUMNS = 4;
    public static final boolean GET_ThEmE_ICONS = true;

	private static final String ALMOSTNEXUS_PREFERENCES = "launcher.preferences.almostnexus";

	public static boolean isRomVersion(Context context) {
		return context.getResources().getString(R.string.adw_version).contains("ROM");
	}    

	public static int getDesktopScreens(Context context) {
		SharedPreferences sp = context.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, Context.MODE_PRIVATE);
		int screens = sp.getInt("desktopScreens", context.getResources().getInteger(R.integer.config_desktopScreens))+1;
		return screens;
	}
	public static int getDefaultScreen(Context context) {
		SharedPreferences sp = context.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, Context.MODE_PRIVATE);
		int def_screen = sp.getInt("defaultScreen", context.getResources().getInteger(R.integer.config_defaultScreen));
		return def_screen;
	}
	public static boolean getWallpaperScrolling(Context context) {
		SharedPreferences sp = context.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, Context.MODE_PRIVATE);
		boolean newD = sp.getBoolean("wallpaper_scrolling", context.getResources().getBoolean(R.bool.config_wallpaper_scroll));
		return newD;
	}
	
	public static boolean getHighQuality(Context context){
		SharedPreferences sp = context.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, Context.MODE_PRIVATE);
		boolean newD = sp.getBoolean("activity_format", context.getResources().getBoolean(R.bool.config_activity_format));
		return newD;
	}
	
	public static boolean getIconShadow(Context context) {
        SharedPreferences sp = context.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, Context.MODE_PRIVATE);
        boolean newD = sp.getBoolean("icon_shadow", context.getResources().getBoolean(R.bool.config_icon_shadow));
        return newD;
    }
	public static int getDesktopEffect(Context context) {
        SharedPreferences sp = context.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, Context.MODE_PRIVATE);
        int newD = Integer.valueOf(sp.getString("pref_key_effects", context.getResources().getString(R.string.config_desktop_effect)));
        return newD;
    }
	public static boolean getScreenloop(Context context) {
		SharedPreferences sp = context.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, Context.MODE_PRIVATE);
		boolean newD = sp.getBoolean("screen_loop", context.getResources().getBoolean(R.bool.config_screen_loop));
		return newD;
    }
	
	public static boolean getAutoArrange(Context context) {
		SharedPreferences sp = context.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, Context.MODE_PRIVATE);
		boolean newD = sp.getBoolean("auto_arrange", context.getResources().getBoolean(R.bool.config_auto_arrange));
		return newD;
    }
	
	public static boolean getSwipeUpDown(Context context) {
		SharedPreferences sp = context.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, Context.MODE_PRIVATE);
		boolean newD = sp.getBoolean("action_updown", context.getResources().getBoolean(R.bool.config_action_updown));
		return newD;
    }
	
	public static void setDesktopScreens(Context context,int screens) {
		SharedPreferences sp = context.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = sp.edit();
		editor.putInt("desktopScreens", screens-1);
	    editor.commit();
	}
	public static void setDefaultScreen(Context context,int screens) {
		SharedPreferences sp = context.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = sp.edit();
		editor.putInt("defaultScreen", screens);
	    editor.commit();
	}
	
	public static boolean getSrollSpeed(Context context) {
		SharedPreferences sp = context.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, Context.MODE_PRIVATE);
		boolean newD = sp.getBoolean("scroll_speed", context.getResources().getBoolean(R.bool.config_scroll_speed));
		return newD;
	}
	
	public static boolean getVibrtorBack(Context context) {
		SharedPreferences sp = context.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, Context.MODE_PRIVATE);
		boolean newD = sp.getBoolean("vibrtorback", context.getResources().getBoolean(R.bool.config_screen_vibrtor));
		return newD;
	}
	/**
	 * Creates the "lewa copyright" dialog to be shown when updating ADW.
	 * @author adw
	 *
	 */
	public static class CopyrightDialogBuilder {
		public static AlertDialog create( Context context ) throws NameNotFoundException {

			String cprtVersion = String.format("%s %s", context.getString(R.string.text_lewa_home), context.getString(R.string.adw_version));
			Spanned cprtText = Html.fromHtml(context.getString(R.string.lewa_copyright, TextView.BufferType.SPANNABLE));

			// Set up the holder scrollview
			ScrollView mainView=new ScrollView(context);
			// Set up the TextView
			final TextView message = new TextView(context);
			mainView.addView(message);
			// We'll use a spannablestring to be able to make links clickable
			//final SpannableString s = new SpannableString(aboutText);

			// Set some padding
			message.setPadding(5, 5, 5, 5);
			// Set up the final string
			message.setText(cprtVersion);
			message.append(cprtText);

			return new AlertDialog.Builder(context).setTitle(R.string.title_lewa_copyright).setCancelable(true).setIcon(R.drawable.lewahome).setPositiveButton(
				 context.getString(android.R.string.ok), null).setView(mainView).create();
		}
	}
	
	public static boolean getDebugShowMemUsage(Context context) {
		if(MyLauncherSettings.IsDebugVersion){
    	    SharedPreferences sp = context.getSharedPreferences(ALMOSTNEXUS_PREFERENCES, Context.MODE_PRIVATE);
    		boolean newD = sp.getBoolean("dbg_show_mem", false);
    		return newD;
		}else{
		    return false;
		}
	}
	
}
