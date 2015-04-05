package com.whitelaning.mmusic.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import com.whitelaning.mmusic.db.DBDao;
import com.whitelaning.mmusic.entity.FolderInfo;
import com.whitelaning.mmusic.entity.MusicInfo;
import com.whitelaning.mmusic.entity.ScanInfo;
import com.whitelaning.mmusic.list.FolderList;
import com.whitelaning.mmusic.list.LyricList;
import com.whitelaning.mmusic.list.MusicList;

/**
 * 扫描管理器
 */
public class ScanUtil {

	private Context context;
	private DBDao db;

	public ScanUtil(Context context) {

		this.context = context;
	}

	/**
	 * 查询音乐媒体库所有目录，缺点是影响一点效率，没有找到直接提供媒体库目录的方法
	 */
	public List<ScanInfo> searchAllDirectory() {
		List<ScanInfo> list = new ArrayList<ScanInfo>();
		StringBuffer sb = new StringBuffer();
		String[] projection = { MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.DATA };
		Cursor cr = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null,
				null, MediaStore.Audio.Media.DISPLAY_NAME);
		String displayName = null;
		String data = null;
		while (cr.moveToNext()) {
			displayName = cr.getString(cr
					.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
			data = cr.getString(cr.getColumnIndex(MediaStore.Audio.Media.DATA));
			data = data.replace(displayName, "");// ----替换文件名留下它的上一级目录
			if (!sb.toString().contains(data)) {
				list.add(new ScanInfo(data, true));
				sb.append(data);
			}
		}
		cr.close();
		return list;
	}

	/**
	 * 扫描SD卡音乐，录入数据库并加入歌曲列表，缺点是假如系统媒体库没有更新媒体库目录则扫描不到
	 * 
	 * @param scanList
	 *            音乐媒体库所有目录
	 */
	public void scanMusicFromSD(List<String> folderList, Handler handler) {
		int count = 0;// ----统计新增数
		db = new DBDao(context);
		// db.deleteLyric();//----不做歌词是否已经存在判断，全部删除后重新扫描
		final int size = folderList.size();
		for (int i = 0; i < size; i++) {
			final String folder = folderList.get(i);
			File file[] = new File(folder).listFiles();
			if (file == null) {
				continue;
			}
			FolderInfo folderInfo = new FolderInfo();
			List<MusicInfo> listInfo = new ArrayList<MusicInfo>();
			for (File temp : file) {
				// ----是文件才保存，里面还有文件夹的，那就算了吧...
				if (temp.isFile()) {
					String fileName = temp.getName();
					final String path = temp.getPath();
					// ----没有后缀的文件，排除
					if (fileName.lastIndexOf(".") == -1) {
						continue;
					}
					final String end = fileName.substring(
							fileName.lastIndexOf(".") + 1, fileName.length());
					fileName = fileName.substring(0, fileName.lastIndexOf("."));
					// ----记录歌曲信息
					if (end.equalsIgnoreCase("mp3")) {// ----不区分大小写
						MusicInfo musicInfo = scanMusicTag(fileName, path);
						// ----小于1分钟的音频，排除
						if ((musicTime / 60 % 60) < 1) {
							continue;
						}
						// ----查询不存在则记录
						if (!db.queryExist(fileName, folder)) {

							// ----第一次扫描最喜爱肯定为false
							db.add(fileName, musicInfo.getName(), path, folder,
									false, musicInfo.getTime(),
									musicInfo.getSize(), musicInfo.getArtist(),
									musicInfo.getFormat(),
									musicInfo.getAlbum(), musicInfo.getYears(),
									musicInfo.getChannels(),
									musicInfo.getGenre(), musicInfo.getKbps(),
									musicInfo.getHz());
							// ----加入所有歌曲列表
							MusicList.list.add(musicInfo);
							// ----加入文件夹临时列表
							listInfo.add(musicInfo);
							count++;
						}
						if (handler != null) {
							Message msg = handler.obtainMessage();
							msg.obj = fileName;
							// ----Message从handler获取，可以直接向该handler对象发送消息
							msg.sendToTarget();
						}
					}
					// ----记录歌词信息(只识别LRC歌词)
					if (end.equalsIgnoreCase("lrc")) {// ----不区分大小写
						db.addLyric(fileName, path);
						LyricList.map.put(fileName, path);
					}
				}
			}
			if (listInfo.size() > 0) {
				boolean exists = false;
				for (int j = 0; j < FolderList.list.size(); j++) {
					// ----做对比，存在同名路径则判断有新增就合并，没有直接添加。此方法比较笨啊，肯定影响效率的...
					if (folder.equals(FolderList.list.get(j).getMusicFolder())) {
						// ----有扫描到新增得歌曲就合并列表
						FolderList.list.get(j).getMusicList().addAll(listInfo);
						exists = true;
						break;// ----跳出循环
					}
				}
				if (!exists) {// ----不存在同名路径才新增
					// ----设置文件夹列表文件夹路径
					folderInfo.setMusicFolder(folder);
					// ----设置文件夹列表歌曲信息
					folderInfo.setMusicList(listInfo);
					// ----加入文件夹列表
					FolderList.list.add(folderInfo);
				}
			}
		}
		if (handler != null) {
			Message msg = handler.obtainMessage();
			msg.obj = "扫描完成，新增" + count + "首歌曲";
			// ----Message从handler获取，可以直接向该handler对象发送消息
			msg.sendToTarget();
		}
		db.close();
	}

