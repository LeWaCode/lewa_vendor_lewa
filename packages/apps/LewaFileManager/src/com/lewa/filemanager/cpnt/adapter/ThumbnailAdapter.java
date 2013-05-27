package com.lewa.filemanager.cpnt.adapter;

import com.lewa.base.adapter.MapAdapter;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.View;

import android.widget.ImageView;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.images.FileIconHelper;
import com.lewa.base.images.ThumbnailBase;

public class ThumbnailAdapter extends MapAdapter {

    public int threadLoadBeginNum;
    public Handler uihandler;
    public int firstPicLoadingNum;
    public int lastestPicLoadingNum;
    public Thread loadThread;
    public int threadLoadEndNum;
    public boolean threadLoadFinish = true;
    public long loadInterval = 10;
    public static final int ScreenChildrenCount = 8;
    private int lazyPeriod;
    public boolean threadSleep;
    public FileIconHelper fileIconHelper;

    public void initPics(Integer headChild, Integer timeWait) {
    }

    public void setThreadLoadBeginNum(int threadLoadBeginNum) {
        this.threadLoadBeginNum = threadLoadBeginNum;
    }

    public void setThreadLoadEndNum(int threadLoadEndNum) {
        this.threadLoadEndNum = threadLoadEndNum;
    }

    public void setThreadLoadLazyPeriod(int lazyPeriod) {
        this.lazyPeriod = lazyPeriod;
    }

    public void setUihandler(Handler uihandler) {
        this.uihandler = uihandler;
    }
    public List<FileInfo> largeFiles = new ArrayList<FileInfo>();

    public List<FileInfo> getLargeImages() {
        return largeFiles;
    }

    public boolean hasLargeImagesToLoad() {
        return largeFiles.size() > 0;
    }

    public ThumbnailAdapter(Context context, AdaptInfo listViewHolder) {
        super(context, listViewHolder);
        fileIconHelper = new FileIconHelper(context);
    }

    @Override
    protected void findAndBindView(View convertView, int pos, Object item,
            String name, Object value) {
        // TODO Auto-generated method stub
        if (name.equals(Constants.FieldConstants.ICON_RES)) {
            if (value.toString().equals("-1")) {
                int theViewId = this.fieldnames.indexOf(name);
                ImageView iv = (ImageView) convertView.findViewById(this.viewsid[theViewId]);
                    fileIconHelper.setIcon(getThumbnailBase((FileInfo) item), iv);

            }
        }
        super.findAndBindView(convertView, pos, item, name, value);
    }

    protected ThumbnailBase getThumbnailBase(FileInfo fileInfo) {
        ThumbnailBase thumbnailBase = new ThumbnailBase(fileInfo.path);
        return thumbnailBase;
    }
}
