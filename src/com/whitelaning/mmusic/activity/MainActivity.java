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

	// ----0~6对应SlidingAdapter的position
	public static final int SLIDING_MENU_SCAN = 0;// ----侧滑-扫描歌曲
	public static final int SLIDING_MENU_ALL = 1;// ----侧滑-全部歌曲
	public static final int SLIDING_MENU_FAVORITE = 2;// ----侧滑-我的最爱
	public static final int SLIDING_MENU_FOLDER = 3;// -----侧滑-文件夹

	public static final int SLIDING_MENU_SETTING = 4;// ----侧滑-设置
	public static final int SLIDING_MENU_ABOUT = 5;// ----侧滑-关于
	public static final int SLIDING_MENU_EXIT = 6;// ----侧滑-退出程序
	public static final int SLIDING_MENU_FOLDER_LIST = 4;// ----侧滑-文件夹-文件夹列表

	public static final int DIALOG_DISMISS = 0;// ----对话框消失
	public static final int DIALOG_SCAN = 1;// ----扫描对话框
	public static final int DIALOG_MENU_REMOVE = 2;// ----歌曲列表移除对话框
	public static final int DIALOG_MENU_DELETE = 3;// ----歌曲列表提示删除对话框
	public static final int DIALOG_MENU_INFO = 4;// ----歌曲详情对话框
	public static final int DIALOG_DELETE = 5;// ----歌曲删除对话框
	public static final int DIALOG_CLEAR = 6;// ----清除全部歌曲列表

	public static final String PREFERENCES_NAME = "settings";// ----SharedPreferences名称
	public static final String PREFERENCES_MODE = "mode";// ----存储播放模式
	public static final String PREFERENCES_SCAN = "scan";// ----存储是否扫描过

	public static final String BROADCAST_PLAYER_BUTTON_PREVIOUS = "com.whitelaning.mmusic.actionplayer.previous";// ----播放界面上一首标记
	public static final String BROADCAST_PLAYER_BUTTON_NEXT = "com.whitelaning.mmusic.actionplayer.next";// ----播放界面下一首标记
	public static final String BROADCAST_ACTION_MENU_REMOVE = "com.whitelaning.mmusic.action.menu.remove";// ----listview菜单点击事件--移除
	public static final String BROADCAST_ACTION_MENU_DELETE = "com.whitelaning.mmusic.action.menu.delete";// ----listview菜单点击事件--删除
	public static final String BROADCAST_ACTION_MENU_INFO = "com.whitelaning.mmusic.action.menu.info";// ----listview菜单点击事件--信息
	public static final String BROADCAST_ACTION_SCAN = "com.whitelaning.mmusic.action.scan";// ----扫描广播标志
	public static final String BROADCAST_ACTION_MENU = "com.whitelaning.mmusic.action.menu";// ----弹出菜单广播标志
	public static final String BROADCAST_ACTION_FAVORITE = "com.whitelaning.mmusic.action.favorite";// ----喜爱广播标志
	public static final String BROADCAST_ACTION_EXIT = "com.whitelaning.mmusic.action.exit";// ----退出程序广播标志
	public static final String BROADCAST_INTENT_PAGE = "com.whitelaning.mmusic.intent.page";// ---页面状态
	public static final String BROADCAST_INTENT_POSITION = "com.whitelaning.mmusic.intent.position";// ----歌曲索引
	public static Boolean isExit = false;
	
	private String TITLE_ALL;
	private String TITLE_FAVORITE;
	private String TITLE_FOLDER;
	private String TIME_NORMAL;

	private int slidingPage = SLIDING_MENU_ALL;// ----页面状态
	private int playerPage;// ----发送给PlayerActivity的页面状态
	private int musicPosition;// ----当前播放歌曲索引
	private int folderPosition;// ----文件夹列表索引
	private int dialogMenuPosition;// ----记住弹出歌曲列表菜单的歌曲索引
	private boolean canSkip = true;// ----防止用户频繁点击造成多次解除服务绑定，true：允许解绑
	private boolean bindState = false;// ----服务绑定状态

	private String mp3Current;// ----歌曲当前时长
	private String mp3Duration;// ----歌曲总时长
	private String dialogMenuPath;// ----记住弹出歌曲列表菜单的歌曲路径

	private TextView mainTitle;// ----列表标题
	private TextView mainSize;// ----歌曲数量
	private TextView mainArtist;// ----艺术家
	private TextView mainName;// ----歌曲名称
	private TextView mainTime;// ----歌曲时间
	private ImageView mainAlbum;// ----专辑图片
	private ImageView btnShowSliding;

	private ImageButton btnPlay;// ----播放和暂停按钮
	private ImageButton btnNext;// ----下一首按钮
	private Animation myAnimation_Translate_alpha; // ----淡化推出动画

	private LinearLayout viewBack;// ----返回上一级
	private LinearLayout viewControl;// ----底部播放控制视图

	private Animation myAnimation_rotate;// ----旋转动画
	private String nowPlayingMusicPath = null; //----记录正在播放的音乐地址

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
		init();// ----初始化
	}

	@Override
	protected void onResume() {
		// ----发送广播，启动服务
		Intent intent = new Intent(MediaService.BROADCAST_ACTION_SERVICE);
		intent.putExtra(MediaService.INTENT_ACTIVITY,
				MediaService.ACTIVITY_MAIN);
		sendBroadcast(intent);

		// ----绑定服务
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
	 * 初始化所有相关工作
	 */
	private void init() {
		initSlidingMenu();// ----始化侧滑栏
		initActivity();// ----初始化主界面
		initServiceConnection();// ----后初始化服务绑定
	}

	/**
	 * 初始化侧滑相关 <----设置SlidingMenu的几种手势模式---->
	 * TOUCHMODE_FULLSCREEN：全屏模式，在content页面中，滑动，可以打开SlidingMenu
	 * TOUCHMODE_MARGIN：边缘模式，在content页面中，需要在屏幕边缘滑动才可以打开SlidingMenu
	 * TOUCHMODE_NONE：不能通过手势打开
	 */
	private void initSlidingMenu() {
		setBehindContentView(R.layout.activity_main_sliding);
		slidingMenu = getSlidingMenu();
		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		slidingMenu.setShadowWidth(20);

		// ----侧滑栏listView配置
		ListView listView = (ListView) slidingMenu.getMenu().findViewById(
				R.id.activity_main_sliding_list);// ----activity_man_sliding.xml中定义slidingMenu的布局文件
		listView.setAdapter(new SlidingAdapter(getApplicationContext()));

		// ----侧滑栏Item点击事件
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// ----判断是否点击Item，点击外部即隐藏侧滑栏
				if (viewBack.getVisibility() != View.GONE) {
					viewBack.setVisibility(View.GONE);
				}

				switch (position) {

				case SLIDING_MENU_SCAN:// ----扫描歌曲
					intentScanActivity();
					break;

				case SLIDING_MENU_ALL:// ----全部歌曲
					if (musicAdapter.getPage() != SLIDING_MENU_ALL) { // ----判断是否已经是全部歌曲界面
						mainTitle.setText(TITLE_ALL);
						musicAdapter.update(SLIDING_MENU_ALL);
						mainSize.setText(musicAdapter.getCount() + "");
					}
					break;

				case SLIDING_MENU_FAVORITE:// ----我的最爱
					if (musicAdapter.getPage() != SLIDING_MENU_FAVORITE) {
						mainTitle.setText(TITLE_FAVORITE);
						musicAdapter.update(SLIDING_MENU_FAVORITE);
						mainSize.setText(musicAdapter.getCount() + "");
					}
					break;

				case SLIDING_MENU_FOLDER:// ----文件夹
					if (musicAdapter.getPage() != SLIDING_MENU_FOLDER) {
						mainTitle.setText(TITLE_FOLDER);
						musicAdapter.update(SLIDING_MENU_FOLDER);
						mainSize.setText(musicAdapter.getCount() + "");
					}
					break;

				case SLIDING_MENU_SETTING:// ----设置（只有休眠功能，所以直接调用）
					Intent intent = new Intent(getApplicationContext(),
							SleepActivity.class);
					startActivity(intent);
					break;

				case SLIDING_MENU_ABOUT:// ----关于
					showAboutDialog();
					break;

				case SLIDING_MENU_EXIT:// ----退出程序
					exitProgram();
					break;
				}
				toggle();// ----关闭侧滑菜单
			}
		});
	}

	/**
	 * 弹出关于对话框
	 */
	private void showAboutDialog() {
		AboutDialog aboutDialog = new AboutDialog(this);
		aboutDialog.show();
	}

	/**
	 * 初始化主界面相关
	 */
	private void initActivity() {
		TITLE_ALL = this.getString(R.string.all_music);
		TITLE_FAVORITE = this.getString(R.string.my_best_love);
		TITLE_FOLDER = this.getString(R.string.folder);
		TIME_NORMAL = "00:00";

		// ----绑定控件
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

		// ----初始化动画
		myAnimation_Translate_alpha = AnimationUtils.loadAnimation(this,
				R.anim.translate_alpha);
		myAnimation_rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);

		// ----绑定点击事件
		viewBack.setOnClickListener(this);
		viewControl.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		btnShowSliding.setOnClickListener(this);
		musicAdapter = new MusicAdapter(getApplicationContext(),
				SLIDING_MENU_ALL);
		lv.setAdapter(musicAdapter);

		mainSize.setText(musicAdapter.getCount() + "");// ----获取歌曲数目

		playIntent = new Intent(getApplicationContext(), MediaService.class);// ----绑定服务
		sleepIntent = new Intent(getApplicationContext(), SleepService.class);
		receiver = new MainReceiver();
		// ----注册广播
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
				Context.MODE_PRIVATE);// ----检查是否扫描过歌曲

		if (!preferences.getBoolean(PREFERENCES_SCAN, false)) {
			ScanDialog scanDialog = new ScanDialog(this);
			scanDialog.setDialogId(DIALOG_SCAN);
			scanDialog.setOnTVAnimDialogDismissListener(this);
			scanDialog.show();
		}
	}

	/**
	 * 初始化服务绑定
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
					canSkip = true;// ----重置
					binder.setOnPlayStartListener(new OnPlayStartListener() {

						// ----播放歌曲初始化
						@Override
						public void onStart(MusicInfo info) {
							btnPlay.setImageResource(R.drawable.part_suspend);

							playerPage = musicAdapter.getPage(); // ----获取播放的sliding选择界面
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
										.setImageResource(R.drawable.default_ablum_bg);// ----默认图片
							} else {
								mainAlbum.setImageBitmap(CoverList.cover); 
							}
							mainAlbum.startAnimation(myAnimation_rotate);// ----启动动画
						}
					});

					// ----播放开始时
					binder.setOnPlayingListener(new OnPlayingListener() {

						@Override
						public void onPlay(int currentPosition) {
							mp3Current = FormatUtil.formatTime(currentPosition);
							mainTime.setText(mp3Current + " - " + mp3Duration);
						}
					});

					// ----暂停时
					binder.setOnPlayPauseListener(new OnPlayPauseListener() {

						@Override
						public void onPause() {
							btnPlay.setImageResource(R.drawable.part_play);
							mainAlbum.clearAnimation();
						}
					});

					// ----播放完成时
					binder.setOnPlayCompletionListener(new OnPlayCompleteListener() {

						@Override
						public void onPlayComplete() {

							mp3Current = null;
							notifySetSelctItemPosition(++musicPosition);
							mainAlbum.clearAnimation();
						}
					});

					// ----播放错误时
					binder.setOnPlayErrorListener(new OnPlayErrorListener() {

						@Override
						public void onPlayError() {

							dialogMenuPosition = musicPosition;
							removeList(dialogMenuPosition);// -----文件不存在从列表移除
						}
					});
					binder.setLyricView(null);// ----无歌词视图
				}
			}
		};
	}

	/**
	 * 播放完毕，自动调节高亮到下一个
	 */
	public void autoAddMusicposition() {
		notifySetSelctItemPosition(++musicPosition);
	}

	/**
	 * 跳转至扫描页面
	 */
	private void intentScanActivity() {
		Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_zoomin,
				R.anim.activity_zoomout);
	}

	/**
	 * 从当前歌曲列表中移除歌曲
	 */
	public void removeList(int dialogMenuPosition) {
		MusicInfo info = null; // ----先清空处理
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
			dialogMenuPath = info.getPath(); // ----记住Item的路径
		}

		MusicList.list.remove(info);
		FavoriteList.list.remove(info);

		for (int i = 0; i < FolderList.list.size(); i++) {
			FolderList.list.get(i).getMusicList().remove(info);
		}

		musicAdapter.update(slidingPage);
		mainSize.setText(musicAdapter.getCount() + "");

		DBDao db = new DBDao(getApplicationContext());

		db.delete(dialogMenuPath);// ----从数据库中删除
		db.close();// -----关闭

		if (binder != null && musicPosition == dialogMenuPosition) {
			if (musicPosition == (size - 1)) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PREVIOUS);
			} else {
				playIntent.putExtra(MediaService.INTENT_LIST_PAGE, slidingPage);
				playIntent.putExtra(MediaService.INTENT_LIST_POSITION,
						musicPosition);
				startService(playIntent);// ----从当前position处播放
			}
		}
	}

	/**
	 * 文件的删除
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
			dialogMenuPath = info.getPath(); // ----记住Item的路径
		}

		File file = new File(dialogMenuPath);

		if (file.exists() && file.delete()) {
			Toast.makeText(getApplicationContext(), R.string.Toast_delete_ok,
					Toast.LENGTH_LONG).show();
			removeList(dialogMenuPosition);// -----删除后更新列表
		}
	}

	/**
	 * 退出程序
	 */
	private void exitProgram() {
		stopService(sleepIntent);
		stopService(playIntent);
		finish();
	}

	/**
	 * 点击事件
	 */
	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.activity_main_view_back:// ----返回上一级监听
			viewBack.setVisibility(View.GONE);
			mainTitle.setText(TITLE_FOLDER);
			musicAdapter.update(SLIDING_MENU_FOLDER);
			mainSize.setText(musicAdapter.getCount() + "");
			break;

		case R.id.activity_main_view_bottom:// ----底部播放控制视图监听
			if (serviceConnection != null && canSkip) {
				canSkip = false;
				unbindService(serviceConnection);// ----解除绑定
				bindState = false;// ----状态更新
			}

			Intent intent = new Intent(this, PlayerActivity.class);
			intent.putExtra(BROADCAST_INTENT_POSITION, musicPosition);
			startActivity(intent);
			overridePendingTransition(R.anim.activity_zoomin,
					R.anim.activity_zoomout);
			break;

		case R.id.activity_main_ib_play:// ----播放按钮监听
			if (binder != null) {
				btnPlay.startAnimation(myAnimation_Translate_alpha);
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PLAY);
				notifySetSelctItemPosition(musicPosition);
			}
			break;
		case R.id.activity_main_ib_next:// ----下一首按钮监听
			if (binder != null) {
				btnNext.startAnimation(myAnimation_Translate_alpha);
				binder.setControlCommand(MediaService.CONTROL_COMMAND_NEXT);
				if(!(musicAdapter.getCount() == 1)) {
					autoAddMusicposition();
				}
			}
			break;

		case R.id.activity_main_show_sliding_btn:
			toggle(); // ----显示/隐藏 侧边栏
			break;
		}
	}

	/**
	 * 长按事件
	 */
	@Override
	public boolean onLongClick(View v) {
		return true;// ----屏蔽onClick事件
	}

	/**
	 * 触摸事件
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
	 * Listveiw Item点击事件
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		slidingPage = musicAdapter.getPage();
		playIntent.putExtra(MediaService.INTENT_LIST_PAGE, slidingPage);
		musicPosition = position;

		switch (slidingPage) {
		case SLIDING_MENU_FOLDER:// ----文件夹
			folderPosition = position;
			viewBack.setVisibility(View.VISIBLE);
			mainTitle.setText(FolderList.list.get(folderPosition)
					.getMusicFolder());
			musicAdapter.setFolderPosition(folderPosition);
			musicAdapter.update(SLIDING_MENU_FOLDER_LIST);
			mainSize.setText(musicAdapter.getCount() + "");
			return;// ----不执行播放

		case SLIDING_MENU_FOLDER_LIST:// ----文件夹歌曲列表
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
	 * 通知musicAdapter改变hideLinlayout的显示位置
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
	 * 通知musicAdapter改变焦点颜色
	 * 
	 * @param musicPostition
	 */
	public void notifySetSelctItemPosition(int musicPostition) {
		musicAdapter.setSelectItem(musicPostition);
		musicAdapter.notifyDataSetChanged();
	}

	private int currentPositionSave = -1;

	/**
	 * Dialog对话框关闭监视
	 */
	@Override
	public void onDismiss(int dialogId) {

		switch (dialogId) {
		case DIALOG_SCAN:// ----扫描页面
			intentScanActivity();
			break;

		case DIALOG_MENU_REMOVE:// ----执行移除
			removeList(dialogMenuPosition);
			break;

		case DIALOG_MENU_DELETE:// ----删除对话框
			DeleteDialog deleteDialog = new DeleteDialog(this);
			deleteDialog.setOnTVAnimDialogDismissListener(this);
			deleteDialog.show();
			break;

		case DIALOG_DELETE:// ----确认删除
			deleteFile(currentPositionSave);
			break;
		}
	}

	/**
	 * 触摸键监控
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitBy2Click();// ----调用双击退出函数
		}
		return false;
	}

	/**
	 * 菜单
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
	 * 清除确认dialog
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
	 * 清除全部歌曲
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

			if (nowPlayingMusicPath == info.getPath()) { // ----当前播放的音乐不清除
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

			dialogMenuPath = info.getPath();// ----记住Item的路径
			db.delete(dialogMenuPath);// ----从数据库中删除
			musicAdapter.update(slidingPage);
		}
		db.close();// ----关闭数据库
		mainSize.setText(musicAdapter.getCount() + "");
		
		//发送广播，通知服务修改position值为0
		Intent intent = new Intent(MediaService.INTENT_LIST_POSITION);
		sendBroadcast(intent);
		
		sendBroadcast(playIntent);
	}

	/**
	 * 双击退出函数
	 */
	private void exitBy2Click() {
		Timer tExit = null;
		if (isExit == false) {
			isExit = true; // ----准备退出
			Toast.makeText(this,
					this.getString(R.string.Press_again_to_exit_the_program),
					Toast.LENGTH_SHORT).show();
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false; // ----取消退出
				}
			}, 1500); // ----如果1.5秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

		} else {
			exitProgram();
		}
	}

	/**
	 * 接收歌曲列表菜单和歌曲标记，广播接收
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
					// ----从扫描页面返回的更新全部歌曲列表数据
					musicAdapter.update(SLIDING_MENU_ALL);
					mainSize.setText(musicAdapter.getCount() + "");
					return;
				}

				// ----没有传值的就是标记的，默认赋值上次点击播放的页面，为0则默认为全部歌曲
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
							info.setFavorite(false);// ----删除标记
							FavoriteList.list.remove(info);// ----移除
						} else {
							info.setFavorite(true);// ----标记
							FavoriteList.list.add(info);// ----新增
							FavoriteList.sort();// ----重新排序
						}
						DBDao db = new DBDao(getApplicationContext());
						db.update(info.getName(), info.isFavorite());// ----更新数据库
						db.close();// ----关闭数据库

						musicAdapter.update(musicAdapter.getPage());
						mainSize.setText(musicAdapter.getCount() + "");

					} else if (action.equals(BROADCAST_ACTION_MENU_INFO)) {
						InfoDialog infoDialog = new InfoDialog(
								MainActivity.this);
						infoDialog
								.setOnTVAnimDialogDismissListener(MainActivity.this);
						infoDialog.show();

						switch (slidingPage) {// ----必须在show后执行
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
