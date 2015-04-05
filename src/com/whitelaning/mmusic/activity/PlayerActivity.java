package com.whitelaning.mmusic.activity;

import java.util.ArrayList;

import com.whitelaning.mmusic.R;
import com.whitelaning.mmusic.Binder.MediaBinder;
import com.whitelaning.mmusic.Binder.MediaBinder.OnModeChangeListener;
import com.whitelaning.mmusic.Binder.MediaBinder.OnPlayCompleteListener;
import com.whitelaning.mmusic.Binder.MediaBinder.OnPlayErrorListener;
import com.whitelaning.mmusic.Binder.MediaBinder.OnPlayPauseListener;
import com.whitelaning.mmusic.Binder.MediaBinder.OnPlayStartListener;
import com.whitelaning.mmusic.Binder.MediaBinder.OnPlayingListener;
import com.whitelaning.mmusic.custom.PushView;
import com.whitelaning.mmusic.entity.MusicInfo;
import com.whitelaning.mmusic.list.CoverList;
import com.whitelaning.mmusic.lyric.LyricView;
import com.whitelaning.mmusic.service.MediaService;
import com.whitelaning.mmusic.util.FormatUtil;
import com.whitelaning.mmusic.util.ImageUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayerActivity extends Activity implements OnClickListener,
		OnLongClickListener, OnTouchListener, OnSeekBarChangeListener {

	private final String TIME_NORMAL = "00:00";
	private final int[] modeImage = { R.drawable.player_btn_mode_normal_style,
			R.drawable.player_btn_mode_repeat_one_style,
			R.drawable.player_btn_mode_repeat_all_style,
			R.drawable.player_btn_mode_random_style };

	private int musicPosition;// ----当前播放歌曲索引
	private boolean isFavorite = false;// ----当前歌曲是否为最爱
	private ImageButton btnMode;// ----播放模式按钮
	private ImageButton btnPrevious;// ----上一首按钮
	private ImageButton btnPlay;// ----播放和暂停按钮
	private ImageButton btnNext;// ----下一首按钮
	private ImageButton btnFavorite;// ----我的最爱按钮
	private ImageView albumSkin;// ----专辑背景图
	private ImageView blurBg;// ----模糊背景
	private TextView currentTime;// ----当前时间
	private TextView totalTime;// ----总时间
	private SeekBar seekBar;// ----进度条
	private PushView mp3Name;// ----歌名
	private PushView mp3Years;// ----年代
	private PushView mp3Artist;// ----艺术家
	private LyricView lyricView;// ----歌词视图
	private Animation myAnimation_rotate;// ----动画
	private TextView nolyricView;
	private LinearLayout linearPlayerInfo;
	private Intent playIntent;
	private MediaBinder binder;
	private SharedPreferences preferences;
	private ServiceConnection serviceConnection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// ----屏幕常亮

		init();// ----初始化

		Intent intent = new Intent(MediaService.BROADCAST_ACTION_SERVICE);
		intent.putExtra(MediaService.INTENT_ACTIVITY,
				MediaService.ACTIVITY_PLAYER);
		sendBroadcast(intent);
	}

	@Override
	public void finish() {
		super.finish();
		if (serviceConnection != null) {
			unbindService(serviceConnection);// ----一定要在finish之前解除绑定
			serviceConnection = null;
		}
	}

	/**
	 * 初始化
	 */
	private void init() {
		musicPosition = getIntent().getIntExtra(
				MainActivity.BROADCAST_INTENT_POSITION, 0);
		// ----取出用户设置信息,判断歌曲播放模式
		preferences = getSharedPreferences(MainActivity.PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		playIntent = new Intent(getApplicationContext(), MediaService.class);

		initPortraitActivity();// ----初始化界面
	}

	/**
	 * 初始化界面Start
	 */
	@SuppressLint("NewApi")
	private void initPortraitActivity() {
		setContentView(R.layout.activity_player);
		linearPlayerInfo = (LinearLayout) findViewById(R.id.activity_player_info);
		blurBg = (ImageView) findViewById(R.id.activity_Blur_bg);
		albumSkin = (ImageView) findViewById(R.id.album_skin);
		btnMode = (ImageButton) findViewById(R.id.activity_player_ib_mode);
		btnPrevious = (ImageButton) findViewById(R.id.activity_player_ib_previous);
		btnPlay = (ImageButton) findViewById(R.id.activity_player_ib_play);
		btnNext = (ImageButton) findViewById(R.id.activity_player_ib_next);
		btnFavorite = (ImageButton) findViewById(R.id.activity_player_ib_favorite);
		seekBar = (SeekBar) findViewById(R.id.activity_player_seek);
		currentTime = (TextView) findViewById(R.id.activity_player_tv_time_current);
		totalTime = (TextView) findViewById(R.id.activity_player_tv_time_total);
		mp3Name = (PushView) findViewById(R.id.activity_player_tv_name);
		mp3Years = (PushView) findViewById(R.id.activity_player_tv_years);
		mp3Artist = (PushView) findViewById(R.id.activity_player_tv_artist);
		lyricView = (LyricView) findViewById(R.id.activity_player_lyric);
		nolyricView = (TextView) findViewById(R.id.activity_player_nolyeric);

		myAnimation_rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
		currentTime.setText(TIME_NORMAL);
		totalTime.setText(TIME_NORMAL);
		btnMode.setOnClickListener(this);
		btnPrevious.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		btnFavorite.setOnClickListener(this);
		btnPrevious.setOnLongClickListener(this);
		btnNext.setOnLongClickListener(this);
		btnPrevious.setOnTouchListener(this);
		btnNext.setOnTouchListener(this);
		seekBar.setOnSeekBarChangeListener(this);
		linearPlayerInfo.setOnClickListener(this);

		btnMode.setImageResource(modeImage[preferences.getInt(
				MainActivity.PREFERENCES_MODE, 0)]);

		// ----初始化主界面End

		// ----初始化服务
		serviceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {

				binder = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {

				binder = (MediaBinder) service;
				binder.setLyricView(lyricView);// ----设置歌词视图

				if (binder != null) {
					binder.setOnPlayStartListener(new OnPlayStartListener() {

						@Override
						public void onStart(MusicInfo info) {
							// ----设置中间和背景图片为专辑图片
							if (CoverList.cover != null) {
								albumSkin.setBackground(ImageUtil
										.bitmapToDrawable(CoverList.cover));
								blurBg.setBackground(ImageUtil
										.BoxBlurFilter(CoverList.cover));
							} else {
								albumSkin.setBackground(getResources()
										.getDrawable(R.drawable.default_ablum_bg));
								blurBg.setBackground(getResources()
										.getDrawable(R.drawable.skin_bg));
							}
							albumSkin.startAnimation(myAnimation_rotate);
							mp3Name.setText(info.getName());
							mp3Years.setText(info.getYears());
							currentTime.setText(TIME_NORMAL);
							totalTime.setText(info.getTime());
							seekBar.setMax(info.getMp3Duration());
							
							//----歌词判断更新------------------------
							if(LyricView.getLyricSize() == 0){
								nolyricView.setVisibility(0);
							}else{
								nolyricView.setVisibility(8);
							}

							// ----音乐信息
							ArrayList<String> list = new ArrayList<String>();
							list.add(info.getFormat());
							list.add("大小: " + info.getSize());
							list.add(info.getGenre());
							list.add(info.getAlbum());
							list.add(info.getYears());
							list.add(info.getChannels());
							list.add(info.getKbps());
							list.add(info.getHz());

							mp3Artist.setText(info.getArtist());

							btnPlay.setImageResource(R.drawable.player_btn_pause_style);

							isFavorite = info.isFavorite();

							btnFavorite
									.setImageResource(isFavorite ? R.drawable.player_btn_favorite_star_style
											: R.drawable.player_btn_favorite_nostar_style);
						}
					});

					binder.setOnPlayingListener(new OnPlayingListener() {
						@Override
						public void onPlay(int currentPosition) {

							seekBar.setProgress(currentPosition);

							currentTime.setText(FormatUtil
									.formatTime(currentPosition));
						}
					});

					binder.setOnPlayPauseListener(new OnPlayPauseListener() {

						@Override
						public void onPause() {
							btnPlay.setImageResource(R.drawable.player_btn_play_style);
							albumSkin.clearAnimation();

						}
					});

					binder.setOnPlayCompletionListener(new OnPlayCompleteListener() {
						@Override
						public void onPlayComplete() {
							albumSkin.clearAnimation();

							Intent intent = new Intent(
									MainActivity.BROADCAST_PLAYER_BUTTON_NEXT);
							sendBroadcast(intent);
						}
					});

					binder.setOnPlayErrorListener(new OnPlayErrorListener() {

						@Override
						public void onPlayError() {

						}
					});

					binder.setOnModeChangeListener(new OnModeChangeListener() {

						@Override
						public void onModeChange(int mode) {
							btnMode.setImageResource(modeImage[mode]);
						}
					});
				}
			}
		};
		bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		// ----初始化服务绑定End
	}

	@Override
	public void onClick(View v) {

		Intent intent = null;

		switch (v.getId()) {
		case R.id.activity_player_ib_mode:// ----播放模式切换按钮监听
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_MODE);
			}
			break;

		case R.id.activity_player_ib_previous:// ----上一首按钮监听
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PREVIOUS);
				binder.setLyricView(lyricView);

				// ----通知MainAcitivy更新高亮listview item
				intent = new Intent(
						MainActivity.BROADCAST_PLAYER_BUTTON_PREVIOUS);
				sendBroadcast(intent);

				musicPosition--;
			}
			break;

		case R.id.activity_player_ib_play:// ----播放按钮监听
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_PLAY);
			}
			break;

		case R.id.activity_player_ib_next:// ----下一首按钮监听
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_NEXT);
				binder.setLyricView(lyricView);
				intent = new Intent(MainActivity.BROADCAST_PLAYER_BUTTON_NEXT);
				sendBroadcast(intent);

				musicPosition++;
			}
			break;

		case R.id.activity_player_ib_favorite:// ----我的最爱按钮监听
			if (seekBar.getMax() > 0) {// ----此判断表示播放过歌曲
				intent = new Intent(MainActivity.BROADCAST_ACTION_FAVORITE);
				intent.putExtra(MainActivity.BROADCAST_INTENT_POSITION,
						musicPosition);// ----传Position过去
				sendBroadcast(intent);

				if (isFavorite) {
					btnFavorite
							.setImageResource(R.drawable.player_btn_favorite_nostar_style);
					isFavorite = false;
				} else {
					btnFavorite
							.setImageResource(R.drawable.player_btn_favorite_star_style);
					startFavoriteImageAnimation();// ----现通过动画来阻止频繁点击
					isFavorite = true;
				}
			}
			break;
		case R.id.activity_player_info:
			break;
		}
	}

	/**
	 * 长按事件监听
	 */
	@Override
	public boolean onLongClick(View v) {

		switch (v.getId()) {
		case R.id.activity_player_ib_previous:// ----快退
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_REWIND);
			}
			break;

		case R.id.activity_player_ib_next:// ----快进
			if (binder != null) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_FORWARD);
			}
			break;
		}
		return true;// ----返回true屏蔽onClick
	}

	/**
	 * 用于支持长按快进、快退播放
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		switch (v.getId()) {

		case R.id.activity_player_ib_previous:// ----松手播放
			if (binder != null && event.getAction() == MotionEvent.ACTION_UP) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
			}
			break;

		case R.id.activity_player_ib_next:// ----松手播放
			if (binder != null && event.getAction() == MotionEvent.ACTION_UP) {
				binder.setControlCommand(MediaService.CONTROL_COMMAND_REPLAY);
			}
			break;
		}
		return false;
	}

	/**
	 * seekBar进度数值的改变
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (seekBar.getId() == R.id.activity_player_seek
				&& (fromUser && seekBar.getMax() > 0)) {
			currentTime.setText(FormatUtil.formatTime(progress));
		}
	}

	/**
	 * seekBar开始拖动
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

		if (seekBar.getId() == R.id.activity_player_seek && binder != null) {
			binder.seekBarStartTrackingTouch();
		}
	}

	/**
	 * seekBar停止拖动
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

		if (seekBar.getId() == R.id.activity_player_seek && binder != null) {
			binder.seekBarStopTrackingTouch(seekBar.getProgress());
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(getApplicationContext(),
					MainActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.activity_zoomin,
					R.anim.activity_zoomout);
			PlayerActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 我的最爱图片动画 用于防止快速点击
	 */
	private void startFavoriteImageAnimation() {
		AnimationSet animationset = new AnimationSet(false);

		ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f,
				1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimation.setInterpolator(new OvershootInterpolator(5F));// ----弹出再回来的动画的效果
		scaleAnimation.setDuration(700);
		AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
		alphaAnimation.setDuration(500);
		alphaAnimation.setStartOffset(700);
		animationset.addAnimation(scaleAnimation);
		animationset.addAnimation(alphaAnimation);
		animationset.setFillAfter(true);
		animationset.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {

			}
		});
	}
}
