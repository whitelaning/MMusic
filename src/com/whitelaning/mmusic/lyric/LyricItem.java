package com.whitelaning.mmusic.lyric;

//----�����Ϣ-----------------------

public class LyricItem {

	private String lyric; //----������
	private int time;//----���ʱ��

	//----��õ�����--------------------
	public String getLyric() {
		return lyric;
	}

	//----���õ�����--------------------

	public void setLyric(String lyric) {
		this.lyric = lyric;
	}

	//---- ��õ�����ʱ��----------------

	public int getTime() {
		return time;
	}

	//----���õ�����ʱ��----------------

	public void setTime(int time) {
		this.time = time;
	}
}
