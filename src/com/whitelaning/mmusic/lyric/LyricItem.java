package com.whitelaning.mmusic.lyric;

//----歌词信息-----------------------

public class LyricItem {

	private String lyric; //----单句歌词
	private int time;//----歌词时间

	//----获得单句歌词--------------------
	public String getLyric() {
		return lyric;
	}

	//----设置单句歌词--------------------

	public void setLyric(String lyric) {
		this.lyric = lyric;
	}

	//---- 获得单句歌词时间----------------

	public int getTime() {
		return time;
	}

	//----设置单句歌词时间----------------

	public void setTime(int time) {
		this.time = time;
	}
}
