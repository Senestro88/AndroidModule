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

public class NotificationUtils {
    private final String tag = NotificationUtils.class.getName();

    private final Context context;

    public NotificationUtils(@NonNull Context context) {
        this.context = context;
    }

    public String generateId(@NonNull String identifier) {
        return context.getPackageName() + "." + identifier;
    }

    public boolean channelExists(@NonNull String channelId) {
        return !supportsChannel() || manager().getNotificationChannel(channelId) != null;
    }

    public void createChannel(@NonNull String channelId, @NonNull String channelName, @NonNull String channelDesc, boolean enableLights, boolean enableVibration, boolean setBadge, boolean enableSound, boolean setBypassDnd, boolean showOnLockscreen) {
        if (supportsChannel() && getChannel(channelId, true) == null) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channelDesc);
            channel.enableLights(enableLights);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(enableVibration);
            if (enableVibration) {
                channel.setVibrationPattern(new long[]{0, 1000, 1000, 1000});
            } else {
                channel.setVibrationPattern(new long[]{0});
            }
            channel.setShowBadge(setBadge);
            channel.setBypassDnd(setBypassDnd);
            if (showOnLockscreen) {
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            } else {
                channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            }
            if (enableSound) {
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                channel.setSound(soundUri, null);
            } else {
                channel.setSound(null, null);
            }
            manager().createNotificationChannel(channel);
        }
    }

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
            if (pendingIntent != null) {
                builder.setContentIntent(pendingIntent);
            }
            return builder.build();
        } catch (Throwable e) {
            Log.e(tag, e.getMessage(), e);
            return null;
        }
    }

    public void show(@Nullable Notification notificationBuild, int notificationId) {
        if (notificationBuild != null) {
            try {
                manager().notify(notificationId, notificationBuild);
            } catch (Throwable e) {
                Log.e(tag, e.getMessage(), e);
            }
        }
    }

    public PendingIntent createIntent(@NonNull Class<?> aClass, @Nullable String action, @Nullable HashMap<String, String> extras, int pendingIntentFlag) {
        Intent intent = new Intent(context, aClass);
        if (action != null) {
            intent.setAction(action);
        }
        if (extras != null) {
            for (Map.Entry<String, String> entry : extras.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        return PendingIntent.getActivity(context, 0, intent, pendingIntentFlag);
    }

    public void cancel(int id) {
        NotificationManager manager = manager();
        manager.cancel(id);
    }

    // ================================================== //
    private NotificationManagerCompat compatManager() {
        return NotificationManagerCompat.from(context);
    }

    private NotificationManager manager() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private NotificationChannel getChannel(@NonNull String channelId, boolean useCompat) {
        if (useCompat) {
            compatManager().getNotificationChannel(channelId);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return manager().getNotificationChannel(channelId);
        }
        return null;
    }

    // Android 8+
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
    private boolean supportsChannel() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }
}