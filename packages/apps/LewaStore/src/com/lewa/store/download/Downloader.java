package com.lewa.store.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import com.lewa.store.utils.Constants;
import com.lewa.store.utils.FileHelper;
import com.lewa.store.utils.StrUtils;

public class Downloader {

	public static String TAG = Downloader.class.getSimpleName();

	private String murlstr;
	private int mAppId;
	private String localfile;
	private int threadcount;
	private Handler mHandler;
	private Dao dao;
	private int fileSize;
	private List<DownloadInfo> infos;
	private static final int INIT = 1;
	private static final int DOWNLOADING = 2;
	private static final int PAUSE = 3;
	private int state = INIT;
	private Context context;

	private ThreadPoolExecutor executor;

	private LoadInfo loadInfo = null;

	public LoadInfo getLoadInfo() {
		return loadInfo;
	}

	public void setLoadInfo(LoadInfo loadInfo) {
		this.loadInfo = loadInfo;
	}

	public Handler mServiceHandler = null;

	public Downloader(String urlstr, int AppId, String localfile,
			int threadcount, Context context, Dao d, ThreadPoolExecutor exec,
			Handler ServiceHandler) {
		this.murlstr = urlstr;
		this.mAppId = AppId;
		this.localfile = localfile;
		this.threadcount = threadcount;
		this.mServiceHandler = ServiceHandler;
		this.context = context;
		this.dao = d;
		this.executor = exec;
	}

	public String getLocalfile() {
		return localfile;
	}

	public void setLocalfile(String localfile) {
		this.localfile = localfile;
	}

	public synchronized boolean isdownloading() {
		return getState() == DOWNLOADING;
	}

	public void deleteExistUrl(String urlstr) {
		synchronized (this) {
			dao.delete(urlstr);
			this.reset();
		}
	}

	private boolean isFirst(String urlstr) {
		synchronized (this) {
			this.deleteExistUrl(urlstr);
			return dao.isHasInfors(urlstr);
		}
	}

	public void recordDownloadInfo(int fileSize, String urlstr, int appid) {
		if (isFirst(urlstr)) {
			infos = new ArrayList<DownloadInfo>();
			DownloadInfo info = new DownloadInfo(threadcount - 1,
					(threadcount - 1), fileSize - 1, 0, fileSize, appid,
					urlstr, Constants.BUTTON_STATUS_DOWNLOADING);
			infos.add(info);
			dao.saveInfos(infos);
		}
	}

	public LoadInfo getLoadInfo(int fileSize, String url) {
		LoadInfo loadInfo = null;
		Log.v("TAG", "isFirst download");
		dao.updataFileSizeInfo(fileSize + Constants.PROGRESSBAR_TEMPSIZE, url);
		loadInfo = new LoadInfo(fileSize, 0, url);
		return (loadInfo != null) ? loadInfo : null;
	}

	public synchronized void download() {
		if (state == DOWNLOADING) {
			return;
		}
		state = DOWNLOADING;
		this.executor.execute(new DownloadRunnable(murlstr));
	}

	class DownloadRunnable implements Runnable {

		private int threadId;
		private int startPos;
		private int endPos;
		private int compeleteSize;
		private String murlstr;

		public DownloadRunnable(String urlstr) {
			this.murlstr = urlstr;
			Log.e(TAG, "DownloadRunnable runing...,threadName="
					+ Thread.currentThread().getName() + ",thread id="
					+ Thread.currentThread().getId());
//			Log.e(TAG, "DownloadRunnable,url=" + this.murlstr);
		}

