/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.base;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class NotifyUtil {

    static NotificationManager notificationManager;
    Notification notification = new Notification();
    public static final int ID_INSTALLED = 0;
    public static Map<Integer, Notification> notificationMap = new HashMap<Integer, Notification>();

    static {
        notificationMap.put(ID_INSTALLED, new Notification());
    }

    public NotifyUtil(Context context) {
        notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
    }

    public void notify(int notifyid, int iconid, String tickerText, String titleText, String word, Intent intent, Context activity, Class flagClazz) {
        Notification notification = notificationMap.get(notifyid);
        if (notification == null) {
            throw new IllegalArgumentException("wrong notifyid " + notifyid);
        }
        PendingIntent pendingIntent = null;
        if (flagClazz == Activity.class) {
            pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
        } else if (flagClazz == Service.class) {
            pendingIntent = PendingIntent.getService(activity.getApplicationContext(), 0, intent, 0);
        } else if (flagClazz == BroadcastReceiver.class) {
            pendingIntent = PendingIntent.getBroadcast(activity.getApplicationContext(), 0, intent, 0);
        }
        notification.icon = iconid;
        notification.tickerText = tickerText;// \u95C1\u8DE8\u5590\u93CB\u5A5A\u5E4F\u9411\u82A5\u6678\u95BA\u5098\u5016\u701A\u5F52\u67E8\u9414\u544A\u706E\u95B9\u70FD\uE5DF\u6FEE\u6401\u5E40\u6E1A\uFFFD\u6678\u95BA\u5098\u5016\u701A\u5F52\u67E8\u9414\u544A\u706E\u95B9\u70FD\uE5DF\u9287\u6C36\u67E8\u9414\u544A\u706E\u95B9\u98CE\u5158\u93C1\u64BB\u5F2C\u9288\u55D7\uE076\u95C1\u8DE8\u5590\u93CB\u5A5A\u5E4F\u951F\uFFFD		// \u95C1\u8DE8\u5590\u93CB\u5A5A\u5E4F\u9411\u82A5\u6678\u95BA\u5098\u5016\u701A\u5F52\u67C5\u59D8\u8FA9\u53C0\u95C1\u8DE8\u5590\u93CB\u5A5A\u5E4F\u690B\u5E9B\u4EDB\u95C1\u8DE8\u558D\u947C\u5EA3\u62E0\u890E\u701A\u5F52\u67E8\u9414\u544A\u706E\u95B9\u51E4\u62F7
        notification.setLatestEventInfo(activity.getApplicationContext(),
                titleText,
                word,
                pendingIntent);
        notificationManager.notify(notifyid, notification);// \u95B9\u7B1B\u56E8\u6678\u95BA\u5098\u5016\u701A\u5F52\u67C5\u59D8\u8FA9\u53C0.
    }

    public static void cancel(Context context, int id) {
        notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }
}