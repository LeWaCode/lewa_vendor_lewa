package com.lewa.search.adapter;

import java.lang.reflect.Method;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.lewa.search.bean.FileInfo;
import com.lewa.search.decorator.Decorator;
import com.lewa.search.util.StringUtil;

/**
 * This class defines an adapter connected view and model.
 * @author		wangfan
 * @version	2012.07.04
 */

public class FileInfoAdapter extends BaseAdapter {

	//this fileList contains all fileItems willing to show on the screen
	public List<FileInfo> fileList;
	
	//this inflater helps to set fileItem's layout with layoutId
	//each adapter can only have one kind of item layout
	private LayoutInflater inflater;
	private int layoutId;
	
	//this string helps to build method name in invocation
	private String methodPrefix;
	
	//this map caches method name got by invocation,reduce times of invocation
	private Map<String, Method> methods = new HashMap<String, Method>();
	
	/**
	 * This method initialize this adapter with inflater.
	 * @param inflater  helps to set layout
	 */
	public FileInfoAdapter(LayoutInflater inflater) {
		super();
		this.inflater = inflater;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return fileList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return fileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getLayoutId() {
		return layoutId;
	}

	public void setLayoutId(int layoutId) {
		this.layoutId = layoutId;
	}

	public void setMethodArgs(String methodPrefix)
	{
		this.methodPrefix = methodPrefix;
	}

	/**
	 * This method returns the View for each item
	 * @param position  the item position
	 * @param contentView  view of this item
	 * @param parent  parent view
	 */
	@Override
	public View getView(int position, View contentView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		FileInfo fileItem = fileList.get(position);
		int viewid = this.layoutId;
		
		//if this contentView has not been created,then create one
		if(contentView == null) 
		{
			contentView = inflater.inflate(viewid, parent, false);
		} 
		
		//bind data with view
		//each view showed on the screen has a tag,this tag maps an attribute name in model class
		//if the model class has an attribute named "title", then the TextView who wants to show it must have a tag "title",
		//		then combine with methodPrefix to assemble a method name for invocation(in this system, methodPrefix is "get"),
		//		so method name is "getTitle".
		contentView = dataBindView(fileItem, contentView);
		
		return contentView;
	}
	
	public View dataBindView(FileInfo fileInfo, View view)
	{
		@SuppressWarnings("static-access")
		//this string array tells the system which attribute is willing to show on the screen
		String sequenceShow[] = fileInfo.sequenceShow;
		
		//bind each attribute with their container on the screen
		for(int i = 0; i < sequenceShow.length; i ++)
		{
			//find a attribute and map a view
			String tag = sequenceShow[i];
			View child = view.findViewWithTag(new String(tag));

			if(child instanceof TextView)
			{	
				//build method name
				String methodGetter = methodPrefix + StringUtil.firstLetter2UpperCase(tag);
				
				try
				{				
					//get value by invocation
					Method method = methods.get(methodGetter);
					if(method == null)
					{
						method = FileInfo.class.getDeclaredMethod(methodGetter); 
						method.setAccessible(true);
						
						//put method into cache for later use 
						methods.put(methodGetter, method);
					}

					Object value = method.invoke(fileInfo); 
					String text = String.valueOf(value);
					
					//each attribute show on the screen can only have one decorator
					Decorator decorator = fileInfo.decorators != null? fileInfo.decorators.get(tag) : null;
					if(decorator != null)
					{	
						//has a decorator,set decorated string
						((TextView) child).setText((SpannableStringBuilder)decorator.getDecorated(text));
					}
					else
					{
						//has no decorator,set plain string
						((TextView) child).setText(text);
					}
					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			else if(child instanceof Button)
			{
					
			}
			else if(child instanceof CheckBox)
			{
					
			}
			else if(child instanceof ImageView)
			{
				
				String methodGetter = methodPrefix + StringUtil.firstLetter2UpperCase(tag);
				try
				{
					Method method = methods.get(methodGetter);
					if(method == null)
					{
						method = fileInfo.getClass().getMethod(methodGetter); 
						methods.put(methodGetter, method);
					}

					Object value = method.invoke(fileInfo); 
					Drawable picture = (Drawable) value;
					
					Decorator decorator = fileInfo.decorators != null? fileInfo.decorators.get(tag) : null;
					if(decorator != null)
					{	
						//has a decorator, set decorated image
						((ImageView) child).setImageDrawable((Drawable)decorator.getDecorated(picture));
					}
					else
					{
						//has a decorator, set plain image
						((ImageView) child).setImageDrawable(picture);
					}
					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
			}
		}
		
		return view;
	}

}
