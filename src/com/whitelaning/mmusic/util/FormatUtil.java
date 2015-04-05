package com.whitelaning.mmusic.util;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;

//----转换工作------------------------------

public class FormatUtil {

	//----格式化时间------------------------

	public static String formatTime(int time) {

		if (time == 0) {
			return "00:00";
		}
		time = time / 1000;
		int m = time / 60 % 60;
		int s = time % 60;
		return (m > 9 ? m : "0" + m) + ":" + (s > 9 ? s : "0" + s);
	}

	//----格式化文件大小----------------------------------

	public static String formatSize(long size) {

		DecimalFormat df = new DecimalFormat("#.00");
		String fileSize = "0B";
		if (size < 1024) {
			fileSize = df.format((double) size) + "B";
		} else if (size < 1048576) {
			fileSize = df.format((double) size / 1024) + "KB";
		} else if (size < 1073741824) {
			fileSize = df.format((double) size / 1048576) + "MB";
		} else {
			fileSize = df.format((double) size / 1073741824) + "GB";
		}
		return fileSize;
	}

	//----对乱码的处理-----------------------------------
	
	public static String formatGBKStr(String s){
		String str = null;
		try {
			if(s.equals(new String(s.getBytes("ISO-8859-1"),"ISO-8859-1"))){
				str = new String(s.getBytes("ISO-8859-1"), "GB2312");
				return str;
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}

}
