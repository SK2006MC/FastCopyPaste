package com.sk.fcp;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class FloatingTextService extends Service {

    private WindowManager mWindowManager;
    private View mFloatingView;
	private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private WindowManager.LayoutParams params;

    public FloatingTextService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        mFloatingView = LayoutInflater.from(this).inflate(R.layout.activity_main,null);
	    LinearLayout mFloatingLinearLayout = mFloatingView.findViewById(R.id.linearLayout1);
	    EditText editTextFileContent = mFloatingView.findViewById(R.id.editTextFileContent);
	    EditText editTextFileName = mFloatingView.findViewById(R.id.editTextFileName);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (mWindowManager != null) {
            mWindowManager.addView(mFloatingView, params);
        }

        //Implement drag based on touch listener
        mFloatingLinearLayout.setOnTouchListener(this::onTouch);

        editTextFileContent.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                mWindowManager.updateViewLayout(mFloatingView, params);
            } else {
                params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                mWindowManager.updateViewLayout(mFloatingView, params);
            }
        });

        editTextFileName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                mWindowManager.updateViewLayout(mFloatingView, params);
            } else {
                params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                mWindowManager.updateViewLayout(mFloatingView, params);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null && mWindowManager != null) {
            mWindowManager.removeView(mFloatingView);
        }
    }

    private boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                mWindowManager.updateViewLayout(mFloatingView, params);
                return true;
            case MotionEvent.ACTION_UP:
                return true;
        }
        return false;
    }
}