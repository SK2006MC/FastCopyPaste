package com.sk.fcp;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.sk.fcp.databinding.FloatingWindowLayoutBinding;

import java.io.IOException;

/**
 * Service that manages a floating window for the FastCopyPaste application.
 * Provides a draggable window interface for quick access to file operations.
 */
public class FloatingWindowService extends Service {
    private static final String TAG = "FloatingWindowService";
    private static final int INITIAL_X = 0;
    private static final int INITIAL_Y = 100;

    private WindowManager windowManager;
    private View floatingWindowView;
    private WindowManager.LayoutParams layoutParams;
    private FloatingWindowLayoutBinding binding;

    public FloatingWindowService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            initializeFloatingWindow();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing floating window", e);
            Toast.makeText(this, "Failed to initialize floating window", Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    /**
     * Initializes the floating window with proper layout and parameters.
     */
    private void initializeFloatingWindow() {
        // Inflate the floating window layout
        binding = FloatingWindowLayoutBinding.inflate(LayoutInflater.from(this));
        floatingWindowView = binding.getRoot();

        // Set layout parameters for the floating window
        layoutParams = createLayoutParams();

        // Add the view to the window
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (windowManager == null) {
            throw new IllegalStateException("Window service not available");
        }
        windowManager.addView(floatingWindowView, layoutParams);

        // Make the window draggable
        setupDragAndDrop();
        setupButtons();
        setInitialText();
    }

    /**
     * Creates layout parameters for the floating window.
     */
    private WindowManager.LayoutParams createLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = INITIAL_X;
        params.y = INITIAL_Y;
        return params;
    }

    /**
     * Sets up drag and drop functionality for the floating window.
     */
    private void setupDragAndDrop() {
        View titleBar = floatingWindowView;
        titleBar.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float touchX, touchY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                view.performClick();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        layoutParams.x = initialX + (int) (event.getRawX() - touchX);
                        layoutParams.y = initialY + (int) (event.getRawY() - touchY);
                        windowManager.updateViewLayout(floatingWindowView, layoutParams);
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                }
                return false;
            }
        });
    }

    /**
     * Sets up button click listeners for the floating window.
     */
    private void setupButtons() {
        binding.buttonToggleFloat.setOnClickListener(v -> stopSelf());
        binding.buttonSave.setOnClickListener(v -> saveCurrentFile());
        binding.buttonCopy.setOnClickListener(v -> copyToClipboard());
        binding.buttonPaste.setOnClickListener(v -> pasteFromClipboard());
    }

    /**
     * Sets the initial text in the floating window.
     */
    private void setInitialText() {
        if (binding.editTextFileName != null) {
            binding.editTextFileName.setText(R.string.floating_window_running_text);
        } else {
            Log.e(TAG, "editTextFileName is null");
        }
    }

    /**
     * Saves the current file content.
     */
    private void saveCurrentFile() {
        try {
            String fileName = binding.editTextFileName.getText().toString();
            String content = binding.editTextFileContent.getText().toString();
            
            if (fileName.isEmpty()) {
                Toast.makeText(this, "Please enter a file name", Toast.LENGTH_SHORT).show();
                return;
            }

            FileManager.saveFile(fileName, content, this);
            Toast.makeText(this, "File saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Error saving file", e);
            Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Copies content to clipboard.
     */
    private void copyToClipboard() {
        String content = binding.editTextFileContent.getText().toString();
        if (!content.isEmpty()) {
            try {
                ClipboardManagerHelper.copyToClipboard(content, this);
                Toast.makeText(this, "Content copied to clipboard", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error copying to clipboard", e);
                Toast.makeText(this, "Error copying to clipboard", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Nothing to copy (File content is empty)", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Pastes content from clipboard.
     */
    private void pasteFromClipboard() {
        try {
            String text = ClipboardManagerHelper.pasteFromClipboard(this);
            if (text != null) {
                int currentCursorPos = binding.editTextFileContent.getSelectionStart();
                binding.editTextFileContent.getText().insert(currentCursorPos, text);
                binding.editTextFileContent.setSelection(currentCursorPos + text.length());
                Toast.makeText(this, "Content pasted from clipboard", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error pasting from clipboard", e);
            Toast.makeText(this, "Error pasting from clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingWindowView != null && windowManager != null) {
            try {
                windowManager.removeView(floatingWindowView);
                floatingWindowView = null;
            } catch (Exception e) {
                Log.e(TAG, "Error removing floating window", e);
            }
        }
    }
}