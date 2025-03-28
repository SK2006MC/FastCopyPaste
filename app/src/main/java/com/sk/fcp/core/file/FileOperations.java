package com.sk.fcp.core.file;

import java.io.File;
import java.io.IOException;

public interface FileOperations {
    void saveFile(File file, String content) throws IOException;
    String readFile(File file) throws IOException;
    boolean isValidFileName(String fileName);
    String sanitizeFileName(String fileName);
    String getFileExtension(String fileName);
    String getFileNameWithoutExtension(String fileName);
} 