package com.official.senestro.core;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class Dialog extends AlertDialog {
    private final Context context;

    public Dialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public Dialog(@NonNull Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    protected Dialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    // PUBLIC CLASS
    public static class Builder extends AlertDialog.Builder {
        private final Context context;
        private String title;
        private String message;

        public Builder(@NonNull Context context) {
            super(context);
            this.context = context;
        }

        public Builder(@NonNull Context context, int themeResId) {
            super(context, themeResId);
            this.context = context;
        }
    }
}