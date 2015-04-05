package com.whitelaning.mmusic.service;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.whitelaning.mmusic.R;
import com.whitelaning.mmusic.Binder.MediaBinder;
import com.whitelaning.mmusic.Binder.MediaBinder.OnServiceBinderListener;
import com.whitelaning.mmusic.activity.MainActivity;
import com.whitelaning.mmusic.entity.MusicInfo;
import com.whitelaning.mmusic.list.CoverList;
import com.whitelaning.mmusic.list.FavoriteList;
import com.whitelaning.mmusic.list.FolderList;
import com.whitelaning.mmusic.list.LyricList;
import com.whitelaning.mmusic.list.MusicList;
import com.whitelaning.mmusic.lyric.LyricItem;
import com.whitelaning.mmusic.lyric.LyricParser;
import com.whitelaning.mmusic.lyric.LyricView;
import com.whitelaning.mmusic.util.AlbumUtil;

/**
 * 控制播放服务
 */
public class MediaService extends Service {

	public static final int CONTROL_COMMAND_PLAY = 0;//----控制命令：播放或者暂停
	public static final int CONTROL_COMMAND_PREVIOUS = 1;//----控制命令：上一首
	public static final int CONTROL_COMMAND_NEXT = 2;//----控制命令：下一首
	public static final int CONTROL_COMMAND_MODE = 3;//----控制命令：播放模式切换
	public static final int CONTROL_COMMAND_REWIND = 4;//----控制命令：快退
	public static final int CONTROL_COMMAND_FORWARD = 5;//----控制命令：快进
	public static final int CONTROL_COMMAND_REPLAY = 6;//----控制命令：用于快退、快进后的继续播放

	public static final int ACTIVITY_SCAN = 0x101;//----扫描界面
	public static final int ACTIVITY_MAIN = 0x102;//----主界面
	public static final int ACTIVITY_PLAYER = 0x103;//----播放界面
	public static final int ACTIVITY_SETTING = 0x104;//----设置界面

	public static final String INTENT_ACTIVITY = "activity";//----区分来自哪个界面
	public static final String INTENT_LIST_PAGE = "list_page";//----列表页面
	public static final String INTENT_LIST_POSITION = "list_position";//----列表当前项
	public static final String INTENT_FOLDER_POSITION = "folder_position";//----文件夹列表当前项
	
	public static final String BROADCAST_ACTION_SERVICE = 
			"com.whitelaning.mmusic.action.service";//----广播标志
	public static final String BROADCAST_NOTIFICATION_ITEM_BUTTON_PLAY = 
			"com.whitelaning.mmusic.notification.play";//----通知栏播放按钮
	public static final String BROADCAST_NOTIFICATION_ITEM_BUTTON_NEXT = 
			"com.whitelaning.mmusic.notification.next";//----通知栏下一首按钮
	public static final String BROADCAST_SLEEP_CLOSE = 
			"com.whitelaning.mmusic.sleep.close"; //----睡眠结束广播

	private static final int MEDIA_PLAY_ERROR = 0;
	private static final int MEDIA_PLAY_START = 1;
	private static final int MEDIA_PLAY_UPDATE = 2;
	private static final int MEDIA_PLAY_COMPLETE = 3;
	private static final int MEDIA_PLAY_UPDATE_LYRIC = 4;
	private static final int MEDIA_PLAY_REWIND = 5;
	private static final int MEDIA_PLAY_FORWARD = 6;


	private final int MODE_NORMAL = 0;//----顺序播放，放到最后一首停止
	private final int MODE_REPEAT_ONE = 1;//----单曲循环
	private final int MODE_REPEAT_ALL = 2;//----全部循环
	private final int MODE_RANDOM = 3;//----随即播放
	private final int UPDATE_LYRIC_TIME = 150;//----歌词更新间隔0.15秒
	private final int UPDATE_UI_TIME = 1000;//----UI更新间隔1秒

	private MusicInfo info;//----歌曲详情
	private List<LyricItem> lyricList;//----歌词列表
	private List<Integer> positionList;//----列表当前项集合，目的记住前面所播放的所有歌曲

