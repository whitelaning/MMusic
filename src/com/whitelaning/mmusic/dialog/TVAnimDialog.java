package com.whitelaning.mmusic.dialog;

import com.whitelaning.mmusic.R;
import com.whitelaning.mmusic.activity.MainActivity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
/**
 * 电视机开关效果的Dialog
 * 继承该类就带有该动画效果
 */

public class TVAnimDialog extends Dialog {

	private int dialogId = MainActivity.DIALOG_DISMISS;
	private OnTVAnimDialogDismissListener listener;

	public TVAnimDialog(Context context) {
		super(context, R.style.TVAnimDialog);//----此处附上Dialog样式
		
	}

	public TVAnimDialog(Context context, int theme) {
		
		super(context, theme);
	}

	protected TVAnimDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(R.style.TVAnimDialogWindowAnim);//----此处附上Dialog动画
	}

	@Override
	public void dismiss() {

		super.dismiss();
		if (listener != null) {
			listener.onDismiss(dialogId);
		}
	}

	//----用于区分Dialog用途-------------------------

	public void setDialogId(int dialogId) {
		this.dialogId = dialogId;
	}

	//----设置监听器--------------------------------

	public void setOnTVAnimDialogDismissListener(
			OnTVAnimDialogDismissListener listener) {
		this.listener = listener;
	}

	//----用于监听对话框关闭的接口-----------------------
	 
	public interface OnTVAnimDialogDismissListener {
		void onDismiss(int dialogId);
	}

}
