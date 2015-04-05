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
	private ArrayList<String> arrays;//----字符串集

	public PushView(Context context) {
		super(context);

		init();
	}

	public PushView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.attrs = attrs;
		init();
	}

	//----初始化----------------------------
	private void init() {

		index = 0;
		arrays = new ArrayList<String>();
		setFactory(this);
		setInAnimation(animIn());
		setOutAnimation(animOut());
	}

	//----进入动画---------------------------

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

	//----离开动画------------------------------
	 
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

	//----返回TextView，就是看到的View------------
	@Override
	public View makeView() {

		MarqueeTextView t = new MarqueeTextView(getContext(), attrs);
		t.setLayoutParams(new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT));
		return t;//----XML可以直接引用TextView的属性使用，加上LayoutParams才能垂直居中
	}

	@Override
	protected void onDetachedFromWindow() {
	
		try {
			removeCallbacks(runnable);//----页面销毁时定要移除
			super.onDetachedFromWindow();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	//----设置字符串集，并开启动画效果-------------------------------------
	
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

	//----永久获得焦点维持跑马灯效果-------------------------------------------
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
		
			return true;//----永久获得焦点
		}
	}

}
