package com.lewa.face.adapters.online;

import android.app.Activity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.lewa.face.R;
import com.lewa.face.adapters.ThumbnailImageLoader;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.lewa.face.loadimage.FileIconHelper;
import com.lewa.face.online.OnlineBaseActivity;
import com.lewa.face.util.Logs;
import java.io.File;

public abstract class ThumbnailOnlineAdapter extends BaseAdapter implements OnClickListener,OnSharedPreferenceChangeListener{

    public void onClick(View arg0) {
        comingSource = (Activity) mContext;
        Logs.i("============ comingSource " + comingSource);
    }
    public static Activity comingSource;
	private static final String TAG = ThumbnailOnlineAdapter.class.getSimpleName();
    
    private ArrayList<ThemeBase> mThemeBases = null;
    protected ArrayList<String> downloadedModels = null;
    private ArrayList<ImageView> flagViews = new ArrayList<ImageView>();
    
    private LayoutInflater mInflater = null;
    
    private ThumbnailImageLoader thumbnailImageLoader = null;
    
    protected Context mContext = null;
    protected String url = null;
    protected String thumbnailPath = null;
    SharedPreferences.Editor editor = null; // add by zjyu 
    private HashMap<Integer, Bitmap> statusFlags = new HashMap<Integer, Bitmap>();
    
    private Bitmap flagDownloaded = null;
    private Bitmap flagDownloading = null;
    
    protected SharedPreferences sharedPreferences = null;
    private FileIconHelper fileIconHelper;
    
