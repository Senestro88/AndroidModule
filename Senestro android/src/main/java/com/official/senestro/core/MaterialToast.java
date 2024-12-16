package com.official.senestro.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

public class MaterialToast {
    private final Context context;
    private final Typeface MAT_LOADED_TOAST_TYPEFACE;
    private Typeface matTypeface;
    private int matTextSize;
    private boolean matTintIcon;
    private boolean matAllowQueue;
    private int matToastGravity;
    private int matXOffset;
    private int matYOffset;
    private boolean matSupportDarkTheme;
    private boolean matIsRTL;
    private Toast matLastToast;
    public final static int LENGTH_SHORT = 0;
    public final static int LENGTH_LONG = 1;

    public MaterialToast(Context context) {
        this.context = context;
        this.MAT_LOADED_TOAST_TYPEFACE = getTypeface(R.font.cambria);
        this.resetConfig();
    }

    @CheckResult
    public Toast normalToast(@StringRes int message) {
        return normalToast(context.getString(message), 0, null, false);
    }

    @CheckResult
    public Toast normalToast(@NonNull CharSequence message) {
        return normalToast(message, 0, null, false);
    }

    @CheckResult
    public Toast normalToast(@StringRes int message, Drawable icon) {
        return normalToast(context.getString(message), 0, icon, true);
    }

    @CheckResult
    public Toast normalToast(@NonNull CharSequence message, Drawable icon) {
        return normalToast(message, 0, icon, true);
    }

    @CheckResult
    public Toast normalToast(@StringRes int message, int length) {
        return normalToast(context.getString(message), length, null, false);
    }

    @CheckResult
    public Toast normalToast(@NonNull CharSequence message, int length) {
        return normalToast(message, length, null, false);
    }

    @CheckResult
    public Toast normalToast(@StringRes int message, int length, Drawable icon) {
        return normalToast(context.getString(message), length, icon, true);
    }

    @CheckResult
    public Toast normalToast(@NonNull CharSequence message, int length, Drawable icon) {
        return normalToast(message, length, icon, true);
    }

    @CheckResult
    public Toast normalToast(@StringRes int message, int length, Drawable icon, boolean withIcon) {
        return newToastWithNormalDarkThemeSupport(context.getString(message), icon, length, withIcon);
    }

    @CheckResult
    public Toast normalToast(@NonNull CharSequence message, int length, Drawable icon, boolean withIcon) {
        return newToastWithNormalDarkThemeSupport(message, icon, length, withIcon);
    }

    @CheckResult
    public Toast warningToast(@StringRes int message) {
        return warningToast(context.getString(message), 0, true);
    }

    @CheckResult
    public Toast warningToast(@NonNull CharSequence message) {
        return warningToast(message, 0, true);
    }

    @CheckResult
    public Toast warningToast(@StringRes int message, int length) {
        return warningToast(context.getString(message), length, true);
    }

    @CheckResult
    public Toast warningToast(@NonNull CharSequence message, int length) {
        return warningToast(message, length, true);
    }

    @CheckResult
    public Toast warningToast(@StringRes int message, int length, boolean withIcon) {
        return createCustom(context.getString(message), getDrawable(R.mipmap.material_toast_ic_error_outline_white), getColor(R.color.materialToastWarningColor), getColor(R.color.materialToastDefaultTextColor), length, withIcon, true);
    }

    @CheckResult
    public Toast warningToast(@NonNull CharSequence message, int length, boolean withIcon) {
        return createCustom(message, getDrawable(R.mipmap.material_toast_ic_error_outline_white), getColor(R.color.materialToastWarningColor), getColor(R.color.materialToastDefaultTextColor), length, withIcon, true);
    }

    @CheckResult
    public Toast infoToast(@StringRes int message) {
        return infoToast(context.getString(message), 0, true);
    }

    @CheckResult
    public Toast infoToast(@NonNull CharSequence message) {
        return infoToast(message, 0, true);
    }

    @CheckResult
    public Toast infoToast(@StringRes int message, int length) {
        return infoToast(context.getString(message), length, true);
    }

    @CheckResult
    public Toast infoToast(@NonNull CharSequence message, int length) {
        return infoToast(message, length, true);
    }

    @CheckResult
    public Toast infoToast(@StringRes int message, int length, boolean withIcon) {
        return createCustom(context.getString(message), getDrawable(R.mipmap.material_toast_ic_info_outline_white), getColor(R.color.materialToastInfoColor), getColor(R.color.materialToastDefaultTextColor), length, withIcon, true);
    }

    @CheckResult
    public Toast infoToast(@NonNull CharSequence message, int length, boolean withIcon) {
        return createCustom(message, getDrawable(R.mipmap.material_toast_ic_info_outline_white), getColor(R.color.materialToastInfoColor), getColor(R.color.materialToastDefaultTextColor), length, withIcon, true);
    }

    @CheckResult
    public Toast successToast(@StringRes int message) {
        return successToast(context.getString(message), 0, true);
    }

