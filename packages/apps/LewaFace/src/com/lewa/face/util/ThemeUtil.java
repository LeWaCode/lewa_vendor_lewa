package com.lewa.face.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.IActivityManager;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.lewaface.LewaTheme;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.lewa.face.R;
import com.lewa.face.app.ThemeApplication;
import com.lewa.face.model.ThemeDeleteTask;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.pojos.ThemeModelInfo;
import com.lewa.face.preview.slide.base.OnlineSlideBase;
import com.lewa.face.preview.slide.online.ThemePkg;
import com.lewa.os.ui.ActivityResultBridge;
import com.lewa.os.ui.ActivityResultBridge.ActivityResultReceiver;
import android.content.res.lewaface.*;
/**
 * 
 * @author fulw
 * 
 */
public class ThemeUtil implements ThemeConstants {

	private static final String TAG = ThemeUtil.class.getSimpleName();

	private static final boolean DEBUG_MODLE = true;

	public static boolean isWVGA = false;

	public static boolean isEN = false;

	public static String THEME_URL;
	public static String THEME_INFO_URL;
	public static String WALLPAPER_URL;
	public static String WALLPAPER_PREVIEW_URL;
	public static String WALLPAPER_THUMBNAIL_URL;
	public static String WALLPAPER_INFO_URL;
	// public static String LOCKSCREEN_URL;
	public static String LOCKSCREEN_INFO_URL;
	// public static String FONTS_URL;
	public static String FONTS_INFO_URL;
	// public static String BOOTS_URL;
	public static String BOOTS_INFO_URL;
	// Begin, When software is FC ,reset downloadThreads which are still
	// downloading, add by zjyu ,2012.5.15
	public static String dcontrolFlag = "true"; // 涓嬭浇鎺у埗鏍囩,涓簍rue琛ㄧず鍙互鍔犺浇
	public static boolean threadcontrolFlag = true; // 鎺у埗绾跨▼鑳藉惁杩愯鏍囩
	public static boolean startFlag = true; // 鎺у埗绾跨▼鍙惎鍔ㄤ竴娆�
	public static ArrayList<ThemeBase> baselist = new ArrayList<ThemeBase>(); // 涓嬭浇涓婚闃熷垪
	public static Set<String> modulesOnApplied = new TreeSet<String>();
	public static boolean firstApply;

