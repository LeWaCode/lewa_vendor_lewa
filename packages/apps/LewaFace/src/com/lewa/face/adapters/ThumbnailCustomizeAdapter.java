package com.lewa.face.adapters;

import java.util.ArrayList;

import com.lewa.face.R;
import com.lewa.face.util.ThemePair;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author fulw
 *
 */
public class ThumbnailCustomizeAdapter extends BaseAdapter implements OnClickListener{
    
    private static final String TAG = ThumbnailCustomizeAdapter.class.getSimpleName();
    
    private final ArrayList<Integer> mModelNames;
    private final ArrayList<Integer> mModelBmps;
    private final ArrayList<String> mModelActions;
    
    private final LayoutInflater mInflater;
    
    private final Context mContext;
    
    
    public ThumbnailCustomizeAdapter(int themeType,ArrayList<Integer> modelNames,ArrayList<Integer> modelBmps,ArrayList<String> modelActions,Context context){
        mModelNames = modelNames;
        mModelBmps = modelBmps;
        mModelActions = modelActions;
        mContext = context;
        mInflater = LayoutInflater.from(context);  
    }

    @Override
    public int getCount() {
        if(mModelNames != null){
            return mModelNames.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(mModelNames != null){
            return mModelNames.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewCache viewCache = null;
        
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.theme_grid_item_customize, null);
            
            viewCache = new ViewCache();
            viewCache.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            viewCache.themeModeName = (TextView) convertView.findViewById(R.id.model_name);
            
            convertView.setTag(viewCache);
        }else {
            viewCache = (ViewCache) convertView.getTag();
        }
        
        viewCache.thumbnail.setImageResource(mModelBmps.get(position));
        ThemePair<Integer, String> pair = new ThemePair<Integer, String>(position, mModelActions.get(position));
        viewCache.thumbnail.setTag(pair);
        viewCache.thumbnail.setOnClickListener(this);
        
        viewCache.themeModeName.setText(mModelNames.get(position));
        
        return convertView;
    }
    
    public class ViewCache{
        public ImageView thumbnail;
        public TextView themeModeName;
    }
    
    @Override
    public void onClick(View v) {
        @SuppressWarnings("unchecked")
        ThemePair<Integer, String> pair = (ThemePair<Integer, String>) v.getTag();
        Intent intent = new Intent();
        /**
         * pair.first 0:lockscreen wallpaper 1:wallpaper
         */
        intent.putExtra("modelName", pair.first);
        intent.setAction(pair.second);
        mContext.startActivity(intent);
    }
    
}
