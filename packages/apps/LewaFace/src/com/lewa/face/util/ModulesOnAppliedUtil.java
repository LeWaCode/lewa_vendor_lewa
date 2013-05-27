package com.lewa.face.util;

import java.util.Iterator;
import java.util.Set;

public class ModulesOnAppliedUtil {
	public static String toString(Set<String> set){
		String result = "";
		Iterator iterator = set.iterator();
		while(iterator.hasNext()){
			result += ","+iterator.next();
		}
		return result.length()>=2?result.substring(1):result;
	}
}
