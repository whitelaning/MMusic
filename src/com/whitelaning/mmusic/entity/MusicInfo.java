package com.whitelaning.mmusic.entity;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;

//----������ϸ��Ϣ-------------------------------------------

public class MusicInfo implements Comparator<Object> {

	private String file;//----�ļ���
	private String time;//----ʱ��
	private String size;//----��С
	private String name;//----����
	private String artist;//----������
	private String path;//----·��
	private String format;//----��ʽ(��������)
	private String album;//----ר��
	private String years;//----���
	private String channels;//----����
	private String genre;//----���
	private String kbps;//----������
	private String hz;//----������

	private int audioSessionId;//----��Ƶ�ỰID
	private int mp3Duration;//----��ȷ������ʱ��(����SeekBar�ܳ���)

	private boolean favorite;//----�Ƿ��

	private Collator collator;

	public MusicInfo() {
		
		collator = Collator.getInstance();
	}

	//----����ļ���-----------------------------

	public String getFile() {
		return file;
	}

	//----�����ļ���-----------------------------

	public void setFile(String file) {
		this.file = file;
	}

	//----���ʱ��------------------------------

	public String getTime() {
		return time;
	}

	//----����ʱ��------------------------------

	public void setTime(String time) {
		this.time = time;
	}

	//----��ô�С-----------------------------

	public String getSize() {
		return size;
	}

	//----�ô�С------------------------------

	public void setSize(String size) {
		this.size = size;
	}

	//----��ø���-----------------------------

	public String getName() {
		return name;
	}

	//----���ø���-----------------------------

	public void setName(String name) {
		this.name = name;
	}

	//----���������-----------------------------

	public String getArtist() {
		return artist;
	}

	//----����������-----------------------------
	
	public void setArtist(String artist) {
		this.artist = artist;
	}

	//----���·��-----------------------------

	public String getPath() {
		return path;
	}

	//----����·��-----------------------------

	public void setPath(String path) {
		this.path = path;
	}

	//----��ø�ʽ(��������)-----------------------------

	public String getFormat() {
		return format;
	}

	//----���ø�ʽ(��������)-----------------------------

	public void setFormat(String format) {
		this.format = format;
	}

	//----���ר��-----------------------------

	public String getAlbum() {
		return album;
	}

	//----����ר��-----------------------------

	public void setAlbum(String album) {
		this.album = album;
	}
	
	//----������-----------------------------

	public String getYears() {
		return years;
	}

	//----�������-----------------------------

	public void setYears(String years) {
		this.years = years;
	}

	//----�������-----------------------------

	public String getChannels() {
		return channels;
	}

	//----��������-----------------------------

	public void setChannels(String channels) {
		this.channels = channels;
	}

	//----��÷��-----------------------------

	public String getGenre() {
		return genre;
	}

	//----���÷��-----------------------------

	public void setGenre(String genre) {
		this.genre = genre;
	}

	//----��ñ�����-----------------------------

	public String getKbps() {
		return kbps;
	}

	//----���ñ�����-----------------------------

	public void setKbps(String kbps) {
		this.kbps = kbps;
	}

	//----��ò�����-----------------------------

	public String getHz() {
		return hz;
	}

	//----���ò�����-----------------------------

	public void setHz(String hz) {
		this.hz = hz;
	}

	//----���audioSessionId-----------------------------

	public int getAudioSessionId() {
		return audioSessionId;
	}

	//----����audioSessionId-----------------------------

	public void setAudioSessionId(int audioSessionId) {
		this.audioSessionId = audioSessionId;
	}

	//----��þ�ȷ������ʱ��-----------------------------

	public int getMp3Duration() {
		return mp3Duration;
	}

	//----���þ�ȷ������ʱ��-----------------------------

	public void setMp3Duration(int mp3Duration) {
		this.mp3Duration = mp3Duration;
	}

	//----�Ƿ��-----------------------------

	public boolean isFavorite() {
		return favorite;
	}

	//----�����-----------------------------

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

	@Override
	public int compare(Object object1, Object object2) {
		// ���ַ���ת��Ϊһϵ�б��أ����ǿ����Ա�����ʽ�� CollationKeys��Ƚ�
		// Ҫ�벻���ִ�Сд���бȽ���o1.toString().toLowerCase()
		CollationKey key1 = collator.getCollationKey(object1.toString());
		CollationKey key2 = collator.getCollationKey(object2.toString());
		// ���صķֱ�Ϊ1,0,-1�ֱ������ڣ����ڣ�С�ڡ�Ҫ�밴����ĸ��������Ļ��Ӹ���-����
		return key1.compareTo(key2);
	}

}
