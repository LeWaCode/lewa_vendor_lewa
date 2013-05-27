/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.beans;

/**
 *
 * @author chenliang
 */
public class ReportInfo {

    public ReportInfo(String cleanName, String cleanPath) {
        this.firstline_name = cleanName;
        this.secondline_detail = cleanPath;
    }

    public String firstline_name;
    public String secondline_detail;
    public static final String FIRSTLINE_NAME = "firstline_name";
    public static final String SECONDLINE_DETAIL = "secondline_detail";

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ReportInfo other = (ReportInfo) obj;
        if ((this.firstline_name == null) ? (other.firstline_name != null) : !this.firstline_name.equals(other.firstline_name)) {
            return false;
        }
        return true;
    }
}
