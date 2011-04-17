package com.ebarch.ipgamepad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

public class ControllerComponent extends View {
	private Drawable mImage;	// The indicator image that we're drawing
	private float radius;		// Radius of the indicator image
    private float mX, mY;		// Current X,Y position
    private float iX, iY;		// Initial X,Y position
    
    public ControllerComponent(Context context, float initX, float initY, float scale, int imgResource) {
        super(context);
        
        // Get the image resource
        mImage = context.getResources().getDrawable(imgResource);
        float diameter = (mImage.getIntrinsicHeight() / scale);
        radius = diameter / 2;
        mImage.setBounds(0, 0, (int)diameter, (int)diameter);	// Scale the image appropriately

        // Initial setup
        mX = iX = (initX - radius);
        mY = iY = (initY - radius);
    }
    
    /* Update the X,Y position of the indicator */
    public void setXY(float x, float y) {
    	mX = (x - radius);
    	mY = (y - radius);
    	super.invalidate();
    }
    
    /* Update the X,Y position of the indicator to those of its initial values */
    public void setInitial() {
    	mX = iX;
    	mY = iY;
    	super.invalidate();
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