package com.lewa.filemanager.beans;

import java.io.File;
import java.util.List;

import android.content.Context;
import com.lewa.base.Logs;
import com.lewa.base.images.MimeTypeMap;

public class FileInfoUtil {

    static FileInfo fileInfo;

    public static List<? extends FileInfo> getFileInfos(List<FileInfo> fileinfos, Class<? extends FileInfo> clazz, List<File> filesArray, Context context) {
        if (fileinfos == null) {
            throw new IllegalStateException("fileinfos is null");
        }
        if (filesArray == null) {
            return fileinfos;
        }
        for (File f : filesArray) {
            if (f == null) {
                continue;
            }
            fileinfos.add(FileInfo.hasFileInfo(clazz, f, context));
        }
        return fileinfos;
    }

    public static List<? extends FileInfo> getFileInfos(List<FileInfo> fileinfos, Class<? extends FileInfo> clazz, File[] filesArray, Context context, String extRequired) {
        if (fileinfos == null) {
            throw new IllegalStateException("fileinfos is null");
        }
        if (filesArray == null) {
            return fileinfos;
        }
        for (File f : filesArray) {
            if (f == null) {
                continue;
            }
            if (f.isDirectory() || extRequired == null || ((f.isFile() && extRequired.equals(FileUtil.getRealLowerCaseExtension(f.getName()))))) {
                if (clazz == null) {
                    if (isFileType(new FileInfo(f.getAbsolutePath())) == FileType.Audio) {
                        fileinfos.add(FileInfo.hasFileInfo(MusicInfo.class, f, context));
                    }
                }
                fileinfos.add(FileInfo.hasFileInfo(clazz, f, context));
                continue;
            }
//            if(){
//                fileinfos.add(FileInfo.hasFileInfo(clazz, f, context));
//                continue;
//            }
        }
        return fileinfos;
    }

    public enum FileType {
        Apk, Lwt, Audio, Vedio, Image, Doc, Others
    }

    public static FileType isFileType(FileInfo info) {
        if (info.getType() == null) {
            info.buildExtension();
        }
        if (info.getType().equalsIgnoreCase("apk")) {
            return FileType.Apk;
        } else if (info.getType().equalsIgnoreCase("lwt")) {
            return FileType.Lwt;
        } else {
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(info.type);
            if (mime == null) {
                return FileType.Others;
            } else if (mime.toUpperCase().startsWith("AUDIO")) {
                return FileType.Audio;
            } else if (mime.toUpperCase().startsWith("VEDIO")) {
                return FileType.Vedio;
            } else if (mime.toUpperCase().startsWith("IMAGE")) {
                return FileType.Image;
            } else {
                return FileType.Doc;
            }
        }
    }
}
