package com.lewa.base.highlight;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;


public class TextHighLightDecorator extends Decorator{
	
	private KeyMatcher matcher;
	private int highLightColor;
	
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
	
	public void setMatcher(String key) {
		this.matcher = new KeyMatcher(key);
	}
	
	public int getHighLightColor() {
		return highLightColor;
	}

	public void setHighLightColor(int highLightColor) {
		this.highLightColor = highLightColor;
	}

	@Override
	public SpannableStringBuilder getDecorated(Object object)
	{
		String matchStr = object.toString();
		String matchAlpha = Unicode2Alpha.toAlpha(matchStr);
		matcher.match(matchStr, matchAlpha);
		return getHilite(matchStr, matcher.getStart(), matcher.getEnd());
	}
	
	private SpannableStringBuilder getHilite(String matchStr, int start, int end) 
	{
		SpannableStringBuilder style = new SpannableStringBuilder(matchStr);
	
		if(start == 0 && end == 0 || start > end)
		{
			return style;
		}
        style.setSpan(new ForegroundColorSpan(highLightColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        matcher.matcherClean();
        return style;
	}

}
