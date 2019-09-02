package sliit.ranjitha.fyp.interfaces;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import org.json.JSONArray;

import java.util.List;


public interface ShowWifiListener {
    void onNetworksFound(WifiManager wifiManager, List<ScanResult> wifiScanResult);
    void onNetworksFound(JSONArray wifiList);
    void errorSearchingNetworks(int errorCode);
}