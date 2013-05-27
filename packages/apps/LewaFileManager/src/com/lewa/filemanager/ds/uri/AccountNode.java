/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.uri;

/**
 *
 * @author Administrator
 */
public class AccountNode extends NavigationNode {

    public AccountNode(Object category, String displayname) {
        producingSource = category;
        this.displayname = displayname;
    }

    
}
