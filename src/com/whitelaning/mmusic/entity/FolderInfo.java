package com.whitelaning.mmusic.entity;

import java.util.List;

//----�����ļ��ж�Ӧ�ĸ�����Ϣ---------------------------
public class FolderInfo {

	private String musicFolder;//----���������ļ���
	private List<MusicInfo> musicList;//----�����б�

	//----����ļ���·����----------------------------

	public String getMusicFolder() {
		return musicFolder;
	}

	//----�����ļ���·����----------------------------

	public void setMusicFolder(String musicFolder) {
		this.musicFolder = musicFolder;
	}

	//----����ļ����µĸ����б�-----------------------

	public List<MusicInfo> getMusicList() {
		return musicList;
	}

	//----�����ļ����¸����б�------------------------

	public void setMusicList(List<MusicInfo> musicList) {
		this.musicList = musicList;
	}

}
