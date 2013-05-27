/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.uri;

import com.lewa.filemanager.beans.FileInfo;
import java.io.File;

/**
 *
 * @author Administrator
 */
public class SDCardNode extends NavigationNode {


    public SDCardNode(FileInfo file,int clickPosition) {
        this(file);
        this.defaultPosition = clickPosition;
    }

    public SDCardNode(FileInfo file) {
        this.producingSource = file;
        this.displayname = file.getName();
    }
    public SDCardNode(FileInfo file,String displayName) {
        this(file);
        this.displayname = displayName;
    }
}
