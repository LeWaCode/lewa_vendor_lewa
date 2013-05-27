package com.lewa.PIM.contacts;

import java.util.ArrayList;

public class PageManager {
    private static ArrayList<ArrayList<String>> cIds;
    private static int pageNo;

    public static void setSimilarIds(ArrayList<ArrayList<String>> ids) {
        cIds = ids;
    }
    
    public static int getTotalCount() {
        return cIds.size();
    }
    
    public static boolean isHasPre() {
        return (pageNo > 0);
    }
    
    public static boolean isHasNext() {
        return (pageNo < cIds.size() - 1);
    }
    
    public static ArrayList<String> getNextIds() {
        return cIds.get(++pageNo);
    }
    
    public static ArrayList<String> getPreIds() {
        return cIds.get(--pageNo);
    }
    
    public static void updatePageNo() {
        cIds.remove(pageNo);
    }
    
    public static ArrayList<String> getFirstIds() {
        if (cIds.size() >= 1) {
            pageNo = 0;
            return cIds.get(pageNo);
        }
        return null;
    }
}
