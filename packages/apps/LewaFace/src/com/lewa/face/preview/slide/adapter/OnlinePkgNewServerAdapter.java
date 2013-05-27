package com.lewa.face.preview.slide.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.lewa.view.LewaPagerView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.util.Logs;
import com.lewa.face.util.ThemeUtil;

public class OnlinePkgNewServerAdapter extends OnlinePkgSlide {
	String previewLocalPrefix;
	public OnlinePkgNewServerAdapter(ArrayList<String> source, Context context,
			ThemeBase themeBase) {
		super(source, context, themeBase);
		if(mSource.size()==0){
			throw new IllegalStateException("error previews");
		}
		previewLocalPrefix = mSource.get(0).substring(0,mSource.get(0).lastIndexOf("/")+1);
		
		mSource.clear();
		mSource.addAll(themeBase.previewpath);
		
	}

	@Override
	protected String initUrl(String previewPath) {
		// TODO Auto-generated method stub
		return previewPath;
	}
	
	@Override
    public Object instantiateItem(View container, int position) {

        ImageView imageView = new ImageView(mContext);
        
        Bitmap bitmap = slideImageLoader.getBitmapFromCache(position);
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
        }else {
            String previewFileName = mSource.get(position);
            previewFileName = previewFileName.substring(previewFileName.lastIndexOf("/")+1);
            Logs.i("slide online "+previewFileName+" "+initUrl(mSource.get(position)));
            
            slideImageLoader.loadImage(imageView, position, previewLocalPrefix+previewFileName, initUrl(mSource.get(position)));
        }
        ((LewaPagerView)container).addView(imageView, 0, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        return imageView;
    }
	
}
