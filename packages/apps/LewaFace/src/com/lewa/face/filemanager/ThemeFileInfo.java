package com.lewa.face.filemanager;



import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.drawable.Drawable;

/**
 * Theme file infomations,such as: 
 * fileName = "Lewa.lwt"
 * fileSize = "2.5M"
 * fileDate = "2012年03月05日"
 * filePath = "/mnt/sdcard/theme/lwt/Lewa.lwt"
 * fileIcon = "fold.png"
 * @author fulw
 *
 */
public class ThemeFileInfo implements java.io.Serializable{


	/**
     * 
     */
    private static final long serialVersionUID = -5143908171891497774L;
    
    private String fileName;
    private String fileSize;
    private String fileDate;
    private String filePath;
    private Drawable fileIcon;
    
    public ThemeFileInfo(){}

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(long length) {
    	
    	StringBuilder sb = new StringBuilder();
    	long file_B = length%1024;
        long file_KB = length/1024;
        long file_KB_B = file_B;
        long file_MB = file_KB/1024;
        long file_MB_KB = file_KB%1024;
        // The order can't change
        if(file_MB > 0){
        	sb.append(file_MB).append(".").append(String.valueOf(file_MB_KB).substring(0, 1)).append("MB");
        }else if(file_KB > 0){
        	sb.append(file_KB).append(".").append(String.valueOf(file_KB_B).substring(0, 1)).append("KB");
        }else if(file_B > 0){
        	sb.append(length).append("B");
        }
        this.fileSize = sb.toString();
    }

    public String getFileDate() {
        return fileDate;
    }

    public void setFileDate(long milliseconds) {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        this.fileDate = sdf.format(new Date(milliseconds));
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Drawable getFileIcon() {
        return fileIcon;
    }

    public void setFileIcon(Drawable fileIcon) {
        this.fileIcon = fileIcon;
    }
        
    
}
