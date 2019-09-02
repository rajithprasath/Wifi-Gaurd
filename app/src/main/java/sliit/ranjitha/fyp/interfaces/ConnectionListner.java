package sliit.ranjitha.fyp.interfaces;


import android.net.wifi.SupplicantState;


public interface ConnectionListner {

    void onNetworkConnected(String SSID, String BSSID);
    void onNetworksError(int codeReason);
    void onNetworkStateChange(SupplicantState supplicantState);
}
