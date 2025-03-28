package com.sk.fcp.impl;

import android.content.Context;
import android.util.Patterns;
import com.sk.fcp.interfaces.FileOperations;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileOperationsImpl implements FileOperations {
    private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");
    private final Context context;

    public FileOperationsImpl(Context context) {
        this.context = context;
    }

    private File getFilesDirectory() {
        File filesDir = context.getFilesDir();
        if (filesDir == null) {
            throw new IllegalStateException("Files directory is not available");
        }
        return filesDir;
    }

    @Override
    public void saveFile(String fileName, String content) throws IOException {
        if (!validateFileOperation(fileName, content)) {
            throw new IllegalArgumentException("Invalid file operation");
        }
        File file = new File(getFilesDirectory(), fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    @Override
    public void saveFileAs(String fileName, String content) throws IOException {
        saveFile(fileName, content);
    }

    @Override
    public String openFile(String fileName) throws IOException {
        File file = new File(getFilesDirectory(), fileName);
        StringBuilder content = new StringBuilder();
        try (FileReader reader = new FileReader(file)) {
            char[] buffer = new char[1024];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, bytesRead);
            }
        }
        return content.toString();
    }

    @Override
    public String[] getListOfFiles() {
        File directory = getFilesDirectory();
        File[] files = directory.listFiles();
        List<String> fileNames = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileNames.add(file.getName());
                }
            }
        }
        return fileNames.toArray(new String[0]);
    }

    @Override
    public boolean validateFileOperation(String fileName, String content) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        if (INVALID_FILENAME_CHARS.matcher(fileName).find()) {
            return false;
        }
        return content != null;
    }
} 