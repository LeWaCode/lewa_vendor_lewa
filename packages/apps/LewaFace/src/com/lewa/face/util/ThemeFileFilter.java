package com.lewa.face.util;

import java.io.File;
import java.io.FileFilter;

/**
 * 
 * @author fulw
 *
 */
public class ThemeFileFilter implements FileFilter{
    
    private boolean mDirectoryFilter = false;
    private String mFilterWords = null;
    //private String mIndexOfFilterWords = null;
    /**
     * 0 : endsWith
     * 1 : startsWith
     * 2 : indexOf
     */
    private int mFilterModel = 0;
    
    /**
     * ��ĳĳ�ؼ��ֽ�β
     */
    public static final int ENDSWITH = 0;
    /**
     * ��ĳĳ�ؼ��ֿ�ͷ
     */
    public static final int STARTSWITH = 1;
    /**
     * ֻҪ����ĳĳ�ؼ���
     */
    public static final int INDEXOF = 2;
    
    /**
     * ���˰���ĳĳ�ؼ��֣���INDEXOF�෴
     */
    public static final int NOT = 3;
    
    /**
     * �����ļ���
     * @param directoryFilter
     */
    public ThemeFileFilter(boolean directoryFilter){
        mDirectoryFilter = directoryFilter;
    }
    
    /**
     * �����ļ�
     * @param filterWords
     * @param filterModel
     */
    public ThemeFileFilter(String filterWords,int filterModel){
        mFilterWords = filterWords;
        mFilterModel = filterModel;
    }

    @Override
    public boolean accept(File file) {
        
        if(mDirectoryFilter){
            if(file.isDirectory()){
                return true;
            }else{
                return false;
            }
        }
        
        String pathName = file.getName();
        
        if(mFilterWords != null){
            if(mFilterModel == ENDSWITH){
                if(pathName.endsWith(mFilterWords)){
                    return true;
                }else{
                    return false;
                }
            }else if(mFilterModel == STARTSWITH){
                if(pathName.startsWith(mFilterWords)){
                    return true;
                }else{
                    return false;
                }
            }else if(mFilterModel == INDEXOF){
                if(pathName.toLowerCase().indexOf(mFilterWords.toLowerCase()) != -1){
                    return true;
                }else{
                    return false;
                }
            }else if(mFilterModel == NOT){
                if(pathName.indexOf(mFilterWords) != -1){
                    return false;
                }else {
                    return true;
                }
            }
        }
        
        return false;
    }

}
