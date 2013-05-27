package com.lewa.filemanager.ds.database.service;

import java.io.File;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import com.lewa.filemanager.ds.util.FileMgrCategory;
import com.lewa.base.images.MimeUtil;
import com.lewa.filemanager.beans.FileUtil;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.Logs;
import com.lewa.filemanager.util.StatusCheckUtil;
import java.util.HashMap;

public class CountFilter extends com.lewa.filemanager.ds.sdcard.TypeFilter
        implements java.io.FileFilter {

    private DataSychroInMemory dataSychroInMemory;
    Context context;
    Map<String, String> oftenUsedType = new HashMap<String, String>();
    String type;
    int i = 0;

    public CountFilter(DataSychroInMemory dataSychroInMemory, Context context,
            Integer[] filter_mode, String[] exclude, String[] includeExamples, String type) {
        super(filter_mode, exclude, includeExamples);
        this.context = context;
        this.type = type;
        this.dataSychroInMemory = dataSychroInMemory;
        dataSychroInMemory.receiveDBImage(context, type);
        if (type == null || type.equals(Constants.CateContants.CATE_IMAGES)) {
            oftenUsedType.put("jpg", "image");
            oftenUsedType.put("jpeg", "image");
            oftenUsedType.put("bmp", "image");
            oftenUsedType.put("gif", "image");
            oftenUsedType.put("png", "image");
            
        }
        if (type == null || type.equals(Constants.CateContants.CATE_MUSIC)) {
            oftenUsedType.put("mp3", "audio");
            oftenUsedType.put("wav", "audio");
            oftenUsedType.put("amr", "audio");
        }
        if (type == null || type.equals(Constants.CateContants.CATE_VIDEO)) {
            oftenUsedType.put("avi", "video");
            oftenUsedType.put("wmv", "video");
            oftenUsedType.put("mpg", "video");
            oftenUsedType.put("3gp", "video");
            oftenUsedType.put("3gp", "video");
            oftenUsedType.put("mp4", "video");
        }
        if (type == null || type.equals(Constants.CateContants.CATE_DOCS)) {
            oftenUsedType.put("doc", "doc");
            oftenUsedType.put("docx", "doc");
            oftenUsedType.put("xls", "doc");
            oftenUsedType.put("xlsx", "doc");
            oftenUsedType.put("ppt", "doc");
            oftenUsedType.put("pptx", "doc");
            oftenUsedType.put("htm", "doc");
            oftenUsedType.put("html", "doc");
            oftenUsedType.put("txt", "doc");
            oftenUsedType.put("rtx", "doc");
            oftenUsedType.put("rtf", "doc");
            oftenUsedType.put("zip", "doc");
        }
        if (type == null || type.equals(Constants.CateContants.CATE_PACKAGE)) {
            oftenUsedType.put("apk", "apk");
        }
        if (type == null || type.equals(Constants.CateContants.CATE_THEME)) {
            oftenUsedType.put("lwt", "lewa/theme");
        }
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean accept(File file) {
        // TODO Auto-generated method stub
        if (!super.accept(file)) {
            return false;
        }
        if (file.isDirectory()) {
//            if (file.getAbsolutePath().toLowerCase().startsWith("/mnt/sdcard/lewa/theme")) {
//                return false;
//            }
            file.listFiles(this);
            return false;
        }
        String ext = FileUtil.getRealLowerCaseExtension(file.getName());
        if (ext.length() == 0) {
            return false;
        }
        if (oftenUsedType.containsKey(ext)) {
            String catename = oftenUsedType.get(ext);
            Logs.i("", " fname " + file.getName() + " | " + catename);
            if (catename == null) {
                catename = FileMgrCategory.getCategory(MimeUtil.parseWholeMime(ext));
            }
            Integer id = null;
            boolean ifExists = false;
            FileImage fimage = null;
            try {
                fimage = dataSychroInMemory.getMemoryImage().get(catename);

                ifExists = ((id = fimage.records.get(file.getAbsolutePath())) != null);
                Logs.i("", "ifExists " + ifExists);
                if (StatusCheckUtil.srcardStateResolve()) {
                    return false;
                }
                FileInfoDBManager.doDBSync(file, fimage.operUri, context, ifExists, id, ext);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!ifExists) {
                i++;
                if (i % 500 == 0) {
                    Intent intent = new Intent(ScanReceiver.ACTION_RECEIVER_SCAN);
                    context.sendBroadcast(intent);
                    i = 0;
                }
            } else {
                fimage.records.remove(file.getAbsolutePath());
//                Logs.i("=======", "remove Image" + file.getAbsolutePath());
            }
        }
        return false;
    }
}
