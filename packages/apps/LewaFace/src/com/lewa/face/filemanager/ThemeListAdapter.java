package com.lewa.face.filemanager;



import java.util.ArrayList;
import java.util.HashMap;

import com.lewa.face.R;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ThemeListAdapter extends BaseAdapter{
    
    private ArrayList<ThemeFileInfo> mThemeFileArrayList;
    private Context mContext;
    private LayoutInflater mInflater;
    public static HashMap<Integer, Boolean> isSelected = new HashMap<Integer, Boolean>();
    
    public ThemeListAdapter(Context context,ArrayList<ThemeFileInfo> themeFileArrayList){
        this.mContext = context;
        this.mThemeFileArrayList = themeFileArrayList;
        this.mInflater = LayoutInflater.from(mContext);
        for(int i=0;i<mThemeFileArrayList.size();i++){
            isSelected.put(i, false);
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mThemeFileArrayList.size();
    }

    @Override
    public Object getItem(int postion) {
        // TODO Auto-generated method stub
        return mThemeFileArrayList.get(postion);
    }

    @Override
    public long getItemId(int postion) {
        // TODO Auto-generated method stub
        return postion;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ThemeFileInfo themeFile = null;
        ViewHolder viewHolder = null;
        themeFile = mThemeFileArrayList.get(position);
        if(convertView == null){
            convertView = (RelativeLayout) mInflater.inflate(R.layout.theme_fileitem, null);
            viewHolder = new ViewHolder();
            
            viewHolder.themeIcon = (ImageView) convertView.findViewById(R.id.themeicon);
            viewHolder.themeName = (TextView) convertView.findViewById(R.id.themename);
            viewHolder.themeSize = (TextView) convertView.findViewById(R.id.themesize);
            viewHolder.themeDate = (TextView) convertView.findViewById(R.id.themedate);
            viewHolder.themeCheckBox = (CheckBox) convertView.findViewById(R.id.select_theme);
            
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.themeIcon.setBackgroundDrawable(themeFile.getFileIcon());
        viewHolder.themeName.setText(themeFile.getFileName());
        viewHolder.themeSize.setText(themeFile.getFileSize());
        viewHolder.themeDate.setText(themeFile.getFileDate());
        viewHolder.themeCheckBox.setChecked(isSelected.get(position));
        return convertView;
    }

    public final class ViewHolder {
        public ImageView themeIcon;
        public TextView themeName;
        public TextView themeSize;
        public TextView themeDate;
        public CheckBox themeCheckBox;
    }  
    
    public void clearUp(){
        if(mThemeFileArrayList != null){
            mThemeFileArrayList.clear();
        }
        isSelected.clear();
    }
}
