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
	private ScanUtil manager;//----ɨ�������

	private ImageView logoView;//----LOGO�����ؼ�

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//----�жϷ����Ƿ��Ѿ�������ѡ����ת����
		if (isServiceRunning()) {
			//----��ת��������
			Intent intent = new Intent(getApplicationContext(),MainActivity.class);
			startActivity(intent); 
			overridePendingTransition(R.anim.activity_zoomin, R.anim.activity_zoomout);
			LogoActivity.this.finish();
		} else {
			//----���ų�ʼ���涯��
			initActivity();
		}
	}
	//----�ͷ��ڴ�
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mHandler != null) {
			mHandler.removeCallbacks(scan);
			mHandler.removeCallbacks(runnable);
		}
	}
	
	/**
	 * ��ʼ��
	 */
	private void initActivity() {
		setContentView(R.layout.activity_logo);
		
		logoView = (ImageView) findViewById(R.id.activity_logo_name);
		
		manager = new ScanUtil(getApplicationContext());
		
		//----���ö���
		final Animation logoAnim = AnimationUtils.loadAnimation(
				getApplicationContext(), R.anim.activity_logo);
		logoView.startAnimation(logoAnim);//----��������
		
		//----����Handlerִ�г�ʱ��ĸ��¹���
		mHandler = new Handler();
		mHandler.post(scan);//----ִ��ɨ��
		mHandler.postDelayed(runnable, 3500);//----�ӳ�ִ����ת
	}
	
	/**
	 * ��ת����ҳ��
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
	 * ɨ���ļ�
	 */
	private Runnable scan = new Runnable() {
		@Override
		public void run() {
			manager.scanMusicFromDB();
		}
	};
	
	/**
	 * �������Ƿ���������
	 * @return true/false
	 */
	private boolean isServiceRunning() {
		
		//----��ȡϵͳ�������е�ȫ��������Ϣ
		List<ActivityManager.RunningServiceInfo> serviceList = getSystemRunningServiceInfo();
		
		//----û�з��񣨲�������
		if (!(serviceList.size() > 0)) {
			return false;
		}
		
		//----�жϷ����Ƿ�ΪMM��MediaService����
		for (int i = 0; i < serviceList.size(); i++) {
			//----��������д���MM��MedisService���񣬾ͷ���true
			if (serviceList.get(i).service.getClassName().equals(
					"com.whitelaning.mmusic.service.MediaService")) { 
				return true;
			}
		}
		//----�����в�����MM��MedisService����Ĭ�Ϸ���false
		return false;
	}
	
	/**
	 * ��ȡϵͳ�������е�ȫ������
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
