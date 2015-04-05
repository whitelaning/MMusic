package com.whitelaning.mmusic.util;

import java.io.File;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.images.Artwork;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

//----ɨ��������Ƕר��ͼƬ������----------------------

public class AlbumUtil {

	public Bitmap scanAlbumImage(String path) {
		File file = new File(path);
		Bitmap bitmap = null;

		if (file.exists()) {
			try {
				MP3File mp3File = (MP3File) AudioFileIO.read(file);
				
				if (mp3File.hasID3v1Tag()) {
					
					Tag tag = mp3File.getTag();
					Artwork artwork = tag.getFirstArtwork();//----���ר��ͼƬ
					
					if (artwork != null) {
						byte[] byteArray = artwork.getBinaryData();//----����ȡ����ר��ͼƬת�ɶ�����
						bitmap = BitmapFactory.decodeByteArray(byteArray, 0,
								byteArray.length); //----ͨ��BitmapFactoryת��Bitmap
					}
				} else if (mp3File.hasID3v2Tag()) {//----����������������������ִ������ķ���
					
					AbstractID3v2Tag v2Tag = mp3File.getID3v2Tag();
					Artwork artwork = v2Tag.getFirstArtwork();//----���ר��ͼƬ
					
					if (artwork != null) {
						byte[] byteArray = artwork.getBinaryData();//----����ȡ����ר��ͼƬת�ɶ�����
						bitmap = BitmapFactory.decodeByteArray(byteArray, 0,
								byteArray.length); //----ͨ��BitmapFactoryת��Bitmap
					}
				}

			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}

		return bitmap;
	}

}
