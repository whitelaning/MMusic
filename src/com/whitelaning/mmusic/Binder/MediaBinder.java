package com.whitelaning.mmusic.Binder;

import android.os.Binder;

import com.whitelaning.mmusic.entity.MusicInfo;
import com.whitelaning.mmusic.lyric.LyricView;

//----控制播放Binder类------------------------------

public class MediaBinder extends Binder {

	private OnPlayStartListener onPlayStartListener;
	private OnPlayingListener onPlayingListener;
	private OnPlayPauseListener onPlayPauseListener;
	private OnPlayCompleteListener onPlayCompleteListener;
	private OnPlayErrorListener onPlayErrorListener;
	private OnModeChangeListener onModeChangeListener;

	private OnServiceBinderListener onServiceBinderListener;

	public void playStart(MusicInfo info) {
		if (onPlayStartListener != null) {
			onPlayStartListener.onStart(info);
		}
	}

	public void playUpdate(int currentPosition) {
		if (onPlayingListener != null) {
			onPlayingListener.onPlay(currentPosition);
		}
	}

	public void playPause() {
		if (onPlayPauseListener != null) {
			onPlayPauseListener.onPause();
		}
	}

	public void playComplete() {
		if (onPlayCompleteListener != null) {
			onPlayCompleteListener.onPlayComplete();
		}
	}

	public void playError() {
		if (onPlayErrorListener != null) {
			onPlayErrorListener.onPlayError();
		}
	}

	public void modeChange(int mode) {
		if (onModeChangeListener != null) {
			onModeChangeListener.onModeChange(mode);
		}
	}

	//----触及SeekBar时响应-------------------------------------
	 
	public void seekBarStartTrackingTouch() {
		if (onServiceBinderListener != null) {
			onServiceBinderListener.seekBarStartTrackingTouch();
		}
	}

	//----离开SeekBar时响应--------------------------------------

	public void seekBarStopTrackingTouch(int progress) {
		if (onServiceBinderListener != null) {
			onServiceBinderListener.seekBarStopTrackingTouch(progress);
		}
	}

	/**
	 * 设置歌词视图
	 * 
	 * @param lrcView
	 *            歌词视图
	 */
	public void setLyricView(LyricView lrcView) {
		if (onServiceBinderListener != null) {
			onServiceBinderListener.lrc(lrcView);
		}
	}

	/**
	 * 设置控制命令
	 * 
	 * @param command
	 *            控制命令
	 */
	public void setControlCommand(int command) {
		if (onServiceBinderListener != null) {
			onServiceBinderListener.control(command);
		}
	}

	public void setOnPlayStartListener(OnPlayStartListener onPlayStartListener) {
		this.onPlayStartListener = onPlayStartListener;
	}

	public void setOnPlayingListener(OnPlayingListener onPlayingListener) {
		this.onPlayingListener = onPlayingListener;
	}

	public void setOnPlayPauseListener(OnPlayPauseListener onPlayPauseListener) {
		this.onPlayPauseListener = onPlayPauseListener;
	}

	public void setOnPlayCompletionListener(
			OnPlayCompleteListener onPlayCompleteListener) {
		this.onPlayCompleteListener = onPlayCompleteListener;
	}

	public void setOnPlayErrorListener(OnPlayErrorListener onPlayErrorListener) {
		this.onPlayErrorListener = onPlayErrorListener;
	}

	public void setOnModeChangeListener(
			OnModeChangeListener onModeChangeListener) {
		this.onModeChangeListener = onModeChangeListener;
	}

	public void setOnServiceBinderListener(
			OnServiceBinderListener onServiceBinderListener) {
		this.onServiceBinderListener = onServiceBinderListener;
	}

	/**
	 * 开始播放回调接口
	 */
	public interface OnPlayStartListener {
		public void onStart(MusicInfo info);
	}

	/**
	 * 正在播放回调接口
	 */
	public interface OnPlayingListener {
		public void onPlay(int currentPosition);
	}

	/**
	 * 暂停播放回调接口
	 */
	public interface OnPlayPauseListener {
		public void onPause();
	}

	/**
	 * 播放完成回调接口
	 */
	public interface OnPlayCompleteListener {
		public void onPlayComplete();
	}

	/**
	 * 播放出错回调接口
	 */
	public interface OnPlayErrorListener {
		public void onPlayError();
	}

	/**
	 * 播放模式更改回调接口
	 */
	public interface OnModeChangeListener {
		public void onModeChange(int mode);
	}

	/**
	 * 回调接口，只允许service使用
	 */
	public interface OnServiceBinderListener {
		
		void seekBarStartTrackingTouch();//----触及SeekBar时响应
		void seekBarStopTrackingTouch(int progress);//----离开SeekBar时响应
		void lrc(LyricView lyricView);//----设置歌词
		void control(int command);//----播放控制(播放、暂停、上一首、下一首、播放模式切换)

	}

}
