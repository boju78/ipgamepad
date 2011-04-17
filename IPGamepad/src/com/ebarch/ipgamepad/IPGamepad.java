package com.ebarch.ipgamepad;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
    private volatile FrameLayout mainLayout;
    
    /* CONSTANTS - Used for tweaking the UI */
    private final double JOYSTICK_BACK_SCALE_FACTOR = 1.25;
    private final double JOYSTICK_FRONT_SCALE_FACTOR = 1.15;
    private final float JOYSTICK_TRIM = 50;	// Keep the joystick within the target zones - minor tweak
    
    // We want to know if we should send data over or not
    private boolean controlsAlive = false;
    
    // Networking constants - these should eventually become preferences
    private final int PACKET_RATE_MS = 25;	//Number of ms between UDP packet transmission
    private final String IP_ADDRESS = "10.4.4.33";
    private final int PORT = 4444;
    
    private volatile ControllerComponent leftIndicator;
    private volatile ControllerComponent rightIndicator;
    private volatile StaticLayout mainStaticLayout;
    
    private volatile NetworkingThread networkThread;
    private volatile DatagramSocket udpSocket;
    private volatile InetAddress ipAddress;
    
    private float viewWidth, viewHeight;
	private float leftCenterX, leftCenterY;
	private float rightCenterX, rightCenterY;
	private float viewOffset, rOuter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        
        mainLayout = (FrameLayout)findViewById(R.id.main_view);
        
        // Create and draw the main layout for the joysticks
        mainStaticLayout = new StaticLayout(this);
        mainLayout.addView(mainStaticLayout);
        
        // Setup the networking
        try {
        	udpSocket = new DatagramSocket();
			ipAddress = InetAddress.getByName(IP_ADDRESS);
        }
        catch (Exception e) {
        	// Networking exception
        }
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
	        		if (Math.pow((mPos2X - leftCenterX),2) + Math.pow((mPos2Y - leftCenterY),2) < Math.pow(rOuter,2)) {
	        			moveLeftIndicator(mPos2X,mPos2Y);
	        		}
	        		if (Math.pow((mPos1X - rightCenterX),2) + Math.pow((mPos1Y - rightCenterY),2) < Math.pow(rOuter,2)) {
	        			moveRightIndicator(mPos1X,mPos1Y);
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
    	// Move indicators to initial position and Redraw
    	rightIndicator.setInitial();
    	leftIndicator.setInitial();
    }
    
    
    public void moveRightIndicator(float x, float y) {
    	// Move right indicator to x/y and Redraw
    	rightIndicator.setXY(x, y);
    }

    
    public void moveLeftIndicator(float x, float y) {
    	// Move left indicator to x/y and Redraw
    	leftIndicator.setXY(x, y);
    }
    
    
    /* This class handles drawing the initial target zones and indicators on the screen */
    public class StaticLayout extends View {
    	Bitmap joystick_back;
    	Context mContext;
    	float scaledJBack, joyTop, joyLeft1, joyLeft2;
    	
        public StaticLayout(Context context) {
            super(context);
            mContext = context;
            
            joystick_back = BitmapFactory.decodeResource(getResources(), R.drawable.joy_back);
        }
        
        // This method is called the first time the view is drawn on the screen - sets up all the values we'll need
        protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
            super.onSizeChanged(xNew, yNew, xOld, yOld);
            
            viewWidth = (float)xNew;
	        viewHeight = (float)yNew;
	        
	        // Get the height of the entire screen and subtract out the height of our view to find the offset for touch events
	        Display display = getWindowManager().getDefaultDisplay();
	        viewOffset = display.getHeight() - viewHeight;
	        
	        // Find the centers of the joysticks
	        leftCenterX = (viewWidth / 4);
	        leftCenterY = (viewHeight / 2);
	        rightCenterX = (viewWidth * (float).75);
	        rightCenterY = (viewHeight / 2);
	        
	        // get values for top/left when drawing joysticks
	        scaledJBack = (viewHeight / (float)JOYSTICK_BACK_SCALE_FACTOR);
	        joyTop = leftCenterY - (scaledJBack / 2);
	        joyLeft1 = leftCenterX - (scaledJBack / 2);
	        joyLeft2 = rightCenterX - (scaledJBack / 2);
	        
	        rOuter = (scaledJBack / 2) - JOYSTICK_TRIM;	// Radius of the target zones - tweaked by JOYSTICK_TRIM
	        joystick_back = Bitmap.createScaledBitmap(joystick_back, (int)scaledJBack, (int)scaledJBack, true);
	        
	        // Create the joystick indicators
	        leftIndicator = new ControllerComponent(mContext, leftCenterX, leftCenterY, rOuter, (float)JOYSTICK_FRONT_SCALE_FACTOR, R.drawable.joy_front);
	        rightIndicator = new ControllerComponent(mContext, rightCenterX, rightCenterY, rOuter, (float)JOYSTICK_FRONT_SCALE_FACTOR, R.drawable.joy_front);
	        
	        // Initial draw of the joystick indicators
	        mainLayout.addView(leftIndicator);
	        mainLayout.addView(rightIndicator);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            // Draw the joystick backs
	        canvas.drawBitmap(joystick_back, joyLeft1, joyTop, null);
	        canvas.drawBitmap(joystick_back, joyLeft2, joyTop, null);
        }
    }
    
    
    /* The main networking thread that sends the joystick UDP packets */
    class NetworkingThread extends Thread {
        private volatile boolean stop = false;

        public void run() {
                while (!stop) {
                	if (controlsAlive) {
                		// Robot is enabled - let's send some data
    		    		try {
    		    			// A packet contains 4 bytes - leftJoystickX, leftJoystickY, rightJoystickX, rightJoystickY
    						byte[] buf = new byte[] { leftIndicator.getJoystickByteX(), leftIndicator.getJoystickByteY(), rightIndicator.getJoystickByteX(), rightIndicator.getJoystickByteY() };
    						DatagramPacket p = new DatagramPacket(buf, buf.length, ipAddress, PORT);
    						udpSocket.send(p);
    					} catch (Exception e) {}
    					try {
    						Thread.sleep(PACKET_RATE_MS);
    					}
    					catch (InterruptedException e) {}
    	    		}
    	    		else {
    	    			// Robot is disabled - wait a little bit before trying again
    	    			try {
    						Thread.sleep(PACKET_RATE_MS);
    					}
    					catch (InterruptedException e) {}
    	    		}
                }
        }

        public synchronized void requestStop() {
                stop = true;
        }
    }

    
    /* Call this to start the main networking thread */
    public synchronized void startNetworkingThread(){
        if(networkThread == null){       
                networkThread = new NetworkingThread();
                networkThread.start();
        }
    }

    
    /* Call this to stop the main networking thread */
    public synchronized void stopNetworkingThread(){
        if(networkThread != null){
                networkThread.requestStop();
                networkThread = null;
        }
    }
    
    
    @Override
    protected void onPause() {
    	// End Ethernet communications
    	stopNetworkingThread();
    	
    	super.onPause();
    }
    
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	// Begin Ethernet communications
    	startNetworkingThread();
    }
}