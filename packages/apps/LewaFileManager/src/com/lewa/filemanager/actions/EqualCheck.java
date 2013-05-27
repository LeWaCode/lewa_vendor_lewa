/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.actions;


import com.lewa.filemanager.actions.sort.ObjectSort;
import com.lewa.filemanager.beans.FileInfo;

/**
 *
 * @author Administrator
 */
public class EqualCheck extends ObjectSort{
    public boolean result;
    int tmp;
    public EqualCheck(int policy, int seq) {
        super(policy, seq);
    }

    public boolean isResult() {
        return result;
    }

    @Override
    public int compare(FileInfo one, FileInfo another) {
        if((tmp = super.compare(one, another))==0){
        result = true;            
        }
        return tmp;
    }
    
}
