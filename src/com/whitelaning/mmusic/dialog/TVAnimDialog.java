package com.whitelaning.mmusic.dialog;

import com.whitelaning.mmusic.R;
import com.whitelaning.mmusic.activity.MainActivity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
/**
 * ���ӻ�����Ч����Dialog
 * �̳и���ʹ��иö���Ч��
 */

public class TVAnimDialog extends Dialog {

	private int dialogId = MainActivity.DIALOG_DISMISS;
	private OnTVAnimDialogDismissListener listener;

	public TVAnimDialog(Context context) {
		super(context, R.style.TVAnimDialog);//----�˴�����Dialog��ʽ
		
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
		getWindow().setWindowAnimations(R.style.TVAnimDialogWindowAnim);//----�˴�����Dialog����
	}

	@Override
	public void dismiss() {

		super.dismiss();
		if (listener != null) {
			listener.onDismiss(dialogId);
		}
	}

	//----��������Dialog��;-------------------------

	public void setDialogId(int dialogId) {
		this.dialogId = dialogId;
	}

	//----���ü�����--------------------------------

	public void setOnTVAnimDialogDismissListener(
			OnTVAnimDialogDismissListener listener) {
		this.listener = listener;
	}

	//----���ڼ����Ի���رյĽӿ�-----------------------
	 
	public interface OnTVAnimDialogDismissListener {
		void onDismiss(int dialogId);
	}

}
