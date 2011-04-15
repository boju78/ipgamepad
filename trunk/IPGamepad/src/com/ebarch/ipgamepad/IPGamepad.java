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
    
    /* CONSTANTS - Used for tweaking the UI */
    private final float TARGET_ZONE_THICKNESS = 5;
    private final float TARGET_ZONE_CENTER_THICKNESS = 15;
    private final float TARGET_ZONE_CLEARANCE = 10;
    private final int TARGET_ZONE_BORDER_COLOR = 0xFF000099;
    private final int TARGET_ZONE_INNER_COLOR = 0xFF222222;
    private final int INDICATOR_COLOR = 0xFF000000;
    private final float INDICATOR_LINE_WIDTH = 5;
    private final float INDICATOR_WIDTH = 30;
    private final int FINGER_OFFSET = 100;
    
    // We want to know if we should send data over or not
    private boolean controlsAlive = false;
	
    // Store the views for the indicators
    View priIndicator;
    View secIndicator;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        
        mainLayout = (FrameLayout)findViewById(R.id.main_view);
        mainLayout.addView(new drawTargetZones(this));
        
        // Draw the initial points before a touch event occurs - this just places them smack in the center of the "target zones"
    	priIndicator = new drawIndicator(this,0,0,true);
    	mainLayout.addView(priIndicator);
    }
    
    
    /* This is our touch event code that is called whenever there is an ACTION_MOVE, ACTION_DOWN, or ACTION_UP event */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        boolean exception = false;	// If this becomes true at any point, don't update the indicator

        if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
        	// Get the x,y position of each pointer. Use FINGER_OFFSET to get a more accurate position
        	mPos1X = ev.getX(0);
        	mPos1Y = ev.getY(0) - FINGER_OFFSET;
        	mPos2X = ev.getX(1);
        	mPos2Y = ev.getY(1) - FINGER_OFFSET;
        	
        	if (ev.getPointerCount() != 2)	// We don't see 2 fingers on the screen - bail out
        		exception = true;
        	else if (((mPos1X < (viewWidth / 2)) && (mPos2X < (viewWidth / 2))) || ((mPos1X >= (viewWidth / 2)) && (mPos2X >= (viewWidth / 2))))
        		exception = true;	// The user has placed 2 fingers in the same half of the screen - bail out
        	
        	if (!exception) {
        		controlsAlive = true;	// Tell anyone else who needs to know that controls are valid and active
	        	if (mPos1X < (viewWidth / 2)) {	// The first point is on the left
	        		// Make sure the user's finger is within the target zone, otherwise don't update
	        		if (Math.pow((mPos1X - leftCenterX),2) + Math.pow((mPos1Y - leftCenterY),2) < Math.pow(rOuter,2)) {
	        			mainLayout.removeView(priIndicator);
	        			priIndicator = new drawIndicator(this,mPos1X,mPos1Y,false);
	        			mainLayout.addView(priIndicator);
	        		}
	        		if (Math.pow((mPos2X - rightCenterX),2) + Math.pow((mPos2Y - rightCenterY),2) < Math.pow(rOuter,2)) {
	        			mainLayout.removeView(secIndicator);
	        			secIndicator = new drawIndicator(this,mPos2X,mPos2Y,false);
	        			mainLayout.addView(secIndicator);
	        		}
	        	}
	        	else {	// The first point is on the right
	        		// Make sure the user's finger is within the target zone, otherwise don't update
	        		if (Math.pow((mPos1X - rightCenterX),2) + Math.pow((mPos1Y - rightCenterY),2) < Math.pow(rOuter,2)) {
	        			mainLayout.removeView(priIndicator);
	        			priIndicator = new drawIndicator(this,mPos1X,mPos1Y,false);
	        			mainLayout.addView(priIndicator);
	        		}
	        		if (Math.pow((mPos2X - leftCenterX),2) + Math.pow((mPos2Y - leftCenterY),2) < Math.pow(rOuter,2)) {
	        			mainLayout.removeView(secIndicator);
	        			secIndicator = new drawIndicator(this,mPos2X,mPos2Y,false);
	        			mainLayout.addView(secIndicator);
	        		}
	        	}
        	}
        	else {
        		// We had an exception - move the indicators to the center and make controlsAlive false
        		controlsAlive = false;
        		mainLayout.removeView(priIndicator);
            	mainLayout.removeView(secIndicator);
            	priIndicator = new drawIndicator(this,leftCenterX,leftCenterY,false);
            	secIndicator = new drawIndicator(this,rightCenterX,rightCenterY,false);
            	mainLayout.addView(priIndicator);
            	mainLayout.addView(secIndicator);
        	}
        }
        
        return true;
    }
    
    
    /* This class handles drawing the points and lines associated with where the user's fingers are at on the screen */
    public class drawIndicator extends View {
        private final float x;
        private final float y;
        private final Paint ptPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint lPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final boolean initial;	// Is this the first call before a touch event has occurred?
        
        public drawIndicator(Context context, float x, float y, boolean initial) {
            super(context);
            ptPaint.setColor(INDICATOR_COLOR);
            ptPaint.setStrokeWidth(INDICATOR_WIDTH);
            lPaint.setColor(INDICATOR_COLOR);
            lPaint.setStrokeWidth(INDICATOR_LINE_WIDTH);
            this.x = x;
            this.y = y;
            this.initial = initial;
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // If this isn't the first call, we'll only be drawing one indicator (in either the left or right target zone)
            if (initial == false) {
            	// This is for the line that traces the distance between the center of the target zone and the user's finger
	            if (x < (viewWidth / 2))	// Position is in the left half of the screen, draw in reference to the leftCenterX/Y
	            		canvas.drawLine(leftCenterX, leftCenterY, x, y, lPaint);
	            else						// Position is in the right half of the screen
	            		canvas.drawLine(rightCenterX, rightCenterY, x, y, lPaint);
	            
	            // Okay, so let's place in the indicator of where the user's finger is
	            canvas.drawPoint(x, y, ptPaint);
            }
            else {
            	// This is the first call, create 2 of the finger indicators right in the center of the target zones
            	// We don't need to worry about the indicator lines because we're in the center right now
            	canvas.drawPoint(leftCenterX, leftCenterY, ptPaint);
            	canvas.drawPoint(rightCenterX, rightCenterY, ptPaint);
            }
        }
    }
    
    
    /* This class handles the initial drawing of the "target zones" or circles used as joysticks */
    public class drawTargetZones extends View {
        private final Paint outerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint ptPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        public drawTargetZones(Context context) {
            super(context);
            outerPaint.setColor(TARGET_ZONE_BORDER_COLOR);
            innerPaint.setColor(TARGET_ZONE_INNER_COLOR);
            ptPaint.setColor(INDICATOR_COLOR);
            ptPaint.setStrokeWidth(TARGET_ZONE_CENTER_THICKNESS);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
        	viewWidth = (float)mainLayout.getWidth();
            viewHeight = (float)mainLayout.getHeight();
            rOuter = (viewHeight/2) - TARGET_ZONE_CLEARANCE;
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