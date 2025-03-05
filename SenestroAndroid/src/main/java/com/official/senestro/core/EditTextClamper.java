package com.official.senestro.core;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import androidx.annotation.NonNull;

public class EditTextClamper {
    private boolean isExpanded = false;
    private final EditText editText;
    private final int maxLines;
    private final boolean enableExpand;

    public EditTextClamper(@NonNull EditText editText, int maxLines, boolean enableExpand) {
        this.editText = editText;
        this.maxLines = maxLines;
        this.enableExpand = enableExpand;
        editText.setMaxLines(maxLines);
        editText.setEllipsize(TextUtils.TruncateAt.END);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setSingleLine(false);
        ViewTreeObserver observer = editText.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(() -> {
            int lineCount = editText.getLineCount();
            if (lineCount > maxLines) {
                String originalText = editText.getText().toString();
                String truncatedText = getTruncatedText(originalText, editText);
                if (enableExpand) {
                    editText.setText(truncatedText);
                    editText.setOnClickListener(v -> {
                        if (isExpanded) {
                            animateTextHeight(editText, editText.getHeight(), getTextViewHeight(editText, maxLines));
                            editText.setText(truncatedText);
                        } else {
                            animateTextHeight(editText, editText.getHeight(), getTextViewHeight(editText, Integer.MAX_VALUE));
                            editText.setText(originalText);
                        }
                        isExpanded = !isExpanded;
                    });
                } else {
                    editText.setText(truncatedText); // Just truncate without expand/collapse
                }
                editText.setTag(originalText);
            }
        });
    }

    // PRIVATE
    @SuppressLint("SetTextI18n")
    private String getTruncatedText(@NonNull String text, @NonNull EditText editText) {
        editText.setText(text);
        editText.setMaxLines(maxLines);
        while (editText.getLineCount() > maxLines) {
            text = text.substring(0, text.length() - 1);
            editText.setText(text + "...");
        }
        return text + "...";
    }

    private void animateTextHeight(final @NonNull EditText editText, int startHeight, int endHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            editText.getLayoutParams().height = (int) animation.getAnimatedValue();
            editText.requestLayout();
        });
        animator.start();
    }

    private int getTextViewHeight(@NonNull EditText editText, int maxLines) {
        int lineHeight = editText.getLineHeight();
        return lineHeight * maxLines + editText.getPaddingTop() + editText.getPaddingBottom();
    }
}
