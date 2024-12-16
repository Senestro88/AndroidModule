package com.official.senestro.core.callbacks.interfaces;

import android.net.wifi.WifiManager;

public interface XWifiLocalHotspotCallback {
        void onHotspotStarted(WifiManager.LocalOnlyHotspotReservation reservation);

        void onHotspotStopped();

        void onHotspotFailed(int reason);
}