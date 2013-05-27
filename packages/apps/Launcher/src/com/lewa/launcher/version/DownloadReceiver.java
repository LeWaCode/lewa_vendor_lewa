package com.lewa.launcher.version;

import java.io.File;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.Downloads;
import com.lewa.launcher.R;

public class DownloadReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		ContentResolver cr = context.getContentResolver();

		if("android.intent.action.PACKAGE_ADDED".equals(action)){
			String dat = intent.getExtras().getString("dat");
			if(("package:"+(context.getString(R.string.application_name))).equals(dat)){
				delFile();
			}
			return;
		}
		Uri data = Downloads.Impl.CONTENT_URI;
		Cursor cursor = null;

		cursor = cr.query(data, new String[] { Downloads.Impl.COLUMN_TITLE,
				Downloads.Impl.COLUMN_URI, Downloads.COLUMN_STATUS,
				BaseColumns._ID }, BaseColumns._ID + "=?",
				new String[] { String.valueOf(UpdateAssistantMoreThan2_2.lewaSetupId) },
				null);

		if (cursor.getCount() == 0) {
			return;
		}
		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
			UpdateAssistantMoreThan2_2.lewaSetupId = -1l;
			File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+"/"+VersionUtil.getApkName());
			
			Uri uri = Uri.fromFile(file); 
			Intent install = new Intent(Intent.ACTION_VIEW);
			install.setDataAndType(uri,
					"application/vnd.android.package-archive");
			install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(install);
		} else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
			try {
				if (cursor.moveToFirst()) {
					String netAddress = cursor.getString(1);
					final String mimetype = "application/vnd.android.package-archive";
					UpdateAssistantMoreThan2_2.lewaSetupId = cursor.getLong(3);
					if (Downloads.ACTION_NOTIFICATION_CLICKED.equals(action)) {
						int status = cursor.getInt(2);
						if (Downloads.isStatusCompleted(status)
								&& Downloads.isStatusSuccess(status)) {
							Intent launchIntent = new Intent(Intent.ACTION_VIEW);
							Uri path = Uri.parse(netAddress);
							// If there is no scheme, then it must be a file
							if (path.getScheme() == null) {
								path = Uri.fromFile(new File(netAddress));
							}
							launchIntent.setDataAndType(path, mimetype);
							launchIntent
									.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							try {
								context.startActivity(launchIntent);
							} catch (ActivityNotFoundException ex) {
								ex.printStackTrace();
							}
						} else {
							Message message = new Message();
							message.what = UpdateAssistantMoreThan2_2.DOWNLOAD_FINISH_FLAG;
							UpdateAssistantMoreThan2_2.handler.sendMessage(message);

						}
					}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {

				if (cursor != null)
					cursor.close();
			}

		}
	}
	public void delFile() {		
		File myFile = new File(Environment.DIRECTORY_DOWNLOADS+"/apkInstaller.apk");
		if (myFile.exists()) {
			myFile.delete();
		}
	}
}
