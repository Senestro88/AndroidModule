package com.official.senestro.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

public class Receivers {
    private final @NonNull Context context;

    public Receivers(@NonNull Context context) {
        this.context = context;
    }

    public static void postOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    // ================================================= //
    public static class PhoneState {
        private final Context context;
        private static boolean isRegistered = false;
        private static Receiver receiver;

        public PhoneState(@NonNull Context context) {
            this.context = context;
        }

        public boolean isRegistered() {
            return isRegistered;
        }

        public void register(@NonNull Callback callback) {
            if (!isRegistered()) {
                receiver = new Receiver(callback);
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
                context.registerReceiver(receiver, intentFilter);
                isRegistered = true;
            }
        }

        public void unRegister() {
            if (isRegistered()) {
                context.unregisterReceiver(receiver);
                isRegistered = false;
            }
        }

        public interface Callback {
            void onRinging(String number);

            void onOffHook();

            void onIdle();
        }

        public static class SimpleCallback implements Callback {

            @Override
            public void onRinging(String number) {

            }

            @Override
            public void onOffHook() {

            }

            @Override
            public void onIdle() {

            }
        }

        public static class Receiver extends BroadcastReceiver {

            private Callback callback = null;

            public Receiver() {
            }

            public Receiver(Callback callback) {
                this.callback = callback;
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                if (callback != null) {
                    String action = intent.getAction();
                    if (action != null) {
                        if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                            String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                            if (phoneState != null && phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                                // Phone is ringing
                                // Do something when the phone is ringing
                                String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                                postOnMainThread(() -> callback.onRinging(number));
                            } else if (phoneState != null && phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                                // Phone is idle (not in a call)
                                // Do something when the phone is idle
                                postOnMainThread(() -> callback.onIdle());
                            } else if (phoneState != null && phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                                // Phone is off the hook (in a call)
                                // Do something when the phone is off the hook
                                postOnMainThread(() -> callback.onOffHook());
                            }
                        }
                    }
                }
            }
        }
    }

    // ================================================= //
    public static class Network {
        private final Context context;
        private static boolean isRegistered = false;
        private Receiver receiver;
        private NetworkCallback networkCallback;

        public Network(@NonNull Context context) {
            this.context = context;
        }

        public boolean isRegistered() {
            return isRegistered;
        }

        public void register(@NonNull Callback callback) {
            if (!isRegistered()) {
                receiver = new Receiver(context, callback);
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                context.registerReceiver(receiver, intentFilter);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    networkCallback = new NetworkCallback(context, callback);
                    ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (connectivity != null) {
                        connectivity.registerDefaultNetworkCallback(networkCallback);
                    }
                }
                isRegistered = true;
            }
        }

        public void unRegister() {
            if (isRegistered()) {
                context.unregisterReceiver(receiver);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (connectivityManager != null) {
                        connectivityManager.unregisterNetworkCallback(networkCallback);
                    }
                }
                isRegistered = false;
            }
        }

        public interface Callback {
            void isConnected();

            void onAvailable(@NonNull android.net.Network network);

            void onLosing(@NonNull android.net.Network network, int maxMsToLive);

            void onLost(@NonNull android.net.Network network);

            void onUnavailable();

            void onCapabilitiesChanged(@NonNull android.net.Network network, @NonNull NetworkCapabilities networkCapabilities, boolean hasCapability);

            void onLinkPropertiesChanged(@NonNull android.net.Network network, @NonNull LinkProperties linkProperties);

            void onBlockedStatusChanged(@NonNull android.net.Network network, boolean blocked);
        }

        public static class SimpleCallback implements Callback {

            @Override
            public void isConnected() {

            }

            @Override
            public void onAvailable(@NonNull android.net.Network network) {

            }

            @Override
            public void onLosing(@NonNull android.net.Network network, int maxMsToLive) {

            }

            @Override
            public void onLost(@NonNull android.net.Network network) {

            }

            @Override
            public void onUnavailable() {

            }

            @Override
            public void onCapabilitiesChanged(@NonNull android.net.Network network, @NonNull NetworkCapabilities networkCapabilities, boolean hasCapability) {

            }

            @Override
            public void onLinkPropertiesChanged(@NonNull android.net.Network network, @NonNull LinkProperties linkProperties) {

            }

            @Override
            public void onBlockedStatusChanged(@NonNull android.net.Network network, boolean blocked) {

            }
        }

        public static class NetworkCallback extends ConnectivityManager.NetworkCallback {
            private final @NonNull Context context;
            private final @NonNull Callback callback;

            public NetworkCallback(@NonNull Context context, @NonNull Callback callback) {
                super();
                this.context = context;
                this.callback = callback;
            }

            @Override
            public void onAvailable(@NonNull android.net.Network network) {
                super.onAvailable(network);
                postOnMainThread(() -> callback.onAvailable(network));
            }

            @Override
            public void onLosing(@NonNull android.net.Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
                postOnMainThread(() -> callback.onLosing(network, maxMsToLive));
            }

            @Override
            public void onLost(@NonNull android.net.Network network) {
                super.onLost(network);
                postOnMainThread(() -> callback.onLost(network));
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                postOnMainThread(callback::onUnavailable);
            }

            @Override
            public void onCapabilitiesChanged(@NonNull android.net.Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                postOnMainThread(() -> callback.onCapabilitiesChanged(network, networkCapabilities, networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)));
            }

            @Override
            public void onLinkPropertiesChanged(@NonNull android.net.Network network, @NonNull LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);
                postOnMainThread(() -> callback.onLinkPropertiesChanged(network, linkProperties));
            }

            @Override
            public void onBlockedStatusChanged(@NonNull android.net.Network network, boolean blocked) {
                super.onBlockedStatusChanged(network, blocked);
                postOnMainThread(() -> callback.onBlockedStatusChanged(network, blocked));
            }
        }

        public static class Receiver extends BroadcastReceiver {
            private final Context context;
            private Callback callback = null;

            public Receiver(Context context, Callback callback) {
                this.context = context;
                this.callback = callback;
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null && callback != null) {
                    boolean isConnected = activeNetworkInfo.isConnected();
                    if (isConnected) {
                        postOnMainThread(() -> callback.isConnected());
                    }
                }
            }
        }
    }
    // ================================================= //
}