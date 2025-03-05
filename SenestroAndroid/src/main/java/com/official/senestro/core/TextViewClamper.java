package com.official.senestro.core;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class TextViewClamper {
    private boolean isExpanded = false;
    private final TextView textView;
    private final int maxLines;
    private final boolean enableReadMore;

    public TextViewClamper(@NonNull TextView textView, int maxLines, boolean enableReadMore) {
        this.textView = textView;
        this.maxLines = maxLines;
        this.enableReadMore = enableReadMore;
        textView.setMaxLines(maxLines);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        ViewTreeObserver observer = textView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(() -> {
            int lines = textView.getLineCount();
            if (lines > maxLines) {
                String originalText = textView.getText().toString();
                String truncatedText = getTruncatedText(originalText, textView);
                if (enableReadMore) {
                    textView.setText(truncatedText);
                    textView.append(" ");
                    appendClickableText(textView, "Read More");
                    textView.setOnClickListener(v -> {
                        if (isExpanded) {
                            animateTextHeight(textView, textView.getHeight(), getTextViewHeight(textView, maxLines));
                            textView.setText(truncatedText);
                            textView.append(" ");
                            appendClickableText(textView, "Read More");
                        } else {
                            animateTextHeight(textView, textView.getHeight(), getTextViewHeight(textView, Integer.MAX_VALUE));
                            textView.setText(originalText);
                            textView.append(" ");
                            appendClickableText(textView, "Read Less");
                        }
                        isExpanded = !isExpanded;
                    });
                } else {
                    // Just truncate without Read More
                    textView.setText(truncatedText);
                }
                textView.setTag(originalText);
            }
        });
    }

    // PRIVATE
    @SuppressLint("SetTextI18n")
    private String getTruncatedText(@NonNull String text, @NonNull TextView textView) {
        textView.setText(text);
        textView.setMaxLines(maxLines);
        while (textView.getLineCount() > maxLines) {
            text = text.substring(0, text.length() - 1);
            textView.setText(text + "...");
        }
        return text + "...";
    }

    private void appendClickableText(@NonNull TextView textView, @NonNull String text) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new UnderlineSpan(), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.append(spannableString);
    }

    private void animateTextHeight(final @NonNull TextView textView, int startHeight, int endHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            textView.getLayoutParams().height = (int) animation.getAnimatedValue();
            textView.requestLayout();
        });
        animator.start();
    }

    private int getTextViewHeight(@NonNull TextView textView, int maxLines) {
        int lineHeight = textView.getLineHeight();
        return lineHeight * maxLines + textView.getPaddingTop() + textView.getPaddingBottom();
    }
}
