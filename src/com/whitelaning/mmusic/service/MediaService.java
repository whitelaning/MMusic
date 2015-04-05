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
 * ���Ʋ��ŷ���
 */
public class MediaService extends Service {

	public static final int CONTROL_COMMAND_PLAY = 0;//----����������Ż�����ͣ
	public static final int CONTROL_COMMAND_PREVIOUS = 1;//----���������һ��
	public static final int CONTROL_COMMAND_NEXT = 2;//----���������һ��
	public static final int CONTROL_COMMAND_MODE = 3;//----�����������ģʽ�л�
	public static final int CONTROL_COMMAND_REWIND = 4;//----�����������
	public static final int CONTROL_COMMAND_FORWARD = 5;//----����������
	public static final int CONTROL_COMMAND_REPLAY = 6;//----����������ڿ��ˡ������ļ�������

	public static final int ACTIVITY_SCAN = 0x101;//----ɨ�����
	public static final int ACTIVITY_MAIN = 0x102;//----������
	public static final int ACTIVITY_PLAYER = 0x103;//----���Ž���
	public static final int ACTIVITY_SETTING = 0x104;//----���ý���

	public static final String INTENT_ACTIVITY = "activity";//----���������ĸ�����
	public static final String INTENT_LIST_PAGE = "list_page";//----�б�ҳ��
	public static final String INTENT_LIST_POSITION = "list_position";//----�б�ǰ��
	public static final String INTENT_FOLDER_POSITION = "folder_position";//----�ļ����б�ǰ��
	
	public static final String BROADCAST_ACTION_SERVICE = 
			"com.whitelaning.mmusic.action.service";//----�㲥��־
	public static final String BROADCAST_NOTIFICATION_ITEM_BUTTON_PLAY = 
			"com.whitelaning.mmusic.notification.play";//----֪ͨ�����Ű�ť
	public static final String BROADCAST_NOTIFICATION_ITEM_BUTTON_NEXT = 
			"com.whitelaning.mmusic.notification.next";//----֪ͨ����һ�װ�ť
	public static final String BROADCAST_SLEEP_CLOSE = 
			"com.whitelaning.mmusic.sleep.close"; //----˯�߽����㲥

	private static final int MEDIA_PLAY_ERROR = 0;
	private static final int MEDIA_PLAY_START = 1;
	private static final int MEDIA_PLAY_UPDATE = 2;
	private static final int MEDIA_PLAY_COMPLETE = 3;
	private static final int MEDIA_PLAY_UPDATE_LYRIC = 4;
	private static final int MEDIA_PLAY_REWIND = 5;
	private static final int MEDIA_PLAY_FORWARD = 6;


	private final int MODE_NORMAL = 0;//----˳�򲥷ţ��ŵ����һ��ֹͣ
	private final int MODE_REPEAT_ONE = 1;//----����ѭ��
	private final int MODE_REPEAT_ALL = 2;//----ȫ��ѭ��
	private final int MODE_RANDOM = 3;//----�漴����
	private final int UPDATE_LYRIC_TIME = 150;//----��ʸ��¼��0.15��
	private final int UPDATE_UI_TIME = 1000;//----UI���¼��1��

	private MusicInfo info;//----��������
	private List<LyricItem> lyricList;//----����б�
	private List<Integer> positionList;//----�б�ǰ��ϣ�Ŀ�ļ�סǰ�������ŵ����и���

	private String mp3Path;//----mp3�ļ�·��
	private String lyricPath;//----����ļ�·��

	private int mode = MODE_NORMAL;//----����ģʽ(Ĭ��˳�򲥷�)
	private int page = MainActivity.SLIDING_MENU_ALL;//----�б�ҳ��(Ĭ��ȫ������)
	private int lastPage = 0;//----��ס��һ�ε��б�ҳ��
	private int position = 0;//----�б�ǰ��
	private int folderPosition = 0;//----�ļ����б�ǰ��
	private int mp3Current = 0;//----������ǰʱ��
	private int mp3Duration = 0;//----������ʱ��


