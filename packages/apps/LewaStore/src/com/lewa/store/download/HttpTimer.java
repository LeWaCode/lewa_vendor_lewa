package com.lewa.store.download;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.lewa.store.utils.Constants;

public class HttpTimer extends Thread {

	private String TAG = HttpTimer.class.getSimpleName();

	/** 每多少毫秒检测一次 */
	protected int m_rate = 100;
	/** 超时时间长度,按毫秒计算 */
	private int m_length;
	/** 已经运行的时间 */
	private int m_elapsed;

	private Handler mHandler;
	private HttpURLConnection connection;
	private URL url;

	public HttpTimer(int length, Handler h) {
		m_length = length;
		m_elapsed = 0;
		this.mHandler = h;
	}

	public synchronized void reset() {
		m_elapsed = 0;
		System.out.println("reset http timer");
	}

	public synchronized void setTimeOut() {
		m_elapsed = m_length + 1;
	}

	public void run() {
		Log.d(TAG, "http timer running");
		for (;;) {
			int responseCode = makeRequest();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				try {
					Thread.sleep(m_rate);
				} catch (InterruptedException ioe) {
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
				synchronized (this) {
					m_elapsed += m_rate;
					Log.e(TAG, "time out ==" + m_elapsed + " ms");
					if (m_elapsed > m_length) {
						timeout();
						break;
					}
				}
			} else {
				break;
			}
		}
	}

	/**
	 * 超时处理
	 */
	private void timeout() {
		Message msg = Message.obtain();
		msg.what = Constants.HTTP_REQUEST_ERROR;
		mHandler.sendMessage(msg);
	}

	private int makeRequest() {
		int responseCode = 0;
		try {
			url = new URL(Constants.HTTP_REQUEST_URL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(20 * 1000);
			connection.setRequestMethod("GET");
			connection.connect();
			responseCode = connection.getResponseCode();
			connection.disconnect();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		returnRquestCode(responseCode);
		return responseCode;
	}

	private void returnRquestCode(int responseCode) {
		switch (responseCode) {
		case java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT:// 504
			break;
		case java.net.HttpURLConnection.HTTP_FORBIDDEN:// 403
			break;
		case java.net.HttpURLConnection.HTTP_INTERNAL_ERROR:// 500
			break;
		case java.net.HttpURLConnection.HTTP_NOT_FOUND:// 404
			break;
		case java.net.HttpURLConnection.HTTP_OK://200
			break;
		default:
			break;
		}
		Log.d(TAG, "responseCode==" + responseCode);
	}
}