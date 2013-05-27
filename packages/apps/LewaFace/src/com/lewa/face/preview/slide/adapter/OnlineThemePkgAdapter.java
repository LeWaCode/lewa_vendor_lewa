package com.lewa.face.preview.slide.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.lewa.view.LewaPagerView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.util.Logs;
import com.lewa.face.util.ThemeConstants;

public class OnlineThemePkgAdapter extends OnlinePkgSlide {
	private List<String> priviewPaths;
	public OnlineThemePkgAdapter(ArrayList<String> source, Context context,
			ThemeBase themeBase) {
		super(source, context, themeBase);
		priviewPaths = themeBase.previewpath;
		Logs.i("== OnlineThemePkgAdapter " +priviewPaths);
		// TODO Auto-generated constructor stub
	}
	@Override
    public Object instantiateItem(View container, int position) {

        ImageView imageView = new ImageView(mContext);
        
        Bitmap bitmap = slideImageLoader.getBitmapFromCache(position);
//        if (this instanceof OnlinePackageSlide) {
            if (bitmap != null && !bitmap.isRecycled()) {
                imageView.setImageBitmap(bitmap);
            } else {
                String previewPath = priviewPaths.get(position);
                if (previewPath.startsWith("//")) {
                    previewPath = previewPath.substring(1);
                }
                String localpath = new StringBuilder().append(ThemeConstants.THEME_ONLINE_PREVIEW).append("/").append(themeBase.getName()).append(previewPath.substring(previewPath.lastIndexOf("/"))).toString();
                Logs.i("== == 1 " + localpath);
                localpath = localpath.substring(0, localpath.lastIndexOf("_")) + localpath.substring(localpath.length() - 4);
                Logs.i("== == 2 " + localpath);
                slideImageLoader.loadImage(imageView, position, localpath, previewPath);
            }
//        } else {
//        if(bitmap != null){
//            imageView.setImageBitmap(bitmap);
//        }else {
//            String previewPath = mSource.get(position);
//            slideImageLoader.loadImage(imageView, position, previewPath, initUrl(previewPath));
//        }
//        }
        ((LewaPagerView)container).addView(imageView, 0, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        return imageView;
    }
}
