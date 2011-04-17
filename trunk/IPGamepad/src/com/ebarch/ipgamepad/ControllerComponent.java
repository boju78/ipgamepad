package com.ebarch.ipgamepad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

public class ControllerComponent extends View {
	private Drawable mImage;	// The indicator image that we're drawing
	private float iRadius;		// Radius of the indicator image
	private float oRadius;		// Radius of the target zone - joystick_trim
    private float mX, mY;		// Current X,Y position
    private float iX, iY;		// Initial X,Y position
    
    public ControllerComponent(Context context, float initX, float initY, float rOuter, float scale, int imgResource) {
        super(context);
        
        // Get the image resource
        mImage = context.getResources().getDrawable(imgResource);
        float diameter = (mImage.getIntrinsicHeight() / scale);
        iRadius = diameter / 2;
        mImage.setBounds(0, 0, (int)diameter, (int)diameter);	// Scale the image appropriately

        // Initial setup
        mX = iX = (initX - iRadius);
        mY = iY = (initY - iRadius);
        oRadius = rOuter;
    }
    
    /* Update the X,Y position of the indicator */
    public void setXY(float x, float y) {
    	mX = (x - iRadius);
    	mY = (y - iRadius);
    	super.invalidate();
    }
    
    /* Update the X,Y position of the indicator to those of its initial values */
    public void setInitial() {
    	mX = iX;
    	mY = iY;
    	super.invalidate();
    }
    
    /* Generate a byte value that scales between the min and max value of the X axis */
    public byte getJoystickByteX()
    {
    	long OUT_MIN = 0;
    	long OUT_MAX = 255;
    	long IN_MIN = (long)(iX - oRadius);
    	long IN_MAX = (long)(iX + oRadius);
    	
    	long result = ((long)mX - IN_MIN) * (OUT_MAX - OUT_MIN) / (IN_MAX - IN_MIN) + OUT_MIN;
    	if (result < 0)
    		result = 0;
    	else if (result > 255)
    		result = 255;
    	
    	return (byte)result;
    }
    
    /* Generate a byte value that scales between the min and max value of the Y axis */
    public byte getJoystickByteY()
    {
    	long OUT_MIN = 0;
    	long OUT_MAX = 255;
    	long IN_MIN = (long)(iY - oRadius);
    	long IN_MAX = (long)(iY + oRadius);
    	
    	long result = ((long)mY - IN_MIN) * (OUT_MAX - OUT_MIN) / (IN_MAX - IN_MIN) + OUT_MIN;
    	if (result < 0)
    		result = 0;
    	else if (result > 255)
    		result = 255;
    	
    	// Flip the values so that moving the joystick up gives a higher value
    	result = 255 - result;
    	
    	return (byte)result;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Translate by the current X,Y values
        canvas.save();
        canvas.translate(mX, mY);
        mImage.draw(canvas);
        canvas.restore();
    }
}