package com.sk.fcp;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.sk.fcp.databinding.FloatingWindowLayoutBinding;
import com.sk.fcp.impl.ClipboardOperationsImpl;
import com.sk.fcp.impl.FileOperationsImpl;
import com.sk.fcp.interfaces.ClipboardOperations;
import com.sk.fcp.interfaces.FileOperations;

import java.io.IOException;
import java.util.Locale;

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
    private final FileOperations fileOperations;
    private final ClipboardOperations clipboardOperations;

    public FloatingWindowService() {
        super();
        this.fileOperations = new FileOperationsImpl(this);
        this.clipboardOperations = new ClipboardOperationsImpl(this);
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

        // Setup all functionality
        setupDragAndDrop();
        setupButtons();
        setupTextWatcher();
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
        binding.buttonSave.setOnClickListener(v -> saveFile());
        binding.buttonSaveAs.setOnClickListener(v -> saveFileAs());
        binding.buttonClear.setOnClickListener(v -> clearFields());
        binding.buttonOpen.setOnClickListener(v -> showFileDialog());
        binding.buttonNew.setOnClickListener(v -> newFile());
        binding.buttonCopy.setOnClickListener(v -> copyToClipboard());
        binding.buttonPaste.setOnClickListener(v -> pasteFromClipboard());
    }

    /**
     * Sets up text change listener for word count updates.
     */
    private void setupTextWatcher() {
        binding.editTextFileContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateWordCount();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
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
     * Saves the current file.
     */
    private void saveFile() {
        String fileName = binding.editTextFileName.getText().toString().trim();
        String fileContent = binding.editTextFileContent.getText().toString();

        if (!validateFileOperation(fileName, fileContent)) {
            return;
        }

        try {
            fileOperations.saveFile(fileName, fileContent);
            Toast.makeText(this, "File saved as: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Error saving file", e);
            Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves the current file with a new name.
     */
    private void saveFileAs() {
        String fileName = binding.editTextFileName.getText().toString().trim();
        String fileContent = binding.editTextFileContent.getText().toString();

        if (!validateFileOperation(fileName, fileContent)) {
            return;
        }

        try {
            fileOperations.saveFileAs(fileName, fileContent);
            Toast.makeText(this, "File saved as new file: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Error saving file as", e);
            Toast.makeText(this, "Error saving file as: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates file operation parameters.
     */
    private boolean validateFileOperation(String fileName, String fileContent) {
        if (fileName.isEmpty()) {
            Toast.makeText(this, "Please enter a file name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (fileContent.isEmpty()) {
            Toast.makeText(this, "File content is empty, nothing to save.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Clears all input fields.
     */
    private void clearFields() {
        binding.editTextFileName.setText("");
        binding.editTextFileContent.setText("");
    }

    /**
     * Shows the file selection dialog.
     */
    private void showFileDialog() {
        String[] fileNames = fileOperations.getListOfFiles();
        if (fileNames.length == 0) {
            Toast.makeText(this, "No files saved yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Open File");
        builder.setItems(fileNames, (dialog, which) -> {
            String selectedFileName = fileNames[which];
            binding.editTextFileName.setText(selectedFileName);
            openFile(selectedFileName);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * Opens a selected file.
     */
    private void openFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            Toast.makeText(this, "Please select a file to open", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String content = fileOperations.openFile(fileName);
            binding.editTextFileContent.setText(content);
            Toast.makeText(this, "File opened: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Error opening file", e);
            Toast.makeText(this, "Error opening file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates a new file.
     */
    private void newFile() {
        clearFields();
        binding.editTextFileName.setText(R.string.new_file_txt);
        binding.editTextFileContent.requestFocus();
    }

    /**
     * Copies content to clipboard.
     */
    private void copyToClipboard() {
        String content = binding.editTextFileContent.getText().toString();
        if (!content.isEmpty()) {
            try {
                clipboardOperations.copyToClipboard(content);
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
            String text = clipboardOperations.pasteFromClipboard();
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

    /**
     * Updates the word and character count display.
     */
    private void updateWordCount() {
        String text = binding.editTextFileContent.getText().toString().trim();
        int wordCount = 0;
        int charCount = text.length();

        if (!text.isEmpty()) {
            String[] words = text.split("\\s+");
            wordCount = words.length;
        }

        binding.textViewWordCount.setText(String.format(Locale.ENGLISH, "Words: %d, Chars: %d", wordCount, charCount));
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