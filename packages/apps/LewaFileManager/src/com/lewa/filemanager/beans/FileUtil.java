/* 
 * Copyright (C) 2007-2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lewa.filemanager.beans;

import android.content.Context;
import java.io.File;
import java.util.HashMap;

import android.os.StatFs;
import com.lewa.filemanager.util.ReadSystemMemory;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @version 2009-07-03
 * 
 * @author Peli
 * 
 */
public class FileUtil {

    /** TAG for log messages. */
    static final String TAG = "FileUtils";
    private static final int COPY_BUFFER_SIZE = 32 * 1024;
    public static String parseMimePrefix(String mime) {
        int sep;
        if((sep = mime.lastIndexOf("/"))==-1){
            throw new IllegalStateException("cannt find seperator");
        }
        return mime.substring(0,sep);
    }
    public static String getParent(File file) {
        return getParent(file.getAbsolutePath());
    }

    public static String getName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String getNameTitle(String path) {
        String name = getName(path);
        int dot;
        if ((dot = name.lastIndexOf(".")) == -1) {
            return name;
        }
        return name.substring(0, dot);
    }

    public static String getParent(String absPath) {
        String filename = absPath.substring(absPath.lastIndexOf("/") + 1);
        String filepath = absPath;
        // Construct path without file name.
        String pathwithoutname = filepath.substring(0,
                filepath.length() - filename.length());
        if (pathwithoutname.endsWith("/")) {
            pathwithoutname = pathwithoutname.substring(0,
                    pathwithoutname.length() - 1);
        }
        return pathwithoutname;
    }

    public static String removeExt(String name) {
        int dot = -1;
        if ((dot = name.lastIndexOf(".")) <= 0) {
            return name;
        } else {
            return name.substring(0, dot);
        }
    }

    public static File reaplcePath(File src, File parentpath) {
        return new File(parentpath.getAbsolutePath() + "/" + src.getName());
    }

    public static String getRealLowerCaseExtension(String name) {
        String ext = getRealExtension(name);
        return ext.equals("") ? "" : ext.toLowerCase();
    }

    public static String getRealExtension(String name) {
        if (name == null) {
            return null;
        }

        int dot = name.lastIndexOf(".");
        if (dot >= 0) {
            return name.substring(dot + 1).trim();
        } else {
            // No extension.
            return "";
        }
    }

    public static HashMap<String, Object> getAvailableStore(String filePath) {
        HashMap<String, Object> blockSizeMap = new HashMap<String, Object>();

        StatFs statFs = new StatFs(filePath);

        long blockSize = statFs.getBlockSize();

        long totalBlocks = statFs.getBlockCount();

        long availaBlocks = statFs.getFreeBlocks();

        String total = formatSize(totalBlocks * blockSize);

        String availableSpare = formatSize(availaBlocks * blockSize);

        String totalSizeStr = formatSize(totalBlocks * blockSize);
        //modify 
        //String total = formatSize4GB(totalBlocks * blockSize);只显示GB用这句

        String availableSizeStr = formatSize(availaBlocks * blockSize);

        blockSizeMap.put("totalSize", (totalBlocks * blockSize));

        blockSizeMap.put("availaSize", (availaBlocks * blockSize));

        blockSizeMap.put("usedSize", (totalBlocks - availaBlocks) * blockSize);

        blockSizeMap.put("totalBlocks", total);

        blockSizeMap.put("availaBlocks", availableSpare);

        blockSizeMap.put("memTotalBlocks", ReadSystemMemory.getTotalInternalMemorySize());

        blockSizeMap.put("memAvailaBlocks", ReadSystemMemory.getAvailableInternalMemorySize());

        blockSizeMap.put("availaSizeStr", availableSizeStr);

        blockSizeMap.put("totalSizeStr", totalSizeStr);

        return blockSizeMap;
    }

    public static File getPathWithoutFilename(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                // no file to be split off. Return everything
                return file;
            } else {
                String pathwithoutname = getParent(file);
                return new File(pathwithoutname);
            }
        }
        return null;
    }

    public static String formatSize4GB(float size) {
        long kb = 1024;
        long mb = (kb * 1024);
        long gb = (mb * 1024);

        return String.format("%.2f GB", size / gb);

    }

    public static File getFile(File curdirFile, String file) {
        String curdir = curdirFile.getAbsolutePath();
        String separator = "/";
        if (curdir.endsWith("/")) {
            separator = "";
        }
        File clickedFile = new File(curdir + separator + file);
        return clickedFile;
    }

    // 递归
    public static long getFileSize(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        if (flist == null) {
            return f.length();
        }
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSize(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }
        return size;
    }

    public static String formatSize(float size) {
        long kb = 1024;
        long mb = (kb * 1024);
        long gb = (mb * 1024);
        String strvalue;
        String unit;
        float value;
        if (size < kb) {
            value = size;
            unit = " B";
        } else if (size < mb) {
            value = size / kb;
            unit = " K";
        } else if (size < gb) {
            value = size / mb;
            unit = " M";
        } else {
            value = size / gb;
            unit = " G";
        }
        strvalue = new BigDecimal(value).setScale(2, RoundingMode.CEILING).toString();
        strvalue = strvalue.endsWith("0") ? strvalue.substring(0, strvalue.length() - 1) : strvalue;
        strvalue = strvalue.endsWith("0") ? strvalue.substring(0, strvalue.length() - 1) : strvalue;
        strvalue = strvalue.endsWith(".") ? strvalue.substring(0, strvalue.length() - 1) : strvalue;
        return strvalue + unit;
    }

    public static String addParentheses(String varStr) {
        return " ( " + varStr + " ) ";
    }
}
