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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

	private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");
	private static final int SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST = 1234;
	ActivityResultLauncher<Intent> activityResultLauncher;
	private FloatingWindowLayoutBinding binding;
	ActivityMainBinding mainBinding;
	private boolean isServiceRunning = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
		binding = FloatingWindowLayoutBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.buttonSave.setOnClickListener(this::saveFile);
		binding.buttonSaveAs.setOnClickListener(this::saveFileAs);
		binding.buttonClear.setOnClickListener(v -> clearFields());
		binding.buttonOpen.setOnClickListener(this::showFileDialog);
		binding.buttonNew.setOnClickListener(v -> newFile());
		binding.buttonCopy.setOnClickListener(this::copyToClipboard);
		binding.buttonPaste.setOnClickListener(this::pasteFromClipboard);
		binding.buttonToggleFloat.setOnClickListener(v -> toggleFloatingService());

		binding.editTextFileContent.requestFocus();

		binding.editTextFileContent.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				updateWordCount();
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		updateWordCount();

		activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
			//pass
		});
	}

	private void toggleFloatingService() {
		if (!Settings.canDrawOverlays(this)) {
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
			activityResultLauncher.launch(intent);
		} else {
			toggleService();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST) {
			if (Settings.canDrawOverlays(this)) {
				toggleService();
			} else {
				Toast.makeText(this, "Overlay permission denied.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void toggleService() {
		Intent serviceIntent = new Intent(this, FloatingWindowService.class);
		if (!isServiceRunning) {
			startService(serviceIntent);
			binding.buttonToggleFloat.setText(R.string.stop_floating);
		} else {
			stopService(serviceIntent);
			binding.buttonToggleFloat.setText(R.string.start_floating);
		}
		isServiceRunning = !isServiceRunning; // Toggle service state
	}

	private boolean isFloatingServiceRunning() {
		return isServiceRunning;
	}

	private void saveFile(View view) {
		String fileName = binding.editTextFileName.getText().toString().trim();
		String fileContent = binding.editTextFileContent.getText().toString();

		if (fileName.isEmpty()) {
			Snackbar.make(view, "Please enter a file name", Snackbar.LENGTH_SHORT).show();
			return;
		}

		if (INVALID_FILENAME_CHARS.matcher(fileName).find()) {
			Snackbar.make(view, "Invalid filename characters.", Snackbar.LENGTH_LONG).show();
			return;
		}

		if (fileContent.isEmpty()) {
			Snackbar.make(view, "File content is empty, nothing to save.", Snackbar.LENGTH_SHORT).show();
			return;
		}

		File directory = getExternalFilesDir(null);
		File file = new File(directory, fileName);

		try (FileWriter writer = new FileWriter(file)) {
			writer.write(fileContent);
			Snackbar.make(view, "File saved as: " + fileName, Snackbar.LENGTH_SHORT).show();
		} catch (IOException e) {
			Log.e("SaveFileError", "Error saving file: " + e.getMessage());
			Snackbar.make(view, "Error saving file: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
		}
	}

	private void saveFileAs(View view) {
		String fileName = binding.editTextFileName.getText().toString().trim();
		String fileContent = binding.editTextFileContent.getText().toString();

		if (fileName.isEmpty()) {
			Snackbar.make(view, "Please enter a file name to save as", Snackbar.LENGTH_SHORT).show();
			return;
		}

		if (INVALID_FILENAME_CHARS.matcher(fileName).find()) {
			Snackbar.make(view, "Invalid filename characters.", Snackbar.LENGTH_LONG).show();
			return;
		}

		if (fileContent.isEmpty()) {
			Snackbar.make(view, "File content is empty, cannot save.", Snackbar.LENGTH_SHORT).show();
			return;
		}

		File directory = getExternalFilesDir(null);
		File file = new File(directory, fileName);

		try (FileWriter writer = new FileWriter(file)) {
			writer.write(fileContent);
			Snackbar.make(view, "File saved as new file: " + fileName, Snackbar.LENGTH_SHORT).show();
		} catch (IOException e) {
			Log.e("SaveFileAsError", "Error saving file as: " + e.getMessage());
			Snackbar.make(view, "Error saving file as: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
		}
	}

	private void clearFields() {
		binding.editTextFileName.setText("");
		binding.editTextFileContent.setText("");
	}

	private void showFileDialog(View view) {
		File directory = getExternalFilesDir(null);
		assert directory != null;
		File[] files = directory.listFiles();
		if (files == null || files.length == 0) {
			Snackbar.make(view, "No files saved yet.", Snackbar.LENGTH_SHORT).show();
			return;
		}

		List<String> fileNames = new ArrayList<>();
		for (File file : files) {
			if (file.isFile()) {
				fileNames.add(file.getName());
			}
		}

		if (fileNames.isEmpty()) {
			Snackbar.make(view, "No files saved yet.", Snackbar.LENGTH_SHORT).show();
			return;
		}

		CharSequence[] fileList = fileNames.toArray(new CharSequence[0]);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Open File");
		builder.setItems(fileList, (dialog, which) -> {
			String selectedFileName = fileNames.get(which);
			binding.editTextFileName.setText(selectedFileName);
			openFile(selectedFileName, view);
		});
		builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
		builder.create().show();
	}

	private void openFile(String fileName, View view) {
		if (fileName == null || fileName.isEmpty()) {
			Snackbar.make(view, "Please select a file to open", Snackbar.LENGTH_SHORT).show();
			return;
		}

		File directory = getExternalFilesDir(null);
		File file = new File(directory, fileName);

		if (!file.exists()) {
			Snackbar.make(view, "File not found: " + fileName, Snackbar.LENGTH_SHORT).show();
			return;
		}

		StringBuilder text = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
			binding.editTextFileContent.setText(text.toString());
			Snackbar.make(view, "File opened: " + fileName, Snackbar.LENGTH_SHORT).show();

		} catch (IOException e) {
			Log.e("OpenFileError", "Error opening file: " + e.getMessage());
			Snackbar.make(view, "Error opening file: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
		}
	}

	private void newFile() {
		clearFields();
		binding.editTextFileName.setText(R.string.new_file_txt);
		binding.editTextFileContent.requestFocus();
	}

	private void copyToClipboard(View view) {
		ClipboardManagerHelper.copyToClipboard(binding.editTextFileContent.getText().toString(), this);
	}

	private void pasteFromClipboard(View view) {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clipData = clipboard.getPrimaryClip();
		if (clipData != null && clipData.getItemCount() > 0) {
			CharSequence text = clipData.getItemAt(0).getText();
			if (text != null) {
				int currentCursorPos = binding.editTextFileContent.getSelectionStart();
				binding.editTextFileContent.getText().insert(currentCursorPos, text);
				binding.editTextFileContent.setSelection(currentCursorPos + text.length());
				Snackbar.make(view, "Content pasted from clipboard", Snackbar.LENGTH_SHORT).show();
			} else {
				Snackbar.make(view, "No text content found in clipboard", Snackbar.LENGTH_SHORT).show();
			}
		} else {
			Snackbar.make(view, "Clipboard is empty", Snackbar.LENGTH_SHORT).show();
		}
	}

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
}