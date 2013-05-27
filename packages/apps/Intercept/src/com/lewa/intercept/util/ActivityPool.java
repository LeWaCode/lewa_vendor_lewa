package com.lewa.intercept.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

public class ActivityPool {
    public static List<Activity> pool = new ArrayList<Activity>(0);

    public static void add(Activity activity) {
        if (!pool.contains(activity)) {
            pool.add(activity);
        }
    }

    public static ParentActivity get(Class<?> clazz) {
        for (Activity a : pool) {
            if (a.getClass() == clazz) {
                return (ParentActivity)a;
            }
        }
        return null;
    }
}
