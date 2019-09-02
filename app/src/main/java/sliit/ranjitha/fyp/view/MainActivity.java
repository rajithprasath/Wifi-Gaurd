package sliit.ranjitha.fyp.view;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.thanosfisherman.wifiutils.WifiUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import sliit.ranjitha.fyp.R;
import sliit.ranjitha.fyp.database.DatabaseHandler;
import sliit.ranjitha.fyp.interfaces.ConnectionListner;
import sliit.ranjitha.fyp.interfaces.RemoveWifiListener;
import sliit.ranjitha.fyp.interfaces.ShowWifiListener;
import sliit.ranjitha.fyp.interfaces.WifiConnectorModel;
import sliit.ranjitha.fyp.interfaces.WifiStateListener;
import sliit.ranjitha.fyp.model.WifiNetwork;
import sliit.ranjitha.fyp.util.WifiConnector;

public class MainActivity extends Activity implements WifiConnectorModel, ConnectionListner {

    // ui
    private Switch mSwitch;
    private TextView mWifiActiveTxtView;

    private RecyclerView rv;

    private WifiListRvAdapter adapter;
    private WifiConnector wifiConnector;
    private DatabaseHandler db;
    private ScanResult selectedScanResult;
    private int TRUSTED = 1;
    private int BLOCKED = 2;
    private int NOTVERIFIED = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DatabaseHandler(this);
        mSwitch = findViewById(R.id.wifiActivationSwitch);
        mWifiActiveTxtView = findViewById(R.id.wifiActivationTv);
        rv = findViewById(R.id.wifiRv);

