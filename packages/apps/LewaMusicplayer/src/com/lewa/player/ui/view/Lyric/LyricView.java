package com.lewa.player.ui.view.Lyric;

import java.util.ArrayList;
import java.util.List;

import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;


public class LyricView extends TextView {
	private Paint NotCurrentPaint; // �ǵ�ǰ��ʻ���
	private Paint CurrentPaint; // ��ǰ��ʻ���
	private Paint MovingCurrentPaint;
	private Paint MovingLinePaint;
	private float previousX = 0;
	private float previousY = 0;
	private float moveScroll = 0;
	private long movingCurrentTime = 0; 
	private boolean onmove = false;
	private int hrindex;

	private int notCurrentPaintColor = Color.GRAY;// �ǵ�ǰ��ʻ��� ��ɫ
	private int CurrentPaintColor = Color.RED; // ��ǰ��ʻ��� ��ɫ
	private int MovingCurrentPaintColor = Color.WHITE;
	private int MovingLinePaintColor = Color.WHITE;
	private Typeface Texttypeface = Typeface.SERIF;
	private Typeface CurrentTexttypeface = Typeface.DEFAULT_BOLD;
	private float width;
	private static Lyric mLyric;
	private int brackgroundcolor = 0xf000000; // ������ɫ
	private float lrcTextSize = 0; // ��ʴ�С
	private float CurrentTextSize = 0;
	private float SecondSize = 22;
	private float ThridSize = 16;
	private float ForthSize = 12;
	private int speedScroll = 20;
	private int indexBeforeMove = 0;
	private IMediaPlaybackService mService;
	long sct;
	int sctmove;
	// private Align = Paint.Align.CENTER��

	public float mTouchHistoryY;

	private int height;
	private long currentDunringTime; // ��ǰ�и�ʳ�����ʱ�䣬�ø�ʱ����sleep
	// private float middleY;// y���м�
	private int TextHeight = 20; // ÿһ�еļ��
	private boolean lrcInitDone = false;// �Ƿ��ʼ�������
	public int index = 0;
	private int lastIndex = 0;
	
	private int sentenceNumber = 0;
	private List<Sentence> Sentencelist; // ����б�

	private long currentTime;

	private long sentenctTime;
	
	public void setService(IMediaPlaybackService mService){
		this.mService = mService;
	}

	public Paint getNotCurrentPaint() {
		return NotCurrentPaint;
	}

	public void setNotCurrentPaint(Paint notCurrentPaint) {
		NotCurrentPaint = notCurrentPaint;
	}

	public boolean isLrcInitDone() {
		return lrcInitDone;
	}

	public Typeface getCurrentTexttypeface() {
		return CurrentTexttypeface;
	}

	public void setCurrentTexttypeface(Typeface currrentTexttypeface) {
		CurrentTexttypeface = currrentTexttypeface;
	}

	public void setLrcInitDone(boolean lrcInitDone) {
		this.lrcInitDone = lrcInitDone;
	}

	public float getLrcTextSize() {
		return lrcTextSize;
	}

	public void setLrcTextSize(float lrcTextSize) {
		this.lrcTextSize = lrcTextSize;
	}

	public float getCurrentTextSize() {
		return CurrentTextSize;
	}

	public void setCurrentTextSize(float currentTextSize) {
		CurrentTextSize = currentTextSize;
	}

	public static Lyric getmLyric() {
		return mLyric;
	}

	public void setmLyric(Lyric mLyric) {
		LyricView.mLyric = mLyric;
	}

	public Paint getCurrentPaint() {
		return CurrentPaint;
	}

	/*public void setCurrentPaint(Paint currentPaint) {
		CurrentPaint = currentPaint;
	}*/

	public List<Sentence> getSentencelist() {
		return Sentencelist;
	}

	public void setSentencelist(List<Sentence> sentencelist) {
		Sentencelist = sentencelist;
		sentenceNumber = Sentencelist.size();
	}

	public int getNotCurrentPaintColor() {
		return notCurrentPaintColor;
	}

	public void setNotCurrentPaintColor(int notCurrentPaintColor) {
		this.notCurrentPaintColor = notCurrentPaintColor;
	}

	public int getCurrentPaintColor() {
		return CurrentPaintColor;
	}

	public void setCurrentPaintColor(int currrentPaintColor) {
		CurrentPaintColor = currrentPaintColor;
	}

	public Typeface getTexttypeface() {
		return Texttypeface;
	}

	public void setTexttypeface(Typeface texttypeface) {
		Texttypeface = texttypeface;
	}

	public int getBrackgroundcolor() {
		return brackgroundcolor;
	}

	public void setBrackgroundcolor(int brackgroundcolor) {
		this.brackgroundcolor = brackgroundcolor;
	}

