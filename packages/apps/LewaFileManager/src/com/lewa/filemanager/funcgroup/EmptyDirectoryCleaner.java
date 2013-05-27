/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.funcgroup;

import com.lewa.filemanager.beans.ReportInfo;
import com.lewa.filemanager.ds.sdcard.TypeFilter;
import com.lewa.base.Logs;
import com.lewa.filemanager.beans.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author chenliang
 */
public class EmptyDirectoryCleaner extends TypeFilter {

    public EmptyDirectoryCleaner(Integer[] filter_mode, String[] exclude, String[] includeExamples) {
        super(filter_mode, exclude, includeExamples);
    }
    
    public static List<ReportInfo> cleanedEntities = new ArrayList<ReportInfo>();

    public static int getCleanedCount() {
        return cleanedEntities.size();
    }
    private String[] namelist;

    public static void clearCleanData() {
        cleanedEntities.clear();
    }
    String postfix;

    @Override
    public boolean accept(File file) {
        if (super.accept(file)) {
            postfix = FileUtil.getName(file.getAbsolutePath().toUpperCase());
            if (file.isDirectory() && !postfix.equalsIgnoreCase("LEWA/THEME") && !postfix.equalsIgnoreCase(".NOMEDIA")) {
                namelist = file.list();
                if (namelist == null || namelist.length == 0) {
                    cleanedEntities.add(new ReportInfo(file.getName(), file.getAbsolutePath()));
                    file.delete();
                } else {                    
                    file.listFiles(this);
                }
            }
            return false;
        }
        return false;
    }
}