    @CheckResult
    public Toast successToast(@NonNull CharSequence message) {
        return successToast(message, 0, true);
    }

    @CheckResult
    public Toast successToast(@StringRes int message, int length) {
        return successToast(context.getString(message), length, true);
    }

    @CheckResult
    public Toast successToast(@NonNull CharSequence message, int length) {
        return successToast(message, length, true);
    }

    @CheckResult
    public Toast successToast(@StringRes int message, int length, boolean withIcon) {
        return createCustom(context.getString(message), getDrawable(R.mipmap.material_toast_ic_check_white), getColor(R.color.materialToastSuccessColor), getColor(R.color.materialToastDefaultTextColor), length, withIcon, true);
    }

    @CheckResult
    public Toast successToast(@NonNull CharSequence message, int length, boolean withIcon) {
        return createCustom(message, getDrawable(R.mipmap.material_toast_ic_check_white), getColor(R.color.materialToastSuccessColor), getColor(R.color.materialToastDefaultTextColor), length, withIcon, true);
    }

    @CheckResult
    public Toast errorToast(@StringRes int message) {
        return errorToast(context.getString(message), 0, true);
    }

    @CheckResult
    public Toast errorToast(@NonNull CharSequence message) {
        return errorToast(message, 0, true);
    }

    @CheckResult
    public Toast errorToast(@StringRes int message, int length) {
        return errorToast(context.getString(message), length, true);
    }

    @CheckResult
    public Toast errorToast(@NonNull CharSequence message, int length) {
        return errorToast(message, length, true);
    }

    @CheckResult
    public Toast errorToast(@StringRes int message, int length, boolean withIcon) {
        return createCustom(context.getString(message), getDrawable(R.mipmap.material_toast_ic_clear_white), getColor(R.color.materialToastErrorColor), getColor(R.color.materialToastDefaultTextColor), length, withIcon, true);
    }

    @CheckResult
    public Toast errorToast(@NonNull CharSequence message, int length, boolean withIcon) {
        return createCustom(message, getDrawable(R.mipmap.material_toast_ic_clear_white), getColor(R.color.materialToastErrorColor), getColor(R.color.materialToastDefaultTextColor), length, withIcon, true);
    }

    @CheckResult
    public Toast createCustom(@StringRes int message, Drawable icon, int length, boolean withIcon) {
        return createCustom(context.getString(message), icon, -1, getColor(R.color.materialToastDefaultTextColor), length, withIcon, false);
    }

    @CheckResult
    public Toast createCustom(@NonNull CharSequence message, Drawable icon, int length, boolean withIcon) {
        return createCustom(message, icon, -1, getColor(R.color.materialToastDefaultTextColor), length, withIcon, false);
    }

    @CheckResult
    public Toast createCustom(@StringRes int message, @DrawableRes int iconRes, @ColorRes int tintColorRes, int length, boolean withIcon, boolean shouldTint) {
        return createCustom(context.getString(message), getDrawable(iconRes), getColor(tintColorRes), getColor(R.color.materialToastDefaultTextColor), length, withIcon, shouldTint);
    }

    @CheckResult
    public Toast createCustom(@NonNull CharSequence message, @DrawableRes int iconRes, @ColorRes int tintColorRes, int length, boolean withIcon, boolean shouldTint) {
        return createCustom(message, getDrawable(iconRes), getColor(tintColorRes), getColor(R.color.materialToastDefaultTextColor), length, withIcon, shouldTint);
    }

    @CheckResult
    public Toast createCustom(@StringRes int message, Drawable icon, @ColorRes int tintColorRes, int length, boolean withIcon, boolean shouldTint) {
        return createCustom(context.getString(message), icon, getColor(tintColorRes), getColor(R.color.materialToastDefaultTextColor), length, withIcon, shouldTint);
    }

    @CheckResult
    public Toast createCustom(@StringRes int message, Drawable icon, @ColorRes int tintColorRes, @ColorRes int textColorRes, int length, boolean withIcon, boolean shouldTint) {
        return createCustom(context.getString(message), icon, getColor(tintColorRes), getColor(textColorRes), length, withIcon, shouldTint);
    }

