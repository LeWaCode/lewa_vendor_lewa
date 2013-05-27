/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.funcgroup.privacy;

import android.content.Context;
import com.lewa.filemanager.funcgroup.privacy.StringCrypter;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.beans.FileUtil;
import com.lewa.base.Logs;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class PrivacyInfo extends FileInfo {

    private String encryptedName;
    public String encryptedPath;

    public PrivacyInfo(File file, Context context) {
        super(file, context);
    }

    @Override
    public File getFile() {
        if(this.encryptedPath==null){
            this.buildFile();
        }
        return new File(this.encryptedPath);
    }

    @Override
    public void buildFile() {
        super.buildFile();
        privacySetup();
    }

    public String getEncryptedName() {
        return encryptedName;
    }

    public void privacySetup() {
        try {
            String tmpName = this.getName();
            setName(StringCrypter.decrypt(getName()));
            this.encryptedName = tmpName;

            String tmpPath = this.getPath();
            setPath(FileUtil.getParent(getPath()) + "/" + this.encryptedName);
            this.encryptedPath = tmpPath;

            Logs.i("", "path " + encryptedPath + " name " + encryptedName);
        } catch (Exception ex) {
            Logger.getLogger(PrivacyInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        setType(FileUtil.getRealExtension(getName()));
    }



    @Override
    public void buildFile(File file) throws NumberFormatException {
        super.buildFile(file);
        privacySetup();
    }
}
