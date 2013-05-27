package com.lewa.base.highlight;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class KeyMatcher {
	
	private static boolean isRegex = false;
	private static boolean isFuzzy = true;
	
	private Pattern pattern;
	
	private int start;
	private int end;
	
	private boolean isMatchAlpha;
	
	public KeyMatcher(String regex)
	{	
		pattern = toRegex(regex);
	}
	
	public KeyMatcher()
	{	
		pattern = null;
	}
	
	public void updateMatcher(String regex)
	{
		pattern = toRegex(regex);
	}

	public boolean match(String matchStr, String matchAlpha) 
	{
		if(match(matchStr))
		{
			return true;
		}
		else
		{
			if(match(matchAlpha))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean match(String matchStr)
	{
		Matcher matcher = pattern.matcher(matchStr);
		if(matcher.find()) 
		{
			start = matcher.start();
			end = matcher.end();
			
			return true;
		}
		
		else
		{
			return false;
		}
	}
	
	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}
	
	public void matcherClean()
	{
		this.start = 0;
		this.end = 0;
	}

	private static Pattern toRegex(String key) 
	{
		Pattern pattern;
		String patternKey;
		if(isRegex || key.startsWith("re:")) 
		{
			if(key.startsWith("re:")) 
			{
				patternKey = key.substring(3, key.length());
			} 
			else 
			{
				patternKey = key;
			}				
			if(!isFuzzy && !patternKey.startsWith("^")) 
			{
				pattern = Pattern.compile("^" + patternKey, Pattern.CASE_INSENSITIVE);
			} 
			else 
			{
				pattern = Pattern.compile(patternKey, Pattern.CASE_INSENSITIVE);
			}
		} 
		else 
		{
			patternKey = key.replace("\\", "\\u005C")
							.replace(".", "\\u002E")
							.replace("$", "\\u0024")
							.replace("^", "\\u005E")
							.replace("{", "\\u007B")
							.replace("[", "\\u005B")
							.replace("(", "\\u0028")
							.replace(")", "\\u0029")
							.replace("+", "\\u002B")
							.replace("*", "[\\s\\S]*")
							.replace("?", "[\\s\\S]");
			if(isFuzzy) 
			{
				pattern = Pattern.compile(patternKey, Pattern.CASE_INSENSITIVE);
			} 
			else 
			{
				pattern = Pattern.compile("^" + patternKey, Pattern.CASE_INSENSITIVE);
			}
		}
		return pattern;
	}

	public boolean isMatchAlpha() {
		return isMatchAlpha;
	}

	public void setMatchAlpha(boolean isMatchAlpha) {
		this.isMatchAlpha = isMatchAlpha;
	}
	
}
