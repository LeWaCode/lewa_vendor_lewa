package com.lewa.face.online;

import java.io.IOException;
import java.io.InputStream;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import android.util.Log;

import com.lewa.face.adapters.online.OnlineLauncherStyleAdapter;
import com.lewa.face.adapters.online.ThumbnailOnlineAdapter;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.server.intf.UrlParam;
import com.lewa.face.util.ThemeConstants;

/**
 * Icons、Launcher、ThemePkg online Activity
 * @author fulw
 *
 */
public class LauncherStyle extends OnlineBaseActivity{
    
    private static final String TAG = LauncherStyle.class.getSimpleName();
    
    @Override
    protected Object initUrl() {
        // TODO Auto-generated method stub
    	return UrlParam.newUrlParam(UrlParam.FAVORITEBAR);
//        return ThemeUtil.THEME_INFO_URL;
    }
    
    protected boolean parseExcel(InputStream inputStream) {
        Workbook book = null;
        try {  
            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("UTF-8"); 
            book = Workbook.getWorkbook(inputStream,ws);
            book.getNumberOfSheets();  
            Sheet sheet = book.getSheet(0); //get the first sheet 
            int totalRows = sheet.getRows();
            totalThemes = totalRows - 1;
              
            for (int i = 1; i < totalRows; ++i) {// the first row can't contain
                
                ThemeBase themeBase = new ThemeBase(null,ThemeConstants.LEWA,null,false);
                
                themeBase.setCnName(sheet.getCell(1,i).getContents());
                themeBase.setEnName(sheet.getCell(2,i).getContents());
                themeBase.setCnAuthor(sheet.getCell(3,i).getContents());
                themeBase.setEnAuthor(sheet.getCell(4,i).getContents());
                themeBase.setVersion(sheet.getCell(5,i).getContents());
                themeBase.setSize(sheet.getCell(6, i).getContents());
                themeBase.setPkg(sheet.getCell(7,i).getContents());
                //themeBase.setName(sheet.getCell(8,i).getContents());
                
                themeBases.add(themeBase);
                
            }
            Log.i(TAG, "Total theme numbers is " + totalThemes);
            return true; 
        } catch (Exception e) {  
           e.printStackTrace();
           return false;
        }finally{
            
            try {
                if(inputStream != null){
                    inputStream.close();
                    inputStream = null;
                }
                if(book != null){
                    book.close();
                    book = null;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }  
    }
    
  
    @Override
    protected ThumbnailOnlineAdapter onlineAdapterInstance() {
        
        return new OnlineLauncherStyleAdapter(mContext, themeBases, ThemeConstants.THEME_ONLINE);
    }
}
