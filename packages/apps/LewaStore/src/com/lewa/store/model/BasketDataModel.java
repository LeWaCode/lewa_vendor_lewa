package com.lewa.store.model;

import java.util.List;
import java.util.Map;

public class BasketDataModel {

    //是否存在onMycloud
    public boolean isExistMyCloud(List<Map<String, Object>> cloudItemList,int appId){
        boolean flag=false;
        Map<String,Object> map=null;
        int size=cloudItemList.size();
        for(int i=0;i<size;i++){
            map=cloudItemList.get(i);
            if(null!=map && null!=map.get("appId") && Integer.parseInt(map.get("appId").toString())==appId){
                flag=true;
                break;
            }
        }
        return flag;
    }
    
    //是否在手机上
    public boolean isExistMyMobile(List<Map<String, Object>> mobileItemList,int appId){
        boolean flag=false;
        Map<String,Object> map=null;
        int size=mobileItemList.size();
        for(int i=0;i<size;i++){
            map=mobileItemList.get(i);
            if(null!=map && null!=map.get("appId") && Integer.parseInt(map.get("appId").toString())==appId){
                flag=true;
                break;
            }
        }
        return flag;
    }
    //是否在手机上
    public boolean isExistMyMobile(List<Map<String, Object>> mobileItemList,String pkgName){
        boolean flag=false;
        Map<String,Object> map=null;
        int size=mobileItemList.size();
        for(int i=0;i<size;i++){
            map=mobileItemList.get(i);
            if(null!=map && null!=map.get("packageName") && map.get("packageName").toString().trim().equals(pkgName)){
                flag=true;
                break;
            }
        }
        return flag;
    }
    
    public void removeMyMobile(List<Map<String, Object>> mobileItemList,String pkgName){
        if(this.isExistMyMobile(mobileItemList, pkgName)){
            Map<String,Object> map=null;
            int size=mobileItemList.size();
            for(int i=0;i<size;i++){
                map=mobileItemList.get(i);
                if(null!=map && null!=map.get("packageName") && map.get("packageName").toString().trim().equals(pkgName)){
                    mobileItemList.remove(i);
                    break;
                }
            }
        }        
    }
}
