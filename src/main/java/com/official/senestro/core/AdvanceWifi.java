package com.official.senestro.core;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.official.senestro.core.callbacks.interfaces.AdvanceWifiCallback;
import com.official.senestro.core.callbacks.interfaces.AdvanceWifiLocalHotspotCallback;
import com.official.senestro.core.enums.AdvanceWifiApState;
import com.official.senestro.core.enums.AdvanceWifiState;
import com.official.senestro.core.enums.AdvanceWifiNetworkMode;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class AdvanceWifi {
    private final @NonNull Context context;
    private final @NonNull CallBackHandler handler;
    private AdvanceWifiCallback callback;
    private final @NonNull WIFIReceiver receiver;
    protected final @NonNull WifiManager wifi;
    protected final @NonNull ConnectivityManager connectivity;
    @SuppressLint("StaticFieldLeak")
    private static volatile AdvanceWifi instance;
    private WifiManager.LocalOnlyHotspotReservation hotspotStartedReservation;
    private boolean isReceiverRegistered = false;

    public AdvanceWifi(@NonNull Context context) {
        this.context = context;
        this.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.handler = new CallBackHandler(this);
        this.receiver = new WIFIReceiver();
        this.callback = null;
    }

    public static synchronized AdvanceWifi getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (AdvanceWifi.class) {
                if (instance == null) {
                    instance = new AdvanceWifi(context);
                }
            }
        }
        return instance;
    }

    public String getAvailableIPAddress() {
        try {
            // Get all available network interfaces
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress instanceof java.net.Inet4Address) {
                        // Use any available IPv4 address found (excluding loopback and link-local addresses)
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "127.0.0.1"; // Default to loopback address if no suitable IPv4 address is found
    }

    public void openWifi() {
        if (!isWifiEnabled()) {
            this.wifi.setWifiEnabled(true);
        }
    }

    public void closeWifi() {
        if (isWifiEnabled()) {
            this.wifi.setWifiEnabled(false);
        }
    }

    public boolean isWifiEnabled() {
        return this.wifi.isWifiEnabled();
    }

    public void registerWifiCallback(@NonNull AdvanceWifiCallback callback) {
        this.callback = callback;
        if (!isReceiverRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION); // State changed
            intentFilter.addAction(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);  // Scan results available
            intentFilter.addAction(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION); // Network state has changed (connected, disconnected, etc.)
            intentFilter.addAction(android.net.wifi.WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
            intentFilter.addAction(android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION); // Supplicant state changed (authentication etc.)
            intentFilter.addAction(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            intentFilter.addAction(android.net.wifi.WifiManager.RSSI_CHANGED_ACTION); // Signal strength has changed
            context.registerReceiver(receiver, intentFilter);
            isReceiverRegistered = true;
        }
    }

    public void unregisterWifiCallback() {
        callback = null;
        if (isReceiverRegistered) {
            context.unregisterReceiver(receiver);
            isReceiverRegistered = false;
        }
    }

    @RequiresApi(value = Build.VERSION_CODES.Q)
    public void openWifiSettings() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setAction(Settings.ACTION_WIFI_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.context.startActivity(intent);
    }

    public void openTetherSettings() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.context.startActivity(intent);
    }

    public void openWirelessSettings() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.context.startActivity(intent);
    }

    @RequiresApi(value = Build.VERSION_CODES.M)
    public boolean canWriteSettings() {
        return Settings.System.canWrite(this.context);
    }

    @RequiresApi(value = Build.VERSION_CODES.M)
    public void openWriteSettings(boolean force) {
        if (force || !canWriteSettings()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + this.context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.context.startActivity(intent);
        }
    }

    public void startWifiScan() {
        this.wifi.startScan();
    }

    public List<ScanResult> getWifiScanResults() {
        return this.wifi.getScanResults();
    }

    public Object getActiveNetworkInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return getActiveNetworkInfoForAPI30AndAbove();
        } else {
            return getActiveNetworkInfoForAPI29AndBelow();
        }
    }

    @RequiresApi(value = Build.VERSION_CODES.N)
    public void registerNetworkCallback(@NonNull ConnectivityManager.NetworkCallback networkCallback) {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        NetworkRequest networkRequest = builder.build();
        this.connectivity.registerNetworkCallback(networkRequest, networkCallback);
    }

    public void unregisterNetworkCallback(@NonNull ConnectivityManager.NetworkCallback networkCallback) {
        this.connectivity.unregisterNetworkCallback(networkCallback);
    }

    public WifiInfo getConnectionInfo() {
        return this.wifi.getConnectionInfo();
    }

    public List<WifiConfiguration> getConfiguredNetworks() {
        return this.wifi.getConfiguredNetworks();
    }

    public boolean disableWifi(int networkId) {
        return this.wifi.disableNetwork(networkId);
    }

    public boolean disconnectWifi(int networkId) {
        WifiInfo info = getConnectionInfo();
        if (info != null) {
            if (info.getNetworkId() == networkId) {
                return this.wifi.disconnect();
            }
        }
        return false;
    }

    public boolean disconnectConnectedWifi() {
        WifiInfo info = getConnectionInfo();
        if (info != null) {
            return this.wifi.disconnect();
        }
        return false;
    }

    public boolean disableConnectedWifi() {
        WifiInfo info = getConnectionInfo();
        if (info != null) {
            return this.wifi.disableNetwork(info.getNetworkId());
        }
        return false;
    }

    public WifiConfiguration newNetworkConfiguration(@NonNull String ssid, @NonNull String password, @NonNull AdvanceWifiNetworkMode networkMode) {
        WifiConfiguration configuration = new WifiConfiguration();
        if (networkMode == AdvanceWifiNetworkMode.WEP) {
            configuration = newWEPNetworkConfiguration(ssid, password);
        } else if (networkMode == AdvanceWifiNetworkMode.WPA) {
            configuration = newWPANetworkConfiguration(ssid, password);
        } else if (networkMode == AdvanceWifiNetworkMode.WPA2) {
            configuration = newWPA2NetworkConfiguration(ssid, password);
        } else if (networkMode == AdvanceWifiNetworkMode.OPEN) {
            configuration = newOPENNetworkConfiguration(ssid);
        }
        return configuration;
    }

    public void newNetwork(@NonNull String ssid, @NonNull String password, @NonNull AdvanceWifiNetworkMode networkMode, boolean attemptConnect) {
        WifiConfiguration configuration = newNetworkConfiguration(ssid, password, networkMode);
        int networkId = this.wifi.addNetwork(configuration);
        if (networkId != -1) {
            this.wifi.enableNetwork(networkId, attemptConnect);
        }
    }

    public WifiConfiguration newOPENNetworkConfiguration(@NonNull String ssid) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = addQuotation(ssid);
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        return configuration;
    }

    public void newOPENNetwork(@NonNull String ssid, boolean attemptConnect) {
        WifiConfiguration configuration = newOPENNetworkConfiguration(ssid);
        int networkId = this.wifi.addNetwork(configuration);
        if (networkId != -1) {
            this.wifi.enableNetwork(networkId, attemptConnect);
        }
    }

    public WifiConfiguration newWEPNetworkConfiguration(@NonNull String ssid, @NonNull String password) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = addQuotation(ssid);
        configuration.wepKeys[0] = addQuotation(password);
        configuration.wepTxKeyIndex = 0;
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        return configuration;
    }

    public void newWEPNetwork(@NonNull String ssid, @NonNull String password, boolean attemptConnect) {
        WifiConfiguration configuration = newWEPNetworkConfiguration(ssid, password);
        int networkId = this.wifi.addNetwork(configuration);
        if (networkId != -1) {
            this.wifi.enableNetwork(networkId, attemptConnect);
        }
    }

    public WifiConfiguration newWPANetworkConfiguration(@NonNull String ssid, @NonNull String password) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = addQuotation(ssid);
        configuration.preSharedKey = addQuotation(password);
        configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        return configuration;
    }

    public void newWPANetwork(@NonNull String ssid, @NonNull String password, boolean attemptConnect) {
        WifiConfiguration configuration = newWPANetworkConfiguration(ssid, password);
        int networkId = this.wifi.addNetwork(configuration);
        if (networkId != -1) {
            this.wifi.enableNetwork(networkId, attemptConnect);
        }
    }

    public WifiConfiguration newWPA2NetworkConfiguration(@NonNull String ssid, @NonNull String password) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = "\"" + ssid + "\"";
        configuration.preSharedKey = "\"" + password + "\"";
        configuration.allowedKeyManagement.clear();
        configuration.allowedPairwiseCiphers.clear();
        configuration.allowedAuthAlgorithms.clear();
        configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        return configuration;
    }

    public void newWPA2Network(@NonNull String ssid, @NonNull String password, boolean attemptConnect) {
        WifiConfiguration configuration = newWPA2NetworkConfiguration(ssid, password);
        int networkId = this.wifi.addNetwork(configuration);
        if (networkId != -1) {
            this.wifi.enableNetwork(networkId, attemptConnect);
        }
    }

    public int addNetwork(@NonNull WifiConfiguration configuration) {
        int networkId = this.wifi.addNetwork(configuration);
        if (-1 != networkId && saveConfiguration()) {
            return networkId;
        }
        return -1;
    }

    public boolean enableNetwork(int networkId, boolean attemptConnect) {
        return this.wifi.enableNetwork(networkId, attemptConnect);
    }

    public int updateNetwork(@NonNull WifiConfiguration configuration) {
        int networkId = this.wifi.updateNetwork(configuration);
        if (-1 != networkId && saveConfiguration()) {
            return networkId;
        }
        return -1;
    }

    public boolean deleteNetwork(int networkId) {
        return this.wifi.disableNetwork(networkId) && this.wifi.removeNetwork(networkId) && this.wifi.saveConfiguration();
    }

    public boolean saveConfiguration() {
        return this.wifi.saveConfiguration();
    }

    public boolean reconnect() {
        return this.wifi.reconnect();
    }

    public WifiConfiguration getConfiguredNetworkBySSID(@NonNull String ssid) {
        List<WifiConfiguration> configuredNetworks = getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration config : configuredNetworks) {
                if (config.SSID != null && config.SSID.equals(addQuotation(ssid))) {
                    return config;
                }
            }
        }
        // No matching network found
        return null;
    }

    public WifiConfiguration updateNetwork(WifiConfiguration configuration, @NonNull String password, AdvanceWifiNetworkMode networkMode) {
        if (networkMode == AdvanceWifiNetworkMode.WEP) {
            configuration.wepKeys[0] = addQuotation(password);
        } else if (networkMode == AdvanceWifiNetworkMode.WPA || networkMode == AdvanceWifiNetworkMode.WPA2) {
            configuration.preSharedKey = addQuotation(password);
        }
        return configuration;
    }

    public int updateNetwork(@NonNull String ssid, @NonNull String password, AdvanceWifiNetworkMode networkMode) {
        if (TextUtils.isEmpty(ssid)) {
            return -1;
        } else {
            WifiConfiguration configuration = getConfiguredNetworkBySSID(ssid);
            if (null == configuration) {
                return addNetwork(newNetworkConfiguration(ssid, password, networkMode));
            } else {
                return updateNetwork(updateNetwork(configuration, password, networkMode));
            }
        }
    }

    public int calculateSignalLevel(int rssi, int numLevels) {
        return WifiManager.calculateSignalLevel(rssi, numLevels);
    }

    public AdvanceWifiNetworkMode getNetworkModeEnum(@NonNull ScanResult wifiScanResult) {
        String capabilities = wifiScanResult.capabilities;
        if (capabilities.contains("WPA")) {
            return AdvanceWifiNetworkMode.WPA;
        } else if (capabilities.contains("WPA2")) {
            return AdvanceWifiNetworkMode.WPA2;
        } else if (capabilities.contains("WEP")) {
            return AdvanceWifiNetworkMode.WEP;
        } else {
            return AdvanceWifiNetworkMode.OPEN;
        }
    }

    // WIfi Ap
    public AdvanceWifiApState getApState() {
        try {
            Method method = this.wifi.getClass().getMethod("getWifiApState");
            int tmp = ((Integer) Objects.requireNonNull(method.invoke(this.wifi)));
            // Fix for Android 4
            tmp = tmp >= 10 ? tmp - 10 : tmp;
            return Objects.requireNonNull(AdvanceWifiApState.class.getEnumConstants())[tmp];
        } catch (Exception ignored) {
        }
        return AdvanceWifiApState.AP_STATE_FAILED;
    }

    public boolean isApEnabled() {
        return getApState() == AdvanceWifiApState.AP_STATE_ENABLED;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startLocalOnlyHotspot(@NonNull AdvanceWifiLocalHotspotCallback callback) {
        closeWifi();
        this.wifi.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {
            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                hotspotStartedReservation = reservation;
                callback.onHotspotStarted(reservation);
            }

            @Override
            public void onStopped() {
                super.onStopped();
                callback.onHotspotStopped();
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                callback.onHotspotFailed(reason);
            }
        }, new Handler(Looper.getMainLooper()));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void stopLocalOnlyHotspot() {
        if (hotspotStartedReservation != null) {
            hotspotStartedReservation.close();
        }
    }

    /* *******************************************************************************************/
    public static ArrayList<ScanResult> getScanResultsExcludeRepetition(@NonNull List<ScanResult> scanResults) {
        HashMap<String, ScanResult> hashMap = new HashMap<>();
        for (ScanResult scanResult : scanResults) {
            String ssid = scanResult.SSID;
            if (TextUtils.isEmpty(ssid)) {
                continue;
            }
            ScanResult tempResult = hashMap.get(ssid);
            if (null == tempResult) {
                hashMap.put(ssid, scanResult);
                continue;
            }
            if (WifiManager.calculateSignalLevel(tempResult.level, 100) < WifiManager.calculateSignalLevel(scanResult.level, 100)) {
                hashMap.put(ssid, scanResult);
            }
        }
        ArrayList<ScanResult> results = new ArrayList<>();
        for (Map.Entry<String, ScanResult> entry : hashMap.entrySet()) {
            results.add(entry.getValue());
        }
        return results;
    }

    /* *******************************************************************************************/
    private String addQuotation(@NonNull String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        } else {
            return "\"" + text + "\"";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private Object getActiveNetworkInfoForAPI30AndAbove() {
        Network network = connectivity.getActiveNetwork();
        NetworkCapabilities capabilities = connectivity.getNetworkCapabilities(network);
        LinkProperties properties = connectivity.getLinkProperties(network);
        if (capabilities != null && properties != null) {
            HashMap<String, String> data = new HashMap<>();
            data.put("NETWORK_TYPE", capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ? "WIFI" : (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ? "CELLULAR" : "Undefined"));
            data.put("OWNER_UID", String.valueOf(capabilities.getOwnerUid()));
            data.put("NETWORK_SPECIFIER", String.valueOf(capabilities.getNetworkSpecifier()));
            data.put("TRANSPORT_INFO", String.valueOf(capabilities.getTransportInfo()));
            data.put("DOWNSTREAM_BANDWIDTH (Kbps)", String.valueOf(capabilities.getLinkDownstreamBandwidthKbps()));
            data.put("UPSTREAM_BANDWIDTH (Kbps)", String.valueOf(capabilities.getLinkUpstreamBandwidthKbps()));
            data.put("SIGNAL_STRENGTH", String.valueOf(capabilities.getSignalStrength()));
            data.put("INTERFACE_NAME", String.valueOf(properties.getInterfaceName()));
            data.put("DHCP_SERVER", String.valueOf(properties.getDhcpServerAddress()));
            data.put("DOMAINS", String.valueOf(properties.getDomains()));
            data.put("HTTP_PROXY", String.valueOf(properties.getHttpProxy()));
            data.put("DNS_SERVERS", String.valueOf(properties.getDnsServers()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data.put("ENTERPRISE_IDS", Arrays.toString(capabilities.getEnterpriseIds()));
            }
            return data;
        }
        return null;
    }

    private Object getActiveNetworkInfoForAPI29AndBelow() {
        NetworkInfo network = connectivity.getActiveNetworkInfo();
        if (network != null && network.isConnected()) {
            int getType = network.getType();
            HashMap<String, String> data = new HashMap<>();
            data.put("NETWORK_TYPE", getType == ConnectivityManager.TYPE_WIFI ? "WIFI" : (getType == ConnectivityManager.TYPE_MOBILE ? "MOBILE" : "Undefined"));
            data.put("EXTRA_INFO", network.getExtraInfo());
            data.put("DETAILED_STATE", String.valueOf(network.getDetailedState()));
            data.put("SUB_TYPE", String.valueOf(network.getSubtype()));
            data.put("SUB_TYPE_NAME", String.valueOf(network.getSubtypeName()));
            data.put("STATE", String.valueOf(network.getState()));
            data.put("TYPE_NAME", String.valueOf(network.getTypeName()));
            data.put("REASON", String.valueOf(network.getReason()));
            return data;
        }
        return null;
    }

    /* *******************************************************************************************/
    public class WIFIReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (manager != null && intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION:
                            handleWiFiStateChanged(intent);
                            break;
                        case android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                            handleWiFiScanResultsAvailable(intent, manager);
                            break;
                        case android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION:
                            handleNetworkStateChanged(intent);
                            break;
                        case WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION:
                            boolean isConnected = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
                            handleSupplicantConnectionChanged(intent, manager, isConnected);
                            break;
                        case android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                            handleSupplicantStateChanged(intent, manager);
                            break;
                        case android.net.wifi.WifiManager.RSSI_CHANGED_ACTION:
                            int newRssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, 0);
                            handleRssiChanged(intent, newRssi);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    /* *******************************************************************************************/
    private void handleWiFiStateChanged(@NonNull Intent intent) {
        int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                handler.sendMessage(AdvanceWifiState.WIFI_LOG, "Enabling WiFi...");
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                handler.sendMessage(AdvanceWifiState.WIFI_STATE_ENABLED, null);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                handler.sendMessage(AdvanceWifiState.WIFI_LOG, "Disabling WiFi...");
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                handler.sendMessage(AdvanceWifiState.WIFI_STATE_DISABLED, null);
                break;
            default:
                handler.sendMessage(AdvanceWifiState.WIFI_LOG, "Unknown WiFi");
                break;
        }
    }

    private void handleWiFiScanResultsAvailable(@NonNull Intent intent, @NonNull android.net.wifi.WifiManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean resultsUpdated = intent.getBooleanExtra(android.net.wifi.WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (resultsUpdated) {
                handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi scan results updated");
            }
        }
        handler.sendMessage(AdvanceWifiState.WIFI_SCAN_RESULTS_UPDATED, manager.getScanResults());
    }

    private void handleNetworkStateChanged(@NonNull Intent intent) {
        NetworkInfo networkInfo = intent.getParcelableExtra(android.net.wifi.WifiManager.EXTRA_WIFI_INFO);
        if (networkInfo != null) {
            NetworkInfo.State state = networkInfo.getState();
            if (state == NetworkInfo.State.CONNECTING) {
                handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi is connecting");
            } else if (state == NetworkInfo.State.CONNECTED) {
                handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi has been connected");
            } else if (state == NetworkInfo.State.DISCONNECTED) {
                handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi disconnected");
            } else if (state == NetworkInfo.State.SUSPENDED) {
                handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi suspended");
            } else if (state == NetworkInfo.State.UNKNOWN) {
                handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi connection state is unknown");
            }
        }
    }

    private void handleSupplicantConnectionChanged(@NonNull Intent intent, @NonNull android.net.wifi.WifiManager manager, boolean isConnected) {
        if (isConnected) {
            WifiInfo info = manager.getConnectionInfo();
            String ssid = info.getSSID();
            handler.sendMessage(AdvanceWifiState.WIFI_CONNECT_LOG, "WiFi is connected (" + ssid + ")");
        } else {
            handler.sendMessage(AdvanceWifiState.WIFI_CONNECT_LOG, "WiFi isn't connected");
        }
    }

    private void handleSupplicantStateChanged(@NonNull Intent intent, android.net.wifi.WifiManager manager) {
        SupplicantState supplicantState = intent.getParcelableExtra(android.net.wifi.WifiManager.EXTRA_NEW_STATE);
        if (supplicantState != null) {
            switch (supplicantState) {
                case INTERFACE_DISABLED:
                    handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi interface is disabled");
                    break;
                case DISCONNECTED:
                    handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi disconnected");
                    break;
                case INACTIVE:
                    handleSupplicantInactiveState(manager);
                    break;
                case SCANNING:
                    handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi scanning...");
                    break;
                case AUTHENTICATING:
                    handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi authenticating...");
                    break;
                case ASSOCIATING:
                    handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi associating...");
                    break;
                case ASSOCIATED:
                    handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi associated");
                    break;
                case FOUR_WAY_HANDSHAKE:
                    handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi four way handshake");
                    break;
                case GROUP_HANDSHAKE:
                    handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi group handshake");
                    break;
                case COMPLETED:
                    handleSupplicantCompletedState(manager);
                    break;
                case DORMANT:
                    handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi dormant");
                    break;
                case UNINITIALIZED:
                    handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi uninitialized");
                    break;
                case INVALID:
                    handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi invalid");
                    break;
                default:
                    break;
            }
        }
    }

    private void handleRssiChanged(@NonNull Intent intent, int newRssi) {
        handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi signal has changed: " + newRssi);
    }

    private void handleSupplicantInactiveState(android.net.wifi.WifiManager manager) {
        WifiInfo info = manager.getConnectionInfo();
        handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi inactive: ConnectFailureInfo = " + info);
        if (info != null) {
            handler.sendMessage(AdvanceWifiState.WIFI_CONNECT_FAILURE, info.getSSID());
        }
    }

    private void handleSupplicantCompletedState(android.net.wifi.WifiManager manager) {
        handler.sendMessage(AdvanceWifiState.WIFI_LOG, "WiFi connect success");
        WifiInfo info = manager.getConnectionInfo();
        if (info != null) {
            handler.sendMessage(AdvanceWifiState.WIFI_CONNECT_SUCCESS, info.getSSID());
        }
    }

    private static void postOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private static class CallBackHandler {
        private final @NonNull AdvanceWifi instance;

        public CallBackHandler(@NonNull AdvanceWifi instance) {
            this.instance = instance;
        }

        public void sendMessage(@NonNull AdvanceWifiState which, @Nullable Object data) {
            if (instance.callback != null) {
                switch (which) {
                    case WIFI_STATE_ENABLED:
                        postOnMainThread(() -> instance.callback.onWifiEnabled(true));
                        break;
                    case WIFI_STATE_DISABLED:
                        postOnMainThread(() -> instance.callback.onWifiEnabled(false));
                        break;
                    case WIFI_SCAN_RESULTS_UPDATED:
                        @SuppressWarnings("unchecked") List<ScanResult> results = (List<ScanResult>) data;
                        postOnMainThread(() -> instance.callback.onWifiScanResults(results));
                        break;
                    case WIFI_LOG:
                        postOnMainThread(() -> instance.callback.onWifiLog((String) data));
                        break;
                    case WIFI_CONNECT_LOG:
                        postOnMainThread(() -> instance.callback.onWifiConnectionLog((String) data));
                        break;
                    case WIFI_CONNECT_SUCCESS:
                        postOnMainThread(() -> instance.callback.onWifiConnected((String) data));
                        break;
                    case WIFI_CONNECT_FAILURE:
                        postOnMainThread(() -> instance.callback.onWifiConnectionFailure((String) data));
                        break;
                    default:
                        break;
                }
            }
        }
    }
}