package com.lewa.search.match;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class defines a matcher.
 * Each matcher uses static method to match a key.
 * @author		wangfan
 * @version	2012.07.04
 */

public final class KeyMatcher {
	
	//these tags used in match method
	private static boolean isRegex = false;
	private static boolean isFuzzy = true;
	private Pattern pattern;
	
	//start and end positions for match result
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
	
	/**
	 * This method matches key both for the original string and its alpha form.
	 * @param matchStr  the original string
	 * @param matchStr  the alpha form of this original string	
	 */
	public boolean match(String matchStr, String matchAlpha) 
	{
		//match the original string first
		if(match(matchStr))
		{
			return true;
		}
		else
		{
			//if the original string doesn't match, match its alpha form
			if(match(matchAlpha))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method matches key for for the matched string.
	 * This method records start and end positions of this matched result
	 * @param matchStr  the matched string
	 */
	public boolean match(String matchStr)
	{
		//initialize a matcher
		Matcher matcher = pattern.matcher(matchStr);
		if(matcher.find()) 
		{
			//match successfully, records start and end positions
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

	/**
	 * This method gets match results with Pattern. It was real match method.
	 * This method refers to some resources from the Internet.
	 * @param key  key to match
	 */
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
