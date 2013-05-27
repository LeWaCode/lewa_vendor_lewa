package com.lewa.PIM.mms.ui;

import android.R.integer;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

public class RowLayout extends ViewGroup
{
    private RecipientEdit mEdit = null;
    
 public static class LayoutParams extends android.view.ViewGroup.MarginLayoutParams
 {     
     public LayoutParams(int i, int j)
     {
         super(i, j);
     }

     public LayoutParams(Context context, AttributeSet attributeset)
     {
         super(context, attributeset);
     }

     public LayoutParams(android.view.ViewGroup.LayoutParams layoutparams)
     {
         super(layoutparams);
     }

     public LayoutParams(android.view.ViewGroup.MarginLayoutParams marginlayoutparams)
     {
         super(marginlayoutparams);
     }
 }

 public static class RowInfo
 {

     int childCount;
     int height;
     int width;

     public RowInfo()
     {
     }
 }


 public RowLayout(Context context)
 {
     super(context);
     mVerticalSpacing = 0;
     mHorizontalSpacing = 0;
     ArrayList arraylist = new ArrayList();
     mRows = arraylist;
 }

 public RowLayout(Context context, AttributeSet attributeset)
 {
     super(context, attributeset);
     mVerticalSpacing = 0;
     mHorizontalSpacing = 0;
     ArrayList arraylist = new ArrayList();
     mRows = arraylist;
     initAttributes(attributeset);
 }

 public RowLayout(Context context, AttributeSet attributeset, int i)
 {
     super(context, attributeset, i);
     mVerticalSpacing = 0;
     mHorizontalSpacing = 0;
     ArrayList arraylist = new ArrayList();
     mRows = arraylist;
     initAttributes(attributeset);
 }

 public void setRecipientEdit(RecipientEdit edit){
     mEdit = edit;
 }
 
 private void initAttributes(AttributeSet attributeset)
 {
     Context context = getContext();
     int ai[] = {0x7f010001, 0x7f010002};
     TypedArray typedarray = context.obtainStyledAttributes(attributeset, ai);
     int i = typedarray.getDimensionPixelSize(0, 0);
     setVerticalSpacing(i);
     int j = typedarray.getDimensionPixelSize(1, 0);
     setHorizontalSpacing(j);
 }

 public android.view.ViewGroup.LayoutParams generateLayoutParams1(AttributeSet attributeset)
 {
     return generateLayoutParams1(attributeset);
 }

 public LayoutParams generateLayoutParams(AttributeSet attributeset)
 {
     Context context = getContext();
     return new LayoutParams(context, attributeset);
 }

 protected void onLayout(boolean flag, int i, int j, int k, int l)
 {
     int i1 = 0;
     int j1 = getPaddingTop();
     int k1 = j + j1;
     int l1 = 0;
     int childWiths = 0;

     do
     {
         int i2 = mRows.size();
         if(l1 >= i2){
             if (mEdit != null) {
                 mEdit.setScrollerLayout(childWiths);                                     
            }
             return;
         }
         int j2 = getPaddingLeft();
         int k2 = i + j2;
         int l2 = 0;
         do
         {
             int i3 = ((RowInfo)mRows.get(l1)).childCount;
             if(l2 >= i3){
                 break;                 
             }
             View view = getChildAt(i1);
             int j3 = view.getMeasuredWidth();
             int k3 = k2 + j3;
             int l3 = view.getMeasuredHeight() + k1;
             view.layout(k2, k1, k3, l3);
             
             childWiths += k3 - k2;
             
             int i4 = mHorizontalSpacing;
             k2 = k3 + i4;
             i1++;
             l2++;
         } while(true);
         int j4 = ((RowInfo)mRows.get(l1)).height;
         int k4 = mVerticalSpacing;
         int l4 = j4 + k4;
         k1 += l4;
         l1++;
     } while(true);     
 }

 protected void onMeasure(int i, int j)
 {
     int k = android.view.View.MeasureSpec.getMode(i);
     int l = android.view.View.MeasureSpec.getSize(i);
     int i1 = android.view.View.MeasureSpec.getMode(j);
     int j1 = android.view.View.MeasureSpec.getSize(j);
     int k1 = getChildCount();
     int l1 = 0;
     int i2 = 0;
     mRows.clear();
     RowInfo rowinfo = new RowInfo();
     int j2 = 0;
     do
     {
         int j3;
         int k3;
label0:
         {
label1:
             {
                 if(j2 < k1)
                 {
                     RowLayout rowlayout = this;
                     int k2 = j2;
                     View view = rowlayout.getChildAt(k2);
                     RowLayout rowlayout1 = this;
                     int l2 = i;
                     int i3 = j;
                     rowlayout1.measureChildWithMargins(view, l2, 0, i3, l1);
                     j3 = view.getMeasuredHeight();
                     k3 = view.getMeasuredWidth();
                     int l3 = rowinfo.width + k3;
                     if(rowinfo.childCount > 0)
                     {
                         int i4 = mHorizontalSpacing;
                         l3 += i4;
                     }
                     if(k == 0)
                         break label0;
                     int j4 = l3;
                     int k4 = l;
                     if(j4 <= k4)
                         break label0;
                     if(i1 == 0 || l1 < j1)
                         break label1;
                 }
                 if(rowinfo.childCount > 0)
                 {
                     if(mRows.size() > 0)
                     {
                         int l4 = mVerticalSpacing;
                         l1 += l4;
                     }
                     int i5 = rowinfo.height;
                     l1 += i5;
                     ArrayList arraylist = mRows;
                     RowInfo rowinfo1 = rowinfo;
                     boolean flag = arraylist.add(rowinfo1);
                 }
                 int j5 = getPaddingLeft() + i2;
                 int k5 = getPaddingRight();
                 int l5 = j5 + k5;
                 int i6 = i;
                 int j6 = resolveSize(l5, i6);
                 int k6 = getPaddingTop() + l1;
                 int l6 = getPaddingBottom();
                 int i7 = k6 + l6;
                 int j7 = j;
                 int k7 = resolveSize(i7, j7);
                 RowLayout rowlayout2 = this;
                 int l7 = j6;
                 int i8 = k7;
                 rowlayout2.setMeasuredDimension(l7, i8);
                 return;
             }
             if(mRows.size() > 0)
             {
                 int j8 = mVerticalSpacing;
                 l1 += j8;
             }
             int k8 = rowinfo.height;
             l1 += k8;
             ArrayList arraylist1 = mRows;
             RowInfo rowinfo2 = rowinfo;
             boolean flag1 = arraylist1.add(rowinfo2);
             rowinfo = new RowInfo();
         }
         if(rowinfo.childCount > 0)
         {
             int l8 = rowinfo.width;
             int i9 = mHorizontalSpacing;
             int j9 = l8 + i9;
             rowinfo.width = j9;
         }
         int k9 = rowinfo.width + k3;
         rowinfo.width = k9;
         int l9 = rowinfo.childCount + 1;
         rowinfo.childCount = l9;
         int i10 = rowinfo.width;
         i2 = Math.max(i2, i10);
         int j10 = Math.max(rowinfo.height, j3);
         rowinfo.height = j10;
         j2++;
     } while(true);
 }

 public void setHorizontalSpacing(int i)
 {
     mHorizontalSpacing = i;
     requestLayout();
 }

 public void setVerticalSpacing(int i)
 {
     mVerticalSpacing = i;
     requestLayout();
 }

 private int mHorizontalSpacing;
 protected ArrayList mRows;
 private int mVerticalSpacing;
}
