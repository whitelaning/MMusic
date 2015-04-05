package com.whitelaning.mmusic.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.whitelaning.mmusic.R;

public class AboutDialog extends TVAnimDialog {

	private Button button;

	public AboutDialog(Context context) {
		super(context);

	}

	public AboutDialog(Context context, int theme) {
		super(context, theme);

	}

	protected AboutDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_about);

		button = (Button) findViewById(R.id.dialog_about_btn_ok);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				dismiss();
			}
		});

	}

}
