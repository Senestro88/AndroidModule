package com.official.senestro.core.callbacks.interfaces;

import android.net.wifi.ScanResult;

import java.util.List;

public interface AdvanceWifiEventsCallback {
    void onWifiEnabled(boolean isEnabled);

    void onWifiScanResults(List<ScanResult> results);

    void onWifiConnected(String SSID);

    void onWifiConnectionFailure(String SSID);

    void onWifiLog(String log);

    void onWifiConnectionLog(String log);
}