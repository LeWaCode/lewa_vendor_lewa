package com.lewa.filemanager.ds.sdcard;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TypeFilter implements java.io.FileFilter {

    public List<Integer> filter_mode = new ArrayList<Integer>();
    public List<String> excludeExamples = new ArrayList<String>();
    public List<String> includeExamples = new ArrayList<String>();
    public static final int FILTER_REMOVE_HIDDEN = 0;
    public static final int FILTER_RETAIN_DIRECTORY = 1;
    public static final int FILTER_RETAIN_FILE = 2;
    public static final int FILTER_BOTH_DIR_FILE = 4;

    public TypeFilter(Integer[] filter_mode, String[] exclude, String[] includeExamples) {
        super();
        if (filter_mode != null) {
            this.filter_mode = Arrays.asList(filter_mode);
        }
        if (exclude != null) {
            this.excludeExamples = Arrays.asList(exclude);
        }
        if (includeExamples != null) {
            this.includeExamples = Arrays.asList(includeExamples);
        }
    }

    public boolean filter(File file) {
        // TODO Auto-generated method stub
        if (excludeExamples == null) {
            return true;
        } else if (excludeExamples.contains(file.getAbsolutePath())) {
            return false;
        }
        if (this.filter_mode.contains(FILTER_BOTH_DIR_FILE) || this.filter_mode.contains(FILTER_RETAIN_DIRECTORY)) {
            if (file.isDirectory()) {
                if (!this.filter_mode.contains(FILTER_REMOVE_HIDDEN)) {
                    return true;
                } else {
                    if (file.isHidden()) {
                        if (includeExamples == null) {
                            return false;
                        } else if (includeExamples.contains(file.getAbsolutePath())) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }
        if (this.filter_mode.contains(FILTER_RETAIN_FILE)
                || this.filter_mode.contains(FILTER_BOTH_DIR_FILE)) {
            if (file.isFile()) {
                if (this.filter_mode.contains(FILTER_REMOVE_HIDDEN)) {
                    if (file.isHidden()) {
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }

        if (this.filter_mode.contains(FILTER_REMOVE_HIDDEN)) {
            if (file.isHidden()) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public void setMode(Integer[] filter_mode, String[] exclude) {
        if (filter_mode != null) {
            this.filter_mode = Arrays.asList(filter_mode);
        }
        if (exclude != null) {
            this.excludeExamples = Arrays.asList(exclude);
        }
    }

    @Override
    public boolean accept(File file) {
        return filter(file);
    }
}
