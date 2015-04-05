package com.whitelaning.mmusic.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.whitelaning.mmusic.R;
import com.whitelaning.mmusic.entity.ScanInfo;

public class ScanAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<ScanInfo> data;

	public ScanAdapter(Context context, List<ScanInfo> data) {

		this.inflater = LayoutInflater.from(context);
		this.data = data;
	}

	@Override
	public int getCount() {
		
		return data.size();
	}

	@Override
	public Object getItem(int position) {

		return null;
	}

	@Override
	public long getItemId(int position) {

		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.adapter_scan_item, null);
			holder.checkBox = (CheckBox) convertView
					.findViewById(R.id.adapter_scan_item_cb);
			holder.textView = (TextView) convertView
					.findViewById(R.id.adapter_scan_item_tv);
			holder.checkBox
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							
							data.get(position).setChecked(isChecked);
						}
					});
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.textView.setText(data.get(position).getFolderPath());
		holder.checkBox.setChecked(data.get(position).isChecked());

		return convertView;
	}

	static class ViewHolder {
		CheckBox checkBox;
		TextView textView;
	}

	//----返回用户勾选的路径--------------------------------
	public List<String> getPath() {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).isChecked()) {
				list.add(data.get(i).getFolderPath());
			}
		}
		return list;
	}
}
