package com.whitelaning.mmusic.activity;

import com.whitelaning.mmusic.R;
import com.whitelaning.mmusic.service.MediaService;
import com.whitelaning.mmusic.service.SleepService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SleepActivity extends Activity {
	private ImageButton sleepqueding, sleepquxiao;
	private TextView sleeptext;
	SeekBar sleepseekbar;
	RelativeLayout sleeptextrela;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sleep);
		sleepqueding = (ImageButton) this.findViewById(R.id.sleepqueding);
		sleepquxiao = (ImageButton) this.findViewById(R.id.sleepquxiao);
		sleeptext = (TextView) this.findViewById(R.id.sleeptext);
		sleepseekbar = (SeekBar) this.findViewById(R.id.sleepseekbar);
		sleeptextrela = (RelativeLayout) this.findViewById(R.id.sleeptextrela);

		sleepquxiao.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		sleepseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				sleeptextrela.setPadding(arg1 * 44 / 10, 0, 0, 0);
				sleeptext.setText(String.valueOf(arg1 + 5));
			}
		});

		sleepqueding.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!MediaService.mediaPlayer.isPlaying()) {
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.no_playing),
							Toast.LENGTH_SHORT).show();
				} else {
					Intent intent = new Intent(SleepActivity.this,
							SleepService.class);
					intent.putExtra("sleeptime",
							(sleepseekbar.getProgress() + 5) * 60);
					startService(intent);

					if (sleepseekbar.getProgress() >= 55) {
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(R.string.one_hour)
										+ (sleepseekbar.getProgress() - 55)
										+ getResources()
												.getString(
														R.string.minutes_later_trun_off),
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(
								getApplicationContext(),
								sleepseekbar.getProgress()
										+ 5
										+ getResources()
												.getString(
														R.string.minutes_later_trun_off),
								Toast.LENGTH_SHORT).show();
					}
					finish();
				}
			}
		});
	}
}
