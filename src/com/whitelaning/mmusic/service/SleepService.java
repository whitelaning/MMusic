package com.whitelaning.mmusic.service;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
public class SleepService extends Service{
	int time=0;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
    private int count = 0;   
    private boolean run = false;   
    private Handler handler = new Handler();  
    
    private Runnable task = new Runnable() {
        public void run() {
            if (run) {
                count = count + 60;
                handler.postDelayed(task, 60*1000); 
            }
            if(count>time){
            	Intent intent1 = new Intent("com.whitelaning.mmusic.sleep.close");
    			sendBroadcast(intent1);
            	run=false;
            	Intent intent2 = new Intent(getApplicationContext(), SleepService.class);
            	stopService(intent2);
            }
        }
    };
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		time=0;
		time=intent.getIntExtra("sleeptime", 0);
		run = true;
		handler.postDelayed(task, 1000); 
	}
}
