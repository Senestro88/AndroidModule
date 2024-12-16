package com.official.senestro.core.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
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
 * Utility class for managing notifications in Android. It supports creating notification channels,
 * building and displaying notifications, and creating PendingIntents for notifications.
 */
public class NotificationUtils {

    private final String tag = NotificationUtils.class.getName();
    private final Context context;

    /**
     * Constructor for NotificationUtils.
     *
     * @param context the application context.
     */
    public NotificationUtils(@NonNull Context context) {
        this.context = context;
    }

    /**
     * Generates a unique ID for the notification based on the application package name and the provided identifier.
     *
     * @param identifier a custom identifier for the notification.
     * @return the generated notification ID.
     */
    public String generateId(@NonNull String identifier) {
        return context.getPackageName() + "." + identifier;
    }

    /**
     * Checks if a notification channel exists.
     *
     * @param channelId the ID of the notification channel.
     * @return true if the channel exists, false otherwise.
     */
    public boolean channelExists(@NonNull String channelId) {
        NotificationManager manager = manager();
        return !supportsChannel() || manager.getNotificationChannel(channelId) != null;
    }

    /**
     * Creates a new notification channel with specified properties. This method is only applicable for Android 8.0 and above.
     *
     * @param channelId        the ID for the notification channel.
     * @param channelName      the name of the channel.
     * @param channelDesc      a description of the channel.
     * @param enableLights     whether to enable lights for the channel.
     * @param enableVibration  whether to enable vibration for the channel.
     * @param setBadge         whether to set a badge for the channel.
     * @param enableSound      whether to enable sound for the channel.
     * @param setBypassDnd     whether to allow the channel to bypass Do Not Disturb mode.
     * @param showOnLockscreen whether the notification should be visible on the lock screen.
     */
    public void createChannel(@NonNull String channelId, @NonNull String channelName, @NonNull String channelDesc, boolean enableLights, boolean enableVibration, boolean setBadge, boolean enableSound, boolean setBypassDnd, boolean showOnLockscreen) {
        // Only supports creating channels on Android O and above.
        if (supportsChannel() && getChannel(channelId, true) == null) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channelDesc);
            channel.enableLights(enableLights);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(enableVibration);
            // Define vibration pattern
            if (enableVibration) {
                channel.setVibrationPattern(new long[]{0, 1000, 1000, 1000});
            } else {
                channel.setVibrationPattern(new long[]{0});
            }
            channel.setShowBadge(setBadge);
            channel.setBypassDnd(setBypassDnd);
            channel.setLockscreenVisibility(showOnLockscreen ? Notification.VISIBILITY_PUBLIC : Notification.VISIBILITY_PRIVATE);
            if (enableSound) {
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                channel.setSound(soundUri, null);
            } else {
                channel.setSound(null, null);
            }
            manager().createNotificationChannel(channel);
        }
    }

    /**
     * Builds a notification with the given parameters.
     *
     * @param channelId     the ID of the notification channel.
     * @param title         the title of the notification.
     * @param message       the message content of the notification.
     * @param autoCancel    whether the notification should auto-cancel when clicked.
     * @param showWhen      whether to show the time the notification was posted.
     * @param setOngoing    whether the notification should be ongoing.
     * @param color         the color of the notification.
     * @param icon          the resource ID of the notification icon.
     * @param setProgress   whether to show progress in the notification.
     * @param maxProgress   the maximum progress value.
     * @param progress      the current progress value.
     * @param pendingIntent the PendingIntent to trigger when the notification is clicked.
     * @return the built notification.
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
            if (pendingIntent instanceof PendingIntent) {
                builder.setContentIntent(pendingIntent);
            }
            return builder.build();
        } catch (Throwable e) {
            Log.e(tag, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Displays a notification with the specified ID.
     *
     * @param notificationBuild the notification to be displayed.
     * @param notificationId    the ID of the notification.
     */
    public void show(@Nullable Notification notificationBuild, int notificationId) {
        if (notificationBuild instanceof Notification) {
            try {
                NotificationManager manager = manager();
                manager.notify(notificationId, notificationBuild);
            } catch (Throwable e) {
                Log.e(tag, e.getMessage(), e);
            }
        }
    }

    /**
     * Creates a PendingIntent that can be triggered by a notification.
     *
     * @param aClass            the class to be triggered by the PendingIntent.
     * @param action            an optional action to be set for the intent.
     * @param extras            optional extras to be added to the intent.
     * @param pendingIntentFlag flags for the PendingIntent.
     * @return the created PendingIntent.
     */
    public PendingIntent createIntent(@NonNull Class<?> aClass, @Nullable String action, @Nullable HashMap<String, String> extras, int pendingIntentFlag) {
        Intent intent = new Intent(context, aClass);
        if (action instanceof String) {
            intent.setAction(action);
        }
        if (extras instanceof HashMap) {
            for (Map.Entry<String, String> entry : extras.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        return PendingIntent.getActivity(context, 0, intent, pendingIntentFlag);
    }

    /**
     * Cancels a notification with the specified ID.
     *
     * @param id the ID of the notification to cancel.
     */
    public void cancel(int id) {
        NotificationManager manager = manager();
        manager.cancel(id);
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
     * @param useCompat whether to use the compatibility version of the manager.
     * @return the NotificationChannel, or null if not found.
     */
    private NotificationChannel getChannel(@NonNull String channelId, boolean useCompat) {
        if (useCompat) {
            compatManager().getNotificationChannel(channelId);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return manager().getNotificationChannel(channelId);
        }
        return null;
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
