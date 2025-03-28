package com.sk.fcp.impl;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import com.sk.fcp.FloatingWindowService;
import com.sk.fcp.R;
import com.sk.fcp.interfaces.FloatingWindowManager;

public class FloatingWindowManagerImpl implements FloatingWindowManager {
    private final ComponentActivity activity;
    private boolean isServiceRunning = false;
    private final ActivityResultLauncher<Intent> activityResultLauncher;

    public FloatingWindowManagerImpl(ComponentActivity activity) {
        this.activity = activity;
        this.activityResultLauncher = registerForActivityResult();
    }

    private ActivityResultLauncher<Intent> registerForActivityResult() {
        return activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (hasOverlayPermission()) {
                    startFloatingService();
                }
            }
        );
    }

    @Override
    public void startFloatingService() {
        Intent intent = new Intent(activity, FloatingWindowService.class);
        activity.startService(intent);
        isServiceRunning = true;
    }

    @Override
    public void stopFloatingService() {
        Intent intent = new Intent(activity, FloatingWindowService.class);
        activity.stopService(intent);
        isServiceRunning = false;
    }

    @Override
    public boolean isServiceRunning() {
        return isServiceRunning;
    }

    @Override
    public void requestOverlayPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.overlay_permission_request_message);
        builder.setPositiveButton("OK", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            activityResultLauncher.launch(intent);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public boolean hasOverlayPermission() {
        return Settings.canDrawOverlays(activity);
    }
} 