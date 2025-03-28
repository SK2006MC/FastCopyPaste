package com.sk.fcp;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Manages file operations for the FastCopyPaste application.
 * Handles saving, opening, and listing files with proper error handling and validation.
 */
public class FileManager {
	private static final String TAG = "FileManager";
	private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");

	/**
	 * Saves content to a file with validation and error handling.
	 *
	 * @param fileName    The name of the file to save
	 * @param fileContent The content to save
	 * @param context     The application context
	 * @throws IllegalArgumentException if fileName is invalid or fileContent is null
	 * @throws IOException if file operations fail
	 */
	public static void saveFile(String fileName, String fileContent, Context context) throws IOException {
		validateFileName(fileName);
		if (fileContent == null) {
			throw new IllegalArgumentException("File content cannot be null");
		}

		File directory = context.getFilesDir();
		File file = new File(directory, fileName);

		try (FileWriter writer = new FileWriter(file)) {
			writer.write(fileContent);
			Log.d(TAG, "File saved successfully: " + fileName);
		} catch (IOException e) {
			Log.e(TAG, "Error saving file: " + fileName, e);
			throw new IOException("Failed to save file: " + e.getMessage(), e);
		}
	}

	/**
	 * Opens and reads content from a file.
	 *
	 * @param fileName The name of the file to open
	 * @param context  The application context
	 * @return The content of the file as a String
	 * @throws IllegalArgumentException if fileName is invalid
	 * @throws IOException if file operations fail
	 */
	public static String openFile(String fileName, Context context) throws IOException {
		validateFileName(fileName);

		File directory = context.getFilesDir();
		File file = new File(directory, fileName);

		if (!file.exists()) {
			throw new IOException("File does not exist: " + fileName);
		}

		StringBuilder text = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
			Log.d(TAG, "File opened successfully: " + fileName);
			return text.toString();
		} catch (IOException e) {
			Log.e(TAG, "Error opening file: " + fileName, e);
			throw new IOException("Failed to open file: " + e.getMessage(), e);
		}
	}

	/**
	 * Gets a list of all files in the application's file directory.
	 *
	 * @param context The application context
	 * @return List of file names
	 */
	public static List<String> getListOfFiles(Context context) {
		List<String> fileNames = new ArrayList<>();
		File directory = context.getFilesDir();
		File[] files = directory.listFiles();

		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					fileNames.add(file.getName());
				}
			}
		}

		Log.d(TAG, "Retrieved " + fileNames.size() + " files");
		return fileNames;
	}

	/**
	 * Validates a file name for invalid characters.
	 *
	 * @param fileName The file name to validate
	 * @throws IllegalArgumentException if the file name is invalid
	 */
	private static void validateFileName(String fileName) {
		if (fileName == null || fileName.trim().isEmpty()) {
			throw new IllegalArgumentException("File name cannot be null or empty");
		}
		if (INVALID_FILENAME_CHARS.matcher(fileName).find()) {
			throw new IllegalArgumentException("File name contains invalid characters");
		}
	}
	//... other functions
}