package com.whitelaning.mmusic.activity;

import java.util.List;
import com.whitelaning.mmusic.R;
import com.whitelaning.mmusic.util.ScanUtil;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


public class LogoActivity extends Activity {

	private Handler mHandler;
	private ScanUtil manager;//----扫描管理器

	private ImageView logoView;//----LOGO动画控件

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//----判断服务是否已经开启，选择跳转界面
		if (isServiceRunning()) {
			//----跳转到主界面
			Intent intent = new Intent(getApplicationContext(),MainActivity.class);
			startActivity(intent); 
			overridePendingTransition(R.anim.activity_zoomin, R.anim.activity_zoomout);
			LogoActivity.this.finish();
		} else {
			//----播放初始界面动画
			initActivity();
		}
	}
	//----释放内存
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mHandler != null) {
			mHandler.removeCallbacks(scan);
			mHandler.removeCallbacks(runnable);
		}
	}
	
	/**
	 * 初始化
	 */
	private void initActivity() {
		setContentView(R.layout.activity_logo);
		
		logoView = (ImageView) findViewById(R.id.activity_logo_name);
		
		manager = new ScanUtil(getApplicationContext());
		
		//----设置动画
		final Animation logoAnim = AnimationUtils.loadAnimation(
				getApplicationContext(), R.anim.activity_logo);
		logoView.startAnimation(logoAnim);//----启动动画
		
		//----启动Handler执行长时间的更新工作
		mHandler = new Handler();
		mHandler.post(scan);//----执行扫描
		mHandler.postDelayed(runnable, 3500);//----延迟执行跳转
	}
	
	/**
	 * 跳转到主页面
	 */
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			Intent intent = new Intent(LogoActivity.this, MainActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.activity_zoomin, R.anim.activity_zoomout);
			LogoActivity.this.finish();
		}
	};
	
	/**
	 * 扫描文件
	 */
	private Runnable scan = new Runnable() {
		@Override
		public void run() {
			manager.scanMusicFromDB();
		}
	};
	
	/**
	 * 检查服务是否正在运行
	 * @return true/false
	 */
	private boolean isServiceRunning() {
		
		//----获取系统正在运行的全部服务信息
		List<ActivityManager.RunningServiceInfo> serviceList = getSystemRunningServiceInfo();
		
		//----没有服务（不常见）
		if (!(serviceList.size() > 0)) {
			return false;
		}
		
		//----判断服务是否为MM的MediaService服务
		for (int i = 0; i < serviceList.size(); i++) {
			//----如果服务中存在MM的MedisService服务，就返回true
			if (serviceList.get(i).service.getClassName().equals(
					"com.whitelaning.mmusic.service.MediaService")) { 
				return true;
			}
		}
		//----服务中不存在MM的MedisService服务，默认返回false
		return false;
	}
	
	/**
	 * 获取系统正在运行的全部服务
	 * @return serviceList
	 */
	private List<ActivityManager.RunningServiceInfo> getSystemRunningServiceInfo() {
		ActivityManager activityManager = (ActivityManager) getApplicationContext()
				.getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(Integer.MAX_VALUE);
		return serviceList;
	}

}
