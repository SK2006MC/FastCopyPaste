package com.sk.fcp.core.clipboard;

import android.content.Context;

public interface ClipboardOperations {
    void copyToClipboard(Context context, String text);
    String pasteFromClipboard(Context context);
    boolean hasClipboardContent(Context context);
    void clearClipboard(Context context);
} 