package com.sk.fcp.core.window;

import android.content.Context;
import android.view.WindowManager;

public interface WindowOperations {
    void showFloatingWindow(Context context);
    void hideFloatingWindow(Context context);
    void updateFloatingWindowPosition(Context context, int x, int y);
    boolean isFloatingWindowVisible(Context context);
    WindowManager.LayoutParams getDefaultLayoutParams();
} 