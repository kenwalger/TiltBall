package com.zyzzyxtech.tiltball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by Ken on 2/13/2015.
 */


public class BallView extends View {
    
    public float mX;
    public float mY;
    private final int mR;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    // Construct new ball object
    public BallView(Context context, float x, float y, int r) {
        super(context);
        
        // Color HEX is [transparency][red][green][blue]
        mPaint.setColor(0xFF0000FF);  // Not transparent. Color is blue.
        this.mX = x;
        this.mY = y;
        this.mR = r;  // Radius of ball.
    }
    
    // Called by invalidate()
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mX, mY, mR, mPaint);
        
    }
    
}
