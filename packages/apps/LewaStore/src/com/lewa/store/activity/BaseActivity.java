package com.lewa.store.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public abstract class BaseActivity extends Activity implements OnClickListener {

	protected final int SHOW_PROGRESSDIALOG = 10000;//显示progress dialog
	protected final int CANCEL_PROGRESSDIALOG = 10001;//隐藏

	protected ProgressDialog progressDialog = null; // 加载对话框
	protected int layoutId;//布局id
	protected InputMethodManager imm; // 软键盘

	protected Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int what = msg.what;
			if (what == SHOW_PROGRESSDIALOG) {
				showProgressDialog("正在连接，请稍候...");
			} else if (what == CANCEL_PROGRESSDIALOG) {
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
			}

			handlerMessage(msg.what);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
//		requestWindowFeature(Window.FEATURE_NO_TITLE);//无title
/*		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏
*/
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/*
	 * 初始化页面组件
	 */
	public abstract void initPages();

	/*
	 * 处理单击事件
	 */
	public abstract void onClickListener(View view);

	/*
	 * 处理handler消息
	 */
	public abstract void handlerMessage(int msg);

	/*
	 * 显示网络不可用对话框
	 */
	protected void alertDialog(String title, String des, final Class<?> goClass) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(title);
		adb.setMessage(des);
		adb.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (goClass != null) {
					Intent intent = new Intent();
					intent.setClass(BaseActivity.this, goClass);
					startActivity(intent);
					finish();
				}

			}

		});
		adb.show();
	}

	@Override
	public void onClick(View v) {

		onClickListener(v);
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
	 * 显示加载对话框
	 */
	public void showProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("正在加载数据，请稍候...");
		progressDialog.show();
	}

	/*
	 * 隐藏软键盘
	 */
	public void hideSoftKeyword(View view) {
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	/*
	 *显示toast提示 
	 */
	public void toast(String msg){
		Toast.makeText(BaseActivity.this, msg,Toast.LENGTH_SHORT).show();
	}
}
