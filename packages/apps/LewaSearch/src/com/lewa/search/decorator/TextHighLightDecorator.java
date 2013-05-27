package com.lewa.search.decorator;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.lewa.search.match.KeyMatcher;

/**
 * This class defines a decorator for string object.
 * This decorator uses an inner matcher to highlight a part of the decorated string.
 * @author		wangfan
 * @version	2012.07.04
 */

public class TextHighLightDecorator extends Decorator{
	
	//this matcher matches the highlight string
	private KeyMatcher matcher;
	//color of highlight string
	private int highLightColor;
	
	//each king of decorator must define a type
	public static final int decoratorType = DECORATOR_HIGHLIGHT;
	
	public TextHighLightDecorator(int highLightColor) {
		super();
		this.highLightColor = highLightColor;
	}

	public KeyMatcher getMatcher() {
		return matcher;
	}

	public void setMatcher(KeyMatcher matcher) {
		this.matcher = matcher;
	}
	
	/**
	 * This method sets the key to matcher
	 * @param key  initialize the matcher with the key
	 */
	public void setMatcher(String key) {
		this.matcher = new KeyMatcher(key);
	}
	
	public int getHighLightColor() {
		return highLightColor;
	}

	public void setHighLightColor(int highLightColor) {
		this.highLightColor = highLightColor;
	}

	/**
	 * This method matches the key and decorate it
	 * @param object  the string willing to be decorated
	 */
	@Override
	public SpannableStringBuilder getDecorated(Object object)
	{
		String matchStr = (String) object;
		
		//match the string
		//the key is already set to matcher
		//the matcher records start position and end position of highlighted string
		matcher.match(matchStr);
		//highlight the string
		return getHilite(matchStr, matcher.getStart(), matcher.getEnd());
	}
	
	/**
	 * This method highlight the pointed string
	 * @param matchStr  the string willing to be decorated
	 * @param start  decorating start position
	 * @param end  decorating end position
	 */
	private SpannableStringBuilder getHilite(String matchStr, int start, int end) 
	{
		//swap the string to "SpannableStringBuilder" type to be decorated 
		SpannableStringBuilder style = new SpannableStringBuilder(matchStr);
	
		if(start == 0 && end == 0 || start > end)
		{
			return style;
		}
		//set pointed part of the string highlighted
        style.setSpan(new ForegroundColorSpan(highLightColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        //clean the matcher
        matcher.matcherClean();
        return style;
	}
}
