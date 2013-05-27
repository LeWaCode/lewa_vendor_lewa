/*
 * Copyright (c) 2011 LewaTek
 * All rights reserved.
 * 
 * DESCRIPTION:
 *
 * WHEN          | WHO               | what, where, why
 * --------------------------------------------------------------------------------
 * 2011-08-29  | GanFeng          | Create file
 */

package com.lewa.os.util;

import java.util.ArrayList;

import android.text.TextUtils;

import com.android.internal.util.HanziToPinyin;
import com.android.internal.util.HanziToPinyin.Token;

public final class Util {
    public static final char[] ALPHABET_2_DIGIT_TBL = {
        '2', '2', '2',      //a~c
        '3', '3', '3',      //d~f
        '4', '4', '4',      //g~i
        '5', '5', '5',      //j~l
        '6', '6', '6',      //m~o
        '7', '7', '7', '7', //p~s
        '8', '8', '8',      //t~v
        '9', '9', '9', '9', //w~z
    };
    //add by zenghuaying fix bug #9151
    private static final String[] IP_PREFIX = new String[]{
        "17951","17911","12593","11808","10193","17909","17908","96688"
    };
    
    public static final boolean isStartWithIpPrefix(String number){
        for(int i = 0;i<IP_PREFIX.length;i++){
            if(number.startsWith(IP_PREFIX[i])){
                return true;
            }
        }
        return false;
    }
    //add end

    public static final class MatchKey {
        public String mMatchStr;
        public int    mMatchPos;

        public MatchKey() {
        }

        public MatchKey(String matchStr, int matchPos) {
            mMatchStr = matchStr;
            mMatchPos = matchPos;
        }
    };
    
    public static char convert2Digit(char c) {
        if ((c >= 'a') && (c <= 'z')) {
            return ALPHABET_2_DIGIT_TBL[c - 'a'];
        }
        else if ((c >= 'A') && (c <= 'Z')) {
            return ALPHABET_2_DIGIT_TBL[c - 'A'];
        }
        else {
            return c;
        }
    }

    public static String[] convert2Digit(String[] astrSrc) {
        String[] astrDigit = new String[astrSrc.length];
        for (int i = 0; i < astrSrc.length; ++i) {
            int nLen = astrSrc[i].length();
            StringBuilder strBuilder = new StringBuilder(astrSrc[i]);
            for (int j = 0; j < nLen; ++j) {
            	strBuilder.setCharAt(j, convert2Digit(astrSrc[i].charAt(j)));
            }
            astrDigit[i] = strBuilder.toString();
        }
        return astrDigit;
    }

    public static MatchKey searchApproximateString(String content, CharSequence constraint) {
        int nIndex = -1;
        int nKeyPos = 0;
        int pos = -1;
        int nKeyLen = constraint.length();
        do {
            nIndex = content.indexOf(constraint.charAt(nKeyPos), (nIndex + 1));
            if (pos == -1 && -1 != nIndex) {
                pos = nIndex;
            }
            ++nKeyPos;
        } while ((-1 != nIndex) && (nKeyPos < nKeyLen));

        if ((-1 != nIndex) && (nKeyPos == nKeyLen)) {
            return new MatchKey(constraint.toString(), pos);
            //return constraint.toString();
        }
        else {
            return null;
        }
    }

    public static MatchKey searchNumericString(String content, CharSequence digits) {
        int nKeyLen = digits.length();
        int nContentLen = content.length();

        if (nContentLen < nKeyLen) {
            return null;
        }

        StringBuilder digitContent = new StringBuilder(content);
        for (int i = 0; i < nContentLen; ++i) {
            digitContent.setCharAt(i, convert2Digit(digitContent.charAt(i)));
        }
        
        int nMatchPos = digitContent.indexOf(digits.toString());
        if (-1 != nMatchPos) {
            // StringBuilder matchKey = new StringBuilder(nKeyLen);
            //matchKey.append(content.substring(nMatchPos, (nMatchPos + nKeyLen)));
            return new MatchKey(content.substring(nMatchPos, (nMatchPos + nKeyLen)).toString(), nMatchPos);
            //return content.substring(nMatchPos, (nMatchPos + nKeyLen)).toString();
        }
        return null;
        
//        int nIndex = -1;
//        int nKeyPos = 0;
//        StringBuilder matchKey = new StringBuilder(nKeyLen);
//        while (nKeyPos < nKeyLen) {
//            nIndex = digitContent.indexOf(String.valueOf(digits.charAt(nKeyPos)), (nIndex + 1));
//            if (-1 == nIndex) {
//                break;
//            }
//
//            matchKey.append(content.charAt(nIndex));
//            ++nKeyPos;
//        }
//
//        digitContent = null;
//        if ((-1 != nIndex) && (nKeyPos == nKeyLen)) {
//            return matchKey.toString();
//        }
//        else {
//            matchKey = null;
//            return null;
//        }
    }