    @SuppressLint({"ShowToast", "InflateParams"})
    @CheckResult
    public Toast createCustom(@NonNull CharSequence message, Drawable icon, @ColorInt int tintColor, @ColorInt int textColor, int length, boolean withIcon, boolean shouldTint) {
        Toast currentToast = Toast.makeText(context, "", length);
        LayoutInflater inflater = LayoutInflater.from(context);
        View toastLayout = inflater.inflate(R.layout.material_toast_layout, null);
        ConstraintLayout toastRoot = toastLayout.findViewById(R.id.material_toast_root);
        ImageView toastIcon = toastLayout.findViewById(R.id.material_toast_icon);
        TextView toastTextView = toastLayout.findViewById(R.id.material_toast_text);
        setToastBackground(toastLayout, shouldTint, tintColor);
        setToastIcon(toastRoot, toastIcon, icon, textColor, withIcon);
        toastTextView.setText(message);
        toastTextView.setTextColor(textColor);
        toastTextView.setTypeface(matTypeface);
        toastTextView.setTextSize(2, (float) matTextSize);
        currentToast.setView(toastLayout);
        setToastQueue(currentToast);
        int gravity = matToastGravity == -1 ? currentToast.getGravity() : matToastGravity;
        int xOffset = matXOffset == -1 ? currentToast.getXOffset() : matXOffset;
        int yOffset = matYOffset == -1 ? currentToast.getYOffset() : matYOffset;
        currentToast.setGravity(gravity, xOffset, yOffset);
        return currentToast;
    }

    public void setConfig(@NonNull Typeface typeface, int textSize, boolean tintIcon, boolean allowQueue, int toastGravity, int xOffset, int yOffset, boolean supportDarkTheme, boolean isRTL) {
        this.matTypeface = typeface;
        this.matTextSize = textSize;
        this.matTintIcon = tintIcon;
        this.matAllowQueue = allowQueue;
        this.matToastGravity = toastGravity;
        this.matXOffset = xOffset;
        this.matYOffset = yOffset;
        this.matSupportDarkTheme = supportDarkTheme;
        this.matIsRTL = isRTL;
    }

    public void setConfig(@NonNull Typeface typeface, int textSize, boolean tintIcon, boolean allowQueue, int toastGravity, boolean supportDarkTheme, boolean isRTL) {
        this.matTypeface = typeface;
        this.matTextSize = textSize;
        this.matTintIcon = tintIcon;
        this.matAllowQueue = allowQueue;
        this.matToastGravity = toastGravity;
        this.matSupportDarkTheme = supportDarkTheme;
        this.matIsRTL = isRTL;
    }

    public void resetConfig() {
        this.matTypeface = MAT_LOADED_TOAST_TYPEFACE;
        this.matTextSize = 16;
        this.matTintIcon = true;
        this.matAllowQueue = true;
        this.matToastGravity = -1;
        this.matXOffset = -1;
        this.matYOffset = -1;
        this.matSupportDarkTheme = true;
        this.matIsRTL = false;
    }

    // PRIVATE METHODS
    private Toast newToastWithNormalDarkThemeSupport(@NonNull CharSequence message, Drawable icon, int length, boolean withIcon) {
        if (matSupportDarkTheme && Build.VERSION.SDK_INT >= 29) {
            int uiMode = context.getResources().getConfiguration().uiMode & 48;
            return uiMode == 16 ? newToastWithLightTheme(message, icon, length, withIcon) : newToastWithDarkTheme(message, icon, length, withIcon);
        } else {
            return Build.VERSION.SDK_INT >= 27 ? newToastWithLightTheme(message, icon, length, withIcon) : newToastWithDarkTheme(message, icon, length, withIcon);
        }
    }

    private Toast newToastWithLightTheme(@NonNull CharSequence message, Drawable icon, int length, boolean withIcon) {
        return createCustom(message, icon, getColor(R.color.materialToastDefaultTextColor), getColor(R.color.materialToastNormalColor), length, withIcon, true);
    }

    private Toast newToastWithDarkTheme(@NonNull CharSequence message, Drawable icon, int length, boolean withIcon) {
        return createCustom(message, icon, getColor(R.color.materialToastNormalColor), getColor(R.color.materialToastDefaultTextColor), length, withIcon, true);
    }

    @SuppressLint("ObsoleteSdkInt")
    private void setToastDirection(@NonNull ConstraintLayout toastRoot) {
        if (matIsRTL && Build.VERSION.SDK_INT >= 17) {
            toastRoot.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    private void setToastQueue(@NonNull Toast currentToast) {
        if (!matAllowQueue) {
            if (matLastToast != null) {
                matLastToast.cancel();
            }
            matLastToast = currentToast;
        }
    }

    private void setToastIcon(ConstraintLayout toastRoot, ImageView toastIcon, Drawable icon, @ColorInt int textColor, boolean withIcon) {
        if (withIcon && icon != null) {
            toastIcon.setVisibility(View.VISIBLE);
            setToastDirection(toastRoot);
            toastIcon.setBackground(icon);
        }
    }

    private void setToastBackground(View toastLayout, boolean shouldTint, @ColorInt int tintColor) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(shouldTint ? tintColor : getColor(R.color.materialToastDefaultBackground));
        gradientDrawable.setCornerRadius(10);
        toastLayout.setBackground(gradientDrawable);
    }

    private Typeface getTypeface(int fontResId) {
        return ResourcesCompat.getFont(context, fontResId);
    }

    private int getColor(@ColorRes int color) {
        return ContextCompat.getColor(context, color);
    }

    private Drawable getDrawable(@DrawableRes int id) {
        return AppCompatResources.getDrawable(context, id);
    }
}