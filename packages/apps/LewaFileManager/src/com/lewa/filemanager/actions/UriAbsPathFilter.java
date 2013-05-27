/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.actions;

import android.net.Uri;
import com.lewa.filemanager.ds.sdcard.TypeFilter;
import java.io.File;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class UriAbsPathFilter extends TypeFilter {

    private final List<Uri> uris;

    public UriAbsPathFilter(Integer[] filter_mode, String[] exclude, String[] includeExamples, List<Uri> uris) {
        super(filter_mode, exclude, includeExamples);
        this.uris = uris;
    }

    @Override
    public boolean accept(File file) {
        if (super.accept(file)) {
            if (file.isDirectory()) {
                file.listFiles(this);
            }else {
                uris.add(Uri.fromFile(file));
            }
        }
        return false;
    }
}
