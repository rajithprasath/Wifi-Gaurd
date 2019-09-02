/*
 * Created by Jose Flavio on 2/5/18 6:14 PM.
 * Copyright (c) 2017 JoseFlavio.
 * All rights reserved.
 */

package sliit.ranjitha.fyp.interfaces;

import android.net.wifi.ScanResult;

public interface WifiConnectorModel {

    void createWifiConnectorObject();

    void scanForWifiNetworks();

    void connectToWifiAccessPoint(ScanResult scanResult, String password);

    void disconnectFromAccessPoint(ScanResult scanResult);

    void destroyWifiConnectorListeners();

}
