package com.official.senestro.core;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class AdvanceProgressDialog {
    private AlertDialog alertDialog;
    private View view;
    private TextView title;
    private ProgressBar spinnerProgressBar;
    private ProgressBar horizontalProgressBar;
    private TextView horizontalProgressBarPercentage;
    private TextView message;
    private boolean isDeterminate = false;

    public AdvanceProgressDialog(@NonNull Activity activity) {
        setAlertDialog(activity);
        setViews();
        setDeterminate(isDeterminate);
    }

    public void setTitle(@NonNull String title) {
        this.title.setText(title);
    }

    public void setMessage(@NonNull String message) {
        this.message.setText(message);
    }

    public void show() {
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    public void dismiss() {
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    public boolean isShowing() {
        return alertDialog.isShowing();
    }

    public void setDeterminate(boolean determinate) {
        isDeterminate = determinate;
        if (isDeterminate) {
            showHorizontalProgressBar();
        } else {
            hideHorizontalProgressBar();
        }
    }

    public void setProgress(int progress) {
        if (isDeterminate) {
            horizontalProgressBar.setProgress(progress);
            horizontalProgressBarPercentage.setText(progress + "%");
        }
    }

    // PRIVATE
    private void setAlertDialog(@NonNull Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Custom_AlertDialog);
        view = activity.getLayoutInflater().inflate(R.layout.advance_progress_dialog, null);
        builder.setView(view);
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
    }

    private void setViews() {
        // Initialize views
        title = view.findViewById(R.id.advance_progress_dialog_title);
        message = view.findViewById(R.id.advance_progress_dialog_message);
        spinnerProgressBar = view.findViewById(R.id.advance_progress_dialog_spinner_progress_bar);
        horizontalProgressBar = view.findViewById(R.id.advance_progress_dialog_horizontal_progress_bar);
        horizontalProgressBarPercentage = view.findViewById(R.id.advance_progress_dialog_horizontal_percentage);
    }

    private void hideHorizontalProgressBar() {
        // Show spinner style
        spinnerProgressBar.setVisibility(View.VISIBLE);
        // Hide horizontal style
        horizontalProgressBar.setVisibility(View.GONE);
        horizontalProgressBarPercentage.setVisibility(View.GONE);
        horizontalProgressBar.setProgress(0);
        horizontalProgressBarPercentage.setText("");
    }

    private void showHorizontalProgressBar() {
        // Hide spinner style
        spinnerProgressBar.setVisibility(View.GONE);
        // Show horizontal style
        horizontalProgressBar.setVisibility(View.VISIBLE);
        horizontalProgressBarPercentage.setVisibility(View.VISIBLE);
    }
}
