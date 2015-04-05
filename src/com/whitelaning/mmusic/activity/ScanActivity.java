package com.whitelaning.mmusic.activity;

import java.lang.ref.WeakReference;

import com.whitelaning.mmusic.R;
import com.whitelaning.mmusic.adapter.ScanAdapter;
import com.whitelaning.mmusic.service.MediaService;
import com.whitelaning.mmusic.util.ScanUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 歌曲扫描，主要扫描SD卡新增歌曲
 * 
 * @author Administrator
 *
 */

public class ScanActivity extends Activity implements OnClickListener {

	private String INFO_NORMAL;
	private String INFO_SCAN;
	private String INFO_FINISH;

	private boolean scaning = true;// ----默认允许扫描
	private boolean canReturn = true;// ----默认允许退出

	private ImageButton btnReturn;// ----页面结束按钮
	private Button btnScan;// ----扫描按钮
	private ImageView btnScanBg;

	private TextView scanFinishText;
	private TextView scanText;// ----扫描时的文字更新
	private ListView scanList;// ----媒体库目录列表

	private ScanHandler handler;
	private ScanUtil manager;
	private ScanAdapter adapter;

	/**
	 * 更新系统媒体库 在Android 4.4后，此方法失效
	 */
	private void upSystemMedia() {
		// ----在4.4后此方法失效
		// sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
		// Uri.parse("file://"
		// + Environment.getExternalStorageDirectory())));

		MediaScannerConnection.scanFile(this, new String[] { Environment.getExternalStorageDirectory().toString() }, null, new MediaScannerConnection.OnScanCompletedListener() {
			public void onScanCompleted(String path, Uri uri) {
			}
		});

		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		upSystemMedia();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		upSystemMedia();// ----更新系统媒体库

		setContentView(R.layout.activity_scan);

		initialize();// ----初始化

		handler = new ScanHandler(this);

		Intent intent = new Intent(MediaService.BROADCAST_ACTION_SERVICE);
		intent.putExtra(MediaService.INTENT_ACTIVITY, MediaService.ACTIVITY_SCAN);
		sendBroadcast(intent);
	}

	/**
	 * 初始化
	 */
	private void initialize() {
		INFO_NORMAL = this.getString(R.string.Scan_songs);
		INFO_SCAN = this.getString(R.string.Scanning);
		INFO_FINISH = this.getString(R.string.Scan_completed);

		btnReturn = (ImageButton) findViewById(R.id.activity_scan_ib_return);
		btnScanBg = (ImageView) findViewById(R.id.activity_scan_btn_scan_bg);
		btnScan = (Button) findViewById(R.id.activity_scan_btn_scan);
		scanText = (TextView) findViewById(R.id.activity_scan_text);
		scanList = (ListView) findViewById(R.id.activity_scan_lv);
		scanFinishText = (TextView) findViewById(R.id.activity_scan_finish_text);

		scanText.setText(INFO_NORMAL);

		btnScan.setOnClickListener(this);
		btnReturn.setOnClickListener(this);
		scanFinishText.setOnClickListener(this);
		manager = new ScanUtil(getApplicationContext());
		adapter = new ScanAdapter(getApplicationContext(), manager.searchAllDirectory());
		scanList.setAdapter(adapter);
		myAnimation_rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!canReturn) {// ----扫描中暂不可退出，此处为!判断
				return true;
			} else {
				if (!scaning) {
					sendUpdateBroadcast();// ----通知上一个页面更新
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void sendUpdateBroadcast() {
		Intent intent = new Intent(MainActivity.BROADCAST_ACTION_SCAN);
		sendBroadcast(intent);
	}

	/**
	 * 提示文字
	 * 
	 * @param text
	 */
	private void updateText(String text) {
		scanText.setText(text);
	}

	/**
	 * 执行扫描任务
	 * 
	 * @author Administrator
	 */
	private class ScanTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			manager.scanMusicFromSD(adapter.getPath(), handler);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			super.onPostExecute(result);
			SharedPreferences preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
			preferences.edit().putBoolean(MainActivity.PREFERENCES_SCAN, true).commit();// ----完成扫描记录
			btnScanBg.clearAnimation();
			scanFinishText.setText(INFO_FINISH);
			btnScan.setEnabled(true);
			canReturn = true;
		}
	}

	/**
	 * 实时更新UI静态嵌套类
	 * 
	 * @author Administrator
	 *
	 */
	private static class ScanHandler extends Handler {

		private WeakReference<ScanActivity> mReference;

		public ScanHandler(ScanActivity activity) {

			mReference = new WeakReference<ScanActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			if (mReference.get() != null) {
				ScanActivity theActivity = mReference.get();
				theActivity.updateText(msg.obj.toString());
			}
		}

	}

	private Animation myAnimation_rotate;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activity_scan_ib_return:
			if (canReturn) {// ----扫描中暂不可退出
				if (!scaning) {
					sendUpdateBroadcast();// ----通知上一个页面更新
				}
				finish();
			}
			break;

		case R.id.activity_scan_btn_scan:
			if (scaning) {
				scaning = false;
				canReturn = false;
				new ScanTask().execute();
				scanFinishText.setText(INFO_SCAN);
				btnScan.setEnabled(false);
				btnScanBg.startAnimation(myAnimation_rotate);
			}
			break;

		case R.id.activity_scan_finish_text:
			sendUpdateBroadcast();// ----通知上一个页面更新
			finish();
			overridePendingTransition(R.anim.activity_zoomin, R.anim.activity_zoomout);
			break;

		default:
			break;
		}
	}
}
