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

//----�滭��ʣ��Զ�����Ч��--------------------

public class LyricView extends TextView {

	private int index = 0;//----�������ڼ�����
	public static  int lyricSize = 0;//----����ܾ���

	private int currentTime = 0;//----��ǰ�����Ĳ���λ��
	private int dunringTime = 0;//----��ǰ���ʵĳ���ʱ��
	private int startTime = 0;//----��ǰ���ʿ�ʼ��ʱ��

	private float height = 0;//----��õĻ�����
	private float tempW = 0;//----���㻭�����м�λ��(��)
	private float tempH = 0;//----���㻭�����м�λ��(��)


	private float textHeight = 35;//----�����ָ߶�
	private float textSize = 20;//----�����С

	private Paint currentPaint = null;//----��ǰ�仭��
	private Paint defaultPaint = null;//----�ǵ�ǰ�仭��
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

		//----��������
		currentPaint = new Paint();
		currentPaint.setAntiAlias(true);
		currentPaint.setTextAlign(Paint.Align.CENTER);
		currentPaint.setColor(Color.argb(200, 255, 33, 64));
		currentPaint.setTextSize(textSize);
		currentPaint.setTypeface(Typeface.SERIF);

		//----�Ǹ�������
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
			//----���Ϲ��� ����Ǹ��ݸ�ʵ�ʱ�䳤������������������
			canvas.translate(0, -plus);

			try {
				canvas.drawText(mSentenceEntities.get(index).getLyric(), tempW,
						tempH, currentPaint);

				float tempY = tempH;
				//----��������֮ǰ�ľ���
				for (int i = index - 1; i >= 0; i--) {
					//----��������
					tempY = tempY - textHeight;

					canvas.drawText(mSentenceEntities.get(i).getLyric(), tempW,
							tempY, defaultPaint);
				}
				tempY = tempH;
				//----��������֮��ľ���
				for (int i = index + 1; i < getLyricSize(); i++) {
					//----��������
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

	//----���ĸ�ʸ���ɫ----------------------------

	public void setLyricHighlightColor(int color) {
		currentPaint.setColor(color);
	}

	//---�������---------------------------------------

	public void setSentenceEntities(List<LyricItem> mSentenceEntities) {
		this.mSentenceEntities = mSentenceEntities;
		LyricView.setLyricSize(mSentenceEntities.size());
	}

	//----���������Ϣ{�������,��ǰ�����Ĳ���λ��,��ǰ���ʵĳ���ʱ��,��ǰ���ʿ�ʼ��ʱ��}-----------

	public void setIndex(int[] indexInfo) {
		this.index = indexInfo[0];
		this.currentTime = indexInfo[1];
		this.startTime = indexInfo[2];
		this.dunringTime = indexInfo[3];
	}

	//----��ղ���-----------------------------------
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
