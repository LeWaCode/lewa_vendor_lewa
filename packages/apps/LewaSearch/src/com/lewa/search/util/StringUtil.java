package com.lewa.search.util;

import java.util.regex.Pattern;
/**
 * This class defines all of the string operations in this program. 
 * 
 * @author		wangfan
 * @version	2012.07.04
 */

public class StringUtil {

	public static int[] matche(String str, String key)
	{
		int[] posArray = new int[2];
		
		String[] innerStrs = str.split(" ");
		int len = innerStrs.length;
		String tempStr = "";
		int[] count = new int[len];
		count[0] = 0;
		int index = 1;
		int lenTemp = 0;
		
		for(int i = 0; i < len; i ++)
		{
			String innerStr = innerStrs[i];

			if(innerStr.length() == 1)
			{
				char c = innerStr.charAt(0);
				
				if(( c > 0x4e00  && c < 0x9FA5) || ( c > 0xF900 && c < 0xFA2D))
				{
					continue;
				}
				else
				{
					tempStr += innerStr;
					lenTemp += innerStr.length();
					count[index ++] = lenTemp;
					
				}
			}
			else
			{
				if(innerStr.length() != 0)
				{
					tempStr += innerStr;
					lenTemp += innerStr.length();
					count[index ++] = lenTemp;
				}
				
			}
		}
		
		tempStr = tempStr.toLowerCase();
		key = key.toLowerCase();
		int pos = tempStr.indexOf(key);
		int start = 0;
		int end = 0;
		
		if(tempStr.contains(key))
		{
			for(int i = 0; i < len; i ++)
			{	
				if(i > 0 && count[i] == 0)
				{
					break;
				}
				
				if(count[i] < pos)
				{
					start ++;
					end = start;
					continue;
				}
				
				if(count[i] < pos + key.length())
				{System.out.println(count[i]);
					end ++;
				}
				else
				{
					break;
				}
			}
		
		}
		
		posArray[0] = start;
		posArray[1] = end;
		
		return posArray;
	}
	
	public static String firstLetter2UpperCase(String str)
	{
		if(str == null)
		{
			return null;
		}
		
		char firstLetter = str.charAt(0);
		str = (char)(firstLetter - 32) + str.substring(1);
		return str;
	}
	
	public static boolean isNumeric(String str)
	{ 
		Pattern pattern = Pattern.compile("[0-9]*"); 
		return pattern.matcher(str).matches(); 
	}
	
	public static boolean isLetter(String str)
	{
		return isLowerCase(str) || isUpperCase(str);
	}
	
	public static boolean isLowerCase(String str)
	{
		Pattern pattern = Pattern.compile("[a-z]*"); 
		return pattern.matcher(str).matches(); 
	}
	
	public static boolean isUpperCase(String str)
	{
		Pattern pattern = Pattern.compile("[A-Z]*"); 
		return pattern.matcher(str).matches(); 
	}
	
	public static String deletePrefix(String str, int length)
	{
		return str.substring(length);
	}
	
