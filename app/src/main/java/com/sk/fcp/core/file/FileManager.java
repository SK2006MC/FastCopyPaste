package com.sk.fcp.core.file;

import android.util.Log;

import com.sk.fcp.utils.Constants;
import com.sk.fcp.utils.FileUtils;

import java.io.File;
import java.io.IOException;

public class FileManager implements FileOperations {
    private static final String TAG = Constants.TAG + ".FileManager";

    @Override
    public void saveFile(File file, String content) throws IOException {
        try {
            FileUtils.writeFileContent(file, content);
        } catch (IOException e) {
            Log.e(TAG, "Error saving file: " + file.getPath(), e);
            throw e;
        }
    }

    @Override
    public String readFile(File file) throws IOException {
        try {
            return FileUtils.readFileContent(file);
        } catch (IOException e) {
            Log.e(TAG, "Error reading file: " + file.getPath(), e);
            throw e;
        }
    }

    @Override
    public boolean isValidFileName(String fileName) {
        return FileUtils.isValidFileName(fileName);
    }

    @Override
    public String sanitizeFileName(String fileName) {
        return FileUtils.sanitizeFileName(fileName);
    }

    @Override
    public String getFileExtension(String fileName) {
        return FileUtils.getFileExtension(fileName);
    }

    @Override
    public String getFileNameWithoutExtension(String fileName) {
        return FileUtils.getFileNameWithoutExtension(fileName);
    }
} 