package com.whitelaning.mmusic.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.whitelaning.mmusic.entity.MusicInfo;

//----����һ�����õ���ϲ�������б�---------------

public class FavoriteList {

	public static final List<MusicInfo> list = new ArrayList<MusicInfo>();

	
	
	public static void sort() {
		Collections.sort(list, new MusicInfo());//----����ĸ����
	}

}
