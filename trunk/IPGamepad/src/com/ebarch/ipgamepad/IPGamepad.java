package com.ebarch.ipgamepad;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.widget.FrameLayout;


public class IPGamepad extends Activity {
	private float mPos1X, mPos2X;
    private float mPos1Y, mPos2Y;
    FrameLayout mainLayout;
    
    private float viewWidth, viewHeight, rOuter;
    private float leftCenterX, leftCenterY, rightCenterX, rightCenterY;
    
    private final float TARGET_ZONE_THICKNESS = 5;
    private final int TARGET_ZONE_BORDER_COLOR = 0xFF000099;
    private final int TARGET_ZONE_INNER_COLOR = 0xFF222222;
    private final int INDICATOR_COLOR = 0xFF000000;
    
    private final int FINGER_OFFSET = 100;
	
    View priIndicator;
    View secIndicator;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        
        mainLayout = (FrameLayout)findViewById(R.id.main_view);
        mainLayout.addView(new drawTargetZones(this));
        
        // Draw the initial points before a touch event occurs
    	priIndicator = new drawIndicator(this,0,0,true);
    	mainLayout.addView(priIndicator);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        if (ev.getPointerCount() == 2 && (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN)) {
        	mPos1X = ev.getX(0);
        	mPos1Y = ev.getY(0) - FINGER_OFFSET;
        	mPos2X = ev.getX(1);
        	mPos2Y = ev.getY(1) - FINGER_OFFSET;
        	mainLayout.removeView(priIndicator);
        	mainLayout.removeView(secIndicator);
        	priIndicator = new drawIndicator(this,mPos1X,mPos1Y,false);
        	secIndicator = new drawIndicator(this,mPos2X,mPos2Y,false);
        	mainLayout.addView(priIndicator);
        	mainLayout.addView(secIndicator);
        }
        else {
        	mainLayout.removeView(priIndicator);
        	mainLayout.removeView(secIndicator);
        	priIndicator = new drawIndicator(this,leftCenterX,leftCenterY,false);
        	secIndicator = new drawIndicator(this,rightCenterX,rightCenterY,false);
        	mainLayout.addView(priIndicator);
        	mainLayout.addView(secIndicator);
        }
        
        return true;
    }
    
    public class drawIndicator extends View {
        private final float x;
        private final float y;
        private final Paint ptPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint lPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final boolean initial;
        
        public drawIndicator(Context context, float x, float y, boolean initial) {
            super(context);
            ptPaint.setColor(INDICATOR_COLOR);
            ptPaint.setStrokeWidth(30);
            lPaint.setColor(INDICATOR_COLOR);
            lPaint.setStrokeWidth(5);
            this.x = x;
            this.y = y;
            this.initial = initial;
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (initial == false) {
	            if (x < (viewWidth / 2))
	            		canvas.drawLine(leftCenterX, leftCenterY, x, y, lPaint);
	            else
	            		canvas.drawLine(rightCenterX, rightCenterY, x, y, lPaint);
	            
	            canvas.drawPoint(x, y, ptPaint);
            }
            else {
            	canvas.drawPoint(leftCenterX, leftCenterY, ptPaint);
            	canvas.drawPoint(rightCenterX, rightCenterY, ptPaint);
            }
        }
    }
    
    public class drawTargetZones extends View {
        private final Paint outerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint ptPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        public drawTargetZones(Context context) {
            super(context);
            outerPaint.setColor(TARGET_ZONE_BORDER_COLOR);
            innerPaint.setColor(TARGET_ZONE_INNER_COLOR);
            ptPaint.setColor(INDICATOR_COLOR);
            ptPaint.setStrokeWidth(15);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
        	viewWidth = (float)mainLayout.getWidth();
            viewHeight = (float)mainLayout.getHeight();
            rOuter = (viewHeight/2) - 10;
            leftCenterX = (float)(viewWidth * .25);
            leftCenterY = (float)(viewHeight / 2);
            rightCenterX = (float)(viewWidth * .75);
            rightCenterY = (float)(viewHeight / 2);
            super.onDraw(canvas);
            
            // Draw left target zone
            canvas.drawCircle(leftCenterX, leftCenterY, rOuter, outerPaint);
            canvas.drawCircle(leftCenterX, leftCenterY, rOuter - TARGET_ZONE_THICKNESS, innerPaint);
            // Draw right target zone
            canvas.drawCircle(rightCenterX, rightCenterY, rOuter, outerPaint);
            canvas.drawCircle(rightCenterX, rightCenterY, rOuter - TARGET_ZONE_THICKNESS, innerPaint);
            // Draw points in the middle of the target zones
            canvas.drawPoint(leftCenterX, leftCenterY, ptPaint);
            canvas.drawPoint(rightCenterX, rightCenterY, ptPaint);
        }
    }
}