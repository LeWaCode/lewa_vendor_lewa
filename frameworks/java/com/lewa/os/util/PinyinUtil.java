package com.lewa.os.util;

public class PinyinUtil {
    public static String extractPinyin(String pinyinStr) {
        if (null == pinyinStr) {
            return null;
        }

        char c = 0;
        int i = 0;
        int nCount = pinyinStr.length();
        StringBuilder strBuilder = null;

        for (i = 0; i < nCount; ++i) {
            if (pinyinStr.charAt(i) > 128) { //chinese character
                break;
            }
        }

        //no chinese character
        if (i == nCount) {
            return null;
        }
        
        for (i = 0; i < nCount; ++i) {
            c = pinyinStr.charAt(i);
            if (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))) {
                if (null == strBuilder) {
                    strBuilder = new StringBuilder(nCount);
                }
                strBuilder.append(c);
            }
        }

        return ((null != strBuilder)? strBuilder.toString() : null);
    }

    public static String extractPinyinHead(String pinyinStr) {
        if (null == pinyinStr) {
            return null;
        }

        char c = 0;
        int i = 0;
        int nCount = pinyinStr.length();
        StringBuilder strBuilder = null;

        for(i = 0; i < nCount; ++i) {
            if (pinyinStr.charAt(i) > 128) { //chinese character
                break;
            }
        }

        //no chinese character
        if (i == nCount) {
            return null;
        }
        
        for (i = 0; i < nCount; ++i) {
            c = pinyinStr.charAt(i);
            if (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))) {
                if (null == strBuilder) {
                    strBuilder = new StringBuilder(nCount);
                }
                strBuilder.append(c);

                ++i;
                while (i < nCount){
                    c = pinyinStr.charAt(i);
                    if (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))) {
                        ++i;
                    }
                    else {
                        break;
                    }
                }
            }
        }

        return ((null != strBuilder)? strBuilder.toString() : null);
    }

    public static String[] splitPinyin(String pinyinStr, char separator) {
        if (null == pinyinStr) {
            return null;
        }
        
        return pinyinStr.split(String.valueOf(separator));
    }

    public static String hanziToPinyin(String hanzi){
        return hanziToPinyin(hanzi, " ");
    }
    /**
     * @param hanzi
     * @param separator
     * @return
     */
    public static String hanziToPinyin(String hanzi,String separator){
            return hanzi;
    }
}
