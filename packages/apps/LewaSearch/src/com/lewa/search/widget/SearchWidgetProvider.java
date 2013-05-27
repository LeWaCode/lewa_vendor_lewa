package com.lewa.search.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.lewa.search.R;

/**
 * This widget gives the programe a image shortcut in desktop.
 * @author		wangfan
 * @version	2012.07.04
 */

public class SearchWidgetProvider extends AppWidgetProvider {
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// TODO Auto-generated method stub
		
		final int N = appWidgetIds.length;
		int appWidgetId = 0;
		for (int i = 0; i < N; i++) {
			appWidgetId = appWidgetIds[i];
			Log.i("myLog", "this is [" + appWidgetId + "] onUpdate!");

		}
		
		//initialize a remoteView
		RemoteViews views = new RemoteViews(context
				.getPackageName(), R.layout.search_widget);
		
		//set a pending intent for this view
		Intent intent = new Intent();
		intent.setClassName("com.lewa.search", "com.lewa.search.LewaSearchActivity");
		
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		//set this view click event
		views.setOnClickPendingIntent(R.id.search_button, pendingIntent);
		
		//update this widget
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		
	}
}
