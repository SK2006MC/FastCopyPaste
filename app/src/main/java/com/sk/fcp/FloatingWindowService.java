package com.sk.fcp;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.sk.fcp.databinding.ActivityMainoBinding;
import com.sk.fcp.databinding.FloatingWindowLayoutBinding;

public class FloatingWindowService extends Service {

    private WindowManager mWindowManager;
    private View mFloatingView;

    public FloatingWindowService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    FloatingWindowLayoutBinding binding;
    ActivityMainoBinding binding2;
    @Override
    public void onCreate() {
        super.onCreate();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        binding = FloatingWindowLayoutBinding.inflate(layoutInflater);
        binding2 = ActivityMainoBinding.inflate(layoutInflater);
        // Inflate the floating window layout
        mFloatingView = binding2.getRoot();

        // Set layout parameters
        final WindowManager.LayoutParams params;
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // Specify the position of the floating window
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100; // Initial Y position

        // Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

        // Handle touch events to make the window draggable
        final View titleBar = binding2.getRoot();
        titleBar.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float touchX, touchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - touchX);
                        params.y = initialY + (int) (event.getRawY() - touchY);
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                }
                return false;
            }
        });

        // Handle close button click
        Button closeButton = binding2.buttonToggleFloat;
        closeButton.setOnClickListener(v -> stopSelf());

        // You can add more functionality here, like updating the TextView
        TextView textView = binding2.editTextFileName;
        textView.setText(R.string.floating_window_running_text);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) {
            mWindowManager.removeView(mFloatingView);
        }
    }
}
