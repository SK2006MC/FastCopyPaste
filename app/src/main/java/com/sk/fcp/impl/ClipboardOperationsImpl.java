package com.sk.fcp.impl;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import com.sk.fcp.interfaces.ClipboardOperations;

public class ClipboardOperationsImpl implements ClipboardOperations {
    private final Context context;
    private final ClipboardManager clipboardManager;

    public ClipboardOperationsImpl(Context context) {
        this.context = context;
        this.clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public void copyToClipboard(String content) {
        ClipData clip = ClipData.newPlainText("text", content);
        clipboardManager.setPrimaryClip(clip);
    }

    @Override
    public String pasteFromClipboard() {
        if (!clipboardManager.hasPrimaryClip()) {
            return null;
        }
        ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
        return item.getText().toString();
    }
} 