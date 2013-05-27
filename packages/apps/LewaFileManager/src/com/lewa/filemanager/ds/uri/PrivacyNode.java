/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.uri;

import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.config.Constants;
import java.io.File;

/**
 *
 * @author Administrator
 */
public class PrivacyNode extends SDCardNode {

    public PrivacyNode(FileInfo file) {
        super(file);
        changeDisplayName();
    }

    public void changeDisplayName() {
        if (((FileInfo) this.producingSource).getFile().getAbsolutePath().equals(Constants.PRIVACY_HOME)) {
            this.displayname = NavigationConstants.PRIVACYHOME;
        }
    }

    public PrivacyNode(FileInfo file, int clickPosition) {
        super(file, clickPosition);
    }
}
