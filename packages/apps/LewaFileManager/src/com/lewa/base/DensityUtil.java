package com.lewa.base;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class DensityUtil {

    public static final int WVGA = 0;
    public static final int HVGA = 1;
    public static final int OTHER_DENSITY = 2;

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int getDensity(Activity context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float density = displayMetrics.density;
        if (density == 1.0) {
            return HVGA;
        } else if (density == 1.5) {
            return WVGA;
        } else {
            return OTHER_DENSITY;
        }
    }

    public static int getScreenWidth(Activity acty) {
        return acty.getWindowManager().getDefaultDisplay().getWidth();
    }

    public static int getScreenHeight(Activity acty) {
        return acty.getWindowManager().getDefaultDisplay().getHeight();
    }
}
