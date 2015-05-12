package alexkotsc.wyred.peer;

import java.util.HashMap;

/**
 * Created by AlexKotsc on 11-05-2015.
 */
public interface IPeerActivity {
    public void wifiStateChanged(boolean state);

    public void handlePeers(HashMap<String,Peer> peers);

    public String getPeerName();

}
