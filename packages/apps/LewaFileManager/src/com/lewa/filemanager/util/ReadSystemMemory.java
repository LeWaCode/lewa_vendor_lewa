package com.lewa.filemanager.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.widget.TextView;
import com.lewa.filemanager.beans.FileUtil;
import java.io.File;
import java.util.Map;

public class ReadSystemMemory {

    TextView tv = null;
    public static Map blockSizeMap;

    public static Map getBlockSizeMap( String path) {
        if (blockSizeMap == null) {
            blockSizeMap = FileUtil.getAvailableStore(path);
        }
        return blockSizeMap;
    }

    public static String getAvailableInternalMemorySizeText() {
        return FileUtil.formatSize(getAvailableInternalMemorySize());
    }

    public static Long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }
    public static Long getAvailableExternalMemorySize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }
    public static String getTotalMemoryText() {
        return FileUtil.formatSize(getTotalInternalMemorySize());
    }

    public static Long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }
}
