package com.lewa.os.common.ui.indexbar;

import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;

public class IndexBarMenuItemClickListener implements OnMenuItemClickListener {

	private IndexBar indexBar;

	public IndexBarMenuItemClickListener(IndexBar sidebar) {
		super();
		// TODO Auto-generated constructor stub
		this.indexBar = sidebar;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		indexBar.show();
		return false;
	}}
