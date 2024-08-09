package com.official.senestro.core;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class ViewAppearance {
    private final Context context;
    private final View view;
    private GradientDrawable $drawable; // Original drawable
    private GradientDrawable drawable; // Styling drawable

    public ViewAppearance(@NonNull Context context, @NonNull View view) {
        this.context = context;
        this.view = view;
        this.set$Drawable();
        this.setDrawable();
        this.drawable.setShape(GradientDrawable.RECTANGLE);
    }

    public void setBorderStroke(int width, int color) {
        drawable.setStroke(width, color);
        view.setBackground(drawable);
    }

    public void setBorderRadius(int radius) {
        drawable.setCornerRadius(radius);
        view.setBackground(drawable);
    }

    public void setBackgroundColor(int color) {
        drawable.setColor(color);
        view.setBackground(drawable);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void setPadding(int left, int top, int right, int bottom) {
        drawable.setPadding(left, top, right, bottom);
        view.setBackground(drawable);
    }

    public void reset() {
        view.setBackground($drawable);
        this.setDrawable();
    }

    // PRIVATE
    private void set$Drawable() {
        Drawable background = view.getBackground();
        this.$drawable = background instanceof GradientDrawable ? (GradientDrawable) background : new GradientDrawable();
    }

    private void setDrawable() {
        Drawable.ConstantState $drawableConstantState = $drawable.getConstantState();
        this.drawable = $drawableConstantState != null ? (GradientDrawable) $drawableConstantState.newDrawable().mutate() : new GradientDrawable();
    }
}