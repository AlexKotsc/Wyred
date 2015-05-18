package alexkotsc.wyred.peer;

import android.net.wifi.p2p.WifiP2pDevice;

import java.util.HashMap;

/**
 * Created by AlexKotsc on 11-05-2015.
 */
public interface IPeerActivity {
    public void wifiStateChanged(boolean state);

    public void handlePeers(HashMap<String,Peer> peers);

    public String getPeerName();

    public void connectedTo(WifiP2pDevice p);

    void setConnectionManager(ConnectionManager obj);

    void receiveMessage(String readMessage);
}