	public static String simplifyStringAtPosition(String str, int showNum, int startPosition, int endPosition, String replaceStr, boolean isWithSuffix)
	{
		String strResult = "";
		int start;
		int end;
		int len = str.length();
		
		boolean hasSufReplaceStr = false;
		int numSufReplaceStr;
		
		boolean hasPreReplaceStr = false;
		int numPreReplaceStr;
		
		String suffixStr;
		int numSuffixStr;
		
		String showString;
		int numShowString;
		
		boolean hasSuffix = isWithSuffix;
		
		if(len <= showNum)
		{
			start = startPosition;
			end = endPosition;
	
			str = (str + " " + start + " " + end);
			
			return str;
		}
		
		if(isWithSuffix == true)
		{
			suffixStr = getSuffix(str);
			numSuffixStr = suffixStr.length();
			
			numShowString = showNum - numSuffixStr;
			
			int startPositionDefault = replaceStr.length();
			if(startPosition < startPositionDefault)
			{
				hasSufReplaceStr = true;
				numSufReplaceStr = len - numShowString - numSuffixStr;
				
				hasPreReplaceStr = false;
				
				start = startPosition;
				showString = str.substring(0, numShowString);
				
				int sub = endPosition - startPosition - (numShowString + numSufReplaceStr);
				if(sub >= 0)
				{
					end = start + numShowString + replaceStr.length() + sub;
				}
				else
				{
					if(endPosition > numShowString)
					{
						end = numShowString + replaceStr.length();
					}
					else
					{
						end = start + (endPosition - startPosition);
					}
				}
			}
			else
			{
				hasPreReplaceStr = true;
				numPreReplaceStr = len - showNum;

				if(len - startPosition > numShowString + numSuffixStr)
				{
					start = startPositionDefault;
					showString = str.substring(startPosition, startPosition + numShowString);
					
					hasSufReplaceStr = true;
					numSufReplaceStr = len - startPosition - (numShowString + numSuffixStr);
					
					int sub = endPosition - startPosition - (numShowString + numSufReplaceStr);
					if(sub > 0)
					{
						end = start + numShowString + replaceStr.length() + sub;
					}
					else
					{
						if(endPosition - startPosition > numShowString)
						{
							end = start + numShowString + replaceStr.length();
						}
						else
						{
							end = start + (endPosition - startPosition);
						}
					}
				}
				else
				{
					hasSuffix = false;
					
					start = startPosition - numPreReplaceStr + replaceStr.length();
					
					showString = str.substring(len - showNum);
					
					hasSufReplaceStr = false; 
					end = start + (endPosition - startPosition);
				}
			}
			
			if(hasPreReplaceStr == true)
			{
				strResult += replaceStr;
			}
			strResult += showString;
			if(hasSufReplaceStr == true)
			{
				strResult += replaceStr;
			}
			if(hasSuffix == true)
			{
				strResult += suffixStr;
			}
			
			strResult += (" " + start + " " + end);
		}
		else
		{
			numShowString = showNum;
			int startPositionDefault = replaceStr.length();
			
			if(startPosition < startPositionDefault)
			{
				hasSufReplaceStr = true;
				numSufReplaceStr = len - numShowString;
				
				hasPreReplaceStr = false;
				
				start = startPosition;
				showString = str.substring(0, numShowString);
				
				if(endPosition > numShowString)
				{
					end = numShowString + replaceStr.length();
				}
				else
				{
					end = start + (endPosition - startPosition);
				}
			}
			else
			{
				hasPreReplaceStr = true;
				numPreReplaceStr = len - showNum;
				
				if(len - startPosition > numShowString)
				{
					start = startPositionDefault;
					showString = str.substring(startPosition, startPosition + numShowString);
					
					hasSufReplaceStr = true;
					
					if(endPosition - startPosition > numShowString)
					{
						end = start + numShowString + replaceStr.length();
					}
					else
					{
						end = start + (endPosition - startPosition);
					}
				}
				else
				{
					
					hasSuffix = false;
					
					start = startPosition - numPreReplaceStr + replaceStr.length();
			
					showString = str.substring(len - showNum);
					
					hasSufReplaceStr = false; 
					end = start + (endPosition - startPosition);
				}
			}
			if(hasPreReplaceStr == true)
			{
				strResult += replaceStr;
			}
			strResult += showString;
			if(hasSufReplaceStr == true)
			{
				strResult += replaceStr;
			}
			
			strResult += (" " + start + " " + end);
			
			
		}
		
		return strResult;
	}
	
	public static String getSuffix(String fileName)
	{
		int suffixPosition = fileName.lastIndexOf(".");
		return fileName.substring(suffixPosition);
	}
	
	public static String getFileName(String path) 
	{
		if(path != null)
        {
			return path.substring(path.lastIndexOf("/") + 1);
        }
		else
		{
			return "";
		}
    }
	
	public static String getFileExtension(String path)
	{
		if(path != null)
        {
			return path.substring(path.lastIndexOf(".")+1).toLowerCase();
        }
		else
		{
			return "";
		}
	}
	
}
