package com.lewa.face.preview.slide.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeUtil;
public class OnlinePkgSlide extends OnlineSlideAdapter{

	protected ThemeBase themeBase;
	private Map<String, String> previewPrefix;
    public OnlinePkgSlide(ArrayList<String> source, Context context,
			ThemeBase themeBase) {
		super(source, context, themeBase.getPkg());
		this.themeBase = themeBase;
    }

    @Override
    protected String initUrl(String previewPath) {
        // TODO Auto-generated method stub
		String previewName = previewPath
				.substring(previewPath.lastIndexOf("/") + 1);
		String result = new StringBuilder().append(ThemeUtil.THEME_URL)
				.append("/").append(mThemeName).append("/").append(previewName)
				.toString();
		return result;
    }

}
