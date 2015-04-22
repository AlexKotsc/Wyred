package alexkotsc.wyred;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class WifiP2PReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2P wifiP2P;

    public WifiP2PReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiP2P activity){
        super();
        this.mManager = manager;
        this.mChannel = channel;
        wifiP2P = activity;
    }

    public WifiP2PReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            Log.d(wifiP2P.logtag, "State changed!");

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            wifiP2P.setP2PState(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            Log.d(wifiP2P.logtag, "Peers changed!");

            /*if(mManager != null){
                mManager.requestPeers(mChannel, new PeerListener(wifiP2P));
            }*/

            // Call WifiP2pManager.requestPeers() to get a list of current peers
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(wifiP2P.logtag, "Connection changed!");
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(wifiP2P.logtag, "Device changed!");
            // Respond to this device's wifi state changing
        }
    }
}