	private boolean hasLyric = false;//----�Ƿ��и��
	private boolean isCommandPrevious = false;//----�Ƿ�������һ�ײ�������

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
				mp3Current = 0;//----����
				prepared();//----׼������
			}
		});
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				
				removeAllMsg();//----�Ƴ�������Ϣ
				mHandler.sendEmptyMessage(MEDIA_PLAY_COMPLETE);
			}
		});
		mediaPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
			
				removeAllMsg();//----�Ƴ�������Ϣ
				mp.reset();
				page = MainActivity.SLIDING_MENU_ALL;
				position = 0;
				File file = new File(mp3Path);
				if (file.exists()) {
					Toast.makeText(getApplicationContext(), "���ų���",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "�ļ��Ѳ�����",
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
				MediaService.this.lyricView = lyricView;//----��ø����ͼ
			}

			@Override
			public void control(int command) {
				
				switch (command) {
				case CONTROL_COMMAND_PLAY://----��������ͣ
					if (mediaPlayer.isPlaying()) {
						pause();
					} else {
						if (mp3Path != null) {
							mediaPlayer.start();
							prepared();
						} else {//----��ָ������²���ȫ�������б�ĵ�һ��
							startServiceCommand();
							
						}
					}
					break;

				case CONTROL_COMMAND_PREVIOUS://----��һ��
					previous();
					break;

				case CONTROL_COMMAND_NEXT://----��һ��
					next();
					break;

				case CONTROL_COMMAND_MODE://----����ģʽ
					if (mode < MODE_RANDOM) {
						mode++;
					} else {
						mode = MODE_NORMAL;
					}
					switch (mode) {
					case MODE_NORMAL:
						Toast.makeText(getApplicationContext(), "˳�򲥷�",
								Toast.LENGTH_SHORT).show();
						break;

					case MODE_REPEAT_ONE:
						Toast.makeText(getApplicationContext(), "����ѭ��",
								Toast.LENGTH_SHORT).show();
						break;

					case MODE_REPEAT_ALL:
						Toast.makeText(getApplicationContext(), "ȫ��ѭ��",
								Toast.LENGTH_SHORT).show();
						break;

					case MODE_RANDOM:
						Toast.makeText(getApplicationContext(), "�������",
								Toast.LENGTH_SHORT).show();
						break;
					}
					mBinder.modeChange(mode);
					break;

				case CONTROL_COMMAND_REWIND://----����
					if (mediaPlayer.isPlaying()) {
						removeAllMsg();
						rewind();
					}
					break;

				case CONTROL_COMMAND_FORWARD://----���
					if (mediaPlayer.isPlaying()) {
						removeAllMsg();
						forward();
					}
					break;

				case CONTROL_COMMAND_REPLAY://----���ڿ��ˡ������ļ�������
					if (mediaPlayer.isPlaying()) {
						replay();
					}
					break;
				}
			}
		});
		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		mode = preferences.getInt(MainActivity.PREFERENCES_MODE, MODE_NORMAL);//----ȡ���ϴεĲ���ģʽ

		notification = new Notification();//----֪ͨ�����
		notification.icon = R.drawable.ic_launcher;
		notification.flags = Notification.FLAG_NO_CLEAR;
		notification.contentIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, new Intent(getApplicationContext(),
						MainActivity.class), 0);
		remoteViews = new RemoteViews(getPackageName(),
				R.layout.notification_item);

		Intent buttonPlayIntent = new Intent(BROADCAST_NOTIFICATION_ITEM_BUTTON_PLAY); //----����֪ͨ����ť�㲥
        PendingIntent pendButtonPlayIntent = PendingIntent.getBroadcast(this, 0, buttonPlayIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.notification_item_btn_pause, pendButtonPlayIntent);//----���ö�Ӧ�İ�ťID���
      
		Intent buttonNextIntent = new Intent(BROADCAST_NOTIFICATION_ITEM_BUTTON_NEXT);
		PendingIntent pendButtonNextIntent = PendingIntent.getBroadcast(this, 0, buttonNextIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.notification_item_btn_next, pendButtonNextIntent);

		receiver = new ServiceReceiver();//----ע��㲥
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BROADCAST_NOTIFICATION_ITEM_BUTTON_PLAY);//---֪ͨ����ť�㲥
		intentFilter.addAction(BROADCAST_NOTIFICATION_ITEM_BUTTON_NEXT);
		intentFilter.addAction(Intent.ACTION_HEADSET_PLUG); //----��������״̬�㲥
		intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
		intentFilter.addAction(BROADCAST_ACTION_SERVICE);
		intentFilter.addAction(BROADCAST_SLEEP_CLOSE);
		intentFilter.addAction(INTENT_LIST_POSITION);
		registerReceiver(receiver, intentFilter);

		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);//----��ȡ�绰ͨѶ����
		telephonyManager.listen(new ServicePhoneStateListener(),
				PhoneStateListener.LISTEN_CALL_STATE);//----����һ���������󣬼����绰״̬�ı��¼�
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
		preferences.edit().putInt(MainActivity.PREFERENCES_MODE, mode).commit();//----�����ϴεĲ���ģʽ
	}

	@Override
	public IBinder onBind(Intent intent) {

		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {

		lyricView = null;
		removeAllMsg();//----�Ƴ�������Ϣ
		return true;//----һ������true������ִ��onRebind
	}

	@Override
	public void onRebind(Intent intent) {
	
		super.onRebind(intent);
		if (mediaPlayer.isPlaying()) {//----������ڲ������°󶨷����ʱ������ע��
			prepared();//----��Ϊ��Ϣ�Ѿ��Ƴ���������Ҫ���¿������²���
		} else {
			if (mp3Path != null) {//----��ͣԭ�Ȳ������¿�ҳ����Ҫ�ָ�ԭ�ȵ�״̬
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

	//----���Ų���----------------------------------------

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
				initMedia();//----�ȳ�ʼ������
				initLrc();//----�ٳ�ʼ�����
			}
			lastPage = page;
			if (!isCommandPrevious) {
				positionList.add(position);
			}
			isCommandPrevious = false;
		}
	}

	//----�Զ����Ų���----------------------------

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

	//---��һ�ײ���-----------------------------

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

	//----��һ�ײ���----------------------
	 
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

	//----����-----------------------------
	 
	private void rewind() {
		int current = mp3Current - 1000;
		mp3Current = current > 0 ? current : 0;
		mBinder.playUpdate(mp3Current);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_REWIND, 100);
	}

	//----���-----------------------------
	 
	private void forward() {
		int current = mp3Current + 1000;
		mp3Current = current < mp3Duration ? current : mp3Duration;
		mBinder.playUpdate(mp3Current);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_FORWARD, 100);
	}

	//----���ڿ��ˡ������ļ�������---------------------
	 
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
					UPDATE_LYRIC_TIME);//----֪ͨˢ�¸��
		}
	}

	//----����б��������--------------------------------
	
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

	 // ----�ڲ�ģ�������������������
	private void startServiceCommand() {
		if(positionList.size() > 15){
			positionList.clear();
		}
		
		Intent intent2 = new Intent(getApplicationContext(), MediaService.class);
		intent2.putExtra(INTENT_LIST_PAGE, page);
		intent2.putExtra(INTENT_LIST_POSITION, position);
		startService(intent2);
	}

	//----��ʼ��ý�岥����------------------------------
	
	private void initMedia() {
		try {
			removeAllMsg();//----�������²�����Ҫ�Ƴ�������Ϣ
			mediaPlayer.reset();
			mediaPlayer.setDataSource(mp3Path);
			mediaPlayer.prepareAsync();
			stopForeground(true);
		} catch (Exception e) {
	
			e.printStackTrace();
		}
	}

	//----��ʼ�����--------------------------------

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

	//----׼���ÿ�ʼ���Ź���----------------------------
	
	private void prepared() {
		mHandler.sendEmptyMessage(MEDIA_PLAY_START);//----֪ͨ�����Ѳ���
		if (lyricView != null) {
			if (hasLyric) {
				lyricView.setSentenceEntities(lyricList);
				mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC,
						UPDATE_LYRIC_TIME);//----֪ͨˢ�¸��
			}
		}
	}

	//----��ʼ���ţ������ʱ���AudioSessionId������������UI����-------------

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
		startForeground(1, notification);//----id��Ϊ0��������ʾNotification
	}

	private void update() {
		
		mp3Current = mediaPlayer.getCurrentPosition();
		mBinder.playUpdate(mp3Current);
		mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE, UPDATE_UI_TIME);
	}

	//----��ͣ����-------------------------
	
	private void pause() {
		removeAllMsg();//----�Ƴ�������Ϣ
		mediaPlayer.pause();
		mBinder.playPause();
	}

	//----�Ƴ�����UI����Ϣ-------------------

	private void removeUpdateMsg() {
		if (mHandler != null && mHandler.hasMessages(MEDIA_PLAY_UPDATE)) {
			mHandler.removeMessages(MEDIA_PLAY_UPDATE);
		}
	}

	//----�������-------------------------

	private void complete() {
		
		mBinder.playComplete();
		mBinder.playUpdate(mp3Duration);
		autoPlay();
	}

	//----���ų���------------------------

	private void error() {
		mBinder.playError();
		mBinder.playPause();
		positionList.clear();
	}

	//----ˢ�¸��-----------------------

	private void updateLrcView() {
		if (lyricList.size() > 0) {
			lyricView.setIndex(getLrcIndex(mediaPlayer.getCurrentPosition(),
					mp3Duration));
			lyricView.invalidate();
			mHandler.sendEmptyMessageDelayed(MEDIA_PLAY_UPDATE_LYRIC,
					UPDATE_LYRIC_TIME);
		}
	}

	//----�Ƴ����¸�ʵ���Ϣ-----------------

	private void removeUpdateLrcViewMsg() {
		if (mHandler != null && mHandler.hasMessages(MEDIA_PLAY_UPDATE_LYRIC)) {
			mHandler.removeMessages(MEDIA_PLAY_UPDATE_LYRIC);
		}
	}

	//----�Ƴ�������Ϣ------------------------------

	private void removeAllMsg() {
		removeUpdateMsg();
		removeUpdateLrcViewMsg();
	}



	//----���ͬ������-----------------------------

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
					&& mediaPlayer != null && mediaPlayer.isPlaying()) { //----����
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
				 if (intent.getAction().equals(BROADCAST_NOTIFICATION_ITEM_BUTTON_PLAY)) {//----֪ͨ�����Ű�ť��Ӧ�¼�
					 if (mediaPlayer.isPlaying()) {
							pause();
						} else {
							if (mp3Path != null) {
								mediaPlayer.start();
								prepared();
							} else {//----��ָ������²���ȫ�������б�ĵ�һ��
								startServiceCommand();
							}
						}
				 }
				 
				 
				 if (intent.getAction().equals(BROADCAST_NOTIFICATION_ITEM_BUTTON_NEXT)) {//----֪ͨ����һ�װ�ť��Ӧ�¼�
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
					theService.start();//----���ſ�ʼ
					break;

				case MEDIA_PLAY_UPDATE:
					theService.update();//----����UI
					break;

				case MEDIA_PLAY_COMPLETE:
					theService.complete();//----�������
					break;

				case MEDIA_PLAY_ERROR:
					theService.error();//----���ų���
					break;

				case MEDIA_PLAY_UPDATE_LYRIC:
					theService.updateLrcView();//----ˢ�¸��
					break;

				case MEDIA_PLAY_REWIND:
					theService.rewind();//----�����߳�
					break;

				case MEDIA_PLAY_FORWARD:
					theService.forward();//----����߳�
					break;

				}
			}
		}
	}

}

