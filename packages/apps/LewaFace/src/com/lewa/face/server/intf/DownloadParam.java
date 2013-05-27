/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.face.server.intf;

/**
 *
 * @author Administrator
 */
public class DownloadParam extends NetBaseParam{
    public int id = -1;

    public DownloadParam(int id,String type) {
        super(type);
        this.id = id;
    }

    @Override
    public String toString() {
        return  PrefixUrl + mType + "_download/" + id;
    }
    
    
}
