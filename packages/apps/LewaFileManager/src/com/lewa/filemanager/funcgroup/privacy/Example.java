/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.funcgroup.privacy;

import com.lewa.filemanager.ds.uri.NavigationConstants;
import com.lewa.base.Logs;

/**
 *
 * @author chenliang
 */
public class Example {

    public void main() {
        String seed = "test";
        String crypted = null;
        try {
            crypted = StringCrypter.encrypt(seed, NavigationConstants.SDCARD_PATH + "/1d1/2d2/3d3.jpg");
            Logs.i("", "encrypt --> " + crypted + " decrypted --> " + StringCrypter.decrypt(seed, crypted));
        } catch (Exception ex) {
        }
    }
}
