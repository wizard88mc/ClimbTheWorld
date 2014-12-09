package android.widget;

import org.unipd.nbeghin.climbtheworld.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;

/**
 * Custom android widget: vertical seekbar customized by Silvia Segato
 * 
 * @url https://github.com/AndroSelva/Vertical-SeekBar-Android/
 * 
 */
public class VerticalSeekBar extends SeekBar {
	
	 private Bitmap thumb1 = BitmapFactory.decodeResource(getResources(), R.drawable.star);

	 private static double starHeight = 0;
	 private static int totalHeight = 0;
	 private int height = 0;
	 private int width = 0;
	 private View line;
     
	public VerticalSeekBar(Context context) {
		super(context);
		requestLayout();
		setWillNotDraw(false);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		requestLayout();
		setWillNotDraw(false);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		requestLayout();
		setWillNotDraw(false);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(h, w, oldh, oldw);
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { 
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
			height = MeasureSpec.getSize(heightMeasureSpec);
			width = MeasureSpec.getSize(widthMeasureSpec);	
	}
	
	
	
	protected void onDraw(Canvas c) {
		c.rotate(-90);
		c.translate(-getHeight(), 0);
		RelativeLayout parent = (RelativeLayout) this.getParent();
		line = parent.findViewById(R.id.redLine);
		line.setLayoutParams(new LayoutParams(width/2, 2));
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)line.getLayoutParams();
		params.setMargins(0,  totalHeight - ((int) starHeight) - 30, 0, 0); //substitute parameters for left, top, right, bottom
		line.setLayoutParams(params);
		line.setBackgroundColor(getResources().getColor(R.color.red));
		
		System.out.println("starheight " + starHeight);
		c.drawBitmap(thumb1, (float)starHeight , 40,null);
        
        
		super.onDraw(c);
	}
	
	public void nextStar(int progress){
		thumb1 = BitmapFactory.decodeResource(getResources(), R.drawable.star);
		//System.out.println("height " + height  + " progress " + progress);
		starHeight = (((double) progress * ((double) height)) /(double) 100) + 5;
		if(starHeight >= height)
			starHeight = height - 60 + 5;
		//System.out.println(starHeight);
		invalidate();
		
	}
	
	public void setTotalHeight(){
		totalHeight = height;
	}
	
	public void goldStar(){
//		thumb1 = BitmapFactory.decodeResource(getResources(), R.drawable.gold_star);
//		invalidate();
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