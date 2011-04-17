package com.ebarch.ipgamepad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

public class ControllerComponent extends View {
	private Drawable mImage;
    private float mPosX, mPosY;
    private float mLatestX, mLatestY;
    private float mLastX, mLastY;
    private float radius;
    private float iX, iY;
    
    public ControllerComponent(Context context, float initX, float initY, float scale, int imgResource) {
        super(context);
        mImage = context.getResources().getDrawable(imgResource);
        float diameter = (mImage.getIntrinsicHeight() / scale);
        radius = diameter / 2;
        mImage.setBounds(0, 0, (int)diameter, (int)diameter);

        mLatestX = mLastX = mPosX = iX = (initX - radius);
        mLatestY = mLastY = mPosY = iY = (initY - radius);
    }
    
    public void setX(float x) {
    	mLatestX = (x - radius);
    }
    
    public void setY(float y) {
    	mLatestY = (y - radius);
    }
    
    public void setInitial() {
    	mLatestX = iX;
    	mLatestY = iY;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        final float dx = mLatestX - mLastX;
        final float dy = mLatestY - mLastY;

        mPosX += dx;
        mPosY += dy;

        mLastX = mLatestX;
        mLastY = mLatestY;
        
        canvas.save();
        canvas.translate(mPosX, mPosY);
        mImage.draw(canvas);
        canvas.restore();
    }
}