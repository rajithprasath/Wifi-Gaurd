package sliit.ranjitha.fyp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;

import sliit.ranjitha.fyp.util.WifiConnector;


public class WifiConnectionReceiver extends BroadcastReceiver {

    private WifiConnector wifiConnector;

    public WifiConnectionReceiver(WifiConnector wifiConnector) {
        System.out.println("dhdhdhdhdhdhd 5555555555 " );
        this.wifiConnector = wifiConnector;
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        System.out.println("dhdhdhdhdhdhd 66666666 " );
        if (wifiConnector.getConnectionResultListener() == null) return;
        String action = intent.getAction();
        System.out.println("dhdhdhdhdhdhd 777777 " + action);
        if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            System.out.println("dhdhdhdhdhdhd 88888888 " + action);
            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

            wifiLog("Connection state: " + state);
            System.out.println("dhdhdhdhdhdhd 999999999 " + state);
            wifiConnector.getConnectionResultListener().onStateChange(state);

            switch (state) {
                case COMPLETED:
                    System.out.println("dhdhdhdhdhdhd *************** " + wifiConnector.getWifiManager().getConnectionInfo().getBSSID());
                    wifiLog("Connection to Wifi was successfully completed...\n" +
                            "Connected to BSSID: " + wifiConnector.getWifiManager().getConnectionInfo().getBSSID() +
                            " And SSID: " + wifiConnector.getWifiManager().getConnectionInfo().getSSID());
                    if (wifiConnector.getWifiManager().getConnectionInfo().getBSSID() != null) {
                        wifiConnector.setCurrentWifiSSID(wifiConnector.getWifiManager().getConnectionInfo().getSSID());
                        wifiConnector.setCurrentWifiBSSID(wifiConnector.getWifiManager().getConnectionInfo().getBSSID());
                        wifiConnector.getConnectionResultListener().successfulConnect(wifiConnector.getCurrentWifiSSID(),wifiConnector.getWifiManager().getConnectionInfo().getBSSID());
//                        wifiConnector.unregisterWifiConnectionListener();
                    }
                    // if BSSID is null, may be is still triying to get information about the access point
                    break;

                case DISCONNECTED:
                    int supl_error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                    wifiLog("Disconnected... Supplicant error: " + supl_error);

                    // only remove broadcast listener if error was ERROR_AUTHENTICATING
                    if (supl_error == WifiManager.ERROR_AUTHENTICATING) {
                        wifiLog("Authentication error...");
                        if (wifiConnector.deleteWifiConf()) {
                            wifiConnector.getConnectionResultListener().errorConnect(WifiConnector.AUTHENTICATION_ERROR);
                        } else {
                            wifiConnector.getConnectionResultListener().errorConnect(WifiConnector.UNKOWN_ERROR);
                        }
//                        wifiConnector.unregisterWifiConnectionListener();
                    }
                    break;

                case AUTHENTICATING:
                    wifiLog("Authenticating...");
                    break;
            }

        }
    }

    private void wifiLog(String text) {
        if (wifiConnector.isLogOrNot()) Log.d(WifiConnector.TAG, "ConnectionReceiver: " + text);
    }

}