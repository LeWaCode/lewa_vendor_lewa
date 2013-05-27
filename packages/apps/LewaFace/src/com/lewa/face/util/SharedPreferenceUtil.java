/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.face.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

/**
 *
 * @author chenliang
 */
public class SharedPreferenceUtil {

    static Map<String, SharedPreferences> preferences = new HashMap<String, SharedPreferences>();
    public static SharedPreferences onOpen(Context context, String spname) {
        if (!preferences.containsKey(spname)) {
            preferences.put(spname, context.getSharedPreferences(spname, 0));
        }
        return preferences.get(spname);
    }

    public static void putValue(Context context, String spname, String key, Object value) {
        SharedPreferences.Editor spedit = onOpen(context, spname).edit();
        if(value == null){
            spedit.putString(key, null);
        }else if (value instanceof Boolean) {
            spedit.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            spedit.putInt(key, (Integer) value);
        } else if (value instanceof String) {
            spedit.putString(key, (String) value);
        } else if (value instanceof Float) {
            spedit.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            spedit.putLong(key, (Long) value);
        }
        spedit.commit();
    }
    public static Object getValue(Context context, String spname, String key,Class clazz) {
        SharedPreferences sp = onOpen(context, spname);
        if (clazz == Boolean.class) {
            return sp.getBoolean(key, false);
        } else if (clazz == Integer.class) {
            return sp.getInt(key, -1);
        } else if (clazz == String.class) {
            return sp.getString(key,null);
        } else if (clazz == Float.class) {
            return sp.getFloat(key, -1);
        } else if (clazz == Long.class) {
            return sp.getLong(key, -1);
        }
        return null;
    }
    public static void clear(String spname) {
        if (preferences.containsKey(spname)) {
            SharedPreferences sp = preferences.remove(spname);
            sp.edit().clear();
            sp.edit().commit();
        }
    }
}
