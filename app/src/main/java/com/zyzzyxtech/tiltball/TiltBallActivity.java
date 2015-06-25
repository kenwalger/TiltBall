package com.zyzzyxtech.tiltball;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;

import java.util.Timer;
import java.util.TimerTask;

public class TiltBallActivity extends Activity {

    public static final String TAG = TiltBallActivity.class.getSimpleName();
    BallView mBallView = null;
    Handler RedrawHandler = new Handler(); // So redraw occurs in main thread.
    Timer mTmr = null;
    TimerTask mTsk = null;
    int mScrWidth, mScrHeight;
    PointF mBallPos, mBallSpd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide the title bar

        // Set app to full screen and keep screen on
        getWindow().setFlags(0xFFFFFFFF,
                LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tilt_ball);

        // Create pointer to main screen
        final FrameLayout mainView = (android.widget.FrameLayout) findViewById(R.id.main_view);

        // Set the initial variable values. Start the ball in the center of the screen
        // with a speed of zero.

        // Get screen dimensions

        final int version = Build.VERSION.SDK_INT;
        Display display = getWindowManager().getDefaultDisplay();

        if (version >= 13) {
            Point size = new Point();
            display.getSize(size);
            mScrWidth = size.x;
            mScrHeight = size.y;
        } else {

            mScrWidth = display.getWidth();
            mScrHeight = display.getHeight();
        }

        mBallPos = new android.graphics.PointF();
        mBallSpd = new android.graphics.PointF();

        // Create variables for ball position and speed
        mBallPos.x = mScrWidth / 2;
        mBallPos.y = mScrHeight / 2;
        mBallSpd.x = 0;
        mBallSpd.y = 0;

        // Create initial ball
        mBallView = new BallView(this, mBallPos.x, mBallPos.y, 5);

        mainView.addView(mBallView);    // Add ball to the main screen
        mBallView.invalidate();         // Call onDraw in BallView.

        // Listener for accelerometer, use anonymous class for simplicity
        ((SensorManager) getSystemService(Context.SENSOR_SERVICE)).registerListener(
                new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        //set ball speed based on phone tilt (ignore Z axis)
                        mBallSpd.x = -event.values[0];
                        mBallSpd.y = event.values[1];
                        //timer event will redraw ball
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    } //ignore
                },
                ((SensorManager) getSystemService(Context.SENSOR_SERVICE))
                        .getSensorList(Sensor.TYPE_ACCELEROMETER).get(0),
                SensorManager.SENSOR_DELAY_NORMAL);

        // Listener for touch event
        mainView.setOnTouchListener(new android.view.View.OnTouchListener() {
            public boolean onTouch(android.view.View v, android.view.MotionEvent e) {
                //set ball position based on screen touch
                mBallPos.x = e.getX();
                mBallPos.y = e.getY();
                //timer event will redraw ball
                return true;
            }
        });
    } // onCreate

    // Listener for menu button on phone
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Exit");   // Only one menu item
        return super.onCreateOptionsMenu(menu);
    }

    // Listener for menu item clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getTitle() == "Exit")  // User clicked Exit
            finish();                   // Will call onPause
        return super.onOptionsItemSelected(item);
    }

    // For state flow see http://developer.android.com/reference/android/app/Activity.html
    @Override
    public void onPause() {      // App moved to background, stop background threads
        mTmr.cancel();           // Kill/release timer (our only background thread)
        mTmr = null;
        mTsk = null;
        super.onPause();
    }
    
    @Override
    public void onResume() {    // App moved to foreground (also occurs at app startup)
        
        // Create timer to move ball to new position
        mTmr = new Timer();
        mTsk = new TimerTask() {
            public void run() {
                
                // If debugging with external device,
                // a log cat viewer will be needed on the device
                android.util.Log.d(TAG, "Timer Hit - " + mBallPos.x + ":" + mBallPos.y);

                // Move ball based on current speed
                mBallPos.x += mBallSpd.x;
                mBallPos.y += mBallSpd.y;
                
                // If ball goes off screen, reposition to opposite side of screen.
                // Without this the bill will disappear. Code can be modified to ball to be in a "box".
                if (mBallPos.x > mScrWidth) mBallPos.x = 0;
                if (mBallPos.y > mScrHeight) mBallPos.y = 0;
                if (mBallPos.x < 0) mBallPos.x = mScrWidth;
                if (mBallPos.y < 0) mBallPos.y = mScrHeight;
                
                // Update ball class instance
                mBallView.mX = mBallPos.x;
                mBallView.mY = mBallPos.y;
                
                // Redraw ball. Must run in background thread to prevent thread lock.
                RedrawHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mBallView.invalidate();
                    }
                });
                
            }
        }; // TimerTask
        
        mTmr.schedule(mTsk, 10, 10); // Start Timer
        super.onResume();
    }  // onResume

    @Override
    public void onDestroy()     // Main thread stopped
    {
        super.onDestroy();
        // Wait for threads to exit before clearing app
        finish();
        // Remove app from memory
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    // Listener for config change.
    // This is called when user tilts device enough to trigger landscape view
    // we want our app to stay in portrait view, so bypass event.
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
    }
    
} // TiltBallActivity