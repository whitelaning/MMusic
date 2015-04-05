package com.whitelaning.mmusic.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.whitelaning.mmusic.R;

//----首次进入程序提示扫描对话框------------------------
public class ScanDialog extends TVAnimDialog {

	private Button button;

	public ScanDialog(Context context) {
		super(context);
		
	}

	public ScanDialog(Context context, int theme) {
		super(context, theme);
	
	}

	protected ScanDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_scan);

		button = (Button) findViewById(R.id.dialog_scan_btn_ok);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
			
				dismiss();
			}
		});

	}

}
