package alexkotsc.wyred.peer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;

import alexkotsc.wyred.WifiP2P;

/**
 * Created by AlexKotsc on 22-04-2015.
 */
public class PeerReceiver extends BroadcastReceiver {

    public PeerReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        Intent i = new Intent(context, WifiPeerService.class);
        Bundle b = new Bundle();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                b.putString("action", WifiPeerService.WifiAction.P2P_ENABLED.toString());
            } else {
                b.putString("action", WifiPeerService.WifiAction.P2P_DISABLED.toString());
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            b.putString("action", WifiPeerService.WifiAction.PEERS_CHANGED.toString());

           /* if(mManager != null){
                mManager.requestPeers(mChannel, new PeerListener(wifiP2P));
            }*/

            // Call WifiP2pManager.requestPeers() to get a list of current peers
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            b.putString("action", WifiPeerService.WifiAction.CONNECTION_CHANGED.toString());
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            b.putString("action", WifiPeerService.WifiAction.DEVICE_CHANGED.toString());
            // Respond to this device's wifi state changing
        }

        i.putExtras(b);
        //Log.d(WifiP2P.logtag, "Starting service with: " + action);
        context.startService(i);
    }
}
