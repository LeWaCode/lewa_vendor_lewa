/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.sdcard;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author Administrator
 */
public class HiddenFilter implements FilenameFilter{
    private  boolean toHide;
    private int count;
    public HiddenFilter(boolean hide) {
        this.toHide = hide;
    }

    public int getCount() {
        return count;
    }

    public void clearCount(){
        count = 0;
    }
    public boolean accept(File dir, String name) {
        if(toHide&&name.startsWith(".")){
            return false;
        }else{
            count++;
            return true;
        }
    }
    
}
