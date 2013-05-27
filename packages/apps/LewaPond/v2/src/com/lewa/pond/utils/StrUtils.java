package com.lewa.pond.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtils {
	
	public static boolean isEmail(String str) {
		String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		return m.find();
	}

	public static String getRandomNums(int num) {
		String str = "";
		String[] numbers={"0","1","2","3","4","5","6","7","8","9"};
		for (int i = 0; i <num; i++) {
			int r = (int) (Math.random() * (numbers.length - 1));
			str += "  " + numbers[r];
		}
		return str;
	}
	
	public static int strToInt(String str){
		int a=0;
		str=str.trim().replace(" ","");
		try {
			a=Integer.parseInt(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return a;
	}
}
