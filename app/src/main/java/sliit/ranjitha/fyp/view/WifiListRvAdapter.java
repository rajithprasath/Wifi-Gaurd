package sliit.ranjitha.fyp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sliit.ranjitha.fyp.R;
import sliit.ranjitha.fyp.database.DatabaseHandler;
import sliit.ranjitha.fyp.util.WifiConnector;

public class WifiListRvAdapter extends RecyclerView.Adapter<WifiListRvAdapter.WifiItem> {

    private List<ScanResult> scanResultList = new ArrayList<>();
    private WifiConnector wifiConnector;
    private WifiItemListener wifiItemListener;
    private DatabaseHandler db;
    private Context mContext;
    public WifiListRvAdapter(Context context,WifiConnector wifiConnector, WifiItemListener wifiItemListener) {
        this.wifiConnector = wifiConnector;
        this.wifiItemListener = wifiItemListener;
        this.mContext = context;
        this.db = new DatabaseHandler(mContext);
    }

    public void setScanResultList(List<ScanResult> scanResultList) {
        this.scanResultList = scanResultList;
        notifyDataSetChanged();
    }

    @Override
    public WifiItem onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WifiItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.accesspoint_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final WifiItem holder, final int position) {
        holder.fill(scanResultList.get(position), wifiConnector.getCurrentWifiSSID());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiConnector.isConnectedToBSSID(scanResultList.get(position).BSSID)) {
                    Toast.makeText(holder.itemView.getContext(), "Already connected!", Toast.LENGTH_SHORT).show();
                } else {
                    wifiItemListener.onWifiItemClicked(scanResultList.get(position));
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                wifiItemListener.onWifiItemLongClick(scanResultList.get(position));
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.scanResultList.size();
    }

    @Override
    public void onViewRecycled(WifiItem holder) {
        super.onViewRecycled(holder);
        holder.wifiName.setTextColor(Color.BLACK);
    }

     class WifiItem extends RecyclerView.ViewHolder {

        private TextView wifiName;
        private TextView wifiIntensity;
        private TextView bssidTextview;

        public WifiItem(View itemView) {
            super(itemView);
            wifiName = itemView.findViewById(R.id.apItem_name);
            wifiIntensity = itemView.findViewById(R.id.apItem_intensity);
            bssidTextview = itemView.findViewById(R.id.bssid_textview);
        }

        @SuppressLint("SetTextI18n")
        public void fill(ScanResult scanResult, String currentSsid) {
            if (scanResult.SSID.equals(currentSsid)) {
                wifiName.setTextColor(Color.GREEN);
            }else if(db.getWifiNetwork(scanResult.BSSID)==null){

            }else if(db.getWifiNetwork(scanResult.BSSID)!=null){
               if( db.getWifiNetwork(scanResult.BSSID).getIsTrusted()==2){
                   wifiName.setTextColor(Color.RED);
               }
            }
            wifiName.setText(scanResult.SSID);
            bssidTextview.setText("MAC address:- "+ scanResult.BSSID);
            wifiIntensity.setText(WifiManager.calculateSignalLevel(scanResult.level, 100) + "%");
        }

    }

    interface WifiItemListener {
        void onWifiItemClicked(ScanResult scanResult);

        void onWifiItemLongClick(ScanResult scanResult);
    }

}
