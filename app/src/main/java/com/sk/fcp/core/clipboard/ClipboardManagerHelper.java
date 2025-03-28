package com.sk.fcp.core.clipboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

import com.sk.fcp.utils.Constants;

public class ClipboardManagerHelper implements ClipboardOperations {
    private static final String TAG = Constants.TAG + ".ClipboardManagerHelper";
    private final ClipboardManager clipboardManager;

    public ClipboardManagerHelper(Context context) {
        this.clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public void copyToClipboard(Context context, String text) {
        try {
            ClipData clipData = ClipData.newPlainText("text", text);
            clipboardManager.setPrimaryClip(clipData);
        } catch (Exception e) {
            Log.e(TAG, "Error copying to clipboard", e);
        }
    }

    @Override
    public String pasteFromClipboard(Context context) {
        try {
            if (hasClipboardContent(context)) {
                ClipData clipData = clipboardManager.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    return clipData.getItemAt(0).getText().toString();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error pasting from clipboard", e);
        }
        return "";
    }

    @Override
    public boolean hasClipboardContent(Context context) {
        return clipboardManager.hasPrimaryClip();
    }

    @Override
    public void clearClipboard(Context context) {
        try {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""));
        } catch (Exception e) {
            Log.e(TAG, "Error clearing clipboard", e);
        }
    }
} 