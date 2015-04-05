package com.whitelaning.mmusic.Binder;

import android.os.Binder;

import com.whitelaning.mmusic.entity.MusicInfo;
import com.whitelaning.mmusic.lyric.LyricView;

//----���Ʋ���Binder��------------------------------

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

	//----����SeekBarʱ��Ӧ-------------------------------------
	 
	public void seekBarStartTrackingTouch() {
		if (onServiceBinderListener != null) {
			onServiceBinderListener.seekBarStartTrackingTouch();
		}
	}

	//----�뿪SeekBarʱ��Ӧ--------------------------------------

	public void seekBarStopTrackingTouch(int progress) {
		if (onServiceBinderListener != null) {
			onServiceBinderListener.seekBarStopTrackingTouch(progress);
		}
	}

	/**
	 * ���ø����ͼ
	 * 
	 * @param lrcView
	 *            �����ͼ
	 */
	public void setLyricView(LyricView lrcView) {
		if (onServiceBinderListener != null) {
			onServiceBinderListener.lrc(lrcView);
		}
	}

	/**
	 * ���ÿ�������
	 * 
	 * @param command
	 *            ��������
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
	 * ��ʼ���Żص��ӿ�
	 */
	public interface OnPlayStartListener {
		public void onStart(MusicInfo info);
	}

	/**
	 * ���ڲ��Żص��ӿ�
	 */
	public interface OnPlayingListener {
		public void onPlay(int currentPosition);
	}

	/**
	 * ��ͣ���Żص��ӿ�
	 */
	public interface OnPlayPauseListener {
		public void onPause();
	}

	/**
	 * ������ɻص��ӿ�
	 */
	public interface OnPlayCompleteListener {
		public void onPlayComplete();
	}

	/**
	 * ���ų���ص��ӿ�
	 */
	public interface OnPlayErrorListener {
		public void onPlayError();
	}

	/**
	 * ����ģʽ���Ļص��ӿ�
	 */
	public interface OnModeChangeListener {
		public void onModeChange(int mode);
	}

	/**
	 * �ص��ӿڣ�ֻ����serviceʹ��
	 */
	public interface OnServiceBinderListener {
		
		void seekBarStartTrackingTouch();//----����SeekBarʱ��Ӧ
		void seekBarStopTrackingTouch(int progress);//----�뿪SeekBarʱ��Ӧ
		void lrc(LyricView lyricView);//----���ø��
		void control(int command);//----���ſ���(���š���ͣ����һ�ס���һ�ס�����ģʽ�л�)

	}

}