    public ThumbnailOnlineAdapter(Context context,ArrayList<ThemeBase> themeBases,int themeType){
        mContext = context;
        mThemeBases = themeBases;
        mInflater = LayoutInflater.from(context);
        editor = context.getSharedPreferences("DOWNLOADED", Context.MODE_PRIVATE).edit();
        
        //thumbnailImageLoader = new ThumbnailImageLoader(30,15, mContext);
        thumbnailImageLoader = new ThumbnailImageLoader(10,10, mContext);
        
        int count = mThemeBases.size();
        for(int i=0;i<count;i++){
            statusFlags.put(i, null);
        }
        
        downloadedModels = listDownloadFiles();
        
        flagDownloaded = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.theme_downloaded);
        flagDownloading = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.theme_downloading);
        
        sharedPreferences = mContext.getSharedPreferences("DOWNLOADED", Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        
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
            viewCache.thumbnail.setOnClickListener(this);
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

        int flag = addFlag(themeBase);
        
        if(flag == ThemeConstants.DOWNLOADED){
           statusFlags.put(position, flagDownloaded);
           editor.putLong(themeBase.getPkg(), ThemeConstants.DOWNLOADED);
           editor.commit();
	   
        }else if(flag == ThemeConstants.DOWNLOADING){
            statusFlags.put(position, flagDownloading);
        }else {// add by zjyu  2012.5.24 initialize the sharedPreferences when first come in download page
            editor.putLong(themeBase.getPkg(), ThemeConstants.DOWNLOADFAIL);
            editor.commit();
        }
        
        /**
         * 对应位置有标签的则标记标签，否则为null
         */
        viewCache.statusFlag.setImageBitmap(statusFlags.get(position));
        
        flagViews.add(viewCache.statusFlag);
        
        /**
         * 将对应位置的标签Image给定pkg和position
         */
        viewCache.statusFlag.setTag(themeBase.getPkg());
        
            if (!OnlineBaseActivity.mBusy) {
//                //当前不处于加载数据的忙碌时期（没有滚屏），则显示数据
//
              initPath(themeBase);
                thumbnailImageLoader.loadImage(viewCache.thumbnail, position, thumbnailPath, url);
            } else {
//                //当前处于滚屏阶段，不加载数据，直接显示Cache中的数据或者默认图片
                Bitmap bitmap = thumbnailImageLoader.getBitmapFromCache(position);
                if (bitmap != null) {
                    viewCache.thumbnail.setImageBitmap(bitmap);
                } else {
                    viewCache.thumbnail.setImageBitmap(thumbnailImageLoader.defaultBitmap);
                }
            }
        
        viewCache.thumbnail.setTag(themeBase);
        
        viewCache.thumbnail.setOnClickListener(this);
        
        return convertView;
    }
    
    public class DownloadTask extends AsyncTask {

        ImageView imageview;
        String thumbnailPath;
        int defaultBg;
        String url;

        public DownloadTask(ImageView imageview, String thumbnailPath, String url, int defaultBg) {
            this.imageview = imageview;
            this.thumbnailPath = thumbnailPath;
            this.defaultBg = defaultBg;
            this.url = url;
        }

        @Override
        protected Object doInBackground(Object... arg0) {
            Bitmap bitmap = null;

            File thumbnailFile = new File(thumbnailPath);
            if (!thumbnailFile.exists()) {
                /**
                 * 如果当前Task被cancel，则没必要继续执行
                 */
                if (isCancelled()) {
                    return null;
                }
                bitmap = thumbnailImageLoader.downloadBitmap(thumbnailFile, url, null);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            notifyDataSetChanged();
        }
    }
    private class ViewCache{
        public ImageView thumbnail;
        public ImageView statusFlag;
        public TextView theme_name;
    }
    
    public void onDestroy(){
        if(mThemeBases != null){
        	mThemeBases.clear();
        }
        for(Entry<Integer, Bitmap> entry : statusFlags.entrySet()){
            Bitmap downloaded = entry.getValue();
            if(downloaded != null && !downloaded.isRecycled()){
                downloaded.recycle();
                downloaded = null;
            }
        }
        statusFlags.clear();
        
        if(flagDownloaded != null && !flagDownloaded.isRecycled()){
            flagDownloaded.recycle();
            flagDownloaded = null;
        }
        
        flagViews.clear();
        
        thumbnailImageLoader.clearCache();
    }
    
    protected abstract void initPath(ThemeBase themeBase);
    
    /**
     * 此方法主要是遍历指定的文件夹，以确定已经下载的文件，并给文件贴上相应的标签
     * @return
     */
    protected abstract ArrayList<String> listDownloadFiles(); 
    
    /**
     * 
     * @param themeBase
     * @return 0(DOWNLOADED)：已下载  1(DOWNLOADING)：下载中  -1(Nothing):无任何标签
     */
    protected abstract int addFlag(ThemeBase themeBase);

    /**
     * 如果xml发生了变化也就意味着数据也跟着变化了，key发生变化的那个key-value
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
        
        this.sharedPreferences = sharedPreferences;
        
        if(sharedPreferences.getLong(key, -1) == -1){ //如果状态是-1，那么没必要继续执行
            
            return;
        }else if(sharedPreferences.getLong(key, -1) == ThemeConstants.DOWNLOADING){ //如果状态是DOWNLOADING，那么贴正在下载的标签
            
            addFlagToThumbnail(key,flagDownloading);
            
        }else if(sharedPreferences.getLong(key, -1) == ThemeConstants.DOWNLOADFAIL){//如果状态是DOWNLOADFAIL，那么去掉正在下载的标签
            
            addFlagToThumbnail(key,null);
            ThemeUtil.dcontrolFlag = "true"; //add by zjyu
            
        }else{ // DOWNLOADED,贴已下载的标签
            /**
             * 更新已经下载文件的数量
             */
            downloadedModels = listDownloadFiles();
            
            addFlagToThumbnail(key, flagDownloaded);
            ThemeUtil.dcontrolFlag = "true"; //add by zjyu
        }
        
       
    }
    
    private void addFlagToThumbnail(String key,Bitmap flag){
        int size = flagViews.size();
        for(int i=0;i<size;i++){
            ImageView flagView = flagViews.get(i);
            String pkg = (String) flagView.getTag();
            if(pkg.equals(key) && sharedPreferences.contains(key)){
                flagView.setImageBitmap(flag);
            }
        }
    }
    
}
