package com.lewa.PIM.contacts;

import java.util.ArrayList;

public class MmsInfoForContacts {
    
    public ArrayList<String> mNumber ;
    public String mDate;
    public String mName; 
    public String mId;
    public int mUnreadCount = 0;
    
    
    public MmsInfoForContacts(String id, String number, String name, String date){
        mNumber = new ArrayList<String>();
        mName = name;
        mNumber.add(number);
        mDate = date;
        mId = id;
    }  
}

