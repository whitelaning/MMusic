package com.whitelaning.mmusic.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.whitelaning.mmusic.R;

@SuppressLint("Recycle") public class SlidingAdapter extends BaseAdapter {

	private TypedArray icons;
	private String[] texts;
	private int white;

	public SlidingAdapter(Context context) {

		Resources res = context.getResources();
		icons = res.obtainTypedArray(R.array.sliding_list_icon);
		texts = res.getStringArray(R.array.sliding_list_text);
		white = res.getColor(R.color.white);
	}

	@Override
	public int getCount() {
		
		return icons.length();
	}

	@Override
	public Object getItem(int position) {

		return null;
	}

	@Override
	public long getItemId(int position) {

		return 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView;
		if (convertView == null) {
			textView = new TextView(parent.getContext());
			textView.setLayoutParams(new ListView.LayoutParams(
					ListView.LayoutParams.FILL_PARENT, 70));//----���ÿ��
			textView.setPadding(20, 0, 0, 0);//----���ü��
			textView.setCompoundDrawablePadding(20);//----����ͼƬ�����ֵļ��
			textView.setGravity(Gravity.CENTER_VERTICAL);//----��ֱ����
			textView.setTextColor(white);//----��ɫ
			textView.setTextSize(16);//----��С
		} else {
			textView = (TextView) convertView;
		}

		textView.setText(texts[position]);
		textView.setCompoundDrawablesWithIntrinsicBounds(
				icons.getDrawable(position), null, null, null);

		return textView;
	}

}