    public static MatchKey searchNumericString(String[] astrContent, CharSequence digits) {
        int nKeyPos = 0;
        int nKeyLen = digits.length();
        char nextChar = 0;

        StringBuilder matchContent = new StringBuilder(nKeyLen);
        String[] astrDigit = convert2Digit(astrContent);

        if (astrDigit.length >= nKeyLen) {
            int pos = -1;
            for (int i = 0; ((i < astrContent.length) && (nKeyPos < nKeyLen)); ++i) {
                if (0 == astrDigit[i].length()) {
                    continue;
                }
                
                if (astrDigit[i].charAt(0) == digits.charAt(nKeyPos)) {
                    if ((i + 1) < astrContent.length) {
                        nextChar = astrContent[i + 1].charAt(0);
                        if (nextChar > 128) {
                            ArrayList<Token> tokens = HanziToPinyin.getInstance().get(astrContent[i + 1]);
                            if ((null != tokens) && (tokens.size() > 0)) {
                                Token token = tokens.get(0);
                                if (Token.PINYIN == token.type) {
                                    ++i;
                                }
                            }
                        }
                    }
                    if (pos == -1) {
                        pos = i;
                    }
                    matchContent.append(astrContent[i].charAt(0));
                    ++nKeyPos;
                }
            }

            if (nKeyPos == nKeyLen) {
                return new MatchKey(matchContent.toString(), pos);
            }
            else {
                matchContent.delete(0, matchContent.length());
            }
        }

        String strOrigDigit = digits.toString();
        String strRemainDigit = strOrigDigit;
        boolean matchPinyin = false;
        int pos = -1;
        for (int i = 0; i < astrContent.length; ++i) {
            if (0 == astrDigit[i].length()) {
                continue;
            }
            
            matchPinyin = false;
            if (astrDigit[i].startsWith(strRemainDigit)) {
                if ((i + 1) < astrContent.length) {
                    nextChar = astrContent[i + 1].charAt(0);
                    if (nextChar > 128) {
                        ArrayList<Token> tokens = HanziToPinyin.getInstance().get(astrContent[i + 1]);
                        if ((null != tokens) && (tokens.size() > 0)) {
                            Token token = tokens.get(0);
                            if (Token.PINYIN == token.type) {
                                matchPinyin = true;
                            }
                        }
                    }

                    if (matchPinyin) {
                        ++i;
                        matchContent.append(astrContent[i].charAt(0));
                        if (pos == -1) {
                            pos = i;
                        }
                    }
                    else {
                        matchContent.append(astrContent[i], 0, strRemainDigit.length());
                        if (pos == -1) {
                            pos = i;
                        }
                    }
                }
                else {
                    matchContent.append(astrContent[i], 0, strRemainDigit.length());
                    if (pos == -1) {
                        pos = i;
                    }
                }
                return new MatchKey(matchContent.toString(), pos);
            }
            else {
                if (strRemainDigit.startsWith(astrDigit[i])) {
                    strRemainDigit = strRemainDigit.substring(astrDigit[i].length(), strRemainDigit.length());
                    if ((i + 1) < astrContent.length) {
                        nextChar = astrContent[i + 1].charAt(0);
                        if (nextChar > 128) {
                            ArrayList<Token> tokens = HanziToPinyin.getInstance().get(astrContent[i + 1]);
                            if ((null != tokens) && (tokens.size() > 0)) {
                                Token token = tokens.get(0);
                                if (Token.PINYIN == token.type) {
                                    matchPinyin = true;
                                }
                            }
                        }

                        if (matchPinyin) {
                            ++i;
                            matchContent.append(astrContent[i].charAt(0));
                            if (pos == -1) {
                                pos = i;
                            }
                        }
                        else {
                            matchContent.append(astrContent[i], 0, astrContent[i].length());
                            if (pos == -1) {
                                pos = i;
                            }
                        }
                    }
                    else {
                        matchContent.append(astrContent[i], 0, astrContent[i].length());
                        if (pos == -1) {
                            pos = i;
                        }
                    }
                }
                else {
                    strRemainDigit = strOrigDigit;
                    matchContent.delete(0, matchContent.length());
                    pos = -1;
                }
            }
        }
        
        return null;
    }

    public static boolean isEmergencyNumber(String number, String[] emergencyNumbers) {
        if (null == emergencyNumbers) {
            return (number.equals("112") || number.equals("911") || number.equals("110") || number.equals("120"));
        }

        for (int i = 0; i < emergencyNumbers.length; ++i) {
            if (0 == emergencyNumbers[i].compareTo(number)) {
                return true;
            }
        }

        return (number.equals("112") || number.equals("911") || number.equals("110") || number.equals("120"));
    }

    public static final String numberArrayToInClauseString(ArrayList<String> numberArray, String chinaCallCode, String ipPrefix) {
        String number = null;
        int count = numberArray.size();
        int callCodeLen = chinaCallCode.length();
        StringBuilder strBuilder = new StringBuilder();
        if (TextUtils.isEmpty(ipPrefix)) {
            for (int i = 0; i < count; ++i) {
                number = numberArray.get(i);
                strBuilder.append("'" + number + "'");
                if (number.startsWith(chinaCallCode)) {
                    strBuilder.append(",'" + number.substring(callCodeLen) + "'");
                }
                else {
                    if (number.length() >= 7) {
                        strBuilder.append(",'" + chinaCallCode + number + "'");
                    }
                }
                
                if ((i + 1) < count) {
                    strBuilder.append(",");
                }
            }
        }
        else {
            int ipPrefixLen = ipPrefix.length();
            for (int i = 0; i < count; ++i) {
                number = numberArray.get(i);
                strBuilder.append("'" + number + "'");
                if (number.startsWith(chinaCallCode)) {
                    strBuilder.append(",'" + number.substring(callCodeLen) + "'");
                }
                else {
                    if (number.startsWith(ipPrefix)) {
                        strBuilder.append(",'" + number.substring(ipPrefixLen) + "'");
                    }
                    else {
                        if (number.length() >= 7) {
                            strBuilder.append(",'" + chinaCallCode + number + "'");
                        }
                    }
                }
                
                if ((i + 1) < count) {
                    strBuilder.append(",");
                }
            }
        }
        return strBuilder.toString();
    }
}