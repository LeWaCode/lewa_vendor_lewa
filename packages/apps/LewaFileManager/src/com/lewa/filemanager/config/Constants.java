package com.lewa.filemanager.config;

import com.lewa.filemanager.ds.uri.NavigationConstants;
import java.util.ArrayList;
import java.util.List;

public class Constants {

    public static final int MB = 1048576;
    public static final int KB = 1024;
    public static final int SIZE_LIMIT = 1024 * 0;
    public static final List<String> excludes = new ArrayList<String>();
    public static String[] HIDDEN_EXCLUDED = null;
    public static String[] HIDDEN_INCLUDED = null;
    public static final String LEWA_HOME = "/LEWA";
    public static final String PRIVACY_HOME = NavigationConstants.SDCARD_PATH + LEWA_HOME + "/.privacy";

    static {
        excludes.add(NavigationConstants.SDCARD_PATH + "/DCIM/.thumbnails");
        excludes.add(NavigationConstants.SDCARD_PATH + "/.thumbnails");
        excludes.add(NavigationConstants.SDCARD_PATH + "/.android_secure");
        excludes.add(NavigationConstants.SDCARD_PATH + "/.Bluetooth");
        excludes.add(NavigationConstants.SDCARD_PATH + "/LOST.DIR");
        excludes.add(NavigationConstants.SDCARD_PATH + "/LEWA/theme");
        excludes.add(NavigationConstants.SDCARD_PATH + "/DX-Theme");
        excludes.add(PRIVACY_HOME);
        HIDDEN_EXCLUDED = new String[excludes.size()];
        excludes.<String>toArray(HIDDEN_EXCLUDED);

    }
    public static final String LOWERCASE_APK = "apk";
    public static final String LOWERCASE_LWT = "lwt";

    public static final class GoToInvokeLWT {

        public static final String ACTION_INVOKE_LWT = "com.lewa.lwt.action";
        public static final String INVOKE_LWT_FILE_FIELD = "com.lewa.lwt.field.filepath";
        public static final String FLAG_INVOKE_LWT = "filemgr";
        public static final String FLAG_KEY_INVOKE_LWT = "from";
        public static final String ACTION_DELETE_LWT = "com.lewa.lwt.delete.action";
    }

    public static final class InvokedPath {

        public static final String ACTION_INSTALL_PATH = "com.lewa.filemgr.install";
        public static final String ACTION_INVOKED_PATH = "com.lewa.filemgr.path_start";
        public static final String ACTION_INVOKED_PATH_EXT = "com.lewa.filemgr.path_start.file_ext";
        public static final String CALLBACK_ACTION = "CALLBACK_ACTION";
        public static final String KEY_INVOKED_PATH_RESULT = "filepath";
    }

    public static final class MULTI_SEND {

        public static final String KEY_LEWA_SEND_FLAG = "com.lewa.filemgr.SEND_FLAG";
        public static final int VALUE_LEWA_SEND_FLAG = 1;
        public static final int VALUE_LEWA_MULTY_SEND_FLAG = 2;
    }

    public static final class OperationContants {

        public static final int FINISH_OPERATION = 0;
        public static final int ICON_LOADED = 1;
        public static final int CHOOSE_TEXT_CHANGED = 2;
        public static final int LOAD_HANDLE = 3;
        public static final int RENAME_FINISH = 4;
        public static final int RENAMING = 5;
        public static final int DIR_REFRESH = 6;
        public static final int SEARCHNOTIFY = 7;
        public static final int SEARCH_NOTIFY_EMPTY = 8;
        public static final int CLEAN_NOTIFY = 9;
        public static final int SHOW_SOFT_INPUT = 10;
        public static final int REFRESH_APKINFO = 11;
        public static final int LOAD_PATHACTIVITY = 12;
        public static final int LOAD_PATHACTIVITY_VIEW = 13;
        public static final int ONINSTALL_REPORT = 14;
    }

    public static final class CateContants {

        public static final String CATE_MUSIC = "audio";
        public static final String CATE_IMAGES = "image";
        public static final String CATE_VIDEO = "video";
        public static final String CATE_DOCS = "doc";
        public static final String CATE_PACKAGE = "apk";
        public static final String CATE_THEME = "lewa/theme";
    }

    public static final class MIMEContants {

        public static final String MIME_APPLICATION = "application";
        public static final String MIME_TEXT = "text";
    }

    public static final class FieldConstants {

        public static final String PATH = "path";
        public static final String NAME = "name";
        public static final String SIZE = "sizeText";
        public static final String LASTMODIFIED = "lastModified";
        public static final String COUNT = "countStr";
        public static final String ICON_RES = "iconRes";
        public static final String TYPE = "type";
        public static final String UNFORMATTEDSIZE = "unformattedSize";
        public static final String UNFORMATTEDDATE = "unformattedDate";
        public static final String CHECKBOX_OPTION = "isUISelected";
    }

    public static final class ApkInfo {

        public static final String VERSION_NAME = "versionNameStr";
        public static final String VERSION_CONDITION = "versionCondition";
    }

    public static final class MusicInfo {

        public static final String NAME = "name";
        public static final String GENRE = "genre";
        public static final String ALBUM = "album";
        public static final String ALBUMARTIST = "artist";
        public static final String THUMBNAIL = "thumbnail";
        public static final String ARTISTKEY = "artistkey";
        public static final String NAMEKEY = "namekey";
        public static final String LINE = "line";
    }

    public static final class SharedPrefernce {

        public static final String RememberedCategory = "ApkRememberedPage";
        public static final String KEY_CATEGORY = "apk";
        public static final String KEY_LWT_ISDELETED = "ISDELETED";
        public static final String SP_IMAGE_FILTER_SIZE = "SP_IMAGE_FILTER_SIZE";
        public static final String KEY_IMAGE_FILTER_SIZE = "KEY_IMAGE_FILTER_SIZE";
    }
}