	// End
	public static void multipleSend(Activity context, List<File> files) {
		ArrayList<Uri> paths = new ArrayList<Uri>();
		for (File fi : files) {
			paths.add(Uri.fromFile(fi));
		}
		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		if (paths.size() > 1) {
			intent.putExtra(ThemeConstants.KEY_LEWA_SEND_FLAG,
					ThemeConstants.VALUE_LEWA_MULTY_SEND_FLAG);
		} else if (paths.size() == 1) {
			intent.putExtra(ThemeConstants.KEY_LEWA_SEND_FLAG,
					ThemeConstants.VALUE_LEWA_SEND_FLAG);
		}
		intent.setType("*/*");
		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, paths);
		context.startActivity(Intent.createChooser(intent, context.getTitle()));
	}

	// Begin, When software is FC ,reset downloadThreads which are still
	// downloading, add by zjyu ,2012.5.15
	public static void resetDownloadThread() {
		OnlineSlideBase osb = new ThemePkg();
		Context context = osb;
		SharedPreferences.Editor editor = context.getSharedPreferences(
				"DOWNLOADED", Context.MODE_PRIVATE).edit();
		while (!baselist.isEmpty()) {
			editor.putLong(baselist.get(0).getPkg(),
					ThemeConstants.DOWNLOADFAIL);
			baselist.remove(0);
		}
		editor.commit();
	}

	// End

	public static void initURL() {
		if (isWVGA) {
			THEME_URL = new StringBuilder().append(ThemeConstants.URL)
					.append("/wvga/theme").toString();
			THEME_INFO_URL = new StringBuilder().append(ThemeConstants.URL)
					.append("/wvga/theme/Themes.xls").toString();
			WALLPAPER_URL = new StringBuilder().append(ThemeConstants.URL)
					.append("/wvga/wallpaper").toString();
			WALLPAPER_PREVIEW_URL = new StringBuilder()
					.append(ThemeConstants.URL)
					.append("/wvga/wallpaper/preview").toString();
			WALLPAPER_THUMBNAIL_URL = new StringBuilder()
					.append(ThemeConstants.URL)
					.append("/wvga/wallpaper/thumbnail").toString();
			WALLPAPER_INFO_URL = new StringBuilder().append(ThemeConstants.URL)
					.append("/wvga/wallpaper/thumbnail/Wallpaper.xls")
					.toString();
			// LOCKSCREEN_URL = new
			// StringBuilder().append(ThemeConstants.URL).append("/wvga/theme").toString();
			LOCKSCREEN_INFO_URL = new StringBuilder()
					.append(ThemeConstants.URL)
					.append("/wvga/theme/Lockscreen.xls").toString();
			// FONTS_URL = new
			// StringBuilder().append(ThemeConstants.URL).append("/wvga/theme").toString();
			FONTS_INFO_URL = new StringBuilder().append(ThemeConstants.URL)
					.append("/wvga/theme/Fonts.xls").toString();
			// BOOTS_URL = new
			// StringBuilder().append(ThemeConstants.URL).append("/wvga/theme").toString();
			BOOTS_INFO_URL = new StringBuilder().append(ThemeConstants.URL)
					.append("/wvga/theme/BootAnimation.xls").toString();
		} else {
			THEME_URL = new StringBuilder().append(ThemeConstants.URL)
					.append("/hvga/theme").toString();
			THEME_INFO_URL = new StringBuilder().append(ThemeConstants.URL)
					.append("/hvga/theme/Themes.xls").toString();
			WALLPAPER_URL = new StringBuilder().append(ThemeConstants.URL)
					.append("/hvga/wallpaper").toString();
			WALLPAPER_PREVIEW_URL = new StringBuilder()
					.append(ThemeConstants.URL)
					.append("/hvga/wallpaper/preview").toString();
			WALLPAPER_THUMBNAIL_URL = new StringBuilder()
					.append(ThemeConstants.URL)
					.append("/hvga/wallpaper/thumbnail").toString();
			WALLPAPER_INFO_URL = new StringBuilder().append(ThemeConstants.URL)
					.append("/hvga/wallpaper/thumbnail/Wallpaper.xls")
					.toString();
			// LOCKSCREEN_URL = new
			// StringBuilder().append(ThemeConstants.URL).append("/hvga/theme").toString();
			LOCKSCREEN_INFO_URL = new StringBuilder()
					.append(ThemeConstants.URL)
					.append("/hvga/theme/Lockscreen.xls").toString();
			// FONTS_URL = new
			// StringBuilder().append(ThemeConstants.URL).append("/hvga/theme").toString();
			FONTS_INFO_URL = new StringBuilder().append(ThemeConstants.URL)
					.append("/hvga/theme/Fonts.xls").toString();
			// BOOTS_URL = new
			// StringBuilder().append(ThemeConstants.URL).append("/hvga/theme").toString();
			BOOTS_INFO_URL = new StringBuilder().append(ThemeConstants.URL)
					.append("/hvga/theme/BootAnimation.xls").toString();
		}
	}

	public static void debugMode() {
		if (DEBUG_MODLE) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectNetwork().penaltyLog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectAll().penaltyLog().build());
		}

	}

	private static final Runtime s_runtime = Runtime.getRuntime();

	private static long usedMemory() {
		return s_runtime.totalMemory() - s_runtime.freeMemory();
	}

	@SuppressWarnings("static-access")
	public static void runGC() {
		long usedMem1 = usedMemory(), usedMem2 = Long.MAX_VALUE;
		for (int i = 0; (usedMem1 < usedMem2) && (i < 20); ++i) {
			s_runtime.runFinalization();
			s_runtime.gc();
			Thread.currentThread().yield();
			usedMem2 = usedMem1;
			usedMem1 = usedMemory();
		}
	}

	/**
	 * create a .nomedia file to prevent the mediascanner from scanning media
	 * files in LEWA/theme directory
	 * 
	 * @param nomediaPath
	 *            where create the file
	 */
	public static void createNomedia(String nomediaPath) {
		try {
			File nomedia = new File(new StringBuilder().append(nomediaPath)
					.append("/.nomedia").toString());
			if (!nomedia.exists()) {
				nomedia.createNewFile();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Can not create .nomedia file!");
			e.printStackTrace();
		}
	}

	/**
	 * the network is work well?
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetWorkEnable(Context context) {
		boolean isNetworkAvailable = false;
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager.getActiveNetworkInfo() != null) {
			isNetworkAvailable = connManager.getActiveNetworkInfo()
					.isAvailable();
		}
		return isNetworkAvailable;
	}

	/**
	 * the network type,is wifi or gprs?
	 * 
	 * @param context
	 * @return
	 */
	public static String getNetworkType(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		if (state == State.CONNECTED || state == State.CONNECTING) {
			return "wifi";
		}

		state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.getState();
		if (state == State.CONNECTED || state == State.CONNECTING) {
			return "mobile";
		}
		return "none";

	}

	/**
	 * the sdcard is work well,and is not in data storage mode
	 * 
	 * @return
	 */
	public static boolean isSDCardEnable() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}

	/**
	 * @param themeType
	 *            (0:THEME_LOCAL 1:THEME_ONLINE)
	 * @return
	 */
	public static ArrayList<String> getPreviewPathByThemeType(int themeType) {
		ArrayList<String> previewPaths = new ArrayList<String>();
		if (themeType == THEME_LOCAL) {
			File localPreview = new File(THEME_LOCAL_PREVIEW);
			if (!localPreview.exists()) {
				localPreview.mkdirs();
				return previewPaths;// size is 0
			}
			File[] localPreviews = localPreview.listFiles();
			int localPreviewCount = localPreviews.length;
			for (int i = 0; i < localPreviewCount; i++) {
				previewPaths.add(localPreviews[i].getName());
			}
		} else {

		}
		return previewPaths;
	}

	/**
	 * run shell commond
	 * 
	 * @param commond
	 * @return
	 */
	private static boolean runShellBat(String commond) {
		Runtime runtime = Runtime.getRuntime();
		String[] commonds = new String[3];
		commonds[0] = "sh";
		commonds[1] = "-c";
		commonds[2] = commond;
		try {
			Process process = runtime.exec(commonds);
			int exitValue = process.waitFor();
			if (exitValue != 0) {
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static void changeFilePermission(File file) {
		if (file != null) {
			file.setLastModified(System.currentTimeMillis());

			runShellBat(new StringBuilder().append("chmod 777 ")
					.append(file.getAbsolutePath()).toString());
		}
	}

	/**
	 * copy source file to target file,if success return true,or return false
	 * the param deleteSource is make sure if delete the source file
	 * 
	 * @param source
	 * @param target
	 * @param deleteSource
	 * @return
	 */
	public static boolean copyFile(File source, File target,
			boolean deleteSource) {
		FileInputStream fis = null;
		try {

			fis = new FileInputStream(source);

			FileUtils.copyInputStreamToFile(fis, target);

			if (deleteSource && source != null && source.exists()) { // for test
				FileUtils.forceDelete(source);
			}
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
					fis = null;
				}
			} catch (Exception e2) {
			}

		}
		return false;
	}

	public static boolean unThemeZIP(File zipFile) {

		ZipFile zip = null;

		try {
			zip = new ZipFile(zipFile);

			String nameNoLwt = getNameNoBuffix(zipFile.getName());

			@SuppressWarnings("unchecked")
			Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip
					.entries();

			ZipEntry zipEntry = null;

			String localPreviewPath = new StringBuilder()
					.append(ThemeConstants.THEME_LOCAL_PREVIEW).append("/")
					.append(nameNoLwt).toString();
			File localPreview = new File(localPreviewPath);
			if (!localPreview.exists()) {
				localPreview.mkdirs();
			}

			while (entries.hasMoreElements()) {
				zipEntry = entries.nextElement();
				String entryName = zipEntry.getName();

				InputStream source = zip.getInputStream(zipEntry);
				FileOutputStream fos = null;
				FileInputStream fis = null;
				FileOutputStream target = null;
				FileOutputStream wallpaperFos = null;
				FileInputStream wallpaperFis = null;
				try {
					if (entryName.indexOf("preview") != -1
							&& entryName.indexOf(ThemeConstants.BUFFIX_JPG) != -1) {

						File targetFile = new File(localPreviewPath,
								entryName.substring(entryName.indexOf("/")));
						target = new FileOutputStream(targetFile);
						if (entryName.indexOf(ThemeConstants.MODEL_PREVIEW_PIM) != -1) {
							writeSourceToTarget(source, target);
							fis = new FileInputStream(targetFile);
							createThumbnailForBuffix(entryName,
									ThemeConstants.THEME_THUMBNAIL_PIM_PREFIX,
									nameNoLwt, fis);

						}
						if (entryName
								.indexOf(ThemeConstants.MODEL_PREVIEW_PHONE) != -1) {
							writeSourceToTarget(source, target);
							fis = new FileInputStream(targetFile);
							createThumbnailForBuffix(
									entryName,
									ThemeConstants.THEME_THUMBNAIL_PHONE_PREFIX,
									nameNoLwt, fis);

						}
						if (entryName
								.indexOf(ThemeConstants.MODEL_PREVIEW_SETTING) != -1) {
							writeSourceToTarget(source, target);
							fis = new FileInputStream(targetFile);
							createThumbnailForBuffix(
									entryName,
									ThemeConstants.THEME_THUMBNAIL_SETTING_PREFIX,
									nameNoLwt, fis);

						}
						if (entryName
								.indexOf(ThemeConstants.MODEL_PREVIEW_ICONS) != -1) {
							writeSourceToTarget(source, target);
							fis = new FileInputStream(targetFile);
							createThumbnailForBuffix(
									entryName,
									ThemeConstants.THEME_THUMBNAIL_ICONS_PREFIX,
									nameNoLwt, fis);

						} else if (entryName
								.indexOf(ThemeConstants.MODEL_PREVIEW_LAUNCHER) != -1) {
							writeSourceToTarget(source, target);
							fis = new FileInputStream(targetFile);

							createThumbnailForBuffix(
									entryName,
									ThemeConstants.THEME_THUMBNAIL_LAUNCHER_PREFIX,
									nameNoLwt, fis);

						} else if (entryName
								.indexOf(ThemeConstants.MODEL_PREVIEW_LOCKSCREEN) != -1) {
							writeSourceToTarget(source, target);
							fis = new FileInputStream(targetFile);

							createThumbnailForBuffix(
									entryName,
									ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_PREFIX,
									nameNoLwt, fis);

						} else if (entryName
								.indexOf(ThemeConstants.MODEL_PREVIEW_BOOTANIMATION) != -1) {
							writeSourceToTarget(source, target);
							fis = new FileInputStream(targetFile);

							createThumbnailForBuffix(
									entryName,
									ThemeConstants.THEME_THUMBNAIL_BOOTS_PREFIX,
									nameNoLwt, fis);

						} else if (entryName
								.indexOf(ThemeConstants.MODEL_PREVIEW_NOTIFY) != -1) {
							writeSourceToTarget(source, target);
							fis = new FileInputStream(targetFile);

							createThumbnailForBuffix(
									entryName,
									ThemeConstants.THEME_THUMBNAIL_SYSTEMUI_PREFIX,
									nameNoLwt, fis);

						} else if (entryName
								.indexOf(ThemeConstants.MODEL_PREVIEW_FONTS) != -1) {
							if (entryName.indexOf("thumbnail_fonts") != -1) {

								String targetPath = new StringBuilder()
										.append(ThemeConstants.THEME_LOCAL_THUMBNAIL)
										.append("/")
										.append(ThemeConstants.THEME_THUMBNAIL_FONTS_PREFIX)
										.append(nameNoLwt).toString();

								target = new FileOutputStream(targetPath);

							}

							writeSourceToTarget(source, target);

							// fis = new FileInputStream(targetFile);

							// createThumbnailForBuffix(entryName,ThemeConstants.THEME_THUMBNAIL_FONTS_PREFIX,
							// nameNoLwt, fis);
						}
						 else if (entryName
									.indexOf(ThemeConstants.MODEL_PREVIEW_OTHERS) != -1) {
								if (entryName.indexOf("thumbnail_fonts") != -1) {

									String targetPath = new StringBuilder()
											.append(ThemeConstants.THEME_LOCAL_THUMBNAIL)
											.append("/")
											.append(ThemeConstants.THEME_THUMBNAIL_OTHERS_PREFIX)
											.append(nameNoLwt).toString();

									target = new FileOutputStream(targetPath);

								}

								writeSourceToTarget(source, target);
							}
					} else if (entryName.indexOf("wallpaper.jpg") != -1) {

						if (entryName.indexOf(ThemeConstants.BUFFIX_JPG) != -1) {
							if (entryName
									.indexOf(ThemeConstants.MODEL_PREVIEW_LOCKSCREEN_WALLPAPER) != -1) {

								String lockscreenWallpaperPath = new StringBuilder()
										.append(ThemeConstants.THEME_WALLPAPER)
										.append("/")
										.append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_WALLPAPER_PREFIX)
										.append(nameNoLwt).toString();

								File lockscreenWallpaper = new File(
										lockscreenWallpaperPath);
								if (!lockscreenWallpaper.getParentFile()
										.exists()) {
									lockscreenWallpaper.getParentFile()
											.mkdirs();
								}
								wallpaperFos = new FileOutputStream(
										lockscreenWallpaperPath);
								writeSourceToTarget(source, wallpaperFos);

								String lockscreenWallpaperThumbNailPath = new StringBuilder()
										.append(ThemeConstants.THEME_LOCAL_WALLPAPER_THUMBNAIL)
										.append("/")
										.append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_WALLPAPER_PREFIX)
										.append(nameNoLwt).toString();

								File lockscreenWallpaperThumbNail = new File(
										lockscreenWallpaperThumbNailPath);
								if (!lockscreenWallpaperThumbNail
										.getParentFile().exists()) {
									lockscreenWallpaperThumbNail
											.getParentFile().mkdirs();
								}
								wallpaperFis = new FileInputStream(
										lockscreenWallpaper);
								createThumbnail(wallpaperFis,
										lockscreenWallpaperThumbNail, 60, true);

							} else if (entryName
									.indexOf(ThemeConstants.MODEL_PREVIEW_WALLPAPER) != -1) {

								String wallpaperPath = new StringBuilder()
										.append(ThemeConstants.THEME_WALLPAPER)
										.append("/")
										.append(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX)
										.append(nameNoLwt).toString();

								File wallpaper = new File(wallpaperPath);
								if (!wallpaper.getParentFile().exists()) {
									wallpaper.getParentFile().mkdirs();
								}
								wallpaperFos = new FileOutputStream(
										wallpaperPath);
								writeSourceToTarget(source, wallpaperFos);

								String wallpaperThumbNailPath = new StringBuilder()
										.append(ThemeConstants.THEME_LOCAL_WALLPAPER_THUMBNAIL)
										.append("/")
										.append(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX)
										.append(nameNoLwt).toString();

								File wallpaperThumbNail = new File(
										wallpaperThumbNailPath);
								if (!wallpaperThumbNail.getParentFile()
										.exists()) {
									wallpaperThumbNail.getParentFile().mkdirs();
								}
								wallpaperFis = new FileInputStream(wallpaper);
								createThumbnail(wallpaperFis,
										wallpaperThumbNail, 60, true);

							}
						}

					} else if (entryName.indexOf("json") != -1) {
						File targetFile = new File(localPreviewPath, entryName);
						target = new FileOutputStream(targetFile);
						writeSourceToTarget(source, target);
					} else if (entryName.indexOf("lockscreen") != -1) {
						String fileName = new StringBuilder()
								.append(ThemeConstants.THEME_MODEL_LOCKSCREEN)
								.append("_").append(nameNoLwt).toString();
						File targetFile = new File(
								ThemeConstants.THEME_MODEL_LOCKSCREEN_STYLE,
								fileName);

						target = new FileOutputStream(targetFile);
						writeSourceToTarget(source, target);
					} else if (entryName.indexOf("icons") != -1) {
						String fileName = new StringBuilder()
								.append(ThemeConstants.THEME_MODEL_ICONS)
								.append("_").append(nameNoLwt).toString();
						File targetFile = new File(
								ThemeConstants.THEME_MODEL_ICONS_STYLE,
								fileName);

						target = new FileOutputStream(targetFile);
						writeSourceToTarget(source, target);
					} else if (entryName.indexOf("launcher") != -1) {
						String fileName = new StringBuilder()
								.append(ThemeConstants.THEME_MODEL_LAUNCHER)
								.append("_").append(nameNoLwt).toString();
						File targetFile = new File(
								ThemeConstants.THEME_MODEL_LAUNCHER_STYLE,
								fileName);

						target = new FileOutputStream(targetFile);
						writeSourceToTarget(source, target);
					} else if (entryName.indexOf("boots") != -1) {
						String fileName = new StringBuilder()
								.append(ThemeConstants.THEME_MODEL_BOOTS)
								.append("_").append(nameNoLwt).toString();
						File targetFile = new File(
								ThemeConstants.THEME_MODEL_BOOTS_STYLE,
								fileName);

						target = new FileOutputStream(targetFile);
						writeSourceToTarget(source, target);
					} else if (entryName.indexOf("fonts") != -1) {
						String fileName = new StringBuilder()
								.append(ThemeConstants.THEME_MODEL_FONTS)
								.append("_").append(nameNoLwt).toString();
						File targetFile = new File(
								ThemeConstants.THEME_MODEL_FONTS_STYLE,
								fileName);

						target = new FileOutputStream(targetFile);
						writeSourceToTarget(source, target);
					} else if (entryName.indexOf("com.android.systemui") != -1) {
						String fileName = new StringBuilder()
								.append(ThemeConstants.THEME_MODEL_NOTIFY)
								.append("_").append(nameNoLwt).toString();
						File targetFile = new File(
								ThemeConstants.THEME_MODEL_NOTIFY_STYLE,
								fileName);
						if (!targetFile.getParentFile().exists()) {
							targetFile.getParentFile().mkdirs();
						}
						target = new FileOutputStream(targetFile);
						writeSourceToTarget(source, target);
					} else if (entryName.indexOf("com.android.phone") != -1) {
						String fileName = new StringBuilder()
								.append(ThemeConstants.THEME_MODEL_PHONE)
								.append("_").append(nameNoLwt).toString();
						File targetFile = new File(
								ThemeConstants.THEME_MODEL_PHONE_STYLE,
								fileName);
						if (!targetFile.getParentFile().exists()) {
							targetFile.getParentFile().mkdirs();
						}
						target = new FileOutputStream(targetFile);
						writeSourceToTarget(source, target);
					}

					else if (entryName.indexOf("com.android.settings") != -1) {
						String fileName = new StringBuilder()
								.append(ThemeConstants.THEME_MODEL_SETTINGS)
								.append("_").append(nameNoLwt).toString();
						File targetFile = new File(
								ThemeConstants.THEME_MODEL_SETTING_STYLE,
								fileName);
						if (!targetFile.getParentFile().exists()) {
							targetFile.getParentFile().mkdirs();
						}
						target = new FileOutputStream(targetFile);
						writeSourceToTarget(source, target);
					} else if (entryName.indexOf("com.lewa.PIM") != -1) {
						String fileName = new StringBuilder()
								.append(ThemeConstants.THEME_MODEL_PIM)
								.append("_").append(nameNoLwt).toString();
						File targetFile = new File(
								ThemeConstants.THEME_MODEL_PIM_STYLE, fileName);
						if (!targetFile.getParentFile().exists()) {
							targetFile.getParentFile().mkdirs();
						}
						target = new FileOutputStream(targetFile);
						writeSourceToTarget(source, target);
					} else if (entryName.toLowerCase().indexOf("framework-res") != -1) {
						String fileName = new StringBuilder()
								.append(ThemeConstants.THEME_MODEL_FRAMEWORK)
								.append("_").append(nameNoLwt).toString();
						File targetFile = new File(
								ThemeConstants.THEME_MODEL_FRAMEWORK_STYLE,
								fileName);
						if (!targetFile.getParentFile().exists()) {
							targetFile.getParentFile().mkdirs();
						}
						target = new FileOutputStream(targetFile);
						writeSourceToTarget(source, target);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (wallpaperFos != null) {
						wallpaperFos.close();
						wallpaperFos = null;
					}
					if (target != null) {
						target.close();
						target = null;
					}
					if (source != null) {
						source.close();
						source = null;
					}
					if (fos != null) {
						fos.close();
						fos = null;
					}
					if (fis != null) {
						fis.close();
						fis = null;
					}
					if (wallpaperFis != null) {
						wallpaperFis.close();
						wallpaperFis = null;
					}
				}

			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (zip != null) {
					zip.close();
					zip = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return false;
	}

	public static boolean writeSourceToTarget(InputStream source,
			FileOutputStream target) {
		BufferedOutputStream bos = null;

		try {
			bos = new BufferedOutputStream(target);
			byte[] buffer = new byte[10240];
			int temp = -1;
			while ((temp = source.read(buffer)) != -1) {
				bos.write(buffer, 0, temp);
			}
			bos.flush();
			target.flush();
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (bos != null) {
					bos.close();
					bos = null;
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 鏍规嵁鍚嶇О涓甫鏈塤0鐨勫浘鐗囩敓鎴愮缉鐣ュ浘锛堜竴鑸琠0鐨勫浘鐗囬兘涓鸿妯″潡鐨勭涓�紶鍥剧墖锛�
	 * 
	 * @param zipEntryName
	 * @param prefix
	 * @param suffix
	 * @param source
	 */
	public static void createThumbnailForBuffix(String zipEntryName,
			String prefix, String suffix, FileInputStream source) {
		FileOutputStream target = null;
		try {
			if (zipEntryName.lastIndexOf("_0") != -1) {
				File thumbnail = new File(new StringBuilder()
						.append(ThemeConstants.THEME_LOCAL_THUMBNAIL)
						.append("/").append(prefix).append(suffix).toString());

				createThumbnail(source, thumbnail, 60, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (target != null) {
					target.close();
					target = null;
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

	}

	/**
	 * create thumbnail with the screen size(WVGA or HVGA)
	 * 
	 * @param is
	 * @param fos
	 * @param quality
	 *            鍘嬬缉璐ㄩ噺
	 * @param changeSize
	 *            鏄惁鐢熸垚缂╃暐鍥撅紝濡傛灉涓簍rue锛屽垯鏍规嵁灞忓箷鍒嗚鲸鐜囩敓鎴愬搴旂殑缂╃暐鍥�
	 * @return
	 */
	public static boolean createThumbnail(InputStream source, File target,
			int quality, boolean changeSize) {

		Bitmap bitmap = null;
		FileOutputStream fos = null;

		try {
			if (source == null) {
				return false;
			}
			bitmap = BitmapFactory.decodeStream(source, null, getOptions(1));

			if (bitmap == null) {
				Log.e(TAG, "Can't decode to Bitmap from IO!");
				return false;
			}

			if (changeSize) {
				boolean isWVGA = ThemeUtil.isWVGA;
				if (isWVGA) {
					bitmap = ThumbnailUtils.extractThumbnail(bitmap,
							ThemeConstants.THUMBNAIL_WVGA_WIDTH,
							ThemeConstants.THUMBNAIL_WVGA_HEIGHT,
							ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
				} else {
					bitmap = ThumbnailUtils.extractThumbnail(bitmap,
							ThemeConstants.THUMBNAIL_HVGA_WIDTH,
							ThemeConstants.THUMBNAIL_HVGA_HEIGHT,
							ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
				}
			}
			File parent = target.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			fos = new FileOutputStream(target);
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);

			fos.flush();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
					fos = null;
				}
				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}

		}

		return false;
	}

	public static String fileLengthToSize(long length) {

		StringBuilder sb = new StringBuilder();
		long file_B = length % 1024;
		long file_KB = length / 1024;
		long file_KB_B = file_B;
		long file_MB = file_KB / 1024;
		long file_MB_KB = file_KB % 1024;
		// The order can't change
		if (file_MB > 0) {
			sb.append(file_MB).append(".")
					.append(String.valueOf(file_MB_KB).substring(0, 1))
					.append("MB");
		} else if (file_KB > 0) {
			sb.append(file_KB).append(".")
					.append(String.valueOf(file_KB_B).substring(0, 1))
					.append("KB");
		} else if (file_B > 0) {
			sb.append(length).append("B");
		}
		return sb.toString();
	}

	public static Options getOptions(int inSampleSize) {

		Options options = new Options();

		options.inPurgeable = true;
		options.inInputShareable = true;
		try {
			BitmapFactory.Options.class.getField("inNativeAlloc").setBoolean(
					options, true);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		options.inSampleSize = inSampleSize;
		return options;
	}

	/**
	 * the sdcard space
	 * 
	 * @param sizeMb
	 * @return
	 */
	public static boolean sdcardHasSpace(int sizeMb) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

			String sdcard = Environment.getExternalStorageDirectory().getPath();
			StatFs statFs = new StatFs(sdcard);
			long blockSize = statFs.getBlockSize();
			long blocks = statFs.getAvailableBlocks();
			long availableSpare = (blocks * blockSize) / (1024 * 1024);
			if (sizeMb > availableSpare) {
				return false;
			} else {
				return true;
			}
		}
		return false;

	}

	public static String getNameNoBuffix(String pkg) {
		String name = null;
		if (pkg != null) {
			if (pkg.lastIndexOf(".") != -1) {
				name = pkg.substring(0, pkg.lastIndexOf("."));
			} else {
				name = pkg;
			}
		}
		return name;
	}

	public static void exitApplication(Context context) {
		SharedPreferenceUtil.putValue(context,
				ThemeApplication.modulesOnApplied,
				ThemeApplication.modulesOnAppliedKey,

				ModulesOnAppliedUtil.toString(ThemeUtil.modulesOnApplied));
		exitApplication();
	}
	public static void exitApplication() {
		ArrayList<Activity> activities = ThemeApplication.activities;
		int size = activities.size();
		for (int i = 0; i < size; i++) {
			activities.get(i).finish();
		}
	}

	public static void showToast(Context context, int strId, boolean shortTime) {
		if (shortTime) {
			Toast.makeText(context, strId, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, strId, Toast.LENGTH_LONG).show();
		}

	}

	/**
	 * 浠绘剰璺緞涓嬬殑lwt鏂囦欢閮藉彲浠ヨ鍒嗕韩
	 */
	public static void shareByBT(ThemeBase themeBase, Activity activity) {

		File shareFile = null;
		if (themeBase.getLwtPath() == null) {
			shareFile = new File(new StringBuilder()
					.append(ThemeConstants.THEME_LWT).append("/")
					.append(themeBase.getPkg()).toString());
		} else {
			shareFile = new File(themeBase.getLwtPath());
		}
		List<File> files = new ArrayList<File>();
		files.add(shareFile);
		multipleSend(activity, files);
	}

	/**
	 * 鍙互鍒犻櫎鎸囧畾璺緞鐨勬枃浠跺拰鐩綍
	 * 
	 * @param filePath
	 */
	public static void deleteFile(String filePath) {
		try {
			File file = new File(filePath);
			if (file.exists()) {
				if (file.isFile()) {
					FileUtils.forceDelete(file);
				} else if (file.isDirectory()) {
					FileUtils.deleteDirectory(file);
				} else {
					Log.e(TAG, "Delete file is error : " + filePath);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 淇濆瓨涓婚淇℃伅
	 * 
	 * @param context
	 * @param themePkg
	 * @param position
	 *            浣嶄簬鍝釜浣嶇疆锛岄粯璁や富棰樹负0锛屽叾瀹冪殑鏈�繎鍔犲叆鐨勪富棰樹负1
	 */
	public static void addThemeInfo(Context context, String themePkg,
			int position,boolean addPkg) {

		/**
		 * 璇ヤ富棰樼殑妯″潡淇℃伅浠ュ強妯″潡鏁伴噺
		 */
		ThemeModelInfo themeModelInfo = new ThemeModelInfo(themePkg);

		ThemeBase themeBase = new ThemeBase(themeModelInfo, themePkg, null,
				true);

		if (themeModelInfo.getPim() != null) {
			String path = new StringBuilder()
					.append(ThemeConstants.THEME_WALLPAPER).append("/")
					.append(ThemeConstants.THEME_THUMBNAIL_PIM_PREFIX)
					.append(themeBase.getName()).toString();

			File pim = new File(path);
			if (pim.exists()) {
				themeBase
						.setSize(ThemeUtil.fileLengthToSize(pim.length()));
			}

			ArrayList<ThemeBase> pims = ThemeHelper.getThemeBases(
					context, ThemeConstants.THEME_LOCAL,
					ThemeConstants.PIM);

			pims.add(pims.size()==0?0:position, themeBase);

			ThemeHelper.saveThemeBases(context, pims,
					ThemeConstants.THEME_LOCAL, ThemeConstants.PIM);

			pims.clear();
		}

		if (themeModelInfo.getSettings() != null) {
			String path = new StringBuilder()
					.append(ThemeConstants.THEME_SETTING).append("/")
					.append(ThemeConstants.THEME_THUMBNAIL_SETTING_PREFIX)
					.append(themeBase.getName()).toString();

			File setting = new File(path);
			if (setting.exists()) {
				themeBase
						.setSize(ThemeUtil.fileLengthToSize(setting.length()));
			}

			ArrayList<ThemeBase> settings = ThemeHelper.getThemeBases(
					context, ThemeConstants.THEME_LOCAL,
					ThemeConstants.SETTING);

			settings.add(settings.size()==0?0:position, themeBase);

			ThemeHelper.saveThemeBases(context, settings,
					ThemeConstants.THEME_LOCAL, ThemeConstants.SETTING);

			settings.clear();
		}
		if (themeModelInfo.getSystemui() != null) {
			String path = new StringBuilder()
					.append(ThemeConstants.THEME_SYSTEMUI).append("/")
					.append(ThemeConstants.THEME_THUMBNAIL_SYSTEMUI_PREFIX)
					.append(themeBase.getName()).toString();

			File systemui = new File(path);
			if (systemui.exists()) {
				themeBase
						.setSize(ThemeUtil.fileLengthToSize(systemui.length()));
			}

			ArrayList<ThemeBase> systemuis = ThemeHelper.getThemeBases(
					context, ThemeConstants.THEME_LOCAL,
					ThemeConstants.NOTIFY);

			systemuis.add(systemuis.size()==0?0:position, themeBase);

			ThemeHelper.saveThemeBases(context, systemuis,
					ThemeConstants.THEME_LOCAL, ThemeConstants.NOTIFY);

			systemuis.clear();
		}

		if (themeModelInfo.getPhone()!= null) {
			String path = new StringBuilder()
					.append(ThemeConstants.THEME_PHONE).append("/")
					.append(ThemeConstants.THEME_THUMBNAIL_PHONE_PREFIX)
					.append(themeBase.getName()).toString();

			File phone = new File(path);
			if (phone.exists()) {
				themeBase
						.setSize(ThemeUtil.fileLengthToSize(phone.length()));
			}

			ArrayList<ThemeBase> phones = ThemeHelper.getThemeBases(
					context, ThemeConstants.THEME_LOCAL,
					ThemeConstants.PHONE);

			phones.add(phones.size()==0?0:position, themeBase);

			ThemeHelper.saveThemeBases(context, phones,
					ThemeConstants.THEME_LOCAL, ThemeConstants.PHONE);

			phones.clear();
		}
		if (themeModelInfo.getWallpaper() != null) {
			String path = new StringBuilder()
					.append(ThemeConstants.THEME_WALLPAPER).append("/")
					.append(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX)
					.append(themeBase.getName()).toString();

			File wallpaper = new File(path);
			if (wallpaper.exists()) {
				themeBase
						.setSize(ThemeUtil.fileLengthToSize(wallpaper.length()));
			}

			ArrayList<ThemeBase> wallPapers = ThemeHelper.getThemeBases(
					context, ThemeConstants.THEME_LOCAL,
					ThemeConstants.WALLPAPER);

			wallPapers.add(wallPapers.size()==0?0:position, themeBase);

			ThemeHelper.saveThemeBases(context, wallPapers,
					ThemeConstants.THEME_LOCAL, ThemeConstants.WALLPAPER);

			wallPapers.clear();
		}

		/**
		 * 閿佸睆澹佺焊
		 */
		if (themeModelInfo.getLockscreenWallpaper() != null) {
			String path = new StringBuilder()
					.append(ThemeConstants.THEME_WALLPAPER)
					.append("/")
					.append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_WALLPAPER_PREFIX)
					.append(themeBase.getName()).toString();

			File lockscreenwallpaper = new File(path);
			if (lockscreenwallpaper.exists()) {
				themeBase.setSize(ThemeUtil
						.fileLengthToSize(lockscreenwallpaper.length()));
			}

			ArrayList<ThemeBase> lsWallPapers = ThemeHelper.getThemeBases(
					context, ThemeConstants.THEME_LOCAL,
					ThemeConstants.LSWALLPAPER);

			lsWallPapers.add(lsWallPapers.size()==0?0:position, themeBase);

			ThemeHelper.saveThemeBases(context, lsWallPapers,
					ThemeConstants.THEME_LOCAL, ThemeConstants.LSWALLPAPER);

			lsWallPapers.clear();
		}

		/**
		 * 閿佸睆
		 */
		if (themeModelInfo.getLockscreen() != null) {
			String path = new StringBuilder()
					.append(ThemeConstants.THEME_MODEL_LOCKSCREEN_STYLE)
					.append("/").append(ThemeConstants.THEME_MODEL_LOCKSCREEN)
					.append("_").append(themeBase.getName()).toString();

			File lockscreen = new File(path);
			if (lockscreen.exists()) {
				themeBase.setSize(ThemeUtil.fileLengthToSize(lockscreen
						.length()));
			}

			/**
			 * 濡傛灉鍖呭惈閿佸睆澹佺焊锛岄偅杈瑰氨鍖呭惈涓や釜妯″潡锛屽惁鍒欏彧鏈夐攣灞忎竴涓ā鍧�
			 */
			if (themeModelInfo.getLockscreenWallpaper() != null) {
				themeBase.setModelNum("2");
			} else {
				themeBase.setModelNum("1");
			}

			ArrayList<ThemeBase> lockScreenStyles = ThemeHelper.getThemeBases(
					context, ThemeConstants.THEME_LOCAL,
					ThemeConstants.LOCKSCREEN);

			lockScreenStyles.add(lockScreenStyles.size()==0?0:position, themeBase);

			ThemeHelper.saveThemeBases(context, lockScreenStyles,
					ThemeConstants.THEME_LOCAL, ThemeConstants.LOCKSCREEN);

			lockScreenStyles.clear();
		}

		/**
		 * 妗岄潰鏍峰紡
		 */
		if (themeModelInfo.getLauncher() != null) {
			String path = new StringBuilder()
					.append(ThemeConstants.THEME_MODEL_LAUNCHER_STYLE)
					.append("/").append(ThemeConstants.THEME_MODEL_LAUNCHER)
					.append("_").append(themeBase.getName()).toString();

			File launcher = new File(path);
			if (launcher.exists()) {
				themeBase
						.setSize(ThemeUtil.fileLengthToSize(launcher.length()));
			}

			ArrayList<ThemeBase> launcherStyles = ThemeHelper.getThemeBases(
					context, ThemeConstants.THEME_LOCAL,
					ThemeConstants.LAUNCHER);

			launcherStyles.add(launcherStyles.size()==0?0:position, themeBase);

			ThemeHelper.saveThemeBases(context, launcherStyles,
					ThemeConstants.THEME_LOCAL, ThemeConstants.LAUNCHER);

			launcherStyles.clear();
		}

		/**
		 * 涓婚鍖呭彧鏈夊寘鍚簡icons鎵嶇畻涓�釜瀹屾暣鐨勪富棰樺寘
		 * 
		 */
		if (themeModelInfo.getIcons() != null) {

			String path = new StringBuilder()
					.append(ThemeConstants.THEME_MODEL_ICONS_STYLE).append("/")
					.append(ThemeConstants.THEME_MODEL_ICONS).append("_")
					.append(themeBase.getName()).toString();

			File iconStyle = new File(path);
			if (iconStyle.exists()) {
				themeBase
						.setSize(ThemeUtil.fileLengthToSize(iconStyle.length()));
			}

			ArrayList<ThemeBase> iconsStyles = ThemeHelper.getThemeBases(
					context, ThemeConstants.THEME_LOCAL, ThemeConstants.ICONS);

			iconsStyles.add(iconsStyles.size()==0?0:position, themeBase);

			ThemeHelper.saveThemeBases(context, iconsStyles,
					ThemeConstants.THEME_LOCAL, ThemeConstants.ICONS);

			iconsStyles.clear();

		}
		if(addPkg){
		String pkgPath = new StringBuilder().append(ThemeConstants.THEME_LWT)
				.append("/").append(themeBase.getPkg()).toString();
		File themePkgFile = new File(pkgPath);
		if (themePkgFile.exists()) {
			themeBase
					.setSize(ThemeUtil.fileLengthToSize(themePkgFile.length()));
			ArrayList<ThemeBase> themePkgs = ThemeHelper.getThemeBases(context,
					ThemeConstants.THEME_LOCAL, ThemeConstants.THEMEPKG);

			themePkgs.add(themePkgs.size()==0?0:position, themeBase);

			ThemeHelper.saveThemeBases(context, themePkgs,
					ThemeConstants.THEME_LOCAL, ThemeConstants.THEMEPKG);

			themePkgs.clear();
		}
		}
		/**
		 * 寮�満鍔ㄧ敾
		 */
		if (themeModelInfo.getBoots() != null) {
			String path = new StringBuilder()
					.append(ThemeConstants.THEME_MODEL_BOOTS_STYLE).append("/")
					.append(ThemeConstants.THEME_MODEL_BOOTS).append("_")
					.append(themeBase.getName()).toString();

			File boots = new File(path);
			if (boots.exists()) {
				themeBase.setSize(ThemeUtil.fileLengthToSize(boots.length()));
			}

			ArrayList<ThemeBase> bootsStyles = ThemeHelper.getThemeBases(
					context, ThemeConstants.THEME_LOCAL, ThemeConstants.BOOTS);

			bootsStyles.add(bootsStyles.size()>0||(
					0 < position && position < bootsStyles.size()) ? position
							: 0, themeBase);

			ThemeHelper.saveThemeBases(context, bootsStyles,
					ThemeConstants.THEME_LOCAL, ThemeConstants.BOOTS);

			bootsStyles.clear();
		}

		/**
		 * 瀛椾綋
		 */
		if (themeModelInfo.getFonts() != null) {
			String path = new StringBuilder()
					.append(ThemeConstants.THEME_MODEL_FONTS_STYLE).append("/")
					.append(ThemeConstants.THEME_MODEL_FONTS).append("_")
					.append(themeBase.getName()).toString();

			File fonts = new File(path);
			if (fonts.exists()) {
				themeBase.setSize(ThemeUtil.fileLengthToSize(fonts.length()));
			}

			ArrayList<ThemeBase> fontsStyles = ThemeHelper.getThemeBases(
					context, ThemeConstants.THEME_LOCAL, ThemeConstants.FONTS);

			fontsStyles.add(
					fontsStyles.size()>0||(0 <= position && position < fontsStyles.size())? position
							: 0, themeBase);

			ThemeHelper.saveThemeBases(context, fontsStyles,
					ThemeConstants.THEME_LOCAL, ThemeConstants.FONTS);

			fontsStyles.clear();
		}

	}

	/**
	 * 鍒犻櫎涓婚浠ュ強涓庝箣鏈夊叧鐨勬墍鏈夎祫婧愬拰淇℃伅
	 * 
	 * @param activity
	 * @param themeBase
	 */
	public static void deleteThemeInfo(Context activity, ThemeBase themeBase) {

		/**
		 * 濡傛灉涓婚瀵硅薄涓殑lwtPath涓嶄负绌猴紝鍒欒〃绀烘涓婚鍖呮枃浠朵笉鍦�theme/lwt鍐�
		 */
		if (themeBase.getLwtPath() != null) {
			/**
			 * 鍒犻櫎.lwt婧愭枃浠�
			 */
			ThemeUtil.deleteFile(themeBase.getLwtPath());
			/**
			 * 鍒犻櫎姝wt鏂囦欢瀵瑰簲鐨勯瑙堝浘鍜屼俊鎭�
			 */
			ThemeUtil.deleteFile(new StringBuilder()
					.append(ThemeConstants.THEME_LOCAL_PREVIEW).append("/")
					.append(themeBase.getName()).toString());
			ThemeUtil.deleteFile(new StringBuilder()
			.append(ThemeConstants.THEME_MODEL_FRAMEWORK_STYLE)
			.append("/").append(ThemeConstants.THEME_MODEL_FRAMEWORK)
			.append("_").append(themeBase.getName()).toString());
			Intent intent = new Intent(ThemeActions.ACTION_DELETE_LWT);
			intent.putExtra(ThemeConstants.FROM_FILE_MANAGER_KEY,
					themeBase.getLwtPath());
			activity.sendBroadcast(intent);
			if (activity instanceof Activity) {
				((Activity) activity).finish();
			}
			return;
		}

		ArrayList<ThemeBase> pkgs = ThemeHelper.getThemeBases(activity,
				ThemeConstants.THEME_LOCAL, ThemeConstants.THEMEPKG);

		ArrayList<ThemeBase> wallPapers = ThemeHelper.getThemeBases(activity,
				ThemeConstants.THEME_LOCAL, ThemeConstants.WALLPAPER);

		ArrayList<ThemeBase> lockScreenWallPapers = ThemeHelper.getThemeBases(
				activity, ThemeConstants.THEME_LOCAL,
				ThemeConstants.LSWALLPAPER);

		ArrayList<ThemeBase> lockScreenStyles = ThemeHelper
				.getThemeBases(activity, ThemeConstants.THEME_LOCAL,
						ThemeConstants.LOCKSCREEN);

		ArrayList<ThemeBase> iconsStyles = ThemeHelper.getThemeBases(activity,
				ThemeConstants.THEME_LOCAL, ThemeConstants.ICONS);

		ArrayList<ThemeBase> launcherStyles = ThemeHelper.getThemeBases(
				activity, ThemeConstants.THEME_LOCAL, ThemeConstants.LAUNCHER);

		ArrayList<ThemeBase> bootsStyles = ThemeHelper.getThemeBases(activity,
				ThemeConstants.THEME_LOCAL, ThemeConstants.BOOTS);

		ArrayList<ThemeBase> fontsStyles = ThemeHelper.getThemeBases(activity,
				ThemeConstants.THEME_LOCAL, ThemeConstants.FONTS);

		
		ArrayList<ThemeBase> systemuis = ThemeHelper.getThemeBases(activity,
				ThemeConstants.THEME_LOCAL, ThemeConstants.NOTIFY);

		ArrayList<ThemeBase> phones = ThemeHelper.getThemeBases(
				activity, ThemeConstants.THEME_LOCAL, ThemeConstants.PHONE);

		ArrayList<ThemeBase> pims = ThemeHelper.getThemeBases(activity,
				ThemeConstants.THEME_LOCAL, ThemeConstants.PIM);

		ArrayList<ThemeBase> settings = ThemeHelper.getThemeBases(activity,
				ThemeConstants.THEME_LOCAL, ThemeConstants.SETTING);

		
		String themePkg = themeBase.getPkg();
		String themeName = themeBase.getName();

		for (ThemeBase pkg : pkgs) {
			if (pkg.getPkg().equals(themePkg)) {

				ThemeUtil.deleteFile(new StringBuilder()
						.append(ThemeConstants.THEME_LWT).append("/")
						.append(themePkg).toString());

				ThemeUtil.deleteFile(new StringBuilder()
						.append(ThemeConstants.THEME_LOCAL_PREVIEW).append("/")
						.append(themeName).toString());

				/**
				 * 鍥犱负浠ockscreen_pkgname浣滀负棣栭〉缂╃暐鍥撅紝浣嗗彲鑳芥涓婚涓嶅寘鍚攣灞忥紝鎵�互鍦ㄦ鍒犻櫎
				 */
				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_LOCAL_THUMBNAIL)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_PREFIX)
								.append(themeName).toString());

				pkgs.remove(pkg);

				ThemeHelper.saveThemeBases(activity, pkgs,
						ThemeConstants.THEME_LOCAL, ThemeConstants.THEMEPKG);

				pkgs.clear();
				break;
			}

		}

		for (ThemeBase phone : phones) {
			if (phone.getPkg().equals(themePkg)) {

				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_PHONE)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_PHONE_PREFIX)
								.append(themeName).toString());

				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_LOCAL_PHONE_THUMBNAIL)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_PHONE_PREFIX)
								.append(themeName).toString());
				ThemeUtil.deleteFile(new StringBuilder()
				.append(ThemeConstants.THEME_MODEL_PHONE_STYLE)
				.append("/")
				.append(ThemeConstants.THEME_MODEL_PHONE)
				.append("_").append(themeName).toString());
				phones.remove(phone);

				ThemeHelper.saveThemeBases(activity, phones,
						ThemeConstants.THEME_LOCAL, ThemeConstants.PHONE);

				phones.clear();
				break;
			}

		}
		for (ThemeBase pim : pims) {
			if (pim.getPkg().equals(themePkg)) {

				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_PIM)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_PIM_PREFIX)
								.append(themeName).toString());

				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_LOCAL_PIM_THUMBNAIL)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_PIM_PREFIX)
								.append(themeName).toString());
				ThemeUtil.deleteFile(new StringBuilder()
				.append(ThemeConstants.THEME_MODEL_PIM_STYLE)
				.append("/")
				.append(ThemeConstants.THEME_MODEL_PIM)
				.append("_").append(themeName).toString());
				pims.remove(pim);

				ThemeHelper.saveThemeBases(activity, pims,
						ThemeConstants.THEME_LOCAL, ThemeConstants.PIM);

				pims.clear();
				break;
			}

		}		for (ThemeBase systemui : systemuis) {
			if (systemui.getPkg().equals(themePkg)) {

				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_SYSTEMUI)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_SYSTEMUI_PREFIX)
								.append(themeName).toString());

				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_LOCAL_SYSTEMUI_THUMBNAIL)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_SYSTEMUI_PREFIX)
								.append(themeName).toString());
				ThemeUtil.deleteFile(new StringBuilder()
				.append(ThemeConstants.THEME_MODEL_NOTIFY_STYLE)
				.append("/")
				.append(ThemeConstants.THEME_MODEL_NOTIFY)
				.append("_").append(themeName).toString());systemuis.remove(systemui);

				ThemeHelper.saveThemeBases(activity, systemuis,
						ThemeConstants.THEME_LOCAL, ThemeConstants.NOTIFY);

				systemuis.clear();
				break;
			}

		}		for (ThemeBase setting : settings) {
			if (setting.getPkg().equals(themePkg)) {

				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_SETTING)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_SETTING_PREFIX)
								.append(themeName).toString());

				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_LOCAL_SETTING_THUMBNAIL)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_SETTING_PREFIX)
								.append(themeName).toString());
				ThemeUtil.deleteFile(new StringBuilder()
				.append(ThemeConstants.THEME_MODEL_SETTING_STYLE)
				.append("/")
				.append(ThemeConstants.THEME_MODEL_SETTINGS)
				.append("_").append(themeName).toString());
				settings.remove(setting);

				ThemeHelper.saveThemeBases(activity, settings,
						ThemeConstants.THEME_LOCAL, ThemeConstants.SETTING);

				settings.clear();
				break;
			}

		}
		for (ThemeBase wallpaper : wallPapers) {
			if (wallpaper.getPkg().equals(themePkg)) {

				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_WALLPAPER)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX)
								.append(themeName).toString());

				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_LOCAL_WALLPAPER_THUMBNAIL)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_WALLPAPER_PREFIX)
								.append(themeName).toString());

				wallPapers.remove(wallpaper);

				ThemeHelper.saveThemeBases(activity, wallPapers,
						ThemeConstants.THEME_LOCAL, ThemeConstants.WALLPAPER);

				wallPapers.clear();
				break;
			}

		}

		for (ThemeBase lockScreenWallPaper : lockScreenWallPapers) {
			if (lockScreenWallPaper.getPkg().equals(themePkg)) {

				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_WALLPAPER)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_WALLPAPER_PREFIX)
								.append(themeName).toString());

				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_LOCAL_WALLPAPER_THUMBNAIL)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_WALLPAPER_PREFIX)
								.append(themeName).toString());

				lockScreenWallPapers.remove(lockScreenWallPaper);

				ThemeHelper.saveThemeBases(activity, lockScreenWallPapers,
						ThemeConstants.THEME_LOCAL, ThemeConstants.LSWALLPAPER);

				lockScreenWallPapers.clear();
				break;
			}

		}

		for (ThemeBase lockScreenStyle : lockScreenStyles) {
			if (lockScreenStyle.getPkg().equals(themePkg)) {

				/**
				 * 濡傛灉涓婚鍖呬腑鍙寘鍚攣灞忥紝閭ｄ箞鍦ㄥ垹闄ょ殑鏃跺�锛屽厛鍒犻櫎.lwt鏂囦欢锛屽苟涓旂浉鍏抽瑙堝浘
				 */
				String lockscreenPath = new StringBuilder()
						.append(ThemeConstants.THEME_LWT).append("/")
						.append(themePkg).toString();
				File lockscreen = new File(lockscreenPath);
				if (lockscreen.exists()) {
					ThemeUtil.deleteFile(lockscreenPath);

					ThemeUtil.deleteFile(new StringBuilder()
							.append(ThemeConstants.THEME_LOCAL_PREVIEW)
							.append("/").append(themeName).toString());
				}

				/**
				 * 鍥犱负浠ockscreen_pkgname浣滀负棣栭〉缂╃暐鍥撅紝浣嗗彲鑳芥涓婚涓嶅寘鍚攣灞忥紝鎵�互鍦ㄦ鍒犻櫎
				 */
				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_LOCAL_THUMBNAIL)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_PREFIX)
								.append(themeName).toString());

				ThemeUtil.deleteFile(new StringBuilder()
						.append(ThemeConstants.THEME_MODEL_LOCKSCREEN_STYLE)
						.append("/")
						.append(ThemeConstants.THEME_MODEL_LOCKSCREEN)
						.append("_").append(themeName).toString());

				ThemeUtil
						.deleteFile(new StringBuilder()
								.append(ThemeConstants.THEME_LOCAL_THUMBNAIL)
								.append("/")
								.append(ThemeConstants.THEME_THUMBNAIL_LOCKSCREEN_PREFIX)
								.append(themeName).toString());

				lockScreenStyles.remove(lockScreenStyle);

				ThemeHelper.saveThemeBases(activity, lockScreenStyles,
						ThemeConstants.THEME_LOCAL, ThemeConstants.LOCKSCREEN);

				lockScreenStyles.clear();
				break;
			}

		}

		for (ThemeBase iconsStyle : iconsStyles) {
			if (iconsStyle.getPkg().equals(themePkg)) {

				ThemeUtil.deleteFile(new StringBuilder()
						.append(ThemeConstants.THEME_MODEL_ICONS_STYLE)
						.append("/").append(ThemeConstants.THEME_MODEL_ICONS)
						.append("_").append(themeName).toString());

				ThemeUtil.deleteFile(new StringBuilder()
						.append(ThemeConstants.THEME_LOCAL_THUMBNAIL)
						.append("/")
						.append(ThemeConstants.THEME_THUMBNAIL_ICONS_PREFIX)
						.append(themeName).toString());

				iconsStyles.remove(iconsStyle);

				ThemeHelper.saveThemeBases(activity, iconsStyles,
						ThemeConstants.THEME_LOCAL, ThemeConstants.ICONS);

				iconsStyles.clear();
				break;
			}

		}

		for (ThemeBase launcherStyle : launcherStyles) {
			if (launcherStyle.getPkg().equals(themePkg)) {

				ThemeUtil.deleteFile(new StringBuilder()
						.append(ThemeConstants.THEME_MODEL_LAUNCHER_STYLE)
						.append("/")
						.append(ThemeConstants.THEME_MODEL_LAUNCHER)
						.append("_").append(themeName).toString());

				ThemeUtil.deleteFile(new StringBuilder()
						.append(ThemeConstants.THEME_LOCAL_THUMBNAIL)
						.append("/")
						.append(ThemeConstants.THEME_THUMBNAIL_LAUNCHER_PREFIX)
						.append(themeName).toString());

				launcherStyles.remove(launcherStyle);

				ThemeHelper.saveThemeBases(activity, launcherStyles,
						ThemeConstants.THEME_LOCAL, ThemeConstants.LAUNCHER);

				launcherStyles.clear();
				break;
			}

		}

		for (ThemeBase bootsStyle : bootsStyles) {
			if (bootsStyle.getPkg().equals(themePkg)) {

				/**
				 * 濡傛灉涓婚鍖呬腑鍙寘鍚紑鏈哄姩鐢伙紝閭ｄ箞鍦ㄥ垹闄ょ殑鏃跺�锛屽厛鍒犻櫎.lwt鏂囦欢锛屽苟涓旂浉鍏抽瑙堝浘
				 */
				String bootsPath = new StringBuilder()
						.append(ThemeConstants.THEME_LWT).append("/")
						.append(themePkg).toString();
				File boots = new File(bootsPath);
				if (boots.exists()) {
					ThemeUtil.deleteFile(bootsPath);

					ThemeUtil.deleteFile(new StringBuilder()
							.append(ThemeConstants.THEME_LOCAL_PREVIEW)
							.append("/").append(themeName).toString());
				}

				ThemeUtil.deleteFile(new StringBuilder()
						.append(ThemeConstants.THEME_MODEL_BOOTS_STYLE)
						.append("/").append(ThemeConstants.THEME_MODEL_BOOTS)
						.append("_").append(themeName).toString());

				ThemeUtil.deleteFile(new StringBuilder()
						.append(ThemeConstants.THEME_LOCAL_THUMBNAIL)
						.append("/")
						.append(ThemeConstants.THEME_THUMBNAIL_BOOTS_PREFIX)
						.append(themeName).toString());

				bootsStyles.remove(bootsStyle);

				ThemeHelper.saveThemeBases(activity, bootsStyles,
						ThemeConstants.THEME_LOCAL, ThemeConstants.BOOTS);

				bootsStyles.clear();
				break;
			}

		}

		for (ThemeBase fontsStyle : fontsStyles) {
			if (fontsStyle.getPkg().equals(themePkg)) {

				/**
				 * 濡傛灉涓婚鍖呬腑鍙寘鍚瓧浣擄紝閭ｄ箞鍦ㄥ垹闄ょ殑鏃跺�锛屽厛鍒犻櫎.lwt鏂囦欢锛屽苟涓旂浉鍏抽瑙堝浘
				 */
				String fontsPath = new StringBuilder()
						.append(ThemeConstants.THEME_LWT).append("/")
						.append(themePkg).toString();
				File fonts = new File(fontsPath);
				if (fonts.exists()) {
					ThemeUtil.deleteFile(fontsPath);

					ThemeUtil.deleteFile(new StringBuilder()
							.append(ThemeConstants.THEME_LOCAL_PREVIEW)
							.append("/").append(themeName).toString());
				}

				ThemeUtil.deleteFile(new StringBuilder()
						.append(ThemeConstants.THEME_MODEL_FONTS_STYLE)
						.append("/").append(ThemeConstants.THEME_MODEL_FONTS)
						.append("_").append(themeName).toString());

				ThemeUtil.deleteFile(new StringBuilder()
						.append(ThemeConstants.THEME_LOCAL_THUMBNAIL)
						.append("/")
						.append(ThemeConstants.THEME_THUMBNAIL_FONTS_PREFIX)
						.append(themeName).toString());

				fontsStyles.remove(fontsStyle);

				ThemeHelper.saveThemeBases(activity, fontsStyles,
						ThemeConstants.THEME_LOCAL, ThemeConstants.FONTS);

				fontsStyles.clear();
				break;
			}

		}

		SharedPreferences sharedPreferences = activity.getSharedPreferences(
				"DOWNLOADED", Context.MODE_PRIVATE);

		sharedPreferences.edit().remove(themePkg).commit();

		Intent deleteTheme = new Intent();
		deleteTheme.setAction(ThemeActions.DELETE_THEME_OVER);
		activity.sendBroadcast(deleteTheme);
		if (activity instanceof Activity) {
			((Activity) activity).finish();
		}
	}

	/**
	 * 榛樿涓婚璧勬簮涓嶈兘鍒犻櫎鐨勬彁绀烘
	 * 
	 * @param context
	 */
	public static void defaultThemeDialog(Context context) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.delete_default_theme_title)
				.setMessage(R.string.delete_default_theme_msg)
				.setPositiveButton(R.string.theme_ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// do nothing
							}
						}).create().show();
	}

	/**
	 * 鏄惁瑕佸垹闄や富棰樿祫婧愮殑鎻愮ず妗�
	 * 
	 * @param context
	 */
	public static void deleteThemeDialog(final Activity activity,
			final ThemeBase themeBase) {

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.theme_delete_confirm_title);
		builder.setMessage(R.string.theme_delete_confirm_msg);
		builder.setNegativeButton(R.string.theme_cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// do nothing
					}
				});
		builder.setPositiveButton(R.string.theme_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						new ThemeDeleteTask(themeBase, activity).execute("");

					}
				});
		builder.create().show();
	}

	public static void applyThemeAndExit(Context context) {

		((Activity) context).finish();

		Intent intent = new Intent();
		intent.setAction(ThemeActions.THEME_CHANGED);
		context.sendBroadcast(intent);
		SharedPreferenceUtil.putValue(context,
				ThemeApplication.modulesOnApplied,
				ThemeApplication.modulesOnAppliedKey,
				ModulesOnAppliedUtil.toString(ThemeUtil.modulesOnApplied));
		for (Activity activity : ThemeApplication.activities) {
			Log.i(TAG, "activity == " + activity);
			activity.finish();
		}
		Intent home = new Intent(Intent.ACTION_MAIN);
		home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		home.addCategory(Intent.CATEGORY_HOME);
		context.startActivity(home);
	}

	public static void lockscreenChanged(Context context) {
		 Settings.System.putInt(context.getContentResolver(),
		 Settings.System.LOCKSCREEN_CHANGED,
		 ThemeConstants.LOCKSCREEN_CHANGED);
	}

	/**
	 * 鍚堝苟鏁扮粍
	 * 
	 * @param srcFirst
	 * @param srcSecond
	 * @return
	 */
	public static File[] contact(File[] srcFirst, File[] srcSecond) {

		int srcFirstLength = 0;
		int srcSecondLength = 0;
		int totalLength = 0;

		if (srcFirst != null) {
			srcFirstLength = srcFirst.length;
		}
		if (srcSecond != null) {
			srcSecondLength = srcSecond.length;
		}

		totalLength = srcFirstLength + srcSecondLength;

		if (totalLength == 0) {
			return null;
		}

		File[] target = new File[totalLength];

		if (srcFirst != null) {
			System.arraycopy(srcFirst, 0, target, 0, srcFirstLength);
		}

		if (srcSecond != null) {
			System.arraycopy(srcSecond, 0, target, srcFirstLength,
					srcSecondLength);
		}

		return target;
	}

	public static void createDirs() {

		File theme = new File(ThemeConstants.LEWA_THEME_PATH);
		if (!theme.exists()) {
			boolean mkdirSuccess = theme.mkdirs();
			if (mkdirSuccess) {
				ThemeUtil.createNomedia(ThemeConstants.LEWA_THEME_PATH);
			}
		} else {
			ThemeUtil.createNomedia(ThemeConstants.LEWA_THEME_PATH);
		}

		File lwt = new File(ThemeConstants.THEME_LWT);
		if (!lwt.exists()) {
			lwt.mkdirs();
		}

		File preview = new File(ThemeConstants.THEME_PREVIEW);
		if (!preview.exists()) {
			preview.mkdirs();
		}

		File localPreview = new File(ThemeConstants.THEME_LOCAL_PREVIEW);
		if (!localPreview.exists()) {
			localPreview.mkdirs();
		}

		File wallpaperDir = new File(ThemeConstants.THEME_WALLPAPER);
		if (!wallpaperDir.exists()) {
			wallpaperDir.mkdirs();
		}

		File localThumbnail = new File(ThemeConstants.THEME_LOCAL_THUMBNAIL);
		if (!localThumbnail.exists()) {
			localThumbnail.mkdirs();
		}

		File onlineThumbnail = new File(ThemeConstants.THEME_ONLINE_THUMBNAIL);
		if (!onlineThumbnail.exists()) {
			onlineThumbnail.mkdirs();
		}

		File modelLockScreen = new File(
				ThemeConstants.THEME_MODEL_LOCKSCREEN_STYLE);
		if (!modelLockScreen.exists()) {
			modelLockScreen.mkdirs();
		}

		File modelIcons = new File(ThemeConstants.THEME_MODEL_ICONS_STYLE);
		if (!modelIcons.exists()) {
			modelIcons.mkdirs();
		}

		File modelLauncher = new File(ThemeConstants.THEME_MODEL_LAUNCHER_STYLE);
		if (!modelLauncher.exists()) {
			modelLauncher.mkdirs();
		}

		File modelBoots = new File(ThemeConstants.THEME_MODEL_BOOTS_STYLE);
		if (!modelBoots.exists()) {
			modelBoots.mkdirs();
		}

		File modelFonts = new File(ThemeConstants.THEME_MODEL_FONTS_STYLE);
		if (!modelFonts.exists()) {
			modelFonts.mkdirs();
		}

		File onlineWallpaperPreview = new File(
				ThemeConstants.THEME_ONLINE_WALLPAPRE);
		if (!onlineWallpaperPreview.exists()) {
			onlineWallpaperPreview.mkdirs();
		}

		File onlineWallpaperThumbnail = new File(
				ThemeConstants.THEME_ONLINE_WALLPAPER_THUMBNAIL);
		if (!onlineWallpaperThumbnail.exists()) {
			onlineWallpaperThumbnail.mkdirs();
		}
	}

	/**
	 * 鏍规嵁閫夊畾鐨勫浘鐗囪繘琛岃鍓�
	 * 
	 * @param activity
	 * @param sourceImagePath
	 *            閫夊畾鍥剧墖鐨勮矾寰�
	 * @param aspectX
	 *            瀹芥瘮渚�
	 * @param aspectY
	 *            楂樻瘮渚�
	 * @param outputX
	 *            瀹藉儚绱�
	 * @param outputY
	 *            楂樺儚绱�
	 * @param requestCode
	 */
	public static void cropImage(Activity activity, String sourceImagePath,
			int aspectX, int aspectY, int outputX, int outputY, int requestCode) {

		File file = new File(sourceImagePath);

		Uri uri = Uri.fromFile(file);

		Intent intent = new Intent("com.android.camera.action.CROP");

		intent.setDataAndType(uri, "image/*");

		intent.putExtra("scale", true);
		intent.putExtra("aspectX", aspectX);
		intent.putExtra("aspectY", aspectY);

		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);

		intent.putExtra("noFaceDetection", true);

		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * 浠庡叾浠栫▼搴忎腑閫夋嫨鍥剧墖瑁佸壀锛屽苟浣滀负閿佸睆澹佺焊鎴栬�妗岄潰
	 * 
	 * @param activity
	 * @param arb
	 * @param aspectX
	 *            瀹芥瘮渚�
	 * @param aspectY
	 *            楂樻瘮渚�
	 * @param outputX
	 *            瀹藉儚绱�
	 * @param outputY
	 *            楂樺儚绱�
	 * @param requestCode
	 */
	public static void cropImageFromGallery(Activity activity,
			ActivityResultBridge arb, int aspectX, int aspectY, int outputX,
			int outputY, int requestCode) {
		Intent intent = new Intent();
		/**
		 * 鐩存帴鎸囧畾蹇浘搴旂敤
		 */
		intent.setClassName("com.alensw.PicFolder",
				"com.alensw.PicFolder.GalleryActivity");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.setType("image/jpeg");
		intent.setType("image/png");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", aspectX);
		intent.putExtra("aspectY", aspectY);

		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);

		intent.putExtra("noFaceDetection", true);

		arb.startActivityForResult((ActivityResultReceiver) activity, intent,
				requestCode);
	}

	/**
	 * 
	 * 
	 * @param activity
	 * @param themeName
	 *            涓婚鍚嶇О
	 * @param modelNames
	 *            鍙樻崲涓婚鐨勬ā鍧�
	 */
	public static void applyTheme(String themeName,
			Collection<String> modelNames) {
		try {
			IActivityManager am = ActivityManagerNative.getDefault();
			Configuration config = am.getConfiguration();

			LewaTheme lewaTheme = new LewaTheme(false);

			lewaTheme.setThemeName(themeName);
			
			for (String modelName : modelNames) {
                            if(modelName.trim().equals("")){
                                continue;    
                            }
				if (modelName.equals(ThemeConstants.THEME_MODEL_FRAMEWORK)) {
					modulesOnApplied.add("android");
					lewaTheme.addModelName("android");
				} else {
					lewaTheme.addModelName(modelName);
					modulesOnApplied.add(modelName);
				}
			}
			 config.lewaTheme = lewaTheme;
			
			config.lewaTheme = lewaTheme;
			am.updateConfiguration(config);
			// Trigger the dirty bit for the Settings Provider.
			BackupManager.dataChanged("com.android.providers.settings");
		} catch (RemoteException e) {
			// Intentionally left blank
		}
		// activity.finish();
	}

	/**
	 * 鎶婁富棰樺寘鏂囦欢涓寘鍚殑棰勮鍥惧強涓婚淇℃伅鏂囦欢瑙ｅ帇骞朵繚瀛樺埌ThemeConstants.
	 * THEME_LOCAL_PREVIEW璺緞涓�
	 * 
	 * @param lwt
	 */
	public static void unZIPForThemeInfos(String lwtPath, String nameNoLwt) {
		ZipFile zip = null;
		try {
			zip = new ZipFile(lwtPath);

			String localPreviewPath = new StringBuilder()
					.append(ThemeConstants.THEME_LOCAL_PREVIEW).append("/")
					.append(nameNoLwt).toString();

			File localPreview = new File(localPreviewPath);
			if (!localPreview.exists()) {
				localPreview.mkdirs();
			}

			@SuppressWarnings("unchecked")
			Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip
					.entries();

			while (entries.hasMoreElements()) {

				ZipEntry zipEntry = (ZipEntry) entries.nextElement();
				String entryName = zipEntry.getName();

				InputStream source = zip.getInputStream(zipEntry);
				FileOutputStream target = null;
				try {

					if (entryName.indexOf("preview") != -1
							&& entryName.indexOf(ThemeConstants.BUFFIX_JPG) != -1) {

						File targetFile = new File(localPreviewPath,
								entryName.substring(entryName.indexOf("/")));

						target = new FileOutputStream(targetFile);

						writeSourceToTarget(source, target);
					} else if (entryName.indexOf("theme.json") != -1) {

						File targetFile = new File(localPreviewPath, entryName);

						target = new FileOutputStream(targetFile);

						writeSourceToTarget(source, target);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (target != null) {
							target.close();
							target = null;
						}
						if (source != null) {
							source.close();
							source = null;
						}
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (zip != null) {
					zip.close();
					zip = null;
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}

		}
	}

	/**
	 * 瑙ｅ帇缂╂枃浠躲�濡傛灉瑙ｅ帇鍚庣殑鏂囦欢浠嶇劧鏄帇缂╂枃浠讹紝閭ｄ箞鍐嶈繘琛岄�褰掕В鍘�
	 * 
	 * @param zipfile
	 * @param targetDir
	 * @return
	 */
	public static boolean unZip(File source, String targetDir) {

		String loadZipFile = source.getAbsolutePath();
		/**
		 * 鐢熸垚涓存椂鏂囦欢
		 */
		File temp_source = new File(new StringBuilder().append(loadZipFile)
				.append("_temp").toString());

		source.renameTo(temp_source);

		byte[] buffer = new byte[10240];

		int temp = -1;

		ZipFile zipFile = null;

		try {

			zipFile = new ZipFile(temp_source);

			@SuppressWarnings("unchecked")
			Enumeration<ZipEntry> enumeration = (Enumeration<ZipEntry>) zipFile
					.entries();

			ZipEntry zipEntry = null;

			while (enumeration.hasMoreElements()) {

				zipEntry = (ZipEntry) enumeration.nextElement();
				File targetFile = new File(new StringBuilder()
						.append(targetDir).append("/")
						.append(zipEntry.getName()).toString());
				if (zipEntry.isDirectory()) {
					targetFile.mkdirs();
				} else {

					if (!targetFile.getParentFile().exists()) {
						targetFile.getParentFile().mkdirs();
					}

					InputStream inputStream = null;
					OutputStream outputStream = null;
					try {
						inputStream = zipFile.getInputStream(zipEntry);
						outputStream = new FileOutputStream(targetFile);

						while ((temp = inputStream.read(buffer)) > 0) {
							outputStream.write(buffer, 0, temp);
						}

					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							if (outputStream != null) {
								outputStream.close();
								outputStream = null;
							}

							if (inputStream != null) {
								inputStream.close();
								inputStream = null;
							}
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}

				}

				ThemeUtil.changeFilePermission(targetFile);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (zipFile != null) {
					zipFile.close();
					zipFile = null;
				}
			} catch (Exception e2) {
				// TODO: handle exception
			}
			temp_source.delete();
		}
		return true;
	}

	/**
	 * 濡傛灉杩斿洖鍊间负504b03,鍒欒〃绀轰负zip绫诲瀷鏂囦欢
	 * 
	 * @param src
	 * @return
	 */
	private static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * 鏄惁涓簔ip鏂囦欢
	 * 
	 * @return
	 */
	public static boolean isZipFile(File file) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			byte[] b = new byte[3];
			fis.read(b, 0, b.length);

			if (bytesToHexString(b).equals("504b03")) {
				return true;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
					fis = null;
				}
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		return false;
	}

	/**
	 * 鍒涘缓璧勬簮鍦ㄦ墜鏈哄唴瀛樹腑鐨勭洰褰�
	 */
	public static void createFaceDir() {
		try {
			if (!new File("/data/system/face").exists()) {
				SuCommander sc = new SuCommander();
				sc.exec("mkdir /data/system/face");
				Thread.currentThread();
				/**
				 * 闃叉/data/system/face鐩綍杩樻病鏈夊缓鎴愶紝灏辨墽琛屼笅涓�潯鍛戒护
				 */
				Thread.sleep(100);
				/**
				 * 姣忎釜鐢ㄦ埛鎷ユ湁璇诲啓鏉冮檺drwx-rwx-r-x
				 */
				sc.exec("chmod 777 /data/system/face");
			} else {
				SuCommander sc = new SuCommander();
				sc.exec("chmod 777 /data/system/face");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 鍒濆鍖栧綋鍓嶈瑷�幆澧�
	 */
	public static void initLocale(Context context) {
		boolean isEN = context.getResources().getConfiguration().locale
				.getLanguage().equals("en");
		if (isEN) {
			ThemeUtil.isEN = true;
		} else {
			ThemeUtil.isEN = false;
		}
	}

	/**
	 * 鏂颁富棰樻槸鍚﹀寘鍚玣onts妯″潡,濡傛灉鍖呭惈鍒欏垹闄ace/fonts搴曚笅鍘熸潵鐨�ttf鏂囦欢,鍚﹀垯涓嶅仛浠讳綍鎿嶄綔
	 */
	public static void removeOldFonts(File file) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			ZipEntry fonts = zipFile.getEntry(ThemeConstants.THEME_MODEL_FONTS);
			if (fonts != null) {

				File oldFonts = new File(ThemeConstants.THEME_FACE_FONTS);
				if (oldFonts.exists()) {
					Log.e(TAG,
							"The new theme contain fonts, so delete the old theme's fonts");
					FileUtils.forceDelete(oldFonts);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (zipFile != null) {
					zipFile.close();
					zipFile = null;
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	/**
	 * 鏂颁富棰樻槸鍚﹀寘鍚玝oots妯″潡,濡傛灉鍖呭惈鍒欏垹闄ace/boots搴曚笅鍘熸潵鐨�zip鏂囦欢,鍚﹀垯涓嶅仛浠讳綍鎿嶄綔
	 */
	public static void removeOldBoots(File file) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			ZipEntry fonts = zipFile.getEntry(ThemeConstants.THEME_MODEL_BOOTS);
			if (fonts != null) {

				File oldBoots = new File(ThemeConstants.THEME_FACE_BOOTS);
				if (oldBoots.exists()) {
					Log.e(TAG,
							"The new theme contain boots, so delete the old theme's boots");
					FileUtils.forceDelete(oldBoots);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (zipFile != null) {
					zipFile.close();
					zipFile = null;
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public static void unzipFontsBeforeReboot() {
		File fonts_temp = null;
		try {
			/**
			 * 濡傛灉fonts_temp姝ゆ枃浠跺瓨鍦紝鍒欒
			 * 鏄庤繘琛屼簡瀛椾綋鏇存崲锛屽垯鍦ㄥ叧鏈哄墠锛屽厛鎶婃棫鐨勫瓧浣撳垹闄わ紝鐒跺悗鎶婃柊瀛椾綋锛坒onts_temp锛夎繘琛岃В鍘嬶紝鏈
			 * �悗鍒犻櫎fonts_temp鏂囦欢
			 */
			fonts_temp = new File(ThemeConstants.THEME_FACE_FONTS_TEMP);
			if (fonts_temp.exists()) {
				File fonts = new File(ThemeConstants.THEME_FACE_FONTS);
				if (fonts.exists()) {
					FileUtils.forceDelete(fonts);
				}

			}

			/**
			 * 濡傛灉鏄痜onts_temp鍘嬬缉鍖咃紝鍒欎粛闇�杩涜瑙ｅ帇
			 */
			if (ThemeUtil.isZipFile(fonts_temp)) {

				File fonts = new File(ThemeConstants.THEME_FACE_FONTS);

				fonts_temp.renameTo(fonts);

				boolean success = ThemeUtil.unZip(fonts,
						fonts.getAbsolutePath());

				if (success) {
					Log.i(TAG, "unzip fonts is success");
				} else {
					Log.i(TAG, "unzip fonts is fail");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 鏄惁閲嶅惎
	 */
	public static void reboot(final Context mContext) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.reboot_for_fonts_title)
				.setMessage(R.string.reboot_for_fonts_msg)
				.setPositiveButton(R.string.reboot_now,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								new RebootTask(mContext).execute("");
							}
						})
				.setNegativeButton(R.string.reboot_late,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								ThemeUtil.applyThemeAndExit(mContext);
							}
						}).create().show();
	}
    
    public static void createAlertDialog(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.sdcard_status_changed_title)
        .setMessage(R.string.sdcard_status_changed_msg)
        .setCancelable(false)
        .setPositiveButton(R.string.theme_ok, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                ThemeUtil.exitApplication();
            }
        });
        builder.create().show();
    }
}
