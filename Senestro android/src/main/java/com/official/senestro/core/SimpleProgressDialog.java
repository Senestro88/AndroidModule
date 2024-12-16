package com.official.senestro.core;

import android.app.ProgressDialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.official.senestro.core.utils.XUtils;

public class SimpleProgressDialog {

    public static ProgressDialog create(@NonNull Context context, @Nullable String title, @Nullable String message, int max, boolean cancelable, boolean styleHorizontal) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        if (XUtils.notNull(title)) {
            progressDialog.setTitle(title);
        }
        if (XUtils.notNull(message)) {
            progressDialog.setMessage(message);
        }
        progressDialog.setMax(max);
        progressDialog.setCancelable(cancelable);
        progressDialog.setCanceledOnTouchOutside(cancelable);
        progressDialog.setProgressStyle(styleHorizontal ? ProgressDialog.STYLE_HORIZONTAL : ProgressDialog.STYLE_SPINNER);
        return progressDialog;
    }
}
