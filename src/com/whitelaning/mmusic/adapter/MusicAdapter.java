package com.whitelaning.mmusic.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.whitelaning.mmusic.R;
import com.whitelaning.mmusic.activity.MainActivity;
import com.whitelaning.mmusic.list.FavoriteList;
import com.whitelaning.mmusic.list.FolderList;
import com.whitelaning.mmusic.list.MusicList;

public class MusicAdapter extends BaseAdapter implements OnClickListener {

	private int page = MainActivity.SLIDING_MENU_ALL;// ----默认值
	private int folderPosition;
	private Context mContext;
	private LayoutInflater mInflater;
	private int selectItem = -1;
	private int currentPosition = -1;
	
	/**
	 * 构造函数
	 * 
	 * @param context
	 * @param page
	 */
	public MusicAdapter(Context context, int page) {

		this.mContext = context;
		this.page = page;
		this.mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {

		int count = 0;
		switch (page) {
		case MainActivity.SLIDING_MENU_ALL:
			count = MusicList.list.size();
			break;

		case MainActivity.SLIDING_MENU_FAVORITE:
			count = FavoriteList.list.size();
			break;

		case MainActivity.SLIDING_MENU_FOLDER:
			count = FolderList.list.size();
			break;

		case MainActivity.SLIDING_MENU_FOLDER_LIST:
			count = FolderList.list.get(folderPosition).getMusicList().size();
			break;
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.adapter_music_item, null);
			holder.view = (LinearLayout) convertView
					.findViewById(R.id.adapter_music_item_view);
			holder.favorite = (ImageView) convertView
					.findViewById(R.id.adapter_music_item_ib_favorite);
			holder.name = (TextView) convertView
					.findViewById(R.id.adapter_music_item_tv_name);
			holder.artist = (TextView) convertView
					.findViewById(R.id.adapter_music_item_tv_artist);
			holder.time = (TextView) convertView
					.findViewById(R.id.adapter_music_item_tv_time);
			holder.menu = (ImageButton) convertView
					.findViewById(R.id.adapter_music_item_ib_menu);
			holder.folder = (TextView) convertView
					.findViewById(R.id.adapter_music_item_tv_folder);
			holder.playView = (View) convertView
					.findViewById(R.id.adapter_music_item_play);

			holder.removeRelativeLayout = (RelativeLayout) convertView
					.findViewById(R.id.item_move);
			holder.deleteRelativeLayout = (RelativeLayout) convertView
					.findViewById(R.id.item_delete);
			holder.infoRelativeLayout = (RelativeLayout) convertView
					.findViewById(R.id.item_info);
			holder.hideLinearLayout = (LinearLayout) convertView
					.findViewById(R.id.hide_item);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		switch (page) {
		case MainActivity.SLIDING_MENU_ALL:
			if (holder.view.getVisibility() != View.VISIBLE) {
				holder.view.setVisibility(View.VISIBLE);
			}
			holder.name.setText((position + 1) + ". "
					+ MusicList.list.get(position).getName());
			holder.artist.setText(MusicList.list.get(position).getArtist());
			holder.time.setText(MusicList.list.get(position).getTime());
			holder.favorite.setImageResource(MusicList.list.get(position)
					.isFavorite() ? R.drawable.music_item_btn_favourite_pressed
					: R.drawable.music_item_btn_favourite_normal);
			if (holder.folder.getVisibility() == View.VISIBLE) {
				holder.folder.setVisibility(View.GONE);
			}

			holder.menu.setTag(position);
			holder.favorite.setTag(position);
			holder.menu.setOnClickListener(this);
			holder.favorite.setOnClickListener(this);
			break;

		case MainActivity.SLIDING_MENU_FAVORITE:
			if (holder.view.getVisibility() != View.VISIBLE) {
				holder.view.setVisibility(View.VISIBLE);
			}
			holder.name.setText((position + 1) + ". "
					+ FavoriteList.list.get(position).getName());
			holder.artist.setText(FavoriteList.list.get(position).getArtist());
			holder.time.setText(FavoriteList.list.get(position).getTime());
			holder.favorite.setImageResource(FavoriteList.list.get(position)
					.isFavorite() ? R.drawable.music_item_btn_favourite_pressed
					: R.drawable.music_item_btn_favourite_normal);
			if (holder.folder.getVisibility() == View.VISIBLE) {
				holder.folder.setVisibility(View.GONE);
			}

			holder.menu.setTag(position);
			holder.favorite.setTag(position);
			holder.menu.setOnClickListener(this);
			holder.favorite.setOnClickListener(this);
			break;

		case MainActivity.SLIDING_MENU_FOLDER:
			if (holder.folder.getVisibility() != View.VISIBLE) {
				holder.folder.setVisibility(View.VISIBLE);
			}
			if (holder.view.getVisibility() == View.VISIBLE) {
				holder.view.setVisibility(View.GONE);
			}
			holder.folder.setText(FolderList.list.get(position)
					.getMusicFolder());
			break;

		case MainActivity.SLIDING_MENU_FOLDER_LIST:
			if (holder.view.getVisibility() != View.VISIBLE) {
				holder.view.setVisibility(View.VISIBLE);
			}
			holder.name.setText((position + 1)
					+ ". "
					+ FolderList.list.get(folderPosition).getMusicList()
							.get(position).getName());
			holder.artist.setText(FolderList.list.get(folderPosition)
					.getMusicList().get(position).getArtist());
			holder.time.setText(FolderList.list.get(folderPosition)
					.getMusicList().get(position).getTime());
			holder.favorite.setImageResource(FolderList.list
					.get(folderPosition).getMusicList().get(position)
					.isFavorite() ? R.drawable.music_item_btn_favourite_pressed
					: R.drawable.music_item_btn_favourite_normal);
			if (holder.folder.getVisibility() == View.VISIBLE) {
				holder.folder.setVisibility(View.GONE);
			}

			holder.menu.setTag(position);
			holder.favorite.setTag(position);
			holder.menu.setOnClickListener(this);
			holder.favorite.setOnClickListener(this);
			break;
		}

		if (position == selectItem) {
			holder.playView.setBackgroundColor(0xffb0120a);
		} else {
			holder.playView.setBackgroundColor(0xffeeeeee);
		}

		if (position == currentPosition) {

			holder.hideLinearLayout.setVisibility(View.VISIBLE);
			holder.removeRelativeLayout.setClickable(true);
			holder.deleteRelativeLayout.setClickable(true);
			holder.infoRelativeLayout.setClickable(true);
			holder.removeRelativeLayout
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = null;
							intent = new Intent(
									MainActivity.BROADCAST_ACTION_MENU_REMOVE);
							intent.putExtra("currentPosition", currentPosition);
							mContext.sendBroadcast(intent);	
							
							currentPosition = -1;
						}
					});
			holder.deleteRelativeLayout
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = null;
							intent = new Intent(
									MainActivity.BROADCAST_ACTION_MENU_DELETE);
							intent.putExtra("currentPosition", currentPosition);
							mContext.sendBroadcast(intent);
							currentPosition = -1;
						}
					});
			holder.infoRelativeLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = null;
					intent = new Intent(MainActivity.BROADCAST_ACTION_MENU_INFO);
					intent.putExtra("currentPosition", currentPosition);
					mContext.sendBroadcast(intent);
					currentPosition = -1;
				}
			});
		} else {
			holder.hideLinearLayout.setVisibility(View.GONE);
		}

		return convertView;
	}


	/**
	 * 设置more按钮点击事件的Postion，显示隐藏菜单
	 * 
	 * @param currentPosition
	 */
	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

	/**
	 * 设置item点击事件的Postion，高亮处理
	 * 
	 * @param selectItem
	 */
	public void setSelectItem(int selectItem) {
		this.selectItem = selectItem;
	}

	// private int selectItem = -1;

	static class ViewHolder {

		View playView;
		LinearLayout hideLinearLayout;
		RelativeLayout removeRelativeLayout;
		RelativeLayout deleteRelativeLayout;
		RelativeLayout infoRelativeLayout;
		LinearLayout view;
		ImageView favorite;
		ImageButton menu;
		TextView name;
		TextView artist;
		TextView time;
		TextView folder;
	}

	/**
	 * 刷新数据
	 * 
	 * @param page
	 *            传入的界面ID
	 */
	public void update(int page) {
		this.page = page;
		notifyDataSetChanged();
	}

	public void setFolderPosition(int position) {
		this.folderPosition = position;
	}

	public int getPage() {
		return page;
	}

	@Override
	public void onClick(View v) {

		Intent intent = null;
		switch (v.getId()) {
		case R.id.adapter_music_item_ib_favorite:// ----标记为我的最爱
			intent = new Intent(MainActivity.BROADCAST_ACTION_FAVORITE);
			break;

		case R.id.adapter_music_item_ib_menu:// ----弹出菜单
			intent = new Intent(MainActivity.BROADCAST_ACTION_MENU);
			break;
		}
		if (intent != null) {
			intent.putExtra(MainActivity.BROADCAST_INTENT_PAGE, page);
			intent.putExtra(MainActivity.BROADCAST_INTENT_POSITION,
					(Integer) v.getTag());
			mContext.sendBroadcast(intent);
		}
	}

}
