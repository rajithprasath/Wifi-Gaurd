package sliit.ranjitha.fyp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import sliit.ranjitha.fyp.util.WifiConnector;

public class ShowWifiListReceiver extends BroadcastReceiver {
    
    private WifiConnector wifiConnector;

    public ShowWifiListReceiver(WifiConnector wifiConnector) {
        this.wifiConnector = wifiConnector;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (this.wifiConnector.getShowWifiListListener() == null) return;

        final JSONArray wifiList = new JSONArray();
        List<ScanResult> wifiScanResult = this.wifiConnector.getWifiManager().getScanResults();
        int scanSize = wifiScanResult.size();

        wifiLog("Showwifireceiver action:  " + intent.getAction());

        try {
            scanSize--;
            wifiLog("Scansize: " + scanSize);
            if (scanSize > 0) {
                this.wifiConnector.getShowWifiListListener().onNetworksFound(this.wifiConnector.getWifiManager(), wifiScanResult);
                while (scanSize >= 0) {

                    if (!wifiScanResult.get(scanSize).SSID.isEmpty()) {
                        /**
                         * individual wifi item information
                         */
                        JSONObject wifiItem = new JSONObject();

                        wifiItem.put("SSID", wifiScanResult.get(scanSize).SSID);
                        wifiItem.put("BSSID", wifiScanResult.get(scanSize).BSSID);
                        wifiItem.put("INFO", wifiScanResult.get(scanSize).capabilities);

                        /**
                         * this check if device has a current WiFi connection
                         */
                        if (wifiScanResult.get(scanSize).BSSID.equals(this.wifiConnector.getWifiManager().getConnectionInfo().getBSSID())) {
                            wifiItem.put("CONNECTED", true);
                            this.wifiConnector.setCurrentWifiSSID(wifiScanResult.get(scanSize).SSID);
                            this.wifiConnector.setCurrentWifiBSSID(wifiScanResult.get(scanSize).BSSID);
                        } else {
                            wifiItem.put("CONNECTED", false);
                        }
                        wifiItem.put("SECURITY_TYPE", WifiConnector.getWifiSecurityType(wifiScanResult.get(scanSize)));
                        wifiItem.put("LEVEL", WifiManager.calculateSignalLevel(wifiScanResult.get(scanSize).level, 100) + "%");

                        wifiList.put(wifiItem);
                    }

                    scanSize--;
                }

                this.wifiConnector.getShowWifiListListener().onNetworksFound(wifiList);

            } else {
                this.wifiConnector.getShowWifiListListener().errorSearchingNetworks(WifiConnector.NO_WIFI_NETWORKS);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            this.wifiConnector.getShowWifiListListener().errorSearchingNetworks(WifiConnector.UNKOWN_ERROR);
        }

        this.wifiConnector.unregisterShowWifiListListener();

    }

    private void wifiLog(String text) {
        if (this.wifiConnector.isLogOrNot()) Log.d(WifiConnector.TAG, "ShowWifiListListener: " + text);
    }

}