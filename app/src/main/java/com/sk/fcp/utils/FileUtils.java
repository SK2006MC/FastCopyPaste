package com.sk.fcp.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class FileUtils {
    private static final String TAG = Constants.TAG + ".FileUtils";
    private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile(Constants.INVALID_FILENAME_CHARS_PATTERN);

    public static String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    public static String readFileContentFromUri(Context context, Uri uri) throws IOException {
        StringBuilder content = new StringBuilder();
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    public static void writeFileContent(File file, String content) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    public static boolean isValidFileName(String fileName) {
        return fileName != null && !fileName.isEmpty() && !INVALID_FILENAME_CHARS.matcher(fileName).find();
    }

    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return Constants.DEFAULT_FILENAME;
        }
        return INVALID_FILENAME_CHARS.matcher(fileName).replaceAll("_");
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return Constants.FILE_EXTENSION;
        }
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : Constants.FILE_EXTENSION;
    }

    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return Constants.DEFAULT_FILENAME;
        }
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }
} 