	private String mp3Path;//----mp3文件路径
	private String lyricPath;//----歌词文件路径

	private int mode = MODE_NORMAL;//----播放模式(默认顺序播放)
	private int page = MainActivity.SLIDING_MENU_ALL;//----列表页面(默认全部歌曲)
	private int lastPage = 0;//----记住上一次的列表页面
	private int position = 0;//----列表当前项
	private int folderPosition = 0;//----文件夹列表当前项
	private int mp3Current = 0;//----歌曲当前时间
	private int mp3Duration = 0;//----歌曲总时间


	private boolean hasLyric = false;//----是否有歌词
	private boolean isCommandPrevious = false;//----是否属于上一首操作命令

	public static MediaPlayer mediaPlayer;
	private MediaBinder mBinder;
	private AlbumUtil albumUtil;
	private LyricView lyricView;

	private RemoteViews remoteViews;
	private ServiceHandler mHandler;
	private ServiceReceiver receiver;
	private Notification notification;
	private SharedPreferences preferences;

	@Override
	public void onCreate() {
		
		super.onCreate();
		mediaPlayer = new MediaPlayer();
		mHandler = new ServiceHandler(this);
		mBinder = new MediaBinder();
		albumUtil = new AlbumUtil();
		lyricList = new ArrayList<LyricItem>();
		positionList = new ArrayList<Integer>();

		mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				
				mp.start();
				mp3Current = 0;//----重置
				prepared();//----准备播放
			}
		});
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				
				removeAllMsg();//----移除所有消息
				mHandler.sendEmptyMessage(MEDIA_PLAY_COMPLETE);
			}
		});
		mediaPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
			
				removeAllMsg();//----移除所有消息
				mp.reset();
				page = MainActivity.SLIDING_MENU_ALL;
				position = 0;
				File file = new File(mp3Path);
				if (file.exists()) {
					Toast.makeText(getApplicationContext(), "播放出错",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "文件已不存在",
							Toast.LENGTH_SHORT).show();
					mHandler.sendEmptyMessage(MEDIA_PLAY_ERROR);
				}
				mp3Path = null;
				return true;
			}
		});
		mBinder.setOnServiceBinderListener(new OnServiceBinderListener() {

			@Override
			public void seekBarStartTrackingTouch() {
				if (mediaPlayer.isPlaying()) {
					removeUpdateMsg();
				}
			}

			@Override
			public void seekBarStopTrackingTouch(int progress) {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.seekTo(progress);
					update();
				}
			}
			@Override
			public void lrc(LyricView lyricView) {
				MediaService.this.lyricView = lyricView;//----获得歌词视图
			}

			@Override
			public void control(int command) {
				
				switch (command) {
				case CONTROL_COMMAND_PLAY://----播放与暂停
					if (mediaPlayer.isPlaying()) {
						pause();
					} else {
						if (mp3Path != null) {
							mediaPlayer.start();
							prepared();
						} else {//----无指定情况下播放全部歌曲列表的第一首
							startServiceCommand();
							
						}
					}
					break;

				case CONTROL_COMMAND_PREVIOUS://----上一首
					previous();
					break;

				case CONTROL_COMMAND_NEXT://----下一首
					next();
					break;

				case CONTROL_COMMAND_MODE://----播放模式
					if (mode < MODE_RANDOM) {
						mode++;
					} else {
						mode = MODE_NORMAL;
					}
					switch (mode) {
					case MODE_NORMAL:
						Toast.makeText(getApplicationContext(), "顺序播放",
								Toast.LENGTH_SHORT).show();
						break;

					case MODE_REPEAT_ONE:
						Toast.makeText(getApplicationContext(), "单曲循环",
								Toast.LENGTH_SHORT).show();
						break;

					case MODE_REPEAT_ALL:
						Toast.makeText(getApplicationContext(), "全部循环",
								Toast.LENGTH_SHORT).show();
						break;

					case MODE_RANDOM:
						Toast.makeText(getApplicationContext(), "随机播放",
								Toast.LENGTH_SHORT).show();
						break;
					}
					mBinder.modeChange(mode);
					break;

				case CONTROL_COMMAND_REWIND://----快退
					if (mediaPlayer.isPlaying()) {
						removeAllMsg();
						rewind();
					}
					break;

				case CONTROL_COMMAND_FORWARD://----快进
					if (mediaPlayer.isPlaying()) {
						removeAllMsg();
						forward();
					}
					break;

				case CONTROL_COMMAND_REPLAY://----用于快退、快进后的继续播放
					if (mediaPlayer.isPlaying()) {
						replay();
					}
					break;
				}
			}
		});
		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		mode = preferences.getInt(MainActivity.PREFERENCES_MODE, MODE_NORMAL);//----取出上次的播放模式

		notification = new Notification();//----通知栏相关
		notification.icon = R.drawable.ic_launcher;
		notification.flags = Notification.FLAG_NO_CLEAR;
		notification.contentIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, new Intent(getApplicationContext(),
						MainActivity.class), 0);
		remoteViews = new RemoteViews(getPackageName(),
				R.layout.notification_item);

		Intent buttonPlayIntent = new Intent(BROADCAST_NOTIFICATION_ITEM_BUTTON_PLAY); //----设置通知栏按钮广播
        PendingIntent pendButtonPlayIntent = PendingIntent.getBroadcast(this, 0, buttonPlayIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.notification_item_btn_pause, pendButtonPlayIntent);//----设置对应的按钮ID监控
      
		Intent buttonNextIntent = new Intent(BROADCAST_NOTIFICATION_ITEM_BUTTON_NEXT);
		PendingIntent pendButtonNextIntent = PendingIntent.getBroadcast(this, 0, buttonNextIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.notification_item_btn_next, pendButtonNextIntent);

		receiver = new ServiceReceiver();//----注册广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BROADCAST_NOTIFICATION_ITEM_BUTTON_PLAY);//---通知栏按钮广播
		intentFilter.addAction(BROADCAST_NOTIFICATION_ITEM_BUTTON_NEXT);
		intentFilter.addAction(Intent.ACTION_HEADSET_PLUG); //----耳机插入状态广播
		intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
		intentFilter.addAction(BROADCAST_ACTION_SERVICE);
		intentFilter.addAction(BROADCAST_SLEEP_CLOSE);
		intentFilter.addAction(INTENT_LIST_POSITION);
		registerReceiver(receiver, intentFilter);

		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);//----获取电话通讯服务
		telephonyManager.listen(new ServicePhoneStateListener(),
				PhoneStateListener.LISTEN_CALL_STATE);//----创建一个监听对象，监听电话状态改变事件
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null && !bundle.isEmpty()) {
				page = bundle.getInt(INTENT_LIST_PAGE, 0);
				position = bundle.getInt(INTENT_LIST_POSITION, 0);
				folderPosition = bundle.getInt(INTENT_FOLDER_POSITION,
						folderPosition);
				play();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		if (mediaPlayer != null) {
			stopForeground(true);
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
			}
			removeAllMsg();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		preferences.edit().putInt(MainActivity.PREFERENCES_MODE, mode).commit();//----保存上次的播放模式
	}

	@Override
	public IBinder onBind(Intent intent) {

		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {

		lyricView = null;
		removeAllMsg();//----移除所有消息
		return true;//----一定返回true，允许执行onRebind
	}

	@Override
	public void onRebind(Intent intent) {
	
		super.onRebind(intent);
		if (mediaPlayer.isPlaying()) {//----如果正在播放重新绑定服务的时候重新注册
			prepared();//----因为消息已经移除，所有需要重新开启更新操作
		} else {
			if (mp3Path != null) {//----暂停原先播放重新开页面需要恢复原先的状态
				mp3Duration = mediaPlayer.getDuration();
				info.setMp3Duration(mp3Duration);
				CoverList.cover = albumUtil.scanAlbumImage(info.getPath());
				mBinder.playStart(info);
				mp3Current = mediaPlayer.getCurrentPosition();
				mBinder.playUpdate(mp3Current);
				mBinder.playPause();
			}
		}
		mBinder.modeChange(mode);
	}

	//----播放操作----------------------------------------

	private void play() {
		int size = 0;
		switch (page) {
		case MainActivity.SLIDING_MENU_ALL:
			size = MusicList.list.size();
			if (size > 0) {
				info = MusicList.list.get(position);
			}
			break;

		case MainActivity.SLIDING_MENU_FAVORITE:
			size = FavoriteList.list.size();
			if (size > 0) {
				info = FavoriteList.list.get(position);
			}
			break;

		case MainActivity.SLIDING_MENU_FOLDER_LIST:
			size = FolderList.list.get(folderPosition).getMusicList().size();
			if (size > 0) {
				info = FolderList.list.get(folderPosition).getMusicList()
						.get(position);
			}
			break;
		}
		if (size > 0) {
			mp3Path = info.getPath();
			lyricPath = LyricList.map.get(info.getFile());
			if (mp3Path != null) {
				initMedia();//----先初始化音乐
				initLrc();//----再初始化歌词
			}
			lastPage = page;
			if (!isCommandPrevious) {
				positionList.add(position);
			}
			isCommandPrevious = false;
		}
	}

	//----自动播放操作----------------------------

	private void autoPlay() {
		if (mode == MODE_NORMAL) {
			if (position != getSize() - 1) {
				next();
			} else {
				mBinder.playPause();
			}
		} else if (mode == MODE_REPEAT_ONE) {
			play();
		} else {
			next();
		}
	}

	//---上一首操作-----------------------------

	private void previous() {
		int size = getSize();
		if (size > 0) {
			isCommandPrevious = true;
			if (mode == MODE_RANDOM) {
				if (lastPage == page) {
					if (positionList.size() > 1) {
						positionList.remove(positionList.size() - 1);
						position = positionList.get(positionList.size() - 1);
					} else {
						position = (int) (Math.random() * size);
					}
				} else {
					positionList.clear();
					position = (int) (Math.random() * size);
				}
			} else {
				if (position == 0) {
					position = size - 1;
				} else {
					position--;
				}
			}
			startServiceCommand();
		}
	}

	//----下一首操作----------------------
	 
	private void next() {
		int size = getSize();
		if (size > 0) {
			if (mode == MODE_RANDOM) {
				position = (int) (Math.random() * size);
			} else {
				if (position == size - 1) {
					position = 0;
				} else {
					position++;
				}
			}
			startServiceCommand();
		}
	}

	//----快退-----------------------------
	 
	private void rewind() {
		int current = mp3Current - 1000;
		mp3Current = current > 0 ? current : 0;
		mBinder.playUpdate(mp3Current);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_REWIND, 100);
	}

	//----快进-----------------------------
	 
	private void forward() {
		int current = mp3Current + 1000;
		mp3Current = current < mp3Duration ? current : mp3Duration;
		mBinder.playUpdate(mp3Current);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_FORWARD, 100);
	}

	//----用于快退、快进后的继续播放---------------------
	 
	private void replay() {
		if (mHandler.hasMessages(MEDIA_PLAY_REWIND)) {
			mHandler.removeMessages(MEDIA_PLAY_REWIND);
		}
		if (mHandler.hasMessages(MEDIA_PLAY_FORWARD)) {
			mHandler.removeMessages(MEDIA_PLAY_FORWARD);
		}
		mediaPlayer.seekTo(mp3Current);
		mHandler.sendEmptyMessage(MEDIA_PLAY_UPDATE);
		if (lyricView != null && hasLyric) {
			lyricView.setSentenceEntities(lyricList);
			mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC,
					UPDATE_LYRIC_TIME);//----通知刷新歌词
		}
	}

	//----获得列表歌曲数量--------------------------------
	
	private int getSize() {
		int size = 0;
		switch (page) {
		case MainActivity.SLIDING_MENU_ALL:
			size = MusicList.list.size();
			break;

		case MainActivity.SLIDING_MENU_FAVORITE:
			size = FavoriteList.list.size();
			break;

		case MainActivity.SLIDING_MENU_FOLDER_LIST:
			size = FolderList.list.get(folderPosition).getMusicList().size();
			break;
		}
		return size;
	}

	 // ----内部模拟生成启动服务的命令
	private void startServiceCommand() {
		if(positionList.size() > 15){
			positionList.clear();
		}
		
		Intent intent2 = new Intent(getApplicationContext(), MediaService.class);
		intent2.putExtra(INTENT_LIST_PAGE, page);
		intent2.putExtra(INTENT_LIST_POSITION, position);
		startService(intent2);
	}

	//----初始化媒体播放器------------------------------
	
	private void initMedia() {
		try {
			removeAllMsg();//----对于重新播放需要移除所有消息
			mediaPlayer.reset();
			mediaPlayer.setDataSource(mp3Path);
			mediaPlayer.prepareAsync();
			stopForeground(true);
		} catch (Exception e) {
	
			e.printStackTrace();
		}
	}

	//----初始化歌词--------------------------------

	private void initLrc() {
		hasLyric = false;
		if (lyricPath != null) {
			try {
				LyricParser parser = new LyricParser(lyricPath);
				lyricList = parser.parser();
				hasLyric = true;
			} catch (Exception e) {
		
				e.printStackTrace();
			}
		} else {
			if (lyricView != null) {
				lyricView.clear();
			}
		}
	}

	//----准备好开始播放工作----------------------------
	
	private void prepared() {
		mHandler.sendEmptyMessage(MEDIA_PLAY_START);//----通知歌曲已播放
		if (lyricView != null) {
			if (hasLyric) {
				lyricView.setSentenceEntities(lyricList);
				mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC,
						UPDATE_LYRIC_TIME);//----通知刷新歌词
			}
		}
	}

	//----开始播放，获得总时间和AudioSessionId，并启动更新UI任务-------------

	private void start() {
		mp3Duration = mediaPlayer.getDuration();
		info.setMp3Duration(mp3Duration);
		info.setAudioSessionId(mediaPlayer.getAudioSessionId());
		CoverList.cover = albumUtil.scanAlbumImage(info.getPath());
		mBinder.playStart(info);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE, UPDATE_UI_TIME);

		final String artist = info.getArtist();
		final String name = info.getName();
		notification.tickerText = artist + " - " + name;
		if (CoverList.cover == null) {
			remoteViews.setImageViewResource(R.id.notification_item_album,
					R.drawable.default_ablum_bg);
		} else {
			remoteViews.setImageViewBitmap(R.id.notification_item_album,
					CoverList.cover);
		}
		remoteViews.setTextViewText(R.id.notification_item_name, name);
		remoteViews.setTextViewText(R.id.notification_item_artist, artist);
		notification.contentView = remoteViews;
		startForeground(1, notification);//----id设为0将不会显示Notification
	}

	private void update() {
		
		mp3Current = mediaPlayer.getCurrentPosition();
		mBinder.playUpdate(mp3Current);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE, UPDATE_UI_TIME);
	}

	//----暂停音乐-------------------------
	
	private void pause() {
		removeAllMsg();//----移除所有消息
		mediaPlayer.pause();
		mBinder.playPause();
	}

	//----移除更新UI的消息-------------------

	private void removeUpdateMsg() {
		if (mHandler != null && mHandler.hasMessages(MEDIA_PLAY_UPDATE)) {
			mHandler.removeMessages(MEDIA_PLAY_UPDATE);
		}
	}

	//----播放完成-------------------------

	private void complete() {
		
		mBinder.playComplete();
		mBinder.playUpdate(mp3Duration);
		autoPlay();
	}

	//----播放出错------------------------

	private void error() {
		mBinder.playError();
		mBinder.playPause();
		positionList.clear();
	}

	//----刷新歌词-----------------------

	private void updateLrcView() {
		if (lyricList.size() > 0) {
			lyricView.setIndex(getLrcIndex(mediaPlayer.getCurrentPosition(),
					mp3Duration));
			lyricView.invalidate();
			mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC,
					UPDATE_LYRIC_TIME);
		}
	}

	//----移除更新歌词的消息-----------------

	private void removeUpdateLrcViewMsg() {
		if (mHandler != null && mHandler.hasMessages(MEDIA_PLAY_UPDATE_LYRIC)) {
			mHandler.removeMessages(MEDIA_PLAY_UPDATE_LYRIC);
		}
	}

	//----移除所有消息------------------------------

	private void removeAllMsg() {
		removeUpdateMsg();
		removeUpdateLrcViewMsg();
	}



	//----歌词同步处理-----------------------------

	private int[] getLrcIndex(int currentTime, int duration) {
		int index = 0;
		int size = lyricList.size();
		if (currentTime < duration) {
			for (int i = 0; i < size; i++) {
				if (i < size - 1) {
					if (currentTime < lyricList.get(i).getTime() && i == 0) {
						index = i;
					}
					if (currentTime > lyricList.get(i).getTime()
							&& currentTime < lyricList.get(i + 1).getTime()) {
						index = i;
					}
				}
				if (i == size - 1 && currentTime > lyricList.get(i).getTime()) {
					index = i;
				}
			}
		}
		int temp1 = lyricList.get(index).getTime();
		int temp2 = (index == (size - 1)) ? 0 : lyricList.get(index + 1)
				.getTime() - temp1;
		return new int[] { index, currentTime, temp1, temp2 };
	}

	private class ServicePhoneStateListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			
			if (state == TelephonyManager.CALL_STATE_RINGING
					&& mediaPlayer != null && mediaPlayer.isPlaying()) { //----来电
				pause();
			}
		}
	}

	private class ServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent != null) {
				if (intent.getAction().equals(BROADCAST_SLEEP_CLOSE)) {
					pause();
				}
				if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
					//----headphone plugged  
	                 if(intent.getIntExtra("state", 0) == 1){
//	                	 if (!mediaPlayer.isPlaying()) {
//	                		 mediaPlayer.start();
//							 prepared();
//		 				 } 
	                 }else{  
	              //----headphone unplugged
	                	 if (mediaPlayer.isPlaying()) {
	 						pause();
	 				     } 
	                 }  
				}
				if (intent.getAction().equals(INTENT_LIST_POSITION)) {
					position = 0;
				}
				 if (intent.getAction().equals(BROADCAST_NOTIFICATION_ITEM_BUTTON_PLAY)) {//----通知栏播放按钮响应事件
					 if (mediaPlayer.isPlaying()) {
							pause();
						} else {
							if (mp3Path != null) {
								mediaPlayer.start();
								prepared();
							} else {//----无指定情况下播放全部歌曲列表的第一首
								startServiceCommand();
							}
						}
				 }
				 
				 
				 if (intent.getAction().equals(BROADCAST_NOTIFICATION_ITEM_BUTTON_NEXT)) {//----通知栏下一首按钮响应事件
					 next();
					 intent = new Intent(MainActivity.BROADCAST_PLAYER_BUTTON_NEXT);
					 sendBroadcast(intent);
				 }
			}
		}
	}

	private static class ServiceHandler extends Handler {

		private WeakReference<MediaService> reference;

		public ServiceHandler(MediaService service) {
			
			reference = new WeakReference<MediaService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			
			if (reference.get() != null) {
				MediaService theService = reference.get();
				switch (msg.what) {
				case MEDIA_PLAY_START:
					theService.start();//----播放开始
					break;

				case MEDIA_PLAY_UPDATE:
					theService.update();//----更新UI
					break;

				case MEDIA_PLAY_COMPLETE:
					theService.complete();//----播放完成
					break;

				case MEDIA_PLAY_ERROR:
					theService.error();//----播放出错
					break;

				case MEDIA_PLAY_UPDATE_LYRIC:
					theService.updateLrcView();//----刷新歌词
					break;

				case MEDIA_PLAY_REWIND:
					theService.rewind();//----快退线程
					break;

				case MEDIA_PLAY_FORWARD:
					theService.forward();//----快进线程
					break;

				}
			}
		}
	}

}

