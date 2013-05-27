package com.lewa.filemanager.activities.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.lewa.base.Logs;
import java.util.List;

public class ViewUtil {

    public static void findViewByIds(View view, int tag,List<View> views) {
        if (tag==view.getId()) {
            views.add(view);
            Logs.i("", "========= addview "+view);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            View child = null;
            for (int i = 0; i < group.getChildCount(); i++) {
                child = group.getChildAt(i);
                findViewByIds(child, tag,views);
            }
            return;
        }
        return ;
    }

    public static View findViewById(Object view, int id) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            View child = group.findViewById(id);
            if (child == null) {
                for (int i = 0; i < group.getChildCount(); i++) {
                    view = group.getChildAt(i);
                    if (view instanceof ViewGroup) {
                        if (findViewById((ViewGroup) view, id) == null) {
                            return null;
                        }
                    }
                }
            }
            return child;
        } else {
            return null;
        }
    }

    public static View findListViewByItemView(View view) {
        if (view == null || view.getParent() instanceof ListView) {
            return view;
        } else {
            Log.i("---", "---" + view);
            return findListViewByItemView((ViewGroup) view.getParent());
        }
    }
}
