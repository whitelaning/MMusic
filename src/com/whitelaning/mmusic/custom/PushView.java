package com.whitelaning.mmusic.custom;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class PushView extends TextSwitcher implements ViewSwitcher.ViewFactory {

	private int index;
	private int size;

	private AttributeSet attrs;
	private ArrayList<String> arrays;//----�ַ�����

	public PushView(Context context) {
		super(context);

		init();
	}

	public PushView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.attrs = attrs;
		init();
	}

	//----��ʼ��----------------------------
	private void init() {

		index = 0;
		arrays = new ArrayList<String>();
		setFactory(this);
		setInAnimation(animIn());
		setOutAnimation(animOut());
	}

	//----���붯��---------------------------

	private Animation animIn() {
		TranslateAnimation anim = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 1.0f,
				Animation.RELATIVE_TO_PARENT, -0.0f);
		anim.setDuration(1500);
		anim.setInterpolator(new LinearInterpolator());
		return anim;
	}

	//----�뿪����------------------------------
	 
	private Animation animOut() {
		TranslateAnimation anim = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, -1.0f);
		anim.setDuration(1500);
		anim.setInterpolator(new LinearInterpolator());
		return anim;
	}

	//----����TextView�����ǿ�����View------------
	@Override
	public View makeView() {

		MarqueeTextView t = new MarqueeTextView(getContext(), attrs);
		t.setLayoutParams(new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT));
		return t;//----XML����ֱ������TextView������ʹ�ã�����LayoutParams���ܴ�ֱ����
	}

	@Override
	protected void onDetachedFromWindow() {
	
		try {
			removeCallbacks(runnable);//----ҳ������ʱ��Ҫ�Ƴ�
			super.onDetachedFromWindow();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	//----�����ַ�����������������Ч��-------------------------------------
	
	public void setTextList(ArrayList<String> texts) {
		removeCallbacks(runnable);
		this.index = 0;
		this.size = texts.size();
		this.arrays.clear();
		this.arrays = texts;
		setText(null);
		postDelayed(runnable, 500);
	}

	private Runnable runnable = new Runnable() {

		@Override
		public void run() {

			setText(arrays.get(index));
			if (size > 1) {
				index = (index == size - 1) ? 0 : index + 1;
				postDelayed(this, 5000);
			}
		}
	};

	//----���û�ý���ά�������Ч��-------------------------------------------
	private class MarqueeTextView extends TextView {

		public MarqueeTextView(Context context) {
			super(context);
		
		}

		public MarqueeTextView(Context context, AttributeSet attrs) {
			super(context, attrs);
		
		}

		public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		
		}

		@Override
		public boolean isFocused() {
		
			return true;//----���û�ý���
		}
	}

}
