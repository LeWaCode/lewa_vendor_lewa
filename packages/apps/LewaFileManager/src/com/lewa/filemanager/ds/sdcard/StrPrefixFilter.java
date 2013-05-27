/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.sdcard;

import com.lewa.filemanager.beans.FileInfo;
import java.io.File;
import java.util.List;

/**
 *
 * @author chenliang
 */
public class StrPrefixFilter extends TypeFilter {

    private List<FileInfo> infos;
    private String name;
    
    @Override
    public boolean accept(File file) {
        if(!super.accept(file)){
            return false;
        }
        if(file.getName().startsWith(name)){
            FileInfo fi = new FileInfo();
            fi.setName(file.getName());
            infos.add(fi);
        }
        return false;
    }

    public StrPrefixFilter(Integer[] filter_mode, String[] exclude,String[] includeExamples ,List<FileInfo> infos,String name) {
        super(filter_mode, exclude,includeExamples);
        this.infos = infos;
        this.name = name;
    }
}
