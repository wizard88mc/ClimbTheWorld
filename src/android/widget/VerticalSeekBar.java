package android.widget;

import org.unipd.nbeghin.climbtheworld.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

/**
 * Custom android widget: vertical seekbar
 * 
 * @url https://github.com/AndroSelva/Vertical-SeekBar-Android/
 * 
 */
public class VerticalSeekBar extends SeekBar {
	
	 private Bitmap thumb1 = BitmapFactory.decodeResource(getResources(), R.drawable.star);

	 private double starHeight = 0;
	 private int height;
		private int thumbHalfWidth;
     
	public VerticalSeekBar(Context context) {
		super(context);
		requestLayout();
	}

	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		requestLayout();
	}

	public VerticalSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		requestLayout();
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(h, w, oldh, oldw);
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
		System.out.println(MeasureSpec.getSize(heightMeasureSpec));
		System.out.println(getMeasuredHeight());
		height = MeasureSpec.getSize(heightMeasureSpec);
//		if (getHeight() > 0)
//			init();
	}

	protected void onDraw(Canvas c) {
		c.rotate(-90);
		c.translate(-getHeight(), 0);
		
		c.drawBitmap(thumb1, (float)starHeight , 0,null);
		//c.drawBitmap(thumb2, 1000 , 20,null);
        
        
		super.onDraw(c);
	}
	
	public void nextStar(int progress){
		thumb1 = BitmapFactory.decodeResource(getResources(), R.drawable.star);
		System.out.println("height " + height + " progress " + progress);
		starHeight = (((double) progress * (double) height) /(double) 100) - 98;//(double) progress / (double) 100;
		System.out.println(starHeight);
		invalidate();
		
	}
	
	
	
	public void goldStar(){
		thumb1 = BitmapFactory.decodeResource(getResources(), R.drawable.gold_star);
		invalidate();
	}

	/*
	 * nbeghin: added onSizeChanged to solve thumb image not updated
	 * @see android.widget.ProgressBar#setProgress(int)
	 */
	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}
	
//	private void init() {
//		//printLog("View Height =" + getHeight() + "\t\t Thumb Height :"+ thumb.getHeight());
//		if (thumb1.getHeight() > getHeight())
//			getLayoutParams().height = thumb1.getHeight();
//
//		thumbY = (getHeight() / 2) - (thumb1.getHeight() / 2);
//		//printLog("View Height =" + getHeight() + "\t\t Thumb Height :"+ thumb.getHeight() + "\t\t" + thumbY);
//		
//		thumbHalfWidth = thumb1.getWidth()/2;
//		thumb1X = thumbHalfWidth;
//		thumb2X = getWidth()/2 ;
//	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
//		if (!isEnabled()) {
//			return false;
//		}
//		switch (event.getAction()) {
//			case MotionEvent.ACTION_DOWN:
//			case MotionEvent.ACTION_MOVE:
//			case MotionEvent.ACTION_UP:
//				int i = 0;
//				i = getMax() - (int) (getMax() * event.getY() / getHeight());
//				setProgress(i);
//				onSizeChanged(getWidth(), getHeight(), 0, 0);
//				break;
//			case MotionEvent.ACTION_CANCEL:
//				break;
//		}
//		return true;
	}
	
	
	
}