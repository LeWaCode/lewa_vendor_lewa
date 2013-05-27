/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.cpnt.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import com.lewa.base.highlight.KeyMatcher;
import com.lewa.base.highlight.TextHighLightDecorator;
import com.lewa.filemanager.beans.FileInfo;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author chenliang
 */
public class SearchAdapter extends CountAdapter {

    KeyMatcher matcher = new KeyMatcher("");
    TextHighLightDecorator decoratorHighLight;
    List<String> fieldstoimpose;
    int color;

    public SearchAdapter(Context context, AdaptInfo listViewHolder) {
        super(context, listViewHolder);
    }

    public void setHighlightInfo(String matcherText, String[] fieldstoimpose, int color) {
        try{
        decoratorHighLight = new TextHighLightDecorator(color);
        switchKeyWords(matcherText);
        this.fieldstoimpose = Arrays.<String>asList(fieldstoimpose);
        this.color = color;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void switchKeyWords(String matcherText) {
        this.matcher = new KeyMatcher(matcherText);
        decoratorHighLight.setMatcher(matcher);

    }

    @Override
    protected void findAndBindView(View convertView, int pos, Object item, String name, Object value) {
        if (decoratorHighLight != null && fieldstoimpose != null) {
            if (fieldstoimpose.contains(name)) {
                value = decoratorHighLight.getDecorated(value.toString());
            }            
        }
        super.findAndBindView(convertView, pos, item, name, value);
    }
    @Override
    public FileInfo getSimpleFileItem(int pos) {
       return  ((FileInfo)this.getItem(pos));
        
    }
}
