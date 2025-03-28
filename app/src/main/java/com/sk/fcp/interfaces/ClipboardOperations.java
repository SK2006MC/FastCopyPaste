package com.sk.fcp.interfaces;

public interface ClipboardOperations {
    void copyToClipboard(String content);
    String pasteFromClipboard();
} 