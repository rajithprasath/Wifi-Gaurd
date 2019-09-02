package sliit.ranjitha.fyp.interfaces;

import android.net.wifi.SupplicantState;


public interface ConnectionResultListener {
    void successfulConnect(String SSID,String BSSID);
    void errorConnect(int codeReason);
    void onStateChange(SupplicantState supplicantState);
}