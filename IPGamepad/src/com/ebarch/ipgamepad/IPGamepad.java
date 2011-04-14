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
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        
        mainLayout = (FrameLayout)findViewById(R.id.main_view);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        if (ev.getPointerCount() == 2 && (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN)) {
        	mPos1X = ev.getX(0);
        	mPos1Y = ev.getY(0) - 100;
        	mPos2X = ev.getX(1);
        	mPos2Y = ev.getY(1) - 100;
        	mainLayout.removeAllViews();
        	mainLayout.addView(new drawIndicator(this,mPos1X,mPos1Y,50));
        	mainLayout.addView(new drawIndicator(this,mPos2X,mPos2Y,50));
        }
        
        return true;
    }
    
    public class drawIndicator extends View {
        private final float x;
        private final float y;
        private final int r;
        private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        public drawIndicator(Context context, float x, float y, int r) {
            super(context);
            mPaint.setColor(0xFFFF0000);
            this.x = x;
            this.y = y;
            this.r = r;
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawCircle(x, y, r, mPaint);
        }
    }
}