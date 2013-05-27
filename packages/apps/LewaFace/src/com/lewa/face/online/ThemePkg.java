package com.lewa.face.online;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;


import com.lewa.face.R;
import com.lewa.face.adapters.online.OnlineThemePkgAdapter;
import com.lewa.face.adapters.online.ThumbnailOnlineAdapter;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.pojos.ThemeModelInfo;
import com.lewa.face.server.intf.NetBaseParam;
import com.lewa.face.server.intf.UrlParam;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeUtil;

import android.util.Log;

/**
 * 
 * @author fulw
 *
 */
public class ThemePkg extends OnlineBaseActivity{
    
    private static final String TAG = ThemePkg.class.getSimpleName();
   
    @Override
    protected Object initUrl() {
        // TODO Auto-generated method stub
        return UrlParam.newUrlParam(NetBaseParam.THEMEPACKAGE);
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
            
            ThemeModelInfo themeModelInfo = new ThemeModelInfo(ThemeConstants.LEWA);
            
            ArrayList<Integer> modelNames = new ArrayList<Integer>();
            
            modelNames.add(R.string.theme_model_lockscreen_wallpaper);
            modelNames.add(R.string.theme_model_wallpaper);
            modelNames.add(R.string.theme_model_lockscreen_style);
            modelNames.add(R.string.theme_model_icon_style);
            modelNames.add(R.string.theme_model_launcher);
            
            themeModelInfo.setModelNames(modelNames);
            
            for (int i = 1; i < totalRows; ++i) {// the first row can't contain
            	
                ThemeBase themeBase = new ThemeBase(null,ThemeConstants.LEWA,null,false);
                
                themeBase.setCnName(sheet.getCell(1,i).getContents());//主题中文名字
                themeBase.setEnName(sheet.getCell(2,i).getContents());//主题英文名字
                themeBase.setCnAuthor(sheet.getCell(3,i).getContents());//作者中文名字
                themeBase.setEnAuthor(sheet.getCell(4,i).getContents());//作者英文名字
                themeBase.setVersion(sheet.getCell(5,i).getContents());//主题版本号
                themeBase.setSize(sheet.getCell(6, i).getContents());//主题大小
                themeBase.setPkg(sheet.getCell(7,i).getContents());//主题包文件名
                //themeBase.setName(sheet.getCell(8,i).getContents());
                if(sheet.getCell(9,i).getContents().equals("0")){//是否包含锁屏
                    themeBase.setContainLockScreen(false);
                }else {
                    themeBase.setContainLockScreen(true);
                }
                
                themeBase.setModelNum("5");
                
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
        
        return new OnlineThemePkgAdapter(mContext, themeBases, ThemeConstants.THEME_ONLINE);
    }
 
}
