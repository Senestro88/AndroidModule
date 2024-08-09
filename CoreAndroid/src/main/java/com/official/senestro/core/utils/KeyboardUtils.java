package com.official.senestro.core.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

public class KeyboardUtils {

    private final Context context;
    private final InputMethodManager imm;

    public KeyboardUtils(@NonNull Context context) {
        this.context = context;
        this.imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public boolean isViewKeyboardActive(@NonNull View view) {
        return imm != null && imm.isActive(view);
    }

    public void hideViewKeyboard(@NonNull View view) {
        if (imm != null && imm.isActive(view)) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showViewKeyboard(@NonNull View view) {
        if (imm != null && !imm.isActive(view)) {
            imm.showSoftInput(view, 0);
        }
    }

    public boolean isWindowKeyboardActive() {
        return imm != null && imm.isAcceptingText();
    }

    public void hideWindowKeyboard() {
        View view = getCurrentFocusOrDecorView();
        if (view != null && imm != null && imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showWindowKeyboard() {
        if (imm != null && !imm.isAcceptingText()) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private View getCurrentFocusOrDecorView() {
        if (context instanceof Activity) {
            View currentFocus = ((Activity) context).getCurrentFocus();
            return currentFocus != null ? currentFocus : ((Activity) context).getWindow().getDecorView();
        }
        return null;
    }
}