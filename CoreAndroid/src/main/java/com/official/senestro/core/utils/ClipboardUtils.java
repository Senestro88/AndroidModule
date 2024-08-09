package com.official.senestro.core.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class ClipboardUtils {
    private final Context context;

    public ClipboardUtils(@NonNull Context context) {
        this.context = context;
    }

    public void copy(@NonNull String label, @NonNull String text) {
        // Get the clipboard manager
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            // Create ClipData object to store the copied text
            ClipData data = ClipData.newPlainText(label, text);
            // Set the ClipData to the clipboard
            manager.setPrimaryClip(data);
        }
    }

    public void paste(@NonNull EditText editText) {
        // Get the clipboard manager
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null && manager.hasPrimaryClip()) {
            ClipData data = manager.getPrimaryClip();
            if (data != null) {
                ClipData.Item item = data.getItemAt(0);
                if (item != null) {
                    CharSequence sequence = manager.getPrimaryClip().getItemAt(0).getText();
                    editText.setText(sequence);
                }
            }
        }
    }
}