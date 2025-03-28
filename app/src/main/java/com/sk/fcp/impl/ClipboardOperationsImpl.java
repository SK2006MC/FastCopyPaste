package com.sk.fcp.impl;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import com.sk.fcp.interfaces.ClipboardOperations;

public class ClipboardOperationsImpl implements ClipboardOperations {
    private static final String TAG = "ClipboardOperationsImpl";
    private final Context context;

    public ClipboardOperationsImpl(Context context) {
        this.context = context;
    }

    private ClipboardManager getClipboardManager() {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            throw new IllegalStateException("Clipboard service is not available");
        }
        return clipboardManager;
    }

    @Override
    public void copyToClipboard(String content) {
        try {
            if (content == null) {
                Log.w(TAG, "Attempted to copy null content to clipboard");
                return;
            }
            ClipboardManager clipboardManager = getClipboardManager();
            ClipData clip = ClipData.newPlainText("text", content);
            clipboardManager.setPrimaryClip(clip);
        } catch (Exception e) {
            Log.e(TAG, "Error copying to clipboard", e);
            throw new IllegalStateException("Failed to copy to clipboard: " + e.getMessage(), e);
        }
    }

    @Override
    public String pasteFromClipboard() {
        try {
            ClipboardManager clipboardManager = getClipboardManager();
            if (!clipboardManager.hasPrimaryClip()) {
                return null;
            }
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData == null || clipData.getItemCount() == 0) {
                return null;
            }
            ClipData.Item item = clipData.getItemAt(0);
            if (item == null || item.getText() == null) {
                return null;
            }
            return item.getText().toString();
        } catch (Exception e) {
            Log.e(TAG, "Error pasting from clipboard", e);
            throw new IllegalStateException("Failed to paste from clipboard: " + e.getMessage(), e);
        }
    }
} 