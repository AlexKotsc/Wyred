package alexkotsc.wyred.peer;

import android.net.wifi.p2p.WifiP2pDevice;

import java.util.HashMap;

/**
 * Created by AlexKotsc on 11-05-2015.
 */
public interface IPeerActivity {
    void wifiStateChanged(boolean state);

    void handlePeers(HashMap<String, Peer> peers);

    String getPeerName();

    void connectedTo(WifiP2pDevice p);

    void setConnectionManager(ConnectionManager obj);

    void receiveMessage(String readMessage);
}
