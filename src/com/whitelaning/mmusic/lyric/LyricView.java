package com.whitelaning.mmusic.lyric;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

//----绘画歌词，自动滚动效果--------------------

public class LyricView extends TextView {

	private int index = 0;//----索引，第几句歌词
	public static  int lyricSize = 0;//----歌词总句数

	private int currentTime = 0;//----当前歌曲的播放位置
	private int dunringTime = 0;//----当前句歌词的持续时间
	private int startTime = 0;//----当前句歌词开始的时间

	private float height = 0;//----获得的画布高
	private float tempW = 0;//----计算画布的中间位置(宽)
	private float tempH = 0;//----计算画布的中间位置(高)


	private float textHeight = 35;//----单行字高度
	private float textSize = 20;//----字体大小

	private Paint currentPaint = null;//----当前句画笔
	private Paint defaultPaint = null;//----非当前句画笔
	private List<LyricItem> mSentenceEntities = new ArrayList<LyricItem>();
	
	private boolean isKLOK = false;
	
	public static int getLyricSize() {
		return lyricSize ;
	}

	public LyricView(Context context) {
		super(context);

		init();
	}

	public LyricView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init();
	}

	public LyricView(Context context, AttributeSet attrs) {
		super(context, attrs);
	
		init();
	}

	private void init() {
	
		setFocusable(true);

		//----高亮部分
		currentPaint = new Paint();
		currentPaint.setAntiAlias(true);
		currentPaint.setTextAlign(Paint.Align.CENTER);
		currentPaint.setColor(Color.argb(200, 255, 33, 64));
		currentPaint.setTextSize(textSize);
		currentPaint.setTypeface(Typeface.SERIF);

		//----非高亮部分
		defaultPaint = new Paint();
		defaultPaint.setAntiAlias(true);
		defaultPaint.setTextAlign(Paint.Align.CENTER);
		defaultPaint.setColor(Color.argb(200, 171, 175, 179));
		defaultPaint.setTextSize(textSize);
		defaultPaint.setTypeface(isKLOK ? Typeface.SERIF : Typeface.DEFAULT);
	}


	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);

		if (canvas == null || getLyricSize() <= 0
				|| index >= mSentenceEntities.size()) {
				return ;
		}else{
			float plus = dunringTime == 0 ? 0
					: (((float) currentTime - (float) startTime) / (float) dunringTime)
							* (float) 30;
			//----向上滚动 这个是根据歌词的时间长短来滚动，整体上移
			canvas.translate(0, -plus);

			try {
				canvas.drawText(mSentenceEntities.get(index).getLyric(), tempW,
						tempH, currentPaint);

				float tempY = tempH;
				//----画出本句之前的句子
				for (int i = index - 1; i >= 0; i--) {
					//----向上推移
					tempY = tempY - textHeight;

					canvas.drawText(mSentenceEntities.get(i).getLyric(), tempW,
							tempY, defaultPaint);
				}
				tempY = tempH;
				//----画出本句之后的句子
				for (int i = index + 1; i < getLyricSize(); i++) {
					//----往下推移
					tempY = tempY + textHeight;
					if (tempY > height) {
						break;
					}
					canvas.drawText(mSentenceEntities.get(i).getLyric(), tempW,
							tempY, defaultPaint);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	
		super.onSizeChanged(w, h, oldw, oldh);

		this.height = h;
		this.tempW = w / 2;
		this.tempH = h / 2;

	}

	//----更改歌词高亮色----------------------------

	public void setLyricHighlightColor(int color) {
		currentPaint.setColor(color);
	}

	//---引进歌词---------------------------------------

	public void setSentenceEntities(List<LyricItem> mSentenceEntities) {
		this.mSentenceEntities = mSentenceEntities;
		LyricView.setLyricSize(mSentenceEntities.size());
	}

	//----歌词索引信息{歌词索引,当前歌曲的播放位置,当前句歌词的持续时间,当前句歌词开始的时间}-----------

	public void setIndex(int[] indexInfo) {
		this.index = indexInfo[0];
		this.currentTime = indexInfo[1];
		this.startTime = indexInfo[2];
		this.dunringTime = indexInfo[3];
	}

	//----清空操作-----------------------------------
	public void clear() {
		this.mSentenceEntities.clear();
		this.index = 0;
		LyricView.setLyricSize(0);
		this.invalidate();
	}

	public static void setLyricSize(int lyricSize) {
		LyricView.lyricSize = lyricSize;
	}
}
