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

package com.lewa.os.ui;

import android.app.ListActivity;
import android.widget.AbsListView;

public class ListBaseActivity extends ListActivity implements AbsListView.OnScrollListener {
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged (AbsListView view, int scrollState) {
    }
}