	/**
	 * 查新数据库记录的所有歌曲
	 */
	public void scanMusicFromDB() {
		db = new DBDao(context);
		db.queryAll(searchAllDirectory());
		db.close();
	}

	private int musicTime;

	/**
	 * 获取MP3详细信息，比如歌名、歌手、比特率之类的
	 * 
	 * @param path
	 *            文件路径
	 */
	private MusicInfo scanMusicTag(String fileName, String path) {
		File file = new File(path);
		MusicInfo info = new MusicInfo();

		if (file.exists()) {
			try {
				MP3File mp3File = (MP3File) AudioFileIO.read(file);
				MP3AudioHeader header = mp3File.getMP3AudioHeader();

				musicTime = header.getTrackLength();

				info.setFile(fileName);
				// ----时长(此处可能与MediaPlayer获得长度不一致，但误差不大)
				info.setTime(FormatUtil.formatTime((int) (header
						.getTrackLength() * 1000)));
				info.setSize(FormatUtil.formatSize(file.length()));// ----大小
				info.setPath(path);// 路径
				info.setFormat("格式: " + header.getEncodingType());// ----格式(编码类型)
				final String channels = header.getChannels();
				if (channels.equals("Joint Stereo")) {
					info.setChannels("声道: 立体声");
				} else {
					info.setChannels("声道: " + header.getChannels());// ----声道
				}
				info.setKbps("比特率: " + header.getBitRate() + "Kbps");// ----比特率
				info.setHz("采样率: " + header.getSampleRate() + "Hz");// ----采样率

				if (mp3File.hasID3v1Tag()) {
					Tag tag = mp3File.getTag();
					try {
						final String tempName = tag.getFirst(FieldKey.TITLE);
						if (tempName == null || tempName.equals("")) {
							info.setName(fileName);// ----扫描不到存文件名
						} else {
							info.setName(FormatUtil.formatGBKStr(tempName));// ----歌名
						}
					} catch (KeyNotFoundException e) {

						info.setName(fileName);// ----扫描出错存文件名
					}

					try {
						final String tempArtist = tag.getFirst(FieldKey.ARTIST);
						if (tempArtist == null || tempArtist.equals("")) {
							info.setArtist("未知艺术家");
						} else {
							info.setArtist(FormatUtil.formatGBKStr(tempArtist));// ----艺术家
						}
					} catch (KeyNotFoundException e) {

						info.setArtist("未知艺术家");
					}

					try {
						final String tempAlbum = tag.getFirst(FieldKey.ALBUM);
						if (tempAlbum == null || tempAlbum.equals("")) {
							info.setAlbum("专辑: 未知");
						} else {
							info.setAlbum("专辑: "
									+ FormatUtil.formatGBKStr(tempAlbum));// ----专辑
						}
					} catch (KeyNotFoundException e) {

						info.setAlbum("专辑: 未知");
					}

					try {
						final String tempYears = tag.getFirst(FieldKey.YEAR);
						if (tempYears == null || tempYears.equals("")) {
							info.setYears("年代: 未知");
						} else {
							info.setYears("年代: "
									+ FormatUtil.formatGBKStr(tempYears));// ----年代
						}
					} catch (KeyNotFoundException e) {

						info.setYears("年代: 未知");
					}

					try {
						final String tempGener = tag.getFirst(FieldKey.GENRE);
						if (tempGener == null || tempGener.equals("")) {
							info.setGenre("风格: 未知");
						} else {
							info.setGenre("风格: "
									+ FormatUtil.formatGBKStr(tempGener));// ----风格
						}
					} catch (KeyNotFoundException e) {

						info.setGenre("风格: 未知");
					}
				} else if (mp3File.hasID3v2Tag()) {// ----如果上面的条件不成立，才执行下面的方法
					AbstractID3v2Tag v2Tag = mp3File.getID3v2Tag();
					try {
						final String tempName = v2Tag.getFirst(FieldKey.TITLE);
						if (tempName == null || tempName.equals("")) {
							info.setName(fileName);// ----扫描不到存文件名
						} else {
							info.setName(FormatUtil.formatGBKStr(tempName));// ----歌名
						}
					} catch (KeyNotFoundException e) {

						info.setName(fileName);// ----扫描出错存文件名
					}

					try {
						final String tempArtist = v2Tag
								.getFirst(FieldKey.ARTIST);
						if (tempArtist == null || tempArtist.equals("")) {
							info.setArtist("未知艺术家");
						} else {
							info.setArtist(FormatUtil.formatGBKStr(tempArtist));// ----艺术家
						}
					} catch (KeyNotFoundException e) {

						info.setArtist("未知艺术家");
					}

					try {
						final String tempAlbum = v2Tag.getFirst(FieldKey.ALBUM);
						if (tempAlbum == null || tempAlbum.equals("")) {
							info.setAlbum("专辑: 未知");
						} else {
							info.setAlbum("专辑: "
									+ FormatUtil.formatGBKStr(tempAlbum));// ----专辑
						}
					} catch (KeyNotFoundException e) {

						info.setAlbum("专辑: 未知");
					}

					try {
						final String tempYears = v2Tag.getFirst(FieldKey.YEAR);
						if (tempYears == null || tempYears.equals("")) {
							info.setYears("年代: 未知");
						} else {
							info.setYears("年代: "
									+ FormatUtil.formatGBKStr(tempYears));// ----年代
						}
					} catch (KeyNotFoundException e) {

						info.setYears("年代: 未知");
					}

					try {
						final String tempGener = v2Tag.getFirst(FieldKey.GENRE);
						if (tempGener == null || tempGener.equals("")) {
							info.setGenre("风格: 未知");
						} else {
							info.setGenre("风格: "
									+ FormatUtil.formatGBKStr(tempGener));// ----风格
						}
					} catch (KeyNotFoundException e) {

						info.setGenre("风格: 未知");
					}
				} else {
					info.setName(fileName);
					info.setArtist("未知艺术家");
					info.setAlbum("专辑: 未知");
					info.setYears("年代: 未知");
					info.setGenre("风格: 未知");
				}
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		return info;
	}

}
