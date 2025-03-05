package com.official.senestro.core.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.ChecksSdkIntAtLeast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing notifications in Android.
 * <p>
 * This class includes methods for creating notification channels, building and displaying
 * notifications, managing notification IDs, and handling PendingIntents.
 */
public class NotificationUtils {

    private final String tag = NotificationUtils.class.getName();
    private final Context context;

    /**
     * Constructor for NotificationUtils.
     *
     * @param context The application context used for accessing system services.
     */
    public NotificationUtils(@NonNull Context context) {
        this.context = context;
    }

    /**
     * Generates a unique notification ID using the application package name.
     *
     * @param identifier A unique string identifier for the notification.
     * @return A unique notification ID string.
     */
    public String generateId(@NonNull String identifier) {
        return context.getPackageName() + "." + identifier;
    }

    /**
     * Checks if a notification channel with the specified ID exists.
     *
     * @param channelId The ID of the notification channel to check.
     * @return True if the channel exists, false otherwise.
     */
    public boolean channelExists(@NonNull String channelId) {
        return !supportsChannel() || AdvanceUtils.notNull(manager().getNotificationChannel(channelId));
    }

    /**
     * Creates a notification channel for Android O and above.
     *
     * @param channelId        The ID of the channel.
     * @param channelName      The name of the channel.
     * @param channelDesc      The description of the channel.
     * @param enableLights     Whether to enable lights.
     * @param enableVibration  Whether to enable vibration.
     * @param setBadge         Whether to allow badge display.
     * @param enableSound      Whether to enable sound.
     * @param setBypassDnd     Whether to bypass Do Not Disturb mode.
     * @param showOnLockscreen Whether to show notifications on the lock screen.
     */
    public void createChannel(@NonNull String channelId, @NonNull String channelName, @NonNull String channelDesc, boolean enableLights, boolean enableVibration, boolean setBadge, boolean enableSound, boolean setBypassDnd, boolean showOnLockscreen) {
        // Only supports creating channels on Android O and above.
        if (supportsChannel() && AdvanceUtils.isNull(getChannel(channelId, true))) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channelDesc);
            channel.enableLights(enableLights);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(enableVibration);
            channel.setVibrationPattern(enableVibration ? new long[]{0, 1000, 1000, 1000} : new long[]{0});
            channel.setShowBadge(setBadge);
            channel.setBypassDnd(setBypassDnd);
            channel.setLockscreenVisibility(showOnLockscreen ? Notification.VISIBILITY_PUBLIC : Notification.VISIBILITY_PRIVATE);
            channel.setSound(enableSound ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : null, null);
            manager().createNotificationChannel(channel);
        }
    }

    /**
     * Builds a notification using the specified parameters.
     *
     * @param channelId     The ID of the channel.
     * @param title         The notification title.
     * @param message       The notification content.
     * @param autoCancel    Whether the notification should dismiss when tapped.
     * @param showWhen      Whether to show the timestamp.
     * @param setOngoing    Whether the notification is ongoing.
     * @param color         The color for the notification.
     * @param icon          The icon for the notification.
     * @param setProgress   Whether to show progress.
     * @param maxProgress   The maximum progress value.
     * @param progress      The current progress value.
     * @param pendingIntent The intent triggered when the notification is tapped.
     * @return The built notification or null
     */
    public Notification build(@NonNull String channelId, @NonNull String title, @NonNull String message, boolean autoCancel, boolean showWhen, boolean setOngoing, int color, int icon, boolean setProgress, int maxProgress, int progress, @Nullable PendingIntent pendingIntent) {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
            builder.setContentTitle(title);
            builder.setContentText(message);
            builder.setAutoCancel(autoCancel);
            builder.setSmallIcon(icon);
            builder.setShowWhen(showWhen);
            builder.setOngoing(setOngoing);
            builder.setOnlyAlertOnce(true);
            builder.setColor(color);
            builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            if (setProgress) {
                builder.setProgress(maxProgress, progress, false);
            }
            if (AdvanceUtils.notNull(pendingIntent)) {
                builder.setContentIntent(pendingIntent);
            }
            return builder.build();
        } catch (Throwable e) {
            Log.e(tag, "Error building notification", e);
            return null;
        }
    }

    /**
     * Displays a notification with a given ID.
     *
     * @param notificationBuild The built notification.
     * @param notificationId    The ID of the notification.
     */
    public void show(@Nullable Notification notificationBuild, int notificationId) {
        if (AdvanceUtils.notNull(notificationBuild)) {
            try {
                NotificationManager manager = manager();
                manager.notify(notificationId, notificationBuild);
            } catch (Throwable e) {
                Log.e(tag, "Error displaying notification", e);
            }
        }
    }

    /**
     * Creates a PendingIntent for a given action.
     *
     * @param aClass            The class to launch.
     * @param action            The action for the intent.
     * @param extras            Additional data for the intent.
     * @param pendingIntentFlag PendingIntent flags.
     * @return The created PendingIntent.
     */
    public PendingIntent createIntent(@NonNull Class<?> aClass, @Nullable String action, @Nullable HashMap<String, String> extras, int pendingIntentFlag) {
        Intent intent = new Intent(context, aClass);
        if (AdvanceUtils.notNull(action)) {
            intent.setAction(action);
        }
        if (AdvanceUtils.notNull(extras)) {
            for (Map.Entry<String, String> entry : extras.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        return PendingIntent.getActivity(context, 0, intent, pendingIntentFlag);
    }

    /**
     * Creates a PendingIntent for the given Intent.
     *
     * @param intent            The Intent to be triggered by the PendingIntent.
     * @param pendingIntentFlag Flags that control the behavior of the PendingIntent.
     *                          Examples include {@link PendingIntent#FLAG_UPDATE_CURRENT}.
     * @return The created PendingIntent, ready to be used with a notification or other components.
     */
    public PendingIntent createIntent(@NonNull Intent intent, int pendingIntentFlag) {
        // Creates a PendingIntent that wraps the given Intent. The context is used to resolve the activity.
        return PendingIntent.getActivity(context, 0, intent, pendingIntentFlag);
    }

    /**
     * Cancels a notification with a given ID.
     *
     * @param id The notification ID.
     */
    public void cancel(int id) {
        manager().cancel(id);
    }

    // ================================================== //
    // Private Helper Methods
    // ================================================== //

    /**
     * Returns the NotificationManagerCompat instance.
     *
     * @return the NotificationManagerCompat instance.
     */
    private NotificationManagerCompat compatManager() {
        return NotificationManagerCompat.from(context);
    }

    /**
     * Returns the NotificationManager instance.
     *
     * @return the NotificationManager instance.
     */
    private NotificationManager manager() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Retrieves the NotificationChannel by ID.
     *
     * @param channelId the ID of the notification channel.
     * @return the NotificationChannel, or null if not found.
     */
    private NotificationChannel getChannel(@NonNull String channelId, boolean useCompat) {
        return supportsChannel() ? manager().getNotificationChannel(channelId) : null;
    }

    /**
     * Checks if the device supports notification channels (Android 8.0 and above).
     *
     * @return true if notification channels are supported, false otherwise.
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
    private boolean supportsChannel() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }
}
