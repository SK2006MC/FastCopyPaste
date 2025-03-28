package com.sk.fcp.config;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.sk.fcp.utils.Constants;

public class AppConfig {
    private static final String TAG = Constants.TAG + ".AppConfig";
    private final SharedPreferences preferences;
    private final Context context;

    public AppConfig(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getLastDirectory() {
        return preferences.getString(Constants.PREF_LAST_DIRECTORY, "");
    }

    public void setLastDirectory(String directory) {
        preferences.edit().putString(Constants.PREF_LAST_DIRECTORY, directory).apply();
    }

    public int getWindowPositionX() {
        return preferences.getInt(Constants.PREF_WINDOW_POSITION_X, 0);
    }

    public void setWindowPositionX(int x) {
        preferences.edit().putInt(Constants.PREF_WINDOW_POSITION_X, x).apply();
    }

    public int getWindowPositionY() {
        return preferences.getInt(Constants.PREF_WINDOW_POSITION_Y, 0);
    }

    public void setWindowPositionY(int y) {
        preferences.edit().putInt(Constants.PREF_WINDOW_POSITION_Y, y).apply();
    }

    public int getWindowOpacity() {
        return preferences.getInt(Constants.PREF_WINDOW_OPACITY, Constants.DEFAULT_WINDOW_OPACITY);
    }

    public void setWindowOpacity(int opacity) {
        // Ensure opacity is within bounds
        opacity = Math.max(Constants.MIN_WINDOW_OPACITY, Math.min(opacity, Constants.MAX_WINDOW_OPACITY));
        preferences.edit().putInt(Constants.PREF_WINDOW_OPACITY, opacity).apply();
    }

    public boolean isAutoStartEnabled() {
        return preferences.getBoolean(Constants.PREF_AUTO_START, false);
    }

    public void setAutoStartEnabled(boolean enabled) {
        preferences.edit().putBoolean(Constants.PREF_AUTO_START, enabled).apply();
    }

    public boolean hasOverlayPermission() {
        return android.provider.Settings.canDrawOverlays(context);
    }
} 