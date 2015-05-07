package alexkotsc.wyred.peer;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by AlexKotsc on 07-05-2015.
 */
public interface IPeer {
    public String getDeviceName();
    public String getDeviceAddress();
    public String getPeerName();
    public void setPeerName(String peerName);
    public String getPublicKey();
    public void setPublicKey(String publicKey);
    public boolean isConnected();
    public WifiP2pDevice getWifiP2pDevice();
    public void setWifiP2pDevice(WifiP2pDevice wifiP2pDevice);
}
