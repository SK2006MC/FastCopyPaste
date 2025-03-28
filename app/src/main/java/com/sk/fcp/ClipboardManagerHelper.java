package com.sk.fcp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

/**
 * Helper class for managing clipboard operations in the FastCopyPaste application.
 * Provides methods for copying and pasting text with proper error handling and validation.
 */
public class ClipboardManagerHelper {
	private static final String TAG = "ClipboardManagerHelper";
	private static final String CLIP_LABEL = "FastCopyPaste";

	/**
	 * Copies text to the system clipboard.
	 *
	 * @param textToCopy The text to copy to clipboard
	 * @param context    The application context
	 * @throws IllegalArgumentException if textToCopy is null
	 */
	public static void copyToClipboard(String textToCopy, Context context) {
		if (textToCopy == null) {
			throw new IllegalArgumentException("Text to copy cannot be null");
		}

		try {
			ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			if (clipboard == null) {
				throw new IllegalStateException("Clipboard service not available");
			}

			ClipData clip = ClipData.newPlainText(CLIP_LABEL, textToCopy);
			clipboard.setPrimaryClip(clip);
			Log.d(TAG, "Text copied to clipboard successfully");
		} catch (Exception e) {
			Log.e(TAG, "Error copying to clipboard", e);
			throw new RuntimeException("Failed to copy to clipboard: " + e.getMessage(), e);
		}
	}

	/**
	 * Retrieves text from the system clipboard.
	 *
	 * @param context The application context
	 * @return The text from clipboard, or null if clipboard is empty or contains no text
	 */
	public static String pasteFromClipboard(Context context) {
		try {
			ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			if (clipboard == null) {
				throw new IllegalStateException("Clipboard service not available");
			}

			ClipData clipData = clipboard.getPrimaryClip();
			if (clipData != null && clipData.getItemCount() > 0) {
				CharSequence text = clipData.getItemAt(0).getText();
				if (text != null) {
					Log.d(TAG, "Text retrieved from clipboard successfully");
					return text.toString();
				}
			}
			Log.d(TAG, "Clipboard is empty or contains no text");
			return null;
		} catch (Exception e) {
			Log.e(TAG, "Error pasting from clipboard", e);
			throw new RuntimeException("Failed to paste from clipboard: " + e.getMessage(), e);
		}
	}
}