        setLocationPermission();
        createWifiConnectorObject();


    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        destroyWifiConnectorListeners();
        super.onDestroy();
    }

    @Override
    public void createWifiConnectorObject() {
        wifiConnector = new WifiConnector(this);
        wifiConnector.setLog(true);
        wifiConnector.setConnectionListener(this);
        wifiConnector.registerWifiStateListener(new WifiStateListener() {
            @Override
            public void onStateChange(int wifiState) {
            }

            @Override
            public void onWifiEnabled() {
                MainActivity.this.onWifiEnabled();
            }

            @Override
            public void onWifiEnabling() {
            }

            @Override
            public void onWifiDisabling() {

            }

            @Override
            public void onWifiDisabled() {

                MainActivity.this.onWifiDisabled();
            }
        });

        if (wifiConnector.isWifiEnbled()) {
            mSwitch.setChecked(true);
            onWifiEnabled();
        } else {
            mSwitch.setChecked(false);
            onWifiDisabled();

        }

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wifiConnector.enableWifi();
                } else {
                    wifiConnector.disableWifi();
                }
            }
        });

        adapter = new WifiListRvAdapter(MainActivity.this, this.wifiConnector, new WifiListRvAdapter.WifiItemListener() {
            @Override
            public void onWifiItemClicked(ScanResult scanResult) {
                if (scanResult.BSSID != null) {
                    if (db.getWifiNetwork(scanResult.BSSID) == null) {
                        createAlertDialog(scanResult, false);
                    } else {
                        WifiNetwork wifiNetwork = db.getWifiNetwork(scanResult.BSSID);
                        if (wifiNetwork.getIsTrusted() == TRUSTED) {
                            createPasswordDialog(scanResult);
                        } else if (wifiNetwork.getIsTrusted() == BLOCKED) {
                            createAlertDialog(scanResult, true);
                        } else {
                            createAlertDialog(scanResult, false);
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Unable to find mac address",
                            Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onWifiItemLongClick(ScanResult scanResult) {
                disconnectFromAccessPoint(scanResult);
            }
        });
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);

    }

    private void onWifiEnabled() {
        mWifiActiveTxtView.setText("Disable Wifi");
        if (permisionLocationOn()) {
            scanForWifiNetworks();
        } else {
            checkLocationTurnOn();
        }
    }

    private void onWifiDisabled() {
        mWifiActiveTxtView.setText("Enable Wifi");
        if (adapter != null)
            adapter.setScanResultList(new ArrayList<ScanResult>());


    }

    @Override
    public void scanForWifiNetworks() {
        wifiConnector.showWifiList(new ShowWifiListener() {
            @Override
            public void onNetworksFound(WifiManager wifiManager, List<ScanResult> wifiScanResult) {
                if (adapter != null)
                    adapter.setScanResultList(wifiScanResult);
            }

            @Override
            public void onNetworksFound(JSONArray wifiList) {
            }

            @Override
            public void errorSearchingNetworks(int errorCode) {
                Toast.makeText(MainActivity.this, "Error on getting wifi list, error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void connectToWifiAccessPoint(final ScanResult scanResult, String password) {
        selectedScanResult = scanResult;
        WifiUtils.withContext(getApplicationContext())
                .connectWith(scanResult.SSID, scanResult.BSSID, password)
                .onConnectionResult(this::checkResult)
                .start();

    }

    private void checkResult(boolean isSuccess) {
        System.out.println("checking network connection 99999999 " + isSuccess);
        if (isSuccess) {
            if (selectedScanResult != null) {
                if (db.getWifiNetwork(selectedScanResult.BSSID) == null) {
                    long time = System.currentTimeMillis();
                    WifiNetwork wifiNetwork = new WifiNetwork();
                    wifiNetwork.setId(String.valueOf(time));
                    wifiNetwork.setSsid(selectedScanResult.SSID);
                    wifiNetwork.setBssid(selectedScanResult.BSSID);
                    wifiNetwork.setIsTrusted(TRUSTED);
                    wifiNetwork.setIsBlocked(0);
                    long insertValue = db.addWifiNetwork(wifiNetwork);
                    if (insertValue != -1) {

                    }
                }
                selectedScanResult = null;
            }

            Toast.makeText(MainActivity.this, "Connected and added to trusted list.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Unable to connect", Toast.LENGTH_SHORT).show();

            selectedScanResult = null;
        }

    }

    @Override
    public void disconnectFromAccessPoint(ScanResult scanResult) {
        this.wifiConnector.removeWifiNetwork(scanResult, new RemoveWifiListener() {
            @Override
            public void onWifiNetworkRemoved() {
                Toast.makeText(MainActivity.this, "You have removed this wifi!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWifiNetworkRemoveError() {
                Toast.makeText(MainActivity.this, "Error on removing this network!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void destroyWifiConnectorListeners() {
        wifiConnector.unregisterWifiStateListener();
    }

    // region permission
    private Boolean permisionLocationOn() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }
    }

    private Boolean checkLocationTurnOn() {
        boolean onLocation = true;
        boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionGranted) {
            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gps_enabled) {
                onLocation = false;
                AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Theme_AppCompat_Dialog));
                //android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
                dialog.setMessage("Please turn on your location");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
                dialog.show();
            }
        }
        return onLocation;
    }

    private void startLocationIntent() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gps_enabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Theme_AppCompat_Dialog));
            //android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
            dialog.setMessage("Please turn on your location");
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                }
            });
            dialog.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        startLocationIntent();
                        onWifiEnabled();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
                        }

                    }
                }
            }
        }
    }


    @Override
    public void onNetworkConnected(String SSID, String BSSID) {
        System.out.println("checking network connection 1111 " + SSID);
        if (db.getWifiNetwork(BSSID) == null) {
            long time = System.currentTimeMillis();
            WifiNetwork wifiNetwork = new WifiNetwork();
            wifiNetwork.setId(String.valueOf(time));
            wifiNetwork.setSsid(SSID);
            wifiNetwork.setBssid(BSSID);
            wifiNetwork.setIsTrusted(TRUSTED);
            wifiNetwork.setIsBlocked(0);
            long insertValue = db.addWifiNetwork(wifiNetwork);
            if (insertValue != -1) {

            }
        }


    }

    @Override
    public void onNetworksError(int codeReason) {

    }

    @Override
    public void onNetworkStateChange(SupplicantState supplicantState) {

    }

    private void createAlertDialog(final ScanResult scanResult, boolean isBlocked) {
        LayoutInflater myLayout = LayoutInflater.from(MainActivity.this);
        final View dialogView = myLayout.inflate(R.layout.connection_alert_dialog, null);
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                MainActivity.this);
        alertDialogBuilder.setView(dialogView);
        final android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();


        TextView messageTextView = (TextView) alertDialog.findViewById(R.id.greeting_text_view);
        if (isBlocked) {
            messageTextView.setText("This network is in the blocked list. Do you wish to connect?");
        }

        TextView okTextView = (TextView) alertDialog.findViewById(R.id.yes_button_text_view);
        TextView cancelTextView = (TextView) alertDialog.findViewById(R.id.cancel_button_text_view);


        okTextView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                alertDialog.dismiss();
                createPasswordDialog(scanResult);

            }
        });
        cancelTextView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                alertDialog.dismiss();
                if (db.getWifiNetwork(scanResult.BSSID) == null) {
                    long time = System.currentTimeMillis();
                    WifiNetwork wifiNetwork = new WifiNetwork();
                    wifiNetwork.setId(String.valueOf(time));
                    wifiNetwork.setSsid(scanResult.SSID);
                    wifiNetwork.setBssid(scanResult.BSSID);
                    wifiNetwork.setIsTrusted(BLOCKED);
                    wifiNetwork.setIsBlocked(1);
                    long insertValue = db.addWifiNetwork(wifiNetwork);
                    if (insertValue != -1) {
                        Toast.makeText(MainActivity.this, "This network added to the blocked list",
                                Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(MainActivity.this, "This network already in the blocked list",
                            Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private void createPasswordDialog(final ScanResult scanResult) {
        LayoutInflater myLayout = LayoutInflater.from(MainActivity.this);
        final View dialogView = myLayout.inflate(R.layout.enter_password_dialog, null);
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                MainActivity.this);
        alertDialogBuilder.setView(dialogView);
        final android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();


        final EditText passwordEdittext = (EditText) alertDialog.findViewById(R.id.input_password);
        TextView proceedTextView = (TextView) alertDialog.findViewById(R.id.proceed_button_text_view);
        TextView cancelTextView = (TextView) alertDialog.findViewById(R.id.cancel_button_text_view);


        proceedTextView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String password = passwordEdittext.getText().toString();
                if (password != null && !password.isEmpty()) {
                    alertDialog.dismiss();
                    connectToWifiAccessPoint(scanResult, password);
                } else {

                    Toast.makeText(MainActivity.this, "Please enter the password to connect",
                            Toast.LENGTH_SHORT).show();
                }


            }
        });
        cancelTextView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                alertDialog.dismiss();


            }
        });
    }

}
