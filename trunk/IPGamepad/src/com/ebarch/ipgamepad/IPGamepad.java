package com.ebarch.ipgamepad;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.View;
import android.widget.FrameLayout;


public class IPGamepad extends Activity {
	private float mPos1X, mPos2X;
    private float mPos1Y, mPos2Y;
    FrameLayout mainLayout;
    
    private float viewWidth, viewHeight, rOuter;
    private float leftCenterX, leftCenterY, rightCenterX, rightCenterY;
    
    /* CONSTANTS - Used for tweaking the UI */
    private final double JOYSTICK_BACK_SCALE_FACTOR = 1.25;
    private final double JOYSTICK_FRONT_SCALE_FACTOR = 2.1;
    private final int FINGER_OFFSET = 100;
    
    // We want to know if we should send data over or not
    private boolean controlsAlive = false;
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        
        mainLayout = (FrameLayout)findViewById(R.id.main_view);
        
        mainLayout.addView(new drawStaticLayout(this));
        
        // Draw the initial points before a touch event occurs - this just places them smack in the center of the "target zones"
        moveIndicatorsToCenter();
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
	        			moveLeftIndicator(mPos1X,mPos1Y);
	        		}
	        		if (Math.pow((mPos2X - rightCenterX),2) + Math.pow((mPos2Y - rightCenterY),2) < Math.pow(rOuter,2)) {
	        			moveRightIndicator(mPos2X,mPos2Y);
	        		}
	        	}
	        	else {	// The first point is on the right
	        		// Make sure the user's finger is within the target zone, otherwise don't update
	        		if (Math.pow((mPos1X - rightCenterX),2) + Math.pow((mPos1Y - rightCenterY),2) < Math.pow(rOuter,2)) {
	        			moveLeftIndicator(mPos1X,mPos1Y);
	        		}
	        		if (Math.pow((mPos2X - leftCenterX),2) + Math.pow((mPos2Y - leftCenterY),2) < Math.pow(rOuter,2)) {
	        			moveRightIndicator(mPos2X,mPos2Y);
	        		}
	        	}
        	}
        	else {
        		// We had an exception - move the indicators to the center and make controlsAlive false
        		controlsAlive = false;
        		moveIndicatorsToCenter();
        	}
        }
        
        return true;
    }
    
    public void moveIndicatorsToCenter() {
    	//TODO
    }
    
    public void moveRightIndicator(float x, float y) {
    	//TODO
    }

    public void moveLeftIndicator(float x, float y) {
    	//TODO
    }
    
    /* This class handles drawing the initial target zones on the screen */
    public class drawStaticLayout extends View {
    	Bitmap joystick_back;
        
        public drawStaticLayout(Context context) {
            super(context);
            
            joystick_back = BitmapFactory.decodeResource(getResources(), R.drawable.joy_back);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            viewWidth = (float)mainLayout.getWidth();
            viewHeight = (float)mainLayout.getHeight();
            leftCenterX = (float)(viewWidth * .25);
            leftCenterY = (float)(viewHeight / 2);
            rightCenterX = (float)(viewWidth * .75);
            rightCenterY = (float)(viewHeight / 2);
            int scaledJBack = (int)((float)viewHeight / JOYSTICK_BACK_SCALE_FACTOR);
            rOuter = (float)scaledJBack / (float)4;
            int joyTop = (int)leftCenterY - (int)((float)scaledJBack / 2);
            int joyLeft1 = (int)leftCenterX - (int)((float)scaledJBack / 2);
            int joyLeft2 = (int)rightCenterX - (int)((float)scaledJBack / 2);
            
            joystick_back = Bitmap.createScaledBitmap(joystick_back, scaledJBack, scaledJBack, true);
            
            canvas.drawBitmap(joystick_back, joyLeft1, joyTop, null);
            canvas.drawBitmap(joystick_back, joyLeft2, joyTop, null);
        }
    }
}