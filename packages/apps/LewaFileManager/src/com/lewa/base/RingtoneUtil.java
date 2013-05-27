/*
 * To change context.template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.base;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;

/**
 *
 * @author chenliang
 */
public class RingtoneUtil {

    public static final int RINGTONE = 0;                   //铃声
    public static final int NOTIFICATION = 1;               //通知音
    public static final int ALARM = 2;                      //闹钟
    public static final int ALL = 3;

    public static void setVoice(Context context,String path2, int id) {
        ContentValues cv = new ContentValues();
        Uri newUri = null;
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(path2);
        // 查询音乐文件在媒体库是否存在
        Cursor cursor = context.getContentResolver().query(uri, null, MediaStore.MediaColumns.DATA + "=?", new String[]{path2}, null);
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            String _id = cursor.getString(0);
            switch (id) {
                case RINGTONE:
                    cv.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                    cv.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                    cv.put(MediaStore.Audio.Media.IS_ALARM, false);
                    cv.put(MediaStore.Audio.Media.IS_MUSIC, false);
                    break;
                case NOTIFICATION:
                    cv.put(MediaStore.Audio.Media.IS_RINGTONE, false);
                    cv.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                    cv.put(MediaStore.Audio.Media.IS_ALARM, false);
                    cv.put(MediaStore.Audio.Media.IS_MUSIC, false);
                    break;
                case ALARM:
                    cv.put(MediaStore.Audio.Media.IS_RINGTONE, false);
                    cv.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                    cv.put(MediaStore.Audio.Media.IS_ALARM, true);
                    cv.put(MediaStore.Audio.Media.IS_MUSIC, false);
                    break;
                case ALL:
                    cv.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                    cv.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                    cv.put(MediaStore.Audio.Media.IS_ALARM, true);
                    cv.put(MediaStore.Audio.Media.IS_MUSIC, false);
                    break;
                default:
                    break;
            }
            // 把需要设为铃声的歌曲更新铃声库
            context.getContentResolver().update(uri, cv, MediaStore.MediaColumns.DATA + "=?", new String[]{path2});
            newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));
            // 一下为关键代码：
            switch (id) {
                case RINGTONE:
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);
                    break;
                case NOTIFICATION:
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, newUri);
                    break;
                case ALARM:
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, newUri);
                    break;
                case ALL:
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALL, newUri);
                    break;
                default:
                    break;
            }
            //播放铃声
            //         Ringtone rt = RingtoneManager.getRingtone(context. newUri);
            //         rt.play();
        }
    }
}