		@Override
		public synchronized void run() {
			android.os.Process
					.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			HttpURLConnection connection = null;
			RandomAccessFile randomAccessFile = null;
			InputStream is = null;
			try {
				URL url = new URL(murlstr);
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(10 * 1000);
				connection.setRequestMethod("GET");
				fileSize = connection.getContentLength();

				if (fileSize == Constants.FILE_SIZE_NEGATIVE) {
					// download failed
					Message msg = Message.obtain();
					msg.what = Constants.HandlerWhatFlagThree;
					msg.obj = (Integer) mAppId;
					mServiceHandler.sendMessage(msg);
					return;
				}
				loadInfo = getLoadInfo(fileSize, murlstr);
				setLoadInfo(loadInfo);
				this.startPos = 0;
				this.compeleteSize = 0;
				this.endPos = fileSize - 1;

				localfile = StrUtils.replaceSpace(localfile);
				File file = new File(localfile);// 创建本地文件
				if (file.exists() && file.delete()) {
					Log.e(TAG, "fileSize==" + fileSize);
					Log.e(TAG, "delete old file ok! path==" + localfile);
				}
				if (!file.exists()) {
					try {
						FileHelper.createDir(Constants.DATA_DIR
								+ File.separator);
						file.createNewFile();
					} catch (Exception e) {
						e.printStackTrace();
						Log.e(TAG, "create file error,msg=" + e.getMessage());
						/*
						 * if(e.getMessage().equals(Constants.
						 * EXCEPITION_NO_SUCH_FILE_OR_DIRECTORY)){ Message msg =
						 * Message.obtain(); msg.what =
						 * Constants.HandlerWhatFlagSeven; msg.obj = (Integer)
						 * mAppId; mServiceHandler.sendMessage(msg);
						 * 
						 * FileHelper.createDir(Constants.DATA_DIR +
						 * File.separator); }
						 */
					}
				}
				Log.i(TAG, "fileSize**********************************=="
						+ fileSize);
				Log.i(TAG, "localfile==" + localfile);

				Message mess = Message.obtain();
				mess.what = Constants.HandlerWhatFlagDownloading;
				mess.obj = (Integer) mAppId;
				mServiceHandler.sendMessage(mess);

				randomAccessFile = new RandomAccessFile(localfile, "rwd");
				randomAccessFile.setLength(fileSize);
				randomAccessFile.seek(startPos + compeleteSize);
				try {
					is = connection.getInputStream();
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG,
							"connection.getInputStream() error,==="
									+ e.getMessage());
				}
				Log.d("Downloader inputStream ok", is.toString());
				byte[] buffer = new byte[4096];
				int length = -1;
				int fileSize = endPos + 1;
				int times = 0;
				Intent pbIntent = null;
				try {
					while ((length = is.read(buffer)) != -1) {
						if (Downloader.this.getState() == INIT) {
							File f = new File(localfile);
							if (file.exists() && file.delete()) {
								Log.d(TAG, "Cancel download,delete file path=="
										+ localfile);
							}
							return;
						}
						randomAccessFile.write(buffer, 0, length);
						compeleteSize += length;
						if (times == 0
								|| ((int) (compeleteSize * 100 / fileSize) - 10 > times)) {
							dao.updataInfos(threadId, compeleteSize, murlstr);
							times += 10;

							pbIntent = new Intent(
									Constants.BROADCAST_UPDATE_PROGRESSBAR);
							pbIntent.putExtra("urlstr", murlstr);
							pbIntent.putExtra("compeleteSize", compeleteSize);
							pbIntent.putExtra("fileSize", fileSize);
							pbIntent.putExtra("isNormal", 1);
							context.sendBroadcast(pbIntent);

							Message msg = Message.obtain();
							msg.what = Constants.HandlerWhatFlagTwo;
							msg.obj = (Integer) mAppId;
							msg.arg1 = compeleteSize;
							msg.arg2 = fileSize;
							mServiceHandler.sendMessage(msg);
						}
						Thread.sleep(10);
					}
				} catch (SocketException e) {
					e.printStackTrace();
					Log.e(TAG, "is.read() error," + e.getMessage());
					Message msg = Message.obtain();
					msg.what = Constants.HandlerWhatFlagThree;
					msg.obj = (Integer) mAppId;
					msg.arg1 = compeleteSize;
					msg.arg2 = fileSize;
					mServiceHandler.sendMessage(msg);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Log.e(TAG, "file not found,msg=" + e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "write disk error,msg=" + e.getMessage());
					if (e.getMessage().equals(
							Constants.EXCEPITION_IO_UNEXPECTED_END_OF_STREAM)) {
						Message msg = Message.obtain();
						msg.what = Constants.HandlerWhatFlagSix;
						msg.obj = (Integer) mAppId;
						mServiceHandler.sendMessage(msg);
					}
					Log.e(TAG, "msg==" + e.getMessage());
					if (e.getMessage().equals(
							Constants.EXCEPITION_IO_NO_SPACE_LEFT_ON_DEVICE)) {
						Message msg = Message.obtain();
						msg.what = Constants.HandlerWhatFlagFive;
						msg.obj = (Integer) mAppId;
						mServiceHandler.sendMessage(msg);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					Message msg = Message.obtain();
					msg.what = Constants.HandlerWhatFlagFour;
					msg.obj = (Integer) mAppId;
					msg.arg1 = compeleteSize;
					msg.arg2 = fileSize;
					mServiceHandler.sendMessage(msg);
					Log.e(TAG, "error," + e1.getMessage());
				}
				if (this.compeleteSize == fileSize) {
					Log.i(TAG, "send to download success broadcast");
					Intent ii = new Intent(Constants.BROADCAST_DOWNLOAD_SUCESS);
					ii.putExtra("urlstr", murlstr);
					ii.putExtra("fileSize", fileSize);
					ii.putExtra("compeleteSize", compeleteSize);
					ii.putExtra("localfile", localfile);
					context.sendBroadcast(ii);

					Message msg = Message.obtain();
					msg.what = Constants.HandlerWhatFlagTwo;
					msg.obj = (Integer) mAppId;
					msg.arg1 = compeleteSize;
					msg.arg2 = fileSize;
					mServiceHandler.sendMessage(msg);
				}
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "msg==" + e.getMessage());
				if (e.getMessage().equals(
						Constants.EXCEPITION_IO_NO_SPACE_LEFT_ON_DEVICE)) {
					Message msg = Message.obtain();
					msg.what = Constants.HandlerWhatFlagFive;
					msg.obj = (Integer) mAppId;
					mServiceHandler.sendMessage(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (null != is) {
						is.close();
					}
					if (null != randomAccessFile) {
						randomAccessFile.close();
					}
					if (null != connection) {
						connection.disconnect();
					}
					if (null != dao) {
						dao.closeDb();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void delete() {
		synchronized (this) {
			dao.delete(murlstr);
		}
	}

	public void delete(String urlstr) {
		synchronized (this) {
			Log.d(TAG, "delete url=" + urlstr);
			dao.delete(urlstr);
		}
	}

	// 设置暂停
	public void pause() {
		synchronized (this) {
			state = PAUSE;
		}
	}

	public int getState() {
		synchronized (this) {
			return this.state;
		}
	}

	// 取消下载
	public void cancel() {
		synchronized (this) {
			this.delete(murlstr);
			this.reset();
		}
	}

	// 重置下载状态
	public void reset() {
		synchronized (this) {
			state = INIT;
		}
	}
}
