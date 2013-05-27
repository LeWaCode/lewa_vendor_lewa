package com.lewa.store.dialog;

public interface IMessageDialogListener {

	/**
	 * @param requestCode 对话框返回id
	 */
	public void onDialogClickOk(int requestCode);

	public void onDialogClickCancel(int requestCode);
	
	public void onDialogClickClose(int requestCode);
}
