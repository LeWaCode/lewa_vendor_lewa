package com.lewa.PIM.contacts;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactInfo {
    public static final String DISPLAYNAME = "displayname";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String IM = "im";
    public static final String POSTAL = "postal";
    public static final String ORGNIZATION = "orgnization";
    public static final String NICKNAME = "nickname";
    public static final String WEBSITE = "website";
    public static final String SIP = "sip";
    public static final String NOTE = "note";
    
    public static final String[] FIELD = new String[] {
        DISPLAYNAME, PHONE, EMAIL, IM, POSTAL, ORGNIZATION, NICKNAME, WEBSITE, SIP, NOTE};
    public HashMap<String, ArrayList<String[]>> fieldNameMap;
    
    public ContactInfo() {
        fieldNameMap = new HashMap<String, ArrayList<String[]>>();
        fieldNameMap.put(DISPLAYNAME, new ArrayList<String[]>());
        fieldNameMap.put(PHONE, new ArrayList<String[]>());
        fieldNameMap.put(EMAIL, new ArrayList<String[]>());
        fieldNameMap.put(IM, new ArrayList<String[]>());
        fieldNameMap.put(POSTAL, new ArrayList<String[]>());
        fieldNameMap.put(ORGNIZATION, new ArrayList<String[]>());
        fieldNameMap.put(NICKNAME, new ArrayList<String[]>());
        fieldNameMap.put(WEBSITE, new ArrayList<String[]>());
        fieldNameMap.put(SIP, new ArrayList<String[]>());
        fieldNameMap.put(NOTE, new ArrayList<String[]>());
    }
}
