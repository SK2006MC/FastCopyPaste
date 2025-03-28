package com.sk.fcp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.sk.fcp.databinding.ActivityMainBinding;
import com.sk.fcp.databinding.FloatingWindowLayoutBinding;
import com.sk.fcp.impl.ClipboardOperationsImpl;
import com.sk.fcp.impl.FileOperationsImpl;
import com.sk.fcp.impl.FloatingWindowManagerImpl;
import com.sk.fcp.interfaces.ClipboardOperations;
import com.sk.fcp.interfaces.FileOperations;
import com.sk.fcp.interfaces.FloatingWindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Main activity for the FastCopyPaste application.
 * Provides the main interface for file operations and floating window management.
 */
public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");
	private static final int SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST = 1234;
	ActivityResultLauncher<Intent> activityResultLauncher;
	private FloatingWindowLayoutBinding binding;
	ActivityMainBinding mainBinding;
	private final FileOperations fileOperations;
	private final ClipboardOperations clipboardOperations;
	private final FloatingWindowManager floatingWindowManager;
	private boolean isServiceRunning = false;

	public MainActivity() {
		this.fileOperations = new FileOperationsImpl(this);
		this.clipboardOperations = new ClipboardOperationsImpl(this);
		this.floatingWindowManager = new FloatingWindowManagerImpl(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initializeViews();
		setupClickListeners();
		setupTextWatcher();
		setupActivityResultLauncher();
		updateWordCount();
	}

	/**
	 * Initializes the views using ViewBinding.
	 */
	private void initializeViews() {
		mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
		binding = FloatingWindowLayoutBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		binding.editTextFileContent.requestFocus();
	}

	/**
	 * Sets up click listeners for all buttons.
	 */
	private void setupClickListeners() {
		binding.buttonSave.setOnClickListener(this::saveFile);
		binding.buttonSaveAs.setOnClickListener(this::saveFileAs);
		binding.buttonClear.setOnClickListener(v -> clearFields());
		binding.buttonOpen.setOnClickListener(this::showFileDialog);
		binding.buttonNew.setOnClickListener(v -> newFile());
		binding.buttonCopy.setOnClickListener(this::copyToClipboard);
		binding.buttonPaste.setOnClickListener(this::pasteFromClipboard);
		binding.buttonToggleFloat.setOnClickListener(v -> toggleFloatingService());
	}

	/**
	 * Sets up text change listener for word count updates.
	 */
	private void setupTextWatcher() {
		binding.editTextFileContent.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// Not used
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				updateWordCount();
			}

			@Override
			public void afterTextChanged(Editable s) {
				// Not used
			}
		});
	}

	/**
	 * Sets up the activity result launcher for permission requests.
	 */
	private void setupActivityResultLauncher() {
		activityResultLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result -> {
				if (Settings.canDrawOverlays(this)) {
					startFloatingService();
				} else {
					Snackbar.make(binding.getRoot(), R.string.overlay_permission_denied, Snackbar.LENGTH_SHORT).show();
				}
			}
		);
	}

	/**
	 * Saves the current file.
	 */
	private void saveFile(View view) {
		String fileName = binding.editTextFileName.getText().toString().trim();
		String fileContent = binding.editTextFileContent.getText().toString();

		if (!validateFileOperation(fileName, fileContent, view)) {
			return;
		}

		try {
			fileOperations.saveFile(fileName, fileContent);
			Snackbar.make(view, "File saved as: " + fileName, Snackbar.LENGTH_SHORT).show();
		} catch (IllegalArgumentException e) {
			Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_SHORT).show();
		} catch (IOException e) {
			Log.e(TAG, "Error saving file", e);
			Snackbar.make(view, "Error saving file: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
		}
	}

	/**
	 * Saves the current file with a new name.
	 */
	private void saveFileAs(View view) {
		String fileName = binding.editTextFileName.getText().toString().trim();
		String fileContent = binding.editTextFileContent.getText().toString();

		if (!validateFileOperation(fileName, fileContent, view)) {
			return;
		}

		try {
			fileOperations.saveFileAs(fileName, fileContent);
			Snackbar.make(view, "File saved as new file: " + fileName, Snackbar.LENGTH_SHORT).show();
		} catch (IllegalArgumentException e) {
			Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_SHORT).show();
		} catch (IOException e) {
			Log.e(TAG, "Error saving file as", e);
			Snackbar.make(view, "Error saving file as: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
		}
	}

	/**
	 * Validates file operation parameters.
	 */
	private boolean validateFileOperation(String fileName, String fileContent, View view) {
		if (fileName.isEmpty()) {
			Snackbar.make(view, "Please enter a file name", Snackbar.LENGTH_SHORT).show();
			return false;
		}

		if (fileContent.isEmpty()) {
			Snackbar.make(view, "File content is empty, nothing to save.", Snackbar.LENGTH_SHORT).show();
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
	private void showFileDialog(View view) {
		String[] fileNames = fileOperations.getListOfFiles();
		if (fileNames.length == 0) {
			Snackbar.make(view, "No files saved yet.", Snackbar.LENGTH_SHORT).show();
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Open File");
		builder.setItems(fileNames, (dialog, which) -> {
			String selectedFileName = fileNames[which];
			binding.editTextFileName.setText(selectedFileName);
			openFile(selectedFileName, view);
		});
		builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
		builder.create().show();
	}

	/**
	 * Opens a selected file.
	 */
	private void openFile(String fileName, View view) {
		if (fileName == null || fileName.isEmpty()) {
			Snackbar.make(view, "Please select a file to open", Snackbar.LENGTH_SHORT).show();
			return;
		}

		try {
			String content = fileOperations.openFile(fileName);
			binding.editTextFileContent.setText(content);
			Snackbar.make(view, "File opened: " + fileName, Snackbar.LENGTH_SHORT).show();
		} catch (IOException e) {
			Log.e(TAG, "Error opening file", e);
			Snackbar.make(view, "Error opening file: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
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
	private void copyToClipboard(View view) {
		try {
			String content = binding.editTextFileContent.getText().toString();
			if (!content.isEmpty()) {
				clipboardOperations.copyToClipboard(content);
				Snackbar.make(view, "Content copied to clipboard", Snackbar.LENGTH_SHORT).show();
			} else {
				Snackbar.make(view, "Nothing to copy (File content is empty)", Snackbar.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.e(TAG, "Error copying to clipboard", e);
			Snackbar.make(view, "Error copying to clipboard", Snackbar.LENGTH_SHORT).show();
		}
	}

	/**
	 * Pastes content from clipboard.
	 */
	private void pasteFromClipboard(View view) {
		try {
			String text = clipboardOperations.pasteFromClipboard();
			if (text != null) {
				int currentCursorPos = binding.editTextFileContent.getSelectionStart();
				binding.editTextFileContent.getText().insert(currentCursorPos, text);
				binding.editTextFileContent.setSelection(currentCursorPos + text.length());
				Snackbar.make(view, "Content pasted from clipboard", Snackbar.LENGTH_SHORT).show();
			} else {
				Snackbar.make(view, "Clipboard is empty", Snackbar.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.e(TAG, "Error pasting from clipboard", e);
			Snackbar.make(view, "Error pasting from clipboard", Snackbar.LENGTH_SHORT).show();
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

	/**
	 * Toggles the floating window service.
	 */
	private void toggleFloatingService() {
		if (!floatingWindowManager.isServiceRunning()) {
			if (floatingWindowManager.hasOverlayPermission()) {
				startFloatingService();
				binding.buttonToggleFloat.setText(R.string.stop_floating);
				Snackbar.make(binding.getRoot(), R.string.start_floating_window, Snackbar.LENGTH_SHORT).show();
			} else {
				floatingWindowManager.requestOverlayPermission();
			}
		} else {
			floatingWindowManager.stopFloatingService();
			binding.buttonToggleFloat.setText(R.string.start_floating);
		}
	}

	/**
	 * Starts the floating window service.
	 */
	private void startFloatingService() {
		Intent intent = new Intent(this, FloatingWindowService.class);
		startService(intent);
		isServiceRunning = true;
		binding.buttonToggleFloat.setText(R.string.stop_floating);
		Snackbar.make(binding.getRoot(), R.string.start_floating_window, Snackbar.LENGTH_SHORT).show();
	}

	/**
	 * Stops the floating window service.
	 */
	private void stopFloatingService() {
		Intent intent = new Intent(this, FloatingWindowService.class);
		stopService(intent);
		isServiceRunning = false;
		binding.buttonToggleFloat.setText(R.string.start_floating);
	}
}