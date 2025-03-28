package com.sk.fcp.utils;

public class Constants {
    public static final String TAG = "FastCopyPaste";
    public static final String INVALID_FILENAME_CHARS_PATTERN = "[\\\\/:*?\"<>|]";
    public static final int SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST = 1234;
    
    // File operations
    public static final String FILE_EXTENSION = ".txt";
    public static final String DEFAULT_FILENAME = "untitled";
    public static final int MAX_FILENAME_LENGTH = 255;
    public static final int MAX_FILE_SIZE = 1024 * 1024; // 1MB
    
    // Preferences
    public static final String PREF_LAST_DIRECTORY = "last_directory";
    public static final String PREF_WINDOW_POSITION_X = "window_position_x";
    public static final String PREF_WINDOW_POSITION_Y = "window_position_y";
    public static final String PREF_WINDOW_OPACITY = "window_opacity";
    public static final String PREF_AUTO_START = "auto_start";
    
    // Service
    public static final String ACTION_TOGGLE_FLOATING_WINDOW = "com.sk.fcp.TOGGLE_FLOATING_WINDOW";
    public static final String ACTION_UPDATE_WINDOW_POSITION = "com.sk.fcp.UPDATE_WINDOW_POSITION";
    
    // Window
    public static final int DEFAULT_WINDOW_OPACITY = 255;
    public static final int MIN_WINDOW_OPACITY = 128;
    public static final int MAX_WINDOW_OPACITY = 255;
    
    private Constants() {
        // Private constructor to prevent instantiation
    }
} 