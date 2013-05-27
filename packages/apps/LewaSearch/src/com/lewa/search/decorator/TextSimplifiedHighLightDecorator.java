package com.lewa.search.decorator;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.lewa.search.match.KeyMatcher;
import com.lewa.search.util.StringUtil;

/**
 * This class defines a decorator for string object.
 * This decorator uses an inner matcher to highlight a part of the decorated string and cut the string in a suitable way 
 * @author		wangfan
 * @version	2012.07.04
 */

public class TextSimplifiedHighLightDecorator extends Decorator{

	//this matcher matches the highlight string
	private KeyMatcher matcher;
	//color of highlight string
	private int highLightColor;
	//this integer shows how many letters remain on one row
	private int showNum;
	//this string replace the cut part of the string
	private String replaceString;
	//this tag records whether the string has an extension
	//extension will not be cut
	private boolean isWithSuffix;
	
	public static final int decoratorType = DECORATOR_HIGHLIGHT_AND_SIMPLIFIED;

	public TextSimplifiedHighLightDecorator(int highLightColor, int showNum,
			String replaceString, boolean isWithSuffix) {
		super();
		this.highLightColor = highLightColor;
		this.showNum = showNum;
		this.replaceString = replaceString;
		this.isWithSuffix = isWithSuffix;
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

	public int getShowNum() {
		return showNum;
	}

	public void setShowNum(int showNum) {
		this.showNum = showNum;
	}

	public String getReplaceString() {
		return replaceString;
	}

	public void setReplaceString(String replaceString) {
		this.replaceString = replaceString;
	}

	/**
	 * This method matches the key and decorate it
	 * @param object  the string willing to be decorated
	 */
	@Override
	public SpannableStringBuilder getDecorated(Object object)
	{
		String matchStr = (String) object;
		//String matchAlpha = Unicode2Alpha.toAlpha(matchStr);
		//match the string
		//the key is already set to matcher
		//the matcher records start position and end position of highlighted string
		matcher.match(matchStr);
		int start = matcher.getStart();
		int end = matcher.getEnd();
		//get the simplified string in a suitable way
		String simplifiedStr = getSimplified(matchStr, start, end);
		
		//get highlight start and end positions of the simplified string
		//this simplified string returns as (simplifiedStr + " " + start position + " " + end position) 
		int endTagPosition = simplifiedStr.lastIndexOf(" ");
		String strTemp1 = simplifiedStr.substring(0, endTagPosition);
		int endPosition = Integer.parseInt((simplifiedStr.substring(endTagPosition)).trim());
		
		int startTagPosition = strTemp1.lastIndexOf(" ");
		String strTemp2 = strTemp1.substring(0, endTagPosition);
		int startPosition = Integer.parseInt((strTemp1.substring(startTagPosition)).trim());
		
		String str = strTemp2.substring(0, startTagPosition);
		
		//highlight the simplified string
		return getHilite(str, startPosition, endPosition);
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
		end = end > matchStr.length()? matchStr.length(): end;
		
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
	
	/**
	 * This method returns the simplified string
	 * @param str  the string willing to be decorated
	 * @param start  decorating start position
	 * @param end  decorating end position
	 */
	private String getSimplified(String str, int startPosition, int endPosition)
	{
		return StringUtil.simplifyStringAtPosition(str, showNum, startPosition, endPosition, replaceString, isWithSuffix);
	}

}
