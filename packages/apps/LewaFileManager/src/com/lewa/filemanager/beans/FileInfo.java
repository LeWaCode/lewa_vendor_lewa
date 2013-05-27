package com.lewa.filemanager.beans;

import java.io.File;
import java.util.Date;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;

import com.lewa.app.filemanager.R;
import com.lewa.filemanager.config.Config;
import com.lewa.filemanager.ds.sdcard.TypeFilter;

public class FileInfo {

    public static TypeFilter fileFetcher = new TypeFilter(new Integer[]{TypeFilter.FILTER_BOTH_DIR_FILE, Config.getHiddenOption(Config.SDCARD_HIDE_OPTION)}, null, null);
    public static final int DOC_ICON = R.drawable.lewa_ic_folder;
    public static final int IMAGE_LOADING_ICON = R.drawable.lewa_ic_loading_picture;
    public static final int VIDEO_ICON = R.drawable.lewa_ic_folder_video;
    public static final int PACKAGE_ICON = R.drawable.lewa_ic_folder_package_setup;
    public static final int AUDIO_ICON = R.drawable.lewa_ic_folder_music_mp3;
    public static final int THEME_ICON = R.drawable.lewa_ic_folder_theme;
    public Boolean checkboxOption = false;
    public final static int DIR_RES = R.drawable.ic_launcher_folder;
    public final static int OTHER_FILE_RES = R.drawable.lewa_ic_other_folder;
    public static Drawable iconVideo;
    public static Drawable iconPackage;
    public static Drawable iconDir;
    public static Drawable iconOtherFile;
    public static Drawable iconDoc;
    public static Drawable iconAudio;
    public static Drawable iconImageLoading;
    public static Drawable iconTheme;
    public String path;
    public String name;
    public String sizeText;
    public Long leng;
    public String type;
    public String lastModified;
    public Boolean isDir;
    public Integer count;
    public String countStr;
    public Object iconRes = -1;
    public Long lastModifiedInt;
    public Date unformattedDate;
    public Boolean overrideFlag;
    public Boolean isCutNew;
    public boolean isFileBuilt;
    public boolean isIconBinded;
    public boolean isUISelected;

    public static void init(Context context){
        if (iconDir == null) {
            iconDir = context.getResources().getDrawable(DIR_RES);
        }
        if (iconOtherFile == null) {
            iconOtherFile = context.getResources().getDrawable(OTHER_FILE_RES);
        }
        if (iconDoc == null) {
            iconDoc = context.getResources().getDrawable(DOC_ICON);
        }
        if (iconImageLoading == null) {
            iconImageLoading = context.getResources().getDrawable(IMAGE_LOADING_ICON);
        }
        if (iconVideo == null) {
            iconVideo = context.getResources().getDrawable(VIDEO_ICON);
        }
        if (iconAudio == null) {
            iconAudio = context.getResources().getDrawable(AUDIO_ICON);
        }
        if (iconPackage == null) {
            iconPackage = context.getResources().getDrawable(PACKAGE_ICON);
        }
        if (iconTheme == null) {
            iconTheme = context.getResources().getDrawable(THEME_ICON);
        }
    }
    public Integer getCount() {
//        return (Integer) get("count");
        return count;
    }

    public void setCount(Integer count) {
//        this.put("count", count);
        this.count = count;
    }

    public Boolean getIsDir() {
//        return (Boolean) this.get("isDir");
        return isDir;
    }

    public void setIsDir(Boolean isDir) {
//        this.put("isDir", isDir);
        this.isDir = isDir;
    }

    public Long getLastModifiedInt() {
//        return (Long) this.get("lastModifiedInt");
        return this.lastModifiedInt;
    }

    public void setLastModifiedInt(Long lastModifiedInt) {
//        this.put("lastModifiedInt", lastModifiedInt);
        this.lastModifiedInt = lastModifiedInt;
    }

    public Long getLeng() {
//        return (Long) this.get("leng");
        return this.leng;
    }

    public void setLeng(Long leng) {
//        put("leng", leng);
        this.leng = leng;
    }

    public String getName() {
//        return (String) this.get("name");
        return this.name;
    }

    public void setName(String name) {
//        put("name", name);
        this.name = name;
    }

    public String getPath() {
//        return (String) this.get("path");
        return this.path;
    }

    public void setPath(String path) {
//        put("path", path);
        this.path = path;
    }

    public String getLastModified() {
//        return (String) get("lastModified");
        return this.lastModified;
    }

    public void setLastModified(String lastModified) {
//        put("lastModified", lastModified);
        this.lastModified = lastModified;
    }

    public String getCountStr() {
//        return (String) get("countStr");
        return this.countStr;
    }

    public void setCountStr(String countStr) {
//        put("countStr", countStr);
        this.countStr = countStr;
    }

