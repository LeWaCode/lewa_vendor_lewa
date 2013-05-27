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

package com.lewa.os.filter;

import android.text.TextUtils;

public abstract class FilterItem {
    public static final int NAME_FIELD    = 0;
    public static final int NUMBER_FIELD  = 1;
    public static final int INVALID_FIELD = -1;

    public static final int EXACT_MATCH_MODE       = 0;
    public static final int DIGIT_MATCH_MODE       = 1;
    public static final int APPROXIMATE_MATCH_MODE = 2;
    
    private int    mMatchField = INVALID_FIELD;
    private String mMatchKey;
    private int    mMatchPos;

    public abstract int getFilterCount();
    public abstract String getFilterContent(int field);
    public abstract int getFilterMode(int field);

    public String[] getFilterAlphabetContents(int field) {
        return null;
    }

    public int getMatchPos() {
        return mMatchPos;
    }

    public void setMatchPos(int matchPos) {
        mMatchPos = matchPos;
    }

    public int getMatchField() {
        return mMatchField;
    }

    public void setMatchField(int matchField) {
        mMatchField = matchField;
    }

    public void setMatchKey(String matchKey) {
        mMatchKey = matchKey;
    }
    
    public void Visit(Visitor visitor) {
        int matchMode = getFilterMode(mMatchField);
        String matchContent = getFilterContent(mMatchField);
        if (TextUtils.isEmpty(matchContent) || TextUtils.isEmpty(mMatchKey)) {
            return;
        }

        int nKeyPos = 0;
        int nKeyEndPos = 0;
        int nContentPos = 0;
        int nContentStartPos = 0;
        int nContentEndPos = 0;

        if (EXACT_MATCH_MODE == matchMode) {
            nContentPos = matchContent.indexOf(mMatchKey);
            if (-1 != nContentPos) {
                nContentEndPos = matchContent.length();
                visitor.onVisitText(matchContent.substring(0, nContentPos), false);
                visitor.onVisitText(mMatchKey, true);
                nContentPos += mMatchKey.length();
                if (nContentPos < nContentEndPos) {
                    visitor.onVisitText(matchContent.substring(nContentPos, nContentEndPos), false);
                }
            }
            else {
                visitor.onVisitText(matchContent, false);
            }
        }
        else {
            nKeyEndPos = mMatchKey.length();
            nContentEndPos = matchContent.length();
            while ((nKeyPos < nKeyEndPos) && (nContentPos < nContentEndPos)) {
                while ((nContentPos < nContentEndPos)
                        && (matchContent.charAt(nContentPos) != mMatchKey.charAt(nKeyPos))) {
                    ++nContentPos;
                }
                
                if (nContentStartPos != nContentPos) {
                    visitor.onVisitText(matchContent.substring(nContentStartPos, nContentPos), false);
                    nContentStartPos = nContentPos;
                }
                
                while ((nKeyPos < nKeyEndPos)
                        && (nContentPos < nContentEndPos)
                        && (matchContent.charAt(nContentPos) == mMatchKey.charAt(nKeyPos))) {
                    ++nContentPos;
                    ++nKeyPos;
                }

                if (nContentStartPos != nContentPos) {
                    visitor.onVisitText(matchContent.substring(nContentStartPos, nContentPos), true);
                    nContentStartPos = nContentPos;
                }
            }

            if (nContentPos < nContentEndPos) {
                visitor.onVisitText(matchContent.substring(nContentStartPos, nContentEndPos), false);
            }
        }
    }
    

    public abstract static class Visitor<T> {
        protected T mContainer;
        
        public void Visit(FilterItem item, T container) {
            mContainer = container;
            item.Visit(this);
            mContainer = null;
        }

        public abstract void onVisitText(String text, boolean match);
    }
}