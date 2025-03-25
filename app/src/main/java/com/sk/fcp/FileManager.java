package com.sk.fcp.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

	private static final String TAG = "FileManager";

	public static void saveFile(String fileName, String fileContent, Context context) throws IOException {
		File directory = context.getFilesDir();
		File file = new File(directory, fileName);

		try (FileWriter writer = new FileWriter(file)) {
			writer.write(fileContent);
		}
	}

	public static String openFile(String fileName, Context context) throws IOException {
		File directory = context.getFilesDir();
		File file = new File(directory, fileName);

		StringBuilder text = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
		}
		return text.toString();
	}

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

		return fileNames;
	}
	//... other functions
}