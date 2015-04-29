package alexkotsc.wyred.peer;

import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;

/**
 * Created by AlexKotsc on 21-04-2015.
 */
public class Peer {

    private String peerName;
    private String peerAddress;

    public Peer(){

    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public String getPeerAddress() {
        return peerAddress;
    }

    public void setPeerAddress(String peerAddress) {
        this.peerAddress = peerAddress;
    }

/*    public static WifiP2pConfig getPeerConfig(String peerAddress){
        WifiP2pConfig config = new WifiP2pConfig();

        config.deviceAddress = peerAddress;
        config.wps.setup = WpsInfo.PBC;

        return config;
    }*/

    public WifiP2pConfig getPeerConfig() {

        if(peerAddress != null){
            WifiP2pConfig config = new WifiP2pConfig();

            config.deviceAddress = peerAddress;
            config.wps.setup = WpsInfo.PBC;

            return config;
        }

        return null;
    }
}
