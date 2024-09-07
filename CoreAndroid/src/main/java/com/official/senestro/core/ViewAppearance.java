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
    private GradientDrawable originalDrawable; // Original drawable
    private GradientDrawable drawable; // Styling drawable

    public ViewAppearance(@NonNull Context context, @NonNull View view) {
        this.context = context;
        this.view = view;
        this.setDrawable();
        this.setMutableDrawable();
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
        view.setBackground(originalDrawable);
        this.setMutableDrawable();
    }

    // PRIVATE
    private void setDrawable() {
        Drawable background = view.getBackground();
        this.originalDrawable = background instanceof GradientDrawable ? (GradientDrawable) background : new GradientDrawable();
    }

    private void setMutableDrawable() {
        Drawable.ConstantState dcs = originalDrawable.getConstantState();
        this.drawable = dcs != null ? (GradientDrawable) dcs.newDrawable().mutate() : new GradientDrawable();
    }
}