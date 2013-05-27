package com.lewa.os.filter;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.lewa.os.filter.FilterItem;

public class SpannableFilterItemVisitor extends FilterItem.Visitor<SpannableStringBuilder> {
    public void onVisitText(String text, boolean match) {
        if (!TextUtils.isEmpty(text)) {
            SpannableStringBuilder strBuilder = (SpannableStringBuilder )mContainer;
            int nStart = strBuilder.length();
            strBuilder.append(text);
            if (match) {
                int nEnd   = nStart + text.length();
                ForegroundColorSpan FgColorSpan = new ForegroundColorSpan(0xff007aff); //R.color.filter_text);
                strBuilder.setSpan(FgColorSpan, nStart, nEnd, Spanned.SPAN_POINT_MARK);
            }
        }
    }
}