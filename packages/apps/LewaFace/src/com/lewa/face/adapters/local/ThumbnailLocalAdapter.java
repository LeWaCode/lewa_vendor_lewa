package com.lewa.face.adapters.local;

import java.util.ArrayList;
import java.util.HashMap;

import com.lewa.face.R;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.util.ThemeActions;
import com.lewa.face.util.ThemeUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.lewa.face.loadimage.FileIconHelper;
import com.lewa.face.loadimage.ThumbnailBase;
import com.lewa.face.local.LocalBaseActivity;

public abstract class ThumbnailLocalAdapter extends BaseAdapter implements OnClickListener,OnSharedPreferenceChangeListener{

	private static final String TAG = ThumbnailLocalAdapter.class.getSimpleName();
    
    protected ArrayList<ThemeBase> mThemeBases = null;
    
    private LayoutInflater mInflater = null;
    
    protected Context mContext = null;
    
    protected HashMap<Integer, Bitmap> statusFlags = new HashMap<Integer, Bitmap>();
    
    
    protected Bitmap statusFlagBitmap = null;
    
    protected SharedPreferences sharedPreferences = null;
    
    /**
     * 保存本地缩略图的缓存
     */
    protected ArrayList<Bitmap> thumbnailCache = new ArrayList<Bitmap>();
    
    private Handler mHandler = null;
    
    private Bitmap defaultBitmap = null;
    
    FileIconHelper fileIconHelper;
    public ThumbnailLocalAdapter(Context context,ArrayList<ThemeBase> themeBases,int themeType,Handler handler){
        mContext = context;
        mThemeBases = themeBases;
        mInflater = LayoutInflater.from(context);
        mHandler = handler;
        
        int count = mThemeBases.size();
        for(int i=0;i<count;i++){
            statusFlags.put(i, null);
            thumbnailCache.add(i,null);
        }
        statusFlagBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.theme_using);
        sharedPreferences = mContext.getSharedPreferences("CURRENT_USING", Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        
        defaultBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.lewa);
        fileIconHelper = new FileIconHelper(context);
    }

    @Override
    public int getCount() {
        if(mThemeBases != null){
            return mThemeBases.size(); 
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(mThemeBases != null){
            return mThemeBases.get(position);
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
            convertView = mInflater.inflate(R.layout.theme_grid_item_thumbnail, null);
            
            viewCache = new ViewCache();
            viewCache.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            viewCache.statusFlag = (ImageView) convertView.findViewById(R.id.status_flag);
            viewCache.theme_name = (TextView) convertView.findViewById(R.id.theme_name);
            viewCache.thumbnailframe= (ImageView) convertView.findViewById(R.id.thumbnailframe);
            convertView.setTag(viewCache);
        }else {
            viewCache = (ViewCache) convertView.getTag();
        }
        
            
        ThemeBase themeBase = mThemeBases.get(position);
        
        if(ThemeUtil.isEN){
            viewCache.theme_name.setText(themeBase.getEnName());
        }else {
            viewCache.theme_name.setText(themeBase.getCnName());
        }
        
        attachLabel(themeBase,position);
        
        viewCache.statusFlag.setImageBitmap(statusFlags.get(position));
        
        if (!LocalBaseActivity.mBusy) {
            String thumbnailPath = getThumbnailPath(themeBase);
            loadImage(viewCache.thumbnail, position , thumbnailPath);
        } else {
            //当前处于滚屏阶段，不加载数据，直接显示Cache中的数据或者默认图片
        	Bitmap bitmap = getBitmapFromCache(position);
            if(bitmap != null){
                viewCache.thumbnail.setImageBitmap(bitmap);
            }else {
                viewCache.thumbnail.setImageBitmap(defaultBitmap);
            }
        }
//        fileIconHelper.setIcon(new ThumbnailBase(getThumbnailPath(themeBase)), viewCache.thumbnail, viewCache.thumbnailframe, R.drawable.lewa, R.drawable.lewa, R.drawable.lewa);
        viewCache.thumbnail.setTag(themeBase);
        viewCache.thumbnail.setOnClickListener(this);
            
        return convertView;
    }
    
    private class ViewCache{
        public ImageView thumbnailframe;
        public ImageView thumbnail;
        public ImageView statusFlag;
        public TextView theme_name;
    }
    
    public void registerReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ThemeActions.ADD_THEME_OVER);
        intentFilter.addAction(ThemeActions.DELETE_THEME_OVER);
        intentFilter.addAction(ThemeActions.APPLAY_THEME_OVER);
        mContext.registerReceiver(themeChanged, intentFilter);
    }
    
    public void onDestroy(){
        
        mContext.unregisterReceiver(themeChanged);
        
        if(mThemeBases != null){
        	mThemeBases.clear();
        }
        int size = thumbnailCache.size();
        for(int i=0;i<size;i++){
        	Bitmap bitmap = thumbnailCache.get(i);
        	
    		if(bitmap != null && !bitmap.isRecycled()){
    			bitmap.recycle();
    			bitmap = null;
    		}
        	
        }
        thumbnailCache.clear();
    }
    
    private BroadcastReceiver themeChanged = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(ThemeActions.ADD_THEME_OVER.equals(action)
                    || ThemeActions.DELETE_THEME_OVER.equals(action)
                    || ThemeActions.APPLAY_THEME_OVER.equals(action)){
            	
                mThemeBases = updateThemeBases();
                
                int count = mThemeBases.size();
                for(int i=0;i<count;i++){
                    statusFlags.put(i, null);
                    thumbnailCache.add(i,null);
                }
                
                Message message = mHandler.obtainMessage();
                message.what = 1;
                mHandler.sendMessage(message);
                
            }

        }
    };
    
    protected abstract ArrayList<ThemeBase> updateThemeBases();
    
   /**
     * 贴"使用中","已下载"等标签
     * @param imageView
     */
    protected abstract void attachLabel(ThemeBase themeBase,int position);
    
    protected abstract String getThumbnailPath(ThemeBase themeBase);
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        this.sharedPreferences = sharedPreferences;
        
    }
    
    private void loadImage(ImageView imageView,int position,String thumbnailPath){
    	Bitmap bitmap = getBitmapFromCache(position);
    	if(bitmap == null){
    		bitmap = BitmapFactory.decodeFile(thumbnailPath);
    	    thumbnailCache.add(position, bitmap);
    		imageView.setImageBitmap(bitmap);
    	}else {
			imageView.setImageBitmap(bitmap);
		}
    }
    
    private Bitmap getBitmapFromCache(int position){
    	Bitmap bitmap = thumbnailCache.get(position);
		if(bitmap != null){
			return bitmap;
		}
    	return null;
    }
}
