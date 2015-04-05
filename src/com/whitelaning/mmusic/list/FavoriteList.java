package com.whitelaning.mmusic.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.whitelaning.mmusic.entity.MusicInfo;

//----创建一个公用的最喜爱歌曲列表---------------

public class FavoriteList {

	public static final List<MusicInfo> list = new ArrayList<MusicInfo>();

	
	
	public static void sort() {
		Collections.sort(list, new MusicInfo());//----按字母排序
	}

}
