/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.database;

import com.lewa.filemanager.ds.database.CategoryInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class CategoryItem {
    public boolean loadingFlag = true;
    public List<Map<String, Object>> categoryInfos = new ArrayList<Map<String, Object>>();
    public CategoryInfo categoryInfo;
}
