package com.lewa.filemanager.actions;

import android.app.Activity;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import android.widget.Toast;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.config.Config;
import com.lewa.filemanager.ds.database.copersynch.DatasrcDelete;
import com.lewa.filemanager.ds.database.copersynch.DatasrcPaste;
import com.lewa.app.filemanager.ui.CommonActivity;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.beans.FileInfoUtil;
import com.lewa.filemanager.config.Constants;
import com.lewa.filemanager.util.DialogUtil;
import com.lewa.filemanager.beans.FileUtil;
import com.lewa.filemanager.funcgroup.privacy.PrivacyInfo;
import com.lewa.filemanager.ds.sdcard.TypeFilter;
import com.lewa.base.Logs;
import com.lewa.app.filemanager.ui.CountActivity;
import com.lewa.app.filemanager.ui.PrivacyActivity;
import com.lewa.base.images.MimeTypeMap;
import com.lewa.filemanager.util.ReadSystemMemory;
import com.lewa.filemanager.util.StatusCheckUtil;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class OperationUtil {

    public static String COPY_OF;
    public static CommonActivity selectedActivity;
    public static List<FileInfo> selectedEntities = new ArrayList<FileInfo>();
    public static List<FileInfo> cutDirectlyMovedDir = new ArrayList<FileInfo>();
    public List<FileInfo> pasteOperEntities = new ArrayList<FileInfo>();
    public static final int STATUS_CUT_COPY = 1;
    public static final int STATUS_BROWSE = 2;
    public static final int STATUS_SELECT = 3;
    public static final int OPER_TYPE_CUT = 0;
    public static final int OPER_TYPE_COPY = 1;
    public static final int OPER_TYPE_DEL = 2;
    public static final int OPER_TYPE_MOVE_PRIVACY = 3;
    private static int operType = 3;
    public String name;
    public String newpath;
    public static Integer mOperationState = STATUS_BROWSE;
    private File file;
    public static ProgressDialog operatingDialog;
    private String destination;
    public Map<String, Boolean> duplicated = new HashMap<String, Boolean>();
    boolean nameConflictFlag = true;
    private static final int COPY_BUFFER_SIZE = 32 * 1024;
    public int actualDuplicated;
    public static Map<FileInfo, FileInfo> operMatcher = new HashMap<FileInfo, FileInfo>();

    public static void initStrCnst(Context context) {
        COPY_OF = context.getString(R.string.copyof);
    }

    public static int getOperType() {
        return operType;
    }

    public static void setOperType(int operType) {
        OperationUtil.operType = operType;
    }
    private static int copied;

    public String getDestination() {
        return destination;
    }

    public static List<String> toListPath(Context context) {
        List<String> paths = new ArrayList<String>();
        for (FileInfo fStr : selectedEntities) {
            paths.add(fStr.getPath());
        }
        return paths;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void composeOpertaingingDialog(Context context) {
        int titleId = -1;
        switch (this.operType) {
            case OPER_TYPE_DEL:
                titleId = R.string.deleting;
                break;
            case OPER_TYPE_COPY:
                titleId = R.string.copiing;
                break;
            case OPER_TYPE_CUT:
                titleId = R.string.cutting;
                break;
            case OPER_TYPE_MOVE_PRIVACY:
                titleId = R.string.moving_to_privacy;
                break;
        }
        if (operatingDialog == null) {
            operatingDialog = new ProgressDialog(((Activity) context));
            operatingDialog.setMessage(context.getString(titleId));
            operatingDialog.setCancelable(false);
            operatingDialog.setIndeterminate(true);
        } else {
            operatingDialog.setMessage(context.getString(titleId));
        }
    }
    private OnClickListener operClickListener = new PerformOperationClickListener(
            this);
    private OnClickListener cancelClickListener = new CancelOperationClickListener(
            this);
    private OnClickListener delCancelClickListener = new OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            dialog.dismiss();
        }
    };
    public Thread thread;

    public int tackleNewFolderText(List<FileInfo> names, String prefix) {
        String regex = prefix + "[\\s]*\\([\\d]+\\)";
        Pattern p = Pattern.compile(regex);
        Integer n = 0;
        Integer tmp;
        for (FileInfo fi : names) {
            if (p.matcher(fi.getName()).matches()) {
                Logs.i("", "========== tt" + fi.getName());
                tmp = Integer.parseInt(fi.getName().split("\\(")[1].split("\\)")[0]);
                n = Math.max(n, tmp);

            }
        }
        return n;
    }
    public static boolean operationRunnableOver;

    private class OperationRunnable implements Runnable {

        Context context;
        OperationUtil oper;

        public OperationRunnable(Context context, OperationUtil oper) {
            super();
            this.context = context;
            this.oper = oper;
        }

        public void run() {
            // TODO Auto-generated method stub
            Looper.prepare();
            operationRunnableOver = false;
            oper.operate();
            operationRunnableOver = true;
            if (StatusCheckUtil.mediaUpdating > 0) {
                return;
            }
            Message mess = new Message();
            mess.what = Constants.OperationContants.FINISH_OPERATION;
            mess.obj = operatingDialog;
            ((CommonActivity) context).handler.sendMessage(mess);

            Looper.loop();
        }
    }
    public Context context;

    private class PerformOperationClickListener implements OnClickListener {

        OperationUtil oper;

        public PerformOperationClickListener(OperationUtil oper) {
            super();
            this.oper = oper;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // TODO Auto-generated method stub
            dialog.cancel();
            dialog.dismiss();
            if (operatingDialog != null) {
                if (operatingDialog.isShowing()) {
                    return;
                }
                if (thread != null && thread.isAlive()) {
                    return;
                }
                thread.start();
                operatingDialog.show();
            }
        }
    }

    public List<String> initPathList(List<FileInfo> paths) {
        List<String> files = new ArrayList<String>();
        for (FileInfo p : paths) {
            if (p instanceof PrivacyInfo) {
                if (((PrivacyInfo) p).encryptedPath == null) {
                    ((PrivacyInfo) p).buildFile();
                }
                files.add(((PrivacyInfo) p).encryptedPath);
            } else {
                files.add(p.getPath());

            }
        }
        return files;
    }

    public List<File> initFileList(List<FileInfo> paths) {
        List<File> files = new ArrayList<File>();
        for (FileInfo p : paths) {
            if (p instanceof PrivacyInfo) {
                if (((PrivacyInfo) p).encryptedPath == null) {
                    ((PrivacyInfo) p).buildFile();
                }
                files.add(new File(((PrivacyInfo) p).encryptedPath));
            } else {
                files.add(new File(p.getPath()));

            }
        }
        return files;
    }
    public static boolean duplicatedDialogFlag = false;

    private class CancelOperationClickListener implements OnClickListener {

        OperationUtil oper;

        public CancelOperationClickListener(OperationUtil oper) {
            super();
            this.oper = oper;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // TODO Auto-generated method stub
            dialog.cancel();
            dialog.dismiss();
            ((CommonActivity) context).makeSelectAll(false, true);
            ((CommonActivity) context).makeSeenToAll(true);
            selectedEntities.clear();
            OperationUtil.mOperationState = OperationUtil.STATUS_BROWSE;
            if (CountActivity.categoryActivity != null) {
                CountActivity.categoryActivity.refresh();
            }
            duplicated.clear();
        }
    }

    public List<FileInfo> getOperationTargets() {
        return selectedEntities;
    }

    public void addOperationTarget(FileInfo path) {
        if (!selectedEntities.contains(path)) {
            selectedEntities.add(path);
        }
    }

    public <T extends FileInfo> void removeOperationTarget(T path) {
        if (selectedEntities.contains(path)) {
            selectedEntities.remove(path);
        }
    }

    public void operate() {
        Log.i("---", "---123" + operType + " " + context);
        operate(OperationUtil.operType, context);
    }

    private void operate(int command, Context context) {
        // TODO Auto-generated method stub

        switch (command) {
            case OPER_TYPE_DEL:
                for (int i = selectedEntities.size() - 1; i >= 0; i--) {
                    if (selectedEntities.size() < i) {
                        break;
                    }
                    FileInfo fi = selectedEntities.get(i);
                    if(fi==null){
                        continue;
                    }
                    file = fi.getFile();
                    recursiveDelete(file);
                }
                break;
            case OPER_TYPE_CUT:
                recursiveCut(initPathList(this.selectedEntities), destination);
                break;
            case OPER_TYPE_COPY:
                recursiveCopy(OperationUtil.selectedEntities, destination, true);
                break;
            case OPER_TYPE_MOVE_PRIVACY:
                moveInPrivacy(initPathList(this.selectedEntities), destination, context);
                break;
        }
        updateEndOnFiles(OperationUtil.getOperType());
        Logs.i("toSelectedString", "duplicated --" + this.duplicated.size());
    }

    private void updateEndOnFiles(int operType) {
        switch (operType) {
            case OperationUtil.OPER_TYPE_DEL:
                updateDelOnCategory();
                break;
            case OperationUtil.OPER_TYPE_COPY:
            case OperationUtil.OPER_TYPE_CUT:
                updateCutCopyOnFiles();
                break;
            case OperationUtil.OPER_TYPE_MOVE_PRIVACY:
                CommonActivity.refreshCountActivity();
                PrivacyActivity.activityInstance.refresh();
                break;
        }
    }

    public void updateCutCopyOnFiles() {
        FileInfo srcInfo;
        FileInfo targetInfo;
        for (Entry en : OperationUtil.operMatcher.entrySet()) {
            srcInfo = (FileInfo) en.getKey();
            targetInfo = (FileInfo) en.getValue();
            DatasrcPaste.updateCutCopyOnSingleFile(targetInfo, targetInfo, srcInfo, context);
        }
    }

    public void updateDelOnCategory() {
        for (FileInfo info : OperationUtil.operMatcher.keySet()) {
            DatasrcDelete.recursiveUpdateDel(info, context);
        }
    }

    public void updateDelOnCategory4Privacy() {
        for (FileInfo info : OperationUtil.operMatcher.keySet()) {
            DatasrcDelete.recursiveUpdateDel(info, context);
        }
    }

    public static void dataClear() {
        if (!selectedEntities.isEmpty()) {
            selectedEntities.clear();
            copied = 0;
            sdcardFullMarked = false;
        }
    }

    public void setCommand(int command) {
        this.operType = command;
    }

    public void invokeOperation(Context context) {
        invokeOperation(this.operType, context);
    }

    public void invokeOperation(int command, Context context) {
        operType = command;
        this.context = context;
        composeOpertaingingDialog(this.context);
        thread = new Thread(new OperationRunnable(context, this));
        int titleId;
        String title;
        int cancelBtnId;
        int operName;
        if (this.operType == this.OPER_TYPE_DEL) {
            if(selectedEntities.size() == 1 ){
                title = context.getString(R.string.sure_delete,context.getString(selectedEntities.get(0).isDir?R.string.directory:R.string.file));
            }else{
                title = context.getString(R.string.sure_mass_delete);
            }
            DialogUtil.DialogAbstract da = new DialogUtil.DialogAbstract();
            da.context = context;
            da.title = context.getString(R.string.delete);
            da.message = title;
            da.positiveButtonText = context.getString(R.string.yes);
            da.negativeButtonText = context.getString(R.string.no);
            da.positiveButtonClickListener = this.operClickListener;
            da.negativeButtonClickListener = delCancelClickListener;
            DialogUtil.showChoiceDialog(da);
        } else if (this.operType == this.OPER_TYPE_COPY) {
            titleId = R.string.copy_continuecopy;
            cancelBtnId = R.string.cancelcopy;
            operName = R.string.copy;
            accountDuplicated(context, titleId, cancelBtnId, operName);
        } else if (this.operType == this.OPER_TYPE_CUT) {
            titleId = R.string.cut_continuecut;
            cancelBtnId = R.string.cancelcut;
            operName = R.string.cut;
            accountDuplicated(context, titleId, cancelBtnId, operName);
        } else if (this.operType == OPER_TYPE_MOVE_PRIVACY) {
            titleId = R.string.cut_continuecut;
            cancelBtnId = R.string.cancel_move;
            operName = R.string.move_to_privacy_box;
            accountDuplicated(context, titleId, cancelBtnId, operName);
        }
    }

    private void accountDuplicated(Context context, int titleId,
            int cancelBtnId, int operName) {
        String num;
        String[] childrenFiles = new File(destination).list();
        accountDuplicated(this.initFileList(selectedEntities), new File(destination),
                childrenFiles == null ? new ArrayList<String>() : Arrays.asList(childrenFiles),
                duplicated = new HashMap<String, Boolean>());
        num = this.duplicated.size() + "";
        if (!duplicated.isEmpty()) {
            DialogUtil.DialogAbstract da = new DialogUtil.DialogAbstract();
            da.context = context;
            da.title = context.getString(titleId);

            da.message = context.getString(R.string.overwrite_confirm,
                    /* context.getString(operName), */ num);
            da.positiveButtonText = context.getString(R.string.overwrite_continue);
            da.negativeButtonText = context.getString(cancelBtnId);
            da.positiveButtonClickListener = this.operClickListener;
            da.negativeButtonClickListener = cancelClickListener;
            duplicatedDialogFlag = true;
            DialogUtil.showChoiceDialog(da);
        } else {
            Logs.i("", "============= dialog 's context " + context);
            thread.start();
            operatingDialog.show();
        }
    }

    private void recursiveCut(List<String> src, String dest) {
        File target;
        String oldPath;
        String destTmp;
        for (String abspath : src) {
            file = new File(abspath);
            oldPath = file.getParent();
            if (oldPath.equals(dest)) {
                Logs.i("", "needn't move");
                continue;
            }
            destTmp = dest + "/" + file.getName();
            target = new File(destTmp);
            this.recursiveMove(file, target, true);
        }

    }

    public boolean recursiveMove(File src, File target, boolean isRoot) {
        FileInfo info;
        if (src.isDirectory()) {
            if (!target.exists()) {
                target.mkdirs();
            }
            File[] fs = src.listFiles();
            if (fs != null) {
                for (File f : fs) {
                    this.recursiveMove(f, FileUtil.reaplcePath(f, target), false);
                }
            }
            src.delete();
        } else {
            info = new FileInfo(target, context);
            if (target.exists()) {
                boolean delete = target.delete();
                actualDuplicated++;
                info.overrideFlag = true;
            }

            operMatcher.put(new FileInfo(src, context), info);
            boolean b = this.move(src, info.getFile());
            Logs.i("", "src ---------- " + src.getAbsolutePath() + "-----------" + target.getAbsolutePath() + "-----------" + b);

            return b;
        }
        return false;

    }
    public static boolean sdcardFullMarked;

    private void recursiveCopy(List<FileInfo> origiSrc, String destDir, boolean isRootDir) {
        File target;
        FileInfo info;
        String[] names = new File(destDir).list();
        List<String> destFileNames = (names == null ? new ArrayList<String>() : Arrays.<String>asList(names));

        for (FileInfo src : origiSrc) {
            Logs.i("", "========== src getPath" + src.getPath());
            if (src == null) {
                continue;
            }
            String prefix = "";
            if (src.getFile().getParent().equals(destDir)) {
                prefix = COPY_OF;
            }

            target = new File(destDir + "/" + prefix + src.getName());
            Logs.i("", "========== target path " + target.getAbsolutePath());
            while (target.exists()) {
                if (prefix.equals("")) {
                    break;
                } else {
                    prefix += COPY_OF;
                }
                target = new File(destDir + "/" + prefix + src.getName());
            }
            Logs.i("copy", " src " + src.getFile().getAbsolutePath() + " target " + target.getAbsolutePath());
            if (src.getFile().isFile()) {
                Logs.i("==Read ReadSystemMemory.getAvailableInternalMemorySize(): " + ReadSystemMemory.getAvailableInternalMemorySize());
                Logs.i("==Read src.length(): " + src.getFile().length());
                Logs.i("==Read boolean " + (ReadSystemMemory.getAvailableExternalMemorySize() < src.getFile().length()));
                if (ReadSystemMemory.getAvailableExternalMemorySize() < src.getFile().length()) {
                    if (!sdcardFullMarked) {
                        Toast.makeText(context, context.getString(R.string.copied, copied + ""), Toast.LENGTH_LONG).show();
                        sdcardFullMarked = true;
                    }
                    return;
                }
                Boolean overrided = null;
                if (destFileNames.contains(src.getName()) && target.exists()) {
                    target.delete();
                    overrided = true;
                }
                this.copy(src.getFile(), target);
                try {
                    operMatcher.put(src, FileInfo.hasFileInfo(src.getClass(), target, context, overrided));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                copied++;
            } else {
                if (!target.exists() || destFileNames.contains(src.getName())) {
                    target.mkdir();
                }
                Log.i("file", "-----------" + src.getFile().getAbsolutePath());
                File[] children = src.getFile().listFiles();
                if (children == null) {
                    return;
                }
                List<FileInfo> fileinfos = new ArrayList<FileInfo>();
                FileInfoUtil.getFileInfos(fileinfos, FileInfo.class, children, context, null);
                recursiveCopy(fileinfos,
                        target.getAbsolutePath(), false);
            }
        }
    }

    private void accountDuplicated(List<File> src, File dest,
            List<String> currDestSrc, Map<String, Boolean> duplicated) {
        File fileTmp;
        if(src==null){
            return;
        }
        for (File file : src) {
            if (file == null) {
                continue;
            }
            newpath = dest + "/" + file.getName();
            fileTmp = new File(newpath);
            if (currDestSrc.contains(file.getName()) && fileTmp.exists()) {
                if (file.isHidden()) {
                    continue;
                }
                if (fileTmp.isFile()) {
                    if (!file.getParent().equals(dest.getAbsolutePath())) {
                        duplicated.put(newpath, false);
                    }
                } else {
                    String[] destlist = fileTmp.list();
                    accountDuplicated(Arrays.asList(file.listFiles()), fileTmp,
                            destlist != null ? Arrays.asList(destlist) : new ArrayList<String>(), duplicated);
                }
            }
        }
    }

    private void copy(File oldFile, File newFile) {

        try {
            FileInputStream input = new FileInputStream(oldFile);
            FileOutputStream output = new FileOutputStream(newFile);

            byte[] buffer = new byte[COPY_BUFFER_SIZE];

            while (true) {
                int bytes = input.read(buffer);

                if (bytes <= 0) {
                    break;
                }

                output.write(buffer, 0, bytes);
            }

            output.close();
            input.close();

        } catch (Exception e) {
            // toast = R.string.error_copying_file;
        }
    }

    private boolean move(File oldFile, File newFile) {
        return oldFile.renameTo(newFile);
    }

    public boolean recursiveDelete(File file) {
        // Recursively delete all contents.
        File[] files;
        if (file.isDirectory()) {
            files = file.listFiles();
            if (files != null) {
                for (int x = 0; x < files.length; x++) {
                    recursiveDelete(files[x]);
                }
            }
        } else {
            operMatcher.put(new FileInfo(file, context), null);
        }
        file.delete();
        return false;
    }

    public static void openFile(FileInfo fileInfo, Context context) {
        File aFile = fileInfo.getFile();
        if (!aFile.exists()) {
            return;
        }
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(aFile);
        String type = MimeTypeMap.getSingleton().getExtensionToMimeTypeMap().get(
                FileUtil.getRealLowerCaseExtension((fileInfo.getShowName()).toString()));
        if (type == null) {
            Toast.makeText(context, R.string.find_no_associated_app, Toast.LENGTH_SHORT).show();
            return;
        }
        type = type.startsWith("package") ? "application"
                + type.substring(type.indexOf("/")) : type;
        Logs.i("type", "===" + type);
        intent.setDataAndType(data, type == null ? "*/*" : type);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(context, R.string.find_no_associated_app, Toast.LENGTH_LONG).show();
        }
    }

    public static Boolean renameFileOrFolder(File file, String newFileName, CommonActivity context) {
        Logs.i("", "--> rename " + file.getName() + " " + newFileName);
        File newFile = new File(FileUtil.getPathWithoutFilename(file.getParentFile()) + "/" + newFileName);
        Logs.i("", "--> rename " + file.getName() + " " + newFile.getName());
        if (newFile.getName().equals(file.getName())) {
            Toast.makeText(context, R.string.notrenameforyou,
                    Toast.LENGTH_SHORT).show();
            return null;
        } else if (newFile.exists()) {
            Toast.makeText(context, R.string.cannt_rename_same_name,
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        Logs.i("", "--> rename " + file.getName() + " " + newFile.getName());
        boolean b = file.renameTo(newFile);
        Logs.i("", "--> rename " + b);

        return b;
    }

    public void multipleSend(Context context) {
        String mime = ((CommonActivity) context).prepareMime();
        if (mime.equals(Constants.CateContants.CATE_PACKAGE)) {
        }
        ArrayList<Uri> paths = new ArrayList<Uri>();
        File file;
        TypeFilter fileFetcher = null;
        try {
            fileFetcher = new UriAbsPathFilter(new Integer[]{TypeFilter.FILTER_BOTH_DIR_FILE, Config.getHiddenOption(((CommonActivity) context).isHiddenFileShow())}, Constants.HIDDEN_EXCLUDED, Constants.HIDDEN_INCLUDED, paths);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (FileInfo fi : OperationUtil.selectedEntities) {
            file = fi.getFile();
            if (file.isDirectory()) {
                file.listFiles(fileFetcher);
            } else {
                paths.add(Uri.fromFile(fi.getFile()));
            }
        }
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        if (paths.size() > 1) {
            intent.putExtra(Constants.MULTI_SEND.KEY_LEWA_SEND_FLAG, Constants.MULTI_SEND.VALUE_LEWA_MULTY_SEND_FLAG);
        } else if (paths.size() == 1) {
            intent.putExtra(Constants.MULTI_SEND.KEY_LEWA_SEND_FLAG, Constants.MULTI_SEND.VALUE_LEWA_SEND_FLAG);
        }
        intent.setType(mime);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, paths);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.menu_send)));
        ((CommonActivity) context).makeSelectAll(false, true);
        ((CommonActivity) context).treatBottmBar();
    }

    public void moveInPrivacy(List<String> src, String dest, Context context) {
        recursiveCut(src, dest);
        ((CommonActivity) context).updateDelOnCategory4Privacy();
    }
}
