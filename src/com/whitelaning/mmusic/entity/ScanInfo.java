package com.whitelaning.mmusic.entity;

//----ɨ����Ϣ��SD��·�����û��Ƿ�ѡ----------------------------------

public class ScanInfo {

	private String folderPath;//----�ļ���·��
	private boolean isChecked;//----�û��Ƿ�ѡ

	public ScanInfo(String folderPath, boolean isChecked) {

		this.folderPath = folderPath;
		this.isChecked = isChecked;
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

}
