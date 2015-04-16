package alexkotsc.wyred;

import android.content.BroadcastReceiver;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by AlexKotsc on 15-04-2015.
 */
public class PeerListener implements WifiP2pManager.PeerListListener {

    private WifiP2P activity;

    public PeerListener(WifiP2P activity){
        Log.d(WifiP2P.logtag, "Peerlistener created");
        this.activity = activity;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        Log.d(WifiP2P.logtag, "Listener received peers");
        activity.receivePeers(peers);
    }
}
