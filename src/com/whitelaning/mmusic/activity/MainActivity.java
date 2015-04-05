package com.whitelaning.mmusic.activity;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.whitelaning.mmusic.R;
import com.whitelaning.mmusic.Binder.MediaBinder;
import com.whitelaning.mmusic.Binder.MediaBinder.OnPlayCompleteListener;
import com.whitelaning.mmusic.Binder.MediaBinder.OnPlayErrorListener;
import com.whitelaning.mmusic.Binder.MediaBinder.OnPlayPauseListener;
import com.whitelaning.mmusic.Binder.MediaBinder.OnPlayStartListener;
import com.whitelaning.mmusic.Binder.MediaBinder.OnPlayingListener;
import com.whitelaning.mmusic.adapter.MusicAdapter;
import com.whitelaning.mmusic.adapter.SlidingAdapter;
import com.whitelaning.mmusic.db.DBDao;
import com.whitelaning.mmusic.dialog.AboutDialog;
import com.whitelaning.mmusic.dialog.DeleteDialog;
import com.whitelaning.mmusic.dialog.InfoDialog;
import com.whitelaning.mmusic.dialog.ScanDialog;
import com.whitelaning.mmusic.dialog.TVAnimDialog.OnTVAnimDialogDismissListener;
import com.whitelaning.mmusic.entity.MusicInfo;
import com.whitelaning.mmusic.list.CoverList;
import com.whitelaning.mmusic.list.FavoriteList;
import com.whitelaning.mmusic.list.FolderList;
import com.whitelaning.mmusic.list.MusicList;
import com.whitelaning.mmusic.service.MediaService;
import com.whitelaning.mmusic.service.SleepService;
import com.whitelaning.mmusic.slidingmenu.SlidingListActivity;
import com.whitelaning.mmusic.slidingmenu.SlidingMenu;
import com.whitelaning.mmusic.util.FormatUtil;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "NewApi", "ClickableViewAccessibility" })
public class MainActivity extends SlidingListActivity implements
		OnClickListener, OnLongClickListener, OnTouchListener,
		OnTVAnimDialogDismissListener {

	// ----0~6��ӦSlidingAdapter��position
	public static final int SLIDING_MENU_SCAN = 0;// ----�໬-ɨ�����
	public static final int SLIDING_MENU_ALL = 1;// ----�໬-ȫ������
	public static final int SLIDING_MENU_FAVORITE = 2;// ----�໬-�ҵ��
	public static final int SLIDING_MENU_FOLDER = 3;// -----�໬-�ļ���

	public static final int SLIDING_MENU_SETTING = 4;// ----�໬-����
	public static final int SLIDING_MENU_ABOUT = 5;// ----�໬-����
	public static final int SLIDING_MENU_EXIT = 6;// ----�໬-�˳�����
	public static final int SLIDING_MENU_FOLDER_LIST = 4;// ----�໬-�ļ���-�ļ����б�

	public static final int DIALOG_DISMISS = 0;// ----�Ի�����ʧ
	public static final int DIALOG_SCAN = 1;// ----ɨ��Ի���
	public static final int DIALOG_MENU_REMOVE = 2;// ----�����б��Ƴ��Ի���
	public static final int DIALOG_MENU_DELETE = 3;// ----�����б���ʾɾ���Ի���
	public static final int DIALOG_MENU_INFO = 4;// ----��������Ի���
	public static final int DIALOG_DELETE = 5;// ----����ɾ���Ի���
	public static final int DIALOG_CLEAR = 6;// ----���ȫ�������б�

	public static final String PREFERENCES_NAME = "settings";// ----SharedPreferences����
	public static final String PREFERENCES_MODE = "mode";// ----�洢����ģʽ
	public static final String PREFERENCES_SCAN = "scan";// ----�洢�Ƿ�ɨ���

	public static final String BROADCAST_PLAYER_BUTTON_PREVIOUS = "com.whitelaning.mmusic.actionplayer.previous";// ----���Ž�����һ�ױ��
	public static final String BROADCAST_PLAYER_BUTTON_NEXT = "com.whitelaning.mmusic.actionplayer.next";// ----���Ž�����һ�ױ��
	public static final String BROADCAST_ACTION_MENU_REMOVE = "com.whitelaning.mmusic.action.menu.remove";// ----listview�˵�����¼�--�Ƴ�
	public static final String BROADCAST_ACTION_MENU_DELETE = "com.whitelaning.mmusic.action.menu.delete";// ----listview�˵�����¼�--ɾ��
	public static final String BROADCAST_ACTION_MENU_INFO = "com.whitelaning.mmusic.action.menu.info";// ----listview�˵�����¼�--��Ϣ
	public static final String BROADCAST_ACTION_SCAN = "com.whitelaning.mmusic.action.scan";// ----ɨ��㲥��־
	public static final String BROADCAST_ACTION_MENU = "com.whitelaning.mmusic.action.menu";// ----�����˵��㲥��־
	public static final String BROADCAST_ACTION_FAVORITE = "com.whitelaning.mmusic.action.favorite";// ----ϲ���㲥��־
	public static final String BROADCAST_ACTION_EXIT = "com.whitelaning.mmusic.action.exit";// ----�˳�����㲥��־
	public static final String BROADCAST_INTENT_PAGE = "com.whitelaning.mmusic.intent.page";// ---ҳ��״̬
	public static final String BROADCAST_INTENT_POSITION = "com.whitelaning.mmusic.intent.position";// ----��������
	public static Boolean isExit = false;
	
	private String TITLE_ALL;
	private String TITLE_FAVORITE;
	private String TITLE_FOLDER;
	private String TIME_NORMAL;

	private int slidingPage = SLIDING_MENU_ALL;// ----ҳ��״̬
	private int playerPage;// ----���͸�PlayerActivity��ҳ��״̬
	private int musicPosition;// ----��ǰ���Ÿ�������
	private int folderPosition;// ----�ļ����б�����
	private int dialogMenuPosition;// ----��ס���������б�˵��ĸ�������
	private boolean canSkip = true;// ----��ֹ�û�Ƶ�������ɶ�ν������󶨣�true��������
	private boolean bindState = false;// ----�����״̬

	private String mp3Current;// ----������ǰʱ��
	private String mp3Duration;// ----������ʱ��
	private String dialogMenuPath;// ----��ס���������б�˵��ĸ���·��

	private TextView mainTitle;// ----�б����
	private TextView mainSize;// ----��������
	private TextView mainArtist;// ----������
	private TextView mainName;// ----��������
	private TextView mainTime;// ----����ʱ��
	private ImageView mainAlbum;// ----ר��ͼƬ
	private ImageView btnShowSliding;

	private ImageButton btnPlay;// ----���ź���ͣ��ť
	private ImageButton btnNext;// ----��һ�װ�ť
	private Animation myAnimation_Translate_alpha; // ----�����Ƴ�����

	private LinearLayout viewBack;// ----������һ��
	private LinearLayout viewControl;// ----�ײ����ſ�����ͼ

	private Animation myAnimation_rotate;// ----��ת����
	private String nowPlayingMusicPath = null; //----��¼���ڲ��ŵ����ֵ�ַ

	private Intent playIntent, sleepIntent;
	private MediaBinder binder;
	private MainReceiver receiver;
	private SlidingMenu slidingMenu;
	private MusicAdapter musicAdapter;
	private SharedPreferences preferences;
	private ServiceConnection serviceConnection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();// ----��ʼ��
	}

	@Override
	protected void onResume() {
		// ----���͹㲥����������
		Intent intent = new Intent(MediaService.BROADCAST_ACTION_SERVICE);
		intent.putExtra(MediaService.INTENT_ACTIVITY,
				MediaService.ACTIVITY_MAIN);
		sendBroadcast(intent);

		// ----�󶨷���
		bindState = bindService(playIntent, serviceConnection,
				Context.BIND_AUTO_CREATE);

		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bindState)
			unbindService(serviceConnection);

		serviceConnection = null;
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}

	/**
	 * ��ʼ��������ع���
	 */
	private void init() {
		initSlidingMenu();// ----ʼ���໬��
		initActivity();// ----��ʼ��������
		initServiceConnection();// ----���ʼ�������
	}

	/**
	 * ��ʼ���໬��� <----����SlidingMenu�ļ�������ģʽ---->
	 * TOUCHMODE_FULLSCREEN��ȫ��ģʽ����contentҳ���У����������Դ�SlidingMenu
	 * TOUCHMODE_MARGIN����Եģʽ����contentҳ���У���Ҫ����Ļ��Ե�����ſ��Դ�SlidingMenu
	 * TOUCHMODE_NONE������ͨ�����ƴ�
	 */
	private void initSlidingMenu() {
		setBehindContentView(R.layout.activity_main_sliding);
		slidingMenu = getSlidingMenu();
		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		slidingMenu.setShadowWidth(20);

		// ----�໬��listView����
		ListView listView = (ListView) slidingMenu.getMenu().findViewById(
				R.id.activity_main_sliding_list);// ----activity_man_sliding.xml�ж���slidingMenu�Ĳ����ļ�
		listView.setAdapter(new SlidingAdapter(getApplicationContext()));

		// ----�໬��Item����¼�
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// ----�ж��Ƿ���Item������ⲿ�����ز໬��
				if (viewBack.getVisibility() != View.GONE) {
					viewBack.setVisibility(View.GONE);
				}

				switch (position) {

				case SLIDING_MENU_SCAN:// ----ɨ�����
					intentScanActivity();
					break;

				case SLIDING_MENU_ALL:// ----ȫ������
					if (musicAdapter.getPage() != SLIDING_MENU_ALL) { // ----�ж��Ƿ��Ѿ���ȫ����������
						mainTitle.setText(TITLE_ALL);
						musicAdapter.update(SLIDING_MENU_ALL);
						mainSize.setText(musicAdapter.getCount() + "");
					}
					break;

				case SLIDING_MENU_FAVORITE:// ----�ҵ��
					if (musicAdapter.getPage() != SLIDING_MENU_FAVORITE) {
						mainTitle.setText(TITLE_FAVORITE);
						musicAdapter.update(SLIDING_MENU_FAVORITE);
						mainSize.setText(musicAdapter.getCount() + "");
					}
					break;

				case SLIDING_MENU_FOLDER:// ----�ļ���
					if (musicAdapter.getPage() != SLIDING_MENU_FOLDER) {
						mainTitle.setText(TITLE_FOLDER);
						musicAdapter.update(SLIDING_MENU_FOLDER);
						mainSize.setText(musicAdapter.getCount() + "");
					}
					break;

				case SLIDING_MENU_SETTING:// ----���ã�ֻ�����߹��ܣ�����ֱ�ӵ��ã�
					Intent intent = new Intent(getApplicationContext(),
							SleepActivity.class);
					startActivity(intent);
					break;

				case SLIDING_MENU_ABOUT:// ----����
					showAboutDialog();
					break;

				case SLIDING_MENU_EXIT:// ----�˳�����
					exitProgram();
					break;
				}
				toggle();// ----�رղ໬�˵�
			}
		});
	}

	/**
	 * �������ڶԻ���
	 */
	private void showAboutDialog() {
		AboutDialog aboutDialog = new AboutDialog(this);
		aboutDialog.show();
	}

	/**
	 * ��ʼ�����������
	 */
	private void initActivity() {
		TITLE_ALL = this.getString(R.string.all_music);
		TITLE_FAVORITE = this.getString(R.string.my_best_love);
		TITLE_FOLDER = this.getString(R.string.folder);
		TIME_NORMAL = "00:00";

		// ----�󶨿ؼ�
		mainTitle = (TextView) this.findViewById(R.id.activity_main_tv_title);
		mainSize = (TextView) this.findViewById(R.id.activity_main_tv_count);
		mainArtist = (TextView) findViewById(R.id.activity_main_tv_artist);
		mainName = (TextView) findViewById(R.id.activity_main_tv_name);
		mainTime = (TextView) findViewById(R.id.activity_main_tv_time);
		mainAlbum = (ImageView) findViewById(R.id.activity_main_iv_album);
		viewBack = (LinearLayout) findViewById(R.id.activity_main_view_back);
		viewControl = (LinearLayout) findViewById(R.id.activity_main_view_bottom);
		btnShowSliding = (ImageView) findViewById(R.id.activity_main_show_sliding_btn);
		btnPlay = (ImageButton) findViewById(R.id.activity_main_ib_play);
		btnNext = (ImageButton) findViewById(R.id.activity_main_ib_next);
		ListView lv = (ListView) findViewById(android.R.id.list);

		mainTitle.setText(TITLE_ALL);
		mainName.setText("");
		mainTime.setText(TIME_NORMAL + " - " + TIME_NORMAL);

		// ----��ʼ������
		myAnimation_Translate_alpha = AnimationUtils.loadAnimation(this,
				R.anim.translate_alpha);
		myAnimation_rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);

		// ----�󶨵���¼�
		viewBack.setOnClickListener(this);
		viewControl.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		btnShowSliding.setOnClickListener(this);
		musicAdapter = new MusicAdapter(getApplicationContext(),
				SLIDING_MENU_ALL);
		lv.setAdapter(musicAdapter);

		mainSize.setText(musicAdapter.getCount() + "");// ----��ȡ������Ŀ

		playIntent = new Intent(getApplicationContext(), MediaService.class);// ----�󶨷���
		sleepIntent = new Intent(getApplicationContext(), SleepService.class);
		receiver = new MainReceiver();
		// ----ע��㲥
		IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_PLAYER_BUTTON_NEXT);
		filter.addAction(BROADCAST_PLAYER_BUTTON_PREVIOUS);
		filter.addAction(BROADCAST_ACTION_SCAN);
		filter.addAction(BROADCAST_ACTION_MENU);
		filter.addAction(BROADCAST_ACTION_FAVORITE);
		filter.addAction(BROADCAST_ACTION_EXIT);
		filter.addAction(BROADCAST_ACTION_MENU_INFO);
		filter.addAction(BROADCAST_ACTION_MENU_REMOVE);
		filter.addAction(BROADCAST_ACTION_MENU_DELETE);
		registerReceiver(receiver, filter);

		preferences = getSharedPreferences(PREFERENCES_NAME,
				Context.MODE_PRIVATE);// ----����Ƿ�ɨ�������

		if (!preferences.getBoolean(PREFERENCES_SCAN, false)) {
			ScanDialog scanDialog = new ScanDialog(this);
			scanDialog.setDialogId(DIALOG_SCAN);
			scanDialog.setOnTVAnimDialogDismissListener(this);
			scanDialog.show();
		}
	}

	/**
	 * ��ʼ�������
	 */
	private void initServiceConnection() {
		serviceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				binder = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				binder = (MediaBinder) service;
				if (binder != null) {
					canSkip = true;// ----����
					binder.setOnPlayStartListener(new OnPlayStartListener() {

						// ----���Ÿ�����ʼ��
						@Override
						public void onStart(MusicInfo info) {
							btnPlay.setImageResource(R.drawable.part_suspend);

							playerPage = musicAdapter.getPage(); // ----��ȡ���ŵ�slidingѡ�����
							mainArtist.setText(info.getArtist());
							mainName.setText(info.getName());
							mp3Duration = info.getTime();
							
							nowPlayingMusicPath = info.getPath();
							if (mp3Current == null) {
								mainTime.setText(TIME_NORMAL + " - "
										+ mp3Duration);
							} else {
								mainTime.setText(mp3Current + " - "
										+ mp3Duration);
							}
							if (CoverList.cover == null) {
								mainAlbum
										.setImageResource(R.drawable.default_ablum_bg);// ----Ĭ��ͼƬ
							} else {
								mainAlbum.setImageBitmap(CoverList.cover); 
							}
							mainAlbum.startAnimation(myAnimation_rotate);// ----��������
						}
					});

					// ----���ſ�ʼʱ
					binder.setOnPlayingListener(new OnPlayingListener() {

						@Override
						public void onPlay(int currentPosition) {
							mp3Current = FormatUtil.formatTime(currentPosition);
							mainTime.setText(mp3Current + " - " + mp3Duration);
						}
					});

					// ----��ͣʱ
					binder.setOnPlayPauseListener(new OnPlayPauseListener() {

						@Override
						public void onPause() {
							btnPlay.setImageResource(R.drawable.part_play);
							mainAlbum.clearAnimation();
						}
					});

					// ----�������ʱ
					binder.setOnPlayCompletionListener(new OnPlayCompleteListener() {

						@Override
						public void onPlayComplete() {

							mp3Current = null;
							notifySetSelctItemPosition(++musicPosition);
							mainAlbum.clearAnimation();
						}
					});

					// ----���Ŵ���ʱ
					binder.setOnPlayErrorListener(new OnPlayErrorListener() {

						@Override
						public void onPlayError() {

							dialogMenuPosition = musicPosition;
							removeList(dialogMenuPosition);// -----�ļ������ڴ��б��Ƴ�
						}
					});
					binder.setLyricView(null);// ----�޸����ͼ
				}
			}
		};
	}

	/**
	 * ������ϣ��Զ����ڸ�������һ��
	 */
	public void autoAddMusicposition() {
		notifySetSelctItemPosition(++musicPosition);
	}

	/**
	 * ��ת��ɨ��ҳ��
	 */
	private void intentScanActivity() {
		Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_zoomin,
				R.anim.activity_zoomout);
	}

	/**
	 * �ӵ�ǰ�����б����Ƴ�����
	 */
	public void removeList(int dialogMenuPosition) {
		MusicInfo info = null; // ----����մ���
		int size = 0;
		switch (slidingPage) {
		case MainActivity.SLIDING_MENU_ALL:
			size = MusicList.list.size();
			info = MusicList.list.get(dialogMenuPosition);
			break;

		case MainActivity.SLIDING_MENU_FAVORITE:
			size = FavoriteList.list.size();
			info = FavoriteList.list.get(dialogMenuPosition);
			break;

		case MainActivity.SLIDING_MENU_FOLDER_LIST:
			size = FolderList.list.get(folderPosition).getMusicList().size();
			info = FolderList.list.get(folderPosition).getMusicList()
					.get(dialogMenuPosition);
			break;
		}

		if (dialogMenuPath == null) {
			dialogMenuPath = info.getPath(); // ----��סItem��·��
		}

		MusicList.list.remove(info);
		FavoriteList.list.remove(info);

		for (int i = 0; i < FolderList.list.size(); i++) {
			FolderList.list.get(i).getMusicList().remove(info);
		}

		musicAdapter.update(slidingPage);
		mainSize.setText(musicAdapter.getCount() + "");

		DBDao db = new DBDao(getApplicationContext());

		db.delete(dialogMenuPath);// ----�����ݿ���ɾ��
		db.close();// -----�ر�

		if (binder != null && musicPosition == dialogMenuPosition) {
			if (musicPosition == (size - 1)) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PREVIOUS);
			} else {
				playIntent.putExtra(MediaService.INTENT_LIST_PAGE, slidingPage);
				playIntent.putExtra(MediaService.INTENT_LIST_POSITION,
						musicPosition);
				startService(playIntent);// ----�ӵ�ǰposition������
			}
		}
	}

	/**
	 * �ļ���ɾ��
	 */
	private void deleteFile(int dialogMenuPosition) {

		MusicInfo info = null;
		switch (slidingPage) {
		case MainActivity.SLIDING_MENU_ALL:
			info = MusicList.list.get(dialogMenuPosition);
			break;

		case MainActivity.SLIDING_MENU_FAVORITE:
			info = FavoriteList.list.get(dialogMenuPosition);
			break;

		case MainActivity.SLIDING_MENU_FOLDER_LIST:
			info = FolderList.list.get(folderPosition).getMusicList()
					.get(dialogMenuPosition);
			break;
		}

		if (dialogMenuPath == null) {
			dialogMenuPath = info.getPath(); // ----��סItem��·��
		}

		File file = new File(dialogMenuPath);

		if (file.exists() && file.delete()) {
			Toast.makeText(getApplicationContext(), R.string.Toast_delete_ok,
					Toast.LENGTH_LONG).show();
			removeList(dialogMenuPosition);// -----ɾ��������б�
		}
	}

	/**
	 * �˳�����
	 */
	private void exitProgram() {
		stopService(sleepIntent);
		stopService(playIntent);
		finish();
	}

	/**
	 * ����¼�
	 */
	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.activity_main_view_back:// ----������һ������
			viewBack.setVisibility(View.GONE);
			mainTitle.setText(TITLE_FOLDER);
			musicAdapter.update(SLIDING_MENU_FOLDER);
			mainSize.setText(musicAdapter.getCount() + "");
			break;

		case R.id.activity_main_view_bottom:// ----�ײ����ſ�����ͼ����
			if (serviceConnection != null && canSkip) {
				canSkip = false;
				unbindService(serviceConnection);// ----�����
				bindState = false;// ----״̬����
			}

			Intent intent = new Intent(this, PlayerActivity.class);
			intent.putExtra(BROADCAST_INTENT_POSITION, musicPosition);
			startActivity(intent);
			overridePendingTransition(R.anim.activity_zoomin,
					R.anim.activity_zoomout);
			break;

		case R.id.activity_main_ib_play:// ----���Ű�ť����
			if (binder != null) {
				btnPlay.startAnimation(myAnimation_Translate_alpha);
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PLAY);
				notifySetSelctItemPosition(musicPosition);
			}
			break;
		case R.id.activity_main_ib_next:// ----��һ�װ�ť����
			if (binder != null) {
				btnNext.startAnimation(myAnimation_Translate_alpha);
				binder.setControlCommand(MediaService.CONTROL_COMMAND_NEXT);
				if(!(musicAdapter.getCount() == 1)) {
					autoAddMusicposition();
				}
			}
			break;

		case R.id.activity_main_show_sliding_btn:
			toggle(); // ----��ʾ/���� �����
			break;
		}
	}

	/**
	 * �����¼�
	 */
	@Override
	public boolean onLongClick(View v) {
		return true;// ----����onClick�¼�
	}

	/**
	 * �����¼�
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//
		if (binder != null && event.getAction() == MotionEvent.ACTION_UP) {
			binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
		}
		return false;
	}

	/**
	 * Listveiw Item����¼�
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		slidingPage = musicAdapter.getPage();
		playIntent.putExtra(MediaService.INTENT_LIST_PAGE, slidingPage);
		musicPosition = position;

		switch (slidingPage) {
		case SLIDING_MENU_FOLDER:// ----�ļ���
			folderPosition = position;
			viewBack.setVisibility(View.VISIBLE);
			mainTitle.setText(FolderList.list.get(folderPosition)
					.getMusicFolder());
			musicAdapter.setFolderPosition(folderPosition);
			musicAdapter.update(SLIDING_MENU_FOLDER_LIST);
			mainSize.setText(musicAdapter.getCount() + "");
			return;// ----��ִ�в���

		case SLIDING_MENU_FOLDER_LIST:// ----�ļ��и����б�
			playIntent.putExtra(MediaService.INTENT_FOLDER_POSITION,
					folderPosition);
			break;
		}

		playIntent.putExtra(MediaService.INTENT_LIST_POSITION, musicPosition);
		startService(playIntent);
		notifySetSelctItemPosition(musicPosition);

		if (hintMusicPostition) {
			notifyCurrentPosition(musicPosition);
		}

	}

	/**
	 * ֪ͨmusicAdapter�ı�hideLinlayout����ʾλ��
	 */
	private boolean hintMusicPostition = false;

	public void notifyCurrentPosition(int musicPosition) {
		if (hintMusicPostition) {
			musicAdapter.setCurrentPosition(-1);
			musicAdapter.notifyDataSetChanged();
			hintMusicPostition = false;
		} else {
			musicAdapter.setCurrentPosition(musicPosition);
			musicAdapter.notifyDataSetChanged();
			hintMusicPostition = true;
		}
	}

	/**
	 * ֪ͨmusicAdapter�ı佹����ɫ
	 * 
	 * @param musicPostition
	 */
	public void notifySetSelctItemPosition(int musicPostition) {
		musicAdapter.setSelectItem(musicPostition);
		musicAdapter.notifyDataSetChanged();
	}

	private int currentPositionSave = -1;

	/**
	 * Dialog�Ի���رռ���
	 */
	@Override
	public void onDismiss(int dialogId) {

		switch (dialogId) {
		case DIALOG_SCAN:// ----ɨ��ҳ��
			intentScanActivity();
			break;

		case DIALOG_MENU_REMOVE:// ----ִ���Ƴ�
			removeList(dialogMenuPosition);
			break;

		case DIALOG_MENU_DELETE:// ----ɾ���Ի���
			DeleteDialog deleteDialog = new DeleteDialog(this);
			deleteDialog.setOnTVAnimDialogDismissListener(this);
			deleteDialog.show();
			break;

		case DIALOG_DELETE:// ----ȷ��ɾ��
			deleteFile(currentPositionSave);
			break;
		}
	}

	/**
	 * ���������
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitBy2Click();// ----����˫���˳�����
		}
		return false;
	}

	/**
	 * �˵�
	 * 
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_clear:
			showClearDialog();
			break;
		case R.id.menu_exit:
			exitProgram();
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * ���ȷ��dialog
	 * 
	 * @param title
	 */
	private void showClearDialog() {
		new AlertDialog.Builder(this)
				.setTitle(
						getResources().getString(
								R.string.xml_dialog_clear_title))
				.setMessage(
						getResources().getString(
								R.string.xml_dialog_clear_message))
				.setPositiveButton(
						getResources().getString(
								R.string.xml_dialog_clear_positive),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								 clearList();
							}
						})
				.setNegativeButton(
						getResources().getString(
								R.string.xml_dialog_clear_negative),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						}).create().show();
	}

	/**
	 * ���ȫ������
	 */
	public void clearList() {
		MusicInfo info = null;
		int musicSize = 0;
		int folderSize = 0;

		DBDao db = new DBDao(getApplicationContext());
		musicSize = MusicList.list.size();
		folderSize = FolderList.list.size();
		for (int j = 0; j < musicSize; musicSize--) {
			info = MusicList.list.get(j);

			if (nowPlayingMusicPath == info.getPath()) { // ----��ǰ���ŵ����ֲ����
				j++;
				musicSize++;
				musicPosition = 0;
				notifySetSelctItemPosition(0);
				continue;
			}
			MusicList.list.remove(info);
			FavoriteList.list.remove(info);

			for (int i = 0; i < folderSize; i++) {
				FolderList.list.get(i).getMusicList().remove(info);
			}

			dialogMenuPath = info.getPath();// ----��סItem��·��
			db.delete(dialogMenuPath);// ----�����ݿ���ɾ��
			musicAdapter.update(slidingPage);
		}
		db.close();// ----�ر����ݿ�
		mainSize.setText(musicAdapter.getCount() + "");
		
		//���͹㲥��֪ͨ�����޸�positionֵΪ0
		Intent intent = new Intent(MediaService.INTENT_LIST_POSITION);
		sendBroadcast(intent);
		
		sendBroadcast(playIntent);
	}

	/**
	 * ˫���˳�����
	 */
	private void exitBy2Click() {
		Timer tExit = null;
		if (isExit == false) {
			isExit = true; // ----׼���˳�
			Toast.makeText(this,
					this.getString(R.string.Press_again_to_exit_the_program),
					Toast.LENGTH_SHORT).show();
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false; // ----ȡ���˳�
				}
			}, 1500); // ----���1.5������û�а��·��ؼ�����������ʱ��ȡ�����ղ�ִ�е�����

		} else {
			exitProgram();
		}
	}

	/**
	 * ���ո����б�˵��͸�����ǣ��㲥����
	 * 
	 * @author Administrator
	 *
	 */
	private class MainReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				final String action = intent.getAction();

				if (action.equals(BROADCAST_ACTION_EXIT)) {
					exitProgram();
					return;
				} else if (action.equals(BROADCAST_ACTION_SCAN)
						&& musicAdapter != null) {
					// ----��ɨ��ҳ�淵�صĸ���ȫ�������б�����
					musicAdapter.update(SLIDING_MENU_ALL);
					mainSize.setText(musicAdapter.getCount() + "");
					return;
				}

				// ----û�д�ֵ�ľ��Ǳ�ǵģ�Ĭ�ϸ�ֵ�ϴε�����ŵ�ҳ�棬Ϊ0��Ĭ��Ϊȫ������
				slidingPage = intent.getIntExtra(BROADCAST_INTENT_PAGE,
						playerPage == 0 ? SLIDING_MENU_ALL : playerPage);
				dialogMenuPosition = intent.getIntExtra(
						BROADCAST_INTENT_POSITION, 0);
				MusicInfo info = null;

				switch (slidingPage) {
				case MainActivity.SLIDING_MENU_ALL:
					info = MusicList.list.get(dialogMenuPosition);
					break;

				case MainActivity.SLIDING_MENU_FAVORITE:
					info = FavoriteList.list.get(dialogMenuPosition);
					break;

				case MainActivity.SLIDING_MENU_FOLDER_LIST:
					info = FolderList.list.get(folderPosition).getMusicList()
							.get(dialogMenuPosition);
					break;
				}

				if (info != null) {
					int currentPosition = intent.getIntExtra("currentPosition",
							0);
					if (action.equals(BROADCAST_PLAYER_BUTTON_PREVIOUS)) {
						if(!(musicAdapter.getCount() == 1)) {
							notifySetSelctItemPosition(--musicPosition);
						}
					} else if (action.equals(BROADCAST_PLAYER_BUTTON_NEXT)) {
						if(!(musicAdapter.getCount() == 1)) {
							autoAddMusicposition();
						}
					} else if (action.equals(BROADCAST_ACTION_MENU)) {
						notifyCurrentPosition(dialogMenuPosition);

					} else if (action.equals(BROADCAST_ACTION_FAVORITE)) {
						if (info.isFavorite()) {
							info.setFavorite(false);// ----ɾ�����
							FavoriteList.list.remove(info);// ----�Ƴ�
						} else {
							info.setFavorite(true);// ----���
							FavoriteList.list.add(info);// ----����
							FavoriteList.sort();// ----��������
						}
						DBDao db = new DBDao(getApplicationContext());
						db.update(info.getName(), info.isFavorite());// ----�������ݿ�
						db.close();// ----�ر����ݿ�

						musicAdapter.update(musicAdapter.getPage());
						mainSize.setText(musicAdapter.getCount() + "");

					} else if (action.equals(BROADCAST_ACTION_MENU_INFO)) {
						InfoDialog infoDialog = new InfoDialog(
								MainActivity.this);
						infoDialog
								.setOnTVAnimDialogDismissListener(MainActivity.this);
						infoDialog.show();

						switch (slidingPage) {// ----������show��ִ��
						case MainActivity.SLIDING_MENU_ALL:
							infoDialog.setInfo(MusicList.list
									.get(currentPosition));
							break;

						case MainActivity.SLIDING_MENU_FAVORITE:
							infoDialog.setInfo(FavoriteList.list
									.get(currentPosition));
							break;

						case MainActivity.SLIDING_MENU_FOLDER_LIST:
							infoDialog.setInfo(FolderList.list
									.get(folderPosition).getMusicList()
									.get(currentPosition));
							break;
						}

					} else if (action.equals(BROADCAST_ACTION_MENU_REMOVE)) {
						removeList(currentPosition);

					} else if (action.equals(BROADCAST_ACTION_MENU_DELETE)) {
						currentPositionSave = currentPosition;
						DeleteDialog deleteDialog = new DeleteDialog(
								MainActivity.this);
						deleteDialog
								.setOnTVAnimDialogDismissListener(MainActivity.this);
						deleteDialog.show();
						notifyCurrentPosition(currentPosition);

					}

					dialogMenuPath = info.getPath();
				}
			}
		}
	}
}
