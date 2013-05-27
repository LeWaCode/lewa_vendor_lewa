/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.database.copersynch;

import android.content.ContentValues;
import android.os.Message;
import android.provider.MediaStore;
import com.lewa.filemanager.ds.util.ContentValuesUtil;
import com.lewa.filemanager.ds.database.service.FileInfoDBManager;
import com.lewa.base.images.FileTypeInfo;
import com.lewa.filemanager.ds.database.MimeSrc;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.app.filemanager.ui.CommonActivity;
import com.lewa.app.filemanager.ui.PathActivity;
import com.lewa.filemanager.util.ActivityPool;
import com.lewa.filemanager.config.Constants;
import java.io.File;
import java.util.List;
import com.lewa.base.Logs;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class DatasrcRename {

    static Thread renamethread = null;

    public static void invokeUpdate(final FileInfo target, final FileInfo src, final CommonActivity commonActivity, final boolean dirRename) {
        if (target.getIsDir()) {
            renameBatchUpdate(target, src, commonActivity);
        } else {
            renameSingleUpdate(target, src, commonActivity, false);
        }
    }

    public static void renameUpdate(final FileInfo target, final FileInfo src, final CommonActivity commonActivity, final boolean subdir) {
        if (target.getIsDir()) {
            final File[] fs = target.getFile().listFiles();
            if (fs == null) {
                if (!subdir) {
                    locate(target, commonActivity);
                }
                return;
            }
            for (File file : fs) {
                renameUpdate(new FileInfo(file, commonActivity), new FileInfo(src.getPath() + "/" + file.getName()), commonActivity, subdir);
            }
        } else {
            renameSingleUpdate(target, src, commonActivity, subdir);
        }
    }

    public static void renameBatchUpdate(final FileInfo target, final FileInfo src, final CommonActivity commonActivity) {
        if (target.getIsDir()) {
            final File[] fs = target.getFile().listFiles();
            if (fs == null) {
                locate(target, commonActivity);
                return;
            }
            if (commonActivity.renameWaitDialog.isShowing()) {
                for (File file : fs) {
                    renameUpdate(new FileInfo(file, commonActivity), new FileInfo(src.getPath() + "/" + file.getName()), commonActivity, true);
                }
            } else {
                commonActivity.renameWaitDialog.show();
                new Thread() {

                    @Override
                    public void run() {
                        super.run();
                        for (File file : fs) {
                            renameUpdate(new FileInfo(file, commonActivity), new FileInfo(src.getPath() + "/" + file.getName()), commonActivity, true);
                        }
//                        locate(target, commonActivity);
                        Message msg = new Message();
                        msg.obj = target;
                        msg.what = Constants.OperationContants.RENAMING;
                        commonActivity.handler.sendMessage(msg);
                    }
                }.start();
            }
        }
    }

    public static void renameSingleUpdate(FileInfo target, FileInfo src, CommonActivity commonActivity, boolean dirRename) {

        FileTypeInfo targetInfo = FileTypeInfo.getTypeInfo(target);
        target.buildName();
        src.buildName();
        FileTypeInfo srcInfo = FileTypeInfo.getTypeInfo(src);
        Logs.i("", "===><===> target.path : " + target.getPath() + " " + targetInfo.ext + " " + srcInfo.ext + " src.path : " + src.getPath() + " ");
        if (srcInfo.ext.equalsIgnoreCase(targetInfo.ext)) {
            boolean isOnlyPathActivity = false;
            if (targetInfo.uri != null) {
                ContentValues cv = ContentValuesUtil.constructContentValues(new File(target.getPath()), targetInfo);
                commonActivity.getContentResolver().update(targetInfo.uri, cv, MediaStore.Audio.Media.DATA + " like ?", new String[]{src.getPath()});
            } else {
                isOnlyPathActivity = true;
            }
            if (!dirRename) {
                if (isOnlyPathActivity) {
                    commonActivity.sortRefresh();
                } else {
                    ActivityPool.getInstance().sortrefresh();
                }
                locate(target, commonActivity);
            }
        } else {
            //out?            
            if (MimeSrc.categoryRepository.containsKey(srcInfo.category)) {
                Logs.i("", "mimePrefix -------------> remove " + srcInfo.mime + " " + srcInfo.category);
                FileInfoDBManager.delete(commonActivity, srcInfo.uri, src.getPath());
                if (!dirRename) {
                    commonActivity.refresh();
                }
            }
            //in?
            if (MimeSrc.categoryRepository.containsKey(targetInfo.category)) {
                Logs.i("", "mimePrefix -------------> add " + targetInfo.mime + " " + targetInfo.uri + " " + targetInfo.category);
                ContentValues contentvalues = ContentValuesUtil.constructContentValues(target.getFile(), targetInfo);
                FileInfoDBManager.insert(commonActivity, targetInfo.uri, contentvalues);
            }
            if (!dirRename 
                   // && (commonActivity instanceof PathActivity)
                    ) {
                commonActivity.sortRefresh();
                locate(target, commonActivity);
            }
        }
    }

    public static void locate(final FileInfo target, final CommonActivity commonActivity) {

//        final int selection = commonActivity.adapter.getPathOnlyItemPos(target);
//        Logs.i("", "===><===>" + target.getPath() + " " + commonActivity + " " + selection);
//        if (selection == -1) {
//            return;
//        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DatasrcRename.class.getName()).log(Level.SEVERE, null, ex);
                }
                Message mess = commonActivity.handler.obtainMessage(Constants.OperationContants.RENAMING);
                mess.obj = target;
//                mess.arg1 = selection;
//                Logs.i("", "===><===>" + mess.arg1);
                commonActivity.handler.sendMessage(mess);

            }
        }).start();

    }
}
