package com.sk.fcp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardManagerHelper {
	public static void copyToClipboard(String textToCopy, Context context) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("fileContent", textToCopy);
		clipboard.setPrimaryClip(clip);
	}

	public static String pasteFromClipboard(Context context) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clipData = clipboard.getPrimaryClip();
		if (clipData != null && clipData.getItemCount() > 0) {
			CharSequence text = clipData.getItemAt(0).getText();
			if (text != null) {
				return text.toString();
			}
		}
		return null;
	}
}