package com.sk.fcp.core.window;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.sk.fcp.utils.Constants;

public class FloatingWindowManager implements WindowOperations {
    private static final String TAG = Constants.TAG + ".FloatingWindowManager";
    private final WindowManager windowManager;
    private final Context context;
    private View floatingView;
    private WindowManager.LayoutParams currentParams;

    public FloatingWindowManager(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.currentParams = getDefaultLayoutParams();
    }

    @Override
    public void showFloatingWindow(Context context) {
        try {
            if (!isFloatingWindowVisible(context)) {
                // Create and show the floating window
                if (floatingView == null) {
                    // TODO: Initialize floatingView with your custom layout
                    // floatingView = LayoutInflater.from(context).inflate(R.layout.floating_window_layout, null);
                }
                windowManager.addView(floatingView, currentParams);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing floating window", e);
        }
    }

    @Override
    public void hideFloatingWindow(Context context) {
        try {
            if (isFloatingWindowVisible(context)) {
                windowManager.removeView(floatingView);
                floatingView = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding floating window", e);
        }
    }

    @Override
    public void updateFloatingWindowPosition(Context context, int x, int y) {
        try {
            if (isFloatingWindowVisible(context)) {
                currentParams.x = x;
                currentParams.y = y;
                windowManager.updateViewLayout(floatingView, currentParams);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating window position", e);
        }
    }

    @Override
    public boolean isFloatingWindowVisible(Context context) {
        return floatingView != null && floatingView.getParent() != null;
    }

    @Override
    public WindowManager.LayoutParams getDefaultLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        return params;
    }

    public void setFloatingView(View view) {
        this.floatingView = view;
    }
} 