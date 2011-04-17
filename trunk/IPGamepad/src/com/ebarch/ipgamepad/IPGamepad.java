package com.ebarch.ipgamepad;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.View;
import android.widget.FrameLayout;


public class IPGamepad extends Activity {
    FrameLayout mainLayout;
    
    /* CONSTANTS - Used for tweaking the UI */
    private final double JOYSTICK_BACK_SCALE_FACTOR = 1.25;
    private final double JOYSTICK_FRONT_SCALE_FACTOR = 1.15;
    private final float JOYSTICK_TRIM = 30;	// Keep the joystick within the target zones - minor tweak
    
    // We want to know if we should send data over or not
    private boolean controlsAlive = false;
    
    ControllerComponent leftIndicator;
    ControllerComponent rightIndicator;
    StaticLayout mainStaticLayout;
    
    public float viewWidth, viewHeight;
	public float leftCenterX, leftCenterY;
	public float rightCenterX, rightCenterY;
	public float viewOffset, rOuter;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        
        mainLayout = (FrameLayout)findViewById(R.id.main_view);
        
        mainStaticLayout = new StaticLayout(this);
        mainLayout.addView(mainStaticLayout);  
    }
    

    /* This is our touch event code that is called whenever there is an ACTION_MOVE, ACTION_DOWN, or ACTION_UP event */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        boolean exception = false;	// If this becomes true at any point, don't update the indicator

        if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
        	// Get the x,y position of each pointer. Use FINGER_OFFSET to get a more accurate position
        	float mPos1X = ev.getX(0);
        	float mPos1Y = ev.getY(0) - viewOffset;
        	float mPos2X = ev.getX(1);
        	float mPos2Y = ev.getY(1) - viewOffset;
        	
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
    	rightIndicator.setInitial();
    	leftIndicator.setInitial();
    	leftIndicator.invalidate();
    	rightIndicator.invalidate();
    }
    
    public void moveRightIndicator(float x, float y) {
    	rightIndicator.setX(x);
    	rightIndicator.setY(y);
    	rightIndicator.invalidate();
    }

    public void moveLeftIndicator(float x, float y) {
    	leftIndicator.setX(x);
    	leftIndicator.setY(y);
    	leftIndicator.invalidate();
    }
    
    
    /* This class handles drawing the initial target zones on the screen */
    public class StaticLayout extends View {
    	Bitmap joystick_back;
    	Context mContext;
    	float scaledJBack, joyTop, joyLeft1, joyLeft2;
    	
        public StaticLayout(Context context) {
            super(context);
            mContext = context;
            
            joystick_back = BitmapFactory.decodeResource(getResources(), R.drawable.joy_back);
        }
        
        protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        	// This method is called the first time the view is drawn on the screen - sets up all the values we'll need
            super.onSizeChanged(xNew, yNew, xOld, yOld);
            
            viewWidth = (float)xNew;
	        viewHeight = (float)yNew;
	        
	        Display display = getWindowManager().getDefaultDisplay();
	        viewOffset = display.getHeight() - viewHeight;
	        
	        leftCenterX = (viewWidth / 4);
	        leftCenterY = (viewHeight / 2);
	        rightCenterX = (viewWidth * (float).75);
	        rightCenterY = (viewHeight / 2);
	        
	        scaledJBack = (viewHeight / (float)JOYSTICK_BACK_SCALE_FACTOR);
	        joyTop = leftCenterY - (scaledJBack / 2);
	        joyLeft1 = leftCenterX - (scaledJBack / 2);
	        joyLeft2 = rightCenterX - (scaledJBack / 2);
	        
	        rOuter = (scaledJBack / 2) - JOYSTICK_TRIM;
	        joystick_back = Bitmap.createScaledBitmap(joystick_back, (int)scaledJBack, (int)scaledJBack, true);
	        
	        leftIndicator = new ControllerComponent(mContext, leftCenterX, leftCenterY, (float)JOYSTICK_FRONT_SCALE_FACTOR, R.drawable.joy_front);
	        rightIndicator = new ControllerComponent(mContext, rightCenterX, rightCenterY, (float)JOYSTICK_FRONT_SCALE_FACTOR, R.drawable.joy_front);
	        
	        mainLayout.addView(leftIndicator);
	        mainLayout.addView(rightIndicator);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
	        canvas.drawBitmap(joystick_back, joyLeft1, joyTop, null);
	        canvas.drawBitmap(joystick_back, joyLeft2, joyTop, null);
        }
    }
}