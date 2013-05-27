package com.lewa.pond.auth;

import com.lewa.core.util.LewaUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.Log;

public class LewaConnectionAlertDialog implements DialogInterface.OnClickListener, OnCancelListener {

	public static final String TAG = "LewaConnectionAlertDialog";
	
	protected Context context = null;
	
	private AlertDialogListener listener = null;
	
	private String title = "";
	private String message = "";
	private String retry = "";
	private String back = "";
	
	public LewaConnectionAlertDialog(Context context, AlertDialogListener listener) {
		this.context = context;
		this.listener = listener;
	}

	public boolean connected() {
		boolean hasConnection = false;
		int connectionCode = LewaUtils.checkNetworkInfo(context);
		Log.d(TAG, String.format("connection state: %d [0=no]", connectionCode));
		if (connectionCode != 0) {
			hasConnection = true;
		}
		return hasConnection;
	}
	
	public void prepareBuilder(String title, String message, String retry, String back) {
		this.title = title;
		this.message = message;
		this.retry = retry;
		this.back = back;
	}
	
	public AlertDialog.Builder getNewBuilder() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setCancelable(true);
		builder.setPositiveButton(retry, this);
		builder.setNegativeButton(back, this);
		builder.setOnCancelListener(this);
		return builder;
	}
	
	public AlertDialog dialog = null;
	private boolean loop = true;
	public void loopDialog(boolean force) {
		boolean c = this.connected();
		if (c && !force) {
			Log.d(TAG, "connected");
			loop = false;
			this.listener.OnHasConnectionListener();
			return;
		}
		Log.d(TAG, "not connected");
		this.dialog = this.getNewBuilder().show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.cancel();
		if (which == DialogInterface.BUTTON_POSITIVE) {
			Log.d(TAG, "looping dialog");
			this.loopDialog(false);
		} else if (which == DialogInterface.BUTTON_NEGATIVE) {
			Log.d(TAG, "aborting dialog");
			this.listener.OnAbortedDialogListener();
			this.loop = false;
		}
	}
	
	public void clearDialog() {
		if (this.dialog != null) {
			this.dialog.cancel();
			this.dialog = null;
		}
	}
	
	public interface AlertDialogListener {
		public void OnHasConnectionListener();
		public void OnAbortedDialogListener();
		public void OnCancelConnectionRetryListener();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		this.listener.OnCancelConnectionRetryListener();
	}
}
