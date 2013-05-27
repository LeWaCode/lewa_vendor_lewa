package com.lewa.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.List;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.lewa.launcher.theme.ThemeConstants;

public class MyLauncherSettings extends PreferenceActivity implements
		OnPreferenceChangeListener, OnSharedPreferenceChangeListener {

	public static final boolean IsDebugVersion = false;
	private static final String ALMOSTNEXUS_PREFERENCES = "launcher.preferences.almostnexus";
	private boolean shouldRestart = false;
	private Context mContext;

	private static final String PREF_BACKUP_FILENAME = "lewa_settings.xml";
	private static final String CONFIG_BACKUP_FILENAME = "lewa_launcher.db";
	private static final String CONFIG_BACKUP_FILENAME_SHM = "lewa_launcher.db-shm";
	private static final String CONFIG_BACKUP_FILENAME_WAL = "lewa_launcher.db-wal";
	private static final String NAMESPACE = "com.lewa.launcher";
	private String[] mCurrentEffectStr = null;
	private Preference autoArrange;
	private String mFilePath = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: ADW should i read stored values after
		// addPreferencesFromResource?
		super.onCreate(savedInstanceState);
		getPreferenceManager()
				.setSharedPreferencesName(ALMOSTNEXUS_PREFERENCES);
		getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
		//if (Launcher.mIsRom) {
			addPreferencesFromResource(R.xml.launcher_settings_rom);
		//} else {
		//	addPreferencesFromResource(R.xml.launcher_settings);
		//}

		mContext = this;

		if (IsDebugVersion) {
			addPreferencesFromResource(R.xml.debugging_settings);
		}
		if (!Launcher.mIsRom) {
			Preference adw_version = findPreference("lewa_copyright");
			if(adw_version != null) {
				adw_version
						.setOnPreferenceClickListener(new OnPreferenceClickListener() {
							public boolean onPreferenceClick(Preference preference) {
								try {
									AlertDialog builder = AlmostNexusSettingsHelper.CopyrightDialogBuilder
											.create(mContext);
									builder.show();
								} catch (Exception e) {
									e.printStackTrace();
								}
								return false;
							}
						});
			}
		
			Preference exportConfig = findPreference("db_export");
			if(exportConfig != null) {
				exportConfig
						.setOnPreferenceClickListener(new OnPreferenceClickListener() {
							public boolean onPreferenceClick(Preference preference) {
								AlertDialog alertDialog = new AlertDialog.Builder(
										mContext).create();
								alertDialog.setTitle(getResources().getString(
										R.string.title_dialog_xml));
								alertDialog.setMessage(getResources().getString(
										R.string.message_dialog_export_config));
								alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
										getResources().getString(android.R.string.ok),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int which) {
												new ExportDatabaseTask().execute();
												new ExportPrefsTask().execute();
											}
										});
								alertDialog.setButton(
										DialogInterface.BUTTON_NEGATIVE,
										getResources().getString(
												android.R.string.cancel),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int which) {
			
											}
										});
								alertDialog.show();
								return true;
							}
						});
			}
			Preference importConfig = findPreference("db_import");
			if (importConfig != null) {
				importConfig
						.setOnPreferenceClickListener(new OnPreferenceClickListener() {
							public boolean onPreferenceClick(Preference preference) {
								AlertDialog alertDialog = new AlertDialog.Builder(
										mContext).create();
								alertDialog.setTitle(getResources().getString(
										R.string.title_dialog_xml));
								alertDialog.setMessage(getResources().getString(
										R.string.message_dialog_import_config));
								alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
										getResources().getString(android.R.string.ok),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int which) {
												new ImportDatabaseTask().execute();
												new ImportPrefsTask().execute();
											}
										});
								alertDialog.setButton(
										DialogInterface.BUTTON_NEGATIVE,
										getResources().getString(
												android.R.string.cancel),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int which) {
			
											}
										});
								alertDialog.show();
								return true;
							}
						});
			}
		}

		ListPreference effect = (ListPreference) findPreference("pref_key_effects");

		effect.setOnPreferenceChangeListener(this);
		int val = Integer.valueOf(effect.getValue());
		mCurrentEffectStr = getResources().getStringArray(
				R.array.desktop_transition_effect_entries);
		if (mCurrentEffectStr != null && val >= 0
				&& val < mCurrentEffectStr.length)
			effect.setSummary(mCurrentEffectStr[val]);

		autoArrange = findPreference("auto_arrange");
		autoArrange.setEnabled(false);
		// Begin [jxli] register a sharedpref listener
		getSharedPreferences("launcher.preferences.almostnexus",
				Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(
				this);
		// End
		
		mFilePath = new StringBuilder()
		.append(Environment.getExternalStorageDirectory().getPath())
		.append("/").append(ThemeConstants.SD_ROOT)
		.append("/").append("Launcher")
		.append("/").append("Backup").toString();
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (Launcher.isBindedDraw)
			autoArrange.setEnabled(true);
		// Begin [jxli] for set the restore_desktop_preference default checked
		// value
		CheckBoxPreference restoreDTPreference = (CheckBoxPreference) findPreference("restoreDesktopPrefences");
		if (restoreDTPreference != null) {
			restoreDTPreference.setChecked(false);
		}
		// End
	}

	@Override
	protected void onPause() {
		if (shouldRestart) {
			if (Build.VERSION.SDK_INT <= 7) {
				Intent intent = new Intent(getApplicationContext(),
						Launcher.class);
				PendingIntent sender = PendingIntent.getBroadcast(
						getApplicationContext(), 0, intent, 0);

				// We want the alarm to go off 30 seconds from now.
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());
				calendar.add(Calendar.SECOND, 1);

				// Schedule the alarm!
				AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
				am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
						sender);
				ActivityManager acm = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
				acm.restartPackage("com.lewa.launcher");
			} else {
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		getSharedPreferences("launcher.preferences.almostnexus",
				Context.MODE_PRIVATE)
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals("pref_key_effects")) {
			int val = Integer.valueOf((String) newValue);
			if (mCurrentEffectStr != null && val >= 0
					&& val < mCurrentEffectStr.length)
				preference.setSummary(mCurrentEffectStr[val]);
		}
		return true;
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// Begin [lijiuxiang, added suggestions and feedback setting & restore
		// default home, 20110708]
		if (!Launcher.mIsRom && preference.getKey().equals("feedback_suggestions_Prefences")) {
			String path = "http://www.lewaos.com/feedback";
			// send some info to web
			try {
				PackageInfo homePackageInfo = getPackageManager()
						.getPackageInfo(this.getPackageName(), 0);
				Log.e("phone info", "?LewaVersion="
						+ homePackageInfo.versionName + ",PhoneModel="
						+ Build.MODEL + ",SDK=" + Build.VERSION.SDK);
				path = path + "?LewaVersion=" + homePackageInfo.versionName
						+ "&PhoneModel=" + Build.MODEL + "&SDK="
						+ Build.VERSION.SDK;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				Uri uri = Uri.parse(path);
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(browserIntent);
			}
		} else if (preference.getKey().equals("restoreDesktopPrefences")) {
			ResolveInfo homePackageInfo = getDefaultHomeLauncher();
			if (homePackageInfo != null) {
				// back to the default home & clear lewa home default setting
				Intent homeIntent = new Intent(Intent.ACTION_MAIN);
				homeIntent.addCategory(Intent.CATEGORY_HOME);
				Log.e("home launcher resolveinfo",
						homePackageInfo.activityInfo.name);
				// getPackageManager().clearPackagePreferredActivities("com.lewa.launcher");
				homeIntent.setClassName(
						homePackageInfo.activityInfo.packageName,
						homePackageInfo.activityInfo.name);
				startActivity(homeIntent);
				finish();
			}
			return true;
			// End
		} else if (preference.getKey().equals("themePrefences")) {
			//Intent intent = new Intent().setClass(this, ThemeMain.class);
			//startActivity(intent);
		} else if (preference.getKey().equals("reset_launcher")) {
			makesureReset();
		}
		return false;
	}
	
	private void makesureReset() {
		AlertDialog alertDialog = new AlertDialog.Builder(mContext)
			         .create();
			 alertDialog.setTitle(getResources().getString(R.string.pref_title_resetlauncher));
			 alertDialog.setMessage(getResources().getString(
			         R.string.message_resetlauncher));
			 alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
			         getResources().getString(android.R.string.ok),
			         new DialogInterface.OnClickListener() {
			             @Override
			             public void onClick(DialogInterface dialog,
			                     int which) {
			            	 File dbFile = new File(Environment.getDataDirectory() 
			            	         + "/data/" + NAMESPACE + "/databases/launcher.db");
			                 File dbFile_shm = new File(Environment.getDataDirectory()
			                         + "/data/" + NAMESPACE + "/databases/launcher.db-shm");
			                 File dbFile_wal = new File(Environment.getDataDirectory()
			                         + "/data/" + NAMESPACE + "/databases/launcher.db-wal");
			            	 
			     	            if (dbFile.exists()){
			     	                dbFile.delete();
			     	                dbFile_shm.delete();
			     	                dbFile_wal.delete();
			     	                android.os.Process.killProcess(android.os.Process.myPid());
			     					finish();
			     					Intent intent = new Intent();
			     					intent.setClass(MyLauncherSettings.this, Launcher.class);
			     					startActivity(intent);
			     	            }
		     					
			             }
			         });
			 alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
			         getResources().getString(android.R.string.cancel),
			         new DialogInterface.OnClickListener() {
			             @Override
			             public void onClick(DialogInterface dialog,
			                     int which) {
			            	 //finish();
			             }
			         });
			 alertDialog.show();
	}
	

	private ResolveInfo getDefaultHomeLauncher() {
		PackageManager pManager = getPackageManager();
		Intent homeIntent = new Intent(Intent.ACTION_MAIN);
		homeIntent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> homeList = pManager.queryIntentActivities(homeIntent,
				0);
		int num = homeList.size();
		try {
			for (int i = 0; i < num; i++) {
				PackageInfo homePackageInfo = pManager.getPackageInfo(
						(homeList.get(i).activityInfo.packageName), 0);
				if ((homePackageInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
					return homeList.get(i);
				}
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Wysie: Adapted from
	// http://code.google.com/p/and-examples/source/browse/#svn/trunk/database/src/com/totsp/database
	private class ExportPrefsTask extends AsyncTask<Void, Void, String> {
		private final ProgressDialog dialog = new ProgressDialog(mContext);

		// can use UI thread here
		@Override
		protected void onPreExecute() {
			this.dialog.setMessage(getResources().getString(
					R.string.xml_export_dialog));
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		@Override
		protected String doInBackground(final Void... args) {
			if (!Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())) {
				return getResources().getString(
						R.string.import_export_sdcard_unmounted);
			}

			File prefFile = new File(Environment.getDataDirectory() + "/data/"
					+ NAMESPACE
					+ "/shared_prefs/launcher.preferences.almostnexus.xml");
			
			File dirFile = new File(mFilePath);
			if(!dirFile.exists()){
				dirFile.mkdirs();
			}
			
			File file = new File(mFilePath,	PREF_BACKUP_FILENAME);

			try {
				file.createNewFile();
				copyFile(prefFile, file);
				return getResources().getString(R.string.xml_export_success);
			} catch (IOException e) {
				return getResources().getString(R.string.xml_export_error);
			}
		}

		// can use UI thread here
		@Override
		protected void onPostExecute(final String msg) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
		}
	}

	// Wysie: Adapted from
	// http://code.google.com/p/and-examples/source/browse/#svn/trunk/database/src/com/totsp/database
	private class ImportPrefsTask extends AsyncTask<Void, Void, String> {
		private final ProgressDialog dialog = new ProgressDialog(mContext);

		@Override
		protected void onPreExecute() {
			this.dialog.setMessage(getResources().getString(
					R.string.xml_import_dialog));
			this.dialog.show();
		}

		// could pass the params used here in AsyncTask<String, Void, String> -
		// but not being re-used
		@Override
		protected String doInBackground(final Void... args) {
			if (!Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())) {
				return getResources().getString(
						R.string.import_export_sdcard_unmounted);
			}

			File prefBackupFile = new File(mFilePath, PREF_BACKUP_FILENAME);

			if (!prefBackupFile.exists()) {
				return getResources().getString(R.string.xml_file_not_found);
			} else if (!prefBackupFile.canRead()) {
				return getResources().getString(R.string.xml_not_readable);
			}

			File prefFile = new File(Environment.getDataDirectory() + "/data/"
					+ NAMESPACE
					+ "/shared_prefs/launcher.preferences.almostnexus.xml");

			if (prefFile.exists()) {
				prefFile.delete();
			}

			try {
				prefFile.createNewFile();
				copyFile(prefBackupFile, prefFile);
				shouldRestart = true;
				return getResources().getString(R.string.xml_import_success);
			} catch (IOException e) {
				return getResources().getString(R.string.xml_import_error);
			}
		}

		@Override
		protected void onPostExecute(final String msg) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
		}
	}

	// Wysie: Adapted from
	// http://code.google.com/p/and-examples/source/browse/#svn/trunk/database/src/com/totsp/database
	private class ExportDatabaseTask extends AsyncTask<Void, Void, String> {
		private final ProgressDialog dialog = new ProgressDialog(mContext);

		// can use UI thread here
		@Override
		protected void onPreExecute() {
			this.dialog.setMessage(getResources().getString(
					R.string.dbfile_export_dialog));
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		@Override
		protected String doInBackground(final Void... args) {
			if (!Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())) {
				return getResources().getString(
						R.string.import_export_sdcard_unmounted);
			}

			File dbFile = new File(Environment.getDataDirectory() + "/data/"
					+ NAMESPACE + "/databases/launcher.db");

			File dbFile_shm = new File(Environment.getDataDirectory()
					+ "/data/" + NAMESPACE + "/databases/launcher.db-shm");

			File dbFile_wal = new File(Environment.getDataDirectory()
					+ "/data/" + NAMESPACE + "/databases/launcher.db-wal");

			File dirFile = new File(mFilePath);
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}

			File file = new File(mFilePath, CONFIG_BACKUP_FILENAME);

			File file_shm = new File(mFilePath, CONFIG_BACKUP_FILENAME_SHM);

			File file_wal = new File(mFilePath, CONFIG_BACKUP_FILENAME_WAL);

			try {
				file.createNewFile();
				copyFile(dbFile, file);

				dbFile_shm.createNewFile();
				copyFile(dbFile_shm, file_shm);

				dbFile_wal.createNewFile();
				copyFile(dbFile_wal, file_wal);

				return getResources().getString(R.string.dbfile_export_success);
			} catch (IOException e) {
				return getResources().getString(R.string.dbfile_export_error);
			}
		}

		// can use UI thread here
		@Override
		protected void onPostExecute(final String msg) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
		}
	}

	// Wysie: Adapted from
	// http://code.google.com/p/and-examples/source/browse/#svn/trunk/database/src/com/totsp/database
	private class ImportDatabaseTask extends AsyncTask<Void, Void, String> {
		private final ProgressDialog dialog = new ProgressDialog(mContext);

		@Override
		protected void onPreExecute() {
			this.dialog.setMessage(getResources().getString(
					R.string.dbfile_import_dialog));
			this.dialog.show();
		}

		// could pass the params used here in AsyncTask<String, Void, String> -
		// but not being re-used
		@Override
		protected String doInBackground(final Void... args) {
			if (!Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())) {
				return getResources().getString(
						R.string.import_export_sdcard_unmounted);
			}

			boolean isExits_From = false;
			boolean isExits_to = false;

			File dbBackupFile = new File(mFilePath, CONFIG_BACKUP_FILENAME);

			if (!dbBackupFile.exists()) {
				return getResources().getString(R.string.dbfile_not_found);
			} else if (!dbBackupFile.canRead()) {
				return getResources().getString(R.string.dbfile_not_readable);
			}

			File dbBackupFile_shm = new File(mFilePath, CONFIG_BACKUP_FILENAME_SHM);

			File dbBackupFile_wal = new File(mFilePath, CONFIG_BACKUP_FILENAME_WAL);

			if (dbBackupFile_shm.exists() && dbBackupFile_wal.exists()) {
				isExits_From = true;
			}

			File dbFile = new File(Environment.getDataDirectory() + "/data/"
					+ NAMESPACE + "/databases/launcher.db");
			File dbFile_shm = new File(Environment.getDataDirectory()
					+ "/data/" + NAMESPACE + "/databases/launcher.db-shm");
			File dbFile_wal = new File(Environment.getDataDirectory()
					+ "/data/" + NAMESPACE + "/databases/launcher.db-wal");

			if (dbFile.exists()) {
				dbFile.delete();
			}

			if (dbFile_shm.exists() && dbFile_wal.exists()) {
				dbFile_shm.delete();
				dbFile_wal.delete();
				isExits_to = true;
			}

			try {
				dbFile.createNewFile();
				copyFile(dbBackupFile, dbFile);

				if (isExits_From && isExits_to) {
					dbFile_shm.createNewFile();
					copyFile(dbBackupFile_shm, dbFile_shm);
					dbFile_wal.createNewFile();
					copyFile(dbBackupFile_wal, dbFile_wal);
				}

				shouldRestart = true;
				return getResources().getString(R.string.dbfile_import_success);
			} catch (IOException e) {
				return getResources().getString(R.string.dbfile_import_error);
			}
		}

		@Override
		protected void onPostExecute(final String msg) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
		}
	}

	public static void copyFile(File src, File dst) throws IOException {
		FileChannel inChannel = new FileInputStream(src).getChannel();
		FileChannel outChannel = new FileOutputStream(dst).getChannel();

		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {

			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	// Begin [jxli] for Close self activity when set lewa theme
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		// TODO Auto-generated method stub
		// if (AlmostNexusSettingsHelper.needsRestart(key)) {
		// finish();
		// }
	}

	// End

	// Begin [jxli] for avoid switch screen
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		Log.e("mylauncherSetting", "onConfigurationChanged");
		try {
			super.onConfigurationChanged(newConfig);

			if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		} catch (Exception ex) {
		}
	}
	// End
}
