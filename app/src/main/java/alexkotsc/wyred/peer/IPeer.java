package alexkotsc.wyred.peer;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by AlexKotsc on 07-05-2015.
 */
public interface IPeer {
    String getDeviceName();
    String getDeviceAddress();
    String getPeerName();
    void setPeerName(String peerName);
    String getPublicKey();
    void setPublicKey(String publicKey);
    boolean isConnected();
    WifiP2pDevice getWifiP2pDevice();
    void setWifiP2pDevice(WifiP2pDevice wifiP2pDevice);
}
