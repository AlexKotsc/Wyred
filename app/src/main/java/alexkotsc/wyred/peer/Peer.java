package alexkotsc.wyred.peer;

import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by AlexKotsc on 21-04-2015.
 */
public class Peer implements IPeer {

    private String peerName = null;
    private String publicKey = null;
    private WifiP2pDevice wifiP2pDevice = null;

    public Peer(){

    }

    @Override
    public String getDeviceName() {
        return wifiP2pDevice.deviceName;
    }

    @Override
    public String getDeviceAddress() {
        return wifiP2pDevice.deviceAddress;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public boolean isConnected(){
        if(wifiP2pDevice.status!=WifiP2pDevice.CONNECTED) return false;
        if(publicKey==null) return false;
        return true;
    }

    @Override
    public WifiP2pDevice getWifiP2pDevice() {
        return wifiP2pDevice;
    }

    @Override
    public void setWifiP2pDevice(WifiP2pDevice wifiP2pDevice) {
        this.wifiP2pDevice = wifiP2pDevice;
    }
}
