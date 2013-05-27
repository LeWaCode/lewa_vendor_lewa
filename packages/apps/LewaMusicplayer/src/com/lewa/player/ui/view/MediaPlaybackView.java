package com.lewa.player.ui.view;

import com.lewa.player.MediaPlaybackService;
import com.lewa.player.R;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MediaPlaybackView extends ViewGroup {

	public MediaPlaybackView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		View mediaPlaybackView = LayoutInflater.from(context).inflate(R.layout.mediaplaying_view, null);
		mediaPlaybackView.setId(1);
		this.addView(mediaPlaybackView);
	}

	
    
	
	@Override
	protected void onLayout(boolean arg0, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			switch (child.getId()) {
			case 1:
				child.setVisibility(View.VISIBLE);
				child.measure(r - l, b - t);
				child.layout(l, t, r, b);
				break;
			default:
				//
			}
		}
	}

}
