package com.lewa.face.server.intf.xlsimpl;

import com.lewa.face.R;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.pojos.ThemeModelInfo;
import com.lewa.face.server.intf.NetHelper;
import com.lewa.face.server.intf.PageResolver;
import com.lewa.face.util.ThemeConstants;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
//import jxl.Sheet;
//import jxl.Workbook;
//import jxl.WorkbookSettings;

/**
 *  瀹屾垚涓昏鍔熻兘锛�
 *  瀵艰埅(hasNext),宸￠〉(setPageNo)锛岃幏鍙栧垎椤佃褰�getResult)
 */
public class PageLocalParser extends PageResolver {

    public List entities;

    @Override
    public List pretPages(int n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PageLocalParser(int pageSize) {
        super(pageSize);
    }

    /**
     * 椤靛唴鐨勬暟鎹垪琛�
     */
    public List getEntities() {
        if (entities == null) {
            throw new IllegalStateException("data not assigned,invalid use");
        }
        return entities;
    }

    public void setEntities(List result) {
        this.entities = result;
    }

    @Override
    public List getRequestedEntities() {
        return this.getEntities();
    }
    
    
    
    //-------------- 鍒嗛〉

    /**
     * 鑾峰緱褰撳墠椤甸璁板綍鍦ㄥ叏闆嗕腑鐨勪綅缃�
     */
    private int getHeadResultInCurrentPage() {
        if (pageNo < 1 || (pageSize < 1 && pageSize != -1)) {
            return -1;
        } else {
            return ((pageNo - 1) * pageSize);
        }
    }

    @Override
    public List nextPage() {
        if (this.hasNext()) {
            pageNo++;
            return currentPage();
        }
        return null;
    }

    public List currentPage() {
        if (this.getPageSize() == -1) {
            return this.entities;
        }
        int startIndex = getHeadResultInCurrentPage();
        if (startIndex == -1) {
            throw new IllegalArgumentException("Index of first record in current page out of bounds: -1");
        }
        return this.entities.subList(startIndex, startIndex + this.getPageSize());
    }

    @Override
    public void init() {
    }

    @Override
    public boolean count(Object... ifneeded) {
        String url = ifneeded.length==0? this.clientResolver.getUrl() : ifneeded[0].toString();
        InputStream is = NetHelper.getNetInputStream(url);
        this.setEntities(parsePkgExcel(is));
        if (is != null) {
            try {
                is.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            is = null;
        }
        if (this.getEntities() == null) {
            return false;
        }
        this.totalCount = this.getEntities().size();
        this.updatePagination();
        return true;
    }

    @Override
    public void requestList() {
        if (this.getEntities() == null && this.clientResolver != null) {
            count(this.clientResolver.getUrl());
        } else {
            throw new IllegalStateException("state invalid for som params wrong");
        }
    }

    public static List<ThemeBase> parsePkgExcel(InputStream inputStream) {
        List<ThemeBase> themeBases = new ArrayList<ThemeBase>();
//        Workbook book = null;
//        try {
//            WorkbookSettings ws = new WorkbookSettings();
//            ws.setEncoding("UTF-8");
//            book = Workbook.getWorkbook(inputStream, ws);
//            book.getNumberOfSheets();
//            Sheet sheet = book.getSheet(0); //get the first sheet 
//            int totalRows = sheet.getRows();
////            totalThemes = totalRows - 1;
//
//            ThemeModelInfo themeModelInfo = new ThemeModelInfo(ThemeConstants.LEWA);
//
//            ArrayList<Integer> modelNames = new ArrayList<Integer>();
//
//            modelNames.add(R.string.theme_model_lockscreen_wallpaper);
//            modelNames.add(R.string.theme_model_wallpaper);
//            modelNames.add(R.string.theme_model_lockscreen_style);
//            modelNames.add(R.string.theme_model_icon_style);
//            modelNames.add(R.string.theme_model_launcher);
//
//            themeModelInfo.setModelNames(modelNames);
//
//            for (int i = 1; i < totalRows; ++i) {// the first row can't contain
//
//                ThemeBase themeBase = new ThemeBase(null, ThemeConstants.LEWA, null, false);
//
//                themeBase.setCnName(sheet.getCell(1, i).getContents());//锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
//                themeBase.setEnName(sheet.getCell(2, i).getContents());//锟斤拷锟斤拷英锟斤拷锟斤拷锟斤拷
//                themeBase.setCnAuthor(sheet.getCell(3, i).getContents());//锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
//                themeBase.setEnAuthor(sheet.getCell(4, i).getContents());//锟斤拷锟斤拷英锟斤拷锟斤拷锟斤拷
//                themeBase.setVersion(sheet.getCell(5, i).getContents());//锟斤拷锟斤拷姹撅拷锟�
//                themeBase.setSize(sheet.getCell(6, i).getContents());//锟斤拷锟斤拷锟叫�
//                themeBase.setPkg(sheet.getCell(7, i).getContents());//锟斤拷锟斤拷锟斤拷募锟斤拷锟�
//                //themeBase.setName(sheet.getCell(8,i).getContents());
//                if (sheet.getCell(9, i).getContents().equals("0")) {//锟角凤拷锟斤拷锟斤拷锟�
//                    themeBase.setContainLockScreen(false);
//                } else {
//                    themeBase.setContainLockScreen(true);
//                }
//
//                themeBase.setModelNum("5");
//
//                themeBases.add(themeBase);
//
//            }
//            return themeBases;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        } finally {
//
//            try {
//                if (inputStream != null) {
//                    inputStream.close();
//                    inputStream = null;
//                }
//                if (book != null) {
//                    book.close();
//                    book = null;
//                }
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
        return themeBases;
    }
}