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
import android.widget.EditText;
import android.widget.Toast;

import com.sk.fcp.databinding.FloatingWindowLayoutBinding;

import java.io.IOException;

public class FloatingWindowService extends Service {

    // Constants
    private static final int INITIAL_X = 0;
    private static final int INITIAL_Y = 100;
    private WindowManager windowManager;
    private View floatingWindowView;
    private WindowManager.LayoutParams layoutParams;
    private FloatingWindowLayoutBinding binding;

    public FloatingWindowService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeFloatingWindow();
    }

    private void initializeFloatingWindow() {
        // Inflate the floating window layout
        binding = FloatingWindowLayoutBinding.inflate(LayoutInflater.from(this));
        floatingWindowView = binding.getRoot();

        // Set layout parameters for the floating window
        layoutParams = createLayoutParams();

        // Add the view to the window
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(floatingWindowView, layoutParams);

        // Make the window draggable
        setupDragAndDrop();
        //set the functions of the buttons
//        setupButtons();

        // Set initial text
        setInitialText();
    }

    private WindowManager.LayoutParams createLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        // Specify the position of the floating window
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = INITIAL_X;
        params.y = INITIAL_Y;
        return params;
    }

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

    private void setupButtons() {
        binding.buttonToggleFloat.setOnClickListener(v -> stopSelf());
        binding.buttonSave.setOnClickListener(v -> saveCurrentFile());
        binding.buttonCopy.setOnClickListener(v -> copyToClipboard());
        binding.buttonPaste.setOnClickListener(v -> pasteFromClipboard());
    }

    private void setInitialText() {
        EditText editText = binding.editTextFileName;
        if (editText != null) {
            editText.setText(R.string.floating_window_running_text);
        } else {
            Log.e("FloatingWindowService", "editText null");
        }
    }

    private void saveCurrentFile() {
        try {
            FileManager.saveFile(binding.editTextFileName.getText().toString(), binding.editTextFileContent.getText().toString(), this);
            Toast.makeText(this, "File saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("FloatingWindowService", "Error saving file: " + e.getMessage());
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyToClipboard() {
        if (!binding.editTextFileContent.getText().toString().isEmpty()) {
            ClipboardManagerHelper.copyToClipboard(binding.editTextFileContent.getText().toString(), this);
            Toast.makeText(this, "Content copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Nothing to copy (File content is empty)", Toast.LENGTH_SHORT).show();
        }

    }

    private void pasteFromClipboard() {
        String text = ClipboardManagerHelper.pasteFromClipboard(this);
        if (text != null) {
            int currentCursorPos = binding.editTextFileContent.getSelectionStart();
            binding.editTextFileContent.getText().insert(currentCursorPos, text);
            binding.editTextFileContent.setSelection(currentCursorPos + text.length());
            Toast.makeText(this, "Content pasted from clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingWindowView != null) {
            windowManager.removeView(floatingWindowView);
            floatingWindowView = null;
        }
    }
}