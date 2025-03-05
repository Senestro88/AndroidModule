package com.official.senestro.core.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;

/**
 * Utility class for managing the soft keyboard (input method) for views and windows.
 */
public class KeyboardUtils {

    private final Context context;
    private final InputMethodManager imm;

    /**
     * Constructor to initialize the KeyboardUtils with the given context.
     *
     * @param context The application or activity context.
     */
    public KeyboardUtils(@NonNull Context context) {
        this.context = context;
        this.imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /**
     * Hides the soft keyboard for the given view if it is active.
     *
     * @param view The view for which the keyboard should be hidden.
     */
    public void hideViewKeyboard(@NonNull View view) {
        if (isKeyboardActiveForView(view)) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard for the given view if it is not already active.
     *
     * @param view The view for which the keyboard should be shown.
     */
    public void showViewKeyboard(@NonNull View view) {
        if (isImmAvailable() && !isKeyboardActiveForView(view)) {
            imm.showSoftInput(view, 0);
        }
    }

    /**
     * Checks if the soft keyboard is currently active for the entire window.
     *
     * @return true if the keyboard is active for the window, false otherwise.
     */
    public boolean isWindowKeyboardActive() {
        return isImmAvailable() && imm.isAcceptingText();
    }

    /**
     * Hides the soft keyboard for the current window if it is active.
     */
    public void hideViewKeyboard() {
        View view = getCurrentFocusOrDecorView();
        if (isImmAvailable() && AdvanceUtils.notNull(view)) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard for the current window if it is not already active.
     */
    public void showWindowKeyboard() {
        if (!isWindowKeyboardActive()) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    /**
     * Hides the soft keyboard for the entire window.
     */
    @SuppressLint("WrongConstant")
    public void hideWindowKeyboard() {
        if (isWindowKeyboardActive()) {
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }

    // PRIVATE

    /**
     * Retrieves the current focused view or the window's decor view if no view is focused.
     *
     * @return The current focused view or the window's decor view.
     */
    private View getCurrentFocusOrDecorView() {
        if (context instanceof Activity) {
            View currentFocus = ((Activity) context).getCurrentFocus();
            return AdvanceUtils.notNull(currentFocus) ? currentFocus : ((Activity) context).getWindow().getDecorView();
        }
        return null;
    }

    /**
     * Checks if the InputMethodManager is available.
     *
     * @return true if InputMethodManager is available, false otherwise.
     */
    private boolean isImmAvailable() {
        return AdvanceUtils.notNull(imm);
    }

    /**
     * Checks if the soft keyboard is currently active for the given view.
     *
     * @param view The view to check if the keyboard is active on.
     * @return true if the keyboard is active for the view, false otherwise.
     */
    private boolean isKeyboardActiveForView(@NonNull View view) {
        return isImmAvailable() && imm.isActive(view);
    }
}
