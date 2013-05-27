/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.database.service;

import android.net.Uri;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Administrator
 */
public class FileImage {

    public Uri operUri;
    public Map<Integer, String> errorRecords = new TreeMap<Integer, String>(new Comparator<Integer>() {

        public int compare(Integer str0, Integer str1) {
            return str0 - str1;
        }
    });
    public Map<String, Integer> records = new TreeMap<String, Integer>(new Comparator<String>() {

        public int compare(String str0, String str1) {
            return str0.compareTo(str1);
        }
    });

    public FileImage(Uri operUri) {
        this.operUri = operUri;
    }
}
