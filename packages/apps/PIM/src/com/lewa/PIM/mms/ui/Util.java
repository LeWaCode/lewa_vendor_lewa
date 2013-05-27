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

package com.lewa.PIM.mms.ui;

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

    public static String searchApproximateString(String content, CharSequence constraint) {
        int nIndex = -1;
        int nKeyPos = 0;
        int nKeyLen = constraint.length();
        do {
            nIndex = content.indexOf(constraint.charAt(nKeyPos), (nIndex + 1));
            ++nKeyPos;
        } while ((-1 != nIndex) && (nKeyPos < nKeyLen));

        if ((-1 != nIndex) && (nKeyPos == nKeyLen)) {
            return constraint.toString();
        }
        else {
            return null;
        }
    }

    public static String searchNumericString(String content, CharSequence digits) {
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
            return content.substring(nMatchPos, (nMatchPos + nKeyLen)).toString();
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

    public static boolean isEmergencyNumber(String number, String[] emergencyNumbers) {
        if (null == emergencyNumbers) {
            return (number.equals("112") || number.equals("911"));
        }

        for (int i = 0; i < emergencyNumbers.length; ++i) {
            if (0 == emergencyNumbers[i].compareTo(number)) {
                return true;
            }
        }

        return (number.equals("112") || number.equals("911"));
    }
}