    public Object getIconRes() {
//        return get("iconRes");
        return this.iconRes;
    }

    public void setIconRes(Object iconRes) {
//        put("iconRes", iconRes);
        this.iconRes = iconRes;
    }

    public String getSizeText() {
//        return (String) get("sizeText");
        return this.sizeText;
    }

    public void setSizeText(String sizeText) {
//        put("sizeText", sizeText);
        this.sizeText = sizeText;
    }

    public FileInfo(File file, Context context) {
        buildFile(file);
        isFileBuilt = true;
    }

    public FileInfo(File parent, String name, Context context) {
        path = parent.getAbsolutePath() + "/" + name;
    }

    public void buildCountStr() {
        count = this.getCount();
        countStr = count == -1 ? "" : "  ( " + (count == -2 ? "..." : count + "") + " )";
//        put("countStr", countStr);
    }

    public String getShowName() {
        return name;
    }

    public String getType() {
//        return this.get("type").toString();
        return this.type;
    }

    public void buildFile() {
        if (!isFileBuilt) {
            buildFile(new File(path));
            isFileBuilt = true;
        }
    }

    public void buildName() {
//        Object objName = this.get("name");
//        if (objName != null) {
//            this.put("name", FileUtil.getName(get("path").toString()));
//        }
        name = path == null ? null : FileUtil.getName(path);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void buildExtension() {
//        Object objType = this.get("type");
//        if (objType != null) {
//            this.put("type", FileUtil.getRealExtension(this.getName()).toString());
//        }
        type = type == null ? null : FileUtil.getRealExtension(this.getName());
    }

    public static FileInfo hasFileInfo(Class<? extends FileInfo> clazz, File file, Context context) {
        return hasFileInfo(clazz, file, context, null);
    }

    public static FileInfo hasFileInfoOnEntity(FileInfo item, File file, Context context) {
        if(!(item instanceof FileInfo)){
            throw new IllegalStateException("wrong type info");
        }
        return hasFileInfo(item.getClass(), file, context, null);
    }

    public static FileInfo hasFileInfo(Class<? extends FileInfo> clazz, File file, Context context, Boolean duplicatedFlag) {
        FileInfo fileinfo = null;
        try {
            if (duplicatedFlag == null) {
                fileinfo = clazz.getConstructor(File.class, Context.class).newInstance(file, context);
            } else {
                fileinfo = clazz.getConstructor(File.class, Context.class, Boolean.class).newInstance(file, context, duplicatedFlag);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return fileinfo;
    }

    public void buildFile(File file) throws NumberFormatException {
        isDir = file.isDirectory();
        path = file.getAbsolutePath();
        name = file.getName();
        type = FileUtil.getRealExtension(name);
        leng = file.length();
        sizeText = FileUtil.formatSize(leng);
        if (this.isDir) {
            sizeText = sizeText.contains("0 B") ? "" : sizeText;
        } else {
            sizeText = sizeText;
        }
        lastModifiedInt = file.lastModified();
        lastModified = DateFormat.format("yyyy-MM-dd kk:mm:ss",
                new Date(lastModifiedInt)).toString();
        count = isDir ? -2 : -1;
        iconRes = -1;
        buildCountStr();
    }
//    public void buildFile(File file) throws NumberFormatException {
//        this.put("isDir", file.isDirectory());
//        put("path", file.getAbsolutePath());
//        put("name", file.getName());
//        put("type", FileUtil.getRealExtension(this.getName()));
//        put("leng", file.length());
//        sizeText = FileUtil.formatSize(file.length());
//        if (file.isDirectory()) {
//            sizeText = sizeText.contains("0 B") ? "" : sizeText;
//        } else {
//            sizeText = sizeText;
//        }
//        put("sizeText", sizeText);
//        lastModifiedInt = file.lastModified();
//        put("lastModifiedInt", lastModifiedInt);
//        lastModified = DateFormat.format("yyyy-MM-dd kk:mm:ss",
//                new Date(lastModifiedInt)).toString();
//        put("lastModified", lastModified);
//        count = file.isDirectory() ? -2 : -1;
//        put("count", count);
//        iconRes = -1;
//        put("iconRes", iconRes);
//        buildCountStr();
//    }

    public FileInfo() {
    }

    public FileInfo(String path) {
        this.path = path;
    }

    public File getFile() {
        return this.getPath() == null ? null : new File(this.getPath());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        final FileInfo other = (FileInfo) obj;
        if ((this.getPath() == null) ? (other.getPath() != null) : !this.getPath().equals(other.getPath())) {
            return false;
        }
        return true;
    }

    public FileInfo(File file, Context context, Boolean duplicatedFlag) {
        this(file, context);
        overrideFlag = duplicatedFlag;
    }
}
