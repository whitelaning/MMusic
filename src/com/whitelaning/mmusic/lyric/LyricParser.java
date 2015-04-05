package com.whitelaning.mmusic.lyric;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricParser {

	private String lyricPath = null;

	private List<LyricItem> lyricList = null;

	//----���캯��----------------------------------

	public LyricParser(String lyricPath) {
		
		this.lyricPath = lyricPath;
		this.lyricList = new ArrayList<LyricItem>();
	}

	public List<LyricItem> parser() throws Exception {
		String encode = "UTF-8";//----Ĭ�ϱ���
		File file = new File(lyricPath);
		CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
		detector.add(new ParsingDetector(false));
		detector.add(JChardetFacade.getInstance());//----�õ�antlr.jar��chardet.jar
		detector.add(ASCIIDetector.getInstance());
		detector.add(UnicodeDetector.getInstance());
		Charset set = null;
		try {
			set = detector.detectCodepage(file.toURI().toURL());//----����ļ�����
		} catch (Exception e) {

			e.printStackTrace();
		}
		if (set != null) {
			encode = set.name();
		}
		InputStream inputStream = new FileInputStream(file);
		InputStreamReader inputReader = new InputStreamReader(inputStream,
				encode);
		BufferedReader bufferedReader = new BufferedReader(inputReader);

		String temp = null;
		//----����
		int line = 0;
		//----ÿһ�д��ڵ�ʱ������
		int count = 0;
		//----ÿһ�д��ڵ�ʱ������ֳ���
		int timeLength = 0;

		//----�ݴ�ʱ������ݣ���Ϊ�е�һ���ж��ʱ�䣬��Ҫ����ȡ����ʱ�䣬�����ȡ���
		ArrayList<Integer> timeTemp = new ArrayList<Integer>();

		//----����һ��������ʽ��������ƥ��[00:00.00]/[00:00:00]/[00:00]
		Pattern p = Pattern
				.compile("\\[\\s*[0-9]{1,2}\\s*:\\s*[0-5][0-9]\\s*[\\.:]?\\s*[0-9]?[0-9]?\\s*\\]");
		String msg = null;
		//----һ��һ�ж�ȡ
		while ((temp = bufferedReader.readLine()) != null) {
			line++;
			//----����ǰ����
			count = 0;
			//----����ݴ��б�
			timeTemp.clear();
			//----��һ�н���ƥ��
			Matcher m = p.matcher(temp);
			//----���ƥ�䵽�ֶδ���
			while (m.find()) {
				count++;
				//----��ȡƥ�䵽���ֶ�
				String timeStr = m.group();
				timeStr = timeStr.substring(1, timeStr.length() - 1);
				timeLength = timeStr.length() + 2;//----ʱ�����ֵĳ��ȣ����ں����ȡ
				//----����ƥ�䵽���ֶμ����ʱ��
				int timeMill = time2ms(timeStr);
				//----�����б�
				timeTemp.add(timeMill);
			}
			//----�������ʱ�������
			if (count > 0) {
				//----����С����˳�����ʱ�������
				for (int j = 0; j < timeTemp.size(); j++) {
					LyricItem item = new LyricItem();
					//----����б�Ϊ��ֱ�����
					if (lyricList.size() == 0) {
						//----ʱ���������ӵ��б�
						item.setTime(timeTemp.get(j));
						//----��ȡ����ַ�������һ����һ����]�������⴦��
						if (line == 1) {
							msg = temp.substring(timeLength * count + 1);
						} else {
							msg = temp.substring(timeLength * count);
						}
						//----�����ӵ��б�
						item.setLyric(msg);
						lyricList.add(item);
					}
					//----���ʱ������б������һ��ʱ��ֱ����ӵ���β
					else if (timeTemp.get(j) > lyricList.get(
							lyricList.size() - 1).getTime()) {
						item.setTime(timeTemp.get(j));
						if (line == 1) {
							msg = temp.substring(timeLength * count + 1);
						} else {
							msg = temp.substring(timeLength * count);
						}
						item.setLyric(msg);
						lyricList.add(item);
					}
					//----���򰴴�С˳�����
					else {
						for (int index = 0; index < lyricList.size(); index++) {
							if (timeTemp.get(j) <= lyricList.get(index)
									.getTime()) {
								item.setTime(timeTemp.get(j));
								if (line == 1) {
									msg = temp
											.substring(timeLength * count + 1);
								} else {
									msg = temp.substring(timeLength * count);
								}
								item.setLyric(msg);
								lyricList.add(index, item);
								break;
							}
						}
					}
				}
			}
		}
		bufferedReader.close();
		inputReader.close();
		inputStream.close();
		return lyricList;
	}

	//----LRC�ļ�֧�����ֲ�ͬ��ʱ���ʽ mm:ss.ms/mm:ss:ms/mm:ss-------------

	public int time2ms(String timeStr) {
		String s[] = timeStr.split(":");
		int min = Integer.parseInt(s[0]);
		int sec = 0;
		int mill = 0;
		//----�����ʽΪmm:ss:ms
		if (s.length > 2) {
			sec = Integer.parseInt(s[1]);
			mill = Integer.parseInt(s[2]);
		} else {
			String ss[] = s[1].split("\\.");
			//----�����ʽΪmm:ss.ms
			if (ss.length > 1) {
				sec = Integer.parseInt(ss[0]);
				mill = Integer.parseInt(ss[1]);
			}
			//----�����ʽΪmm:ss
			else {
				sec = Integer.parseInt(ss[0]);
				mill = 0;
			}
		}
		return min * 60 * 1000 + sec * 1000 + mill * 10;
	}

}