	public int getTextHeight() {
		return TextHeight;
	}

	public void setTextHeight(int textHeight) {
		TextHeight = textHeight;
	}

	public LyricView(Context context, int dip) {
		super(context);
		init();
		setPram(dip);
	}

	private void setPram(int dip) {
		// TODO Auto-generated method stub
	    lrcTextSize = getResources().getDimensionPixelOffset(R.dimen.lrc_text_size);
		CurrentTextSize = getResources().getDimensionPixelOffset(R.dimen.lrc_current_text_size);
		TextHeight = getResources().getDimensionPixelOffset(R.dimen.lrc_text_height);
	    speedScroll = getResources().getDimensionPixelOffset(R.dimen.lrc_speed_scroll);
	}

	public LyricView(Context context, AttributeSet attr) {
		super(context, attr);
		init();
	}

	public LyricView(Context context, AttributeSet attr, int i) {
		super(context, attr, i);
		init();
	}

	private void init() {
		setFocusable(true);

		// �Ǹ�������
		NotCurrentPaint = new Paint();
		NotCurrentPaint.setAntiAlias(true);
		
		NotCurrentPaint.setTextAlign(Paint.Align.CENTER);

		// �������� ��ǰ���
		CurrentPaint = new Paint();
		CurrentPaint.setAntiAlias(true);
		// CurrentPaint.setColor(CurrentPaintColor);
		
		CurrentPaint.setTextAlign(Paint.Align.CENTER);
		// list = mLyric.list;
		
		MovingCurrentPaint = new Paint();
		MovingCurrentPaint.setAntiAlias(true);
		MovingCurrentPaint.setTextAlign(Paint.Align.CENTER);
		
		this.MovingLinePaint = new Paint();

		MovingLinePaint.setAntiAlias(true);
		MovingLinePaint.setTextAlign(Paint.Align.LEFT);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int totalHeight = this.getHeight();
		final int iAction = event.getAction();
		final float iCurrentx = event.getX();
		final float iCurrenty = event.getY();
		
		switch(iAction){
			case MotionEvent.ACTION_DOWN:
				previousX = iCurrentx;
				previousY = iCurrenty;
				indexBeforeMove = index;
				//Log.i("MotionEvent","ACTION_DOWN");
				moveScroll = 0;
				sct = currentTime - sentenctTime;
				if(currentDunringTime == 0){
					sctmove = 0;
				}else{
					sctmove =  (int )(20*sct / currentDunringTime);
				}
				movingCurrentTime = 0;
				break;
			case MotionEvent.ACTION_MOVE:
								
				onmove = true;
				float moved = iCurrenty - previousY;
				moveScroll = moved ;
				
				
				break;
			case MotionEvent.ACTION_UP:
					moveScroll = 0;
				//Log.i("currentTime",""+currentTime);
				/*if (index != -1) {
					Sentence sen = Sentencelist.get(index);
					
					sentenctTime = sen.getFromTime();
					
					currentDunringTime = sen.getDuring();
					currentTime = sen.getFromTime();
					Log.i("currentTime",""+currentTime);
					Log.i("sentenctTime",""+sentenctTime);
					Log.i("currentDunringTime",""+currentDunringTime);
				}*/
				
				try {
					//if(movingCurrentTime == 0){
					//	mService.seek(currentTime);
					//}else{
						mService.seek(this.Sentencelist.get(hrindex).getFromTime());
					//}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				movingCurrentTime = 0;
				
				onmove = false;
				break;
			case MotionEvent.ACTION_CANCEL:
				//Log.i("MotionEvent","cancel");
				onmove = false;
				break;
			default:
				break;
			
		}
		return true;
	}
	
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// Log.e("Update", "onDraw");
		canvas.drawColor(brackgroundcolor);
		NotCurrentPaint.setColor(notCurrentPaintColor);
		CurrentPaint.setColor(CurrentPaintColor);
		MovingCurrentPaint.setColor(MovingCurrentPaintColor);
		MovingLinePaint.setColor(MovingLinePaintColor);
		
		NotCurrentPaint.setTextSize(lrcTextSize);
		// NotCurrentPaint.setColor(notCurrentPaintColor);
		NotCurrentPaint.setTypeface(Texttypeface);

		CurrentPaint.setTextSize(lrcTextSize);
		CurrentPaint.setTypeface(Texttypeface);
		
		MovingCurrentPaint.setTextSize(lrcTextSize);
		MovingCurrentPaint.setTypeface(Texttypeface);
		
		MovingLinePaint.setTextSize(lrcTextSize);
		MovingLinePaint.setTypeface(CurrentTexttypeface);
		
		
		// // ���ȿ��ǲ��ǳ�ʼ������� 
		// if (!Lyric.initDone) {
		// Sentence temp = new Sentence("Search Lyric...");
		// canvas.drawText(temp.getContent(), width / 2, height / 2,
		// CurrentPaint);
		// return;
		// }

		
		float plus = currentDunringTime == 0 ? speedScroll
				: speedScroll
						+ (((float) currentTime - (float) sentenctTime) / (float) currentDunringTime)
						* (float) speedScroll;

		float tempY = height / 2;
		if(onmove && movingCurrentTime / 1000 != 0){
			//canvas.drawLine(0, tempY - speedScroll, getWidth(), tempY-speedScroll, MovingLinePaint);
			//drawText(currentTime+"", canvas, MovingCurrentPaint, tempY, false);
			canvas.drawText(MusicUtils.makeTimeString(mContext, movingCurrentTime / 1000)+"", 30, tempY, MovingCurrentPaint);
		}
		// ���Ϲ��� ����Ǹ��ݸ�ʵ�ʱ�䳤������������������
		
		/*if(onmove){
			//Log.i("moveScroll",moveScroll+"");
			canvas.translate(0, -plus + moveScroll);
		}else{
			canvas.translate(0, -plus);
		}*/
		
		float moveFromSentenceStart;
		int moveHeightSentence;
		hrindex = index;
		float movingScroll = moveScroll;
		if(onmove){
			moveFromSentenceStart = moveScroll - plus;
			moveHeightSentence = -(int)Math.ceil(moveFromSentenceStart/speedScroll);
			
			if(moveHeightSentence < 0 ){
				int mhs = moveHeightSentence;
				int count = 0;
				while(mhs<0){
					
					count++;
					if(index - count < 0){
						count = index;
						break;
					}
					//Log.i("hrindex",(index - count)+"");
					List<String> str = autoSplit(Sentencelist.get(index-count).getContent(), NotCurrentPaint, getWidth() - 60);
					
					mhs += str.size();  
				}
				
				
				hrindex = index - count ;
				
				float leftTimeScroll = speedScroll - (-moveHeightSentence*speedScroll - moveFromSentenceStart);
				
				String hrContent =  Sentencelist.get(hrindex).getContent();
				int strSize = autoSplit(hrContent, NotCurrentPaint, getWidth() - 60).size();
				
				float leftTime = leftTimeScroll * Sentencelist.get(hrindex).getDuring() / (strSize * speedScroll);
				
				//Log.i("leftTime",""+leftTime);
				//float leftTime = leftTimeScroll * 
				movingCurrentTime = (long) (Sentencelist.get(hrindex ).getToTime() - leftTime);
				Log.i("movingCurrentTime",movingCurrentTime+"");
			}else{
				int mhs = 0;
				int count = 0;
				while(mhs <= moveHeightSentence){
					if (index+count >=  Sentencelist.size()){
						count = Sentencelist.size()   - index;
						break;
					}
					List<String> str = autoSplit(Sentencelist.get(index+count).getContent(), NotCurrentPaint, getWidth() - 60);
					mhs += str.size();
					count++;
				}
				
				count --;
				hrindex = index + count ;
				
				float leftTimeScroll = - moveFromSentenceStart - speedScroll * moveHeightSentence;
				String hrContent =  Sentencelist.get(hrindex).getContent();
				int strSize = autoSplit(hrContent, NotCurrentPaint, getWidth() - 60).size();
				float leftTime = leftTimeScroll * Sentencelist.get(hrindex).getDuring() / (strSize * speedScroll);
				movingCurrentTime = (long) (Sentencelist.get(hrindex ).getFromTime() + leftTime);
			}
			
		}
		
		if(onmove){
			//Log.i("moveScroll",moveScroll+"");
			canvas.translate(0, -plus + moveScroll);
		}else{
			canvas.translate(0, -plus);
		}
		
		//Log.i("currentDunringTime",currentDunringTime+"");
		// �Ȼ���ǰ�У�֮���ٻ�����ǰ��ͺ��棬�����ͱ��ֵ�ǰ�����м��λ��
		
		try {
			String content = "";
			if(index > 0) {
				content = Sentencelist.get(index).getContent();	
			}else {
				content = "   ";
			}
			ArrayList<String> texts;
			//ArrayList<String> texts = autoSplit(content, CurrentPaint, getWidth() - 60);
			if(onmove){
				if(index == hrindex){
					texts = autoSplit(content, MovingCurrentPaint, getWidth() - 60);
					tempY = drawText(texts, canvas, MovingCurrentPaint, tempY, false);
				}else{
					texts = autoSplit(content, NotCurrentPaint, getWidth() - 60);
					tempY = drawText(texts, canvas, NotCurrentPaint, tempY, false);
				}
				
				//canvas.drawl
			}else{
				texts = autoSplit(content, CurrentPaint, getWidth() - 60);
				tempY = drawText(texts, canvas, CurrentPaint, tempY, false);
				
			}
			// canvas.translate(0, plus);

			float baseY = tempY;
			tempY = height / 2;
			// ��������֮ǰ�ľ���
			for (int i = index - 1; i >= 0; i--) {
				// Sentence sen = list.get(i);
				// ��������
				tempY = (float) (tempY - TextHeight);
				
				if (!onmove && tempY < 0) {
					break;
				}
				
				
				if(index > 0) {
					content = Sentencelist.get(i).getContent();	
				}else {
					content = "   ";
				}
				
				if(onmove && i == hrindex){
					texts = autoSplit(content, MovingCurrentPaint, getWidth() - 60);
					tempY = drawText(texts, canvas, MovingCurrentPaint, tempY, true);
				}else{
					texts = autoSplit(content, NotCurrentPaint, getWidth() - 60);
					tempY = drawText(texts, canvas, NotCurrentPaint, tempY, true);
				}

			}

			tempY = baseY;
			// ��������֮��ľ���
			//Log.i("size",this.Sentencelist.size()+"");
			for (int j = index + 1; j < this.Sentencelist.size(); j++) {
				// ��������
				tempY = (float) (tempY + TextHeight);
				if (!onmove && tempY > height) {
					break;
				}
				/*if(j == index + 1) {
					NotCurrentPaint.setTextSize(SecondSize);
				}else if(j == index + 2){
					NotCurrentPaint.setTextSize(ThridSize);
				}else if(j >= index + 3){
					NotCurrentPaint.setTextSize(ForthSize);
				}*/
				if(index > 0) {
					content = Sentencelist.get(j).getContent();	
				}else {
					content = "   ";
				}

				if(onmove && j == hrindex){
					texts = autoSplit(content, MovingCurrentPaint, getWidth() - 60);
					tempY = drawText(texts, canvas, MovingCurrentPaint, tempY, false);
				}else{
					texts = autoSplit(content, NotCurrentPaint, getWidth() - 60);
					tempY = drawText(texts, canvas, NotCurrentPaint, tempY, false);
				}
				//Log.i("j",j+"");
			}
			


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	

	protected void onSizeChanged(int w, int h, int ow, int oh) {
		super.onSizeChanged(w, h, ow, oh);
		width = w; // remember the center of the screen
		height = h;
		// middleY = h * 0.5f;
	}
	
	public float drawText(ArrayList<String> texts, Canvas canvas, Paint paint, float y, boolean ifUp) {
	    int textsLen = texts.size();
	    int s = 0;
	    if(ifUp) {
            y -= TextHeight * (textsLen - 1);
        }
	    while(s < textsLen) {
	        canvas.drawText(texts.get(s), width / 2, y, paint);
	        if(s < textsLen - 1) {
	            y += TextHeight;
	        }
	        s++;
	    }
	    if(ifUp) {
	        y -= TextHeight * (textsLen - 1);
	    }
	    return y;
	}
	
	/** 
     * �Զ��ָ��ı� 
     * @param content ��Ҫ�ָ���ı� 
     * @param p  ���ʣ�����������������ı��Ŀ�� 
     * @param width ָ���Ŀ�� 
     * @return һ���ַ���ArrayList������ÿ�е��ı� 
     */  
    private ArrayList<String> autoSplit(String content, Paint p, float width) {  
        int length = content.length();  
        float textWidth = p.measureText(content); 
        ArrayList<String> lineTexts = new ArrayList<String>();
        if(textWidth <= width) {
            lineTexts.add(content);
            return lineTexts;  
        }  
          
        int start = 0, end = 1, i = 0;  
//        int lines = (int) Math.ceil(textWidth / width); //��������   
        
        while(start < length) {  
            if(p.measureText(content, start, end) > width) { //�ı���ȳ����ؼ����ʱ   
                lineTexts.add((String) content.subSequence(start, end));  
                start = end;  
            }
            if(end == length) { //����һ�е��ı�   
                lineTexts.add((String) content.subSequence(start, end));  
                break;  
            }  
            end += 1;  
        }
        int len = lineTexts.size();
        for(int j = 0; j < len; j++) {
            if("".equals(lineTexts.get(j))) {
                lineTexts.remove(lineTexts.get(j));
            }
        }
        return lineTexts;  
    }  

	//
	/**
	 * @param time
	 *            ��ǰ��ʵ�ʱ����
	 * 
	 * @return null
	 */
	public void updateIndex(long time) {
		if(onmove){
			return;
		}
		this.currentTime = time;
		// ������
		index = mLyric.getNowSentenceIndex(time);
		if (index != -1) {
			Sentence sen = Sentencelist.get(index);
			sentenctTime = sen.getFromTime();
			currentDunringTime = sen.getDuring();
		}
	}

}