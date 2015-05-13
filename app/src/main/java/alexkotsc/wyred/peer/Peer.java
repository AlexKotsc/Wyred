package alexkotsc.wyred.peer;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by AlexKotsc on 21-04-2015.
 */
public class Peer implements IPeer, Parcelable {

    private String peerName = null;
    private String publicKey = null;
    private WifiP2pDevice wifiP2pDevice = null;

    public Peer(){
        peerName = "testPeer";
        publicKey = "testPublicKey";
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

    protected Peer(Parcel in) {
        peerName = in.readString();
        publicKey = in.readString();
        wifiP2pDevice = (WifiP2pDevice) in.readValue(WifiP2pDevice.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(peerName);
        dest.writeString(publicKey);
        dest.writeValue(wifiP2pDevice);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Peer> CREATOR = new Parcelable.Creator<Peer>() {
        @Override
        public Peer createFromParcel(Parcel in) {
            return new Peer(in);
        }

        @Override
        public Peer[] newArray(int size) {
            return new Peer[size];
        }
    };

    @Override
    public boolean equals(Object o) {

        Peer otherPeer = (Peer) o;

        if(getDeviceName().equals(otherPeer.getDeviceName())){
            return true;
        } else {
            return false;
        }
    }
}
