package com.lewa.store.activity;

import com.lewa.store.R;
import com.lewa.store.nav.ApplicationManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

public class BaseClass extends Activity {

	ProgressDialog progressDialog = null; // 加载对话框

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	/*
	 * 显示加载对话框,自定义内容
	 */
	public void showProgressDialog(String message) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(message);
		progressDialog.show();
	}

	/*
	 * close
	 */
	public void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	protected void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 提示信息后,进入某个Activity
	 * 
	 * @param title
	 * @param des
	 * @param goClass
	 */
	protected void alertDialog(String title, String des, final Class<?> goClass) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(title);
		adb.setMessage(des);
		adb.setPositiveButton(getString(R.string.dialog_ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (goClass != null) {
							Intent intent = new Intent();
							intent.setClass(BaseClass.this, goClass);
							startActivity(intent);
							finish();
						}

					}

				});
		adb.show();
	}

}
