package com.sk.fcp.interfaces;

import java.io.IOException;

public interface FileOperations {
    void saveFile(String fileName, String content) throws IOException;
    void saveFileAs(String fileName, String content) throws IOException;
    String openFile(String fileName) throws IOException;
    String[] getListOfFiles();
    boolean validateFileOperation(String fileName, String content);
} 